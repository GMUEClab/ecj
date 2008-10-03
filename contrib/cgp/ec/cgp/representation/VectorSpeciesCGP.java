package ec.cgp.representation;

import java.util.Random;


import ec.EvolutionState;
import ec.cgp.Evaluator;
import ec.cgp.Util;
import ec.cgp.functions.Functions;
import ec.util.Parameter;
import ec.vector.VectorDefaults;
import ec.vector.VectorSpecies;

/**
 * 
 * The CGPVectorSpecies handles much of the behavior and representation of
 * Cartesian Genetic Programs. It is here that the phenotype/genotype mappings
 * and various representational constraints are imposed.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public abstract class VectorSpeciesCGP extends VectorSpecies {
	/** Gene position that represents an output node */
	public static final int GENE_OUTPUT = 0;
	/** Gene position that represents a function reference */
	public static final int GENE_FUNCTION = 1;
	/** Gene position that represents a function argument */
	public static final int GENE_ARGUMENT = 2;

	/** configuration names used in the parameter files */
	public static final String P_VECTORSPECIES = "species";
	public static final String P_NUM_NODES = "nodes";
	public static final String P_NUM_INPUTS = "inputs";
	public static final String P_NUM_OUTPUTS = "outputs";
	public static final String P_NUM_FUNCTIONS = "num-functions";
	public static final String P_MAX_ARITY = "maxArity";
	public static final String P_FUNCTIONS = "functions";

	/**
	 * Number of function call nodes comprising the CGP. Does not include output
	 * nodes.
	 */
	public int numNodes;

	/**
	 * Number of input nodes. Includes any automatically-managed constants (see
	 * CGPProblem).
	 */
	public int numInputs;

	/** Number of output nodes */
	public int numOutputs;

	/** Number of functions in the function set. */
	public int numFunctions;

	/** Max arity of the function set */
	public int maxArity;

	/**
	 * Computes the total number of gene values needed for all function and
	 * output nodes (a gene in this sense is a single element of the underlying
	 * genome vector)
	 */
	public int numGenes() {
		return numNodes * (maxArity + 1) + numOutputs;
	}

	/**
	 * Determine the function number from the given genome and position (int
	 * version).
	 */
	public int function(int position, float[] genome) {
		float val = genome[functionSub(position)];
		return Util.scale(val, numFunctions);
	}

	/**
	 * Determine the function number from the given genome and position (float
	 * version).
	 */
	public int function(int position, int[] genome) {
		return functionSub(position);
	}

	/**
	 * Computation shared by both function(position, genome) methods. Returns
	 * function position that corresponds to the given position.
	 */
	int functionSub(int position) {
		return (position / (maxArity + 1)) * (maxArity + 1);
	}

	/** From the given genome position, determine the phenotype (int version) */
	public int phenotype(int position, int[] genome) {
		if (position >= numNodes * (maxArity + 1))
			return GENE_OUTPUT;
		return position % (maxArity + 1) == 0 ? GENE_FUNCTION : GENE_ARGUMENT;
	}

	/** From the given genome position, determine the phenotype (float version) */
	public int phenotype(int position, float[] genome) {
		if (position >= numNodes * (maxArity + 1))
			return GENE_OUTPUT;
		return position % (maxArity + 1) == 0 ? GENE_FUNCTION : GENE_ARGUMENT;
	}

	/**
	 * Interprets float at position pos based on max possible int represented by
	 * that position.
	 */
	public int interpretFloat(int pos, float[] genome) {
		return Util.scale(genome[pos], computeMaxGene(pos, genome) + 1);
	}

	/**
	 * Computes max possible gene value for the given position. Min gene value
	 * is assumed to always be zero. The max value depends on the phenotype of
	 * the given position.
	 */
	public int computeMaxGene(int position, int[] genome) {
		int phenotype = phenotype(position, genome);
		if (phenotype == GENE_OUTPUT)
			return numInputs + numNodes - 1;
		if (phenotype == GENE_FUNCTION) {
			return numFunctions - 1;
		}
		/*
		 * Otherwise, this is a function argument. Since this is a feed-forward
		 * CGP, the max gene value must refer only to input nodes, or program
		 * nodes to the left of the current gene.
		 */
		return nodeNumber(position, genome) - 1; 
	}

	/**
	 * Computes max possible gene value for the given position. Min gene value
	 * is assumed to always be zero. The max value depends on the phenotype of
	 * the given position.
	 */
	public int computeMaxGene(int position, float[] genome) {
		int phenotype = phenotype(position, genome);
		if (phenotype == GENE_OUTPUT)
			return numInputs + numNodes - 1;
		if (phenotype == GENE_FUNCTION) {
			return numFunctions - 1;
		}
		/*
		 * Otherwise, this is a function argument. Since this is a feed-forward
		 * CGP, the max gene value must refer only to input nodes, or program
		 * nodes to the left of the current gene.
		 */
		return nodeNumber(position, genome) - 1; 
	}

	/**
	 * Determine the node number from the given genome position. Function
	 * arguments refer to node numbers. Inputs are numbered from [0, numInputs -
	 * 1]. Function nodes are numbered from [numInputs, numInputs + numNodes -
	 * 1]. Output nodes are numbered from [numInputs + numNodes, numInputs +
	 * numNodes + numOuputs - 1].
	 */
	public int nodeNumber(int position, int[] genome) {
		if (phenotype(position, genome) == GENE_OUTPUT)
			return numInputs + numNodes
					+ (position - (numNodes * (maxArity + 1)));
		return numInputs + (position / (maxArity + 1));
	}

	/**
	 * Determine the node number from the given genome position. Function
	 * arguments refer to node numbers. Inputs are numbered from [0, numInputs -
	 * 1]. Function nodes are numbered from [numInputs, numInputs + numNodes -
	 * 1]. Output nodes are numbered from [numInputs + numNodes, numInputs +
	 * numNodes + numOuputs - 1].
	 */
	public int nodeNumber(int position, float[] genome) {
		if (phenotype(position, genome) == GENE_OUTPUT)
			return numInputs + numNodes
					+ (position - (numNodes * (maxArity + 1)));
		return numInputs + (position / (maxArity + 1));
	}

	/**
	 * Determine the start position of the given node number. WARNING: do not
	 * call this with a nodeNumber < numInputs, because the inputs are not
	 * represented in the genome.
	 */
	public int positionFromNodeNumber(int nodeNumber) {
		if (nodeNumber < numInputs)
			throw new IllegalArgumentException("Sorry, nodeNumber ("
					+ nodeNumber + ") cannot be less than numInputs ("
					+ numInputs + ").");
		return (nodeNumber - numInputs) * (maxArity + 1);
	}

	public Parameter defaultBase() {
		return VectorDefaults.base().push(P_VECTORSPECIES);
	}

	public void setup(EvolutionState state, Parameter base) {
		System.out.println("species setup");

		Parameter def = defaultBase();
		numNodes = state.parameters.getInt(base.push(P_NUM_NODES), def
				.push(P_NUM_NODES), 1);
		numInputs = state.parameters.getInt(base.push(P_NUM_INPUTS), def
				.push(P_NUM_INPUTS), 1);
		System.out.println(numInputs + " SMEG!");
		numOutputs = state.parameters.getInt(base.push(P_NUM_OUTPUTS), def
				.push(P_NUM_OUTPUTS), 1);
		maxArity = state.parameters.getInt(base.push(P_MAX_ARITY), def
				.push(P_MAX_ARITY), 1);
		numFunctions = state.parameters.getInt(base.push(P_NUM_FUNCTIONS), def
				.push(P_NUM_FUNCTIONS), 1);
		if (numFunctions == 0) {
			state.output.fatal("species.num-functions must be > 0.");
		}

		/**
		 * TODO: make these more general because this is hard-coded to set
		 * params only for subpop #0.
		 */
		state.parameters.set(new Parameter("pop.subpop.0.species.genome-size"),
				"" + this.numGenes());
		state.parameters.set(new Parameter("pop.subpop.0.species.min-gene"),
				"0");

		Evaluator.functions = (Functions) state.parameters
				.getInstanceForParameter(base.push(P_FUNCTIONS), def
						.push(P_FUNCTIONS), Functions.class);

		/*
		 * for some reason, Evolve initializes Output with its "store" param set
		 * to true, and it is no longer configured via a parameter. So, we hard
		 * code it here to turn "store" off to conserve memory.
		 */
		state.output.setStore(false);

		state.output.exitIfErrors();
		super.setup(state, base);

		/* re-read this param because it got clobbered in super.setup */
		crossoverProbability = state.parameters.getFloat(base
				.push(P_CROSSOVERPROB), def.push(P_CROSSOVERPROB), 0.0, 1.0);

	}

	/** Get a simple description of the given phenotype */
	public static String phenotypeDescription(int phenotype) {
		if (phenotype == GENE_OUTPUT)
			return "output";
		if (phenotype == GENE_FUNCTION)
			return "function";
		return "function-argument";
	}

	public String toString() {
		return "CGP int species. " + numNodes + " nodes, " + numInputs
				+ " inputs, " + numOutputs + " outputs, " + numFunctions
				+ " functions, " + maxArity + " maxArity.  Number of genes: "
				+ numGenes();
	}

}
