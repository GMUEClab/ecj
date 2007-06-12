/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.peerselect;

import drm.agentbase.Address;
import ec.EvolutionState;
import ec.drm.EvolutionAgent;
import ec.select.SelectDefaults;
import ec.util.Parameter;
    
public class RingPeerSelection extends PeerSelectionMethod{
	/** Serialization identificator */
	private static final long serialVersionUID = 833584182806685871L;
    
	/** Name of the hosting agent. */
	private String name;
	
	/** default base */
	public static final String P_RING_PEER = "ring";
    	
	public Parameter defaultBase(){
		return SelectDefaults.base().push(P_RING_PEER);
	}

	public void setup(final EvolutionState state, Parameter base){
    	if (state.job == null || state.job.length != 1 || state.job[0] == null ||
    		    !(state.job[0] instanceof EvolutionAgent))
    		state.output.fatal("RingPeerSelection requires an  EvolutionAgent",null,null);
    	name = ((EvolutionAgent)state.job[0]).getName();
	};
    
	/** RingPeerSelection depends on the order of the contributions in the collective, I don't know yet if this
	 * order changes through time, if that is the case I would change this to use the agent names. */
	public Address select(final EvolutionState state, Address[] knownPeers){
		for(int i=0; i<knownPeers.length; i++){
			if(knownPeers[i].name.equals(name)) 
				return knownPeers[(i+1)%knownPeers.length];
		}
		return null;
	}
}