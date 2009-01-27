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
 * RIStranspositionPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * RIStranspositionPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class RIStranspositionPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_RISTRANSPOSITION_PIPE = "RIStransposition";
    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_RISTRANSPOSITION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        RIStranspositionPipeline c = (RIStranspositionPipeline)(super.clone());
        
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
        // ris transposition probability (rate). So we will choose the numberToTranspose as:
        //
        //    numberToTranspose = risTranspostionProbability * (populationSize - numberOfElites)
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

        int numberToTranspose = (int)Math.round(s.risTranspositionProbability * (double)n);
try {
	    for(int q=0; q<numberToTranspose; q++) 
	    {
	    	int select = srt.nextInt(n); // choose 1 to transpose --- should be a without replacement selection!
        	GEPIndividual ind = (GEPIndividual)inds[select];
	    	int numChromosomes = ind.chromosomes.length;
	    	// do this for each chromosome in the individual
	    	for (int i=0; i<numChromosomes; i++)
	    	{
	    		GEPChromosome chromosome = ind.chromosomes[i];
	            risTranspose(chromosome.genome, srt, s);
                chromosome.parsedGeneExpressions = null;
	    	}
            ind.evaluated = false;
            ind.chromosomesParsed = false;
	    }
} catch (Exception e) { e.printStackTrace(); }

        return n;
     }
    /** RIS (Root) Transposition will transpose (insert) a 'small' fragment of a gene to (in) 
     *  the head of a gene (AT the root position). The fragment start position is in the head of
     *  the gene and must be a function. The new location can be in the same gene 
     *  in which the gene was found or one of the other genes. 
     *  <br>
     *  So we do the following:
     *  <br>
     *  1. choose the gene from which to extract the fragment (gf) <br>
     *  2. choose a position in the gene head as the start of the fragment (gfStart);
     *     must be a function, if not scan right till find one or exit. <br>
     *  3. choose the size of the fragment (from 1 to min(headsize, genelength-gfStart)) <br>
     *     (this will restrict the gene fragment from being too large to transpose 
     *     and going beyond the size of the head that it can replace) <br>
     *     NOTE: Ferreira's papers suggest that 3 fixed sizes are allowed only ... 
     *     probably 1 2 or 3.<br>
     *  4. choose the gene to receive the fragment (gt) <br>
     *  5. shift the gene contents right in the head to make room for the insertion and then copy 
     *     the fragment into the gene. <br>
     *   <br>
     * 
     */
    
    public void risTranspose( int genome[][], MersenneTwisterFast srt, GEPSpecies s)
    {  
try {
           int headsize = s.headSize;
	       int index = srt.nextInt(genome.length);
           int gf[] = genome[index]; // the gene from which we extract the fragment
           int gfStart = srt.nextInt(headsize);// only select from the head
           // must be a function at the start position (not a terminal)
           while (gfStart < headsize &&
        		  (s.symbolSet.symbols[gf[gfStart]] instanceof GEPTerminalSymbol))
        	   gfStart++;
           if (gfStart >= headsize) // no function past the head ... so do nothing
        	   return;
           
           // ??? should we set up a set of f3 fixed sizes to choose from as GeneXpro does (I think)
                      int gfSize = srt.nextInt(headsize < gf.length-gfStart ? headsize : gf.length-gfStart)+1;
           index = srt.nextInt(genome.length);
           int gt[] = genome[index]; // the gene in which we will insert the fragment
           // always insert at beginning of the target gene
           int temp[] = new int[gfSize];
           int i;
           for (i=0; i<gfSize; i++) // copy the fragment
        	   temp[i] = gf[gfStart+i];
           for (i=headsize-gfSize-1; i>=0; i--) // shift head of the receiving gene to the right
        	   gt[i+gfSize] = gt[i];
           for (i=0; i<gfSize; i++) // insert the fragment
        	   gt[i] = temp[i];
} catch (Exception e) { e.printStackTrace(); }
    }
}
