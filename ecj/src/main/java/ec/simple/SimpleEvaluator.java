/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import java.util.ArrayList;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.util.*;
import ec.gp.ge.*;

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
 * each evaluating thread, and chunks of individuals are sent to each thread
 * for evaluation.
 * 
 * The Problem must implement either SimpleProblemForm or GroupedProblemForm.  
 * If a GroupedProblemForm is provided, then an entire chunk of individuals is 
 * sent to the problem to be evaluated together in a batch.  If a SimpleProblemForm
 * is provided, then each thread sends individuals sequentially to be evaluated
 * one-at-a-time.
 *
 * @author Sean Luke
 * @author Eric Scott
 * @version 2.0 
 *
 * Thanks to Ralf Buschermohle <lobequadrat@googlemail.com> for early versions
 * of code which led to this version.
 *
 */

public class SimpleEvaluator extends Evaluator
    {
    private static final long serialVersionUID = 1;
    
    public static final String P_CLONE_PROBLEM = "clone-problem";
    public static final String P_NUM_TESTS = "num-tests";
    public static final String P_MERGE = "merge";

    public static final String V_MEAN = "mean";
    public static final String V_MEDIAN = "median";
    public static final String V_BEST = "best";
        
    public static final String P_CHUNK_SIZE = "chunk-size";
    public static final String V_AUTO = "auto";

    public static final int MERGE_MEAN = 0;
    public static final int MERGE_MEDIAN = 1;
    public static final int MERGE_BEST = 2;

    public int numTests = 1;
    public int mergeForm = MERGE_MEAN;
    public boolean cloneProblem;

    Object[] lock = new Object[0];          // Arrays are serializable
    int individualCounter = 0;
    int subPopCounter = 0;
    int chunkSize;  // a value >= 1, or C_AUTO
    public static final int C_AUTO = 0;
        
    public ThreadPool pool = new ThreadPool();

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

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
                
        if (!state.parameters.exists(base.push(P_CHUNK_SIZE), null))
            {
            chunkSize = C_AUTO;
            }
        else if (state.parameters.getString(base.push(P_CHUNK_SIZE), null).equalsIgnoreCase(V_AUTO))
            {
            chunkSize = C_AUTO;
            }
        else
            {
            chunkSize = (state.parameters.getInt(base.push(P_CHUNK_SIZE), null, 1));
            if (chunkSize == 0)  // uh oh
                state.output.fatal("Chunk Size must be either an integer >= 1 or 'auto'", base.push(P_CHUNK_SIZE), null);
            }

        } 

    Population oldpop = null;
    
    // replace the population with one that has some N copies of the original individuals
    void expand(EvolutionState state)
        {
        Population pop = (Population)(state.population.emptyClone());
        
        // populate with clones
        for(int i = 0;i<pop.subpops.size();++i)
            {
            for(int j = 0;j<state.population.subpops.get(i).individuals.size();++j)
                {
                for(int k=0;k<numTests;++k)
                    {
                    pop.subpops.get(i).individuals.add((Individual) state.population.subpops.get(i).individuals.get(j).clone());
                    }
                }
            }
        
        // swap
        oldpop = state.population;
        state.population = pop;
        }
    

    // Take the N copies of the original individuals and fold their fitnesses back into the original
    // individuals, replacing them with the original individuals in the process.  See expand(...)
    void contract(EvolutionState state)
        {
        // swap back
        Population pop = state.population;
        state.population = oldpop;

        // merge fitnesses again
        for(int i = 0; i < pop.subpops.size(); i++)
            {
            Fitness[] fits = new Fitness[numTests];
            for(int j = 0; j < state.population.subpops.get(i).individuals.size(); j++)
                {
                for (int k=0; k < numTests; k++)
                    {
                    fits[k] = pop.subpops.get(i).individuals.get(numTests * j + k).fitness;
                    }

                if (mergeForm == MERGE_MEAN)
                    {
                    state.population.subpops.get(i).individuals.get(j).fitness.setToMeanOf(state, fits);
                    }
                else if (mergeForm == MERGE_MEDIAN)
                    {
                    state.population.subpops.get(i).individuals.get(j).fitness.setToMedianOf(state, fits);
                    }
                else  // MERGE_BEST
                    {
                    state.population.subpops.get(i).individuals.get(j).fitness.setToBestOf(state, fits);
                    }

                state.population.subpops.get(i).individuals.get(j).evaluated = true;
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
            
        // reset counters.  Only used in multithreading
        individualCounter = 0;
        subPopCounter = 0;

        // start up if single-threaded?
        if (state.evalthreads == 1)
            {
            int[] numinds = new int[state.population.subpops.size()];
            int[] from = new int[numinds.length];
                        
            for(int i = 0; i < numinds.length; i++)
                {
                numinds[i] =  state.population.subpops.get(i).individuals.size();
                from[i] = 0;
                }
                                
            Problem prob = null;
            if (cloneProblem)
                prob = (Problem)(p_problem.clone());
            else 
                prob = (Problem)(p_problem);  // just use the prototype
            evalPopChunk(state, numinds, from, 0, prob);
            }
        else
            {
            ThreadPool.Worker[] threads = new ThreadPool.Worker[state.evalthreads];
            for(int i = 0; i < threads.length; i++)
                {
                SimpleEvaluatorThread run = new SimpleEvaluatorThread();
                run.threadnum = i;
                run.state = state;
                run.prob = (Problem)p_problem.clone();
                threads[i] = pool.start(run, "ECJ Evaluation Thread " + i);
                }
                        
            // join
            pool.joinAll();
            }

        if (numTests > 1)
            contract(state);
        }

    /** The SimpleEvaluator determines that a run is complete by asking
        each individual in each population if he's optimal; if he 
        finds an individual somewhere that's optimal,
        he signals that the run is complete. */
    public String runComplete(final EvolutionState state)
        {
        for(int x = 0; x< state.population.subpops.size(); x++)
            for(int y = 0; y< state.population.subpops.get(x).individuals.size(); y++)
                if (state.population.subpops.get(x).
                    individuals.get(y).fitness.isIdealFitness())
                    return "Individual " + y + " of subpopulation " + x + " has an ideal fitness." ;
        
        if (runComplete != null) return runComplete;
        else return null;
        }




    /** A private helper function for evaluatePopulation which evaluates a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        protected, you should not call it. */

    protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, Problem p)
        {
        ((ec.Problem)p).prepareToEvaluate(state,threadnum);

        if (!((p instanceof SimpleProblemForm) || (p.isGroupedProblem())))
            state.output.fatal(String.format("%s used, but the Problem must be of either %s or %s", this.getClass().getSimpleName(), SimpleProblemForm.class.getSimpleName(), GroupedProblemForm.class.getSimpleName()));
        
        ArrayList<Subpopulation> subpops = state.population.subpops;
        int len = subpops.size();
        
        for(int pop=0;pop<len;pop++)
            {
            // Get the chunk of individuals we're meant to evaluate
            int fp = from[pop];
            int upperbound = fp+numinds[pop];
            ArrayList<Individual> inds = subpops.get(pop).individuals;
            final Individual[] chunk = new Individual[numinds[pop]];
            int i = 0;
            for (int x=fp; x < upperbound; x++)
                chunk[i++] = inds.get(x);

            if (p.isGroupedProblem())  // Evaluate the chunk all at once
            	{
                ((GroupedProblemForm)p).evaluate(state, chunk, null, false, null, threadnum);
            	}
            else // Evaluate each individual in the chunk sequentially
            	{
                for (Individual ind : chunk)
                    ((SimpleProblemForm)p).evaluate(state, ind, pop, threadnum);
            	}
            state.incrementEvaluations(upperbound - fp);
            }
                        
        ((ec.Problem)p).finishEvaluating(state,threadnum);
        }


    // computes the chunk size if 'auto' is set.  This may be different depending on the subpopulation,
    // which is backward-compatible with previous ECJ approaches.
    int computeChunkSizeForSubpopulation(EvolutionState state, int subpop, int threadnum)
        {
        int numThreads = state.evalthreads;
                
        // we will have some extra individuals.  We distribute these among the early subpopulations
        int individualsPerThread = state.population.subpops.get(subpop).individuals.size() / numThreads;  // integer division
        int slop = state.population.subpops.get(subpop).individuals.size() - numThreads * individualsPerThread;
                
        if (threadnum >= slop) // beyond the slop
            return individualsPerThread;
        else return individualsPerThread + 1;
        }



    /** A helper class for implementing multithreaded evaluation */
    class SimpleEvaluatorThread implements Runnable
        {
        public int threadnum;
        public EvolutionState state;
        public Problem prob = null;
        
        public void run() 
            {
            ArrayList<Subpopulation> subpops = state.population.subpops;

            int[] numinds = new int[subpops.size()];
            int[] from = new int[subpops.size()];

            int count = 1;
            int start = 0;
            int subpop = 0;

            while (true)
                {
                // We need to grab the information about the next chunk we're responsible for.  This stays longer
                // in the lock than I'd like :-(
                synchronized(lock)
                    {
                    // has everyone done all the jobs?
                    if (subPopCounter >= subpops.size()) // all done
                        return;
                                        
                    // has everyone finished the jobs for this subpopulation?
                    if (individualCounter >= subpops.get(subPopCounter).individuals.size())  // try again, jump to next subpop
                        {
                        individualCounter = 0; 
                        subPopCounter++;
                                        
                        // has everyone done all the jobs?  Check again.
                        if (subPopCounter >= subpops.size()) // all done
                            return;
                        }

                    start = individualCounter;
                    subpop = subPopCounter;
                    count = chunkSize;
                    if (count == C_AUTO)  // compute automatically for subpopulations
                        count = computeChunkSizeForSubpopulation(state, subpop, threadnum);
                    
                    individualCounter += count;  // it can be way more than we'll actually do, that's fine                    
                    }
                
                // Modify the true count
                if (count >= subpops.get(subpop).individuals.size() - start)
                    count = subpops.get(subpop).individuals.size() - start;

                // Load into arrays to reuse evalPopChunk
                for(int i = 0; i < from.length; i++)
                    numinds[i] = 0;
                        
                numinds[subpop] = count;
                from[subpop] = start;
                evalPopChunk(state, numinds, from, threadnum, prob);
                }
            }
        }


    }
