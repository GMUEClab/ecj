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

import java.util.StringTokenizer;

/**
* Contains a command to be executed on a Node.
* The commands database in the DRM collective contains objects of this type.
* It implements <code>equals</code> and <code>hashCode</code>
* to allow correct command management.
*/
public class NodeCommand implements java.io.Serializable {
static final long serialVersionUID = -3564369650116231150L;


// =============== package fields ==================================
// =================================================================


/** the byte code of the command */
byte com;

/** null if there are no parameters to the command. */
Object[] pars = null;

/** well, even if it is not unique (which is rather unlikely),
* together with the rest of the fields makes
* the command unique.
*/
long quiteUniqueID = System.currentTimeMillis();


// =============== public fields ===================================
// =================================================================


public static final byte CLEANALL = 0;


// =============== public constructors =============================
// =================================================================


/**
* Creates a command object by parsing the given string.
* Currently known commands are the following:
* <ul>
* <li><code>cleanall [tout]</code>: Cleans out the whole DRM collective,
* removing all agents from each node. Each node will spend the given
* timeout (in milliseconds) offline, making sure that each agent
* is killed. The longer this interval, the higher the chance that the DRM
* will be cleaned out. Default value is 10000 (=10 sec). This
* default is used if the given number is negative or cannot be parsed, or
* if no number is given.</li>
* </ul>
* @throws IllegalArgumentException if the given command cannot be parsed
* for any reason.
*/
public NodeCommand( String command ) {

	if( command == null ) throw new IllegalArgumentException(
		"parameter is null");
	StringTokenizer st = new StringTokenizer(command);
	if( st.countTokens() == 0 ) throw new IllegalArgumentException(
		"No command in given string \'"+command+"\'");

	String cname = st.nextToken();
	if( cname.equals("cleanall") )
	{
		com = CLEANALL;
		pars = new Object[] { new Long(10000) };
		if( st.hasMoreTokens() )
		{
			try { pars[0] = new Long(st.nextToken()); }
			catch( NumberFormatException e ) {}
			if( ((Long)pars[0]).longValue() < 0 )
				pars[0] = new Long(10000);
		}
	}
	else
	{
		throw new IllegalArgumentException(
			"Unknown command \'"+command+"\'");
	}
}


// =============== public methods ==================================
// =================================================================


/**
* Returns true if the object is a NodeCommand and describes the same
* command invocation event. That is, if the object describes the same
* command invoked by the same entity at the same time.
*/
public boolean equals( Object o ) {
	
	if( ! (o instanceof NodeCommand) ) return false;
	
	NodeCommand nc = (NodeCommand)o;
	
	if( nc.com!=com || nc.quiteUniqueID!=quiteUniqueID ) return false;
	
	if( nc.pars == null && pars == null ) return true;
	if( nc.pars == null || pars == null || pars.length != nc.pars.length)
		return false;
	
	for(int i=0; i<pars.length; ++i)
		if( ! nc.pars[i].equals(pars[i]) ) return false;

	return true;
}

// -----------------------------------------------------------------

/** hash consistent with equals */
public int hashCode() {

	return  com + (int)(quiteUniqueID^(quiteUniqueID>>>32));
}

// -----------------------------------------------------------------

/** Returns a string representation of the same format expected by the
* constructor.*/
public String toString() {

	String result = "";
	
	switch( com )
	{
		case CLEANALL:
			result = result + "cleanall ";
			break;
	}
	
	if( pars != null )
	for( int i=0; i<pars.length; ++i )
	{
		result = result + pars[i] + " ";
	}
	return result;
}

}

