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
 * InversionPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * InversionPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class InversionPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_INVERSION_PIPE = "inversion";
    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_INVERSION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        InversionPipeline c = (InversionPipeline)(super.clone());
        
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

        // invert 'em - randomly select from the entire population based on the
        // inversion probability (rate). So we will choose the numberToInvert as:
        //
        //    numberToInvert = inversionProbability * (populationSize - numberOfElites)
        //
        // where n returned from (produced by) the sources breeder is the
        // (populationSize - numberOfElites)
        //
        // Note: we could walk through the entire population and use the probability to decide 
        //       if the individual should be inverted but this is slower and not it seems what Ferreira
        //       does in GeneXpro
        //
        // ... can't invert a genome more than once I believe so we should be selecting the n
        // individuals to invert using a 'without replacement method' -- Ferreira confirmed this

        GEPSpecies s = (GEPSpecies) inds[0].species;
        int headsize = s.headSize;
        MersenneTwisterFast srt = state.random[thread];

        int numberToInvert = (int)Math.round(s.inversionProbability * (double)n);
        
        for(int q=0; q<numberToInvert; q++)
        {
        	int select = srt.nextInt(n); // choose 1 to invert --- should be a without replacement selection!
        	GEPIndividual ind = (GEPIndividual)inds[select];
	    	int numChromosomes = ind.chromosomes.length;
	    	// do this for each chromosome in the individual
	    	for (int i=0; i<numChromosomes; i++)
	    	{
	    		GEPChromosome chromosome = ind.chromosomes[i];
	            invert(chromosome.genome, srt, headsize);
                chromosome.parsedGeneExpressions = null;
	    	}
            ind.evaluated = false;
            ind.chromosomesParsed = false;
        }

        return n;
     }
    /** Inversion will randomly select a gene in the chromosome (individual),
     *  randomly select start end and positions in the gene and reverse
     *  or invert the symbols (terminals and functions) in this range. It
     *  only is done on the head of the gene.
     * 
     */
    
    public void invert( int genome[][], MersenneTwisterFast srt, int headsize)
    {  
try {  
       int geneToModify = srt.nextInt(genome.length);// the gene index
       int gene[] = genome[geneToModify];

       if (headsize > 2) // if only 1 elt in head can't invert anything
       {   // start and end will be 0-based
          int start = srt.nextInt(headsize-1); // must be less than the last symbol position in the head
          int end = srt.nextInt(headsize-start-1) + start +1; // must be > the start position
          // now invert them
          int temp[] = new int[end-start+1];
          for (int i=0; i<temp.length; i++)
          	temp[i] = gene[i+start];
          for (int i=0; i<temp.length; i++)
          	gene[end-i] = temp[i];
       }
} catch (Exception e) { e.printStackTrace(); }
    }
    
}
