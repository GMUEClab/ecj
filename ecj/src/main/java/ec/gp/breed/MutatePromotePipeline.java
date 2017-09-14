/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.breed;
import ec.*;
import ec.util.*;
import ec.gp.*; 

/* 
 * MutatePromotePipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutatePromotePipeline works very similarly to the PromoteNode algorithm
 * described in  Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98, and is also similar to the "deletion" operator found in
 * Una-May O'Reilly's thesis,
 * <a href="http://www.ai.mit.edu/people/unamay/thesis.html">
 * "An Analysis of Genetic Programming"</a>.
 *
 * <p>MutatePromotePipeline tries <i>tries</i> times to find a tree
 * that has at least one promotable node.  It then picks randomly from
 * all the promotable nodes in the tree, and promotes one.  If it cannot
 * find a valid tree in <i>tries</i> times, it gives up and simply
 * copies the individual.
 *
 * <p>"Promotion" means to take a node <i>n</i> whose parent is <i>m</i>,
 * and replacing the subtree rooted at <i>m</i> with the subtree rooted at <i>n</i>.
 *
 * <p>A "Promotable" node means a node which is capable of promotion
 * given the existing type constraints.  In general to promote a node <i>foo</i>,
 * <i>foo</i> must have a parent node, and must be type-compatible with the
 * child slot that its parent fills.
 *

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the source produces

 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 gp.breed.mutate-promote


 * @author Sean Luke
 * @version 1.0 
 */

public class MutatePromotePipeline extends GPBreedingPipeline
    {
    public static final String P_MUTATEPROMOTE = "mutate-promote";
    public static final String P_NUM_TRIES = "tries";
    public static final int NUM_SOURCES = 1;
    
    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    /** The number of times the pipeline tries to build a valid mutated
        tree before it gives up and just passes on the original */
    int numTries;


    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_MUTATEPROMOTE); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("MutatePromotePipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        tree = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0)))
            {
            tree = state.parameters.getInt(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0),0);
            if (tree==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        }

    private boolean promotable(final GPInitializer initializer,
        final GPNode node)
        {
        // A node is promotable if:
        // 1: its parent is a GPNode
        if (!(node.parent instanceof GPNode))
            return false;
        GPNode parent = (GPNode)(node.parent);

        GPType t;
        if (parent.parent instanceof GPNode)  // ugh, expensive
            t = ((GPNode)(parent.parent)).constraints(initializer).childtypes[parent.argposition];
        else 
            t = ((GPTree)(parent.parent)).constraints(initializer).treetype;

        // 2: the node's returntype is type-compatible with its GRANDparent's return slot
        return (node.constraints(initializer).returntype.compatibleWith(initializer,t));
        }
    
    
    private void promoteSomething(final GPNode node)
        {
        // the node's parent MUST be a GPNode -- we've checked that already
        GPNode parent = (GPNode)(node.parent);

        node.parent = parent.parent;
        node.argposition = parent.argposition;
        
        if (parent.parent instanceof GPNode)
            ((GPNode)(parent.parent)).children[parent.argposition] = node;
        else ((GPTree)(parent.parent)).child = node;
        return;
        }

    private int numPromotableNodes(final GPInitializer initializer,
        final GPNode root, int soFar)
        {
        if (promotable(initializer,root)) soFar++;
        for(int x=0;x<root.children.length;x++) 
            soFar = numPromotableNodes(initializer,root.children[x],soFar);
        return soFar;
        }


    private GPNode promotableNode;

    // sticks the node in 
    private int pickPromotableNode(final GPInitializer initializer,
        final GPNode root, int num)
        {
        if (promotable(initializer,root))
            {
            num--;
            if (num==-1)  // found it
                {
                promotableNode = root;
                return num;
                }
            }
        for(int x=0;x<root.children.length;x++)
            {
            num = pickPromotableNode(initializer,root.children[x],num);
            if (num==-1) break;  // someone found it
            }
        return num;     
        }
    

    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 
        {
        // grab n individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did



        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds[q];
                    
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutatePromotePipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            GPIndividual j;
            if (sources[0] instanceof BreedingPipeline)
                // it's already a copy, so just smash the tree in
                {
                j=i;
                }
            else // need to copy it
                {
                j = (GPIndividual)(i.lightClone());
                
                // Fill in various tree information that didn't get filled in there
                j.trees = new GPTree[i.trees.length];
                
                for(int x=0;x<j.trees.length;x++)
                    {
                    j.trees[x] = (GPTree)(i.trees[x].lightClone());
                    j.trees[x].owner = j;
                    j.trees[x].child = (GPNode)(i.trees[x].child.clone());
                    j.trees[x].child.parent = j.trees[x];
                    j.trees[x].child.argposition = 0;
                    }
                }

            for (int x=0;x<numTries;x++)
                {
                int t;
                // pick random tree
                if (tree==TREE_UNFIXED)
                    if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                    else t = 0;
                else t = tree;
                
                // is the tree promotable?
                int numpromote = numPromotableNodes(initializer, j.trees[t].child,0);
                if (numpromote==0) continue; // uh oh, try again
                
                // promote the node, or if we're unsuccessful, just leave it alone
                pickPromotableNode(initializer, j.trees[t].child,state.random[thread].
                    nextInt(numpromote));
                
                // promote it
                promoteSomething(promotableNode );
                j.evaluated = false;
                break;
                }

            
            // add the new individual, replacing its previous source
            inds[q] = j;
            }
        return n;
        }
    }
