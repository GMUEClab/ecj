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
import java.util.*;

public class CompetitiveMaxOnes extends Problem implements GroupedProblemForm
    {
    public void preprocessPopulation(final EvolutionState state, Population pop, boolean[] updateFitness, boolean countVictoriesOnly)
        {
        for(int i = 0; i < pop.subpops.size(); i++ )
            if (updateFitness[i])
                for(int j = 0; j < pop.subpops.get(i).individuals.size() ; j++ )
                    ((SimpleFitness)(pop.subpops.get(i).individuals.get(j).fitness)).trials = new ArrayList();
        }

    public int postprocessPopulation(final EvolutionState state, Population pop, boolean[] updateFitness, boolean countVictoriesOnly)
        {
        int total = 0;
        for(int i = 0; i < pop.subpops.size(); i++ )
            if (updateFitness[i])
                for(int j = 0; j < pop.subpops.get(i).individuals.size() ; j++ )
                    {
                    SimpleFitness fit = ((SimpleFitness)(pop.subpops.get(i).individuals.get(j).fitness));

                    // average of the trials we got
                    int len = fit.trials.size();
                    double sum = 0;
                    for(int l = 0; l < len; l++)
                        sum += ((Double)(fit.trials.get(l))).doubleValue();
                    sum /= len;
                                                                        
                    // we'll not bother declaring the ideal
                    fit.setFitness(state, sum, false);
                    pop.subpops.get(i).individuals.get(j).evaluated = true;
                    total++;
                    }
        return total;
        }

    public void evaluate(final EvolutionState state,
        final Individual[] ind,  // the individuals to evaluate together
        final boolean[] updateFitness,  // should this individuals' fitness be updated?
        final boolean countVictoriesOnly,
        int[] subpops,
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
            if( temp.genome[i] ) value2++;
                        
        double score = value1 - value2;

        if( updateFitness[0] )
            {
            SimpleFitness fit = ((SimpleFitness)(ind[0].fitness));
            fit.trials.add(new Double(score));
                        
            // set the fitness because if we're doing Single Elimination Tournament, the tournament
            // needs to know who won this time around.  Don't bother declaring the ideal here.
            fit.setFitness(state, score, false);
            }

        if( updateFitness[1] )
            {
            SimpleFitness fit = ((SimpleFitness)(ind[1].fitness));
            fit.trials.add(new Double(-score));

            // set the fitness because if we're doing Single Elimination Tournament, the tournament
            // needs to know who won this time around.
            fit.setFitness(state, -score, false);
            }
        }

    }





