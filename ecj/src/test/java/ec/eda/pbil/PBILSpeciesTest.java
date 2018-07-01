/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.eda.pbil;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleFitness;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.IntegerVectorIndividual;
import ec.vector.IntegerVectorSpecies;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class PBILSpeciesTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    
    public PBILSpeciesTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();
        state.parameters.set(BASE.push(PBILSpecies.P_INDIVIDUAL), IntegerVectorIndividual.class.getCanonicalName());
        state.parameters.set(BASE.push(PBILSpecies.P_FITNESS), SimpleFitness.class.getCanonicalName());
        state.parameters.set(BASE.push(PBILSpecies.P_ALPHA), "0.5");
        state.parameters.set(BASE.push(PBILSpecies.P_B), "5");
        state.parameters.set(BASE.push(PBILSpecies.P_GENOMESIZE), "3");
        state.parameters.set(BASE.push(PBILSpecies.P_MINGENE), "0");
        state.parameters.set(BASE.push(PBILSpecies.P_MAXGENE), "4");
        state.parameters.set(BASE.push(PBILSpecies.P_MAXGENE).push("2"), "3");
        state.parameters.set(BASE.push(IntegerVectorSpecies.P_MUTATIONPROB), "0");
        state.parameters.set(BASE.push(IntegerVectorSpecies.P_PIPE), "ec.breed.InitializationPipeline");
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        }

    /** Set the learning parameters. */
    @Test
    public void testSetup1()
        {
        PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        assertEquals(instance.alpha, 0.5, 0.00001);
        assertEquals(instance.b, 5);
        assertEquals(instance.genomeSize, 3);
        }

    /** Throw an exception if alpha is greater than 1. */
    @Test (expected = Output.OutputExitException.class)
    public void testSetup2()
        {
        state.parameters.set(BASE.push(PBILSpecies.P_ALPHA), "2.0");
        PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        }

    /** Throw an exception if alpha is less than zero. */
    @Test (expected = Output.OutputExitException.class)
    public void testSetup3()
        {
        state.parameters.set(BASE.push(PBILSpecies.P_ALPHA), "-1.0");
        PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        }

    /** Throw an exception if b is less than 1. */
    @Test (expected = Output.OutputExitException.class)
    public void testSetup4()
        {
        state.parameters.set(BASE.push(PBILSpecies.P_B), "0");
        PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        }

    /** The initial marginal distributions should be uniform. */
    @Test
    public void testGetMarginalDistribution()
        {
        PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        final double[] expected_4vals = new double[] { 0.25, 0.25, 0.25, 0.25 };
        final double[] expected_5vals = new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 };
        assertArrayEquals(expected_5vals, instance.getMarginalDistribution(0), 0.00001);
        assertArrayEquals(expected_5vals, instance.getMarginalDistribution(1), 0.00001);
        assertArrayEquals(expected_4vals, instance.getMarginalDistribution(2), 0.00001);
        }
    
    /** Generate individuals from the initial uniform distribution. */
    @Test
    public void testNewIndividual1()
        {
        final PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        final int NUM_SAMPLES = 10000;
        
        final List<int[]> newGenomes = new ArrayList<int[]>();
        for (int i = 0; i < NUM_SAMPLES; i++)
            newGenomes.add(((IntegerVectorIndividual)instance.newIndividual(state, 0)).genome);
        
        final int counts[][] = new int[3][];
        counts[0] = new int[5];
        counts[1] = new int[5];
        counts[2] = new int[4];
        
        // Count the number of times each gene value appears in the sample
        for (int i = 0; i < newGenomes.size(); i++)
            {
            assert(newGenomes.get(i).length == 3);
            for (int j = 0; j < 3; j++)
                {
                final int val = newGenomes.get(i)[j];
                counts[j][val]++;
                }
            }
        
        // Compute χ^2 statistic
        double chiSquared = 0.0;
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < counts[i].length; j++)
                chiSquared += Math.pow(counts[i][j] - NUM_SAMPLES/5, 2)/(NUM_SAMPLES/5);
        for (int j = 0; j < counts[2].length; j++)
                chiSquared += Math.pow(counts[2][j] - NUM_SAMPLES/4, 2)/(NUM_SAMPLES/4);
        
        // In this case, a χ^2 of >30 would correspond to p<0.0047.
        // We only want to reject if the p value is very low, because false negatives in unit tests are super annoying.
        assertTrue(chiSquared < 30);
        }
    
    
    /** Generate individuals after updating the distribution. */
    @Test
    public void testNewIndividual2()
        {
        final PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        instance.updateDistribution(state, getTestPopulation().subpops.get(0));
        final int NUM_SAMPLES = 10000;
        
        final List<int[]> newGenomes = new ArrayList<int[]>();
        for (int i = 0; i < NUM_SAMPLES; i++)
            newGenomes.add(((IntegerVectorIndividual)instance.newIndividual(state, 0)).genome);
        
        final int counts[][] = new int[3][];
        counts[0] = new int[5];
        counts[1] = new int[5];
        counts[2] = new int[4];
        
        // Count the number of times each gene value appears in the sample
        for (int i = 0; i < newGenomes.size(); i++)
            {
            assert(newGenomes.get(i).length == 3);
            for (int j = 0; j < 3; j++)
                {
                final int val = newGenomes.get(i)[j];
                counts[j][val]++;
                }
            }
        
        final int expectedCounts[][] = new int[3][];
        
        expectedCounts[0] = new int[] { 1000, 2000, 2000, 2000, 3000 };
        expectedCounts[1] = new int[] { 1000, 1000, 1000, 3000, 4000 };
        expectedCounts[2] = new int[] { 1250, 1250, 3250, 4250 };
        
        // Compute χ^2 statistic
        double chiSquared = 0.0;
        for (int i = 0; i < counts.length; i++)
            for (int j = 0; j < counts[i].length; j++)
                chiSquared += Math.pow(counts[i][j] - expectedCounts[i][j], 2)/(expectedCounts[i][j]);
        
        // In this case, a χ^2 of >30 would correspond to p<0.0047.
        // We only want to reject if the p value is very low, because false negatives in unit tests are super annoying.
        assertTrue(chiSquared < 30);
        }

    /** When the learning rate is 0.5, average the old and new distributions. */
    @Test
    public void testUpdateDistribution1()
        {
        final Population pop = getTestPopulation();
        final PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        instance.updateDistribution(state, pop.subpops.get(0));
        
        final double[] expectedMarginal0 = new double[] { 0.1, 0.2, 0.2, 0.2, 0.3 };
        final double[] expectedMarginal1 = new double[] { 0.1, 0.1, 0.1, 0.3, 0.4 };
        final double[] expectedMarginal2 = new double[] { 0.125, 0.125, 13.0/40, 17.0/40 };
        
        assertArrayEquals(expectedMarginal0, instance.getMarginalDistribution(0), 0.00001);
        assertArrayEquals(expectedMarginal1, instance.getMarginalDistribution(1), 0.00001);
        assertArrayEquals(expectedMarginal2, instance.getMarginalDistribution(2), 0.00001);
        }

    /** When the learning rate is 1.0, replace the distribution completely. */
    @Test
    public void testUpdateDistribution2()
        {
        state.parameters.set(BASE.push(PBILSpecies.P_ALPHA), "1.0");
        final Population pop = getTestPopulation();
        final PBILSpecies instance = new PBILSpecies();
        instance.setup(state, BASE);
        instance.updateDistribution(state, pop.subpops.get(0));
        
        final double[] expectedMarginal0 = new double[] { 0, 0.2, 0.2, 0.2, 0.4 };
        final double[] expectedMarginal1 = new double[] { 0, 0, 0, 0.4, 0.6 };
        final double[] expectedMarginal2 = new double[] { 0, 0, 0.4, 0.6 };
        
        assertArrayEquals(expectedMarginal0, instance.getMarginalDistribution(0), 0.00001);
        assertArrayEquals(expectedMarginal1, instance.getMarginalDistribution(1), 0.00001);
        assertArrayEquals(expectedMarginal2, instance.getMarginalDistribution(2), 0.00001);
        }
    
    private Population getTestPopulation()
        {
        final Population pop = new Population();
        final Subpopulation sp = new Subpopulation();
        pop.subpops = new ArrayList();
        pop.subpops.add(sp);
        sp.individuals = new ArrayList<Individual>();
        sp.individuals.add(createTestIndividual(new int[] { 4, 4, 3 }, 0.7));
        sp.individuals.add(createTestIndividual(new int[] { 4, 3, 3 }, 0.2));
        sp.individuals.add(createTestIndividual(new int[] { 1, 0, 1 }, 0.3));
        sp.individuals.add(createTestIndividual(new int[] { 2, 4, 2 }, 0.6));
        sp.individuals.add(createTestIndividual(new int[] { 3, 3, 3 }, 0.8));
        sp.individuals.add(createTestIndividual(new int[] { 1, 1, 2 }, 0.1));
        sp.individuals.add(createTestIndividual(new int[] { 1, 3, 3 }, 1.0));
        sp.individuals.add(createTestIndividual(new int[] { 3, 4, 2 }, 0.5));
        sp.individuals.add(createTestIndividual(new int[] { 2, 2, 0 }, 0.4));
        sp.individuals.add(createTestIndividual(new int[] { 4, 4, 2 }, 0.9));
        return pop;
        }
    
    private IntegerVectorIndividual createTestIndividual(final int[] genome, final double fitness)
        {
        final IntegerVectorIndividual ind = new IntegerVectorIndividual();
        ind.genome = Arrays.copyOf(genome, genome.length);
        ind.fitness = new SimpleFitness();
        ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
        return ind;
        }
    }
