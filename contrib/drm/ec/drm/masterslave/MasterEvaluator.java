/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.masterslave;

import java.util.*;

import ec.*;
import ec.drm.*;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;

import drm.agentbase.*;

/** Evaluator who sends Individuals for evaluation to SlaveAgents and recovers them later */
public class MasterEvaluator extends SimpleEvaluator{
	private static final long serialVersionUID = 1L;
	
	private List mailbox = Collections.synchronizedList(new ArrayList());
	private List ready_slaves = Collections.synchronizedList(new ArrayList());
	
	public int chunk_size;
	
	public static final String P_CHUNK_SIZE = "chunk-size";
	
	public void receiveEvaluatorData(EvaluatorData evData){
		mailbox.add(evData);
	}
	
	public void receiveReadySlave(Address slave){
		ready_slaves.add(slave);
	}
	
    public void setup(final EvolutionState state, final Parameter base){
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMStatistics requires an  EvolutionAgent",null,null);
    	
        // Load my problem
        p_problem = (Problem)(state.parameters.getInstanceForParameter(
                                  base.push(P_PROBLEM),null,Problem.class));
        p_problem.setup(state,base.push(P_PROBLEM));
        
        chunk_size = state.parameters.getIntWithDefault(base.push(P_CHUNK_SIZE),null,1024);
    }
    
    public void evaluatePopulation(final EvolutionState state){
    	MasterAgent agent = (MasterAgent) state;

    	EvaluatorData evData;
    	Address target;
    	Individual[] inds = state.population.subpops[0].individuals;
    	int number_of_chunks = (int)Math.ceil((double)inds.length / (double)chunk_size);
    	int number_of_remaining_chunks = number_of_chunks;
    	int current_chunk = 0;
    	boolean[] returned_chunks = new boolean[number_of_chunks];
    	Arrays.fill(returned_chunks, false);
    	
    	//mailbox.clear(); // Not really needed, datachuncks from previous generations will be discarded later
    	
    	while(number_of_remaining_chunks > 0){
    		if(mailbox.size() > 0){
    			long t0 = System.currentTimeMillis();
    			evData = (EvaluatorData)mailbox.remove(0);
    			
    			if(returned_chunks[evData.id]) continue;
    			if(evData.generation != state.generation) continue;
    			
    			for(int i=0; i < evData.individuals.length; i++)
    				inds[evData.id*chunk_size+i] = evData.individuals[i];
    			returned_chunks[evData.id] = true;
    			number_of_remaining_chunks--;
    			agent.output.message("Importing time: " + (System.currentTimeMillis()-t0)/1000.0 + " s");
    			continue;
    		}
    		
    		if(ready_slaves.size() > 0) {
    			if(!returned_chunks[current_chunk]){
    				target = (Address)ready_slaves.remove(0);
    				Individual[] newinds = new Individual[Math.min(chunk_size,inds.length - current_chunk*chunk_size)];
    				for(int i=0; i < newinds.length; i++)
    					newinds[i] = inds[current_chunk*chunk_size+i];
    	    		evData = new EvaluatorData(current_chunk,state.generation,null,0,newinds,false);
    				
    	    		long t0 = System.currentTimeMillis();
    	    		/*IRequest request = */agent.fireMessage(target,MasterAgent.M_EVALUATE, evData);
    				/*while(request.getStatus() == IRequest.WAITING)
    					try{Thread.sleep(1000);}
    					catch(InterruptedException e){}
    				if(request.getStatus() != IRequest.DONE)
    					ready_slaves.add(target);*/ // If the message fails we will simply try again later
    				
    				agent.output.message(evData.individuals.length + " individuals exported to " + target.name);
    				agent.output.message("Sending time: " + (System.currentTimeMillis()-t0)/1000.0 + " s");
    			}
				current_chunk = (current_chunk + 1) % number_of_chunks;
    		}else{
    			//try{Thread.sleep(1000);}
    			//catch(Exception e){}
    			//continue;
    			Thread.yield();
    		}
    	}
	}

    public void initializeContacts(EvolutionState state){}
}