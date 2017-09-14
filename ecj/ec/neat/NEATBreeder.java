/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import ec.*;
import ec.util.*;

/**
 * NEATBreeder is a Breeder which overrides the breedPopulation method to first
 * mark the individuals in each subspecies that are allow to reproduce, and
 * replace the population with new individuals in each subspecies. All the heavy
 * lifting is done in NEATSpecies and NEATSubspecies, not here.
 * 
 * @author Ermo Wei and David Freelan
 */

public class NEATBreeder extends Breeder
    {

    public void setup(EvolutionState state, Parameter base)
        {
        // nothing to setup here
        }

    /**
     * This method simply call breedNewPopulation method in NEATSpecies，where
     * all the critical work in done.
     */
    public Population breedPopulation(EvolutionState state)
        {
       
        Population pop = state.population;
        for (int i = 0; i < pop.subpops.size(); i++)
            {
            Subpopulation subpop = pop.subpops.get(i);
            if (!(subpop.species instanceof NEATSpecies))  // uh oh
                state.output.fatal("To use NEATSpecies, subpopulation " + i
                    + " must contain a NEATSpecies.  But it contains a " + subpop.species);

            NEATSpecies species = (NEATSpecies) (subpop.species);

         

         
            species.breedNewPopulation(state, i, 0);

          
            }

        return pop;
        }

    }
