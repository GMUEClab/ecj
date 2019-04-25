/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.knapsack.KnapsackProblem;
import ec.co.Component;
import static ec.co.ant.ProportionateComponentSelectorTest.createKnapsackComponents;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.List;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class PseudorandomProportionateComponentSelectorTest {
    private final static Parameter BASE = new Parameter("base");
    private final static Parameter PROBLEM_BASE = new Parameter("prob");
    private final static Parameter PHEROMONE_BASE = new Parameter("pheromones");
    private EvolutionState state;
    private ParameterDatabase params;
    private KnapsackProblem problem;
    
    @Before
    public void setUp() {
        params = new ParameterDatabase();
        params.set(BASE.push(PseudorandomProportionateComponentSelector.P_PROB_BEST), "0.3");
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
    
    /**
     * After setting up, the probability of choosing the best individual should 
     * be set.
     */
    @Test
    public void testSetup() {
        final PseudorandomProportionateComponentSelector instance = new PseudorandomProportionateComponentSelector();
        instance.setup(state, BASE);
        
        assertEquals(0.3, instance.getProbBest(), 0.000001);
        assertTrue(instance.repOK());
    }
    
    /**
     * If we call the choose method 1000 times, the distribution of the selected
     * components should match the frequency predicted by the proportionate 
     * selection equation.  In this case, the third component is the best.
     */
    @Test
    public void testChoose1() {
        final List<Component> components = createKnapsackComponents(new double[] { 2.3528811 , 1.53512392, 2.59346391, 2.42995666, 0.4010784 });
        final PheromoneTable pheromones = createPheromoneTable(new double[] { 1.00587948, 0.31695738, 3.17696856, 0.2028905 , 1.39808347 }, components);
        final PseudorandomProportionateComponentSelector instance = new PseudorandomProportionateComponentSelector();
        instance.setup(state, BASE);
        
        final int N = 1000;
        final double alpha = 0.01;
        final long[] observedCounts = new long[components.size()];
        for (int i = 0; i < N; i++) {
            final Component c = instance.choose(state, components, pheromones, 0);
            observedCounts[components.indexOf(c)]++;
        }
        
        final double[] expected = new double[] { 167.29355019,  72.68298116, 615.19608775,  76.60131187,
        68.22606902 };
        
        assertFalse((new ChiSquareTest()).chiSquareTest(expected, observedCounts, alpha));
        assertTrue(instance.repOK());
    }
    
    /**
     * If we call the choose method 1000 times, the distribution of the selected
     * components should match the frequency predicted by the proportionate 
     * selection equation.  In this case the first component is the best, and
     * some very small values are included in the pheromone table.
     */
    @Test
    public void testChoose2() {
        final List<Component> components = createKnapsackComponents(new double[] { 2.3528811 , 1.53512392, 2.59346391, 2.42995666, 0.4010784 });
        final PheromoneTable pheromones = createPheromoneTable(new double[] { 1.00587948, 0.0001, 0.0001, 0.2028905 , 1.39808347 }, components);
        final PseudorandomProportionateComponentSelector instance = new PseudorandomProportionateComponentSelector();
        instance.setup(state, BASE);
        
        final int N = 1000;
        final double alpha = 0.01;
        final long[] observedCounts = new long[components.size()];
        for (int i = 0; i < N; i++) {
            final Component c = instance.choose(state, components, pheromones, 0);
            observedCounts[components.indexOf(c)]++;
        }
        
        final double[] expected = new double[] { 671.55074792, 2.86728749, 3.92747385, 170.12774661, 151.52674412 };
        
        assertFalse((new ChiSquareTest()).chiSquareTest(expected, observedCounts, alpha));
        assertTrue(instance.repOK());
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
