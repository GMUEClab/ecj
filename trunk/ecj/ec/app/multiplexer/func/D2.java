/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.multiplexer.func;
import ec.*;
import ec.app.multiplexer.*;
import ec.gp.*;
import ec.util.*;

/* 
 * D2.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class D2 extends GPNode
    {
    final static int bitpos = 2;  /* D2 */

    public String toString() { return "d2"; }

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

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        MultiplexerData md = (MultiplexerData)input;

        if (md.status == MultiplexerData.STATUS_3)
            md.dat_3 = Fast.M_3[bitpos + MultiplexerData.STATUS_3];
        else if (md.status == MultiplexerData.STATUS_6)
            md.dat_6 = Fast.M_6[bitpos + MultiplexerData.STATUS_6];
        else // md.status == MultiplexerData.STATUS_11
            System.arraycopy(Fast.M_11[bitpos + MultiplexerData.STATUS_11],0,
                             md.dat_11,0,
                             MultiplexerData.MULTI_11_NUM_BITSTRINGS);
        }
    }



