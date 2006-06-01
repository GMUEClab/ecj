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
    /** Sets all subpopulations in pop to the expected mu+lambda size.  Does not fill new slots with individuals. */
    public Population setToMuPlusLambda(Population pop, EvolutionState state)
        {
        for(int x=0;x<pop.subpops.length;x++)
            {
            int s = mu[x]+lambda[x];
            
            // check to see if the array's big enough
            if (pop.subpops[x].individuals.length != s)
                // need to increase
                {
                Individual[] newinds = new Individual[s];
                System.arraycopy(pop.subpops[x].individuals,0,newinds,0,
                                 s < pop.subpops[x].individuals.length ? 
                                 s : pop.subpops[x].individuals.length);
                pop.subpops[x].individuals = newinds;
                }
            }
        return pop;
        }

    public Population postProcess(Population newpop, Population oldpop, EvolutionState state)
        {
        // first we need to expand newpop to mu+lambda in size
        newpop = setToMuPlusLambda(newpop,state);
        
        // now we need to dump the old population into the high end of the new population
         
        for(int x=0;x<newpop.subpops.length;x++)
            {
            for(int y=0;y<mu[x];y++)
                {
                newpop.subpops[x].individuals[y+lambda[x]] =
                    (Individual)(oldpop.subpops[x].individuals[y].clone());
                }
            }
        return newpop;
        }
    }
