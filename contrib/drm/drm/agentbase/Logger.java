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

import java.util.Vector;

/**
* Static class that serves as an entry point for logging information.
* It is a part of the protocol specification so eg any compliant agent
* is allowed to use this class.
* <p>This class only forwards messages to registered handlers.
* Any functionality like storing, searching, etc of messages must be
* implemented by handlers if necessary.
* The handlers can be registered and removed.
* By default there are no listeners.
* @see ILogListener
*/
public abstract class Logger {


// ==================== Private Fields ===========================
// ===============================================================


/** The log message handlers. */
private static Vector listeners = new Vector();


// ==================== Private Constructors =====================
//================================================================


/** 
* This makes it impossible to extend the class since there are no more
* constructors and since it is abstract
* it makes it impossible to construct an object. Thus we have a completely
* static and final class.
*/
private Logger() {}


// ==================== Public Static Methods ====================
// ===============================================================


public static synchronized void addListener( ILogListener l ) {

	if( listeners.contains(l) ) return;
	listeners.addElement(l);
}

// ----------------------------------------------------------------

public static synchronized void removeListener( ILogListener l ) {
	
	listeners.removeElement(l);
}

// ----------------------------------------------------------------

/**
* Notfies listeners that log information was emitted.
* @param type The kind of the logged message.
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
* @param thr An optional Throwable object that caused this event.
*/
public static void log( int type, String sender, String comment,
							Throwable thr ) {

	synchronized( listeners )
	{
		for (int i = 0; i < listeners.size(); ++i )
		{
			((ILogListener)
			listeners.elementAt(i)).handleLogMessage(
				type, sender, comment, thr );
		}
	}
}

// ---------------------------------------------------------------

/**
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
* @param thr An optional Throwable object that caused this event.
*/
public static void panic( String sender, String comment, Throwable thr ) {

	log( ILogListener.PANIC, sender, comment, thr );
}

// ---------------------------------------------------------------
	
/**
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
* @param thr An optional Throwable object that caused this event.
*/
public static void error( String sender, String comment, Throwable thr ) {

	log( ILogListener.ERROR, sender, comment, thr );
}

// ---------------------------------------------------------------
	
/**
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
* @param thr An optional Throwable object that caused this event.
*/
public static void warning( String sender, String comment, Throwable thr ) {

	log( ILogListener.WARNING, sender, comment, thr );
}

// ---------------------------------------------------------------

/**
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
*/
public static void debug( String sender, String comment ) {

	log( ILogListener.DEBUG, sender, comment, null );
}

// ---------------------------------------------------------------

/**
* @param sender An optional string that identifies the sender, e.g. the
* name of the function that sends the message.
* @param comment An optional explanatory message.
*/
public static void info( String sender, String comment ) {

	log( ILogListener.INFO, sender, comment, null );
}

}



