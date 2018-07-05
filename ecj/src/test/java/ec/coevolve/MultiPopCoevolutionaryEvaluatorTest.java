/*
  Copyright 2017 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */
package ec.coevolve;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.app.coevolve2.CoevolutionaryECSuite;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.DoubleVectorIndividual;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class MultiPopCoevolutionaryEvaluatorTest {
    
    private final static Parameter BASE = new Parameter("base");
    private ParameterDatabase params;
    private EvolutionState state;
    
    public MultiPopCoevolutionaryEvaluatorTest() {
    }
    
    @Before
    public void setUp() {
        params = new ParameterDatabase();
        params.set(BASE, "mtecj.MultiPopCoevolutionaryEvaluatorTest");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_PROBLEM), "ec.app.coevolve2.CoevolutionaryECSuite");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_PROBLEM).push(CoevolutionaryECSuite.P_WHICH_PROBLEM), "rosenbrock");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_NUM_SHUFFLED), "1");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_NUM_RAND_IND), "0");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_NUM_GURU), "0");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_NUM_IND), "0");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_SELECTION_METHOD_CURRENT), "ec.select.RandomSelection");
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_SELECTION_METHOD_PREV), "ec.select.RandomSelection");
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SIZE), "3");
        state = new EvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.population = new Population();
        state.population.subpops = new ArrayList<Subpopulation>();
        state.population.subpops.add(new Subpopulation());
    }
    
    @Test
    public void testAfterCoevolutionaryEvaluation1() {
        final MultiPopCoevolutionaryEvaluator instance = new MultiPopCoevolutionaryEvaluator();
        instance.setup(state, BASE);
        state.population = getTestPop(); // Input population
        
        assertEquals(null, instance.previousPopulation);
        instance.afterCoevolutionaryEvaluation(state, null);
        assertEquals(null, instance.previousPopulation);
    }
    
    @Test
    public void testAfterCoevolutionaryEvaluation2() {
        params.set(BASE.push(MultiPopCoevolutionaryEvaluator.P_NUM_IND), "1");
        final MultiPopCoevolutionaryEvaluator instance = new MultiPopCoevolutionaryEvaluator();
        instance.setup(state, BASE);
        state.population = getTestPop(); // Input population
        assertNull(instance.previousPopulation);
        
        instance.afterCoevolutionaryEvaluation(state, null);
        assertNotNull(instance.previousPopulation);
        assertNotNull(instance.previousPopulation.subpops);
        assertEquals(2, instance.previousPopulation.subpops.size());
        assertTrue(instance.previousPopulation.subpops.get(0).individuals.isEmpty());
        assertTrue(instance.previousPopulation.subpops.get(1).individuals.isEmpty());
    }
    
    private Population getTestPop() {
        final Population result = new Population();
        result.subpops = new ArrayList<Subpopulation>();
        
        final Subpopulation subpop1 = new Subpopulation();
        final DoubleVectorIndividual ind11 = new DoubleVectorIndividual();
        ind11.genome = new double[] { 0.14, 0.76, 0.36, 0.94, 0.11, 0.65 };
        ind11.fitness = new SimpleFitness();
        ((SimpleFitness)ind11.fitness).setFitness(state, 1.0, false);
        final DoubleVectorIndividual ind12 = new DoubleVectorIndividual();
        ind12.genome = new double[] { 0.52, 0.81, 0.51, 0.72, 0.82, 0.52 };
        ind12.fitness = new SimpleFitness();
        ((SimpleFitness)ind12.fitness).setFitness(state, 2.0, false);
        subpop1.individuals = new ArrayList<Individual>() {{
           add(ind11); add(ind12); 
        }};
        
        final Subpopulation subpop2 = new Subpopulation();
        final DoubleVectorIndividual ind21 = new DoubleVectorIndividual();
        ind21.genome = new double[] { 0.54, 0.57, 0.29, 0.43, 0.01, 0.51 };
        ind21.fitness = new SimpleFitness();
        ((SimpleFitness)ind21.fitness).setFitness(state, 2.0, false);
        final DoubleVectorIndividual ind22 = new DoubleVectorIndividual();
        ind22.genome = new double[] { 0.99, 0.28, 0.15, 0.51, 0.90, 0.43 };
        ind22.fitness = new SimpleFitness();
        ((SimpleFitness)ind22.fitness).setFitness(state, 0.0, false);
        final DoubleVectorIndividual ind23 = new DoubleVectorIndividual();
        ind23.genome = new double[] { 0.21, 0.32, 0.81, 0.01, 0.95, 0.42 };
        ind23.fitness = new SimpleFitness();
        ((SimpleFitness)ind23.fitness).setFitness(state, 0.0, false);
        subpop2.individuals = new ArrayList<Individual>() {{
           add(ind21); add(ind22); add(ind23);
        }};
        
        result.subpops.add(subpop1);
        result.subpops.add(subpop2);
        
        return result;
    }
}
