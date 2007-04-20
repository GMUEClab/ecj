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


package drm.server;

import java.io.File;
import java.util.*;
import java.lang.reflect.*;
import drm.agentbase.JobClassLoader;
import drm.agentbase.Logger;
import drm.core.Node;

/** Loads jobs and runs them on a {@link Node}. */
public abstract class ScriptUtils {
	
/**
* Starts script on the given server. The syntax of
* <code>scriptName</code> is as follows:
* <code>filename[!packagename.classname]</code>
* Here <code>filename</code> can refer to a jar file or a directory.
* The given class is loaded. If the optional class  name is also given,
* then the class is loaded from that file <em>but only if the default classpath
* does not contain the given classname!</em>. Thus, take into account that
* first always the default
* classpath is tried and the jar file or directory is tried only after that.
* If the classes of the experiment can be found also in the default classpath,
* then launching will <em>not be succesful</em>.
* <p>
* The given class has to have a constructor with one {@link Node} parameter
* and it has to be <code>Runnable</code>.
* An instance of the class will be constructed with this constructor and then
* the class will be started in a thread.
* <p>
* The class is always loaded using a {@link JobClassLoader}.
* If the class name is not given, the classname <code>Launch</code> is tried
* (ie it is assumed that a class named <code>Launch</code> is included in the
* default package).
* 
* <p>
* Examples: <code>"exp.jar!exp.LaunchTest"</code> will start class
* <code>exp.LaunchTest</code>
* from jar <code>exp.jar</code>. <code>"expdir"</code> will launch class
* <code>Launch</code> from 
* directory <code>expdir</code>.
* The empty string <code>""</code> will start class Launch from the
* current directory.
* @throws ClassNotFoundException if the given script class cannot be loaded
* @throws NoSuchMethodException The standard constructor, that takes one Node
* as a parameter is missing from the given script class.
* @throws Exception if the class could not be instantiated or it could be
* loaded with the default classloader.
*/
public static void startScript(String scriptName, Node startOn )
throws ClassNotFoundException,
NoSuchMethodException, Exception {

	Logger.debug("ScriptUtils#startScript","Starting '"+scriptName+"'");
	
	Class result = null;
	String fileName = null;
	String className = "Launch";
	JobClassLoader jcl = null;
	
	int bound = scriptName.indexOf('!');
	if( bound != -1 )
	{
		fileName = scriptName.substring(0,bound);
		className = scriptName.substring(bound+1);
	}
	else
	{
		fileName = scriptName;
	}

	// construct new class loader and load class
	jcl = new JobClassLoader(new File(fileName));
	result = Class.forName(className,true,jcl);
	if( !(result.getClassLoader() instanceof JobClassLoader) )
		throw new Exception("class "+className+
			" should not be in class path");

	// call standard constructor
	Class pars[] = { startOn.getClass() };
	Constructor cons = result.getConstructor( pars );
	Object objpars[] = { startOn };
	Runnable script = (Runnable)cons.newInstance( objpars );
	(new Thread(script)).start();
}

// ---------------------------------------------------------------------

/**
* Starts scripts on the given node. It collects every scriptname from
* the given Properties object. The scriptnames are values of keys that
* start with the given prefix. After collecting the names it starts them
* sequentially one by one. The order of starting is determined by the
* alphabetical order of the corresponding keys. Uses
* {@link #startScript(String,Node) startScript} to start
* the scripts.
* @throws Exception if starting of any scripts resulted in an exception.
* The info message of the exception contains all thrown exceptions
* during the startup of all scripts.
*/
public static void startAllScripts(Properties p, String keyPrefix, 
					Node startOn ) throws Exception {

	StringBuffer errorLog = new StringBuffer();
	SortedSet keys = new TreeSet(p.keySet());
	Iterator i = keys.iterator();
	while( i.hasNext() )
	{
		try
		{
			String tmp = (String)i.next();
			if( tmp.startsWith(keyPrefix) )
				startScript( p.getProperty(tmp), startOn);
		}
		catch( Exception e )
		{
			errorLog.append(e).append("\n");
			Logger.error("ScriptUtils#startAllScripts","",e);
		}
	}

	if(errorLog.length()>0) throw new Exception( errorLog.toString() );
}

}

