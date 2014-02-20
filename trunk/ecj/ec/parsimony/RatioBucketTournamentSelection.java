/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.parsimony;

import ec.*;
import ec.util.*;
import ec.steadystate.*;
import ec.select.*;

/* 
 * RatioBucketTournamentSelection.java
 * 
 * Created: Mon Apr 09 17:02:30 2001
 * By: Liviu Panait
 */

/**
 *
 * Does a tournament selection, limited to the subpopulation it's
 * working in at the time.
 *
 * <p>Ratio Bucket Lexicographic Tournament selection works like as follows. The sizes of buckets are
 * proportioned so that low-fitness individuals are placed into much larger buckets than high-fitness
 * individuals.  A bucket ratio <i>1/ratio</i> is specified beforehand.  The bottom <i>1/ratio</i> individuals
 * of the population are placed into the bottom bucket. If any individuals remain in the population
 * with the same fitness as the best individual in the bottom bucket, they too are placed in that bucket.
 * Of the remaining population, the next <i>1/ratio</i> individuals are placed into the next bucket, plus any
 * individuals remaining in the population with the same fitness as the best individual now in that bucket,
 * and so on.  This continues until every member of the population has been placed in a bucket. Once again,
 * the fitness of every individual in a bucket is set to the rank of the bucket relative to other buckets.
 * Ratio bucketing thus allows parsimony to have more of an effect on average when two similar low-fitness
 * individuals are considered than when two high-fitness individuals are considered.
 *
 * After ranking the individuals, <i>size</i> individuals are chosen at random from the
 * population. Of those individuals, the one with the highest rank is selected. If the two
 * individuals are in the same rank, meaning that they have similar fitness, the one
 * with the smallest size is selected.
 *
 * <p>Bucket Lexicographic Tournament selection is so simple that it doesn't
 * need to maintain a cache of any form, so many of the SelectionMethod methods
 * just don't do anything at all.
 *

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>size</tt><br>
 <font size=-1>int &gt;= 1 (default 7)</font></td>
 <td valign=top>(the tournament size)</td></tr>

 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick the <i>worst</i> individual in the tournament instead of the <i>best</i>?)</td></tr>

 <tr><td valign=top><i>base.</i><tt>ratio</tt><br>
 <font size=-1>double &gt;= 2 (default)</font></td>
 <td valign=top>(the ratio of worst out of remaining individuals that go in the next bucket)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.ratio-bucket-tournament

 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class RatioBucketTournamentSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_RATIO_BUCKET_TOURNAMENT = "ratio-bucket-tournament";

    /** size parameter */
    public static final String P_SIZE = "size";

    /** Size of the tournament*/
    public int size;

    /** if the worst individual should be picked in the tournament */
    public static final String P_PICKWORST = "pick-worst";

    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;

    /** The value of RATIO: each step, the worse 1/RATIO individuals are assigned the same fitness */
    public static final String P_RATIO = "ratio";

    /** The default value for RATIO */
    static double defaultRATIO = 2;

    /** The value of RATIO */
    public double ratio;

    // the indexes of the buckets where the individuals should go (will be used instead of fitness)
    int[] bucketValues;
 
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_RATIO_BUCKET_TOURNAMENT);
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        size = state.parameters.getInt(base.push(P_SIZE),def.push(P_SIZE),1);
        if (size < 1)
            state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));

        if( state.parameters.exists( base.push(P_RATIO), def.push(P_RATIO)))
            {
            ratio = state.parameters.getDouble(base.push(P_RATIO),def.push(P_RATIO),2.0f);
            if( ratio<2 )
                {
                state.output.fatal("The value of b must be >= 2.",base.push(P_RATIO),def.push(P_RATIO));
                }
            }
        else
            {
            ratio = defaultRATIO;
            }

        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);
        }

    /** Prepare to produce: create the buckets!!!! */
    public void prepareToProduce(final EvolutionState state, final int subpopulation, final int thread) 
        {
        bucketValues = new int[ state.population.subpops[subpopulation].individuals.length ];
        
        // correct?
        java.util.Arrays.sort(state.population.subpops[subpopulation].individuals,
            new java.util.Comparator()
                {
                public int compare(Object o1, Object o2)
                    {
                    Individual a = (Individual) o1;
                    Individual b = (Individual) o2;
                    if (a.fitness.betterThan(b.fitness))
                        return 1;
                    if (b.fitness.betterThan(a.fitness))
                        return -1;
                    return 0;
                    }
                });

        // how many individuals in current bucket
        int nInd;

        double totalInds = ((double)state.population.subpops[subpopulation].individuals.length);
        double averageBuck = Math.max( totalInds/ratio, 1 );

        // first individual goes into first bucket
        bucketValues[0] = 0;

        // now there is one individual in the first bucket
        nInd = 1;
        totalInds--;

        for( int i = 1 ; i < state.population.subpops[subpopulation].individuals.length ; i++ )
            {
            // if there is still some place left in the current bucket, throw the current individual there too
            if( nInd < averageBuck )
                {
                bucketValues[i] = bucketValues[i-1];
                nInd++;
                }
            else // check if it has the same fitness as last individual
                {
                if( ((Individual)state.population.subpops[subpopulation].individuals[i]).fitness.equivalentTo(
                        ((Individual)state.population.subpops[subpopulation].individuals[i-1]).fitness ) )
                    {
                    // now the individual has exactly the same fitness as previous one,
                    // so we just put it in the same bucket as the previous one(s)
                    bucketValues[i] = bucketValues[i-1];
                    nInd++;
                    }
                else
                    {
                    // new bucket!!!!
                    averageBuck = Math.max( totalInds/ratio, 1 );
                    bucketValues[i] = bucketValues[i-1] - 1; // decrease the fitness, so that high fit individuals have lower bucket values
                    // with only one individual
                    nInd = 1;
                    }
                }
            totalInds--;
            }
        }

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        // pick size random individuals, then pick the best.
        Individual[] oldinds = (state.population.subpops[subpopulation].individuals);
        int i = state.random[thread].nextInt(oldinds.length);
        long si = 0;

        for (int x=1;x<size;x++)
            {
            int j = state.random[thread].nextInt(oldinds.length);
            if (pickWorst)
                {
                if( bucketValues[j]>bucketValues[i] ) { i = j; si = 0; }
                else if( bucketValues[i]>bucketValues[j] ) { } // do nothing
                else
                    {
                    if (si==0)
                        si = oldinds[i].size();
                    long sj = oldinds[j].size();

                    if (sj >= si) // sj's got worse lookin' trees
                        { i = j; si = sj; }
                    }
                }
            else
                { 
                if( bucketValues[j]<bucketValues[i] ) { i = j; si = 0; }
                else if( bucketValues[i]<bucketValues[j] ) { } // do nothing
                else
                    {
                    if (si==0)
                        si = oldinds[i].size();
                    long sj = oldinds[j].size();

                    if (sj < si) // sj's got better lookin' trees
                        { i = j; si = sj; }
                    }
                }
            }
        return i;
        }

    // included for SteadyState
    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual)
        { return; }
    
    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        { return; }
    
    }
