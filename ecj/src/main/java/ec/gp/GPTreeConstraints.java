/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.util.*;
import java.util.Enumeration;

/* 
 * GPTreeConstraints.java
 * 
 * Created: Thu Oct  7 15:38:45 1999
 * By: Sean Luke
 */

/**
 * A GPTreeConstraints is a Clique which defines constraint information
 * common to many different GPTree trees, namely the tree type,
 * builder, and function set.  GPTreeConstraints have unique names
 * by which they are identified.
 *
 * <p>In adding new things to GPTreeConstraints, you should ask yourself
 * the following questions: first, is this something that takes up too
 * much memory to store in GPTrees themseves?  second, is this something
 * that needs to be accessed very rapidly, so cannot be implemented as
 * a method call in a GPTree?  third, can this be shared among different
 * GPTrees?
 *
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of tree constraints)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of tree constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>init</tt><br>
 <font size=-1>classname, inherits and != ec.gp.GPNodeBuilder</font></td>
 <td valign=top>(GP node builder for tree constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>returns</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(tree type for tree constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>fset</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(function set for tree constraint <i>n</i>)</td></tr>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */

public class GPTreeConstraints implements Clique
    {
    private static final long serialVersionUID = 1;

    public static final int SIZE_OF_BYTE = 256;
    public final static String P_NAME = "name";
    public final static String P_SIZE = "size";
    public final static String P_INIT = "init";
    public static final String P_RETURNS = "returns";
    public static final String P_FUNCTIONSET = "fset";

    public String name;
    
    /** The byte value of the constraints -- we can only have 256 of them */
    public byte constraintNumber;

    /** The builder for the tree */
    public GPNodeBuilder init;

    /** The type of the root of the tree */
    public GPType treetype;

    /** The function set for nodes in the tree */
    public GPFunctionSet functionset;
    
    public String toString() { return name; }

    /** This must be called <i>after</i> the GPTypes and GPFunctionSets 
        have been set up. */
    public void setup(final EvolutionState state, final Parameter base)
        {
        // What's my name?
        name = state.parameters.getString(base.push(P_NAME),null);
        if (name==null)
            state.output.fatal("No name was given for this function set.",
                base.push(P_NAME));

        // Register me
        GPTreeConstraints old_constraints = 
            (GPTreeConstraints)(((GPInitializer)state.initializer).treeConstraintRepository.put(name,this));
        if (old_constraints != null)
            state.output.fatal("The GP tree constraint \"" + name + "\" has been defined multiple times.", base.push(P_NAME));

        // Load my initializing builder
        init = (GPNodeBuilder)(state.parameters.getInstanceForParameter(base.push(P_INIT),null,GPNodeBuilder.class));
        init.setup(state,base.push(P_INIT));

        // Load my return type
        String s = state.parameters.getString(base.push(P_RETURNS),null);
        if (s==null)
            state.output.fatal("No return type given for the GPTreeConstraints " + name, base.push(P_RETURNS));
        treetype = GPType.typeFor(s,state);

        // Load my function set

        s = state.parameters.getString(base.push(P_FUNCTIONSET),null);
        if (s==null)
            state.output.fatal("No function set given for the GPTreeConstraints " + name, base.push(P_RETURNS));
        functionset = GPFunctionSet.functionSetFor(s,state);
        state.output.exitIfErrors();  // otherwise checkFunctionSetValidity might crash below

        // Determine the validity of the function set
        // the way we do that is by gathering all the types that
        // are transitively used, starting with treetype, as in:
        Hashtable typ = new Hashtable();
        checkFunctionSetValidity(state, typ, treetype);
        // next we make sure that for every one of these types,
        // there's a terminal with that return type, and *maybe*
        // a nonterminal
        Enumeration e = typ.elements();
        while (e.hasMoreElements())
            {
            GPType t = (GPType)(e.nextElement());
            GPNode[] i = functionset.nodes[t.type];
            if (i.length==0) // yeesh
                state.output.error("In function set " + functionset + " for the GPTreeConstraints " + this + ", no nodes at all are given with the return type " + t + " which is required by other functions in the function set or by the tree's return type.  This almost certainly indicates a serious typing error.", base);
            else
                {
                i = functionset.terminals[t.type];
                if (i.length==0) // uh oh
                    state.output.warning("In function set " + functionset + " for the GPTreeConstraints " + this + ", no terminals are given with the return type " + t + " which is required by other functions in the function set or by the tree's return type.  Nearly all tree-builders in ECJ require the ability to add a terminal of any type for which there is a nonterminal, and at any time.  Without terminals, your code may not work.  One common indication that a tree-builder has failed due to this problem is if you get the MersenneTwister error 'n must be positive'.", base);
                i = functionset.nonterminals[t.type];
                if (i.length==0) // uh oh
                    state.output.warning("In function set " + functionset + " for the GPTreeConstraints " + this + ", no *nonterminals* are given with the return type " + t + " which is required by other functions in the function set or by the tree's return type.  This may or may not be a problem for you.", base);
                }
            }
        state.output.exitIfErrors();
        }

    // When completed, done will hold all the types which are needed
    // in the function set -- you can then check to make sure that
    // they contain at least one terminal and (hopefully) at least
    // one nonterminal.

    private void checkFunctionSetValidity(final EvolutionState state,
        final Hashtable done, 
        final GPType type)
        {
        // put type in the hashtable -- it's being used
        done.put(type,type);
        
        // Grab the array in nodes
        GPNode[] i = functionset.nodes[type.type];

        // For each argument type in a node in i, if it's not in done,
        // then add it to done and call me on it
        GPInitializer initializer = ((GPInitializer)state.initializer);
        for (int x=0; x<i.length;x++)
            for (int y=0;y<i[x].constraints(initializer).childtypes.length;y++)
                if (done.get(i[x].constraints(initializer).childtypes[y])==null)
                    {
                    checkFunctionSetValidity(
                        state, done, i[x].constraints(initializer).childtypes[y]);
                    }
        }



    /** You must guarantee that after calling constraintsFor(...) one or
        several times, you call state.output.exitIfErrors() once. */

    public static GPTreeConstraints constraintsFor(final String constraintsName,
        final EvolutionState state)
        {
        GPTreeConstraints myConstraints = (GPTreeConstraints)(((GPInitializer)state.initializer).treeConstraintRepository.get(constraintsName));
        if (myConstraints==null)
            state.output.error("The GP tree constraint \"" + constraintsName + "\" could not be found.");
        return myConstraints;
        }
    }
