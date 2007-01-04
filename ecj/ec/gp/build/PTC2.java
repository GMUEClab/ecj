/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.build;
import ec.*;
import ec.gp.*;
import ec.util.*;

/* 
 * PTC2.java
 * 
 * Created: Tue Jan 25 21:36:02 2000
 * By: Sean Luke
 */

/**
 * PTC2 implements the "Strongly-typed Probabilistic Tree Creation 2 (PTC2)" algorithm described in 
 *
 * <p>Luke, Sean. 2000. <i>Issues in Scaling Genetic Programming: Breeding Strategies, Tree Generation, and Code Bloat.</i> Ph.D. Dissertation, Department of Computer Science, University of Maryland, College Park, Maryland. 
 *
 * <p> ...and also in
 *
 * <p>Luke, Sean. 2000. Two fast tree-creation algorithms for genetic programming. In <i>IEEE Transactions on Evolutionary Computation</i> 4:3 (September 2000), 274-283. IEEE. 
 *
 * <p> Both can be found at <a href="http://www.cs.gmu.edu/~sean/papers/">http://www.cs.gmu.edu/~sean/papers/</a>
 * 
 * <p> PTC2 requires that your function set to implement PTCFunctionSetForm.  The
 * provided function set, PTCFunctionSet, does exactly this.
 *
 * <p>The Strongly-typed PTC2 algorithm roughly works as follows: 
 * the user provides a requested tree size, and PTC2 attempts to build
 * a tree of that size or that size plus the maximum arity of a nonterminal
 * in the function set.  PTC2 works roughly like this:
 *
 <ol><li>If the tree size requested is 1, pick a random terminal and return it.
 <li> Else pick a random nonterminal as the root and put each of its unfilled child positions into the queue <i>Q</i>.
 <li> Loop until the size of <i>Q</i>, plus the size of the nodes in the tree so far, equals or exceeds the requested tree size:
 <ol><li>Remove a random position from <i>Q</i>.
 <li>Fill the position with a random nonterminal <i>n</i>.
 <li>Put each of </i>n's</i> unfilled child positions into <i>Q</i>.
 </ol>
 <li>For each position in <i>Q</i>, fill the position with a randomly-chosen terminal.
 </ol>
 *
 * <p> Generally speaking, PTC2 picks a random position in the horizon of the tree (unfiled child node positions), fills it with a nonterminal, thus extending the horizon, and repeats this until the number of nodes (nonterminals) in the tree, plus the number of unfilled node positions, is >= the requested tree size.  Then the remaining horizon is filled with terminals.
 *
 * <p> The user-provided requested tree size is either provided directly to the PTC2 algorithm, or if the size is NOSIZEGIVEN, then PTC2 will pick one at random from the GPNodeBuilder probability distribution system (using either max-depth and min-depth, or using num-sizes).
 *
 * <p> PTC2 also has provisions for picking nonterminals with a certain probability over other nonterminals of the same return type (and terminals over other terminals likewise), hence its name.  To change the probability of picking various terminals or nonterminals, you modify your PTCFunctionSetForm function set.
 *
 * <p>PTC2 further has a maximum depth, which you should set to some fairly big value.  If your maximum depth is small enough that PTC2 often creates trees which bump up against it, then PTC2 will only generate terminals at that depth position.  If the depth is *really* small, it's possible that this means PTC2 will generate trees smaller than you had requested.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>max-depth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>maximum allowable tree depth (usually a big value)</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public class PTC2 extends GPNodeBuilder 
    {
    public static final String P_PTC2 = "ptc2";
    public static final String P_MAXDEPTH = "max-depth";

    /** The largest maximum tree depth GROW can specify -- should be big. */
    public int maxDepth;
    
    public Parameter defaultBase()
        {
        return GPBuildDefaults.base().push(P_PTC2);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        // we use size distributions -- did the user specify any?
        if (!canPick())
            state.output.fatal("PTC2 needs a distribution of tree sizes to pick from.  You can do this by either setting a distribution (with " + P_NUMSIZES + ") or with "
                               + P_MINSIZE + " and " + P_MAXSIZE + ".", base, def);

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),
                                           def.push(P_MAXDEPTH),1);
        if (maxDepth < 1)
            state.output.fatal("Maximum depth must be >= 1",
                               base.push(P_MAXDEPTH),
                               def.push(P_MAXDEPTH));
        }

    public final static int MIN_QUEUE_SIZE = 32;
    
    // these are all initialized in enqueue
    GPNode[] s_node;
    int[] s_argpos;
    int[] s_depth;
    int s_size;

    private void enqueue(final GPNode n, final int argpos, final int depth)
        {
        if (s_node==null) 
            {
            s_node = new GPNode[MIN_QUEUE_SIZE];
            s_argpos = new int[MIN_QUEUE_SIZE];
            s_depth = new int[MIN_QUEUE_SIZE];
            s_size = 0;
            }
        else if (s_size==s_node.length) // need to double them
            {
            GPNode[] new_s_node = new GPNode[s_size*2];
            System.arraycopy(s_node,0,new_s_node,0,s_size);
            s_node = new_s_node;
            int[] new_s_argpos = new int[s_size*2];
            System.arraycopy(s_argpos,0,new_s_argpos,0,s_size);
            s_argpos = new_s_argpos;
            int[] new_s_depth = new int[s_size*2];
            System.arraycopy(s_depth,0,new_s_depth,0,s_size);
            s_depth = new_s_depth;
            }
        
        // okay, let's boogie!
        s_node[s_size] = n;
        s_argpos[s_size] = argpos;
        s_depth[s_size] = depth;
        s_size++;
        }

    GPNode dequeue_node;
    int dequeue_argpos;
    int dequeue_depth;

    // stashes in dequeue_*
    private void randomDequeue(final EvolutionState state, final int thread)
        {
        int r = state.random[thread].nextInt(s_size);
        s_size -= 1;
        // put items r into spot dequeue_*
        dequeue_node = s_node[r];
        dequeue_argpos = s_argpos[r];
        dequeue_depth = s_depth[r];
        // put items s_size into spot r
        s_node[r] = s_node[s_size];
        s_argpos[r] = s_argpos[s_size];
        s_depth[r] = s_depth[s_size];
        }


    public GPNode newRootedTree(final EvolutionState state,
                                GPType type,
                                final int thread,
                                final GPNodeParent parent,
                                final GPFunctionSet set,
                                final int argposition,
                                int requestedSize)
        {
        // ptc2 can mess up if there are no available terminals for a given type.  If this occurs,
        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
        // and pick a nonterminal, violating the ptc2 size and depth contracts.  This can lead to pathological situations
        // where the system will continue to go on and on unable to stop because it can't pick a terminal,
        // resulting in running out of memory or some such.  But there are cases where we'd want to let
        // this work itself out.
        boolean triedTerminals = false;

        if (!(set instanceof PTCFunctionSetForm))
            state.output.fatal("Set " + set.name + " is not of the class ec.gp.build.PTCFunctionSetForm, and so cannot be used with PTC Nodebuilders.");

        PTCFunctionSetForm pset = (PTCFunctionSetForm)set;

        // pick a size from the distribution
        if (requestedSize==NOSIZEGIVEN)
            requestedSize = 
                pickSize(state,thread);

        GPNode root;

        int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];          

        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure



        // return a terminal
        if ((   requestedSize==1 ||                                                          // Now pick a terminal if our size is 1
                warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&         // OR if there are NO nonterminals!
            (triedTerminals = true) &&                                                       // [first set triedTerminals]
            terminals.length != 0)                                                           // AND if there are available terminals
            {
            root = (GPNode)
                terminals[RandomChoice.pickFromDistribution(
                              pset.terminalProbabilities(t),
                              state.random[thread].nextFloat(),CHECK_BOUNDARY)].lightClone();
            root.resetNode(state,thread);  // give ERCs a chance to randomize
            root.argposition = (byte)argposition;
            root.parent = parent;
            }
        else   // return a nonterminal-rooted tree
            {
            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminals and we're here because there were none!

            // pick a nonterminal
            root = (GPNode)
                nonterminals[RandomChoice.pickFromDistribution(
                                 pset.nonterminalProbabilities(t),
                                 state.random[thread].nextFloat(),CHECK_BOUNDARY)].lightClone();
            root.resetNode(state,thread);  // give ERCs a chance to randomize
            root.argposition = (byte)argposition;
            root.parent = parent;

            // set the depth, size, and enqueuing, and reset the random dequeue
            
            s_size=0;  // pretty critical!
            int s = 1;
            GPInitializer initializer = ((GPInitializer)state.initializer);
            GPType[] childtypes = root.constraints(initializer).childtypes;
            for(int x=0;x<childtypes.length;x++)
                enqueue(root,x,1);  /* depth 1 */
            
                        
                        
                        
            while(s_size>0)
                {
                triedTerminals = false;
                randomDequeue(state,thread);
                type = dequeue_node.constraints(initializer).childtypes[dequeue_argpos];
                
                int y = type.type;
                terminals = set.terminals[y];
                nonterminals = set.nonterminals[y];
                nodes = set.nodes[y];           

                if (nodes.length == 0)
                    errorAboutNoNodeWithType(type, state);   // total failure

                // pick a terminal 
                if ((   s_size + s >= requestedSize ||                                        // if we need no more nonterminal nodes
                        dequeue_depth==maxDepth ||                                            // OR if we're at max depth and must pick a terminal
                        warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&  // OR if there are NO nonterminals!
                    (triedTerminals = true) &&                                                // [first set triedTerminals]
                    terminals.length != 0)                                                    // AND if there are available terminals
                    {
                    GPNode n = (GPNode)
                        terminals[RandomChoice.pickFromDistribution(
                                      pset.terminalProbabilities(y),
                                      state.random[thread].nextFloat(),CHECK_BOUNDARY)].lightClone();
                    dequeue_node.children[dequeue_argpos] = n;
                    n.resetNode(state,thread);  // give ERCs a chance to randomize
                    n.argposition = (byte)dequeue_argpos;
                    n.parent = dequeue_node;
                    }
                
                // pick a nonterminal and enqueue its children
                else
                    {
                    if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);       // we tried terminals and we're here because there were none!
                                                                                        
                    GPNode n = (GPNode)
                        nonterminals[RandomChoice.pickFromDistribution(
                                         pset.nonterminalProbabilities(y),
                                         state.random[thread].nextFloat(),CHECK_BOUNDARY)].lightClone();
                    dequeue_node.children[dequeue_argpos] = n;
                    n.resetNode(state,thread);  // give ERCs a chance to randomize
                    n.argposition = (byte)dequeue_argpos;
                    n.parent = dequeue_node;
                    
                    childtypes = n.constraints(initializer).childtypes;
                    for(int x=0;x<childtypes.length;x++)
                        enqueue(n,x,dequeue_depth + 1);
                    }
                s++;
                }
            }

        return root;
        }

    }
