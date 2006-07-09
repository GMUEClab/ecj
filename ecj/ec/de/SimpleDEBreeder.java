package ec.de;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * SimpleDEBreeder.java
 * 
 * Created: Wed Apr 26 18:05:31 2006
 * By: Liviu Panait
 */

/**
 * SimpleDEBreeder implements an adaptive Differential Evolution Algorithm, as communicated personally
 * by Dr. Kenneth Price.  The algorithm might also be explored in the recent book
 * "Differential Evolution: A Practical Approach to Global Optimization"
 * by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 * The code relies (with permission from the original authors) on the DE algorithms posted at
 * http://www.icsi.berkeley.edu/~storn/code.html .  For more information on
 * Differential Evolution, please refer to the aforementioned webpage and book.

 * @author Liviu Panait
 * @version 1.0
 */

public class SimpleDEBreeder extends DEBreeder
    {

    public double Pm;
        
    public double K;

    public double F;

    // algorithm for generation random numbers N(0,1), as described at http://www.taygeta.com/random/gaussian.html
    double otherN01 = 1e100;
    double N01( final EvolutionState state )
        {
        if( otherN01 < 1e100 )
            {
            double x = otherN01;
            otherN01 = 1e100;
            return x;
            }
                
        double x1, x2, w;
 
        do
            {
            x1 = 2.0 * state.random[0].nextDouble() - 1.0;
            x2 = 2.0 * state.random[0].nextDouble() - 1.0;
            w = x1 * x1 + x2 * x2;
            }
        while ( w >= 1.0 );

        w = Math.sqrt( (-2.0 * Math.log( w ) ) / w );
        otherN01 = x1 * w;
        return x2 * w;
        }

    public Individual createIndividual( final EvolutionState state,
                                        int subpop,
                                        Individual[] inds,
                                        int index )
        {
        // default value for mutation probability Pm
        double Pm = 1.0 / inds.length;

        // default value for scaling factor F
        double F = state.random[0].nextBoolean(0.5) ?
            1.0 :
            ( 1.9 / Math.sqrt(inds.length) );

        // K is random value distributed N(0,1)
        double K = N01(state);

        // select three indexes different from each other and from that of the current parent
        int r0, r1, r2;
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
        do
            {
            r2 = state.random[0].nextInt(inds.length);
            }
        while( r2 == r1 || r2 == r0 || r2 == index );

        DoubleVectorIndividual v = (DoubleVectorIndividual)(inds[index].clone());
        DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
        DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
        DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);

        int dim = v.genome.length;
        int localIndex = state.random[0].nextInt(dim);
        int counter = 0;

        // create the child
        do
            {
            if( state.random[0].nextDouble() <= Pm )
                {
                v.genome[localIndex] = v.genome[localIndex] + F * (g1.genome[localIndex] - g2.genome[localIndex]);
                }
            else
                {
                v.genome[localIndex] = v.genome[localIndex] + K * (g0.genome[localIndex] - v.genome[localIndex]);
                }
            localIndex = (localIndex+1) % dim;
            }
        while (++counter < dim);

        return v;

        }

    }
