/*
  Copyright 2018 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.DoubleVectorIndividual;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests to verify our hypervolume metric.
 * 
 * Some of these tests were built by comparing against into v1.3 of Fonseca et
 * al.'s hypervolume calculation software, available here:
 * http://lopez-ibanez.eu/hypervolume
 * 
 * @author Eric O. Scott
 */
public class HypervolumeStatisticsTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    
    public HypervolumeStatisticsTest()
        {
        }
    
    @Before
    public void setUp()
        {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.parameters = new ParameterDatabase();
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0 0");
        }

    @Test
    public void testSetup1()
        {
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertArrayEquals(new double[] { 0, 0, 0 }, instance.getReferencePoint(), 0);
        }

    @Test
    public void testSetup2()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "-8 15 0");
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertArrayEquals(new double[] { -8, 15, 0 }, instance.getReferencePoint(), 0);
        }

    /** Error out on missing reference point. */
    @Test (expected = Output.OutputExitException.class)
    public void testSetup3()
        {
        state.parameters.remove(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT));
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        }

    /** Hypervolume of a single 2-D point relative to the origin. */
    @Test
    public void testHypervolume1()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0");
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 0.05558307978709631, 5.3412353920970626 }));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(0.2968823129605937, instance.hypervolume(paretoFront), 0.00001);
        }

    /** Hypervolume of two 2-D points that do not dominate the origin when
     * minimizing all three objectives.
     *  
     *  We expect an exception, because hypervolume is defined as the volume of
     * the dominated space relative to a reference point.  It is undefined if 
     * some point doesn't dominate the reference point.
     */
    @Test (expected = IllegalStateException.class)
    public void testHypervolume2()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0");
        final boolean[] maximize = new boolean[] { false, false };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 0.46157481278023227, 3.1884030239549896 }, maximize));
                add(createIndForPoint(new double[] { 0.43388372996755065, 3.3639460726236994 }, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        instance.hypervolume(paretoFront);
        }

    /** Hypervolume of two 3-D points relative to the origin. */
    @Test
    public void testHypervolume3()
        {
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}));
                add(createIndForPoint(new double[] { 2, 2, 1}));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(6.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of two 3-D points that do not dominate the origin when
     * minimizing all three objectives.
     * 
     * We expect an exception, because hypervolume is defined as the volume of
     * the dominated space relative to a reference point.  It is undefined if 
     * some point doesn't dominate the reference point.
     */
    @Test (expected = IllegalStateException.class)
    public void testHypervolume4()
        {
        final boolean[] maximize = new boolean[] { false, false, false };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}, maximize));
                add(createIndForPoint(new double[] { 2, 2, 1}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        instance.hypervolume(paretoFront);
        }

    /** Hypervolume of two 3-D points, minimizing one objective and maximizing
     * the other two.
     */
    @Test
    public void testHypervolume5()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "3 0 0");
        final boolean[] maximize = new boolean[] { false, true, true };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}, maximize));
                add(createIndForPoint(new double[] { 2, 2, 1}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(8.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of two 3-D points, minimizing one objective and maximizing
     * the other two.
     */
    @Test
    public void testHypervolume6()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 3 0");
        final boolean[] maximize = new boolean[] { true, false, true };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}, maximize));
                add(createIndForPoint(new double[] { 2, 2, 1}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(3.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of two 3-D points, minimizing one objective and maximizing
     * the other two.
     */
    @Test
    public void testHypervolume7()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0 3");
        final boolean[] maximize = new boolean[] { true, true, false };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}, maximize));
                add(createIndForPoint(new double[] { 2, 2, 1}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(8.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of two 3-D points, minimizing one objective and maximizing
     * the other two.
     */
    @Test
    public void testHypervolume8()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0 4");
        final boolean[] maximize = new boolean[] { true, true, false };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}, maximize));
                add(createIndForPoint(new double[] { 2, 2, 1}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(12.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of three 3-D points relative to the origin. */
    @Test
    public void testHypervolume9()
        {
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 2, 2}));
                add(createIndForPoint(new double[] { 2, 2, 1}));
                add(createIndForPoint(new double[] { 2, 1, 2}));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(7.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of an empty set. */
    @Test
    public void testHypervolume10()
        {
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>();
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(0.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of five 4-D points relative to the origin.
     * 
     * The expected result was generated with Fonseca et al.'s code.
     */
    @Test
    public void testHypervolume11()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 0 0 0");
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 4, 5, 6, 7}));
                add(createIndForPoint(new double[] { 7, 5, 6, 4}));
                add(createIndForPoint(new double[] { 6, 8, 5, 7}));
                add(createIndForPoint(new double[] { 1, 1, 10, 1}));
                add(createIndForPoint(new double[] { 10, 1, 1, 1}));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(1987.0, instance.hypervolume(paretoFront), 0.0);
        }

    /** Hypervolume of four 4-D points, with mixed minimization and maximization.
     * 
     * The expected result was generated with Fonseca et al.'s code.
     */
    @Test
    public void testHypervolume12()
        {
        state.parameters.set(BASE.push(HypervolumeStatistics.P_REFERENCE_POINT), "0 5 0 50");
        final boolean[] maximize = new boolean[] { true, false, true, false };
        final ArrayList<Individual> paretoFront = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 10, 4, 1, 40}, maximize));
                add(createIndForPoint(new double[] { 15, 3, 0.5, 49}, maximize));
                add(createIndForPoint(new double[] { 15, 2, 0.5, 30}, maximize));
                add(createIndForPoint(new double[] { 1, 4, 5, 10}, maximize));
        }};
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        assertEquals(685.0, instance.hypervolume(paretoFront), 0.0);
        }

    @Test
    public void testInclusiveHypervolume1()
        {
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        final Individual ind = createIndForPoint(new double[] { 0, 0, 0});
        assertEquals(0.0, instance.inclusiveHypervolume(ind), 0.0);
        }

    @Test
    public void testInclusiveHypervolume2()
        {
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        final Individual ind = createIndForPoint(new double[] { 1, 1, 1});
        assertEquals(1.0, instance.inclusiveHypervolume(ind), 0.0);
        }

    @Test
    public void testInclusiveHypervolume3()
        {
        final HypervolumeStatistics instance = new HypervolumeStatistics();
        instance.setup(state, BASE);
        final Individual ind = createIndForPoint(new double[] { 1, 5, 4});
        assertEquals(20.0, instance.inclusiveHypervolume(ind), 0.0);
        }
    
    private static Individual createIndForPoint(final double[] fitnesses)
        {
        final boolean[] maximize = new boolean[fitnesses.length];
        for (int i = 0; i < fitnesses.length; i++)
            maximize[i] = true;
        return createIndForPoint(fitnesses, maximize);
        }
    
    private static Individual createIndForPoint(final double[] fitnesses, final boolean[] maximize)
        {
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.genome = new double[] {};
        final MultiObjectiveFitness fitness = new MultiObjectiveFitness();
        fitness.objectives = fitnesses;
        fitness.maximize = maximize;
        ind.fitness = fitness;
        return ind;
        }
    }
