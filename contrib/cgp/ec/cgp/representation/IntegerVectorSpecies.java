package ec.cgp.representation;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * Integer-based genome representation of a Cartesian Genetic Program. Each
 * integer value is restricted to a range that is a function of its position in
 * the genome.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class IntegerVectorSpecies extends VectorSpeciesCGP {

	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"), "10000000"); 
			/* arbitrarily large.  but computeMaxGene will usually limit gene to contain much smaller values. */		
	}

}
