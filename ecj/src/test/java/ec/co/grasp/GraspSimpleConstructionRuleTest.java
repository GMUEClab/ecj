package ec.co.grasp;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.tsp.TSPIndividual;
import ec.app.tsp.TSPProblem;
import ec.co.ConstructiveIndividual;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleEvolutionState;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraspSimpleConstructionRuleTest
    {
        private final static Parameter BASE = new Parameter("base");
        private final static Parameter PROBLEM_BASE = new Parameter("prob");
        private EvolutionState state;
        private ParameterDatabase params;
        private TSPProblem problem;

        public GraspSimpleConstructionRuleTest()
        {
        }

        @Before
        public void setUp()
        {
            params = new ParameterDatabase();
            params.set(PROBLEM_BASE.push(TSPProblem.P_FILE), "src/main/resources/ec/app/tsp/berlin52.tsp");
            params.set(BASE.push(ec.co.grasp.SimpleConstructionRule.P_SELECTOR), GreedyComponentSelector.class.getCanonicalName());
            params.set(BASE.push(SimpleConstructionRule.P_START), "TSPComponent[from=0, to=21]");
            state = new SimpleEvolutionState();
            state.parameters = params;
            state.output = Evolve.buildOutput();
            state.output.getLog(0).silent = true;
            state.output.getLog(1).silent = true;
            state.output.setThrowsErrors(true);
            state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
            state.evaluator = new SimpleEvaluator();
            problem = new TSPProblem();
            problem.setup(state, PROBLEM_BASE);
            state.evaluator.p_problem = problem;
        }

        private TSPIndividual tourToInd(final int[] tour)
        {
            assert(tour != null);
            assert(tour.length == problem.numNodes());
            final TSPIndividual ind = new TSPIndividual();
            for (int i = 0; i < tour.length - 1; i++)
                ind.add(state, problem.getComponent(tour[i], tour[i + 1]));
            return ind;
        }

        @Test
        public void testConstructSolution1()
        {
            final SimpleConstructionRule instance = new SimpleConstructionRule();
            instance.setup(state, BASE);
            final TSPIndividual expResult = tourToInd(new int[] { 0, 21, 48, 31, 35, 34, 33, 38, 39, 37, 36, 47,
                    23, 4, 14, 5, 3, 24, 45, 43, 15, 49, 19, 22, 30,
                    17, 2, 18, 44, 40, 7, 9, 8, 42, 32, 50, 11, 27,
                    26, 25, 46, 12, 13, 51, 10, 28, 29, 20, 16, 41, 6, 1 });

            final ConstructiveIndividual result = instance.constructSolution(state, new TSPIndividual(), 0);
            assertEquals(expResult, result);
            assertTrue(instance.repOK());
            assertTrue(result.repOK());
        }

        @Test
        public void testConstructSolution2()
        {
            params.set(BASE.push(SimpleConstructionRule.P_START), "TSPComponent[from=27, to=26]");
            final SimpleConstructionRule instance = new SimpleConstructionRule();
            instance.setup(state, BASE);
            final TSPIndividual expResult = tourToInd(new int[] { 27, 26, 25, 46, 12, 13, 51, 10, 50, 11,
                    24, 3, 5, 4, 14, 23, 47, 37, 39, 36,
                    38, 35, 34, 33, 43, 45, 15, 49, 19, 22,
                    30, 17, 21, 0, 48, 31, 44, 18, 40, 7,
                    9, 8, 42, 32, 2, 16, 20, 29, 28, 41, 6, 1 });
            final ConstructiveIndividual result = instance.constructSolution(state, new TSPIndividual(), 0);
            assertEquals(expResult, result);
            assertTrue(instance.repOK());
            assertTrue(result.repOK());
        }
    }
