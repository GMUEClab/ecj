/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.co.ConstructiveIndividual;
import ec.util.IIntPoint;
import ec.util.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eric O. Scott
 */
public class AntSystemUpdateRule implements UpdateRule
{
    public final static String P_DECAY_RATE = "decayRate";
    private double decayRate;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        decayRate = state.parameters.getDouble(base.push(P_DECAY_RATE), null);
        if (decayRate < 0.0 || decayRate >= 1.0 || Double.isInfinite(decayRate) || Double.isNaN(decayRate))
            state.output.fatal(String.format("%s: '%s' parameter is set to '%f,' but must be on the interval [0,1).", this.getClass().getSimpleName(), base.push(P_DECAY_RATE), decayRate));
        assert(repOK());
    }

    @Override
    public void updatePheremoneMatrix(final PheromoneMatrix matrix, final Subpopulation subpop)
    {
        assert(matrix != null);
        assert(subpop != null);
        final Map<IIntPoint, Double> scores = new HashMap();
        for (final Individual o : subpop.individuals)
            {
            assert(o instanceof ConstructiveIndividual);
            final ConstructiveIndividual ind = (ConstructiveIndividual) o;
            assert(ind.genomeLength() > 0);
            int currentNode = ind.genome[0];
            for (int i = 1; i < ind.genomeLength(); i++)
                {
                final IIntPoint edge = new IIntPoint(currentNode, ind.genome[i]);
                final double fitness = ind.fitness.fitness();
                assert(fitness > 0);
                final double edgeScore = 1.0/fitness;
                if (scores.containsKey(edge))
                    scores.put(edge, scores.get(edge) + edgeScore);
                else
                    scores.put(edge, edgeScore);
                }
            }
        for (final IIntPoint edge : scores.keySet())
            {
            final double oldPheromone = matrix.get(edge.x, edge.y);
            final double newPheromone = (1.0-decayRate) * oldPheromone + scores.get(edge);
            matrix.set(edge.x, edge.y, newPheromone);
            }
        assert(repOK());
    }
    
    public final boolean repOK()
    {
        return P_DECAY_RATE != null
                && !P_DECAY_RATE.isEmpty()
                && !Double.isInfinite(decayRate)
                && !Double.isNaN(decayRate)
                && decayRate >= 0.0
                && decayRate < 1.0;
    }
}
