/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.HashMap;

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
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
        
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);

        // should we use them straight?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }

        // else mutate 'em
        for(int q=start;q<n+start;q++)
            {
            ((VectorIndividual)inds.get(q)).defaultMutate(state,thread);
            ((VectorIndividual)inds.get(q)).evaluated=false;
            }

        return n;
        }

    }
    
    
