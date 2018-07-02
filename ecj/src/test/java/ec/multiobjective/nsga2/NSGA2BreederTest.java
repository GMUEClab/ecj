/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective.nsga2;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleBreeder;
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
public class NSGA2BreederTest
    {
    private final static Parameter BASE = new Parameter("base");
    private final static Parameter FITNESS_BASE = new Parameter("fitness");
    private EvolutionState state;
    private MultiObjectiveFitness p_fitness;
    
    public NSGA2BreederTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();
        state.parameters.set(new Parameter(Initializer.P_POP).push(Population.P_SIZE), "1");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_NUMOBJECTIVES), "2");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("0"), "100");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("1"), "0.5");
        p_fitness = new NSGA2MultiObjectiveFitness();
        p_fitness.setup(state, FITNESS_BASE);
        }

    @Test
    public void testSetup1()
        {
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        }

    @Test (expected = Output.OutputExitException.class)
    public void testSetup2()
        {
        state.parameters.set(BASE.push(SimpleBreeder.P_SEQUENTIAL_BREEDING), "true");
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        }

    @Test (expected = Output.OutputExitException.class)
    public void testSetup3()
        {
        state.parameters.set(BASE.push(SimpleBreeder.P_CLONE_PIPELINE_AND_POPULATION), "false");
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        }

    @Test
    public void testLoadElitesSize()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        assertEquals(1, newpop.subpops.size());
        assertNotNull(newpop.subpops.get(0));
        assertEquals(10, newpop.subpops.get(0).individuals.size());
        }

    @Test
    public void testLoadElites()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        
        final ArrayList<Individual> expectedArchive = new ArrayList<Individual>() {{
            addAll(getRank1());
            addAll(getRank2());
            // Boundary points of rank 3 (crowding distance of infinity)
            add(createTestIndividual(new double[] { 10, 0.3}, 7));
            add(createTestIndividual(new double[] { 65, 0}, 12));
            // Rank-3 individual with the highest finite crowding distance
            add(createTestIndividual(new double[] { 60, 0.1}, 11));
        }};
        
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        assertTrue(expectedArchive.containsAll(newpop.subpops.get(0).individuals));
        assertTrue(newpop.subpops.get(0).individuals.containsAll(expectedArchive));
        assertTrue(expectedArchive.containsAll(state.population.subpops.get(0).individuals));
        assertTrue(state.population.subpops.get(0).individuals.containsAll(expectedArchive));
        }

    @Test
    public void testLoadElitesRanks()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        
        instance.loadElites(state, newpop);
        
        // Assert that ranks were assigned to the individuals in the archive
        testRank(newpop.subpops.get(0).individuals, getRank1(), 0);
        testRank(newpop.subpops.get(0).individuals, getRank2(), 1);
        testRank(newpop.subpops.get(0).individuals, getRank3(), 2);
        
        // Assert that ranks were assigned to individuals in the original combined population
        testRank(state.population.subpops.get(0).individuals, getRank1(), 0);
        testRank(state.population.subpops.get(0).individuals, getRank2(), 1);
        testRank(state.population.subpops.get(0).individuals, getRank3(), 2);
        testRank(state.population.subpops.get(0).individuals, getRank4(), 3);
        }
    
    private void testRank(final List<Individual> archive, final List<Individual> rank, final int rankID)
        {
        final List<Individual> rank1 = new ArrayList<Individual>(archive);
        rank1.retainAll(rank);
        for (final Individual ind : rank1)
            assertEquals(rankID, ((NSGA2MultiObjectiveFitness)ind.fitness).rank);
        }
    
    @Test
    public void testLoadElitesSparsity1()
        {
        final double[] expectedRank3Sparsities = new double[] { Double.POSITIVE_INFINITY, 0.5, 0.33, 0.40, 0.56, Double.POSITIVE_INFINITY };
        testSparisities(expectedRank3Sparsities);
        }
    
    @Test
    public void testLoadElitesSparsity2()
        {
        // If we change the bounds of the objectives, then it changes the sparsity
        // calculation (because the objective values are now normalized differently,
        // and the cuboids have different relative sizes.
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("0"), "200");
        state.parameters.set(FITNESS_BASE.push(MultiObjectiveFitness.P_MAXOBJECTIVES).push("1"), "1.0");
        final double[] expectedRank3Sparsities = new double[] { Double.POSITIVE_INFINITY, 0.25, 0.165, 0.2, 0.28, Double.POSITIVE_INFINITY };
        testSparisities(expectedRank3Sparsities);
        }
    
    private void testSparisities(final double[] expectedRank3Sparsities)
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        
        // The 3rd Pareto rank is the one that is only partially included in the archive.
        // Every individual in the 3rd rank has its Pareto rank calculated.
        instance.loadElites(state, newpop); // A side effect of this method is that sparsities are assigned
        
        // Not all rank-3 individuals get added to the archive, but they all still exist in
        // the original population.
        final List<Individual> individuals = instance.oldPopulation.subpops.get(0).individuals;
        final List<Individual> rank3 = getRank3();
        assertEquals(expectedRank3Sparsities.length, rank3.size());
        
        // Loop through all rank-3 individuals and ensure that they have received the correct sparsity assignment
        for (int i = 0; i < rank3.size(); i++)
            {
            final int index = individuals.indexOf(rank3.get(i));
            assertTrue(index >= 0);
            final Individual ind = individuals.get(index);
            final double sparsity = ((NSGA2MultiObjectiveFitness)ind.fitness).sparsity;
            assertEquals(expectedRank3Sparsities[i], sparsity, 0.00001);
            }
        }
    
    @Test (expected = Output.OutputExitException.class)
    public void testLoadElitesBadState()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        // Trying to call loadElites() twice in a row (should fail)
        instance.loadElites(state, newpop);
        }

    @Test
    public void testNumElites()
        {
        state.population = getTestPopulation();
        final Population newpop = state.population.emptyClone();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        instance.loadElites(state, newpop);
        
        final int result = instance.numElites(state, 0);
        assertEquals(10, result);
        }

    @Test (expected = Output.OutputExitException.class)
    public void testNumElitesBadState()
        {
        state.population = getTestPopulation();
        final NSGA2Breeder instance = new NSGA2Breeder();
        instance.setup(state, BASE);
        
        // Trying to call numElites() without first calling loadElites() (should fail)
        final int result = instance.numElites(state, 0);
        assertEquals(10, result);
        }
    
    private Population getTestPopulation()
        {
        final Population pop = new Population();
        pop.subpops = new ArrayList();
        pop.subpops.add(new Subpopulation());
        pop.subpops.get(0).individuals = new ArrayList<Individual>();
        pop.subpops.get(0).individuals.addAll(getRank2());
        pop.subpops.get(0).individuals.addAll(getRank3());
        pop.subpops.get(0).individuals.addAll(getRank1());
        pop.subpops.get(0).individuals.addAll(getRank4());
        return pop;
        }
    
    private List<Individual> getRank1()
        {
        // The Pareto front is non-convex and non-concave
        return new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 50, 0.5}, 0));
           add(createTestIndividual(new double[] { 75, 0.4}, 1));
           add(createTestIndividual(new double[] { 80, 0.2}, 2));
           add(createTestIndividual(new double[] { 100, 0.05}, 3));
        }};
        }
    
    private List<Individual> getRank2()
        {
        // Rank 2 is convex
        return new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 50, 0.45}, 4));
           add(createTestIndividual(new double[] { 75, 0.25}, 5));
           add(createTestIndividual(new double[] { 80, 0}, 6));
        }};
        }
    
    private List<Individual> getRank3()
        {
        // Rank 3 is convex, and constructed such that order induced by
        // crowding distance will be different if the algorithm fails to 
        // normalized the ranges of the objectives.
        return new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 10, 0.3}, 7)); // Crowding distance: infinity
           add(createTestIndividual(new double[] { 30, 0.27}, 8)); // 0.5 (40.05 unnormalized)
           add(createTestIndividual(new double[] { 50, 0.25}, 9)); // 0.33 (25.06 unnormalized)
           add(createTestIndividual(new double[] { 55, 0.23}, 10)); // 0.4 (10.15 unnormalized)
           add(createTestIndividual(new double[] { 60, 0.1}, 11)); // 0.56 (10.23 unnormalized)
           add(createTestIndividual(new double[] { 65, 0}, 12)); // infinity
        }};
        }
    
    private List<Individual> getRank4()
        {
        return new ArrayList<Individual>() {{
           add(createTestIndividual(new double[] { 0, 0.25 }, 13));
           add(createTestIndividual(new double[] { 5, 0.23 }, 14));
           add(createTestIndividual(new double[] { 10, 0.20 }, 15));
           add(createTestIndividual(new double[] { 15, 0.17 }, 16));
           add(createTestIndividual(new double[] { 20, 0.15 }, 17));
           add(createTestIndividual(new double[] { 25, 0.14 }, 18));
           add(createTestIndividual(new double[] { 30, 0.1 }, 19));
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
