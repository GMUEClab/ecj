/*
* Copyright (c) 2006 by National Research Council of Canada.
*
* This software is the confidential and proprietary information of
* the National Research Council of Canada ("Confidential Information").
* You shall not disclose such Confidential Information and shall use it only
* in accordance with the terms of the license agreement you entered into
* with the National Research Council of Canada.
*
* THE NATIONAL RESEARCH COUNCIL OF CANADA MAKES NO REPRESENTATIONS OR
* WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
* THE NATIONAL RESEARCH COUNCIL OF CANADA SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
*
*
*/

package ec.gep.breed;
import ec.*;
import ec.util.*;
import ec.vector.VectorIndividual;
import ec.gep.*;

/* 
 * GenetranspositionPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * GenetranspositionPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class GenetranspositionPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_GENETRANSPOSITION_PIPE = "genetransposition";
    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_GENETRANSPOSITION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        GenetranspositionPipeline c = (GenetranspositionPipeline)(super.clone());
        
        // deep-cloned stuff
        return c;
    }

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);

    }

    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 
    {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there -- for gep we force all of the population to
    	// be dealt with at once so min is set to max .. should be the entire population
    	// we use without the elite(s) individuals
    	int n = sources[0].produce(max,max,start,subpopulation,inds,state,thread);

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // transpose 'em - randomly select from the entire population based on the
        // gene tranposition probability (rate). So we will choose the numberToTranspose as:
        //
        //    numberToTranspose = geneTranspostionProbability * (populationSize - numberOfElites)
        //
        // where n returned from (produced by) the sources breeder is the
        // (populationSize - numberOfElites)
        //
        // Note: we could walk through the entire population and use the probability to decide 
        //       if the individual should be transposed but this is slower and not it seems what Ferreira
        //       does in GeneXpro
        //
        // ... can't transpose a genome more than once I believe so we should be selecting the n
        // individuals to transpose using a 'without replacement method' -- Ferreira confirmed this
        
        GEPSpecies s = (GEPSpecies) inds[0].species;
        MersenneTwisterFast srt = state.random[thread];

        int numberToTranspose = (int)Math.round(s.geneTranspositionProbability * (double)n);
        
	    for(int q=0; q<numberToTranspose; q++) 
        {
	    	int select = srt.nextInt(n); // choose 1 to transpose --- should be a without replacement selection!
	    	GEPIndividual ind = (GEPIndividual)inds[select];
	    	int numChromosomes = ind.chromosomes.length;
	    	// do this for each chromosome in the individual
	    	for (int i=0; i<numChromosomes; i++)
	    	{
	    		GEPChromosome chromosome = ind.chromosomes[i];
                geneTranspose(chromosome.genome, srt);
                chromosome.parsedGeneExpressions = null;
	    	}
            ind.evaluated = false;
            ind.chromosomesParsed = false;
        }

        return n;
     }
    /** Gene Transposition will transpose will swap the 1st gene in the chromosome
     *  with one of the other genes ... pretty simple. 
     *  <br>
     *  So we do the following (assuming there is more than 1 gene) :
     *  <br>
     *  1. choose one of the 'other' genes to swap with the first one. <br>
     *  2. swap them in the genome
     *   <br>
     * 
     */
    
    public void geneTranspose( int genome[][], MersenneTwisterFast srt)
    {  
        // only if there is more than 1 gene
        if ( genome.length > 1)
        {
           int gene_index = srt.nextInt(genome.length-1)+ 1; // add 1 to get beyond the 1st gene
           int temp[] = genome[0];
           genome[0] = genome[gene_index];
           genome[gene_index] = temp;
        }
    }
}
