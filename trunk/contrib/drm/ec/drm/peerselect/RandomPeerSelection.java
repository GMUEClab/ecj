/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.peerselect;

import drm.agentbase.Address;
import ec.EvolutionState;
import ec.select.SelectDefaults;
import ec.util.Parameter;
    
public class RandomPeerSelection extends PeerSelectionMethod{
	/** Serialization identificator */
	private static final long serialVersionUID = 833584182806685870L;
    	
	/** default base */
	public static final String P_RANDOM_PEER = "random";
    	
	public Parameter defaultBase(){
		return SelectDefaults.base().push(P_RANDOM_PEER);
	}
    	
	public void setup(final EvolutionState state, Parameter base){};
    	
	public Address select(final EvolutionState state, Address[] knownPeers){
		if(knownPeers.length == 0) return null;
		return knownPeers[state.random[0].nextInt(knownPeers.length)]; //Where should I guess which random to use?
	}
}