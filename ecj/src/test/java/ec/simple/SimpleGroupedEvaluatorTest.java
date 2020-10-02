/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.simple;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
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
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.parameters = new ParameterDatabase();
        state.evalthreads = 1;

        // Parameters for our constructor
        state.parameters.set(BASE.push(SimpleGroupedEvaluator.P_PROBLEM), "ec.test.StubGroupedProblem");
        }

    @Test
    public void testSetup()
        {
        SimpleGroupedEvaluator instance = new SimpleGroupedEvaluator();
        instance.setup(state, BASE);
        }
    

    /** After calling evaluate() on a population, their fitnesses should all have the 
     * value determiend by the objective function.
     */
    @Test
    public void testEvaluate()
        {
            SimpleGroupedEvaluator instance = new SimpleGroupedEvaluator();
            instance.setup(state, BASE);

            state.population = getPopulation1();
            instance.evaluatePopulation(state);

            final ArrayList<Individual> inds = state.population.subpops.get(0).individuals;
            for (final Individual ind : inds)
                assertEquals(ind.fitness.fitness(), ((DoubleVectorIndividual)ind).genome[0], 0.00001);
        }
    
    /** Create a test population of real-vector individuals. */
    private Population getPopulation1()
        {
        final Subpopulation subpop = new Subpopulation();
        subpop.individuals = new ArrayList<>();
        subpop.individuals.add(createTestIndividual(new double[] { 0, 0.25 }));
        subpop.individuals.add(createTestIndividual(new double[] { 5, 0.23 }));
        subpop.individuals.add(createTestIndividual(new double[] { 10, 0.20 }));
        subpop.individuals.add(createTestIndividual(new double[] { 15, 0.17 }));
        subpop.individuals.add(createTestIndividual(new double[] { 20, 0.15 }));
        subpop.individuals.add(createTestIndividual(new double[] { 25, 0.14 }));
        subpop.individuals.add(createTestIndividual(new double[] { 30, 0.1 }));

        final Population pop = new Population();
        pop.subpops = new ArrayList<Subpopulation>();
        pop.subpops.add(subpop);
        return pop;
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
    }
