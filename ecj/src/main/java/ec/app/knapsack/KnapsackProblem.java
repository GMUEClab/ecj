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
 * Definition of a basic knapsack problem.
 *
 * This problem takes a list of sizes (<code>base.sizes</code>) and values (<code>base.values</code>), as well as an
 * overall knapsack size (<code>base.knapsack-size</code>).
 *
 * @author Eric O. Scott
 * @see KnapsackComponent
 * @see ec.co
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
    public List<Component> getAllComponents()
        {
        return new ArrayList<Component>(components); // Defensive copy
        }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        assert(state != null);
        assert(base != null);
        knapsackSize = state.parameters.getDouble(base.push(P_KNAPSACK_SIZE), null);
        allowDuplicates = state.parameters.getBoolean(base.push(P_ALLOW_DUPLICATES), null, false);
        
        final double[] sizes = state.parameters.getDoubles(base.push(P_SIZES), null, 0);
        final double[] values = state.parameters.getDoubles(base.push(P_VALUES), null, 0);
        if (sizes.length != values.length)
            state.output.fatal(String.format("%s: '%s' has %d elements, but '%s' has %d elements.  Must be the same length.", this.getClass().getSimpleName(), base.push(P_SIZES), sizes.length, base.push(P_VALUES), values.length));
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

    /**
     * Choose a random component from the full component set.
     * 
     * @param state The state.  Its PRNG field (state.random) must exist.
     * @param thread The thread the caller is operating on.  If the caller is single-threaded, just set this to zero.
     * @return An component selected at random from all of the non-self-loop edges in the TSP graph.
     */
    @Override
    public KnapsackComponent getArbitraryComponent(final EvolutionState state, final int thread) {
        assert(state != null);
        assert(thread >= 0);
        final KnapsackComponent result = components.get(state.random[thread].nextInt(components.size()));
        assert(repOK());
        return result;
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

    @Override
    public Component getComponentFromString(final String s)
        {
        assert(s != null);
        assert(!s.isEmpty());
        final String error = String.format("%s: failed to decode string representation of %s.  It must have the form '%s[size=M, value=N]' where M, N are floating point numbers, but was '%s'.", this.getClass().getSimpleName(), KnapsackComponent.class.getSimpleName(), KnapsackComponent.class.getSimpleName(), s);
        
        String[] splits = s.split("\\["); // "KnapsackComponent" "size=M, value=N]"
        if (splits.length != 2)
            throw new IllegalArgumentException(error);
        final String name = splits[0].trim();
        if (!name.equals(KnapsackComponent.class.getSimpleName()))
            throw new IllegalArgumentException(error);
        
        splits = splits[1].split(","); // "size=M" "value=N]"
        if (splits.length != 2)
            throw new IllegalArgumentException(error);
        final String sizeStr = splits[0]; // "size=M"
        final String valueStr = splits[1].substring(0, splits[1].length() - 1); // "value=N"
        
        splits = sizeStr.split("="); // "from" "M"
        if (!splits[0].trim().equals("size"))
            throw new IllegalArgumentException(error);
        final double size;
        try {
            size = Double.parseDouble(splits[1]);
            }
        catch (final NumberFormatException e)
            {
            throw new IllegalArgumentException(error);
            }
        
        splits = valueStr.split("="); // "from" "M"
        if (!splits[0].trim().equals("value"))
            throw new IllegalArgumentException(error);
        final double value;
        try {
            value = Double.parseDouble(splits[1]);
            }
        catch (final NumberFormatException e)
            {
            throw new IllegalArgumentException(error);
            }
        
        assert(repOK());
        return new KnapsackComponent(size, value);
        }
    }
