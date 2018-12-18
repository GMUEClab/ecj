/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.vector.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ec.vector.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>GeneDuplicationPipeline is designed to duplicate a sequence of genes from the chromosome and append
 * them to the end of the chromosome.  The sequence of genes copied are randomly determined.  That is to
 * say, a random begining index is selected and a random ending index is selected from the chromosome.  Then
 * this area is copied (begining inclusive, ending exclusive) and appended to the end of the chromosome.
 * Since randomness is a factor several checks are performed to make sure the begining and ending indicies are
 * valid.  For example, since the ending index is exclusive, the ending index cannot equal the begining index (a
 * new ending index would be randomly seleceted in this case).  Likewise the begining index cannot be larger than the
 * ending index (they would be swapped in this case).</p>
 *
 * <p><b>Default Base</b><br>
 * ec.vector.breed.GeneDuplicationPipeline
 *
 * @author Sean Luke, Joseph Zelibor III, and Eric Kangas
 * @version 1.0
 */
public class GeneDuplicationPipeline extends BreedingPipeline
    {
    public static final String P_DUPLICATION = "duplicate";
    public static final int NUM_SOURCES = 1;

    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_DUPLICATION);
        }

    public int numSources() { return NUM_SOURCES; }

    public int produce(int min,
        int max,
        int subpopulation,
        ArrayList<Individual> inds,
        EvolutionState state,
        int thread, HashMap<String, Object> misc)
        {
        int start = inds.size();
                
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,subpopulation,inds, state,thread, misc);


        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            {
            return n;
            }

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            //duplicate from the genome between a random begin and end point,
            //and put that at the end of the new genome.
            VectorIndividual ind = (VectorIndividual)(inds.get(q));
            
            int len = ind.genomeLength();

            //zero length individual, just return
            if (len == 0)
                {
                return n;
                }

            int end = 0;
            int begin = state.random[thread].nextInt(len+1);
            do 
                {
                end = state.random[thread].nextInt(len+1);
                } 
            while (begin == end);  //because the end is exclusive, start cannot be
            //equal to end.
            

            if (end < begin) 
                {
                int temp = end;  //swap if necessary
                end = begin;
                begin = temp;
                }

            // copy the original into a new array.
            Object[] original = new Object[2];
            ind.split(new int[] {0, len}, original);
                        
            // copy the splice into a new array
            Object[] splice = new Object[3];
            ind.split(new int[] {begin, end}, splice);
                        
            // clone the genes in splice[1] (which we'll concatenate back in) in case we're using GeneVectorIndividual
            ind.cloneGenes(splice[1]);
            
            // appends the pieces together with the splice at the end.
            ind.join(new Object[] {original[1], splice[1]});
            }
        return n;  // number of individuals produced, 1 here.
        }

    }
