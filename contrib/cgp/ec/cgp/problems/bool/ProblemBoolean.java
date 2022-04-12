package ec.cgp.problems.bool;

import ec.EvolutionState;
import ec.cgp.problems.ProblemCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.Parameter;

/**
 * Abstract base class for boolean problems
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 * 
 *         TODO: Revise implementation with the use of Templates
 */
public abstract class ProblemBoolean extends ProblemCGP {

	public Object[][] inputsCompressed;
	public Object[][] outputsCompressed;

	public static int NUM_BITS;
	
	public int numInputs;
	public int numOutputs;
	public int numChunks;

	protected int getBit(int n, int k) {
		return (n >> k) & 1;
	}

	protected long getBit(long n, long k) {
		return (n >> k) & 1;
	}

	/*
	 * Evaluates the outputs of an individual against the real outputs of the boolean function.
	 * This evaluation method is used when we 
	 */
	protected int evaluate(Object outputsReal[], Object outputsInd[], VectorSpeciesCGP s) {

		int diff = 0;

		for (int i = 0; i < s.numOutputs; i++) {

			// Are we dealing with integer values?
			if (outputsReal instanceof Integer[]) {
				Integer result = (Integer) outputsInd[i];
				
				// Bitwise comparison of the outputs
				Integer compare = ~(result ^ (Integer) outputsReal[i]);

				// Calculate bit error
				for (int j = 0; j < NUM_BITS; j++) {
					int temp = compare;
					diff = diff + getBit(temp, j);
				}
			} else {
			// Otherwise, we have to do long arithmetic
				long result = (long) outputsInd[i];
				long compare = ~(result ^ (long) outputsReal[i]);

				for (int j = 0; j < NUM_BITS; j++) {
					long temp = compare;
					diff = (int) (diff + getBit(temp, j));
				}
			}
		}
		return diff;
	}

	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
	}

}
