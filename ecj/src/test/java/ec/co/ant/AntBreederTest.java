/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Population;
import ec.simple.SimpleEvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class AntBreederTest
{
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    private ParameterDatabase parameters;
    
    public AntBreederTest()
    {
    }
    
    @Before
    public void setUp()
    {
        state = getTestState();
        parameters = getTestParams();
    }
    
    private EvolutionState getTestState()
    {
        final EvolutionState state = new SimpleEvolutionState();
        state.parameters = getTestParams();
        return state;
    }
    
    private ParameterDatabase getTestParams()
    {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(BASE.push(AntBreeder.P_NUM_ANTS), "10");
        params.set(BASE.push(AntBreeder.P_CONSTRUCTION_RULE), "ec.co.aco.TestConstructionRule"); // A test stub that provides a deterministic construction method.
        params.set(BASE.push(AntBreeder.P_UPDATE_RULE), "ec.co.aco.TestUpdateRule"); // XXX We an probably use a real rule here, rather than a stub
        return params;
    }

    //@Test
    public void testGetNumAnts1()
    {
        System.out.println("getNumAnts (10)");
        final AntBreeder instance = new AntBreeder();
        instance.setup(state, BASE);
        assertEquals(10, instance.getNumAnts());
    }

    //@Test
    public void testGetNumAnts2()
    {
        System.out.println("getNumAnts (1)");
        state.parameters.set(BASE.push(AntBreeder.P_NUM_ANTS), "1");
        final AntBreeder instance = new AntBreeder();
        instance.setup(state, BASE);
        assertEquals(1, instance.getNumAnts());
    }

    //@Test(expected = IllegalStateException.class)
    public void testGetNumAnts3()
    {
        System.out.println("getNumAnts (0)");
        state.parameters.set(BASE.push(AntBreeder.P_NUM_ANTS), "0");
        final AntBreeder instance = new AntBreeder();
        instance.setup(state, BASE);
        // This test will fail, because ECJ will System.exit() here instead of throwing an ISE.
    }

    /**
     * Test of getPheremoneMatrix method, of class AntBreeder.
     */
    //@Test
    public void testGetPheremoneMatrix()
    {
        System.out.println("getPheremoneMatrix");
        int i = 0;
        AntBreeder instance = new AntBreeder();
        PheromoneMatrix expResult = null;
        PheromoneMatrix result = instance.getPheremoneMatrix(i);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setup method, of class AntBreeder.
     */
    //@Test
    public void testSetup()
    {
        System.out.println("setup");
        EvolutionState state = null;
        Parameter base = null;
        AntBreeder instance = new AntBreeder();
        instance.setup(state, base);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of breedPopulation method, of class AntBreeder.
     */
    //@Test
    public void testBreedPopulation()
    {
        System.out.println("breedPopulation");
        EvolutionState state = null;
        AntBreeder instance = new AntBreeder();
        Population expResult = null;
        Population result = instance.breedPopulation(state);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
