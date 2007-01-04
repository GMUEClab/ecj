package ec.de;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * Rand1ExpDEBreeder.java
 * 
 * Created: Wed Apr 26 17:56:04 2006
 * By: Liviu Panait
 */

/**
 * Rand1ExpDEBreeder implements the DE/rand/1/exp Differential Evolution Algorithm,
 * explored recently in the "Differential Evolution: A Practical Approach to Global Optimization"
 * book by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 * The code relies (with permission from the original authors) on the DE algorithms posted at
 * http://www.icsi.berkeley.edu/~storn/code.html .  For more information on
 * Differential Evolution, please refer to the aforementioned webpage and book.

 * @author Liviu Panait
 * @version 1.0
 */

public class Rand1ExpDEBreeder extends DEBreeder
    {
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

    public Individual createIndividual( final EvolutionState state,
                                        int subpop,
                                        Individual[] inds,
                                        int index,
                                        int thread)
        {
        // select three indexes different from one another and from that of the current parent
        int r0, r1, r2;
        do
            {
            r0 = state.random[thread].nextInt(inds.length);
            }
        while( r0 == index );
        do
            {
            r1 = state.random[thread].nextInt(inds.length);
            }
        while( r1 == r0 || r1 == index );
        do
            {
            r2 = state.random[thread].nextInt(inds.length);
            }
        while( r2 == r1 || r2 == r0 || r2 == index );

        DoubleVectorIndividual v = (DoubleVectorIndividual)(inds[index].clone());
        DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
        DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
        DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);

        int dim = v.genome.length;
        int localIndex = state.random[thread].nextInt(dim);
        int counter = 0;

        // create the child
        do
            { 
            v.genome[localIndex] = g0.genome[localIndex] + F * (g1.genome[localIndex] - g2.genome[localIndex]);
            localIndex = ++localIndex % dim;
            }
        while ((state.random[thread].nextDouble() < Cr) && (++counter < dim));

        return v;

        }

    }
