/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.*;
import ec.gp.*;
import java.io.*;
import ec.util.*;
import ec.simple.*;

/* 
 * KozaShortStatistics.java
 * 
 * Created: Fri Nov  5 16:03:44 1999
 * By: Sean Luke
 */

/**
 * A Koza-style statistics generator, intended to be easily parseable with
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
 <li> (if do-time) how long initialization took in milliseconds, or how long the previous generation took to breed to form this generation
 <li> (if do-time) How long evaluation took in milliseconds this generation
 </ul>

 <p>Then, (if do-subpops) the following items appear, once per each subpopulation:
 <ul>
 <li> (if do-depth) [a b c...], representing the average depth of tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 <li> (if do-size) [a b c...], representing the average number of nodes used in tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 <li> (if do-size) The average size of an individual this generation
 <li> (if do-size) The average size of an individual so far in the run
 <li> (if do-size) The size of the best individual this generation
 <li> (if do-size) The size of the best individual so far in the run
 <li> The mean standardized fitness of the subpopulation this generation
 <li> The best standardized fitness of the subpopulation this generation
 <li> The best standardized fitness of the subpopulation so far in the run
 </ul>
 
 <p>Then the following items appear, for the whole population:
 <ul>
 <li> (if do-depth) [a b c...], representing the average depth of tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 <li> (if do-size) [a b c...], representing the average number of nodes used in tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 <li> (if do-size) The average size of an individual this generation
 <li> (if do-size) The average size of an individual so far in the run
 <li> (if do-size) The size of the best individual this generation
 <li> (if do-size) The size of the best individual so far in the run
 <li> The mean standardized fitness of the subpopulation this generation
 <li> The best standardized fitness of the subpopulation this generation
 <li> The best standardized fitness of the subpopulation so far in the run
 </ul>

 KozaStatistics assumes that every one of the Individuals in your population (and all subpopualtions) are GPIndividuals, 
 and further that they all have the same number of trees.

 Besides the parameter below, KozaShortStatistics obeys all the SimpleShortStatistics parameters.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>do-depth</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(print depth information?)</td></tr>
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public class KozaShortStatistics extends SimpleShortStatistics
    {
    public static final String P_DO_DEPTH = "do-depth";

    public boolean doDepth;

    long totalDepthSoFarTree[][];
    long totalSizeSoFarTree[][];
    long[][] totalSizeThisGenTree;                  // per-subpop total size of individuals this generation per tree
    long[][] totalDepthThisGenTree;                 // per-subpop total size of individuals this generation per tree


    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        doDepth = state.parameters.getBoolean(base.push(P_DO_DEPTH),null,false);
        }

    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
                
        totalDepthSoFarTree = new long[state.population.subpops.length][];
        totalSizeSoFarTree = new long[state.population.subpops.length][];

        for(int x = 0 ; x < state.population.subpops.length; x++)
            {
            // check to make sure they're the right class
            if ( !(state.population.subpops[x].species instanceof GPSpecies ))
                state.output.fatal("Subpopulation " + x +
                    " is not of the species form GPSpecies." + 
                    "  Cannot do timing statistics with KozaShortStatistics.");
                
            GPIndividual i = (GPIndividual)(state.population.subpops[x].individuals[0]);
            totalDepthSoFarTree[x] = new long[i.trees.length];
            totalSizeSoFarTree[x] = new long[i.trees.length];
            }
        }


    protected void prepareStatistics(EvolutionState state)
        {
        totalDepthThisGenTree = new long[state.population.subpops.length][];
        totalSizeThisGenTree = new long[state.population.subpops.length][];

        for(int x = 0 ; x < state.population.subpops.length; x++)
            {
            GPIndividual i = (GPIndividual)(state.population.subpops[x].individuals[0]);
            totalDepthThisGenTree[x] = new long[i.trees.length];
            totalSizeThisGenTree[x] = new long[i.trees.length];
            }
        }


    protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual)
        {
        GPIndividual i = (GPIndividual)(state.population.subpops[subpop].individuals[individual]);
        for(int z =0; z < i.trees.length; z++)
            {
            totalDepthThisGenTree[subpop][z] += i.trees[z].child.depth();
            totalDepthSoFarTree[subpop][z] += totalDepthThisGenTree[subpop][z];
            totalSizeThisGenTree[subpop][z] += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
            totalSizeSoFarTree[subpop][z] += totalSizeThisGenTree[subpop][z];
            }
        }

    protected void printExtraSubpopStatisticsBefore(EvolutionState state, int subpop)
        {
        if (doDepth)
            {
            state.output.print("[ ", statisticslog);
            for(int z = 0 ; z < totalDepthThisGenTree[subpop].length; z++)
                state.output.print("" + (totalIndsThisGen[subpop] > 0 ? ((double)totalDepthThisGenTree[subpop][z])/totalIndsThisGen[subpop] : 0) + " ",  statisticslog);
            state.output.print("] ", statisticslog);
            }
        if (doSize)
            {
            state.output.print("[ ", statisticslog);
            for(int z = 0 ; z < totalSizeThisGenTree[subpop].length; z++)
                state.output.print("" + (totalIndsThisGen[subpop] > 0 ? ((double)totalSizeThisGenTree[subpop][z])/totalIndsThisGen[subpop] : 0) + " ",  statisticslog);
            state.output.print("] ", statisticslog);
            }
        }

    protected void printExtraPopStatisticsBefore(EvolutionState state)
        {
        long[] totalDepthThisGenTreePop = new long[totalDepthSoFarTree[0].length];
        long[] totalSizeThisGenTreePop = new long[totalSizeSoFarTree[0].length];                // will assume each subpop has the same tree size
        long totalIndsThisGenPop = 0;
        long totalDepthThisGenPop = 0;
        long totalDepthSoFarPop = 0;

        int subpops = state.population.subpops.length;

        for(int y = 0; y < subpops; y++)
            {
            totalIndsThisGenPop += totalIndsThisGen[y];
            for(int z =0; z < totalSizeThisGenTreePop.length; z++)
                totalSizeThisGenTreePop[z] += totalSizeThisGenTree[y][z];
            for(int z =0; z < totalDepthThisGenTreePop.length; z++)
                totalDepthThisGenTreePop[z] += totalDepthThisGenTree[y][z];
            }

        if (doDepth)
            {
            state.output.print("[ ", statisticslog);
            for(int z = 0 ; z < totalDepthThisGenTreePop.length; z++)
                state.output.print("" + (totalIndsThisGenPop > 0 ? ((double)totalDepthThisGenTreePop[z])/totalIndsThisGenPop : 0) + " ",  statisticslog);
            state.output.print("] ", statisticslog);
            }
        if (doSize)
            {
            state.output.print("[ ", statisticslog);
            for(int z = 0 ; z < totalSizeThisGenTreePop.length; z++)
                state.output.print("" + (totalIndsThisGenPop > 0 ? ((double)totalSizeThisGenTreePop[z])/totalIndsThisGenPop : 0) + " ",  statisticslog);
            state.output.print("] ", statisticslog);
            }
        }
    }
        