/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.es;

import ec.*;

/* 
 * MuPlusLambdaBreeder.java
 * 
 * Created: Thu Sep  7 18:49:42 2000
 * By: Sean Luke
 */

/**
 * MuPlusLambdaBreeder is a subclass of MuCommaLambdaBreeder which, together with
 * ESSelection, implements the (mu + lambda) breeding strategy and gathers
 * the comparison data you can use to implement a 1/5-rule mutation mechanism.
 * Note that MuPlusLambdaBreeder increases subpopulation sizes by their mu
 * values in the second generation and keep them at that size thereafter.
 * See MuCommaLambdaBreeder for information about how to set mu and lambda.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class MuPlusLambdaBreeder extends MuCommaLambdaBreeder
    {
    private static final long serialVersionUID = 1;
    
    public int maximumMuLambdaDivisor() { return 1; }

    public Population postProcess(Population newpop, Population oldpop, EvolutionState state)
        {
        // now we need to dump the old population into the high end of the new population
        for(int x = 0; x< newpop.subpops.size(); x++)
            {
            for(int y=0;y<mu[x];y++)
                {
                newpop.subpops.get(x).individuals.add((Individual)(oldpop.subpops.get(x).individuals.get(y).clone()));
                }
            }
        return newpop;
        }
    }
