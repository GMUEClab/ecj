/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule.breed;

import ec.rule.*;
import ec.*;
import ec.util.*;

/* 
 * RuleMutationPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 RuleMutationPipeline is a BreedingPipeline which implements a simple default Mutation
 for RuleIndividuals.  Normally it takes an individual and returns a mutated 
 child individual. RuleMutationPipeline works by calling mutateRules(...) on each RuleSet in the 
 parent individual.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 1

 <p><b>Number of Sources</b><br>
 1

 <p><b>Default Base</b><br>
 rule.mutate (not that it matters)

 * @author Sean Luke
 * @version 1.0
 */

public class RuleMutationPipeline extends BreedingPipeline
    {
    public static final String P_MUTATION = "mutate";
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;

    public Parameter defaultBase() { return RuleDefaults.base().push(P_MUTATION); }
    
    /** Returns 1 */
    public int numSources() { return NUM_SOURCES; }

    /** Returns 1 */
    // DO I need to change this?
    public int typicalIndsProduced() { return (INDS_PRODUCED); }

    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 
        {
        // grab n individuals from our source and stick 'em right into inds.
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

            ((RuleIndividual)inds[q]).preprocessIndividual(state,thread);

            /*
              int len = ((RuleIndividual)inds[q]).rulesets.length;
              for( int x = 0 ; x < len ; x++ )
              {
              ((RuleIndividual)inds[q]).rulesets[x].mutateRules( state, thread );
              }
            */
            ((RuleIndividual)inds[q]).mutate(state, thread);
            ((RuleIndividual)inds[q]).postprocessIndividual(state,thread);

            ((RuleIndividual)inds[q]).evaluated=false;
            }

        return n;
        }

    }
    
    
