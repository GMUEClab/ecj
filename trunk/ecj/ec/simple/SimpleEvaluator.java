/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;
import ec.util.*;

/* 
 * SimpleEvaluator.java
 * 
 * Created: Wed Aug 18 21:31:18 1999
 * By: Sean Luke
 */

/**
 * The SimpleEvaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its
 * own problem space.  One Problem instance is cloned from p_problem for
 * each evaluating thread.  The Problem must implement SimpleProblemForm.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleEvaluator extends Evaluator
    {
    public static final String P_CLONE_PROBLEM = "clone-problem";
    public static final String P_NUM_TESTS = "num-tests";
    public static final String P_MERGE = "merge";
    
    public static final String V_MEAN = "mean";
    public static final String V_MEDIAN = "median";
    public static final String V_BEST = "best";
    
    public static final int MERGE_MEAN = 0;
    public static final int MERGE_MEDIAN = 1;
    public static final int MERGE_BEST = 2;

    public int numTests = 1;
    public int mergeForm = MERGE_MEAN;
    public boolean cloneProblem;

    // checks to make sure that the Problem implements SimpleProblemForm
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        if (!(p_problem instanceof SimpleProblemForm))
            state.output.fatal("" + this.getClass() + " used, but the Problem is not of SimpleProblemForm",
                base.push(P_PROBLEM));

        cloneProblem =state.parameters.getBoolean(base.push(P_CLONE_PROBLEM), null, true);
        if (!cloneProblem && (state.breedthreads > 1)) // uh oh, this can't be right
            state.output.fatal("The Evaluator is not cloning its Problem, but you have more than one thread.", base.push(P_CLONE_PROBLEM));

        numTests = state.parameters.getInt(base.push(P_NUM_TESTS), null, 1);
        if (numTests < 1) numTests = 1;
        else if (numTests > 1)
            {
            String m = state.parameters.getString(base.push(P_MERGE), null);
            if (m == null)
                state.output.warning("Merge method not provided to SimpleEvaluator.  Assuming 'mean'");
            else if (m.equals(V_MEAN))
                mergeForm = MERGE_MEAN;
            else if (m.equals(V_MEDIAN))
                mergeForm = MERGE_MEDIAN;
            else if (m.equals(V_BEST))
                mergeForm = MERGE_BEST;
            else
                state.output.fatal("Bad merge method: " + m, base.push(P_NUM_TESTS), null);
            }
        }

    Population oldpop = null;
    void expand(EvolutionState state)
        {
        Population pop = (Population)(state.population.emptyClone());
        
        // populate with clones
        for(int i = 0; i < pop.subpops.length; i++)
            {
            pop.subpops[i].individuals = new Individual[numTests * state.population.subpops[i].individuals.length];
            for(int j = 0; j < state.population.subpops[i].individuals.length; j++)
                {
                for (int k=0; k < numTests; k++)
                    {
                    pop.subpops[i].individuals[numTests * j + k] =
                        (Individual)(state.population.subpops[i].individuals[j].clone());
                    }
                }
            }
        
        // swap
        Population oldpop = state.population;
        state.population = pop;
        }
                
    void contract(EvolutionState state)
        {
        // swap back
        Population pop = state.population;
        state.population = oldpop;
        
        // merge fitnesses again
        for(int i = 0; i < pop.subpops.length; i++)
            {
            Fitness[] fits = new Fitness[numTests];
            for(int j = 0; j < state.population.subpops[i].individuals.length; j++)
                {
                for (int k=0; k < numTests; k++)
                    {
                    fits[k] = pop.subpops[i].individuals[numTests * j + k].fitness;
                    }
                                
                if (mergeForm == MERGE_MEAN)
                    {
                    state.population.subpops[i].individuals[j].fitness.setToMeanOf(state, fits);
                    }
                else if (mergeForm == MERGE_MEDIAN)
                    {
                    state.population.subpops[i].individuals[j].fitness.setToMedianOf(state, fits);
                    }
                else  // MERGE_BEST
                    {
                    state.population.subpops[i].individuals[j].fitness.setToBestOf(state, fits);
                    }
                                
                state.population.subpops[i].individuals[j].evaluated = true;
                }
            }
        }


    /** A simple evaluator that doesn't do any coevolutionary
        evaluation.  Basically it applies evaluation pipelines,
        one per thread, to various subchunks of a new population. */
    public void evaluatePopulation(final EvolutionState state)
        {
        if (numTests > 1)
            expand(state);
                
        if (state.evalthreads==1)
            {
            // a minor bit of optimization
            int numinds[] = new int[state.population.subpops.length];
            int from[] = new int[state.population.subpops.length];
            for(int i = 0; i < state.population.subpops.length; i++)
                { numinds[i] = state.population.subpops[i].individuals.length; from[i] = 0; }
            if (cloneProblem)
                evalPopChunk(state,numinds,from,0,(SimpleProblemForm)(p_problem.clone()));
            else
                evalPopChunk(state,numinds,from,0,(SimpleProblemForm)(p_problem));
            }
        else
            {
            // how many threads do we really need?  No more than the maximum number of individuals in any subpopulation
            int numThreads = 0;
            for(int x = 0; x < state.population.subpops.length; x++)
                numThreads = Math.max(numThreads, state.population.subpops[x].individuals.length);
            numThreads = Math.min(numThreads, state.evalthreads);
            if (numThreads < state.breedthreads)
                state.output.warnOnce("Largest subpopulation size (" + numThreads +") is smaller than number of evalthreads (" + state.breedthreads +
                    "), so fewer evalthreads will be created.");
            
            int numinds[][] = 
                new int[numThreads][state.population.subpops.length];
            int from[][] = 
                new int[numThreads][state.population.subpops.length];
        
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // we will have some extra individuals.  We distribute these among the early subpopulations
                int individualsPerThread = state.population.subpops[x].individuals.length / numThreads;  // integer division
                int slop = state.population.subpops[x].individuals.length - numThreads * individualsPerThread;
                int currentFrom = 0;
                                
                for(int y=0;y<numThreads;y++)
                    {
                    if (slop > 0)
                        {
                        numinds[y][x] = individualsPerThread + 1;
                        slop--;
                        }
                    else
                        numinds[y][x] = individualsPerThread;
                    
                    if (numinds[y][x] == 0)
                        {
                        state.output.warnOnce("More threads exist than can be used to evaluate some subpopulations (first example: subpopulation " + x + ")");
                        }

                    from[y][x] = currentFrom;
                    currentFrom += numinds[y][x];
                    }
                }

            Thread[] t = new Thread[numThreads];
            
            // start up the threads
            for(int y=0;y<numThreads;y++)
                {
                SimpleEvaluatorThread r = new SimpleEvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = (SimpleProblemForm)(p_problem.clone()); // should ignore cloneProblem parameter here 
                t[y] = new Thread(r);
                t[y].start();
                }

            // gather the threads
            for(int y=0;y<numThreads;y++) 
                try 
                    { 
                    t[y].join(); 
                    }
                catch(InterruptedException e)
                    {
                    state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                    }

            }
            
        if (numTests > 1)
            contract(state);
        }

    /** A private helper function for evaluatePopulation which evaluates a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from,
        int threadnum, SimpleProblemForm p)
        {
        ((ec.Problem)p).prepareToEvaluate(state,threadnum);
        
        Subpopulation[] subpops = state.population.subpops;
        int len = subpops.length;
        
        for(int pop=0;pop<len;pop++)
            {
            // start evaluatin'!
            int fp = from[pop];
            int upperbound = fp+numinds[pop];
            Individual[] inds = subpops[pop].individuals;
            for (int x=fp;x<upperbound;x++)
                p.evaluate(state,inds[x], pop, threadnum);
            }
                        
        ((ec.Problem)p).finishEvaluating(state,threadnum);
        }
    
    /** The SimpleEvaluator determines that a run is complete by asking
        each individual in each population if he's optimal; if he 
        finds an individual somewhere that's optimal,
        he signals that the run is complete. */
    public boolean runComplete(final EvolutionState state)
        {
        for(int x = 0;x<state.population.subpops.length;x++)
            for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].
                    individuals[y].fitness.isIdealFitness())
                    return true;
        return false;
        }
    }

/** A private helper class for implementing multithreaded evaluation */
class SimpleEvaluatorThread implements Runnable
    {
    public int[] numinds;
    public int[] from;
    public SimpleEvaluator me;
    public EvolutionState state;
    public int threadnum;
    public SimpleProblemForm p;
    public synchronized void run() 
        { me.evalPopChunk(state,numinds,from,threadnum,p); }
    }
