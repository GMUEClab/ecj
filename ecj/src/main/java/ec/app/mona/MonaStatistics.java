/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.mona; 

import ec.*;
import ec.simple.*;

public class MonaStatistics extends Statistics
    {
    public Individual best_of_run;

    /** Logs the best individual of the generation. */
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);

        boolean newBest = false;
        for(int y = 0; y< state.population.subpops.get(0).individuals.size(); y++)
            if (best_of_run==null || 
                state.population.subpops.get(0).individuals.get(y).fitness.betterThan(best_of_run.fitness))
                {
                best_of_run = (Individual)(state.population.subpops.get(0).individuals.get(y).clone());
                newBest = true;
                }

        if (newBest)
            {
            ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(
                state, best_of_run, 0, 0, 0);  
            }
        }

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);

        ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(
            state, best_of_run, 0, 0, 0);      
        }
    }
