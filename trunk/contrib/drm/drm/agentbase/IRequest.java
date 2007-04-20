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

/** Interface to handle assync requests. */
public interface IRequest {

	/** To sign waiting status. */
	int WAITING = 0;
	
	/** To sign ready status. */
	int DONE = 1;
	
	/** To sign error status. */
	int ERROR = 2;

	/** Returns the status. Either WAITING, DONE or ERROR. */
	int getStatus();

	/** Returns the starting date in the format returned by
	* <code>System.currentTimeMillis()</code>
	*/
	long getStartTime();

	/**
	* If the status is ERROR and the problem was catching a throwable
	* object then returns the corresponding
	* throwable object. If the status is not ERROR
	* or the error was not due to a Throwable then returns null.
	*/
	Throwable getThrowable();

	/**
	* Implementations can use this function to return additional
	* information about the request.
	* @param q the string id of the requested information
	* @return the requested info or null if query is not known
	*/
	Object getInfo( String q );
}

