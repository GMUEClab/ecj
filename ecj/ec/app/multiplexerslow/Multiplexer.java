/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.multiplexerslow;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Multiplexer.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Multiplexer implements the family of <i>n</i>-Multiplexer problems.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.multiplexer.MultiplexerData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Multiplexer problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>bits</tt><br>
 <font size=-1>1, 2, or 3</font></td>
 <td valign=top>(The number of address bits (1 == 3-multiplexer, 2 == 6-multiplexer, 3==11-multiplexer)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Multiplexer extends GPProblem implements SimpleProblemForm
    {
    public static final int NUMINPUTS = 20;
    public static final String P_NUMBITS = "bits";

    public int bits;  // number of bits in the data
    public int amax; // maximum address value
    public int dmax; // maximum data value
    public int addressPart;  // the current address part
    public int dataPart;     // the current data part

    // we'll need to deep clone this one though.
    public MultiplexerData input;

    public Object clone()
        {
        Multiplexer myobj = (Multiplexer) (super.clone());
        myobj.input = (MultiplexerData)(input.clone());
        return myobj;
        }

    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // not using any default base -- it's not safe

        // I figure 3 bits is plenty -- otherwise we'd be dealing with
        // REALLY big arrays!
        bits = state.parameters.getIntWithMax(base.push(P_NUMBITS),null,1,3);
        if (bits<1)
            state.output.fatal("The number of bits for Multiplexer must be between 1 and 3 inclusive");
        
        amax=1;
        for(int x=0;x<bits;x++) amax *=2;   // safer than Math.pow(...)

        dmax=1;
        for(int x=0;x<amax;x++) dmax *=2;   // safer than Math.pow(...)
        
        // set up our input
        input = (MultiplexerData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA),null, MultiplexerData.class);
        input.setup(state,base.push(P_DATA));
        }


    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int sum = 0;
                
            for(addressPart = 0; addressPart < amax; addressPart++)
                for(dataPart = 0; dataPart < dmax; dataPart++)
                    {
                    ((GPIndividual)ind).trees[0].child.eval(
                        state,threadnum,input,stack,((GPIndividual)ind),this);
                    sum += 1- (                  /* "Not" */
                        ((dataPart >>> addressPart) & 1) /* extracts the address-th 
                                                            bit in data and moves 
                                                            it to position 0, 
                                                            clearing out all 
                                                            other bits */
                        ^                   /* "Is Different from" */
                        (input.x & 1));      /* A 1 if input.x is 
                                                non-zero, else 0. */
                    }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)(amax*dmax - sum));
            f.hits = sum;
            ind.evaluated = true;
            }
        }
    }
