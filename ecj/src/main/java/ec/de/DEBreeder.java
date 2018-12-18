package ec.de;

import java.util.ArrayList;

import ec.*;
import ec.util.*;
import ec.vector.*;

/* 
 * DEBreeder.java
 * 
 * Created: Wed Apr 26 17:37:59 2006
 * By: Liviu Panait
 */

/**
 * DEBreeder provides a straightforward Differential Evolution (DE) breeder
 * for the ECJ system.  The code is derived from the "classic" DE algorithm, known as DE/rand/1/bin, found on page 140 of
 * "Differential Evolution: A Practical Approach to Global Optimization"
 * by Kenneth Price, Rainer Storn, and Jouni Lampinen.
 *
 * <p>DEBreeder requires that all individuals be DoubleVectorIndividuals.
 *
 * <p>In short, the algorithm is as follows.  For each individual in the population, we produce a child
 * by selecting three (different) individuals, none the original individual, called r0, r1, and r2.
 * We then create an individal c, defined as c = r0 + F * (r1 - r2).  Last, we cross over c with the
 * original individual and produce a single child, using uniform crossover with gene-independent 
 * crossover probability "Cr".
 *
 * <p>This class should be used in conjunction with 
 * DEEvaluator, which allows the children to enter the population only if they're superior to their
 * parents (the original individuals).  If so, they replace their parents.
 * 
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>f</tt><br>
 * <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 * <td valign=top>The "F" mutation scaling factor</td></tr>
 *
 * <tr><td valign=top><i>base.</i><tt>cr</tt><br>
 * <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 * <td valign=top>The "Cr" probability of crossing over genes</td></tr>
 * </table>
 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0
 */

public class DEBreeder extends Breeder
    {
    public static final double CR_UNSPECIFIED = -1;

    /** Scaling factor for mutation */
    public double F = 0.0;
    /** Probability of crossover per gene */
    public double Cr = CR_UNSPECIFIED;
    
    public int retries = 0;
        
    public static final String P_F = "f";
    public static final String P_Cr = "cr";
    public static final String P_OUT_OF_BOUNDS_RETRIES = "out-of-bounds-retries";
        
    /** the previous population is stored in order to have parents compete directly with their children */
    public Population previousPopulation = null;

    /** the best individuals in each population (required by some DE breeders).  It's not required by DEBreeder's algorithm */
    public int[] bestSoFarIndex = null;

    public void setup(final EvolutionState state, final Parameter base) 
        {
        if (!state.parameters.exists(base.push(P_Cr), null))  // it wasn't specified -- hope we know what we're doing
            Cr = CR_UNSPECIFIED;
        else
            {
            Cr = state.parameters.getDouble(base.push(P_Cr),null,0.0);
            if ( Cr < 0.0 || Cr > 1.0 )
                state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_Cr), null );
            }
                        
        F = state.parameters.getDouble(base.push(P_F),null,0.0);
        if ( F < 0.0 || F > 1.0 )
            state.output.fatal( "Parameter not found, or its value is outside of [0.0,1.0].", base.push(P_F), null );
            
        retries = state.parameters.getInt(base.push(P_OUT_OF_BOUNDS_RETRIES), null, 0);
        if (retries < 0)
            state.output.fatal(" Retries must be a value >= 0.0.", base.push(P_OUT_OF_BOUNDS_RETRIES), null);
        }

    // this function is called just before chldren are to be bred
    public void prepareDEBreeder(EvolutionState state)
        {
        // update the bestSoFar for each population
        if( bestSoFarIndex == null || state.population.subpops.size() != bestSoFarIndex.length )
            bestSoFarIndex = new int[state.population.subpops.size()];

        for(int subpop = 0; subpop < state.population.subpops.size(); subpop++ )
            {
            ArrayList<Individual> inds = state.population.subpops.get(subpop).individuals;
            bestSoFarIndex[subpop] = 0;
            for( int j = 1 ; j < inds.size() ; j++ )
                if( inds.get(j).fitness.betterThan(inds.get(bestSoFarIndex[subpop]).fitness) )
                    bestSoFarIndex[subpop] = j;
            }
        }

    public Population breedPopulation(EvolutionState state)
        {
        // double check that we're using DEEvaluator
        if (!(state.evaluator instanceof DEEvaluator))
            state.output.warnOnce("DEEvaluator not used, but DEBreeder used.  This is almost certainly wrong.");
                
        // prepare the breeder (some global statistics might need to be computed here)
        prepareDEBreeder(state);

        // create the new population
        Population newpop = (Population) state.population.emptyClone();

        // breed the children
        for(int subpop = 0; subpop < state.population.subpops.size(); subpop++ )
            {

            if (state.population.subpops.get(subpop).individuals.size() < 4)  // Magic number, sorry.  createIndividual() requires at least 4 individuals in the pop
                state.output.fatal("Subpopulation " + subpop + " has fewer than four individuals, and so cannot be used with DEBreeder.");
            // by Ermo. We should use add instead of set in here
            //            ArrayList<Individual> inds = newpop.subpops.get(subpop).individuals;
            //            for( int i = 0 ; i < inds.size() ; i++ )
            //                {
            //                newpop.subpops.get(subpop).individuals.set(i, createIndividual( state, subpop, i, 0));  // unthreaded for now
            //                }
            
            ArrayList<Individual> inds = newpop.subpops.get(subpop).individuals;
            int size = state.population.subpops.get(subpop).individuals.size();
            for( int i = 0 ; i < size ; i++ )
                {
                newpop.subpops.get(subpop).individuals.add(createIndividual( state, subpop, i, 0));  // unthreaded for now
                }
            }

        // store the current population for competition with the new children
        previousPopulation = state.population;
        return newpop;
        }

    /** Tests the Individual to see if its values are in range. */
    public boolean valid(DoubleVectorIndividual ind)
        {
        //FloatVectorSpecies species = (FloatVectorSpecies)(ind.species);
        return (ind.isInRange());
        }

    public DoubleVectorIndividual createIndividual(
        EvolutionState state,
        int subpop,
        int index,
        int thread)
        {
        //Individual[] inds = state.population.subpops.get(subpop).individuals;
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
                v.genome[i] = g0.genome[i] + F * (g1.genome[i] - g2.genome[i]);
            }
        while(!valid(v) && retry < retries);
        if (retry >= retries && !valid(v))  // we reached our maximum
            {
            // completely reset and be done with it
            v.reset(state, thread);
            }

        return crossover(state, (DoubleVectorIndividual)(inds.get(index)), v, thread);
        }


    /** Crosses over child with target, storing the result in child and returning it.  The default
        procedure copies each value from the target, with independent probability CROSSOVER, into
        the child.  The crossover guarantees that at least one child value, chosen at random, will
        not be overwritten.  Override this method to perform some other kind of crossover. */
                
    public DoubleVectorIndividual crossover(EvolutionState state, DoubleVectorIndividual target, DoubleVectorIndividual child, int thread)
        {
        if (Cr == CR_UNSPECIFIED)
            state.output.warnOnce("Differential Evolution Parameter cr unspecified.  Assuming cr = 0.5");
                        
        // first, hold one value in abeyance
        int index = state.random[thread].nextInt(child.genome.length);
        double val = child.genome[index];
                
        // do the crossover
        for(int i = 0; i < child.genome.length; i++)
            {
            if (state.random[thread].nextDouble() < Cr)
                child.genome[i] = target.genome[i];
            }
                
        // reset the one value so it's not just a duplicate copy
        child.genome[index] = val;
        
        return child;
        }
                        
    }
