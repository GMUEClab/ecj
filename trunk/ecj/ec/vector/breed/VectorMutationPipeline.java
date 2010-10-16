/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.*;

/* 
 * VectorMutationPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 VectorMutationPipeline is a BreedingPipeline which implements a simple default Mutation
 for VectorIndividuals.  Normally it takes an individual and returns a mutated 
 child individual. VectorMutationPipeline works by calling defaultMutate(...) on the 
 parent individual.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 (however many its source produces)

 <p><b>Number of Sources</b><br>
 1

 <p><b>Default Base</b><br>
 vector.mutate (not that it matters)

 * @author Sean Luke
 * @version 1.0
 */

public class VectorMutationPipeline extends BreedingPipeline
    {
    public static final String P_MUTATION = "mutate";
    public static final int NUM_SOURCES = 1;

    public Parameter defaultBase() { return VectorDefaults.base().push(P_MUTATION); }
    
    /** Returns 1 */
    public int numSources() { return NUM_SOURCES; }

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

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // mutate 'em
        for(int q=start;q<n+start;q++)
            {
            ((VectorIndividual)inds[q]).defaultMutate(state,thread);
            ((VectorIndividual)inds[q]).evaluated=false;
            }

        return n;
        }

    }
    
    
