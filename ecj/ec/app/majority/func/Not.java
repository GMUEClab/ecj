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

public class Not extends GPNode
    {
    public String toString() { return "not"; }

    public int expectedChildren() { return 1; }
    
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        children[0].eval(state,thread,input,stack,individual,problem);

        MajorityData md = (MajorityData) input;
        md.data0 = ~(md.data0);
        md.data1 = ~(md.data1);
        }
    }



