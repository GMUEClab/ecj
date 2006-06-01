/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.twobox.func;
import ec.*;
import ec.app.twobox.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Mul.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Mul extends GPNode
    {
    public String toString() { return "*"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=2)
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
        TwoBoxData rd = ((TwoBoxData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        if ( rd.x != 0 ) // safe to short-circuit
            {
            double result;
            result = rd.x;
            children[1].eval(state,thread,input,stack,individual,problem);
            rd.x = result * rd.x;
            }
        }
    }



