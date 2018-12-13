/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.knapsack;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.co.ant.ComponentSelector;
import ec.co.ant.ConstructionRule;
import ec.co.ant.PheromoneTable;
import ec.co.ant.ProportionateComponentSelector;
import ec.util.Parameter;
import java.util.List;

/**
 *
 * @author Eric O. Scott
 */
public class KnapsackConstructionRule implements ConstructionRule, Setup {
    public final static String P_ALPHA = "alpha";
    public final static String P_BETA = "beta";
    public final static String P_FIRST_RANDOM = "first-random";

    private boolean firstRandom;
    private ComponentSelector selector;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        firstRandom = state.parameters.getBoolean(base.push(P_FIRST_RANDOM), null, true);
        final double alpha = state.parameters.getDouble(base.push(P_ALPHA), null);
        final double beta = state.parameters.getDouble(base.push(P_BETA), null);
        selector = new ProportionateComponentSelector(alpha, beta);
        assert(repOK());
    }

    /** Constructs a solution by greedily adding the lowest-cost component at 
     * each step until a complete solution is formed.  The pheromone matrix
     * argument is ignored, and may be null.
     */
    @Override
    public ConstructiveIndividual constructSolution(final EvolutionState state, final ConstructiveIndividual ind, final PheromoneTable pheromones, final int thread)
    {
        assert(state != null);
        assert(ind != null);
        assert(ind.isEmpty());
        assert(state.evaluator.p_problem instanceof ConstructiveProblemForm);
        
        final ConstructiveProblemForm problem = (ConstructiveProblemForm) state.evaluator.p_problem;
        assert(!problem.isCompleteSolution(ind));
        
        if (firstRandom)
        { // Choose the first component randomly
            final List<Component> allowedMoves = problem.getAllowedComponents(ind);
            final Component component = allowedMoves.get(state.random[thread].nextInt(allowedMoves.size()));
            ind.add(state, component);
        }
        
        // Constructively build a new individual
        while (!problem.isCompleteSolution(ind))
            {
            final List<Component> allowedMoves = problem.getAllowedComponents(ind);
            final Component component = selector.choose(state, allowedMoves, pheromones, thread);
            ind.add(state, component);
            }
        
        assert(repOK());
        return ind;
    }
    
    public final boolean repOK()
    {
        return P_ALPHA != null
                && !P_ALPHA.isEmpty()
                && P_BETA != null
                && !P_BETA.isEmpty()
                && selector != null;
    }
}
