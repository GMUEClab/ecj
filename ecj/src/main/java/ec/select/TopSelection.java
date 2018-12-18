/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.util.*;

import java.util.ArrayList;

import ec.*;
/* 
 * TopSelection.java
 * 
 * Created: Thu Jun  8 14:27:40 CEST 2017
 * By: Sean Luke
 */

/**
 * Returns the single fittest individual in the population, breaking ties randomly.
 *
 * <p>The individual can be <i>cached</i> so it is not recomputed every single time;
 * the cache is cleared after <tt>prepareToProduce(...)</tt> is called.  Note that this
 * means that if there are multiple individuals with the top fitness, and we're caching,
 * only one of them will be returned throughout the series of multiple produce(...) calls.
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>cache</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we cache the individual?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.top

 * @author Sean Luke
 * @version 1.0 
 */

public class TopSelection extends SelectionMethod 
    {
    /** Default base */
    public static final String P_TOP = "top";
    public static final String P_CACHE = "cache";
        
    boolean cache;
    int best;
    
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_TOP);
        }

    // don't need clone etc. 

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        cache = state.parameters.getBoolean(base.push(P_CACHE),def.push(P_CACHE), false);
        }

    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        super.prepareToProduce(s, subpopulation, thread);
        
        if (cache) 
            best = -1;
        }
        
    public void cacheBest(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        ArrayList<Individual> oldinds = state.population.subpops.get(subpopulation).individuals;
        int len = oldinds.size();

        int b = 0;                                                      // this is the INDEX of the best known individual
        Individual bi = oldinds.get(b);         // this is the best known individual            
        int ties = 1;
                                
        for (int i = 1; i < len; i++)
            {
            Individual ni = oldinds.get(i);
                        
            // if he's better, definitely adopt him and reset the ties
            if (ni.fitness.betterThan(bi.fitness))
                { 
                bi = ni; 
                b = i; 
                ties = 1;
                }
            // if he's the same, adopt him with 1/n probability
            else if (ni.fitness.equivalentTo(bi.fitness))
                {
                ties++;
                if (state.random[thread].nextBoolean(1.0 / ties))
                    {
                    bi = ni;
                    b = i;
                    }
                }
            }
        best = b;
        }

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        if (cache && best >= 0)
            {
            // do nothing, it's cached
            }
        else
            {
            cacheBest(subpopulation, state, thread);
            }
        return best;
        }
    }
