package ec.drm.app.tutorial4;

import ec.*;
import ec.drm.*;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;

public class MasterEvaluator extends SimpleEvaluator{
	private static final long serialVersionUID = 1L;
	
    public void setup(final EvolutionState state, final Parameter base){
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMStatistics requires an  EvolutionAgent",null,null);
    	
        // Load my problem
        p_problem = (Problem)(state.parameters.getInstanceForParameter(
                                  base.push(P_PROBLEM),null,Problem.class));
        p_problem.setup(state,base.push(P_PROBLEM));
    }
    
    public void evaluatePopulation(final EvolutionState state){
    	super.evaluatePopulation(state);
    	
    	MasterAgent agent = (MasterAgent) state;
		if(state.generation % 10 == 0){
			Individual[] inds = state.population.subpops[0].individuals;
			//Individual[] newinds = new Individual[inds.length];
			//System.arraycopy(inds, 0, newinds, 0, inds.length);
			agent.fireMessage(agent.slave,MasterAgent.M_EVALUATE_POP_CHUNK, inds);
		}
	}

    public void initializeContacts(EvolutionState state){}
}