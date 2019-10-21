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
    public final static String P_INITIAL_CONCENTRATION = "initial-concentration";
    public final static String P_INITIALIZE_WITH_NOISE = "initialize-with-noise";
    public final static String P_NOISE = "starting-noise";

    private int numComponents;
    private double initialConcentration;
    private boolean initializeWithNoise;
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
        initializeWithNoise = state.parameters.getBoolean(base.push(P_INITIALIZE_WITH_NOISE), null, true);
        if (initializeWithNoise)
            {
            if (state.parameters.exists(base.push(P_INITIAL_CONCENTRATION), null))
                state.output.warnOnce(String.format("%s: '%s' is set to true, so I'm ignoring the '%s' parameter.  Is this what you intended?", this.getClass().getSimpleName(), base.push(P_INITIALIZE_WITH_NOISE), base.push(P_INITIAL_CONCENTRATION)), base.push(P_INITIAL_CONCENTRATION));
            noise = state.parameters.getDoubleWithDefault(base.push(P_NOISE), null, 0.000001);
            if (noise <= 0.0)
                state.output.fatal(String.format("%s: '%s' is set to '%f', but must be positive.", this.getClass().getSimpleName(), base.push(P_NOISE), noise), base.push(P_NOISE));
            }
        else
            {
            if (state.parameters.exists(base.push(P_INITIAL_CONCENTRATION), null))
                state.output.warnOnce(String.format("%s: '%s' is set to false, so I'm ignoring the '%s' parameter.  Is this what you intended?", this.getClass().getSimpleName(), base.push(P_INITIALIZE_WITH_NOISE), base.push(P_NOISE)), base.push(P_NOISE));
            initialConcentration = state.parameters.getDoubleWithDefault(base.push(P_INITIAL_CONCENTRATION), null, 0.000001);
            }
        assert(repOK());
        }
    
    @Override
    public double get(final EvolutionState state, final Component c, final int thread) {
        assert(c != null);
        if (pheromones.containsKey(c))
            {
            final double value = pheromones.get(c);
            if (value <= 0.0)
                state.output.fatal(String.format("%s: a pheremone value of '%f' was found, but must be positive.", this.getClass().getSimpleName(), value));
            else
                return value;
            }
        else if (initializeWithNoise) // Uninitialized pheromone: replace with a small random number
            {
            final double perturbation = state.random[thread].nextDouble()*noise;
            pheromones.put(c, perturbation);
            return perturbation;
            }
        else // Uninitialized pheromone: replace with a small fixed number
            {
            pheromones.put(c, initialConcentration);
            return initialConcentration;
            }
        throw new IllegalStateException("Unexpected error.  Please report.");
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.numComponents;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.noise) ^ (Double.doubleToLongBits(this.noise) >>> 32));
        hash = 67 * hash + (this.pheromones != null ? this.pheromones.hashCode() : 0);
        return hash;
        }
    
    @Override
    public boolean equals(final Object o)
        {
        if (o == this)
            return true;
        if (!(o instanceof SimplePheromoneTable))
            return false;
        final SimplePheromoneTable ref = (SimplePheromoneTable) o;
        return numComponents == ref.numComponents
            && noise == ref.noise
            && initializeWithNoise == ref.initializeWithNoise
            && initialConcentration == ref.initialConcentration
            && pheromones.equals(ref.pheromones);
        }
    }
