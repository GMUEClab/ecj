/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.util.Parameter;
import ec.vector.*;

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
        int start, 
        int subpopulation,
        Individual[] inds, 
        EvolutionState state, 
        int thread) 
        {

        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            if (sources[0] instanceof SelectionMethod)
                inds[q] = (Individual)(inds[q].clone());

            VectorIndividual ind = (VectorIndividual)(inds[q]);
            
            //duplicate from the genome between a random begin and end point, 
            //and put that at the end of the new genome.
            int len = ind.genomeLength();
            int end = 0;
            int begin = state.random[thread].nextInt(len);
            do 
                {
                end = state.random[thread].nextInt(len);
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
