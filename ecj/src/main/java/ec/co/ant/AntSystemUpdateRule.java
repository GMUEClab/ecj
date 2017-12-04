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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eric O. Scott
 */
public class AntSystemUpdateRule implements UpdateRule
{
    public final static String P_DECAY_RATE = "decayRate";
    public final static String P_DEPOSIT_RULE = "depositRule";
    public final static String P_Q = "Q";
    private double decayRate;
    public enum DepositRule { ANT_CYCLE };
    private DepositRule depositRule;
    private double q;
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        decayRate = state.parameters.getDouble(base.push(P_DECAY_RATE), null);
        if (decayRate < 0.0 || decayRate >= 1.0 || Double.isInfinite(decayRate) || Double.isNaN(decayRate))
            state.output.fatal(String.format("%s: '%s' parameter is set to '%f,' but must be on the interval [0,1).", this.getClass().getSimpleName(), base.push(P_DECAY_RATE), decayRate));
        final String depositString = state.parameters.getString(base.push(P_DEPOSIT_RULE), null);
        try
            {
            depositRule = DepositRule.valueOf(depositString);
            if (depositRule.equals(DepositRule.ANT_CYCLE))
                {
                q = state.parameters.containsKey(base.push(P_Q)) ? state.parameters.getDouble(base.push(P_Q), null) : 1.0;
                if (q <= 0.0)
                    state.output.fatal(String.format("%s: parameter '%s' has a value of %f, but must be positive.", this.getClass().getSimpleName(), base.push(P_Q), q));
                }
            }
        catch (final NullPointerException e)
            {
            state.output.fatal(String.format("%s: invalid value '%s' found for parameter '%s'.  Allowed values are %s.", this.getClass().getSimpleName(), depositString, base.push(P_DEPOSIT_RULE), Arrays.asList(DepositRule.values())));
            }
        catch (final IllegalArgumentException e)
            {
            state.output.fatal(String.format("%s: invalid value '%s' found for parameter '%s'.  Allowed values are %s.", this.getClass().getSimpleName(), depositString, base.push(P_DEPOSIT_RULE), Arrays.asList(DepositRule.values())));
            }
        assert(repOK());
    }
    
    public double getDecayRate()
    {
        return decayRate;
    }
    
    public DepositRule getDepositRule()
    {
        return depositRule;
    }
    
    public double getQ()
    {
        return q;
    }

    @Override
    public void updatePheremoneMatrix(final PheromoneMatrix matrix, final Subpopulation subpop)
    {
        assert(matrix != null);
        assert(subpop != null);
        final Map<IIntPoint, Double> newPheremones = new HashMap();
        // Loop through every individual and record its pheremone contributions (scores)
        for (final Individual o : subpop.individuals)
            {
            assert(o instanceof ConstructiveIndividual);
            final ConstructiveIndividual ind = (ConstructiveIndividual) o;
            assert(ind.pathLength() > 0);
            int currentNode = ind.path[0];
            for (int i = 1; i < ind.pathLength(); i++)
                {
                final IIntPoint edge = new IIntPoint(currentNode, ind.path[i]);
                final double indPheromone = pheromoneContribution(ind, i);
                if (newPheremones.containsKey(edge))
                    newPheremones.put(edge, newPheremones.get(edge) + indPheromone); // 
                else
                    newPheremones.put(edge, indPheromone);
                }
            }
        // Apply the new pheromones
        for (final IIntPoint edge : newPheremones.keySet())
            {
            final double oldPheromone = matrix.get(edge.x, edge.y);
            final double newPheromone = (1.0-decayRate) * oldPheromone + newPheremones.get(edge);
            matrix.set(edge.x, edge.y, newPheromone);
            }
        assert(repOK());
    }
    
    private double pheromoneContribution(final ConstructiveIndividual ind, final int i)
    {
        assert(ind != null);
        assert(i >= 0);
        assert(i < ind.pathLength());
        if (depositRule.equals(DepositRule.ANT_CYCLE))
            {
            final double fitness = ind.fitness.fitness();
            assert(fitness > 0);
            return q/fitness;
            }
        throw new IllegalStateException(String.format("%s: no deposit rule logic implemented for %s.", this.getClass().getSimpleName(), depositRule));
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
