/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;
import java.util.*;

/* 
 * UniquePipeline.java
 * 
 * Created: Fri Jan 23 12:19:01 EST 2015
 * By: Sean Luke
 */

/**
 * UniquePipeline is a BreedingPipeline which tries very hard to guarantee that all
 * the individuals it produces are unique from members of the original subpopulation.
 *
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the child produces

 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>generate-max</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(do we always generate the maximum number of possible individuals, or at least the minimum number?)</td></tr>
 <tr><td valign=top><i>base.</i><tt>duplicate-retries</tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top>(number of times we try to find a duplicate individual before giving up and just filling the remainder with non-duplicate individuals)</td></tr>
 </table>
 <p><b>Default Base</b><br>
 breed.unique

 * @author Sean Luke
 * @version 1.0 
 */

public class UniquePipeline extends BreedingPipeline
    {
    public static final String P_UNIQUE = "unique";
    public static final String P_GEN_MAX = "generate-max";
    public static final String P_RETRIES = "duplicate-retries";
    
    public static final int NUM_SOURCES = 1;
    
    public HashSet set = new HashSet();
    
    public Parameter defaultBase() { return BreedDefaults.base().push(P_UNIQUE); }

    public int numSources() { return NUM_SOURCES; }

    public boolean resetEachGeneration;
    public int numDuplicateRetries;
    boolean generateMax;
        
    public Object clone()
        {
        UniquePipeline c = (UniquePipeline)(super.clone());
        c.set = (HashSet)(set.clone());
        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        generateMax = state.parameters.getBoolean(base.push(P_GEN_MAX), def.push(P_GEN_MAX),false);

        if (likelihood != 1.0)
            state.output.warning("UniquePipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                base.push(P_LIKELIHOOD),
                def.push(P_LIKELIHOOD));

        // How often do we retry if we find a duplicate?
        numDuplicateRetries = state.parameters.getInt(
            base.push(P_RETRIES),def.push(P_RETRIES),0);
        if (numDuplicateRetries < 0) state.output.fatal(
            "The number of retries for duplicates must be an integer >= 0.\n",
            base.push(P_RETRIES),def.push(P_RETRIES));      
        }



    public void prepareToProduce(
        final EvolutionState state,
        final int subpopulation,
        final int thread)
        {
        set.clear();
        ArrayList<Individual> inds = state.population.subpops.get(subpopulation).individuals;
        for(int i = 0; i < inds.size(); i++)
            set.add(inds.get(i));
        }

    int removeDuplicates(ArrayList<Individual> inds, int start, int num)
        {
        for(int i = start; i < start + num; i++)
            {
            if (set.contains(inds.get(i)))  // swap in from top
                {
                inds.set(i, inds.get(start+num - 1));
                inds.set(start+num-1, null);
                num--;
                i--;  // try again
                }
            }
        return num;
        }

    public int produce(
        final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = 0;
        
        int n = 0;  // unique individuals we've built so far
        int remainder = (generateMax ? max : min);
        for(int retry = 0; retry < numDuplicateRetries + 1; retry++)
            {
            // grab individuals from our source and stick 'em right into inds.
            // we'll verify them from there
            int newmin = Math.min(Math.max(min - n, 1), max - n);
            int num = sources[0].produce(newmin,max - n,subpopulation,inds, state,thread, misc);
            
            int total = removeDuplicates(inds, start + n, num);  // unique individuals out of the num
            n += total;  // we'll keep those
            }
        
        if (n < remainder)  // never succeeded to build unique individuals, just make some non-unique ones
            n += sources[0].produce(remainder - n,max - n,subpopulation,inds, state,thread, misc);

        return n;
        }
    }
