/** 
 * Copyright 2007 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm.masterslave;

import ec.*;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.drm.*;

import drm.agentbase.*;
import drm.core.*;

/** Launcher for master/slave settings. Takes two parameter files. */
public class Launch extends DRMLauncher{
	
    public static final String A_MASTER = "-master";
    public static final String A_SLAVE = "-slave";
    
	public void run(){
		ParameterDatabase masterParameters = null;
		ParameterDatabase slaveParameters = null;
		EvolutionState evolutionState = null;
		
		for(int a=0; a < args.length; a++){
		    if(args[a].equals(A_MASTER)){
				System.out.print("Loading master parameter database...");
				masterParameters = loadParameterDatabase(args[++a]);
				System.out.println("OK");
    		}
		    if(args[a].equals(A_SLAVE)){
				System.out.print("Loading slave parameter database...");
				slaveParameters = loadParameterDatabase(args[++a]);
				System.out.println("OK");
    		}
		    if(args[a].equals(A_CHECKPOINT)){
	    		System.out.print("Loading checkpoint...");
	    		evolutionState = restoreFromCheckpoint(args[++a]);
	    		System.out.println("OK");
	    	}
		}

		Parameter p = new Parameter(P_STATE);

		MasterAgent masterAgent = (MasterAgent)
        masterParameters.getInstanceForParameterEq(p,null,MasterAgent.class);
		
		if(evolutionState != null)
			masterAgent = (MasterAgent)evolutionState;
		
		// preLaunchSetup()
		masterAgent.parameters = masterParameters;
		masterAgent.slaveParameters = slaveParameters;
		masterAgent.setName(p); // An agent without name will get nasty errors when arriving to a node.
		
		p = new Parameter(ProblemData.P_PROBLEM_DATA);
		if(masterParameters.exists(p)){ // If not it should anyway instance and setup a blank ProblemData
			masterAgent.data = (ProblemData)masterParameters.getInstanceForParameterEq(p,null,ProblemData.class);
			masterAgent.data.setup(masterAgent, p); // This setup will load the data from disk to masterAgent.data
		}
		
		System.out.println("Launching master agent " + masterAgent.getName());
		IRequest request = node.launch("DIRECT", masterAgent, null);
    	while(request.getStatus() == IRequest.WAITING)
    		Thread.yield();
    		//try{Thread.sleep(1000);}
    		//catch(Exception e){}
    	if(request.getStatus() != IRequest.DONE)
    		System.err.println("There was an error launching the agent: " + request.getThrowable());
		
	}
    
	public Launch(Node n, String[] a){super(n,a);}
}