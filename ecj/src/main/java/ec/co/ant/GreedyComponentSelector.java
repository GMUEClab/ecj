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
 *
 * @author Eric O. Scott
 */
public class GreedyComponentSelector implements ComponentSelector, Setup {
    public final static String P_MINIMIZE = "minimize-local-cost";
    
    private boolean minimize;
    
    public GreedyComponentSelector() {}
    
    public GreedyComponentSelector(final boolean minimize)
    {
        this.minimize = minimize;
    }
    
    public boolean isMinimize()
    {
        return minimize;
    }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        minimize = state.parameters.getBoolean(base.push(P_MINIMIZE), null, true);
        assert(repOK());
    }

    @Override
    public Component choose(final EvolutionState state, final List<Component> components, final PheromoneTable pheromones, final int thread) {
        assert(components != null);
        assert(!components.isEmpty());
        assert(!Misc.containsNulls(components));
        
        double bestCost = minimize ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        Component best = null;
        for (final Component c : components)
            {
                if (minimize ? c.cost() <= bestCost : c.cost() >= bestCost)
                    {
                    bestCost = c.cost();
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
