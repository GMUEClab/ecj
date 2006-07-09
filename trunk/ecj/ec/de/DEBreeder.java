package ec.de;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * DEBreeder.java
 * 
 * Created: Wed Apr 26 17:37:59 2006
 * By: Liviu Panait
 */

/**
 * DEBreeder provides a straightforward Differential Evolution (DE) breeder
 * for the ECJ system.  The code relies (with permission from the original
 * authors) on the DE algorithms posted at
 * http://www.icsi.berkeley.edu/~storn/code.html .  For more information on
 * Differential Evolution, please refer to the aforementioned webpage.

 * @author Liviu Panait
 * @version 1.0
 */

public abstract class DEBreeder extends Breeder
    {
    // the previous population is stored in order to have parents compete directly with their children
    public Population previousPopulation = null;

    public void setup(final EvolutionState state, final Parameter base) 
        {
        // there is nothing to set up by default
        }

    // this function is called just before chldren are to be bred
    public void prepareDEBreeder(EvolutionState state)
        {
        }

    public Population breedPopulation(EvolutionState state)
        {
        // keep the better of the previous population and the current one, if there is a previous population.
        // in case this function has been performed by DEStatistics, this code is skipped
        if( previousPopulation != null )
            {
            if( previousPopulation.subpops.length != state.population.subpops.length )
                state.output.fatal( "The current population should have the same number of subpopulations as the previous population." );
            for( int i = 0 ; i < previousPopulation.subpops.length ; i++ )
                {
                if( state.population.subpops[i].individuals.length != previousPopulation.subpops[i].individuals.length )
                    state.output.fatal( "Subpopulation " + i + " should have the same number of individuals in all generations." );
                for( int j = 0 ; j < state.population.subpops[i].individuals.length ; j++ )
                    if( previousPopulation.subpops[i].individuals[j].fitness.betterThan( state.population.subpops[i].individuals[j].fitness ) )
                        state.population.subpops[i].individuals[j] = previousPopulation.subpops[i].individuals[j];
                }
            previousPopulation = null;
            }

        // prepare the breeder (some global statistics might need to be computed here)
        prepareDEBreeder(state);

        // create the new population
        Population newpop = (Population) state.population.emptyClone();

        // breed the children
        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
            {
            Individual[] inds = state.population.subpops[subpop].individuals;
            for( int i = 0 ; i < inds.length ; i++ )
                {
                newpop.subpops[subpop].individuals[i] = createIndividual( state, subpop, inds, i );
                }
            }

        // store the current population for competition with the new children
        previousPopulation = state.population;
        return newpop;
        }

    public abstract Individual createIndividual( final EvolutionState state,
                                                 int subpop,
                                                 Individual[] inds,
                                                 int index );

    }
