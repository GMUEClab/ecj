/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.ConstructiveIndividual;
import ec.util.Parameter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class GreedyConstructionRuleTest
{
    
    public GreedyConstructionRuleTest()
    {
    }
    
    @Before
    public void setUp()
    {
    }

    /**
     * Test of setup method, of class GreedyConstructionRule.
     */
    @Test
    public void testSetup()
    {
        System.out.println("setup");
        EvolutionState state = null;
        Parameter base = null;
        GreedyConstructionRule instance = new GreedyConstructionRule();
        instance.setup(state, base);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of constructSolution method, of class GreedyConstructionRule.
     */
    @Test
    public void testConstructSolution()
    {
        System.out.println("constructSolution");
        EvolutionState state = null;
        ConstructiveIndividual ind = null;
        int startNode = 0;
        PheromoneMatrix pheromones = null;
        GreedyConstructionRule instance = new GreedyConstructionRule();
        ConstructiveIndividual expResult = null;
        ConstructiveIndividual result = instance.constructSolution(state, ind, startNode, pheromones);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of repOK method, of class GreedyConstructionRule.
     */
    @Test
    public void testRepOK()
    {
        System.out.println("repOK");
        GreedyConstructionRule instance = new GreedyConstructionRule();
        boolean expResult = false;
        boolean result = instance.repOK();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
