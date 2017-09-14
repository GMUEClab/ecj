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

/**
 *
 * DoubleTournamentSelection.java
 *
 * There are 2 tournaments for each selection of an individual. In the first
 * ("qualifying") tournament, <i>size</i> individuals
 * are selected and the <i>best</i> one (based on individuals' length if <i>do-length-first</i>
 * is true, or based on individual's fitness otherwise). This process repeat <i>size2</i> times,
 * so we end up with <i>size2</i> winners on one criteria. Then, there is second "champion" tournament
 * on the other criteria (fitness if <i>do-length-first</i> is true, size otherwise) among the
 * <i>size2</i> individuals, and the best one is the one returned by this selection method.
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>size</tt><br>
 <font size=-1>int &gt;= 1 (default 7)</font></td>
 <td valign=top>(the tournament size for the initial ("qualifying") tournament)</td></tr>

 <tr><td valign=top><i>base.</i><tt>size2</tt><br>
 <font size=-1>int &gt;= 1 (default 7)</font></td>
 <td valign=top>(the tournament size for the final ("champion") tournament)</td></tr>

 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick the <i>worst</i> individual in the initial ("qualifying") tournament instead of the <i>best</i>?)</td></tr>

 <tr><td valign=top><i>base.</i><tt>pick-worst2</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick the <i>worst</i> individual in the final ("champion") tournament instead of the <i>best</i>?)</td></tr>

 <tr><td valign=top><i>base.</i><tt>do-length-first</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(should the initial ("qualifying") tournament be based on the length of the individual or (if false) the fitness of the individual?  The final ("champion") tournament will be based on the alternative option)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.double-tournament

 *
 */

/**
 *
 *
 * @author Sean Luke & Liviu Panait
 * @version 1.0 
 *
 */

public class DoubleTournamentSelection extends SelectionMethod implements SteadyStateBSourceForm
    {
    /** default base */
    public static final String P_TOURNAMENT = "double-tournament";

    public static final String P_PICKWORST = "pick-worst";
    public static final String P_PICKWORST2 = "pick-worst2";

    public static final String P_DOLENGTHFIRST = "do-length-first";
    
    /** size parameter */
    public static final String P_SIZE = "size";
    public static final String P_SIZE2 = "size2";

    /** Size of the tournament*/
    public int size;
    public int size2;

    /** What's our probability of selection? If 1.0, we always pick the "good" individual. */
    public double probabilityOfSelection;
    public double probabilityOfSelection2;

    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;
    public boolean pickWorst2;
    public boolean doLengthFirst;

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

        val = state.parameters.getDouble(base.push(P_SIZE2),def.push(P_SIZE2),1.0);
        if (val < 1.0)
            state.output.fatal("Tournament size2 must be >= 1.",base.push(P_SIZE2),def.push(P_SIZE2));
        else if (val > 1 && val < 2) // pick with probability
            {
            size2 = 2;
            probabilityOfSelection2 = (val/2);
            }
        else if (val != (int)val)  // it's not an integer
            state.output.fatal("If >= 2, Tournament size2 must be an integer.", base.push(P_SIZE2), def.push(P_SIZE2));
        else
            {
            size2 = (int)val;
            probabilityOfSelection2 = 1.0;
            }

        doLengthFirst = state.parameters.getBoolean(base.push(P_DOLENGTHFIRST),def.push(P_DOLENGTHFIRST),true);
        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);
        pickWorst2 = state.parameters.getBoolean(base.push(P_PICKWORST2),def.push(P_PICKWORST2),false);
        }

    /**
       Produces the index of a person selected from among several by a tournament.
       The tournament's criteria is fitness of individuals if doLengthFirst is true,
       otherwise the size of the individuals.
    */
    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        int[] inds = new int[size2];
        for(int x=0;x<size2;x++) inds[x] = make(subpopulation,state,thread);

        if (!doLengthFirst)
            {
            // pick size random individuals, then pick the best.
            Individual[] oldinds = state.population.subpops[subpopulation].individuals;
            int i = inds[0];
            int bad = i;
            
            for (int x=1;x<size2;x++)
                {
                int j = inds[x];
                if (pickWorst2)
                    { if (oldinds[j].size() > oldinds[i].size()) { bad = i; i = j; } else bad = j; }
                else
                    { if (oldinds[j].size() < oldinds[i].size()) { bad = i; i = j;} else bad = j; }
                }
            
            if (probabilityOfSelection2 != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection2))
                i = bad;
            return i;
            }
        else 
            {
            // pick size random individuals, then pick the best.
            Individual[] oldinds = state.population.subpops[subpopulation].individuals;
            int i = inds[0];
            int bad = i;
            
            for (int x=1;x<size2;x++)
                {
                int j = inds[x];
                if (pickWorst2)
                    { if (!(oldinds[j].fitness.betterThan(oldinds[i].fitness))) { bad = i; i = j; } else bad = j; }
                else
                    { if (oldinds[j].fitness.betterThan(oldinds[i].fitness)) { bad = i; i = j;} else bad = j; }
                }
            
            if (probabilityOfSelection2 != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection2))
                i = bad;
            return i;
            }
        }

    /**
       Produces the index of a person selected from among several by a tournament.
       The tournament's criteria is size of individuals if doLengthFirst is true,
       otherwise the fitness of the individuals.
    */
    public int make(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        if (doLengthFirst) // if length first, the first tournament is based on size
            {
            // pick size random individuals, then pick the best.
            Individual[] oldinds = state.population.subpops[subpopulation].individuals;
            int i = state.random[thread].nextInt(oldinds.length) ;
            int bad = i;
            
            for (int x=1;x<size;x++)
                {
                int j = state.random[thread].nextInt(oldinds.length);
                if (pickWorst)
                    { if (oldinds[j].size() > oldinds[i].size()) { bad = i; i = j; } else bad = j; }
                else
                    { if (oldinds[j].size() < oldinds[i].size()) { bad = i; i = j;} else bad = j; }
                }
            
            if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
                i = bad;
            return i;
            }
        else
            {
            // pick size random individuals, then pick the best.
            Individual[] oldinds = state.population.subpops[subpopulation].individuals;
            int i = state.random[thread].nextInt(oldinds.length) ;
            int bad = i;
            
            for (int x=1;x<size;x++)
                {
                int j = state.random[thread].nextInt(oldinds.length);
                if (pickWorst)
                    { if (!(oldinds[j].fitness.betterThan(oldinds[i].fitness))) { bad = i; i = j; } else bad = j; }
                else
                    { if (oldinds[j].fitness.betterThan(oldinds[i].fitness)) { bad = i; i = j;} else bad = j; }
                }
            
            if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
                i = bad;
            return i;
            }
        }


    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual)
        { return; }
    
    public void sourcesAreProperForm(final SteadyStateEvolutionState state)
        { return; }
    
    }
