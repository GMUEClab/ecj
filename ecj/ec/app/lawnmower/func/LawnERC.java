/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.lawnmower.func;
import ec.*;
import ec.app.lawnmower.*;
import ec.gp.*;
import ec.util.*;
import java.io.*;

/* 
 * LawnERC.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class LawnERC extends ERC
    {
    public int maxx;
    public int maxy;

    public int x;
    public int y;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        // figure the coordinate base -- this will break if the underlying
        // base changes, oops
        Parameter newbase = 
            new Parameter(EvolutionState.P_EVALUATOR).push(Evaluator.P_PROBLEM);

        // obviously not using the default base for any of this stuff

        // load our map coordinates
        maxx = state.parameters.getInt(newbase.push(Lawnmower.P_X),null,1);
        if (maxx==0)
            state.output.error("The width (x dimension) of the lawn must be >0",
                               newbase.push(Lawnmower.P_X));
        maxy = state.parameters.getInt(newbase.push(Lawnmower.P_Y),null,1);
        if (maxy==0)
            state.output.error("The length (y dimension) of the lawn must be >0",
                               newbase.push(Lawnmower.P_X));
        state.output.exitIfErrors();      
        }

    public void resetNode(final EvolutionState state, final int thread)
        {
        x = state.random[thread].nextInt(maxx);
        y = state.random[thread].nextInt(maxy);
        }

    public int nodeHashCode()
        {
        // a reasonable hash code
        return this.getClass().hashCode() + x*maxy + y;
        }

    public boolean nodeEquals(final GPNode node)
        {
        // check first to see if we're the same kind of ERC -- 
        // won't work for subclasses; in that case you'll need
        // to change this to isAssignableTo(...)
        if (this.getClass() != node.getClass()) return false;
        // now check to see if the ERCs hold the same value
        LawnERC n = (LawnERC)node;
        return (n.x==x && n.y==y);
        }

    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        x = dataInput.readInt();
        y = dataInput.readInt();
        }

    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(x);
        dataOutput.writeInt(y);
        }

    public String encode()
        { return Code.encode(x) + Code.encode(y); }

    public boolean decode(DecodeReturn dret)
        {
        // store the position and the string in case they
        // get modified by Code.java
        int pos = dret.pos;
        String data = dret.data;

        // decode
        Code.decode(dret);

        if (dret.type != DecodeReturn.T_INT) // uh oh!
            {
            // restore the position and the string; it was an error
            dret.data = data;
            dret.pos = pos;
            return false;
            }

        // store the data
        x = (int)(dret.l);

        // decode
        Code.decode(dret);

        if (dret.type != DecodeReturn.T_INT) // uh oh!
            {
            // restore the position and the string; it was an error
            dret.data = data;
            dret.pos = pos;
            return false;
            }

        // store the data
        y = (int)(dret.l);

        return true;
        }

    public String name() { return ""; } // I'm the only ERC class, this is fine

    public String toStringForHumans()
        { return "[" + x + "," + y + "]"; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        LawnmowerData rd = ((LawnmowerData)(input));
        rd.x = x;
        rd.y = y;
        }
    }



