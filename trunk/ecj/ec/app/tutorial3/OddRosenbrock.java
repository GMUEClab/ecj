/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.tutorial3;

import ec.util.*;
import ec.*;
import ec.simple.*;
import ec.vector.*;

public class OddRosenbrock extends Problem implements SimpleProblemForm
    {
    public void setup(final EvolutionState state, final Parameter base) { }

    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int threadnum)
        {
        if( !( ind instanceof DoubleVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals." );

        double[] genome = ((DoubleVectorIndividual)ind).genome;
        int len = genome.length;
        double value = 0;

        // Compute the Rosenbrock function for our genome
        for( int i = 1 ; i < len ; i++ )
            value += 100*(genome[i-1]*genome[i-1]-genome[i])*
                (genome[i-1]*genome[i-1]-genome[i]) +
                (1-genome[i-1])*(1-genome[i-1]);

        // Rosenbrock is a minimizing function which does not drop below 0. 
        // But SimpleFitness requires a maximizing function -- where 0 is worst
        // and 1 is best.  To use SimpleFitness, we must convert the function.
        // This is the Koza style of doing it:

        value = 1.0 / ( 1.0 + value );
        ((SimpleFitness)(ind.fitness)).setFitness( state, (float)value, value==1.0 );
    
        ind.evaluated = true;
        }

    public void describe(final Individual ind, 
                         final EvolutionState _state, 
                         final int threadnum,
                         final int log,
                         final int verbosity)  { return; }
    }
