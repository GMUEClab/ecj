/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.knapsack;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.simple.SimpleProblemForm;
import ec.util.Misc;
import ec.util.Parameter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eric O. Scott
 */
public class KnapsackProblem extends Problem implements SimpleProblemForm, ConstructiveProblemForm {
    public final static String P_SIZES = "sizes";
    public final static String P_COSTS = "costs";
    public final static String P_KNAPSACK_SIZE = "knapsack-size";
    
    private double[] sizes;
    private double[] costs;
    private double knapsackSize;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(state != null);
        assert(base != null);
        knapsackSize = state.parameters.getDouble(base.push(P_KNAPSACK_SIZE), null);
        sizes = state.parameters.getDoubles(base.push(P_SIZES), null, 0);
        costs = state.parameters.getDoubles(base.push(P_COSTS), null, 0, sizes.length);
        assert(repOK());
    }
            
    @Override
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        assert(state != null);
        if (!(ind instanceof ConstructiveIndividual))
            state.output.fatal(String.format("%s requires a %s, but was given a %s.", this.getClass().getSimpleName(), ConstructiveIndividual.class.getSimpleName(), ind.getClass().getSimpleName()));
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double cost(final int component) {
        if (component < 0 || component > numComponents())
            throw new IllegalStateException(String.format("%s: attempted to evaluate the cost of component %d, but component IDs must be between 0 and %d (inclusive).", this.getClass().getSimpleName(), component, numComponents()));
        assert(repOK());
        return costs[component];
    }

    @Override
    public boolean isViolated(final Set<Integer> partialSolution, final int component) {
        assert(partialSolution != null);
        assert(component > 0 && component < numComponents());
        assert(!Misc.containsNulls(partialSolution));
        double cost = cost(component);
        for (final int c : partialSolution)
            cost += cost(c);
        return cost > knapsackSize;
    }

    @Override
    public int numComponents() {
        assert(repOK());
        return sizes.length;
    }

    @Override
    public KnapsackComponent getComponent(final int component) {
        if (component < 0 || component > numComponents())
            throw new IllegalStateException(String.format("%s: attempted to get component %d, but component IDs must be between 0 and %d (inclusive).", this.getClass().getSimpleName(), component, numComponents()));
        return new KnapsackComponent(sizes[component], costs[component]);
    }

    @Override
    public Set<Integer> componentSet() {
        final Set<Integer> result = new HashSet<Integer>(numComponents());
        for (int i = 0; i < numComponents(); i++)
            result.add(i);
        assert(repOK());
        return result;
    }
    
    public final boolean repOK() {
        return P_COSTS != null
                && !P_COSTS.isEmpty()
                && P_SIZES != null
                && !P_SIZES.isEmpty()
                && P_KNAPSACK_SIZE != null
                && !P_KNAPSACK_SIZE.isEmpty()
                && sizes != null
                && sizes.length > 0
                && costs != null
                && costs.length > 0
                && sizes.length == costs.length
                && !Double.isNaN(knapsackSize);
    }
    
    public class KnapsackComponent {
        private final double size;
        private final double cost;
        
        public double getSize() { return size; }
        public double getCost() { return cost; }
        
        public KnapsackComponent(final double size, final double cost) {
            this.size = size;
            this.cost = cost;
        }
    }
}
