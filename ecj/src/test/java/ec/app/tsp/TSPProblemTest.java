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
    
    @Test
    public void testGetComponentID1()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        final int result = instance.getComponentId(531, 0);
        final int result2 = instance.getComponentId(0, 531);
        assertEquals(531, result);
        assertEquals(result, result2);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testGetComponentID2()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        final int result = instance.getComponentId(531, 1);
        final int result2 = instance.getComponentId(1, 531);
        assertEquals(2*532 - 1, result);
        assertEquals(result, result2);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testGetComponentID3()
    {
        state.parameters.set(BASE.push(TSPProblem.P_DIRECTED), "true");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        final int result = instance.getComponentId(531, 0);
        final int result2 = instance.getComponentId(0, 531);
        assertEquals(531*532, result);
        assertEquals(531, result2);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testGetComponentID4()
    {
        state.parameters.set(BASE.push(TSPProblem.P_DIRECTED), "true");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        final int result = instance.getComponentId(531, 1);
        final int result2 = instance.getComponentId(1, 531);
        assertEquals(531*532 + 1, result);
        assertEquals(2*532 - 1, result2);
        assertTrue(instance.repOK());
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
        state.parameters.set(BASE.push(TSPProblem.P_DIRECTED), "true");
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(Math.rint(2* Math.sqrt(2)), instance.cost(1), 0.00001);
        assertEquals(Math.rint(2.5), instance.cost(2), 0.00001);
        assertEquals(Math.rint(2.692582403567252), instance.cost(3), 0.00001);
        assertEquals(Math.rint(0.5), instance.cost(6), 0.00001);
        assertEquals(Math.rint(1.118033988749895), instance.cost(7), 0.00001);
        assertEquals(Math.rint(0.7071067811865476), instance.cost(11), 0.00001);
        
        // Symmetric matrix
        assertEquals(instance.cost(1), instance.cost(4), 0.00001);
        assertEquals(instance.cost(2), instance.cost(8), 0.00001);
        assertEquals(instance.cost(3), instance.cost(12), 0.00001);
        assertEquals(instance.cost(6), instance.cost(9), 0.00001);
        assertEquals(instance.cost(7), instance.cost(13), 0.00001);
        assertEquals(instance.cost(11), instance.cost(14), 0.00001);
        
        // Zero diagonal
        assertEquals(0, instance.cost(0), 0.00001);
        assertEquals(0, instance.cost(5), 0.00001);
        assertEquals(0, instance.cost(10), 0.00001);
        assertEquals(0, instance.cost(15), 0.00001);
        
        assertEquals(Math.rint(2* Math.sqrt(2) + 0.5 + 0.7071067811865476 + 2.692582403567252), canonicalDistance(instance), 0.00001);
    }
        
    
    @Test
    public void testCostAtt532a()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(1);
        assertEquals(109, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostAtt532b()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(instance.getComponentId(531, 1)); // Edge 531—>1
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
        double result = instance.cost(1);
        assertEquals(666, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostBerlin52b()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.cost(instance.getComponentId(51, 0)); // Edge 51—>0
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
        for (int i = 0; i < instance.numNodes() - 1; i++)
            sum += instance.cost(instance.getComponentId(i, i+1));
        sum += instance.cost(instance.getComponentId(instance.numNodes() - 1, 0));
        return sum;
    }
    
    @Test
    public void testNumComponentsDirected()
    {
        final TSPProblem instance = new TSPProblem();
        state.parameters.set(BASE.push(TSPProblem.P_DIRECTED), "true");
        instance.setup(state, BASE);
        assertEquals((int) Math.pow(532, 2), instance.numComponents());
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testNumComponentsUndirected()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(532*(532 + 1)/2, instance.numComponents());
        assertTrue(instance.repOK());
    }
}
