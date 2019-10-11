package ec.co.grasp;

import ec.EvolutionState;
import ec.co.Component;
import ec.util.Misc;
import ec.util.Parameter;

import java.util.List;

public class GreedyComponentSelector implements ComponentSelector {

    public GreedyComponentSelector() {}

    @Override
    public Component choose(EvolutionState state, List<Component> components, int thread)
        {
        assert(components != null);
        assert(!components.isEmpty());
        assert(!Misc.containsNulls(components));

        double bestValue = Double.NEGATIVE_INFINITY;
        Component best = null;
        for (final Component c : components)
            {
            if (c.desirability() >= bestValue)
                {
                bestValue = c.desirability();
                best = c;
                }
            }
        assert(best != null);
        return best;
        }

    public void setup(final EvolutionState state, final Parameter base) {
        assert(repOK());
        }

    public final boolean repOK()
        {
        return true;
        }
    }
