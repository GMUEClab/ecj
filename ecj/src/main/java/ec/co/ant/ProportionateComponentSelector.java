/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a function that chooses a <code>Component</code> stochastically in the style of
 * Ant System.  Each component's probability of being chosen is proportional to
 * the product of its phereomone concentration and its heuristic desirability, each
 * weighted according to an exponential scaling factor (<code>alpha</code> and <code>beta</code>,
 * respectively).
 * 
 * @author Eric O. Scott
 */
public class ProportionateComponentSelector implements ComponentSelector, Setup {
    public final static String P_ALPHA = "alpha";
    public final static String P_BETA = "beta";
    private double alpha;
    private double beta;
    
    public double getAlpha()
        {
        return alpha;
        }
    
    public double getBeta()
        {
        return beta;
        }
    
    public ProportionateComponentSelector() { }
    
    public ProportionateComponentSelector(final double alpha, final double beta)
        {
        if (!Double.isFinite(alpha))
            throw new IllegalArgumentException(String.format("%s: alpha is %f, but must be finite.", this.getClass().getSimpleName(), alpha));
        if (!Double.isFinite(beta))
            throw new IllegalArgumentException(String.format("%s: beta is %f, but must be finite.", this.getClass().getSimpleName(), beta));
        this.alpha = alpha;
        this.beta = beta;
        assert(repOK());
        }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(state != null);
        assert(base != null);
        alpha = state.parameters.getDouble(base.push(P_ALPHA), null, 0);
        beta = state.parameters.getDouble(base.push(P_BETA), null, 0);
        assert(repOK());
        }

    @Override
    public Component choose(final EvolutionState state, final List<Component> components, final PheromoneTable pheromones, final int thread) {
        assert(state != null);
        assert(state.random != null);
        assert(components != null);
        assert(!components.isEmpty());
        assert(pheromones != null);
        assert(thread >= 0);
        
        double denominator = 0.0;
        final List<Double> scores = new ArrayList<Double>();
        for (final Component c : components)
            {
            final double tau = pheromones.get(state, c, thread);
            final double eta = c.desirability();
            final double score = Math.pow(tau, alpha)*Math.pow(eta, beta);
            scores.add(score);
            denominator += score;
            }
        assert(!Double.isInfinite(denominator));
        assert(!Double.isNaN(denominator));
        assert(denominator >= 0);
        
        final double dart = state.random[thread].nextDouble();
        int i = 0;
        double val = scores.get(0)/denominator;
        while (val < dart)
            val += scores.get(++i)/denominator;
        assert(i < components.size());
        assert(repOK());
        return components.get(i);
        }
    
    public final boolean repOK()
        {
        return P_ALPHA != null
            && !P_ALPHA.isEmpty()
            && P_BETA != null
            && !P_BETA.isEmpty()
            && !Double.isInfinite(alpha)
            && !Double.isNaN(alpha)
            && !Double.isInfinite(beta)
            && !Double.isNaN(beta);
        }
    }
