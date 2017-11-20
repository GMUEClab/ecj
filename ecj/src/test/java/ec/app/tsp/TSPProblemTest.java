/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Evolve;
import ec.simple.SimpleEvolutionState;
import ec.util.Output.OutputExitException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class TSPProblemTest
{
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    private ParameterDatabase params;
    
    public TSPProblemTest()
    {
    }
    
    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        params.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/att532.tsp");
        state = new SimpleEvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
    }
    
    @Test(expected = OutputExitException.class)
    public void testSetup()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "/dev/null");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
    }
    
    @Test
    public void testDesireability1()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.desireability(1, 2);
        assertEquals(344, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testDesireability2()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.desireability(532, 2);
        assertEquals(6157, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testNumComponents()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(532, instance.numComponents());
        assertTrue(instance.repOK());
    }
}
