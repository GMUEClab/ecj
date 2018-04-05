/*
  Copyright 2018 by Sunil Kumar Rajendran
  With modifications by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.pbil;

import java.util.ArrayList;

import ec.*;
import ec.util.*;

/* 
 * PBILBreeder.java
 * 
 * Created: Wed Jan 10 16:15:00 EDT 2018
 * By: Sunil Kumar Rajendran
 */

/**
 * PBILBreeder is a Breeder which overrides the breedPopulation method
 * to first update PBIL's internal distribution, then replace all the
 * individuals in the population with new samples generated from the
 * distribution.  All the heavy lifting is done in PBILSpecies, not here.
 *
 * @author Sunil Kumar Rajendran
 * @version 1.0 
 */

public class PBILBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing to setup
        }
    
    /** Updates the PBIL distribution given the current population, then 
        replaces the population with new samples generated from the distribution.
        Returns the revised population. */
    
    public Population breedPopulation(final EvolutionState state)
        {
        Population pop = state.population;
        for(int i = 0; i < pop.subpops.size(); i++)
            {
            Subpopulation subpop = pop.subpops.get(i);
            if (!(subpop.species instanceof PBILSpecies)) 
                state.output.fatal("To use PBILBreeder, subpopulation " + i + " must contain a PBILSpecies.  But it contains a " + subpop.species);
                        
            PBILSpecies species = (PBILSpecies)(subpop.species);
                
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
