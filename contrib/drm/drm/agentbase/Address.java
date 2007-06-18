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

import java.net.InetAddress;

/**
* This is for addressing all entities, ie agents and bases. 
* The main component is the name which is guaranteed not to be null
* or empty.
*/
public class Address implements java.io.Serializable {
private static final long serialVersionUID = 1L;


// =========== Package Fields ========================================
// ===================================================================


/** The host where the agent exists. */
InetAddress host = null;


// =========== Public Fields =========================================
// ===================================================================


/** The port on which the base is listening. */
public final int port;


/** The name of the agent. */
public final String name;


// =========== Public Constructors ===================================
// ===================================================================


/**
* Constructs an address for a local agent.
*/
public Address( String name ) {

	if( name == null || name.length()==0 ) throw new
		IllegalArgumentException("Name must not be empty in Address.");
	
	this.name = name;
	port = -1;
}

// -------------------------------------------------------------------

/**
* Constructs an address for a possibly remote agent or base.
* @throws IllegalArgumentException If name is null or empty, or if port is
* not positive when the host is not null.
*/
public Address( InetAddress host, int port, String name ) {

	if( name == null || name.length()==0 ) throw new
		IllegalArgumentException("Name must not be empty in Address.");
	if( port <= 0 && host!=null )  throw new
		IllegalArgumentException("Port must be positive in Address.");
	
	this.host = host;
	this.port = port;
	this.name = name;
}


// =========== Public Methods ========================================
// ===================================================================


public InetAddress getHost() { return host; }

// -------------------------------------------------------------------

/** 
* Returns true if this address does not contain a physical address,
* only a name.
*/
public boolean isLocal() { return host==null && port<=0; }


// =========== Public Object Implementations =========================
// ===================================================================


/**
* Must return true if and only if the object o is an Address
* and it addresses the same entity. That is if the name fields
* equal, since names are supposed to be unique. If both names
* are null returns true although it is an illegal state for a
* name to be null.
*/
public boolean equals( Object o ) {

	try
	{
		Address a = (Address)o;
		
		if( ( a.name == null && name != null ) ||
		    ( a.name != null && name == null ))
		    	return false;
		if( name == null ) return true;
		return a.name.equals(name);
	}
	catch( Exception e )
	{
		return false;
	}
}

// -------------------------------------------------------------------

/**
* Since we override Object.equals(), we need a new hashCode that is
* consistent with it.
*/
public int hashCode() {

	if( name != null ) return name.hashCode();
	else return 0;
}

// -------------------------------------------------------------------

public String	toString() {
	
	String result = "";
	if( !isLocal() )
	{
		if( host != null ) result = host.toString();
		else result = "<host not known>";
		result += ":";
		if( port != -1 ) result += ""+port;
		else result += "<no port known!>";
		result += ":";
	}
	if( name != null ) result += name;
	else result += "<no name known!>";
	return result;
}

}

