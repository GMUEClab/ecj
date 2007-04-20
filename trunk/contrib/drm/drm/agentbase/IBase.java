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

import java.util.Set;

/**
* This is the functionality the base offers to its agents.
*/
public interface IBase {

	/**
	* Retuns a base property. Properties are mostly set and documented
	* at construction time of implementor classes 
	* (see also the constructors of the extending classes).
	* The following properties must be defined in every case:
	* <ul>
	* <li><code>drm.baseName</code> The name of the base.</li>
	* </ul>
	* @param prop Name of the property.
	* @return As in java.util.Properties.
	* @see Base#Base(java.util.Properties)
	*/
	String getProperty( String prop );

	/**
	* Gets the names of all agents on this base. Returns a newly
	* allocated Set of Strings. This is the only solution that is
	* guaranteed to be thread safe.
	*/
	Set getNames();

	/**
	* Destroys an agent with all necessary clean up. The method
	* {@link IAgent#onDestruction()} will
	* be called before removing the agent from the base. The agent has to
	* stop all its threads (java offers no possibility at the moment
	* (version 1.4) to stop threads). The agent will be cut off of the
	* outside world, even if it fails to stop its threads.
	* @param name Name of the agent.
	*/
	void destroyAgent( String name );

	/**
	* Dispatches an agent with all necessary clean up. The succesful
	* operation means sending the agent to the address, then destroying
	* the local copy. Permission to send an agent implies destroying it
	* so it is not possible that sending is succesful but destruction is
	* not.
	* If succesful then the local agent is destroyed after sending by
	* calling <code>destroyAgent</code>.
	* The operation is assynchronous.
	* @param name Name of the agent.
	* @param destination Where to send the agent.
	* @return Returns a request so that the user can check the status
	* of the operation.
	* @see #destroyAgent(String)
	*/
	IRequest dispatchAgent( String name, Address destination );

	/**
	* Launches an agent to a specified destination.
	* It is assumed that the agent to be launched is not in the base.
	* The operation is assynchronous.
	* @param method The algorithm used for launching the agent.
	* At least "DIRECT" has to be implemented. Other algorithms have to
	* be documented in the implementing class.
	* @param agent Agent to launch.
	* @param parameter An arbitrary parameter (list) of the selected
	* launching algorithm. In the case of "DIRECT" the address of the
	* recipient base of type Address.
	* If null then the local base is the destination.
	* @return Returns a request so that the user can check the status
	* of the operation.
	* @throws IllegalArgumentException if the arguments are not of the
	* right type or inconsistent.
	*/
	IRequest launch( String method, IAgent agent, Object parameter );
	
	/**
	* Fires the given message. The recipient can be local or global.
	* From the sender address only the name is used the rest is filled
	* in by the base and the recipient base. The message is sent by a
	* separate thread in case of a remote destination. 
	* @return Returns a request so that the user can check the status
	* of the operation. The method call
	* <code>IRequest.getInfo("reply")</code> returns the reply object to
	* the message (after it arrives of course).
	* @see IRequest#getInfo(String)
	*/
	IRequest fireMessage( Message m );
	
	/** Returns the online status of the base. If the base is not online,
	* communication with the outside world is not possible. */
	boolean isOnline();
}



