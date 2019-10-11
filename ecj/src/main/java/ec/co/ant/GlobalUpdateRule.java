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
import java.util.List;

/**
 * A "global" pheromone update rule in the style of Ant Colony System.
 *
 * Either the best ant of the run (<code>GLOBAL_BEST</code>) or best of the generation (<code>ITERATION_BEST</code>)
 * is chosen according to <code>base.best-strategy</code> and used to deposit pheromones.
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
    public void updatePheromones(final EvolutionState state, final PheromoneTable pheromones, final List individuals) {
        assert(pheromones != null);
        assert(individuals != null);
        assert(!individuals.isEmpty());

        updateBest(individuals);

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


    private void updateBest(final List<ConstructiveIndividual> individuals) {
        assert(individuals != null);
        assert(!individuals.isEmpty());
        switch(bestStrategy)
            {
            case ITERATION_BEST:
                best = individuals.get(0);
                // flowing through to GLOBAL_BEST on purpose!

            case GLOBAL_BEST:
                for (final Individual ind : individuals) {
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
            && !Double.isInfinite(rho)
            && !Double.isNaN(rho)
            && rho >= 0.0
            && ((best == null) || (best.size() > 0));
        }
    }
