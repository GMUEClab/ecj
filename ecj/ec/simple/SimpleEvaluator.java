/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.Evaluator;
import ec.EvolutionState;
import ec.util.Parameter;

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
    // checks to make sure that the Problem implements SimpleProblemForm
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        if (!(p_problem instanceof SimpleProblemForm))
            state.output.fatal("SimpleEvaluator used, but the Problem is not of SimpleProblemForm",
                               base.push(P_PROBLEM));
        }
    
    /** A simple evaluator that doesn't do any coevolutionary
        evaluation.  Basically it applies evaluation pipelines,
        one per thread, to various subchunks of a new population. */
    public void evaluatePopulation(final EvolutionState state)
        {
        int numinds[][] = 
            new int[state.evalthreads][state.population.subpops.length];
        int from[][] = 
            new int[state.evalthreads][state.population.subpops.length];
        
        for(int y=0;y<state.evalthreads;y++)
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // figure numinds
                if (y<state.evalthreads-1) // not last one
                    numinds[y][x]=
                        state.population.subpops[x].individuals.length/
                        state.evalthreads;
                else // in case we're slightly off in division
                    numinds[y][x]=
                        state.population.subpops[x].individuals.length/
                        state.evalthreads +
                        
                        (state.population.subpops[x].individuals.length -
                         (state.population.subpops[x].individuals.length /
                          state.evalthreads)  // note integer division
                         *state.evalthreads);                    

                // figure from
                from[y][x]=
                    (state.population.subpops[x].individuals.length/
                     state.evalthreads) * y;
                }

        if (state.evalthreads==1)
            evalPopChunk(state,numinds[0],from[0],0,(SimpleProblemForm)(p_problem.clone()));  
        
        else
            {
            Thread[] t = new Thread[state.evalthreads];
            
            // start up the threads
            for(int y=0;y<state.evalthreads;y++)
                {
                SimpleEvaluatorThread r = new SimpleEvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = (SimpleProblemForm)(p_problem.clone());
                t[y] = new Thread(r);
                t[y].start();
                }

            // gather the threads
            for(int y=0;y<state.evalthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                }

            }
        }

    /** A private helper function for evaluatePopulation which evaluates a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    void evalPopChunk(EvolutionState state, int[] numinds, int[] from,
                      int threadnum, SimpleProblemForm p)
        {
        ((ec.Problem)p).prepareToEvaluate(state,threadnum);
        
        for(int pop=0;pop<state.population.subpops.length;pop++)
            {
            // start evaluatin'!
            int upperbound = from[pop]+numinds[pop];
            for (int x=from[pop];x<upperbound;x++)
                {
                p.evaluate(state,state.population.subpops[pop].individuals[x], threadnum);
                }
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
