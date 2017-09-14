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

public class If extends GPNode
    {
    public String toString() { return "if"; }

    public int expectedChildren() { return 3; }
    
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        children[0].eval(state,thread,input,stack,individual,problem);

        MajorityData md = (MajorityData) input;
        long y0 = md.data0;
        long y1 = md.data1;
        
        children[1].eval(state,thread,input,stack,individual,problem);
        long z0 = md.data0;
        long z1 = md.data1;

        children[2].eval(state,thread,input,stack,individual,problem);

        // IF Y THEN Z ELSE MD is
        // (Y -> Z) ^ (~Y -> MD)
        // (!Y v Z) ^ (Y v MD)
        md.data0 = (~y0 | z0) & (y0 | (md.data0));
        md.data1 = (~y1 | z1) & (y1 | (md.data1));
        }
    }



