/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;

/* 
 * GenerationSwitchPipeline.java
 * 
 * Created: Sat Nov 10 14:10:35 EST 2001
 * By: Sean Luke
 */

/**
 * GenerationSwitchPipeline is a simple BreedingPipeline which switches its source depending
 * on the generation.  If the generation number is < n, then GenerationSwitchPipeline uses
 * source.0.  If the generation number if >= n, then GenerationSwitchPipeline uses source.1.
 
 * <p><b>Important Note:</b> Because GenerationSwitchPipeline gets the generation number
 * from the EvolutionState, and this number is not protected by a mutex, if you create
 * an EvolutionState or Breeder which uses multiple threads that can update the generation
 * number as they like, you could cause a race condition.  This doesn't occur with the
 * present EvolutionState objects provided with ECJ, but you should be aware of the
 * possibility.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Defined as the max of its children's responses. 

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>generate-max</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(Each time produce(...) is called, should the GenerationSwitchPipeline
 force all its sources to produce exactly the same number of individuals as the largest
 typical number of individuals produced by any source in the group?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>switch-at</tt><br>
 <font size=-1> int &gt;= 0</tt></font></td>
 <td valign=top>(The generation we will switch at)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 breed.generation-switch

 *
 * @author Sean Luke
 * @version 1.0 
 */
 
public class GenerationSwitchPipeline extends BreedingPipeline
    {
    public static final String P_SWITCHAT = "switch-at";
    public static final String P_MULTIBREED = "generation-switch";
    public static final String P_GEN_MAX = "generate-max";
    public static final int NUM_SOURCES = 2;

    public int maxGeneratable;
    public boolean generateMax;
    public int generationSwitch;

    public Parameter defaultBase()
        {
        return BreedDefaults.base().push(P_MULTIBREED);
        }

    public int numSources() { return NUM_SOURCES; }    

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        state.output.exitIfErrors();

        generationSwitch = state.parameters.getInt(base.push(P_SWITCHAT),def.push(P_SWITCHAT),0);
        if (generationSwitch < 0)
            state.output.fatal("GenerationSwitchPipeline must have a switch-at >= 0", 
                               base.push(P_SWITCHAT),def.push(P_SWITCHAT));

        generateMax = state.parameters.getBoolean(base.push(P_GEN_MAX),def.push(P_GEN_MAX),true);
        maxGeneratable=0;  // indicates that I don't know what it is yet.  
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
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 

        {
        BreedingSource s = (state.generation < generationSwitch ?
                            sources[0] : sources[1] );
        int total;

        if (generateMax)
            {
            if (maxGeneratable==0)
                maxGeneratable = maxChildProduction();
            int n = maxGeneratable;
            if (n < min) n = min;
            if (n > max) n = max;

            total = s.produce(
                n,n,start,subpopulation,inds,state,thread);
            }
        else
            {
            total = s.produce(
                min,max,start,subpopulation,inds,state,thread);
            }
            
        // clone if necessary
        if (s instanceof SelectionMethod)
            for(int q=start; q < total+start; q++)
                inds[q] = (Individual)(inds[q].clone());
        
        return total;
        }
    }
