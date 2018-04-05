/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;
import ec.select.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * RepeatPipeline.java
 * 
 * Created: Wed Jun  7 15:14:17 CEST 2017
 * By: Sean Luke
 */

/**
 * RepeatPipeline is a BreedingPipeline which, after prepareToProduce() is called,
 * produces a single individual from its single source, then repeatedly clones that
 * child to fulfill requests to produce().

 <p><b>Number of Sources</b><br>
 1

 <p><b>Default Base</b><br>
 breed.repeat

 * @author Sean Luke
 * @version 1.0 
 */

public class RepeatPipeline extends BreedingPipeline
    {
    public static final String P_REPEAT = "repeat";
    public static final int NUM_SOURCES = 1;
    
    public Individual individual = null;
    public IntBag parents;

    public Parameter defaultBase() { return BreedDefaults.base().push(P_REPEAT); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
                
        if (likelihood != 1.0)
            state.output.warning("RepeatPipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                base.push(P_LIKELIHOOD),
                def.push(P_LIKELIHOOD));
        }

    public void prepareToProduce(final EvolutionState state, final int subpopulation, final int thread)
        {
        super.prepareToProduce(state, subpopulation, thread);
        individual = null;
        }

    public int produce(
        final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        
        // First things first: build our individual and his parents array
        if (individual == null)
            {
            HashMap<String, Object> misc1 = null;
            if (misc != null && misc.get(SelectionMethod.KEY_PARENTS) != null)
                {
                // the user is providing a parents array.  We'll need to make our own.
                IntBag[] parentsArray = new IntBag[1];
                misc1 = new HashMap();
                misc1.put(SelectionMethod.KEY_PARENTS, parentsArray);
                }
            ArrayList<Individual> temp = new ArrayList<Individual>();
            sources[0].produce(1, 1, subpopulation, temp, state, thread, misc1);
            individual = temp.get(0);

            // Now we extract from misc1 if we have to
            if (misc1 != null && misc1.get(SelectionMethod.KEY_PARENTS) != null) // we already know this second fact unless it was somehow removed
                {
                parents = ((IntBag[])misc.get(SelectionMethod.KEY_PARENTS))[0];
                }
            else parents = null;
            }

        int start = inds.size();
        
        // Now we can copy the individual in
        for(int i = 0; i < min; i++)
            {
            inds.add((Individual)(individual.clone()));
            }
        
        // add in the parents if we need to
        if (parents != null && misc != null && misc.get(SelectionMethod.KEY_PARENTS) != null)
            {
            IntBag[] parentsArray = ((IntBag[])misc.get(SelectionMethod.KEY_PARENTS));
            for(int i = 0 ; i < min; i++)
                {
                parentsArray[start + i] = new IntBag(parents);
                }
            }
                
        return min;
        }
    }
