/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.util.*;
import ec.*;
/* 
 * BestSelection.java
 * 
 * Created: Thu Feb 10 18:52:09 2000
 * By: Sean Luke
 */

/**
 * Performs a tournament selection restricted to only the best, or worst, <i>n</i>
 * indivdiuals in the population.  If the best individuals, then tournament selection
 * will prefer the better among them; if the worst individuals, then tournament selection
 * will prefer the worse among them.  The procedure for performing restriction is expensive to
 * set up and bring down, so it's not appropriate for steady-state evolution.  Like
 * TournamentSelection, the size of the tournament can be any 
 * If you're not familiar with the relative advantages of 
 * selection methods and just want a good one,
 * use TournamentSelection instead.   Not appropriate for
 * multiobjective fitnesses.
 *
 * <p>The tournament <i>size</i> can be any floating point value >= 1.0.  If it is a non-
 * integer value <i>x</i> then either a tournament of size ceil(x) is used
 * (with probability x - floor(x)), else a tournament of size floor(x) is used.
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick from among the <i>worst n</i> individuals in the tournament instead of the <i>best n</i>?)</td></tr>
 <tr><td valign=top><i>base.</i><tt>size</tt><br>
 <font size=-1>double &gt;= 1</font></td>
 <td valign=top>(the tournament size)</td></tr>
 <tr><td valign=top><i>base.</i><tt>n</tt><br>
 <font size=-1> int > 0 </font></td>
 <td valign=top>(the number of best-individuals to select from)</td></tr>
 <tr><td valign=top><i>base.</i><tt>n-fraction</tt><br>
 <font size=-1> 0.0 <= double < 1.0 (default is 1)</font></td>
 <td valign=top>(the number of best-individuals to select from, as a fraction of the total population)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.best

 * @author Sean Luke
 * @version 1.0 
 */

public class BestSelection extends SelectionMethod 
    {
    /** Default base */
    public static final String P_BEST = "best";
    
    public static final String P_N = "n";
    public static final String P_N_FRACTION = "n-fraction";
    public static final String P_PICKWORST = "pick-worst";
    public static final String P_SIZE = "size";

    /** Base size of the tournament; this may change.  */
    public int size;

    /** Probablity of picking the size plus one. */
    public double probabilityOfPickingSizePlusOne;
    
    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;
    
    /** Sorted, normalized, totalized fitnesses for the population */
    public int[] sortedPop;

    public static final int NOT_SET = -1;
    public int bestn = NOT_SET;
    public double bestnFrac = NOT_SET;

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_BEST);
        }

    // don't need clone etc. 

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        if ( state.parameters.exists(base.push(P_N),def.push(P_N)) )
            {
            bestn =
                state.parameters.getInt(base.push(P_N),def.push(P_N),1);
            if (bestn == 0 )
                state.output.fatal("n must be an integer greater than 0", base.push(P_N),def.push(P_N));
            }
        else if (state.parameters.exists(base.push(P_N_FRACTION),def.push(P_N_FRACTION)) )
            {
            if (state.parameters.exists(base.push(P_N),def.push(P_N)) )
                state.output.fatal("Both n and n-fraction specified for BestSelection.", base.push(P_N),def.push(P_N));
            bestnFrac =
                state.parameters.getDoubleWithMax(base.push(P_N_FRACTION),def.push(P_N_FRACTION),0.0,1.0);
            if (bestnFrac <= 0.0)
                state.output.fatal("n-fraction must be a double floating-point value greater than 0.0 and <= 1.0", base.push(P_N_FRACTION),def.push(P_N_FRACTION));
            }
        else state.output.fatal("Either n or n-fraction must be defined for BestSelection.", base.push(P_N),def.push(P_N));
                
        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);

        double val = state.parameters.getDouble(base.push(P_SIZE),def.push(P_SIZE),1.0);
        if (val < 1.0)
            state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));
        else if (val == (int) val)  // easy, it's just an integer
            {
            size = (int) val;
            probabilityOfPickingSizePlusOne = 0.0;
            }
        else
            {
            size = (int) Math.floor(val);
            probabilityOfPickingSizePlusOne = val - size;  // for example, if we have 5.4, then the probability of picking *6* is 0.4
            }
        }

    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        // load sortedPop integers
        final Individual[] i = s.population.subpops[subpopulation].individuals;

        sortedPop = new int[i.length];
        for(int x=0;x<sortedPop.length;x++) sortedPop[x] = x;

        // sort sortedPop in increasing fitness order
        QuickSort.qsort(sortedPop, 
            new SortComparatorL()
                {
                public boolean lt(long a, long b)
                    {
                    return ((Individual)(i[(int)b])).fitness.betterThan(
                        ((Individual)(i[(int)a])).fitness);
                    }

                public boolean gt(long a, long b)
                    {
                    return ((Individual)(i[(int)a])).fitness.betterThan(
                        ((Individual)(i[(int)b])).fitness);
                    }
                });

        if (!pickWorst)  // gotta reverse it
            for(int x = 0; x < sortedPop.length / 2; x++)
                {
                int p = sortedPop[x];
                sortedPop[x] = sortedPop[sortedPop.length - x - 1];
                sortedPop[sortedPop.length - x - 1] = p;
                }
                        
        // figure out bestn
        if (bestnFrac != NOT_SET)
            {
            bestn = (int) Math.max(Math.floor(s.population.subpops[subpopulation].individuals.length * bestnFrac), 1);
            }
        }


    /** Returns a tournament size to use, at random, based on base size and probability of picking the size plus one. */
    int getTournamentSizeToUse(MersenneTwisterFast random)
        {
        double p = probabilityOfPickingSizePlusOne;   // pulls us to under 35 bytes
        if (p == 0.0) return size;
        return size + (random.nextBoolean(p) ? 1 : 0);
        }

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        // pick size random individuals, then pick the best.
        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int best = state.random[thread].nextInt(bestn);  // only among the first N
        
        int s = getTournamentSizeToUse(state.random[thread]);
                
        if (pickWorst)
            for (int x=1;x<s;x++)
                {
                int j = state.random[thread].nextInt(bestn);  // only among the first N
                if (!(oldinds[sortedPop[j]].fitness.betterThan(oldinds[sortedPop[best]].fitness)))  // j isn't better than best
                    best = j;
                }
        else
            for (int x=1;x<s;x++)
                {
                int j = state.random[thread].nextInt(bestn);  // only among the first N
                if (oldinds[sortedPop[j]].fitness.betterThan(oldinds[sortedPop[best]].fitness))  // j is better than best
                    best = j;
                }
        
        return sortedPop[best];
        }
    
    public void finishProducing(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        // release the distributions so we can quickly 
        // garbage-collect them if necessary
        sortedPop = null;
        }    
    }
