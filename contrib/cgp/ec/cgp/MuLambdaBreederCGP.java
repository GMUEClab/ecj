package ec.cgp;


import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.cgp.representation.VectorIndividualCGP;
import ec.es.MuPlusLambdaBreeder;

/**
 * The CGP implementation requires this slightly modified MuPlusLambdaBreeder
 * when using Evolutionary Strategies. Its sole purpose is to reset string
 * representations of the expressions represented by all genomes in the
 * population. Resetting forces re-computation of each expression during
 * evaluation of CGP nodes, but only for the first such evaluation.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class MuLambdaBreederCGP extends MuPlusLambdaBreeder {

	@Override
	public Population breedPopulation(EvolutionState state)
            {
                for (final Subpopulation subpop : state.population.subpops)
                    for (final Individual ind : subpop.individuals)
                        ((VectorIndividualCGP)ind).expression = null;
		return super.breedPopulation(state);
            }

}
