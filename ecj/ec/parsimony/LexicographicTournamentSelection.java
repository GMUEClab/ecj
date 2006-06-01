/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.parsimony;

import ec.*;
import ec.util.*;
import ec.steadystate.*;
import ec.select.*;

/* 
 * LexicographicTournamentSelection.java
 * 
 * Created: Mon Aug 30 19:27:15 1999
 * By: Liviu Panait & Sean Luke
 */

/**
 * Does a simple tournament selection, limited to the subpopulation it's
 * working in at the time.
 *
 * <p>Tournament selection works like this: first, <i>size</i> individuals
 * are chosen at random from the population.  Then of those individuals,
 * the one with the best fitness is selected.  If two individuals have the
 * same fitness, the one with smaller size is prefered.
 *
 * The default tournament size is 7.
 *
 *

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the tournament size)</td></tr>

 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick the <i>worst</i> individual in the tournament instead of the <i>best</i>?)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 select.lexicographic-tournament

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class LexicographicTournamentSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_TOURNAMENT = "lexicographic-tournament";

    public static final String P_PICKWORST = "pick-worst";

    /** size parameter */
    public static final String P_SIZE = "size";

    /** Default size */
    public static final int DEFAULT_SIZE = 7;

    /** Size of the tournament*/
    public int size;

    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_TOURNAMENT);
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        size = state.parameters.getInt(base.push(P_SIZE),def.push(P_SIZE),1);
        if (size < 1)
            state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));

        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);
        }


    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        // pick size random individuals, then pick the best.
        Individual[] oldinds = (state.population.subpops[subpopulation].individuals);
        int i = state.random[thread].nextInt(oldinds.length);
        long si = 0;

        for (int x=1;x<size;x++)
            {
            int j = state.random[thread].nextInt(oldinds.length);
            if (pickWorst)
                {
                if( !oldinds[j].fitness.betterThan(oldinds[i].fitness)) { i = j; si = 0; }
                else if( !oldinds[i].fitness.betterThan(oldinds[j].fitness)) { } // do nothing
                else
                    {
                    if (si==0)
                        si = oldinds[i].size();

                    long sj = oldinds[j].size();

                    if (sj >= si) // sj's got worse lookin' trees
                        { i = j; si = sj; }
                    }
                }
            else
                { 
                if( oldinds[j].fitness.betterThan( oldinds[i].fitness ) ) { i = j; si = 0; }
                else if( oldinds[i].fitness.betterThan(oldinds[j].fitness)) { } // do nothing
                else 
                    {
                    if (si==0)
                        si = oldinds[i].size();

                    long sj = oldinds[j].size();

                    if (sj < si) // sj's got better lookin' trees
                        { i = j; si = sj; }
                    }
                }
            }
        return i;
        }

    public void individualReplaced(final SteadyStateEvolutionState state,
                                   final int subpopulation,
                                   final int thread,
                                   final int individual)
        { return; }
    
    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        { return; }
    
    }
