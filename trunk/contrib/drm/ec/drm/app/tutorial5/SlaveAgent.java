package ec.drm.app.tutorial5;

import drm.agentbase.Message;
import ec.drm.*;
import ec.*;

import java.util.*;

public class SlaveAgent extends EvolutionAgent{
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
	private ArrayList mailbox = new ArrayList();
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object o ) {
		if(!super.handleMessage(m,o)){
			if( m.getType().equals(MasterAgent.M_EVALUATE) ){
				output.message("EvaluatorData received from " + m.getSender().name);
				((EvaluatorData)o).sender = m.getSender();
				synchronized(mailbox){mailbox.add(o);}
			}else if( m.getType().equals(MasterAgent.M_END_EXPERIMENT) ){
				output.message("End experiment message received from " + m.getSender().name);
				shouldLive = false;
			}else return false;
		}
		return true;
	}

	
    public void run(){
    	setup(this, null);
    	startFresh(); // Never forget to call initializers if you plan to use population
    	
    	EvaluatorData evData;
    	
    	while(shouldLive){
    		if(mailbox.size() == 0){
    			//output.message("Waiting for individuals to evaluate...");
    			try{Thread.sleep(1000);}
    			catch(Exception e){}
    			continue;
    		}
    		synchronized(mailbox){evData = (EvaluatorData)mailbox.remove(0);}
    		if(evData.evaluated) continue; // It should warn
    		population.subpops[0].individuals = (Individual[])evData.individuals;
    		evaluator.evaluatePopulation(this);
    		evData.evaluated = true;
    		synchronized(evData){fireMessage(evData.sender,MasterAgent.M_EVALUATE, evData);}
    		output.message("Sending EvaluatorData to " + evData.sender.name);
    	}
    	output.message("All tasks finished.");
    	suicide();
    }
}