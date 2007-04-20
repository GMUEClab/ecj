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

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.net.MalformedURLException;

/**
* Every agent that lives on a {@link Base} must have a classloader of
* this type.
*/
public class JobClassLoader extends URLClassLoader {


// ================= public fields ==================================
// ==================================================================


/** The absolute form of the file given to the constructor. */
public final File file;


// ================== public constructors ===========================
// ==================================================================


/**
* Creates a job classloader using the given file. The file
* can be a jar file or a directory. The absolute form
* of the filename is used, converted to an URL.
* The default classloader is the system classloader..
* @throws IllegalArgumentException if the argument is null or
* not readable, or cannot be converted to an URL.
*/
public JobClassLoader( File f ) {

	super(new URL[]{});

	if( f == null ) throw new IllegalArgumentException(
		"argument mustn't be null");
	
	file = f.getAbsoluteFile();
	
	if( !file.canRead() ) throw new
		IllegalArgumentException("'"+file+"' is not readable");

	try { addURL(file.getAbsoluteFile().toURL()); }
	catch( MalformedURLException e )
	{
		throw new IllegalArgumentException(
			"'"+file+"' cannot be converted to an URL");
	}
}


// ================== public methods ================================
// ==================================================================


public String toString() {

	return "JobClassLoader "+" "+file;
}
}

