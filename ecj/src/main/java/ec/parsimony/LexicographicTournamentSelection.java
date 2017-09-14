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

public class LexicographicTournamentSelection extends TournamentSelection
    {
    /** default base */
    public static final String P_TOURNAMENT = "lexicographic-tournament";

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_TOURNAMENT);
        }
    
    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread)
        {
        return (first.fitness.betterThan(second.fitness) ||
            (first.fitness.equivalentTo(second.fitness) && first.size() < second.size()));
        }
                
    }
