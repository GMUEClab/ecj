/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.simple;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.DoubleVectorIndividual;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 *
 * @author Eric O. Scott
 */
public class SimpleGroupedEvaluatorTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    
    public SimpleGroupedEvaluatorTest()
        {
        }
    
    @Before
    public void setUp()
        {
        // Boiler plate
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();

        // Parameters for our constructor
        state.parameters.set(BASE.push(SimpleGroupedEvaluator.P_PROBLEM), "ec.test.StubGroupedProblem");
        }

    @Test
    public void testSetup()
        {
        SimpleGroupedEvaluator instance = new SimpleGroupedEvaluator();
        instance.setup(state, BASE);
        }
    

    @Test
    public void testEvaluate()
        {
            SimpleGroupedEvaluator instance = new SimpleGroupedEvaluator();
            instance.setup(state, BASE);

            state.population = getPopulation1();
            instance.evaluatePopulation(state);
        }
    
    /** Create a test population of real-vector individuals. */
    private ArrayList<Individual> getPopulation1()
        {
        return new ArrayList<Individual>() {{
        add(createTestIndividual(new double[] { 0, 0.25 }));
        add(createTestIndividual(new double[] { 5, 0.23 }));
        add(createTestIndividual(new double[] { 10, 0.20 }));
        add(createTestIndividual(new double[] { 15, 0.17 }));
        add(createTestIndividual(new double[] { 20, 0.15 }));
        add(createTestIndividual(new double[] { 25, 0.14 }));
        add(createTestIndividual(new double[] { 30, 0.1 }));
        }};
        }

    /** Create a DoubleVectorIndividual with the given genome. */
    private Individual createTestIndividual(final double[] genome)
        {
        assert(genome != null);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.genome = genome;
        ind.fitness = new SimpleFitness();
        ind.fitness.setup(state, new Parameter(""));
        return ind;
        }

    /** Check that elitism defaults to false. */
    // @Test
    // public void testUsingElitism1()
    //     {
    //     SimpleBreeder instance = new SimpleBreeder();
    //     instance.setup(state, BASE);
    //     assertEquals(false, instance.usingElitism(0));
    //     assertEquals(false, instance.usingElitism(1));
    //     }

    // /** Try to check elitism for a subpopulation that doesn't exist. */
    // @Test (expected = ArrayIndexOutOfBoundsException.class)
    // public void testUsingElitism2()
    //     {
    //     SimpleBreeder instance = new SimpleBreeder();
    //     instance.setup(state, BASE);
    //     instance.usingElitism(2);
    //     }
    
    }
