package ec.cgp;

import ec.EvolutionState;
import ec.Individual;
import ec.es.ESSelection;
import ec.es.MuCommaLambdaBreeder;

import java.util.List;

/**
 * CGP benefits greatly from 'neutrality', the idea that genetic drift yields to
 * diverse individuals having equal fitness. This hacked ESSelection ensures
 * equally-fit individuals are uniformly considered during ES selection.
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class ESSelectionCGP extends ESSelection {
	public int produce(final int min, final int max, final int start,
			final int subpopulation, final Individual[] inds,
			final EvolutionState state, final int thread) {
		if (min > 1) // uh oh
			state.output
					.fatal("ESSelection used, but it's being asked to produce more than one individual.");
		if (!(state.breeder instanceof MuCommaLambdaBreeder))
			state.output
					.fatal("ESSelection was handed a Breeder that's not either MuCommaLambdaBreeder or MuCommaPlusLambdaBreeder.");
		MuCommaLambdaBreeder breeder = (MuCommaLambdaBreeder) (state.breeder);

		// determine my position in the array
		int pos = (breeder.lambda[subpopulation] % state.breedthreads == 0 ? breeder.lambda[subpopulation]
				/ state.breedthreads
				: breeder.lambda[subpopulation] / state.breedthreads + 1)
				* thread + breeder.count[thread]; // note integer division

		// determine the parent
		int parent = pos
				/ (breeder.lambda[subpopulation] / breeder.mu[subpopulation]); // note
		// outer
		// integer
		// division

		List<Individual> inds2 = state.population.subpops.get(subpopulation).individuals;

		
		/*
		 * our slight modification to standard ESSelection: check for other
		 * individuals that have the same fitness as the normally selected
		 * parent. return one of these same-fitness individuals uniformly at
		 * random.
		 */
		if (pos == 0) { /* only do this for the first parent. */
			double fit = inds2.get(parent).fitness.fitness();
			int x = 0;
			while (fit == inds2.get(parent + x).fitness.fitness()
					&& parent + x < inds.length)
				x++;
			if (x > 0) {
				parent += state.random[thread].nextInt(x);
			}
		}

		// and so we return the parent
		if (pos == 0) {
			inds[start] = state.population.subpops.get(subpopulation).individuals.get(parent);
			/*
			 * swap them since we forget which parent we selected next time
			 * produce is called.
			 */
			Individual tmp = state.population.subpops.get(subpopulation).individuals.get(parent);
			state.population.subpops.get(subpopulation).individuals.set(parent, state.population.subpops.get(subpopulation).individuals.get(0));
			state.population.subpops.get(subpopulation).individuals.set(0, tmp);
		} else
			inds[start] = state.population.subpops.get(subpopulation).individuals.get(0);

		// and so we return the parent
		return 1;
	}

}
