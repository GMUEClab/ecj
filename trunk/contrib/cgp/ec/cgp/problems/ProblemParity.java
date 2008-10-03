package ec.cgp.problems;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;
import ec.cgp.Evaluator;
import ec.cgp.FitnessCGP;
import ec.cgp.representation.VectorIndividualCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.multiobjective.*;

import java.util.*;

/**
 * 
 * Even n-bit parity problem.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class ProblemParity extends ProblemCGP {

	/** Number of bits in this parity problem. */
	public static int NUM_BITS;
	
	/** Max value represented by this parity problem. */
	public static int max() {
		int result = 1;
		for (int i=0; i<NUM_BITS; i++) result *= 2;
		return result;
	}

	/** Evaluate the CGP and compute fitness. */
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;

		VectorSpeciesCGP s = (VectorSpeciesCGP) ind.species;
		if (NUM_BITS == 0)
			NUM_BITS = s.numInputs;
		
		VectorIndividualCGP ind2 = (VectorIndividualCGP) ind;

		int diff = 0;
		Boolean[] inputs;
		
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<max(); i++) {

			/* generate binary input vector */
			int pow = 1;
			boolean even = true;
			inputs = new Boolean[NUM_BITS];
			for (int j=0; j<NUM_BITS; j++) {
				if (j>0) pow*=2;
				inputs[NUM_BITS-1-j] = (i & pow) > 0;
				if(inputs[NUM_BITS-1-j]) even = !even;
			}

			/* evaluate CGP */
			Object[] outputs = Evaluator.evaluate(state, threadnum, inputs, ind2);
			Boolean result = (Boolean) outputs[0];
			
			sb.append(result ? "1 " : "0 ");
			if (result != even) diff++;
		}
		
		((FitnessCGP)ind.fitness).setFitness(state, diff, diff == 0);
		
		ind2.expression.append("  Output: [" + sb + "]");
		ind.evaluated = true;
	}
	
}
