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
 * MutateDemotePipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * MutateDemotePipeline works very similarly to the DemoteNode algorithm
 * described in  Kumar Chellapilla,
 * "A Preliminary Investigation into Evolving Modular Programs without Subtree
 * Crossover", GP98, and is also similar to the "insertion" operator found in
 * Una-May O'Reilly's thesis,
 * <a href="http://www.ai.mit.edu/people/unamay/thesis.html">
 * "An Analysis of Genetic Programming"</a>.
 *
 * <p>MutateDemotePipeline tries picks a random tree, then picks
 * randomly from all the demotable nodes in the tree, and demotes one.  
 * If its chosen tree has no demotable nodes, or demoting
 * its chosen demotable node would make the tree too deep, it repeats
 * the choose-tree-then-choose-node process.  If after <i>tries</i> times
 * it has failed to find a valid tree and demotable node, it gives up and simply
 * copies the individual.
 *
 * <p>"Demotion" means to take a node <i>n</i> and insert a new node <i>m</i>
 * between <i>n</i> and <i>n</i>'s parent.  <i>n</i> becomes a child of
 * <i>m</i>; the place where it becomes a child is determined at random
 * from all the type-compatible slots of <i>m</i>.  The other child slots
 * of <i>m</i> are filled with randomly-generated terminals.  
 * Chellapilla's version of the algorithm always
 * places <i>n</i> in child slot 0 of <i>m</i>.  Because this would be
 * unneccessarily restrictive on strong typing, MutateDemotePipeline instead
 * picks the slot at random from all available valid choices.
 *
 * <p>A "Demotable" node means a node which is capable of demotion
 * given the existing function set.  In general to demote a node <i>foo</i>,
 * there must exist in the function set a nonterminal whose return type
 * is type-compatible with the child slot <i>foo</i> holds in its parent;
 * this nonterminal must also have a child slot which is type-compatible
 * with <i>foo</i>'s return type.
 *
 * <p>This method is very expensive in searching nodes for
 * "demotability".  However, if the number of types is 1 (the
 * GP run is typeless) then the type-constraint-checking
 * code is bypassed and the method runs a little faster.
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

 <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid depth of a mutated tree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 gp.breed.mutate-demote


 * @author Sean Luke
 * @version 1.0 
 */

public class MutateDemotePipeline extends GPBreedingPipeline
    {
    public static final String P_MUTATEDEMOTE = "mutate-demote";
    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final int NUM_SOURCES = 1;
    
    /** The number of times the pipeline tries to build a valid mutated
        tree before it gives up and just passes on the original */
    int numTries;

    /** The maximum depth of a mutated tree */
    int maxDepth;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_MUTATEDEMOTE); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("MutateDemotePipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));
    
        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),
            def.push(P_MAXDEPTH),1);
        if (maxDepth==0)
            state.output.fatal("The MutateDemotePipeline " + base + "has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));

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

    private boolean demotable(final GPInitializer initializer,
        final GPNode node, final GPFunctionSet set)
        {
        GPType t;

        if (node.parent instanceof GPNode)  // ugh, expensive
            t = ((GPNode)(node.parent)).constraints(initializer).childtypes[node.argposition];
        else 
            t = ((GPTree)(node.parent)).constraints(initializer).treetype;

        // Now, out of the nonterminals compatible with that return type,
        // do any also have a child compatible with that return type?  This
        // will be VERY expensive

        for(int x=0;x<set.nonterminals[t.type].length;x++)
            for(int y=0;y<set.nonterminals[t.type][x].constraints(initializer).
                    childtypes.length;y++)
                if (set.nonterminals[t.type][x].constraints(initializer).childtypes[y].
                    compatibleWith(initializer,node.constraints(initializer).returntype))
                    return true;
        return false;
        }
    

    private void demoteSomething(final GPNode node, final EvolutionState state, final int thread, final GPFunctionSet set) 
        {
        // if I have just one type, do it the easy way
        if (((GPInitializer)state.initializer).numAtomicTypes + 
            ((GPInitializer)state.initializer).numSetTypes == 1)
            _demoteSomethingTypeless(node,state,thread,set);
        // otherwise, I gotta do the dirty work
        else _demoteSomething(node,state,thread,set);
        }


    private void _demoteSomething(final GPNode node, final EvolutionState state, final int thread, final GPFunctionSet set) 
        {
        int numDemotable = 0;

        GPType t;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        if (node.parent instanceof GPNode)  // ugh, expensive
            t = ((GPNode)(node.parent)).constraints(initializer).childtypes[node.argposition];
        else 
            t = ((GPTree)(node.parent)).constraints(initializer).treetype;

        // Now, determine how many nodes we can demote this under --
        // note this doesn't select based on the total population
        // of "available child positions", but on the total population
        // of *nodes* regardless of if they have more than one possible
        // valid "child position".

        for(int x=0;x<set.nonterminals[t.type].length;x++)
            for(int y=0;y<set.nonterminals[t.type][x].constraints(initializer).
                    childtypes.length;y++)
                if (set.nonterminals[t.type][x].constraints(initializer).childtypes[y].
                    compatibleWith(initializer,node.constraints(initializer).returntype))
                    {
                    numDemotable++; break; // breaks out to enclosing for
                    }

        // pick a random item to demote -- numDemotable is assumed to be > 0
        int demoteItem = state.random[thread].nextInt(numDemotable);

        numDemotable=0;
        // find it

        for(int x=0;x<set.nonterminals[t.type].length;x++)
            for(int y=0;y<set.nonterminals[t.type][x].constraints(initializer).
                    childtypes.length;y++)
                if (set.nonterminals[t.type][x].constraints(initializer).childtypes[y].
                    compatibleWith(initializer,node.constraints(initializer).returntype))
                    {
                    if (numDemotable==demoteItem)
                        {
                        // clone the node
                        GPNode cnode = (GPNode)(set.nonterminals[t.type][x].lightClone());

                        // choose a spot to hang the old parent under
                        int numSpots=0;
                        GPType retyp = node.constraints(initializer).returntype;
                        GPType[] chityp = cnode.constraints(initializer).childtypes;

                        for(int z=0;z<cnode.children.length;z++)
                            if (chityp[z].compatibleWith(initializer,retyp))
                                numSpots++;
                        int choice = state.random[thread].nextInt(numSpots);

                        numSpots=0;
                        for(int z=0;z<cnode.children.length;z++)
                            if (chityp[z].compatibleWith(initializer,retyp))
                                {
                                if (numSpots==choice)
                                    {
                                    // demote the parent, inserting cnode
                                    cnode.parent = node.parent;
                                    cnode.argposition = node.argposition;
                                    cnode.children[z] = node;
                                    node.parent = cnode;
                                    node.argposition = (byte)z;
                                    if (cnode.parent instanceof GPNode)
                                        ((GPNode)(cnode.parent)).
                                            children[cnode.argposition] = cnode;
                                    else ((GPTree)(cnode.parent)).child = cnode;

                                    // this is important to ensure that the
                                    // demotion only happens once!  Otherwise
                                    // you'll get really nasty bugs
                                    numSpots++;  // notice no break
                                    }
                                else 
                                    {
                                    // hang a randomly-generated terminal off of cnode
                                    GPNode term = (GPNode)(set.terminals[chityp[z].type][
                                            state.random[thread].nextInt(
                                                set.terminals[chityp[z].type].length)].lightClone());
                                    cnode.children[z] = term;
                                    term.parent = cnode; // just in case
                                    term.argposition = (byte)z;  // just in case
                                    term.resetNode(state,thread);  // let it randomize itself if necessary

                                    // increase numSpots
                                    numSpots++;  // notice no break
                                    }
                                }
                            else
                                {
                                // hang a randomly-generated terminal off of cnode
                                GPNode term = (GPNode)(set.terminals[chityp[z].type][
                                        state.random[thread].nextInt(
                                            set.terminals[chityp[z].type].length)].lightClone());
                                cnode.children[z] = term;
                                term.parent = cnode; // just in case
                                term.argposition = (byte)z;  // just in case
                                term.resetNode(state,thread);  // let it randomize itself if necessary
                                }
                        return;
                        }
                    else 
                        {
                        numDemotable++; break; // breaks out to enclosing for
                        }
                    }
        // should never reach here
        throw new InternalError("Bug in demoteSomething -- should never be able to reach the end of the function");
        }



    private void _demoteSomethingTypeless(final GPNode node, final EvolutionState state, final int thread, final GPFunctionSet set) 
        {
        int numDemotable = 0;

        // since we're typeless, we can demote under any nonterminal
        numDemotable = set.nonterminals[0].length;

        // pick a random item to demote -- numDemotable is assumed to be > 0
        int demoteItem = state.random[thread].nextInt(numDemotable);

        numDemotable=0;
        // find it

        // clone the node
        GPNode cnode = (GPNode)(set.nonterminals[0][demoteItem].lightClone());
        
        GPType[] chityp = cnode.constraints(((GPInitializer)state.initializer)).childtypes;

        // choose a spot to hang the old parent under
        int choice = state.random[thread].nextInt(cnode.children.length);
        
        for(int z=0;z<cnode.children.length;z++)
            if (z==choice)
                {
                // demote the parent, inserting cnode
                cnode.parent = node.parent;
                cnode.argposition = node.argposition;
                cnode.children[z] = node;
                node.parent = cnode;
                node.argposition = (byte)z;
                if (cnode.parent instanceof GPNode)
                    ((GPNode)(cnode.parent)).
                        children[cnode.argposition] = cnode;
                else ((GPTree)(cnode.parent)).child = cnode;
                }
            else 
                {
                // hang a randomly-generated terminal off of cnode
                GPNode term = (GPNode)(
                    set.terminals[chityp[z].type][
                        state.random[thread].nextInt(
                            set.terminals[chityp[z].type].length)].lightClone());
                cnode.children[z] = term;
                term.parent = cnode; // just in case
                term.argposition = (byte)z;  // just in case
                term.resetNode(state,thread);  // let it randomize itself if necessary
                }
        }




    private int numDemotableNodes(final GPInitializer initializer,
        final GPNode root, int soFar, final GPFunctionSet set)
        {
        // if I have just one type, skip this and just return
        // the number of nonterminals in the tree
        if (initializer.numAtomicTypes + 
            initializer.numSetTypes == 1)
            return root.numNodes(GPNode.NODESEARCH_ALL);
        // otherwise, I gotta do the dirty work
        else return _numDemotableNodes(initializer,root,soFar,set);
        }


    private int _numDemotableNodes(final GPInitializer initializer,
        final GPNode root, int soFar, final GPFunctionSet set)
        {
        if (demotable(initializer,root, set)) soFar++;
        for(int x=0;x<root.children.length;x++) 
            soFar = _numDemotableNodes(initializer,root.children[x],soFar, set);
        return soFar;
        }


    private GPNode demotableNode;


    private int pickDemotableNode(final GPInitializer initializer,
        final GPNode root, int num, final GPFunctionSet set)
        {
        // if I have just one type, skip this and just 
        // the num-th nonterminal
        if (initializer.numAtomicTypes + 
            initializer.numSetTypes == 1)
            {
            demotableNode = root.nodeInPosition(num,GPNode.NODESEARCH_ALL);
            return -1; // what _pickDemotableNode() returns...
            }
        // otherwise, I gotta do the dirty work
        else return _pickDemotableNode(initializer,root,num,set);
        }
    

    // sticks the node in 
    private int _pickDemotableNode(final GPInitializer initializer,
        final GPNode root, int num, final GPFunctionSet set)
        {
        if (demotable(initializer,root, set))
            {
            num--;
            if (num==-1)  // found it
                {
                demotableNode = root;
                return num;
                }
            }
        for(int x=0;x<root.children.length;x++)
            {
            num = _pickDemotableNode(initializer, root.children[x],num,set);
            if (num==-1) break;  // someone found it
            }
        return num;     
        }
    

    /** Returns true if inner1's depth + atdepth +1 is within the depth bounds */

    private boolean verifyPoint(GPNode inner1)
        {
        // We know they're swap-compatible since we generated inner1
        // to be exactly that.  So don't bother.

        // next check to see if inner1 can be demoted
        if (inner1.depth()+inner1.atDepth()+1 > maxDepth) return false;

        // checks done!
        return true;
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


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds.get(q);
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("MutateDemotePipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
            for (int x=0;x<numTries;x++)
                {
                int t;
                // pick random tree
                if (tree==TREE_UNFIXED)
                    if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                    else t = 0;
                else t = tree;
                
                // is the tree demotable?
                int numdemote = numDemotableNodes(initializer, i.trees[t].child,0,i.trees[t].constraints(initializer).functionset);
                if (numdemote==0) continue; // uh oh, try again
                
                // demote the node, or if we're unsuccessful, just leave it alone
                pickDemotableNode(initializer, i.trees[t].child,state.random[thread].nextInt(numdemote),i.trees[t].constraints(initializer).functionset);
                
                // does this node exceed the maximum depth limits?
                if (!verifyPoint(demotableNode)) continue; // uh oh, try again
                
                // demote it
                demoteSomething(demotableNode,state,thread,i.trees[t].constraints(initializer).functionset);
                i.evaluated = false;
                break;
                }

            // add the new individual, replacing its previous source
            inds.set(q,i);
            
            }
        return n;
        }
    }
