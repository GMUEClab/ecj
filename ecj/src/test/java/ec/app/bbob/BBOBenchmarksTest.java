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
        params.set(BASE.push(BBOBenchmarks.P_WHICH_PROBLEM), "sphere");
        params.set(BASE.push(BBOBenchmarks.P_NOISE), "none");
        params.set(BASE.push(BBOBenchmarks.P_REEVALUATE_NOISY_PROBLEMS), "true");
        state = new EvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
    }

    @Test
    public void testEvaluate() {
        for (int i = 0; i < 1000; i++) {
            BBOBenchmarks instance = new BBOBenchmarks();
            instance.setup(state, BASE);
            final DoubleVectorIndividual ind = new DoubleVectorIndividual();
            ind.setGenome(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0 });
            ind.fitness = new SimpleFitness();
            instance.evaluate(state, ind, 0, 0);
            assertTrue(ind.fitness.fitness() <= -instance.fOpt);
        }
    }
}
