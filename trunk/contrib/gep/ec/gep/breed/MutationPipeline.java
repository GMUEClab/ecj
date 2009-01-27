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
import ec.gep.*;

/* 
 * MutationPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * MutationPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class MutationPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_MUTATION_PIPE = "mutation";    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_MUTATION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        MutationPipeline c = (MutationPipeline)(super.clone());
        
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
    	// (excluding the elite individual(s) that are passed on unaltered)
        int n = sources[0].produce(max,max,start,subpopulation,inds,state,thread);

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // mutate 'em - randomly select from the entire population
        // ... can allow mutation to happen on a genome more than once
        // Number of points to mutate depends on the mutation rate and the 
        // total number of points in the population ... according to Ferreira what she does
        // is calculate this number:
        //
        //	numberOfPointsToMutate = #genomes * sizeOfGenome * mutationProbability;
        //  (number of individuals or number of chromosome is same as number of genomes)
        //  (mutationProbability is mutation rate ... usually set to give about
        //   2 point mutations per chromosome ... so normal default of 0.044 works for
        //   example with chromosome of size 51 since 2/51 is approx 0.044)
        //  
        // Then she randomly selects this many points in the population (excluding
        // the elite one(s) ... in her case always 1 elite member from previous generation)
        // and does a mutation.
        //
        MersenneTwisterFast srt = state.random[thread];
        GEPSpecies s = (GEPSpecies) inds[start].species;
        // should we round up the number of points
        int numberOfPointsToMutate = (int)Math.round((double)(n * s.geneSize * s.numberOfGenes) * s.mutationProbability);
        
        // Note: below we choose an individual, then for each chromosome in the individual we
        //       choose a gene from the chromosome, then a point in the gene
        //       We could also have chosen a point from the total number of points in the
        //       population (excluding the elite(s)) and then mapped that to the ind/gene/point...
        //       might be faster than calling random number generator 3 times
        for(int q=0; q<numberOfPointsToMutate; q++)
        {   // choose a random individual form new pop excluding the elite(s))
try {
	        GEPIndividual randInd = (GEPIndividual)inds[srt.nextInt(n)]; // the genome (chromosome) to mutate
	    	int numChromosomes = randInd.chromosomes.length;
	    	// do this for each chromosome in the individual
	    	for (int i=0; i<numChromosomes; i++)
	    	{
	    		GEPChromosome chromosome = randInd.chromosomes[i];
        	    int genome[][] = chromosome.genome;
        	    // choose a gene in the genome
        	    int gene[] = genome[srt.nextInt(s.numberOfGenes)];
        	    // and the position within the gene
        	    int genePos = srt.nextInt(gene.length);
        	    // now set the new point to a random terminal or function
        	    gene[genePos] = s.symbolSet.chooseFunctionOrTerminalSymbol(state, thread, genePos, s);
                chromosome.parsedGeneExpressions = null;
	    	}
	    	randInd.evaluated = false;
	    	randInd.chromosomesParsed = false;
} catch (Exception e) { e.printStackTrace(); }
        }
        return n;
     }
    
}
