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

import drm.agentbase.IRequest;
import drm.agentbase.Address;

/**
* Defines functionality necessary to read information from a collective.
* Implementors of this interface do not contribute anyting
* but can follow what's going on.
* @see Collective
*/
public interface Observer {

	/**
	* Called when new contributions arrived. The parameter is the
	* contribution of the peer which sent the update.
	* Therefore it is the freshest contribution.
	*/
	void collectiveUpdated( ContributionBox peer );

	/**
	* Called when no peers are accessable or no peers are known.
	* Without peers to talk to the interface cannot fulfill its
	* duties.
	* This method should return a list of living (or possibly living)
	* peer addresses.
	*/
	Address[] getPeerAddresses();
	
	/**
	* The observer must provide this method to allow requesting
	* information.
	*/
	IRequest fireMessage( Address recipient, String type, Object content );

}

