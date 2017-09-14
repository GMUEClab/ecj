package ec.app.tutorial3copy;

import ec.util.*;
import ec.*;
import ec.simple.*;
import ec.vector.*;

public class OddRosenbrock extends Problem implements SimpleProblemForm
    {
    public void setup(final EvolutionState state, final Parameter base) { }
        
    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if( !( ind instanceof DoubleVectorIndividual) )
            state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals.");

        double[] genome = ((DoubleVectorIndividual)ind).genome;
        int len = genome.length;
        double value = 0;
                

        for( int i = 1; i < len; i++ )
            value += 100*(genome[i-1]*genome[i-1]-genome[i])*
                (genome[i-1]*genome[i-1]-genome[i]) + 
                (1-genome[i-1])*(1-genome[i-1]);


        value = 1.0 / (1.0 + value );
        ((SimpleFitness)(ind.fitness)).setFitness( state, value, value==1.0);

        ind.evaluated = true;
        }
    }














