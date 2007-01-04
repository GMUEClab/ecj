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

public class SpatialBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing -- just to implement the abstract requirement
        }
                
    public Population breedPopulation(EvolutionState state) 
        {

        Population newpop = (Population) state.population.emptyClone();
        
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            if( !( newpop.subpops[subpop] instanceof Space ) )
                state.output.fatal( "SpatialBreeder assumes all subpopulations implement the Space interface." );

            Space space = (Space)(state.population.subpops[subpop]);

            BreedingPipeline bp = (BreedingPipeline)newpop.subpops[subpop].
                species.pipe_prototype.clone();
                
            // check to make sure that the breeding pipeline produces
            // the right kind of individuals.  Don't want a mistake there! :-)
            if (!bp.produces(state,newpop,subpop,0))
                state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
            bp.prepareToProduce(state,subpop,0);
                
            for( int x = 0 ; x < newpop.subpops[subpop].individuals.length ; x++ )
                {
                space.setIndex(0,x);
                // We need to ask the children to be created one at a time!
                // This is because we need to set the index for each of them appropriately.
                if( bp.produce( 1,  // minimum individuals to be produced
                                1,  // maximum individuals to be produced
                                x,  // index where they should be stored
                                subpop, // the subpopulation the individuals belong to
                                newpop.subpops[subpop].individuals, // where to store the individuals
                                state,  // the evolution state
                                0)  // the thread number
                    != 1 )
                    state.output.fatal( "The pipelines should produce one individual at a time!" );
                }

            bp.finishProducing(state,subpop,0);
            }

        return newpop;

        }

    }


