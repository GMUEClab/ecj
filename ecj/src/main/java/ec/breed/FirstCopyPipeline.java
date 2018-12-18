/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * FirstCopyPipeline.java
 * 
 * Created: Wed Jun  7 15:14:17 CEST 2017
 * By: Sean Luke
 */

/**
 * FirstCopyPipeline is a BreedingPipeline similar to ReproductionPipeline, except
 * that after a call to prepareToProduce(...), the immediate next child produced
 * is produced from source 0, and all the remaining children in that produce()
 * call and in subsequent produce() calls are produced from source 1.  This allows
 * a simple approach to doing a one-child elitism by loading the elitist child from
 * source 0 and the rest from source 1.  See ec/app/ecsuite/sa.params for an example.
 
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the child produces

 <p><b>Number of Sources</b><br>
 2

 <p><b>Default Base</b><br>
 breed.reproduce

 * @author Sean Luke
 * @version 1.0 
 */

public class FirstCopyPipeline extends BreedingPipeline
    {
    public static final String P_FIRST_COPY = "first-copy";
    public static final int NUM_SOURCES = 2;
    
    public Parameter defaultBase() { return BreedDefaults.base().push(P_FIRST_COPY); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
                
        if (likelihood != 1.0)
            state.output.warning("FirstCopyPipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                base.push(P_LIKELIHOOD),
                def.push(P_LIKELIHOOD));
        }
        
    public boolean firstTime = true;
    
    public void prepareToProduce(final EvolutionState state,
        final int subpopulation,
        final int thread)
        {
        super.prepareToProduce(state, subpopulation, thread);
        
        // reset
        firstTime = true;
        }

    public int produce(
        final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();

        if (firstTime)
            {
            // Load our very first child from source 0
            int n = sources[0].produce(1, 1, subpopulation, inds, state, thread, misc);
                
            // Were we asked to make more kids than this?  If so, make the rest from source 1
            if (min > 1)
                {
                n += sources[1].produce(min - 1, max - 1, subpopulation, inds, state, thread, misc);
                }
                        
            firstTime = false;
            return n;
            }
        else
            {
            // take all kids from source 1
            int n = sources[1].produce(min ,max, subpopulation, inds, state, thread, misc);
            return n;
            }
        }
    }
