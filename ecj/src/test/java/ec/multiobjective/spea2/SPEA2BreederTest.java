/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective.spea2;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.breed.BreedDefaults;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.DoubleVectorIndividual;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class SPEA2BreederTest
    {
    private final static Parameter BASE = new Parameter("base");
    private final static Parameter FITNESS_BASE = new Parameter("fitness");
    private EvolutionState state;
    private MultiObjectiveFitness p_fitness;
    
    public SPEA2BreederTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();
        state.parameters.set(new Parameter(Initializer.P_POP).push(Population.P_SIZE), "20");
        state.parameters.set(new Parameter(BreedDefaults.P_BREED).push(SPEA2Breeder.P_ELITE).push("0"), "10");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_NUMOBJECTIVES), "2");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("0"), "100");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("1"), "0.5");
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() }; // PRNG is used for an internal algorithm, but the result is deterministic.
        p_fitness = new SPEA2MultiObjectiveFitness();
        p_fitness.setup(state, FITNESS_BASE);
        }

    @Test
    public void testSetup()
        {
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        }

    /** Build an archive of size 10 with the default k. */
    @Test
    public void testLoadElites1()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        final List<Individual> expectedArchive = new ArrayList<Individual>() {{
           addAll(getTestPopParetoFront());
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 80, 0}, 6));
           add(createTestIndividual(new double[] { 30, 0.27}, 8));
           add(createTestIndividual(new double[] { 55, 0.23}, 10));
           add(createTestIndividual(new double[] { 60, 0.1}, 11));
        }};
        
        assertEquals(10, newpop.subpops.get(0).individuals.size());
        assertTrue(newpop.subpops.get(0).individuals.containsAll(expectedArchive));
        assertTrue(expectedArchive.containsAll(newpop.subpops.get(0).individuals));
        }

    /** Build an archive of size 10 with k=5. */
    @Test
    public void testLoadElites2()
        {
        state.parameters.set(BASE.push(SPEA2Breeder.P_K), "5");
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        final List<Individual> expectedArchive = new ArrayList<Individual>() {{
           addAll(getTestPopParetoFront());
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 80, 0}, 6));
           add(createTestIndividual(new double[] { 10, 0.3}, 7));
           add(createTestIndividual(new double[] { 55, 0.23}, 10));
           add(createTestIndividual(new double[] { 60, 0.1}, 11));
        }};
        
        assertEquals(10, newpop.subpops.get(0).individuals.size());
        assertTrue(newpop.subpops.get(0).individuals.containsAll(expectedArchive));
        assertTrue(expectedArchive.containsAll(newpop.subpops.get(0).individuals));
        }

    /** Build an archive of size 12 with the default k. */
    @Test
    public void testLoadElites3()
        {
        state.parameters.set(new Parameter(BreedDefaults.P_BREED).push(SPEA2Breeder.P_ELITE).push("0"), "12");
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        final List<Individual> expectedArchive = new ArrayList<Individual>() {{
           addAll(getTestPopParetoFront());
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 80, 0}, 6));
           add(createTestIndividual(new double[] { 10, 0.3}, 7));
           add(createTestIndividual(new double[] { 30, 0.27}, 8));
           add(createTestIndividual(new double[] { 55, 0.23}, 10));
           add(createTestIndividual(new double[] { 60, 0.1}, 11));
           add(createTestIndividual(new double[] { 65, 0}, 12));
        }};
        
        assertEquals(12, newpop.subpops.get(0).individuals.size());
        assertTrue(newpop.subpops.get(0).individuals.containsAll(expectedArchive));
        assertTrue(expectedArchive.containsAll(newpop.subpops.get(0).individuals));
        }
    
    /** Check the fitnesses after building an archive of size 10 with the
     * default k. */
    @Test
    public void testLoadElitesFitnesses1()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        final List<Individual> expectedArchive = new ArrayList<Individual>() {{
           addAll(getTestPopParetoFront());
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 80, 0}, 6));
           add(createTestIndividual(new double[] { 30, 0.27}, 8));
           add(createTestIndividual(new double[] { 55, 0.23}, 10));
           add(createTestIndividual(new double[] { 60, 0.1}, 11));
        }};
        
        final double[] expectedFitnesses = new double[] { 0.399745, 0.418320, 0.431736, 0.414540, 11.414624, 14.434783, 10.416667, 35.444444, 25.441991, 33.438050};
        final double[] expectedStrengths = new double[] { 11, 14, 8, 2, 10, 11, 1, 7, 6, 1 };
        final double[] expectedKthNNDistances = new double[] { 0.501597, 0.390512, 0.316228, 0.412311, 0.411825, 0.3, 0.4, 0.25, 0.262488, 0.282843 };
        
        testFitnesses(expectedArchive, expectedFitnesses, expectedStrengths, expectedKthNNDistances);
        }
    
    private void testFitnesses(final List<Individual> expectedPop, final double[] expectedFitnesses, final double[] expectedStrengths, final double[] expectedKthNNDistances)
        {
        assert(expectedFitnesses.length == expectedPop.size());
        assert(expectedStrengths.length == expectedFitnesses.length);
        assert(expectedKthNNDistances.length == expectedFitnesses.length);
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        
        // The 3rd Pareto rank is the one that is only partially included in the archive.
        // Every individual in the 3rd rank has its Pareto rank calculated.
        instance.loadElites(state, newpop); // A side effect of this method is that sparsities are assigned
        
        // Not all rank-3 individuals get added to the archive, but they all still exist in
        // the original population.
        final List<Individual> individuals = state.population.subpops.get(0).individuals;
        assert(expectedFitnesses.length == expectedPop.size());
        
        // Loop through all rank-3 individuals and ensure that they have received the correct sparsity assignment
        for (int i = 0; i < expectedPop.size(); i++)
            {
            final int index = individuals.indexOf(expectedPop.get(i));
            assertTrue(index >= 0);
            final Individual ind = individuals.get(index);
            final SPEA2MultiObjectiveFitness fitness = (SPEA2MultiObjectiveFitness)ind.fitness;
            assertEquals(expectedFitnesses[i], fitness.fitness, 0.00001);
            assertEquals(expectedStrengths[i], fitness.strength, 0.00001);
            assertEquals(expectedKthNNDistances[i], fitness.kthNNDistance, 0.00001);
            }
        }
    
    /** Throw an exception if we try to call loadElites() twice in a row. */
    @Test (expected = Output.OutputExitException.class)
    public void testLoadElitesBadState()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final SPEA2Breeder instance = new SPEA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        // Trying to call loadElites() twice in a row (should fail)
        instance.loadElites(state, newpop);
        }
    
    private Population getTestPopulation()
        {
        final Population pop = new Population();
        pop.subpops = new ArrayList();
        pop.subpops.add(new Subpopulation());
        
        pop.subpops.get(0).individuals = new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 25, 0.14 }, 18));
           add(createTestIndividual(new double[] { 80, 0.2}, 2));
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 20, 0.15 }, 17));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 10, 0.20 }, 15));
           add(createTestIndividual(new double[] { 60, 0.1}, 11));
           add(createTestIndividual(new double[] { 75, 0.4}, 1));
           add(createTestIndividual(new double[] { 10, 0.3}, 7));
           add(createTestIndividual(new double[] { 30, 0.27}, 8));
           add(createTestIndividual(new double[] { 15, 0.17 }, 16));
           add(createTestIndividual(new double[] { 50, 0.25}, 9));
           add(createTestIndividual(new double[] { 30, 0.1 }, 19));
           add(createTestIndividual(new double[] { 50, 0.5}, 0));
           add(createTestIndividual(new double[] { 55, 0.23}, 10));
           add(createTestIndividual(new double[] { 100, 0.05}, 3));
           add(createTestIndividual(new double[] { 80, 0}, 6));
           add(createTestIndividual(new double[] { 65, 0}, 12));
           add(createTestIndividual(new double[] { 0, 0.25 }, 13));
           add(createTestIndividual(new double[] { 5, 0.23 }, 14));
        }};
        return pop;
        }
    
    private List<Individual> getTestPopParetoFront()
        {
        // The Pareto front is non-convex and non-concave
        return new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 50, 0.5}, 0));
           add(createTestIndividual(new double[] { 75, 0.4}, 1));
           add(createTestIndividual(new double[] { 80, 0.2}, 2));
           add(createTestIndividual(new double[] { 100, 0.05}, 3));
        }};
        }
    
    private Individual createTestIndividual(final double[] fitnesses, final int geneValue)
        {
        assert(fitnesses != null);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        final MultiObjectiveFitness fitness = (MultiObjectiveFitness) p_fitness.clone();
        fitness.setup(state, FITNESS_BASE);
        fitness.setObjectives(state, fitnesses);
        ind.fitness = fitness;
        ind.genome = new double[] { geneValue };
        return ind;
        }
    }
