/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.*;

/* 
 * VectorCrossoverPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 VectorCrossoverPipeline is a BreedingPipeline which implements a simple default crossover
 for VectorIndividuals.  Normally it takes two individuals and returns two crossed-over 
 child individuals.  Optionally, it can take two individuals, cross them over, but throw
 away the second child (a one-child crossover).  VectorCrossoverPipeline works by calling
 defaultCrossover(...) on the first parent individual.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 is set, in which case it's simply the minimum typical number.

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 vector.xover

 * @author Sean Luke
 * @version 1.0
 */

public class VectorCrossoverPipeline extends BreedingPipeline
    {
    public static final String P_TOSS = "toss";
    public static final String P_CROSSOVER = "xover";
    public static final int NUM_SOURCES = 2;

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;

    /** Temporary holding place for parents */
    VectorIndividual parents[];

    public VectorCrossoverPipeline() { parents = new VectorIndividual[2]; }
    public Parameter defaultBase() { return VectorDefaults.base().push(P_CROSSOVER); }

    /** Returns 2 */
    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        VectorCrossoverPipeline c = (VectorCrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.parents = (VectorIndividual[]) parents.clone();

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
            def.push(P_TOSS),false);
        }
        
    /** Returns 2 * minimum number of typical individuals produced by any sources, else
        1* minimum number if tossSecondParent is true. */
    public int typicalIndsProduced()
        {
        return (tossSecondParent? minChildProduction(): minChildProduction()*2);
        }

    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
                
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                {
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    { 
                    parents[0] = (VectorIndividual)(parents[0].clone());
                    parents[1] = (VectorIndividual)(parents[1].clone());
                    }
                }
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    parents[0] = (VectorIndividual)(parents[0].clone());
                if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
                    parents[1] = (VectorIndividual)(parents[1].clone());
                }
                
            // at this point, parents[] contains our two selected individuals,
            // AND they're copied so we own them and can make whatever modifications
            // we like on them.
    
            // so we'll cross them over now.  Since this is the default pipeline,
            // we'll just do it by calling defaultCrossover on the first child
            
            parents[0].defaultCrossover(state,thread,parents[1]);
            parents[0].evaluated=false;
            parents[1].evaluated=false;
            
            // add 'em to the population
            inds[q] = parents[0];
            q++;
            if (q<n+start && !tossSecondParent)
                {
                inds[q] = parents[1];
                q++;
                }
            }
        return n;
        }
    }
    
    
    
    
    
    
    
