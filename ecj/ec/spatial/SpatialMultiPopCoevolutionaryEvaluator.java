/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.*;
import ec.util.*;
import ec.coevolve.*;

/* 
 * SpatialMultiPopCoevolutionaryEvaluator.java
 * 
 * By: Liviu Panait
 */

/** 
 * SpatialMultiPopCoevolutionaryEvaluator implements a coevolutionary evaluator involving multiple
 * spatially-embedded subpopulations.
 *
 *
 *
 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class SpatialMultiPopCoevolutionaryEvaluator extends Evaluator
    {

    /** The preamble for selecting partners from each subpopulation. */
    public static final String P_SUBPOP = "subpop";

    /** The selection method used to select the other partners from the previous generation. */
    public static final String P_SELECTIONMETHOD = "select";
    SelectionMethod[] selectionMethod;

    /** The number of partners used for the coevolutionary evaluation from each of the subpopulations. */
    public static final String P_NUM_PARTNERS = "num-partners";
    int[] numPartners;

    /** Whether one set of partners should be selected as the individuals with the same index from the other subpopulations. */
    public static final String P_USE_SAME_LOCATION_PARTNER = "use-same-location-partners";
    boolean[] sameLocationPartners;

    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );

        // at this point, we do not know the number of subpopulations, so we read it as well from the parameters file
        Parameter tempSubpop = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numSubpopulations = state.parameters.getInt( tempSubpop, null, 0 );
        if( numSubpopulations <= 0 )
            state.output.fatal( "Parameter not found, or it has a non-positive value.", tempSubpop );

        selectionMethod = new SelectionMethod[numSubpopulations];
        numPartners = new int[numSubpopulations];
        sameLocationPartners = new boolean[numSubpopulations];

        for( int i = 0 ; i < numSubpopulations ; i++ )
            {
            if( state.parameters.exists(base.push(P_SUBPOP).push(""+i).push(P_USE_SAME_LOCATION_PARTNER)) )
                sameLocationPartners[i] = state.parameters.getBoolean(base.push(P_SUBPOP).push(""+i).push(P_USE_SAME_LOCATION_PARTNER),null,true);
            else
                state.output.fatal( "Parameter not found. " + base.push(P_SUBPOP).push(""+i).push(P_USE_SAME_LOCATION_PARTNER) );

            numPartners[i] = state.parameters.getInt( base.push(P_SUBPOP).push(""+i).push(P_NUM_PARTNERS), null, 0 );
            if( numPartners[i] < 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_SUBPOP).push(""+i).push(P_NUM_PARTNERS) );
            else if( (!sameLocationPartners[i]) || (numPartners[i]>1) ) // greater than 1, because for 1 we only used the partners at the same location
                {
                selectionMethod[i] = (SelectionMethod)
                    (state.parameters.getInstanceForParameter(
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTIONMETHOD),null,SelectionMethod.class));
                selectionMethod[i].setup(state,base.push(P_SUBPOP).push(""+i).push(P_SELECTIONMETHOD));
                }
            }
        }

    public boolean runComplete( final EvolutionState state )
        {
        return false;
        }

    public void evaluatePopulation(final EvolutionState state)
        {
        ((GroupedProblemForm)p_problem).preprocessPopulation(state,state.population);
        performCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        ((GroupedProblemForm)p_problem).postprocessPopulation(state, state.population);
        }

    private Individual[] mates = null;
    private boolean[] updates = null;
    public void performCoevolutionaryEvaluation( final EvolutionState state,
                                                 final Population population,
                                                 final GroupedProblemForm prob )
        {

        for( int i = 0 ; i < selectionMethod.length ; i++ )
            if( (!sameLocationPartners[i]) || (numPartners[i]>1) )
                selectionMethod[i].prepareToProduce( state, i, 0 );

        // the individuals to be evaluated together (one from each subpopulation)
        if( mates == null || mates.length != numPartners.length )
            mates = new Individual[numPartners.length];

        // the fitnesses of which individuals need to be updated
        if( updates == null || updates.length != mates.length )
            updates = new boolean[mates.length];
        for( int i = 0 ; i < updates.length ; i++ )
            updates[i] = false;

        // the indexes (in order to generate all possible combinations)
        int[] indexes = new int[numPartners.length-1];

        for( int i = 0 ; i < population.subpops.length ; i++ )
            {
            updates[i] = true;

            int totalCases = 1;
            for( int j = 0 ; j < population.subpops.length ; j++ )
                if( j != i )
                    totalCases = totalCases * numPartners[j];

            for( int j = 0 ; j < population.subpops[i].individuals.length ; j++ )
                {
                mates[i] = population.subpops[i].individuals[j];

                // generate all possible combinations of mates
                for( int k = 0 ; k < indexes.length ; k++ )
                    indexes[k] = 0;
                for( int testcase = 0 ; testcase < totalCases ; testcase++ )
                    {
                    // select the mates according to the current case
                    int curI = 0;
                    for( int k = 0 ; k < mates.length ; k++ )
                        if( k != i )
                            {
                            if( (indexes[curI]==0) && sameLocationPartners[i] ) // the first partner is specified as the one at that location
                                {
                                mates[k] = population.subpops[k].individuals[j];
                                }
                            else // the remaining individuals are to be selected from the neighborhood
                                {
                                Space space = null;
                                try
                                    {
                                    space = (Space)(state.population.subpops[k]);
                                    }
                                catch( ClassCastException e )
                                    {
                                    state.output.fatal( "SpatialMultiPopCoevolutionaryEvaluator found that subpopulation " + k + " is not a spatially-embedded.\n" + e );
                                    }

                                space.setIndex(0,j);
                                                                
                                selectionMethod[k].produce( 1,1,k,k,mates,state,0 );  // we overwrite the individual with the selected one
                                }
                            curI++;
                            }

                    // perform the coevolutionary evaluation of the group of individuals
                    prob.evaluate(state,mates,updates,false,0);

                    curI = 0;
                    // select the next case
                    for( int k = 0 ; k < numPartners.length ; k++ )
                        if( k != i )
                            {
                            if( indexes[curI] < numPartners[k]-1 )
                                {
                                indexes[curI]++;
                                break;
                                }
                            else
                                {
                                indexes[curI] = 0;
                                }
                            curI++;
                            }
                    }
                }

            updates[i] = false;
            }

        for( int i = 0 ; i < selectionMethod.length ; i++ )
            if( (!sameLocationPartners[i]) || (numPartners[i]>1) )
                selectionMethod[i].finishProducing( state, i, 0 );

        }

    }
