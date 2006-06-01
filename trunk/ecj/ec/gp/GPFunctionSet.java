/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import java.io.*;
import ec.*;
import ec.util.*;
import java.util.*;

/* 
 * GPFunctionSet.java
 * 
 * Created: Wed Oct 13 22:35:06 1999
 * By: Sean Luke
 */

/**
 * GPFunctionSet is a Clique which represents a set of GPNode prototypes
 * forming a standard function set for forming certain trees in individuals.
 * GPFunctionSets instances have unique names with which they're referenced by
 * GPTreeConstraints objects indicating that they're used for certain trees.
 * GPFunctionSets store their GPNode Prototypes in three hashtables,
 * one for all nodes, one for nonterminals, and one for terminals.  Each
 * hashed item is an array of GPNode objects,
 * hashed by the return type of the GPNodes in the array.
 *
 * GPFunctionSets also contain prototypical GPNode nodes which they
 * clone to form their arrays.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of function set.  Must be different from other function set instances)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of functions in the function set)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>func.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != ec.gp.GPNode</font></td>
 <td valign=top>(class of function node <i>n</i> in the set)</td></tr>

 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>func.</tt><i>n</i></td>
 <td>function node <i>n</i></td></tr>
 </table>
 
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class GPFunctionSet implements Clique
    {
    public final static String P_NAME = "name";
    public final static String P_FUNC = "func";
    public final static String P_SIZE = "size";

    /** Name of the GPFunctionSet */
    public String name;

    /** The nodes that our GPTree can use: arrays of nodes hashed by type. */
    public Hashtable nodes_h;
    /** The nodes that our GPTree can use: nodes[type][thenodes]. */
    public GPNode[][] nodes;
    /** The nonterminals our GPTree can use: arrays of nonterminals hashed by type. */
    public Hashtable nonterminals_h;
    /** The nonterminals our GPTree can use: nonterminals[type][thenodes]. */
    public GPNode[][] nonterminals;
    /** The terminals our GPTree can use: arrays of terminals hashed by type. */
    public Hashtable terminals_h;
    /** The terminals our GPTree can use: terminals[type][thenodes]. */
    public GPNode[][] terminals;


    // some convenience methods which speed up various kinds
    // of mutation operators


    /** Nodes == a given arity, that is: nodesByArity[type][arity][thenodes] */
    public GPNode[][][]nodesByArity;

    /** Nonterminals <= a given arity, that is: nonterminalsUnderArity[type][arity][thenodes] --
        this will be O(n^2).  Obviously, the number of nonterminals at arity slot 0 is 0.*/
    public GPNode[][][]nonterminalsUnderArity;

    /** Nonterminals >= a given arity, that is: nonterminalsOverArity[type][arity][thenodes] --
        this will be O(n^2).  Obviously, the number of nonterminals at arity slot 0 is all the 
        nonterminals of that type. */
    public GPNode[][][]nonterminalsOverArity;
    
    /** Returns the name. */
    public String toString() { return name; }


    /** Sets up all the GPFunctionSet, loading them from the parameter
        file.  This must be called before anything is called which refers
        to a type by name. */

    /** Sets up the arrays based on the hashtables */

    public void postProcessFunctionSet()
        {
        nodes = new GPNode[nodes_h.size()][];
        terminals = new GPNode[terminals_h.size()][];
        nonterminals = new GPNode[nonterminals_h.size()][];

        Enumeration e = nodes_h.keys();
        while(e.hasMoreElements())
            {
            GPType gpt = (GPType)(e.nextElement());
            GPNode[] gpfi = (GPNode[])(nodes_h.get(gpt));
            nodes[gpt.type] = gpfi;
            }
        e = nonterminals_h.keys();
        while(e.hasMoreElements())
            {
            GPType gpt = (GPType)(e.nextElement());
            GPNode[] gpfi = (GPNode[])(nonterminals_h.get(gpt));
            nonterminals[gpt.type] = gpfi;
            }
        e = terminals_h.keys();
        while(e.hasMoreElements())
            {
            GPType gpt = (GPType)(e.nextElement());
            GPNode[] gpfi = (GPNode[])(terminals_h.get(gpt));
            terminals[gpt.type] = gpfi;
            }

        // set up arity-based arrays
        // first, determine the maximum arity
        int max_arity=0;
        for(int x=0;x<nodes.length;x++)
            for(int y=0;y<nodes[x].length;y++)
                if (max_arity < nodes[x][y].children.length)
                    max_arity = nodes[x][y].children.length;

        // next set up the == array
        nodesByArity = new GPNode[nodes.length][max_arity+1][];
        for(int x=0;x<nodes.length;x++)
            for(int a = 0; a <= max_arity; a++)
                {
                // how many nodes do we have?
                int num_of_a = 0;
                for(int y=0;y<nodes[x].length;y++)
                    if (nodes[x][y].children.length == a) num_of_a++;
                // allocate and fill
                nodesByArity[x][a] = new GPNode[num_of_a];
                int cur_a = 0;
                for(int y=0;y<nodes[x].length;y++)
                    if (nodes[x][y].children.length == a )
                        nodesByArity[x][a][cur_a++] = nodes[x][y];
                }

        // now set up the <= nonterminals array
        nonterminalsUnderArity = new GPNode[nonterminals.length][max_arity+1][];
        for(int x=0;x<nonterminals.length;x++)
            for (int a = 0;a <= max_arity; a++)
                {
                // how many nonterminals do we have?
                int num_of_a = 0;
                for(int y=0;y<nonterminals[x].length;y++)
                    if (nonterminals[x][y].children.length <= a) num_of_a++;
                // allocate and fill
                nonterminalsUnderArity[x][a] = new GPNode[num_of_a];
                int cur_a = 0;
                for(int y=0;y<nonterminals[x].length;y++)
                    if (nonterminals[x][y].children.length <= a )
                        nonterminalsUnderArity[x][a][cur_a++] = nonterminals[x][y];
                }



        // now set up the >= nonterminals array
        nonterminalsOverArity = new GPNode[nonterminals.length][max_arity+1][];
        for(int x=0;x<nonterminals.length;x++)
            for (int a = 0;a <= max_arity; a++)
                {
                // how many nonterminals do we have?
                int num_of_a = 0;
                for(int y=0;y<nonterminals[x].length;y++)
                    if (nonterminals[x][y].children.length >= a) num_of_a++;
                // allocate and fill
                nonterminalsOverArity[x][a] = new GPNode[num_of_a];
                int cur_a = 0;
                for(int y=0;y<nonterminals[x].length;y++)
                    if (nonterminals[x][y].children.length >= a )
                        nonterminalsOverArity[x][a][cur_a++] = nonterminals[x][y];
                }

        }




    /** Must be done <i>after</i> GPType and GPNodeConstraints have been set up */

    public void setup(final EvolutionState state, final Parameter base)
        {
        // What's my name?
        name = state.parameters.getString(base.push(P_NAME),null);
        if (name==null)
            state.output.fatal("No name was given for this function set.",
                               base.push(P_NAME));
        // Register me
        GPFunctionSet old_functionset = (GPFunctionSet)(((GPInitializer)state.initializer).functionSetRepository.put(name,this));
        if (old_functionset != null)
            state.output.fatal("The GPFunctionSet \"" + name + "\" has been defined multiple times.", base.push(P_NAME));

        // How many functions do I have?
        int numFuncs = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (numFuncs < 1)
            state.output.error("The GPFunctionSet \"" + name + "\" has no functions.",
                               base.push(P_SIZE));
        
        Parameter p = base.push(P_FUNC);
        Vector tmp = new Vector();
        for(int x = 0; x < numFuncs; x++)
            {
            // load
            Parameter pp = p.push(""+x);
            GPNode gpfi = (GPNode)(state.parameters.getInstanceForParameter(
                                       pp, null, GPNode.class));
            gpfi.setup(state,pp);

            // add to my collection
            tmp.addElement(gpfi);
            }

        // Make my hash tables
        nodes_h = new Hashtable();
        terminals_h = new Hashtable();
        nonterminals_h = new Hashtable();

        // Now set 'em up according to the types in GPType

        Enumeration e = ((GPInitializer)state.initializer).typeRepository.elements();
        GPInitializer initializer = ((GPInitializer)state.initializer);
        while(e.hasMoreElements())
            {
            GPType typ = (GPType)(e.nextElement());
            
            // make vectors for the type.
            Vector nodes_v = new Vector();
            Vector terminals_v = new Vector();
            Vector nonterminals_v = new Vector();

            // add GPNodes as appropriate to each vector
            Enumeration v = tmp.elements();
            while (v.hasMoreElements())
                {
                GPNode i = (GPNode)(v.nextElement());
                if (typ.compatibleWith(initializer,i.constraints(initializer).returntype))
                    {
                    nodes_v.addElement(i);
                    if (i.children.length == 0)
                        terminals_v.addElement(i);
                    else nonterminals_v.addElement(i);
                    }
                }

            // turn nodes_h' vectors into arrays
            GPNode[] ii = new GPNode[nodes_v.size()];
            nodes_v.copyInto(ii);
            nodes_h.put(typ,ii);

            // turn terminals_h' vectors into arrays
            ii = new GPNode[terminals_v.size()];
            terminals_v.copyInto(ii);
            terminals_h.put(typ,ii);

            // turn nonterminals_h' vectors into arrays
            ii = new GPNode[nonterminals_v.size()];
            nonterminals_v.copyInto(ii);
            nonterminals_h.put(typ,ii);
            }

        // I don't check to see if the generation mechanism will be valid here
        // -- I check that in GPTreeConstraints, where I can do the weaker check
        // of going top-down through functions rather than making sure that every
        // single function has a compatible argument function (an unneccessary check)

        state.output.exitIfErrors();  // because I promised when I called n.setup(...)

        // postprocess the function set
        postProcessFunctionSet();
        }


    /** Returns the function set for a given name.
        You must guarantee that after calling functionSetFor(...) one or
        several times, you call state.output.exitIfErrors() once. */

    public static GPFunctionSet functionSetFor(final String functionSetName,
                                               final EvolutionState state)
        {
        GPFunctionSet set = (GPFunctionSet)(((GPInitializer)state.initializer).functionSetRepository.get(functionSetName));
        if (set==null)
            state.output.error("The GP function set \"" + functionSetName + "\" could not be found.");
        return set;
        }


    private void writeObject(ObjectOutputStream out) throws IOException
        {
        // this wastes an hashtable pointer, but what the heck.

        out.defaultWriteObject();
        }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        in.defaultReadObject();
        }
    }
