/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/* 
 * MultipleVectorCrossoverPipeline.java
 * 
 * Created: Thu May 14 2009
 * By: Beenish Jamil
 */


/**
 *
 MultipleVectorCrossoverPipeline is a BreedingPipeline which implements a uniform
 (any point) crossover between multiple vectors. It is intended to be used with
 three or more vectors. It takes n parent individuals and returns n crossed over
 individuals. The number of parents and consequently children is specified by the
 number of sources parameter. 
 <p>The standard vector crossover probability is used for this crossover type. 
 <br> <i> Note</i> : It is necessary to set the crossover-type parameter to 'any' 
 in order to use this pipeline.
 
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 number of parents

 <p><b>Number of Sources</b><br>
 variable (generally 3 or more)

 
 <p><b>Default Base</b><br>
 vector.multixover
*/

// This class is MUCH MUCH longer than it need be.  We could just do it by using 
// ECJ's generic split and join operations, but only rely on that in the default
// case, and instead use faster per-array operations.


public class MultipleVectorCrossoverPipeline extends BreedingPipeline {

    /** default base */
    public static final String P_CROSSOVER = "multixover";
    
    /** Temporary holding place for parents */
    ArrayList<Individual> parents;
   
    public Parameter defaultBase() { return VectorDefaults.base().push(P_CROSSOVER); }    
    
    /** Returns the number of parents */
    public int numSources() { return DYNAMIC_SOURCES;}
    
    public Object clone()
        {
        MultipleVectorCrossoverPipeline c = (MultipleVectorCrossoverPipeline)(super.clone());

        // deep-cloned stuff
        c.parents = new ArrayList<Individual>(parents);
        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase(); 

        if (sources.length <= 2)  // uh oh
            state.output.fatal("num-sources must be provided and > 2 for MultipleVectorCrossoverPipeline",
                base.push(P_NUMSOURCES), def.push(P_NUMSOURCES));

        parents = new ArrayList<Individual>();
        }
        
    /**
     * Returns the minimum number of children that are produced per crossover 
     */
    public int typicalIndsProduced()
        {
        return minChildProduction()*sources.length; // minChild is always 1     
        }
    
        
    public int produce(final int min,
        final int max,
        final int subpopulation,
        final ArrayList<Individual> inds,
        final EvolutionState state,
        final int thread, HashMap<String, Object> misc)

        {
        int start = inds.size();
        
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            // just load from source 0
            sources[0].produce(n,n,subpopulation,inds, state,thread,misc);
            return n;
            }

        parents.clear();
        // fill up parents: 
        for(int i = 0; i< sources.length; i++)
            {
            // produce one parent from each source 
            sources[i].produce(1,1,subpopulation, parents, state,thread, misc);
            }

        // We assume all of the species are the same species ... 
        VectorSpecies species = (VectorSpecies)((VectorIndividual) parents.get(0)).species;

        // an array of the split points (width = 1)
        int[] points = new int[((VectorIndividual) parents.get(0)).genomeLength() - 1];
        for(int i = 0; i < points.length; i++)
            {
            points[i] = i + 1;    // first split point/index = 1
            }

        // split all the parents into object arrays 
        Object[][] pieces = new Object[parents.size()][((VectorIndividual) parents.get(0)).genomeLength()];
        
        // splitting...
        for(int i = 0; i < parents.size(); i++)
            {
            if(((VectorIndividual) parents.get(i)).genomeLength() != ((VectorIndividual) parents.get(0)).genomeLength())
                state.output.fatal("All vectors must be of the same length for crossover!");
            else
                ((VectorIndividual) parents.get(i)).split(points, pieces[i]);
            }


        // crossing them over now
        for(int i = 0; i < pieces[0].length; i++)
            {   
            if(state.random[thread].nextBoolean(species.crossoverProbability))
                {
                // shuffle
                for(int j = pieces.length-1; j > 0; j--) // no need to shuffle first index at the end
                    {
                    // find parent to swap piece with
                    int parent2 = state.random[thread].nextInt(j); // not inclusive; don't want to swap with self
                        
                    // swap
                    Object temp = pieces[j][i];
                    pieces[j][i] = pieces[parent2][i];
                    pieces[parent2][i] = temp;
                    }
                }
            }

        // join them and add them to the population starting at the start location
        for(int i = 0, q = start; i < parents.size(); i++, q++)
            { 
            ((VectorIndividual) parents.get(i)).join(pieces[i]);
            parents.get(i).evaluated = false;
            //            if(q<inds.size()) // just in case
            //                {               
            //                inds.set(q, (VectorIndividual) parents.get(i));
            //                }
            // by Ermo. The comment code seems to be wrong. inds are empty, which means indes.size() returns 0.
            // I think it should be changed to following code
            // Sean -- right?
            inds.add(parents.get(i));
            }

        return n;
        }
    }
