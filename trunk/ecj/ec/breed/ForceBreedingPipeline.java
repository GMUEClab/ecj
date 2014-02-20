/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;

/* 
 * ForceBreedingPipeline.java
 * 
 * Created: December 28, 1999
 * By: Sean Luke
 */

/**
 *
 * ForceBreedingPipeline has one source.  To fill its quo for produce(...),
 * ForceBreedingPipeline repeatedly forces its source to produce exactly numInds
 * individuals at a time, except possibly the last time, where the number of
 * individuals its source produces may be as low as 1.  This is useful for forcing
 * Crossover to produce only one individual, or mutation to produce 2 individuals
 * always, etc.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Determined by <i>base</i>.<tt>num-inds</tt>

 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-inds</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(The number of individuals this breeding pipeline will force its
 source to produce each time in order to fill the quo for produce(...).)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 breed.force

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class ForceBreedingPipeline extends BreedingPipeline
    {
    public static final String P_NUMINDS = "num-inds";
    public static final String P_FORCE = "force";

    public int numInds;

    public Parameter defaultBase()
        {
        return BreedDefaults.base().push(P_FORCE);
        }

    public int numSources() { return 1; }    

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();
        numInds = state.parameters.getInt(base.push(P_NUMINDS),def.push(P_NUMINDS),1);
        if (numInds==0)
            state.output.fatal("ForceBreedingPipeline must produce at least 1 child at a time", base.push(P_NUMINDS),def.push(P_NUMINDS));

        // declare that likelihood isn't used
        if (likelihood < 1.0)
            state.output.warning("ForceBreedingPipeline does not respond to the 'likelihood' parameter.",
                base.push(P_LIKELIHOOD), def.push(P_LIKELIHOOD));
        }

    /** Returns the max of typicalIndsProduced() of all its children */
    public int typicalIndsProduced()
        {
        return numInds;
        }

    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        int n = numInds;
        if (n < min) n = min;
        if (n > max) n = max;

        int total;
        int numToProduce;
        for(total=0; total<n; )
            {
            numToProduce = n - total;
            if (numToProduce > numInds) numToProduce = numInds;

            total += sources[0].produce(numToProduce,numToProduce,start+total,
                subpopulation,inds,state,thread);
            }
        
        // clone if necessary
        if (sources[0] instanceof SelectionMethod)
            for(int q=start; q < total+start; q++)
                inds[q] = (Individual)(inds[q].clone());
                
        return total;
        }
    }
