package ec.cgp.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import ec.EvolutionState;
import ec.cgp.Stats;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.vector.VectorIndividual;

/**
 * This class is an extension of the CGP IntegerVectorIndividual class. 
 * It contains a set of advanced genetic operators which have been proposed
 * recently. 
 * 
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 *         
 * @author Jakub Husa, ihusa@fit.vut.cz
 * 		   https://www.vut.cz/en/people/jakub-husa-138342?aid_redir=1
 *         
 */
public class AdvancedIntegerVectorIndividual extends IntegerVectorIndividual {

	int maxActiveGenes;

	public ArrayList<Integer> activeFunctionNodes;
	public ArrayList<Integer> passiveFunctionNodes;

	/**
	 * TODO Check if the overflow case is really needed
	 */
	public int randomValueFromClosedInterval(int min, int max, int val, MersenneTwisterFast random) {
		int l = 0;
		if (max - min < 0 || max == min) {
			return val;
		} else {

			do {
				l = min + random.nextInt(max - min + 1);
			} while (l == val);

			return l;
		}
	}

	/**
	 * Checks whether a certain gene is active or not. 
	 */
	public boolean geneActive(ArrayList<Integer> activeFunctionNodes, AdvancedIntegerVectorSpecies s, int nodeNum,
			int genePos) {
		return (activeFunctionNodes.contains(nodeNum) || s.phenotype(genePos, genome) == s.GENE_OUTPUT);
	}


	/*
	 *  Simple genotypic one point crossover technique. Did not work very well in the past. 
	 * 
	 *  References: 
	 *  Miller (1999) http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.47.5554
	 *  Husa and Kalkreuth (2018) http://dx.doi.org/10.1007/978-3-319-77553-1_13
	 */
	public void onepointCrossover(EvolutionState state, int thread, AdvancedIntegerVectorIndividual ind)
	{
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;// the current species, same for both parents
		AdvancedIntegerVectorIndividual i = (AdvancedIntegerVectorIndividual) ind;// the second parent
		int tmp;
		int point;

		// check that the chromosomes are equally long (true in most cases)
		int len = Math.min(genome.length, i.genome.length);
		if (len != genome.length || len != i.genome.length)
			state.output.warnOnce(
					"Genome lengths are not the same.  Vector crossover will only be done in overlapping region.");

		point = state.random[thread].nextInt((len / s.chunksize));// randomly select the point of crossover
		for (int x = 0; x < point * s.chunksize; x++)// swaps the first halves of the chromosomes
		{
			tmp = i.genome[x];
			i.genome[x] = genome[x];
			genome[x] = tmp;
		}

	}
	
	/*
	 * Adaption of discrete/uniform recombination in CGP. This is a phenotypic variation 
	 * method for discrete recombination in CGP. 
	 * 
	 * It adapts discrete recombination in CGP by means of phenotypic functional variation 
	 * which is performed through the exchange of function genes of active function nodes.
	 * 
	 * References: 
	 * Kalkreuth (2022): Towards Discrete Phenotypic Recombination in Cartesian Genetic Programming 
	 * (accepted for publication the seventeenth International Conference on Parallel Problem Solving from Nature (PPSN XVII) 
	 * 
	*/
	public void discreteCrossover(EvolutionState state, int thread, AdvancedIntegerVectorIndividual ind)
	{
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species; // the current species, same for both parents
		AdvancedIntegerVectorIndividual i = (AdvancedIntegerVectorIndividual) ind;// the second parent
		
		// Determine active nodes
		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);
		s.determineActiveFunctionNodes(i.activeFunctionNodes, s, genome);
		
		// Sort the nodes in ascending order 
		Collections.sort(activeFunctionNodes);
		Collections.sort(i.activeFunctionNodes);
		
		int tmp = 0;
		
		// Node numbers are stored if two nodes are selected for the swap of the function gene 
		int swapNode1 = 0;
		int swapNode2 = 0;
		
		// Indices for the function genes
		int index1 = 0;
		int index2 = 0;
				
		boolean boundaryExpansion = true;
		
		// Determine the phenotypic length
		int len1 = activeFunctionNodes.size();
		int len2 = i.activeFunctionNodes.size();

		// check that the chromosomes are equally long (true in most cases)
		int min = Math.min(len1, len2);
		int max = Math.max(len1, len2);
	
		// Iterate over the minimum phenotype length
		for (int x = 0; x < min; x++) 
		{
			if(state.random[thread].nextBoolean()) {
				
				if(boundaryExpansion && x == (min - 1) && len1 != len2  ) {
					
					int r = state.random[thread].nextInt(max - x);
					
					if(len1 < len2) {
						swapNode1 = activeFunctionNodes.get(x);
						swapNode2 = i.activeFunctionNodes.get(x + r);
					} else {
						swapNode1 = activeFunctionNodes.get(x + r);
						swapNode2 = i.activeFunctionNodes.get(x);
					}
				
					
				} else {
					swapNode1 = activeFunctionNodes.get(x);
					swapNode2 = i.activeFunctionNodes.get(x);
				}
				
				// calculate the swap indexes
				index1 = (swapNode1 - s.numInputs) * (1 + s.maxArity);
				index2 = (swapNode2 - s.numInputs) * (1 + s.maxArity);
				
				// perform the swaps
				tmp = genome[index1];
				genome[index1] = i.genome[index2];
				i.genome[index2] = tmp;
			}			
		}
	}
	
	

	/*
	 * Determines a set of active function node by chance in accordance to the predefined 
	 * maximum block size.
	 */
	public void determineSwapNodes(int blockSize, ArrayList<Integer> swapNodesList,
			ArrayList<Integer> activeFunctionNodes, EvolutionState state, int thread) {

		int j = 0;
		int randIndex;
		int nodeNumber;

		ArrayList<Integer> possibleNodes = new ArrayList<>(activeFunctionNodes);

		while (j < blockSize) {
			randIndex = state.random[thread].nextInt(possibleNodes.size());
			nodeNumber = possibleNodes.get(randIndex);

			swapNodesList.add(nodeNumber);

			possibleNodes.remove(randIndex);
			j++;
		}
	}
	

	
	/*
	 * Block crossover swaps blocks of active function genes between two individuals.
	 * The block crossover uses a parameter blockSize which defines the maximum block size.
	 * 
	 * You can set this parameter in your parameter file with pop.subpop.0.species.block-size
	 * 
	 * Reasonable results were obtained on several symbolic regression benchmarks (Kalkreuth (2021))
	 * 
	 * References: 
	 * Husa and Kalkreuth (2018) http://dx.doi.org/10.1007/978-3-319-77553-1_13
	 * Kalkreuth (2021) http://dx.doi.org/10.17877/DE290R-22504
	 */
	public void blockCrossover(EvolutionState state, int thread, AdvancedIntegerVectorIndividual ind, int blockSize) {
		int swapNode1 = 0;
		int swapNode2 = 0;
		int swapIndex1 = 0;
		int swapIndex2 = 0;

		int j = 0;
		int temp = 0; // used to store values during swapping

		// memorize the individual's sepecies and the other individual
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		AdvancedIntegerVectorIndividual i = (AdvancedIntegerVectorIndividual) ind;// the second parent
		
		ArrayList<Integer> swapNodesList1 = new ArrayList<Integer>();
		ArrayList<Integer> swapNodesList2 = new ArrayList<Integer>();

		// Determine active nodes
		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);
		s.determineActiveFunctionNodes(i.activeFunctionNodes, s, genome);

		// Validate the phenotype length before the recombination process
		if ((activeFunctionNodes.size() == 0) || (i.activeFunctionNodes.size() == 0)) {
			return;
		}

		if ((activeFunctionNodes.size() < blockSize) || (i.activeFunctionNodes.size() < blockSize)) {
			blockSize = Math.min(activeFunctionNodes.size(), i.activeFunctionNodes.size());
		}
		
		// Deterrmine the numbers of function nodes which will be swapped
		determineSwapNodes(blockSize, swapNodesList1, activeFunctionNodes, state, thread);
		determineSwapNodes(blockSize, swapNodesList2, i.activeFunctionNodes, state, thread);

		for (j = 0; j < blockSize; j++) {
			// Get the node numbers selected for the swap
			swapNode1 = swapNodesList1.get(j);
			swapNode2 = swapNodesList2.get(j);

			// Calculate the function node indexes 
			swapIndex1 = (swapNode1 - s.numInputs) * (1 + s.maxArity);
			swapIndex2 = (swapNode2 - s.numInputs) * (1 + s.maxArity);
		
			// perform the swaps
			temp = genome[swapIndex1];
			genome[swapIndex1] = i.genome[swapIndex2];
			i.genome[swapIndex2] = temp;

		}
	}



	/*
	 * Single active gene mutation strategy (SAM). The genome is randomly mutated by point mutation until
	 * exactly one active gene has been hit. 
	 * 
	 * References: 
	 * Goldman and Punch (2014) http://dx.doi.org/10.1109/TEVC.2014.2324539
	 * 
	 */
	public void singleActiveGeneMutation(EvolutionState state, int thread) {
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		ArrayList<Integer> activeFunctionNodes = new ArrayList<Integer>();
		
		// Determine the active function nodes
		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);
		
		int nodeNum;
		int genePos;
		int geneVal;
		
		boolean hitActiveGene = false;

		do {
			// Select a gene by chance within the genotype
			genePos = state.random[thread].nextInt(genome.length);
			geneVal = genome[genePos];
			
			// Mutate the gene by resetting its value
			genome[genePos] = randomValueFromClosedInterval(0, s.computeMaxGene(genePos, genome), geneVal,
					state.random[thread]);

			// Get the node number
			nodeNum = s.nodeNumber(genePos, genome);

			// Check if the mutated gene is an active one
			if (geneActive(activeFunctionNodes, s, nodeNum, genePos)) {
				hitActiveGene = true;
			}

		} while (!hitActiveGene);
	}
	
	/**
	 * This is an extension of the single active gene mutation strategy. 
	 * The genome is randomly mutated by point mutation until the predefined number of 
	 * active genes have been hit. 
	 * 
	 * You can set the number of active genes in your parameter file with pop.subpop.0.species.mutate-active-genes 
	 */
	public void multiActiveGeneMutation(EvolutionState state, int thread, int num) {
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		ArrayList<Integer> activeFunctionNodes = new ArrayList<Integer>();
		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);
		int nodeNum;
		int genePos;
		int geneVal;
		int activeGenesHit = 0;

		do {
			genePos = state.random[thread].nextInt(genome.length);
			geneVal = genome[genePos];
			genome[genePos] = randomValueFromClosedInterval(0, s.computeMaxGene(genePos, genome), geneVal,
					state.random[thread]);

			nodeNum = s.nodeNumber(genePos, genome);

			if (geneActive(activeFunctionNodes, s, nodeNum, genePos)) {
				activeGenesHit++;
			}
		// The strategy is equal to SAM but here we mutate active genes 
		// until a predefined number of genes has been hit
		} while (activeGenesHit < num);

	}


	/**
	 * Mutate the genome. Adapted from IntegerVectorIndividual. The acceptable value
	 * range for each position is determined by CGPVectorSpecies.computeMaxGene.
	 */
	public void pointMutation(EvolutionState state, int thread) {
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
		for (int x = 0; x < genome.length; x++)
			if (state.random[thread].nextBoolean(s.mutationProbability(x))) {
				genome[x] = randomValueFromClosedInterval(0, s.computeMaxGene(x, genome), state.random[thread]);
			}
	}



	/**
	 * First, the maximum possible depth for the duplication and inversion mutation is determined. The depth
	 * is than chosen by chance in respect to the maximum.
	 */
	public int stochasticDepth(EvolutionState state, int thread, int maxDepth, int numActiveFunctionNodes) {
		int depth;
		int max;

		if (numActiveFunctionNodes <= maxDepth) {
			max = numActiveFunctionNodes - 1;
		} else {
			max = maxDepth;
		}

		depth = state.random[thread].nextInt(max) + 1;

		return depth;
	}

	/**
	 * Determines a suitable start index for the duplication and inversion mutation by chance 
	 * and in respect to the number of active function nodes. 
	 */
	public int startIndex(EvolutionState state, int thread, int numactiveFunctionNodes, int depth) {
		int startMax = numactiveFunctionNodes - depth;
		int start;

		if (startMax <= 0) {
			start = 0;
		} else {
			start = state.random[thread].nextInt(startMax);
		}

		return start;
	}
	
	/**
	 * Phenotypic inverison mutation: 
	 * Inverts the order of function genes of a randomly selected set of active nodes. The size of the set 
	 * is determined by chance and in respect to the number of active nodes.
	 *  
	 * Kalkreuth (2022): Phenotypic Duplication and Inversion in Cartesian Genetic Programming applied to Boolean Function Learning
 	 * (accepted for poster presentation at GECCO’22)
	 */
	public void inversion(EvolutionState state, int thread) {
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;

		if (!state.random[thread].nextBoolean(s.inversionProbability)) {
			return;
		}

		int depth;
		int start;
		int end;
		int leftNode;
		int leftPosition;
		int rightNode;
		int rightPosition;
		int middle;
		int tmp;

		boolean debug = false;
		
		int numactiveFunctionNodes = activeFunctionNodes.size();
		
		
		Collections.sort(activeFunctionNodes);

		// We need at least two active function nodes
		if (numactiveFunctionNodes <= 1) {
			return;
		}

		// Determine a valid inversion depth by chance and get a suitable start index
		depth = stochasticDepth(state, thread, s.maxInversionDepth, numactiveFunctionNodes);
		start = startIndex(state, thread, numactiveFunctionNodes, depth);
		
		// Calculate end point and middle for the set of nodes which will be mutated
		end = start + depth;
		middle = (int) Math.round(depth / 2.0);

		// Perform the inversion by iterating until the middle is reached and pairwise exchanging 
		// the function genes
		for (int i = 0; i < middle; i++) {
			leftNode = activeFunctionNodes.get(start + i);
			rightNode = activeFunctionNodes.get(end - i);

			leftPosition = s.positionFromNodeNumber(leftNode);
			rightPosition = s.positionFromNodeNumber(rightNode);

			tmp = genome[leftPosition];
			genome[leftPosition] = genome[rightPosition];
			genome[rightPosition] = tmp;
		}
	}

	/**
	 * Phenotypic duplication mutation: 
	 * Duplicates the function gene of a randomly selected active node to a following sequence of active nodes. 
	 * The size of the sequence is determined by chance and in respect to the number of active nodes.
	 *  
	 * Kalkreuth (2022): Phenotypic Duplication and Inversion in Cartesian Genetic Programming applied to Boolean Function Learning
 	 * (accepted for poster presentation at GECCO’22)
	 */
	public void duplication(EvolutionState state, int thread) {
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;

		if (!state.random[thread].nextBoolean(s.duplicationProbability)) {
			return;
		}
		
		int depth;
		int start;
		int end;
		int position;
		int node;
		int tmp;
		int function;

		boolean debug = false;
		int numactiveFunctionNodes = activeFunctionNodes.size();
		
		Collections.sort(activeFunctionNodes);

		// We need at least two active function nodes
		if (numactiveFunctionNodes <= 1) {
			return;
		}
		
		// Determine a valid inversion depth by chance and get a suitable start index
		depth = stochasticDepth(state, thread, s.maxDuplicationDepth, numactiveFunctionNodes);
		start = startIndex(state, thread, numactiveFunctionNodes, depth);
		end = start + depth;

		// Get the node number with respect to the determined start index
		node = activeFunctionNodes.get(start);
		
		// Get the position of the function gene
		position = s.positionFromNodeNumber(node);
		
		// Get the function gene value 
		function = genome[position];

		// Finally duplicate the gene with the respective depth
		for (int i = start + 1; i <= end; i++) {
			node = activeFunctionNodes.get(i);
			position = s.positionFromNodeNumber(node);
			genome[position] = function;
		}
	}


	/**
	 * First the primary mutation is selected and then the additional advanced mutation(s) is/are executed.
	 * It is highly recommended to use inversion and duplication with probabilistic point or active gene mutation.
	 */
	public void defaultMutate(EvolutionState state, int thread) {
		
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		
		if(s.mutationType == s.C_POINT) {
			pointMutation(state, thread);
		} else if (s.mutationType == s.C_SINGLE) {
			singleActiveGeneMutation(state, thread);
		} else if (s.mutationType == s.C_MULTI) {
			multiActiveGeneMutation(state, thread, s.mutateActiveGenes);
		}
		
		if(s.inversionProbability > 0.0f || s.duplicationProbability > 0.0f) {
			s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);	
		}
		
		if(s.inversionProbability > 0.0f) {
			inversion(state, thread);	
		}
		
		if(s.duplicationProbability > 0.0f) {
			duplication(state, thread);
		}

	}
	
	/** Make a full copy of this individual. */
	public Object clone() {
		AdvancedIntegerVectorIndividual myobj = (AdvancedIntegerVectorIndividual) (super.clone());

		if (activeFunctionNodes != null) {
			myobj.activeFunctionNodes = new ArrayList<Integer>();
		}
		
		if (passiveFunctionNodes != null) {
			myobj.passiveFunctionNodes = new ArrayList<Integer>();
		}

		return myobj;
	}
	
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base); // actually unnecessary unless
		activeFunctionNodes = new ArrayList<Integer>();
		passiveFunctionNodes = new ArrayList<Integer>();
		
	}


}
