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
import drm.util.StringCollections;
import java.util.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.io.IOException;

/**
* This class extends {@link Base} to become a DRM node.
* A node is a member of the DRM (distributed resource machine) collective,
* a collective which has the goal of serving distributed applications.
* The agents living in a Node are given
* the chance to observe the DRM.
* The agents can cast the IBase object set through setBase into
* the type IDRM, and they can access information about the DRM through it.
*
* <p>All communication is implemented via the standard Base messaging
* interface, the low level protocol is not extended.
* @see IDRM
* @see IAgent#setBase(IBase)
*/
public final class Node extends Base
implements Observer, Contributor, Controller, IDRM {
private static final long serialVersionUID = 1L;

// ===================== Private Fields ===========================
// ================================================================


/**
* The interface to the collective forming the distributed resource machine
* by connecting the standalone agentbases (Base) together.
* The collective contains contributions of other participants and
* collection-wide controll commands.
*/
private Collective DRM = null;

/** we store the last peer the collective communicated with here */
private ContributionBox lastPeer = null;

/**
* Temporary storage place for the commands we want to spread through the
* collective. <code>getCommands()</code> reads it.
*/ 
private HashSet newCommands = new HashSet();

/**
* A queue of executed commands. The maximal size of the queue is
* {@link Collective#MAX_COMMANDS_SIZE}. After reaching this size the oldest
* element is removed.
*/ 
private List executedCommands = new Vector();


// =========== Private member classes ================================
// ===================================================================


private class LaunchThread extends Thread implements IRequest {

	private final IAgent agent;

	private int status = WAITING;
	
	private Throwable thr = null;

	private final long startTime;
		
	/**
	* When finished gives the destination. null with a DONE status
	* means local launch.
	*/
	private Address target = null;
	
	/**
	* when finished gives the launch type as documented at launch
	*/
	String type = "failed";
	
	// --------------------------------------------------------
	
	public int getStatus() { return status; }

	public Throwable getThrowable() { return thr; }
	
	public long getStartTime() { return startTime; }
	
	public Object getInfo( String q ) {
	
		if( q == null ) return null;
		else if( q.equals("type") ) return type;
		else if( q.equals("address") )
		{
			if( status != DONE ) return null;
			if( target == null )
				return new Address(agent.getName());
			return new Address(
				target.getHost(),target.port,agent.getName());
		}
		else return null;
	}
	
	// --------------------------------------------------------

	/**
	* agent must not be a Node
	*/
	public LaunchThread( IAgent agent ) {
		
		super( "Launch-" + agent.getName() );
		this.agent = agent;
		startTime = System.currentTimeMillis();
		
		if( agent.getType().equals( "Node" ) )
			throw new IllegalArgumentException(
			agent.getName() + " is a node.");
	}
	
	// --------------------------------------------------------

	/**
	* Send the message through a socket connection.
	*/
	public void run() {

		Logger.debug( getClass().getName(), "Launching " + agent );
	
		List peers = DRM.getContributions();

		try
		{
	
			// ---- look in local node ---------------------------------
		
			final String pre = agent.getType() + "." + agent.getJob();
			if( !StringCollections.containsPrefix(getNames(),pre)
			    || peers.size()==0 )
			{
				type = "local";
				return;
			}
			
			// ---- random target -------------------------------------
	
			int p = (int) Math.floor( Math.random() * (peers.size()+1) );
			ContributionBox peer = null;
			if( p < peers.size() ) peer = (ContributionBox)peers.get(p);
			
			if( p==peers.size() || peer.contributor.name.equals(name))
			{
				type = "random local";
				return;
			}
			else
			{
				type = "random";
				target = peer.contributor;
			}

		}
		catch( Throwable e ) { thr = e; }
		finally 
		{
			if( thr != null ) { status = ERROR; }
			else if( target == null ) // local
			{
				Logger.debug( getClass().getName(),
					"Launch type: '" + type + "'" );
				IRequest r = launch( "DIRECT", agent, target );
				status = r.getStatus();
				thr = r.getThrowable();
			}
			else
			{
				Logger.debug( getClass().getName(),
					"Launch type '"+type+"' to "+target);
				IRequest r = launch( "DIRECT", agent, target );
				try { ((Thread)r).join(); }
				catch( Exception e ){}
				status = r.getStatus();
				thr = r.getThrowable();
			}
				
		}
	}
}

// -------------------------------------------------------------------

/**
* agents can cast their IBase object into IDRM so that they can
* access info about the DRM collective.
* Does not provide info any longer after the firewall is closed.
*/
private class NodeFirewall extends Firewall implements IDRM {

	public ContributionBox getContribution(String nodeName) {
	
		return ((IDRM)b).getContribution(nodeName);
	}

	// -----------------------------------------------------------
	
	public ContributionBox getNewestContribution() {
		
		return ((IDRM)b).getNewestContribution();
	}
	
	// -----------------------------------------------------------
	
	public List getContributions() {
	
		return ((IDRM)b).getContributions();
	}
}


// =========== Public constructors ===================================
// ===================================================================


/**
* Calls super constructor. The additional configuration parameters
* understood by the class are the following:
* <ul>
* <li> Every property that starts with <code>"node"</code> gives the address of
* another living node in the form host:port.</li>
* <li> If the property <code>spy</code> is "true" (case sensitive) then when
* exchanging information with other nodes this will not publis its
* own descriptor. Thus this will be an observer of the collective but not
* a contributor. The default is false. </li>
* </li>
*/
public Node( Properties cfg ) { super(cfg); }


// =========== Public IBase Implementations ==========================
// ===================================================================


/**
* Adds implementation of launch method "RANDOM".
* Launches an agent to an indirectly specified location.
* If no peers of the agent are present (a peer of a thread is a thread
* from the same job) at the local host then launches the agent locally
* ("local" type) otherwise launches to a random base ("random" type or
* "random local" type if the random base was the local base).
* @param method If "RANDOM", runs the above algorithm otherwise propagates
* call to superclass.
* @param par ignored
* @return The request. The <code>getInfo</code> method there knows two
* kinds of queries: "type" which returns the type as desribed above in String
* format and "address" which returns the address of the launched agent
* (type {@link Address}) if status is DONE, otherwise null.
*/
public IRequest launch( String method, final IAgent agent, final Object par ) {

	if( method != null && method.equals("RANDOM") )
		return (IRequest)
		AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
			LaunchThread t = new LaunchThread( agent );
			t.start();
			return t;
		}
		});
	else return super.launch(method,agent,par);
}


// =========== Public IDRM Implementations ===========================
// ===================================================================

	
public ContributionBox getContribution(String nodeName) {

	if( DRM == null ) return null;
	return DRM.getContribution(nodeName);
}

// -------------------------------------------------------------------
	
public ContributionBox getNewestContribution() { return lastPeer; }
	
// -------------------------------------------------------------------
	
public List getContributions() {

	if( DRM == null ) return Collections.EMPTY_LIST;
	return DRM.getContributions();
}


// =========== Public Base Implementations ===========================
// ===================================================================


/**
* Calls super implementation and after that closes the collective.
*/
public void close() {
	
	super.close();
	if( DRM != null ) DRM.close();
}

// -------------------------------------------------------------------

public Firewall getFirewall() { return new NodeFirewall(); }


// =========== Public IAgent Implementations =========================
// ===================================================================


/**
* Handles message type <code>"collectiveUpdate-"+getJob()</code>. In fact
* only forwards it to the collective.
*/
public boolean handleMessage( Message m, Object object ) {

	if( super.handleMessage( m, object ) ) return true;

	if( m.getType().equals("collectiveUpdate-"+getJob()) )
	{
		return DRM.handleMessage(m,object);
	}
	else if( m.getType().equals("addCommand") )
	{
		newCommands.add(object);
	}
	else if( m.getType().equals("getInfo") )
	{
		m.setReply("java.version "+System.getProperty("java.version")+
			"\nDRM.version "+version()+
			"\nmemory "+Runtime.getRuntime().freeMemory());
	}
	else if( m.getType().equals("getStatus") )
	{
		m.setReply("running");
	}
	else
	{
		Logger.debug(getClass().getName()+"#handleMessage",
			m.getType() + " received from " + m.getSender());
		return false;
	}
	
	return true;
}


// -------------------------------------------------------------------

/** Returns "Node". */
public String getType() { return "Node"; }

// -------------------------------------------------------------------

/** Connects to the DRM collective. It is called by the Base when going online.
* It is not supposed to be called by users, it has to be public because it
* is an interface function. */
public void onArrival( Address from, Address to ) {

	boolean spy = cfg.getProperty("spy","false").equals("true");
	DRM = new Collective( group, this, (spy?null:this), this );
	new Thread(DRM,"DRM Collective").start();
}


// =========== Public Collective-related implementations =============
// ===================================================================


/**
* Stores the peer, and performs new commands.
*/
public synchronized void collectiveUpdated( ContributionBox peer ) {

	if( peer == null ) return;
	
	lastPeer = peer;
	
	Collection commands = DRM.getCommands();
	int oldSize = executedCommands.size();
	synchronized(commands)
	{
		Iterator i = commands.iterator();
		while( i.hasNext() )
		{
			Object o = i.next();
			if( (o instanceof NodeCommand) &&
			    !executedCommands.contains(o) )
			{
				executedCommands.add(o);
			}
		}
	}
	commands=null;
	
	for(int i=oldSize; i<executedCommands.size(); ++i)
	{
		invokeCommandLocally((NodeCommand)executedCommands.get(i));
	}

	while( executedCommands.size() > Collective.MAX_COMMANDS_SIZE )
		executedCommands.remove(0);
}

// -------------------------------------------------------------------

/**
* Returns always null. Implements required functionality via calling
* <code>addPeerAddress<code> instead, thus through a side effect.
* @see Collective#addPeerAddress(Address)
*/
public Address[] getPeerAddresses() {

	addNodes(cfg);
	return null;
}
	
// -------------------------------------------------------------------

public IRequest fireMessage( Address recipient, String type, Object content ) {
	
	try
	{
		return fireMessage( new Message(
		new Address(getName()), recipient, type, content ) );
		// using a local address as sender is intentional,base fixes it
	}
	catch(IOException e)
	{
		return new StaticRequest(IRequest.ERROR,e);
	}
}

// -------------------------------------------------------------------

public Object getContribution() {
	
	return new NodeContribution(null,getNames());
}

// -------------------------------------------------------------------

public Set getCommands() {

	Set ret = (Set)newCommands.clone();
	newCommands.clear();
	return ret;
}


// =========== Public Methods ========================================
// ===================================================================


/**
* Adds the peers defined in <code>cfg</code> to the default peer list
* of the collective.
* Initializes peers from the properties starting with "base" or "node".
* The format of the property value should be host[:port].
* If no port is given then the one defined in cfg is used, or 10101 as
* default.
*/
public void addNodes( Properties cfg ) {

	final String logSender = getClass().getName()+"#addNodes";
	Enumeration i = cfg.propertyNames();
	while( i.hasMoreElements() )
	{
		String tmp = (String)i.nextElement();
		if( ! tmp.startsWith("base") &&
		    ! tmp.startsWith("node") ) continue;

		// --- parse the value
		try
		{
			String addr = cfg.getProperty(tmp);
			if( addr == null || addr.length()==0 ) continue;
		
			// ---- split addr into host and port
		
			addr = addr.trim();
			int port = Integer.parseInt(
				cfg.getProperty("port","10101"));
			InetAddress host=null;
			
			StringTokenizer st = new StringTokenizer(addr," :");
			if( st.countTokens() > 0 )
				host = InetAddress.getByName( st.nextToken() );
			if( st.hasMoreTokens() )
				port = Integer.parseInt(st.nextToken());
			
			Logger.debug( logSender, "checking " + host + ":"
				+ port );
			String n = getBaseName( host, port, group, 10000 );
			if( n == null )
			{
				Logger.error( logSender, "" + host
					+ ":" + port + " is not alive", null );
			}
			else if( !name.equals(n) )
			{
				Address a = new Address( host, port, n );
				Logger.debug( logSender, "adding " + a );
				DRM.addPeerAddress(a);
			}
		}
		catch( Exception e )
		{
			Logger.error( logSender, tmp, e );
		}
	}
}

// -------------------------------------------------------------------

/**
* Invokes the command on the DRM.
*/
public void invokeCommand( NodeCommand command ) {
	
	newCommands.add(command);
}

// -------------------------------------------------------------------

/**
* Invokes the command only on this local node.
*/
public void invokeCommandLocally( NodeCommand command ) {

	switch( command.com )
	{
		case NodeCommand.CLEANALL:
			wipeClean( ((Long)command.pars[0]).longValue() );
			break;
	}
}

// -------------------------------------------------------------------

public String toString() { return name; };
}


