package ec.cgp.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.cgp.Evaluator;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

/**
 * 
 * A CGP Problem. Provides a single-threaded setup hook that initializes static
 * objects used during evaluation, and provides storage for constants.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public abstract class ProblemCGP extends Problem implements SimpleProblemForm {

	/**
	 * Constants randomly initialized at setup for some problems. Once the
	 * constants are created, they are kept throughout the run.
	 */
	static float[] constants;

	/** number of constants to use */
	static int numConstants;

	/** closed lower bound for generated constants */
	static float constantMin;

	/** open upper bound for generated constants */
	static float constantMax;

	static String P_CONSTANTS = "constants";
	static String P_CONSTANT_MIN = "constant-min";
	static String P_CONSTANT_MAX = "constant-max";

	/**
	 * Currently does nothing except enforce the SimpleProblemForm contract.
	 */
        @Override
	public void describe(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum, final int log)
        {
            return;
        }

	/**
	 * Initialize the evaluation cache, and configure the random constants.
	 */
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		/** Initialize the List of Maps used during evaluation. */
		Evaluator.nodeMap = new ArrayList<Map<Integer, Object>>();
		for (int i = 0; i < state.evalthreads; i++)
			Evaluator.nodeMap.add(new HashMap<Integer, Object>());

		Evaluator.expressionMap = new ArrayList<Map<Integer, String>>();
		for (int i = 0; i < state.evalthreads; i++)
			Evaluator.expressionMap.add(new HashMap<Integer, String>());

		Parameter def = defaultBase();

		numConstants = state.parameters.getIntWithDefault(base
				.push(P_CONSTANTS), def.push(P_CONSTANTS), 0);
		constantMin = state.parameters.getFloatWithDefault(base
				.push(P_CONSTANT_MIN), def.push(P_CONSTANT_MIN), 0);
		constantMax = state.parameters.getFloatWithDefault(base
				.push(P_CONSTANT_MAX), def.push(P_CONSTANT_MAX), 1);
		initConstants(state);
	}

	/**
	 * Initialize the random constants once before this run. Values are
	 * generated uniformly at random within the half-open range [constantMin,
	 * constantMax). The same constant values are used throughout this run.
	 */
	void initConstants(EvolutionState state) {
		constants = new float[numConstants];
		/* make some fixed random constants to be used for the entire run. */
		for (int i = 0; i < numConstants; i++)
			constants[i] = constantMin + state.random[0].nextFloat()
					* (constantMax - constantMin);
	}

}
