/** Some code taken from Sean Luke's ECJ and M�rk Jelasity's DRM.
 * Copyright 2006 Alberto Cuesta Ca�ada, licensed under the Academic Free License.
 * @author Alberto Cuesta Ca�ada
 * @version 0.1 
 */

package ec.drm;

import java.io.*;
import java.util.zip.GZIPInputStream;

import ec.*;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import drm.agentbase.*;
import drm.core.*;

/** A DRM replacement for ec.Evolve, it initializes whatever is needed
 * before starting the initial agent and launches it locally */
public class DRMLauncher implements Runnable{
    public static final String P_STATE = "state";
    
	/** The argument indicating that we're starting up from a checkpoint file. */
    public static final String A_CHECKPOINT = "-checkpoint";
    
    /** The argument indicating that we're starting fresh from a new parameter file. */
    public static final String A_PARAMS = "-params";
    
    /** The old argument for the parameter file, keep for compatibility. */
    public static final String A_PARAMS_COMP = "-file";
    
    /** The argument indicating a data file we could need. */
    //public static final String A_DATA = "-data";
	
    protected final Node node;
	protected final String[] args;

	protected ParameterDatabase loadParameterDatabase(String filename){
		try{
			return new ParameterDatabase(
				new File(new File(filename).getAbsolutePath()));
		}
		catch(FileNotFoundException e){ 
		    Output.initialError(
		    	"A File Not Found Exception was generated upon" +
		    	"reading the parameter file:\n" + e);}
		catch(IOException e){ 
		    Output.initialError(
		        "An IO Exception was generated upon reading the" +
		        "parameter file:\n" + e);}
		catch(NullPointerException e){ 
		    Output.initialError(
			        "The parameter file filename was not found:\n" + e);
		}
		return null;
	}
	
	//protected Object loadData(String filename){return null;}
	
    protected static EvolutionState restoreFromCheckpoint(String filename){
    	EvolutionState state = null;

	    try{
	    	// load from the file
		    AgentInputStream s = 
		        new AgentInputStream(
		            new GZIPInputStream (
		                new BufferedInputStream (
		                    new FileInputStream (filename))),
					(new EvolutionState()).getClass().getClassLoader());
		
		    state = (EvolutionState) s.readObject();
		    s.close();
	    }
		catch(FileNotFoundException e){
		    Output.initialError(
			    	"A File Not Found Exception was generated upon" +
			    	"reading the checkpoint file:\n" + e);}
		catch(IOException e){
		    Output.initialError(
			    	"A IO Exception was generated upon" +
			    	"reading the checkpoint file:\n" + e);}
		catch(ClassNotFoundException e){		    
			Output.initialError(
					"A Class Not Found Exception was generated upon" +
					"reading the checkpoint file:\n" + e);}
		catch(NullPointerException e){ 
		    Output.initialError(
			        "The checkpoint file filename was not found:\n" + e); 
		}
	    return state; 
	}
    
	public void run(){
		ParameterDatabase parameters = null;
		EvolutionState evolutionState = null;
		
		for(int a=0; a < args.length; a++){
		    if(args[a].equals(A_PARAMS) || args[a].equals(A_PARAMS_COMP)){
				System.out.print("Loading parameter database...");
				parameters = loadParameterDatabase(args[++a]);
				System.out.println("OK");
    		}
		    if(args[a].equals(A_CHECKPOINT)){
	    		System.out.print("Loading checkpoint...");
	    		evolutionState = restoreFromCheckpoint(args[++a]);
	    		System.out.println("OK");
	    	}
		}

		Parameter p = new Parameter(P_STATE);

		EvolutionAgent rootAgent = (EvolutionAgent)
        parameters.getInstanceForParameterEq(p,null,EvolutionAgent.class);
		
		if(evolutionState != null)
			rootAgent = (EvolutionAgent)evolutionState;
		
		// preLaunchSetup(), an agent without name will get nasty errors.
		rootAgent.parameters = parameters;
		rootAgent.setName(p);
		
		p = new Parameter(ProblemData.P_PROBLEM_DATA);
		if(parameters.exists(p)){ // If not it should anyway instance and setup a blank ProblemData
			rootAgent.data = (ProblemData)parameters.getInstanceForParameterEq(p,null,ProblemData.class);
			rootAgent.data.setup(rootAgent, p);
		}
		
		System.out.println("Launching root agent " + rootAgent.getName());
		IRequest request = node.launch("DIRECT", rootAgent, null);
    	while(request.getStatus() == IRequest.WAITING)
    		Thread.yield();
    		//try{Thread.sleep(1000);}
    		//catch(Exception e){}
    	if(request.getStatus() != IRequest.DONE)
    		System.err.println("There was an error launching the agent: " + request.getThrowable());
		
	}
    
	public DRMLauncher(Node n, String[] a){
		node = n;
		args = a;
	}
}