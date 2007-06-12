package ec.drm.app.tutorial4;

import drm.agentbase.Message;
import ec.drm.*;
import ec.*;

public class SlaveAgent extends EvolutionAgent{
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object o ) {
		if(!super.handleMessage(m,o)){
			if( m.getType().equals(MasterAgent.M_EVALUATE_POP_CHUNK) ){
				output.message("A chunk arrived from " + m.getSender().name);
				//Individual[] inds =(Individual[])o;
				//Individual[] newinds = new Individual[inds.length];
				//System.arraycopy(inds, 0, newinds, 0, inds.length);
				//System.arraycopy(inds, 0, population.subpops[0].individuals, 0, inds.length);
				population.subpops[0].individuals = (Individual[])o;
	    		evaluator.evaluatePopulation(this);
	    		statistics.postEvaluationStatistics(this);
			}else return false;
		}
		return true;
	}

	
    public void run(){
    	setup(this, null);
    	startFresh(); // Never forget to call initializers if you plan to use population
    }
}