/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.knapsack;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric O. Scott
 */
public class KnapsackProblem extends Problem implements SimpleProblemForm, ConstructiveProblemForm {
    public final static String P_SIZES = "sizes";
    public final static String P_VALUES = "values";
    public final static String P_KNAPSACK_SIZE = "knapsack-size";
    public final static String P_ALLOW_DUPLICATES = "allow-duplicates";
    
    private List<KnapsackComponent> components;
    
    private double knapsackSize;
    private boolean allowDuplicates;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(state != null);
        assert(base != null);
        knapsackSize = state.parameters.getDouble(base.push(P_KNAPSACK_SIZE), null);
        allowDuplicates = state.parameters.getBoolean(base.push(P_ALLOW_DUPLICATES), null, false);
        
        final double[] sizes = state.parameters.getDoubles(base.push(P_SIZES), null, 0);
        final double[] values = state.parameters.getDoubles(base.push(P_VALUES), null, 0, sizes.length);
        assert(sizes.length == values.length);
        components = new ArrayList<KnapsackComponent>(sizes.length);
        for (int i = 0; i < sizes.length; i++)
            components.add(new KnapsackComponent(sizes[i], values[i]));
        assert(repOK());
    }
            
    @Override
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        assert(state != null);
        if (!(ind instanceof ConstructiveIndividual))
            state.output.fatal(String.format("%s requires a %s, but was given a %s.", this.getClass().getSimpleName(), ConstructiveIndividual.class.getSimpleName(), ind.getClass().getSimpleName()));
        ((SimpleFitness)ind.fitness).setFitness(state, totalValue((ConstructiveIndividual)ind), false);
        ind.evaluated = true;
        assert(repOK());
    }
    
    private double totalValue(final ConstructiveIndividual solution) {
        assert(solution != null);
        double value = 0.0;
        for (final Object c : solution)
        {
            if (!(c instanceof KnapsackComponent))
                throw new IllegalArgumentException(String.format("%s: found a %s containing a %s, but must contain only %ss.", this.getClass().getSimpleName(), solution.getClass().getSimpleName(), c.getClass().getSimpleName(), KnapsackComponent.class.getSimpleName()));
            value += ((KnapsackComponent)c).value();
        }
        assert(repOK());
        return value;
    }
    
    private double totalSize(final ConstructiveIndividual solution) {
        assert(solution != null);
        double size = 0.0;
        for (final Object c : solution)
        {
            if (!(c instanceof KnapsackComponent))
                throw new IllegalArgumentException(String.format("%s: found a %s containing a %s, but must contain only %ss.", this.getClass().getSimpleName(), solution.getClass().getSimpleName(), c.getClass().getSimpleName(), KnapsackComponent.class.getSimpleName()));
            size += ((KnapsackComponent)c).size();
        }
        assert(repOK());
        return size;
    }

    @Override
    public boolean isViolated(final ConstructiveIndividual partialSolution, final Component component) {
        assert(partialSolution != null);
        assert(component != null);
        if (!(component instanceof KnapsackComponent))
            throw new IllegalArgumentException(String.format("%s: tried to check constraints on a %s containing a %s, but must be a %s.", this.getClass().getSimpleName(), partialSolution.getClass().getSimpleName(), component.getClass().getSimpleName(), KnapsackComponent.class.getSimpleName()));
        return totalSize(partialSolution) + ((KnapsackComponent)component).size() > knapsackSize;
    }

    @Override
    public int numComponents() {
        assert(repOK());
        return components.size();
    }
    
    public final boolean repOK() {
        return P_VALUES != null
                && !P_VALUES.isEmpty()
                && P_SIZES != null
                && !P_SIZES.isEmpty()
                && P_KNAPSACK_SIZE != null
                && !P_KNAPSACK_SIZE.isEmpty()
                && components != null
                && !components.isEmpty()
                && knapsackSize > 0.0
                && !Double.isNaN(knapsackSize);
    }

    @Override
    public List<Component> getAllowedComponents(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
        
        final double partialSolutionSize = totalSize(partialSolution);
        final List<Component> allowedComponents = new ArrayList<Component>();
        for (final KnapsackComponent c : components)
            if (allowDuplicates || !partialSolution.contains(c))
                if (partialSolutionSize + c.size() <= knapsackSize)
                    allowedComponents.add(c);
        return allowedComponents;
    }

    @Override
    public boolean isCompleteSolution(final ConstructiveIndividual solution) {
        assert(solution != null);
        
        final double size = totalSize(solution);
        for (final KnapsackComponent c : components)
            if (allowDuplicates || !solution.contains(c))
                if (size + c.size() <= knapsackSize)
                    return false;
        return true;
    }
}
