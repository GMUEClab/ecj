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
import ec.gp.GPNode;
import ec.util.*;
import java.io.*;
import java.util.*;
import jscl.math.Expression;
import jscl.text.ParseException;

/* 
 * GEPIndividual.java
 * 
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * GEPIndividual is an Individual used for GEP evolution runs. 
 * It encodes an individual that can have multiple chromosomes and each
 * chromosome can have one or more genes. The genes encode a model
 * that has evolved during the evolution run (in the form of a Karva
 * expression ... details provided in the Gene Expression Programming
 * book by Ferreira).
 * 
  
 <p><b>Default Base</b><br>
 gep.individual

 *
 * @author Bob Orchard
 * @version 1.0 
 */

public class GEPIndividual extends Individual
{
    public static final String P_INDIVIDUAL = "individual";
    public static final int CHECK_BOUNDARY = 8;
    public static final String P_SIMPLIFY_EXPRESSIONS = "simplify-expressions";
    
    /**
     * Each GEP Individual can have 1 or more GEPchromosomes each of which
     * can have 1 or more genes. This is an extension of the Ferreira system to allow
     * support for vector functions (i.e. systems with the same independent variable but multiple
     * dependent variable. With 1 dependent variable it is the same as Ferreira's system. 
     * When more than 1 chromosome is present then special fitness functions will be required as well.
     */
    public GEPChromosome chromosomes[] = null;
        
    /** This flag indicates if the expression for the chromosomes in this individual have been 
     * parsed or not.
     */
    public boolean chromosomesParsed = false;

	/** 
	 * Should we print simplified expressions as well as the generated expression in genoTypeToStringForHumans?
	 * 
	 */
    static boolean simplifyExpressions = true;


	/**
	 * When the problem is a classification problem AND there is only 1 chromosome
	 * then the evaluated expressions must have their results converted to a 0 or a 1. 
	 * This is the standard Ferreira classifications. Then the threshold is used to 
	 * make that determination. If the value is >= threshold then 1 else 0.
	 * The threshold can be set with the method:
	 * <code>
	 * 
	 * 		GEPFitnessFunction.setThreshold( thresholdValue );
	 * 
	 * </code>
	 * This also turns on the use of the threshold values when it is set.
	 * Use of the threshold value can be turned off with the method:
	 * <code>
	 * 
	 * 		GEPFitnessFunction.setThresholdOFF();
	 * 
	 * </code>
	 * 
	 */
    
    
	private static double threshold = 0.5;
	
	/**
	 * When the problem is a classification problem then the evaluated expressions
	 * must have their results converted to a 0 or a 1 when a fitness is being calcualated. 
	 * The threshold is used to make that determination. If the value is >= threshold 
	 * then the value is set to 1 else it is set to 0.
	 * <li>
	 * The threshold can be set with the method:
	 * <code>
	 * 
	 * 		GEPFitnessFunction.setThreshold( thresholdValue );
	 * 
	 * </code>
	 * This also turns on the use of the threshold values when it is set.
	 * Use of the threshold value can be turned off with the method:
	 * <code>
	 * 
	 * 		GEPFitnessFunction.setThresholdOFF();
	 * 
	 * </code>
	 */
	private static boolean thresholdON = false;
	
	
	/**
	 * Turns on the use of the threshold value and assigns new threshold value.
	 * @param t the new threshold value
	 */
	public static void setThreshold( double t )
	{
		threshold = t;
		thresholdON = true;
	}

	/**
	 * Provides the current threshold value.
	 * @return the current threshold value
	 */
	public static double getThreshold( )
	{
		return threshold;
	}

	/**
	 * Turns off the use of the threshold value when calculating fitness values.
	 */
	public static void setThresholdOFF( )
	{
		thresholdON = false;
	}

	/**
	 * Turns on the use of the threshold value when calculating fitness values.
	 */
	public static void setThresholdON( )
	{
		thresholdON = true;
	}

	/**
	 * Indicates whether or not the threshold value is being used for 
	 * fitness calculations.
	 * @return true if threshold use in ON, else false.
	 */
	public static boolean isThresholdON( )
	{
		return thresholdON;
	}


    public Parameter defaultBase()
    {
        return GEPDefaults.base().push(P_INDIVIDUAL);
    }

    public boolean equals(Object ind)
    {
    	int j;
    	
        if (!(this.getClass().equals(ind.getClass()))) return false; // GEPIndividuals are special.
        GEPIndividual i = (GEPIndividual)ind;
        // each chromosome must be equal and the number of chromosome must be the same as well.
        if (chromosomes == null && i.chromosomes != null) return false;
        if (i.chromosomes == null && chromosomes != null) return false;
        if (chromosomes == null && i.chromosomes == null) return true;
        if (chromosomes.length != i.chromosomes.length) 
        	return false;
        for (j=0; j<chromosomes.length; j++)
        	if (!(chromosomes[j].equals(i.chromosomes[j])))
        		return false;
        
        return true;
    }
    
    public int hashCode()
    {
        // stolen from GPIndividual.  It's a decent algorithm.
        int hash = this.getClass().hashCode();

        // seems excessively long calculation to me ... maybe should just use
        // 1st 3 or 4 of each gene. They will generally all be small integers
        // (e.g. with 2 terminals and 4 operators only 6 small +ve values in the array ...
        // this will lead to very few non-zero bits in the integers and possibly poor hash codes
        hash = ( hash << 1 | hash >>> 31 );
        for (int i=0; i<chromosomes.length; i++)
           hash = ( hash << 1 | hash >>> 31 ) ^ (chromosomes[i].hashCode());

        return hash;
    }
    
    /** Sets up a prototypical GEPIndividual with those features which it
        shares with other GEPIndividuals in its species, and nothing more. */

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        Parameter def = defaultBase();

        // set my evaluation to false and indicate chromosomes not parsed yet
        evaluated = false;
        chromosomesParsed = false;

	    // make sure we are associated with the correct species
        if (!(species instanceof GEPSpecies)) 
            state.output.fatal("GEPIndividual must be associated with a GEPSpecies", base, def);
        GEPSpecies s = (GEPSpecies) species;
        
        simplifyExpressions = state.parameters.getBoolean(base.push(P_SIMPLIFY_EXPRESSIONS),
        		base.push(P_SIMPLIFY_EXPRESSIONS), true);

                
        // allocate space for the chromosomes
	    chromosomes = new GEPChromosome[s.numberOfChromosomes];
	    for (int i=0; i<s.numberOfChromosomes; i++)
	    {
	    	chromosomes[i] = new GEPChromosome();
	    	chromosomes[i].setup(state, base, s);
	    }
    }
    
    /** Initializes the individual by randomly choosing terminals, constants and functions.
     *  Used to create the initial population. 
     */
    public void reset(EvolutionState state, int thread)
    {
        for (int i=0; i<chromosomes.length; i++) // for each gene in the genome
            chromosomes[i].reset(state, thread);
            
     	evaluated = false;
    	chromosomesParsed = false;
    }
    
    public String genotypeToStringForHumans()
    {
			String s = "Linking function: " + ((GEPSpecies)species).linkingFunctionName + "\n";
			s = s + "\nGEP-MODEL\n";
			s = s + "KARVA\n" + genotypeToStringForHumansKarva();
			String mathExpression = genotypeToStringForHumansMathExpression();
			s = s + "MATH\n" + mathExpression;
			if (simplifyExpressions)
			{
				String mathSimplifiedExpression = genotypeToStringForHumansMathSimplifiedExpression();
				s = s + "\nMATH (SIMPLIFIED)\n" + mathSimplifiedExpression;
			}			
			s = s + "\n";
			return s;
    }
        
    public String genotypeToStringForHumansKarva()
    {
        try 
        {
        	int numChromosomes = chromosomes.length;
        	String s = "";
			if (numChromosomes > 1)
				s = "Number of chromosomes: " + numChromosomes + "\n\n";
			for( int i=0 ; i<numChromosomes ; i++ )
			{
				int j = i+1;
				if (numChromosomes > 1)
					s = s + "Chromosome " + j + ":\n";
			    s = s + chromosomes[i].genotypeToStringForHumansKarva() + "\n";
			}
			return s;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return "";
		}
    }
    
    public String genotypeToStringForHumansMathExpression()
    {
    	int numChromosomes = chromosomes.length;
    	String s = "";
		if (numChromosomes > 1)
			s = "Number of chromosomes: " + numChromosomes + "\n\n";
		for( int i=0 ; i<numChromosomes ; i++ )
		{
			int j = i+1;
		    if (numChromosomes > 1)
			    s = s + "Chromosome " + j + ":\n";
		    s = s + chromosomes[i].genotypeToStringForHumansMathExpression() + "\n";
		}
		return s;
    }
            
    public String genotypeToStringForHumansMathSimplifiedExpression()
    {
    	int numChromosomes = chromosomes.length;
    	String s = "";
    	String simplified = "";
		if (numChromosomes > 1)
			s = "Number of chromosomes: " + numChromosomes + "\n\n";
		for( int i=0 ; i<numChromosomes ; i++ )
		{
			int j = i+1;
		    if (numChromosomes > 1)
			    s = s + "Chromosome " + j + ":\n";
			try
			{
				if (simplifyExpressions)
				   simplified = Expression.valueOf(chromosomes[i].genotypeToStringForHumansMathExpression()).simplify().toString();
			}
			catch (Exception e)
			{
				s = s + "\nMATH (SIMPLIFIED)\nUnable to simplify the math expression ... jscl.meditor simplify failed";
			}
		    s = s + simplified + "\n";
		}
		return s;
    }
            
    public String genotypeToString()
    {
    	int numChromosomes = chromosomes.length;
		String s = Code.encode( numChromosomes );
		for( int i=0 ; i<numChromosomes ; i++ )
		    s = s + chromosomes[i].genotypeToString();

		return s;
    }
                
    protected void parseGenotype(final EvolutionState state,
                                 final LineNumberReader reader) throws IOException
    {
        // The first item is the number of chromosomes
    	// and then each gene
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode( d );
        int lll = (int)(d.l);
        chromosomes = new GEPChromosome[lll];
         for( int i=0 ; i<lll ; i++ )
         {
        	 chromosomes[i] = new GEPChromosome();
        	 chromosomes[i].parseGenotype(state, reader);
         }
         chromosomesParsed = false;
         evaluated = false;
    }

        
    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
    {
    	int numChromosomes = chromosomes.length;
    	dataOutput.writeInt(numChromosomes);
		for( int i=0 ; i<numChromosomes ; i++ )
			chromosomes[i].writeGenotype(state, dataOutput);
    }

    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
    {
        int numChromosomes = dataInput.readInt();
		for( int i=0 ; i<numChromosomes ; i++ )
			chromosomes[i].readGenotype(state, dataInput);
    }

	
    public Object clone()
    {
        GEPIndividual myobj = (GEPIndividual) (super.clone());

        // must clone the chromosomes
        myobj.chromosomes = new GEPChromosome[chromosomes.length];
        for (int i=0; i<chromosomes.length; i++)
        {
        	myobj.chromosomes[i] = (GEPChromosome)(chromosomes[i].clone());
        	myobj.chromosomes[i].myGEPIndividual = myobj;
        }
        
        myobj.chromosomesParsed = chromosomesParsed;
        
        return myobj;
    } 

    
    /**
     * Evaluate the expressions for the first chromosome (normally the only chromosome)
     * 
	 * @param useTrainingData if true use Training data else use Testing data
     * @param valueIndex an index that specifies which value to use in each terminal in the expression.
     * @return the value of the expression for the individual.
     */
    public double eval(boolean useTrainingData, int valueIndex)
    {
    	return chromosomes[0].eval(useTrainingData, valueIndex);
    }
    
    /**
     * Evaluate the expressions for the specified chromosome in the set of chromosomes for the individual
     * 
     * @param chromosome which of the chromosomes to evaluate
	 * @param useTrainingData if true use Training data else use Testing data
     * @param valueIndex an index that specifies which value to use in each terminal in the expression.
     * @return the value of the expression for the individual.
     */
    public double eval(int chromosome, boolean useTrainingData, int valueIndex)
    {
    	return chromosomes[chromosome].eval(useTrainingData, valueIndex);
    }
    
    /** Returns the "size" of the individual, namely, the number of nodes
     *  in all of its parsed genes in all of its chromosomes -- does not include the linking functions.  
     */
    public long size()
    {
        long size = 0;
        // if parsedGeneExpressions is null then must parse the expression before
        // we can determine its size
    	for (int i=0; i< chromosomes.length; i++)
    		size += chromosomes[i].size();

        return size;
    }

    
}
