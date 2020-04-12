/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>ANT_CYCLE</code>, <code>ANT_DENSITY</code>, and <code>ANT_QUANTITY</code> pheromone update rules, in the style
 * of the Ant System algorithm.
 *
 * @author Eric O. Scott
 */
public class AntSystemUpdateRule implements UpdateRule
    {
    private static final long serialVersionUID = 1;

    public final static String P_DECAY_RATE = "decay-rate";
    public final static String P_DEPOSIT_RULE = "deposit-rule";
    public final static String P_Q = "Q";
    private double decayRate;
    public enum DepositRule { ANT_CYCLE, ANT_DENSITY, ANT_QUANTITY };
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
        q = state.parameters.exists(base.push(P_Q), null) ? state.parameters.getDouble(base.push(P_Q), null) : 1.0;
        if (q <= 0.0)
            state.output.fatal(String.format("%s: parameter '%s' has a value of %f, but must be positive.", this.getClass().getSimpleName(), base.push(P_Q), q));
        String depositString = state.parameters.getString(base.push(P_DEPOSIT_RULE), null);
        if (depositString == null)
            state.output.fatal(String.format("%s: missing required parameter '%s'.", this.getClass().getSimpleName(), base.push(P_DEPOSIT_RULE)));
        try
            {
            depositString = depositString.replace('-', '_');
            depositRule = DepositRule.valueOf(depositString);
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
    public void updatePheromones(final EvolutionState state, final PheromoneTable pheromones, final List individuals)
        {
        assert(pheromones != null);
        assert(individuals != null);
        assert(!individuals.isEmpty());

        decayPheromones(state, pheromones);

        final Map<Component, Double> contributions = new HashMap<>();
        // Loop through every individual and record its pheremone contributions (scores) for each edge
        for (final Object o : individuals)
            {
            final ConstructiveIndividual<?> ind = (ConstructiveIndividual<?>) o;
            assert(ind.size() > 0);
            for (final Object oo : ind)
                {
                assert(oo instanceof Component);
                final Component c = (Component) oo;
                final double cPheromone = pheromoneContribution(ind, c);
                if (contributions.containsKey(c))
                    contributions.put(c, contributions.get(c) + cPheromone);
                else
                    contributions.put(c, cPheromone);
                }
            }
        // Apply the new pheromones
        for (final Component c : contributions.keySet())
            {
            final double oldPheromone = pheromones.get(state, c, 0); // Using thread 0 because we are in a single-threaded function
            final double newPheromone = oldPheromone + contributions.get(c);
            pheromones.set(c, newPheromone);
            }
        assert(repOK());
        }

    private void decayPheromones(final EvolutionState state, final PheromoneTable pheromones)
        {
        assert(state != null);
        assert(pheromones != null);
        final List<? extends Component> components = ((ConstructiveProblemForm<?>)state.evaluator.p_problem).getAllComponents();
        for (final Component c : components)
            pheromones.set(c, (1.0-decayRate)*pheromones.get(state, c, 0)); // Using thread 0 because we are in a single-threaded function
        }

    private double pheromoneContribution(final ConstructiveIndividual<?> ind, final Component component)
        {
        assert(ind != null);
        assert(component != null);
        final double fitness = ind.fitness.fitness();
        switch (depositRule)
            {
            case ANT_CYCLE:
                assert(fitness > 0);
                return q*fitness;
            case ANT_DENSITY:
                return q;
            case ANT_QUANTITY:
                return q*component.desirability();
            default:
                throw new IllegalStateException(String.format("%s: no deposit rule logic implemented for %s.", this.getClass().getSimpleName(), depositRule));
            }
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
