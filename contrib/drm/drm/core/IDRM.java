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

import java.util.List;

/**
* Interface for getting information about the DRM collective.
* The object passed to the agents as a parameter of setBase
* implements not only IBase but also
* this interface if the agent lives on a Node.
* This way agents that live on a Node can get information
* about the DRM collective the Node participates in. 
*/
public interface IDRM {

	/**
	* Returns the contribution that belongs to the given node name.
	* If the contribution of the given node is not known, returns null.
	*/
	ContributionBox getContribution(String nodeName);

	/**
	* Returns the most up-to-date contribution. Returns null if the
	* node has never communicated with anyone.
	*/
	ContributionBox getNewestContribution();

	/**
	* Returns all known contributions in the DRM collective.
	* Dynamic type of objects is {@link ContributionBox}, and the
	* type of the contribution field is {@link NodeContribution}.
	* Returned list might be empty, but it's never null.
	* @see Collective#getContributions()
	*/
	List getContributions();
}

