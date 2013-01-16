/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.tutorial2;
import ec.vector.*;
import ec.*;
import ec.util.*;

/*
 * OurMutatorPipeline.java
 */

/**
   OurMutatorPipeline is a BreedingPipeline which negates the sign of genes.
   The individuals must be IntegerVectorIndividuals.  Because we're lazy,
   we'll use the individual's species' mutation-probability parameter to tell
   us whether or not to mutate a given gene.
 
   <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
   (however many its source produces)

   <p><b>Number of Sources</b><br>
   1
*/


public class OurMutatorPipeline extends BreedingPipeline
    {
    public static final String P_OURMUTATION = "our-mutation";

    // We have to specify a default base, even though we never use it 
    public Parameter defaultBase() { return VectorDefaults.base().push(P_OURMUTATION); }
    
    public static final int NUM_SOURCES = 1;

    // Return 1 -- we only use one source
    public int numSources() { return NUM_SOURCES; }

    // We're supposed to create a most _max_ and at least _min_ individuals,
    // drawn from our source and mutated, and stick them into slots in inds[]
    // starting with the slot inds[start].  Let's do this by telling our 
    // source to stick those individuals into inds[] and then mutating them
    // right there.
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 
        {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        // clone the individuals if necessary -- if our source is a BreedingPipeline
        // they've already been cloned, but if the source is a SelectionMethod, the
        // individuals are actual individuals from the previous population
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // Check to make sure that the individuals are IntegerVectorIndividuals and
        // grab their species.  For efficiency's sake, we assume that all the 
        // individuals in inds[] are the same type of individual and that they all
        // share the same common species -- this is a safe assumption because they're 
        // all breeding from the same subpopulation.

        if (!(inds[start] instanceof IntegerVectorIndividual)) // uh oh, wrong kind of individual
            state.output.fatal("OurMutatorPipeline didn't get an IntegerVectorIndividual." +
                "The offending individual is in subpopulation " + subpopulation + " and it's:" + inds[start]);
        IntegerVectorSpecies species = (IntegerVectorSpecies)(inds[start].species);

        // mutate 'em!
        for(int q=start;q<n+start;q++)
            {
            IntegerVectorIndividual i = (IntegerVectorIndividual)inds[q];
            for(int x=0;x<i.genome.length;x++)
                if (state.random[thread].nextBoolean(species.mutationProbability(x)))
                    i.genome[x] = -i.genome[x];
            // it's a "new" individual, so it's no longer been evaluated
            i.evaluated=false;
            }

        return n;
        }

    }
    
    
