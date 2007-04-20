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


package drm.server;

import drm.agentbase.ILogListener;
import java.util.Date;

/**
* Loggs messages to the console. Debugging goes to the standard
* output, warnings and errors to the standard error.
*
* Every message is prefixed by a timestamp.
* The amount of information emitted is determined by the verbosity
* level ({@link #verbosity}).
* The behaviour of the logger as a function of this value is as follows.
* <ul>
* <li>0: nothing is printed</li>
* <li>1: only panic messages are printed (stderr)</li>
* <li>2: error messages are also printed (stderr)</li>
* <li>3: warning messages are also printed (stderr)</li>
* <li>4: debug messages are also printed (stdout).</li>
* <li>10 and above: the stack trace of exceptions is also printed if available
* (stderr).</li> 
* </ul>
*/
public class ConsoleLogger implements ILogListener {

public int verbosity = 10;


// ====================== ILogListener implementation ===============
// ==================================================================


public void handleLogMessage( int type, String sender, String comment,
				Throwable thr ) {

	final String s1 = new Date()+" "+(sender==null ? "" : sender);
	final String s2 = (comment==null ? "" : comment);
	final String s3 = (thr==null ? "" : thr.toString());
	
	switch(type)
	{
		case PANIC:
			if( verbosity > 0 )
			{
				System.err.print("PANIC! ");
				System.err.println(s1 + ": " + s2 + " " +s3);
				break;
			}
		case ERROR:
			if( verbosity > 1 )
			{
				System.err.print("ERROR! ");
				System.err.println(s1 + ": " + s2 + " " +s3);
				break;
			}
		case WARNING:
			if( verbosity > 2 )
			{
				System.err.print("WARNING! ");
				System.err.println(s1 + ": " + s2 + " " +s3);
				break;
			}
		case DEBUG:
		default:
			if( verbosity > 3 )
			{
				System.out.println(s1 + ": " + s2 + " " +s3);
				break;
			}
	}
	
	if( verbosity > 9 && thr != null ) thr.printStackTrace();
}
}


