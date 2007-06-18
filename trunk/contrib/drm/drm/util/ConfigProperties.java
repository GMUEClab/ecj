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


package drm.util;

import java.util.Properties;
import java.io.*;

/**
* Class for handling configuration files. Extends the functionality
* of Properties by handling files, system resources and command lines.
*/
public class ConfigProperties extends Properties {
private static final long serialVersionUID = 1L;

// =========== Public Constructors ===================================
// ===================================================================


/**
* Calls the Properties onstructor.
*/
public ConfigProperties() { super(); }

// -------------------------------------------------------------------

/**
* Constructs a ConfigProperty object from a parameter list.
* The algorithm is as follows: first <code>resource</code> is used to attempt
* loading default values from the give system resource.
* Then pars[0] is tried if it is an
* existing filename. If it is, reading properties from that file is
* attempted. Then (if pars[0] was a filename then from index 0 otherwise
* from index 1) pars is loaded as if it was a command line argument list
* using <code>loadCommandLineDefs</code>.
*
* A little inconvinience is that if pars[0] is supposed to be the first
* command line argument but it is a valid filename at the same time by
* accident. The caller must take care of that.
*
* No exceptions are thrown, instead error messages are written to the
* standard error. Users who want a finer control should use
* the public methods of this class.
*
* @param pars The (probably command line) parameter list.
* @param resource The name of the system resource that contains the
* defaults. null if there isn't any.
* 
*/
public	ConfigProperties( String[] pars, String resource ) {
	
	try
	{
		if( resource != null )
		{
			loadSystemResource(resource);
			System.err.println("ConfigProperties: System resource "
			+resource+" loaded.");
		}
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}
	
	if( pars == null || pars.length == 0 ) return;
	
	try
	{
		load( pars[0] );
		System.err.println(
			"ConfigProperties: File "+pars[0]+" loaded.");
		pars[0] = "";
	}
	catch( IOException e )
	{
		System.err.println("ConfigProperties: Failed loading '"+pars[0]
		+"' as a file, interpreting it as a property.");
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}

	if( pars.length==1 && pars[0].length()==0 ) return;
	
	try
	{
		loadCommandLineDefs( pars );
		System.err.println(
			"ConfigProperties: Command line defs loaded.");
	}
	catch( Exception e )
	{
		System.err.println("ConfigProperties: " + e );
	}
}

// -------------------------------------------------------------------

/**
* Constructs a ConfigProperty object by loading a file.
* @param fileName The name of the configuration file.
*/
public ConfigProperties( String fileName ) throws IOException {

	load( fileName );
}

// -------------------------------------------------------------------

/**
* Calls the Properties onstructor.
*/
public	ConfigProperties( Properties props ) {

	super( props );
}


// =========== Public methods ========================================
// ===================================================================


/**
* Loads given file. Calls <code>Properties.load</code> with a file
* input stream to the given file.
*/
public void load( String fileName ) throws IOException {

	FileInputStream fis = new FileInputStream( fileName );
	load( fis );
	fis.close();
}

// -------------------------------------------------------------------

/**
* Adds the properties from the given property file. Searches in the class path
* for the file with the given name.
*/
public void loadSystemResource( String n ) throws IOException {
	
	ClassLoader cl = getClass().getClassLoader();
	load( cl.getResourceAsStream( n ) );
}

// -------------------------------------------------------------------


/**
* Appends properties defined in the given command line arg list.
* Every string in the array is considered as a property file line.
* The strings are converted to byte arrays according to the
* default character encoding and then the properties are loaded by the
* <code>Properties.load</code> method. This means that the ISO-8859-1
* (or compatible) encoding is assumed.
*/
public void loadCommandLineDefs( String[] cl ) throws IOException {

	StringBuffer sb = new StringBuffer();
	for(int i=0; i<cl.length; ++i) sb.append( cl[i] ).append( "\n" );
	load( new ByteArrayInputStream(sb.toString().getBytes()) );
}
}

