/** Most code taken from Sean Luke's ECJ (ec.simple.SimpleStatistics),
 * licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */

package ec.drm;

import drm.agentbase.*;

import ec.*;
import ec.util.*;

import java.io.*;
import java.util.*;

/**
 * A basic Statistics class suitable for simple problem applications.
 *
 * SimpleStatistics prints out the best individual, per subpopulation,
 * each generation.  At the end of a run, it also prints out the best
 * individual of the run.  SimpleStatistics outputs this data to a log
 * which may either be a provided file or stdout.  Compressed files will
 * be overridden on restart from checkpoint; uncompressed files will be 
 * appended on restart.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 </table>
 */

public class DRMStatistics extends Statistics
    {
	
	/** Serialization identificator */
	private static final long serialVersionUID = 833584182806685867L;
	
    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";

    /** set this accordingly if you have too much network traffic */
    public static final String P_FREQUENCY = "frequency";
    
    /** should we store the best of run in machine-readable format? */
    public static final String P_STORE_BEST = "store-best";
    
    /** should we use the collective to distribute statistics? */
    //public static final String P_COLLECTIVE = "use-collective";
    
    /** The Statistics' log */
    public int defaultlog = -1;
    
    /** The common filename part for all logfiles */
    public String basefilename;
    
    /** stores which file each agent is logging to */
    protected transient Hashtable logtable;
    
    /** How many generations pass for each stats report */
    public int frequency;
    
    /**  */
    public boolean store_best;
    
    /**  */
    //public boolean use_collective;

    /** Creation time */
    public long creationtime;
    
    /** Sets up needed parameters when loading from checkpoint */
    public void reset(final EvolutionState state){
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMStatistics requires an  EvolutionAgent, reset failed",null,null);
    	EvolutionAgent agent = (EvolutionAgent)state;
    	
    	if (agent.iamroot){
	    	/*basefilename = System.getProperty("user.dir") + File.separator
	    		+ state.parameters.getFile(base.push(P_STATISTICS_FILE),null).getName();
	    	basefilename = basefilename.substring(0,basefilename.lastIndexOf("."));*/
    		logtable = new Hashtable();
    	}else
    		defaultlog = -2; 
    }
    
    /** Sets up parameters.*/
    public void setup(final EvolutionState state, final Parameter base)
        {
    	if (!(state instanceof EvolutionAgent))
    		state.output.fatal("DRMStatistics requires an  EvolutionAgent",null,null);
    	EvolutionAgent agent = (EvolutionAgent)state;
    	
    	super.setup(state,base);
    	frequency = state.parameters.getInt(base.push(P_FREQUENCY),null,1);
    	store_best = state.parameters.getBoolean(base.push(P_STORE_BEST),null,true);
    	//use_collective = state.parameters.getBoolean(base.push(P_COLLECTIVE),null,true);

    	
    	// If we are root, set up the base filename and the logtable
    	if (agent.iamroot){
    		// I'm not sure that outputting everything to the current directory is right
	    	basefilename = System.getProperty("user.dir") + File.separator
	    		+ state.parameters.getFile(base.push(P_STATISTICS_FILE),null).getName();
    		logtable = new Hashtable();
        }else
        	defaultlog = -3; //Maybe can be useful for recognizing it as a non valid log
    	
    	creationtime = System.currentTimeMillis();
    }

    /** Opens a file and checks it worked. */
    private File openFile(final EvolutionState state, final String filename){
    	File file = new File(filename);
    	if (file != null) state.output.message("File created: " + filename);
    	else state.output.error("DRMStatistics#OpenFile: The file " + filename + " wasn't created.");
    	return file;
    }
    
    /** Creates a new log and updates the logtable. */
    public int addLog(final EvolutionState state, final String owner){
    	File file = openFile(state, 
    			basefilename + 
    			owner.substring(owner.lastIndexOf(".")) + 
    			".stat");
        
        try{
        	int log = state.output.addLog(file,Output.V_NO_GENERAL-1,false,
                   !state.parameters.getBoolean(new Parameter(P_COMPRESS),null,false),
                   state.parameters.getBoolean(new Parameter(P_COMPRESS),null,false));
        	logtable.put(owner, new Integer(log));
        	return log;
        }catch (IOException i){
        	state.output.fatal("An IOException occurred while trying to create the log " + file + ":\n" + i);
        }
        return -4;
    }
    
    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
        }

    /** Logs standard statistics information. Posts it to the root agent if required.*/
    public void postEvaluationStatistics(final EvolutionState state){
    	super.postEvaluationStatistics(state);
    	
        if(frequency == 0) return; // Frequency == 0 means that we want only final stats
        if(state.generation % frequency != 0) return;

        EvolutionAgent agent = (EvolutionAgent)state;
        
        StatsData data = new StatsData(
        		new Address(agent.getName()),
        		state.generation,
        		getBestIndividual(state));
    	
        if(agent.iamroot)	// Local logging
        	printStatistics(state, data);
        else{				// DRM logging
        	/*if(use_collective){
        		data.put("type", EvolutionAgent.STATS_MESSAGE);
        		agent.setContribution(data);
        	}else*/
        	agent.fireMessage(agent.getRootAddress(),EvolutionAgent.M_STATS,data);
        
        }
    }
    
    /** Returns an array with the current best individual for each subpopulation. */
    public Individual[] getBestIndividual(EvolutionState state){
    	Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++){
            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
        }
        return best_i;
    }
    
    /** This one checks that the stats message was received. If not, the message is 
     * sent again up to five times. Run time an best individual of run are logged. */
    public void finalStatistics(final EvolutionState state, final int result){
	    super.finalStatistics(state,result);
        
        EvolutionAgent agent = (EvolutionAgent)state;
        
        StatsData data = new StatsData(
        		new Address(agent.getName()),
        		state.generation,
        		System.currentTimeMillis() - creationtime,
        		getBestIndividual(state),
        		getBestIndividual(state));
        
    	
        if(agent.iamroot)	// Local logging
        	printStatistics(state, data); // Every statistic will go there
        else{				// DRM logging
        	for (int i=0; i<5;i++){ // Try to send final data 5 times
	    		IRequest request = agent.fireMessage(agent.getRootAddress(),EvolutionAgent.M_STATS,data);
	    		while(request.getStatus() == IRequest.WAITING){
	    			try{Thread.sleep(1000);}
	    			catch(Exception e){state.output.error("Exception: " + e);}
	    		}
	    		if(request.getStatus() == IRequest.DONE){break;}
	    		else{
	    			state.output.error("There was an error sending final statistics.");
	    			try{Thread.sleep(1000*i^2);}
	    			catch(Exception e){state.output.error("Exception: " + e);}
	    		}
            }
        }
    }
    
    /** Saves an individual to disk under a given filename, in computer-readable format. */
    public void storeIndividual(final EvolutionState state, String filename, Individual ind){
    	try{
    		File file = openFile(state,filename);
    		//PrintWriter writer = new PrintWriter(file);
    		//ind.printIndividual(state, writer);
    		//writer.close();
    		int log = state.output.addLog(file,Output.V_NO_GENERAL-1,false,
                    !state.parameters.getBoolean(new Parameter(P_COMPRESS),null,false),
                    state.parameters.getBoolean(new Parameter(P_COMPRESS),null,false));
			ind.printIndividual(state, log, Output.V_NO_GENERAL);
        	state.output.message("Best Individual stored in " + filename);
    	}catch(Exception e){
    		state.output.error("Exception " + e);
    	}
    }
    
    /** Prints StatsData structures to local logs. */
    public void printStatistics(final EvolutionState state, StatsData data){	
    	int log = -6;
    	String sender;
    	
        EvolutionAgent agent = (EvolutionAgent)state;
    	
	    sender = data.sender.name;
	    if(logtable.containsKey(sender)) log = ((Integer)logtable.get(sender)).intValue();
	    else log = addLog(state,sender);
    	if(log < 0){
    		if(defaultlog < 0) defaultlog = addLog(state,agent.getName());
    		state.output.message("Received a stats message from an unknown sender, will be logged to: " + 
    				((Integer)logtable.get(new Integer(defaultlog))).intValue());
    		log = defaultlog;
    	}
    	state.output.println(data.toStringForHumans(),Output.V_NO_GENERAL,log);
    	
    	
    	if(store_best && data.finalStats){
    		for(int i = 0; i < data.best_of_run.length; i++)
    			storeIndividual(state,
    					basefilename+
    					data.sender.name.substring(data.sender.name.lastIndexOf("."))+
    					".best",
    					data.best_of_run[i]);
    	}
    }
}