package ec.eda.dovs;

import java.util.ArrayList;

import ec.*;
import ec.simple.*;
import ec.vector.*;

/**
 * DOVSInitializer is a SimpleInitializer which ensures that the subpopulations
 * are create from an existing individual read from file. This individual will
 * be serve as the start search point for our algorithm.
 *
 * @author Ermo Wei and David Freelan
 */
public class DOVSInitializer extends SimpleInitializer
    {
    private static final long serialVersionUID = 1;

    /**
     * In DOVS, we provide the algorithm with a start individual from file, this
     * start individual is the start search point of the DOVS algorithm. We use
     * this start point to construct a hyperbox contains promising solutions,
     * and sample from this region, the number of sample is equal to parameter
     * "pop.subpop.X.size" in parameter files.
     * 
     * However, due to redundant samples, we the final individuals size may be
     * smaller than what have been specified in pop.subpop.X.size.
     */
    public Population initialPopulation(final EvolutionState state, int thread)
        {
        Population p = super.initialPopulation(state, thread);
        // make sure the each subpop only have one individual
        for (int i = 0; i < p.subpops.size(); i++)
            {
            if (p.subpops.get(i).species instanceof DOVSSpecies)
                {
                DOVSSpecies species = (DOVSSpecies) p.subpops.get(i).species;

                if (p.subpops.get(i).individuals.size() != 1)
                    state.output.fatal("contain more than one start point");

                // add the start point to the visited ArrayList
                species.visited.clear();
                species.visited.add(p.subpops.get(i).individuals.get(0));
                species.visitedIndexMap.put(p.subpops.get(i).individuals.get(0), 0);
                species.optimalIndex = 0;

                IntegerVectorIndividual ind = (IntegerVectorIndividual) species.visited.get(species.optimalIndex);
                // For the visited solution, record its coordinate
                // positions in the multimap
                for (int j = 0; j < species.genomeSize; ++j)
                    {
                    // The individual is the content. The key is its
                    // coordinate position
                    species.corners.get(j).insert(ind.genome[j], ind);
                    }

                // update MPA
                species.updateMostPromisingArea(state);

                // sample from MPA
                int initialSize = p.subpops.get(i).initialSize;
                ArrayList<Individual> candidates = species.mostPromisingAreaSamples(state, initialSize);

                // get unique candidates for evaluation, this is Sk in paper
                ArrayList<Individual> uniqueCandidates = species.uniqueSamples(state, candidates);

                // update the individuals
                p.subpops.get(i).individuals = uniqueCandidates;

                }

            }
        return p;
        }

    }
