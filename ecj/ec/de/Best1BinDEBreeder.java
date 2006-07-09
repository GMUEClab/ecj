package ec.de;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * Best1BinDEBreeder.java
 * 
 * Created: Wed Apr 26 17:50:17 2006
 * By: Liviu Panait
 */

/**
 * Best1BinDEBreeder implements the DE/best/1/bin Differential Evolution algorithm.
 * The code relies (with permission from the original authors) on the DE algorithms posted at
 * http://www.icsi.berkeley.edu/~storn/code.html .  For more information on
 * Differential Evolution, please refer to the aforementioned webpage.

 * @author Liviu Panait
 * @version 1.0
 */

public class Best1BinDEBreeder extends DEBreeder
    {

    // the best individuals in each population (required by some DE breeders)
    public Individual[] bestSoFar = null;

    // Cr is crossover constant.  A default value of 0.9 might be a good idea.
    public static final String P_Cr = "Cr";
    public double Cr;

    // F is weighting factor.  A default value of 0.8 might be a good idea.
    public static final String P_F = "F";
    public double F;

    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);

        Cr = state.parameters.getDouble(base.push(P_Cr),null,0.0);
        if ( Cr < 0.0 || Cr > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_Cr), null );

        F = state.parameters.getDouble(base.push(P_F),null,0.0);
        if ( F < 0.0 || F > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_F), null );
        }

    public void prepareDEBreeder(EvolutionState state)
        {
        // update the bestSoFar for each population
        if( bestSoFar == null || state.population.subpops.length != bestSoFar.length )
            bestSoFar = new Individual[state.population.subpops.length];

        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
            {
            Individual[] inds = state.population.subpops[subpop].individuals;
            for( int j = 0 ; j < inds.length ; j++ )
                if( bestSoFar[subpop] == null || inds[j].fitness.betterThan(bestSoFar[subpop].fitness) )
                    bestSoFar[subpop] = inds[j];
            }

        }

    public Individual createIndividual( final EvolutionState state,
                                        int subpop,
                                        Individual[] inds,
                                        int index )
        {
        DoubleVectorIndividual xbest = (DoubleVectorIndividual)(bestSoFar[subpop]);
        // select two indexes different from that of the current parent
        int r0, r1;
        do
            {
            r0 = state.random[0].nextInt(inds.length);
            }
        while( r0 == index );
        do
            {
            r1 = state.random[0].nextInt(inds.length);
            }
        while( r1 == r0 || r1 == index );

        DoubleVectorIndividual v = (DoubleVectorIndividual)(inds[index].clone());
        DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
        DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);

        int dim = v.genome.length;
        int localIndex = state.random[0].nextInt(dim);
        int counter = 0;

        // create the child
        while (counter++ < dim)
            { 
            if ((state.random[0].nextDouble() < Cr) || (counter == dim))
                v.genome[localIndex] = xbest.genome[localIndex] + F * (g0.genome[localIndex] - g1.genome[localIndex]);
            localIndex = ++localIndex % dim;
            }
                
        return v;
                
        }

    }
