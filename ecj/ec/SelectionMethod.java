/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

import ec.util.IntBag;
import java.util.ArrayList;

/* 
 * SelectionMethod.java
 * 
 * Created: Mon Aug 30 19:19:56 1999
 * By: Sean Luke
 */

import java.util.HashMap;

/**
 * A SelectionMethod is a BreedingSource which provides direct IMMUTABLE pointers
 * to original individuals in an old population, not fresh mutable copies.
 * If you use a SelectionMethod as your BreedingSource, you must 
 * SelectionMethods might include Tournament Selection, Fitness Proportional Selection, etc.
 * SelectionMethods don't have parent sources.
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 * @author Sean Luke
 * @version 1.0 
 */

public abstract class SelectionMethod extends BreedingSource
    {
    public static final int INDS_PRODUCED = 1;
    public static final String KEY_PARENTS = "parents";

    /** Returns 1 (the typical default value) */
    public int typicalIndsProduced() { return INDS_PRODUCED; }

    /** A default version of produces -- this method always returns
        true under the assumption that the selection method works
        with all Fitnesses.  If this isn't the case, you should override
        this to return your own assessment. */
    public boolean produces(final EvolutionState state,
        final Population newpop,
        final int subpopulation,
        final int thread)
        {
        return true;
        }


    /** A default version of prepareToProduce which does nothing.  */
    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        { return; }

    /** A default version of finishProducing, which does nothing. */
    public void finishProducing(final EvolutionState s,
        final int subpopulation,
        final int thread)
        { return; }


    public final int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();

        int n = produceWithoutCloning(min, max, subpopulation, inds, state, thread, misc);
        
        // clone every produced individual
        for(int q=start; q < n+start; q++)
            {
//System.err.println("" + this + " makes " + inds.get(q));
            inds.set(q, (Individual)(inds.get(q).clone()));
            }

        return n;
        }
        
    public int produceWithoutCloning(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
        
        int n=INDS_PRODUCED;
        if (n<min) n = min;
        if (n>max) n = max;
        
        for(int q=0;q<n;q++)
            {
            int index = produce(subpopulation,state,thread);

            inds.add(state.population.subpops.get(subpopulation).individuals.get(index));
            // by Ermo. seems the misc forget to check if misc is null
            if (misc!=null && misc.get(KEY_PARENTS)!=null)
                {
                IntBag bag = new IntBag(1);
                bag.add(index);
                ((IntBag[])misc.get(KEY_PARENTS))[start+q] = bag;
                }
            }
        return n;
        }
    
    /** An alternative form of "produce" special to Selection Methods;
        selects an individual from the given subpopulation and 
        returns its position in that subpopulation. */
    public abstract int produce(final int subpopulation,
        final EvolutionState state,
        final int thread);
    }



