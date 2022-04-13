package ec.cgp.problems.bool;

import ec.EvolutionState;
import ec.cgp.problems.ProblemCGP;
import ec.cgp.representation.VectorSpeciesCGP;
import ec.util.Parameter;

/**
 * Abstract base class for boolean problems.
 * 
 * Contains the methods for the fitness evaluation with compressed truth tables.
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *
 */
public abstract class ProblemBoolean extends ProblemCGP {

	public Object[][] inputsCompressed;
	public Object[][] outputsCompressed;

	
	// Number of bits to compare
	public static int NUM_BITS;
	
	public int numInputs;
	public int numOutputs;
	
	// Number of chunks for large truth tables
	public int numChunks;

	// Returns the bit of an integer at position k 
	protected int getBit(int n, int k) {
		return (n >> k) & 1;
	}

	// Returns the bit of a long at position k 
	protected long getBit(long n, long k) {
		return (n >> k) & 1;
	}

	/*
	 * Evaluates the outputs of an individual against the real outputs of the boolean function.
	 * This method is used when we compute the fitness with compressed truth tables.
	 */
	protected int evaluate(Object outputsReal[], Object outputsInd[], VectorSpeciesCGP s) {

		int diff = 0;

		for (int i = 0; i < s.numOutputs; i++) {

			// Are we dealing with integer values?
			if (outputsReal instanceof Integer[]) {
				Integer result = (Integer) outputsInd[i];
				
				// Bitwise comparison of the outputs with XOR and complement
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

	// In derived classes we initialize the compressed truth table and define
	// the geometry of the boolean problem
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
	}

}
