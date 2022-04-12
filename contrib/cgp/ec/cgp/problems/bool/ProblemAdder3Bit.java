package ec.cgp.problems.bool;

import ec.*;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.problems.ProblemCGP;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.Parameter;

/**
 * 
 * 3 bit digital full adder problem class
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *
 */
public class ProblemAdder3Bit extends ProblemBoolean {

	/** Evaluate the CGP and compute fitness. */
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

		numInputs = 7;
		numOutputs = 4;
		numChunks = 4;
		
		NUM_BITS = 32;

		inputsCompressed = new Long[numChunks][numInputs];
		outputsCompressed = new Long[numChunks][numOutputs];

		inputsCompressed[0] = new Long[] { 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L, 2863311530L };

		inputsCompressed[1] = new Long[] { 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[2] = new Long[] { 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[3] = new Long[] { 4294967295L, 4294967295L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		outputsCompressed[0] = new Long[] { 3758129152L, 534806400L, 505313400L, 2576967270L };

		outputsCompressed[1] = new Long[] { 4261476352L, 33425400L, 3789653895L, 2576967270L };

		outputsCompressed[2] = new Long[] { 4292935552L, 3760160895L, 505313400L, 2576967270L };

		outputsCompressed[3] = new Long[] { 4294901752L, 4261541895L, 3789653895L, 2576967270L };

	}

}
