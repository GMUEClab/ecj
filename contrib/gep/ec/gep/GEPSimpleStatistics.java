/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.gep;
import ec.*;
import ec.steadystate.*;
import java.io.IOException;
import ec.util.*;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import jscl.math.Expression;
import jscl.text.ParseException;

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
 <tr><td valign=top><i>base.</i><tt>number-of-best-to-log</tt><br>
 <font size=-1>int (>= 1))</font></td>
 <td valign=top>(number of best individuals to display in final results of run; normally 1)</td></tr>
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
    public static final String P_NUMBER_OF_BEST_TO_LOG = "number-of-best-to-log";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";

    /** The Statistics' log */
    public int statisticslog;
    public int observedVsComputedlog;
    public int observedVsComputedTestlog;
    public boolean noObserveredComputedDisplay = false;;
    public String detailToLog = "change";
    public int numberOfBestToLog = 1;  // for final results display this many of the top individuals 

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
        
        numberOfBestToLog = state.parameters.getIntWithDefault(base.push(P_NUMBER_OF_BEST_TO_LOG), 
        		                                               base.push(P_NUMBER_OF_BEST_TO_LOG), 1);
        if (numberOfBestToLog < 1) // can't be less than 1
        	numberOfBestToLog = 1;
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
        for (int i=1; i<seeds.length; i++)
        	seedStr = seedStr + ", " + String.valueOf(seeds[i]);
        state.output.println("GENERAL PARAMETERS", Output.V_NO_GENERAL,statisticslog);
        String runtimeargs = "";
        String runtimeargsarray[] = state.runtimeArguments;
        if (runtimeargsarray != null)
	        for (int i=0; i<runtimeargsarray.length; i++)
	        	runtimeargs = runtimeargsarray[i] + " ";
        state.output.println("Arguments used in this run: " + runtimeargs, Output.V_NO_GENERAL,statisticslog);
        state.output.println("Maximum number of generations in this run: " + state.numGenerations,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Size of population in this run: " + state.population.subpops[0].individuals.length,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Number of Chromosomes per individual: " + species.numberOfChromosomes,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Number of genes per chromosome: " + species.numberOfGenes,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Size of gene head: " + species.headSize,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Seed(s) used in this job: " + seedStr,Output.V_NO_GENERAL,statisticslog);
        state.output.println("Problem type: " + species.problemTypeName,Output.V_NO_GENERAL,statisticslog);
        if (species.problemType == GEPSpecies.PT_CLASSIFICATION)
        	state.output.println("Classification rounding threshold: " + GEPIndividual.getThreshold(),Output.V_NO_GENERAL,statisticslog);        	
        Parameter p = new Parameter("eval.problem.fitness-function");
        String fitnessFunction = state.parameters.getStringWithDefault(p, p, "SPECIFIED IN USER PROGRAM");
        state.output.println("Fitness function: " + fitnessFunction,Output.V_NO_GENERAL,statisticslog);
        if (!fitnessFunction.equals("SPECIFIED IN USER PROGRAM"))
        {
          // might be some parameters ... if so print them
            p = new Parameter("eval.problem.fitness-function-arg0");
            if (state.parameters.exists(p))
            {
          	  double d = state.parameters.getDoubleWithDefault(p, p, 0.0);
                state.output.println("Fitness function param 1: " + d,Output.V_NO_GENERAL,statisticslog);
            }
            p = new Parameter("eval.problem.fitness-function-arg1");
            if (state.parameters.exists(p))
            {
          	  double d = state.parameters.getDoubleWithDefault(p, p, 0.0);
                state.output.println("Fitness function param 2: " + d,Output.V_NO_GENERAL,statisticslog);
            }
        }
        state.output.println("",Output.V_NO_GENERAL,statisticslog);
            }

    /** Logs the best individual of the generation. Will log the best individual if:
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
                state.output.println("BEST INDIVIDUAL OF GENERATION",Output.V_NO_GENERAL,statisticslog);
                state.output.println("Generation: " + state.generation,Output.V_NO_GENERAL,statisticslog);
                best_i[x].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
            }
        }     
    }

    static private class BestGEPIndividualComparator implements SortComparatorL
    {
     Individual[] inds;
     public BestGEPIndividualComparator(Individual[] inds) {super(); this.inds = inds;}
     public boolean lt(long a, long b)
        { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
     public boolean gt(long a, long b)
        { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
    }

    /** 
     * Logs the best individual(s) of the run showing details 
     */
    public void finalStatistics(final EvolutionState state, final int result)
    {
        super.finalStatistics(state,result);
        
        for(int x=0;x<state.population.subpops.length;x++ )
        {
          // make sure number of best individuals to log is not bigger than size of population
          int popSize = state.population.subpops[x].individuals.length;
          if (numberOfBestToLog > popSize)
        	  numberOfBestToLog = popSize;
          // for now we just print the individual(s) with the best fitness value(s)
          //
          // get the n best individuals
          GEPIndividual bestN[] = new GEPIndividual[numberOfBestToLog];
          if (numberOfBestToLog == 1)
          	bestN[0] = (GEPIndividual)best_of_run[x]; // already have the best one
          else
          { // otherwise get the best n from the last generation ... the top individual will always be in the
          	// last generation; the next ones we assume are also in the last generation; this may not be completely
            // since some very good ones might have been lost in the evolution ... however, if one sets the 
            // number of 'elites' to propagate to each generation to be the same (or greater) than the number
            // of 'best' individuals to display in the final stats then we will have the actual best n.
        	// We'll need to sort them...
            int[] orderedPop = new int[popSize];
            for(int j=0; j<popSize; j++) 
            	orderedPop[j] = j;

            // sort the best so far where "<" means "not as fit as"
            QuickSort.qsort(orderedPop, new BestGEPIndividualComparator(state.population.subpops[x].individuals));
            // load the best n individuals from the sorted population .. in increasing order of fitness so take from end!
            Individual[] oldinds = state.population.subpops[x].individuals;
            for(int j=0; j<numberOfBestToLog; j++)
            	bestN[j] = (GEPIndividual)(oldinds[orderedPop[popSize-1-j]]);
          }
          
          for (int bn=0; bn<bestN.length; bn++)
          {  
	        GEPIndividual best = bestN[bn];
            if (bn == 0)
            {	state.output.println("\nBEST INDIVIDUAL 1",Output.V_NO_GENERAL,statisticslog);
            	state.output.println("Found at Generation: " + best_of_run_generation[x], Output.V_NO_GENERAL,statisticslog);
            }
            else
            	state.output.println("\n\nBEST INDIVIDUAL "+ (bn+1),Output.V_NO_GENERAL,statisticslog);
            state.output.println("Raw Fitness: " + ((1000.0/best.fitness.fitness())-1.0), Output.V_NO_GENERAL,statisticslog);
            best.printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
        
	        // also print some stats about the expression ... its size, number of terminals used and
	        // the number of times each variable and function is used
	        int numChromosomes = best.chromosomes.length;
	        // print stuff for each chromosome in the individual
	        state.output.println("MODEL COMPOSITION",Output.V_NO_GENERAL,statisticslog);
	        for (int n=0; n<numChromosomes; n++)
	        {
	        	int j = n+1;
	        	if (n>0) state.output.println("", Output.V_NO_GENERAL,statisticslog);
	        	if (numChromosomes>1)
	        		state.output.println("Chromosome " + j + ":", Output.V_NO_GENERAL,statisticslog);
	        	GEPChromosome chromosome = best.chromosomes[n];
		        state.output.println("Size of program: " + chromosome.size(), 
   		             Output.V_NO_GENERAL,statisticslog);
	        	int counts[] = chromosome.variableUseageCounts();
	        	int countsNotZero = 0;
	        	for (int i=0; i<counts.length; i++)  
	        		if (counts[i] > 0) countsNotZero++;

		        state.output.print("Variables used(variable count) " + countsNotZero + ": ", Output.V_NO_GENERAL,statisticslog);
	        	GEPSymbolSet ss = ((GEPSpecies)state.population.subpops[x].species).symbolSet;
	        	boolean first = true;
	        	for (int i=0; i<counts.length; i++)
	        	{   
	        		if (counts[i] > 0)
	        		{
	        			if (!first)
	        				state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
	        			String terminalName = ss.symbols[ss.terminals[i]].symbol;
	        			state.output.print(terminalName +" " + counts[i], 
	   	            		           Output.V_NO_GENERAL,statisticslog);
	        			first = false;
	        		}
	        	}
		        HashMap fcounts = chromosome.functionUseageCounts();
		        Set countsSet = fcounts.entrySet();
		        state.output.print("\nFunctions used(function count) " + countsSet.size() + ": ", Output.V_NO_GENERAL,statisticslog);
		        first = true;
		        for (Iterator iter = countsSet.iterator(); iter.hasNext ();) 
		        {
		        	if (!first) 
		        		state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
		        	first = false;
		        	Map.Entry e = (Map.Entry)iter.next();
	   	            String key = (String)e.getKey();
		            String value = ((Integer)e.getValue()).toString();
	   	            state.output.print(key +" " + value, 
	        		           Output.V_NO_GENERAL,statisticslog);
		        }

		        if (GEPIndividual.simplifyExpressions)
		        {
			        // print the functions and variables (and count) used in the simplified MATH expression
					String mathExpression = chromosome.genotypeToStringForHumansMathExpression();
					String mathExpressionSimplified = "";
					try
					{ mathExpressionSimplified = Expression.valueOf(mathExpression).simplify().toString();
					}
					catch (ParseException e)
					{}
	
			        displaySimplifiedMathExpressionVariableFunctionCounts(state, ss, "MATH", mathExpressionSimplified);
		        }

				// print Model quality measures
	        	GEPSpecies species = (GEPSpecies)state.population.subpops[0].species;
		        state.output.println("\n\nMODEL QUALITY MEASURES (TRAINING)", Output.V_NO_GENERAL,statisticslog);	
		        displayStatistics(state, species, best, n, true);
		        // Do the same thing for the test data if there is any
		        double testingValues[] = GEPDependentVariable.testingData.getDependentVariableValues(n);
		  	    if (testingValues != null)
		  	    {
			        state.output.println("\nMODEL QUALITY MEASURES (TEST)", Output.V_NO_GENERAL,statisticslog);	
	  	    		int numErrors = 0;
	  	  	    	// in test data we might get values that lead to errors when applying the model
	  	  	    	// since there could be division by zero, etc.). So count those that will fail when
	  	  	    	// evaluated with the model
	  	            for (int i=0; i<testingValues.length; i++)
	  	            {
	  	              double computed = best.eval(n, false, i);
	  	              if (computed == Double.POSITIVE_INFINITY || computed == Double.NEGATIVE_INFINITY || Double.isNaN(computed))
	  	            	numErrors++;
	  	            }
	  	  		    state.output.println("Number of Calculation Errors: " + numErrors + 
	  	  		    		" out of " + testingValues.length + " test sets",Output.V_NO_GENERAL,statisticslog);
			        displayStatistics(state, species, best, n, false);
		  	    }
		        // reset back to using the training values
	
		        // And also the final observed versus computed values for the 'best' of run model
		  	    // if desired
		  	    if (!noObserveredComputedDisplay)
		  	    {
		  		    state.output.println("OBSERVED	AND COMPUTED (TRAINING)",Output.V_NO_GENERAL,observedVsComputedlog);
		  		    state.output.println("#Observed GEP-Model",Output.V_NO_GENERAL,observedVsComputedlog);
		  	        double dependentVarValues[] = GEPDependentVariable.trainingData.getDependentVariableValues(n);
		  		    for (int i=0; i<dependentVarValues.length; i++)
		  		    {
		  		    	double observed = dependentVarValues[i];
		  		    	double computed = best.eval(n, true, i);
		  		        state.output.println(Double.toString(observed)+"\t"+Double.toString(computed),Output.V_NO_GENERAL,observedVsComputedlog);
		  		    }
		  		    // and if testing data available add it to the end of this data or to it's own file
			  	    if (testingValues != null)
			  	    {
				        state.output.println("", Output.V_NO_GENERAL,statisticslog);	
			  		    state.output.println("OBSERVED	AND COMPUTED (TEST)",Output.V_NO_GENERAL,observedVsComputedlog);
			  		    state.output.println("#Observed GEP-Model",Output.V_NO_GENERAL,observedVsComputedlog);
				        dependentVarValues = GEPDependentVariable.testingData.getDependentVariableValues(n);
			  		    for (int i=0; i<dependentVarValues.length; i++)
			  		    {
			  		    	double observed = dependentVarValues[i];
			  		    	double computed = best.eval(n, false, i);
			  		        state.output.println(Double.toString(observed)+"\t"+Double.toString(computed),
			  		        		Output.V_NO_GENERAL, observedVsComputedTestlog);
			  		    }
			  	    }
		  		    state.output.println("#----------",Output.V_NO_GENERAL,observedVsComputedlog);
		  	    }
	        }
          }
        }        
    }
    
    void displayStatistics(EvolutionState state, GEPSpecies species, GEPIndividual ind, int chromosomeNum, boolean useTrainingdata)
    {
        // if classification or boolean problem then show the confusion matrix as well
  	    if (species.problemType == GEPSpecies.PT_CLASSIFICATION || species.problemType == GEPSpecies.PT_LOGICAL)
        {
  		    state.output.println("Confusion Matrix: ",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("                Predicted Value\n               |  Yes  |  No   \n               |----------------",Output.V_NO_GENERAL,statisticslog);
  		    int confusionMatrix[] = GEPFitnessFunction.getConfusionMatrixValues(useTrainingdata, ind, 0);
  		    state.output.println("           Yes |"+String.format("%1$6d", confusionMatrix[0])+
  		    		             " |"+String.format("%1$6d", confusionMatrix[1]),Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("Actual Value   |----------------",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("           No  |"+String.format("%1$6d", confusionMatrix[2])+
 		             " |"+String.format("%1$6d", confusionMatrix[3]),Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("               |----------------",Output.V_NO_GENERAL,statisticslog);
        } 
  	    else
  	    {
  		    state.output.println("Statistics: ",Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("MSE:  "+GEPFitnessFunction.MSErawFitness(useTrainingdata, ind, chromosomeNum)+
                                 "    \tRAE:  "+GEPFitnessFunction.RAErawFitness(useTrainingdata, ind, chromosomeNum)+	  		    		             
	                             "    \tRSE:  "+GEPFitnessFunction.RSErawFitness(useTrainingdata, ind, chromosomeNum)  		    		             
    		             ,Output.V_NO_GENERAL,statisticslog);
  		    state.output.println("RMSE: "+GEPFitnessFunction.RMSErawFitness(useTrainingdata, ind, chromosomeNum)+
    		                     "    \tMAE:  "+GEPFitnessFunction.MAErawFitness(useTrainingdata, ind, chromosomeNum)+	  		    		             
	    		                 "    \tRRSE: "+GEPFitnessFunction.RRSErawFitness(useTrainingdata, ind, chromosomeNum)  		    		             
  		    		             ,Output.V_NO_GENERAL,statisticslog);  
  		    state.output.println("Corr Coeff: "+GEPFitnessFunction.CCrawFitness(useTrainingdata, ind, chromosomeNum)
 		             ,Output.V_NO_GENERAL,statisticslog);  
  	    }
        state.output.println("", Output.V_NO_GENERAL,statisticslog);	
    }
    
    void displaySimplifiedMathExpressionVariableFunctionCounts(EvolutionState state, GEPSymbolSet ss, String expressionType, String mathExpressionSimplified)
    {
		int numberOfVariables = 0, numberOfFunctions = 0;
		String partExpression = "";
		
		if (mathExpressionSimplified.equals(""))
		{
			state.output.println("\n\nSize of program ("+ expressionType+" simplified): unknown ... expression simplification failed", 
		             Output.V_NO_GENERAL,statisticslog);
			return;
		}
		// count the variables in the expression
        HashMap varCounts = new HashMap();
        partExpression = mathExpressionSimplified;
        String varNames[] = new String[ss.numberOfTerminals];
        for (int i=0; i<ss.numberOfTerminals; i++)
        {
        	GEPSymbol sym = ss.symbols[ss.terminals[i]];
        	String varName = sym.symbol;
        	// must simplify the var name since sometimes at NRC we use var names like T[t-1] and this
        	// gets simplified to T[-1+t]
        	try
        	{varName = Expression.valueOf(varName).simplify().toString();}
        	catch (ParseException e)
        	{}
        	// must quote every character in case it is a special character of regex
        	// use \Q ... \E of regex
        	varNames[i] = varName;
        }
        // sort them with longest first so counting/replacement doesn't have to worry about
        // names like x1 and x11 ... will count and remove all x11 instances first .. process
        // the longer names first. Also we replace the variables by a # character so it won't
        // get matched again and it keeps the functions separated
        Arrays.sort(varNames);
        for (int i=ss.numberOfTerminals-1; i>=0; i--)
        {
        	String regex = "\\Q"+varNames[i]+"\\E";
        	String expressionWithoutVariable = partExpression.replaceFirst(regex, "#");
        	while (!expressionWithoutVariable.equals(partExpression))
        	{
	        	numberOfVariables++;
				Integer cnt = (Integer)varCounts.get(varNames[i]);
				if (cnt==null)
					varCounts.put(varNames[i], new Integer(1));
				else
					varCounts.put(varNames[i], new Integer((cnt.intValue())+1));
				partExpression = expressionWithoutVariable;
				expressionWithoutVariable = partExpression.replaceFirst(regex, "#");
        	}
        }
		
		// count the functions used in the expression
        // may be some special functions added by the simplification ... e.g. ^ (power), ...
        String SimplificationFunctions[] = {"^", "**"};
        HashMap functionCounts = new HashMap();
        String functionNames[] = new String[ss.numberOfFunctions+SimplificationFunctions.length]; // include functions which simplify might introduce
        for (int i=0; i<ss.numberOfFunctions; i++)
        {
        	GEPSymbol sym = ss.symbols[ss.functions[i]];
        	functionNames[i] = sym.symbol;
        }
        for (int i=0; i<SimplificationFunctions.length; i++)
        	functionNames[ss.numberOfFunctions+i]= SimplificationFunctions[i]; // for the simplification functions
        // sort them with longest first so counting/replacement doesn't have to worry about
        // names like log and loge ... will count and remove all loge instances first .. process
        // the longer names first
        Arrays.sort(functionNames);
        for (int i=functionNames.length-1; i>=0; i--)
        {
        	String regex = "\\Q"+functionNames[i]+"\\E";
        	String expressionWithoutFunction = partExpression.replaceFirst(regex, "");
        	while (!expressionWithoutFunction.equals(partExpression))
        	{        	
        		numberOfFunctions++;
				Integer cnt = (Integer)functionCounts.get(functionNames[i]);
				if (cnt==null)
					functionCounts.put(functionNames[i], new Integer(1));
				else
					functionCounts.put(functionNames[i], new Integer((cnt.intValue())+1));
				partExpression = expressionWithoutFunction;
				expressionWithoutFunction = partExpression.replaceFirst(regex, "");
        	}
        }
		
		state.output.println("\n\nSize of program ("+ expressionType+" simplified): " + (numberOfVariables+numberOfFunctions), 
                 Output.V_NO_GENERAL,statisticslog);
		
        Set countsSet = varCounts.entrySet();
        state.output.print("Variables used(variable count) " + countsSet.size() + ": ", Output.V_NO_GENERAL,statisticslog);
        boolean first = true;
        for (Iterator iter = countsSet.iterator(); iter.hasNext ();) 
        {
        	if (!first) 
        		state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
        	first = false;
        	Map.Entry e = (Map.Entry)iter.next();
	            String key = (String)e.getKey();
            String value = ((Integer)e.getValue()).toString();
	            state.output.print(key +" " + value, 
    		           Output.V_NO_GENERAL,statisticslog);
        }
        countsSet = functionCounts.entrySet();
        state.output.print("\nFunctions used(function count) " + countsSet.size() + ": ", Output.V_NO_GENERAL,statisticslog);
        first = true;
        for (Iterator iter = countsSet.iterator(); iter.hasNext ();) 
        {
        	if (!first) 
        		state.output.print(", ", Output.V_NO_GENERAL,statisticslog);
        	first = false;
        	Map.Entry e = (Map.Entry)iter.next();
	            String key = (String)e.getKey();
            String value = ((Integer)e.getValue()).toString();
	            state.output.print(key +" " + value, 
    		           Output.V_NO_GENERAL,statisticslog);
        }

    }
}
