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

/**
* Interface that should be implemented by handlers that are registered
* to handle messages received by Logger.
* @see Logger
*/
public interface ILogListener {

	/** A fatal error after which the jvm will exit. */
	final int PANIC = 0;

	/** An error occured but we continue running. */
	final int ERROR = 1;

	/** A strange or suspicious situation. */ 
	final int WARNING = 2;

	/** A debug message to see what's happening, for programmers. */
	final int DEBUG = 3;
	
	/** An info message to the public audience. */
	final int INFO = 4;

	/**
	* Reaction when diagnostic information is emitted by
	* an entity (the base or an agent).
	* @param type The type of the information. The constants defined
	* in this interface are used by the base.
	* @param sender Identifies the sender, e.g. the
	* name of the function that sends the message. Must not be null.
	* @param comment An optional explanatory message. Might be null.
	* @param thr An optional Throwable object that caused this event.
	* Might be null.
	*/
	void handleLogMessage( int type, String sender, String comment,
				Throwable thr );
}



