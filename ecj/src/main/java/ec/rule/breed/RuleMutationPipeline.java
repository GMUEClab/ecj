/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule.breed;

import ec.rule.*;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.HashMap;

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
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
        
        // grab n individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }

        // mutate 'em
        for(int q=start;q<n+start;q++)
            {

            ((RuleIndividual)inds.get(q)).preprocessIndividual(state,thread);

            /*
              int len = ((RuleIndividual)inds[q]).rulesets.length;
              for( int x = 0 ; x < len ; x++ )
              {
              ((RuleIndividual)inds[q]).rulesets[x].mutateRules( state, thread );
              }
            */
            ((RuleIndividual)inds.get(q)).mutate(state, thread);
            ((RuleIndividual)inds.get(q)).postprocessIndividual(state,thread);

            ((RuleIndividual)inds.get(q)).evaluated=false;
            }

        return n;
        }

    }
    
    
