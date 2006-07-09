package ec.de;

import ec.*;
import ec.util.*;

/* 
 * DEStatistics.java
 * 
 * Created: Wed Apr 26 17:11:23 2006
 * By: Liviu Panait
 */

/**
 * DEStatistics provides a straightforward solution to one problem
 * many existing ECJ statistics classes have when used in conjunction
 * with Differential Evolution (DE), namely reporting the fitness of individuals
 * after they have been evaluated.  The problem stems from the fact that all
 * individuals create children (there is no selection pressure).  Rather, the child
 * competes immediately with its parent, and only the best of the two survives.  As
 * a result, all other statistics classes would report the fitness of the child, as
 * opposed to the fitness of the better of the child and the parent.  In many cases,
 * that fitness might provide misleading information (for example, it might appear
 * that the average fitness of the population is too random, and that there is no
 * evident progress).  To fix this, the DEStatistics class performs the competition
 * between the child and the parent right before other statistics classes might be
 * invoked.  Make sure DEStatistics is set as the main statistics class, and the other
 * are set as its children.

 * @author Liviu Panait
 * @version 1.0
 */

public class DEStatistics extends Statistics
    {

    public void postEvaluationStatistics(final EvolutionState state)
        {
        // keep the better of the previous population and the current one, if there is a previous population
        if( state.breeder instanceof DEBreeder )
            {
            Population previousPopulation = ((DEBreeder)(state.breeder)).previousPopulation; // for faster access
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
                // remove the previous population from the DEBreeder, it is no longer needed
                ((DEBreeder)(state.breeder)).previousPopulation = null;
                }
            }

        // call other statistics
        super.postEvaluationStatistics(state);
        }

    }
