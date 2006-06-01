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
 * If.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class If extends GPNode
    {
    public String toString() { return "if"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=3)
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
        long[] dat_11_1=null;  // quiets compiler complaints
        long[] dat_11_2=null;  // quiets compiler complaints
        long dat_6_1=0L;
        long dat_6_2=0L;
        byte dat_3_1=0;
        byte dat_3_2=0;

        // No shortcuts for now
        children[0].eval(state,thread,input,stack,individual,problem);

        if (md.status == MultiplexerData.STATUS_3)
            dat_3_1 = md.dat_3;
        else if (md.status == MultiplexerData.STATUS_6)
            dat_6_1 = md.dat_6;
        else // md.status == MultiplexerData.STATUS_11
            {
            dat_11_1 = md.popDat11();
            System.arraycopy(md.dat_11,0,
                             dat_11_1,0,
                             MultiplexerData.MULTI_11_NUM_BITSTRINGS);
            }

        children[1].eval(state,thread,input,stack,individual,problem);

        if (md.status == MultiplexerData.STATUS_3)
            dat_3_2 = md.dat_3;
        else if (md.status == MultiplexerData.STATUS_6)
            dat_6_2  = md.dat_6;
        else // md.status == MultiplexerData.STATUS_11
            {
            dat_11_2 = md.popDat11();
            System.arraycopy(md.dat_11,0,
                             dat_11_2,0,
                             MultiplexerData.MULTI_11_NUM_BITSTRINGS);
            }

        // tweak -- if a then b else c is equivalent to
        // (a -> b) ^ (~a -> c) which is equivalent to
        // (~a v b) ^ (a v c).  In Java, ^ (-1) is the same
        // is bitwise not.

        children[2].eval(state,thread,input,stack,individual,problem);

        if (md.status == MultiplexerData.STATUS_3)
            md.dat_3 = (byte)(
                ((dat_3_1 ^ (byte)(-1)) | dat_3_2 ) &
                ((dat_3_1 | md.dat_3)));

        else if (md.status == MultiplexerData.STATUS_6)
            md.dat_6 = 
                ((dat_6_1 ^ (-1L)) | dat_6_2 ) &
                ((dat_6_1 | md.dat_6));

        else // md.status == MultiplexerData.STATUS_11
            {
            for(int x=0;x<MultiplexerData.MULTI_11_NUM_BITSTRINGS;x++)
                md.dat_11[x] = 
                    ((dat_11_1[x] ^ (-1L)) | dat_11_2[x] ) &
                    ((dat_11_1[x] | md.dat_11[x]));
            md.pushDat11(dat_11_2);
            md.pushDat11(dat_11_1);
            }
        }
    }



