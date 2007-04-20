package contributionjob;
import java.util.*;

import drm.agents.*;
import drm.agentbase.*;
import drm.core.*;

/** The purpose of this very simple example is to show how 
 * some agents can pass information through a collective. */
public class Contributor extends ContributorAgent {
	public static final long serialVersionUID = 78944524352L;
	
	public Contributor(String job, String name, Address root){
		super("Contributor", job, name, root);
	}
	
	public Contributor(String job, String name){
		super("Contributor", job, name, null);
	}
	
	/** This function is called by the collective to update
	 * the contributions. So this is the point where you can
	 * contribute to the collective :)*/
	public Object getContribution(){
		return "It's " + System.currentTimeMillis() + 
				" here at " + name;
	}
	
	/** This one gets called each time a collective message
	 * arrives or the refresh() method is called. Here we only
	 * print the contributions, but much more complex things can
	 * be done. You can access to the contributions at any point
	 * you have access to the collective, not only here.*/
	public void collectiveUpdated(ContributionBox peer){
		Iterator<ContributionBox> contributions = 
			collective.getContributions().iterator();
		while(contributions.hasNext()){
			ContributionBox cb = contributions.next();
			System.out.println("Contribution from " 
					+ cb.contributor.getHost().toString() + "\n"
					+ cb.contribution + "\n");
		}
	}
	
	public void run(){
		/* Child nodes only become active with the getContribution()
		 * and collectiveUpdated() methods. */
		if(root != null)return;

		// We send an agent to any available node
		Iterator<ContributionBox> contributions = 
				getDRM().getContributions().iterator();
		while(contributions.hasNext()){
			Address target = contributions.next().contributor;
			Contributor child = new Contributor(job,"" + 
					Math.random(),new Address(null,-1,this.name));
			IRequest r = base.launch("DIRECT", child, target);
			while(r.getStatus() == IRequest.WAITING){
				try{Thread.sleep(1000);}
				catch(Exception e){
					System.err.println("Exception: " + e);
				}
			}
			if(r.getStatus() == IRequest.DONE) 
				System.out.println("Contributor sent to " 
						+ target.getHost().toString());
			else 
				System.err.println("Contributor couldn't arrive to " 
						+ target.getHost().toString());
		}
	}
}