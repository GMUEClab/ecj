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
 * MutateSwapPipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutateSwapPipeline works very similarly to the Swap algorithm
 * described in  Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98.
 *
 * <p>MutateSwapPipeline picks a random tree, then picks
 * randomly from all the swappable nodes in the tree, and swaps two of its subtrees.  
 * If its chosen tree has no swappable nodes, it repeats
 * the choose-tree process.  If after <i>tries</i> times
 * it has failed to find a tree with swappable nodes, it gives up and simply
 * copies the individual.
 *
 * <p>"Swapping" means to take a node <i>n</i>, and choose two children
 * nodes of <i>n</i>, <i>x</i> and <i>y</i>, such that <i>x</i>'s return
 * type is swap-compatible with <i>y</i>'s slot, and <i>y</i>'s return
 * type is swap-compatible with <i>x</i>'s slot.  The subtrees rooted at
 * <i>x</i> and <i>y</i> are swapped.
 *
 * <p>A "Swappable" node means a node which is capable of swapping
 * given the existing function set.  In general to swap a node <i>foo</i>,
 * it must have at least two children whose return types are type-compatible
 * with each other's slots in <i>foo</i>.
 *
 * <p>This method is very expensive in searching nodes for
 * "swappability".  However, if the number of types is 1 (the
 * GP run is typeless) then the type-constraint-checking
 * code is bypassed and the method runs a little faster.

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
 gp.breed.mutate-swap


 * @author Sean Luke
 * @version 1.0 
 */

public class MutateSwapPipeline extends GPBreedingPipeline
    {
    public static final String P_MUTATESWAP = "mutate-swap";
    public static final String P_NUM_TRIES = "tries";
    public static final int NUM_SOURCES = 1;
   
    /** The number of times the pipeline tries to build a valid mutated
        tree before it gives up and just passes on the original */
    int numTries;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_MUTATESWAP); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("MutateSwapPipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

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


    /** This very expensive method (for types) 
        might be improved in various ways I guess. */

    private boolean swappable(final GPInitializer initializer,
        final GPNode node)
        {
        if (node.children.length < 2)
            return false;  // fast check

        if (initializer.numAtomicTypes + initializer.numSetTypes == 1)
            return true;  // next fast check

        // we're typed, so now we have to check our type compatibility
        for(int x=0;x<node.constraints(initializer).childtypes.length-1;x++)
            for(int y=x+1;y<node.constraints(initializer).childtypes.length;y++)
                if (node.children[x].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[y]) &&
                    node.children[y].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[x]))
                    // whew!
                    return true;
        return false;
        }
    
    
    private void swapSomething(final GPNode node, final EvolutionState state, final int thread)
        {
        if (((GPInitializer)state.initializer).numAtomicTypes + ((GPInitializer)state.initializer).numSetTypes == 1) // typeless
            _swapSomethingTypeless(node,state,thread);
        else _swapSomething(node,state,thread);
        }

    private void _swapSomethingTypeless(final GPNode node, final EvolutionState state, final int thread)
        {
        // assumes that number of child nodes >= 2

        // pick a random first node
        int x = state.random[thread].nextInt(node.children.length);
        // pick a random second node
        int y = state.random[thread].nextInt(node.children.length-1);
        if (y >= x) y++; // adjust for first node

        // swap the nodes

        GPNode tmp = node.children[x];
        node.children[x] = node.children[y];
        node.children[y] = tmp;
        node.children[x].argposition = (byte)x;
        node.children[y].argposition = (byte)y;
        // no need to set parent -- it's the same parent of course
        }

    
    private void _swapSomething(final GPNode node, final EvolutionState state, final int thread)
        {
        int numSwappable = 0;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        for(int x=0;x<node.constraints(initializer).childtypes.length-1;x++)
            for(int y=x+1;y<node.constraints(initializer).childtypes.length;y++)
                if (node.children[x].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[y]) &&
                    node.children[y].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[x]))
                    // whew!
                    numSwappable++;

        // pick a random item to swap -- numSwappable is assumed to be > 0
        int swapItem = state.random[thread].nextInt(numSwappable);

        numSwappable=0;
        // find it

        for(int x=0;x<node.constraints(initializer).childtypes.length-1;x++)
            for(int y=x+1;y<node.constraints(initializer).childtypes.length;y++)
                if (node.children[x].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[y]) &&
                    node.children[y].constraints(initializer).returntype.compatibleWith(initializer,
                        node.constraints(initializer).childtypes[x]))
                    {
                    if (numSwappable==swapItem) // found it
                        {
                        // swap the children
                        GPNode tmp = node.children[x];
                        node.children[x] = node.children[y];
                        node.children[y] = tmp;
                        node.children[x].argposition = (byte)x;
                        node.children[y].argposition = (byte)y;
                        // no need to set parent -- it's the same parent of course
                        return;
                        }
                    else numSwappable++;
                    }
        }


    private int numSwappableNodes(final GPInitializer initializer,
        final GPNode root, int soFar)
        {
        if (swappable(initializer, root)) soFar++;
        for(int x=0;x<root.children.length;x++) 
            soFar = numSwappableNodes(initializer, root.children[x],soFar);
        return soFar;
        }


    private GPNode swappableNode;

    // sticks the node in 
    private int pickSwappableNode(final GPInitializer initializer,
        final GPNode root, int num)
        {
        if (swappable(initializer, root))
            {
            num--;
            if (num==-1)  // found it
                {
                swappableNode = root;
                return num;
                }
            }
        for(int x=0;x<root.children.length;x++)
            {
            num = pickSwappableNode(initializer, root.children[x],num);
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



        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds[q];
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutateSwapPipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
            
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
                
                // is the tree swappable?      
                GPInitializer initializer = ((GPInitializer)state.initializer);
                int numswap = numSwappableNodes(initializer, j.trees[t].child,0);
                if (numswap==0) continue; // uh oh, try again
                
                // swap the node, or if we're unsuccessful, just leave it alone
                pickSwappableNode(initializer, j.trees[t].child,state.random[thread].nextInt(numswap));
                
                // node is now in swappableNode, swap it
                swapSomething(swappableNode,state,thread);

                j.evaluated = false;
                break;
                }

            // add the new individual, replacing its previous source
            inds[q] = j;
            }
        return n;
        }
    }
