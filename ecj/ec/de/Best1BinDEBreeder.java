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
 * Best1BinDEBreeder is a differential evolution breeding operator.
 * The code is derived from a DE algorithm, known as DE/best/1/bin "with uniform jitter", 
 * found on page 140 of
 * "Differential Evolution: A Practical Approach to Global Optimization"
 * by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 *
 * <p>Best1BinDEBreeder requires that all individuals be DoubleVectorIndividuals.
 *
 * <p>In short, the algorithm is as follows.  For each individual in the population, we produce a child
 * by first selecting the best individual in the population, which we call r0.  We then
 * select two more (different) individuals, none the original individual nor r0, called r1 and r2.
 * We then create an individal c, defined as c = r0 + FJitter() * (r1 - r2), where FJitter() is
 * a per-gene independent random number defined as F + F_NOISE * (random(0,1) - 0.5).  A common value for
 * F_NOISE is 0.001.  Last, we cross over c with the
 * original individual and produce a single child, using uniform crossover with gene-independent 
 * crossover probability "Cr".
 *
 * <p>This class should be used in conjunction with 
 * DEEvaluator, which allows the children to enter the population only if they're superior to their
 * parents (the original individuals).  If so, they replace their parents.
 * 
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>f-noise</tt><br>
 * <font size=-1>0.0 &lt;= double </font></td>
 * <td valign=top>The "F_NOISE" jitter value</td></tr>
 *
 * </table>
 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0
 */

public class Best1BinDEBreeder extends DEBreeder
    {
    /** limits on uniform noise for F */
    public double F_NOISE = 0.0;
        
    public static final String P_FNOISE = "f-noise";
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);

        F_NOISE = state.parameters.getDouble(base.push(P_FNOISE), null, 0.0);
        if ( F_NOISE < 0.0 )
            state.output.fatal( "Parameter not found, or its value is below 0.0.", base.push(P_FNOISE), null );
        }
        

    public DoubleVectorIndividual createIndividual( final EvolutionState state,
        int subpop,
        int index,
        int thread)
        {
        Individual[] inds = state.population.subpops[subpop].individuals;
                
        DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops[subpop].species.newIndividual(state, thread));
        int retry = -1;
        do
            {
            retry++;
            
            // select three indexes different from each other and from that of the current parent
            int r0, r1, r2;
            // do
                    {
                    r0 = bestSoFarIndex[subpop];
                    }
            // while( r0 == index );
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

            DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds[r0]);
            DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds[r1]);
            DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds[r2]);

            for(int i = 0; i < v.genome.length; i++)
                v.genome[i] = g0.genome[i] + 
                    (F + state.random[thread].nextDouble() * F_NOISE - (F_NOISE / 2.0)) *
                    (g1.genome[i] - g2.genome[i]);
            }
        while(!valid(v) && retry < retries);
        if (retry >= retries && !valid(v))  // we reached our maximum
            {
            // completely reset and be done with it
            v.reset(state, thread);
            }
                                        
        return crossover(state, (DoubleVectorIndividual)(inds[index]), v, thread);
        }

    }
