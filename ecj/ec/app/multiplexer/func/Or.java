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
 * Or.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Or extends GPNode
    {
    public String toString() { return "or"; }

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
        MultiplexerData md = (MultiplexerData)input;
        long[] dat_11=null;  // quiets compiler complaints
        long dat_6=0L;
        byte dat_3=0;

        // No shortcuts for now
        children[0].eval(state,thread,input,stack,individual,problem);

        if (md.status == MultiplexerData.STATUS_3)
            dat_3 = md.dat_3;
        else if (md.status == MultiplexerData.STATUS_6)
            dat_6 = md.dat_6;
        else // md.status == MultiplexerData.STATUS_11
            {
            dat_11 = md.popDat11();
            System.arraycopy(md.dat_11,0,
                             dat_11,0,
                             MultiplexerData.MULTI_11_NUM_BITSTRINGS);
            }

        children[1].eval(state,thread,input,stack,individual,problem);

        // modify

        if (md.status == MultiplexerData.STATUS_3)
            md.dat_3 |= dat_3;
        else if (md.status == MultiplexerData.STATUS_6)
            md.dat_6 |= dat_6;
        else // md.status == MultiplexerData.STATUS_11
            {
            for(int x=0;x<MultiplexerData.MULTI_11_NUM_BITSTRINGS;x++)
                md.dat_11[x] |= dat_11[x];
            md.pushDat11(dat_11);
            }
        }
    }



