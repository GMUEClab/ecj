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
 * RehangPipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * RehangPipeline picks a nonterminal node other than the root
 * and "rehangs" it as
 * a new root. Imagine if the tree were nodes connected with string.
 * Grab the new node and pick it up, letting the other nodes hang
 * underneath it as a new "root".  That's in effect what you're doing.
 *
 * <p><b>Important Note</b>: Because it must be free of any constraints
 * by nature, RehangPipeline does not work with strong typing.  You must
 * not have more than one type defined in order to use RehangPipeline.  
 *
 * <p>RehangPipeline picks a random tree, then picks randomly from
 * all the nonterminals in the tree other than the root, and rehangs the
 * chosen nonterminal 
 * as the new root. If its chosen tree has no nonterminals, it repeats
 * the choose-tree process.  If after <i>tries</i> times
 * it has failed to find a tree with nonterminals (other than the root),
 * it gives up and simply
 * copies the individual.  As you might guess, determining if a tree has
 * nonterminals is very fast, so <i>tries</i> can be pretty large with
 * little to no detriment to evolution speed.
 *
 * <p>"Rehanging" is complicated to describe.  First, you pick a random
 * child of your chosen nonterminal <i>n</i>, 
 * and remove this subtree from the tree.
 * Call this subtree <i>T</i>.  Next, you set the nonterminal as a new root; its
 * former parent <i>p</i> now fills the slot left behind by the missing subtree.
 * The <i>p</i>'s former parent <i>q</i> now fills the slot left behind by 
 * <i>n</i>.  <i>q</i>'s former parent <i>r</i> now fills the slot left behind
 * by <i>p</i>, and so on.  This proceeds all the way up to the old root, which
 * will be left with one empty slot (where its former child was that is now its new
 * parent).  This slot is then filled with <i>T</i>

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
 gp.breed.rehang


 * @author Sean Luke
 * @version 1.0 
 */

public class RehangPipeline extends GPBreedingPipeline
    {
    public static final String P_REHANG = "rehang";
    public static final String P_NUM_TRIES = "tries";
    public static final int NUM_SOURCES = 1;
   
    /** The number of times the pipeline tries to find a tree with a
        nonterminal before giving up and just copying the individual. */
    int numTries;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;

    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_REHANG); }

    public int numSources() { return NUM_SOURCES; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("RehangPipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        if (((GPInitializer)state.initializer).numAtomicTypes + ((GPInitializer)state.initializer).numSetTypes > 1)
            state.output.fatal("RehangPipeline only works when there is only one type (the system is typeless", base,def);

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



    private int numRehangableNodes(final GPNode root, int soFar)
        {
        // we don't include the tree root
        for(int x=0;x<root.children.length;x++) 
            soFar = _numRehangableNodes(root.children[x],soFar);
        return soFar;   
        }

    private int _numRehangableNodes(final GPNode root, int soFar)
        {
        if (root.children.length>0) soFar++;  // rehangable
        for(int x=0;x<root.children.length;x++) 
            soFar = _numRehangableNodes(root.children[x],soFar);
        return soFar;
        }


    private GPNode rehangableNode;

    private int pickRehangableNode(final GPNode root, int num)
        {
        // we don't include the tree root
        for(int x=0;x<root.children.length;x++)
            {
            num = _pickRehangableNode(root.children[x],num);
            if (num==-1) break;  // someone found it
            }   
        return num;     
        }

    // sticks the node in 
    private int _pickRehangableNode(final GPNode root, int num)
        {
        if (root.children.length>0)  // rehangable
            {
            num--;
            if (num==-1)  // found it
                {
                rehangableNode = root;
                return num;
                }
            }
        for(int x=0;x<root.children.length;x++)
            {
            num = _pickRehangableNode(root.children[x],num);
            if (num==-1) break;  // someone found it
            }
        return num;     
        }
    

    private void rehang(final EvolutionState state, final int thread,
        GPNode pivot, final GPNode root)
        {
        // pivot must not be root
        if (pivot==root) // uh oh
            throw new InternalError("Oops, pivot==root in ec.gp.breed.Rehang.rehang(...)");

        // snip off a random child from the pivot
        byte spot = (byte)(state.random[thread].nextInt(pivot.children.length));
        byte newSpot; byte tmpSpot;
        GPNode cut = pivot.children[spot];

        // rehang pivot as new root and set it up
        GPNode newPivot = (GPNode)(pivot.parent);       
        ((GPTree)root.parent).child = pivot;
        pivot.parent = root.parent;
        newSpot = pivot.argposition;
        pivot.argposition = 0;
        GPNode oldPivot = pivot;
        pivot = newPivot;

        // rehang the intermediate nodes
        while(pivot!=root)
            {
            newPivot = (GPNode)(pivot.parent);
            
            pivot.parent = oldPivot;
            oldPivot.children[spot] = pivot;        
            tmpSpot = pivot.argposition;
            pivot.argposition = spot;
            spot = newSpot;
            newSpot = tmpSpot;
            
            oldPivot = pivot;
            pivot = newPivot;
            }

        // rehang the root and set the cut
        pivot.parent = oldPivot;
        oldPivot.children[spot] = pivot;
        pivot.argposition = spot;
        cut.parent = pivot;
        cut.argposition = newSpot;
        pivot.children[newSpot] = cut;
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
        int n= sources[0].produce(min,max,subpopulation,inds, state,thread, misc);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }



        // now let's rehang 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds.get(q);
            
            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("RehangPipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            for (int x=0;x<numTries;x++)
                {
                int t;
                // pick random tree
                if (tree==TREE_UNFIXED)
                    if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                    else t = 0;
                else t = tree;
                
                // is the tree rehangable?              
                if (i.trees[t].child.children.length==0) continue; // uh oh, try again
                boolean rehangable = false;
                for(int y=0;y<i.trees[t].child.children.length;y++)
                    if (i.trees[t].child.children[y].children.length>0) // nonterminal
                        { rehangable = true; break; }
                if (!rehangable) continue;  // the root's children are all terminals

                int numrehang = numRehangableNodes(i.trees[t].child,0);
                pickRehangableNode(i.trees[t].child,
                    state.random[thread].nextInt(numrehang));
                
                rehang(state,thread,rehangableNode,i.trees[t].child);

                i.evaluated = false;
                }

            // add the new individual, replacing its previous source
            inds.set(q,i);
            
            }
        return n;
        }
    }
