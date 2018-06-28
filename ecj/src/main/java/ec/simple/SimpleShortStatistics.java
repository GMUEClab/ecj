/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;
import java.io.*;
import ec.util.*;

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
 * If do-time is true, then timing information is also given.  If do-size is true, then size information is also given.
 * No final statistics information is provided.  You can also set SimpleShortStatistics to only output every *modulus* generations
 * to keep the tally shorter.  And you can gzip the statistics file.
 *
 * <p> Each line represents a single generation.  
 * The first items on a line are always:
 <ul>
 <li> The generation number
 <li> (if do-time) how long initialization took in milliseconds, or how long the previous generation took to breed to form this generation
 <li> (if do-time) How long evaluation took in milliseconds this generation
 </ul>

 <p>Then, (if do-subpops) the following items appear, once per each subpopulation:
 <ul>
 <li> (if do-size) The average size of an individual this generation
 <li> (if do-size) The average size of an individual so far in the run
 <li> (if do-size) The size of the best individual this generation
 <li> (if do-size) The size of the best individual so far in the run
 <li> The mean fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation so far in the run
 </ul>
 
 <p>Then the following items appear, for the whole population:
 <ul>
 <li> (if do-size) The average size of an individual this generation
 <li> (if do-size) The average size of an individual so far in the run
 <li> (if do-size) The size of the best individual this generation
 <li> (if do-size) The size of the best individual so far in the run
 <li> The mean fitness this generation
 <li> The best fitness this generation
 <li> The best fitness so far in the run
 </ul>


 Compressed files will be overridden on restart from checkpoint; uncompressed files will be 
 appended on restart.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>modulus</tt><br>
 <font size=-1>integer >= 1 (default)</font></td>
 <td valign=top>(How often (in generations) should we print a statistics line?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>do-time</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(print timing information?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>do-size</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(print sizing information?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>do-subpops</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(print information on a per-subpop basis as well as per-population?)</td></tr>
 </table>
 * @author Sean Luke
 * @version 2.0 
 */

public class SimpleShortStatistics extends Statistics
    {
    public static final String P_STATISTICS_MODULUS = "modulus";
    public static final String P_COMPRESS = "gzip";
    public static final String P_FULL = "gather-full";
    public static final String P_DO_SIZE = "do-size";
    public static final String P_DO_TIME = "do-time";
    public static final String P_DO_SUBPOPS = "do-subpops";
    public static final String P_STATISTICS_FILE = "file";
        
    public int statisticslog = 0;  // stdout by default
    public int modulus;
    public boolean doSize;
    public boolean doTime;
    public boolean doSubpops;

    public Individual[] bestSoFar;
    public long[] totalSizeSoFar;
    public long[] totalIndsSoFar;
    public long[] totalIndsThisGen;                         // total assessed individuals
    public long[] totalSizeThisGen;                         // per-subpop total size of individuals this generation
    public double[] totalFitnessThisGen;                    // per-subpop mean fitness this generation
    public Individual[] bestOfGeneration;   // per-subpop best individual this generation
        
    // timings
    public long lastTime;
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        File statisticsFile = state.parameters.getFile(
            base.push(P_STATISTICS_FILE),null);

        modulus = state.parameters.getIntWithDefault(base.push(P_STATISTICS_MODULUS), null, 1);


        if (silentFile)
            {
            statisticslog = Output.NO_LOGS;
            }
        else if (statisticsFile!=null) 
            {
            try
                {
                statisticslog = state.output.addLog(statisticsFile,
                    !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
                    state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
            }
        else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));

        doSize = state.parameters.getBoolean(base.push(P_DO_SIZE),null,false);
        doTime = state.parameters.getBoolean(base.push(P_DO_TIME),null,false);
        if (state.parameters.exists(base.push(P_FULL), null))
            {
            state.output.warning(P_FULL + " is deprecated.  Use " + P_DO_SIZE + " and " + P_DO_TIME + " instead.  Also be warned that the table columns have been reorganized. ", base.push(P_FULL), null);
            boolean gather = state.parameters.getBoolean(base.push(P_FULL), null, false);
            doSize = doSize || gather;
            doTime = doTime || gather;
            }
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS),null,false);
        }


    public Individual[] getBestSoFar() { return bestSoFar; }

    public void preInitializationStatistics(final EvolutionState state)
        {
        super.preInitializationStatistics(state);
        boolean output = (state.generation % modulus == 0);
       
        if (output && doTime) 
            {
            // Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            }
        }
    
    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
        boolean output = (state.generation % modulus == 0);
        
        // set up our bestSoFar array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        bestSoFar = new Individual[state.population.subpops.size()];
        
        // print out our generation number
        if (output) state.output.print("0 ", statisticslog);

        // gather timings       
        totalSizeSoFar = new long[state.population.subpops.size()];
        totalIndsSoFar = new long[state.population.subpops.size()];

        if (output && doTime)
            {
            //Runtime r = Runtime.getRuntime();
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            }
        }

    public void preBreedingStatistics(final EvolutionState state)
        {
        super.preBreedingStatistics(state);
        boolean output = (state.generation % modulus == modulus - 1);
        if (output && doTime) 
            {
            //Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            }
        }

    public void postBreedingStatistics(final EvolutionState state) 
        {
        super.postBreedingStatistics(state);
        boolean output = (state.generation % modulus == modulus - 1);
        if (output) state.output.print("" + state.generation + " ", statisticslog); // 1 because we're putting the breeding info on the same line as the generation it *produces*, and the generation number is increased *after* breeding occurs, and statistics for it

        // gather timings
        if (output && doTime)
            {
            //Runtime r = Runtime.getRuntime();
            //long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            }
        }

    public void preEvaluationStatistics(final EvolutionState state)
        {
        super.preEvaluationStatistics(state);
        boolean output = (state.generation % modulus == 0);

        if (output && doTime) 
            {
            //Runtime r = Runtime.getRuntime();
            lastTime = System.currentTimeMillis();
            }
        }


    // This stuff is used by KozaShortStatistics

    protected void prepareStatistics(EvolutionState state) { }
    protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual) { }
    protected void printExtraSubpopStatisticsBefore(EvolutionState state, int subpop) { }
    protected void printExtraSubpopStatisticsAfter(EvolutionState state, int subpop) { }
    protected void gatherExtraPopStatistics(EvolutionState state, int subpop) { }
    protected void printExtraPopStatisticsBefore(EvolutionState state) { }
    protected void printExtraPopStatisticsAfter(EvolutionState state) { }
        
        
        

    /** Prints out the statistics, but does not end with a println --
        this lets overriding methods print additional statistics on the same line */
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        
        boolean output = (state.generation % modulus == 0);

        // gather timings
        if (output && doTime)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            }
                        
        int subpops = state.population.subpops.size();                          // number of supopulations
        totalIndsThisGen = new long[subpops];                                           // total assessed individuals
        bestOfGeneration = new Individual[subpops];                                     // per-subpop best individual this generation
        totalSizeThisGen = new long[subpops];                           // per-subpop total size of individuals this generation
        totalFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation
        double[] meanFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation


        prepareStatistics(state);

        // gather per-subpopulation statistics
                
        for(int x=0;x<subpops;x++)
            {                   
            for(int y = 0; y< state.population.subpops.get(x).individuals.size(); y++)
                {
                if (state.population.subpops.get(x).individuals.get(y).evaluated)               // he's got a valid fitness
                    {
                    // update sizes
                    long size = state.population.subpops.get(x).individuals.get(y).size();
                    totalSizeThisGen[x] += size;
                    totalSizeSoFar[x] += size;
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;
                                        
                    // update fitness
                    if (bestOfGeneration[x]==null ||
                        state.population.subpops.get(x).individuals.get(y).fitness.betterThan(bestOfGeneration[x].fitness))
                        {
                        bestOfGeneration[x] = state.population.subpops.get(x).individuals.get(y);
                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
                        }
            
                    // sum up mean fitness for population
                    totalFitnessThisGen[x] += state.population.subpops.get(x).individuals.get(y).fitness.fitness();
                                        
                    // hook for KozaShortStatistics etc.
                    gatherExtraSubpopStatistics(state, x, y);
                    }
                }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsBefore(state, x);
                        
            // print out optional average size information
            if (output && doSize && doSubpops)
                {
                state.output.print("" + (totalIndsThisGen[x] > 0 ? ((double)totalSizeThisGen[x])/totalIndsThisGen[x] : 0) + " ",  statisticslog);
                state.output.print("" + (totalIndsSoFar[x] > 0 ? ((double)totalSizeSoFar[x])/totalIndsSoFar[x] : 0) + " ",  statisticslog);
                state.output.print("" + (double)(bestOfGeneration[x].size()) + " ", statisticslog);
                state.output.print("" + (double)(bestSoFar[x].size()) + " ", statisticslog);
                }
                        
            // print out fitness information
            if (output && doSubpops)
                {
                state.output.print("" + meanFitnessThisGen[x] + " ", statisticslog);
                state.output.print("" + bestOfGeneration[x].fitness.fitness() + " ", statisticslog);
                state.output.print("" + bestSoFar[x].fitness.fitness() + " ", statisticslog);
                }

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsAfter(state, x);
            }
  
  
  
        // Now gather per-Population statistics
        long popTotalInds = 0;
        long popTotalIndsSoFar = 0;
        long popTotalSize = 0;
        long popTotalSizeSoFar = 0;
        double popMeanFitness = 0;
        double popTotalFitness = 0;
        Individual popBestOfGeneration = null;
        Individual popBestSoFar = null;
                
        for(int x=0;x<subpops;x++)
            {
            popTotalInds += totalIndsThisGen[x];
            popTotalIndsSoFar += totalIndsSoFar[x];
            popTotalSize += totalSizeThisGen[x];
            popTotalSizeSoFar += totalSizeSoFar[x];
            popTotalFitness += totalFitnessThisGen[x];
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness)))
                popBestOfGeneration = bestOfGeneration[x];
            if (bestSoFar[x] != null && (popBestSoFar == null || bestSoFar[x].fitness.betterThan(popBestSoFar.fitness)))
                popBestSoFar = bestSoFar[x];

            // hook for KozaShortStatistics etc.
            gatherExtraPopStatistics(state, x);
            }
                        
        // build mean
        popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out
                
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsBefore(state);

        // optionally print out mean size info
        if (output && doSize)
            {
            state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
            state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
            state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
            state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
            }
                
        // print out fitness info
        if (output)
            {
            state.output.print("" + popMeanFitness + " " , statisticslog);                                                                                  // mean fitness of pop this gen
            state.output.print("" + (double)(popBestOfGeneration.fitness.fitness()) + " " , statisticslog);                 // best fitness of pop this gen
            state.output.print("" + (double)(popBestSoFar.fitness.fitness()) + " " , statisticslog);                // best fitness of pop so far
            }
                        
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);
        }
    }
