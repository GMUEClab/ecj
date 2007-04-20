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

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
* This class represents a connection between two bases.
*/
class Connection {

// ================== public constants ==========================
// ==============================================================

public final int SOTIMEOUT;

public final ObjectOutputStream oos;

public final ObjectInputStream ois;

public final Socket socket;

public final int peer_version;

// ================= Object Implementations =====================
// ==============================================================

protected void finalize() { try{ close(); } catch( Exception e ) {} }

// ================= public constructors ========================
// ==============================================================

/**
* Initializes the public final fields for use in a communication session.
* On problem throws an exception. Otherwise the building of the connection
* is succesful.
* @param to The address to connect to. The name field is ignored here only
* the host and port is used to initialize the socket.
* @param group The group of the base that calls this constructor.
* @param type The type of connection to build.
* @param cl The class loader to load classes when deserialising objects
* from the incoming object input stream.
* @param tout Used for setting the socket's SO_TIMEOUT. It is also used
*  as a timeout value for the creation of the socket. 
*/
public Connection(Address to,String group,byte type,ClassLoader cl,int tout )
throws IOException, SocketException {

	boolean success = false;
try
{
	SOTIMEOUT = ( tout>=0 ? tout : 0 );
	ConnectSocket cs = new ConnectSocket( to.getHost(), to.port );
	cs.start();
	try{ cs.join(SOTIMEOUT); } catch( Exception e ) {}
	if( ! cs.success() )
	{
		if( cs.exc!=null ) throw cs.exc;
		else
		{
			cs.interrupt();
			throw new IOException("Socket creation to " + to +
				" timed out.");
		}
	}
	socket = cs.s;
	oos = new ObjectOutputStream(socket.getOutputStream());
	oos.writeInt( Base.PROTOCOL_VERSION );
	oos.writeObject( group );
	oos.writeByte( type );
	oos.flush();
	socket.setSoTimeout(SOTIMEOUT);
	ois = new AgentInputStream( socket.getInputStream(), cl );
	peer_version = ois.readInt();
	if( peer_version == Base.GROUP_MISMATCH ) throw new IOException(
		"Group mismatch with peer");
	if( peer_version == Base.PROTOCOL_MISMATCH ) throw new IOException(
	"Version mismatch with peer");
	
	success = true;
}
finally
{
	if( !success ) try { close(); } catch(Exception e) {}
}
}

// --------------------------------------------------------------

/**
* Initializes the public final fields for use in a communication session.
* On problem throws an exception. Uses half minute as SO_TIMEOUT.
* @param to The address to connect to.
* @param group The group of the base that calls this constructor.
* @param type The type of connection to build.
*/
public Connection( Address to, String group, byte type )
throws IOException, SocketException { this( to, group, type, null, 30000 ); }

// --------------------------------------------------------------

/**
* Initializes the public final fields for use in a communication session.
* On problem throws an exception. Uses half minute as SO_TIMEOUT.
* @param to The address to connect to.
* @param group The group of the base that calls this constructor.
* @param type The type of connection to build.
* @param cl The class manager to load classes when deserialising objects
* from the incoming object input stream.
*/
public Connection( Address to, String group, byte type, ClassLoader cl )
throws IOException, SocketException { this( to, group, type, cl, 30000 ); }

// --------------------------------------------------------------

/**
* Initializes the public final fields for use in a communication session.
* On problem throws an exception. Uses half minute as SO_TIMEOUT.
* @param to The address to connect to.
* @param group The group of the base that calls this constructor.
* @param type The type of connection to build.
* @param sotimeout Used for setting the socket's SO_TIMEOUT. It is also used
*  as a timeout value for the creation of the socket. 
*/
public Connection( Address to, String group, byte type, int sotimeout )
throws IOException, SocketException { this(to, group, type, null, sotimeout); }

// ================= public metods ==============================
// ==============================================================

/**
* Closes the connection, ie the streams and the socket.
* @throws IOException if any closing operation throws one
*/
public void close() throws IOException {

	if (ois != null) ois.close();
	if (oos != null) oos.close();
	if (socket != null) socket.close();
}

}


// ==============================================================
// ==============================================================


/**
* This class is needed to implement a decent timeout mechanism for
* creating a new socket to a given address.
*/
class ConnectSocket extends Thread {

	Socket s = null;

	IOException exc = null;

	final InetAddress h;

	final int p;

	public ConnectSocket( InetAddress h, int p ) {
		
		this.h = h;
		this.p = p;
	}
	
	public void run() {
	
		try { s = new Socket( h, p ); }
		catch( IOException e ) { exc = e; }
	}

	public boolean success() { return s!=null && exc==null; }
}

