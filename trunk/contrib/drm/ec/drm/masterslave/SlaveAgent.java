/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.masterslave;

import drm.agentbase.Message;
import ec.drm.*;
import ec.*;

import java.util.*;

/** Agent which only evaluates Individuals. These must arrive inside a 
 * EvaluatorData in a drm message */
public class SlaveAgent extends EvolutionAgent{
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
	private List mailbox = Collections.synchronizedList(new ArrayList());
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object o ) {
		if(!super.handleMessage(m,o)){
			if( m.getType().equals(MasterAgent.M_EVALUATE) ){
				output.message("EvaluatorData received from " + m.getSender().name);
				((EvaluatorData)o).sender = m.getSender();
				mailbox.add(o);
			}else if( m.getType().equals(MasterAgent.M_END_EXPERIMENT) ){
				output.message("End experiment message received from " + m.getSender().name);
				shouldLive = false;
			}else return false;
		}
		return true;
	}

	
    public void run(){
    	setup(this, null); // Always should be the first action of an EvolutionAgent, setups everything
    	startFresh(); // Never forget to call initializers if you plan to use population
    	
    	fireMessage(root,MasterAgent.M_READY_SLAVE, null);
    	EvaluatorData evData;
    	boolean warned = false;
    	
    	while(shouldLive){ // suicide() sets this to false
    		while(mailbox.size() == 0){
    			fireMessage(root,MasterAgent.M_READY_SLAVE, null);
    			if(!warned){
    				output.message("Waiting for individuals to evaluate...");
    				warned = true;
    			}
    			try{Thread.sleep(1000);}
    			catch(Exception e){output.error("Exception: " + e.getMessage());}
    		}
    		warned = false;

    		evData = (EvaluatorData)mailbox.remove(0);
    		output.message(evData.individuals.length + " individuals imported for generation " + evData.generation);
    		if(evData.evaluated) continue; // It should warn
    		population.subpops[0].individuals = (Individual[])evData.individuals;
    		evaluator.evaluatePopulation(this);

    		output.message("Sending EvaluatorData to " + evData.sender.name);
    		fireMessage(evData.sender,MasterAgent.M_EVALUATE, evData); // I think that there is no need to synchronize evData
    	}
    	output.message("All tasks finished.");
    	suicide();
    }
}