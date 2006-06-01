/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.coevolve1;

import ec.simple.SimpleFitness;
import ec.coevolve.*;
import ec.*;
import ec.vector.*;

public class CompetitiveMaxOne extends Problem implements GroupedProblemForm
    {

    public void preprocessPopulation( final EvolutionState state, Population pop )
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).setFitness( state, 0, false );
        }

    public void postprocessPopulation( final EvolutionState state, Population pop )
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                {
                pop.subpops[i].individuals[j].evaluated = true;
                }
        }

    public void evaluate(final EvolutionState state,
                         final Individual[] ind,  // the individuals to evaluate together
                         final boolean[] updateFitness,  // should this individuals' fitness be updated?
                         final boolean countVictoriesOnly,
                         final int threadnum)
        {
        if( ind.length != 2 || updateFitness.length != 2 )
            state.output.fatal( "The InternalSumProblem evaluates only two individuals at a time." );

        if( ! ( ind[0] instanceof BitVectorIndividual ) )
            state.output.fatal( "The individuals in the InternalSumProblem should be FloatVectorIndividuals." );

        if( ! ( ind[1] instanceof BitVectorIndividual ) )
            state.output.fatal( "The individuals in the InternalSumProblem should be FloatVectorIndividuals." );
        
        int value1=0;
        int value2=0;

        BitVectorIndividual temp;

        // calculate the function value for the first individual
        temp = (BitVectorIndividual)ind[0];
        for( int i = 0 ; i < temp.genome.length ; i++ )
            if( temp.genome[i] ) value1++;

        // calculate the function value for the second individual
        temp = (BitVectorIndividual)ind[1];
        for( int i = 0 ; i < temp.genome.length ; i++ )
            if( temp.genome[i] ) value2 ++;

        boolean firstWinsIfDraw = false;
        if( value1 == value2 )
            firstWinsIfDraw = state.random[threadnum].nextBoolean( 0.5 );

        float prevFit1 = ((SimpleFitness)(ind[0].fitness)).fitness();
        float prevFit2 = ((SimpleFitness)(ind[1].fitness)).fitness();

        if( updateFitness[0] )
            {
            if( countVictoriesOnly )
                {
                if( ( value1 > value2 ) || 
                    ( value1 == value2 && firstWinsIfDraw ) )
                    {
                    ((SimpleFitness)(ind[0].fitness)).setFitness( state, (float)(prevFit1+1), false );
                    }
                }
            else
                ((SimpleFitness)(ind[0].fitness)).setFitness( state, (float)(prevFit1+value1-value2), false );
            }

        if( updateFitness[1] )
            {
            if( countVictoriesOnly )
                {
                if( ( value2 > value1 ) ||
                    ( value2 == value1 && !firstWinsIfDraw ) )
                    {
                    ((SimpleFitness)(ind[1].fitness)).setFitness( state, (float)(prevFit2+1), false );
                    }
                }
            else
                ((SimpleFitness)(ind[1].fitness)).setFitness( state, (float)(prevFit2+value2-value1), false );
            }

        }

    }





