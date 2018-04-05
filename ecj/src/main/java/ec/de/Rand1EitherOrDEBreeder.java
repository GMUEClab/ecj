package ec.de;

import java.util.ArrayList;

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
 * Rand1EitherOrDEBreeder is a differential evolution breeding operator.
 * The code is derived from a DE algorithm, known as DE/rand/1/either-or, 
 * found on page 141 of
 * "Differential Evolution: A Practical Approach to Global Optimization"
 * by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 *
 * <p>Rand1EitherOrDEBreeder requires that all individuals be DoubleVectorIndividuals.
 *
 * <p>In short, the algorithm is as follows.  For each individual in the population, we produce a child
 * by selecting three (different) individuals, none the original individual, called r0, r1, and r2.
 * We then create an individal c, defined either c = r0 + F * (r1 - r2), or as c = r0 + 0.5 * (F+1) * (r1 + r2 - 2 * r0),
 * depending on a coin flip of probability "PF" (if 'true', the first equation is used, else the second).
 * Unlike the other DEBreeders in this package, we do *not* cross over the child with the original individual.
 * In fact, if the crossover probability is specified, Rand1EitherOrDEBreeder will issue a warning that it's
 * not using it.
 *
 * <p>This class should be used in conjunction with 
 * DEEvaluator, which allows the children to enter the population only if they're superior to their
 * parents (the original individuals).  If so, they replace their parents.
 * 
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>pf</tt><br>
 * <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 * <td valign=top>The "PF" probability of mutation type</td></tr>
 * </table>
 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0
 */


public class Rand1EitherOrDEBreeder extends DEBreeder
    {
    public double PF = 0.0;
        
    public static final String P_PF = "pf";
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state,base);

        PF = state.parameters.getDouble(base.push(P_PF),null,0.0);
        if ( PF < 0.0 || PF > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_PF), null );
                        
        if (state.parameters.exists(base.push(P_Cr), null))
            state.output.warning("Crossover parameter specified, but Rand1EitherOrDEBreeder does not use crossover.", base.push(P_Cr));
        }
        
    public DoubleVectorIndividual createIndividual( final EvolutionState state,
        int subpop,
        int index,
        int thread )
        {
        ArrayList<Individual> inds = state.population.subpops.get(subpop).individuals;

        DoubleVectorIndividual v = (DoubleVectorIndividual)(state.population.subpops.get(subpop).species.newIndividual(state, thread));
        int retry = -1;
        do
            {
            retry++;
            
            // select three indexes different from each other and from that of the current parent
            int r0, r1, r2;
            do
                {
                r0 = state.random[thread].nextInt(inds.size());
                }
            while( r0 == index );
            do
                {
                r1 = state.random[thread].nextInt(inds.size());
                }
            while( r1 == r0 || r1 == index );
            do
                {
                r2 = state.random[thread].nextInt(inds.size());
                }
            while( r2 == r1 || r2 == r0 || r2 == index );

            DoubleVectorIndividual g0 = (DoubleVectorIndividual)(inds.get(r0));
            DoubleVectorIndividual g1 = (DoubleVectorIndividual)(inds.get(r1));
            DoubleVectorIndividual g2 = (DoubleVectorIndividual)(inds.get(r2));

            for(int i = 0; i < v.genome.length; i++)
                if (state.random[thread].nextBoolean(PF))
                    v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i]);
                else
                    v.genome[i] = g0.genome[i] + 0.5 * (F+1) * (g1.genome[i] + g2.genome[i] - 2 * g0.genome[i]);
            }
        while(!valid(v) && retry < retries);
        if (retry >= retries && !valid(v))  // we reached our maximum
            {
            // completely reset and be done with it
            v.reset(state, thread);
            }

        return v;       // no crossover is performed
        }

    }
