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


package drm.core;

import drm.agentbase.Address;

/**
* This class contains a contribution of an entity. The actual type of the
* contribution object (the data model) is application specific.
*/
public final class ContributionBox implements java.io.Serializable {
static final long serialVersionUID = 258694804122401664L;

// ============== package fields ===============================
// =============================================================

long timeStamp;

// ============== public final fields ==========================
// =============================================================

public final Address contributor;

public final Object contribution;

// ============== package constructor ==========================
// =============================================================

ContributionBox( Address a, long t, Object o ) {

	if( a == null ) throw new IllegalArgumentException(
		"Address must not be null");
	
	contributor = a;
	timeStamp = t;
	contribution = o;
}

// ============== public constructor ===========================
// =============================================================

/**
* Initialises the final fields. The timeStamp is initialised to the current
* time in milliseconds.
*/
public ContributionBox( Address a, Object o ) {

	this( a, System.currentTimeMillis(), o );
}

// ============= public methods ================================
// =============================================================

public long timeStamp() { return timeStamp; }

// ---------------------------------------------------------------

public String toString() {return contributor+" "+timeStamp+" "+contribution;}

}

