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

public class ProportionalTournamentSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_TOURNAMENT = "proportional-tournament";

    public static final String P_PICKWORST = "pick-worst";

    /** size parameter */
    public static final String P_SIZE = "size";

    /** Default size */
    public static final int DEFAULT_SIZE = 7;

    /** The parameter for the probability of having the tournament based on fitness */
    public static final String P_PROBABILITY = "fitness-prob";

    /** The probability of having the tournament based on fitness */
    public double fitnessPressureProb;

    /** Size of the tournament*/
    public int size;

    /** What's our probability of selection? If 1.0, we always pick the "good" individual. */
    public double probabilityOfSelection;
    
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

        double val = state.parameters.getDouble(base.push(P_SIZE),def.push(P_SIZE),1.0);
        if (val < 1.0)
            state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));
        else if (val > 1 && val < 2) // pick with probability
            {
            size = 2;
            probabilityOfSelection = (val/2);
            }
        else if (val != (int)val)  // it's not an integer
            state.output.fatal("If >= 2, Tournament size must be an integer.", base.push(P_SIZE), def.push(P_SIZE));
        else
            {
            size = (int)val;
            probabilityOfSelection = 1.0;
            }

        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);

        fitnessPressureProb = state.parameters.getDouble(base.push(P_PROBABILITY),def.push(P_PROBABILITY),0.0);
        if( fitnessPressureProb<0.0 || fitnessPressureProb>1.0 )
            state.output.fatal( "Probability must be between 0.0 and 1.0",
                                base.push(P_PROBABILITY),def.push(P_PROBABILITY));
        }


    // returns true if x's fitness is better than y's and kind is true,
    //   or if x's size is smaller than y's and kind is false
    private boolean betterThan( final Individual x,
                                final Individual y,
                                final boolean pressureKind )
        {
        return ( ( pressureKind && x.fitness.betterThan(y.fitness) ) ||
                 ( (!pressureKind) && ( x.size() < y.size() ) ) );
        }

    // I hard-code both produce(...) methods for efficiency's sake

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        // pick size random individuals, then pick the best.
        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int i = state.random[thread].nextInt(oldinds.length) ;
        int bad = i;

        // pick pressure on fitness or on size
        boolean pressureKind = state.random[thread].nextBoolean( fitnessPressureProb );

        for (int x=1;x<size;x++)
            {
            int j = state.random[thread].nextInt(oldinds.length);
            if (pickWorst)
                { if (!(betterThan(oldinds[j],oldinds[i],pressureKind))) { bad = i; i = j; } else bad = j; }
            else
                { if (betterThan(oldinds[j],oldinds[i],pressureKind)) { bad = i; i = j;} else bad = j; }
            }
            
        if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
            i = bad;
        return i;
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
            int i = state.random[thread].nextInt(oldinds.length);
            int bad = i;
            
            // pick pressure on fitness or on size
            boolean pressureKind = state.random[thread].nextBoolean( fitnessPressureProb );

            for (int x=1;x<size;x++)
                {
                int j = state.random[thread].nextInt(oldinds.length);
                if (pickWorst)
                    { if (!(betterThan(oldinds[j],oldinds[i],pressureKind)))  { bad = i; i = j; } else bad = j; }
                else
                    { if (betterThan(oldinds[j],oldinds[i],pressureKind))  { bad = i; i = j; } else bad = j; }
                }
            if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
                i = bad;
            inds[start+q] = oldinds[i];  // note it's a pointer transfer, not a copy!
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
