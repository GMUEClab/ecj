package ec.drm.app.tutorial5;

import java.util.*;

import ec.*;
import ec.drm.*;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;

import drm.agentbase.*;

public class MasterEvaluator extends SimpleEvaluator{
	private static final long serialVersionUID = 1L;
	
	private ArrayList mailbox = new ArrayList();
	
	public void receiveEvaluatorData(EvaluatorData evData){
		synchronized(mailbox){mailbox.add(evData);}
	}
	
    public void setup(final EvolutionState state, final Parameter base){
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMStatistics requires an  EvolutionAgent",null,null);
    	
        // Load my problem
        p_problem = (Problem)(state.parameters.getInstanceForParameter(
                                  base.push(P_PROBLEM),null,Problem.class));
        p_problem.setup(state,base.push(P_PROBLEM));
    }
    
    public void evaluatePopulation(final EvolutionState state){
    	//super.evaluatePopulation(state);
    	
    	MasterAgent agent = (MasterAgent) state;

    	EvaluatorData evData;
    	Individual[] inds = state.population.subpops[0].individuals;
    	int chunk_size = inds.length / agent.slaves.size();
    	Individual[] newinds = new Individual[chunk_size];
    	//Iterator slaves_iterator = agent.slaves.iterator();
    	Address target;
    	for(int i=0; i < agent.slaves.size(); i++){
    		//System.arraycopy(inds, 0, newinds, 0, chunk_size);
			for(int j=0; j < chunk_size; j++)
				newinds[j] = inds[j+i*chunk_size];
    		evData = new EvaluatorData(i,null,0,newinds,false);
			target = (Address)agent.slaves.get(i);
			agent.fireMessage(target,MasterAgent.M_EVALUATE, evData);
			state.output.message("Sending EvaluatorData to " + target.name);
    	}
		
		while(mailbox.size() < agent.slaves.size()){
			//state.output.message("Waiting for evaluated individuals to return...");
			Thread.yield();
			//try{Thread.sleep(1000);}
			//catch(Exception e){}
		}
		for(int i = 0; i < mailbox.size(); i++){
			evData = (EvaluatorData)mailbox.get(i);
			for(int j=0; j < chunk_size; j++)
				inds[j+chunk_size*evData.id] = evData.individuals[j];
			//System.arraycopy(evData.individuals, 0, inds, chunk_size*i, chunk_size);
		}
		mailbox.clear();
	}

    public void initializeContacts(EvolutionState state){}
}