/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.majority.func;
import ec.*;
import ec.app.majority.*;
import ec.gp.*;
import ec.util.*;

public class EE extends GPNode
    {
    public String toString() { return "ee"; }

    public int expectedChildren() { return 0; }
    
    public static long X0 = 0;
    public static long X1 = 0;
    static
        {
        for(int i = 0 ; i < 64; i++)
            {
            long val = (i >> 1) & 0x1;  // east east element
            X0 = X0 | (val << i);
            }
                
        for(int i = 64 ; i < 128; i++)
            {
            long val = (i >> 1) & 0x1;  // east east element
            X1 = X1 | (val << (i - 64));
            }
        }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        MajorityData md = (MajorityData) input;
        md.data0 = X0;
        md.data1 = X1;
        }
    }



