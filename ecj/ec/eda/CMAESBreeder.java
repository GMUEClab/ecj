/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda;

import ec.*;
import ec.util.*;

/* 
 * CMAESBreeder.java
 * 
 * Created: Wed Jul  8 12:35:31 EDT 2015
 * By: Sam McKay and Sean Luke
 */

/**
 * A Breeder is a singleton object which is responsible for the breeding
 * process during the course of an evolutionary run.  Only one Breeder
 * is created in a run, and is stored in the EvolutionState object.
 *
 * <p>Breeders typically do their work by applying a Species' BreedingPipelines
 * on subpopulations of that species to produce new individuals for those
 * subpopulations.
 *
 * <p>Breeders may be multithreaded.  The number of threads they may spawn
 * (excepting a parent "gathering" thread) is governed by the EvolutionState's
 * breedthreads value.
 *
 * <p>Be careful about spawning threads -- this system has no few synchronized 
 * methods for efficiency's sake, so you must either divvy up breeding in a
 * thread-safe fashion and assume that all individuals
 * in the current population are read-only (which you may assume for a generational
 * breeder which needs to return a whole new population each generation), or
 * otherwise you must obtain the appropriate locks on individuals in the population
 * and other objects as necessary.
 *
 * @author Sam McKay and Sean Luke
 * @version 1.0 
 */

public class CMAESBreeder extends Breeder
    {
    public void setup(final EvolutionState state, final Parameter base)
        {
		// set myself up if necessary here

        }

    /** Breeds state.population, returning a new population.  In general,
        state.population should not be modified. */

    public Population breedPopulation(final EvolutionState state)
    	{
    	Population pop = state.population;
    	for(int i = 0; i < pop.subpops.length; i++)
    		{
    		Subpopulation subpop = pop.subpops[i];
    		if (!(subpop.species instanceof CMAESSpecies))  // uh oh
    			state.output.fatal("To use CMAESBreeder, subpopulation " + i + " must contain a CMAESSpecies.  But it contains a " + subpop.species);
    			
    		CMAESSpecies species = (CMAESSpecies)(subpop.species);
    		
    		// update distribution[i] for subpop
    		
    		
    		
    		
    		// overwrite individuals
    		Individual[] inds = subpop.individuals;
    		for(int j = 0; j < inds.length; j++)
    			inds[j] = species.newIndividual(state, 0);
    		}
    		
    	return pop;
    	}
    }
