/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga2;

import java.util.ArrayList;
import java.util.Arrays;
import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.QuickSort;
import ec.util.SortComparator;

/* 
 * NSGA2Statistics.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi
 */

/**
 * 
 * @author Faisal Abidi
 */
public class NSGA2Statistics extends SimpleStatistics
    {

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        // super.finalStatistics(state,result);
        // I don't want just a single best fitness
        int i;
        int j;
        Individual[] spopInds;
        Individual individual;
        String s;
        s = "\n-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n Final Front of Run:";
        state.output.message(s);
        for (int sp = 0; sp < state.population.subpops.length; sp++)
            {
            state.output.println(s = "Subpop " + sp + "'s Best Front:", statisticslog);
            state.output.message(s);
            Subpopulation spop = (Subpopulation) state.population.subpops[sp];
            spopInds = spop.individuals;
            ArrayList bestFront = new ArrayList(Arrays.asList(spopInds));
            i = 0;
            while (i < bestFront.size())
                if (((NSGA2MultiObjectiveFitness) ((Individual) bestFront.get(i)).fitness).NSGA2Rank != 0)
                    bestFront.remove(i);
                else
                    i++;
            
            Object[] bestFrontSorted = bestFront.toArray();
            //sort by objective[0]
            QuickSort.qsort(bestFrontSorted, new SortComparator()
                {
                
                public boolean lt(Object a, Object b)
                    {
                    if (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) < (((MultiObjectiveFitness) ((Individual) b).fitness)).getObjective(0))
                        return true;
                    return false;
                    }

                
                public boolean gt(Object a, Object b)
                    {
                    if (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) > ((MultiObjectiveFitness) (((Individual) b).fitness)).getObjective(0))
                        return false;
                    return false;
                    }
                });
            state.output.message("Total: " + bestFrontSorted.length + " individuals in Best Front\n");
            state.output.message("objective1" + "\tobjective2" + "\tSparsity\n");
            for (i = 0; i < bestFront.size(); i++)
                {
                individual = (Individual) (bestFrontSorted[i]);
                // newInds = NSGA2Breeder.loadElites(state, spop.individuals);

                // //NSGA2Fitness.fitnessToStringForHumans prints more than
                // I need, on 2 lines no less;

                // I include that in the stats log though, just in case:
                individual.fitness.printFitnessForHumans(state, statisticslog);
                // I also include the actual individual in the stats log; I
                // could call describe.
                state.output.println(individual.genotypeToStringForHumans(), statisticslog);

                // //all I need to print are the original fitness values, so
                // I'll have to do it myself.
                MultiObjectiveFitness mof = (MultiObjectiveFitness) individual.fitness;
                float[] objectives = mof.getObjectives();
                String line = "";

                for (int f = 0; f < objectives.length; f++)
                    line += objectives[f] + "\t";
                line += ((NSGA2MultiObjectiveFitness) individual.fitness).NSGA2Sparsity;
                state.output.message(line);
                }
            }
        }
    }
