/*
  Copyright 2017 by Ben Brumbac
  Modifications Copyright 2017 Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga3;

import ec.*;
import ec.util.*;
import ec.simple.*;
import java.util.*;
import ec.multiobjective.*;

/* 
 * NSGA3Breeder.java
 * 
 * Created: Sat Jan 20 2018
 * By: Ben Brumback and Sean Luke
 */

/**
 * This SimpleBreeder subclass breeds a set of children from the Population, then
 * joins the original Population with the children in a (mu+mu) fashion.   An NSGA3Breeder
 * may have multiple threads for breeding.
 *
 * <p>NSGA-III has fixed archive size (the population size), and so ignores the 'elites'
 * declaration.  However it will adhere to the 'reevaluate-elites' parameter in SimpleBreeder
 * to determine whether to force fitness reevaluation.

 */

public class NSGA3Breeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        // make sure SimpleBreeder's elites facility isn't being used
        for (int i = 0; i < elite.length; i++)  // we use elite.length here instead of pop.subpops.length because the population hasn't been made yet.
            if (usingElitism(i))
                state.output.warning("You're using elitism with NSGA3Breeder, which is not permitted and will be ignored.  However the reevaluate-elites parameter *will* bre recognized by NSGAEvaluator.",
                    base.push(P_ELITE).push(""+i));

        if (sequentialBreeding) // uh oh, haven't tested with this
            state.output.fatal("NSGA3Breeder does not support sequential evaluation.",
                base.push(P_SEQUENTIAL_BREEDING));

        if (!clonePipelineAndPopulation)
            state.output.fatal("clonePipelineAndPopulation must be true for NSGA3Breeder.");
        }

    int[] numElites = null;
        
    // This method is called AFTER loadElites.  We could just 
    public int numElites(EvolutionState state, int subpopulation)
        {
        return numElites[subpopulation];
        }

    protected void loadElites(EvolutionState state, Population newpop)
        {
        numElites = new int[newpop.subpops.size()];
        
        for(int i = 0; i < newpop.subpops.size(); i++)
            {
            ArrayList list = buildArchive(state, i);
            numElites[i] = list.size();
            newpop.subpops.get(i).individuals.addAll(list);
            }
        }

    /** Build the auxiliary fitness data and reduce the subpopulation to just the archive, which is returned. */
    public ArrayList<Individual> buildArchive(EvolutionState state, int subpop)
        {
        ArrayList<ArrayList<Individual>> ranks = assignFrontRanks(state.population.subpops.get(subpop));
                
        ArrayList<Individual> newSubpopulation = new ArrayList<Individual>();
        int size = ranks.size();
        int originalPopSize = state.population.subpops.get(subpop).individuals.size();

        for(int i = 0; i < size; i++)
            {
            ArrayList<Individual> rank = ranks.get(i);
            if (rank.size() + newSubpopulation.size() >= originalPopSize)
                {
                //This is the differnce between NSGA2 and 3.
                int numObjectives = ((NSGA3MultiObjectiveFitness) rank.get(0).fitness).getObjectives().length;
                List<Individual> frontL = new SelectorTools(ranks.subList(0, i+1), numObjectives).selectFrontLIndividuals(originalPopSize- newSubpopulation.size());
                newSubpopulation.addAll(frontL);
                break;
                }
            else
                {
                // dump in everyone
                newSubpopulation.addAll(rank);
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
                ((NSGA3MultiObjectiveFitness)front.get(ind).fitness).rank = rank;
            }
        return frontsByRank;
        }
    }
