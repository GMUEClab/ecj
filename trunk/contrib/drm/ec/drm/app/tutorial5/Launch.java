package ec.drm.app.tutorial5;

import ec.*;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.drm.*;

import drm.agentbase.*;
import drm.core.*;

public class Launch extends DRMLauncher{
	
    public static final String A_MASTER = "-master";
    public static final String A_SLAVE = "-slave";
    
	public void run(){
		ParameterDatabase masterParameters = null;
		ParameterDatabase slaveParameters = null;
		EvolutionState evolutionState = null;
		
		for(int a=0; a < args.length; a++){
		    if(args[a].equals(A_MASTER)){
				System.out.print("Loading parameter database...");
				masterParameters = loadParameterDatabase(args[++a]);
				System.out.println("OK");
    		}
		    if(args[a].equals(A_SLAVE)){
				System.out.print("Loading parameter database...");
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
		masterAgent.setName(p); // An agent without name will get nasty errors.
		
		System.out.println("Launching root agent " + masterAgent.parameters.getString(p, null));
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