/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.breed;
import ec.*;
import ec.util.*;
import ec.gp.*;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * MutateAllNodesPipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutateAllNodesPipeline implements the AllNodes mutation algorithm described
 * in Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98.
 *
 * <p>MutateAllNodesPipeline chooses a subtree and for each node <i>n</i>
 * in that subtree, it replaces <i>n</i> with a randomly-picked node of the same
 * arity and type constraints.  Thus the original topological structure is
 * the same but the nodes are different.
 *

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the source produces

 <p><b>Number of Sources</b><br>
 1

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns</tt>.0<br>
 <font size=-1>classname, inherits and != GPNodeSelector</font></td>
 <td valign=top>(GPNodeSelector for tree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.breed.mutate-all-nodes

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 <td>The GPNodeSelector selector</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class MutateAllNodesPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_MUTATEALLNODES = "mutate-all-nodes";
    public static final int NUM_SOURCES = 1;
    
    public static final String KEY_PARENTS = "parents";

    /** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() 
        { 
        return GPBreedDefaults.base().push(P_MUTATEALLNODES); 
        }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        MutateAllNodesPipeline c = (MutateAllNodesPipeline)(super.clone());
        
        // deep-cloned stuff
        c.nodeselect = (GPNodeSelector)(nodeselect.clone());
        return c;
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        Parameter p = base.push(P_NODESELECTOR).push(""+0);
        nodeselect = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,def.push(P_NODESELECTOR).push(""+0),
                GPNodeSelector.class));
        nodeselect.setup(state,p);

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


    /** Returns a node which is swap-compatible with returntype, and whose arguments are swap-compatible with the current children of original.  You need to clone this node. */

    private GPNode pickCompatibleNode(
        final GPNode original, final GPFunctionSet set, 
        final EvolutionState state, final GPType returntype, final int thread)
        {
        // an expensive procedure: we will linearly search for a valid node
        int numValidNodes = 0;
        
        int type = returntype.type;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        int len = original.constraints(initializer).childtypes.length;
        boolean failed;

        if (initializer.numAtomicTypes + 
            initializer.numSetTypes == 1)  // easy
            numValidNodes = set.nodesByArity[type][len].length;
        else for(int x=0;x<set.nodesByArity[type][len].length;x++) // ugh, the hard way -- nodes swap-compatible with type, and of arity len
                 {
                 failed = false;
                 for(int y=0;y<set.nodesByArity[type][len][x].constraints(initializer).childtypes.length;y++)
                     if (!set.nodesByArity[type][len][x].constraints(initializer).
                         childtypes[y].compatibleWith(initializer,original.children[y].
                             constraints(initializer).returntype))
                         { failed = true; break; }
                 if (!failed) numValidNodes++;
                 }
        
        // we must have at least success -- the node itself.  Otherwise we're
        // in deep doo-doo.

        // now pick a random node number
        int nodenum = state.random[thread].nextInt(numValidNodes);

        // find and return that node
        int prosnode = 0;
        
        if (numValidNodes == set.nodesByArity[type][len].length) // easy
            return set.nodesByArity[type][len][nodenum];
        else for(int x=0;x<set.nodesByArity[type][len].length;x++) // ugh, the hard way -- nodes swap-compatible with type, and of arity len
                 {
                 failed = false;
                 for(int y=0;y<set.nodesByArity[type][len][x].constraints(initializer).childtypes.length;y++)
                     if (!set.nodesByArity[type][len][x].constraints(initializer).
                         childtypes[y].compatibleWith(initializer,original.children[y].
                             constraints(initializer).returntype))
                         { failed = true; break; }
                 if (!failed) 
                     {
                     if (prosnode == nodenum)  // got it!
                         return set.nodesByArity[type][len][x];
                     prosnode++;
                     }
                 }

        // should never be able to get here
        throw new InternalError();  // whoops!

        }


    /** Returns a brand-new tree which is swap-compatible with returntype, created by making nodes "compatible" with the equivalent nodes in the tree rooted at original.  You need to set the parent and argumentposition of the root yourself.*/

    private GPNode generateCompatibleTree(final GPNode original, final GPFunctionSet set, final EvolutionState state, final GPType returntype, final int thread) 
        {
        // pick a new node and clone it
        GPNode node = (GPNode)(pickCompatibleNode(original,set,state,returntype,thread).lightClone());
        
        // reset it
        node.resetNode(state,thread);

        // fill in its children
        GPInitializer initializer = ((GPInitializer)state.initializer);
        for (int x=0;x<node.children.length;x++)
            {
            node.children[x] = generateCompatibleTree(original.children[x],set,state,original.constraints(initializer).childtypes[x],thread);
            node.children[x].parent = node;
            node.children[x].argposition = (byte)x;
            }
        return node;
        }



    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
                
        // grab n individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);

        
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }


        IntBag[] parentparents = null;
        IntBag[] preserveParents = null;
        if (misc!=null&&misc.get(KEY_PARENTS) != null)
            {
            preserveParents = (IntBag[])misc.get(KEY_PARENTS);
            parentparents = new IntBag[2];
            misc.put(KEY_PARENTS, parentparents);
            }

        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds.get(q);
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutateAllNodesPipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t;
            // pick random tree
            if (tree==TREE_UNFIXED)
                if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                else t = 0;
            else t = tree;
            
            // prepare the nodeselector
            nodeselect.reset();
            
            // pick a node
            
            GPNode p1=null;  // the node we pick
            GPNode p2=null;
            
            // pick a node in individual 1
            p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.trees[t]);
            
            // generate a tree with a new root but the same children,
            // which we will replace p1 with
            
            GPType type;
            type = p1.parentType(initializer);
            
            p2 = generateCompatibleTree(p1,i.trees[t].constraints(initializer).functionset,state,type,thread);
            // we'll need to set p2.argposition and p2.parent further down

            p2.parent = p1.parent;
            p2.argposition = p1.argposition;
            if (p2.parent instanceof GPNode)
                ((GPNode)(p2.parent)).children[p2.argposition] = p2;
            else ((GPTree)(p2.parent)).child = p2;
            i.evaluated = false;  // we've modified it

            // add the new individual, replacing its previous source
            inds.set(q,i);
            if (preserveParents != null)
                {
                parentparents[0].addAll(parentparents[1]);
                preserveParents[q] = new IntBag(parentparents[0]);
                }
            }
        return n;
        }
    }
