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
 * Picks among the best <i>n</i> individuals in a population in 
 * direct proportion to their absolute
 * fitnesses as returned by their fitness() methods relative to the
 * fitnesses of the other "best" individuals in that <i>n</i>.  This is expensive to
 * set up and bring down, so it's not appropriate for steady-state evolution.
 * If you're not familiar with the relative advantages of 
 * selection methods and just want a good one,
 * use TournamentSelection instead.   Not appropriate for
 * multiobjective fitnesses.
 *
 * <p><b><font color=red>
 * Note: Fitnesses must be non-negative.  0 is assumed to be the worst fitness.
 * </font></b>
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>pick-worst</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should we pick from among the <i>worst n</i> individuals in the tournament instead of the <i>best n</i>?)</td></tr>
 <tr><td valign=top><i>base.</i><tt>n</tt><br>
 <font size=-1> int > 0 (default is 1)</font></td>
 <td valign=top>(the number of best-individuals to select from)</td></tr>
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
    public static final String P_PICKWORST = "pick-worst";
    /** Sorted, normalized, totalized fitnesses for the population */
    public float[] sortedFit;
    /** Sorted population -- since I *have* to use an int-sized
        individual (short gives me only 16K), 
        I might as well just have pointers to the
        population itself.  :-( */
    public int[] sortedPop;

    /** Do we pick the worst instead of the best? */
    public boolean pickWorst;

    public int bestn;

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_BEST);
        }

    // don't need clone etc. 

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        bestn =
            state.parameters.getInt(base.push(P_N),def.push(P_N),1);
        if (bestn == 0 )
            state.output.fatal("n must be an integer greater than 0", base.push(P_N),def.push(P_N));
        
        pickWorst = state.parameters.getBoolean(base.push(P_PICKWORST),def.push(P_PICKWORST),false);
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

        // load sortedFit
        sortedFit = new float[Math.min(sortedPop.length,bestn)];
        if (pickWorst)
            for(int x=0;x<sortedFit.length;x++)
                sortedFit[x] = ((Individual)(i[sortedPop[x]])).fitness.fitness();
        else
            for(int x=0;x<sortedFit.length;x++)
                sortedFit[x] = ((Individual)(i[sortedPop[sortedPop.length-x-1]])).fitness.fitness();

        for(int x=0;x<sortedFit.length;x++)
            {
            if (sortedFit[x] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  BestSelection requires that all fitness values be non-negative(offending subpopulation #" + subpopulation + ")");
            }


        // organize the distributions.  All zeros in fitness is fine
        RandomChoice.organizeDistribution(sortedFit, true);
        }

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        // Pick and return an individual from the population
        if (pickWorst)
            return sortedPop[RandomChoice.pickFromDistribution(
                                 sortedFit,state.random[thread].nextFloat(),CHECKBOUNDARY)];
        else
            return sortedPop[sortedPop.length - RandomChoice.pickFromDistribution(
                                 sortedFit,state.random[thread].nextFloat(),CHECKBOUNDARY) - 1];            
        }
    
    public void finishProducing(final EvolutionState s,
                                final int subpopulation,
                                final int thread)
        {
        // release the distributions so we can quickly 
        // garbage-collect them if necessary
        sortedFit = null;
        sortedPop = null;
        }    
    }
