/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.app.tutorial3;

import java.util.*;

import ec.drm.*;
import ec.util.*;

import drm.agentbase.*;
import drm.core.*;

public class IslandAgent extends EvolutionAgent{
	
	/** Serialization identificator */
	private static final long serialVersionUID = 1L;

	/** The root island will store here the addresses to the islands it has created */
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
	

	/** Sends an island to each available node, except the local one */
	public void floodWithIslands(){
		Address target;
		
		String localhost = null;
		try {localhost = java.net.InetAddress.getLocalHost().getHostAddress();}
    	catch (Exception e){output.error("Could not get localhost address");}
		
		Iterator peers = getDRM().getContributions().iterator();
		
		Parameter p = new Parameter(DRMLauncher.P_STATE);

		EvolutionAgent island = (EvolutionAgent)
    	parameters.getInstanceForParameterEq(p,null,EvolutionAgent.class);
		
		island.parameters = parameters;
		island.data = data;
		island.setRoot(new Address(getName()));
		
		while(peers.hasNext()){
			island.setName(p);
			
			target = ((ContributionBox)peers.next()).contributor;
			if(target.port == Integer.parseInt(base.getProperty("port")) 
					&& target.getHost().getHostAddress().equals(localhost)){
					target = null;
					continue;
				}
			
			// Launch the agent to the target host
		    IRequest request = base.launch("DIRECT", island, target);
		    while(request.getStatus() == IRequest.WAITING)
		    	try{Thread.sleep(1000);}
		    	catch(Exception e){}
		    	
		    if(request.getStatus() != IRequest.DONE)
		    	output.error("There was an error sending the agent: " + request.getThrowable());
		    else{
		    	output.message("Agent " + island.getName() + " sent to " + target.name);
		    	islands.add(new Address(target.getHost(), target.port, island.getName()));
		    }
		}
	}
	
	/** The master island will not do evolutionary work, everything is cleaner this way */
    public void run(){
		if(iamroot){
			setup(this,null);
			floodWithIslands();
	        waitForIslands();
	        output.message("Everyone finished, shutting down");
	        suicide();
	        System.exit(0);
		}
		else super.run(); // EvolutionAgent.run()
    }
}