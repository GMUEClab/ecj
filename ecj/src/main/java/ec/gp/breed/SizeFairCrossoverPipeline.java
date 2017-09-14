/*
  Copyright 2012 by Uday Kamath, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.gp.breed;

import ec.*;

import ec.util.*;
import ec.gp.*;

import java.util.*;

/**
 * SizeFairCrossover works similarly to one written in the paper
 * "Size Fair and Homologous Tree Genetic Programming Crossovers" by Langdon (1998). 
 
 * <p>SizeFairCrossover tries <i>tries</i> times to find a tree
 * that has at least one fair size node based on size fair or homologous 
 * implementation.  If it cannot
 * find a valid tree in <i>tries</i> times, it gives up and simply
 * copies the individual.
 
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
 
 <tr><td valign=top><i>base</i>.<tt>homologous</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(Is the implementation homologous (as opposed to size-fair)?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.breed.size-fair

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>

 </table>

 * @author Uday Kamath and Sean Luke
 * @version 1.0 
 */

public class SizeFairCrossoverPipeline extends GPBreedingPipeline 
    {
    private static final long serialVersionUID = 1;

    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_SIZEFAIR = "size-fair";
    public static final String P_TOSS = "toss";
    public static final String P_HOMOLOGOUS = "homologous";
    public static final int INDS_PRODUCED = 2;
    public static final int NUM_SOURCES = 2;
    
    public static final String KEY_PARENTS = "parents";

    /** How the pipeline selects a node from individual 1 */
    public GPNodeSelector nodeselect1;

    /** How the pipeline selects a node from individual 2 */
    public GPNodeSelector nodeselect2;

    /** Is the first tree fixed? If not, this is -1 */
    public int tree1;

    /** Is the second tree fixed? If not, this is -1 */
    public int tree2;

    /** How many times the pipeline attempts to pick nodes until it gives up. */
    public int numTries;

    /**
     * The deepest tree the pipeline is allowed to form. Single terminal trees
     * are depth 1.
     */
    public int maxDepth;

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;

    /** Temporary holding place for parents */
    public ArrayList<Individual> parents;
        
    public boolean homologous;
        

    public SizeFairCrossoverPipeline()
        {
        parents = new ArrayList<Individual>();
        }

    public Parameter defaultBase() 
        {
        return GPBreedDefaults.base().push(P_SIZEFAIR);
        }

    public int numSources() 
        {
        return NUM_SOURCES;
        }
        

    public Object clone() 
        {
        SizeFairCrossoverPipeline c = (SizeFairCrossoverPipeline) (super.clone());

        // deep-cloned stuff
        c.nodeselect1 = (GPNodeSelector) (nodeselect1.clone());
        c.nodeselect2 = (GPNodeSelector) (nodeselect2.clone());
        c.parents = new ArrayList<Individual>(parents);

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state, base);

        Parameter def = defaultBase();
        Parameter p = base.push(P_NODESELECTOR).push("0");
        Parameter d = def.push(P_NODESELECTOR).push("0");

        nodeselect1 = (GPNodeSelector) (state.parameters.getInstanceForParameter(p, d, GPNodeSelector.class));
        nodeselect1.setup(state, p);

        p = base.push(P_NODESELECTOR).push("1");
        d = def.push(P_NODESELECTOR).push("1");

        if (state.parameters.exists(p, d) && state.parameters.getString(p, d).equals(V_SAME))
            {
            // can't just copy it this time; the selectors
            // use internal caches. So we have to clone it no matter what
            nodeselect2 = (GPNodeSelector) (nodeselect1.clone());
            }
        else 
            {
            nodeselect2 = (GPNodeSelector) (state.parameters.getInstanceForParameter(p, d, GPNodeSelector.class));
            nodeselect2.setup(state, p);
            }

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES), def.push(P_NUM_TRIES), 1);
        if (numTries == 0)
            state.output.fatal("GPCrossover Pipeline has an invalid number of tries (it must be >= 1).",
                base.push(P_NUM_TRIES), def.push(P_NUM_TRIES));

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH), def.push(P_MAXDEPTH), 1);
        if (maxDepth == 0)
            state.output.fatal("GPCrossover Pipeline has an invalid maximum depth (it must be >= 1).",
                base.push(P_MAXDEPTH), def.push(P_MAXDEPTH));

        tree1 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push("" + 0), def.push(P_TREE).push("" + 0))) 
            {
            tree1 = state.parameters.getInt(base.push(P_TREE).push("" + 0), def.push(P_TREE).push("" + 0), 0);
            if (tree1 == -1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }

        tree2 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push("" + 1), def.push(P_TREE).push("" + 1))) 
            {
            tree2 = state.parameters.getInt(base.push(P_TREE).push("" + 1), def.push(P_TREE).push("" + 1), 0);
            if (tree2 == -1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS), def.push(P_TOSS), false);
        if(state.parameters.exists(base.push(P_HOMOLOGOUS), null))
            {
            //get the parameter
            homologous = state.parameters.getBoolean(base.push(P_HOMOLOGOUS), null, false);
            }
        }

    /**
     * Returns 2 * minimum number of typical individuals produced by any
     * sources, else 1* minimum number if tossSecondParent is true.
     */
    public int typicalIndsProduced() 
        {
        return (tossSecondParent ? minChildProduction() : minChildProduction() * 2);
        }

    /** Returns true if inner1 can feasibly be swapped into inner2's position. */

    public boolean verifyPoints(final GPInitializer initializer,
        final GPNode inner1, final GPNode inner2) 
        {
        // first check to see if inner1 is swap-compatible with inner2
        // on a type basis
        if (!inner1.swapCompatibleWith(initializer, inner2))
            return false;

        // next check to see if inner1 can fit in inner2's spot
        if (inner1.depth() + inner2.atDepth() > maxDepth)
            return false;

        // checks done!
        return true;
        }

    public int produce(final int min, 
        final int max, 
        final int subpopulation, 
        final ArrayList<Individual> inds,
        final EvolutionState state, 
        final int thread, 
        HashMap<String, Object> misc)
        {
        int start = inds.size();
                
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min)
            n = min;
        if (n > max)
            n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            // just load from source 0 and clone 'em
            sources[0].produce(n,n,subpopulation,inds, state,thread,misc);
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

        GPInitializer initializer = ((GPInitializer) state.initializer);

        for (int q = start; q < n + start; /* no increment */) // keep on going until we're filled up
            {
            parents.clear();
            
            // grab two individuals from our sources
            if (sources[0] == sources[1]) // grab from the same source
                sources[0].produce(2, 2, subpopulation, parents, state, thread, misc);
            else // grab from different sources
                {
                sources[0].produce(1, 1, subpopulation, parents, state, thread, misc);
                sources[1].produce(1, 1, subpopulation, parents, state, thread, misc);
                }
                        

            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1 != TREE_UNFIXED && (tree1 < 0 || tree1 >= ((GPIndividual) parents.get(0)).trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
            if (tree2 != TREE_UNFIXED && (tree2 < 0 || tree2 >= ((GPIndividual) parents.get(1)).trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");

            int t1 = 0;
            int t2 = 0;
            if (tree1 == TREE_UNFIXED || tree2 == TREE_UNFIXED) 
                {
                do
                    // pick random trees -- their GPTreeConstraints must be the same
                    {
                    if (tree1 == TREE_UNFIXED)
                        if (((GPIndividual) parents.get(0)).trees.length > 1)
                            t1 = state.random[thread].nextInt(((GPIndividual) parents.get(0)).trees.length);
                        else
                            t1 = 0;
                    else
                        t1 = tree1;

                    if (tree2 == TREE_UNFIXED)
                        if (((GPIndividual) parents.get(1)).trees.length > 1)
                            t2 = state.random[thread].nextInt(((GPIndividual) parents.get(1)).trees.length);
                        else
                            t2 = 0;
                    else
                        t2 = tree2;
                    } 
                while (((GPIndividual) parents.get(0)).trees[t1].constraints(initializer) != ((GPIndividual) parents.get(1)).trees[t2].constraints(initializer));
                } 
            else 
                {
                t1 = tree1;
                t2 = tree2;
                // make sure the constraints are okay
                if (((GPIndividual) parents.get(0)).trees[t1].constraints(initializer) != ((GPIndividual) parents.get(1)).trees[t2].constraints(initializer)) // uh oh
                    state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
                }

            boolean res1 = false;
            boolean res2 = false;
            GPTree tree2 = ((GPIndividual) parents.get(1)).trees[t2];

            // pick some nodes
            GPNode p1 = null;
            GPNode p2 = null;

            // lets walk on parent2 all nodes to get subtrees for each node, doing it once for O(N) and not O(N^2)
            // because depth etc are computed and not stored
            ArrayList nodeToSubtrees = new ArrayList();
            // also HashMap for size to List() of nodes in that size for O(1) lookup
            HashMap sizeToNodes = new HashMap();
            this.traverseTreeForDepth(tree2.child, nodeToSubtrees, sizeToNodes);
            // sort the ArrayList with comparator that sorts by subtrees
            Collections.sort(nodeToSubtrees, new Comparator() 
                {
                public int compare(Object o1, Object o2) 
                    {
                    NodeInfo node1 = (NodeInfo)o1;
                    NodeInfo node2 = (NodeInfo)o2;
                    int comparison = 0;
                    if (node1.numberOfSubTreesBeneath > node2.numberOfSubTreesBeneath)
                        comparison = 1;
                    else if (node1.numberOfSubTreesBeneath < node2.numberOfSubTreesBeneath)
                        comparison = -1;
                    else if (node1.numberOfSubTreesBeneath == node2.numberOfSubTreesBeneath)
                        comparison = 0;
                    return comparison;
                    }
                });

            for (int x = 0; x < numTries; x++) 
                {
                // pick a node in individual 1
                p1 = nodeselect1.pickNode(state, subpopulation, thread, ((GPIndividual) parents.get(0)), ((GPIndividual) parents.get(0)).trees[t1]);
                // now lets find "similar" in parent 2                          
                p2 = findFairSizeNode(nodeToSubtrees, sizeToNodes, p1,  tree2, state, thread);
                                

                // check for depth and swap-compatibility limits
                res1 = verifyPoints(initializer, p2, p1); // p2 can fill p1's spot -- order is important!
                if (n - (q - start) < 2 || tossSecondParent)
                    res2 = true;
                else
                    res2 = verifyPoints(initializer, p1, p2); // p1 can fill p2's spot --  order is important!

                // did we get something that had both nodes verified?
                // we reject if EITHER of them is invalid. This is what lil-gp
                // does.
                // Koza only has numTries set to 1, so it's compatible as well.
                if (res1 && res2)
                    break;
                }

            // at this point, res1 AND res2 are valid, OR
            // either res1 OR res2 is valid and we ran out of tries, OR
            // neither res1 nor res2 is valid and we rand out of tries.
            // So now we will transfer to a tree which has res1 or res2
            // valid, otherwise it'll just get replicated. This is
            // compatible with both Koza and lil-gp.

            // at this point I could check to see if my sources were breeding
            // pipelines -- but I'm too lazy to write that code (it's a little
            // complicated) to just swap one individual over or both over,
            // -- it might still entail some copying. Perhaps in the future.
            // It would make things faster perhaps, not requiring all that
            // cloning.

            // Create some new individuals based on the old ones -- since
            // GPTree doesn't deep-clone, this should be just fine. Perhaps we
            // should change this to proto off of the main species prototype,
            // but
            // we have to then copy so much stuff over; it's not worth it.

            GPIndividual j1 = (GPIndividual) (((GPIndividual) parents.get(0)).lightClone());
            GPIndividual j2 = null;
            if (n - (q - start) >= 2 && !tossSecondParent)
                j2 = (GPIndividual) (((GPIndividual) parents.get(1)).lightClone());

            // Fill in various tree information that didn't get filled in there
            j1.trees = new GPTree[((GPIndividual) parents.get(0)).trees.length];
            if (n - (q - start) >= 2 && !tossSecondParent)
                j2.trees = new GPTree[((GPIndividual) parents.get(1)).trees.length];

            // at this point, p1 or p2, or both, may be null.
            // If not, swap one in. Else just copy the parent.

            for (int x = 0; x < j1.trees.length; x++) 
                {
                if (x == t1 && res1) // we've got a tree with a kicking cross
                    // position!
                    {
                    j1.trees[x] = (GPTree) (((GPIndividual) parents.get(0)).trees[x].lightClone());
                    j1.trees[x].owner = j1;
                    j1.trees[x].child = ((GPIndividual) parents.get(0)).trees[x].child.cloneReplacing(p2, p1);
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    j1.evaluated = false;
                    } // it's changed
                else 
                    {
                    j1.trees[x] = (GPTree) (((GPIndividual) parents.get(0)).trees[x].lightClone());
                    j1.trees[x].owner = j1;
                    j1.trees[x].child = (GPNode) (((GPIndividual) parents.get(0)).trees[x].child.clone());
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    }
                }

            if (n - (q - start) >= 2 && !tossSecondParent)
                for (int x = 0; x < j2.trees.length; x++) 
                    {
                    if (x == t2 && res2) // we've got a tree with a kicking
                        // cross position!
                        {
                        j2.trees[x] = (GPTree) (((GPIndividual) parents.get(1)).trees[x].lightClone());
                        j2.trees[x].owner = j2;
                        j2.trees[x].child = ((GPIndividual) parents.get(1)).trees[x].child.cloneReplacing(p1, p2);
                        j2.trees[x].child.parent = j2.trees[x];
                        j2.trees[x].child.argposition = 0;
                        j2.evaluated = false;
                        } // it's changed
                    else 
                        {
                        j2.trees[x] = (GPTree) (((GPIndividual) parents.get(1)).trees[x].lightClone());
                        j2.trees[x].owner = j2;
                        j2.trees[x].child = (GPNode) (((GPIndividual) parents.get(1)).trees[x].child.clone());
                        j2.trees[x].child.parent = j2.trees[x];
                        j2.trees[x].child.argposition = 0;
                        }
                    }

            // add the individuals to the population
            // by Ermo. I think this should be add
            // inds.set(q,j1);
            // Yes -- Sean
            inds.add(j1);
            if (preserveParents != null)
                {
                parentparents[0].addAll(parentparents[1]);
                preserveParents[q] = parentparents[0];
                }

            q++;
            if (q < n + start && !tossSecondParent) 
                {
                // by Ermo. Same reason, should changed to add
                //inds.set(q,j2); 
                inds.add(j2);
                if (preserveParents != null)
                    {
                
                    preserveParents[q] = parentparents[0];
                    }

                q++;
                }
            }
        return n;
        }

    /**
     * This method finds a node using the logic given in the langdon paper.
     * @param nodeToSubtrees For Tree of Parent2 all precomputed stats about depth,subtrees etc
     * @param sizeToNodes Quick lookup for LinkedList of size to Nodes
     * @param parent1SelectedNode Node selected in parent1
     * @param tree2 Tree of parent2
     * @param state Evolution State passed for getting access to Random Object of MersenneTwiser
     * @param thread thread number
     */
    protected GPNode findFairSizeNode(ArrayList nodeToSubtrees,
        HashMap sizeToNodes,
        GPNode parent1SelectedNode,  
        GPTree tree2,
        EvolutionState state, 
        int thread)  
        {
        GPNode selectedNode = null;
        // get the size of subtrees of parent1
        int parent1SubTrees = parent1SelectedNode.numNodes(GPNode.NODESEARCH_NONTERMINALS);
        // the maximum length in mate we are looking for
        int maxmatesublen = (parent1SubTrees == 0) ? 0 : 2 * parent1SubTrees + 1;

        // lets see if for all lengths we have trees corresponding
        boolean[] mateSizeAvailable = new boolean[maxmatesublen + 1];
        // initialize the array to false
        for (int i = 0; i < maxmatesublen; i++)
            mateSizeAvailable[i] = false;
        // check for ones we have
        for (int i = 0; i < nodeToSubtrees.size(); i++) 
            {
            NodeInfo nodeInfo = (NodeInfo)nodeToSubtrees.get(i);
            // get the length of trees
            int subtree = nodeInfo.numberOfSubTreesBeneath;
            if (subtree <= maxmatesublen)
                mateSizeAvailable[subtree] = true;
            }
        // choose matesublen so mean size change=0 if possible
        int countOfPositives = 0;
        int countOfNegatives = 0;
        int sumOfPositives = 0;
        int sumOfNegatives = 0;
        int l;
        for (l = 1; l < parent1SubTrees; l++)
            if (mateSizeAvailable[l]) 
                {
                countOfNegatives++;
                sumOfNegatives += parent1SubTrees - l;
                }
        for (l = parent1SubTrees + 1; l <= maxmatesublen; l++)
            if (mateSizeAvailable[l])
                {
                countOfPositives++;
                sumOfPositives += l - parent1SubTrees;
                }
        // if they are missing use the same
        int mateSublengthSelected = 0;
        if (sumOfPositives == 0 || sumOfNegatives == 0) 
            {
            //if so then check if mate has the length and use that
            if(mateSizeAvailable[parent1SubTrees])
                {
                mateSublengthSelected = parent1SubTrees;
                }
            //else we go with zero
            }
        else 
            {
            // probability of same is dependent on do we find same sub trees
            // else 0.0
            double pzero = (mateSizeAvailable[parent1SubTrees]) ? 1.0 / parent1SubTrees : 0.0;
            // positive probability
            double ppositive = (1.0 - pzero) / (countOfPositives + ((double) (countOfNegatives * sumOfPositives) / (sumOfNegatives)));
            // negative probability
            double pnegative = (1.0 - pzero) / (countOfNegatives + ((double) (countOfPositives * sumOfNegatives) / (sumOfPositives)));
            // total probability, just for making sure math is right ;-)
            double total = countOfNegatives * pnegative + pzero + countOfPositives * ppositive;
            // putting an assert for floating point calculations, similar to what langdon does
            // assert(total<1.01&&total>.99);
            // now create a Roulette Wheel
            RouletteWheelSelector wheel = new RouletteWheelSelector(maxmatesublen);
            // add probabilities to the wheel
            // all below the length of parent node get pnegative
            // all above get ppositive and one on node gets pzero
            for (l = 1; l < parent1SubTrees; l++)
                if (mateSizeAvailable[l])
                    wheel.add(pnegative, l);
            if (mateSizeAvailable[parent1SubTrees])
                wheel.add(pzero, parent1SubTrees);
            for (l = parent1SubTrees + 1; l <= maxmatesublen; l++)
                if (mateSizeAvailable[l])
                    wheel.add(ppositive, l);
            // spin the wheel
            mateSublengthSelected = wheel.roulette(state, thread);
            }
        // now we have length chosen, but there can be many nodes with that
        //
        LinkedList listOfNodes = (LinkedList)(sizeToNodes.get(Integer.valueOf(mateSublengthSelected)));
        if(listOfNodes == null)
            {
            state.output. fatal("In SizeFairCrossoverPipeline, nodes for tree length " + mateSublengthSelected + " is null, indicates some serious error");
            }
        // in size fair we choose the elements at random for given length
        int chosenNode = 0;
        // if using fair size get random from the list
        if(!homologous)
            {
            chosenNode = state.random[thread].nextInt(listOfNodes.size());
            }
        // if homologous
        else 
            {
            if(listOfNodes.size() > 1)
                {
                GPInitializer initializer = ((GPInitializer) state.initializer);
                int currentMinDistance = Integer.MAX_VALUE;                             
                for(int i =0 ; i< listOfNodes.size(); i++)
                    {
                    // get the GP node
                    GPNode selectedMateNode = ((NodeInfo)listOfNodes.get(i)).node;
                    // now lets traverse selected and parent 1 to see divergence
                    GPNode currentMateNode = selectedMateNode;
                    GPNode currentParent1Node = parent1SelectedNode;
                    // found a match?
                    boolean foundAMatchInAncestor = false;
                    int distance =0;
                    while(currentMateNode.parent != null && 
                        currentMateNode.parent instanceof GPNode &&
                        currentParent1Node.parent != null && 
                        currentParent1Node.parent instanceof GPNode &&
                        !foundAMatchInAncestor)
                        {
                        GPNode parent1 = (GPNode)currentParent1Node.parent;
                        GPNode parent2 = (GPNode)currentMateNode.parent;
                        // if there is match between compatibility of parents break
                        if(parent1.swapCompatibleWith(initializer, parent2))
                            {
                            foundAMatchInAncestor = true;
                            break;
                            }
                        else
                            {
                            // need to go one level above of both
                            currentMateNode = parent2;
                            currentParent1Node = parent1;
                            //increment the distance
                            distance = distance +1;
                            }
                        }
                    // find the one with least distance
                    if(distance < currentMinDistance)
                        {
                        currentMinDistance = distance;
                        chosenNode = i;
                        }
                    }
                }
            // else take the first node, no choice
            }
        NodeInfo nodeInfoSelected = (NodeInfo)listOfNodes.get(chosenNode);
        selectedNode = nodeInfoSelected.node;

        return selectedNode;
        }

    /**
     * Recursively travel the tree so that depth and subtree below are computed
     * only once and can be reused later.
     * 
     * @param node
     * @param nodeToDepth
     */
    public void traverseTreeForDepth(GPNode node,
        ArrayList nodeToDepth,
        HashMap sizeToNodes) 
        {
        GPNode[] children = node.children;
        NodeInfo nodeInfo = new NodeInfo(node, node.numNodes(GPNode.NODESEARCH_NONTERMINALS));
        nodeToDepth.add(nodeInfo);
        // check to see if there is list in map for that size
        LinkedList listForSize = (LinkedList)(sizeToNodes.get(Integer.valueOf(nodeInfo.numberOfSubTreesBeneath)));
        if (listForSize == null) 
            {
            listForSize = new LinkedList();
            sizeToNodes.put(new Integer(nodeInfo.numberOfSubTreesBeneath), listForSize);
            }
        // add it to the list no matter what
        listForSize.add(nodeInfo);
        // recurse
        if (children.length > 0) 
            {
            for (int i = 0; i < children.length; i++) 
                {
                traverseTreeForDepth(children[i], nodeToDepth, sizeToNodes);
                }
            }
        }

    
   

    /**
     * Inner class to do a quick Roulette Wheel Selection
     *  
     */
    static class RouletteWheelSelector 
        {
        int[] length;
        double[] probability;
        int currentIndex = 0;
        int maxLength = 0;

        RouletteWheelSelector(int size) 
            {
            length = new int[size];
            probability =new double[size];
            }

        public void add(double currentProbability, int currentLength) 
            {
            length[currentIndex] = currentLength;
            probability[currentIndex] = currentProbability;
            currentIndex = currentIndex +1;
            if(currentLength > maxLength) maxLength = currentLength;
            }

        public int roulette(EvolutionState state, int thread)
            {
            int winner = 0;
            int selectedLength = 0;
            // accumulate
            for (int i = 1; i < currentIndex; i++)
                {
                probability[i] += probability[i-1];
                }

            int bot = 0; // binary chop search
            int top = currentIndex - 1;
            double f = state.random[thread].nextDouble() * probability[top];
            
            for(int loop =0; loop< 20; loop++) 
                {                                 
                int index = (top + bot) / 2;
                if (index > 0 && f < probability[index - 1])
                    top = index - 1;
                else if (f > probability[index])
                    bot = index + 1;
                else 
                    {
                    if (f == probability[index] && index + 1 < currentIndex)
                        winner = index + 1;
                    else
                        winner = index;
                    break;
                    }
                } 
            // check for bounds
            if(winner < 0 || winner >= currentIndex) 
                {
                state.output.fatal("roulette() method  winner " + winner + " out of range 0..." + (currentIndex-1));
                winner=0; //safe default
                }
            if(length[winner] < 1 || length[winner] > maxLength)
                {
                state.output.fatal("roulette() method " + length[winner] + " is  out of range 1..." + maxLength);
                // range is specified on creation
                return maxLength; //safe default
                }
            selectedLength = length[winner];
            return selectedLength;
            }

        }
        
    /**
     *Used for O(1) information of number of subtrees
     *
     */
    static class NodeInfo 
        {
        // numberOfSubTrees beneath
        int numberOfSubTreesBeneath;
        // actual node
        GPNode node;
                
        public NodeInfo(GPNode node, int numberOfSubtrees)
            {
            this.node = node;
            this.numberOfSubTreesBeneath = numberOfSubtrees;
            }               
                
        public void setSubtrees(int totalSubtrees)
            {
            this.numberOfSubTreesBeneath = totalSubtrees;
            }
                
        public int getSubtrees()
            {
            return numberOfSubTreesBeneath;
            }               
                                
        public GPNode getNode()
            {
            return node;
            }

        }
    }
