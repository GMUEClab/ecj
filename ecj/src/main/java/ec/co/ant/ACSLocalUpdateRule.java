/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.util.Parameter;
import java.util.List;

/**
 * A "local" pheromone decay rule in the style of Ant Colony System.
 *
 * ACS uses this rule to decay pheromones as soon as they are visited.  The intention is to encourage diversification
 * within the same generation, so that ants are less likely to visit identical paths at the same time.
 *
 * @author Eric O. Scott
 */
public class ACSLocalUpdateRule implements UpdateRule {
    private static final long serialVersionUID = 1;

    final public static String P_RHO = "rho";
    final public static String P_MINIMUM_PHEROMONE = "minimum-pheromone";

    private double rho;
    private double minimumPheromone;

    public double getMinimumPheromone() { return minimumPheromone; }

    public double getRho() { return rho; }

    @Override
    public void setup(final EvolutionState state, final Parameter base)
        {
        assert(state != null);
        assert(base != null);
        rho = state.parameters.getDouble(base.push(P_RHO), null, 0.0);
        minimumPheromone = state.parameters.getDouble(base.push(P_RHO), null, 0.0);
        assert(repOK());
        }

    @Override
    public void updatePheromones(final EvolutionState state, final PheromoneTable pheromones, final List individuals) {
        assert(pheromones != null);
        assert(individuals != null);
        assert(!individuals.isEmpty());

        for (final Object ind : individuals)
            {
            for (final Object oo : (ConstructiveIndividual<?>)ind)
                {
                assert(oo instanceof Component);
                final Component c = (Component) oo;

                final double oldPheromone = pheromones.get(state, c, 0); // Using thread 0 because we are in a single-threaded function
                pheromones.set(c, (1-rho)*oldPheromone + rho*minimumPheromone);
                }
            }

        assert(repOK());
        }

    public final boolean repOK()
        {
        return P_RHO != null
            && !P_RHO.isEmpty()
            && !Double.isInfinite(rho)
            && !Double.isNaN(rho)
            && rho >= 0.0;
        }
    }
