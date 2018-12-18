/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.util.*;
import ec.*;

/* 
 * SUSSelection.java
 * 
 * Created: Thu Feb 12 16:19:52 EST 2009
 * By: Sean Luke
 */

/**
 * Picks individuals in a population using the Stochastic Universal Selection (SUS) process, using
 * fitnesses as returned by their fitness() methods.  This is expensive to
 * set up and bring down, so it's not appropriate for steady-state evolution.
 * If you're not familiar with the relative advantages of 
 * selection methods and just want a good one,
 * use TournamentSelection instead.   Not appropriate for
 * multiobjective fitnesses.
 *
 * <p>By default this implementation of SUS shuffles the order of the individuals
 * in the distribution before performing selection.  This isn't always present in classic
 * implementations of the algorithm but it can't hurt anything and certainly can avoid
 * certain pathological situations.  If you'd prefer not to preshuffle, set shuffle=false
 * Note that we don't actually change the order of the individuals in the population -- instead
 * we maintain our own internal array of indices and shuffle that.
 *
 * <p>Like truncation selection, 
 * SUS samples N individuals (with replacement) up front from the population,
 * Then returns those individuals one by one.
 * ECJ's implementation assumes that N is the size of the population -- that is, you're
 * going to ultimately request a whole population out of this one selection method.
 * This could be a false assumption: for example, if you only sometimes call this
 * selection method, and sometimes TournamentSelection; or if you've got multiple
 * pipelines.  In these cases, SUS is probably a bad choice anyway.
 *
 * <p>If you ask for <i>more</i> than a population's worth of individuals, SUS tries
 * to handle this gracefully by reshuffling its array and starting to select over
 * again.  But again that might suggest you are doing something wrong.
 *
 * <p><b><font color=red>
 * Note: Fitnesses must be non-negative.  0 is assumed to be the worst fitness.
 * </font></b>

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.


 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>shuffle</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(should we preshuffle the array before doing selection?)</td></tr>

 </table>
 <p><b>Default Base</b><br>
 select.sus

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SUSSelection extends SelectionMethod
    {
    /** Default base */
    public static final String P_SUS = "sus";
    public static final String P_SHUFFLE = "shuffle";
    
    /** An array of pointers to individuals in the population, shuffled along with the fitnesses array. */
    public int[] indices;
    /** The distribution of fitnesses. */
    public double[] fitnesses;
    
    /** Should we shuffle first? */
    public boolean shuffle = true;
    /** The floating point value to consider for the next selected individual. */
    public double offset = 0.0;
    /** The index in the array of the last individual selected. */
    public int lastIndex;
    /** How many samples have been done?  */
    public int steps;
    
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_SUS);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        shuffle = state.parameters.getBoolean(base.push(P_SHUFFLE),def.push(P_SHUFFLE),true);
        }

    /* Largely stolen from sim.util.Bag.  Shuffles both the indices and the doubles */
    void shuffle(MersenneTwisterFast random, double[] fitnesses, int[] indices)
        {
        int numObjs = fitnesses.length;
        //double[] fitnesses = this.fitnesses;
        //int[] indices = this.indices;
        
        double f;
        int i;
        int rand;
        
        for(int x=numObjs-1; x >= 1 ; x--)
            {
            rand = random.nextInt(x+1);
            f = fitnesses[x];
            fitnesses[x] = fitnesses[rand];
            fitnesses[rand] = f;

            i = indices[x];
            indices[x] = indices[rand];
            indices[rand] = i;
            }
        }

    // don't need clone etc. 
    public void prepareToProduce(final EvolutionState s,
        final int subpopulation,
        final int thread)
        {
        super.prepareToProduce(s, subpopulation, thread);

        lastIndex = 0;
        steps = 0;
        
        fitnesses = new double[s.population.subpops.get(subpopulation).individuals.size()];

        // compute offset
        offset = (double)(s.random[thread].nextDouble() / fitnesses.length);
        
        // load fitnesses but don't build distribution yet
        for(int x=0;x<fitnesses.length;x++)
            {
            fitnesses[x] = ((Individual)(s.population.subpops.get(subpopulation).individuals.get(x))).fitness.fitness();
            if (fitnesses[x] < 0) // uh oh
                s.output.fatal("Discovered a negative fitness value.  SUSSelection requires that all fitness values be non-negative(offending subpopulation #" + subpopulation + ")");
            }

        // construct and optionally shuffle fitness distribution and indices
        indices = new int[s.population.subpops.get(subpopulation).individuals.size()];
        for(int i=0;i<indices.length;i++) indices[i] = i;
        if (shuffle) shuffle(s.random[thread], fitnesses, indices);
                
        // organize the distribution.  All zeros in fitness is fine
        RandomChoice.organizeDistribution(fitnesses, true);
        }

    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        if (steps >= fitnesses.length)  // we've gone too far, clearly an error
            {
            state.output.warning("SUSSelection was asked for too many individuals, so we're re-shuffling.  This will give you proper results, but it might suggest an error in your code.");
            boolean s = shuffle;
            shuffle = true;
            prepareToProduce(state, subpopulation, thread);  // rebuild
            shuffle = s; // just in case
            }
            
        // find the next index
        for( /* empty */ ; lastIndex < fitnesses.length - 1; lastIndex++)
            if ((lastIndex == 0 || offset >= fitnesses[lastIndex - 1]) && offset < fitnesses[lastIndex])
                break;

        offset += (double)(1.0 / fitnesses.length);  // update for next time
        steps++;
        return indices[lastIndex];
        }
    }
