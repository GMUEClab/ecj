/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.parity.func;
import ec.*;
import ec.app.parity.*;
import ec.gp.*;
import ec.util.*;

/* 
 * D1.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class D1 extends GPNode
    {
    public String toString() { return "D1"; }

    /*
      public void checkConstraints(final EvolutionState state,
      final int tree,
      final GPIndividual typicalIndividual,
      final Parameter individualBase)
      {
      super.checkConstraints(state,tree,typicalIndividual,individualBase);
      if (children.length!=0)
      state.output.error("Incorrect number of children for node " + 
      toStringForError() + " at " +
      individualBase);
      }
    */
    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        ((ParityData)input).x = 
            ((((Parity)problem).bits >>> 1 ) & 1);
        }
    }



