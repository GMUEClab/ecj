/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.*;
import ec.util.Parameter;

/**
 * A basic Breeder that generates a new population by asking each subpopulation's
 * Species to create new individuals.
 *
 * This is used with algorithms like GRASP, which generate new individuals at
 * each step (rather than producing them by running an old population through a pipeline).
 *
 * @author Eric O. Scott
 */
public class ConstructiveBreeder extends Breeder
    {
    private static final long serialVersionUID = 1;

    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        // Do nothing
        }

    @Override
    public Population breedPopulation(final EvolutionState state)
        {
        assert(state != null);

        final Population newPop = state.population.emptyClone();
        // For each subpopulation
        for (int i = 0; i < state.population.subpops.size(); i++)
            {
            final Subpopulation oldSubpop = state.population.subpops.get(i);
            final Species species = state.population.subpops.get(i).species;

            // Execute ants
            for (int j = 0; j < oldSubpop.individuals.size(); j++)
                {
                final Individual newInd = species.newIndividual(state, i);
                newPop.subpops.get(i).individuals.add(newInd);
                }
            }
        return newPop;
        }
    }
