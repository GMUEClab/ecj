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


package drm.util;

import java.util.*;

/**
* Implements algorithms applicable for collections that contain
* Strings.
*
* @author mark
* @version 0.1
*/
public abstract class StringCollections {

// =========== Public Methods ========================================
// ===================================================================


/**
* Returns a new collection with the elements of c that have pref as prefix.
*/
public static Collection prefix( Collection c, String pref ) {

	Collection result = new Vector();
	synchronized( c )
	{
		Iterator i = c.iterator();
		while( i.hasNext() )
		{
			Object s = i.next();
			if( s instanceof String &&
			    ((String)s).startsWith(pref) ) result.add(s);
		}
	}
	return result;
}

// -------------------------------------------------------------------

/**
* Returns true if the given collection contains a string with a given
* prefix.
*/
public static boolean containsPrefix( Collection c, String pref ) {

	synchronized( c )
	{
		Iterator i = c.iterator();
		while( i.hasNext() )
		{
			Object s = i.next();
			if( s instanceof String &&
			    ((String)s).startsWith(pref) ) return true;
		}
	}
	return false;
}


}

