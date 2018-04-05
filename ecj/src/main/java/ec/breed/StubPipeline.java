/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.breed;
import ec.*;
import ec.util.*;
import ec.select.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * StubPipeline.java
 * 
 * Created: Wed Jun  7 15:14:17 CEST 2017
 * By: Sean Luke
 */

/**
 * StubPipeline is a BreedingPipeline subclass which, during fillStubs(), fills all the stubs
 * with its own stub pipeline.  The stub pipeline's stubs are first filled by parent
 * stub sources.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the child produces


 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>stub-source</tt><br>
 <font size=-1>classname, inherits and != ec.BreedingSource</font></td>
 <td valign=top>(the prototypical "stub pipeline" Breeding Source)</td></tr>
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>stub-source</tt></td>
 <td>i_prototype (the stub pipeline)</td></tr>
 </table>
 
 <p><b>Default Base</b><br>
 breed.stub

 * @author Sean Luke
 * @version 1.0 
 */

public class StubPipeline extends ReproductionPipeline
    {
    public static final String P_STUB = "stub";
    public static final String P_STUB_PIPELINE = "stub-source";
    
    public BreedingSource stubPipeline;
    
    public Parameter defaultBase() { return BreedDefaults.base().push(P_STUB); }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        Parameter def = defaultBase();

        // load the breeding pipeline
        stubPipeline = (BreedingSource)(
            state.parameters.getInstanceForParameter(
                base.push(P_STUB_PIPELINE), def.push(P_STUB_PIPELINE), BreedingSource.class));
        stubPipeline.setup(state,base.push(P_STUB_PIPELINE));
        }

    public Object clone()
        {
        StubPipeline other = (StubPipeline)(super.clone());
        other.stubPipeline = (BreedingSource)(other.stubPipeline.clone());
        return other;
        }

    public void fillStubs(final EvolutionState state, BreedingSource source)
        {
        // fill the stubs in my stub-pipeline first with my parent source
        stubPipeline.fillStubs(state, source);
        
        // fill subsidiary stubs with my stubpipeline, including my immediate source
        super.fillStubs(state, stubPipeline);
        }
    }
