/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.*;
import ec.coevolve.*;

/* 
 * SpatialMultiPopCoevolutionaryEvaluator.java
 * 
 * By: Liviu Panait and Sean Luke
 */

/** 
 * SpatialMultiPopCoevolutionaryEvaluator implements a coevolutionary evaluator involving multiple
 * spatially-embedded subpopulations.  You ought to use it in conjuction with SpatialTournamentSelection
 * (for selecting current-generation individuals, set the tournament selection size to 1, which will
 * pick randomly from the space).
 *
 * @author Liviu Panait Sean Luke
 * @version 2.0 
 */

public class SpatialMultiPopCoevolutionaryEvaluator extends MultiPopCoevolutionaryEvaluator
    {
    protected Individual produce(SelectionMethod method, int subpopulation, int individual, EvolutionState state, int thread)
        {
        if (!(state.population.subpops.get(subpopulation) instanceof Space))
            state.output.fatal("Subpopulation " + subpopulation + " is not a Space.");
                        
        Space space = (Space)(state.population.subpops.get(subpopulation));
        space.setIndex(thread, individual);
                
        return state.population.subpops.get(subpopulation).individuals.get(method.produce(subpopulation, state, thread));
        }
    }
