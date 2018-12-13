/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.Component;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.util.HashMap;

/**
 * A straightforward table that stores and retrieves pheromone concentrations
 * (doubles) independently for a number of components.
 * 
 * @author Eric O. Scott
 */
public class SimplePheromoneTable implements PheromoneTable {
    public final static String P_NOISE = "starting-noise";

    private int numComponents;
    private double noise;
    private HashMap<Component, Double> pheromones;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(state != null);
        assert(base != null);
        if (!(state.evaluator.p_problem instanceof ConstructiveProblemForm))
            state.output.fatal(String.format("%s: tried to use pheromone table with a %s, but must be %s.", this.getClass().getSimpleName(), state.evaluator.p_problem.getClass().getSimpleName(), ConstructiveProblemForm.class.getSimpleName()));
        numComponents = ((ConstructiveProblemForm)state.evaluator.p_problem).numComponents();
        pheromones = new HashMap<Component, Double>(numComponents);
        noise = state.parameters.getDoubleWithDefault(base.push(P_NOISE), null, 0.00001);
        assert(repOK());
    }
    
    @Override
    public double get(final EvolutionState state, final Component c, final int thread) {
        assert(c != null);
        final double value = pheromones.containsKey(c) ? pheromones.get(c) : 0.0;
        assert(value >= 0.0);
        if (value > 0.0)
            return value;
        else // zero phermone: replace with a small random number
        {
            final double perturbation = state.random[thread].nextDouble()*noise;
            pheromones.put(c, perturbation);
            return perturbation;
        }
    }

    @Override
    public void set(final Component c, final double value) {
        assert(c != null);
        pheromones.put(c, value);
    }
    
    public boolean repOK()
    {
        return numComponents > 0
                && pheromones != null
                && pheromones.size() <= numComponents;
    }
}
