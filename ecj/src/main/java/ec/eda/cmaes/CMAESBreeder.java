/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.cmaes;

import java.util.ArrayList;

import ec.*;
import ec.util.*;

/* 
 * CMAESBreeder.java
 * 
 * Created: Wed Jul  8 12:35:31 EDT 2015
 * By: Sam McKay and Sean Luke
 */

/**
 * CMAESBreeder is a Breeder which overrides the breedPopulation method
 * to first update CMA-ES's internal distribution, then replace all the
 * individuals in the population with new samples generated from the
 * distribution.  All the heavy lifting is done in CMAESSpecies, not here.
 *
 * @author Sam McKay and Sean Luke
 * @version 1.0 
 */

public class CMAESBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing to setup
        }

    /** Updates the CMA-ES distribution given the current population, then 
        replaces the population with new samples generated from the distribution.
        Returns the revised population. */

    public Population breedPopulation(final EvolutionState state)
        {
        Population pop = state.population;
        for(int i = 0; i < pop.subpops.size(); i++)
            {
            Subpopulation subpop = pop.subpops.get(i);
            if (!(subpop.species instanceof CMAESSpecies))  // uh oh
                state.output.fatal("To use CMAESBreeder, subpopulation " + i + " must contain a CMAESSpecies.  But it contains a " + subpop.species);
                        
            CMAESSpecies species = (CMAESSpecies)(subpop.species);
                
            // update distribution[i] for subpop
            species.updateDistribution(state, subpop);
                
            // overwrite individuals
            ArrayList<Individual> inds = subpop.individuals;
            for(int j = 0; j < inds.size(); j++)
                inds.set(j, species.newIndividual(state, 0));
            }
                
        return pop;
        }
    }
