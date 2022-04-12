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
 * 4 bit digital full adder problem class
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *
 */
public class ProblemAdder4Bit extends ProblemBoolean {

	/** Evaluate the CGP and compute fitness. */
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {

		if (ind.evaluated)
			return;

		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;

		int diff = 0;

		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<numChunks; i++)
		{
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
		numOutputs = 5;
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

		outputsCompressed[0] = new Long[] { 2147483648L, 2147450880L, 2139127680L, 2021161080L, 1717986918L };
		outputsCompressed[1] = new Long[] { 3758096384L, 536862720L, 534781920L, 505290270L, 2576980377L };
		outputsCompressed[2] = new Long[] { 4160749568L, 134215680L, 133695480L, 2273806215L, 1717986918L };
		outputsCompressed[3] = new Long[] { 4261412864L, 33553920L, 33423870L, 3789677025L, 2576980377L };

		outputsCompressed[4] = new Long[] { 4286578688L, 8388480L, 2155839615L, 2021161080L, 1717986918L };
		outputsCompressed[5] = new Long[] { 4292870144L, 2097120L, 3760185375L, 505290270L, 2576980377L };
		outputsCompressed[6] = new Long[] { 4294443008L, 524280L, 4161271815L, 2273806215L, 1717986918L };
		outputsCompressed[7] = new Long[] { 4294836224L, 131070L, 4261543425L, 3789677025L, 2576980377L };

		outputsCompressed[8] = new Long[] { 4294934528L, 2147516415L, 2139127680L, 2021161080L, 1717986918L };
		outputsCompressed[9] = new Long[] { 4294959104L, 3758104575L, 534781920L, 505290270L, 2576980377L };
		outputsCompressed[10] = new Long[] { 4294965248L, 4160751615L, 133695480L, 2273806215L, 1717986918L };
		outputsCompressed[11] = new Long[] { 4294966784L, 4261413375L, 33423870L, 3789677025L, 2576980377L };

		outputsCompressed[12] = new Long[] { 4294967168L, 4286578815L, 2155839615L, 2021161080L, 1717986918L };
		outputsCompressed[13] = new Long[] { 4294967264L, 4292870175L, 3760185375L, 505290270L, 2576980377L };
		outputsCompressed[14] = new Long[] { 4294967288L, 4294443015L, 4161271815L, 2273806215L, 1717986918L };
		outputsCompressed[15] = new Long[] { 4294967294L, 4294836225L, 4261543425L, 3789677025L, 2576980377L };

	}

}
