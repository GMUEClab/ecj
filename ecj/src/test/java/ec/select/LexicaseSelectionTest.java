/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.select;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.gp.koza.KozaFitness;
import ec.util.MersenneTwisterFast;
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
public class LexicaseSelectionTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    
    public LexicaseSelectionTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        state.parameters = new ParameterDatabase();
        state.population = new Population();
        state.population.subpops = new ArrayList();
        state.population.subpops.add(new Subpopulation());
        }

    @Test
    public void testProduce()
        {
        final LexicaseSelection instance = new LexicaseSelection();
        instance.setup(state, BASE);
        
        state.population.subpops.get(0).individuals = getPopulation1();
        
        final int N = 1000;
        int zeroCount = 0;
        int sixCount = 0;
        int otherCount = 0;
        for (int i = 0; i < N; i++)
            {
            final int result = instance.produce(0, state, 0);
            if (result == 0)
                zeroCount++;
            else if (result == 6)
                sixCount++;
            else
                otherCount++;
            }
        
        // Never select an individual that is always lexically dominated
        assertEquals(0, otherCount);
        
        // Use a χ^2 test to see if selecting the 0th and 6th individual is equally likely
        final int expectedZeros = N/2;
        final int expectedSixes = N/2;
        final double chiSquared = (Math.pow(zeroCount - expectedZeros, 2)/expectedZeros + Math.pow(sixCount - expectedSixes, 2)/expectedSixes);
        
        // Reject the hypothesis that the distribution is uniform if p > 0.01 (i.e. if χ^2 > 6.635, the threshold for p = 0.01 at 2-1 = 1 degree of freedom)
        assertFalse(chiSquared > 6.635);
        }
    
    private ArrayList<Individual> getPopulation1()
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
    
    private Individual createTestIndividual(final double[] trialValues, final int geneValue)
        {
        assert(trialValues != null);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        final KozaFitness fitness = new KozaFitness();
        fitness.setup(state, new Parameter(""));
        fitness.trials = new ArrayList();
        for (final double d : trialValues)
            {
            final KozaFitness trialFitness = new KozaFitness();
            trialFitness.setup(state, new Parameter(""));
            trialFitness.setStandardizedFitness(state, d);
            fitness.trials.add(trialFitness);
            }
        ind.fitness = fitness;
        ind.genome = new double[] { geneValue };
        return ind;
        }
    }
