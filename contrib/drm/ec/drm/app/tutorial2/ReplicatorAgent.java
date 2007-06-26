/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.app.tutorial2;

import java.util.*;

import drm.agentbase.*;
import drm.core.*;

import ec.drm.*;
import ec.util.Parameter;

public class ReplicatorAgent extends EvolutionAgent{

	public static final long serialVersionUID = 1L;
	
    public static final String P_REPLICA = "replica";
	
	public void floodWithReplicas(){

		Address target;

		String localhost = null;
		try {localhost = java.net.InetAddress.getLocalHost().getHostAddress();}
    	catch (Exception e){output.error("Could not get localhost address");}
		
		Iterator peers = getDRM().getContributions().iterator();
		
		Parameter p = new Parameter(P_REPLICA);

		EvolutionAgent replica = (EvolutionAgent)
    		parameters.getInstanceForParameterEq(p,null,EvolutionAgent.class);
		
		replica.parameters = parameters;
		replica.data = data;
		replica.setRoot(new Address(getName()));
		
		while(peers.hasNext()){
			/* Each agent must have a different name, if it is not set from parameters
			 * setName() will use a random one */
			replica.setName(p);
			
			// Avoid sending replicas to the local node, it is uncomfortable
			target = ((ContributionBox)peers.next()).contributor;
			if(target.port == Integer.parseInt(base.getProperty("port")) 
					&& target.getHost().getHostAddress().equals(localhost)){
					target = null;
					continue;
				}
			
			// Launch the agent to the target host
		    IRequest request = base.launch("DIRECT", replica, target);
		    while(request.getStatus() == IRequest.WAITING)
		    	Thread.yield();
		    	//try{Thread.sleep(1000);}
		    	//catch(Exception e){}
		    	
		    if(request.getStatus() != IRequest.DONE)
		    	output.error("There was an error sending the agent: " + request.getThrowable());
		    else
		    	output.message("Agent " + replica.getName() + " sent to " + target.name);
		}
	}
	
	public void run(){
		setup(this,null); // We need it to use the output facility
		floodWithReplicas();
	}
}