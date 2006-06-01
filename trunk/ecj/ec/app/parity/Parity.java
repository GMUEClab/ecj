/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.parity;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Parity.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Parity implements the family of <i>n</i>-[even|odd]-Parity problems up 
 * to 32-parity.  Read the README file in this package for information on
 * how to set up the parameter files to your liking -- it's a big family.
 *
 * <p>The Parity family evolves a boolean function on <i>n</i> sets of bits,
 * which returns true if the number of 1's is even (for even-parity) or odd
 * (for odd-parity), false otherwise. 
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.parity.ParityData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Parity problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>even</tt><br>
 <font size=-1> bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>(is this even-parity (as opposed to odd-parity)?)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>bits</tt><br>
 <font size=-1> 2 &gt;= int &lt;= 31</font></td>
 <td valign=top>(The number of data bits)</td></tr>
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

public class Parity extends GPProblem implements SimpleProblemForm
    {
    public static final String P_NUMBITS = "bits";
    public static final String P_EVEN = "even";

    public boolean doEven;
    public int numBits;
    public int totalSize;

    public int bits;  // data bits

    // we'll need to deep clone this one though.
    public ParityData input;

    public Object clone()
        {
        Parity myobj = (Parity) (super.clone());
        myobj.input = (ParityData)(input.clone());
        return myobj;
        }

    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // not using a default base here

        // can't use all 32 bits -- Java is signed.  Must use 31 bits.
        
        numBits = state.parameters.getIntWithMax(base.push(P_NUMBITS),null,2,31);
        if (numBits<2)
            state.output.fatal("The number of bits for Parity must be between 2 and 31 inclusive",base.push(P_NUMBITS));
        
        totalSize = 1;
        for(int x=0;x<numBits;x++)
            totalSize *=2;   // safer than Math.pow()

        doEven = state.parameters.getBoolean(base.push(P_EVEN),null,true);

        // set up our input
        input = (ParityData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA),null, ParityData.class);
        input.setup(state,base.push(P_DATA));
        }


    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int sum = 0;
                
            for(bits=0;bits<totalSize;bits++)
                {
                int tb = 0;
                // first, is #bits even or odd?
                for(int b=0;b<numBits;b++)
                    tb += (bits >>> b) & 1;
                tb &= 1;  // now tb is 1 if we're odd, 0 if we're even

                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                if ((doEven && ((input.x & 1) != tb)) ||
                    ((!doEven) && ((input.x & 1) == tb)))
                    sum++;
                }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)(totalSize - sum));
            f.hits = sum;
            ind.evaluated = true;
            }
        }
    }
