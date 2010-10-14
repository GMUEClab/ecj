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
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleEvaluator;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparator;

/**
 * 
 * The NSGA2Evaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its own
 * problem space. One Problem instance is cloned from p_problem for each
 * evaluating thread.
 * 
 * The evaluator is also responsible for calculating the rank and
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
        for (int x = 0; x < state.population.subpops.length; x++)
            state.population.subpops[x].individuals = 
				computeAuxiliaryData(state, state.population.subpops[x].individuals, x);
        }


    public Individual[] computeAuxiliaryData(EvolutionState state, Individual[] inds, int subpop)
		{
		Individual[] dummy = new Individual[0];
		
		ArrayList ranks = assignFrontRanks(inds);
		
		ArrayList newSubpopulation = new ArrayList();
		int size = ranks.size();
		for(int i = 0; i < size; i++)
			{
			Individual[] rank = (Individual[])((ArrayList)(ranks.get(i))).toArray(dummy);
			assignSparsity(rank);
			if (rank.length + newSubpopulation.size() >= originalPopSize[subpop])
				{
				// first sort the rank by sparsity
				ec.util.QuickSort.qsort(rank, new SortComparator()
                {
                public boolean lt(Object a, Object b)
                    {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness).sparsity > ((NSGA2MultiObjectiveFitness) i2.fitness).sparsity);
                    }

                public boolean gt(Object a, Object b)
                    {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness).sparsity < ((NSGA2MultiObjectiveFitness) i2.fitness).sparsity);
                    }
                });

				// then put the m sparsest individuals in the new population
				int m = originalPopSize[subpop] - newSubpopulation.size();
				for(int j = 0 ; j < m; j++)
					newSubpopulation.add(rank[j]);
				
				// and bail
				break;
				}
			else
				{
				// dump in everyone
				for(int j = 0 ; j < rank.length; j++)
					newSubpopulation.add(rank[j]);
				}
			}
		return (Individual[])(newSubpopulation.toArray(dummy));
		}



	/** Divides inds into ranks and assigns each individual's rank to be the rank it was placed into.
		Each front is an ArrayList. */
    public ArrayList assignFrontRanks(Individual[] inds)
        {
		Individual[] dummy = new Individual[0];
		
        ArrayList frontsByRank = new ArrayList();

		int rank = 0;
		while(inds.length > 0)
			{
			ArrayList front = new ArrayList();
			ArrayList nonFront = new ArrayList();
			MultiObjectiveFitness.partitionIntoParetoFront(inds, front, nonFront);
			
			// build inds out of remainder
			inds = (Individual[]) nonFront.toArray(dummy);
			
			// label front
			int len = front.size();
			for(int i = 0; i < len; i++)
				((NSGA2MultiObjectiveFitness)(((Individual)(front.get(i))).fitness)).rank = rank;
			frontsByRank.add(front);
			rank++;
			}
		return frontsByRank;
		}



    /**
     * Computes and assigns the sparsity values of a given front.
     */
    public void assignSparsity(Individual[] front)
        {
        int numObjectives = ((NSGA2MultiObjectiveFitness) front[0].fitness).getObjectives().length;
		
        for (int i = 0; i < front.length; i++)
            ((NSGA2MultiObjectiveFitness) front[i].fitness).sparsity = 0;

        for (int i = 0; i < numObjectives; i++)
            {
            final int o = i;
            // 1. Sort front by each objective.
            // 2. Sum the manhattan distance of an individual's neighbours over
            // each objective.
            // NOTE: No matter which objectives objective you sort by, the
            // first and last individuals will always be the same (they maybe
            // interchanged though). This is because a Pareto front's
            // objectives values are strictly increasing/decreasing.
            ec.util.QuickSort.qsort(front, new SortComparator()
                {
                public boolean lt(Object a, Object b)
                    {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness).getObjective(o) < ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o));
                    }

                public boolean gt(Object a, Object b)
                    {
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness).getObjective(o) > ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o));
                    }
                });

            // Compute and assign sparsity.
            // the first and last individuals are the sparsest.
            ((NSGA2MultiObjectiveFitness) front[0].fitness).sparsity = Double.POSITIVE_INFINITY;
            ((NSGA2MultiObjectiveFitness) front[front.length - 1].fitness).sparsity = Double.POSITIVE_INFINITY;
            for (int j = 1; j < front.length - 1; j++)
                {
				NSGA2MultiObjectiveFitness f_j = (NSGA2MultiObjectiveFitness) (front[j].fitness);
				NSGA2MultiObjectiveFitness f_jplus1 = (NSGA2MultiObjectiveFitness) (front[j+1].fitness);
				NSGA2MultiObjectiveFitness f_jminus1 = (NSGA2MultiObjectiveFitness) (front[j-1].fitness);
				
                // store the NSGA2Sparsity in sparsity
                f_j.sparsity += (f_jplus1.getObjective(o) - f_jminus1.getObjective(o)) / (f_j.maxObjective[o] - f_j.minObjective[o]);
                }
            }
        }
    }