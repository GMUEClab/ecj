/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.steadystate.*;
import ec.*;
import ec.gp.*;
import ec.util.*;
import java.io.*;

/* 
 * KozaStatistics.java
 * 
 * Created: Fri Nov  5 16:03:44 1999
 * By: Sean Luke
 */

/**
 * A simple Koza-style statistics generator.  Prints the mean fitness 
 * (raw,adjusted,hits) and best individual of each generation.
 * At the end, prints the best individual of the run and the number of
 * individuals processed.
 *
 * <p>If gather-full is true, then final timing information, number of nodes
 * and depths of trees, approximate final memory utilization, etc. are also given.
 *
 * <p>Compressed files will be overridden on restart from checkpoint; uncompressed files will be 
 * appended on restart.
 *
 * <p>KozaStatistics implements a simple version of steady-state statistics in the
 * same fashion that SimpleStatistics does: if it quits before a generation boundary,
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
 <tr><td valign=top><i>base</i>.<tt>gather-full</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we full statistics on individuals (will run slower, though the slowness is due to off-line processing that won't mess up timings)</td></tr>
 </table>
 * @author Sean Luke
 */

public class KozaStatistics extends Statistics implements SteadyStateStatisticsForm
    {
    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";

    /** The Statistics' log */
    public int statisticslog;

    /** The best individual we've found so far */
    public Individual[] best_of_run;

    /** compress? */
    public static final String P_COMPRESS = "gzip";

    public static final String P_FULL = "gather-full";

    boolean doFull;

    // total number of individuals
    long numInds;

    // timings
    long lastTime;
    long initializationTime;
    long breedingTime;
    long evaluationTime;
    long nodesInitialized;
    long nodesEvaluated;
    long nodesBred;

    // memory usage info
    long lastUsage = 0;
    long initializationUsage = 0;
    long breedingUsage = 0;
    long evaluationUsage = 0;

    public KozaStatistics() { best_of_run = null; statisticslog = 0; /* stdout */ }

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
        nodesInitialized = nodesEvaluated = nodesBred = 0;
        breedingTime=evaluationTime=0;
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

        // gather timings       
        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();
            if (curU > lastUsage) initializationUsage = curU - lastUsage;
            initializationTime = System.currentTimeMillis()-lastTime;
            
            // Determine how many nodes we have
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // check to make sure they're the right class
                if ( !(state.population.subpops[x].species instanceof GPSpecies ))
                    state.output.fatal("Subpopulation " + x +
                                       " is not of the species form GPSpecies." + 
                                       "  Cannot do timing statistics with KozaStatistics.");
                
                for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                    {
                    GPIndividual i = 
                        (GPIndividual)(state.population.subpops[x].individuals[y]);
                    for(int z=0;z<i.trees.length;z++)
                        nodesInitialized += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
                    }
                }
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
        // gather timings
        if (doFull)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();
            if (curU > lastUsage) breedingUsage += curU - lastUsage;
            breedingTime += System.currentTimeMillis()-lastTime;
            
            // Determine how many nodes we have
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // check to make sure they're the right class
                if ( !(state.population.subpops[x].species instanceof GPSpecies ))
                    state.output.fatal("Subpopulation " + x +
                                       " is not of the species form GPSpecies." + 
                                       "  Cannot do timing statistics with KozaStatistics.");
                                
                for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                    {
                    GPIndividual i = 
                        (GPIndividual)(state.population.subpops[x].individuals[y]);
                    for(int z=0;z<i.trees.length;z++)
                        nodesBred += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
                    }
                }
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

    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        
        // Gather statistics
        Runtime r = Runtime.getRuntime();
        long curU =  r.totalMemory() - r.freeMemory();
        if (curU > lastUsage) evaluationUsage += curU - lastUsage;
        if (doFull) evaluationTime += System.currentTimeMillis()-lastTime;


        state.output.println("\n\n\nGeneration " + state.generation + "\n================",Output.V_NO_GENERAL,statisticslog);

        Individual[] best_i = new Individual[state.population.subpops.length];
        for(int x=0;x<state.population.subpops.length;x++)
            {
            state.output.println("\nSubpopulation " + x + "\n----------------",Output.V_NO_GENERAL,statisticslog);

            // gather timings
            if (doFull)
                {
                long totNodesPerGen = 0;
                long totDepthPerGen = 0;
                
                // check to make sure they're the right class
                if ( !(state.population.subpops[x].species instanceof GPSpecies ))
                    state.output.fatal("Subpopulation " + x +
                                       " is not of the species form GPSpecies." + 
                                       "  Cannot do timing statistics with KozaStatistics.");
                
                long[] numNodes = new long[((GPIndividual)(state.population.subpops[x].species.i_prototype)).trees.length];
                long[] numDepth = new long[((GPIndividual)(state.population.subpops[x].species.i_prototype)).trees.length];

                for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                    {
                    GPIndividual i = 
                        (GPIndividual)(state.population.subpops[x].individuals[y]);
                    for(int z=0;z<i.trees.length;z++)
                        {
                        nodesEvaluated += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
                        numNodes[z] += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
                        numDepth[z] += i.trees[z].child.depth();
                        }
                    }
                
                for(int tr=0;tr<numNodes.length;tr++) totNodesPerGen += numNodes[tr];
                state.output.println("Avg Nodes: " + ((double)totNodesPerGen)/state.population.subpops[x].individuals.length,Output.V_NO_GENERAL, statisticslog);
                state.output.print("Nodes/tree: [",Output.V_NO_GENERAL, statisticslog);
                for(int tr=0;tr<numNodes.length;tr++)
                    {
                    if (tr>0) state.output.print("|",Output.V_NO_GENERAL, statisticslog);
                    state.output.print(""+((double)numNodes[tr])/state.population.subpops[x].individuals.length,Output.V_NO_GENERAL, statisticslog);
                    }
                state.output.println("]",Output.V_NO_GENERAL, statisticslog);


                for(int tr=0;tr<numDepth.length;tr++) totDepthPerGen += numDepth[tr];
                state.output.println("Avg Depth: " + ((double)totDepthPerGen)/
                                     (state.population.subpops[x].individuals.length * numDepth.length),Output.V_NO_GENERAL, statisticslog);
                state.output.print("Depth/tree: [",Output.V_NO_GENERAL, statisticslog);
                for(int tr=0;tr<numDepth.length;tr++)
                    {
                    if (tr>0) state.output.print("|",Output.V_NO_GENERAL, statisticslog);
                    state.output.print(""+((double)numDepth[tr])/state.population.subpops[x].individuals.length,Output.V_NO_GENERAL, statisticslog);
                    }
                state.output.println("]",Output.V_NO_GENERAL, statisticslog);

                }           
            
            
            float meanRaw = 0.0f;
            float meanAdjusted = 0.0f;
            long hits = 0;

            if (!(state.population.subpops[x].species.f_prototype instanceof KozaFitness))
                state.output.fatal("Subpopulation " + x +
                                   " is not of the fitness KozaFitness.  Cannot do timing statistics with KozaStatistics.");
                

            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                {
                // best individual
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
                // mean for population
                meanRaw += ((KozaFitness)(state.population.subpops[x].individuals[y].fitness)).rawFitness();
                meanAdjusted += ((KozaFitness)(state.population.subpops[x].individuals[y].fitness)).adjustedFitness();
                hits += ((KozaFitness)(state.population.subpops[x].individuals[y].fitness)).hits;
                }

            // compute fitness stats
            meanRaw /= state.population.subpops[x].individuals.length;
            meanAdjusted /= state.population.subpops[x].individuals.length;
            state.output.print("Mean fitness raw: " + meanRaw + " adjusted: " + meanAdjusted + " hits: " + ((double)hits)/state.population.subpops[x].individuals.length,Output.V_NO_GENERAL, statisticslog);
                    
            state.output.println("",Output.V_NO_GENERAL, statisticslog);

            // compute inds stats
            numInds += state.population.subpops[x].individuals.length;
            }

        // now test to see if it's the new best_of_run
        for(int x=0;x<state.population.subpops.length;x++)
            {
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());

            // print the best-of-generation individual
            state.output.println("\nBest Individual of Generation:",Output.V_NO_GENERAL,statisticslog);
            best_i[x].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);
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



    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);
        
        state.output.println("\n\n\nFinal Statistics\n================",Output.V_NO_GENERAL,statisticslog);

        state.output.println("Total Individuals Evaluated: " + numInds,Output.V_NO_GENERAL,statisticslog);
        // for now we just print the best fitness 
        
        state.output.println("\nBest Individual of Run:",Output.V_NO_GENERAL,statisticslog);
        for(int x=0;x<state.population.subpops.length;x++)
            best_of_run[x].printIndividualForHumans(state,statisticslog,Output.V_NO_GENERAL);


        // Output timings
        if (doFull)
            {
            state.output.println("\n\n\nTimings\n=======",Output.V_NO_GENERAL,statisticslog);

            state.output.println("Initialization: " + ((float)initializationTime)/1000 + " secs total, " + nodesInitialized + " nodes, " + nodesInitialized/(((float)initializationTime)/1000) + " nodes/sec",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Evaluating: " + ((float)evaluationTime)/1000 + " secs total, " + nodesEvaluated + " nodes, " + nodesEvaluated/(((float)evaluationTime)/1000) + " nodes/sec",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Breeding: " + ((float)breedingTime)/1000 + " secs total, " + nodesBred + " nodes, " + nodesBred/(((float)breedingTime)/1000) + " nodes/sec",Output.V_NO_GENERAL,statisticslog);

            state.output.println("\n\n\nMemory Usage\n==============",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Initialization: " + ((float)initializationUsage)/1024 + " KB total, " + nodesInitialized + " nodes, " + nodesInitialized/(((float)initializationUsage)/1024) + " nodes/KB",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Evaluating: " + ((float)evaluationUsage)/1024 + " KB total, " + nodesEvaluated + " nodes, " + nodesEvaluated/(((float)evaluationUsage)/1024) + " nodes/KB",Output.V_NO_GENERAL,statisticslog);
            state.output.println("Breeding: " + ((float)breedingUsage)/1024 + " KB total, " + nodesBred + " nodes, " + nodesBred/(((float)breedingUsage)/1024) + " nodes/KB",Output.V_NO_GENERAL,statisticslog);            
            }
        
        }

    }
