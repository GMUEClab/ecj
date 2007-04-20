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

import java.util.Map;

/**
* A convinience class to return requests that are actually
* not assyncronous to conform to the interface.
*/
public class StaticRequest implements IRequest {

private final int status;

private final Throwable thr;

private final long startTime;

private final Map info;

// --------------------------------------------------------

public int getStatus() { return status; }

public Throwable getThrowable() { return thr; }

public long getStartTime() { return startTime; }

public Object getInfo( String q ) {

	if( info == null || q == null ) return null;
	else return info.get(q);
}

// --------------------------------------------------------

/**
* Constructs a constant request. It will always return status <code>s</code>
* and throwable <code>t</code>. The map <code>m</code> is used to simulate
* <code>getInfo(String)</code>: getInfo always returns the object
* this map maps the given string to.
*/
public StaticRequest( int s, Throwable t, Map m ) {
	
	startTime = System.currentTimeMillis();
	thr = t;
	status = s;
	info = m;
}

/**
* Constructs a constant request. It will always return status <code>s</code>
* and throwable <code>t</code>. getInfo will always return null.
*/
public StaticRequest( int s, Throwable t ) { this( s, t, null ); }

}


