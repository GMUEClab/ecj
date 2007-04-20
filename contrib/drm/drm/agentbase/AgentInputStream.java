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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
* Reads an object using a class loader manager if needed.
*/
public class AgentInputStream extends ObjectInputStream {


	// =========== Private Fields ========================================
	// ===================================================================
	
	
	/** Loads missing classes. */
	private ClassLoader classLoader = null; 
	
	
	// =========== Package Methods =======================================
	// ===================================================================
	
	/**
	* Extracts the wrapped object using the classloader of the agent.
	* If the agent is null uses actual classloader.
	* If this is not succesful, exceptions are thrown.
	* @param m Array where a serialized object might be found. If null null
	* is returned.
	* @param agent This object's classloader will be used if not null.
	*/
	public static Object getObject( byte[] m, Object agent )
	throws IOException, ClassNotFoundException {
	
		if( m == null ) return null;
		
		Object result = null;
		ClassLoader cl = null;
		if( agent != null ) cl = agent.getClass().getClassLoader();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(m);
		AgentInputStream mis = new AgentInputStream( bis, cl );
		result = mis.readObject();
	
		return result;
	}
	
	// -------------------------------------------------------------------
	
	/**
	* A possibility to change the classloader while reading an object stream.
	*/
	public void setClassLoader( ClassLoader cl ) { classLoader = cl; }
	
	
	// =========== Protected ObjectInputStream Implementations ===========
	// ===================================================================
	
	
	/**
	* This is called by the super class if a new class is read in. 
	* Resolve class uses the class manager to load the source
	* code of the class if it is not able to load it from the local system.
	*/
	protected Class resolveClass( ObjectStreamClass v ) 
	throws IOException, ClassNotFoundException {
		Class result = null;
		
		try { return super.resolveClass(v); }
		catch( ClassNotFoundException e ) {
			result = Class.forName( v.getName(), true, classLoader );
		}
		if(result == null) throw new ClassNotFoundException("No ClassLoader was found for " + v.getName());
		return result;
	}
	
	// =========== Public Constructors ===================================
	// ===================================================================
	
	
	/**
	* Creates a new AgentInputStream.
	* @param in The stream that this stream is connected to.
	* @param cl The class loader to use if the system class loader is not enough. 
	* @param clm The class loader manager to use if cl is null.
	*/
	public AgentInputStream(InputStream in, ClassLoader cl)
	throws IOException {
	
		super(in);
		classLoader = cl;
	}
}

