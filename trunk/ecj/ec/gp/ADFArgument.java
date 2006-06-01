/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * ADFArgument.java
 * 
 * Created: Tue Oct 26 16:14:09 1999
 * By: Sean Luke
 */

/**
 * An ADFArgument is a GPNode which represents an ADF's 
 * <i>argument terminal</i>, its counterpart which returns argument
 * values in its associated function tree.  In lil-gp this is called an
 * ARG node.
 *
 * <p>Obviously, if you have Argument Terminals in a tree, that tree must
 * be only callable by ADFs and ADMs, otherwise the Argument Terminals
 * won't have anything to return.  Furthermore, you must make sure that
 * you don't have an Argument Terminal in a tree whose number is higher
 * than the smallest arity (number of arguments) of a calling ADF or ADM.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>arg</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(The related argument position for the ADF Argument Node in the associated ADF)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.adf-argument

 * @see ec.gp.ADF
 * @author Sean Luke
 * @version 1.0 
 */

public class ADFArgument extends GPNode 
    {
    public static final String P_ADFARGUMENT = "adf-argument";
    public final static String P_ARGUMENT = "arg";
    int argument;
    
    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_ADFARGUMENT);
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        
        // make sure we don't have any children...
        if (children.length!= 0) 
            state.output.error("Incorrect number of children for ADF Argument terminal -- should be 0.  Check the constraints.",base,def);

        argument = state.parameters.getInt(base.push(P_ARGUMENT),def.push(P_ARGUMENT),0);
        if (argument < 0)
            state.output.fatal("Argument terminal must have a positive argument number.",
                               base.push(P_ARGUMENT),def.push(P_ARGUMENT));
        }

    public String toString() { return  "ARG[" + argument + "]"; }


    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(argument);
        }
         
 
    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        argument = dataInput.readInt();
        }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        // get the current context
        ADFContext c = stack.top(0);
        if (c==null) // uh oh
            state.output.fatal("No context with which to evaluate ADFArgument terminal " +  toStringForError() +  ".  This often happens if you evaluate a tree by hand  which is supposed to only be an ADF's associated tree.");      
        c.evaluate(state,thread,input,stack,individual,problem,argument);
        }
    }
