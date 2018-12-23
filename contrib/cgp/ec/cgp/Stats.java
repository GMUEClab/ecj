package ec.cgp;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Output;

/**
 * 
 * Custom statistics for Cartesian Genetic Programming. Displays detailed
 * information about any new individual found that exceeds the currently
 * tracked best-of-run individual.
 * 
 * 
 * @author David Oranchak, doranchak@gmail.com, http://oranchak.com
 * 
 */
public class Stats extends SimpleStatistics {

	public void postEvaluationStatistics(EvolutionState state) {
		final Individual[] indBefore = new Individual[state.population.subpops.size()];
		boolean newBest = false;

		if (best_of_run == null)
			newBest = true;

		for (int i = 0; i < indBefore.length; i++) {
			if (best_of_run[i] == null)
				newBest = true;
			else
				indBefore[i] = (Individual) best_of_run[i].clone();
		}
		super.postEvaluationStatistics(state);

		for (int i = 0; i < indBefore.length; i++) {
			if (newBest
					|| (best_of_run[i].fitness.betterThan(indBefore[i].fitness) && !best_of_run[i].fitness
							.equivalentTo(indBefore[i].fitness))) {
				String msg = " NEW BEST: Subpop " + i + ", generation "
						+ state.generation + ", individual: "
						+ best_of_run[i].genotypeToStringForHumans() + ", "
						+ best_of_run[i].fitness.fitnessToStringForHumans();
				state.output.println(msg, Output.V_NO_GENERAL, statisticslog);
				state.output.message(msg);
			}
		}

		/*
		 * Compute mean fitness for entire population. TODO: generalize to
		 * multiple subpops
		 */
		float total = 0;
                for (final Individual ind : state.population.subpops.get(0).individuals)
			total += ind.fitness.fitness();
		String msg = "Mean fitness at generation " + state.generation + ": "
				+ (total / state.population.subpops.get(0).individuals.size());
		state.output.println(msg, Output.V_NO_GENERAL, statisticslog);
		state.output.message(msg);
	}

}
