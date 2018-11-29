/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * A straightforward table that stores and retrieves pheromone concentrations
 * (doubles) independently for a number of components (ints).
 * 
 * @author Eric O. Scott
 */
public class SimplePheromoneTable implements PheromoneTable {

    private int numComponents;
    private List<Double> pheromones;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(state != null);
        assert(base != null);
        if (!(state.evaluator.p_problem instanceof ConstructiveProblemForm))
            state.output.fatal(String.format("%s: tried to use pheromone table with a %s, but must be %s.", this.getClass().getSimpleName(), state.evaluator.p_problem.getClass().getSimpleName(), ConstructiveProblemForm.class.getSimpleName()));
        numComponents = ((ConstructiveProblemForm)state.evaluator.p_problem).numComponents();
        pheromones = new ArrayList<Double>(numComponents);
        assert(repOK());
    }
    
    @Override
    public double get(final int c) {
        assert(c >= 0);
        assert(c < numComponents);
        return pheromones.get(c);
    }

    @Override
    public void set(final int c, final double value) {
        assert(c >= 0);
        assert(c < numComponents);
        pheromones.set(c, value);
    }
    
    public boolean repOK()
    {
        return numComponents > 0
                && pheromones != null
                && pheromones.size() == numComponents;
    }
}
