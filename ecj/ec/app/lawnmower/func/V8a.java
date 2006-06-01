/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.lawnmower.func;
import ec.*;
import ec.app.lawnmower.*;
import ec.gp.*;
import ec.util.*;

/* 
 * V8a.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class V8a extends GPNode
    {
    public final static int MODULO = 8;  // odd that it doesn't change with map size

    public String toString() { return "v8a"; }

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
        int resultx;
        int resulty;

        LawnmowerData rd = ((LawnmowerData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        resultx = rd.x;
        resulty = rd.y;

        children[1].eval(state,thread,input,stack,individual,problem);
        rd.x = (resultx + rd.x) % MODULO;
        rd.y = (resulty + rd.y) % MODULO;
        }
    }



