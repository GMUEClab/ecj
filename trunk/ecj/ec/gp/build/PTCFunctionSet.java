/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.build;
import ec.*;
import ec.util.*;
import ec.gp.*;

/* 
 * PTCFunctionSet.java
 * 
 * Created: Wed Jan 26 21:10:59 2000
 * By: Sean Luke
 */

/**
 * PTCFunctionSet is a GPFunctionSet which adheres to PTCFunctionSetForm, and thus
 * can be used with the PTC1 and PTC2 methods.  Terminal and nonterminal probabilities
 * for nodes used in this function set are determined by the <tt>prob</tt> parameter
 * for the nodes' GPNodeConstraints object.  That's not the greatest solution,
 * because it could require making a lot of different GPNodeConstraints, customized for each
 * node, but it's the best I can do for now.
 *
 * The nonterminalSelectionProbabilities() method computes nonterminal selection
 * probability using the probabilities above, per type, for the size requested.
 * If the size is small enough (smaller than CACHE_SIZE), then the result is
 * memoized so it doesn't need to be computed again next time.
 * 
 * @author Sean Luke
 * @version 1.0 
 */

public class PTCFunctionSet extends GPFunctionSet implements PTCFunctionSetForm
    {
    /** terminal probabilities[type][thenodes], in organized form */
    public float q_ty[][];
    /** nonterminal probabilities[type][thenodes], in organized form */
    public float q_ny[][];

    public static final int CACHE_SIZE = 1024;
    /** cache of nonterminal selection probabilities -- dense array 
        [size-1][type].  If any items are null, they're not in the dense cache. */
    public float p_y[][];

    public float[] terminalProbabilities(final int type)
        { return q_ty[type]; }

    public float[] nonterminalProbabilities(final int type)
        { return q_ny[type]; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // load our probabilities here.
        
        q_ny = new float[nonterminals.length][];
        q_ty = new float[terminals.length][];
        
        boolean allOnes = true;
        boolean noOnes = true;
        boolean allZeros = true;
        GPInitializer initializer = ((GPInitializer)state.initializer);

        for(int type=0;type<nonterminals.length;type++)
            {
            q_ny[type] = new float[nonterminals[type].length];
            for(int x=0;x<nonterminals[type].length;x++)
                {
                q_ny[type][x] = nonterminals[type][x].constraints(initializer).probabilityOfSelection;
                if (q_ny[type][x] != 0.0f) allZeros = false;
                if (q_ny[type][x] == 1.0f) noOnes = false;
                else allOnes = false;
                }
            }
            
        if (allZeros)
            state.output.warning("In this function set, the probabilities of all nonterminal functions have a 0.0 selection probability -- this will cause them all to be selected uniformly.  That could be an error.", base);
        allZeros = false;

        for(int type=0;type<terminals.length;type++)
            {
            q_ty[type] = new float[terminals[type].length];
            for(int x=0;x<terminals[type].length;x++)
                {
                q_ty[type][x] = terminals[type][x].constraints(initializer).probabilityOfSelection;
                if (q_ty[type][x] != 0.0f) allZeros = false;
                if (q_ty[type][x] == 1.0f) noOnes = false;
                else allOnes = false;
                }
            }

        if (allZeros)
            state.output.warning("In this function set, the probabilities of all terminal functions have a 0.0 selection probability -- this will cause them all to be selected uniformly.  That could be an error.", base);

        if (!allOnes && !noOnes)
            state.output.warning("In this function set, there are some functions with a selection probability of 1.0, but not all of them.  That could be an error.",base);
        
        // set up our node probabilities.  Allow all zeros.
        for(int x=0;x<q_ty.length;x++)
            {
            RandomChoice.organizeDistribution(q_ty[x], true);
            RandomChoice.organizeDistribution(q_ny[x], true);
            }

        // set up cache
        p_y = new float[CACHE_SIZE][];
        }
    
    public float[] nonterminalSelectionProbabilities(final int expectedTreeSize)
        {
        // check cache first
        if (expectedTreeSize<CACHE_SIZE)
            {
            if (p_y[expectedTreeSize-1]!=null) return p_y[expectedTreeSize-1];
            else return p_y[expectedTreeSize-1] = 
                     computeNonterminalSelectionProbabilities(expectedTreeSize);
            }
        else
            // we'll have to compute it
            return computeNonterminalSelectionProbabilities(expectedTreeSize);
        }

    
    public float[] computeNonterminalSelectionProbabilities(final int expectedTreeSize)
        {
        float[] p = new float[q_ny.length];

        // for each type...
        for(int x=0;x<q_ny.length;x++)
            {
            double count=0;
            // gather branching factor * prob for each nonterminal
            for(int y=0;y<q_ny[x].length;y++)
                count += (y==0 ? q_ny[x][y] : q_ny[x][y]-q_ny[x][y-1]) // it's organized
                    * nonterminals[x][y].children.length;

            p[x] = (float)((1.0-(1.0/expectedTreeSize))/count);
            }
        return p;
        }
    }
