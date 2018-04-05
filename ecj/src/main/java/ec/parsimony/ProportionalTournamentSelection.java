/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.parsimony;

import ec.select.*;
import ec.*;
import ec.util.*;
import ec.steadystate.*;

/* 
 * ProportionalTournamentSelection.java
 * 
 * Created: Fri Feb 01 2002
 * By: Liviu Panait
 */

/**
 * This selection method adds parsimony pressure to the regular tournament selection.  The comparison of
 * individuals is based on fitness with probability <i>prob</i>, and it is based on size with probability
 * <i>1-prob</i>.  For each pairwise comparsion of individuals, the ProportionalTournamentSelection randomly decides
 * whether to compare based on fitness or size. 

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

 <tr><td valign=top><i>base.</i><tt>fitness-prob</tt><br>
 <font size=-1> double &gt;= 0 and &lt;= 1</font></td>
 <td valign=top>(the probability of comparing individuals based on fitness, rather than size)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 select.proportional-tournament

 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class ProportionalTournamentSelection extends TournamentSelection
    {
    /** default base */
    public static final String P_PROPORTIONAL_TOURNAMENT = "proportional-tournament";

    /** The parameter for the probability of having the tournament based on fitness */
    public static final String P_PROBABILITY = "fitness-prob";

    /** The probability of having the tournament based on fitness */
    public double fitnessPressureProb;

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_PROPORTIONAL_TOURNAMENT);
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        fitnessPressureProb = state.parameters.getDouble(base.push(P_PROBABILITY),def.push(P_PROBABILITY),0.0);
        if( fitnessPressureProb<0.0 || fitnessPressureProb>1.0 )
            state.output.fatal( "Probability must be between 0.0 and 1.0",
                base.push(P_PROBABILITY),def.push(P_PROBABILITY));
        }

    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread)
        {
        if (state.random[thread].nextBoolean(fitnessPressureProb))
            return first.fitness.betterThan(second.fitness);
        else
            return first.size() < second.size();
        }
    }
