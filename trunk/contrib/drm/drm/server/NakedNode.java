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

import drm.agentbase.Address;
import drm.agentbase.Logger;
import drm.core.Node;
import drm.util.ConfigProperties;
import java.util.Properties;
import java.util.Random;

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
public class NakedNode {

// =================== public fields ================================
// ==================================================================

public volatile static Node s = null;

public static ConfigProperties cfg = null;

public static ConsoleLogger cl = new ConsoleLogger();

// ==================== private methods =============================
// ==================================================================

/**
* Reads command line args and the default address list if the group
* is default.
*/
private static void loadcfg( String[] args ) {

	cfg = new ConfigProperties( args, null );
	
	if( cfg.getProperty( "group", "default" ).equals("default") )
	{
		try
		{
			cfg.loadSystemResource(
			"drm/resources/AddressList.properties" );
		}
		catch( Throwable e )
		{
			Logger.error("NakedNode","loading address list",e);
		}
	}
}

// ==================== public methods ==============================
// ==================================================================

/**
* @param args Passed as a parameter to ConfigProperties
* cosntructor.
* @see ConfigProperties#ConfigProperties(String[],String)
*/
public static void main( String[] args ) {

	Logger.addListener( cl );
	cl.verbosity = 3;
	
	loadcfg(args);
	
	cl.verbosity = Integer.parseInt(cfg.getProperty("verbosity","10"));
	
	Runtime.getRuntime().addShutdownHook( new Thread() {
		public void run() { if( s != null ) s.close(); }
	});

	// handling undocumented config par "startTime"
	long startTime = -1;
	try { startTime = Long.parseLong( cfg.getProperty("startTime","-1") ); }
	catch( Throwable t )
	{
		Logger.panic( "NakedNode", "config value startTime", t );
		System.exit(1);
	}
	if( startTime - System.currentTimeMillis() > 0 )
	{
		try
		{
			Logger.debug( "NakedNode","waiting until "+startTime );
			Thread.currentThread().sleep(
				startTime - System.currentTimeMillis());
		}
		catch( InterruptedException e ) {}
	}
	
	// starting up node
	s = new Node( cfg );
	int port = Integer.parseInt(cfg.getProperty("port","10101"));
	if( s.goOnline( port, port+10 ) < 0 )
	{
		Logger.panic( "NakedNode", "Could not go online", null );
		s.close();
		System.exit(1);
	}

	// handling undocumented config par "lifeTime"
	long lifeTime = -1;
	try { lifeTime = Long.parseLong( cfg.getProperty("lifeTime","-1") ); }
	catch( Throwable t )
	{
		Logger.panic( "NakedNode", "config value lifeTime", t );
		s.close();
		System.exit(1);
	}

	// handling undocumented config par "restartInterval"
	long intv = -1;
	try { intv=Long.parseLong(cfg.getProperty("restartInterval","-1"));}
	catch( Throwable t )
	{
		Logger.panic( "NakedNode", "config value restartInterval", t );
		s.close();
		System.exit(1);
	}
	
	PeriodicRestarter prs = new PeriodicRestarter(intv);
	prs.start();
	
	// running classes given in config
	try
	{ 
		// first wait 30 secs for connection to the network
		Thread.currentThread().sleep(30000);
		ScriptUtils.startAllScripts( cfg, "runClass", s );
	}
	catch( Throwable t )
	{
		Logger.error( "NakedNode",
			"problems with starting configured tasks", t );
	}
	
	if( lifeTime > 0 )
	{
		try
		{
			Thread.currentThread().sleep(lifeTime*1000);
			Logger.debug( "NakedNode",
				"Timeout "+lifeTime+"s elapsed, exiting");
		}
		catch( InterruptedException e ) {}
			
		s.close();
		System.exit(1);
	}
}
}


// ====================================================================
// ====================================================================


/**
* This class restarts <code>NakedNode.s</code> periodically.
* This feature is
* used in tests to model unreliablility, so this feature is
* not documented.
* It keeps restarting the node after a random time interval.
* This interval is a random variable with the distribution
* <code>N(i,i/2)</code> where <code>i</code> is the parameter of
* the constructor.
*/
class PeriodicRestarter extends Thread {

private long interval = 0;

/**
* @param interval The expected value of the restart interval in seconds.
* If not positive, the class does nothing.
*/
public PeriodicRestarter( long interval ) {
	
	super( "PeriodicRestarter("+interval+")" );
	this.interval = interval;
}

public void run() {

	if( NakedNode.s == null ) throw new IllegalArgumentException(
			"No node" );
	else if( interval<=0 ) return;
	
	Random rnd = new Random();
	while(true)
	{
		int d = (int)(interval + (interval*rnd.nextGaussian()/2));
		if( d <= 0 ) continue;
		try { sleep( d*1000 ); } catch( Exception e ) {}
		Logger.debug( getClass().getName(), "Timeout "+
			d+"s elapsed, restarting");
		NakedNode.s.close();
		NakedNode.s = new Node( NakedNode.cfg );
		int port = Integer.parseInt(
			NakedNode.cfg.getProperty("port","10101"));
		if( NakedNode.s.goOnline( port, port+10 ) < 0 )
		{
			Logger.panic( "NakedNode",
				"Could not go online", null );
			NakedNode.s.close();
			System.exit(1);
		}
	}
		
}
	
}


