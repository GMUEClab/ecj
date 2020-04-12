/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.util.List;

/**
 * Builds an individual by adding one component at a time.  The component
 * is chosen using a <code>ComponentSelector</code> of your choice.
 * 
 * @author Eric O. Scott
 */
public class SimpleConstructionRule implements ConstructionRule {
    private static final long serialVersionUID = 1;

    public final static String P_SELECTOR = "component-selector";
    public final static String P_START = "start-component";

    private String startComponent;
    private ComponentSelector selector;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        assert(state != null);
        assert(base != null);
        startComponent = state.parameters.getString(base.push(P_START), null);
        selector = (ComponentSelector) state.parameters.getInstanceForParameter(base.push(P_SELECTOR), null, ComponentSelector.class);
        selector.setup(state, base.push(P_SELECTOR));
        assert(repOK());
        }

    /** Constructs a solution by greedily adding the lowest-desirability component at
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
        
        // Choose the initial component
        if (startComponent != null) // given manually
            ind.add(state, problem.getComponentFromString(startComponent));
        else // chosen arbitratily
            ind.add(state, problem.getArbitraryComponent(state, thread));
        
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
        return P_SELECTOR != null
            && !P_SELECTOR.isEmpty()
            && P_START != null
            && !P_START.isEmpty()
            && selector != null;
        }
    }
