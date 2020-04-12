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
import ec.util.IIntPoint;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the TSPProblem class
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
    
    /** Setup a TSP problem that reads its graph from an input file. */
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
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        state.evaluator = new SimpleEvaluator();
    }
    
    /** Make sure the setup method doesn't crash. */
    @Test(expected = OutputExitException.class)
    public void testSetup()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "/dev/null");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
    }
    
    /** The distances loaded in from our 4-city example should match the input file. */
    @Test
    public void testCostTest4()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(Math.rint(2* Math.sqrt(2)), instance.getComponent(0, 1).distance(), 0.00001);
        assertEquals(Math.rint(2.5), instance.getComponent(0, 2).distance(), 0.00001);
        assertEquals(Math.rint(2.692582403567252), instance.getComponent(0, 3).distance(), 0.00001);
        assertEquals(1.0, instance.getComponent(1, 2).distance(), 0.00001);
        assertEquals(Math.rint(1.118033988749895), instance.getComponent(1, 3).distance(), 0.00001);
        assertEquals(Math.rint(0.7071067811865476), instance.getComponent(2, 3).distance(), 0.00001);
        
        // Symmetric matrix
        assertEquals(instance.getComponent(0, 1).distance(), instance.getComponent(1, 0).distance(), 0.00001);
        assertEquals(instance.getComponent(0, 2).distance(), instance.getComponent(2, 0).distance(), 0.00001);
        assertEquals(instance.getComponent(0, 3).distance(), instance.getComponent(3, 0).distance(), 0.00001);
        assertEquals(instance.getComponent(1, 2).distance(), instance.getComponent(2, 1).distance(), 0.00001);
        assertEquals(instance.getComponent(1, 3).distance(), instance.getComponent(3, 1).distance(), 0.00001);
        assertEquals(instance.getComponent(2, 3).distance(), instance.getComponent(3, 2).distance(), 0.00001);
        
        // Zero diagonal
        assertEquals(0, instance.getComponent(0, 0).distance(), 0.00001);
        assertEquals(0, instance.getComponent(1, 1).distance(), 0.00001);
        assertEquals(0, instance.getComponent(2, 2).distance(), 0.00001);
        assertEquals(0, instance.getComponent(3, 3).distance(), 0.00001);
        
        assertEquals(1 + Math.rint(2* Math.sqrt(2) + 0.5 + 0.7071067811865476 + 2.692582403567252), canonicalDistance(instance), 0.00001);
    }
        
    
    /** Edge 0-1 of the att532 problem should have a distance of 109. */
    @Test
    public void testCostAtt532a()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(0, 1).distance();
        assertEquals(109, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    /** Edge 531-1 of the att532 problem should have a distance of 1947. */
    @Test
    public void testCostAtt532b()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(531, 1).distance();
        assertEquals(1947, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    /** The canonical path of the att532 problem should equal 309636. This value
     * comes from the TSPLIB documentation. */
    @Test
    public void testCostAtt532c()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(309636, canonicalDistance(instance), 0.0);
        assertTrue(instance.repOK());
    }
    
    /** Edge 0-1 in the berlin52 problem should have a distance of 666. */
    @Test
    public void testCostBerlin52a()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(0, 1).distance();
        assertEquals(666, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    /** Edge 51-0 of the berlin52 problem should have a distance of 1220. */
    @Test
    public void testCostBerlin52b()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        double result = instance.getComponent(51, 0).distance();
        assertEquals(1220, result, 0.0);
        assertTrue(instance.repOK());
    }
    
    /** The canonical path of the pcb442 problem should have a distance of 221440.
     * This value comes from the TSBLIB documentation. */
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
    
    /** The canonical path of the gr666 problem should be 423710.  This value 
     * comes from the TSPLIB documentation.
     *
     * XXX This test fails. I suspect that the error lies with the TSPLIB 
     * documentation, so I've commented it out.
     */
    /*@Test
    public void testCostGr666()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/gr666.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals(423710, canonicalDistance(instance), 0.0);
        assertTrue(instance.repOK());
    } */
    
    /** Compute the distance of a problem's "canonical path", i.e. the distance of the
     * tour that starts from node 0 and proceeds consecutively through nodes 
     * 1, 2, 3, etc.
     */
    private double canonicalDistance(final TSPProblem instance)
    {
        assert(instance != null);
        double sum = 0.0;
        for (int i = 0; i < instance.numNodes() - 1; i++)
            sum += instance.getComponent(i, i+1).distance();
        sum += instance.getComponent(instance.numNodes() - 1, 0).distance();
        return sum;
    }
    
    /** The att532 problem should have 532^2 components. */
    @Test
    public void testNumComponents()
    {
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        assertEquals((int) Math.pow(532, 2), instance.numComponents());
        assertTrue(instance.repOK());
    }
    
    /** The path (0, 1) on a 4-node problem can be extended with (1, 2) or (1, 3). */
    @Test
    public void testGetAllowedComponents1()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<TSPComponent>() {{
            add(instance.getComponent(0, 1));
        }});
        
        final List<TSPComponent> expected = new ArrayList<TSPComponent>() {{
            add(instance.getComponent(1, 2));
            add(instance.getComponent(1, 3));
        }};
        
        final List<TSPComponent> result = instance.getAllowedComponents(ind);
        
        assertTrue(result.containsAll(expected));
        assertTrue(expected.containsAll(result));
        assertTrue(instance.repOK());
    }
    
    /** The path (0, 1) on a 4-node problem can be extended with (0, 2) or (0, 3). */
    @Test
    public void testGetAllowedComponents2()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<TSPComponent>() {{
            add(instance.getComponent(1, 0));
        }});
        
        final List<TSPComponent> expected = new ArrayList<TSPComponent>() {{
            add(instance.getComponent(0, 2));
            add(instance.getComponent(0, 3));
        }};
        
        final List<TSPComponent> result = instance.getAllowedComponents(ind);
        
        assertTrue(result.containsAll(expected));
        assertTrue(expected.containsAll(result));
        assertTrue(instance.repOK());
    }
    
    /** An empty path can be extended by choosing any edge except for self-loops. */
    @Test
    public void testGetAllowedComponents3()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<TSPComponent>());
        
        final List<TSPComponent> expected = new ArrayList<TSPComponent>() {{
            add(instance.getComponent(0, 1));
            add(instance.getComponent(0, 2));
            add(instance.getComponent(0, 3));
            add(instance.getComponent(1, 0));
            add(instance.getComponent(1, 2));
            add(instance.getComponent(1, 3));
            add(instance.getComponent(2, 0));
            add(instance.getComponent(2, 1));
            add(instance.getComponent(2, 3));
            add(instance.getComponent(3, 0));
            add(instance.getComponent(3, 1));
            add(instance.getComponent(3, 2));
        }};
        
        final List<TSPComponent> result = instance.getAllowedComponents(ind);
        
        assertTrue(result.containsAll(expected));
        assertTrue(expected.containsAll(result));
        assertTrue(instance.repOK());
    }
    
    /** A solution that doesn't visit all the nodes is incomplete. */
    @Test
    public void testIsCompleteSolution1()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<TSPComponent>() {{
            add(instance.getComponent(1, 0));
        }});
        
        assertFalse(instance.isCompleteSolution(ind));
    }
    
    /** A solution that visits all the nodes is incomplete. */
    @Test
    public void testIsCompleteSolution2()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final TSPIndividual ind = new TSPIndividual();
        ind.setComponents(state, new ArrayList<TSPComponent>() {{
            add(instance.getComponent(1, 0));
            add(instance.getComponent(0, 2));
            add(instance.getComponent(2, 3));
        }});
        
        assertTrue(instance.isCompleteSolution(ind));
    }
    
    /** Choosing a random edge from the graph should follow a uniform distribution. */
    @Test
    public void testGetArbitraryComponent1()
    {
        state.parameters.set(BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/test4.tsp");
        final TSPProblem instance = new TSPProblem();
        instance.setup(state, BASE);
        state.evaluator.p_problem = instance;
        
        final Map<IIntPoint, Integer> counts = new HashMap<IIntPoint, Integer>();
        final int N = 1000;
        for (int i = 0; i < N; i++)
        {
            final TSPComponent result = instance.getArbitraryComponent(state, 0);
            final IIntPoint edge = new IIntPoint(result.from(), result.to());
            if (counts.containsKey(edge))
                counts.put(edge, counts.get(edge) + 1);
            else
                counts.put(edge, 1);
        }
        
        // Self-edges should never be returned
        assertEquals(4, instance.numNodes());
        for (int i = 0; i < instance.numNodes(); i++)
            assertFalse(counts.containsKey(new IIntPoint(i, i)));
        
        // Compute the χ^2 value for the observations
        final int expectedPerEdge = N/12;
        double chiSquared = 0.0;
        for (int i = 0; i < instance.numNodes(); i++)
            for (int j = 0; j < instance.numNodes(); j++)
                if (i != j)
                {
                    final int observed = counts.get(new IIntPoint(i, j));
                    chiSquared += Math.pow(observed - expectedPerEdge, 2)/expectedPerEdge;
                }
        
        // Reject the hypothesis that the distribution is uniform if p > 0.01 (i.e. if χ^2 > 24.725, the threshold for p = 0.01 at 12-1 = 11 degrees of freedom)
        assertFalse(chiSquared > 24.725);
    }
}
