package ec.cgp.representation;


import ec.EvolutionState;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.vector.VectorDefaults;
import ec.vector.VectorSpecies;

/**
 * Integer-based genome representation of a Cartesian Genetic Program. Each
 * integer value is restricted to a range that is a function of its position in
 * the genome.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class IntegerVectorIndividual extends VectorIndividualCGP {

	public static final String P_INTEGERVECTORINDIVIDUAL = "int-vect-ind";

	/** the genome */
	public int[] genome;

	public Parameter defaultBase() {
		return VectorDefaults.base().push(P_INTEGERVECTORINDIVIDUAL);
	}

	/** Make a full copy of this individual. */
	public Object clone() {
		IntegerVectorIndividual myobj = (IntegerVectorIndividual) (super
				.clone());

		// must clone the genome
		myobj.genome = (int[]) (genome.clone());
		if (expression != null) {
			myobj.expression = new StringBuffer(expression);
		} 

		return myobj;
	}

	/**
	 * Mutate the genome. Adapted from IntegerVectorIndividual. The acceptable
	 * value range for each position is determined by
	 * CGPVectorSpecies.computeMaxGene.
	 */
	public void defaultMutate(EvolutionState state, int thread) {
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
                for (int x = 0; x < genome.length; x++)
                        if (state.random[thread].nextBoolean(s.mutationProbability(x))) {
                                genome[x] = randomValueFromClosedInterval(0, s
                                                .computeMaxGene(x, genome), state.random[thread]);
                        }

	}

	/** Initialize individual. */
	public void reset(EvolutionState state, int thread) {
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
		for (int x = 0; x < genome.length; x++)
			genome[x] = randomValueFromClosedInterval(0, s.computeMaxGene(x,
					genome), state.random[thread]);
	}

	/** Generate the human-readable text of the genotype, including the program's expression. */
	public String genotypeToStringForHumans() {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < genome.length; i++)
			sb.append(" " + genome[i]);
		sb.append(". Expression: " + expression);
		return sb.toString();
	}

	public int hashCode() {
		// stolen from GPIndividual. It's a decent algorithm.
		int hash = this.getClass().hashCode();

		hash = (hash << 1 | hash >>> 31);
		for (int x = 0; x < genome.length; x++)
			hash = (hash << 1 | hash >>> 31) ^ genome[x];

		return hash;
	}

	public boolean equals(Object ind) {
		if (!(this.getClass().equals(ind.getClass())))
			return false; // SimpleRuleIndividuals are special.
		IntegerVectorIndividual i = (IntegerVectorIndividual) ind;
		if (genome.length != i.genome.length)
			return false;
		for (int j = 0; j < genome.length; j++)
			if (genome[j] != i.genome[j])
				return false;
		return true;
	}

	/**
	 * Taken from IntegerVectorIndividual: Returns a random value from between
	 * min and max inclusive. This method handles overflows that complicate this
	 * computation. Does NOT check that min is less than or equal to max. You
	 * must check this yourself.
	 */
	public int randomValueFromClosedInterval(int min, int max,
			MersenneTwisterFast random) {
		if (max - min < 0) // we had an overflow
		{
			int l = 0;
			do
				l = random.nextInt();
			while (l < min || l > max);
			return l;
		} else
			return min + random.nextInt(max - min + 1);
	}

	/** Get the genome. */
	public Object getGenome() {
		return genome;
	}

	/** Set the genome. */
	public void setGenome(Object gen) {
		genome = (int[]) gen;
	}

	/** Return the genome length */
	public int genomeLength() {
		return genome.length;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base); // actually unnecessary unless
		// CGPVectorIndividal does something
		// (Individual.setup()
		// is empty)

		VectorSpecies s = (VectorSpecies) species;
		genome = new int[s.genomeSize];
	}

}
