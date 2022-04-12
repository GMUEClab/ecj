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
 * 9 bit parity-even problem class
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 */
public class ProblemParity9Bit extends ProblemBoolean {

	@Override
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;

		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;

		int diff = 0;

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < numChunks; i++) {
			Object[] outputs = Evaluator.evaluate(state, threadnum, inputsCompressed[i], ind2);
			diff += this.evaluate(outputsCompressed[i], outputs, s);
		}

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

		numInputs = 9;
		numOutputs = 1;
		numChunks = 16;

		NUM_BITS = 32;

		inputsCompressed = new Long[numChunks][numInputs];
		outputsCompressed = new Long[numChunks][numOutputs];

		inputsCompressed[0] = new Long[] { 0L, 0L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };
		inputsCompressed[1] = new Long[] { 0L, 0L, 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };
		inputsCompressed[2] = new Long[] { 0L, 0L, 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };
		inputsCompressed[3] = new Long[] { 0L, 0L, 4294967295L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };

		inputsCompressed[4] = new Long[] { 0L, 4294967295L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };
		inputsCompressed[5] = new Long[] { 0L, 4294967295L, 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };
		inputsCompressed[6] = new Long[] { 0L, 4294967295L, 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };
		inputsCompressed[7] = new Long[] { 0L, 4294967295L, 4294967295L, 4294967295L, 4294901760L, 4278255360L,
				4042322160L, 3435973836L, 2863311530L };

		inputsCompressed[8] = new Long[] { 4294967295L, 0L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };
		inputsCompressed[9] = new Long[] { 4294967295L, 0L, 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };
		inputsCompressed[10] = new Long[] { 4294967295L, 0L, 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };
		inputsCompressed[11] = new Long[] { 4294967295L, 0L, 4294967295L, 4294967295L, 4294901760L, 4278255360L,
				4042322160L, 3435973836L, 2863311530L };

		inputsCompressed[12] = new Long[] { 4294967295L, 4294967295L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };
		inputsCompressed[13] = new Long[] { 4294967295L, 4294967295L, 0L, 4294967295L, 4294901760L, 4278255360L,
				4042322160L, 3435973836L, 2863311530L };
		inputsCompressed[14] = new Long[] { 4294967295L, 4294967295L, 4294967295L, 0L, 4294901760L, 4278255360L,
				4042322160L, 3435973836L, 2863311530L };
		inputsCompressed[15] = new Long[] { 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294901760L,
				4278255360L, 4042322160L, 3435973836L, 2863311530L };

		outputsCompressed[0] = new Long[] { 1771476585L };
		outputsCompressed[1] = new Long[] { 2523490710L };
		outputsCompressed[2] = new Long[] { 2523490710L };
		outputsCompressed[3] = new Long[] { 177147658L };

		outputsCompressed[4] = new Long[] { 2523490710L };
		outputsCompressed[5] = new Long[] { 1771476585L };
		outputsCompressed[6] = new Long[] { 1771476585L };
		outputsCompressed[7] = new Long[] { 2523490710L };

		outputsCompressed[8] = new Long[] { 2523490710L };
		outputsCompressed[9] = new Long[] { 1771476585L };
		outputsCompressed[10] = new Long[] { 1771476585L };
		outputsCompressed[11] = new Long[] { 2523490710L };

		outputsCompressed[12] = new Long[] { 1771476585L };
		outputsCompressed[13] = new Long[] { 2523490710L };
		outputsCompressed[14] = new Long[] { 2523490710L };
		outputsCompressed[15] = new Long[] { 1771476585L };
	}
}