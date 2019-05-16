/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.util.Parameter;
import java.util.Arrays;

/**
 *
 * @author Eric O. Scott
 */
public class GlobalUpdateRule implements UpdateRule {
    final public static String P_RHO = "rho";
    final public static String P_BEST_STRATEGY = "best-strategy";
    
    public enum BestStrategy { GLOBAL_BEST, ITERATION_BEST };
    private BestStrategy bestStrategy;
    private double rho;
    
    private ConstructiveIndividual best;
    
    public BestStrategy getBestStrategy() { return bestStrategy; }
    
    public double getRho() { return rho; }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        rho = state.parameters.getDouble(base.push(P_RHO), null, 0.0);
        String bestString = state.parameters.getString(base.push(P_BEST_STRATEGY), null);
        try
            {
            bestString = bestString.replace('-', '_');
            bestStrategy = BestStrategy.valueOf(bestString);
            }
        catch (final NullPointerException e)
            {
            state.output.fatal(String.format("%s: invalid value '%s' found for parameter '%s'.  Allowed values are %s.", this.getClass().getSimpleName(), bestString, base.push(P_BEST_STRATEGY), Arrays.asList(BestStrategy.values())));
            }
        catch (final IllegalArgumentException e)
            {
            state.output.fatal(String.format("%s: invalid value '%s' found for parameter '%s'.  Allowed values are %s.", this.getClass().getSimpleName(), bestString, base.push(P_BEST_STRATEGY), Arrays.asList(BestStrategy.values())));
            }
        best = null; // Clear best individual
        assert(repOK());
    }
    
    @Override
    public void updatePheromones(final EvolutionState state, final PheromoneTable pheromones, final Subpopulation subpop) {
        assert(pheromones != null);
        assert(subpop != null);
        
        updateBest(subpop);
        
        assert(best.size() > 0);
        final double delta_pheromone = best.fitness.fitness();
        for (final Object oo : best)
            {
            assert(oo instanceof Component);
            final Component c = (Component) oo;
            
            final double oldPheromone = pheromones.get(state, c, 0); // Using thread 0 because we are in a single-threaded function
            pheromones.set(c, (1-rho)*oldPheromone + rho*delta_pheromone);
            }
        
        assert(repOK());
    }
    
    
    private void updateBest(final Subpopulation subpop) {
        assert(subpop != null);
        switch(bestStrategy)
        {
            case ITERATION_BEST:
                best = (ConstructiveIndividual) subpop.individuals.get(0);
                // flowing through to GLOBAL_BEST on purpose!
                
            case GLOBAL_BEST:
                for (final Individual ind : subpop.individuals) {
                    if ((best == null) || ind.fitness.betterThan(best.fitness))
                        best = (ConstructiveIndividual) ind;
                }
                break;
                
            default:
                throw new IllegalStateException(String.format("%s: Unrecognized update strategy '%s'.  Please report.", this.getClass().getSimpleName(), bestStrategy));
        }
    }
    
    public final boolean repOK()
    {
        return P_RHO != null
                && !P_RHO.isEmpty()
                && P_BEST_STRATEGY != null
                && !P_BEST_STRATEGY.isEmpty()
                && bestStrategy != null
                && Double.isFinite(rho)
                && rho >= 0.0
                && ((best == null) || (best.size() > 0));
    }
}
