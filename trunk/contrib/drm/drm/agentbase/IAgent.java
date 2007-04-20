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

import java.io.Serializable;

/**
* This interface defines the agent functionality required by the Base.
* To be able to live on a Base, an object has to implements this interface.
*
* <p>
* An agent has a name. This name has to conform to the rule
* that {@link #getType()}+"."+{@link #getJob()}+"." has to be a prefix
* of {@link #getName()}.
* The job name must be unique, ie two different jobs must
* not use the same name. Of course, one job can contain many agents.
*/
public interface IAgent extends Runnable, Serializable {

	/**
	* This is called before the agent is destroyed by the base.
	* This includes the case when the agent is dispatched. It is
	* called after the serialization of the agent being sent to
	* another base.
	* IMPORTANT: Every implementation must make sure that every thread of
	* the agent stops (if running) or doesn't start after the
	* invocation of this method.
	* After calling this method the base will deny all services to
	* the agent.
	*/
	void onDestruction();

	/**
	* This is called when the agent is put into a base.
	* At this time the base is already set using the {@link #setBase(IBase)}
	* method.
	* @param from The address of the base from which the agent was sent.
	* If the agent has never been on any base it is null.
	* @param to The address of the local base at the time of the arrival.
	* It is not guaranteed to remain valid trough the lifetime of the
	* agent. If the agent has never been on a base it is null.
	* @see #setBase(IBase)
	*/
	void onArrival( Address from, Address to );

	/**
	* Returns the name of the agent.
	*/
	String getName();

	/**
	* Returns job name. Every agent is part of a job. This returns
	* the name of that job.
	*/
	String getJob();

	/**
	* Returns type identifier. Apart from being part of a job,
	* every agent has a type. This is useful when having to make
	* a difference between agents within a job.
	*/
	String getType();
	
	/**
	* This is called by the base when the agent is received.
	* On one base it is called only once.
	* The implementation of the method should store this object so that
	* the agent can use the services offered by this interface.
	* @param b The Base that hosts the agent.
	*/
	void setBase( IBase b );
	
	/**
	* Called when the agent has a new message.
	* The agent can reply to the message by simply using the method
	* <code>m.setReply(Object)</code> in this method. The sender
	* of the message can read the reply through the IRequest interface
	* returned by fireMessage, via calling <code>getInfo("reply")</code>.
	* @param m The message to handle.
	* @param object The object that is wrapped in the message. If null
	* then there is no binary content or it is not a serialized object
	* (in which case it can be read using {@link Message#getBinary()}).
	* @return If the message could be handled succesfully (i.e. it was
	* known to the implementation and no errors occured)
	* returns true, otherwise false.
	* @see Message#setReply(Object)
	* @see IBase#fireMessage(Message)
	*/
	boolean	handleMessage( Message m, Object object );

	/**
	* Returns the version of the agent.
	* This version is the DRM version the agent was designed to run on.
	* Hosting the agent will be attempted in each case by the base anyway,
	* but it allows a finer error control.
	* @see Base#RELEASE_VERSION
	*/
	int version();
}


