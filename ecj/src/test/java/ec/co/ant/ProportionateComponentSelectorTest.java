/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.knapsack.KnapsackComponent;
import ec.app.knapsack.KnapsackProblem;
import ec.co.Component;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Eric O. Scott
 */
public class ProportionateComponentSelectorTest {
    private final static Parameter BASE = new Parameter("base");
    private final static Parameter PROBLEM_BASE = new Parameter("prob");
    private final static Parameter PHEROMONE_BASE = new Parameter("pheromones");
    private EvolutionState state;
    private ParameterDatabase params;
    private KnapsackProblem problem;
        
    public ProportionateComponentSelectorTest() {
    }
    
    @Before
    public void setUp() {
        params = new ParameterDatabase();
        params.set(BASE.push(ProportionateComponentSelector.P_ALPHA), "0.5");
        params.set(BASE.push(ProportionateComponentSelector.P_BETA), "0.6");
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_SIZES), "1 2 3 4 5");
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_VALUES), "2.3528811 1.53512392 2.59346391 2.42995666 0.4010784");
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_KNAPSACK_SIZE), "12");
        params.set(PHEROMONE_BASE.push(SimplePheromoneTable.P_INITIALIZE_WITH_NOISE), "false");
        state = new SimpleEvolutionState();
        state.parameters = params;
        state.evaluator = new SimpleEvaluator();
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        problem = new KnapsackProblem();
        problem.setup(state, PROBLEM_BASE);
        state.evaluator.p_problem = problem;
    }

    /** After setup, our alpha and beta parameters should be set. */
    @Test
    public void testSetup() {
        final ProportionateComponentSelector instance = new ProportionateComponentSelector();
        instance.setup(state, BASE);
        assertEquals(0.5, instance.getAlpha(), 0.00001);
        assertEquals(0.6, instance.getBeta(), 0.00001);
        assertTrue(instance.repOK());
    }

    /**
     * If we call the choose method 1000 times, the distribution of the selected
     * components should match the frequency predicted by the proportionate 
     * selection equation.
     */
    @Test
    public void testChoose1() {
        final List<Component> components = createKnapsackComponents(new double[] { 2.3528811 , 1.53512392, 2.59346391, 2.42995666, 0.4010784 });
        final PheromoneTable pheromones = createPheromoneTable(new double[] { 1.00587948, 0.31695738, 3.17696856, 0.2028905 , 1.39808347 }, components);
        final ProportionateComponentSelector instance = new ProportionateComponentSelector();
        instance.setup(state, BASE);
        
        final int N = 1000;
        final double alpha = 0.01;
        final long[] observedCounts = new long[components.size()];
        for (int i = 0; i < N; i++) {
            final Component c = instance.choose(state, components, pheromones, 0);
            observedCounts[components.indexOf(c)]++;
        }
        
        final double[] expected = new double[] { 238.99078598960045, 103.83283023068613, 450.28012536317226, 109.43044552669576, 97.46581288984535 };
        
        assertFalse((new ChiSquareTest()).chiSquareTest(expected, observedCounts, alpha));
        assertTrue(instance.repOK());
    }

    /**
     * If we call the choose method 1000 times, the distribution of the selected
     * components should match the frequency predicted by the proportionate 
     * selection equation.  This cases includes some very small values in the 
     * pheromone table.
     */
    @Test
    public void testChoose2() {
        final List<Component> components = createKnapsackComponents(new double[] { 2.3528811 , 1.53512392, 2.59346391, 2.42995666, 0.4010784 });
        final PheromoneTable pheromones = createPheromoneTable(new double[] { 1.00587948, 0.0001, 0.0001, 0.2028905 , 1.39808347 }, components);
        final ProportionateComponentSelector instance = new ProportionateComponentSelector();
        instance.setup(state, BASE);
        
        final int N = 1000;
        final double alpha = 0.01;
        final long[] observedCounts = new long[components.size()];
        for (int i = 0; i < N; i++) {
            final Component c = instance.choose(state, components, pheromones, 0);
            observedCounts[components.indexOf(c)]++;
        }
        
        final double[] expected = new double[] { 5307.867827490876, 40.96124988314692, 56.10676929420844, 2430.3963801534023, 2164.6677731783657 };
        
        assertFalse((new ChiSquareTest()).chiSquareTest(expected, observedCounts, alpha));
        assertTrue(instance.repOK());
    }
    
    private static List<Component> createKnapsackComponents(final double[] costs)
    {
        return new ArrayList<Component>(costs.length) {{
            for (final double c : costs)
                add(new KnapsackComponent(c, c));
            }};
    }
    
    private PheromoneTable createPheromoneTable(final double[] pheromones, final List<Component> components)
    {
        assert(pheromones.length == components.size());
        final PheromoneTable result = new SimplePheromoneTable();
        result.setup(state, PHEROMONE_BASE);
        for (int i = 0; i < components.size(); i++)
            result.set(components.get(i), pheromones[i]);
        return result;
    }
}
