package ec.cgp.representation;


import ec.EvolutionState;
import ec.util.Code;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.vector.VectorDefaults;
import ec.vector.VectorIndividual;
import ec.vector.VectorSpecies;

/**
 * Float-based genome representation of a Cartesian Genetic Program. Gene values
 * are restricted to floats in the range [0,1]. During program evaluation, each
 * float value is scaled to integers in the acceptable range that is imposed by
 * the gene's position.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class FloatVectorIndividual extends VectorIndividualCGP {
	public static final String P_FLOATVECTORINDIVIDUAL = "float-vect-ind";

	/** the genome */
	public float[] genome;

	public Parameter defaultBase() {
		return VectorDefaults.base().push(P_FLOATVECTORINDIVIDUAL);
	}

	/** Make a full copy of this individual. */
	public Object clone() {
		FloatVectorIndividual myobj = (FloatVectorIndividual) (super
				.clone());

		// must clone the genome
		myobj.genome = (float[]) (genome.clone());
		if (expression != null)
			myobj.expression = new StringBuffer(expression);

		return myobj;
	}

	/** Mutate the genome. Adapted from FloatVectorIndividual. */
	public void defaultMutate(EvolutionState state, int thread) {
		VectorSpeciesCGP s = (VectorSpeciesCGP) species;
		MersenneTwisterFast rng = state.random[thread];

		for (int x = 0; x < genome.length; x++)
			if (rng.nextBoolean(s.mutationProbability(x)))
				genome[x] = rng.nextFloat();

	}

	/** Any-point crossover; kept here for posterity. */
	public void defaultCrossover2(EvolutionState state, int thread,
			VectorIndividual ind) {
		FloatVectorIndividual i = (FloatVectorIndividual) ind;
		FloatVectorSpecies s = (FloatVectorSpecies) species;
		float tmp;

		/* any point xover */
		for (int x = 0; x < genome.length; x++)
			if (state.random[thread].nextBoolean(s.crossoverProbability))
				for (int y = x; y < (x + 1); y++) {
					tmp = i.genome[y];
					i.genome[y] = genome[y];
					genome[y] = tmp;
				}

	}

	/**
	 * Convex (or "arithmetic") crossover for real-valued genomes. It is shown
	 * to yield improved convergence for regression problems (see Clegg et. al.,
	 * "A new crossover technique for Cartesian genetic programming"). But I
	 * have not yet seen convergence improvement for parity and classification
	 * problems.
	 */
	public void defaultCrossover(EvolutionState state, int thread,
			VectorIndividual ind) {

		VectorSpeciesCGP s = (VectorSpeciesCGP) species;
		float[] p1 = ((FloatVectorIndividual) ind).genome;
		float[] p2 = genome;
		float tmp;

		float[] r;
		do
			r = new float[] { state.random[thread].nextFloat(),
					state.random[thread].nextFloat() };
		while (r[0] == 0 || r[1] == 0);

		for (int i = 0; i < genome.length; i++) {
			tmp = p1[i];
			p1[i] = (1 - r[0]) * p1[i] + r[0] * p2[i];
			p2[i] = (1 - r[1]) * tmp + r[1] * p2[i];
		}

	}

	/**
	 * Initializes the individual by randomly
	 * choosing float values uniformly from mingene to maxgene.
	 * 
	 * Adapted from FloatVectorIndividual.
	 *  
	 */
	public void reset(EvolutionState state, int thread) {
		VectorSpeciesCGP s = (VectorSpeciesCGP) species;
		for (int x = 0; x < genome.length; x++)
			genome[x] = (float) (state.random[thread].nextFloat());
	}

	/** Copied from FloatVectorIndividual */
	public int hashCode() {
		// stolen from GPIndividual. It's a decent algorithm.
		int hash = this.getClass().hashCode();

		hash = (hash << 1 | hash >>> 31);
		for (int x = 0; x < genome.length; x++)
			hash = (hash << 1 | hash >>> 31) ^ Float.floatToIntBits(genome[x]);

		return hash;
	}
	
	/** Generate the human-readable text of the genotype, including the program's expression. */
	public String genotypeToStringForHumans() {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < genome.length; i++) {
			sb.append(" ");
			sb.append(genome[i]);
		}

		sb.append(". Expression: " + expression);
		return sb.toString();
	}

	public String genotypeToString() {
		StringBuffer s = new StringBuffer();
		s.append(Code.encode(genome.length));
		for (int i = 0; i < genome.length; i++)
			s.append(Code.encode(genome[i]));
		return s.toString();
	}

	/** Adapted from FloatVectorIndividual. */
	public boolean equals(Object ind) {
		if (!(this.getClass().equals(ind.getClass())))
			return false; // SimpleRuleIndividuals are special.
		FloatVectorIndividual i = (FloatVectorIndividual) ind;
		if (genome.length != i.genome.length)
			return false;
		for (int j = 0; j < genome.length; j++)
			if (genome[j] != i.genome[j])
				return false;
		return true;
	}

	/** 
	 * 
	 * Generate a random value with a Gaussian distribution.
	 * Currently unused but might be useful for some problems.
	 * 
	 * @param rand The RNG to use.
	 * @param mean The mean we want for our random values.
	 * @param std The standard deviation we want for our random values.
	 * @return the random value
	 */
	public static float randGaussian(MersenneTwisterFast rand, float mean,
			float std) {
		return mean + std * (float) rand.nextGaussian();
	}

	
	/**
	 * Get the genome
	 */
	public Object getGenome() {
		return genome;
	}

	/**
	 * Set the genome
	 */
	public void setGenome(Object gen) {
		genome = (float[]) gen;
	}

	/**
	 * Return the length of the genome
	 */
	public int genomeLength() {
		return genome.length;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base); // actually unnecessary unless
		// CGPVectorIndividal does something
		// (Individual.setup()
		// is empty)

		VectorSpecies s = (VectorSpecies) species;

		genome = new float[s.genomeSize];
	}

	public static void testGaussian() {
		MersenneTwisterFast rand = new MersenneTwisterFast();
		for (int i = 0; i < 100; i++) {
			System.out.println(randGaussian(rand, 3.0f, 0.1f));
		}
	}

	public static void main(String[] args) {
		testGaussian();
	}

}
