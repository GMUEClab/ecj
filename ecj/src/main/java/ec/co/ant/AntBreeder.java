/*
 Copyright 2017 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package ec.co.ant;

import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.Subpopulation;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;

/**
 *
 * @author Eric O. Scott
 */
public class AntBreeder extends Breeder
{
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(repOK());
    }
    
    @Override
    public Population breedPopulation(final EvolutionState state)
    {
        assert(state != null);
        if (!(state.evaluator.p_problem instanceof ConstructiveProblemForm))
            {
            state.output.fatal(String.format("Attempted to use %s with problem %s, but %s can only be used with a %s.", this.getClass().getSimpleName(), state.evaluator.p_problem.getClass().getSimpleName(), this.getClass().getSimpleName(), ConstructiveProblemForm.class.getSimpleName()));
            }
            
        final Population newPop = state.population.emptyClone();
        for (int i = 0; i < state.population.subpops.size(); i++)
            {
            final Subpopulation oldSubpop = state.population.subpops.get(i);
            if (!(state.population.subpops.get(i).species instanceof AntSpecies))
                state.output.fatal(String.format("%s: subpopulation %d has a %s, but %s requires a %s.", this.getClass().getSimpleName(), i, state.population.subpops.get(i).species.getClass().getSimpleName(), AntSpecies.class.getSimpleName()));
            final AntSpecies species = (AntSpecies) state.population.subpops.get(i).species;
            final int numAnts = oldSubpop.individuals.size();
            assert(numAnts > 0);
            
            species.updatePheromones(state, oldSubpop);
            for (int j = 0; j < numAnts; j++)
                {
                final ConstructiveIndividual newInd = species.newIndividual(state, i);
                newPop.subpops.get(i).individuals.add(newInd);
                }
            assert(newPop.subpops.get(i).individuals.size() == numAnts);
            }
        return newPop;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return true;
    }
}
