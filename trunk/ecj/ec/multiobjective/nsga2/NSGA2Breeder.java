/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga2;

import ec.*;
import ec.util.*;
import ec.simple.*;

/* 
 * NSGA2Breeder.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange, and
 * using the NSGA2 approach. A NSGA2Breeder may have multiple threads; it divvys
 * up a subpopulation into chunks and hands one chunk to each thread to
 * populate. One array of BreedingPipelines is obtained from a population's
 * Species for each operating breeding thread.
 * <p>
 * The NSGA2 approach: After breeding a new subpopulation of size N from N
 * parents, NSGA2Breeder appends the children to the parents giving a
 * subpopulation of size 2N in a somewhat mu+lambda fashion.
 * 
 * @author Faisal Abidi (based on Breeder.java by Sean Luke)
 * @version 1.0
 */

public class NSGA2Breeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        // make sure SimpleBreeder's elites facility isn't being used
        for (int i = 0; i < elite.length; i++)
            if (elite[i] != 0)
                state.output.fatal("Elites may not be used with NSGA2Breeder");
        }

    /**
     * Override breedPopulation(). We take the result from the super method in
     * SimpleBreeder and append it to the old population. Hence, after
     * generation 0, every subsequent call to
     * <code>NSGA2Evaluator.evaluatePopulation()</code> will be passed a
     * population of 2x<code>originalPopSize</code> individuals.
     */
    public Population breedPopulation(EvolutionState state)
        {
        Population oldPop = (Population) state.population;
        Population newPop = super.breedPopulation(state);
        Individual[] combinedInds;
        Subpopulation[] subpops = oldPop.subpops;
        Subpopulation oldSubpop;
        Subpopulation newSubpop;
        int subpopsLength = subpops.length;

        for (int i = 0; i < subpopsLength; i++)
            {
            oldSubpop = oldPop.subpops[i];
            newSubpop = newPop.subpops[i];
            combinedInds = new Individual[oldSubpop.individuals.length
                + newSubpop.individuals.length];
            System.arraycopy(newSubpop.individuals, i, combinedInds, i,
                newSubpop.individuals.length);
            System.arraycopy(oldSubpop.individuals, i, combinedInds,
                newSubpop.individuals.length, oldSubpop.individuals.length);
            newSubpop.individuals = combinedInds;
            }
        return newPop;
        }
    }
