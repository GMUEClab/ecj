/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.util.*;
import ec.*;

/* 
 * GPProblem.java
 * 
 * Created: Wed Oct 27 18:07:06 1999
 * By: Sean Luke
 */

/**
 * A GPProblem is a Problem which is meant to efficiently handle GP
 * evaluation.  GPProblems hold one ADFStack, which is used to 
 * evaluate a large number of trees without having to be garbage-collected
 * and reallocated.  Be sure to call stack.reset() after each
 * tree evaluation.
 *
 * <p>GPProblem also provides a default (empty) version of describe(...) for
 * SimpleProblemForm so you don't have to bother with it if you don't want to.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.stack</tt><br>
 <font size=-1>classname, inherits or = ec.ADFStack</font></td>
 <td valign=top>(the class for the GPProblem's ADF Stack)</td></tr>
 <tr><td valign=top><i>base</i><tt>.data</tt><br>
 <font size=-1>classname, inherits and != ec.GPData</font></td>
 <td valign=top>(the class for the GPProblem's basic GPData type)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.problem

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.stack</tt><br>
 <td valign=top>(stack)</td></tr> 
 <tr><td valign=top><i>base</i><tt>.data</tt><br>
 <td valign=top>(data)</td></tr> 
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPProblem extends Problem 
    {
    public final static String P_GPPROBLEM = "problem";
    public final static String P_STACK = "stack";
    public final static String P_DATA = "data";

    /** The GPProblem's stack */
    public ADFStack stack;

    /** The GPProblems' GPData */
    public GPData data;

    /** GPProblem defines a default base so your subclass doesn't
        absolutely have to. */
    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_GPPROBLEM);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter p = base.push(P_STACK);
        Parameter def = defaultBase();

        stack = (ADFStack)
            (state.parameters.getInstanceForParameterEq(
                p,def.push(P_STACK),ADFStack.class));
        stack.setup(state,p);

        p = base.push(P_DATA);
        data = (GPData)
            (state.parameters.getInstanceForParameter(
                p,def.push(P_DATA),GPData.class));
        data.setup(state,p);
        }

    public Object clone()
        {
        GPProblem prob = (GPProblem)(super.clone());
        
        // deep-clone the stack; it's not shared
        prob.stack = (ADFStack)(stack.clone());
        return prob;
        }


    public void describe(final Individual ind, 
                         final EvolutionState state, 
                         final int threadnum, final int log,
                         final int verbosity)
        {
        // default version does nothing
        return;
        }
    }
