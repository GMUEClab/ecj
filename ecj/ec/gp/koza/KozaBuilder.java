/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.*;
import ec.gp.*;
import ec.util.*;

/* 
 * KozaBuilder.java
 * 
 * Created: Sun Oct 29 22:35:34 EST 2006
 * By: Sean Luke
 */

/*
  KozaBuilder is an abstract superclass of three tree builders: GROW, FULL, and RAMPED HALF-AND-HALF,
  all described in I/II.  As all three classes specify a minimum and maximum depth, these instance
  variables and setup methods appear here; but they are described in detail in the relevant subclasses
  (GrowBuilder, HalfBuilder, and FullBuilder).

  <p><b>Parameters</b><br>
  <table>
  <tr><td valign=top><i>base</i>.<tt>min-depth</tt><br>
  <font size=-1>int &gt;= 1</font></td>
  <td valign=top>(smallest "maximum" depth the builder may use for building a tree.  2 is the default.)</td></tr>

  <tr><td valign=top><i>base</i>.<tt>max-depth</tt><br>
  <font size=-1>int &gt;= <i>base</i>.<tt>min-depth</tt></font></td>
  <td valign=top>(largest "maximum" depth the builder may use for building a tree. 6 is the default.)</td></tr>
  </table>

  @author Sean Luke
  @version 1.0 
*/

public abstract class KozaBuilder extends GPNodeBuilder
    {
    public static final String P_MAXDEPTH = "max-depth";
    public static final String P_MINDEPTH = "min-depth";

    /** The largest maximum tree depth RAMPED HALF-AND-HALF can specify. */
    public int maxDepth;

    /** The smallest maximum tree depth RAMPED HALF-AND-HALF can specify. */
    public int minDepth;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        // load maxdepth and mindepth, check that maxdepth>0, mindepth>0, maxdepth>=mindepth
        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth<=0)
            state.output.fatal("The Max Depth for a KozaBuilder must be at least 1.",
                               base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        
        minDepth = state.parameters.getInt(base.push(P_MINDEPTH),def.push(P_MINDEPTH),1);
        if (minDepth<=0)
            state.output.fatal("The Max Depth for a KozaBuilder must be at least 1.",
                               base.push(P_MINDEPTH),def.push(P_MINDEPTH));

        if (maxDepth<minDepth)
            state.output.fatal("Max Depth must be >= Min Depth for a KozaBuilder",
                               base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        }
                
    /** A private recursive method which builds a FULL-style tree for newRootedTree(...) */
    protected GPNode fullNode(final EvolutionState state,
                              final int current,
                              final int max,
                              final GPType type,
                              final int thread,
                              final GPNodeParent parent,
                              final int argposition,
                              final GPFunctionSet set) 
        {
        // fullNode can mess up if there are no available terminals for a given type.  If this occurs,
        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
        // and pick a nonterminal, violating the "FULL" contract.  This can lead to pathological situations
        // where the system will continue to go on and on unable to stop because it can't pick a terminal,
        // resulting in running out of memory or some such.  But there are cases where we'd want to let
        // this work itself out.
        boolean triedTerminals = false;   // did we try -- and fail -- to fetch a terminal?
        
        int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];          

        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure

        // pick a terminal when we're at max depth or if there are NO nonterminals
        if ((  current+1 >= max ||                                                      // Now pick if we're at max depth
               warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&     // OR if there are NO nonterminals!
            (triedTerminals = true) &&                                                  // [first set triedTerminals]
            terminals.length != 0)                                                      // AND if there are available terminals
            {
            GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
                        
        // else force a nonterminal unless we have no choice
        else
            {
            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminals and we're here because there were none!
                                
            GPNode[] nodesToPick = set.nonterminals[type.type];
            if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

            GPNode n = (GPNode)(nodesToPick[state.random[thread].nextInt(nodesToPick.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = fullNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
            }
        }

    /** A private function which recursively returns a GROW tree to newRootedTree(...) */
    protected GPNode growNode(final EvolutionState state,
                              final int current,
                              final int max,
                              final GPType type,
                              final int thread,
                              final GPNodeParent parent,
                              final int argposition,
                              final GPFunctionSet set) 
        {
        // growNode can mess up if there are no available terminals for a given type.  If this occurs,
        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
        // and pick a nonterminal, violating the maximum-depth contract.  This can lead to pathological situations
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

        // pick a terminal when we're at max depth or if there are NO nonterminals
        if ((current+1 >= max) &&                                                       // Now pick if we're at max depth
            (triedTerminals = true) &&                                                  // [first set triedTerminals]
            terminals.length != 0)                                                      // AND if there are available terminals
            {
            GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
                        
        // else pick a random node
        else
            {
            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminals and we're here because there were none!

            GPNode n = (GPNode)(nodes[state.random[thread].nextInt(nodes.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = growNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
            }
        }

    }
