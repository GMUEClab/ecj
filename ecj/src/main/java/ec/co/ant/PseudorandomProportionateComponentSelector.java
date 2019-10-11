/*
  Copyright 2019 by Sean Luke
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
 * The classic ant transition rule used by Ant Colony System.
 *
 * A coin is flipped with probability <code>base.prob-best</code>.  If it's heads, the next <code>Component</code> is
 * chosen greedily to have the highest heuristic value in the neighborhood.  If it's tails,
 * <code>ProportionateComponentSelect</code> is used instead.
 *
 * @author Eric O. Scott
 */
public class PseudorandomProportionateComponentSelector implements ComponentSelector, Setup {
    public final static String P_PROB_BEST = "prob-best";
    
    private double probBest;
    private ProportionateComponentSelector proportionateSelector;
    
    public double getProbBest() {
        return probBest;
        }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        assert(base != null);
        probBest = state.parameters.getDouble(base.push(P_PROB_BEST), null, 0);
        proportionateSelector = new ProportionateComponentSelector();
        proportionateSelector.setup(state, base);
        assert(repOK());
        }
    
    @Override
    public Component choose(final EvolutionState state, final List<Component> components, final PheromoneTable pheromones, final int thread)
        {
        assert(state != null);
        assert(components != null);
        assert(pheromones != null);
        assert(thread >= 0);
        assert(thread < state.random.length);
        
        if (state.random[thread].nextDouble() <= probBest)
            {
            Component best = null;
            double best_score = Double.NEGATIVE_INFINITY;
            for (final Component c : components)
                {
                final double score = c.desirability()*pheromones.get(state, c, thread);
                if (score > best_score)
                    {
                    best = c;
                    best_score = score;
                    }
                }
            assert(repOK());
            return best;
            }
        else
            {
            assert(repOK());
            return proportionateSelector.choose(state, components, pheromones, thread);
            }
        }
    
    @Override
    public boolean equals(final Object o)
        {
        if (o == this)
            return true;
        if (!(o instanceof PseudorandomProportionateComponentSelector))
            return false;
        final PseudorandomProportionateComponentSelector ref = (PseudorandomProportionateComponentSelector)o;
        return Misc.doubleEquals(probBest, ref.probBest, 0.000001)
            && proportionateSelector.equals(ref.proportionateSelector);
        }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.probBest) ^ (Double.doubleToLongBits(this.probBest) >>> 32));
        hash = 89 * hash + (this.proportionateSelector != null ? this.proportionateSelector.hashCode() : 0);
        return hash;
        }
    
    public final boolean repOK()
        {
        return P_PROB_BEST != null
            && !P_PROB_BEST.isEmpty()
            && Double.isFinite(probBest)
            && probBest >= 0.0
            && probBest <= 1.0
            && proportionateSelector != null;
        }
    
    }
