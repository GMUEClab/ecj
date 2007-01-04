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
 * MutateOneNodePipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutateOneNodesPipeline implements the OneNode mutation algorithm described
 * in Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98.
 *
 * <p>MutateOneNodesPipeline chooses a single node in an individual and
 * replaces it with a randomly-chosen node of the same arity and type 
 * constraints.  Thus the original topological structure is
 * the same but that one node is different.
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
 gp.breed.mutate-one-node

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 <td>The GPNodeSelector selector</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class MutateOneNodePipeline extends GPBreedingPipeline
    {
    public static final String P_MUTATEONENODE = "mutate-one-node";
    public static final int NUM_SOURCES = 1;
    
    /** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;    

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_MUTATEONENODE); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        MutateOneNodePipeline c = (MutateOneNodePipeline)(super.clone());
        
        // deep-cloned stuff
        c.nodeselect = (GPNodeSelector)(nodeselect.clone());
        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter p = base.push(P_NODESELECTOR).push(""+0);
        Parameter def = defaultBase();

        nodeselect = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,def.push(P_NODESELECTOR).push(""+0),GPNodeSelector.class));
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


    private GPNode pickCompatibleNode(final GPNode original, final GPFunctionSet set, final EvolutionState state, final GPType returntype, final int thread)
        {
        // an expensive procedure: we will linearly search for a valid node
        int numValidNodes = 0;
        
        int type = returntype.type;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        int len = original.constraints(initializer).childtypes.length;
        boolean failed;

        if (initializer.numAtomicTypes + initializer.numSetTypes == 1)  // easy
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
        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds[q];
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutateOneNodePipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

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
            
            p2 = (GPNode)(pickCompatibleNode(p1,i.trees[t].constraints(initializer).functionset,state,type,thread)).lightClone();

            // if it's an ERC, let it set itself up
            p2.resetNode(state,thread);
            
            // p2's parent and argposition will be set automatically below

            GPIndividual j;

            if (sources[0] instanceof BreedingPipeline)
                // it's already a copy, so just smash the tree in
                {
                j=i;
                p1.replaceWith(p2);
                j.evaluated = false;
                }
            else
                {
                j = (GPIndividual)(i.lightClone());
                
                // Fill in various tree information that didn't get filled in there
                j.trees = new GPTree[i.trees.length];
                
                for(int x=0;x<j.trees.length;x++)
                    {
                    if (x==t)  // we've got a tree with a kicking cross position!
                        { 
                        j.trees[x] = (GPTree)(i.trees[x].lightClone());
                        j.trees[x].child = i.trees[x].child.cloneReplacingAtomic(p2,p1);
                        j.trees[x].child.parent = j.trees[x];
                        j.trees[x].child.argposition = 0;
                        j.evaluated = false; 
                        } // it's changed
                    else 
                        {
                        j.trees[x] = (GPTree)(i.trees[x].lightClone());
                        j.trees[x].child = i.trees[x].child.cloneReplacing();
                        j.trees[x].child.parent = j.trees[x];
                        j.trees[x].child.argposition = 0;
                        }
                    }
                }

            // add the new individual, replacing its previous source
            inds[q] = j;
            }
        return n;
        }
    }
