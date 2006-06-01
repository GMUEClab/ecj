/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.*;
import ec.util.*;
import ec.gp.*;

/* 
 * CrossoverPipeline.java
 * 
 * Created: Mon Aug 30 19:15:21 1999
 * By: Sean Luke
 */


/**
 * CrossoverPipeline is a GPBreedingPipeline which performs a strongly-typed
 * version of 
 * Koza-style "Subtree Crossover".  Two individuals are selected,
 * then a single tree is chosen in each such that the two trees
 * have the same GPTreeConstraints.  Then a random node is chosen
 * in each tree such that the two nodes have the same return type.
 * If by swapping subtrees at these nodes the two trees will not
 * violate maximum depth constraints, then the trees perform the
 * swap, otherwise, they repeat the hunt for random nodes.
 *
 * <p>The pipeline tries at most <i>tries</i> times to a pair
 * of random nodes BOTH with valid swap constraints.  If it
 * cannot find any such pairs after <i>tries</i> times, it 
 * uses the pair of its last attempt.  If either of the nodes in the pair
 * is valid, that node gets substituted with the other node.  Otherwise
 * an individual invalid node isn't changed at all (it's "reproduced").
 *
 * <p><b>Compatibility with constraints.</b> 
 * Since Koza-I/II only tries 1 time, and then follows this policy, this is
 * compatible with Koza.  lil-gp either tries 1 time, or tries forever.
 * Either way, this is compatible with lil-gp.  My hacked 
 * <a href="http://www.cs.umd.edu/users/seanl/gp/">lil-gp kernel</a>
 * either tries 1 time, <i>n</i> times, or forever.  This is compatible
 * as well.
 *
 * <p>This pipeline typically produces up to 2 new individuals (the two newly-
 * swapped individuals) per produce(...) call.  If the system only
 * needs a single individual, the pipeline will throw one of the
 * new individuals away.  The user can also have the pipeline always
 * throw away the second new individual instead of adding it to the population.
 * In this case, the pipeline will only typically 
 * produce 1 new individual per produce(...) call.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 is set, in which case it's simply the minimum typical number.

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid depth of a crossed-over subtree)</td></tr>
 
 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(first tree for the crossover; if parameter doesn't exist, tree is picked at random)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.1</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(second tree for the crossover; if parameter doesn't exist, tree is picked at random.  This tree <b>must</b> have the same GPTreeConstraints as <tt>tree.0</tt>, if <tt>tree.0</tt> is defined.)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != GPNodeSelector,<br>
 or String <tt>same<tt></font></td>
 <td valign=top>(GPNodeSelector for parent <i>n</i> (n is 0 or 1) If, for <tt>ns.1</tt> the value is <tt>same</tt>, then <tt>ns.1</tt> a copy of whatever <tt>ns.0</tt> is.  Note that the default version has no <i>n</i>)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.koza.xover

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class CrossoverPipeline extends GPBreedingPipeline
    {
    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_CROSSOVER = "xover";
    public static final String P_TOSS = "toss";
    public static final int INDS_PRODUCED = 2;
    public static final int NUM_SOURCES = 2;

    /** How the pipeline selects a node from individual 1 */
    public GPNodeSelector nodeselect1;

    /** How the pipeline selects a node from individual 2 */
    public GPNodeSelector nodeselect2;

    /** Is the first tree fixed?  If not, this is -1 */
    public int tree1;

    /** Is the second tree fixed?  If not, this is -1 */
    public int tree2;

    /** How many times the pipeline attempts to pick nodes until it gives up. */
    public int numTries;

    /** The deepest tree the pipeline is allowed to form.  Single terminal trees are depth 1. */
    public int maxDepth;

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;

    /** Temporary holding place for parents */
    public GPIndividual parents[];

    public CrossoverPipeline() { parents = new GPIndividual[2]; }

    public Parameter defaultBase() { return GPKozaDefaults.base().push(P_CROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        CrossoverPipeline c = (CrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.nodeselect1 = (GPNodeSelector)(nodeselect1.clone());
        c.nodeselect2 = (GPNodeSelector)(nodeselect2.clone());
        c.parents = (GPIndividual[]) parents.clone();

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();
        Parameter p = base.push(P_NODESELECTOR).push("0");
        Parameter d = def.push(P_NODESELECTOR).push("0");

        nodeselect1 = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,d, GPNodeSelector.class));
        nodeselect1.setup(state,p);

        p = base.push(P_NODESELECTOR).push("1");
        d = def.push(P_NODESELECTOR).push("1");

        if (state.parameters.exists(p,d) &&
            state.parameters.getString(p,d).equals(V_SAME))
            // can't just copy it this time; the selectors
            // use internal caches.  So we have to clone it no matter what
            nodeselect2 = (GPNodeSelector)(nodeselect1.clone());
        else
            {
            nodeselect2 = (GPNodeSelector)
                (state.parameters.getInstanceForParameter(
                    p,d, GPNodeSelector.class));
            nodeselect2.setup(state,p);
            }

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
                                           def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("GPCrossover Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth==0)
            state.output.fatal("GPCrossover Pipeline has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));

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
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
                                                       def.push(P_TOSS),false);

        }

    /** Returns 2 * minimum number of typical individuals produced by any sources, else
        1* minimum number if tossSecondParent is true. */
    public int typicalIndsProduced()
        {
        return (tossSecondParent? minChildProduction(): minChildProduction()*2);
        }

    /** Returns true if inner1 can feasibly be swapped into inner2's position. */

    public final boolean verifyPoints(final GPInitializer initializer,
                                      final GPNode inner1, final GPNode inner2)
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
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                }
            
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0; int t2=0;
            if (tree1==TREE_UNFIXED || tree2==TREE_UNFIXED) 
                {
                do
                    // pick random trees  -- their GPTreeConstraints must be the same
                    {
                    if (tree1==TREE_UNFIXED) 
                        if (parents[0].trees.length > 1)
                            t1 = state.random[thread].nextInt(parents[0].trees.length);
                        else t1 = 0;
                    else t1 = tree1;

                    if (tree2==TREE_UNFIXED) 
                        if (parents[1].trees.length>1)
                            t2 = state.random[thread].nextInt(parents[1].trees.length);
                        else t2 = 0;
                    else t2 = tree2;
                    } while (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer));
                }
            else
                {
                t1 = tree1;
                t2 = tree2;
                // make sure the constraints are okay
                if (parents[0].trees[t1].constraints(initializer) 
                    != parents[1].trees[t2].constraints(initializer)) // uh oh
                    state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
                }



            // validity results...
            boolean res1 = false;
            boolean res2 = false;
            
            
            // prepare the nodeselectors
            nodeselect1.reset();
            nodeselect2.reset();
            
            
            // pick some nodes
            
            GPNode p1=null;
            GPNode p2=null;
            
            for(int x=0;x<numTries;x++)
                {
                // pick a node in individual 1
                p1 = nodeselect1.pickNode(state,subpopulation,thread,parents[0],parents[0].trees[t1]);
                
                // pick a node in individual 2
                p2 = nodeselect2.pickNode(state,subpopulation,thread,parents[1],parents[1].trees[t2]);
                
                // check for depth and swap-compatibility limits
                res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
                if (n-(q-start)<2 || tossSecondParent) res2 = true;
                else res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
                
                // did we get something that had both nodes verified?
                // we reject if EITHER of them is invalid.  This is what lil-gp does.
                // Koza only has numTries set to 1, so it's compatible as well.
                if (res1 && res2) break;
                }

            // at this point, res1 AND res2 are valid, OR
            // either res1 OR res2 is valid and we ran out of tries, OR
            // neither res1 nor res2 is valid and we rand out of tries.
            // So now we will transfer to a tree which has res1 or res2
            // valid, otherwise it'll just get replicated.  This is
            // compatible with both Koza and lil-gp.


            // at this point I could check to see if my sources were breeding
            // pipelines -- but I'm too lazy to write that code (it's a little
            // complicated) to just swap one individual over or both over,
            // -- it might still entail some copying.  Perhaps in the future.
            // It would make things faster perhaps, not requiring all that
            // cloning.

            
            
            // Create some new individuals based on the old ones -- since
            // GPTree doesn't deep-clone, this should be just fine.  Perhaps we
            // should change this to proto off of the main species prototype, but
            // we have to then copy so much stuff over; it's not worth it.
                    
            GPIndividual j1 = (GPIndividual)(parents[0].lightClone());
            GPIndividual j2 = null;
            if (n-(q-start)>=2 && !tossSecondParent) j2 = (GPIndividual)(parents[1].lightClone());
            
            // Fill in various tree information that didn't get filled in there
            j1.trees = new GPTree[parents[0].trees.length];
            if (n-(q-start)>=2 && !tossSecondParent) j2.trees = new GPTree[parents[1].trees.length];
            
            // at this point, p1 or p2, or both, may be null.
            // If not, swap one in.  Else just copy the parent.
            
            for(int x=0;x<j1.trees.length;x++)
                {
                if (x==t1 && res1)  // we've got a tree with a kicking cross position!
                    { 
                    j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
                    j1.trees[x].child = parents[0].trees[x].child.cloneReplacing(p2,p1); 
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    j1.evaluated = false; 
                    }  // it's changed
                else 
                    {
                    j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
                    j1.trees[x].child = parents[0].trees[x].child.cloneReplacing();
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    }
                }
            
            if (n-(q-start)>=2 && !tossSecondParent) for(int x=0;x<j2.trees.length;x++)
                {
                if (x==t2 && res2)  // we've got a tree with a kicking cross position!
                    { 
                    j2.trees[x] = (GPTree)(parents[1].trees[x].lightClone());           
                    j2.trees[x].child = parents[1].trees[x].child.cloneReplacing(p1,p2); 
                    j2.trees[x].child.parent = j2.trees[x];
                    j2.trees[x].child.argposition = 0;
                    j2.evaluated = false; 
                    } // it's changed
                else 
                    {
                    j2.trees[x] = (GPTree)(parents[1].trees[x].lightClone());           
                    j2.trees[x].child = parents[1].trees[x].child.cloneReplacing();
                    j2.trees[x].child.parent = j2.trees[x];
                    j2.trees[x].child.argposition = 0;
                    }
                }
            
            // add the individuals to the population
            inds[q] = j1;
            q++;
            if (q<n+start && !tossSecondParent)
                {
                inds[q] = j2;
                q++;
                }
            }
        return n;
        }
    }
