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
import ec.app.tsp.TSPProblem;
import ec.co.ConstructiveIndividual;
import ec.co.ant.AntSystemUpdateRule.DepositRule;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.simple.SimpleFitness;
import ec.util.Output;
import ec.util.Output.OutputExitException;
import ec.util.OutputException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
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
    public void testUpdatePheremoneMatrix()
    {
        final AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.setup(state, BASE);
        final PheromoneMatrix matrix = new PheromoneMatrix(4);
        matrix.initZero();
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<Individual>()
        {{
            add(buildPath(new int[] { 0, 1, 2, 3 }, 500.0));
            add(buildPath(new int[] { 0, 1, 2, 3 }, 1000.0));
            add(buildPath(new int[] { 0, 3, 1, 2 }, 700.0));
            add(buildPath(new int[] { 0, 2, 3, 1 }, 800.0));
        }};
        
        final PheromoneMatrix expectedResult = new PheromoneMatrix(4);
        expectedResult.set(0, 1, 0.00425);
        expectedResult.set(0, 2, 0.0026785714285714286);
        expectedResult.set(0, 3, 0.004428571428571428);
        expectedResult.set(1, 2, 0.004428571428571428);
        expectedResult.set(1, 3, 0.0026785714285714286);
        expectedResult.set(2, 3, 0.00425);
        
        instance.updatePheremoneMatrix(matrix, subpop);
        assertEquals(expectedResult, matrix);
    }
    
    private ConstructiveIndividual buildPath(final int[] path, final double fitness)
    {
        assert(path != null);
        final ConstructiveIndividual ind = new ConstructiveIndividual();
        ind.path = path;
        ind.fitness = new SimpleFitness();
        ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
        return ind;
    }
}
