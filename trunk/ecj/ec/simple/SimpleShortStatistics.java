/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;
import java.io.*;
import ec.util.*;
import ec.eval.*;

/* 
 * SimpleShortStatistics.java
 * 
 * Created: Tue Jun 19 15:08:29 EDT 2001
 * By: Sean Luke
 */

/**
 * A Simple-style statistics generator, intended to be easily parseable with
 * awk or other Unix tools.  Prints fitness information,
 * one generation (or pseudo-generation) per line.
 * If gather-full is true, then timing information, number of nodes
 * and depths of trees, etc. are also given.  No final statistics information
 * is given.
 *
 * <p> Each line represents a single generation.  
 * The first items on a line are always:
 <ul>
 <li> The generation number
 <li> (if gather-full) how long initialization took in milliseconds, or how long the previous generation took to breed to form this generation
 <li> (if gather-full) how many bytes initialization took, or how how many bytes the previous generation took to breed to form this generation.  This utilization is an approximation only, made by the Java system, and does not take into consideration the possibility of garbage collection (which might make the number negative).
 <li> (if gather-full) How long evaluation took in milliseconds this generation
 <li> (if gather-full) how many bytes evaluation took this generation.  This utilization is an approximation only, made by the Java system, and does not take into consideration the possibility of garbage collection (which might make the number negative).
 </ul>

 <p>Then, (if gather-subpops) the following items appear, per subpopulation:
 <ul>
 <li> (if gather-full) The average size of an individual this generation
 <li> (if gather-full) The average size of an individual so far in the run
 <li> The mean fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation so far in the run
 <li> (if gather-full) The size of the best individual this generation
 <li> (if gather-full) The size of the best individual so far in the run
 </ul>
 
 <p>Then the following items appear, for the whole population:
 <ul>
 <li> (if gather-full) The average size of an individual this generation
 <li> (if gather-full) The average size of an individual so far in the run
 <li> The mean fitness this generation
 <li> The best fitness this generation
 <li> The best fitness so far in the run
 <li> (if gather-full) The size of the best individual this generation
 <li> (if gather-full) The size of the best individual so far in the run
 </ul>


 Compressed files will be overridden on restart from checkpoint; uncompressed files will be 
 appended on restart.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>gather-full</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we full statistics on individuals (will run slower, though the slowness is due to off-line processing that won't mess up timings)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>gather-subpops</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we full statistics on individuals (will run slower, though the slowness is due to off-line processing that won't mess up timings)</td></tr>
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleShortStatistics extends Statistics // implements ProvidesBestSoFar
    {
    public Individual[] getBestSoFar() { return best_of_run; }
        
    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";

    /** The Statistics' log */
    public int statisticslog;

    /** compress? */
    public static final String P_COMPRESS = "gzip";

    public static final String P_FULL = "gather-full";

    public static final String P_DO_SUBPOPS = "gather-subpops";

    public boolean doFull;
    public boolean doSubpops;

    public Individual[] best_of_run;
    public long totalSizeSoFar[];
	public long totalIndsSoFar[];

    // timings
    public long lastTime;
    
    // usage
    public long lastUsage;
    
    public SimpleShortStatistics() { /*best_of_run = null;*/ statisticslog = 0; /* stdout */ }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        File statisticsFile = state.parameters.getFile(
            base.push(P_STATISTICS_FILE),null);

        if (statisticsFile!=null) try
                                      {
                                      statisticslog = state.output.addLog(statisticsFile,
                                          !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
                                          state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
                                      }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
        doFull = state.parameters.getBoolean(base.push(P_FULL),null,false);
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS),null,false);
        }


    public void preInitializationStatistics(final EvolutionState state)
        {
        super.preInitializationStatistics(state);
        
        if (doFull) 
            {
            Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            lastUsage = r.totalMemory() - r.freeMemory();
            }
        }
    
    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
        
        // set up our best_of_run array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        best_of_run = new Individual[state.population.subpops.length];
        
        // print out our generation number
        state.output.print("0 ", statisticslog);

        // gather timings       
		totalSizeSoFar = new long[state.population.subpops.length];
		totalIndsSoFar = new long[state.population.subpops.length];

        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            state.output.print("" + (curU-lastUsage) + " ",  statisticslog);            
            }
        }

    public void preBreedingStatistics(final EvolutionState state)
        {
        super.preBreedingStatistics(state);
        if (doFull) 
            {
            Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            lastUsage = r.totalMemory() - r.freeMemory();
            }
        }

    public void postBreedingStatistics(final EvolutionState state) 
        {
        super.postBreedingStatistics(state);
        state.output.print("" + (state.generation + 1) + " ", statisticslog); // 1 because we're putting the breeding info on the same line as the generation it *produces*, and the generation number is increased *after* breeding occurs, and statistics for it

        // gather timings
        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            state.output.print("" + (curU-lastUsage) + " ",  statisticslog);            
            }
        }

    public void preEvaluationStatistics(final EvolutionState state)
        {
        super.preEvaluationStatistics(state);
        if (doFull) 
            {
            Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            lastUsage = r.totalMemory() - r.freeMemory();
            }
        }




    /** Prints out the statistics, but does not end with a println --
        this lets overriding methods print additional statistics on the same line */
    protected void _postEvaluationStatistics(final EvolutionState state)
        {
        // gather timings
        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            state.output.print("" + (curU-lastUsage) + " ",  statisticslog);            
            }
			
			
			
			
		// gather per-subpopulation statistics
		int subpops = state.population.subpops.length;				// number of supopulations
		long[] totalSizeThisGen = new long[subpops];				// per-subpop total size of individuals this generation
        Individual[] best_i = new Individual[subpops];				// per-subpop best individual this generation
		double meanFitnessThisGen[] = new double[subpops];			// per-subpop mean fitness this generation
		
																	// ALSO THERE IS:
											// totalIndsSoFar		// total individuals so far per subpopulation
											// totalSizeSoFar		// total size of individuals so far per subpopulation
											// best_so_far			// best individual so far per subpopulation
		
        for(int x=0;x<subpops;x++)
            {
			int numInds = state.population.subpops[x].individuals.length;
			
			totalIndsSoFar[x] += state.population.subpops[x].individuals.length;
			for(int y=0;y<numInds;y++)
				{
				long size = state.population.subpops[x].individuals[y].size();
				totalSizeThisGen[x] += size;
				totalSizeSoFar[x] += size;
				}

            if (doFull)
                {
                if (doSubpops) state.output.print("" + ((double)totalSizeThisGen[x])/numInds + " ",  statisticslog);			// mean size this gen
                if (doSubpops) state.output.print("" + ((double)totalSizeSoFar[x])/totalIndsSoFar[x] + " ",  statisticslog);	// mean size so far
                }
                    
            for(int y=0;y<numInds;y++)
                {
                // find the best individual
                if (best_i[x]==null ||
                    state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];

                // sum up mean fitness for population
                meanFitnessThisGen[x] += state.population.subpops[x].individuals[y].fitness.fitness();
                }
            
            // compute fitness stats
            meanFitnessThisGen[x] /= numInds;
			
            if (doSubpops) state.output.print("" + meanFitnessThisGen[x] + " " + best_i[x].fitness.fitness() + " ", statisticslog);   // mean and best fitness this gen

            // now test to see if it's the new best_of_run[x]
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());
            
            if (doSubpops) state.output.print("" + best_of_run[x].fitness.fitness() + " ", statisticslog);							// best fitness so far

			if( doFull )
				{
				if (doSubpops) state.output.print("" + (double)(best_i[x].size()) + " " + (double)(best_of_run[x].size()) + " ", statisticslog);		// best size this gen and so far
				}
            }
  
  
  
		// Now gather per-Population statistics
		long popTotalInds = 0;
		long popTotalIndsSoFar = 0;
		long popTotalSize = 0;
		long popTotalSizeSoFar = 0;
		double popMeanFitness = 0;
		Individual popBestIndividual = best_i[0];
		Individual popBestIndividualSoFar = best_of_run[0];
		
        for(int x=0;x<subpops;x++)
			{
			popTotalIndsSoFar += totalIndsSoFar[x];
			popTotalInds += state.population.subpops[x].individuals.length;
			popTotalSize += totalSizeThisGen[x];
			popTotalSizeSoFar += totalSizeSoFar[x];
			popMeanFitness += meanFitnessThisGen[x];
			if (x > 0 && best_i[x].fitness.betterThan(popBestIndividual.fitness))
				popBestIndividual = best_i[x];
			if (x > 0 && best_of_run[x].fitness.betterThan(popBestIndividualSoFar.fitness))
				popBestIndividualSoFar = best_of_run[x];
			}
		popMeanFitness /= subpops;		// average out
			
		if (doFull)
			{
			state.output.print("" + popTotalSize / popTotalInds  + " " , statisticslog);						// mean size of pop this gen
			state.output.print("" + popTotalSizeSoFar / popTotalIndsSoFar + " " , statisticslog);				// mean size of pop so far
			}
		state.output.print("" + popMeanFitness + " " , statisticslog);											// mean fitness of pop this gen
		state.output.print("" + (double)(popBestIndividual.fitness.fitness()) + " " , statisticslog);			// best fitness of pop this gen
		state.output.print("" + (double)(popBestIndividualSoFar.fitness.fitness()) + " " , statisticslog);		// best fitness of pop so far
		if (doFull)
			{
			state.output.print("" + (double)(popBestIndividual.size()) + " " , statisticslog);					// size of best ind of pop this gen
			state.output.print("" + (double)(popBestIndividualSoFar.size()) + " " , statisticslog);				// size of best ind of pop so far
			}
		
		// we're done!
        }




    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        _postEvaluationStatistics(state);
        state.output.println("", statisticslog);
        }

    }
