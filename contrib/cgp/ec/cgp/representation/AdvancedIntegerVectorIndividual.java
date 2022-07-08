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
	 * (accepted for publication at the seventeenth International Conference on Parallel Problem Solving from Nature (PPSN XVII) 
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
				
		// Boundary extension is activated by default  
		boolean boundaryExtension = true;
		
		// Determine the phenotypic length
		int len1 = activeFunctionNodes.size();
		int len2 = i.activeFunctionNodes.size();

		// check that the chromosomes are equally long (true in most cases)
		int min = Math.min(len1, len2);
		int max = Math.max(len1, len2);
	
		// Iterate over the minimum phenotypic length
		for (int x = 0; x < min; x++) 
		{
			// Decide uniformly at random whether a gene swap will be performed or not 
			if(state.random[thread].nextBoolean()) {
				
				// If boundary extension is activated, we select a swap node for
				// the phenotypically larger parent beyond the minimum number of 
				// active nodes of both parents
				if(boundaryExtension && x == (min - 1) && len1 != len2  ) {
					
					// Choose the extension by chance
					int r = state.random[thread].nextInt(max - x);
					
					if(len1 < len2) {
						swapNode1 = activeFunctionNodes.get(x);
						swapNode2 = i.activeFunctionNodes.get(x + r);
					} else {
						swapNode1 = activeFunctionNodes.get(x + r);
						swapNode2 = i.activeFunctionNodes.get(x);
					}
				
				// Just choose the swap nodes "in-line" without any extension  	
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
	 * Both sections are connected with a special step that is called neighbourhood connect. 
	 * The step refers to the first active node of the section
	 * behind the crossover point which is connected to the last active node
	 * of the section in front of the crossover point. This is done by adjusting
	 * the connection gene of the first active node of the section behind the
	 * crossover point.
	 */
	public void mergeParts(ArrayList<Integer> legalNodes, ArrayList<Integer> activeNodesPart1,
			ArrayList<Integer> activeNodesPart2, int crossoverPosition, int crossoverPoint, int[] genomePart1,
			int[] genomePart2, int[] newGenome, MersenneTwisterFast rand) {

		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		
	
		boolean connectedNeighborNode = false;
		
		// Copy the genetic material from both parents with respect 
		// to crossover point. Forming of the new genome. 

		for (int i = 0; i < crossoverPosition; i++) {
			newGenome[i] = genomePart1[i];
		}

		for (int i = crossoverPosition; i < genomePart1.length; i++) {
			newGenome[i] = genomePart2[i];
		}
		
		// Connect both sections of the new genome 
		for (int i = crossoverPosition; i < newGenome.length; i++) {
			if (s.phenotype(i, newGenome) == s.GENE_FUNCTION) {
				int nodeNum = s.nodeNumber(i, newGenome);

				// Check wether the node is active 
				if (activeNodesPart2.contains(nodeNum)) {
					for (int j = 1; j <= s.maxArity; j++) {
						int randArg; 
						
						// If the neighbor node has not been directly connected yet
						if (s.connectNeighborNode && !connectedNeighborNode) {
							// Perform the neighborhood connect
							if (crossoverPoint >= 1) {
								newGenome[i + j] = activeNodesPart1.get(crossoverPoint);
							} else {
								newGenome[i + j] = activeNodesPart1.get(0);
							}
							connectedNeighborNode = true;
						// Otherwise, perform the random active connect
						} else if (!legalNodes.contains(newGenome[i + j])) {
							
							// Chose between a connection to an previous function node
							// or input by chance
							if (rand.nextBoolean(0.5)) {
								randArg = rand.nextInt(s.numInputs);
							} else {

								int max = legalNodes.indexOf(nodeNum);
								int min = s.numInputs;
								randArg = rand.nextInt((max - min)) + min;

							}

							newGenome[i + j] = legalNodes.get(randArg);
						}
					}
				}
			}
		}
		reconnectOutputs(newGenome, legalNodes, rand);
	}

	/*
	 * Ensures that all outputs of the offspring are connected to legal nodes. Reconnects all outputs
	 * that are not connected with a legal node. 
	 */ 
	public void reconnectOutputs(int[] genome, ArrayList<Integer> legalNodes, MersenneTwisterFast rand) {

		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
		
		// Perform an random active connect for all outputs that currently connected 
		// to illegal (inactive) nodes. 
		for (int i = 0; i < s.numOutputs; i++) {
			int nodeNum = s.nodeNumber(genome.length - i - 1, genome);
			int randArg;

			if (!legalNodes.contains(genome[genome.length - i - 1])) {
				int max = legalNodes.size();
				int min = s.numInputs;
				randArg = rand.nextInt((max - min)) + min;

				genome[genome.length - i - 1] = randArg;

			}

		}
	}

	/*
	 * 	To ensure that active nodes of the section behind the crossover point do
	 * 	not refer to previous inactive nodes of the section in front of the crossover point,
	 *	the subgraph crossover perform a step which is called random active connect. All connection
	 *	genes of the active nodes of the section behind the crossover point are
	 *	adjusted to the active nodes of the section in front of the crossover point,
	 *	previous active nodes of input nodes. The nodes which are suitable
	 *	for a random active connection are named as legal nodes. 
	 */
	public void createLegalNodes(ArrayList<Integer> legalNodes, ArrayList<Integer> activeNodesPart1,
			ArrayList<Integer> activeNodesPart2, int crossoverNode) {
		IntegerVectorSpecies s = (IntegerVectorSpecies) species;

		// Add the input nodes to the set of legal nodes
		for (int i = 0; i < s.numInputs; i++) {
			legalNodes.add(i);
		}

		// Add the active nodes representing the first phenotype 
		for (int i = 0; i < activeNodesPart1.size(); i++) {
			if (activeNodesPart1.get(i) <= crossoverNode) {
				legalNodes.add(activeNodesPart1.get(i));
			}
		}

		// Add the active nodes representing the second phenotype 
		for (int i = 0; i < activeNodesPart2.size(); i++) {
			if (activeNodesPart2.get(i) > crossoverNode) {
				legalNodes.add(activeNodesPart2.get(i));
			}
		}

	}
	
	/**
	 * The subgraph crossover exclusively recombines 
	 * genetic material of the active paths which represent the phenotypes of the individuals.
	 * Therefore the active function nodes of both parents are determined and a suitable crossover point 
	 * is chosen afterwards. 
	 */
	public void subgraphCrossover(EvolutionState state, int thread, VectorIndividual ind) {
		
		AdvancedIntegerVectorIndividual ind2 = (AdvancedIntegerVectorIndividual) ind;
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;

		int[] genome1 = genome;
		int[] genome2 = ind2.genome;

		ArrayList<Integer> activeFunctionNodes1 = new ArrayList<Integer>();
		ArrayList<Integer> activeFunctionNodes2 = new ArrayList<Integer>();
		ArrayList<Integer> legalNodes = new ArrayList<Integer>();
		

		s.determineActiveFunctionNodes(activeFunctionNodes1, s, genome1);
		s.determineActiveFunctionNodes(activeFunctionNodes2, s, genome2);
		
		// Discard the crossover if at least one parent has no active function nodes
		if (activeFunctionNodes1.size() == 0)
			return;

		if (activeFunctionNodes2.size() == 0)
			return;
		
		MersenneTwisterFast rand = state.random[thread];

		Collections.sort(activeFunctionNodes1);
		Collections.sort(activeFunctionNodes2);

		int c1;
		int c2;
		
		// Choose two potential crossover point by chance 
		c1 = rand.nextInt(activeFunctionNodes1.size());
		c2 = rand.nextInt(activeFunctionNodes2.size());
		
		
		// Array for the genome of the offspring
		int[] offspring = new int[(s.numNodes * (s.maxArity + 1)) + s.numOutputs];
		subgraphCrossover(c1, c2, activeFunctionNodes1, activeFunctionNodes2, legalNodes, genome1, genome2, offspring, state,
				thread);
		
		genome = offspring;
	}
	
	
	/*
	 * Subgraph recombination determines the active paths of both parents and randomly selects subgraphs of active function nodes
	 * from the phenotype. The selected subgraphs are than recombined to produce an offspring. 
	 * 
	 * References: 
	 * Kalkreuth et al.(2017) http://dx.doi.org/10.1007/978-3-319-55696-3_19
	 * Kalkreuth (2020) http://dx.doi.org/10.5220/0010110700590070
	 * Kalkreuth (2021)  http://dx.doi.org/10.5220/0010110700590070
	 */
	public void subgraphCrossover(int c1, int c2, ArrayList<Integer> activeFunctionNodes1,
			ArrayList<Integer> activFunctioneNodes2, ArrayList<Integer> legalNodes, int[] genome1, int[] genome2,
			int[] offspring, EvolutionState state, int thread) {

		IntegerVectorSpecies s = (IntegerVectorSpecies) species;
		MersenneTwisterFast rand = state.random[thread];

		int c1Node = activeFunctionNodes1.get(c1);
		int c2Node = activFunctioneNodes2.get(c2);
		int c1Pos = ((activeFunctionNodes1.get(c1) + 1) - s.numInputs) * (s.maxArity + 1);
		int c2Pos = ((activFunctioneNodes2.get(c2) + 1) - s.numInputs) * (s.maxArity + 1);
		
		
		// A general crossover point is defined by choosing the smaller crossover
		// point. The reason for this is that the subgraphs of the parents which will
		// be placed in front of or behind the crossover point of the offspring genome should be balanced. 
		// The representation of CGP allows active paths of an individual which can start in the middle
		// or back of the graph. The subgraph which will be placed in front of the crossover point has
		// to start at more leading active nodes. 
		if (c1Node <= c2Node) {

			createLegalNodes(legalNodes, activeFunctionNodes1, activFunctioneNodes2, c1Node);
			mergeParts(legalNodes, activeFunctionNodes1, activFunctioneNodes2, c1Pos, c1, genome1, genome2, offspring,
					rand);
		} else {

			createLegalNodes(legalNodes, activFunctioneNodes2, activeFunctionNodes1, c2Node);
			mergeParts(legalNodes, activFunctioneNodes2, activeFunctionNodes1, c2Pos, c2, genome2, genome1, offspring,
					rand);
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
	
	
	/*
	 * Insertion mutation activates exactly one inactive function node by rewiring connection genes 
	 * in the genotype. 
	 * 
	 * References: Kalkreuth (2019) http://dx.doi.org/10.5220/0008070100820092
	 * Kalkreuth (2021) http://dx.doi.org/10.1007/978-3-030-70594-7_4
	 */
	public void insertion(EvolutionState state, int thread, int[] genome) {
		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		MersenneTwisterFast rand = state.random[thread];

		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);

		// Discard the mutation if the number of active nodes is higher or equal than the predefined upper
		// limit of active nodes
		if (activeFunctionNodes.size() >= s.maxNodeInsertion) {
			return;
		}

		passiveFunctionNodes.clear();
		
		
		// Determine the set of passive nodes 
		for (int i = (s.numInputs); i <= (s.numNodes + s.numInputs - 1); i++) {
			if (!activeFunctionNodes.contains(i)) {
				passiveFunctionNodes.add(i);
			}
		}

		// If all nodes are already active, discard the mutation
		if (passiveFunctionNodes.size() == 0)
			return;

		// Chose a passive node by chance
		int j = rand.nextInt(passiveFunctionNodes.size());

		// Determine the node number and position
		int mNodeNumber = passiveFunctionNodes.get(j);
		int mNodePosition = s.positionFromNodeNumber(mNodeNumber);
		
	
		boolean hasLeftNode = false;
		boolean hasRightNode = false;

		int rightNodeNumber = -1;
		int rightNodePosition = -1;

		Collections.sort(activeFunctionNodes);
		
		// Check if the selected passive nodes has neighbor nodes
		if (activeFunctionNodes.size() > 0) {
			if (mNodeNumber > activeFunctionNodes.get(0))
				hasLeftNode = true;

			if (mNodeNumber < (activeFunctionNodes.get(activeFunctionNodes.size() - 1)))
				hasRightNode = true;
		}

		// Case distinction for neighbors of the selected passive node
		
		if (hasRightNode) {
			// Get the node number of the neighbor to the right 
			int i = 0;
			int currentNode = activeFunctionNodes.get(i);
			while (currentNode < mNodeNumber) {
				currentNode = activeFunctionNodes.get(i);
				i++;
			}

			rightNodeNumber = currentNode;
			rightNodePosition = s.positionFromNodeNumber(rightNodeNumber);

		}

		if (hasRightNode) {
			int currentPosition;
			// Overtake the connection genes of the following neighbor node
			for (int i = 1; i <= s.maxArity; i++) {
				currentPosition = mNodePosition + i;
				genome[currentPosition] = genome[rightNodePosition + i];
			}
			
			// Connect the neighbor with the selected passive node, the node now becomes active 
			int randomInputNumber = rand.nextInt(s.maxArity);
			genome[rightNodePosition + randomInputNumber + 1] = mNodeNumber;

		} else if (hasLeftNode) {
			int leftNodeNumber = activeFunctionNodes.get(activeFunctionNodes.size() - 1);
			
			// Adjust at least one output if the previous active node is directly connected 
			// with the output 
			for (int i = 1; i <= s.numOutputs; i++) {
				if (genome[genome.length - i] == leftNodeNumber) {
					genome[genome.length - i] = mNodeNumber;
					break;
				}
			}

			int currentPosition;


			for (int i = 1; i <= s.maxArity; i++) {
				currentPosition = mNodePosition + i;

				if (i == 1) {
					// Adjust one connection gene to the previous function node 
					genome[currentPosition] = leftNodeNumber;
				} else {
					// Adjust the remaining genes to inputs or previous active function nodes
					if (rand.nextBoolean()) {
						genome[currentPosition] = activeFunctionNodes.get(rand.nextInt(activeFunctionNodes.size()));
					} else {
						genome[currentPosition] = rand.nextInt(s.numInputs);
					}
				}

			}
		// If the selected passive node has no neighbors connect the output to it 
		} else {
			int output = (rand.nextInt(s.numOutputs)) + 1;
			genome[genome.length - output] = mNodeNumber;
			
			// Adjust the inputs of the node to the inputs
			int currentPosition;
			for (int i = 1; i <= s.maxArity; i++) {
				currentPosition = mNodePosition + i;
				genome[currentPosition] = rand.nextInt(s.numInputs);
			}
		}

		passiveFunctionNodes.remove(j);
		activeFunctionNodes.add(mNodeNumber);

	}

	/*
	 * Deletion mutation deactivates the first active function node 
	 * 
	 * References: 
	 * Kalkreuth (2019) http://dx.doi.org/10.5220/0008070100820092
	 * Kalkreuth (2021) http://dx.doi.org/10.1007/978-3-030-70594-7_4
	 */
	public void deletion(EvolutionState state, int thread, int[] genome) {

		AdvancedIntegerVectorSpecies s = (AdvancedIntegerVectorSpecies) species;
		MersenneTwisterFast rand = state.random[thread];

		s.determineActiveFunctionNodes(activeFunctionNodes, s, genome);

		// Discard the mutation if the number of active nodes is lower or equal than the predefined lower 
		// limit of active function nodes
		if (activeFunctionNodes.size() <= s.minNodeDeletion) {
			return;
		}

		Collections.sort(activeFunctionNodes);
		
		// First active function node is going to be deactivated
		int mNode = activeFunctionNodes.get(0);
		activeFunctionNodes.remove(0);

		int currentNode;
		int currentIndex;
		int randIndex;

		// Reset all connections that refer to the first function node by chance
		// Distinguish between connection and output genes
		for (int i = 0; i < genome.length; i++) {
			if (s.phenotype(i, genome) == s.GENE_ARGUMENT) {
				currentNode = s.nodeNumber(i, genome);
				if (activeFunctionNodes.contains(currentNode) && genome[i] == mNode) {
					// Binary decision by chance if a function node is connected to
					// an input or active function node
					if (rand.nextBoolean()) {
						currentIndex = activeFunctionNodes.indexOf(currentNode);
						// The new first active function node will be randomly connected to an input
						if (currentIndex == 0) {
							genome[i] = rand.nextInt(s.numInputs);
						} else {
						// Otherwise, connect the node to a previous active function node
							randIndex = rand.nextInt(currentIndex);
							genome[i] = activeFunctionNodes.get(randIndex);
						}
					
					} else {
						genome[i] = rand.nextInt(s.numInputs);
					}
				}
			}

			// The outputs are reconnected in a similar way with a binary decision by chance 
			// if an output is connected to an input or active function node
			if (s.phenotype(i, genome) == s.GENE_OUTPUT) {
				if (genome[i] == mNode) {
					if (rand.nextBoolean()) {
						randIndex = rand.nextInt(activeFunctionNodes.size());
						genome[i] = activeFunctionNodes.get(randIndex);
					} else {
						genome[i] = rand.nextInt(s.numInputs);
					}
				}
			}

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
		
		if(s.insertionProbability > 0.0f) {
			insertion(state, thread, genome);
		}
		
		if(s.deletionProbability > 0.0f) {
			deletion(state, thread, genome);	
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
