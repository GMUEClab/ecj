package ec.drm.app.tutorial8;


import drm.agentbase.*;
import ec.util.*;
import ec.*;
import ec.drm.*;

public class MyStatistics extends DRMStatistics {

	private static final long serialVersionUID = 1L;
	
    /** This one checks that the stats message was received. If not, the message is 
     * sent again up to five times. Run time an best individual of run are logged. */
    public void finalStatistics(final EvolutionState state, final int result){
    	for(int x=0;x<children.length;x++)
            children[x].finalStatistics(state, result);
        
        EvolutionAgent agent = (EvolutionAgent)state;
        
        Individual best_inds[] = getBestIndividual(state);
        Individual test_inds[] = new Individual[best_inds.length];
        for(int i=0; i<best_inds.length; i++){
        	test_inds[i] = (Individual)best_inds[i].clone();
        	((MultiValuedRegression)(state.evaluator.p_problem)).describe(
        			test_inds[i], state, 0, defaultlog, Output.V_NO_GENERAL);
        }
        
        MyStatisticsData data = new MyStatisticsData(
        		new Address(agent.getName()),
        		state.generation,
        		System.currentTimeMillis() - creationtime,
        		best_inds,
        		new Individual[0],
        		test_inds);
    	
        if(agent.iamroot)	// Local logging
        	printStatistics(state, data); // Every statistic will go there
        else{				// DRM logging
        	for (int i=0; i<5;i++){ // Try to send final data 5 times
	    		IRequest request = agent.fireMessage(agent.getRootAddress(),EvolutionAgent.M_STATS,data);
	    		while(request.getStatus() == IRequest.WAITING){
	    			try{Thread.sleep(1000);}
	    			catch(Exception e){state.output.error("Exception: " + e);}
	    		}
	    		if(request.getStatus() == IRequest.DONE){break;}
	    		else{
	    			state.output.error("There was an error sending final statistics.");
	    			try{Thread.sleep(1000*i^2);}
	    			catch(Exception e){state.output.error("Exception: " + e);}
	    		}
            }
        }
    }
}
