/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.bbob;

import ec.EvolutionState;
import ec.Evolve;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleFitness;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.DoubleVectorIndividual;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class BBOBenchmarksTest {
    private final static Parameter BASE = new Parameter("base");
    private ParameterDatabase params;
    private EvolutionState state;

    public BBOBenchmarksTest() {
    }
    
    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "10");
        params.set(BASE.push(BBOBenchmarks.P_NOISE), "none");
        params.set(BASE.push(BBOBenchmarks.P_REEVALUATE_NOISY_PROBLEMS), "true");
        state = new EvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
    }

    /** Sphere function should return a fitness which is less than the optimal fitness*/
    @Test
    public void testEvaluate() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "sphere");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 0);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Sphere function should return the correct value for point (1,1)*/
    @Test
    public void testSpherePointOne(){
        BBOBenchmarks instance = new BBOBenchmarks();
        params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "sphere");
        params.set(BASE.push(BBOBenchmarks.P_ZERO_IS_BEST),"zeroIsBest");
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "2");
        params.set(BASE.push(BBOBenchmarks.P_XOPT),"0 0");
        instance.setup(state, BASE);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.setGenome(new double[] { 1,1});
        ind.fitness = new SimpleFitness();
        instance.evaluate(state, ind, 0, 0);
        assertTrue(instance.problemType == 0);
        assertTrue(ind.fitness.fitness() == -2);
    }

    /** Sphere function should return the correct value for point (3,3)*/
    @Test
    public void testSpherePointTwo() {
        BBOBenchmarks instance = new BBOBenchmarks();
        params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "sphere");
        params.set(BASE.push(BBOBenchmarks.P_ZERO_IS_BEST), "zeroIsBest");
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "2");
        params.set(BASE.push(BBOBenchmarks.P_XOPT), "0 0");
        instance.setup(state, BASE);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.setGenome(new double[]{3, 3});
        ind.fitness = new SimpleFitness();
        instance.evaluate(state, ind, 0, 0);
        assertTrue(instance.problemType == 0);
        assertTrue(ind.fitness.fitness() == -18);
    }

    /** Ellipsoidal function should return a fitness which is less than the optimal*/
    @Test
    public void testEllipsoidal(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "ellipsoidal");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 1);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Ellipsoidal function should return the correct value for point (1,1)*/
    @Test
    public void testEllipsoidalPointOne() {
        BBOBenchmarks instance = new BBOBenchmarks();
        params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "ellipsoidal");
        params.set(BASE.push(BBOBenchmarks.P_ZERO_IS_BEST), "zeroIsBest");
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "2");
        params.set(BASE.push(BBOBenchmarks.P_XOPT), "0 0");
        instance.setup(state, BASE);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.setGenome(new double[]{1, 1});
        ind.fitness = new SimpleFitness();
        instance.evaluate(state, ind, 0, 0);
        assertTrue(instance.problemType == 1);
        assertTrue(ind.fitness.fitness() == -1000001.0);
    }

    /** Sphere function should return the correct value for point (3,3)*/
    @Test
    public void testEllipsoidalPointTwo() {
        BBOBenchmarks instance = new BBOBenchmarks();
        params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "ellipsoidal");
        params.set(BASE.push(BBOBenchmarks.P_ZERO_IS_BEST), "zeroIsBest");
        params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "2");
        params.set(BASE.push(BBOBenchmarks.P_XOPT), "0 0");
        instance.setup(state, BASE);
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.setGenome(new double[]{3, 3});
        ind.fitness = new SimpleFitness();
        instance.evaluate(state, ind, 0, 0);
        assertTrue(instance.problemType == 1);
        assertTrue(Math.abs(ind.fitness.fitness() - (-8720918.33))<=0.002);
    }

    /** Rastrigin function should return a fitness which is less than the optimal*/
    @Test
    public void testRastrigin(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "rastrigin");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 2);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Buche-Rastrigin function should return a fitness which is less than the optimal*/
    @Test
    public void testBucheRastrigin(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "buche-rastrigin");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 3);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** LinearSlope function should return a fitness which is less than the optimal*/
    @Test
    public void testLinearSlope(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "linear-slope");
            params.set(BASE.push(BBOBenchmarks.P_ZERO_IS_BEST),"zeroIsBest");
            params.set(BASE.push(BBOBenchmarks.P_XOPT),"5 5 5 5 5 5 5 5 5 5");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 4);
            assertTrue(ind.fitness.fitness() <= 58.119734925669775);
        }
    }

    /** Attractive-Sector function should return a fitness which is less than the optimal*/
    @Test
    public void testAttractiveSector(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "attractive-sector");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 5);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** StepEllipsoidal function should return a fitness which is less than the optimal*/
    @Test
    public void testStepEllipsoidal(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "step-ellipsoidal");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 6);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Rosenbrock function should return a fitness which is less than the optimal*/
    @Test
    public void testRosenbrock(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "rosenbrock");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 7);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** RosenbrockRotated function should return a fitness which is less than the optimal*/
    @Test
    public void testRosenbrockRotated(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "rosenbrock-rotated");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 8);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Ellipsoidal_2 function should return a fitness which is less than the optimal*/
    @Test
    public void testEllipsoidal2(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "ellipsoidal-2");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 9);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Discus function should return a fitness which is less than the optimal*/
    @Test
    public void testDiscus(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "discus");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 10);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Bent Cigar function should return a fitness which is less than the optimal*/
    @Test
    public void testBentCigar(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "bent-cigar");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 11);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Sharp Ridge function should return a fitness which is less than the optimal*/
    @Test
    public void testSharpRidge(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "sharp-ridge");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 12);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** DifferentPowers function should return a fitness which is less than the optimal*/
    @Test
    public void testDifferentPowers(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "different-powers");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 13);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Rastrigin2 function should return a fitness which is less than the optimal*/
    @Test
    public void testRastrigin2(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "rastrigin-2");
            instance.setup(state, BASE);

            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 14);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Weierstrass function should return a fitness which is less than the optimal*/
    @Test
    public void testWeierstrass(){
        for (int i = 0; i < 1000; i++){
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "weierstrass");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 15);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Schaffers_F7 function should return a fitness which is less than the optimal*/
    @Test
    public void testSchaffersF7() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "schaffers-f7");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 16);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Schaffers_F7_2 function should return a fitness which is less than the optimal*/
    @Test
    public void testSchaffersF72() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "schaffers-f7-2");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 17);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Griewank_Rosenbrock function should return a fitness which is less than the optimal*/
    @Test
    public void testGriewankRosenbrock() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "griewank-rosenbrock");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 18);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Schwefel function should return a fitness which is less than the optimal*/
    @Test
    public void testSchwefel() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "schwefel");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 19);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Gallagher_Gaussian_101ME function should return a fitness which is less than the optimal*/
    @Test
    public void testGallagherGaussian101ME() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "gallagher-gaussian-101me");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 20);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Gallagher_Gaussian_20HI function should return a fitness which is less than the optimal*/
    @Test
    public void testGallagherGaussian20HI() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "gallagher-gaussian-21hi");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 21);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Katsuura function should return a fitness which is less than the optimal*/
    @Test
    public void testKatsuura() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "katsuura");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 22);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }

    /** Lunacek function should return a fitness which is less than the optimal*/
    @Test
    public void testLunacek() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "lunacek");
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(instance.problemType == 23);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }
}
