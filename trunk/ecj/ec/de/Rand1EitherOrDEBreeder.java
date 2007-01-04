package ec.de;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * Rand1EitherOrDEBreeder.java
 * 
 * Created: Wed Apr 26 18:01:11 2006
 * By: Liviu Panait
 */

/**
 * Rand1EitherOrDEBreeder implements the DE/rand/1/either-or Differential Evolution Algorithm,
 * explored recently in the "Differential Evolution: A Practical Approach to Global Optimization"
 * book by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 * The code relies (with permission from the original authors) on the DE algorithms posted at
 * http://www.icsi.berkeley.edu/~storn/code.html .  For more information on
 * Differential Evolution, please refer to the aforementioned webpage and book.

 * @author Liviu Panait
 * @version 1.0
 */

public class Rand1EitherOrDEBreeder extends DEBreeder
    {
    // Pm is the probability of mutation.
    public double Pm;
    // A good default value for K is 0.5 * ( 1 + F )
    public double F;
    // F is scale factor
    public double K;
    
    public static final String P_Pm = "Pm";
    public static final String P_K = "K";
    public static final String P_F = "F";

    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);

        Pm = state.parameters.getDouble(base.push(P_Pm),null,0.0);
        if ( Pm < 0.0 || Pm > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_Pm), null );

        F = state.parameters.getDouble(base.push(P_F),null,0.0);
        if ( F < 0.0 || F > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_F), null );

        if( state.parameters.exists(base.push(P_K)) )
            {
            K = state.parameters.getDouble(base.push(P_K),null,0.0);
            if ( K < 0.0 || K > 1.0 )
                state.output.fatal( "Parameter has value  outside of [0.0,1.0].", base.push(P_K), null );
            }
        else
            K = 0.5 * ( 1.0 + F ); // default value

        }

    public Individual createIndividual( final EvolutionState state,
                                        int subpop,
                                        Individual[] inds,
                                        int index,
                                        int thread )
        {
        // select three indexes different from each other and from that of the current parent
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
            if( state.random[thread].nextDouble() <= Pm )
                {
                v.genome[localIndex] = g0.genome[localIndex] + F * (g1.genome[localIndex] - g2.genome[localIndex]);
                }
            else
                {
                v.genome[localIndex] = g0.genome[localIndex] + K * (g1.genome[localIndex] + g2.genome[localIndex] - g0.genome[localIndex]);
                }
            localIndex = ++localIndex % dim;
            }
        while (++counter < dim);

        return v;

        }

    }
