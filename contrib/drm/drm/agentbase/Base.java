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


package drm.agentbase;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
* A class to host mobile agents. It is able to dispatch
* and receive them, handles their requests and messages.
*/
public class Base implements IBase, IAgent {
private static final long serialVersionUID = 1L;

// ===================== Private Fields ===========================
// ================================================================


/**
* The port number on which the base listens. If -1 then the base
* is offline.
*/
private int port = -1;

/**
* Thread that accepts network connections.
*/
private ListenThread listenThread = null;

/**
* Listens on the main port.
*/
private ServerSocket serverSocket = null;

/**
* Manages class loaders used by the agents.
*/
private ClassLoaderManager clm = null;

/**
* Classes that listen to the fired base events.
*/
private List baseListeners = Collections.synchronizedList(new Vector());

/** Hard wired par. The housekeeping jobs are done in this time intervals. */
private final long REFRESHRATE = 10000;

/**
* Hard wired par. The jar files belonging to the classloaders of agents are
* removed after this timeout of non-usage.
*/
private final long JAR_TIMEOUT = 60000;


// =============== Protected fields ===============================
// ================================================================


/**
* The configuration properties. These mustn't be changed after initialization,
* extending classes must take care of that.
*/
protected final Properties cfg;

/**
* Stores all agents and related information. Key is the agent name,
* value is an AgentBox.
* Extending classes mustn't change this map,
* and they have to synchronise every access and iteration over it.
* Otherwise realiable behaviour is not guaranteed.
*/
protected Map boxes = Collections.synchronizedMap(new Hashtable());


// =============== Public Constants ===============================
// ================================================================


public static final byte MESSAGE = 0;

public static final byte AGENT = 1;

public static final byte ISALIVE = 123;

public static final byte GET_JAR = 100;

public static final int SENDING_DIR = -2;

public static final byte OK = 101;

public static final byte NOT_OK = 102;

public static final int GROUP_MISMATCH = -1;

public static final int PROTOCOL_MISMATCH = -1;

/**
* The protocol version used by this implementation.
* This is the version of the low level of the protocol used for communication
* between bases. see
* <a href="../server/doc-files/protocol.html">protocol specification</a>.
*/
// This version is not fully compatible with previous ones, it sends (and waits for)
// the name of the receiving agent (or node) when sending an agent.
// This version rejects agents or messages if sent to a not existing recipient.
public static final int PROTOCOL_VERSION = 2;

/**
* The version of this release.
* The version consist of 3 numbers,
* in the format of aa.bb.ccc. This number is converted to an
* integer simply by writing aabbccc. Thus eg version 2.0.0 is 200000.
* Note that the API version is aa.bb and the implementation versions
* are denoted by the ccc postfix.
*/
public static final int RELEASE_VERSION = 200000;

/**
* A unique name for the base. Initialized after cfg has been initialized
* using getUniqueName(). This later function can be overridden in extending
* classes and the function can use values from cfg. The generated name
* is stored in cfg as drm.baseName and can be asked via getProperty.
* @see #cfg
* @see #getUniqueName()
* @see IBase#getProperty(String)
*/
public final String name;

/**
* To store the group name. The constructor sets it.
*/
public final String group;


// ================= Private Member Classes =========================
// ==================================================================


/**
 * Handles all incoming connections. 
 */
private class ConnectionHandler extends Thread {

	private Socket socket;
		
	private AgentInputStream ais = null;
	
	private ObjectOutputStream oos = null;

	/** the protocol version of the incoming connection */
	private int version;
	
	private final int SOTIMEOUT = 30000; // half minute

	// --------------------------------------------------------
	
	/**
	* Handles AGENT connections. The agent will be put into the base
	* with all necessary inicializations.
	*/
	private void receiveAgent()
	throws IOException, ClassNotFoundException {
	synchronized(clm)
	{
		Logger.debug( getClass().getName(), "an AGENT arrived" );
		
		/* This code makes this class not compatible with previous versions,
		 * because it changes the protocol. It is needed to make sure that
		 * messages are delivered to the right agent or discarded with an error. */
		String to = (String)ais.readObject();
		if( to == null || !to.equals(name))
		{
			oos.writeByte( NOT_OK );
			oos.flush();
			Logger.warning( getClass().getName(),
					"AGENT rejected: I'm not " + to, null );
			return;
		}else oos.writeByte( OK );
		oos.flush();
		/**/
		
		String cname = (String) ais.readObject();
		
		if( clm.getLoader(cname) != null ) oos.writeByte( OK );
		else
		{
			oos.writeByte( GET_JAR );
			oos.flush();
			clm.putLoader( cname, ais );
			oos.writeByte( OK );
		}
		ais.setClassLoader( clm.getLoader(cname) );
		
		try
		{
			oos.flush();
			IAgent a = (IAgent) ais.readObject();
			Address from = (Address) ais.readObject();
			from.host = socket.getInetAddress();

			addAgent( a, from, new Address(
					socket.getLocalAddress(),
					socket.getLocalPort(), name) );
		}
		catch( Throwable e )
		{
			oos.writeByte(NOT_OK);
			if( e instanceof IOException ) 
				throw (IOException)e;
			else if( e instanceof ClassNotFoundException )
				throw (ClassNotFoundException)e;
			else Logger.error( getClass().getName(), "", e );
			return;
		}
	
		oos.writeByte(OK);
	}
	}
	
	
	// --------------------------------------------------------
	
	/**
	* Handles MESSAGE connections.
	* This method posts the message to the appropriate recipient.
	* The sender host in the message is filled in with
	* the data given by the socket, no matter what it contained
	* previously. This way no wrong sender address is possible.
	* If the recipient name begins with getType()+"."+getJob() than
	* the message is delivered to the base (ie no exact match is necessary
	* in the case of a base recipient until the group is the same).
	*/
	private void receiveMessage()
	throws IOException, ClassNotFoundException {
	
		Logger.debug( getClass().getName(), "a MESSAGE arrived" );
		String to = (String)ais.readObject();
		IAgent rec = null;
		boolean mHandled = false;
		
		if( to != null )
		{
			// deliver message to local agent
			AgentBox box = (AgentBox)boxes.get(to);
			if( box != null )
				rec = box.agent;
		//	Why messages with the wrong recipient should arrive anywhere?
		//	else if( to.startsWith(getType()+"."+getJob()) )
		//		rec = Base.this;
			
		}
		
		if( rec == null )
		{
			oos.writeByte( NOT_OK );
			Logger.debug( getClass().getName(),
				to + " is not here");
		}
		else
		{
			oos.writeByte( OK );
			oos.flush();
			Message m = (Message)ais.readObject();
			m.sender.host = socket.getInetAddress();
			try{
				mHandled = rec.handleMessage( m,
						AgentInputStream.getObject(m.getBinary(),rec));
			}
			catch(Exception e){
				oos.writeByte(NOT_OK);
				Logger.error( getClass().getName(),
						"Handling the message " + m.toString() + 
						" raised an Exception", e);
			}
			if( !mHandled ) oos.writeByte(NOT_OK);
			else
			{
				if( m.reply != null )
				{
					oos.writeByte( MESSAGE );
					oos.writeObject(m.reply);
				}
				else oos.writeByte(OK);
			}
		}
	}
	
	// --------------------------------------------------------
	
	/**
	* Handles ISALIVE connections.
	* This method responds to the request with the name of the base.
	* The name is sent as a serialized string object.
	*/
	private void receiveIsAlive() throws IOException {
		
		Logger.debug( getClass().getName(), "an ISALIVE arrived" );
		oos.writeObject( name );
	}
	
	// --------------------------------------------------------
	
	/**
	 * Creates a new Thread to handle a connection.
	 * @param socket Socket of the incoming connection.
	 */
	public ConnectionHandler( Socket socket ) {
	
		super("ConnectionHandler");
		this.socket = socket;
	}
	
	// --------------------------------------------------------
	
	public void run(){
		String peerGroup = null;
		final byte type;
		
		try
		{
			Logger.debug( getClass().getName(),
				"Connection from "+socket.getInetAddress());
			socket.setSoTimeout(SOTIMEOUT);
			ais = new AgentInputStream(socket.getInputStream(),
				null);
			version = ais.readInt();
			peerGroup = (String)ais.readObject();
			type = ais.readByte();
			
			oos = new ObjectOutputStream(socket.getOutputStream());
		}
		catch(SocketException e){
			Logger.error( getClass().getName(), "Exception caught when setting socket timeout: ", e );
			return;
			}
		catch(IOException e){
			Logger.error( getClass().getName(), "Exception caught when reading data from a socket", e );
			return;
		}
		catch(ClassNotFoundException e){
			Logger.error( getClass().getName(), "I got a non-String as peerGroup: ", e );
			return;
		}

		try
		{	
			if( peerGroup == null || !peerGroup.equals(group) )
			{
				oos.writeInt(GROUP_MISMATCH);
				oos.flush();
				throw new ProtocolException(
					"Peer is from group "+
					peerGroup+", our group is "+group);
			}
				
			if( version != PROTOCOL_VERSION )
			{
				oos.writeInt(PROTOCOL_MISMATCH);
				oos.flush();
				throw new ProtocolException(
					"Peer is using protocol version " +
					version + ", our version is " + PROTOCOL_VERSION);
			}
				
			oos.writeInt(PROTOCOL_VERSION);
			oos.flush();
		}
		catch(ProtocolException e){
			Logger.error( getClass().getName(), "", e );
			return;
		}
		catch(IOException e){
			Logger.error( getClass().getName(), "Exception caught when writing data to a socket: ", e );
			return;
		}
			
		try
		{
			switch(type)
			{
				case AGENT:
					receiveAgent();
					break;
				case MESSAGE:
					receiveMessage();
					break;
				case ISALIVE:
					receiveIsAlive();
					break;
				default:
					throw new ProtocolException("Bad header, type not recognized");
			}
		}
		catch( Exception e )
		{
			Logger.error( getClass().getName(), "Exception caught when receiving an agent or message: ", e );
		}

		try
		{
			if( oos != null )
			{
				oos.flush();
				oos.close();
			}
			if( ais != null ) ais.close();
			if( socket != null ) socket.close();
		}
		catch( IOException e )
		{
			Logger.error( getClass().getName(), "Exception caught when closing the socket: ", e );
		}
	}
}	
			

// -------------------------------------------------------------------

/**
 * Listens on the specified port and spawns new threads to
 * receive incoming agents.
 */
private class ListenThread extends Thread {

	private Socket socket = null;
	
	volatile boolean shouldLive = true;

	// --------------------------------------------------------
	
	public ListenThread() { super("ListenThread"); }
 
	// --------------------------------------------------------
	
	/**
	 * Accepts connections on the server socket.
	 */ 
	public void run() {
	
		try { serverSocket.setSoTimeout(100); }
		catch( SocketException e )
		{ 
			Logger.panic( getClass().getName(), null,  e );
			System.exit(1);
		} 
		while( shouldLive )
		{
			try
			{
				socket = serverSocket.accept();
				new ConnectionHandler( socket ).start();
				yield();
			}
			catch( InterruptedIOException e ) {
			// This happens regularly when the socket times out
			// The outer loop checks, if the server should continue
			// accepting connections
			}
			catch( IOException e )
			{
				Logger.error( getClass().getName(), null, e );
				// but we continue listening
			}
		}
		
		try { serverSocket.close(); } 
		catch( IOException e )
		{ 
			Logger.panic( getClass().getName(), null, e );
			System.exit(1);
		} 
		serverSocket = null;
	}

}

// -------------------------------------------------------------------

/**
 * Sends a Message to another base.
 */
private class SendMessageThread extends Thread implements IRequest {

	private Message m;

	private int status = WAITING;
	
	private Throwable thr = null;

	private final long startTime;
	
	private Object reply;
	
	// --------------------------------------------------------
	
	public int getStatus() { return status; }

	public Throwable getThrowable() { return thr; }
	
	public long getStartTime() { return startTime; }
	
	/** if q="reply" returns field reply */
	public Object getInfo( String q ) {
		
		if( q!=null && q.equals("reply") ) return reply;
		else return null;
	}
	
	// --------------------------------------------------------

	/**
	* Creates a thread that sends a message to another host.
	* The message must contain the receiver's address!
	* From the sender address only the name is used.
	* It is assumed that the base is online.
	* @param mess The message to send.
	*/
	public SendMessageThread( Message mess ) {
		
		super( "SendMessage-" + mess.getType() );
		m = mess;
		m.sender = new Address( null, port, m.sender.name );
			// the receiver sets host using the socket
		startTime = System.currentTimeMillis();
	}
	
	// --------------------------------------------------------

	/**
	* Send the message through a socket connection.
	*/
	public void run() {
	
		if( !isOnline() )
		{
			status = ERROR;
			thr = new IOException("Base is offline");
			m = null;
			return;
		}
		
		Connection c = null;

		try {
			c = new Connection( m.getRecipient(), group, MESSAGE,
				getContextClassLoader() );
			
			c.oos.writeObject(m.recipient.name);
			c.oos.flush();
			byte b = c.ois.readByte();
			if( b == OK )
			{
				c.oos.writeObject(m);
				c.oos.flush();
				
				b = c.ois.readByte();
				if( b == MESSAGE )
				{
					reply = c.ois.readObject();
					b = OK;
				}
			}
			if( b != OK ) status = ERROR;
		}
		catch( Throwable e )
		{
			if(e instanceof ConnectException)
				Logger.debug( getClass().getName(),
						"Message not delivered. Destination unreachable.\n" + m );
			else
				Logger.error( getClass().getName(),
						"Message not delivered.\n" + m, e );
			thr = e;
			status = ERROR;
		}

		try { if( c != null ) c.close(); }
		catch( IOException e )
		{
			Logger.error( getClass().getName(), "Exception when closing a connection to " + 
					m.getRecipient() + ": ", e );
			thr = e;
			status = ERROR;
		}
		
		if( status != ERROR ) status = DONE;
		m = null;
	}
}

// -------------------------------------------------------------------

/**
* Sends an IAgent to another base. The agent doesn't have to live in the base.
* The classloader of the agent has to be a JobClassLoader.
*/
private class SendAgentThread extends Thread implements IRequest {

	private IAgent a;
	
	private Address to;

	private int status = WAITING;
	
	private Throwable thr = null;

	private final long startTime;

	private final boolean destroy;

	// --------------------------------------------------------
	
	/**
	* sends jar to given connection, or directory
	* name if there is no jar
	*/
	private void sendJAR( Connection c ) throws IOException {
		
		File f = ((JobClassLoader)a.getClass().getClassLoader()).file;
		Logger.debug(getClass().getName(),"sending "+f);
		
		if( f.isFile() )
		{
			c.oos.writeInt( (int)f.length() );
			c.oos.flush();
			InputStream jaris = new FileInputStream( f );
			byte[] buff = new byte[1000];
			int n = jaris.read(buff);
			while( n != -1 )
			{
				c.oos.write(buff,0,n);
				n = jaris.read(buff);
			}
			c.oos.flush();
			jaris.close();
		}
		else
		{
			c.oos.writeInt(SENDING_DIR);
			c.oos.writeObject( f );
			c.oos.flush();
		}
		
		c.ois.readByte(); // wait for acknowledgement
		Logger.debug( getClass().getName(),"done sending "+f );
	}
	
	// --------------------------------------------------------
	
	public int getStatus() { return status; }

	public Throwable getThrowable() { return thr; }
	
	public long getStartTime() { return startTime; }
	
	public Object getInfo( String q ) { return null; }
	
	// --------------------------------------------------------

	/**
	 * Creates a thread that sends an agent to another host.
	 * @param a the agent. Must have a classloader of type JobClassLoader.
	 * @param destroy if true destroyes the agent from the base if
	 * sending was succesful.
	 * @see JobClassLoader
	 */
	public SendAgentThread( IAgent a, Address to, boolean destroy ) {
		
		super( "SendAgent" );
		if(!(a.getClass().getClassLoader() instanceof JobClassLoader))
			throw new IllegalArgumentException(
				"Agent must have a JobClassLoader");
		this.a = a;
		this.to = to;
		this.destroy = destroy;
		startTime = System.currentTimeMillis();
	}
	
	public SendAgentThread( IAgent a, Address to ) { this(a,to,false); }
	
	// --------------------------------------------------------

	/**
	 * Send the agent through a socket connection.
	 */
	public void run() {
		
		if( !isOnline() )
		{
			status = ERROR;
			thr = new IOException("Base is offline");
			a = null;
			return;
		}
		
		byte reply = 0;
		Connection c = null;

		try {
			c = new Connection( to, group, AGENT );
			
			/* This code makes this class not compatible with previous versions,
			 * because it changes the protocol. It is needed to make sure that
			 * messages are delivered to the right agent or discarded with an error. */
			c.oos.writeObject(to.name);
			c.oos.flush();
			reply = c.ois.readByte();
			Logger.debug( getClass().getName(),
				"recipient found: "  + reply );
			if( reply == NOT_OK) throw(new ConnectException("Node not found at " + to));
			/**/
			
			c.oos.writeObject( a.getJob() );
			c.oos.flush();
			reply = c.ois.readByte();
			Logger.debug( getClass().getName(),
				"return status: "  + reply );
			if( reply == GET_JAR ) sendJAR(c);
			c.oos.writeObject( a );
			c.oos.writeObject( new Address( null, port, name ) );
			c.oos.flush();
			reply = c.ois.readByte();
		}
		catch( Throwable e ){
			if(e instanceof ConnectException)
				Logger.debug( getClass().getName(),
					"Agent to " + to + " not delivered. Destination unreachable." );
			else
				Logger.error( getClass().getName(),
					"Agent to " + to + " not delivered.", e );
			thr = e;
			status = ERROR;
		}
		try { if( c != null ) c.close(); }
		catch( Throwable e )
		{
			Logger.error( getClass().getName(), "Exception when closing a connection for " + to, e );
			thr = e;
			status = ERROR;
		}
		
		if( reply != OK ) status = ERROR;
		if( status != ERROR )
		{
			status = DONE;
			if(destroy) destroyAgent(a.getName());
		}
		a = null;
		to = null;
	}
} 


// ============ protected member classes ============================
// ==================================================================


/**
* This is an implementation of IBase hiding
* the public functions which are not part of IBase.
* This is the class that serves as a firewall between the base and the
* agents.
*/
protected class Firewall implements IBase {

	protected IBase b = Base.this;

	// ----------------------------------------------------------
	
	public void close() { b = null; }
	
	// ----------------------------------------------------------
	
	public final String getProperty( String p ) {return b.getProperty(p);}

	// ----------------------------------------------------------

	public final Set getNames() { return b.getNames(); }

	// ----------------------------------------------------------

	public final void destroyAgent( String name ) { b.destroyAgent(name); }

	// ----------------------------------------------------------

	public final IRequest dispatchAgent( String name, Address dest ) {

		return b.dispatchAgent( name, dest );
	}

	// ----------------------------------------------------------

	public final IRequest launch( String method, IAgent a, Object par ) {

		return b.launch( method, a, par );
	}

	// ----------------------------------------------------------

	public final IRequest fireMessage(Message m) {return b.fireMessage(m);}

	// ----------------------------------------------------------

	public final boolean isOnline() { return b.isOnline(); }
}


// ======================= Private Methods ==========================
// ==================================================================


/**
* Fires an event if a new agent has arrived.
* @param n Name of the agent.
*/
private void fireAgentArrived( String n ) {

	synchronized( baseListeners )
	{
		Iterator i = baseListeners.iterator();
		while( i.hasNext() ) ((IBaseListener)i.next()).agentArrived(n);
	}
}

// ---------------------------------------------------------------

/**
* Fires an event if an agent has been destroyed.
* @param n Name of the agent.
*/
private void fireAgentDestroyed( String n ) {
	
	synchronized( baseListeners )
	{
		Iterator i = baseListeners.iterator();
		while(i.hasNext()) ((IBaseListener)i.next()).agentDestroyed(n);
	}
}

// ---------------------------------------------------------------

/**
* Adds an agent to the base, performs some initialization and
* starts it's thread.
* The class loader of the agent must be a {@link JobClassLoader}, except
* if the agent is this object.
* It is also checked if the agent name has the type.job. prefix.
*/
private synchronized void addAgent( IAgent a, Address from, Address here )
throws LaunchImpossibleException {
		
	if( a!=this &&
	    !(a.getClass().getClassLoader() instanceof JobClassLoader))
		throw new LaunchImpossibleException(
				"Agent must have a JobClassLoader");
	
	if( !a.getName().startsWith(a.getType()+"."+a.getJob()+".") ) throw new
		LaunchImpossibleException( "Bad agent name "+a.getName() );
		
	
	Firewall fw = getFirewall();
	a.setBase(fw);
	Thread thread = new Thread( a, a.getName() );
	java.util.Date time = new java.util.Date();
	AgentBox box = new AgentBox( a, thread, time, fw );
	
	synchronized(boxes)
	{
		boxes.put( a.getName(), box );
		fireAgentArrived( a.getName() );
		// this must be before onArrival()
		// 'cause onArrival may commit suicide
		a.onArrival( from, here );
	}
	
	clm.inc( a.getJob() );
	thread.start();
}


// =========== Protected Methods =====================================
// ===================================================================


/**
* Called only once to inicialize the name of the base. At calling time
* the properties {@link #cfg} is already set.
*/
protected String getUniqueName() {

	return getType()+"."+group+"."+System.currentTimeMillis() + "-" + 
		System.getProperty("os.name") + "-" +
		System.getProperty("user.name") + "-" +
		System.getProperties().hashCode();
}

// -------------------------------------------------------------------

/**
* This method should return the firewall that is given to the agents.
* It is useful to put it here because extending classes can extend the
* firewall with new functionality that agents can check for.
*/
protected Firewall getFirewall() { return new Firewall(); }


// =========== Public Constructors ===================================
// ===================================================================

/**
* Constructs a Base. The properties used at the moment are the following:
* <ul>
* <li> <code>group</code>: every base belongs to a group of bases.
* Communication is possible only inside a group.
* This prameter gives the name of the group this base belongs to.
* If not defined then "default" is set, and also stored in the config
* properties.</li>
* </ul>
* This coniguration is stored in {@link #cfg}.
* <p>System properties are also used for configuration. At the moment the
* following properties are used:
* <ul>
* <li><code>java.io.tmpdir</code> gives the directory the base
* is allowed to use for storing temporary files as eg the jar files of the
* agents.
* Note that this is a standard system property.
* If the directory is not writable, or cannot be created the system exits.
* </ul>
*/
public Base( Properties c ) {
	
	if( c != null ) cfg = c;
	else cfg = new Properties();
	group = cfg.getProperty("group","default");
	name = getUniqueName();

	cfg.setProperty("group",group);
	cfg.setProperty("drm.baseName",name);

	String drmtmp = System.getProperty("java.io.tmpdir");
	
	if( drmtmp == null )
	{
		Logger.panic( getClass().getName(), "No tmp dir is defined " +
			"in java.io.tmpdir", null );
		System.exit(1);
	}
	
	File tmp = (new File( drmtmp )).getAbsoluteFile();
	
	if( (!tmp.isDirectory() && !tmp.mkdirs()) ||
		!tmp.canRead() || !tmp.canWrite() )
	{
		Logger.panic( getClass().getName(), "Can't use " +
			tmp + " as working directory", null );
		System.exit(1);
	}

	clm = new ClassLoaderManager(tmp);
}


// =========== Public methods ========================================
// ===================================================================


/**
* Returns the name of the remote base if there is one at the given
* address and it answers requests. Otherwise it returns null.
* @param h The IP address of the base.
* @param p The port of the base.
* @param grp The group of the calling entity.
* @param tout The timeout to wait for a reply in ms. If zero then
* we wait until there's a reply, if the connection can be built.
*/
public static String getBaseName(InetAddress h, int p, String grp, int tout ) {
		
	String answer = null;
	Connection c = null;
	
	try
	{
		c=new Connection(new Address(h,p,"?"),grp,Base.ISALIVE,tout);
		answer = (String) c.ois.readObject();
	}
	catch( Throwable e ) {}
		
	try { if( c != null ) c.close(); }
	catch( Throwable e ) {}

	return answer;
}

// ----------------------------------------------------------------

/**
* The base tries to go online on one of the ports from the given
* range (inclusive the limits).
* Being online means that the base starts to listen to incoming connections,
* and initiates a houskeeping thread as a default agent.
* If the base was already online then the given port number is
* ignored and the method simply returns with the old port number.
* @param portFrom The number of the port to start with.
* @param portTo The last port to try.
* @return If negative then the base is offline, otherwise
* the port on which the base listens. It might be that this port
* is outside the given range if the base was already online listening
* to the port when calling this method.
*/
public synchronized int goOnline( int portFrom, int portTo ) {

	for( int i=portFrom; !isOnline() && i<=portTo; ++i )
	{
		try
		{
			serverSocket = new ServerSocket( i );
			listenThread = new ListenThread();
			listenThread.start();
			port = i;
			Logger.debug(getClass().getName()+"#goOnline",
				"port "+port+", "+name );
		}
		catch( BindException e ) {}
		catch( IOException e ) {}
	}
	
	if( !isOnline() ) return -1;
	
	try { if(boxes.get(name)==null) addAgent(this,null,null); }
	catch( LaunchImpossibleException e )
	{
		Logger.panic( getClass().getName()+"#goOnline", null, e );
		System.exit(1);
	}

	return port;
}

// ----------------------------------------------------------------

/**
* After going offline no network traffic is allowed and the base has
* no valid address. Apart from that the agents continue to do their
* thing only they are separated from the world until the base goes
* online again.
*/
public synchronized void goOffline (){
	
	if( !isOnline() ) return;

	final String logSender = getClass().getName()+"#goOffline";	
	try
	{
		Logger.debug( logSender, "Closing server socket... " );
		listenThread.shouldLive = false;
		listenThread.join();
		port = -1;
		Logger.debug( logSender, "Server socket closed" );
	}
	catch( InterruptedException e )
	{
		Logger.error( logSender, null,  e );				
	}
}

// ----------------------------------------------------------------

/**
* Destroys all the agents, stops all the threads, and goes offline.
* The behaviour of the base after calling close is undefined.
* It must be called before releasing the base object for a clean
* shutdown.
*/
public void close() {

	goOffline();
	Iterator i = getNames().iterator();
	while( i.hasNext() )
	{
		String n = (String)i.next();
		if( !n.equals(name) ) destroyAgent(n);
	}
	clm.cleanup(0);
	boxes = null; // this shuts down the agent thread of the base
}

// ----------------------------------------------------------------

public void addListener( IBaseListener l ) {

	synchronized(baseListeners)
	{
		if( baseListeners.contains(l) ) return;
		baseListeners.add(l);
	}
}

// ----------------------------------------------------------------

public void removeListener( IBaseListener l ) {
	
	synchronized(baseListeners)
	{
		baseListeners.remove(l);
	}
}

// ----------------------------------------------------------------

/**
* Cleans the base. It shuts down every agent,
* and makes the base go offline for the given milliseconds.
* The function returns immediately, it does not block.
* <p>
* The base will go online again if it was online when calling this method.
* Although it is not very likely, but it can happen that the port
* the base listens to will change.
*/
public synchronized void wipeClean( final long time ) {

	(new Thread()
	{
		public void run() {
		synchronized(boxes) { synchronized(listenThread)
		{
			int oldPort = port;
			boolean wasOnline = isOnline();
			goOffline();
			
			Iterator i = getNames().iterator();
			while( i.hasNext() )
			{
				String n = (String)i.next();
				if( !n.equals(name) ) destroyAgent(n);
			}
			clm.cleanup(0);
		
			try { sleep( time ); } catch(InterruptedException e) {}
			
			// one more time to make sure before going online
			i = getNames().iterator();
			while( i.hasNext() )
			{
				String n = (String)i.next();
				if( !n.equals(name) ) destroyAgent(n);
			}
			clm.cleanup(0);

			if( wasOnline ) goOnline(oldPort,oldPort+10);
		}}
		}
	}).start();
}


// =========== Public IAgent Implementations =========================
// ===================================================================


/**
* This implementation does not handle any types of messages.
* Returns always false.
*/
public boolean handleMessage( Message m, Object object ) { return false; }

// -------------------------------------------------------------------

/** This implementation is empty, simply returns */
public void onArrival( Address from, Address to ) {}

// -------------------------------------------------------------------

/**
* This should never be called, if called, it is a bug. Exits the jvm with
* a panic message
*/
public final void onDestruction() {

	close();
	Logger.panic( getClass().getName()+"#onDestruction", null, null );
	System.exit(1);
}

// -------------------------------------------------------------------

/**
* returns base name
* @see #name
*/
public final String getName() { return name; }

// -------------------------------------------------------------------

/** returns group name */
public String getJob() { return group; }

// -------------------------------------------------------------------

/** returns "Base" */
public String getType() { return "Base"; }

// -------------------------------------------------------------------

/**
* If the parameter is not a firewall to this, exists the jvm
* with a panic message.
*/
public final void setBase( IBase b ) { 
	
	if( !(b instanceof Firewall) )
	{
		Logger.panic( getClass().getName()+"#setBase", null, null );
		System.exit(1);
	}
}

// -------------------------------------------------------------------

/**
* returns Base.RELEASE_VERSION
* @see #RELEASE_VERSION
*/
public final int version() { return RELEASE_VERSION; }

// -------------------------------------------------------------------

/**
* Main housekeeping tasks.
* If the base is not online, does nothing, until the base
* goes online. Exits only after close() is called.
*/
public final void run() {
	
	while( boxes != null )
	{
		if( isOnline() )
		{
			clm.cleanup( JAR_TIMEOUT );
			try { Thread.sleep( REFRESHRATE ); }
			catch( InterruptedException e ) {}
		}
		else
		{
			try { Thread.sleep(1000); }
			catch( InterruptedException e ) {}
		}
		Thread.yield();
	}
}

// =========== Public IBase Implementations ==========================
// ===================================================================

public String getProperty( String prop ) { return cfg.getProperty(prop); }

// ----------------------------------------------------------------

/**
* @param method has to be "DIRECT"
* @param a has to be non null
* @param par has to be an Address or null
*/
public IRequest launch( String method, final IAgent a, Object par ) {

	if( method==null || !method.equals("DIRECT") || a==null ||
		!(par == null || par instanceof Address) ) throw new
			IllegalArgumentException();
	
	final Address destination = (par != null ? (Address)par : null);

	if( destination == null || destination.isLocal() )
	{
		try { addAgent(a,null,null); }
		catch( Throwable t )
		{
			return new StaticRequest(IRequest.ERROR,t);
		}
		return new StaticRequest(IRequest.DONE,null);
	}
	else
	{
		return (IRequest)
			AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				Thread t = new SendAgentThread(a,destination);
				t.start();
				return (IRequest)t;
			}
		});
	}
}

// ----------------------------------------------------------------

public Set getNames() {

	synchronized(boxes) { return new HashSet(boxes.keySet()); }
}

// ----------------------------------------------------------------

public boolean isOnline() { return port != -1 && listenThread != null; }

// ----------------------------------------------------------------

public final void destroyAgent( String n ) {

	AgentBox box = (AgentBox) boxes.get(n);
	
	if( box == null ) return;
	
	synchronized(boxes)
	{
		box.agent.onDestruction();
		clm.dec( box.agent.getJob() );
		boxes.remove( n );
		fireAgentDestroyed( box.agent.getName() );
	}
	
	try { box.thread.join(5000); }
	catch( Exception e ) {}
	
	((Firewall)box.baseFirewall).close(); // cuts off agent
	// threads should be stopped but it is depretiated...
	// let's wait until they find out something else
}

// ----------------------------------------------------------------

public final IRequest dispatchAgent( String n, final Address to ) {
	
	if( to.name.equals(name) || to.isLocal() ) return
		new StaticRequest(StaticRequest.ERROR, new
		LaunchImpossibleException("Cannot dispatch to local address"));

	AgentBox box = (AgentBox) boxes.get(n);
	
	if( box == null ) return
		new StaticRequest(StaticRequest.ERROR, new
		LaunchImpossibleException("Agent is not in the base"));

	final IAgent a = box.agent;
	Logger.debug( getClass().getName()+"#dispatchAgent",
		"Destination: " + to.getHost());
	
	return (IRequest)
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				Thread t = new SendAgentThread( a, to, true );
				t.start();
				return (IRequest)t;
			}
		});
}

// ----------------------------------------------------------------

public IRequest fireMessage( final Message m ) {
		
	if( m == null || m.getRecipient() == null )
		return new StaticRequest(IRequest.ERROR,null);

	if( m.getRecipient().isLocal() )
	try
	{
		AgentBox box = null;
		boolean mHandled = false;
		
		if( ! name.equals(m.getRecipient().name) ) 
		{
			box = (AgentBox) boxes.get(m.getRecipient().name); 
			if( box != null && box.agent != null )
			{
				mHandled = box.agent.handleMessage( m,
					AgentInputStream.getObject(
						m.getBinary(), box.agent ) ); 
			}
			else Logger.error( getClass().getName()+"#fireMessage",
			   "No local agent " + m.getRecipient().name, null );
		}
		else mHandled = handleMessage( m, AgentInputStream.getObject(
				m.getBinary(), null ) );
		
		Map tmpMap = null;
		if( m.reply != null )
		{
			// simulating sending by cloning through serialization
			tmpMap = new HashMap();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( bos );
			oos.writeObject(m.reply);
			oos.flush();
			tmpMap.put("reply",
				AgentInputStream.getObject(
					bos.toByteArray(), m.reply )); 
		}
		return new StaticRequest(
			(mHandled ? IRequest.DONE : IRequest.ERROR),
			null, tmpMap );
	}
	catch( Exception e )
	{
		return new StaticRequest(IRequest.ERROR, e);
	}
	else
	{
		return (IRequest)
			AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				final Thread t = new SendMessageThread(m);
				AgentBox box = (AgentBox)
					boxes.get(m.getSender().name); 
				t.setContextClassLoader(
					box.agent.getClass().getClassLoader());
				t.start();
				return t;
			}
		});
	}
}
}

