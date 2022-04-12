package ec.cgp.problems.bool;

import ec.EvolutionState;
import ec.Individual;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.Parameter;

/**
 * 
 * 1 bit digital full adder problem class
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *
 */
public class ProblemAdder1Bit extends ProblemBoolean {

	/** Evaluate the CGP and compute fitness. */
	@Override
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;
		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;

		int diff = 0;

		StringBuffer sb = new StringBuffer();

		Object[] outputs = Evaluator.evaluate(state, threadnum, inputsCompressed[0], ind2);

		diff = this.evaluate(outputsCompressed[0], outputs, s);

		((FitnessCGP) ind.fitness).setFitness(state, diff, diff == 0);

		ind2.expression.append("  Output: [" + sb + "]");
		ind.evaluated = true;

	}

	/*
	 * Initializes the compressed truth table for the given boolean function which
	 * is used for the evaluations of the individuals.
	 */
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		int numInputs = 3;
		int numOutputs = 2;
		
		NUM_BITS = (int) Math.pow(2, numInputs);

		inputsCompressed = new Integer[1][numInputs];
		outputsCompressed = new Integer[1][numOutputs];

		inputsCompressed[0] = new Integer[] { 240, 204, 170 };
		outputsCompressed[0] = new Integer[] { 232, 150 };
	}

}
