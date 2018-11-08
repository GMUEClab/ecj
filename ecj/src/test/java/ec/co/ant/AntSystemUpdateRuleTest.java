/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Subpopulation;
import ec.co.ConstructiveIndividual;
import ec.co.ant.AntSystemUpdateRule.DepositRule;
import ec.simple.SimpleEvolutionState;
import ec.simple.SimpleFitness;
import ec.util.Output.OutputExitException;
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
public class AntSystemUpdateRuleTest
{
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    private ParameterDatabase params;
    
    public AntSystemUpdateRuleTest()
    {
    }
    
    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        params.set(BASE.push(AntSystemUpdateRule.P_DECAY_RATE), "0.5");
        params.set(BASE.push(AntSystemUpdateRule.P_DEPOSIT_RULE), DepositRule.ANT_CYCLE.toString());
        state = new SimpleEvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
    }

    @Test
    public void testSetup1()
    {
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        assertEquals(instance.getDecayRate(), 0.5, 0.000001);
        assertEquals(instance.getQ(), 1.0, 0.000001);
        assertEquals(instance.getDepositRule(), DepositRule.ANT_CYCLE);
        assertTrue(instance.repOK());
    }

    @Test
    public void testSetup2()
    {
        params.set(BASE.push(AntSystemUpdateRule.P_Q), "4");
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        assertEquals(instance.getDecayRate(), 0.5, 0.000001);
        assertEquals(instance.getQ(), 4.0, 0.000001);
        assertEquals(instance.getDepositRule(), DepositRule.ANT_CYCLE);
        assertTrue(instance.repOK());
    }

    @Test(expected = NumberFormatException.class)
    public void testSetup3()
    {
        params.remove(BASE.push(AntSystemUpdateRule.P_DECAY_RATE));
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
    }

    @Test(expected = OutputExitException.class)
    public void testSetup4()
    {
        params.remove(BASE.push(AntSystemUpdateRule.P_DEPOSIT_RULE));
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
    }

    @Test
    public void testUpdatePheromones1()
    {
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        final List<Double> pheromones = zeroList(16);
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<Individual>()
        {{
            add(createInd(new int[] { 1, 6, 11, 12 }, 500.0));
            add(createInd(new int[] { 1, 6, 11, 12 }, 1000.0));
            add(createInd(new int[] { 3, 13, 6, 8 }, 700.0));
            add(createInd(new int[] { 2, 11, 13, 4}, 800.0));
        }};
        
        final List<Double> expectedResult = zeroList(16);
        expectedResult.set(1, 0.00425);
        expectedResult.set(2, 0.0026785714285714286);
        expectedResult.set(3, 0.004428571428571428);
        expectedResult.set(6, 0.004428571428571428);
        expectedResult.set(7, 0.0026785714285714286);
        expectedResult.set(11, 0.00425);
        
        instance.updatePheromones(state, pheromones, subpop);
        assertEquals(expectedResult, pheromones);
    }
    
    @Test
    public void testUpdatePheromones2()
    {
        state.parameters.set(BASE.push(AntSystemUpdateRule.P_DEPOSIT_RULE), AntSystemUpdateRule.DepositRule.ANT_DENSITY.toString());
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        final List<Double> pheromones = zeroList(16);
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<Individual>()
        {{
            add(createInd(new int[] { 1, 6, 11, 12 }, 500.0));
            add(createInd(new int[] { 1, 6, 11, 12 }, 1000.0));
            add(createInd(new int[] { 3, 13, 6, 8 }, 700.0));
            add(createInd(new int[] { 2, 11, 13, 4}, 800.0));
        }};
        
        final List<Double> expectedResult = zeroList(16);
        expectedResult.set(1, 3.0);
        expectedResult.set(2, 2.0);
        expectedResult.set(3, 3.0);
        expectedResult.set(6, 3.0);
        expectedResult.set(7, 2.0);
        expectedResult.set(11, 3.0);
        
        instance.updatePheromones(state, pheromones, subpop);
        assertEquals(expectedResult, pheromones);
    }
    
    @Test
    public void testUpdatePheromones3()
    {
        state.parameters.set(BASE.push(AntSystemUpdateRule.P_DEPOSIT_RULE), AntSystemUpdateRule.DepositRule.ANT_QUANTITY.toString());
        // TODO set up problem
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        final List<Double> pheromones = zeroList(16);
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<Individual>()
        {{
            add(createInd(new int[] { 1, 6, 11, 12 }, 500.0));
            add(createInd(new int[] { 1, 6, 11, 12 }, 1000.0));
            add(createInd(new int[] { 3, 13, 6, 8 }, 700.0));
            add(createInd(new int[] { 2, 11, 13, 4}, 800.0));
        }};
        
        final List<Double> expectedResult = zeroList(16);
        // TODO hard-code expected output
        /*expectedResult.set(0, 1, 3.0);
        expectedResult.set(0, 2, 1.0);
        expectedResult.set(0, 3, 3.0);
        expectedResult.set(1, 2, 3.0);
        expectedResult.set(1, 3, 2.0);
        expectedResult.set(2, 3, 3.0);*/
        
        instance.updatePheromones(state, pheromones, subpop);
        assertEquals(expectedResult, pheromones);
    }
    
    private ConstructiveIndividual createInd(final int[] components, final double fitness)
    {
        assert(components != null);
        final ConstructiveIndividual ind = new ConstructiveIndividual();
        final List<Integer> componentsList = new ArrayList<Integer>(components.length);
        for (final int c : components)
            componentsList.add(c);
        ind.setComponents(componentsList);
        ind.fitness = new SimpleFitness();
        ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
        return ind;
    }
    
    private List<Double> zeroList(final int length) {
        return new ArrayList<Double>(16) {{
           for (int i = 0; i < length; i++)
               add(0.0);
        }};
    }
}
