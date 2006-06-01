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
 * KozaNodeSelector.java
 * 
 * Created: Tue Oct 12 17:21:28 1999
 * By: Sean Luke
 */

/**
 * KozaNodeSelector is a GPNodeSelector which picks nodes in trees a-la Koza I,
 * with the addition of having a probability of always picking the root.
 * The method divides the range 0.0...1.0 into four probability areas: 

 <ul>
 <li>One area specifies that the selector must pick a terminal.
 <li>Another area specifies that the selector must pick a nonterminal (if there is one, else a terminal).
 <li>The third area specifies that the selector pick the root node.
 <li>The fourth area specifies that the selector pick any random node.
 </ul>

 * <p>The KozaNodeSelector chooses by probability between these four situations.
 * Then, based on the situation it has picked, it selects either a random 
 * terminal, nonterminal, root, or arbitrary node from the tree and returns it.
 *
 * <p>As the selector picks a node, it builds up some statistics information
 * which makes it able to pick a little faster in subsequent passes.  Thus
 * if you want to reuse this selector on another tree, you need to call
 * reset() first.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>terminals</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0,<br>
 nonterminals + terminals + root <= 1.0</font></td>
 <td valign=top>(the probability we must pick a terminal)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>nonterminals</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0,<br>
 nonterminals + terminals + root <= 1.0</font></td>
 <td valign=top>(the probability we must pick a nonterminal if possible)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>root</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0,<br>
 nonterminals + terminals + root <= 1.0</font></td>
 <td valign=top>(the probability we must pick the root)</td></tr>

 </table>

 <p><b>DefaultBase</b><br>
 gp.koza.ns

 * @author Sean Luke
 * @version 1.0 
 */

public class KozaNodeSelector implements GPNodeSelector 
    {
    public static final String P_NODESELECTOR = "ns";
    public static final String P_TERMINAL_PROBABILITY = "terminals";
    public static final String P_NONTERMINAL_PROBABILITY = "nonterminals";
    public static final String P_ROOT_PROBABILITY = "root";

    /** The probability the root must be chosen */
    public float rootProbability;
    
    /** The probability a terminal must be chosen */
    public float terminalProbability;

    /** The probability a nonterminal must be chosen. */
    public float nonterminalProbability;

    /** The number of nonterminals in the tree, -1 if unknown. */
    public int nonterminals;
    /** The number of terminals in the tree, -1 if unknown. */
    public int terminals;
    /** The number of nodes in the tree, -1 if unknown. */
    public int nodes;

    /** Used internally to look for a node.  This is threadsafe as long as
        an instance of KozaNodeSelector is used by only one thread. */
    protected GPNodeGatherer gatherer;

    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_NODESELECTOR);
        }

    public KozaNodeSelector() 
        {
        gatherer = new GPNodeGatherer(); 
        reset();
        }

    public Object clone()
        {
        try
            {
            KozaNodeSelector s = (KozaNodeSelector)(super.clone());
            // allocate a new gatherer, so we're always threadsafe
            s.gatherer = new GPNodeGatherer();
            s.reset();
            return s;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        }



    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        terminalProbability = state.parameters.getFloat(
            base.push(P_TERMINAL_PROBABILITY),
            def.push(P_TERMINAL_PROBABILITY), 0.0, 1.0);
        if (terminalProbability==-1.0)
            state.output.fatal("Invalid terminal probability for KozaNodeSelector ",
                               base.push(P_TERMINAL_PROBABILITY),
                               def.push(P_TERMINAL_PROBABILITY));
        
        nonterminalProbability = state.parameters.getFloat(
            base.push(P_NONTERMINAL_PROBABILITY), 
            def.push(P_NONTERMINAL_PROBABILITY),0.0, 1.0);
        if (nonterminalProbability==-1.0)
            state.output.fatal("Invalid nonterminal probability for KozaNodeSelector ",
                               base.push(P_NONTERMINAL_PROBABILITY), 
                               def.push(P_NONTERMINAL_PROBABILITY));

        rootProbability = state.parameters.getFloat(
            base.push(P_ROOT_PROBABILITY),
            def.push(P_ROOT_PROBABILITY),0.0, 1.0);
        
        if (rootProbability==-1.0)
            state.output.fatal("Invalid root probability for KozaNodeSelector ",
                               base.push(P_ROOT_PROBABILITY),
                               def.push(P_ROOT_PROBABILITY));

        if (rootProbability+terminalProbability+nonterminalProbability > 1.0f)
            state.output.fatal("The terminal, nonterminal, and root for KozaNodeSelector" + base + " may not sum to more than 1.0. (" + terminalProbability + " " + nonterminalProbability + " " + rootProbability + ")",base);

        reset();
        }


    public void reset()
        {
        nonterminals = terminals = nodes = -1;
        }

    public GPNode pickNode(final EvolutionState s,
                           final int subpopulation,
                           final int thread,
                           final GPIndividual ind,
                           final GPTree tree)
        {
        float rnd = s.random[thread].nextFloat();
        
        if (rnd > nonterminalProbability + terminalProbability + rootProbability)  // pick anyone
            {
            if (nodes==-1) nodes= 
                               tree.child.numNodes(GPNode.NODESEARCH_ALL);
                {
                tree.child.nodeInPosition(s.random[thread].nextInt(nodes),
                                          gatherer,
                                          GPNode.NODESEARCH_ALL);
                return gatherer.node;
                }
            }
        else if (rnd > nonterminalProbability + terminalProbability)  // pick the root
            {
            return tree.child;
            }
        else if (rnd > nonterminalProbability)  // pick terminals
            {
            if (terminals==-1) terminals = 
                                   tree.child.numNodes(GPNode.NODESEARCH_TERMINALS);
            
            tree.child.nodeInPosition(s.random[thread].nextInt(terminals),
                                      gatherer,
                                      GPNode.NODESEARCH_TERMINALS);
            return gatherer.node;
            }
        else  // pick nonterminals if you can
            {
            if (nonterminals==-1)
                nonterminals = tree.child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
            if (nonterminals > 0) // there are some nonterminals
                {
                tree.child.nodeInPosition(s.random[thread].nextInt(nonterminals),
                                          gatherer,
                                          GPNode.NODESEARCH_NONTERMINALS);
                return gatherer.node;
                }
            else // there ARE no nonterminals!  It must be the root node
                {
                return tree.child;
                }
            }
        }
    }
