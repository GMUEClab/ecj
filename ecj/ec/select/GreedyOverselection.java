/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.*;
import ec.util.*;

/* 
 * GreedyOverselection.java
 * 
 * Created: Thu Feb 10 17:39:03 2000
 * By: Sean Luke
 */

/**
 * GreedyOverselection is a SelectionMethod which implements Koza-style
 * fitness-proportionate greedy overselection.  Not appropriate for
 * multiobjective fitnesses.
 *
 * <p> This selection method first 
 * divides individuals in a population into two groups: the "good" 
 * ("top") group, and the "bad" ("bottom") group.  The best <i>top</i>
 * percent of individuals in the population go into the good group.
 * The rest go into the "bad" group.  With a certain probability (determined
 * by the <i>gets</i> setting), an individual will be picked out of the
 * "good" group.  Once we have determined which group the individual
 * will be selected from, the individual is picked using fitness proportionate
 * selection in that group, that is, the likelihood he is picked is 
 * proportionate to his fitness relative to the fitnesses of others in his
 * group.
 *
 * <p> All this is expensive to
 * set up and bring down, so it's not appropriate for steady-state evolution.
 * If you're not familiar with the relative advantages of 
 * selection methods and just want a good one,
 * use TournamentSelection instead. 
 *
 * <p><b><font color=red>
 * Note: Fitnesses must be non-negative.  0 is assumed to be the worst fitness.
 * </font></b>

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>top</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0</font></td>
 <td valign=top>(the percentage of the population going into the "good" (top) group)</td></tr>
 <tr><td valign=top><i>base.</i><tt>gets</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0</font></td>
 <td valign=top>(the likelihood that an individual will be picked from the "good" group)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.greedy
 * @author Sean Luke
 * @version 1.0 
 */

public class GreedyOverselection extends SelectionMethod
    {
    public float[] sortedFitOver;
    public float[] sortedFitUnder;
    /** Sorted population -- since I *have* to use an int-sized
        individual (short gives me only 16K), 
        I might as well just have pointers to the
        population itself.  :-( */
    public int[] sortedPop;

    public static final String P_GREEDY = "greedy";
    public static final String P_TOP = "top";
    public static final String P_GETS = "gets";

    public float top_n_percent;
    public float gets_n_percent;

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_GREEDY);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        top_n_percent =
            state.parameters.getFloat(base.push(P_TOP),def.push(P_TOP),0.0,1.0);
        if (top_n_percent < 0.0)
            state.output.fatal("Top-n-percent must be between 0.0 and 1.0", base.push(P_TOP),def.push(P_TOP));
        
        gets_n_percent =
            state.parameters.getFloat(base.push(P_GETS),def.push(P_GETS),0.0,1.0);
        if (gets_n_percent < 0.0)
            state.output.fatal("Gets-n-percent must be between 0.0 and 1.0", base.push(P_GETS),def.push(P_GETS));
        
        }
    
    
    // don't need clone etc. -- I'll never clone with my arrays intact
    
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
        
        // determine my boundary -- must be at least 1 and must leave 1 over
        int boundary = (int)(sortedPop.length * top_n_percent);
        if (boundary == 0) boundary = 1;
        if (boundary == sortedPop.length) boundary = sortedPop.length-1;
        if (boundary == 0) // uh oh
            s.output.fatal("Greedy Overselection can only be done with a population of size 2 or more (offending subpopulation #" + subpopulation + ")");
        
        // load sortedFitOver
        sortedFitOver = new float[boundary];
        int y=0;
        for(int x=sortedPop.length-boundary;x<sortedPop.length;x++)
            {
            sortedFitOver[y] = (i[sortedPop[x]]).fitness.fitness();
            if (sortedFitOver[y] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  Greedy Overselection requires that all fitness values be non-negative (offending subpopulation #" + subpopulation + ")");
            y++;
            }
        
        // load sortedFitUnder
        sortedFitUnder = new float[sortedPop.length-boundary];
        y=0;
        for(int x=0;x<sortedPop.length-boundary;x++)
            {
            sortedFitUnder[y] = (i[sortedPop[x]]).fitness.fitness();
            if (sortedFitUnder[y] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  Greedy Overselection requires that all fitness values be non-negative (offending subpopulation #" + subpopulation + ")");
            y++;
            }

        // organize the distributions.  All zeros in fitness is fine
        RandomChoice.organizeDistribution(sortedFitUnder, true);
        RandomChoice.organizeDistribution(sortedFitOver, true);
        }

    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        // pick a coin toss
        if (state.random[thread].nextBoolean(gets_n_percent))
            // over -- sortedFitUnder.length to sortedPop.length
            return sortedPop[
                sortedFitUnder.length + RandomChoice.pickFromDistribution(
                    sortedFitOver,state.random[thread].nextFloat(),CHECKBOUNDARY)];
        else
            // under -- 0 to sortedFitUnder.length
            return sortedPop[RandomChoice.pickFromDistribution(
                                 sortedFitUnder,state.random[thread].nextFloat(),CHECKBOUNDARY)];
        }

    public void finishProducing(final EvolutionState s,
                                final int subpopulation,
                                final int thread)
        {
        // release the distributions so we can quickly 
        // garbage-collect them if necessary
        sortedFitUnder = null;
        sortedFitOver = null;
        sortedPop = null;
        }
    }
