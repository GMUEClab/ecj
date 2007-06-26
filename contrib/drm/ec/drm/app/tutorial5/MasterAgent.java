/** Most code taken from Sean Luke's ECJ (ec.EvolutionState, ec.Evolve) and 
 * Màrk Jelasity's DRM (drm.agentbase.IAgent, drm.agents.CollectiveAgent).
 * Copyright 2006 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */
package ec.drm.app.tutorial5;

import java.util.*;

import ec.drm.*;
import ec.util.*;

import drm.agentbase.*;
import drm.core.ContributionBox;

public class MasterAgent extends EvolutionAgent{
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
    /** Address of the slave agent. */
	protected ArrayList slaves = new ArrayList();
    
    public ParameterDatabase slaveParameters;
    
	public static final String M_EVALUATE = "ecj.evaluate";
	
	public static final String M_END_EXPERIMENT = "ecj.end_experiment";
	
	public void floodWithSlaves(){
		Address target;
		
		String localhost = null;
		try {localhost = java.net.InetAddress.getLocalHost().getHostAddress();}
    	catch (Exception e){output.error("Could not get localhost address");}
		
		Iterator peers = getDRM().getContributions().iterator();
		
		Parameter p = new Parameter(Launch.P_STATE);

		SlaveAgent slave;
		
		while(peers.hasNext()){
			slave = (SlaveAgent)
		        	slaveParameters.getInstanceForParameterEq(p,null,SlaveAgent.class);
				
			slave.parameters = slaveParameters;
			slave.data = data;
			slave.setName(p);
			slave.setRoot(new Address(getName()));
			
			target = ((ContributionBox)peers.next()).contributor;
			if(target.port == Integer.parseInt(base.getProperty("port")) 
					&& target.getHost().getHostAddress().equals(localhost)){
					target = null;
					continue;
				}
			
			// Launch the agent to the target host
		    IRequest request = base.launch("DIRECT", slave, target);
		    while(request.getStatus() == IRequest.WAITING)
		    	Thread.yield();
		    	//try{Thread.sleep(1000);}
		    	//catch(Exception e){}
		    	
		    if(request.getStatus() != IRequest.DONE)
		    	output.error("There was an error sending the agent: " + request.getThrowable());
		    else{
		    	output.message("Slave " + slave.getName() + " sent to " + target.name);
		    	synchronized(slaves){
		    		slaves.add(new Address(target.getHost(), target.port, slave.getName()));
		    	}
		    }
		}
	}
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object o ) {
		if(!super.handleMessage(m,o)){
			if( m.getType().equals(MasterAgent.M_EVALUATE) ){
				((MasterEvaluator)evaluator).receiveEvaluatorData((EvaluatorData)o);
			}else return false;
		}
		return true;
	}
	
    /**
     * @param result
     */
    public void finish(int result){
        super.finish(result);
        for(int i=0; i < slaves.size(); i++)
        	fireMessage((Address)slaves.get(i),MasterAgent.M_END_EXPERIMENT, null);
    }
	
    public void run(){
    	setup(this, null);
    	startFresh(); // Never forget to call initializers if you plan to use population
    	
    	floodWithSlaves();
        
        /* the evolving loop */
        int result = R_NOTDONE;
        while ( result == R_NOTDONE && !(ideal_found && quitOnRunComplete))
            {
            result = evolve();
            }
        
        finish(result);

        output.message("All tasks finished.");
		suicide();
		System.exit(0);
    }
}