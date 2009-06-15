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


/* 
 * GEPChromosome.java
 * 
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * GEPChromosome is the main component of a GEPIndividual used for GEP evolution runs. It encodes a
 * chromosome which can have one or more genes. The genes encode a model
 * that has evolved during the evolution run (in the form of a Karva
 * expression ... details provided in the Gene Expression Programming
 * book by Ferreira). A GEPIndividual is in fact a set of 1 or more GEPChromosomes.
 * 
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approacch transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b>reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>
 *
 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the genome vector which is composed
 * one or more genes.  
 * If you add instance variables to GEPIndividual, you'll need to read/write those variables as well.

 <p><b>Default Base</b><br>
 gep.individual

 *
 * @author Bob Orchard
 * @version 1.0 
 */

public class GEPChromosome implements Cloneable
{
	/**
	 *  What GEPIndividual does the chromosome instance belong to.
	 */
	public GEPIndividual myGEPIndividual = null;
	
    /** This array holds a parse tree for each gene in the genome. The parse tree
     *  is used to evaluate the genome.
     */
    public GEPExpressionTreeNode parsedGeneExpressions[] = null;
    
    /** Each chromosome (or genome) can have one or more genes, so genome 
     *  is an array of integer arrays that hold the genes. So for example,
     *  if the genome has 3 genes and each gene had a size of 11 (head size=5 and
     *  tail size = 6, with maximum arity for the functions of 2), then we'd
     *  have an array of size [3][11].
     */
    public int genome[][];
    
    /** genomeConstants holds the constants that can be selected for each gene.  
     *  The values stored in genomeDc are indexes into this array.
     */
    public double genomeConstants [][];
    
    /** genomeDc holds the Dc part of the each gene (same size as tail of the gene).
     *  The values in this array are indexes into the corresponding gene of the 
     *  genomeConstants array (where the actual constants are stored).
     */
    public int genomeDc [][];
    
    public boolean equals(Object ind)
    {
    	int j;
    	
        if (!(this.getClass().equals(ind.getClass()))) return false; // GEPChromosomes are special.
        GEPChromosome i = (GEPChromosome)ind;
        if( genome.length != i.genome.length)
            return false;
        for( int k=0 ; k<genome.length ; k++ )
        {	// must have same points in the genes
            for( j=0 ; j<genome[k].length ; j++ )
                if (genome[k][j] != i.genome[k][j])
                    return false;
            // and same contents in each gene's Dc area
            for( j=0 ; j<genomeDc[k].length ; j++ )
                if (genomeDc[k][j] != i.genomeDc[k][j])
                    return false;
            // and same constants in each gene's constant array
            for( j=0 ; j<genomeConstants[k].length ; j++ )
                if (genomeConstants[k][j] != i.genomeConstants[k][j])
                    return false;
        }
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
        for (int i=0; i<genome.length; i++)
           for (int j=0; j<genome[i].length; j++)
               hash = ( hash << 1 | hash >>> 31 ) ^ genome[i][j];

        return hash;
    }
    
    /** Sets up a prototypical GEPIndividual with those features which it
        shares with other GEPIndividuals in its species, and nothing more. */

    public void setup(final EvolutionState state, final Parameter base, GEPSpecies s)
    {
        // Clear the parsedGeneExpressions
        parsedGeneExpressions = null;

        // allocate space for the genes in the genome
	    genome = new int[s.numberOfGenes][];
	    for (int i=0; i<s.numberOfGenes; i++)
	    	genome[i] = new int[s.geneSize];
	    
	    // allocate space related to constants in genes ... if using constants
	    if (s.useConstants)
	    {
	        // allocate space for the constant references in each gene in the genome
		    genomeDc = new int[s.numberOfGenes][];
		    for (int i=0; i<s.numberOfGenes; i++)
		    	genomeDc[i] = new int[s.tailSize];
	        // allocate space for the actual constant associated with each gene in the genome
		    genomeConstants = new double[s.numberOfGenes][];
		    for (int i=0; i<s.numberOfGenes; i++)
		    	genomeConstants[i] = new double[s.numberOfConstantsPerGene];
	    }
	    else
	    {
	    	genomeDc = null;
	    	genomeConstants = null;
	    }
    }
    

    public double getRandomFromLowerToUpper(MersenneTwisterFast rand, double lowerLimit, double upperLimit)
    {
    	// !!!! should try to find a solution that includes the upper limit since users may
    	// !!!! expect the upper limit to appear in the solutions
    	double r = rand.nextDouble(); // from [0.0, 1.0)
    	return lowerLimit + r*(upperLimit - lowerLimit);
    }


    /** Initializes the individual by randomly choosing terminals, constants and functions.
     *  Used to create the initial population. 
     */
    public void reset(EvolutionState state, int thread)
    {
    	int i, j;
    	
        GEPSpecies s = (GEPSpecies) myGEPIndividual.species;
        for (i=0; i<genome.length; i++) // for each gene in the genome
        {   int gene[] = genome[i];
            GEPSymbolSet symbolset = s.symbolSet;
            
            // As per Ferreira, for the initial population the 1st element 
            // in the head of a gene is a function symbol
            gene[0] = symbolset.chooseFunctionSymbol(state, thread);
            
    	    // the rest are chosen according to being in the head or in the tail of the gene
    	    // Ferreria indicated that the rule of thumb for choosing symbols for the head is:
    	    //   if number of functions < number of terminals 
    	    //   then prob of selecting a function is 2/3
    	    // BUT
    	    //   if number of functions >= number of terminals
    	    //   then equal weighting for functions and terminals
    	    // 
    	    // In the tail just the weights of the terminals (only in GeneXpro there are no 
    	    // so they all have the same weight); in this implementation we can assign weights 
    	    // to the terminals
        	for (j=1; j<gene.length; j++)    
        		gene[j] = symbolset.chooseFunctionOrTerminalSymbol(state, thread, j, s);
        }
        
        // if using constants then we allocate the constants and generate constant
        // references in the dc area.
        if (s.useConstants)
        {
        	MersenneTwisterFast rand = state.random[thread];
            for (i=0; i<genome.length; i++) // for each gene in the genome
            {
            	double constants[] = genomeConstants[i];
            	int constantPoints[] = genomeDc[i];
            	int constantsLen = constants.length;
            	// create the array of constants
            	if (s.integerConstants)
            	{   int range = (int)(s.constantsUpperLimit-s.constantsLowerLimit+1);
            		for (j=0; j<constantsLen; j++)
            		    constants[j] = rand.nextInt(range) + s.constantsLowerLimit;
            	}
            	else
            		for (j=0; j<constantsLen; j++)
            		    constants[j] = getRandomFromLowerToUpper(rand, s.constantsLowerLimit, s.constantsUpperLimit);
            	// create the values in Dc of the gene
            	for (j=0; j<constantPoints.length; j++)
            		constantPoints[j] = rand.nextInt(constantsLen);
            }
        }
        
    	parsedGeneExpressions = null;
    }
    
    public String genotypeToStringForHumans()
    {
			String s = "Linking function: " + ((GEPSpecies)myGEPIndividual.species).linkingFunctionName + "\n";
			s = s + genotypeToStringForHumansKarva();
			s = s + "\n" + genotypeToStringForHumansMathExpression();
			s = s + "\n";
			return s;
    }
        
    public String genotypeToStringForHumansKarva()
    {
        try {
			String s = "";
			for( int i=0 ; i<genome.length ; i++ )
			{   s = s + "Gene " + i + "\n";
			    int constantIndex = 0;
			    GEPSpecies species = (GEPSpecies)this.myGEPIndividual.species;
			    for (int j=0; j<genome[i].length; j++)
			    {	String thePrintableSymbol;
			        GEPSymbol sym = species.symbolSet.symbols[genome[i][j]];
			        // the gene could have more than genomeDc[i].length constants
			        // in it BUT the extras could never be used in an expression so
			        // for those print C?
			        if (sym instanceof GEPConstantTerminalSymbol)
			        	thePrintableSymbol = "C" + 
						    ((constantIndex < genomeDc[i].length) ? String.valueOf(genomeDc[i][constantIndex++]) : "?");
			        else
			        	thePrintableSymbol = sym.symbol;
			        s = s + (j==0 ? "" : ".") + thePrintableSymbol;
			    }
			    // the constants if any
			    if (species.useConstants)
			    {   s = s + "\n";
			    	for (int k=0; k<genomeConstants[i].length; k++)
			    		s = s + "C" + k + ": " + genomeConstants[i][k] + "\n";
			    }
			    else 
			    	s = s + "\n";
			}
			return s;
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
    }
    
    public String genotypeToStringForHumansMathExpression()
    {
    	String s = "";
        if (parsedGeneExpressions == null)
        	parseGenes();
        int numExpressions = parsedGeneExpressions.length;
        String expressions[] = new String[numExpressions];
    	for (int i=0; i< numExpressions; i++)
    	{
    		expressions[i]= nodeToStringMathExpr(parsedGeneExpressions[i]);
    	}
		s = expressions[0];
	    GEPSpecies species = (GEPSpecies)this.myGEPIndividual.species;
	    GEPFunctionSymbol fs = species.linkingFunctionSymbol;
	    int arity = fs.arity;
	    String params[] = new String[arity];
    	for (int i=1; i< numExpressions; )
    	{
    		params[0] = s;
    		for (int j=1; j<arity; j++)
    			params[j] = expressions[i++];
    		s = fs.getMathExpressionAsString(params);
    	}

    	return s;
    }
    
    private String nodeToStringMathExpr(GEPExpressionTreeNode exprNode)
    {
    	String s;
    	if (exprNode.isConstantNode)
    		if (exprNode.constantValue<0)
    			return "(" + Double.toString(exprNode.constantValue) + ")";
    		else
    			return Double.toString(exprNode.constantValue);
    	if (exprNode.symbol instanceof GEPTerminalSymbol)
    	{
        	GEPTerminalSymbol ts = (GEPTerminalSymbol)(exprNode.symbol);
    		return ts.symbol;
    	}
    	// must be a function
    	GEPFunctionSymbol fs = (GEPFunctionSymbol)(exprNode.symbol);
    	if (fs.arity == 0)
    		return fs.symbol;
    	// Each function knows how to print if you provide the parameters as strings
    	int numParams = exprNode.numParameters;
    	String params[] = new String[numParams];
    	for (int i=0; i<exprNode.numParameters; i++)
        	params[i] = nodeToStringMathExpr(exprNode.parameters[i]);
    	s = fs.getMathExpressionAsString(params);

    	return s;
    }
        
    public String genotypeToString()
    {
        StringBuffer s = new StringBuffer();
        s.append( Code.encode( genome.length ) );
        s.append( Code.encode( genome[0].length ) );
        for ( int i=0 ; i<genome.length; i++ )
            for (int j=0; j<genome[i].length; j++)
                s.append( Code.encode( genome[i][j] ) );
        s.append( Code.encode( genomeDc.length ) );
        s.append( Code.encode( genomeDc[0].length ) );
        for ( int i=0 ; i<genomeDc.length; i++ )
            for (int j=0; j<genomeDc[i].length; j++)
                s.append( Code.encode( genomeDc[i][j] ) );
        s.append( Code.encode( genomeConstants.length ) );
        s.append( Code.encode( genomeConstants[0].length ) );
        for ( int i=0 ; i<genomeConstants.length; i++ )
            for (int j=0; j<genomeConstants[i].length; j++)
                s.append( Code.encode( genomeConstants[i][j] ) );
        return s.toString();
    }
                
    protected void parseGenotype(final EvolutionState state,
                                 final LineNumberReader reader) throws IOException
    {
        // The first item is the number of genes
	// and the second is the length of each gene
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode( d );
        int lll = (int)(d.l);
        Code.decode( d );
        int mmm = (int)(d.l);

        genome = new int[lll][mmm];
        // read in the genes
        for( int i=0 ; i<genome.length ; i++ )
            for (int j=0; j<genome[i].length; j++)
            {
              Code.decode( d );
              genome[i][j] = (int)(d.l);
            }
        
        lll = (int)(d.l);
        Code.decode( d );
        mmm = (int)(d.l);
        genomeDc = new int[lll][mmm];

        // read in the gene Dc areas
        for( int i=0 ; i<genomeDc.length ; i++ )
            for (int j=0; j<genomeDc[i].length; j++)
            {
              Code.decode( d );
              genomeDc[i][j] = (int)(d.l);
            }
        
        lll = (int)(d.l);
        Code.decode( d );
        mmm = (int)(d.l);
        genomeConstants = new double[lll][mmm];

        // read in the gene Dc areas
        for( int i=0 ; i<genomeConstants.length ; i++ )
            for (int j=0; j<genomeConstants[i].length; j++)
            {
              Code.decode( d );
              genomeConstants[i][j] = (double)(d.l);
            }
    }

        
    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(genome.length);
        dataOutput.writeInt(genome[0].length);
        for(int i=0; i<genome.length; i++)
            for(int j=0; j<genome[i].length; j++)
                dataOutput.writeInt(genome[i][j]);
        dataOutput.writeInt(genomeDc.length);
        dataOutput.writeInt(genomeDc[0].length);
        for(int i=0; i<genomeDc.length; i++)
            for(int j=0; j<genomeDc[i].length; j++)
                dataOutput.writeInt(genomeDc[i][j]);
        dataOutput.writeInt(genomeConstants.length);
        dataOutput.writeInt(genomeConstants[0].length);
        for(int i=0; i<genomeConstants.length; i++)
            for(int j=0; j<genomeConstants[i].length; j++)
                dataOutput.writeDouble(genomeConstants[i][j]);
        }

    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
    {
        int numGenes = dataInput.readInt();
        int geneLength = dataInput.readInt();
        if (genome==null || genome.length != numGenes || genome[0].length != geneLength)
            genome = new int[numGenes][geneLength];

        for(int i=0; i<genome.length; i++)
            for(int j=0; j<genome[i].length; j++)
                genome[i][j] = dataInput.readInt();
        
        int numDcs = dataInput.readInt();
        int DcLength = dataInput.readInt();
        if (genomeDc==null || genomeDc.length != numDcs || genomeDc[0].length != DcLength)
            genomeDc = new int[numDcs][DcLength];

        for(int i=0; i<genomeDc.length; i++)
            for(int j=0; j<genomeDc[i].length; j++)
                genomeDc[i][j] = dataInput.readInt();
        
        int numGeneConstants = dataInput.readInt();
        int constantsLength = dataInput.readInt();
        if (genomeConstants==null || genomeConstants.length != numGeneConstants || genomeConstants[0].length != constantsLength)
        	genomeConstants = new double[numGeneConstants][constantsLength];

        for(int i=0; i<genomeConstants.length; i++)
            for(int j=0; j<genomeConstants[i].length; j++)
            	genomeConstants[i][j] = dataInput.readDouble();
    }

	
    public Object clone()
    {
        try 
        { 
	    	GEPChromosome myobj = (GEPChromosome) (super.clone());
	
	        // must clone the genome
	        myobj.genome = new int[genome.length][];
	        for (int i=0; i<genome.length; i++)
	        {
	        	myobj.genome[i] = new int[genome[i].length];
	        	for (int j=0; j<genome[i].length; j++)
	        		myobj.genome[i][j] = genome[i][j];
	        }
	        
	        // and the constants information if using constants
	        if (genomeConstants != null)
	        {
		        myobj.genomeConstants = new double[genomeConstants.length][];
		        myobj.genomeDc = new int[genomeDc.length][];
		        for (int i=0; i<genome.length; i++)
		        {
		        	myobj.genomeDc[i] = new int[genomeDc[i].length];
		        	for (int j=0; j<genomeDc[i].length; j++)
		        		myobj.genomeDc[i][j] = genomeDc[i][j];
		        	myobj.genomeConstants[i] = new double[genomeConstants[i].length];
		        	for (int j=0; j<genomeConstants[i].length; j++)
		        		myobj.genomeConstants[i][j] = genomeConstants[i][j];
		        }
	        }
	        else
	        {
		        myobj.genomeConstants = null;
		        myobj.genomeDc = null;
	        }
	        
	        // and the parsedGeneExpressions
	        if (parsedGeneExpressions == null)
	        	myobj.parsedGeneExpressions = null;
	        else
	        {
	        	myobj.parsedGeneExpressions = new GEPExpressionTreeNode[parsedGeneExpressions.length];
	        	for (int i=0; i< parsedGeneExpressions.length; i++)
	        		    myobj.parsedGeneExpressions[i] = (GEPExpressionTreeNode)parsedGeneExpressions[i].clone();
	        }
	        
	        return myobj;
        }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens??
    } 

    /** 
     * For each gene in the genome create an expression tree from the Karva expression
     * encoded in the gene. The expression tree is used to evaluate the expression 
     * for particular values of the terminals (independent variables). 
     *
     */
    public void parseGenes()
    {
	    GEPSpecies species = (GEPSpecies)this.myGEPIndividual.species;
    	boolean hasConstants = species.useConstants;
    	parsedGeneExpressions = new GEPExpressionTreeNode[genome.length];
    	for (int i=0; i<genome.length; i++)
    	{   // parse each gene into an expression tree
    		if (hasConstants)
        		parsedGeneExpressions[i] = parseGeneWithConstants(genome[i], genomeDc[i], 
        				genomeConstants[i], species.symbolSet);
    		else
    		    parsedGeneExpressions[i] = parseGene(genome[i], species.symbolSet);
    	}
    }
    
    /**
     * Create an expression tree for a particular gene in the genome. 
     * In this case the gene does NOT use constants.
     * 
     * @param gene the gene that holds the Karva expression
     * @param ss the GEPSymbolSet that holds the function and terminal 
     *        symbols that are referenced by id in the gene.
     * @return the head of the expression tree for the parsed expression
     */
    public GEPExpressionTreeNode parseGene(int gene[], GEPSymbolSet ss)
    {
        LinkedList nodeQ = new LinkedList();
        LinkedList parentNodeQ = new LinkedList();
        GEPSymbol symbols[] = ss.symbols;
        int nextGeneIndex = 0;
        int nextGeneElt;
        GEPExpressionTreeNode rootNode, parentNode;
        int i;
        
        nextGeneElt = gene[nextGeneIndex++];
        GEPSymbol s = symbols[nextGeneElt];
        rootNode = new GEPExpressionTreeNode(s);
        
        for (i=0; i<s.arity; i++)
        {   // queue the parameters of this node
        	nodeQ.addLast(new GEPExpressionTreeNode(symbols[gene[nextGeneIndex++]]));
        	parentNodeQ.addLast(rootNode);
        }
        
        // while queue still has some elements process nodes
        while (!nodeQ.isEmpty())
        {
        	GEPExpressionTreeNode node = (GEPExpressionTreeNode)nodeQ.removeFirst();
        	parentNode = (GEPExpressionTreeNode)parentNodeQ.removeFirst();
            parentNode.addParameter(node);
            int arity = node.symbol.arity;
            for (i=0; i<arity; i++)
            {   // queue the parameters of this node
            	nodeQ.addLast(new GEPExpressionTreeNode(symbols[gene[nextGeneIndex++]]));
            	parentNodeQ.addLast(node);
            }
        }

        return rootNode;
    }
    
    /**
     * Create an expression tree for a particular gene in the genome. 
     * In this case the gene DOES use constants.
     * 
     * @param gene the gene that holds the Karva expression.
     * @param geneDc the array with the constant part of the gene.
     * @param geneConstants the array with the actual constants referenced in the geneDc array 
     *        (by index into the geneConstants array).
     * @param ss the GEPSymbolSet that holds the function and terminal 
     *        symbols that are referenced by id in the gene.
     * @return the head of the expression tree for the parsed expression.
     */
    public GEPExpressionTreeNode parseGeneWithConstants(int gene[], int geneDc[], double geneConstants[], GEPSymbolSet ss)
    {
        LinkedList nodeQ = new LinkedList();
        LinkedList parentNodeQ = new LinkedList();
        GEPSymbol symbols[] = ss.symbols;
        int nextGeneIndex = 0;
        int nextGeneElt;
        GEPExpressionTreeNode rootNode, parentNode;
        int i;
        int constantIndex = 0;
        GEPSymbol s, s1;
        
        nextGeneElt = gene[nextGeneIndex++];
        s = symbols[nextGeneElt];
        if (s instanceof GEPConstantTerminalSymbol)
        	rootNode = new GEPExpressionTreeNode(s, geneConstants[geneDc[constantIndex++]]);
        else
        	rootNode = new GEPExpressionTreeNode(s);
 
        for (i=0; i<s.arity; i++)
        {   // queue the parameters of this symbol
            s1 = symbols[gene[nextGeneIndex++]];
            if (s1 instanceof GEPConstantTerminalSymbol)
            	// a constant, so add the constant value to the tree node
            	nodeQ.addLast(new GEPExpressionTreeNode(s1, geneConstants[geneDc[constantIndex++]]));
            else
        	    nodeQ.addLast(new GEPExpressionTreeNode(s1));
        	parentNodeQ.addLast(rootNode);
        }
        
        // while queue still has some elements process nodes
        while (!nodeQ.isEmpty())
        {
        	GEPExpressionTreeNode node = (GEPExpressionTreeNode)nodeQ.removeFirst();
        	parentNode = (GEPExpressionTreeNode)parentNodeQ.removeFirst();
            parentNode.addParameter(node);
            int arity = node.symbol.arity;
            for (i=0; i<arity; i++)
            {   // queue the parameters of this node
                s1 = symbols[gene[nextGeneIndex++]];
                if (s1 instanceof GEPConstantTerminalSymbol)
                	// a constant, so add the constant value to the tree node
                	nodeQ.addLast(new GEPExpressionTreeNode(s1, geneConstants[geneDc[constantIndex++]]));
                else
                	nodeQ.addLast(new GEPExpressionTreeNode(s1));
            	parentNodeQ.addLast(node);
            }
        }
        return rootNode;
    }
    
    /**
     * Evaluate the expressions encoded for each gene in this individual using values that are held
     * in the terminal symbols (independent variables) referenced in the expression. 
     * The parameter valueIndex identifies which value to use of the array of values stored 
     * in the terminal symbols. The terminal symbols are populated with values when the
     * problem is initialized (from a file with the test data or by askng the user problem
     * to supply the values). 
     * <br>
     * <br>
     * If the encoded gene expressions have not been parsed then parse them before attempting
     * to do the evaluation.
     * <br>
     * <br>
     * If there are multiple genes then combine the values of each gene expression using the specified
     * as the linking function.
     * <br>
     * <br>
     * If there is an undefined result for the expression then try with arbitrary precision arithmetic
     * to see if it can be resolved.
     * 
	 * @param useTrainingData if true use Training data else use Testing data
     * @param valueIndex an index that specifies which value to use in each terminal in the expression.
     * @return the value of the expression for the individual.
     */
    public double eval(boolean useTrainingData, int valueIndex)
    {
    	// parse the gene expressions if necessary
    	if (parsedGeneExpressions == null)
    		parseGenes();
    	// evaluate the gene's expressions using the value at 'valueIndex' position in the terminal symbols
    	// and combine the gene results using the specified linking function
    	double result = parsedGeneExpressions[0].eval(useTrainingData, valueIndex);
    	if (Double.isNaN(result))
    		return result;
    	if (genome.length == 1)
    		return result;
    	
	    GEPSpecies species = (GEPSpecies)this.myGEPIndividual.species;
    	GEPFunctionSymbol fs = species.linkingFunctionSymbol; 
    	int functionArity = fs.arity;
    	double params[] = new double[functionArity];
    	for (int i=1; i<genome.length; )
    	{
    		params[0] = result;
    		for (int j=1; j<functionArity; j++)
    			params[j] = parsedGeneExpressions[i++].eval(useTrainingData, valueIndex);
    		result = fs.eval(params);
        	if (Double.isNaN(result))
        		return result;
    	}
        if (GEPIndividual.isThresholdON()) // classification problem -- expect dep var values to be 0 or 1
        	result = (result >= GEPIndividual.getThreshold()) ? 1 : 0;

        return result;
    }
    
     
    /** Returns the "size" of the chromosome, namely, the number of nodes
     *  in all of its parsed genes -- does not include the linking functions.  
     */
    public long size()
    {
        long size = 0;
        // if parsedGeneExpressions is null then must parse the expression before
        // we can determine its size
        if (parsedGeneExpressions == null)
        	parseGenes();
    	for (int i=0; i< parsedGeneExpressions.length; i++)
    		size += parsedGeneExpressions[i].numberOfNodes();

        return size;
    }

    /** Calculates the number of times each independent variable is used in
     *  the expression tree from this node down (all it subnodes/parameters).
     *  So if this is the root of the expression it is how many times the variable
     *  is used in the expression. The indices of the array returned are relative
     *  to the beginning of the first integer id assigned for the terminals. that
     *  is index 0 in the array refers to the count of the 1st variable in the list of
     *  terminals.
     *  
     *  @return an array with the count of variable usage for each variable
     */
    public int[] variableUseageCounts()
    {
	    GEPSpecies species = (GEPSpecies)this.myGEPIndividual.species;
        int counts[] = new int[species.symbolSet.numberOfTerminals];
        // if parsedGeneExpressions is null then must parse the expression before
        // we can determine its size
        if (parsedGeneExpressions == null)
        	parseGenes();
    	for (int i=0; i< parsedGeneExpressions.length; i++)
    		parsedGeneExpressions[i].variableUseageCounts(counts);

        return counts;
    }

    /** Calculates the number of times each function is used in
     *  the expression tree from the root node down (all it subnodes/parameters).
     *  The return value is a hash map with the function names as hash values 
     *  and a count for each time the function was referenced in the expression.
     *  
     *  @return a hash map with the function names as hash values 
     *  and a count for each time the function was referenced in the expression.
     */
    public HashMap functionUseageCounts()
    {
        HashMap counts = new HashMap();
        // if parsedGeneExpressions is null then must parse the expression before
        // we can determine its size
        if (parsedGeneExpressions == null)
        	parseGenes();
    	for (int i=0; i< parsedGeneExpressions.length; i++)
    		parsedGeneExpressions[i].functionUseageCounts(counts);

        return counts;
    }

}
