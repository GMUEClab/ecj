/** Most code taken from Sean Luke's ECJ (ec.EvolutionState, ec.Evolve) and 
 * Màrk Jelasity's DRM (drm.agentbase.IAgent, drm.agents.CollectiveAgent).
 * Copyright 2006 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */
package ec.drm.app.tutorial4;

import ec.drm.*;
import ec.util.*;

import drm.agentbase.*;

public class MasterAgent extends EvolutionAgent{
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
    /** Address of the slave agent. */
    protected Address slave;

    public ParameterDatabase slaveParameters;
    
	public static final String M_EVALUATE_POP_CHUNK = "ecj.evaluate_pop_chunk";

	public void createSlave(){
		Address target = getDRM().getNewestContribution().contributor;
		
		Parameter p = new Parameter(Launch.P_STATE);

		SlaveAgent tmpslave = (SlaveAgent)
	        	slaveParameters.getInstanceForParameterEq(p,null,SlaveAgent.class);
			
		tmpslave.parameters = slaveParameters;
		tmpslave.data = data;
		tmpslave.setName(p);
		tmpslave.setRoot(new Address(getName()));
			
		// Launch the agent to the target host
		IRequest request = base.launch("DIRECT", tmpslave, target);
		while(request.getStatus() == IRequest.WAITING)
		    try{Thread.sleep(1000);}
		    catch(Exception e){}
		    	
		if(request.getStatus() != IRequest.DONE)
		    output.error("There was an error sending the slave: " + request.getThrowable());
		else{
		    output.message("Slave " + tmpslave.getName() + " sent to " + target.name);
		    slave = new Address(target.getHost(), target.port, tmpslave.getName());
		}
	}
	
    public void run(){
    	setup(this, null);
        createSlave();
        
        super.run(); // The MasterEvaluator will take care

        output.message("All tasks finished.");
		suicide();
		System.exit(0);
    }
}