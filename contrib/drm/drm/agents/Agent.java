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


package drm.agents;

import drm.agentbase.IAgent;
import drm.agentbase.IBase;
import drm.agentbase.Message;
import drm.agentbase.IRequest;
import drm.agentbase.StaticRequest;
import drm.agentbase.Address;
import drm.core.ContributionBox;
import drm.core.IDRM;
import drm.core.NodeContribution;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* Base class for agents. The class is abstract because the run() method
* has no default implementation here.
*
* <p>It gives the most basic implementation of an agent possible.
* The implementation contains the following functionality that can be used
* by extending classes:
* <ul>
* <li> Signs the state of the agent via the flag {@link #shouldLive}
* that must be used by extending classes
* to properly shut down any threads when this flag becomes false.
* </li>
* <li> It provides a couple of convenience functions that simplify sending
* messages via simpler interfaces.
* <li> It defines methods for debugging suitable for redefinition in extending
* classes. These are {@link #selfTest(PrintStream)} and
* {@link #getState()}. These functions can be invoked through messages
* as well, see {@link #handleMessage(Message,Object)} for more information.
* </li>
* <li> Access to the hosting environment is provided through the object
* {@link #base}, and through the function {@link #getDRM()}.
* </li>
* </ul>
*/
public abstract class Agent implements IAgent {
private static final long serialVersionUID = 833584182806685860L;

// =========== Protected Fields ======================================
// ===================================================================

/**
 * The Base that hosts the agent. An agent can only live on a base.
 */
protected transient IBase base = null;

/**
* If this flag is set the agent should stop executing.
* Every thread run by the agent should watch this flag unless it reimplements
* onDestruction().
* @see #onDestruction()
*/
protected volatile transient boolean shouldLive = true;

/**
 * This is the name of the agent.
 */
protected final String name;

/**
* This is the type identifyer of the agent.
*/
protected final String type;

/**
* This is the job name the agent participates in.
*/
protected final String job;

/**
* The drm version the agent is designed for.
* @see drm.agentbase.Base#RELEASE_VERSION
*/
protected static final int VERSION = 200000;


// =========== private methods =======================================
// ===================================================================


/**
* Generates a random name which is unique with a high probability.
* Used by the constructor if given name is null.
*/
private static String randomName() {
	return (""+Math.random()).substring(2,9);
}


// =========== Public Constructors ===================================
// ===================================================================


/**
* Constructs an Agent with the given name, jobname and type.
* The combination of these three things should be unique.
* The name that will be assigned to the agent (and later returned
* by <code>getName()</code>) is prefixed by type and job, in the format
* <code>type.job.name</code>, as required by the specification.
* @param type The type of the agent.
* @param job The job identifier.
* @param name The agent name. If null, a random name is generated.
*/
public Agent( String type, String job, String name ) {

	if( type == null || job == null ) throw new
		IllegalArgumentException("null argument in agent constructor");

	if( name == null ) name = randomName();
	
	this.name = type+"."+job+"."+name;
	this.job = job;
	this.type = type;
}


// =========== Public IAgent Implementations =========================
// ===================================================================


/**
* Extending classes that override this method must make sure that
* they call <code>super.onArrival</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* super.onArrival(from,to);
* </pre>
*/
public void onArrival( Address from, Address to ) { shouldLive = true; }

// -------------------------------------------------------------------

/**
* Extending classes that override this method must make sure that
* they call <code>super.onDestruction</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* super.onDestruction();
* </pre>
*/
public void onDestruction() { shouldLive = false; }

// -------------------------------------------------------------------

public final String getName() { return name; }
	
// -------------------------------------------------------------------

public final String getJob() { return job; }

// -------------------------------------------------------------------

public final String getType() { return type; }

// -------------------------------------------------------------------

public final void setBase( IBase b ) { base = b; }

// -------------------------------------------------------------------

/** Called to discover peers that participate in the same job.*/

public Address[] getPeerAddresses(){
	Iterator nodes = getDRM().getContributions().iterator();
	Iterator agents;
	List peers = new ArrayList();
	String name;
	ContributionBox cb;
	NodeContribution nc;
	
	while(nodes.hasNext()){
		cb = (ContributionBox)nodes.next();
		nc = (NodeContribution)cb.contribution;
		agents = nc.getAgents().iterator();
		while(agents.hasNext()){
			name = (String)agents.next();
			if(name.contains(job))
				peers.add(new Address(cb.contributor.getHost(),cb.contributor.port, name));
		}
	}
	if(peers.size() > 0)
		return (Address[])peers.toArray( new Address[ peers.size() ] );
	return new Address[0];
}

/**
* Handles incoming messages.
* The known message types are the following.
* <ul>
* <li><code>selfTest</code> Prints some debugging info on the standard error.
* </li>
* <li><code>getState</code> Returns the output of <code>getState()</code>.
* </li>
* </ul>
* <p>Extending classes that override this method must make sure that
* they call <code>super.handleMessage</code>.
* The suggested practice is that every implementation must begin with the
* line
* <pre>
* if( super.handleMessage(m,object) ) return true;
* </pre>
* and after that handling of the class specific messages.
*/
public boolean handleMessage( Message m, Object object ) {

	if( m.getType().equals("selfTest") )
	{
		selfTest( null );
	}
	else if( m.getType().equals("getState") )
	{
		m.setReply( getState() );
	}
	else
	{
		return false;
	}

	return true;
}

// -------------------------------------------------------------------

public final int version() { return VERSION; }


// =========== Public Convinience Functions ==========================
// ===================================================================


/**
* Creates a message and fires it.
* @param recipient Recipient of the message.
* @param type Type of the message.
* @param contstr The string content of the message.
* @param object Object to be wrapped in the message. If null, then
*       no object is wrapped, null is written.
* @return The request to track the status of the sending process.
* @see IBase#fireMessage(Message)
*/
public final IRequest fireMessage(Address recipient,String type,Object object){

	try
	{
		return base.fireMessage( new Message(
		new Address(getName()), recipient, type, object ) );
		//using a local address as sender is intentional,base fixes it
	}
	catch(IOException e)
	{
		return new StaticRequest(IRequest.ERROR,e);
	}
}

// -------------------------------------------------------------------

/**
* Creates a message and fires it to a local destination.
* @param recipient The name of the recipient of the message. It is
*       assumed to be a local agent.
* @param type Type of the message.
* @param contstr The string content of the message.
* @param object Object to be wrapped in the message. If null, then
*       no object is wrapped, null is written.
*/
public final IRequest fireMessage(String recipient,String type,Object object){
	
	try
	{
		return base.fireMessage( new Message( 
		new Address(getName()),new Address(recipient),type,object) );
	}
	catch(IOException e)
	{
		return new StaticRequest(IRequest.ERROR,e);
	}
}

// -------------------------------------------------------------------

/**
* Creates a message and fires it to a local destination. The content is empty.
* @param recipient Recipient of the message.
* @param type Type of the message.
*/
public final IRequest fireMessage( Address recipient, String type ) {
	
	IRequest r = null;
	
	try { r = fireMessage( recipient, type, null ); }
	catch( Exception e ) {} // never happens
	
	return r;
}

// -------------------------------------------------------------------

/**
* Removes the agent from the base. <b>Look out!</b> The method that calls this
* method will not exit, the thread will not be stopped as a result.
* It only calles <code>base.destroyAgent(name)</code>.
* So this method must be followed by exiting from the method explicitly
* e.g. with a <code>return</code>.
*/
public final void suicide() { base.destroyAgent(name); }

// -------------------------------------------------------------------

public void	selfTest( PrintStream output ) {

	if( output == null ) output = System.out;
	
	output.println( "serial version: " + serialVersionUID );
	output.println( "-- Agent name: " + getName() );
	output.println("-- job: " + job );
	output.println("-- type: " + type );
	output.println("-- shouldLive: " + shouldLive );
}

// -------------------------------------------------------------------

/**
* This returns a reference through which information about the hosting DRM
* can be requested.
* @throws ClassCastException If the IDRM interface is not implemented
* by the base proxy known by the agent.
*/
public final IDRM getDRM() { return (IDRM)base; }

// -------------------------------------------------------------------

/**
* This is the function that returns the reply to a message of type
* <code>"getState"</code>. This information that can be displayed
* by eg a user interface. It is useful when debugging. If an implementation
* wants to forbid answering getState requests it has to throw a runtime
* exception. The requestor will get no answer this way, it will seem as if
* the request was not understood. This is the default behaviour.
*/
public String getState() { throw new RuntimeException(); }


// =========== Public Object Implementations =========================
// ===================================================================


public String toString() { return name; }


}

