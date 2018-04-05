/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import java.util.ArrayList;

import ec.*;
import ec.util.*;
import ec.steadystate.*;

import java.util.HashMap;

/* 
 * FirstSelection.java
 * 
 * Created: Mon Aug 30 19:27:15 1999
 * By: Sean Luke
 */

/**
 * Always picks the first individual in the subpopulation.  This is mostly
 * for testing purposes.
 *

 <p><b>Default Base</b><br>
 select.first

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class FirstSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_FIRST = "first";

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_FIRST);
        }
    
    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        return 0;
        }


    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int min,
        final int max,
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int n = 1;
        if (n>max) n = max;
        if (n<min) n = min;

        for(int q = 0; q < n; q++)
            {
            // pick size random individuals, then pick the best.
            ArrayList<Individual> oldinds = state.population.subpops.get(subpopulation).individuals;
            inds[start+q] = oldinds.get(0);  // note it's a pointer transfer, not a copy!
            if(misc!=null&&misc.get(KEY_PARENTS)!=null)
                {
                IntBag parent = new IntBag(1);
                parent.add(0);
                ((IntBag[])misc.get(KEY_PARENTS))[start+q] = parent;
                }
            
            }
        return n;
        }

    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual)
        { return; }
    
    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        { return; }
    
    }
