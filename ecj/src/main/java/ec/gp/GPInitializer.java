/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import java.util.Enumeration;
import java.util.Hashtable;

import ec.simple.SimpleInitializer;
import ec.util.Parameter;
import ec.EvolutionState;

/* 
 * GPInitializer.java
 * 
 * Created: Tue Oct  5 18:40:02 1999
 * By: Sean Luke
 */

/**
 * GPInitializer is a SimpleInitializer which sets up all the Cliques,
 * ( the initial
 * [tree/node]constraints, types, and function sets) for the GP system.
 * 
 * <p>Note that the Cliques must be set up in a very particular order:

 <ol><li>GPType</li><li>GPNodeConstraints</li><li>GPFunctionSets</li><li>GPTreeConstraints</li></ol>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><tt>gp.type</tt></td>
 <td>GPTypes</td></tr>
 <tr><td valign=top><tt>gp.nc</tt></td>
 <td>GPNodeConstraints</td></tr>
 <tr><td valign=top><tt>gp.tc</tt></td>
 <td>GPTreeConstraints</td></tr>
 <tr><td valign=top><tt>gp.fs</tt></td>
 <td>GPFunctionSets</td></tr>

 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class GPInitializer extends SimpleInitializer 
    {
    private static final long serialVersionUID = 1;

    // used just here, so far as I know :-)
    public static final int SIZE_OF_BYTE = 256;
    public final static String P_TYPE = "type";
    public final static String P_NODECONSTRAINTS = "nc";
    public final static String P_TREECONSTRAINTS = "tc";
    public final static String P_FUNCTIONSETS = "fs";
    public final static String P_SIZE = "size";
    public final static String P_ATOMIC = "a";
    public final static String P_SET = "s";
    
    /**
     * TODO Comment these members.
     * TODO Make clients of these members more efficient by reducing unnecessary casting.
     */
    public Hashtable typeRepository;
    public GPType[] types;
    public int numAtomicTypes;
    public int numSetTypes;
    
    public Hashtable nodeConstraintRepository;
    public GPNodeConstraints[] nodeConstraints;
    public byte numNodeConstraints;
    
    public Hashtable functionSetRepository;

    public Hashtable treeConstraintRepository;
    public GPTreeConstraints[] treeConstraints;
    public byte numTreeConstraints;
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        /**
         * TODO Move setup methods to the corresponding GP type.
         */
        // This is a good place to set up the types.  We use our own base off the
        // default GP base.  This MUST be done before loading constraints.
        setupTypes(state,GPDefaults.base().push(P_TYPE));

        // Now let's load our constraints and function sets also.
        // This is done in a very specific order, don't change it or things
        // will break.
        setupNodeConstraints(
            state,GPDefaults.base().push(P_NODECONSTRAINTS));
        setupFunctionSets(
            state,GPDefaults.base().push(P_FUNCTIONSETS));
        setupTreeConstraints(
            state,GPDefaults.base().push(P_TREECONSTRAINTS));
        }

    /** Sets up all the types, loading them from the parameter file.  This
        must be called before anything is called which refers to a type by
        name. */

    public void setupTypes(final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing GP Types");
        
        typeRepository = new Hashtable();
        numAtomicTypes = numSetTypes = 0;
        
        // How many atomic types do we have?
        int x = state.parameters.getInt(base.push(P_ATOMIC).push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of GP atomic types must be at least 1.",base.push(P_ATOMIC).push(P_SIZE));
        
        // Load our atomic types
        
        for(int y=0;y<x;y++)
            new GPAtomicType().setup(state,base.push(P_ATOMIC).push(""+y));
        
        // How many set types do we have?
        if (state.parameters.exists(base.push(P_SET).push(P_SIZE), null))
            {
            x =  state.parameters.getInt(base.push(P_SET).push(P_SIZE),null,1);
            if (x<0) 
                state.output.fatal("The number of GP set types must be at least 0.",base.push(P_SET).push(P_SIZE));
            }
        else // no set types
            x = 0;
        
        // Load our set types
        
        for(int y=0;y<x;y++)
            new GPSetType().setup(state,base.push(P_SET).push(""+y));
        
        // Postprocess the types
        postProcessTypes();
        }
    
    /** Assigns unique integers to each atomic type, and sets up compatibility
        arrays for set types.  If you add new types (heaven forbid), you
        should call this method again to get all the types set up properly. 
        However, you will have to set up the function sets again as well,
        as their arrays are based on these type numbers. */
    public void postProcessTypes()
        {
        // assign positive integers and 0 to atomic types
        int x = 0;
        Enumeration e = typeRepository.elements();
        while(e.hasMoreElements())
            {
            GPType t = (GPType)(e.nextElement());
            if (t instanceof GPAtomicType)
                { t.type = x; x++; }
            }
        
        // at this point, x holds the number of atomic types.
        numAtomicTypes = x;
        
        // assign additional positive integers to set types
        // and set up arrays for the set types
        e = typeRepository.elements();
        while(e.hasMoreElements())
            {
            GPType t = (GPType)(e.nextElement());
            if (t instanceof GPSetType)
                {
                ((GPSetType)t).postProcessSetType(numAtomicTypes);
                t.type = x; x++;
                }
            }
        
        // at this point, x holds the number of set types + atomic types
        numSetTypes = x - numAtomicTypes;
        
        // make an array for convenience.  Presently rarely used.
        types = new GPType[numSetTypes + numAtomicTypes];
        e = typeRepository.elements();
        while(e.hasMoreElements())
            {
            GPType t = (GPType)(e.nextElement());
            types[t.type] = t;
            }
        }
    
    
    /** Sets up all the GPNodeConstraints, loading them from the parameter
        file.  This must be called before anything is called which refers
        to a type by name. */
    
    public void setupNodeConstraints(
        final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing GP Node Constraints");
        
        nodeConstraintRepository = new Hashtable();
        nodeConstraints = new GPNodeConstraints[SIZE_OF_BYTE];
        numNodeConstraints = 0;
        
        // How many GPNodeConstraints do we have?
        int x = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of GP node constraints must be at least 1.",
                base.push(P_SIZE));
        
        // Load our constraints
        for (int y=0;y<x;y++)
            {
            GPNodeConstraints c;
            // Figure the constraint class
            if (state.parameters.exists(base.push(""+y), null))
                c = (GPNodeConstraints)(state.parameters.getInstanceForParameterEq(
                        base.push(""+y),null,GPNodeConstraints.class));
            else
                {
                state.output.message("No GP Node Constraints specified, assuming the default class: ec.gp.GPNodeConstraints for " +  base.push(""+y));
                c = new GPNodeConstraints();
                }
            c.setup(state,base.push(""+y));
            }
        
        // set our constraints array up
        Enumeration e = nodeConstraintRepository.elements();
        while(e.hasMoreElements())
            {
            GPNodeConstraints c = (GPNodeConstraints)(e.nextElement());
            c.constraintNumber = numNodeConstraints;
            nodeConstraints[numNodeConstraints] = c;
            numNodeConstraints++;
            }
        }
    
    
    public void setupFunctionSets(final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing GP Function Sets");
        
        functionSetRepository = new Hashtable();
        // How many GPFunctionSets do we have?
        int x = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of GPFunctionSets must be at least 1.",base.push(P_SIZE));
        
        // Load our FunctionSet
        for (int y=0;y<x;y++)
            {
            GPFunctionSet c;
            // Figure the GPFunctionSet class
            if (state.parameters.exists(base.push(""+y), null))
                c = (GPFunctionSet)(state.parameters.getInstanceForParameterEq(
                        base.push(""+y),null,GPFunctionSet.class));
            else
                {
                state.output.message("No GPFunctionSet specified, assuming the default class: ec.gp.GPFunctionSet for " + base.push(""+y));
                c = new GPFunctionSet();
                }
            c.setup(state,base.push(""+y));
            }
        }
        

    /** Sets up all the GPTreeConstraints, loading them from the parameter
        file.  This must be called before anything is called which refers
        to a type by name. */
        
    public void setupTreeConstraints(
        final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing GP Tree Constraints");
            
        treeConstraintRepository = new Hashtable();
        treeConstraints = new GPTreeConstraints[SIZE_OF_BYTE];
        numTreeConstraints = 0;
        // How many GPTreeConstraints do we have?
        int x = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of GP tree constraints must be at least 1.",base.push(P_SIZE));
            
        // Load our constraints
        for (int y=0;y<x;y++)
            {
            GPTreeConstraints c;
            // Figure the constraint class
            if (state.parameters.exists(base.push(""+y), null))
                c = (GPTreeConstraints)(state.parameters.getInstanceForParameterEq(
                        base.push(""+y),null,GPTreeConstraints.class));
            else
                {
                state.output.message("No GP Tree Constraints specified, assuming the default class: ec.gp.GPTreeConstraints for " + base.push(""+y));
                c = new GPTreeConstraints();
                }
            c.setup(state,base.push(""+y));
            }
            
        // set our constraints array up
        Enumeration e = treeConstraintRepository.elements();
        while(e.hasMoreElements())
            {
            GPTreeConstraints c = (GPTreeConstraints)(e.nextElement());
            c.constraintNumber = numTreeConstraints;
            treeConstraints[numTreeConstraints] = c;
            numTreeConstraints++;
            }
        }
    }
