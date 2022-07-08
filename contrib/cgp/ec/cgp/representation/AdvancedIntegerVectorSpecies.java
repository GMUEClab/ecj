package ec.cgp.representation;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * @author Roman Kalkreuth, roman.kalkreuth@tu-dortmund.de,
 *         https://orcid.org/0000-0003-1449-5131,
 *         https://ls11-www.cs.tu-dortmund.de/staff/kalkreuth,
 *         https://twitter.com/RomanKalkreuth
 */
public class AdvancedIntegerVectorSpecies extends IntegerVectorSpecies {
	public static final String P_INSERTION_PROB = "insertion-prob";
	public static final String P_DELETION_PROB = "deletion-prob";
	public static final String P_MUTATION_RATE = "mutation-rate";
	public static final String P_MAX_INSERTION = "max-node-insertion";
	public static final String P_MIN_DELETION = "min-node-deletion";
	public static final String P_INVERSION_PROB = "inversion-prob";
	public static final String P_MAX_INVERSION_DEPTH = "max-inversion-depth";
	public static final String P_DUPLICATION_PROB = "duplication-prob";
	public static final String P_MAX_DUPLICATION_DEPTH = "max-duplication-depth";
	public static final String P_CNN = "connect-neighbour-node";
	public static final String P_MUTATE_ACTIVE_GENES = "num-active-genes";
	public static final String P_CONNECT_NEIGHBOUR_NODE = "connect-neighbour-node";
	
	public final static String P_MUTATIONTYPE = "mutation-type";
	
    public final static String V_SINGLE = "single";
    public final static String V_MULTI = "multi";
    public final static String V_POINT = "point";
    
    public final static int C_SINGLE = 0;
    public final static int C_MULTI = 1;
    public final static int C_POINT = 2;

	
	public float insertionProbability;
	public int maxNodeInsertion;
	
	public float deletionProbability;
	public int minNodeDeletion;
	
	public float inversionProbability;
	public int maxInversionDepth;
	
	public float duplicationProbability;
	public int maxDuplicationDepth;
	
	public boolean connectNeighborNode;
	public int mutateActiveGenes;
	
	protected int mutationType;


	/**
	 * Decodes the genome on an CGP individual and adds the number of active nodes to the list. 
	 */
	public int decode(int[] genome, int output, ArrayList<Integer> activeNodes, VectorSpeciesCGP species,
			int nodeNum, int numActiveNodes) {

		if (nodeNum > (species.numInputs - 1)) {
			numActiveNodes++;
			if (!activeNodes.contains(nodeNum)) {
				activeNodes.add(nodeNum);
			}

			int index = (nodeNum - species.numInputs) * (species.maxArity + 1);

			for (int i = 1; i <= species.maxArity; i++) {
				decode(genome, output, activeNodes, species, genome[index + i], numActiveNodes);
			}
		}
		return numActiveNodes;
	}

	/**
	 * Determines the active function nodes on an CGP individual.
	 */
	public int determineActiveFunctionNodes(ArrayList<Integer> activeFunctionNodes, VectorSpeciesCGP species, int[] genome) {

		int output;
		int nodeNum;
		int numActiveNodes = 0;

		activeFunctionNodes.clear();
		
		for (int i = 0; i < species.numOutputs; i++) {
			output = i + 1;
			nodeNum = genome[genome.length - species.numOutputs + i];
			decode(genome, output, activeFunctionNodes, species, nodeNum, numActiveNodes);
		}
		return activeFunctionNodes.size();
	}
	
	/**
	 * Determines the active function nodes on an CGP individual.
	 * 
	 * TODO: Revise implementation to achieve more efficiency
	 */
	public void determineActiveGenes(ArrayList<Integer> activeNodes, ArrayList<Integer> activeGenes, VectorSpeciesCGP species) {
		int genePos;
	
		for (Integer node :  activeNodes) { 
			genePos =  species.positionFromNodeNumber(node);
			
			for (int i=0; i < species.maxArity;i++) {
				activeGenes.add(genePos+i);
			}
			
		}
	}
	

	public void setup(EvolutionState state, Parameter base) {
		
		super.setup(state, base);
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"), "10000000"); 
			/* arbitrarily large.  but computeMaxGene will usually limit gene to contain much smaller values. */	
		
		Parameter def = defaultBase();
		
		// MUTATION PARAMETERS
		
		insertionProbability = state.parameters.getFloatWithDefault(base.push(P_INSERTION_PROB), def
				.push(P_INSERTION_PROB), 0.0f);
		
		deletionProbability =state.parameters.getFloatWithDefault(base.push(P_DELETION_PROB), def
				.push(P_DELETION_PROB), 0.0f);

		maxNodeInsertion = state.parameters.getIntWithDefault(base.push(P_MAX_INSERTION), def
				.push(P_MAX_INSERTION), 10);
		
		minNodeDeletion = state.parameters.getIntWithDefault(base.push(P_MIN_DELETION), def
				.push(P_MIN_DELETION), 2);
		
		inversionProbability = state.parameters.getFloatWithDefault(base.push(P_INVERSION_PROB), def
				.push(P_INVERSION_PROB), 0.0f);
		
		maxInversionDepth = state.parameters.getIntWithDefault(base.push(P_MAX_INVERSION_DEPTH), def
				.push(P_MAX_INVERSION_DEPTH), 1);
		
		duplicationProbability = state.parameters.getFloatWithDefault(base.push(P_DUPLICATION_PROB), def
				.push(P_DUPLICATION_PROB), 0.0f);
		
		maxDuplicationDepth = state.parameters.getIntWithDefault(base.push(P_MAX_DUPLICATION_DEPTH), def
				.push(P_MAX_DUPLICATION_DEPTH), 1);
		
		mutateActiveGenes = state.parameters.getIntWithDefault(base.push(P_MUTATE_ACTIVE_GENES), def
				.push(P_MUTATE_ACTIVE_GENES), 1);
		
		connectNeighborNode = state.parameters.getBoolean(base.push(P_CONNECT_NEIGHBOUR_NODE ), def
				.push(P_CONNECT_NEIGHBOUR_NODE ), false);
		
		
		
	     /// MUTATION TYPE

		String mtype = state.parameters.getString(base.push(P_MUTATIONTYPE), def
				.push(P_MUTATIONTYPE));
		
        if (mtype == null) { }  // we're cool
        else if (mtype.equalsIgnoreCase(V_POINT)) {
        	 mutationType = C_POINT; 
        }   
        else if (mtype.equalsIgnoreCase(V_SINGLE)){
             mutationType = C_SINGLE;    
        }
        else if (mtype.equalsIgnoreCase(V_MULTI)){
        	 mutationType = C_MULTI;    
        }
        else {
        	 state.output.error("IntegerVectorSpecies given a bad mutation type");
        }
        
        connectNeighborNode =state.parameters.getBoolean(base.push(P_CNN),def.push(P_CNN), false);
	}

}
