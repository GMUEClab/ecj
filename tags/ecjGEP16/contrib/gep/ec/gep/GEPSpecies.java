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

package ec.gep;
import ec.*;
import ec.util.*;

/* 
 * GEPSpecies.java
 * 
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * GPSpecies is an extension of the Species class which is suitable as a species
 * for GEP subpopulations.  GEPSpecies' individuals must be GEPIndividuals,
 * and their pipelines are GEPBreedingPipelines (at any rate,
 * the pipelines will have to return members of GEPSpecies!).
 *
 <p><b>Default Base</b><br>
 gep.species

 *
 * @author Bob Orchard
 * @version 1.0 
 */

public class GEPSpecies extends Species
{
    public static final String P_GEPSPECIES = "species";
    public final static String P_NUMGENES = "numgenes";
    public final static String P_HEADSIZE = "gene-headsize";
    public final static String P_LINKINGFUNCTION = "gene-linking-function";
    public final static String P_PROBLEMTYPE = "problemtype";
    public final static String P_CLASSIFICATION_THRESHOLD = "classification-threshold";
    public final static String P_TIMESERIES_DELAY = "timeseries-delay";
    public final static String P_TIMESERIES_EMBEDDINGDIMENSION = "timeseries-embeddingdimension";
    public final static String P_TIMESERIES_TESTINGPREDICTIONS = "timeseries-testingpredictions";
    
    public final static String P_INVERSIONPROB = "inversion-prob";
    public final static String P_MUTATIONPROB = "mutation-prob";
    public final static String P_ISTRANSPOSITIONPROB = "istransposition-prob";
    public final static String P_RISTRANSPOSITIONPROB = "ristransposition-prob";
    public final static String P_ONEPOINTRECOMBPROB = "onepointrecomb-prob";
    public final static String P_TWOPOINTRECOMBPROB = "twopointrecomb-prob";
    public final static String P_GENERECOMBPROB = "generecomb-prob";
    public final static String P_GENETRANSPOSITIONPROB = "genetransposition-prob";

    public final static String P_USECONSTANTS = "use-constants";
    public final static String P_NUMCONSTANTSPERGENE = "numconstantspergene";
    public final static String P_INTEGERCONSTANTS = "integer-constants";
    public final static String P_CONSTANTSLOWERLIMIT = "constants-lowerlimit";
    public final static String P_CONSTANTSUPPERLIMIT = "constants-upperlimit";

    public final static String P_DCINVERSIONPROB = "dc-inversion-prob";
    public final static String P_DCMUTATIONPROB = "dc-mutation-prob";
    public final static String P_DCISTRANSPOSITIONPROB = "dc-istransposition-prob";
    public final static String P_RNCMUTATIONPROB = "rnc-mutation-prob";

    public final static String P_SYMBOLSET = "symbolset"; 
    
    // For use in identifying the problem type
    public final static int PT_FUNCTIONFINDING = 0;
    public final static int PT_CLASSIFICATION = 1;
    public final static int PT_LOGICAL = 2;
    public final static int PT_TIMESERIES = 3;
    
    // For use in determining the gene 'linking' function --- historical only since this was early way of specifying linking functions
    public final static int LF_FIELDSIZE_IN_LINKINGFUNCTIONS = 5;
    public final static int LF_ADD = 0;
    public final static int LF_SUB = 1;
    public final static int LF_MUL = 2;
    public final static int LF_DIV = 3;
    public final static int LF_AND = 4;
    public final static int LF_OR = 5;
    public final static int LF_XOR = 6;
    public final static int LF_NAND = 7;
    public final static int LF_NOR = 8;
    public final static int LF_NXOR = 9;

    /** Probability that a gene will mutate */
    public double mutationProbability;
    /** Probability of inversion in a gene */
    public double inversionProbability;
    /** Probability of IS transposition in a gene */
    public double isTranspositionProbability;
    /** Probability of RIS transposition in a gene */
    public double risTranspositionProbability;
    /** Probability of one point recombination in a gene */
    public double onePointRecombinationProbability;
    /** Probability of two point recombination in a gene */
    public double twoPointRecombinationProbability;
    /** Probability of gene recombination in a gene */
    public double geneRecombinationProbability;
    /** Probability of gene transposition */
    public double geneTranspositionProbability;
    
    /** Problem type, one of: PT_FUNCTIONFINDING, PT_CLASSIFICATION, PT_LOGICAL, PT_TIMESERIES. */
    public int problemType = -1;  // initially not known
    // problemTypeNames and problemTypeIds must match their elements
    String problemTypeNames[] = {"functionfinding", "classification", "timeseries", "logical"};
    public int problemTypeIds[] = {PT_FUNCTIONFINDING, PT_CLASSIFICATION, PT_TIMESERIES, PT_LOGICAL};
    public String problemTypeName = "";

    /** How many genes in each genome? */
    public int numberOfGenes;
    /** How big is the head of each gene? */
    public int headSize;
    /** How big is the tail of each gene - calculated from headSize and the 
        maximum arity of any function */
    public int tailSize;
    /** How big is the gene - calculated as headSize + tailSize */
    public int geneSize;
    
    /** Time series delay value (must be > 0) */
    public int timeseriesDelay = 1;
    /** Time series embedding dimension value (must be > 0). This is the number of independent variables
     *  for the timeseries data as it is processed from the raw sequential data set.  */
    public int timeseriesEmbeddingDimension = 1;
    /** Time series Testing Predictions value (must be > 0). This is the number of sets of data (at the end 
     *  of the processed time series data) to be used for testing. Other data is used for training. */
    public int timeseriesTestingPredictions = 1;
    
    /** Which operator to use to combine the values of gene expressions? Must be one of
        + - *  / and or xor nand nor nxor 
    
        NOTE: this is from early version that restricted to these 10 linking functions; now
        any GEPFunctionSymbol function can be used

        Each linking function name within the linkingFunctions String starts on a character
        index such that (index mod 5) is 0. So 5 chars allowed for each name in this string
        (so that each function name has at least one space after it).
     
        Allow: + or add   - or sub  * or mul   / or div
    */
    public String linkingFunctions = "+    add  -    sub  *    mul  /    div  and  or   xor  nand nor  nxor ";
    public String oldLinkingFunctionSymbolNames[] =
    { "Add", "Add", "Sub", "Sub", "Mul", "Mul", "Div", "Div", 
      "And", "Or", "Xor", "Nand", "Nor", "Nxor" 
    };
    public GEPFunctionSymbol linkingFunctionSymbol = null; 
    public String linkingFunctionName = "Add"; // default

    /** Should we use constants for each gene */
    public boolean useConstants = false;
    /** number of constants generated for each gene */
    public int numberOfConstantsPerGene = 0;
    /** Are constants integers or doubles?   */
    public boolean integerConstants = false;
    /** lower limit of constants */
    public double constantsLowerLimit = 0.0;
    /** upper limit of constants */
    public double constantsUpperLimit = 1.0;
    /** Probability that the constants in a gene will mutate */
    public double dcMutationProbability;
    /** Probability of inversion in the constants of a gene */
    public double dcInversionProbability;
    /** Probability of IS transposition in the constants of a gene */
    public double dcIsTranspositionProbability;
    /** Probability of the RNC constants being mutated */
    public double rncMutationProbability;
    
    /** SymbolSet associated with species */
    public GEPSymbolSet symbolSet;
    /** Name of SymbolSet associated with species */
    public String symbolSetName;


    public Parameter defaultBase()
    {
        return GEPDefaults.base().push(P_GEPSPECIES);
    }

    public void setup(final EvolutionState state, final Parameter base)
    {
        Parameter def = defaultBase();

        //*************************************************************************************
        // Handle the rates for mutation, inversion, transposition, and recombination of genes
        //*************************************************************************************
        mutationProbability = state.parameters.getDouble(
            base.push(P_MUTATIONPROB),def.push(P_MUTATIONPROB),0.0,1.0);
        if (mutationProbability==-1.0)
        {   state.output.warning("GEPSpecies must have a mutation probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_MUTATIONPROB),def.push(P_MUTATIONPROB));
            mutationProbability = 0.0;
        }
    
        inversionProbability = state.parameters.getDouble(
            base.push(P_INVERSIONPROB),def.push(P_INVERSIONPROB),0.0,1.0);
        if (inversionProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an inversion probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_INVERSIONPROB),def.push(P_INVERSIONPROB));
        inversionProbability = 0.0;
        }
    
        isTranspositionProbability = state.parameters.getDouble(
            base.push(P_ISTRANSPOSITIONPROB),def.push(P_ISTRANSPOSITIONPROB),0.0,1.0);
        if (isTranspositionProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an IS transposition probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_ISTRANSPOSITIONPROB),def.push(P_ISTRANSPOSITIONPROB));
        isTranspositionProbability = 0.0;
        }

        risTranspositionProbability = state.parameters.getDouble(
            base.push(P_RISTRANSPOSITIONPROB),def.push(P_RISTRANSPOSITIONPROB),0.0,1.0);
        if (risTranspositionProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an RIS transposition probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_RISTRANSPOSITIONPROB),def.push(P_RISTRANSPOSITIONPROB));
        risTranspositionProbability = 0.0;
        }
    
        onePointRecombinationProbability = state.parameters.getDouble(
            base.push(P_ONEPOINTRECOMBPROB),def.push(P_ONEPOINTRECOMBPROB),0.0,1.0);
        if (onePointRecombinationProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an one point recombination probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_ONEPOINTRECOMBPROB),def.push(P_ONEPOINTRECOMBPROB));
        onePointRecombinationProbability = 0.0;
        }
    
        twoPointRecombinationProbability = state.parameters.getDouble(
            base.push(P_TWOPOINTRECOMBPROB),def.push(P_TWOPOINTRECOMBPROB),0.0,1.0);
        if (twoPointRecombinationProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an two point recombination probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_TWOPOINTRECOMBPROB),def.push(P_TWOPOINTRECOMBPROB));
        twoPointRecombinationProbability = 0.0;
        }
    
        geneRecombinationProbability = state.parameters.getDouble(
            base.push(P_GENERECOMBPROB),def.push(P_GENERECOMBPROB),0.0,1.0);
        if (geneRecombinationProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an gene recombination probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_GENERECOMBPROB),def.push(P_GENERECOMBPROB));
        geneRecombinationProbability = 0.0;
        }
    
        geneTranspositionProbability = state.parameters.getDouble(
            base.push(P_GENETRANSPOSITIONPROB),def.push(P_GENETRANSPOSITIONPROB),0.0,1.0);
        if (geneTranspositionProbability==-1.0)
        {   state.output.warning("GEPSpecies must have an gene transposition probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                               base.push(P_GENETRANSPOSITIONPROB),def.push(P_GENETRANSPOSITIONPROB));
        geneTranspositionProbability = 0.0;
        }

        //*************************************************************************************
        // Get the number of genes in each chromosome (individual), the size of each gene's head, 
        //*************************************************************************************
        numberOfGenes = state.parameters.getInt(base.push(P_NUMGENES),def.push(P_NUMGENES),1);
        if (numberOfGenes < 1)
            state.output.error("Number of Genes in Genome must be > 0",
                               base.push(P_NUMGENES),def.push(P_NUMGENES));
                
        // headsize for genes 
        headSize = state.parameters.getInt(base.push(P_HEADSIZE),def.push(P_HEADSIZE),1);
        if (headSize==0)
            state.output.error("Genes must have a head size > 0",
            		base.push(P_HEADSIZE),def.push(P_HEADSIZE));
        
        //*************************************************************************************
        // Determine the problem type
        //*************************************************************************************

        // setup GEPDependentVariable class static info
        GEPDependentVariable.setup();
                
        problemTypeName = state.parameters.getStringWithDefault(base.push(P_PROBLEMTYPE), def.push(P_PROBLEMTYPE), "unknown");
        problemType = -1;
        for (int i=0; i<problemTypeNames.length; i++)
        {
        	if (problemTypeName.equals(problemTypeNames[i]))
        	{
        		problemType = problemTypeIds[i];
        		break;
        	}
        }
        if (problemType<0)
        	state.output.fatal("Must specify a problem type as one of: functionfinding, classification, timeseries, logical",
            		base.push(P_PROBLEMTYPE),def.push(P_PROBLEMTYPE)); 

        //*************************************************************************************
        // linking function for combining values of gene expressions
        // Must be one of:    addition, subtraction, multiplication, division
        // OR for logical problems one of:    and or xor nand nor nxor
        //
        // NOTE: above is historical ... now can be ANY defined GEPFunctionSymbol
        //
        // Default is Add
        //*************************************************************************************
        linkingFunctionName = state.parameters.getStringWithDefault(base.push(P_LINKINGFUNCTION), def.push(P_LINKINGFUNCTION), "Add");
        String linkingFunctionNameLower = linkingFunctionName.trim().toLowerCase();
        // See if it is one of the historical ones ...
        int linkingFunctionIndex = linkingFunctions.indexOf(linkingFunctionNameLower+" ");
        if (linkingFunctionIndex >= 0)
        { // get the actual name of the GEPFunctionSymbol
        	linkingFunctionName = oldLinkingFunctionSymbolNames[linkingFunctionIndex];
        }
        // should have a GEPFunctionSymbol class name in linkingFunctionName at this point!
        try {
            Class classDefinition = Class.forName(GEPSymbolSet.LOCATION_OF_FUNCTION_CLASSES+"."+linkingFunctionName);
            linkingFunctionSymbol = (GEPFunctionSymbol)classDefinition.newInstance();
        } catch (InstantiationException e) 
        {
          if (numberOfGenes > 1)
            state.output.fatal("Unable to create GEPFunctionSymbol class for linking function '" + linkingFunctionName + "'. " + e);
        } catch (IllegalAccessException e)
        {
          if (numberOfGenes > 1)
            state.output.fatal("Unable to create GEPFunctionSymbol class for linking function '" + linkingFunctionName + "' " + e);
        } catch (ClassNotFoundException e)
        {
          if (numberOfGenes > 1)
            state.output.fatal("Unable to create GEPFunctionSymbol class for linking function '" + linkingFunctionName + "' " + e);
        }
        // linkFunctionSymbol will be null if only 1 gene ... no linking function required
        if (linkingFunctionSymbol != null)
        {
        	// only logical linking functions allowed for logical problems, etc. 
	        if (!linkingFunctionSymbol.isLogicalFunction() && (problemType == PT_LOGICAL))		
	        	state.output.fatal("linking function for a logical problem type must be a logical function and not: " + linkingFunctionName,
	            		base.push(P_LINKINGFUNCTION),def.push(P_LINKINGFUNCTION));
	        
	        if (linkingFunctionSymbol.isLogicalFunction() && (problemType != PT_LOGICAL))		
	        	state.output.fatal("linking function for a non logical problem type must not be a logical function as specified: " + linkingFunctionName,
	            		base.push(P_LINKINGFUNCTION),def.push(P_LINKINGFUNCTION)); 
        	// arity of the linking function must suit the number of genes
	        // arity MUST be at least 2
	        // if arity is 2 ... any number of genes is OK
	        // if arity is 3 ... must be 3, 5, 7, 9, etc genes
	        // if arity is 4 ... must be 4, 7, 10, 13, etc genes
	        // and so on ...
	        int functionArity = linkingFunctionSymbol.arity;
	        if ( functionArity > 2 && 
	             ((numberOfGenes-functionArity)% (functionArity-1)) != 0
	           )
	        {
	        	state.output.fatal("Arity (" + linkingFunctionSymbol.arity + ") of linking function '" +
	        			linkingFunctionName + "' is NOT compatible with the number of genes: " + numberOfGenes,
	            		base.push(P_LINKINGFUNCTION),def.push(P_LINKINGFUNCTION)); 
	        }
        }

        //*************************************************************************************
        // Time series problems must specify all of the following if we are using raw (unprocessed)
        // time series data:
        //
        //		timeseries-delay 
        //		timeseries-embeddingdimension 
        //		timeseries-testingpredictions
        //
        // If these are not specified then we expect to get the 'processed' data from
        // a CSV file or from the user program
        //*************************************************************************************
        if (problemType == PT_TIMESERIES)
        {
        	timeseriesDelay = state.parameters.getIntWithDefault( base.push(P_TIMESERIES_DELAY), def.push(P_TIMESERIES_DELAY), -1);
        	timeseriesEmbeddingDimension = state.parameters.getIntWithDefault( base.push(P_TIMESERIES_EMBEDDINGDIMENSION), def.push(P_TIMESERIES_EMBEDDINGDIMENSION), -1);
        	timeseriesTestingPredictions = state.parameters.getIntWithDefault( base.push(P_TIMESERIES_TESTINGPREDICTIONS), def.push(P_TIMESERIES_TESTINGPREDICTIONS), -1);
        	
        	// if all time series params are missing assume will get data in processed form from
        	// user program or CSV file ... as for other problem types.
        	if (timeseriesDelay<=0 && timeseriesEmbeddingDimension<=0 && timeseriesTestingPredictions<=0)
        	    state.output.warning("Assuming time series data is NOT in raw format since delay, embedding dimension and testing prediction parameters not supplied.", 
            		    base.push(P_TIMESERIES_DELAY),def.push(P_TIMESERIES_DELAY));
        	// otherwise we should get raw data from user program or a file (CSV or simple file) ... but
        	// we need all 3 params to do so ... default any missing ones
        	else
        	{   if (timeseriesDelay <= 0)
        		{        		
        	    state.output.warning("Time series delay value must be > 0 ... defaulting to 1.", 
            		    base.push(P_TIMESERIES_DELAY),def.push(P_TIMESERIES_DELAY));
        		timeseriesDelay = 1;
        		}
	        	if (timeseriesEmbeddingDimension <= 0)
	        	{
	        	    state.output.warning("Time series embedding dimension value must be > 0 ... defaulting to 5.", 
	            		    base.push(P_TIMESERIES_EMBEDDINGDIMENSION),def.push(P_TIMESERIES_EMBEDDINGDIMENSION));
	        	    timeseriesEmbeddingDimension = 5;
	        	}
	        	if (timeseriesTestingPredictions <= 0)
	        	{
	        	    state.output.warning("Time series testing predictions value must be > 0 ... defaulting to 5.", 
	            		    base.push(P_TIMESERIES_TESTINGPREDICTIONS),def.push(P_TIMESERIES_TESTINGPREDICTIONS));
	        	    timeseriesTestingPredictions = 5;
	        	}
        	}
        }
        
        //*************************************************************************************
        // classification problems will specify a rounding threshold that is used
        // in fitness evaluation to convert the calculated values to a 0 or 1 for comparison
        // to the expected dependent variable values (which should be 0 or 1)
        //*************************************************************************************
    	GEPIndividual.setThresholdOFF();
        if (problemType == PT_CLASSIFICATION)
        {
        	double threshold = 0.5; // default value
        	if (state.parameters.exists(base.push(P_CLASSIFICATION_THRESHOLD), def.push(P_CLASSIFICATION_THRESHOLD)))
            {
        	    threshold = state.parameters.getDouble(base.push(P_CLASSIFICATION_THRESHOLD), def.push(P_CLASSIFICATION_THRESHOLD), -1.0);
            }
        	else
        	{
        	    state.output.error("Classification problem type must have a rounding threshold set.", 
            		    base.push(P_CLASSIFICATION_THRESHOLD),def.push(P_CLASSIFICATION_THRESHOLD));
        		
        	}
      	  GEPIndividual.setThreshold(threshold);
        }
        
        //*************************************************************************************
        // Constants parameters.
        //
        // Logical problem types can NOT use constants
        //*************************************************************************************
        useConstants = state.parameters.getBoolean(base.push(P_USECONSTANTS),def.push(P_USECONSTANTS),false);

        if (useConstants && (problemType == PT_LOGICAL))
        {
    	    state.output.warning("Cannot use constants with a logical problem type ... turning off constants.", 
        		    base.push(P_USECONSTANTS),def.push(P_USECONSTANTS));
    	    useConstants = false;
        }
        	
        if (useConstants)
        {   // only worry about various constant change probabilities/rates, etc. if in use
            integerConstants = state.parameters.getBoolean(base.push(P_INTEGERCONSTANTS),def.push(P_INTEGERCONSTANTS),false);
            state.output.message("Using " + (integerConstants ? "Integer" : "Floating Point") + " Constants");
            numberOfConstantsPerGene = state.parameters.getInt(base.push(P_NUMCONSTANTSPERGENE),def.push(P_NUMCONSTANTSPERGENE),1);
            if (numberOfConstantsPerGene <= 0)
                state.output.error("Number of constants per gene must be > 0.",
                                   base.push(P_NUMCONSTANTSPERGENE),def.push(P_NUMCONSTANTSPERGENE));
            if (!state.parameters.exists(base.push(P_CONSTANTSLOWERLIMIT), def.push(P_CONSTANTSLOWERLIMIT)))
                    state.output.error("GEPSpecies must have a lower limit for the random constants",
                            base.push(P_CONSTANTSLOWERLIMIT),def.push(P_CONSTANTSLOWERLIMIT));
            else
            	constantsLowerLimit = state.parameters.getDoubleWithDefault(
                    base.push(P_CONSTANTSLOWERLIMIT),def.push(P_CONSTANTSLOWERLIMIT), 0.0);
            if (integerConstants)
            	constantsLowerLimit = Math.floor(constantsLowerLimit);
            if (!state.parameters.exists(base.push(P_CONSTANTSUPPERLIMIT), def.push(P_CONSTANTSUPPERLIMIT)))
                state.output.error("GEPSpecies must have an upper limit for the random constants",
                        base.push(P_CONSTANTSLOWERLIMIT),def.push(P_CONSTANTSLOWERLIMIT));
            else
        	    constantsUpperLimit = state.parameters.getDoubleWithDefault(
                    base.push(P_CONSTANTSUPPERLIMIT),def.push(P_CONSTANTSUPPERLIMIT), 1.0);
            if (integerConstants)
            	constantsUpperLimit = Math.ceil(constantsUpperLimit);
            if (constantsUpperLimit <= constantsLowerLimit)
            	state.output.error("Constants lower limit must be greater than the upper limit");

            dcMutationProbability = state.parameters.getDouble(
                    base.push(P_DCMUTATIONPROB),def.push(P_DCMUTATIONPROB),0.0,1.0);
            if (dcMutationProbability==-1.0)
            {   state.output.warning("GEPSpecies must have a Dc mutation probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                                   base.push(P_DCMUTATIONPROB),def.push(P_DCMUTATIONPROB));
                dcMutationProbability = 0.0;
            }
            dcInversionProbability = state.parameters.getDouble(
                base.push(P_DCINVERSIONPROB),def.push(P_DCINVERSIONPROB),0.0,1.0);
            if (dcInversionProbability==-1.0)
            {   state.output.warning("GEPSpecies must have a Dc inversion probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                                   base.push(P_DCINVERSIONPROB),def.push(P_DCINVERSIONPROB));
                dcInversionProbability = 0.0;
            }
        
            dcIsTranspositionProbability = state.parameters.getDouble(
                base.push(P_DCISTRANSPOSITIONPROB),def.push(P_DCISTRANSPOSITIONPROB),0.0,1.0);
            if (dcIsTranspositionProbability==-1.0)
            {   state.output.warning("GEPSpecies must have a Dc IS transposition probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                                   base.push(P_DCISTRANSPOSITIONPROB),def.push(P_DCISTRANSPOSITIONPROB));
                dcIsTranspositionProbability = 0.0;
            }

            rncMutationProbability = state.parameters.getDouble(
                base.push(P_RNCMUTATIONPROB),def.push(P_RNCMUTATIONPROB),0.0,1.0);
            if (rncMutationProbability==-1.0)
            {   state.output.warning("GEPSpecies must have an RNC mutation probability between 0.0 and 1.0 inclusive, defaulting to 0.0",
                                   base.push(P_RNCMUTATIONPROB),def.push(P_RNCMUTATIONPROB));
            rncMutationProbability = 0.0;
            }
    	
        }

        //*************************************************************************************
        // Load the SymbolSet
        //*************************************************************************************
        Parameter ppbase = base.push(P_SYMBOLSET);
        Parameter ppdef = def.push(P_SYMBOLSET);
        // Figure the GEPSymbolSet class
        if (state.parameters.exists(ppbase) || state.parameters.exists(ppdef))
         symbolSet = (GEPSymbolSet)(state.parameters.getInstanceForParameterEq(
                                        ppbase,ppdef,GEPSymbolSet.class));
        else
        {
            state.output.warning("No GEPSymbolSet specified, assuming the default class: ec.gep.GEPSymbolSet." , ppbase, ppdef);
            symbolSet = new GEPSymbolSet();
        }
        symbolSet.setup(state, ppbase, ppdef, this);
        
        // set tail Size and gene size for genes now that we know the SymbolSet
        tailSize = headSize * (symbolSet.maxArity-1) + 1;
        geneSize = headSize + tailSize;

        state.output.exitIfErrors(); // terminate if errors in any of the above paramaters

        // get the functions and terminals for the genes
        // at some point we might want to allow a different set of functions for each gene in the genome

    
        // NOW call super.setup(...), which will in turn set up the prototypical individual
        super.setup(state,base);

        // check to make sure that our individual prototype is a GEPIndividual
        if (!(i_prototype instanceof GEPIndividual))
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of ec.gep.GEPIndividual.", base );
    } 
    
    /** Returns the symbol set associated with a given name.
    You must guarantee that after calling symbolSetFor(...) one or
    several times, you call state.output.exitIfErrors() once. */

    public GEPSymbolSet symbolSetFor(final String symbolSetName, final EvolutionState state)
    {
    	GEPSymbolSet set = null;
        if (symbolSet.name == symbolSetName)
        	    set = symbolSet;
        if (set==null)
            state.output.error("The GEP symbol set \"" + symbolSetName + "\" could not be found.");
        return set;
    }


    public Individual newIndividual(final EvolutionState state, int thread) 
        {
        GEPIndividual newind = (GEPIndividual)(i_prototype).clone();
        
        // set the genes, constants, etc. in the individual
        newind.reset( state, thread);

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;
        newind.parsedGeneExpressions = null;

        // Set the species to me
        newind.species = this;

        // ...and we're ready!
        return newind;
        }
}
