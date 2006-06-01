/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.select;
import ec.*;
import ec.util.*;

/* 
 * MultiSelection.java
 * 
 * Created: Wed Dec 29 21:44:50 1999
 * By: Sean Luke
 */

/**
 * MultiSelection is a SelectionMethod which stores some <i>n</i> subordinate
 * SelectionMethods.  Each time it must produce an individual, 
 * it picks one of these SelectionMethods at random and has it do the production
 * instead.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 Always 1.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-selects</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(The number of subordinate SelectionMethods)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>select.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != SelectionMethod</tt><br>
 <td valign=top>(Subordinate SelectionMethod <i>n</i>)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 select.multiselect

 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><i>base</i>.<tt>select.</tt><i>n</i><br>
 <td>Subordinate SelectionMethod <i>n</i></td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class MultiSelection extends SelectionMethod
    {
    public static final String P_NUMSELECTS = "num-selects";
    public static final String P_SELECT = "select";
    public static final String P_MULTISELECT = "multiselect";

    /** The MultiSelection's individuals */
    public SelectionMethod selects[];

    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_MULTISELECT);
        }

    public Object clone()
        {
        MultiSelection c = (MultiSelection)(super.clone());
        
        // make a new array
        c.selects = new SelectionMethod[selects.length];

        // clone the selects -- we won't go through the hassle of
        // determining if we have a DAG or not -- we'll just clone
        // it out to a tree.  I doubt it's worth it.

        for(int x=0;x<selects.length;x++)
            c.selects[x] = (SelectionMethod)(selects[x].clone());

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        int numSelects = state.parameters.getInt(
            base.push(P_NUMSELECTS),def.push(P_NUMSELECTS),1);
        if (numSelects==0)
            state.output.fatal("The number of MultiSelection sub-selection methods must be >= 1).",
                               base.push(P_NUMSELECTS),def.push(P_NUMSELECTS));

        // make our arrays
        selects = new SelectionMethod[numSelects];

        float total = 0.0f;

        for(int x=0;x<numSelects;x++)
            {
            Parameter p = base.push(P_SELECT).push(""+x);
            Parameter d = def.push(P_SELECT).push(""+x);

            selects[x] = (SelectionMethod)
                (state.parameters.getInstanceForParameter(
                    p,d, SelectionMethod.class));       
            selects[x].setup(state,p);
            
            // now check probability
            if (selects[x].probability<0.0)
                state.output.error(
                    "MultiSelection select #" + x + 
                    " must have a probability >= 0.0",
                    p.push(P_PROB),d.push(P_PROB));
            else total += selects[x].probability;
            }

        state.output.exitIfErrors();

        // Now check for valid probability
        if (total <= 0.0)
            state.output.fatal("MultiSelection selects do not sum to a positive probability",base);

        if (total != 1.0)
            {
            state.output.message("Must normalize probabilities for " + base);
            for(int x=0;x<numSelects;x++) selects[x].probability /= total;
            }

        // totalize
        float tmp = 0.0f;
        for(int x=0;x<numSelects-1;x++) // yes, it's off by one
            { 
            tmp += selects[x].probability; 
            selects[x].probability = tmp;
            }
        selects[numSelects-1].probability = 1.0f;
        }

    public boolean produces(final EvolutionState state,
                            final Population newpop,
                            final int subpopulation,
                            final int thread)
        {
        if (!super.produces(state,newpop,subpopulation,thread))
            return false;

        for(int x=0;x<selects.length;x++)
            if (!selects[x].produces(state,newpop,subpopulation,thread))
                return false;
        return true;
        }


    public void prepareToProduce(final EvolutionState s,
                                 final int subpopulation,
                                 final int thread)
        {
        for(int x=0;x<selects.length;x++)
            selects[x].prepareToProduce(s,subpopulation,thread);
        }


    public int produce(final int subpopulation,
                       final EvolutionState state,
                       final int thread)
        {
        return selects[BreedingSource.pickRandom(
                           selects,state.random[thread].nextFloat())].produce(
                               subpopulation,state,thread);
        }

    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 

        {
        return selects[BreedingSource.pickRandom(
                           selects,state.random[thread].nextFloat())].produce(
                               min,max,start,subpopulation,inds,state,thread);
        }
    }
