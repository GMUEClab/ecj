package sendjob;

import drm.agents.*;
import drm.agentbase.*;
import drm.core.*;

import java.util.*;

/** This example illustrates how serializable objects can be 
 * sent through messaging. */
public class Sender extends ContributorAgent {
	public static final long serialVersionUID = 78945624352L;
	
	public static final String DATAMESSAGE = "data";
	
	public Sender(String job, String name, Address root){
		super("Sender", job, name, root);
	}
	
	public Sender(String job, String name){
		super("Sender", job, name, null);
	}
	
	/** Children will be sent by root to every other node in 
	 * the DRM network. Any agent will create a hashtable with 
	 * random numbers and sent it to every other node in the 
	 * collective.
	 * The DRM network is composed by the nodes started previously 
	 * without any job, and the collective is formed by the agents 
	 * of a kind hosted in that DRM network.*/
	public void run(){
		Address target;
		IRequest request;
		Iterator<ContributionBox> contributions;
		
		// Get all nodes in the DRM and send them "Sender" agents.
		if(root == null){
			contributions = getDRM().getContributions().iterator();
			while(contributions.hasNext()){
				target = contributions.next().contributor;
				Sender child = new Sender("Sender",null,
						new Address(null,-1,this.name));
				request = base.launch("DIRECT", child, target);
				while(request.getStatus() == IRequest.WAITING){
					try{Thread.sleep(1000);}
					catch(Exception e){
						System.err.println("Exception: " + e);
					}
				}
				if(request.getStatus() != IRequest.DONE)
					System.err.println("There was an error " +
						"sending an agent to " + target.name);
			}
		}

		/* We must wait a prudential time so the agents arrive 
		 * and connects between them to create the collective. */
		System.out.println("Waiting 30s to send objects...");
		try{Thread.sleep(30000);}
		catch(Exception e){System.err.println("Exception: " + e);}
		
		System.out.println("Creating object to send...");
		Hashtable<String,String> data = new Hashtable<String,String>();
		for(int i=0; i<5; i++) data.put(
				"" + Math.random(),"" + Math.random());
		Iterator<String> keys = data.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			System.out.println(key + " - " + data.get(key));
		}
		
		/* We can access to the agents we sent using the collective
		 * field. The data returned is the same type than that we get
		 * with getDRM(), but they refer to different things.*/
		contributions = collective.getContributions().iterator();
		while(contributions.hasNext()){
			target = contributions.next().contributor;
			System.out.println("Sending data to " + target.name);
			request = fireMessage(target, DATAMESSAGE, data);
			while(request.getStatus() == IRequest.WAITING){
				try{Thread.sleep(1000);}
				catch(Exception e){
					System.err.println("Exception: " + e);
				}
			}
			if(request.getStatus() != IRequest.DONE)
				System.err.println("There was an error " +
					"sending the data to " + target.name);
		}
		
		// Let's wait some for the messages that could arrive yet.
		System.out.println("Waiting 30s to exit...");
		try{Thread.sleep(30000);}
		catch(Exception e){System.err.println("Exception: " + e);}
		
		suicide();
	}
	
	/** If the message is recognizable as a data message, print it.*/
	public boolean handleMessage(Message message, Object object){
		if(!super.handleMessage(message, object)){
			if(message.getType().equals(DATAMESSAGE)){
				System.out.println("Data message arrived: ");
				 // Take care of downcasting when messaging objects
				if(!(object instanceof Hashtable)){
					System.err.println("Data must be sent in " +
							"Hashtable format");
					return false;
				}
				Hashtable<String,String> data =
					(Hashtable<String,String>)object;
				Iterator<String> keys = data.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					System.out.println(key + " - " + data.get(key));
				}
			}
			else return false;
		}
		return true;
	}
}