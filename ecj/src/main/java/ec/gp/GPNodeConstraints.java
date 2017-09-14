/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;

/* GPNodeConstraints.java
 * 
 * Created: Fri Aug 27 17:40:40 1999
 * By: Sean Luke
 *
 */

/**
 * A GPNodeConstraints is a Clique which defines constraint information
 * common to many different GPNode functions, namely return types,
 * child types, and number of children. 
 * GPNodeConstraints have unique names by which
 * they are identified.
 *
 * <p>In adding new things to GPNodeConstraints, you should ask yourself
 * the following questions: first, is this something that takes up too
 * much memory to store in GPNodes themselves?  second, is this something
 * that needs to be accessed very rapidly, so cannot be implemented
 * as a method call in a GPNode?  third, can this be shared among
 * different GPNodes, even ones representing different functions?

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of node constraints)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of node constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>returns</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(return type for node constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of child arguments for node constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>child.</tt><i>m</i><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of type for child argument <i>m</i> of node constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>prob</tt><br>
 <font size=-1>double &gt;= 0.0</font></td>
 <td valign=top>(auxillary probability of selection -- used by ec.gp.build.PTC1 and ec.gp.build.PTC2)</td></tr>

 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class GPNodeConstraints implements Clique
    {
    public static final int SIZE_OF_BYTE = 256;
    public final static String P_NAME = "name";
    public final static String P_RETURNS = "returns";
    public final static String P_CHILD = "child";
    public final static String P_SIZE = "size";
    public final static String P_PROBABILITY = "prob";
    public final static double DEFAULT_PROBABILITY = 1.0;

    /** Probability of selection -- an auxillary measure mostly used by PTC1/PTC2
        right now */
    public double probabilityOfSelection;

    /** The byte value of the constraints -- we can only have 256 of them */
    public byte constraintNumber;

    /** The return type for a GPNode */
    public GPType returntype;

    /** The children types for a GPNode */
    public GPType[] childtypes;

    /** The name of the GPNodeConstraints object -- this is NOT the
        name of the GPNode */
    public String name;

    public String toString() { return name; }

    /** A little memory optimization: if GPNodes have no children, they are welcome to
        use share this zero-sized array as their children array. */
    public GPNode zeroChildren[] = new GPNode[0];

    /** This must be called <i>after</i> the GPTypes have been set up. */
    public void setup(final EvolutionState state, final Parameter base)
        {
        // What's my name?
        name = state.parameters.getString(base.push(P_NAME),null);
        if (name==null)
            state.output.fatal("No name was given for this node constraints.",
                base.push(P_NAME));

        // Register me
        GPNodeConstraints old_constraints = (GPNodeConstraints)(((GPInitializer)state.initializer).nodeConstraintRepository.put(name,this));
        if (old_constraints != null)
            state.output.fatal("The GP node constraint \"" + name + "\" has been defined multiple times.", base.push(P_NAME));

        // What's my return type?
        String s = state.parameters.getString(base.push(P_RETURNS),null);
        if (s==null)
            state.output.fatal("No return type given for the GPNodeConstraints " + name, base.push(P_RETURNS));
        returntype = GPType.typeFor(s,state);

        // Load probability of selection

        if (state.parameters.exists(base.push(P_PROBABILITY),null))
            {
            double f = state.parameters.getDouble(base.push(P_PROBABILITY),null,0);
            if (f < 0)
                state.output.fatal("The probability of selection is < 0, which is not valid.",base.push(P_PROBABILITY),null);
            probabilityOfSelection = f;
            }
        else probabilityOfSelection = DEFAULT_PROBABILITY;

        // How many child types do I have?
        
        int x = state.parameters.getInt(base.push(P_SIZE),null,0);
        if (x < 0)
            state.output.fatal("The number of children types for the GPNodeConstraints " + name + " must be >= 0.", base.push(P_SIZE));

        childtypes = new GPType[x];

        Parameter p = base.push(P_CHILD);

        // Load my children
        for (x=0;x<childtypes.length;x++)
            {
            s = state.parameters.getString(p.push(""+x),null);
            if (s==null)
                state.output.fatal("Type #" + x + " is not defined for the GPNodeConstraints " + name +  ".", base.push(""+x));
            childtypes[x] = GPType.typeFor(s,state);
            }
        // ...because I promised when I called typeFor(...)
        state.output.exitIfErrors();    
        }


    /** You must guarantee that after calling constraintsFor(...) one or
        several times, you call state.output.exitIfErrors() once. */

    public static GPNodeConstraints constraintsFor(final String constraintsName,
        final EvolutionState state)
        {
        GPNodeConstraints myConstraints = (GPNodeConstraints)(((GPInitializer)state.initializer).nodeConstraintRepository.get(constraintsName));
        if (myConstraints==null)
            state.output.error("The GP node constraint \"" + constraintsName + "\" could not be found.");
        return myConstraints;
        }
    }
