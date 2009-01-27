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

import java.util.*;


/* 
 * GenerecombinationPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * GenerecombinationPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class GenerecombinationPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_GENERECOMBINATION_PIPE = "generecombination";
    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_GENERECOMBINATION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        GenerecombinationPipeline c = (GenerecombinationPipeline)(super.clone());
        
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
    	
    	int n = 0;
try {
	    n = sources[0].produce(max,max,start,subpopulation,inds,state,thread);
} catch (Exception e) { e.printStackTrace(); }

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // recomine 'em - randomly select from the entire population based on the
        //gene recombination probability (rate). So we will choose the numberToRecombine as:
        //
        //    numberToRecombine = geneRecombinationProbability * (populationSize - numberOfElites)
        //
        // where n returned from (produced by) the sources breeder is the
        // (populationSize - numberOfElites)
        //
        // Note: we could walk through the entire population (in pairs) and use the probability to decide 
        //       if the individual should be transposed but this is slower and not it seems what Ferreira
        //       does in GeneXpro
        //
        // ... can't recombine a genome more than once I believe so we should be selecting the n
        // individuals to recombine using a 'without replacement method' -- Ferreira confirmed this
        
        GEPSpecies s = (GEPSpecies) inds[0].species;
        MersenneTwisterFast srt = state.random[thread];

        // note: must be an even number of genomes to recombine so make this so ...
        int numberToRecombine = (int)Math.round(s.geneRecombinationProbability * (double)n);
        numberToRecombine = (numberToRecombine/2)*2;
        
        // select the genomes without replacement
        int chosenOnes[] = chooseWithoutReplacement(state, thread, numberToRecombine, n);
try {
	    for(int q=0; q<chosenOnes.length-1; q += 2)
	    {
	    	GEPChromosome chromosome1, chromosome2;
	    	GEPIndividual ind1, ind2;
	    	int selInd1 = chosenOnes[q];
	    	int selInd2 = chosenOnes[q+1];
	    	ind1 = (GEPIndividual)inds[selInd1];
	    	ind2 = (GEPIndividual)inds[selInd2];
	    	int numChromosomes = ind1.chromosomes.length;
	    	for (int i=0; i<numChromosomes; i++)
	    	{
	    		chromosome1 = ind1.chromosomes[i];
	    		chromosome2 = ind2.chromosomes[i];
	            geneRecombine(srt, chromosome1, chromosome2);
		        chromosome1.parsedGeneExpressions = null;
		        chromosome2.parsedGeneExpressions = null;
	    	}
	        ind1.evaluated = false;
	        ind1.chromosomesParsed = false;
	        ind2.evaluated = false;
	        ind2.chromosomesParsed = false;
	    }
} catch (Exception e) { e.printStackTrace(); }

        return n;
     }
    
    
    /** Gene recombination will swap one gene in a chromosome
     *  with the same gene in another chromosome ... pretty simple. 
     *  <br>
     *  1. choose gene number to swap. <br>
     *  2. swap the 2 genes between chromosomes
     *   <br>
     * 
     */
    public void geneRecombine( MersenneTwisterFast srt, GEPChromosome chromosome1, 
    		GEPChromosome chromosome2)
    {  
       int genome1[][] = chromosome1.genome;
       int genome2[][] = chromosome2.genome; 
       int gene_index = srt.nextInt(genome1.length); 
       int temp[] = genome1[gene_index];
       genome1[gene_index] = genome2[gene_index];
       genome2[gene_index] = temp;
    }
}
