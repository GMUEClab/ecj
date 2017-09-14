/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.gp.ge.breed;
import ec.gp.ge.*;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.HashMap;

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
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
        
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }


        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GEIndividual ind = (GEIndividual)(inds.get(q));
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
