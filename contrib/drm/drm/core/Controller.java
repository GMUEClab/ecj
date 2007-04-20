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

import java.util.Set;

/**
* Defines functionality necessary to write the command database of a
* collective. 
*/
public interface Controller {

	/**
	* The controller has to return the new commands it intends to
	* execute in the collective.
	* The type of the commands is collective-specific, but
	* command objects have to implement equals and also hashCode to
	* allow storage of commands in hashtables. It is also necessary
	* that every command is unique, ie it does not equal any
	* other command invoked at some other time (maybe by the same
	* user).
	*/
	Set getCommands();
}

