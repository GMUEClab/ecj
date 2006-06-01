/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.regression.func;
import ec.*;
import ec.app.regression.*;
import ec.gp.*;
import ec.util.*;
import java.io.*;


/* 
 * RegERC.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class RegERC extends ERC
    {
    public double value;

    // making sure that we don't have any children is already
    // done in ERC.checkConstraints(), so we don't need to implement that.

    // this will produce numbers from [-1.0, 1.0), which is probably
    // okay but you might want to modify it if you don't like seeing
    // -1.0's occasionally showing up very rarely.
    public void resetNode(final EvolutionState state, final int thread)
        { value = state.random[thread].nextDouble() * 2 - 1.0; }

    public int nodeHashCode()
        {
        // a reasonable hash code
        return this.getClass().hashCode() + Float.floatToIntBits((float)value);
        }

    public boolean nodeEquals(final GPNode node)
        {
        // check first to see if we're the same kind of ERC -- 
        // won't work for subclasses; in that case you'll need
        // to change this to isAssignableTo(...)
        if (this.getClass() != node.getClass()) return false;
        // now check to see if the ERCs hold the same value
        return (((RegERC)node).value == value);
        }

    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        value = dataInput.readDouble();
        }

    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeDouble(value);
        }

    public String encode()
        { return Code.encode(value); }

    public boolean decode(DecodeReturn dret)
        {
        // store the position and the string in case they
        // get modified by Code.java
        int pos = dret.pos;
        String data = dret.data;

        // decode
        Code.decode(dret);

        if (dret.type != DecodeReturn.T_DOUBLE) // uh oh!
            {
            // restore the position and the string; it was an error
            dret.data = data;
            dret.pos = pos;
            return false;
            }

        // store the data
        value = dret.d;
        return true;
        }

    public String name() { return ""; } // I'm the only ERC class, this is fine

    public String toStringForHumans()
        { return "" + (float)value; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        RegressionData rd = ((RegressionData)(input));
        rd.x = value;
        }
    }



