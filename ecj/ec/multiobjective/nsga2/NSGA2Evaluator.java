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
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleEvaluator;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.SortComparator;

/* 
 * SimpleStatistics.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi
 */
/**
 * 
 * The NSGA2Evaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its own
 * problem space. One Problem instance is cloned from p_problem for each
 * evaluating thread.
 * 
 * The evaluator is also responsible for calculating the NSGA2Rank and
 * NSGA2Sparsity values, which are the measures of fitness used by the NSGA2.
 * This function depends on the entire population and so cannot be calculated in
 * the Problem class.
 * 
 * 
 * <p>
 * This is an implementation of Deb2000.
 * 
 * @author Faisal Abidi, Sean Luke (based on Evaluator.java by Sean Luke),
 * @version 1.0
 */
public class NSGA2Evaluator extends SimpleEvaluator
    {
    /**
     * Initial population size at generation 0.
     */
    public int originalPopSize[];

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        Parameter p = new Parameter(Initializer.P_POP);
        int subpopsLength = state.parameters.getInt(p.push(Population.P_SIZE), null, 1);
        Parameter p_subpop;
        originalPopSize = new int[subpopsLength];
        for (int i = 0; i < subpopsLength; i++)
            {
            p_subpop = p.push(Population.P_SUBPOP).push("" + i).push(Subpopulation.P_SUBPOPSIZE);
            originalPopSize[i] = state.parameters.getInt(p_subpop, null, 1);
            }
        }

    /**
     * A simple evaluator that doesn't do any coevolutionary evaluation.
     * Basically it applies evaluation pipelines, one per thread, to various
     * subchunks of a new population.
     */
    public void evaluatePopulation(final EvolutionState state)
        {
        for (int i = 0; i < state.population.subpops.length; i++)
            if (!(state.population.subpops[i] instanceof Subpopulation))
                state.output.fatal("NSGA2Evaluator must only be used with a Subpopulation!", null);
        super.evaluatePopulation(state);
        computeAuxiliaryData(state);
        }

    public void computeAuxiliaryData(EvolutionState state)
        {

        // Ok...now all individuals have been evaluated
        // so we can go ahead and calculate the rank and
        // sparsity values

        // Each subpopulation
        for (int x = 0; x < state.population.subpops.length; x++)
            {
            state.population.subpops[x].individuals = computeAuxiliaryData2(state, state.population.subpops[x].individuals, x);
            }
        }

    /**
     * The NSGA2 algorithm is implemented here, except for the breeder.
     */
    public Individual[] computeAuxiliaryData2(EvolutionState state, Individual[] inds, int subpopIndex)
        {
        // The number of individuals currently selected. We want at most
        // originalPopSize number of elements.
        int numElementsP = 0;
        int i;
        int j;
        int k;
        int l;
        int frontsByRankLength;
        int frontLength;
        Individual[] front = new Individual[0];
        // will contain the final population each generation after we perform
        // selection between parents and children.
        Individual[] p = new Individual[originalPopSize[subpopIndex]];
        MersenneTwisterFast rng = state.random[0];
        // element[i] will be an ArrayList of individuals of NSGA2Rank 'i+1'.
        ArrayList frontsByRank = frontRankAssignmentByNS(inds);
        // Only for the initial generation do we compute the sparsities for the
        // entire population at once.
        if (state.generation == 0)
            // Compute the sparsity of each individual
            for (i = 0; i < frontsByRank.size(); i++)
                multiObjectiveSparsityAssignment((Individual[]) (((ArrayList) (frontsByRank.get(i))).toArray(new Individual[0])));
        frontsByRankLength = frontsByRank.size();
        // for each front rank
        for (i = 0; i < frontsByRankLength; i++)
            {
            front = (Individual[]) (((ArrayList) (frontsByRank.get(i))).toArray(new Individual[0]));
            frontLength = front.length;
            multiObjectiveSparsityAssignment(front);
            // Except for generation 0, the received inds has 2xoriginalPopSize
            // number of individuals because it has parents+children. After
            // Non-Dominated sorting of inds we select only originalPopSize
            // number of individuals to store in p.

            // With the addition of this latest front we will have more
            // individuals than we need. Need to select which individuals from
            // this front get selected.
            if (numElementsP + frontLength > originalPopSize[subpopIndex])
                {
                j = 0;
                k = 0;
                // sort for (originalPopSize - numElementsP) largest
                // sparsity values, breaking ties arbitrarily.
                ec.util.QuickSort.qsort(front, new SortComparator()
                    {
                    public boolean lt(Object a, Object b)
                        {
                        if (((NSGA2MultiObjectiveFitness) ((Individual) a).fitness).NSGA2Sparsity > ((NSGA2MultiObjectiveFitness) ((Individual) b).fitness).NSGA2Sparsity)
                            return true;
                        return false;
                        }

                    public boolean gt(Object a, Object b)
                        {
                        if (((NSGA2MultiObjectiveFitness) ((Individual) a).fitness).NSGA2Sparsity < ((NSGA2MultiObjectiveFitness) ((Individual) b).fitness).NSGA2Sparsity)
                            return true;
                        return false;
                        }
                    });
                k = originalPopSize[subpopIndex] - numElementsP;
                // if there are ties, count them and choose all or a random
                // subset as necessary
                while (j < k && j < frontLength)
                    {
                    l = j;
                    // count all the ties
                    while (j < k && j < frontLength - 1
                        && ((NSGA2MultiObjectiveFitness) front[j].fitness).NSGA2Sparsity == ((NSGA2MultiObjectiveFitness) front[j + 1].fitness).NSGA2Sparsity)
                        j++;

                    // this individual has no sparsity ties
                    if (j == l)
                        {
                        p[numElementsP] = front[j];
                        numElementsP++;
                        }
                    else
                        // if the number of tied individuals is less than or equal
                        // to the remaining number of free spots in array p.
                        if (j - l + 1 <= originalPopSize[subpopIndex] - numElementsP)
                            {
                            // copy all of them.
                            for (int m = l; m <= j; m++)
                                {
                                p[numElementsP] = front[m];
                                numElementsP++;
                                }
                            }
                        else
                            {
                            // We only need a subset of the tied
                            // elements.
                            // Fill an array with (j-l) random unique numbers. The
                            // objective here is to select randomly without
                            // replacement
                            int[] r = new int[j - l];
                            boolean unique;
                            for (int m = 0; m < r.length; m++)
                                {
                                unique = false;
                                while (!unique)
                                    {
                                    unique = true;
                                    r[m] = rng.nextInt(r.length) + l;
                                    for (int n = 0; n < m; n++)
                                        {
                                        if (r[n] == r[m])
                                            unique = false;
                                        }
                                    }
                                }
                            // Select the elements from the front according to the
                            // random indexes in array r
                            for (int m = 0; m < r.length; m++)
                                {
                                p[numElementsP] = front[r[m]];
                                numElementsP++;
                                }
                            }
                    j++;
                    }
                if (numElementsP == originalPopSize[subpopIndex])
                    break;
                }
            else
                {
                // There's enough room in p, so just copy the whole front over.
                System.arraycopy(front, 0, p, numElementsP, frontLength);
                numElementsP += frontLength;
                }
            }
        return p;
        }

    /**
     * Assigns to each individual its Pareto front rank. Returns an ArrayList of
     * fronts ordered by rank, where each front is in turn an ArrayList of
     * individuals of same rank. E.g. element[i] is an ArrayList of individuals
     * of rank=i+1.
     */
    ArrayList frontRankAssignmentByNS(Individual[] inds)
        {
        ArrayList frontsByRank = new ArrayList();
        // We need a shallow clone for this part of the algorithm.
        ArrayList inds2 = new ArrayList(Arrays.asList(inds));

        ArrayList paretoFront;
        int numRanks = 0;
        int i;
        int j = 1;
        int paretoFrontSize;
        int inds2Size;
        // While there are still any unranked individuals left in inds2.
        while (inds2.size() > 0)
            {
            paretoFront = computeParetoFront(inds2);
            paretoFrontSize = paretoFront.size();
            for (i = 0; i < paretoFrontSize; i++)
                {
                ((NSGA2MultiObjectiveFitness) ((Individual) paretoFront.get(paretoFrontSize - 1)).fitness).NSGA2Rank = numRanks;
                inds2Size = inds2.size();
                for (j = 0; j < inds2Size; j++)
                    if (inds2.get(j) == paretoFront.get(i))
                        {
                        inds2.remove(j);
                        break;
                        }
                }
            frontsByRank.add(paretoFront);
            numRanks++;
            }
        return frontsByRank;
        }

    /**
     * Computes the Pareto front for a given set of individuals
     */
    ArrayList computeParetoFront(ArrayList inds)
        {
        ArrayList front = new ArrayList();
        int indsSize = inds.size();
        for (int i = 0; i < indsSize; i++)
            {
            Individual ind = (Individual) (inds.get(i));
            front.add(ind);
            int frontSize = front.size();
            for (int j = 0; j < frontSize - 1; j++)
                {
                Individual frontmember = (Individual) (front.get(j));
                if (((NSGA2MultiObjectiveFitness) (frontmember.fitness)).paretoDominates((NSGA2MultiObjectiveFitness) (ind.fitness)))
                    break; // FAIL
                else if (((NSGA2MultiObjectiveFitness) (ind.fitness)).paretoDominates((NSGA2MultiObjectiveFitness) (frontmember.fitness)))
                    {
                    // delete the front member
                    front.set(j, front.get(frontSize - 1)); // move top guy
                    front.remove(frontSize - 1);
                    frontSize--;
                    j--; // consider the top guy
                    }
                }
            }
        return front;
        }

    /**
     * Computes and assigns the sparsity values of a given front.
     */
    void multiObjectiveSparsityAssignment(Individual[] front)
        {
        int i;
        int j;
        int frontLength = front.length;
        int numObjectives = ((NSGA2MultiObjectiveFitness) front[0].fitness).getObjectives().length;
        for (i = 0; i < frontLength; i++)
            {
            if (front[i] == null)
                return;
            ((NSGA2MultiObjectiveFitness) front[i].fitness).NSGA2Sparsity = 0;
            }
        //We can sort the front by any objective, as the resulting lists will either be the same or the reverse of each other.
        //Hence, the neighbours are the same no matter which objective we sort by.
        final int o=0;
        //Sort front (by any objective)
        // NOTE: No matter which objectives objective you sort by, the
        // first and last individuals will always be the same (the maybe
        // interchanged though). This is because a Pareto front's
        // objectives values are strictly increasing/decreasing.
        ec.util.QuickSort.qsort(front, new SortComparator()
            {
            public boolean lt(Object a, Object b)
                {
                Individual i1 = (Individual) a;
                Individual i2 = (Individual) b;
                if (((NSGA2MultiObjectiveFitness) i1.fitness).getObjective(o) < ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o))
                    return true;
                return false;
                }

            public boolean gt(Object a, Object b)
                {
                Individual i1 = (Individual) a;
                Individual i2 = (Individual) b;
                if (((NSGA2MultiObjectiveFitness) i1.fitness).getObjective(o) > ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o))
                    return true;
                return false;
                }
            });

        // Compute and assign sparsity.
        // the first and last individuals are the sparsest.
        ((NSGA2MultiObjectiveFitness) front[0].fitness).NSGA2Sparsity = Double.POSITIVE_INFINITY;
        ((NSGA2MultiObjectiveFitness) front[front.length - 1].fitness).NSGA2Sparsity = Double.POSITIVE_INFINITY;
        for (j = 1; j < front.length - 1; j++)
            {
            ((NSGA2MultiObjectiveFitness) front[j].fitness).NSGA2Sparsity += (((NSGA2MultiObjectiveFitness) front[j + 1].fitness).getObjective(o) - ((NSGA2MultiObjectiveFitness) front[j - 1].fitness).getObjective(o))
                / (((NSGA2MultiObjectiveFitness) front[j].fitness).maxfitness[o] - ((NSGA2MultiObjectiveFitness) front[j].fitness).minfitness[o]);
            }
        }
    }