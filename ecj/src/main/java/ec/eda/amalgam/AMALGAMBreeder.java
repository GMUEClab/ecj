/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.amalgam;

import java.util.ArrayList;

import ec.*;
import ec.util.*;
import ec.vector.*;

public class AMALGAMBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing to setup
        }

    /** Updates the distribution given the current population, then 
        replaces the population with new samples generated from the distribution.
        Returns the revised population. */

    public Population breedPopulation(final EvolutionState state)
        {
        Population pop = state.population;
        
        for(int i = 0; i < pop.subpops.size(); i++)
            {
            Subpopulation subpop = pop.subpops.get(i);
            if (!(subpop.species instanceof AMALGAMSpecies))  // uh oh
                state.output.fatal("To use AMALGAMBreeder, subpopulation " + i + " must contain a AMALGAMSpecies.  But it contains a " + subpop.species);
                        
            AMALGAMSpecies species = (AMALGAMSpecies)(subpop.species);
                
            // update distribution[i] for subpop
            species.updateDistribution(state, subpop);
                
            // overwrite individuals
            ArrayList<Individual> inds = subpop.individuals;

            // The first individual (which is sorted by update distrbution to have the best fitness) remains unchanged 
            for(int j = 1; j < inds.size(); j++)
                inds.set(j, species.newIndividual(state, 0));

            // shift some number of individuals in the direction of the anticipated mean shift
            // as the individuals are random currently, just use the the first N
            int nAMS = (int)(species.alphaAMS*inds.size());
            // int nAMS = (int)(0.5*species.tau*inds.size());
            for(int j = 0; j < nAMS; j++){
                species.shiftIndividual(state,(DoubleVectorIndividual)inds.get(j+1));
                }

            species.firstGeneration = false;
            }
            
        return pop;
        }
    }
