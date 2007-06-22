/** Most code taken from Sean Luke's ECJ (ec.EvolutionState, ec.Evolve) and 
 * Màrk Jelasity's DRM (drm.agentbase.IAgent).
 * Copyright 2006 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */
package ec.drm.app.tutorial8;

import java.util.*;

import ec.drm.*;
import ec.util.*;

import drm.agentbase.*;
import drm.core.*;

public class IslandAgent extends EvolutionAgent{
	
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;

	protected Set islands = Collections.synchronizedSet(new HashSet());
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object object ) { // I reversed here the handling order
		if( m.getType().equals(M_FINISHED) ){
			output.message(m.getSender().name + " finished.");
			if(iamroot)
				islands.remove(m.getSender());
			return true;
		}else if( m.getType().equals(M_IDEAL_FOUND) ){
			output.message(m.getSender().name + " found an ideal individual.");
			ideal_found = true;
			if(iamroot)
				announceIdealIndividual(m.getSender());
			return true;
		}else return super.handleMessage(m,object);
	}

	/** Sends an IDEAL_FOUND message to all islands. */
	protected void announceIdealIndividual(Address except){
    	output.message("Telling everybody that an ideal individual has been found.");
    	/*Address[] peers = getPeerAddresses();
    	for( int x = 0 ; x < peers.length ; x++ )*/
    	
    	Address target = null;
    	synchronized(islands){
    	Iterator peers = islands.iterator();
	    	while(peers.hasNext())
	    		target = (Address)peers.next();
	    		if(!target.equals(null) && !target.equals(except))
	    			fireMessage(target,M_IDEAL_FOUND,null);
    	}
	}
	
    /** Convenience method */
	private void wait(int s){
		try{Thread.sleep(s*1000);}
    	catch(Exception e){output.error("Exception: " + e);}
	}
	
	/** Waits until we have received a "finished" message from each sent agent. */
	protected void waitForIslands(){
		output.message("Waiting for islands:");
		synchronized(islands){
	    	Iterator keys = islands.iterator();
	    	while(keys.hasNext()) output.message(((Address)keys.next()).name);
			while(islands.size() > 0) wait(5);
		}
	}
	

	
	public void floodWithIslands(){
		Address target;
		
		String localhost = null;
		try {localhost = java.net.InetAddress.getLocalHost().getHostAddress();}
    	catch (Exception e){output.error("Could not get localhost address");}
		
		Iterator peers = getDRM().getContributions().iterator();
		
		Parameter p = new Parameter(DRMLauncher.P_STATE);

		EvolutionAgent agent;
		
		while(peers.hasNext()){
			agent = (EvolutionAgent)
	        	parameters.getInstanceForParameterEq(p,null,EvolutionAgent.class);
			
			agent.parameters = parameters;
			agent.data = data;
			agent.setName(p);
			agent.setRoot(new Address(getName()));
			
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
		    else{
		    	output.message("Agent " + agent.getName() + " sent to " + target.name);
		    	islands.add(new Address(target.getHost(), target.port, agent.getName()));
		    }
		}
	}
	
	public void runMaster(){
		setup(this,null);
		floodWithIslands();
        waitForIslands();
        output.message("Everyone finished, shutting down");
        suicide();
        System.exit(0);
	}
	
    public void run(){
		if(iamroot)runMaster();
		else super.run();
    }
}