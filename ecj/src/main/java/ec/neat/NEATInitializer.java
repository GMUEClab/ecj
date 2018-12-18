/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.util.*;
import ec.*;
import ec.simple.*;

/**
 * NEATInitializer is a SimpleInitializer which ensures that the subpopulations
 * are all create from an existing template individual read from file.
 *
 * @author Ermo Wei and David Freelan
 */

public class NEATInitializer extends SimpleInitializer
    {
    private static final long serialVersionUID = 1;

    /**
     * In NEAT, we provide the algorithm with a start individual from file,
     * after read the start individual from file, we populate the subpopulation with
     * mutated version of that template individual. The number of individual we create is
     * determined by the "pop.subpop.X.size" parameter.
     */
    public Population initialPopulation(EvolutionState state, int thread)
        {
        // read in the start genome as the template
        Population p = setupPopulation(state, thread);
        p.populate(state, thread);

        // go through all the population and populate the NEAT subpop
        for (int i = 0; i < p.subpops.size(); i++)
            {
            // NEAT uses a template to populate the population
            // we first read it in to form the population, then mutate the links
            if (p.subpops.get(i).species instanceof NEATSpecies)
                {
                NEATSpecies species = (NEATSpecies) p.subpops.get(i).species;

                ArrayList<Individual> inds = p.subpops.get(i).individuals;
                // get the template
                NEATIndividual templateInd = (NEATIndividual) inds.get(0);
                // clear the individuals
                inds.clear();

                // spawn the individuals with template
                int initialSize = p.subpops.get(i).initialSize;
                for (int j = 0; j < initialSize; ++j)
                    {
                    NEATIndividual newInd = species.spawnWithTemplate(state, species, thread, templateInd);
                    inds.add(newInd);
                    }

                // state.output.warnOnce("Template genome found, populate the subpopulation with template individual");
                // templateInd.printIndividual(state, 0);

                // set the next available innovation number and node id
                species.setInnovationNumber(templateInd.getGeneInnovationNumberSup());
                species.currNodeId = templateInd.getNodeIdSup();

                // speciate
                for (int j = 0; j < inds.size(); ++j)
                    {
                    species.speciate(state, inds.get(j));
                    }

                // switch to the new generation
                for (int j = 0; j < species.subspecies.size(); ++j)
                    {
                    species.subspecies.get(j).toNewGeneration();
                    }

                }
            }

        return p;
        }

    }
