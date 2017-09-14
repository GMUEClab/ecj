/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * MultiBreedingPipeline.java
 * 
 * Created: December 28, 1999
 * By: Sean Luke
 */

/**
 * MultiBreedingPipeline is a BreedingPipeline stores some <i>n</i> child sources; 
 * each time it must produce an individual or two, 
 * it picks one of these sources at random and has it do the production.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 If by <i>base</i>.<tt>generate-max</tt> is <tt>true</tt>, then always the maximum
 number of the typical numbers of any child source.  If <tt>false</itt>, then varies
 depending on the child source picked.

 <p><b>Number of Sources</b><br>
 Dynamic.  As many as the user specifies.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>generate-max</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(Each time produce(...) is called, should the MultiBreedingPipeline
 force all its sources to produce exactly the same number of individuals as the largest
 typical number of individuals produced by any source in the group?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 breed.multibreed

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class MultiBreedingPipeline extends BreedingPipeline
    {
    public static final String P_GEN_MAX = "generate-max";
    public static final String P_MULTIBREED = "multibreed";

    public int maxGeneratable;
    public boolean generateMax;

    public Parameter defaultBase()
        {
        return BreedDefaults.base().push(P_MULTIBREED);
        }

    public int numSources() { return DYNAMIC_SOURCES; }    

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        double total = 0.0;
        
        if (sources.length == 0)  // uh oh
            state.output.fatal("num-sources must be provided and > 0 for MultiBreedingPipeline",
                base.push(P_NUMSOURCES), def.push(P_NUMSOURCES));
        
        for(int x=0;x<sources.length;x++)
            {
            if (sources[x].probability<0.0) // null checked from state.output.error above
                state.output.error("Pipe #" + x + " must have a probability >= 0.0",base);  // convenient that NO_PROBABILITY is -1...
            else total += sources[x].probability;
            }

        state.output.exitIfErrors();

        // Now check for nonzero probability (we know it's positive)
        if (total == 0.0)
            state.output.warning("MultiBreedingPipeline's children have all zero probabilities -- this will be treated as a uniform distribution.  This could be an error.", base);

        // allow all zero probabilities
        BreedingSource.setupProbabilities(sources);

        generateMax = state.parameters.getBoolean(base.push(P_GEN_MAX),def.push(P_GEN_MAX),true);
        maxGeneratable=0;  // indicates that I don't know what it is yet.  
                
        // declare that likelihood isn't used
        if (likelihood < 1.0)
            state.output.warning("MultiBreedingPipeline does not respond to the 'likelihood' parameter.",
                base.push(P_LIKELIHOOD), def.push(P_LIKELIHOOD));
        }

    /** Returns the max of typicalIndsProduced() of all its children */
    public int typicalIndsProduced()
        { 
        if (maxGeneratable==0) // not determined yet
            maxGeneratable = maxChildProduction();
        return maxGeneratable; 
        }


    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)

        {
        int start = inds.size();

        BreedingSource s = sources[BreedingSource.pickRandom(sources,state.random[thread].nextDouble())];
        int total;
        
        if (generateMax)
            {
            if (maxGeneratable==0)
                maxGeneratable = maxChildProduction();
            int n = maxGeneratable;
            if (n < min) n = min;
            if (n > max) n = max;

            total = s.produce(n,n,subpopulation,inds, state,thread, misc);
            }
        else
            {
            total = s.produce(min,max,subpopulation,inds, state,thread, misc);
            }
            
        return total;
        }
    }
