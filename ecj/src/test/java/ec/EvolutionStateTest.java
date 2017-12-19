/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec;

import ec.test.StubBreeder;
import ec.test.StubEvaluator;
import ec.test.StubExchanger;
import ec.test.StubFinisher;
import ec.test.StubInitializer;
import ec.test.TestStatistics;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class EvolutionStateTest
{
    public ParameterDatabase params;
    public EvolutionState instance;
    
    public EvolutionStateTest()
    {
    }
    
    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        params.set(new Parameter(Evolve.P_STATE), "ec.EvolutionState");
        params.set(new Parameter(EvolutionState.P_CHECKPOINTMODULO), "1");
        params.set(new Parameter(EvolutionState.P_CHECKPOINTPREFIX), "ec");
        params.set(new Parameter(EvolutionState.P_GENERATIONS), "100");
        params.set(new Parameter(EvolutionState.P_INITIALIZER), "ec.test.StubInitializer");
        params.set(new Parameter(EvolutionState.P_FINISHER), "ec.test.StubFinisher");
        params.set(new Parameter(EvolutionState.P_BREEDER), "ec.test.StubBreeder");
        params.set(new Parameter(EvolutionState.P_EVALUATOR), "ec.test.StubEvaluator");
        params.set(new Parameter(EvolutionState.P_STATISTICS), "ec.test.TestStatistics");
        params.set(new Parameter(EvolutionState.P_STATISTICS).push(TestStatistics.P_STATISTICS_FILE), "/dev/null");
        params.set(new Parameter(EvolutionState.P_EXCHANGER), "ec.test.StubExchanger");
        instance = new EvolutionState();
        instance.parameters = params;
        instance.output = Evolve.buildOutput();
        instance.output.setThrowsErrors(true);
        instance.random = new MersenneTwisterFast[] {};
        instance.evalthreads = 1;
    }

    @Test
    public void testSetup()
    {
        System.out.println("setup");
        instance.setup(instance, null);
        assertEquals(instance.checkpointModulo, 1);
        assertEquals(instance.checkpointPrefix, "ec");
        assertEquals(instance.numGenerations, 100);
        assertTrue(instance.initializer instanceof StubInitializer);
        assertTrue(instance.finisher instanceof StubFinisher);
        assertTrue(instance.breeder instanceof StubBreeder);
        assertTrue(instance.evaluator instanceof StubEvaluator);
        assertTrue(instance.statistics instanceof TestStatistics);
        assertTrue(instance.exchanger instanceof StubExchanger);
    }

    @Test
    public void testSynchronizedIncrementEvaluations1()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.synchronizedIncrementEvaluations(1);
        assertEquals(1, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations2()
    {
        instance.setup(instance, null);
        instance.evaluations = 17;
        instance.synchronizedIncrementEvaluations(1);
        assertEquals(18, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations3()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.synchronizedIncrementEvaluations(5);
        assertEquals(5, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations4()
    {
        instance.setup(instance, null);
        instance.evaluations = 18;
        instance.synchronizedIncrementEvaluations(5);
        assertEquals(23, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations5()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.evalthreads = 5;
        instance.synchronizedIncrementEvaluations(1);
        assertEquals(1, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations6()
    {
        instance.setup(instance, null);
        instance.evaluations = 17;
        instance.evalthreads = 5;
        instance.synchronizedIncrementEvaluations(1);
        assertEquals(18, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations7()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.evalthreads = 5;
        instance.synchronizedIncrementEvaluations(5);
        assertEquals(5, instance.evaluations);
    }

    @Test
    public void testSynchronizedIncrementEvaluations8()
    {
        instance.setup(instance, null);
        instance.evaluations = 18;
        instance.evalthreads = 5;
        instance.synchronizedIncrementEvaluations(5);
        assertEquals(23, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations1()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.incrementEvaluations(1);
        assertEquals(1, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations2()
    {
        instance.setup(instance, null);
        instance.evaluations = 17;
        instance.incrementEvaluations(1);
        assertEquals(18, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations3()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.incrementEvaluations(5);
        assertEquals(5, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations4()
    {
        instance.setup(instance, null);
        instance.evaluations = 18;
        instance.incrementEvaluations(5);
        assertEquals(23, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations5()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.evalthreads = 5;
        instance.incrementEvaluations(1);
        assertEquals(1, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations6()
    {
        instance.setup(instance, null);
        instance.evaluations = 17;
        instance.evalthreads = 5;
        instance.incrementEvaluations(1);
        assertEquals(18, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations7()
    {
        instance.setup(instance, null);
        instance.evaluations = 0;
        instance.evalthreads = 5;
        instance.incrementEvaluations(5);
        assertEquals(5, instance.evaluations);
    }

    @Test
    public void testIncrementEvaluations8()
    {
        instance.setup(instance, null);
        instance.evaluations = 18;
        instance.evalthreads = 5;
        instance.incrementEvaluations(5);
        assertEquals(23, instance.evaluations);
    }
}
