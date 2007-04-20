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

import drm.agentbase.JobClassLoader;
import drm.agentbase.Logger;
import drm.core.Node;
import drm.util.ConfigProperties;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
* This is the most simple node possible.
* After reading the configuration file communication to it is possible
* only trough messaging.
* The system resource drm/resources/AddressList.properties is also appended
* to the configuration. It contains default addresses of bases that are
* likely to be part of the network.
* For details on configuration
* please refer to the <a href="doc-files/config.html">configuration manual</a>
* and also to the class {@link ConsoleLogger} which this application uses.
*/
public class NewNode {

// =================== public fields ================================
// ==================================================================

public volatile static Node s = null;

public static ConsoleLogger cl = new ConsoleLogger();

// ==================== private methods ==============================
// ==================================================================

private static void startScript(String scriptName, Node startOn, String[] args )
throws ClassNotFoundException,
NoSuchMethodException, Exception {

	Logger.debug("NewNode#startScript","Starting '"+scriptName+"'");
	
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
	Class pars[] = { startOn.getClass(), args.getClass() };
	Constructor cons = result.getConstructor( pars );
	Object objpars[] = { startOn, args };
	Runnable script = (Runnable)cons.newInstance( objpars );
	(new Thread(script)).start();
}


// ==================== public methods ==============================
// ==================================================================

/**
* @param args Command line parameters
* @see ConfigProperties#ConfigProperties(String[],String)
*/
public static void main( String[] args ) {

	Properties cfg = new Properties();
	
	String[] nodeargs = {};
	
	Logger.addListener( cl );
	cl.verbosity = 4;
	
	int port = 10101;
	
	String scriptName = null;
	
	Runtime.getRuntime().addShutdownHook( new Thread() {
		public void run() { if( s != null ) s.close(); }
	});

	int a = 0;
	while(a < args.length){
		if(args[a].equals("--verbosity") || args[a].equals("-v")){
			Logger.debug("NewNode", "verbosity " + args[a+1]);
			cfg.setProperty("verbosity",""+cl.verbosity);
			cl.verbosity = Integer.parseInt(args[a+1]);
			a = a + 2;
			continue;
		}
		if(args[a].equals("--port") || args[a].equals("-p")){
			Logger.debug("NewNode", "port " + args[a+1]);
			cfg.setProperty("port",""+Integer.parseInt(args[a+1]));
			port = Integer.parseInt(args[a+1]);
			a = a + 2;
			continue;
		}
		if(args[a].equals("--group") || args[a].equals("-g")){
			Logger.debug("NewNode", "group " + args[a+1]);
			cfg.setProperty("group",args[a+1]);
			a = a + 2;
			continue;
		}
		/* Not implemented until an adecuate interface to the MAX_CACHE_SIZE
		 * field in Collective class through Node class builder is implemented.
		 */
		if(args[a].equals("--cachesize") || args[a].equals("-c")){
			Logger.debug("NewNode", "cache size " + args[a+1]);
			cfg.setProperty("cachesize",""+Integer.parseInt(args[a+1]));
			a = a + 2;
			continue;
		}
		if(args[a].equals("--node") || args[a].equals("-n")){
			while(++a < args.length && !args[a].startsWith("-")){
				Logger.debug("NewNode", "node" + a + " " + args[a]);
				cfg.setProperty("node"+a,args[a]);
			}
			continue;
		}
		if(args[a].equals("--runClass") || args[a].equals("-r")){
			Logger.debug("NewNode", "runClass " + args[a+1]);
			cfg.setProperty("scriptName",args[a+1]);
			scriptName = args[a+1];
			a = a + 2;
			continue;
		}
		
		if(args[a].equals("--app") || args[a].equals("-a")){
			// The next arguments are passed to the node
			nodeargs = new String[args.length-a];
			System.arraycopy(args,a,nodeargs,0,args.length-a);
			Logger.debug("NewNode", "Passing " + nodeargs.length + " arguments to " + scriptName);
			break;
		}
		if(args[a].equals("--help") || args[a].equals("-h")){
			a++;
			System.out.println("drm.server.NewNode");
			System.out.println("-v, --verbosity: Verbosity level, 0 for no output, 4 for debugging.");
			System.out.println("-g, --group:     Group name");
			System.out.println("-p, --port:      Port");
			System.out.println("-c, --cachesize: Cache size (not yet implemented)");
			System.out.println("-n, --node:      List of nodes to connect to. Use IP:PORT, spaced by blanks");
			System.out.println("-r, --runClass:  Script to be run. Use jarfile.jar!package.class");
			System.out.println("-a, --app:       Parameters after this one will be passed to the script to be run.");
			System.exit(1);
		}
		Logger.error("NewNode","Unrecognized option " + args[a++] + "\nUse -h or --help for command options",null); 
	}
	
	// starting up node
	s = new Node( cfg );

	Logger.debug("NewNode", "Going online...");
	if( s.goOnline( port, port+10 ) < 0 )
	{
		Logger.panic( "NewNode", "Could not go online", null );
		s.close();
		System.exit(1);
	}
	
	// running classes given in config
	if(scriptName != null){
		try{ 
			// first wait 30 secs for connection to the network
			Thread.sleep(30000);
			if(nodeargs.length > 0) startScript( scriptName, s, nodeargs );
			else ScriptUtils.startScript(scriptName, s);
		}
		catch( Throwable t ){
		Logger.error( "NewNode",
			"problems with starting configured tasks", t );
		}
	}
}
}



