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

import java.util.Date;

/**
 * Stores relevant information to administrate agents on the base.
 */
class AgentBox implements java.io.Serializable {


// =========== Package Fields ========================================
// ===================================================================


/** Reference to the agent. */
IAgent agent;

/** The agents thread. */
transient Thread thread;

/** Time when the agent arrived on the base or was loaded. */
Date timeOfArrival;

/** The interface that was used to set the base for the agent */
IBase baseFirewall;

// =========== Package Constructors ==================================
// ===================================================================


/**
* Creates a new box.
*
* @param a Reference to the agent.
* @param t The agent's thread.
* @param time Date when the agent arrived or was loaded.
*/
AgentBox( IAgent a, Thread t, Date time, IBase fw ) {
	
	agent = a;
	thread = t;
	timeOfArrival = time;
	baseFirewall = fw;
}	

}

