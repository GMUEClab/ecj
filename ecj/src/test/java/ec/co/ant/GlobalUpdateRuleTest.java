/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Subpopulation;
import ec.app.knapsack.KnapsackProblem;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ant.GlobalUpdateRule.BestStrategy;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.simple.SimpleFitness;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class GlobalUpdateRuleTest {
    private final static Parameter BASE = new Parameter("base");
    private final static Parameter PROBLEM_BASE = new Parameter("prob");
    private final static Parameter PHEROMONES_BASE = new Parameter("pheromones");
    private EvolutionState state;
    private ParameterDatabase params;
    private SimplePheromoneTable pheromones;
    private KnapsackProblem problem;

    public GlobalUpdateRuleTest() {
    }

    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_SIZES), "1 2 3 4 5 6 7 8 9 10");
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_VALUES), "1 2 3 4 5 5 4 3 2 1");
        params.set(PROBLEM_BASE.push(KnapsackProblem.P_KNAPSACK_SIZE), "15");
        params.set(PHEROMONES_BASE.push(SimplePheromoneTable.P_INITIALIZE_WITH_NOISE), "false");
        params.set(BASE.push(GlobalUpdateRule.P_BEST_STRATEGY), BestStrategy.GLOBAL_BEST.toString());
        params.set(BASE.push(GlobalUpdateRule.P_RHO), "0.3");
        state = new SimpleEvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        state.evaluator = new SimpleEvaluator();
        problem = new KnapsackProblem();
        problem.setup(state, PROBLEM_BASE);
        state.evaluator.p_problem = problem;
        pheromones = new SimplePheromoneTable();
        pheromones.setup(state, PHEROMONES_BASE);
    }

    /**
     * When we call setup(), the bestStrategy and rho attributes should be set 
     * from the ParameterDatabase.
     */
    @Test
    public void testSetup1() {
        GlobalUpdateRule instance = new GlobalUpdateRule();
        instance.setup(state, BASE);

        assertEquals(instance.getBestStrategy(), BestStrategy.GLOBAL_BEST);
        assertEquals(instance.getRho(), 0.3, 0.000001);
        assertTrue(instance.repOK());
    }

    /**
     * When we call setup(), the bestStrategy and rho attributes should be set 
     * from the ParameterDatabase.
     */
    @Test
    public void testSetup2() {
        params.set(BASE.push(GlobalUpdateRule.P_BEST_STRATEGY), BestStrategy.ITERATION_BEST.toString());
        GlobalUpdateRule instance = new GlobalUpdateRule();
        instance.setup(state, BASE);

        assertEquals(instance.getBestStrategy(), BestStrategy.ITERATION_BEST);
        assertEquals(instance.getRho(), 0.3, 0.000001);
        assertTrue(instance.repOK());
    }

    /**
     * Test of updatePheromones method, of class GlobalUpdateRule.
     */
    @Test
    public void testUpdatePheromones() {
        GlobalUpdateRule instance = new GlobalUpdateRule();
        instance.setup(state, BASE);
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<Individual>()
        {{
            add(createKnapsackInd(new int[] { 1, 2, 3, 4 }, 500.0));
            add(createKnapsackInd(new int[] { 1, 2, 3, 4 }, 1000.0));
            add(createKnapsackInd(new int[] { 0, 3, 6, 8 }, 700.0));
            add(createKnapsackInd(new int[] { 9, 3, 1, 1}, 800.0));
        }};

        instance.updatePheromones(state, pheromones, subpop.individuals);

        final List<Component> pComponents = problem.getAllComponents();
        assertEquals(0.000001, pheromones.get(state, pComponents.get(0), 0), 0.00000001);
        assertEquals(300.0000007, pheromones.get(state, pComponents.get(1), 0), 0.00000001);
        assertEquals(300.0000007, pheromones.get(state, pComponents.get(2), 0), 0.00000001);
        assertEquals(300.0000007, pheromones.get(state, pComponents.get(3), 0), 0.00000001);
        assertEquals(300.0000007, pheromones.get(state, pComponents.get(4), 0), 0.00000001);
        assertEquals(0.000001, pheromones.get(state, pComponents.get(5), 0), 0.00000001);
        assertEquals(0.000001, pheromones.get(state, pComponents.get(6), 0), 0.00000001);
        assertEquals(0.000001, pheromones.get(state, pComponents.get(7), 0), 0.00000001);
        assertEquals(0.000001, pheromones.get(state, pComponents.get(8), 0), 0.00000001);
        assertEquals(0.000001, pheromones.get(state, pComponents.get(9), 0), 0.00000001);
    }

    /** @returns a solution to 'problem' with a manually designated fitness value. */
    private ConstructiveIndividual createKnapsackInd(final int[] components, final double fitness)
    {
        assert(components != null);
        final ConstructiveIndividual ind = new ConstructiveIndividual();
        final List<Component> pComponents = problem.getAllComponents();
        for (final int c : components)
            ind.add(state, pComponents.get(c));
        ind.fitness = new SimpleFitness();
        ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
        return ind;
    }

}
