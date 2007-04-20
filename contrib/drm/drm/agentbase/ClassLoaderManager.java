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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
* A manager for classloader of jobs running on a base. Every job has at most
* one associated classloader which is used to load classes from the jar file
* that contains the classes of the job.
* The job (and thus the classloader) is identified by the job name.
*/
final class ClassLoaderManager {


// ================= Private Fields ==================================
// ===================================================================


/** The base directory for storing jar files. */
private final File dir;

/**
* Stores class loaders. The key value is the class name of the (group of)
* agent(s) using the loader, which is stored in the value
*/
private Hashtable loaders = new Hashtable();

/**
* The thread that is run at shutdown time to clean up files that could not
* have been deleted yet.
*/ 
private final ShutdownCLMThread shutDown;


// ================== Package Methods =================================
// ===================================================================


/** Increments the number of users of the given loader. */
synchronized void inc( String name ) {
	
	ClassLoaderBox box = (ClassLoaderBox) loaders.get(name);
	if( box != null ) box.users++;
}

// -------------------------------------------------------------------

/** Decrements the number of users of the given loader. 
* Negative values are allowed too. If the counter becomes
* less than 1 then the time is noted and cleaning methods are free to
* delete the classloader after that.
*/
synchronized void dec( String name ) {

	ClassLoaderBox box = (ClassLoaderBox) loaders.get(name);
	if( box == null ) return;
	
	box.users--;
	if( box.users == 0 ) box.unusedFrom = System.currentTimeMillis();
}

// -------------------------------------------------------------------

/**
* Constructs a {@link JobClassLoader} and adds it to the known
* loaders under the given name.
* The stream can contain either a jar file, or a File object referring to
* a supposedly locally available directory.
* In case of a jar file, it will be saved to the temporary directory
* given to the constructor.
* @param name The name of this loader.
* @param is The stream from where the jar file arrives. It will not be closed
* by this method.
* @throws IOException If reading the input stream is not succesful, or
* if the stream contains a directory, and it is not available locally.
* @throws ClassNotFoundException if the input is a directory, and the File
* object representing it could not be loaded.
*/
void putLoader( String name, ObjectInputStream is )
throws IOException, ClassNotFoundException {

	File file = null;
	final int N = is.readInt();
	
	if( N==Base.SENDING_DIR )
	{
		file = (File)is.readObject();
		if( !file.canRead() ) throw new IOException(
			file+" is not readable");
	}
	else
	{
		file = File.createTempFile( "jar"+name, null );
		FileOutputStream fos = new FileOutputStream( file );
		Logger.debug( getClass().getName(), "saving " + file );
		final int buffsize = 1000;
		byte[] buff = new byte[ buffsize ];
		int nsum = 0;
		int n = is.read( buff, 0, Math.min( buffsize, N ));
		nsum = n;
		while( n != -1 )
		{
			fos.write(buff,0,n);
			if( nsum >= N ) break;
			n = is.read( buff, 0, Math.min( buffsize, N-nsum ));
			if( n != -1 ) nsum += n;
		}
		fos.close();
	}
	
	loaders.put(name,new ClassLoaderBox( new JobClassLoader(file), 0 ));
}

// -------------------------------------------------------------------

/**
* Returns the class loader of a given agent class name.
*/
ClassLoader getLoader( String name ) {

	ClassLoaderBox box = (ClassLoaderBox) loaders.get(name);
	if( box != null ) return box.cl;
	else return null;
}

// -------------------------------------------------------------------

/**
* Removes all loaders (together with used resources) that haven't had any users
* for the given number of ms-s.
*/
synchronized void cleanup( long timeout ) {

	long t = System.currentTimeMillis();
	
	synchronized(loaders)
	{
		Iterator i = loaders.entrySet().iterator();
		while( i.hasNext() )
		{
			Map.Entry e = (Map.Entry)i.next();
			ClassLoaderBox clb = (ClassLoaderBox)e.getValue();
			final File f = clb.cl.file; // just to make it shorter
			String name = (String)e.getKey();
			if( clb.users <= 0 && t-clb.unusedFrom > timeout )
			{
				Logger.debug( getClass().getName(),
					"scheduling removal of " + name +
					(f.isFile() ? " and "+f : "" ));
					
				if( f.isFile() ) shutDown.toDelete.add(f);
				i.remove();
			}
		}
	}
	shutDown.tryRemoval();
}


// ================= Package Constructors ============================
// ===================================================================


/**
* Create a new ClassLoaderManager.
* @param clDir the directory where the downloaded jar files are stored
* temporarily.
*/
ClassLoaderManager( File clDir ) { 

	dir = clDir.getAbsoluteFile();
	Logger.debug( getClass().getName(), "using: " + dir );

	shutDown = new ShutdownCLMThread();
	Runtime.getRuntime().addShutdownHook( shutDown );
}

}



// ===================================================================
// ===================================================================



/**
* Helper class for the ClassLoaderManager to wrap information on a given
* class loader.
*/
class ClassLoaderBox {

/** The class loader. */
JobClassLoader cl;

/** The number users (running agents) of this classloader. */
int users;

/** The date in ms when the classloader became unused. Meaningful if users=0.*/
long unusedFrom = 0;

/** Creates a new ClassBox. */
ClassLoaderBox( JobClassLoader cl, int users ) { 

	this.cl = cl;
	this.users = users;
	unusedFrom = System.currentTimeMillis();
}

}



// ===================================================================
// ===================================================================



/**
* Thread installed as a shutdown hook. It makes a final attempt to remove
* temporary files that are still there. It is better than deleteOnExit,
* because it is run even when the JVM is terminated with a signal, at
* OS shutdown for example.
*/
class ShutdownCLMThread extends Thread {

Vector toDelete = new Vector();

void tryRemoval() {
	
	synchronized(toDelete)
	{
		Iterator i = toDelete.iterator();
		while( i.hasNext() )
		{
			File f = (File)i.next();
			if( !f.exists() || f.delete() ) i.remove();
			else Logger.warning( getClass().getName(),
				"Still couldn't delete " + f, null );
		}
	}
}

public void run() {

	Logger.debug(getClass().getName(), "Entering shutdown hook." );
	tryRemoval();
}

}

