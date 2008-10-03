package ec.cgp.representation;

import ec.vector.VectorIndividual;


/**
 * Base class for integer- and float-based CGP individuals.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public abstract class VectorIndividualCGP extends VectorIndividual {

	/** Temporary storage for displaying the full program */
	public StringBuffer expression;

	/** Return the genome. */
	public abstract Object getGenome();

}
