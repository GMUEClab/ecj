package ec.multiobjective.spea2;

import ec.*;
import ec.util.*;
import ec.simple.*;
import ec.multiobjective.*;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;

/**
 * 
 * @author Gabriel Balan
 */
public class SPEA2Statistics extends SimpleStatistics
    {

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        // super.finalStatistics(state,result);
        // I don't want just a single best fitness

        String s;
        s = "\n-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n Final Front of Run:";
        state.output.message(s);
        for (int sp = 0; sp < state.population.subpops.length; sp++)
            {
            state.output.println(s = "Subpop " + sp + "'s Final Archive:", statisticslog);
            state.output.message(s);
            SPEA2Subpopulation spop = (SPEA2Subpopulation) state.population.subpops[sp];
            int length = spop.individuals.length;

            Individual[] newInds = new Individual[length];
            SPEA2Breeder.loadElites(state, spop.individuals, newInds, spop.archiveSize);

            Object[] bestFrontSorted = new Object[spop.archiveSize];
            for (int i = 0; i < spop.archiveSize; i++)
                {
                // the archive is at the end, and I read backwards:
                bestFrontSorted[i] = newInds[length - i - 1];
                }
            // sort by objective[0]
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
            state.output.message("objective1" + "\tobjective2" + "\tSPEA2Fitness\n");
            for (int i = 0; i < bestFrontSorted.length; i++)
                {
                // the archive is at the end, and I read backwards:
                Individual individual = (Individual)bestFrontSorted[i];

                // //SPEA2Fitness.fitnessToStringForHumans prints more than I
                // need, on 2 lines no less;

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
                // printing the computed SPEA2Fitness may be helpful.
                line += ((SPEA2MultiObjectiveFitness) individual.fitness).SPEA2Fitness;
                state.output.message(line);
                }
            }
        state.output.flush();
        }

    /*
     * super.postEvaluationStatistics prints info on best individual of
     * generation, which doesn't make sense in the case of multi objective
     * problems (hence the empty body).
     * 
     * At this time the new archive is not ready, all I could print now is the
     * last generation's archive (the one used for parents for this generation's
     * inds).
     */
    public void postEvaluationStatistics(final EvolutionState state)
        {
        }
    }
