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
 * Div.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Div extends GPNode
    {
    public String toString() { return "%"; }

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

        // evaluate children[1] first to determine if the demoniator is 0
        children[1].eval(state,thread,input,stack,individual,problem);
        if (rd.x==0.0) 
            // the answer is 1.0 since the denominator was 0.0
            rd.x = 1.0;
        else
            {
            double result;
            result = rd.x;
            
            children[0].eval(state,thread,input,stack,individual,problem);
            rd.x = rd.x / result;
            }
        }
    }



