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
 * InternalCrossoverPipeline.java
 * 
 * Created: Wed Dec 15 21:41:30 1999
 * By: Sean Luke
 */

/**
 * InternalCrossoverPipeline picks two subtrees from somewhere within an individual,
 * and crosses them over.  Before doing so, it checks to make sure that the
 * subtrees come from trees with the same tree constraints, that the subtrees
 * are swap-compatible with each other, that the new individual does not violate
 * depth constraints, and that one subtree does not contain the other.  It tries
 * <tt>tries</tt> times to find a valid subtree pair to cross over.  Failing this,
 * it just copies the individual.
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
 <td valign=top>(maximum valid depth of the crossed-over individual's trees)</td></tr>
 
 <tr><td valign=top><i>base</i>.<tt>ns.</tt>0<br>
 <font size=-1>classname, inherits and != GPNodeSelector</font></td>
 <td valign=top>(GPNodeSelector for subtree 0.  </td></tr>

 <tr><td valign=top><i>base</i>.<tt>ns.</tt>1<br>
 <font size=-1>classname, inherits and != GPNodeSelector,<br>
 or String <tt>same<tt></font></td>
 <td valign=top>(GPNodeSelector for subtree 1.  If value is <tt>same</tt>, then <tt>ns.1</tt> a copy of whatever <tt>ns.0</tt> is)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(first tree for the crossover; if parameter doesn't exist, tree is picked at random)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.1</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(second tree for the crossover; if parameter doesn't exist, tree is picked at random.  This tree <b>must</b> have the same GPTreeConstraints as <tt>tree.0</tt>, if <tt>tree.0</tt> is defined.)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 gp.breed.internal-xover

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>
 </table>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */

public class InternalCrossoverPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_INTERNALCROSSOVER = "internal-xover";
    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final int NUM_SOURCES = 1;

    
    /** How the pipeline chooses the first subtree */
    public GPNodeSelector nodeselect0;

    /** How the pipeline chooses the second subtree */
    public GPNodeSelector nodeselect1;

    /** How many times the pipeline attempts to pick nodes until it gives up. */
    public int numTries;

    /** The deepest tree the pipeline is allowed to form.  Single terminal trees are depth 1. */
    public int maxDepth;

    /** Is the first tree fixed?  If not, this is -1 */
    public int tree1;

    /** Is the second tree fixed?  If not, this is -1 */
    public int tree2;


    public Parameter defaultBase() { return GPBreedDefaults.base().push(P_INTERNALCROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        InternalCrossoverPipeline c = (InternalCrossoverPipeline)(super.clone());
        
        // deep-cloned stuff
        c.nodeselect0 = (GPNodeSelector)(nodeselect0.clone());
        c.nodeselect1 = (GPNodeSelector)(nodeselect1.clone());
        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();
        Parameter p = base.push(P_NODESELECTOR).push("0");
        Parameter d = def.push(P_NODESELECTOR).push("0");

        nodeselect0 = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,d, GPNodeSelector.class));
        nodeselect0.setup(state,p);

        p = base.push(P_NODESELECTOR).push("1");
        d = def.push(P_NODESELECTOR).push("1");

        if (state.parameters.exists(p,d) &&
            state.parameters.getString(p,d).equals(V_SAME))
            // can't just copy it this time; the selectors
            // use internal caches.  So we have to clone it no matter what
            nodeselect1 = (GPNodeSelector)(nodeselect0.clone());
        else
            {
            nodeselect1 = (GPNodeSelector)
                (state.parameters.getInstanceForParameter(
                    p,d, GPNodeSelector.class));
            nodeselect1.setup(state,p);
            }

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("InternalCrossover Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth==0)
            state.output.fatal("InternalCrossover Pipeline has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));

        tree1 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0)))
            {
            tree1 = state.parameters.getInt(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0),0);
            if (tree1==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }

        tree2 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+1),
                def.push(P_TREE).push(""+1)))
            {
            tree2 = state.parameters.getInt(base.push(P_TREE).push(""+1),
                def.push(P_TREE).push(""+1),0);
            if (tree2==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        }



    /** Returns true if inner1 and inner2 do not contain one another */
    private boolean noContainment(GPNode inner1, GPNode inner2)
        {
        GPNodeParent current = inner1;
        while(current != null && current instanceof GPNode)
            {
            if (current==inner2) return false;  // inner2 contains inner1
            current = ((GPNode)current).parent;
            }
        current = inner2;
        while(current != null && current instanceof GPNode)
            {
            if (current==inner1) return false;  // inner1 contains inner2
            current = ((GPNode)current).parent;
            }
        return true;
        }

    /** Returns true if inner1 can feasibly be swapped into inner2's position. */

    boolean verifyPoints(GPInitializer initializer, GPNode inner1, GPNode inner2)
        {
        // first check to see if inner1 is swap-compatible with inner2
        // on a type basis
        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

        // next check to see if inner1 can fit in inner2's spot
        if (inner1.depth()+inner2.atDepth() > maxDepth) return false;

        // checks done!
        return true;
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

        for(int q=start;q<n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds[q];
                    
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= i.trees.length))
                // uh oh
                state.output.fatal("Internal Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
                
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= i.trees.length))
                // uh oh
                state.output.fatal("Internal Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

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
                    j.trees[x] = (GPTree)(i.trees[x].lightClone());  // light clone
                    j.trees[x].owner = j;
                    j.trees[x].child = (GPNode)(i.trees[x].child.clone());
                    j.trees[x].child.parent = j.trees[x];
                    j.trees[x].child.argposition = 0;
                    }
                }


            int t1=0; int t2=0;
            if (tree1==TREE_UNFIXED || tree2==TREE_UNFIXED) 
                {
                do
                    // pick random trees  -- their GPTreeConstraints must be the same
                    {
                    if (tree1==TREE_UNFIXED)
                        if (i.trees.length > 1)
                            t1 = state.random[thread].nextInt(i.trees.length);
                        else t1 = 0;
                    else t1 = tree1;
                    
                    if (tree2==TREE_UNFIXED) 
                        if (i.trees.length > 1)
                            t2 = state.random[thread].nextInt(i.trees.length);
                        else t2 = 0;
                    else t2 = tree2;
                    } while (i.trees[t1].constraints(initializer) != i.trees[t2].constraints(initializer));
                }
            else
                {
                t1 = tree1;
                t2 = tree2;
                // make sure the constraints are okay
                if (i.trees[t1].constraints(initializer) 
                    != i.trees[t2].constraints(initializer)) // uh oh
                    state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
                }

            
            // prepare the nodeselectors
            nodeselect0.reset();
            nodeselect1.reset();
            
            
            // pick some nodes
            
            GPNode p1=null;
            GPNode p2=null;
            boolean res = false;

            for(int x=0;x<numTries;x++)
                {
                // pick a node in individual 1
                p1 = nodeselect0.pickNode(state,subpopulation,thread,j,j.trees[t1]);
                
                // pick a node in individual 2
                p2 = nodeselect1.pickNode(state,subpopulation,thread,j,j.trees[t2]);
                
                // make sure they're not the same node
                res = (p1!=p2 &&
                    // check for containment
                    (t1!=t2 || noContainment(p1,p2)) &&
                    // check for validity
                    verifyPoints(initializer,p1,p2) &&   // 1 goes into 2
                    verifyPoints(initializer,p2,p1));    // 2 goes into 1
                if (res) break; // got one
                }

            // if res, then it's time to cross over!
            if (res)
                {
                GPNodeParent oldparent = p1.parent;
                byte oldargposition = p1.argposition;
                p1.parent = p2.parent;
                p1.argposition = p2.argposition;
                p2.parent = oldparent;
                p2.argposition = oldargposition;
                
                if (p1.parent instanceof GPNode)
                    ((GPNode)(p1.parent)).children[p1.argposition] = p1;
                else ((GPTree)(p1.parent)).child = p1;

                if (p2.parent instanceof GPNode)
                    ((GPNode)(p2.parent)).children[p2.argposition] = p2;
                else ((GPTree)(p2.parent)).child = p2;

                j.evaluated = false;  // we've modified it
                }
            
            // add the individuals to the population
            inds[q] = j;
            }
        return n;
        }

    }
