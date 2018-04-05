/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.parsimony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ec.*;
import ec.util.*;
import ec.steadystate.*;
import ec.select.*;

/* 
 * BucketTournamentSelection.java
 * 
 * Created: Mon Apr 09 17:02:30 2001
 * By: Liviu Panait
 */

/**
 *
 * Does a tournament selection, limited to the subpopulation it's
 * working in at the time.
 *
 * <p>Bucket Lexicographic Tournament selection works like as follows. There is a 
 * number of buckets (<i>num-buckets</i>) specified beforehand, and each is
 * assigned a rank from 1 to <i>num-buckets</i>.  The population, of size <i>pop-size</i>,
 * is sorted by fitness.  The bottom <i>pop-size</i>/<i>num-buckets</i> individuals are
 * placed in the worst ranked bucket, plus any individuals remaining in the population with
 * the same fitness as the best individual in the bucket.  Then the second worst
 * <i>pop-size</i>/<i>num-buckets</i> individuals are placed in the second worst ranked bucket,
 * plus any individuals in the population equal in fitness to the best individual in that bucket.
 * This continues until there are no individuals in the population.  Note that the topmost bucket
 * with individuals can hold fewer than <i>pop-size</i>/<i>num-buckets</i> individuals, if
 * <i>pop-size</i> is not a multiple of <i>num-buckets</i>. Depending on the number of
 * equal-fitness individuals in the population, there can be some top buckets that are never
 * filled. The fitness of each individual in a bucket is set to the rank of the bucket holding
 * it.  Direct bucketing has the effect of trading off fitness differences for size. Thus the
 * larger the bucket, the stronger the emphasis on size as a secondary objective.
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

 <tr><td valign=top><i>base.</i><tt>num-buckets</tt><br>
 <font size=-1>int &gt;= 1 (default 10)</font></td>
 <td valign=top>(the number of buckets)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.bucket-tournament

 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class BucketTournamentSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** Default base */
    public static final String P_TOURNAMENT = "bucket-tournament";

    /** If the worst individual should be picked in the tournament */
    public static final String P_PICKWORST = "pick-worst";

    /** Tournament size parameter */
    public static final String P_SIZE = "size";

    /** The number of buckets */
    public static final String P_BUCKETS = "num-buckets";

    /** Default number of buckets */
    public static final int N_BUCKETS_DEFAULT = 10;

    /** Size of the tournament*/
    public int size;

    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;

    // the number of buckets
    int nBuckets;

    // the indexes of the buckets where the individuals should go (will be used instead of fitness)
    int[] bucketValues;
 
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_TOURNAMENT);
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        size = state.parameters.getInt(base.push(P_SIZE),def.push(P_SIZE),1);
        if (size < 1)
            state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));

        if( state.parameters.exists( base.push(P_BUCKETS), def.push(P_BUCKETS)))
            {
            nBuckets = state.parameters.getInt(base.push(P_BUCKETS),def.push(P_BUCKETS),1);
            if (nBuckets < 1)
                {
                state.output.fatal("The number of buckets size must be >= 1.",base.push(P_BUCKETS),def.push(P_BUCKETS));
                }
            }
        else
            {
            nBuckets = N_BUCKETS_DEFAULT;
            }

        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);
        }

    /** Prepare to produce: create the buckets!!!! */
    public void prepareToProduce(final EvolutionState state, final int subpopulation, final int thread) 
        {
        bucketValues = new int[ state.population.subpops.get(subpopulation).individuals.size() ];

        // correct?
        Collections.sort(state.population.subpops.get(subpopulation).individuals,
            new Comparator<Individual>() {
                public int compare(Individual a, Individual b) {
                    if (a.fitness.betterThan(b.fitness))
                        return 1;
                    if (b.fitness.betterThan(a.fitness))
                        return -1;
                    return 0;
                    }
                });


        // how many individuals in current bucket
        int nInd;

        double averageBuck = ((double) state.population.subpops.get(subpopulation).individuals.size())/
            ((double)nBuckets);

        // first individual goes into first bucket
        bucketValues[0] = 0;

        // now there is one individual in the first bucket
        nInd = 1;

        for(int i = 1; i < state.population.subpops.get(subpopulation).individuals.size() ; i++ )
            {
            // if there is still some place left in the current bucket, throw the current individual there too
            if( nInd < averageBuck )
                {
                bucketValues[i] = bucketValues[i-1];
                nInd++;
                }
            else // check if it has the same fitness as last individual
                {
                if( ((Individual) state.population.subpops.get(subpopulation).individuals.get(i)).fitness.equivalentTo(
                        ((Individual) state.population.subpops.get(subpopulation).individuals.get(i - 1)).fitness ) )
                    {
                    // now the individual has exactly the same fitness as previous one,
                    // so we just put it in the same bucket as the previous one(s)
                    bucketValues[i] = bucketValues[i-1];
                    nInd++;
                    }
                else
                    {
                    // if there are buckets left
                    if( bucketValues[i-1]+1 < nBuckets )
                        {
                        // new bucket!!!!
                        bucketValues[i] = bucketValues[i-1] - 1;
                        // with only one individual
                        nInd = 1;
                        }
                    else // no more buckets left, just stick everything in the last bucket
                        {
                        bucketValues[i] = bucketValues[i-1];
                        nInd++;
                        }
                    }
                }
            }
        }

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        // pick size random individuals, then pick the best.
        ArrayList<Individual> oldinds = (state.population.subpops.get(subpopulation).individuals);
        int i = state.random[thread].nextInt(oldinds.size());
        long si = 0;

        for (int x=1;x<size;x++)
            {
            int j = state.random[thread].nextInt(oldinds.size());
            if (pickWorst)
                {
                if( bucketValues[j]>bucketValues[i] ) { i = j; si = 0; }
                else if( bucketValues[i]>bucketValues[j] ) { } // do nothing
                else
                    {
                    if (si==0)
                        si = oldinds.get(i).size();
                    long sj = oldinds.get(j).size();

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
                        si = oldinds.get(i).size();
                    long sj = oldinds.get(j).size();

                    if (sj < si) // sj's got better lookin' trees
                        { i = j; si = sj; }
                    }
                }
            }
        return i;
        }

    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual)
        { return; }
    
    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        { return; }
    
    }
