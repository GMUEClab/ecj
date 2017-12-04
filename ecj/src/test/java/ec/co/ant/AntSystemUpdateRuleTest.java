/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.Subpopulation;
import ec.app.tsp.TSPProblem;
import ec.co.ant.AntSystemUpdateRule.DepositRule;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.util.Output;
import ec.util.Output.OutputExitException;
import ec.util.OutputException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
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
        PheromoneMatrix matrix = null;
        Subpopulation subpop = null;
        AntSystemUpdateRule instance = new AntSystemUpdateRule();
        instance.updatePheremoneMatrix(matrix, subpop);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
