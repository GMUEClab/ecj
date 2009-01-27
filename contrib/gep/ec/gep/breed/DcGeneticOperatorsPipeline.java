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
 *DcGeneticOperatorsPipelinePipeline.java
 * 
 * Created: Dec 2006
 * By: Bob Orchard
 */

/**
 * MutationPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class DcGeneticOperatorsPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_DCGENETICOPERATORS_PIPE = "dcgeneticoperators";
    
    public int numSources() { return NUM_SOURCES; }  

    public Parameter defaultBase() { return GEPDefaults.base().push(P_DCGENETICOPERATORS_PIPE);/* */ }

    
    public Object clone()
    {
        DcGeneticOperatorsPipeline c = (DcGeneticOperatorsPipeline)(super.clone());
        
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

        // if not using constants then nothing to do here ... just return the count
        // of individuals produced
        GEPSpecies s = (GEPSpecies) inds[start].species;
        if (!s.useConstants)
        	return n;
        
        // We will do the following to the constants and Dc areas of genes ...
        //
        // 1. RNC mutation ... mutate the actual constants in the genomeConstants array in the individuals
        // 2. Dc mutation of the points (values) in the Dc areas of the genes
        // 3. Dc inversion of the points (values) in the Dc areas of the genes
        // 4. Dc IS transposition of the points (values) in the Dc areas of the genes
        //
        // Dc mutation - randomly select from the entire population
        // ... can allow mutation to happen on a genome more than once
        // Number of points to mutate depends on the Dc mutation rate and the 
        // total number of points in the population ... according to Ferreira what she does
        // is calculate this number:
        //
        //	numberOfPointsToMutate = #genomes * num genes per genome * sizeOfDcArea * dcMutationProbability;
        //  (number of individuals or number of chromosomes is same as number of genomes)
        //  (dcMutationProbability is Dc mutation rate ... usually set to  default of 0.044)
        //  
        // Then randomly select this many points in the population (excluding
        // the elite one(s) ... usually 1 elite member from previous generation)
        // and does a mutation.
        //
        MersenneTwisterFast srt = state.random[thread];
        GEPIndividual randInd;
        int geneIndex, pos;
        int totalNumberOfGenes = n * s.numberOfGenes;
        int chosenOnes[];
        
        // RNC mutation ... change some of the random constants associated with each gene in each chromosome
        int numberOfConstantsToMutate = (int)Math.round((double)(totalNumberOfGenes * s.numberOfConstantsPerGene) * s.rncMutationProbability);
        // Note: below we choose an individual, then for each chromosome in the individual we
        //       choose a gene from the chromosome, then a constant in 
        //       the constants for the gene
        for(int q=0; q<numberOfConstantsToMutate; q++)
        {   // choose a random individual form new pop excluding the elite(s))
try {
	        randInd = (GEPIndividual)inds[srt.nextInt(n)]; // the individual to mutate
	        // mutate the constants in each chromosome of the individual
	        for (int i=0; i<randInd.chromosomes.length; i++)
	        {
	        	GEPChromosome chromosome = randInd.chromosomes[i];
	        	double genomeConstants[][] = chromosome.genomeConstants; // the Constant arrays in this individual (genome)
	        	// choose a specific gene's constant array in the genome
	        	geneIndex = srt.nextInt(s.numberOfGenes);
	        	double constants[] = genomeConstants[geneIndex];
	        	// and the position within the Constant array
	        	pos = srt.nextInt(s.numberOfConstantsPerGene); 
	        	// now set the new constant point to reference one of the constants associated with the gene
	        	// create the values in Dc of the gene
	        	if (s.integerConstants)
	        	{   int range = (int)((s.constantsUpperLimit-s.constantsLowerLimit)+1);
	        		constants[pos] = srt.nextInt(range) + s.constantsLowerLimit;
	        	}
	        	else
	        	    constants[pos] = chromosome.getRandomFromLowerToUpper(srt, s.constantsLowerLimit, s.constantsUpperLimit);
	
	        	chromosome.parsedGeneExpressions = null;
	        }
            randInd.evaluated = false;
            randInd.chromosomesParsed = false;
} catch (Exception e) { e.printStackTrace(); }
        }
        
        // Dc Mutation
        // should we round up the number of points
        int numberOfPointsToMutate = (int)Math.round((double)(totalNumberOfGenes * s.tailSize) * s.dcMutationProbability);
        
        // Note: below we choose an individual, then for each chromosome in the individual we
        //       choose a gene from the chromosome, then a point in the Dc area
        //       We could also have chosen a point from the total number of Dc points in the
        //       population (excluding the elite(s)) and then mapped that to the ind/gene/point...
        //       might be faster than calling random number generator 3 times
        for(int q=0; q<numberOfPointsToMutate; q++)
        {   // choose a random individual form new pop excluding the elite(s))
try {
	        randInd = (GEPIndividual)inds[srt.nextInt(n)]; // the individual to mutate
	        // Dc mutate the constants in each chromosome of the individual
	        for (int i=0; i<randInd.chromosomes.length; i++)
	        {
	        	GEPChromosome chromosome = randInd.chromosomes[i];
	        	int genomeDc[][] = chromosome.genomeDc; // the DcAreas in this individual (genome)
	        	// choose a specific gene's Dc area in the genome
	        	geneIndex = srt.nextInt(s.numberOfGenes);
	        	int DcArea[] = genomeDc[geneIndex];
	        	// and the position within the DcArea
	        	pos = srt.nextInt(DcArea.length); // DcArea.length is same as s.tailSize
	        	// now set the new constant point to reference one of the constants associated with the gene
	        	DcArea[pos] = srt.nextInt(s.numberOfConstantsPerGene);
	        	
	        	chromosome.parsedGeneExpressions = null;
        }
        randInd.evaluated = false;
        randInd.chromosomesParsed = false;
} catch (Exception e) { e.printStackTrace(); }
        }
        
        // Dc inversion - as in standard gene inversion, the inversion rate (probability?) is
        // used with the number of individuals in the population (unlike mutation which uses the
        // number of points in the population)
        // should we round up the number of points
        int numberOfGenomesToInvert = (int)Math.round((double)n * s.dcInversionProbability);
        // Note: below we choose an individual, then for each chromosome in the individual we
        //       choose a gene from the chromosome, then a range in the Dc area to invert.
        // select the genomes without replacement
        chosenOnes = chooseWithoutReplacement(state, thread, numberOfGenomesToInvert, n);
try {
    	for(int q=0; q<numberOfGenomesToInvert; q++)
        {
        	int select = chosenOnes[q];
        	GEPIndividual ind = (GEPIndividual)inds[select];
	        for (int i=0; i<ind.chromosomes.length; i++)
	        {
	        	GEPChromosome chromosome = ind.chromosomes[i];
                dcInvert(chromosome.genomeDc, srt);
	        	chromosome.parsedGeneExpressions = null;
	        }
	        ind.evaluated = false;
	        ind.chromosomesParsed = false;
        }
} catch (Exception e) { e.printStackTrace(); }

		// Dc IS transposition - as in standard gene IS Transposition, the transposition 
        // rate (probability?) is used with the number of individuals in the 
		// population (unlike mutation which uses the number of points in the population)
		// should we round up the number of points ... probably
		int numberOfGenomesToTranspose = (int)Math.round((double)n * s.dcIsTranspositionProbability);
		// Note: below we choose an individual, then for each chromosome in the individual we
        //       choose a gene from the chromosome, then a range in the Dc area to invert.
		// select the genomes without replacement
		chosenOnes = chooseWithoutReplacement(state, thread, numberOfGenomesToTranspose, n);
try {
		for(int q=0; q<numberOfGenomesToTranspose; q++)
		{
			int select = chosenOnes[q];
			GEPIndividual ind = (GEPIndividual)inds[select];
	        for (int i=0; i<ind.chromosomes.length; i++)
	        {
	        	GEPChromosome chromosome = ind.chromosomes[i];
		        dcIsTranspose(chromosome.genomeDc, srt);
        	    chromosome.parsedGeneExpressions = null;
	        }
	        ind.evaluated = false;
	        ind.chromosomesParsed = false;
		}
} catch (Exception e) { e.printStackTrace(); }

        
        return n;
     }
    
    /** IS Transposition will transpose (insert) a 'small' fragment of a gene's Dc area to (in) 
     *  another place in the DcArea. The new location can be in the same gene Dc area
     *  in which the fragment was selected or one of the other gene's Dc areas. This is not 
     *  clear from Ferreira's book but with standard IS transposition another gene could be the target
     *  of the transposition
     *  <br>
     *  So we do the following:
     *  <br>
     *  1. choose the gene Dc area from which to extract the fragment (gf) <br>
     *  2. choose a position in the gene Dc area as the start of the fragment (gfStart) <br>
     *  3. choose the size of the fragment (from 1 to DcAreaLength-gfStart) <br>
     *     (this will restrict the gene fragment from gong beyond the end of the Dc area) <br>  
     *     NOTE: Ferreira's papers seem to suggest that 3 fixed sizes are allowed only ... 
     *     probably 1 2 or 3.<br>
     *  4. choose the gene to receive the fragment (gt) <br>
     *  5. choose position after which the gene will be inserted (from 0 to 
     *     DcAreaSize-gfSize-1); this assumes we do not want to truncate the fragment 
     *     (which may not be correct). <br>
     *  6. shift the gene Dc area contents right to make room for the insertion and then copy 
     *     the fragment into the gene Dc area. <br>
     *   <br>
     * 
     */
    
    public void dcIsTranspose( int genomeDc[][], MersenneTwisterFast srt)
    {  
try {
       int index = srt.nextInt(genomeDc.length); // number of genes in the chromosome
       int gf[] = genomeDc[index]; // the gene from which we extract the fragment
       int dcAreaLength = gf.length;
       int gfStart = srt.nextInt(dcAreaLength);
       // ??? should we set up a set of 3 fixed sizes to choose from as GeneXpro does (I think)
       int gfSize = srt.nextInt(dcAreaLength-gfStart)+1;
       index = srt.nextInt(genomeDc.length);
       int gt[] = genomeDc[index]; // the gene in which we will insert the fragment
       int gtStart = srt.nextInt(dcAreaLength-gfSize+1); // place where we will start the insert (just before this spot)
       int temp[] = new int[gfSize];
       int i;
       for (i=0; i<gfSize; i++) // copy the fragment
    	   temp[i] = gf[gfStart+i];
       for (i=dcAreaLength-gfSize-1; i>=gtStart; i--) // shift receiving gene Dc area to the right
    	   gt[i+gfSize] = gt[i];
       for (i=0; i<gfSize; i++) // insert the fragment
    	   gt[i+gtStart] = temp[i];
} catch (Exception e) { e.printStackTrace(); }
    }

    
    /** Inversion will randomly select a gene in the chromosome (individual),
     *  randomly select start end and positions in the gene Dc area and reverse
     *  or invert the elements in this range. 
     */
    public void dcInvert( int genomeDc[][], MersenneTwisterFast srt)
    {  
try {  
       int geneToModify = srt.nextInt(genomeDc.length);// the gene index
       int geneDc[] = genomeDc[geneToModify];
       int len = geneDc.length;

       if (len > 1) // if only 1 elt in DcArea can't invert anything -- should never be just 1
       {   // start and end will be 0-based
          int start = srt.nextInt(len-1); 
          int end = srt.nextInt(len-start-1) + start +1; // must be > the start position
          // now invert them
          int temp[] = new int[end-start+1];
          for (int i=0; i<temp.length; i++)
          	temp[i] = geneDc[i+start];
          for (int i=0; i<temp.length; i++)
          	geneDc[end-i] = temp[i];
       }
} catch (Exception e) { e.printStackTrace(); }
    }

    
}
