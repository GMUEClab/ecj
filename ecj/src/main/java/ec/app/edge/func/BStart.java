/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.edge.func;
import ec.*;
import ec.app.edge.*;
import ec.gp.*;
import ec.util.*;

/* 
 * BStart.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class BStart extends GPNode
    {
    public String toString() { return "s2"; }

    /*
      public void checkConstraints(final EvolutionState state,
      final int tree,
      final GPIndividual typicalIndividual,
      final Parameter individualBase)
      {
      super.checkConstraints(state,tree,typicalIndividual,individualBase);
      if (children.length!=1)
      state.output.error("Incorrect number of children for node " + 
      toStringForError() + " at " +
      individualBase);
      }
    */
    public int expectedChildren() { return 1; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        int edge = ((EdgeData)(input)).edge;
        Edge prob = (Edge)problem;

        prob.start[prob.from[edge]] = true;

        // pass the edge down

        children[0].eval(state,thread,input,stack,individual,problem);
        }
    }



