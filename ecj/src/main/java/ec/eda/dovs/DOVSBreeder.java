/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.eda.dovs;

import java.util.ArrayList;

import ec.*;
import ec.util.*;

/**
 * DOVSBreeder is a Breeder which overrides the breedPopulation method to first
 * construct hyperbox around current best individual and replace the population
 * with new individuals sampled from this hyperbox. All the heavy lifting is
 * done in DOVSSpecies and its descendant, not here.
 * 
 * @author Ermo Wei and David Freelan
 */

public class DOVSBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing to setup
        }

    /**
     * This method have three major part, first identify the best indiviudal,
     * and then call updateMostPromisingArea(...) to construct a hyperbox around
     * this individual. At last, sampled a new population from the hyperbox and
     * take the none redundant samples and return it.
     */

    public Population breedPopulation(final EvolutionState state)
        {
        Population pop = state.population;
        for (int i = 0; i < pop.subpops.size(); i++)
            {
            Subpopulation subpop = pop.subpops.get(i);
            if (!(subpop.species instanceof DOVSSpecies)) // uh oh
                state.output.fatal("To use DOVSBreeder, subpopulation " + i
                    + " must contain a DOVSSpecies.  But it contains a " + subpop.species);

            DOVSSpecies species = (DOVSSpecies) (subpop.species);

            // we assume backTrackingTest is always false.
            // Thus we combine activeSolution and Sk (individuals) to
            // identify the optimal
            species.findBestSample(state, subpop);

            // Right now activeSolutions only has A_{k-1}, need to combine S_k
            for (int j = 0; j < subpop.individuals.size(); j++)
                species.activeSolutions.add(subpop.individuals.get(i));
            // Ak and bk will have all the constraints, including original
            // problem formulation and MPR
            // A b are original problem formulation constraints
            // activeSolutions will then have the indices for those solutions
            // already visited and define MPR
            // excluding current best solution

            // update MPA
            species.updateMostPromisingArea(state);

            // sample from MPA
            ArrayList<Individual> candidates = species.mostPromisingAreaSamples(state, subpop.initialSize);
            // get Sk for evaluation
            ArrayList<Individual> Sk = species.uniqueSamples(state, candidates);

            // update the individuals
            subpop.individuals = Sk;
            }
        return pop;
        }
    }
