/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;
import ec.simple.*;
import ec.*;

/* 
 * SteadyStateEvaluator.java
 * 
 * Created: Wed Aug 18 21:31:18 1999
 * By: Sean Luke
 */

/**
 * The SteadyStateEvaluator is a simple, mostly single-threaded,
 * non-coevolved steady-state
 * evaluator which evaluates every single member of every subpopulation
 * individually.  On the first pass, SteadyStateEvaluator's functions
 * deal with the whole population.  On subsequent passes, the functions
 * only consider the first state.newnuminds individuals in each subpopulation; thus breeding
 * methods must place the new individuala in the first spota in a subpopulation.
 * Furthermore, the first pass is the only time you'll see multiple threads,
 * if any.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SteadyStateEvaluator extends SimpleEvaluator
    {
    /** If it's the first time around, all the individuals in the population
        are evaluated.  Else only the first individual in each subpopulation
        is evaluated. */
    public void evaluatePopulation(final EvolutionState state)
        {
        final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state; 
        if (st.firstTimeAround)
            // evaluate the initial population
            super.evaluatePopulation(st);
        else
            {
            // evaluate the individual in each subpopulation
            for(int pop=0;pop<st.population.subpops.length;pop++)
                {
                ((SimpleProblemForm)p_problem).evaluate(st, 
                                                        st.population.subpops[pop].individuals[st.newIndividuals[pop]], 0);
                
                // inform the breeder that things have finally changed
                // and been evaluated for these subpopulations
                ((SteadyStateBreeder)(st.breeder)).
                    individualReplaced(st,pop,0,st.newIndividuals[pop]);
                }
            }
        }
    
    /** The SteadyStateEvaluator determines that a run is complete by asking
        each individual if he's optimal; if he finds one that's optimal,
        he signals that the run is complete.  This version checks every
        individual in the population it's the first time around, else it only
        checks the first individual in each subpopulation. */
    public boolean runComplete(final EvolutionState state)
        {
        final SteadyStateEvolutionState st = (SteadyStateEvolutionState) state; 
        if (st.firstTimeAround)
            // check the whole population
            return super.runComplete(st);
        else
            {
            for(int pop = 0;pop<st.population.subpops.length;pop++)
                if (st.population.subpops[pop].individuals[st.newIndividuals[pop]].
                    fitness.isIdealFitness())
                    return true;
            return false;
            }
        }
    }
