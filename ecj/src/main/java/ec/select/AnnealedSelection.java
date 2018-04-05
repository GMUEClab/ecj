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
 * AnnealedSelection.java
 * 
 * Created: Thu Jun  8 14:27:40 CEST 2017
 * By: Sean Luke
 */

/**
 * Returns an individual using a form of simulated annealing.
 *
 * <p>This works as follows.  If there is only one individual in the population, he
 * is selected.  Otherwise we pick a random individual from among the individuals
 * <i>other</i> than the first individual.  If that random individual is <i>fitter</i>
 * than the first individual, it is selected.  Otherwise if that random individual is
 * <i>as fit</i> as the first individual, one of the two is selected at random.  Otherwise
 * if the random individual is <i>not as fit</i> as the first individual, it is selected
 * with a probability P = e^((fit(random ind) - fit(first ind)) / t), where t is a
 * TEMPERATURE.  Otherwise the first individual is selected.
 *
 * <p>The temperature starts at a high value >> 0, and is slowly cut down by multiplying
 * it by a CUTDOWN value every generation.  When the temperature reaches 0, then the first 
 * individual is selected every time.  
 *
 * <p>The selected individual can be <i>cached</i> so the same individual is returned
 * multiple times without being recomputed.  This cache is cleared after 
 * <tt>prepareToProduce(...)</tt> is called.  Note that this option is not appropriate
 * for Steady State Evolution, which only calls <tt>prepareToProduce(...)</tt> once.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>cache</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we cache the individual?)</td></tr>
 <tr><td valign=top><i>base.</i><tt>temperature</tt><br>
 <font size=-1> double > 0 (or undefined)</font></td>
 <td valign=top>(annealing start temperature)</td></tr>
 <tr><td valign=top><i>base.</i><tt>cutdown</tt><br>
 <font size=-1> 0.0 &lt;= double &gt;= 1.0 (default is 0.95)</font></td>
 <td valign=top>(annealing cutdown)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.annealed

 * @author Sean Luke
 * @version 1.0 
 */

public class AnnealedSelection extends SelectionMethod 
    {
    /** Default base */
    public static final String P_ANNEALED = "annealed";
    public static final String P_CACHE = "cache";
    public static final String P_TEMPERATURE = "temperature";
    public static final String P_CUTDOWN = "cutdown";
    
    public static final double V_CUTDOWN = 0.95;
    
    boolean cache;
    int best;
    double temperature;
    double t;
    double cutdown;
    
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_ANNEALED);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        cache = state.parameters.getBoolean(base.push(P_CACHE),def.push(P_CACHE), false);
        temperature = state.parameters.getDoubleWithDefault(base.push(P_TEMPERATURE),def.push(P_TEMPERATURE), 0);
        if (temperature < 0)
            {
            state.output.fatal("TopSelection temperature, if defined, must be >= 0", 
                base.push(P_TEMPERATURE),def.push(P_TEMPERATURE));
            }
        cutdown = state.parameters.getDoubleWithDefault(base.push(P_CUTDOWN),def.push(P_CUTDOWN), 0.95);
        if (cutdown < 0 || cutdown > 1)
            {
            state.output.fatal("TopSelection cutdown, if defined, must be between 0 and 1.  Default is 0.95.", 
                base.push(P_TEMPERATURE),def.push(P_TEMPERATURE));
            }
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
                
        if (len == 1)  // uh oh
            best = 0;
        else
            {
            int candidate = state.random[thread].nextInt(len - 1) + 1;
                
            Individual first = oldinds.get(0);
            Individual next = oldinds.get(candidate);
                
            if (next.fitness.betterThan(first.fitness))
                best = candidate;
                        
            else if (next.fitness.equivalentTo(first.fitness) && state.random[thread].nextBoolean())
                best = candidate;

            // he's worse           
            else if (state.random[thread].nextBoolean(Math.exp((next.fitness.fitness() - first.fitness.fitness()) / t)))
                best = candidate;
                        
            else best = 0;
            }

        // note that we do NOT do temperature = temperature * cutdown,
        // which would ordinarily make perfect sense, except that we're
        // cloning from a prototype.
        t = temperature * Math.pow(cutdown, state.generation);
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
