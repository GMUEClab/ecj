/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.tsp.TSPGraph.TSPComponent;
import ec.co.Component;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.util.Output.OutputExitException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        state.evaluator = new SimpleEvaluator();
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
        assertEquals(Math.rint(2* Math.sqrt(2)), instance.getComponent(0, 1).cost(), 0.00001);
        assertEquals(Math.rint(2.5), instance.getComponent(0, 2).cost(), 0.00001);
        assertEquals(Math.rint(2.692582403567252), instance.getComponent(0, 3).cost(), 0.00001);
        assertEquals(Math.rint(0.5), instance.getComponent(1, 2).cost(), 0.00001);
        assertEquals(Math.rint(1.118033988749895), instance.getComponent(1, 3).cost(), 0.00001);
        assertEquals(Math.rint(0.7071067811865476), instance.getComponent(2, 3).cost(), 0.00001);
        
        // Symmetric matrix
        assertEquals(instance.getComponent(0, 1).cost(), instance.getComponent(1, 0).cost(), 0.00001);
        assertEquals(instance.getComponent(0, 2).cost(), instance.getComponent(2, 0).cost(), 0.00001);
        assertEquals(instance.getComponent(0, 3).cost(), instance.getComponent(3, 0).cost(), 0.00001);
        assertEquals(instance.getComponent(1, 2).cost(), instance.getComponent(2, 1).cost(), 0.00001);
        assertEquals(instance.getComponent(1, 3).cost(), instance.getComponent(3, 1).cost(), 0.00001);
        assertEquals(instance.getComponent(2, 3).cost(), instance.getComponent(3, 2).cost(), 0.00001);
        
        // Zero diagonal
        assertEquals(0, instance.getComponent(0, 0).cost(), 0.00001);
        assertEquals(0, instance.getComponent(1, 1).cost(), 0.00001);
        assertEquals(0, instance.getComponent(2, 2).cost(), 0.00001);
        assertEquals(0, instance.getComponent(3, 3).cost(), 0.00001);
        
        assertEquals(Math.rint(2* Math.sqrt(2) + 0.5 + 0.7071067811865476 + 2.692582403567252), canonicalDistance(instance), 0.00001);
    }
        
    
    @Test
    public void testCostAtt532a()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(0, 1).cost();
        assertEquals(109, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostAtt532b()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(531, 1).cost();
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
        double result = instance.getComponent(0, 1).cost();
        assertEquals(666, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testCostBerlin52b()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(51, 0).cost();
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
            sum += instance.getComponent(i, i+1).cost();
        sum += instance.getComponent(instance.numNodes() - 1, 0).cost();
        return sum;
    }
    
    @Test
    public void testNumComponents()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals((int) Math.pow(532, 2), instance.numComponents());
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testGetAllowedComponents1()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<Component>() {{
            add(instance.getComponent(0, 1));
        }});
        
        final List<Component> expected = new ArrayList<Component>() {{
            add(instance.getComponent(1, 2));
            add(instance.getComponent(1, 3));
        }};
        
        final List<Component> result = instance.getAllowedComponents(ind);
        
        assertTrue(result.containsAll(expected));
        assertTrue(expected.containsAll(result));
        assertTrue(instance.repOK());
    }
    
    @Test
    public void testGetAllowedComponents2()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<Component>() {{
            add(instance.getComponent(1, 0));
        }});
        
        final List<Component> expected = new ArrayList<Component>() {{
            add(instance.getComponent(0, 2));
            add(instance.getComponent(0, 3));
        }};
        
        final List<Component> result = instance.getAllowedComponents(ind);
        
        assertTrue(result.containsAll(expected));
        assertTrue(expected.containsAll(result));
        assertTrue(instance.repOK());
    }
}
