/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information

*/


package ec.app.gpsemantics;
import ec.app.gpsemantics.func.*;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.util.ArrayList;

/*
 * Semantic.java
 *
 */

/**
 * Implements Goldberg and O'Reilly's semantic Order and Majority
 * problems. See the README.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.gp.gpdata</font></td>
 <td valign=top>(the class for the prototypical GPData object)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * Class representing Semantic Order and Majority problems. This
 * problem has a size: typical size is 16, which means the terminals
 * are [X0, N0, ... X16, N16]. For Order, fitness is 1 for every time
 * when Xi occurs before Ni, in an inorder traversal. For Majority,
 * fitness is 1 for every time when Xi occurs more often than Ni.
 *
 * @author James McDermott
 * @version 1.0
 */

public class Semantic extends GPProblem implements SimpleProblemForm
    {

    final static String P_PROBLEM_NAME = "problem_name";
    final static String P_SIZE = "size";
    final static String P_ORDER = "Order";
    final static String P_MAJORITY = "Majority";

    String problemName;
    int problemSize;
    
    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);
        Parameter fsSize = new Parameter(GPDefaults.P_GP).push(GPInitializer.P_FUNCTIONSETS).push("" + 0).push(GPFunctionSet.P_SIZE);
        int numFuncs = state.parameters.getInt(fsSize,null,1);
        problemSize = (numFuncs - 1) / 2;
        problemName = state.parameters.getString(base.push(P_PROBLEM_NAME), base.push(P_ORDER));
        if (!problemName.equals(P_ORDER) && !problemName.equals(P_MAJORITY))
            state.output.error("The problem name is unrecognized",
                base.push(P_PROBLEM_NAME));

        System.out.println("Problem name " + problemName);
        System.out.println("Problem size " + problemSize);
        state.output.exitIfErrors();
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            // trees[0].child is the root
                
            ArrayList output = getSemanticOutput(((GPIndividual) ind).trees[0]);
                
            double score = 0.0;
            for (int i = 0; i < output.size(); i++)
                {
                SemanticNode n = (SemanticNode) output.get(i);
                if (n.value() == 'X')
                    {
                    score += 1;
                    }
                }

            SimpleFitness f = ((SimpleFitness) ind.fitness);
            f.setFitness(state, score, false);
            ind.evaluated = true;
            }
        }

    /**
     * @param t Tree to be "executed"
     * @return expressed output
     */
    ArrayList getSemanticOutput(GPTree t)
        {
        ArrayList p = new ArrayList();
        ArrayList nodes = new ArrayList();
        
        // Is there a better way to get all the nodes in a depth-first
        // traversal? Note that the paper specifies inorder traversal,
        // but since we're only getting the terminals, preorder,
        // inorder, and postorder are equivalent.
        int nterminals = t.child.numNodes(GPNode.NODESEARCH_TERMINALS);
        for (int i = 0; i < nterminals; i++)
            {
            nodes.add(t.child.nodeInPosition(i, GPNode.NODESEARCH_TERMINALS));
            }

        if (problemName.equals(P_ORDER))
            {
            // Order: first occurence counts
            for (int i = 0; i < nodes.size(); i++)
                {
                SemanticNode node = (SemanticNode) nodes.get(i);
                if (!nodeSameIndexExists(p, node.index()))
                    {
                    p.add(node);
                    }
                }
            }
        else
            {
            // Majority: most common counts
            for (int n = 0; n < problemSize; n++)
                {
                int xCount = 0;
                int nCount = 0;
                int lastXNode = -1;
                for (int i = 0; i < nodes.size(); i++)
                    {
                    SemanticNode node = (SemanticNode) nodes.get(i);
                    if (node.value() == 'X' && node.index() == n)
                        {
                        xCount += 1;
                        lastXNode = i;
                        }
                    else if (node.value() == 'N' && node.index() == n)
                        {
                        nCount += 1;
                        }
                    }
                if (xCount >= nCount && xCount > 0)
                    {
                    p.add((SemanticNode) nodes.get(lastXNode));
                    }
                }
            }
        return p;
        }

    
    /**
     * Given a list and an index, check whether a node of that index
     * exists in the list.
     *
     * @param p List of nodes
     * @param n index
     * @return whether node of index n exists in p.
     */
    boolean nodeSameIndexExists(ArrayList p, int n)
        {
        for (int i = 0; i < p.size(); i++)
            {
            if (((SemanticNode) p.get(i)).index() == n)
                {
                return true;
                }
            }
        return false;
        }

    String phenotypeToString(ArrayList p)
        {
        String retval = "";
        for (int i = 0; i < p.size(); i++) {
            retval += p.get(i).toString() + " ";
            }
        return retval;
        }

    // In one paper, there is a parameter for scaling, ie the fitness
    // contribution of each Xi can be uniform, or linearly or
    // exponentially scaled. We don't do that in this version.
    
    public void describe(
        final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum,
        final int log)
        {
        state.output.println("\n\nBest Individual: output = " + phenotypeToString(getSemanticOutput(((GPIndividual) ind).trees[0])), log);
        }
    }

