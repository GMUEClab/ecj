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
    public void testCostTest4()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(Math.rint(2* Math.sqrt(2)), instance.cost(0, 1), 0.00001);
        assertEquals(Math.rint(2.5), instance.cost(0, 2), 0.00001);
        assertEquals(Math.rint(2.692582403567252), instance.cost(0, 3), 0.00001);
        assertEquals(Math.rint(0.5), instance.cost(1, 2), 0.00001);
        assertEquals(Math.rint(1.118033988749895), instance.cost(1, 3), 0.00001);
        assertEquals(Math.rint(0.7071067811865476), instance.cost(2, 3), 0.00001);
        
        // Symmetric matrix
        assertEquals(instance.cost(0, 1), instance.cost(1, 0), 0.00001);
        assertEquals(instance.cost(0, 2), instance.cost(2, 0), 0.00001);
        assertEquals(instance.cost(0, 3), instance.cost(3, 0), 0.00001);
        assertEquals(instance.cost(1, 2), instance.cost(2, 1), 0.00001);
        assertEquals(instance.cost(1, 3), instance.cost(3, 1), 0.00001);
        assertEquals(instance.cost(2, 3), instance.cost(3, 2), 0.00001);
        
        // Zero diagonal
        assertEquals(0, instance.cost(0, 0), 0.00001);
        assertEquals(0, instance.cost(1, 1), 0.00001);
        assertEquals(0, instance.cost(2, 2), 0.00001);
        assertEquals(0, instance.cost(3, 3), 0.00001);
    }
        
    
    @Test
    public void testCostAtt532a()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(0, 1);
        assertEquals(109, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostAtt532b()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(531, 1);
        assertEquals(1947, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    /** The TSPLIB documentation gives the distance of att532's 'canonical path' for verificiation purposes. */
    public void testCostAtt532c()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(309636, canonicalDistance(instance), 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostBerlin52a()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(0, 1);
        assertEquals(666, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostBerlin52b()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(51, 0);
        assertEquals(1220, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    /** The TSPLIB documentation gives the distance of pcb442's 'canonical path' for verificiation purposes. */
    public void testCostPcb442()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/pcb442.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(221440, canonicalDistance(instance), 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    /** The TSPLIB documentation gives the distance of gr666's 'canonical path' for verificiation purposes. */
    public void testCostGr666()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/gr666.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(423710, canonicalDistance(instance), 0.0);
        assertTrue(instance.repOK());
    }
    
    private double canonicalDistance(final TSPProblem instance)
    {
        assert(instance != null);
        double sum = 0.0;
        for (int i = 1; i < instance.numComponents(); i++)
            sum += instance.cost(i - 1, i);
        sum += instance.cost(instance.numComponents() - 1, 0);
        return sum;
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
