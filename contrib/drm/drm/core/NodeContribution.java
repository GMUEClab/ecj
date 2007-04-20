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

import java.util.*;
import java.io.*;

/**
* Contains information about a base. It contains only public final
* fields and a constructor. The object is unmodifiable after
* constructing.
*/
public final class NodeContribution implements java.io.Serializable {
static final long serialVersionUID = -8492076971497844988L;


// =========== Private Fields ========================================
// ===================================================================


/** The information about the base. */
private transient Map descr;

/** A list of agent names who live on this base. */
private transient Set names;


// ============ private methods ======================================
// ===================================================================


private void writeObject( ObjectOutputStream out ) throws IOException {

	// names and descr is never null

	out.writeInt(descr.size());
	synchronized(descr)
	{
		Iterator i = descr.entrySet().iterator();
		while( i.hasNext() )
		{
			Map.Entry e = (Map.Entry)i.next();
			out.writeObject(e.getKey());
			out.writeObject(e.getValue());
		}
	}
	out.writeInt(names.size());
	synchronized(names)
	{
		Iterator i = names.iterator();
		while(i.hasNext()) out.writeObject(i.next());
	}
}

// ---------------------------------------------------------------

private void readObject( ObjectInputStream in )
throws IOException, ClassNotFoundException {

	Map tmpm = new Hashtable();
	Set tmps = new HashSet();
	
	int descrsize = in.readInt();
	for(int i=0; i<descrsize; ++i)
		tmpm.put( in.readObject(), in.readObject() );
	descr = Collections.unmodifiableMap(tmpm);
	
	int namessize = in.readInt();
	for(int i=0; i<namessize; ++i)
		tmps.add( in.readObject() );
	names = Collections.unmodifiableSet(tmps);
}
 
 
// =========== Public Constructors ===================================
// ===================================================================


/**
* Constructs a contribution. All parameters are (shallow) cloned, this object
* is thread safe.
* @param d Unspecified information about the base in a map, where the keys
* are strings. If null empty map is used.
* @param l The collection of agent names who lived on the base at creation
* time. element type is String. If null empty set is used.
*/
public NodeContribution( Map d, Set l ) {

	Map de=null;
	Set na=null;
	
	if( l==null ) na = Collections.EMPTY_SET;
	else na = new HashSet(l); // cloning
	if( d==null ) de = new Hashtable(); // EMPTY_MAP is only java 1.3
	else de = new Hashtable(d); // cloning
	
	names = Collections.unmodifiableSet(na);
	descr = Collections.unmodifiableMap(de);
}


// =========== Public methods ========================================
// ===================================================================

/**
* Returns description of node. Key type is String, value type depends
* on key.
*/
public Map getDescription() { return descr; }

// ---------------------------------------------------------------

/**
* Returns the agent names that were on the node when creating this.
* Element type is String.
*/
public Set getAgents() { return names; }

// ---------------------------------------------------------------

public String	toString() { return descr + " " + names; }


// !!!!! implementalni a writeObject readObject fveket
}

