/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.peerselect;

import drm.agentbase.Address;
import ec.EvolutionState;
import ec.Prototype;
    
public abstract class PeerSelectionMethod implements Prototype{
	/** Serialization identificator */
	private static final long serialVersionUID = 833584182806685869L;  	

	public Address select(final EvolutionState state, Address[] knownPeers){
		return null;
	}
    	
    public Object clone(){
    try { return super.clone(); }
    catch (CloneNotSupportedException e) 
        { throw new InternalError(); } // never happens
    }
}