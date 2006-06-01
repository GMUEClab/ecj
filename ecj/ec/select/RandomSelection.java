/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.*;
import ec.util.*;
import ec.steadystate.*;

/* 
 * RandomSelection.java
 * 
 * Created: Tue Sep 3 2002
 * By: Liviu Panait
 */

/**
 * Picks a random individual in the subpopulation.  This is mostly
 * for testing purposes.
 *

 <p><b>Default Base</b><br>
 select.random

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class RandomSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_RANDOM = "random";

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_RANDOM);
        }

    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        return state.random[thread].nextInt( state.population.subpops[subpopulation].individuals.length );
        }

    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 
        {
        int n = 1;
        if (n>max) n = max;
        if (n<min) n = min;

        for(int q = 0; q < n; q++)
            {
            // pick size random individuals, then pick the best.
            Individual[] oldinds = state.population.subpops[subpopulation].individuals;
            inds[start+q] = oldinds[state.random[thread].nextInt( state.population.subpops[subpopulation].individuals.length )];
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
