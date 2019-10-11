/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import ec.util.Misc;
import ec.util.Parameter;
import java.util.List;

/**
 * A trivial rule that builds solutions greedily to have the highest heuristic value in the neighborhood.
 *
 * @author Eric O. Scott
 */
public class GreedyComponentSelector implements ComponentSelector {
    
    public GreedyComponentSelector() {}

    public void setup(final EvolutionState state, final Parameter base) {
        assert(repOK());
        }

    @Override
    public Component choose(final EvolutionState state, final List<Component> components, final PheromoneTable pheromones, final int thread) {
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
    
    public final boolean repOK()
        {
        return true;
        }
    }
