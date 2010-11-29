/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.coevolve2;

import ec.*;
import ec.coevolve.*;
import ec.vector.DoubleVectorIndividual;
import ec.simple.SimpleFitness;

public class CoevolutionaryRosenbrock extends Problem implements GroupedProblemForm
    {
    double rosenbrock( double i, double j )
        {
        return - ( 100.0d*(i-j*j)*(i-j*j) + (1.0d-j)*(1.0d-j) );
        }

    public void preprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).setFitness( state, Integer.MIN_VALUE, false );
        }

    public void postprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                pop.subpops[i].individuals[j].evaluated = true;
        }

    public void evaluate(final EvolutionState state,
        final Individual[] ind,  // the individuals to evaluate together
        final boolean[] updateFitness,  // should this individuals' fitness be updated?
        final boolean countVictoriesOnly, // can be neglected in cooperative coevolution
        int[] subpops,
        final int threadnum)
        {
        if( ind.length != 2 ||
            ( ! ( ind[0] instanceof CoevolutionaryDoubleVectorIndividual ) ) ||
            ( ! ( ind[1] instanceof CoevolutionaryDoubleVectorIndividual ) ) )
            {
            state.output.error( "There should be two subpopulations, both with CoevolutionaryDoubleVectorIndividual." );
            }

        CoevolutionaryDoubleVectorIndividual ind1 = (CoevolutionaryDoubleVectorIndividual)(ind[0]);
        CoevolutionaryDoubleVectorIndividual ind2 = (CoevolutionaryDoubleVectorIndividual)(ind[1]);

        double i = ind1.genome[0];
        double j = ind2.genome[0];
        double functionValue = rosenbrock( i, j );

        if( updateFitness[0] )
            {
            if( functionValue > ind1.fitness.fitness() )
                {
                ((SimpleFitness)(ind1.fitness)).setFitness( state, (float)functionValue, false );
                ind1.context = new CoevolutionaryDoubleVectorIndividual[2];
                ind1.context[1] = ind2;
                }
            }
        if( updateFitness[1] )
            {
            if( functionValue > ind2.fitness.fitness() )
                {
                ((SimpleFitness)(ind2.fitness)).setFitness( state, (float)functionValue, false );
                ind2.context = new CoevolutionaryDoubleVectorIndividual[2];
                ind2.context[0] = ind1;
                }
            }
        }

    }
