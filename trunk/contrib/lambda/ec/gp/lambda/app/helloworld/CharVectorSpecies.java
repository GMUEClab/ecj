/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.helloworld;

import java.io.Serializable;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.VectorSpecies;

public class CharVectorSpecies extends VectorSpecies implements Serializable{
	private static final long serialVersionUID = 1;
	// the following two assumptions might be wrong
	int intOfA = 65;
	int intOfZ = 90;
	//this actually may be not used anywhere
	double mutationProb = 0.05; // out of 1.0

	public final static String P_CLOSE_WALK_PROBABILITY = "close-walk-probability";

	public boolean inCharTypeRange(char geneVal) {
		int intOfGene = (int) geneVal;
		return (intOfGene >= intOfA && intOfGene <= intOfZ);
	}

	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = defaultBase();
		setupGenome(state, base);
//		{//this part is not used right now. the param P_CLOSE_WALK_PROBABILITY is not specified in param file and will introduce error if uncommented.
//		double _probability = state.parameters.getDoubleWithMax(
//				base.push(P_CLOSE_WALK_PROBABILITY),
//				def.push(P_CLOSE_WALK_PROBABILITY), 0.0, 1.0);
//		if (_probability <= 0)
//			state.output
//					.fatal("If it's going to use random walk mutation as its global mutation type, IntegerVectorSpecies must a random walk mutation probability between 0.0 and 1.0.",
//							base.push(P_CLOSE_WALK_PROBABILITY),
//							def.push(P_CLOSE_WALK_PROBABILITY));
//		mutationProb = _probability;
//		}
		super.setup(state, base);
	}
    protected void loadParametersForGene(EvolutionState state, int index, Parameter base, Parameter def, String postfix){
    	super.loadParametersForGene(state, index, base, def, postfix);
    }
    
    public double getMutationProbability(int x){
    	return mutationProbability[x];
    }
}
