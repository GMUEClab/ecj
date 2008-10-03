package ec.cgp.representation;


import ec.EvolutionState;
import ec.util.Parameter;

/**
 * Float-based genome representation of a Cartesian Genetic Program. Gene values
 * are restricted to floats in the range [0,1]. During program evaluation, each
 * float value is scaled to integers in the acceptable range that is imposed by
 * the gene's position.
 *
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 *
 */
public class FloatVectorSpecies extends VectorSpeciesCGP {

	/** Added setup step that automatically sets max-gene to 1.0. */
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"), "1"); 		
	}
	
}
