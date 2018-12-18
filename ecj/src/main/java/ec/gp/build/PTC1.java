/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.build;
import ec.*;
import ec.gp.*;
import ec.util.*;

/* 
 * PTC1.java
 * 
 * Created: Tue Jan 25 21:36:02 2000
 * By: Sean Luke
 */

/**
 * PTC1 implements the "Strongly-typed Probabilistic Tree Creation 1 (PTC1)" algorithm described in 
 *
 * <p>Luke, Sean. 2000. <i>Issues in Scaling Genetic Programming: Breeding Strategies, Tree Generation, and Code Bloat.</i> Ph.D. Dissertation, Department of Computer Science, University of Maryland, College Park, Maryland. 
 *
 * <p> ...and also in
 *
 * <p>Luke, Sean. 2000. Two fast tree-creation algorithms for genetic programming. In <i>IEEE Transactions on Evolutionary Computation</i> 4:3 (September 2000), 274-283. IEEE. 
 *
 * <p> Both can be found at <a href="http://www.cs.gmu.edu/~sean/papers/">http://www.cs.gmu.edu/~sean/papers/</a>
 * 
 * <p> PTC1 requires that your function set to implement PTCFunctionSetForm.  The
 * provided function set, PTCFunctionSet, does exactly this.
 *
 * <p>The Strongly-typed PTC1 algorithm is a derivative of the GROW algorithm
 * used in ec.gp.koza.GrowBuilder.  The primary differences are:
 * 
 <ul>
 <li> PTC1 guarantees that trees generated will have an <i>expected</i> (mean) tree size, provided by the user.  There is no guarantee on variance.  This is different from GROW, which doesn't give any user control at all.
 <li> PTC1 does not have a min-depth value.  In essence, PTC1's min-depth value is always set to 1.
 <li> PTC1's max-depth value should really only be used to enforce a large memory restriction.  Unlike GROW, where it's used to keep GROW from going nuts.
 <li> PTC1 has provisions for picking nonterminals with various probabilities over other nonterminals (and likewise for terminals).  To use this, tweak the PTCFunctionSetForm object.
 </ul>
 *
 * PTC1 assumes that the requested size passed to newRootedTree(...) is the <i>expected</i> size.   If the value is NOSIZEGIVEN, then PTC1 will use the expected size defined by the <tt>expected-size</tt> parameter.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>expected-size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>default expected tree size</td></tr>
 <tr><td valign=top><i>base</i>.<tt>max-depth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>maximum allowable tree depth (usually a big value)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.build.ptc1


 * @author Sean Luke
 * @version 1.0 
 */

public class PTC1 extends GPNodeBuilder 
    {
    public static final String P_PTC1 = "ptc1";
    public static final String P_EXPECTED = "expected-size";
    public static final String P_MAXDEPTH = "max-depth";

    /** The largest maximum tree depth PTC1 can specify -- should be big. */
    public int maxDepth;

    /** The default expected tree size for PTC1 */
    public int expectedSize;

    public Parameter defaultBase()
        {
        return GPBuildDefaults.base().push(P_PTC1);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        expectedSize = state.parameters.getInt(base.push(P_EXPECTED),
            def.push(P_EXPECTED),1);
        if (expectedSize < 1)
            state.output.fatal("Default expected size must be >= 1",
                base.push(P_EXPECTED),
                def.push(P_EXPECTED));

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),
            def.push(P_MAXDEPTH),1);
        if (maxDepth < 1)
            state.output.fatal("Maximum depth must be >= 1",
                base.push(P_MAXDEPTH),
                def.push(P_MAXDEPTH));
        
        }


    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        final int requestedSize)
        {
        if (!(set instanceof PTCFunctionSetForm))
            state.output.fatal("Set " + set.name + " is not of the form ec.gp.build.PTCFunctionSetForm, and so cannot be used with PTC Nodebuilders.");

        // build the tree
        if (requestedSize == NOSIZEGIVEN)  // use the default
            {
            return ptc1(state,0,type,thread,parent,argposition,
                set,(PTCFunctionSetForm)set,
                ((PTCFunctionSetForm)set).nonterminalSelectionProbabilities(expectedSize));
            }
        if (requestedSize < 1)
            state.output.fatal("etc.gp.build.PTC1 was requested to build a tree, but a requested size was given that is < 1.");
        return ptc1(state,0,type,thread,parent,argposition,
            set,(PTCFunctionSetForm)set,
            ((PTCFunctionSetForm)set).nonterminalSelectionProbabilities(requestedSize));
        }

    

    /** A private function which recursively returns a GROW tree to newRootedTree(...) */
    private GPNode ptc1(final EvolutionState state,
        final int current,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final int argposition,
        final GPFunctionSet set,
        final PTCFunctionSetForm pset, // same as set
        final double[] nonterminalSelectionProbabilities) 
        
        {
        // ptc1 can mess up if there are no available terminals for a given type.  If this occurs,
        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
        // and pick a nonterminal, violating the PTC1 size and depth contracts.  This can lead to pathological situations
        // where the system will continue to go on and on unable to stop because it can't pick a terminal,
        // resulting in running out of memory or some such.  But there are cases where we'd want to let
        // this work itself out.
        boolean triedTerminals = false;
                
        int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];          

        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure

        if ((  (current+1 >= maxDepth) ||                                                    // Now pick if we're at max depth
                !(state.random[thread].nextBoolean(nonterminalSelectionProbabilities[t])) ||  // OR if we're below p_y
                warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&         // OR if there are NO nonterminals!
            (triedTerminals = true) &&                                                       // [first set triedTerminals]
            terminals.length != 0)                                                           // AND if there are available terminals
            {
            GPNode n = (GPNode)
                terminals[RandomChoice.pickFromDistribution(
                    pset.terminalProbabilities(t),
                    state.random[thread].nextDouble())].lightClone();
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
        else  // above p_y, pick a nonterminal by q_ny probabilities
            {
            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminals and we're here because there were none!

            GPNode n = (GPNode)
                nonterminals[RandomChoice.pickFromDistribution(
                    pset.nonterminalProbabilities(t),
                    state.random[thread].nextDouble())].lightClone();
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = ptc1(state,current+1,childtypes[x],thread,n,x,set,pset,nonterminalSelectionProbabilities);
            return n;       
            }
        }
    }
