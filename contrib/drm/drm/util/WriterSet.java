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
import java.io.*;

/**
* A set which is always empty and the add method simply writes its
* argument to a given Writer, together with the current time in ms.
* Useful for debugging.
*/
public class WriterSet extends AbstractSet {

	Writer w = null;

	class EmptyIterator implements Iterator {
		
		public boolean hasNext() { return false; }
		
		public Object next()
		{
			throw new NoSuchElementException();
		}
		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	* @param w The writer to write to. It must be open, an this class
	* only writes to it, does not close it.
	*/
	public WriterSet( Writer w ) { this.w = w; }

	public Iterator iterator() { return new EmptyIterator(); }
	
	public int size() { return 0; }
	
	/**
	* Only writes the argument to the writer, and returns false.
	* Swallows exceptions, only writes a message to the stderr.
	*/
	public boolean add( Object o )
	{
		synchronized( w ) {
		try
		{
			w.write( "" +
			   System.currentTimeMillis() + ": " + o + "\n" );
			w.flush();
		}
		catch( Exception e )
		{
			System.err.println( "WriterSet: " + e );
		}
		return false;
		}
	}
}

