package killjob;

import drm.agents.*;
import drm.agentbase.*;

/** By extending ContributorAgent we enable the agent to 
 * automatically know a root agent to communicate with. Also 
 * it will belong to a collective, not used in the example. 
 * This example illustrates messaging to particular agents. */
public class Killer extends ContributorAgent {
	public static final long serialVersionUID = 7894561344352L;
	
	public static final String INFOMESSAGE = "info";
	
	public static final String SUICIDEMESSAGE = "suicide";
	
	public Killer(String job, String name, Address root){
		super("Killer", job, name, root);
	}
	
	public Killer(String job, String name){
		super("Killer", job, name, null);
	}
	
	/** The root agent will spawn one agent to some node, which 
	 * will be ordered to suicide afterwards. */
	public void run(){
		if(name.endsWith("child")){
			System.out.println("Child initializing");
			fireMessage(root,INFOMESSAGE,"I'm alive!");
			return;
		}

		/* We choose one node from the DRM net, the last node
		 * which communicated with us. */
		Address target = 
			getDRM().getNewestContribution().contributor;
		/* We create the agent locally to get access to its name, 
		 * root IP Address (null) and port will be automatically 
		 * corrected later upon arrival. */
		Killer child = new Killer(job, "child", 
					new Address(null,-1,this.name));
		IRequest r = base.launch("DIRECT", child, target);

		while(r.getStatus() == IRequest.WAITING){
			try{Thread.sleep(1000);}
			catch(Exception e){System.err.println("Exception: " + e);}
		}
		if(r.getStatus() != IRequest.DONE)
			System.err.println("There was an error " +
				"sending an agent to " + target.name);

		System.out.println("Die!");
		/* The address to which we must post messages is the one 
		 * created with the host and port from the recipient node, 
		 * plus the name of the launched agent (There can be many 
		 * agents in each node). */ 
		r = fireMessage(
				new Address(target.getHost(),target.port,child.name), 
				SUICIDEMESSAGE, "Die!");
		
		while(r.getStatus() == IRequest.WAITING){
			try{Thread.sleep(100);}
			catch(Exception e){System.err.println("Exception: " + e);}
		}

		if(r.getStatus() == IRequest.DONE)
		System.out.println("Request obeyed");
		suicide();
	}
	
	/** Prints each message received. If the type of the message
	 *  is "suicide", the agent suicides. If the message is not 
	 *  recognized by this agent, this function must return false.*/
	public boolean handleMessage(Message m, Object o){
		if(!super.handleMessage(m, o)){
			if(m.getType().equals(INFOMESSAGE)){
				System.out.println("Received: " + o);
			}
			else if(m.getType().equals(SUICIDEMESSAGE)){
				System.out.println("Received: " + o);
				fireMessage(m.getSender(),INFOMESSAGE,"I'll be back.");
				suicide();
			}
			else return false;
		}
		return true;
	}
}