/*
DRM, distributed resource machine supporting special distributed applications
Copyright (C) 2002 The European Commission DREAM Project IST-1999-12679

This file is part of DRM.

DRM is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

DRM is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DRM; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

contact: http://www.dr-ea-m.org, http://www.sourceforge.net/projects/dr-ea-m
*/


package drm.core;

import drm.agentbase.*;
import java.util.*;
import java.io.*;


/**
* This class implements a proxy to a collective.
* An entity can use this class either to observe what is
* going on (ie reading) or to participate in the collective
* effort, ie also to contribute to the common effort or issue commands.
* In the first case the entity is an Observer, in the second
* case a Contributor, in the third a Controller. These roles can be
* implemented by the same entity or by different entities.
* <p> To join a collective it is not enough to construct a collective
* object with eg
* <pre>
* Collective c = new Collective("mycollective",this);
* </pre>
*  The collective member also has to forward messages of type
* <code>"collectiveUpdate-mycollective"</code> by calling
* {@link #handleMessage(Message,Object)}.
* @see Observer
* @see Contributor
* @see Controller
*/
public final class Collective implements Runnable, Serializable {
static final long serialVersionUID = 1L;


// ============== private fields ==============================
// ============================================================


/**
* Stores known contributions.
* Element type is ContributionBox, key is the name from the address
* field of ContributionBox. This cache never contains an entry from the
* local contributor (if there is one).
*/
private transient Map cache = new Hashtable();

/**
* Contains contribution of the local contributor (if there is one).
*/
private transient ContributionBox myContribution = null;

/**
* This is either null or contains the actual values in cache.
* Modified by refresh(), handleMessage(), and
* getContributions().
*/
private transient List cacheCollection = null;

/**
* Stores active commands. These commands have to be executed by members of
* the collective.
*/
private transient Map commands = new Hashtable();

/**
* This is either null or contains the actual keys in commands.
* Modified by refresh(), handleMessage(), and
* getCommands().
*/
private transient List commandCollection = null;

/**
* The observer we serve.
*/
private transient Observer observer;

/**
* The contributor we serve.
* Its contributions are propagated to the virtual contribution repository.
*/
private transient Contributor contributor;

/**
* The controller we serve.
* This entity can add commands to the command database of the collective,
* which are spread and executed trhoughout the collective.
*/
private transient Controller controller;

/** if false the thred exits */
private volatile transient boolean shouldLive = true;

/** The name of the collective. */
private transient String name;


// ============== private fields ==============================
// ============================================================


/**
* An information exchange attempt is initiated after waiting
* this many ms-s.
*/
public static final long REFRESHRATE = 10000;

/** Max size of cache. Reading at most this many random contributions is cheap
* as they are stored locally. */
public static final long MAX_CACHE_SIZE = 5;

/** Max size of active commands database. */
public static final long MAX_COMMANDS_SIZE = 100;

/** Commands older than this time are removed. */
public static final long COMMAND_TIMEOUT = 60000;

/** Contributions older than this many milliseconds are thrown away. */
/* The formula for being sure with a probability p that we have 
 * initiated communication with all the c peers in our cache in t 
 * iterations is: (1-((c-1)/c)**t)**c > p 
 * Which, for p = 0.95, is surprinsingly near of 5*c iterations.*/
public static final long CONTRIBUTION_TIMEOUT = 5 * MAX_CACHE_SIZE * REFRESHRATE;


// ============== private methods ===========================
// ==========================================================


private void writeObject( ObjectOutputStream out ) throws IOException {
synchronized(cache) { synchronized(commands)
{
	out.writeObject(name);
	
	// cache and commands are never null
	out.writeLong(System.currentTimeMillis());
	out.writeObject(myContribution);
	out.writeInt(cache.size());
	Iterator i = cache.values().iterator();
	while( i.hasNext() ) out.writeObject(i.next());
	
	out.writeInt(commands.size());
	i = commands.entrySet().iterator();
	while(i.hasNext())
	{
		Map.Entry e = (Map.Entry)i.next();
		long time = ((Long)e.getValue()).longValue();
		out.writeObject(e.getKey());
		out.writeLong(time);
	}
}}
}

// ---------------------------------------------------------------

/**
* Not only deserialises the object, also performs adjustments on the
* timestamps to bring them in alignment with the local time.
* Reads the current time on the peer node first.
* If the timestamp of any items in the cache or commands are newer
* throws and IOException, because that means the object is corrupted.
* Sets the timestamp of the contribution of the peer (if any) to the current
* local time.
*/
private void readObject( ObjectInputStream in )
throws IOException, ClassNotFoundException {

	name = (String)in.readObject();
	
	cache = new Hashtable();
	commands = new Hashtable();
	
	final long max = in.readLong(); // the timestamp that should be maximal
	final long diff = System.currentTimeMillis()-max;
	myContribution = (ContributionBox)in.readObject();
	if( myContribution != null ) myContribution.timeStamp = max+diff;
	
	int cachesize = in.readInt();
	for(int i=0; i<cachesize; ++i)
	{
		ContributionBox cb = (ContributionBox)in.readObject();
		if( cb.timeStamp > max ) throw new IOException(
			"corrupted timestamp in cache: "+cb.timeStamp+" "+max);
		cb.timeStamp += diff;
		cache.put( cb.contributor.name, cb );
	}

	int commandssize = in.readInt();
	for(int i=0; i<commandssize; ++i)
	{
		Object comm = in.readObject();
		long time = in.readLong();
		if( time > max ) throw new IOException(
			"corrupted timestamp in commands: "+time+" "+max);
		time += diff;
		commands.put( comm, new Long(time) );
	}
}
 
// ---------------------------------------------------------------

/**
* Merges the given peer info with the
* local info. A newer contribution of the same contributor replaces the
* older one. Otherwise no elements are removed by this method, ie the
* cache and commands are not truncated.
*/
private synchronized void merge( Collective peer ) {

	synchronized(cache)
	{
		Iterator i = peer.cache.values().iterator();
		while( i.hasNext() )
		{
			ContributionBox cb  = (ContributionBox)i.next();
			ContributionBox x = (ContributionBox)cache.get(
					cb.contributor.name);
			if( x == null || x.timeStamp < cb.timeStamp )
			{
				cache.put( cb.contributor.name, cb );
			}
		}
	}
	synchronized(commands)
	{
		Iterator i = peer.commands.entrySet().iterator();
		while(i.hasNext())
		{
			Map.Entry e = (Map.Entry)i.next();
			Comparable time = (Comparable)commands.get(e.getKey());
			if( time==null || time.compareTo(e.getValue())<0)
			{
				commands.put(e.getKey(),e.getValue());
			}
		}
	}
	cutToSize();
}

// ---------------------------------------------------------------

/**
* Removes contributions and commands that are older than
* CONTRIBUTION_TIMEOUT and
* COMMAND_TIMEOUT respectively.
*/
private void removeOldStuff() {

	final long current = System.currentTimeMillis();
	synchronized( cache )
	{
		Iterator i = cache.values().iterator();
		while( i.hasNext() )
		{
			ContributionBox cb = (ContributionBox)i.next();
			if( current-cb.timeStamp > CONTRIBUTION_TIMEOUT )
				i.remove();
		}
	}
	synchronized( commands )
	{
		Iterator i = commands.values().iterator();
		while( i.hasNext() )
		{
			Long t = (Long)i.next();
			if( current-t.longValue()>COMMAND_TIMEOUT ) i.remove();
		}
	}
}

// ---------------------------------------------------------------

/**
* Removes randomly chosen entries until from the cache and commands until
* the size is not larger than MAX_CACHE_SIZE and MAX_COMMANDS_SIZE respectively.
*/
private void cutToSize() {

	if( MAX_CACHE_SIZE < cache.size() )
	synchronized( cache )
	{
		Vector keyList = new Vector(cache.keySet());
		Collections.shuffle(keyList);
		while( MAX_CACHE_SIZE < cache.size() )
		{
			cache.remove( keyList.get(0) );
			keyList.remove(0);
		}
	}
	if( MAX_COMMANDS_SIZE < commands.size() )
	synchronized( commands )
	{
		Vector keyList = new Vector(commands.keySet());
		Collections.shuffle(keyList);
		while( MAX_COMMANDS_SIZE < commands.size() )
		{
			commands.remove( keyList.get(0) );
			keyList.remove(0);
		}
	}
}

// -------------------------------------------------------------------

/**
* Updates local contribution and commands. Called before information exchange
* attempts. Uses local address of contributor (if it is not null), this is
* replaced on the other side of the communication channel by the correct global
* address.
*/
private synchronized void updateLocalInfo() {
	
	if( contributor != null )
	{
		myContribution = new ContributionBox(
			new Address(contributor.getName()),
			contributor.getContribution() );
	}

	final Long now = new Long(System.currentTimeMillis());
	if( controller != null )
	{
		Set newcomms = controller.getCommands();
		if( newcomms == null ) return;
		Iterator i = newcomms.iterator();
		while( i.hasNext() ) commands.put( i.next(), now );
	}
}

// -------------------------------------------------------------------

/**
* Repairing address of sender (might contain only name).
* This sets the right address of myContribution in c, and puts the
* contribution into c.cache. sender is the address of the peer to our best
* knowledge. However the atual address of the contributor might be different
* due to forwarding and other nuances. The authoritive name is in
* c.myContribution. The authoritive host and port are in sender.
*/
private static void repairSenderAddress( Collective c, Address sender ) {

	if( c.myContribution != null )
	{
		c.cache.put(
			c.myContribution.contributor.name,
			new ContributionBox( 
				new Address( 
					sender.getHost(),
					sender.port,
					c.myContribution.contributor.name
				),
				c.myContribution.timeStamp,
				c.myContribution.contribution
			)
		);
	}
	else
		Logger.warning( "Collective#repairSenderAddress()",
		"Update doesn't contain contribution of sender "+sender,null);
}

// -------------------------------------------------------------------

/**
* Initiates an information exchange with a known and living other
* base if such a base exists.
* @return true if refresh was succesful, false otherwise.
*/
private boolean refresh() {
	
	final String logSender = observer+"#refresh";
	final String contrName = ( (contributor!=null) ? 
					contributor.getName() : null );

	// --- refreshing local contribution and commands
	updateLocalInfo();
	
	// --- creating a random permutation of peers
	Vector peers = null;
	synchronized(cache)
	{
		peers = new Vector(cache.values());
		// just to be sure, shouldn't be there anyway
		if( contrName != null ) peers.remove( cache.get(contrName) );
	}
	Collections.shuffle(peers);
	if( peers.isEmpty() )
	{
		Logger.debug( logSender, "no peers in cache" );
		return false;
	}
	
	// --- reset array representations
	cacheCollection = null;
	commandCollection = null;

	// --- trying to talk to random peer
	IRequest answer = null;
	Address peer = null;
	for(int i=0; i<peers.size(); ++i)
	{
		if( !shouldLive ) return false;

		peer = ((ContributionBox)peers.get(i)).contributor;
		Logger.debug( logSender, "asking " + peer );

		answer = observer.fireMessage(
			peer, "collectiveUpdate-"+name, this );
		while(answer.getStatus()==IRequest.WAITING)
		{
			try{ Thread.currentThread().sleep(100); }
			catch(Exception e) {}
		}
		if( answer.getStatus()==IRequest.DONE ) break;
		Logger.debug( logSender, "not accessable: " + peer );
	}

	if( answer.getStatus()!=IRequest.DONE )
	{
		Logger.debug( logSender, "no accessable peers" );
		observer.collectiveUpdated( null );
		return false;
	}
	else
	{
		Collective c = (Collective)answer.getInfo("reply");
	
		// --- remove possible garbage
		if( contributor != null )
		{
			cache.remove( contributor.getName() );
			c.cache.remove( contributor.getName() );
		}
		cache.remove( peer.name );
		c.cache.remove( peer.name );

		repairSenderAddress( c, peer );
		merge( c );
		observer.collectiveUpdated( (ContributionBox)
			cache.get(c.myContribution.contributor.name));
		return true;
	}
}


// ============== public constructors =======================
// ==========================================================


/**
* Construct a Collective object. The parameter is the owner, the object
* that participates in the collective.
* @param name The name of the collective we want to join.
* @param o The observer that reads information from the collective.
*/
public Collective( String name, Observer o ) { this( name, o, null, null ); }

// -------------------------------------------------------

/**
* Construct a Collective object.
* @param name The name of the collective we want to join.
* @param o The observer that reads information from the collective.
* @param c The contributor that contributes to the collective effort.
*/
public Collective( String name, Observer o, Contributor c ) {
	
	this( name, o, c, null );
}

// -------------------------------------------------------

/**
* Construct a Collective object.
* @param name The name of the collective we want to join.
* @param o The observer that reads information from the collective.
* @param c The controller that issues collective-wide commands.
*/
public Collective( String name, Observer o, Controller c ) {

	this( name, o, null, c );
}

// -------------------------------------------------------

/**
* Construct a Collective object.
* @param name The name of the collective we want to join.
* @param o The observer that reads information from the collective.
* It must be non-null.
* @param cb The contributor that contributes to the collective effort.
* @param c The controller that issues collective-wide commands.
*/
public Collective( String name, Observer o, Contributor cb, Controller c ) {
	
	if( o == null ) throw new IllegalArgumentException("null observer");
	if( name == null ) throw new IllegalArgumentException("null name");
		
	this.name = name;
	observer = o;
	controller = c;
	contributor = cb;
}

// -------------------------------------------------------

/**
* Construct a Collective object.
* @param c The contributions, the commands and the name will be initialized
* from this object.
* @param o The observer that reads information from the collective.
* It must be non-null.
* @param cb The contributor that contributes to the collective effort.
* @param c The controller that issues collective-wide commands.
*/
public Collective( Collective c, Observer o, Contributor cb, Controller cr ) {
	
	this( c.name, o, cb, cr );
	
	if( c == null ) throw new IllegalArgumentException("null collective");
	
	if( c.cache != null ) cache.putAll(c.cache);
	if( c.commands != null ) commands.putAll(c.commands);
	if( contributor != null ) cache.remove( contributor.getName() );
}


// ============== public functions ==========================
// ==========================================================


/** This stops the service. Restarting is not possible. */
public void close() { shouldLive = false; }

// ---------------------------------------------------------------

/**
* Handles a message. Knows type "collectiveUpdate-"+name only. It is the
* responsibility of the owner to propagate messages of this type using
* this method.
*/
public boolean handleMessage( Message m, Object o ) {
	
	if( !shouldLive || 
	    !m.getType().equals("collectiveUpdate-"+name) ) return false;

	final String logSender = observer+"#collectiveUpdate";
	
	Logger.debug( logSender, "Update from "+m.getSender() );

	/**/
	if(!m.getRecipient().name.equals(contributor.getName()))
		Logger.warning( logSender, "Recipient and my contributor are not the same:\n"
				+ "Recipient: " + m.getRecipient().name + "\n"
				+ "Contributor: " + contributor.getName(),null );
	/**/
	
	Collective c = (Collective)o;

	// --- reset array representations
	cacheCollection = null;
	commandCollection = null;
	
	// --- remove possible garbage
	cache.remove( m.getRecipient().name );
	c.cache.remove( m.getRecipient().name );
	cache.remove( m.getSender().name );
	c.cache.remove( m.getSender().name );
	
	// --- sending our contributions
	if( contributor == null )
		Logger.warning( logSender,
		"Non-contributor observer is known by "+m.getSender(),null );
	updateLocalInfo();
	m.setReply( this );
	
	// --- update containers
	repairSenderAddress( c, m.getSender() );
	merge( c );
	observer.collectiveUpdated( (ContributionBox)
		cache.get( m.getSender().name ) );
	
	return true;
}

// -------------------------------------------------------------------

/**
* This method is the main function of the thread of the collective.
* It does housekeeping and regularly talks to
* peers to exchange information. It can be stopped by calling
* <code>close()</code>.
* @see #close()
*/
public final void run() {
	
	while( shouldLive ){
		//try{
			System.gc();
			removeOldStuff();
			if( !refresh() ){
				Address[] a = observer.getPeerAddresses();
				if( a != null )
				synchronized(cache)
				{
					for( int i=0; i<a.length; ++i )
						cache.put( a[i].name, new
							ContributionBox(a[i],null) );
				}
			}
			for(int i=0; i<REFRESHRATE; i+=1000){
				try { Thread.currentThread().sleep( 1000 ); }
				catch( InterruptedException e ) { shouldLive = false; }
				if( shouldLive == false ) break;
				Thread.currentThread().yield();
			}
		/*}
		catch( RuntimeException e )
		{
			Logger.error( "Collective#run()",
			"Runtime exception caught, something is going wrong",e);
			
		}*/
	}

	cache = null;
	observer = null;
}

// -------------------------------------------------------------------

/**
* Adds a peer address to exchange information with.
* This class needs peer addresses to talk to. The implementation of
* the collective requires that we know
* at least one peer from the same collective.
* Through this method a peer can be explicitly set.
* If a peer with the same name exists, its address will be updated.
* The running collective can ask its owner for peers as well using
* {@link Observer#getPeerAddresses()}.
*/
public void addPeerAddress( Address a ) {
	
	if( a == null ) return;
	
	ContributionBox b =
		(ContributionBox)cache.remove(a.name);
	if( b != null )
		cache.put( a.name, new ContributionBox(
			a, b.timeStamp, b.contribution ));
	else
		cache.put( a.name, new ContributionBox( a, null ) );
}

// -------------------------------------------------------------------

/**
* Returns the contribution of the given entity. If no contribution is known
* returns null.
*/
public ContributionBox getContribution( String name ) {
	
	return (ContributionBox)cache.get(name);
}

// -------------------------------------------------------------------

/**
* Returns known contributions in an unmodifiable list.
* The list contains at most {@link #MAX_CACHE_SIZE} random elements from
* the contribution repository.
* Might return the same list when called again if there is two little time
* between the calls.
* The dynamic type of elements is ContributionBox. It is
* safe to call it frequently. Threads should care of synchronization
* when iterating over it.
*/
public synchronized List getContributions() {

	List o = cacheCollection;
	if( o == null )
	{
		o=Collections.unmodifiableList(new ArrayList(cache.values()));
		cacheCollection = o;
	}

	return o;
}

// -------------------------------------------------------------------

/**
* Returns known active commands in an unmodifiable list.
* The list contains at most {@link #MAX_COMMANDS_SIZE} elements from
* the command database.
* It is safe to call it frequently. Threads should care of synchronization
* when iterating over the it.
*/
public synchronized List getCommands() {

	List o = commandCollection;
	if( o == null )
	{
		o = Collections.unmodifiableList(
			new ArrayList(commands.keySet()) );
		commandCollection = o;
	}

	return o;
}

// -------------------------------------------------------------------

public String toString() {

	return myContribution+"\n"+cache+"\n"+commands;
}

}



