package ec.drm.app.tutorial2;

import java.util.*;

import drm.agentbase.*;
import drm.core.*;

import ec.drm.*;
import ec.util.Parameter;

public class FloodEvolutionAgent extends EvolutionAgent{

	public static final long serialVersionUID = 1L;
	
    public static final String P_SLAVES = "slaves";
	
	public void floodWithAgents(){

		Address target;

		String localhost = null;
		try {localhost = java.net.InetAddress.getLocalHost().getHostAddress();}
    	catch (Exception e){output.error("Could not get localhost address");}
		
		Iterator peers = getDRM().getContributions().iterator();
		
		Parameter p = new Parameter(P_SLAVES);

		EvolutionAgent agent;
		
		while(peers.hasNext()){
			agent = (EvolutionAgent)
	        	parameters.getInstanceForParameterEq(p,null,EvolutionAgent.class);
			
			agent.parameters = parameters;
			agent.data = data;
			agent.setName(p);
			agent.setRoot(new Address(getName()));
			
			//agent.setup(agent,p);
			
			target = ((ContributionBox)peers.next()).contributor;
			if(target.port == Integer.parseInt(base.getProperty("port")) 
					&& target.getHost().getHostAddress().equals(localhost)){
					target = null;
					continue;
				}
			
			// Launch the agent to the target host
		    IRequest request = base.launch("DIRECT", agent, target);
		    while(request.getStatus() == IRequest.WAITING)
		    	try{Thread.sleep(1000);}
		    	catch(Exception e){}
		    	
		    if(request.getStatus() != IRequest.DONE)
		    	output.error("There was an error sending the agent: " + request.getThrowable());
		    else
		    	output.message("Agent " + agent.getName() + " sent to " + target.name);
		}
	}
	
	public void run(){
		setup(this,null);
		floodWithAgents();
	}
}