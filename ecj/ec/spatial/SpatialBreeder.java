/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.spatial;

import ec.Initializer;
import ec.Individual;
import ec.BreedingPipeline;
import ec.Breeder;
import ec.simple.*;
import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
import ec.util.*;

/* 
 * SpatialBreeder.java
 * 
 * By: Liviu Panait
 */

/**
 * A slight modification of the simple breeder for spatially-embedded EAs.
 
 * Breeds each subpopulation separately, with no inter-population exchange,
 * and using a generational approach.  A SpatialBreeder may have multiple
 * threads; it divvys up a subpopulation into chunks and hands one chunk
 * to each thread to populate.  One array of BreedingPipelines is obtained
 * from a population's Species for each operating breeding thread.
 *
 *
 *
 *
 * @author Liviu Panait
 * @version 1.0 
 */

public class SpatialBreeder extends SimpleBreeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
                
        // check for elitism and warn about it
        for(int i = 0 ; i < elite.length; i++)   // we use elite.length here instead of pop.subpops.length because the population hasn't been made yet.
            if (usingElitism(i))
                {
                state.output.warning("You're using elitism with SpatialBreeder.  This is unwise as elitism is done by moving individuals around in the population, thus messing up the spatial nature of breeding.",
                    base.push(P_ELITE).push(""+i));
                break;
                }

        if (sequentialBreeding) // uh oh, untested
            state.output.warning("SpationBreeder hasn't been well tested with sequential evaluation, though it should probably work fine.  You're on your own.",
                base.push(P_SEQUENTIAL_BREEDING));

        if (!clonePipelineAndPopulation)
            state.output.fatal("clonePipelineAndPopulation must be true for SpatialBreeder.");
        }
                
    protected void breedPopChunk(Population newpop, EvolutionState state,
        int[] numinds, int[] from, int threadnum) 
        {
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            // if it's subpop's turn and we're doing sequential breeding...
            if (!shouldBreedSubpop(state, subpop, threadnum))  
                {
                // instead of breeding, we should just copy forward this subpopulation.  We'll copy the part we're assigned
                for(int ind=from[subpop] ; ind < numinds[subpop] - from[subpop]; ind++)
                    newpop.subpops[subpop].individuals[ind] = (Individual)(state.population.subpops[subpop].individuals[ind].clone());
                }
            else
                {
                BreedingPipeline bp = (BreedingPipeline)newpop.subpops[subpop].
                    species.pipe_prototype.clone();
                                                                        
                if (!(state.population.subpops[subpop] instanceof Space))
                    state.output.fatal("Subpopulation " + subpop + " does not implement the Space interface.");
                Space space = (Space)(state.population.subpops[subpop]);
                                                        
                // check to make sure that the breeding pipeline produces
                // the right kind of individuals.  Don't want a mistake there! :-)
                if (!bp.produces(state,newpop,subpop,threadnum))
                    state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
                bp.prepareToProduce(state,subpop,threadnum);
                                                        
                // start breedin'!
                for(int x = from[subpop]; x < from[subpop] + numinds[subpop]; x++)
                    {
                    space.setIndex(threadnum, x);
                    if (bp.produce(1, 1, x, subpop, newpop.subpops[subpop].individuals, state, threadnum) != 1)
                        state.output.fatal( "The pipelines should produce one individual at a time!" );
                    }
                                                                        
                bp.finishProducing(state,subpop,threadnum);
                }
            }
        }

    }


