/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.gep;
import ec.*;
import ec.steadystate.*;
import java.io.IOException;
import java.util.*;
import ec.util.*;

import java.io.File;

/* 
 * GEPSimpleStatistics.java
 * 
 * Created: Jan. 31, 2007
 * By: Bob Orchard
 */

/**
 * A basic Statistics class suitable for GEP problem applications (modified
 * version of ec.simple.SimpleStatistics).
 *
 * GEPSimpleStatistics prints out the best individual, per subpopulation,
 * each generation --- ONLY if it better than the previous best individual
 * from any generation.  Note that GEP systems do not use subpopulations but
 * the code will support them (in case this is possible in a future version).
 * At the end of a run, it also prints out the best
 * individual of the run.  SimpleStatistics outputs this data to a log
 * which may either be a provided file or stdout.  Compressed files will
 * be overridden on restart from checkpoint; uncompressed files will be 
 * appended on restart.
 *
 * <p>SimpleStatistics implements a simple version of steady-state statistics:
 * if it quits before a generation boundary,
 * it will include the best individual discovered, even if the individual was discovered
 * after the last boundary.  This is done by using individualsEvaluatedStatistics(...)
 * to update best-individual-of-generation in addition to doing it in
 * postEvaluationStatistics(...).

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file-observed-versus-computed</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies no output)</font></td>
 <td valign=top>(the log for comparison of observed results to model predicted results)</td></tr>
 <tr><td valign=top><i>base.</i><tt>detail-to-log</tt><br>
 <font size=-1>String (one of all, change or final), or nonexistant (change)</font></td>
 <td valign=top>(log results from all generations, only when the fitness changes or only final model)</td></tr>
 </table>

 *
 * @author Bob Orchard
 * @version 1.0 
 */

public class GEPSimpleStatistics extends Statistics implements SteadyStateStatisticsForm
{
    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";
    public static final String P_OBSERVED_VS_COMPUTED_FILE = "file-observed-versus-computed";
    public static final String P_OBSERVED_VS_COMPUTED_TEST_FILE = "file-observed-versus-computed-test";
    public static final String P_DETAIL_TO_LOG = "detail-to-log";
    public static final String P_NO_OBSERVED_VS_COMPUTED = "no-observed-versus-computed";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";

    /** The Statistics' log */
    public int statisticslog;
    public int observedVsComputedlog;
    public int observedVsComputedTestlog;
    public boolean noObserveredComputedDisplay = false;;
    public String detailToLog = "change";

    /** The best individual we've found so far */
    public Individual[] best_of_run;
    /** Generation at which the the best overall individual is found  */
    public int[] best_of_run_generation;


    public GEPSimpleStatistics() { best_of_run = null;
    							   best_of_run_generation = null;
    							   statisticslog = 0; /* stdout */ }

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);
        
        File statisticsFile = state.parameters.getFile(
                base.push(P_STATISTICS_FILE),null);

        if (statisticsFile!=null) try
        {
            statisticslog = state.output.addLog(statisticsFile,Output.V_NO_GENERAL-1,false,
                                                !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
                                                state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
        }
        catch (IOException i)
        {
            state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
        }
        
        noObserveredComputedDisplay = state.parameters.getBoolean(base.push(P_NO_OBSERVED_VS_COMPUTED),
        		base.push(P_NO_OBSERVED_VS_COMPUTED), false);
        
        
        File observedVsComputedFile = state.parameters.getFile(
                base.push(P_OBSERVED_VS_COMPUTED_FILE),null);

        if (observedVsComputedFile!=null) 
            try
	        {
	        	observedVsComputedlog = state.output.addLog(observedVsComputedFile,Output.V_NO_GENERAL-1,false,
	                                                !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
	                                                state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
	        }
	        catch (IOException i)
	        {
	            state.output.fatal("An IOException occurred while trying to create the log " + observedVsComputedFile + ":\n" + i);
	        }
	    else
	    	observedVsComputedlog = statisticslog;	
        
        File observedVsComputedTestFile = state.parameters.getFile(
                base.push(P_OBSERVED_VS_COMPUTED_TEST_FILE),null);

        if (observedVsComputedTestFile==null)
        	observedVsComputedTestlog = observedVsComputedlog;
        else
        	try
	        {
	        	observedVsComputedTestlog = state.output.addLog(observedVsComputedTestFile,Output.V_NO_GENERAL-1,false,
	                                                !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
	                                                state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
	        }
	        catch (IOException i)
	        {
	            state.output.fatal("An IOException occurred while trying to create the log " + observedVsComputedTestFile + ":\n" + i);
	        }
        
        detailToLog = state.parameters.getStringWithDefault(base.push(P_DETAIL_TO_LOG), null, "change");
        if (!detailToLog.equals("change") && !detailToLog.equals("final") && !detailToLog.equals("all"))
        {
        	state.output.warning("Expecting detail-to-log to be one of 'change', 'final' or 'all' not '" + detailToLog + "'. Setting to 'change'.");
        	detailToLog = "change";
        }
    }

    public void postInitializationStatistics(final EvolutionState state)
    {
        super.postInitializationStatistics(state);
        
        // set up our best_of_run array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        best_of_run = new Individual[state.population.subpops.length];
        best_of_run_generation = new int[state.population.subpops.length];
        
        // print some basic info about the problem:  problem type, etc.
        GEPSpecies species = (GEPSpecies)state.population.subpops[0].species;
        int seeds[] = state.seeds;
        String seedStr = String.valueOf(seeds[0]);
        for (int i=0; i<seeds.length-1; i++)
        	seedStr += String.valueOf(seeds[i]);
        state.output.println("Maximum number of generations in this run: " + state.numGenerations,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Size of population in this run: " + state.population.subpops[0].individuals.length,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Number of genes per chromosome: " + species.numberOfGenes,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Size of gene head: " + species.headSize,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Seed(s) used in this job: " + seedStr,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Problem type: " + species.problemTypeName,Output.V_NO_GENERAL,statisticslog);
        if (species.problemType == GEPSpecies.PT_CLASSIFICATION)
        	state.output.println("Classification rounding threshold: " + GEPIndividual.getThreshold(),Output.V_NO_GENERAL,statisticslog);        	
        state.output.println("",Output.V_NO_GENERAL,statisticslog);
            }

    /** Logs the best individual of the generation. Will lof the best individual if:
     * <br>
     * detail-to-log is set 'all'
     * <br>
     * detail-to-log is set to 'change' and the best of the generation is better than previous best
     * <br>
     * 
     *  */
    public void postEvaluationStatistics(final EvolutionState state)
    {
        super.postEvaluationStatistics(state);
        
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++)
        {
            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
        
            // now test to see if it's the new best_of_run ... remember if it is and
            // print it ... we only print the best of generation if it becomes the new
            // best of run
            boolean newBestOfRun = false;
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
            {
                best_of_run[x] = (Individual)(best_i[x].clone());
                best_of_run_generation[x] = state.generation;
                newBestOfRun = true;
            }
            if (detailToLog.equals("all") ||
            	(detailToLog.equals("change") && newBestOfRun))
            {
                // print the best-of-generation individual
                state.output.println("\nGeneration: " + state.generation,Output.V_NO_GENERAL,statisticslog);
                state.output.println("Best Individual:",Output.V_NO_GENERAL,statisticslog);
                best_i[x].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
            }
        }     
    }

    /** Steady State only: loads any additional post-generation boundary stragglers into best_of_run. */
    public void individualsEvaluatedStatistics(SteadyStateEvolutionState state)
    {
        super.individualsEvaluatedStatistics(state);
        
        for(int x=0;x<state.population.subpops.length;x++)
        {
            // best individual
            if (best_of_run[x]==null || 
                state.population.subpops[x].individuals[state.newIndividuals[x]].
                fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = state.population.subpops[x].individuals[state.newIndividuals[x]];
        }
    }

    /** 
     * Logs the best individual of the run showing details 
     */
    public void finalStatistics(final EvolutionState state, final int result)
    {
        super.finalStatistics(state,result);
        
        // for now we just print the best fitness 
        
        for(int x=0;x<state.population.subpops.length;x++ )
        {
            state.output.println("\nBest Individual of Run:",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Found at Generation: " + best_of_run_generation[x],Output.V_NO_GENERAL,statisticslog);
            best_of_run[x].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
        
	        // also print some stats about the expression ... its size, number of terminals used and
	        // the number of times each variable and function is used
	        state.output.println("Size of program: " + best_of_run[x].size(), 
	        		             Output.V_NO_GENERAL,statisticslog);
	        state.output.print("Variables used(count): ", Output.V_NO_GENERAL,statisticslog);
	        int counts[] = ((GEPIndividual)best_of_run[x]).variableUseageCounts();
	        GEPSymbolSet ss = ((GEPSpecies)state.population.subpops[x].species).symbolSet;
	        boolean first = true;
	        for (int i=0; i<counts.length; i++)
	        {   
	        	if (counts[i] > 0)
	        	{
	        		if (!first)
	 	   	           state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
	        	    String terminalName = ss.symbols[ss.terminals[i]].symbol;
	   	            state.output.print(terminalName +"(" + counts[i] + ")", 
	   	            		           Output.V_NO_GENERAL,statisticslog);
	   	            first = false;
	        	}
	        }
	        state.output.print("\nFunctions used(count): ", Output.V_NO_GENERAL,statisticslog);
	        HashMap fcounts = ((GEPIndividual)best_of_run[x]).functionUseageCounts();
	        Set countsSet = fcounts.entrySet();
	        first = true;
	        for (Iterator iter = countsSet.iterator(); iter.hasNext ();) 
	        {
	        	if (!first) 
	        		state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
	        	first = false;
	        	Map.Entry e = (Map.Entry)iter.next();
   	            String key = (String)e.getKey();
	            String value = ((Integer)e.getValue()).toString();
   	            state.output.print(key +"(" + value + ")", 
        		           Output.V_NO_GENERAL,statisticslog);
	        }

	        GEPSpecies species = (GEPSpecies)state.population.subpops[0].species;
	        GEPIndividual ind = (GEPIndividual)best_of_run[x];
	        state.output.println("\n\n***** TRAINING data results *****", Output.V_NO_GENERAL,statisticslog);	
	        displayStatistics(state, species, ind);
	        // Do the same thing for the test data if there is any
	  	    // To do this we need to force the terminal symbols (including the
	  	    // dependent variable) to give the test data rather than the training data
	  	    // temporarily.
  	    	GEPDependentVariable.useTrainingData = false;
	        double testingValues[] = GEPDependentVariable.getDependentVariableValues();
	  	    if (testingValues != null)
	  	    {
		        state.output.println("\n***** TEST data results *****", Output.V_NO_GENERAL,statisticslog);	
  	    		int numErrors = 0;
  	  	    	// in test data we might get values that lead to errors when applying the model
  	  	    	// since there could be div by zero, etc.). So count those that will fail when
  	  	    	// evaluated with the model
  	            for (int i=0; i<testingValues.length; i++)
  	            {
  	              double computed = ind.eval(i);
  	              if (computed == Double.POSITIVE_INFINITY || computed == Double.NEGATIVE_INFINITY || Double.isNaN(computed))
  	            	numErrors++;
  	            }
  	  		    state.output.println("Number of Calculation Errors: " + numErrors + 
  	  		    		" out of " + testingValues.length + " test sets",Output.V_NO_GENERAL,statisticslog);
		        displayStatistics(state, species, ind);
	  	    }
	        // reset back to using the training values
	        GEPDependentVariable.useTrainingData = true;

	        // And also the final observed versus computed values for the 'best' of run model
	  	    // if desired
	  	    if (!noObserveredComputedDisplay)
	  	    {
	  		    state.output.println("Observed\tComputed",Output.V_NO_GENERAL,observedVsComputedlog);
	  	        double dependentVarValues[] = GEPDependentVariable.getDependentVariableValues();
	  		    for (int i=0; i<dependentVarValues.length; i++)
	  		    {
	  		    	double observed = dependentVarValues[i];
	  		    	double computed = ind.eval(i);
	  		        state.output.println(Double.toString(observed)+"\t"+Double.toString(computed),Output.V_NO_GENERAL,observedVsComputedlog);
	  		    }
	  		    // and if testing data available add it to the end of this data or to it's own file
		  	    if (testingValues != null)
		  	    {
		  	    	GEPDependentVariable.useTrainingData = false;
			        state.output.println("", Output.V_NO_GENERAL,statisticslog);	
			        dependentVarValues = GEPDependentVariable.getDependentVariableValues();
		  		    for (int i=0; i<dependentVarValues.length; i++)
		  		    {
		  		    	double observed = dependentVarValues[i];
		  		    	double computed = ind.eval(i);
		  		        state.output.println(Double.toString(observed)+"\t"+Double.toString(computed),
		  		        		Output.V_NO_GENERAL, observedVsComputedTestlog);
		  		    }
			        // reset back to using the training values
		  		  GEPDependentVariable.useTrainingData = true;
		  	    }
	  	    }
        }        
    }
    
    void displayStatistics(EvolutionState state, GEPSpecies species, GEPIndividual ind)
    {
        // if classification or boolean problem then show the confusion matrix as well
  	    if (species.problemType == GEPSpecies.PT_CLASSIFICATION || species.problemType == GEPSpecies.PT_LOGICAL)
        {
  		    state.output.println("Confusion Matrix: ",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("                Predicted Value\n               |  Yes\t|  No\n               |----------------",Output.V_NO_GENERAL,statisticslog);
  		    int confusionMatrix[] = GEPFitnessFunction.getConfusionMatrixValues(ind);
  		    state.output.println("            Yes|  "+confusionMatrix[0]+"\t|  "+confusionMatrix[1],Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("Actual Value   |----------------",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("            No |  "+confusionMatrix[2]+"\t|  "+confusionMatrix[3],Output.V_NO_GENERAL,statisticslog);	  		    
  		    state.output.println("               |----------------",Output.V_NO_GENERAL,statisticslog);
        }
  	    else
  	    {
  		    state.output.println("Statistics: ",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("MSE:  "+GEPFitnessFunction.MSErawFitness(ind)+
                                 "    \tRAE:  "+GEPFitnessFunction.RAErawFitness(ind)+	  		    		             
	                             "    \tRSE:  "+GEPFitnessFunction.RSErawFitness(ind)  		    		             
    		             ,Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("RMSE: "+GEPFitnessFunction.RMSErawFitness(ind)+
    		                     "    \tMAE:  "+GEPFitnessFunction.MAErawFitness(ind)+	  		    		             
	    		                 "    \tRRSE: "+GEPFitnessFunction.RRSErawFitness(ind)  		    		             
  		    		             ,Output.V_NO_GENERAL,statisticslog);  	    	
  	    }
        state.output.println("", Output.V_NO_GENERAL,statisticslog);	
    }
}
