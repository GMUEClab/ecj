/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga2;

import ec.*;
import ec.util.*;
import ec.simple.*;
import java.util.*;
import ec.multiobjective.*;

/* 
 * NSGA2Breeder.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi and Sean Luke
 */

/**
 * This SimpleBreeder subclass breeds a set of children from the Population, then
 * joins the original Population with the children in a (mu+mu) fashion.   An NSGA2Breeder
 * may have multiple threads for breeding.
 *
 * <p>NSGA-II has fixed archive size (the population size), and so ignores the 'elites'
 * declaration.  However it will adhere to the 'reevaluate-elites' parameter in SimpleBreeder
 * to determine whether to force fitness reevaluation.

 */

public class NSGA2Breeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        // make sure SimpleBreeder's elites facility isn't being used
        for (int i = 0; i < elite.length; i++)  // we use elite.length here instead of pop.subpops.length because the population hasn't been made yet.
            if (usingElitism(i))
                state.output.warning("You're using elitism with NSGA2Breeder, which is not permitted and will be ignored.  However the reevaluate-elites parameter *will* bre recognized by NSGAEvaluator.",
                    base.push(P_ELITE).push(""+i));

        if (sequentialBreeding) // uh oh, haven't tested with this
            state.output.fatal("NSGA2Breeder does not support sequential evaluation.",
                base.push(P_SEQUENTIAL_BREEDING));

        if (!clonePipelineAndPopulation)
            state.output.fatal("clonePipelineAndPopulation must be true for NSGA2Breeder.");
        }

    protected void loadElites(EvolutionState state, Population newpop)
    	{
    	for(int i = 0; i < newpop.subpops.size(); i++)
    		{
	    	newpop.subpops.get(i).individuals.addAll(buildArchive(state, i));
	    	}
    	}

    /** Build the auxiliary fitness data and reduce the subpopulation to just the archive, which is
        returned. */
    public ArrayList<Individual> buildArchive(EvolutionState state, int subpop)
        {
        ArrayList<ArrayList<Individual>> ranks = assignFrontRanks(state.population.subpops.get(subpop));
                
        ArrayList<Individual> newSubpopulation = new ArrayList<Individual>();
        int size = ranks.size();
        for(int i = 0; i < size; i++)
            {
            int originalPopSize = state.population.subpops.get(subpop).individuals.size();
            ArrayList<Individual> rank = ranks.get(i);
                
            assignSparsity(rank);
            if (rank.size() + newSubpopulation.size() >= originalPopSize)
                {
                // first sort the rank by sparsity
                // decreasing order
                Collections.sort(rank, new Comparator<Individual>(){
                    public int compare(Individual i1, Individual i2)
                        {
                        return Double.compare(((NSGA2MultiObjectiveFitness) i2.fitness).sparsity, 
                            (((NSGA2MultiObjectiveFitness) i1.fitness).sparsity));
                        }
                    });

                // then put the m sparsest individuals in the new population
                int m = originalPopSize - newSubpopulation.size();
                for(int j = 0 ; j < m; j++)
                    newSubpopulation.add(rank.get(j));
                                
                // and bail
                break;
                }
            else
                {
                // dump in everyone
                for(int j = 0 ; j < rank.size(); j++)
                    newSubpopulation.add(rank.get(j));
                }
            }

        ArrayList<Individual> archive = new ArrayList<Individual>(newSubpopulation);
                
        // maybe force reevaluation
        if (reevaluateElites[subpop])
            for(int i = 0 ; i < archive.size(); i++)
                archive.get(i).evaluated = false;

        return archive;
        }



    /** Divides inds into ranks and assigns each individual's rank to be the rank it was placed into.
        Each front is an ArrayList. */
    public ArrayList<ArrayList<Individual>> assignFrontRanks(Subpopulation subpop)
        {
        ArrayList<Individual> inds = subpop.individuals;
        ArrayList<ArrayList<Individual>> frontsByRank = MultiObjectiveFitness.partitionIntoRanks(inds);

        int numRanks = frontsByRank.size();
        for(int rank = 0; rank < numRanks; rank++)
            {
            ArrayList<Individual> front = frontsByRank.get(rank);
            int numInds = front.size();
            for(int ind = 0; ind < numInds; ind++)
                ((NSGA2MultiObjectiveFitness)front.get(ind).fitness).rank = rank;
            }
        return frontsByRank;
        }



    /**
     * Computes and assigns the sparsity values of a given front.
     */
    public void assignSparsity(ArrayList<Individual> front)
        {
        int numObjectives = ((NSGA2MultiObjectiveFitness) front.get(0).fitness).getObjectives().length;
                
        for (int i = 0; i < front.size(); i++)
            ((NSGA2MultiObjectiveFitness) front.get(i).fitness).sparsity = 0;

        for (int i = 0; i < numObjectives; i++)
            {
            final int o = i;
            // 1. Sort front by each objective.
            // 2. Sum the manhattan distance of an individual's neighbours over
            // each objective.
            // NOTE: No matter which objectives objective you sort by, the
            // first and last individuals will always be the same (they maybe
            // interchanged though). This is because a Pareto front's
            // objective values are strictly increasing/decreasing.

            // increasing order
            Collections.sort(front, new Comparator<Individual>(){
                public int compare(Individual i1, Individual i2)
                    {
                    return Double.compare(((NSGA2MultiObjectiveFitness) i1.fitness).getObjective(o), 
                        ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o));
                    }
                });

            // Compute and assign sparsity.
            // the first and last individuals are the sparsest.
            ((NSGA2MultiObjectiveFitness) front.get(0).fitness).sparsity = Double.POSITIVE_INFINITY;
            ((NSGA2MultiObjectiveFitness) front.get(front.size() - 1).fitness).sparsity = Double.POSITIVE_INFINITY;
            for (int j = 1; j < front.size() - 1; j++)
                {
                NSGA2MultiObjectiveFitness f_j = (NSGA2MultiObjectiveFitness) (front.get(j).fitness);
                NSGA2MultiObjectiveFitness f_jplus1 = (NSGA2MultiObjectiveFitness) (front.get(j+1).fitness);
                NSGA2MultiObjectiveFitness f_jminus1 = (NSGA2MultiObjectiveFitness) (front.get(j-1).fitness);
                                
                // store the NSGA2Sparsity in sparsity
                f_j.sparsity += (f_jplus1.getObjective(o) - f_jminus1.getObjective(o)) / (f_j.maxObjective[o] - f_j.minObjective[o]);
                }
            }
        }


    }
