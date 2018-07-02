/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.simple;

import ec.EvolutionState;
import ec.Evolve;
import ec.Initializer;
import ec.Population;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class SimpleBreederTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    
    public SimpleBreederTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();
        state.parameters.set(new Parameter(Initializer.P_POP).push(Population.P_SIZE), "2");
        }

    @Test
    public void testSetup()
        {
        SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        }
    
    /** Check that elitism defaults to false. */
    public void testUsingElitism1()
        {
        SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        assertEquals(false, instance.usingElitism(0));
        assertEquals(false, instance.usingElitism(1));
        }

    /** Try to check elitism for a subpopulation that doesn't exist. */
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testUsingElitism2()
        {
        SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        instance.usingElitism(2);
        }
    
    /** Check elites = 5. */
    @Test
    public void testUsingElitism3()
        {
        state.parameters.set(BASE.push(SimpleBreeder.P_ELITE).push("0"), "5");
        SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        assertEquals(true, instance.usingElitism(0));
        assertEquals(false, instance.usingElitism(1));
        }

    /** Check that num elites defaults to zero. */
    @Test
    public void testNumElites1()
        {
        final SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        assertEquals(0, instance.numElites(state, 0));
        assertEquals(0, instance.numElites(state, 1));
        }

    /** Try to check num elites for a subpopulation that doesn't exist. */
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void testNumElites2()
        {
        final SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        instance.numElites(state, 2);
        }

    /** Check elites = 5. */
    @Test
    public void testNumElites3()
        {
        state.parameters.set(BASE.push(SimpleBreeder.P_ELITE).push("0"), "5");
        final SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        assertEquals(5, instance.numElites(state, 0));
        assertEquals(0, instance.numElites(state, 1));
        }

    /** Check elites = 5. */
    @Test
    public void testNumElites4()
        {
        state.parameters.set(BASE.push(SimpleBreeder.P_ELITE).push("1"), "5");
        final SimpleBreeder instance = new SimpleBreeder();
        instance.setup(state, BASE);
        assertEquals(0, instance.numElites(state, 0));
        assertEquals(5, instance.numElites(state, 1));
        }
    
    }
