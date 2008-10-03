package ec.cgp;


import ec.EvolutionState;
import ec.Population;
import ec.cgp.representation.VectorIndividualCGP;
import ec.simple.SimpleBreeder;

/**
 * The CGP implementation requires this slightly modified SimpleBreeder. Its
 * sole purpose is to reset string representations of the expressions
 * represented by all genomes in the population. Resetting forces re-computation
 * of each expression during evaluation of CGP nodes, but only for the first
 * such evaluation.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class Breeder extends SimpleBreeder {

	/**
	 * Reset the expressions that were computed and stored in the previous
	 * generation.
	 */
	public Population breedPopulation(EvolutionState state) {
		for (int x = 0; x < state.population.subpops.length; x++)
			for (int y = 0; y < state.population.subpops[x].individuals.length; y++)
				((VectorIndividualCGP) state.population.subpops[x].individuals[y]).expression = null;

		return super.breedPopulation(state);
	}

}
