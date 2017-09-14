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
 * ReproductionPipeline.java
 * 
 * Created: Thu Nov  8 13:39:32 EST 2001
 * By: Sean Luke
 */

/**
 * ReproductionPipeline is a BreedingPipeline which simply makes a copy
 * of the individuals it recieves from its source.  
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the child produces

 <p><b>Number of Sources</b><br>
 1

 <p><b>Default Base</b><br>
 breed.reproduce

 * @author Sean Luke
 * @version 1.0 
 */

public class ReproductionPipeline extends BreedingPipeline
    {
    public static final String P_REPRODUCE = "reproduce";
    public static final int NUM_SOURCES = 1;
    
    public Parameter defaultBase() { return BreedDefaults.base().push(P_REPRODUCE); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
                
        if (likelihood != 1.0)
            state.output.warning("ReproductionPipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                base.push(P_LIKELIHOOD),
                def.push(P_LIKELIHOOD));
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

        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);
        return n;
        }
    }
