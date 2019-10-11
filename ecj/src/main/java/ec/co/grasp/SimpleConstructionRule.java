package ec.co.grasp;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.util.List;

import ec.util.Parameter;

public class SimpleConstructionRule implements ConstructionRule, Setup
    {
    public final static String P_SELECTOR = "component-selector";
    public final static String P_START = "start-component";

    private String startComponent;
    private ComponentSelector selector;

    @Override
    public ConstructiveIndividual constructSolution(EvolutionState state, ConstructiveIndividual ind, int thread) {
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
            final Component component = selector.choose(state, allowedMoves, thread);
            ind.add(state, component);
            }

        assert(repOK());
        return ind;
        }

    @Override
    public void setup(EvolutionState state, Parameter base)
        {
        assert(state != null);
        assert(base != null);
        startComponent = state.parameters.getString(base.push(P_START), null);
        selector = (ComponentSelector) state.parameters.getInstanceForParameter(base.push(P_SELECTOR), null, ec.co.grasp.ComponentSelector.class);
        selector.setup(state, base.push(P_SELECTOR));
        assert(repOK());
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
