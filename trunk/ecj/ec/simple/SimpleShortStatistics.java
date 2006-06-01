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

 <p>Then the following items appear, per subpopulation:
 <ul>
 <li> (if gather-full) The average size of an individual this generation
 <li> (if gather-full) The average size of an individual so far in the run
 <li> The mean fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation this generation
 <li> The best fitness of the subpopulation so far in the run
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
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleShortStatistics extends Statistics
    {
    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";

    /** The Statistics' log */
    public int statisticslog;

    /* The best individual we've found so far */
    //public Individual best_of_run;

    /** compress? */
    public static final String P_COMPRESS = "gzip";

    public static final String P_FULL = "gather-full";

    public boolean doFull;

    public Individual[] best_of_run_a;
    public long lengths[];

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
            statisticslog = state.output.addLog(statisticsFile,Output.V_NO_GENERAL-1,false,
                                                !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
                                                state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
            }
        catch (IOException i)
            {
            state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        doFull = state.parameters.getBoolean(base.push(P_FULL),null,false);
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
        best_of_run_a = new Individual[state.population.subpops.length];
        
        // print out our generation number
        state.output.print("0 ",Output.V_NO_GENERAL, statisticslog);

        // gather timings       
        if (doFull)
            {
            lengths = new long[state.population.subpops.length];
            for(int x=0;x<lengths.length;x++) lengths[x] = 0;
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ", Output.V_NO_GENERAL, statisticslog);
            state.output.print("" + (curU-lastUsage) + " ", Output.V_NO_GENERAL, statisticslog);            
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
        state.output.print("" + (state.generation + 1) + " ",Output.V_NO_GENERAL, statisticslog); // 1 because we're putting the breeding info on the same line as the generation it *produces*, and the generation number is increased *after* breeding occurs, and statistics for it

        // gather timings
        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ", Output.V_NO_GENERAL, statisticslog);
            state.output.print("" + (curU-lastUsage) + " ", Output.V_NO_GENERAL, statisticslog);            
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
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ", Output.V_NO_GENERAL, statisticslog);
            state.output.print("" + (curU-lastUsage) + " ", Output.V_NO_GENERAL, statisticslog);            
            }
        

        long lengthPerGen = 0;
        Individual[] best_i = new Individual[state.population.subpops.length];
        for(int x=0;x<state.population.subpops.length;x++)
            {
            if (doFull)
                {
                lengthPerGen = 0;
                for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                    {
                    long size = state.population.subpops[x].individuals[y].size();
                    lengthPerGen += size;
                    lengths[x] += size;
                    }

                state.output.print("" + ((double)lengthPerGen)/state.population.subpops[x].individuals.length + " ", Output.V_NO_GENERAL, statisticslog);

                state.output.print("" + ((double)lengths[x])/(state.population.subpops[x].individuals.length * (state.generation + 1)) + " ", Output.V_NO_GENERAL, statisticslog);
                }
                    
            // fitness information
            double meanFitness = 0.0;

            for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                {
                // best individual
                if (best_i[x]==null ||
                    state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];

                // mean fitness for population
                meanFitness += state.population.subpops[x].individuals[y].fitness.fitness();
                }
            
            // compute fitness stats
            meanFitness /= state.population.subpops[x].individuals.length;
            state.output.print("" + meanFitness + " " + best_i[x].fitness.fitness() + " ",
                               Output.V_NO_GENERAL, statisticslog);

            // now test to see if it's the new best_of_run_a[x]
            if (best_of_run_a[x]==null || best_i[x].fitness.betterThan(best_of_run_a[x].fitness))
                best_of_run_a[x] = (Individual)(best_i[x].clone());
            
            state.output.print("" + best_of_run_a[x].fitness.fitness() + " ",
                               Output.V_NO_GENERAL, statisticslog);

            if( doFull )
                {
                state.output.print("" + (double)(best_i[x].size()) + " " +
                                   (double)(best_of_run_a[x].size()) + " ",
                                   Output.V_NO_GENERAL, statisticslog);
                }
            }
        // we're done!
        }

    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        _postEvaluationStatistics(state);
        state.output.println("",Output.V_NO_GENERAL, statisticslog);
        }

    }
