/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.gp.ge.breed;
import ec.gp.ge.*;
import ec.*;
import ec.util.*;

/*
 * GEProblem.java
 *
 * Created: Sat Oct 16 23:21:01 EDT 2010
 * By: Sean Luke, Joseph Zelibor III, and Eric Kangas
 */


/**
 * <p>GETruncationPipeline removes the unused genes from the end of the vector.
 * The number of used chromosomes are tracked by GESpecies' <b>comsumed(...)</b> function.
 *
 * Note: truncaton only occurs if the number of comsumed genes is greater than 1.</p>
 *
 * <p><b>Default Base</b><br>
 * ge.truncation
 *
 * @author Sean Luke, Joseph Zelibor III, and Eric Kangas
 * @version 1.0
 */

public class GETruncationPipeline extends BreedingPipeline
    {
    public static final String P_TRUNCATION = "truncation";
    public static final int NUM_SOURCES = 1;

    public int numSources()
        {
        return NUM_SOURCES;
        }

    public Parameter defaultBase()
        {
        return GEDefaults.base().push(P_TRUNCATION);
        }

    public int produce(final int min,
        final int max,
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did



        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            if (sources[0] instanceof SelectionMethod)
                inds[q] = (Individual)(inds[q].clone());

            GEIndividual ind = (GEIndividual)(inds[q]);
            GESpecies species = (GESpecies) (ind.species);

            int consumed = species.consumed(state, ind, thread);
            if (consumed > 1)
                {
                Object[] pieces = new Object[2];
                //System.err.println(consumed);
                ind.split(new int[] { consumed }, pieces);
                ind.join(new Object[] {pieces[0]});
                }
            }
        return n;
        }

    }
