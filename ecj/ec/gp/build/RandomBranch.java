/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec.gp.build;
import ec.gp.*;
import ec.*;
import ec.util.*;

/* 
 * RandomBranch.java
 * 
 * Created: Mon Dec 13 14:26:02 1999
 * By: Sean Luke
 */

/**
 * RandomBranch implements the <tt>Random_Branch</tt> tree generation
 * method described in 
 *
 * <p> Chellapilla, K. 1998.  Evolving Modular Programs without Crossover.
 * in <i>Proceedings of the Third Annual Genetic Programming Conference</i>
 * (GP98), J.R. Koza <i>et al</i>, editors.  San Fransisco: Morgan Kaufmann.
 * 23--31.
 *
 * <p> This algorithm attempts to create a tree of size <tt>requestedSize</tt>,
 * or "slightly less".
 *
 * If the pipeline does not specify a size it wants (it uses <tt>NOSIZEGIVEN</tt>),
 * the algorithm picks a size at random from either [minSize...maxSize] or from
 * sizeDistribution (one of the two <b>must</b> be defined), and attempts to create
 * a tree of that size or "slightly less".
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class RandomBranch extends GPNodeBuilder 
    {
    public static final String P_RANDOMBRANCH = "random-branch";

    public Parameter defaultBase()
        {
        return GPBuildDefaults.base().push(P_RANDOMBRANCH);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // we use size distributions -- did the user specify any?
        if (!canPick())
            state.output.fatal("RandomBranch requires some kind of size distribution set, either with " + P_MINSIZE + "/" + P_MAXSIZE + ", or with " + P_NUMSIZES + ".",
                               base, defaultBase());
        }

    public GPNode newRootedTree(final EvolutionState state,
                                final GPType type,
                                final int thread,
                                final GPNodeParent parent,
                                final GPFunctionSet set,
                                final int argposition,
                                final int requestedSize)
        {
        if (requestedSize == NOSIZEGIVEN)  // pick from the distribution
            return randomBranch(state,type,pickSize(state,thread),thread,parent,argposition,set);
        if (requestedSize < 1)
            state.output.fatal("ec.gp.build.RandomBranch requested to build a tree, but a requested size was given that is < 1.");
        return randomBranch(state,type,requestedSize,thread,parent,argposition,set);
        }

    private GPNode randomBranch(final EvolutionState state,
                                final GPType type,
                                final int maxLength, 
                                final int thread,
                                final GPNodeParent parent,
                                final int argposition,
                                final GPFunctionSet set) 
        {
        if (maxLength == 1)         // pick a random terminal
            {
            GPNode[] nn = set.terminals[type.type];
            GPNode n = (GPNode)(nn[state.random[thread].nextInt(nn.length)].clone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
        else
            {
            // grab all the nodes whose arity is <= maxlength-1
            int len = set.nonterminalsUnderArity[type.type].length-1;
            if (len > maxLength-1) len = maxLength-1;
            GPNode[] okayNonterms = set.nonterminalsUnderArity[type.type][len];

            if (okayNonterms.length == 0) // no nodes, pick a nonterminal
                {
                GPNode[] okayTerms = set.terminals[type.type];
                GPNode n = (GPNode)(okayTerms[state.random[thread].nextInt(okayTerms.length)].clone());
                n.resetNode(state,thread);  // give ERCs a chance to randomize
                n.argposition = (byte)argposition;
                n.parent = parent;
                return n;
                }
            
            else // we've got nonterminals, pick one at random
                {
                GPNode n = (GPNode)(okayNonterms[state.random[thread].nextInt(okayNonterms.length)].clone());
                n.resetNode(state,thread);  // give ERCs a chance to randomize
                n.argposition = (byte)argposition;
                n.parent = parent;

                // Populate the node...
                GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
                for(int x=0;x<childtypes.length;x++)
                    n.children[x] = randomBranch(
                        state,childtypes[x],
                        (maxLength-1)/childtypes.length, // note int division
                        thread,n,x,set);
                return n;
                }
            }
        }

    }
