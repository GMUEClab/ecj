/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.royaltree;
import ec.app.royaltree.func.*;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import java.io.*;
import javax.imageio.stream.*;

/*
 * RoyalTree.java
 *
 */

/**
 * RoyalTree implements Punch's RoyalTree problem. See the README.txt.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.royaltree.RoyalTreeData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the RoyalTree problem)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author James McDermott
 * @version 1.0
 */

public class RoyalTree extends GPProblem implements SimpleProblemForm
    {

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            // trees[0].child is the root
            double score = fitness(((GPIndividual) ind).trees[0].child, state);

            SimpleFitness f = ((SimpleFitness) ind.fitness);
            f.setFitness(state, score, false);
            ind.evaluated = true;
            }
        }

    double fitness(GPNode node, EvolutionState state)
        {
        double completeBonus = 2.0, partialBonus = 1.0,
            fullBonus = 2.0, penalty = 1.0 / 3;
        
        char node_fn = ((RoyalTreeNode) node).value();
        if (node_fn == 'X')
            {
            return 1.0;
            }

        double retval = 0.0;
        boolean nodeIsPerfect = true;
        for (int i = 0; i < node.children.length; i++)
            {
            GPNode child = node.children[i];
            char child_fn = ((RoyalTreeNode) child).value();
            
            if (isPerfect(node_fn, child, state))
                {
                retval += fullBonus * fitness(child, state);
                }
            else if (isSuccessor(node_fn, child_fn, state))
                {
                retval += partialBonus * fitness(child, state);
                nodeIsPerfect = false;
                }
            else
                {
                retval += penalty * fitness(child, state);
                nodeIsPerfect = false;
                }
            }
        
        // Only if every child is a perfect subtree of the appropriate
        // type does this node get completeBonus.
        if (nodeIsPerfect)
            {
            retval *= completeBonus;
            }
        return retval;
        }


    // doesn't need to be cloned
    char[] successors = new char[256];  // we assume we only have letters, and 0 means "no sucessor"
    public RoyalTree()
        {            
        String SUCCESSORS = "XABCDEFGHIJ";
        for(int i = 0; i < SUCCESSORS.length() - 1 ; i++)
            successors[SUCCESSORS.charAt(i)] = SUCCESSORS.charAt(i+1);
        }

    /**
     * @param p parent
     * @param q child
     * @return whether q is the correct "successor", eg p = B and q = A
     */
    boolean isSuccessor(char p, char q, EvolutionState state)
        {
        return successors[p] == q;
        }

    /**
     * Calculate whether the tree rooted at n is a perfect subtree
     * of the appropriate type given the current parent.
     * @param parent
     * @param n root of the sub-tree to be tested.
     * @return whether it is a perfect subtree of the right type.
     */
    boolean isPerfect(char parent, GPNode node, EvolutionState state)
        {
        char node_fn = ((RoyalTreeNode) node).value();
        if (!isSuccessor(parent, node_fn, state))
            {
            return false;
            }
        for (int i = 0; i < node.children.length; i++)
            {
            GPNode child = node.children[i];
            if (!isPerfect(node_fn, child, state))
                {
                return false;
                }
            }
        return true;
        }
    }
