/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ant.func;
import ec.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Progn4.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Progn4 extends GPNode implements EvalPrint
    {
    public String toString() { return "progn4"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=4)
            state.output.error("Incorrect number of children for node " + 
                               toStringForError() + " at " +
                               individualBase);
        }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        // Evaluate children.  Easy as cake.
        children[0].eval(state,thread,input,stack,individual,problem);
        children[1].eval(state,thread,input,stack,individual,problem);
        children[2].eval(state,thread,input,stack,individual,problem);
        children[3].eval(state,thread,input,stack,individual,problem);
        }

    public void evalPrint(final EvolutionState state,
                          final int thread,
                          final GPData input,
                          final ADFStack stack,
                          final GPIndividual individual,
                          final Problem problem,
                          final int[][] map2)
        {
        // Evaluate children.  Easy as cake.
        ((EvalPrint)children[0]).evalPrint(state,thread,input,stack,individual,problem,map2);
        ((EvalPrint)children[1]).evalPrint(state,thread,input,stack,individual,problem,map2);
        ((EvalPrint)children[2]).evalPrint(state,thread,input,stack,individual,problem,map2);
        ((EvalPrint)children[3]).evalPrint(state,thread,input,stack,individual,problem,map2);
        }
    }



