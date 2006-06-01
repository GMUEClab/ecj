/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.es;
import ec.*;
import ec.util.*;

/* 
 * ESSelection.java
 * 
 * Created: Thu Sep  7 19:08:19 2000
 * By: Sean Luke
 */

/**
 * ESSelection is a special SelectionMethod designed to be used with 
 * evolutionary strategies-type breeders.
 * The rule is simple: if your pipeline returns <i>N</i> children when
 * you called it, ESSelection objects must have been called exactly
 * <i>N</i> times (selecting <i>N</i> children altogether in that pass).
 * No more, no less.  You can use other selection methods (Tournament
 * Selection is a good choice) to fill the slack.
 *

 <p><b>Default Base</b><br>
 es.select

 * @author Sean Luke
 * @version 1.0 
 */

public class ESSelection extends SelectionMethod 
    {
    public static final String P_ESSELECT = "select";

    public Parameter defaultBase()
        {
        return ESDefaults.base().push(P_ESSELECT);
        }

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        if (!(state.breeder instanceof MuCommaLambdaBreeder))
            state.output.fatal("ESSelection was handed a Breeder that's not either MuCommaLambdaBreeder or MuCommaPlusLambdaBreeder.");
        MuCommaLambdaBreeder breeder = (MuCommaLambdaBreeder)(state.breeder);
        
        // determine my position in the array
        int pos = (breeder.lambda[subpopulation] % state.breedthreads == 0 ? 
                   breeder.lambda[subpopulation]/state.breedthreads :
                   breeder.lambda[subpopulation]/state.breedthreads + 1) * 
            thread + breeder.count[thread];  // note integer division
        
        // determine the parent
        int parent = pos / breeder.mu[subpopulation]; // note integer division

        // increment our count
        breeder.count[thread]++;

        return parent;
        }


    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 
        {
        if (min>1) // uh oh
            state.output.fatal("ESSelection used, but it's being asked to produce more than one individual.");
        if (!(state.breeder instanceof MuCommaLambdaBreeder))
            state.output.fatal("ESSelection was handed a Breeder that's not either MuCommaLambdaBreeder or MuCommaPlusLambdaBreeder.");
        MuCommaLambdaBreeder breeder = (MuCommaLambdaBreeder)(state.breeder);
        
        // determine my position in the array
        int pos = (breeder.lambda[subpopulation] % state.breedthreads == 0 ? 
                   breeder.lambda[subpopulation]/state.breedthreads :
                   breeder.lambda[subpopulation]/state.breedthreads + 1) * 
            thread + breeder.count[thread];  // note integer division
        
        // determine the parent
        int parent = pos / (breeder.lambda[subpopulation] / breeder.mu[subpopulation]); // note outer integer division

        // increment our count
        breeder.count[thread]++;

        // and so we return the parent
        inds[start] = state.population.subpops[subpopulation].individuals[parent];

        // and so we return the parent
        return 1;
        }
    }
