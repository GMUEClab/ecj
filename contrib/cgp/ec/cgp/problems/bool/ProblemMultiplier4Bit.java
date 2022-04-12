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
 * 4 bit digital multiplier problem class
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 */
public class ProblemMultiplier4Bit extends ProblemBoolean {

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

		numInputs = 8;
		numOutputs = 8;
		numChunks = 8;

		NUM_BITS = 32;

		inputsCompressed = new Long[numChunks][numInputs];
		outputsCompressed = new Long[numChunks][numOutputs];

		inputsCompressed[0] = new Long[] { 0L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[1] = new Long[] { 0L, 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[2] = new Long[] { 0L, 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[3] = new Long[] { 0L, 4294967295L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };

		inputsCompressed[4] = new Long[] { 4294967295L, 0L, 0L, 4294901760L, 4278255360L, 4042322160L, 3435973836L,
				2863311530L };

		inputsCompressed[5] = new Long[] { 4294967295L, 0L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };

		inputsCompressed[6] = new Long[] { 4294967295L, 4294967295L, 0L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };

		inputsCompressed[7] = new Long[] { 4294967295L, 4294967295L, 4294967295L, 4294901760L, 4278255360L, 4042322160L,
				3435973836L, 2863311530L };

		outputsCompressed[0] = new Long[] { 0L, 0L, 0L, 0L, 4278190080L, 4042260480L, 3435921408L, 2863267840L };

		outputsCompressed[1] = new Long[] { 0L, 0L, 4160749568L, 130088704L, 3342397680L, 3031747788L, 1718004394L,
				2863267840L };

		outputsCompressed[2] = new Long[] { 0L, 3758096384L, 528547584L, 0477163760L, 2473381068L, 1515891370L,
				3435921408L, 2863267840L };

		outputsCompressed[3] = new Long[] { 0L, 4227921920L, 3286239168L, 865650488L, 2874455220L, 505308774L,
				1718004394L, 2863267840L };

		outputsCompressed[4] = new Long[] { 2147483648L, 2130771712L, 2029056240L, 1724697804L, 1437248170L,
				4042260480L, 3435921408L, 2863267840L };

		outputsCompressed[5] = new Long[] { 4026589184L, 264249216L, 2386041968L, 1227133804L, 1838307930L, 3031747788L,
				1718004394L, 2863267840L };

		outputsCompressed[6] = new Long[] { 4227921920L, 2212497344L, 1662568248L, 1385477300L, 969303654L, 1515891370L,
				3435921408L, 2863267840L };

		outputsCompressed[7] = new Long[] {4261477376L, 3789603808L, 2576888728L, 1431612244L, 0033431070L,
				505308774L, 1718004394L, 2863267840L };

	}

}
