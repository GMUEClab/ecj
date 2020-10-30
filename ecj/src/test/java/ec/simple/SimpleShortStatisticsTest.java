/*
  Copyright 2019 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.simple;

import ec.EvolutionState;
import ec.Evolve;
import ec.Population;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.IntegerVectorIndividual;
import java.util.ArrayList;
import java.util.Arrays;
import ec.test.TestStatistics;
import java.io.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Javier Hilty
 */
public class SimpleShortStatisticsTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;

    public SimpleShortStatisticsTest()
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
	    state.parameters = new ParameterDatabase();
	    state.population = new Population();
	    state.population.subpops = new ArrayList<>();
        state.population.subpops.add(new Subpopulation());
	    state.population.subpops.get(0).individuals = getTestPopulation();
	    state.parameters.set(BASE.push(TestStatistics.P_STATISTICS_FILE), System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "a.txt");
        }
    
    @Test
    //If doHeader = true proper spacing occuer
    public void headerTest() throws FileNotFoundException, IOException
    	{
	    SimpleShortStatistics statInd = new SimpleShortStatistics();
	    statInd.setup(state,BASE);
	    statInd.doHeader = true;
	    statInd.preInitializationStatistics(state);
	    state.output.flush();
	    BufferedReader buff = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "a.txt"));
        String text = buff.readLine();
        buff.close();
        assertEquals("generation meanFitness bestOfGenFitness bestSoFarFitness", text);
	}
	
    @Test
    public void postEvaluationTest() throws FileNotFoundException, IOException
        {
        SimpleShortStatistics statInd = new SimpleShortStatistics();
        statInd.doHeader = false;
	    statInd.doSize = false;
	    statInd.doTime = false;
	    statInd.doSubpops = false;

	    state.statistics = statInd;
	    statInd.setup(state,BASE);

	    statInd.postInitializationStatistics(state);
	    statInd.postEvaluationStatistics(state);
        state.output.flush();
        BufferedReader buff = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "a.txt"));
        String text = buff.readLine();
        buff.close();
        assertEquals("0 0.55 1.0 1.0", text);
        }

    @Test (expected = Output.OutputExitException.class)
    public void postEvaluationTestException() throws FileNotFoundException, IOException
        {
            SimpleShortStatistics statInd = new SimpleShortStatistics();
            statInd.doHeader = false;
            statInd.doSize = false;
            statInd.doTime = false;
            statInd.doSubpops = false;
            for(int x=0;x<state.population.subpops.get(0).individuals.size();x++){
                state.population.subpops.get(0).individuals.get(x).evaluated = false;
            }

            state.statistics = statInd;
            statInd.setup(state,BASE);

            statInd.postInitializationStatistics(state);
            statInd.postEvaluationStatistics(state);
        }


    private ArrayList<Individual> getTestPopulation()
        {
        final ArrayList<Individual> inds = new ArrayList<>();
        inds.add(createTestIndividual(new int[] { 1, 1, 1 }, 0.7));
        inds.add(createTestIndividual(new int[] { 4, 3, 3 }, 0.2));
        inds.add(createTestIndividual(new int[] { 1, 0, 1 }, 0.3));
        inds.add(createTestIndividual(new int[] { 2, 4, 2 }, 0.6));
        inds.add(createTestIndividual(new int[] { 3, 3, 3 }, 0.8));
        inds.add(createTestIndividual(new int[] { 1, 1, 2 }, 0.1));
        inds.add(createTestIndividual(new int[] { 9, 9, 9 }, 1.0));
        inds.add(createTestIndividual(new int[] { 3, 4, 2 }, 0.5));
        inds.add(createTestIndividual(new int[] { 2, 2, 0 }, 0.4));
        inds.add(createTestIndividual(new int[] { 7, 7, 7 }, 0.9));
        return inds;
        }

    private IntegerVectorIndividual createTestIndividual(final int[] genome, final double fitness)
        {
        final IntegerVectorIndividual ind = new IntegerVectorIndividual();
        ind.genome = Arrays.copyOf(genome, genome.length);
        ind.fitness = new SimpleFitness();
        ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
        ind.evaluated = true;
        return ind;
        }
    }
