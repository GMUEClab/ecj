package ec.cgp.problems.bool;

import ec.*;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.Parameter;

/**
 * 2 bit arithmetic logic unit problem class
 * 
 * Function set: AND, OR, XOR, ADD, SUB
 * 
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 */
public class ProblemALU2Bit extends ProblemBoolean {

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
		numOutputs = 3;
		numChunks = 5;
		
		NUM_BITS = 16;

		inputsCompressed = new Long[numChunks][numInputs];
		outputsCompressed = new Long[numChunks][numOutputs];
		
	
		inputsCompressed[0] = new Long[] { 0L, 0L, 0L, 65280L, 61680L, 52428L, 43690L  };

		inputsCompressed[1] = new Long[] { 0L, 0L, 65535L, 65280L, 61680L, 52428L, 43690L  };

		inputsCompressed[2] = new Long[] { 0L, 65535L, 0L, 65280L, 61680L, 52428L, 43690L  };

		inputsCompressed[3] = new Long[] { 0L, 65535L, 65535L, 65280L, 61680L, 52428L, 43690L };
		
		inputsCompressed[4] = new Long[] { 65535L, 0L, 0L, 65280L, 61680L, 52428L, 43690L};
				

		outputsCompressed[0] = new Long[] { 0L, 52224L, 41120L  };

		outputsCompressed[1] = new Long[] { 0L, 65484L, 64250L };
		
		outputsCompressed[2] = new Long[] { 0L, 13260L, 23130L };

		outputsCompressed[3] = new Long[] { 60544L, 37740L, 23130L  };

		outputsCompressed[4] = new Long[] { 58912L, 14790L, 23130L };

	}

}
