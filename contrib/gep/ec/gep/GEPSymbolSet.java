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
import java.io.*;
import java.nio.charset.Charset;

import ec.*;
import ec.util.*;

import java.util.*;
import java.util.zip.GZIPInputStream;

import com.csvreader.*;

/* 
 * GEPSymbolSet.java
 * 
 * Created: Nov. 9, 2006
 * By: Bob Orchard
 */

/**
 * GEPSymbolSet represents a set of GEPSymbol prototypes
 * (functions and terminals) for forming valid expressions in individuals.
 * GEPSymbolSets store their GEPSymbol Prototypes (GEPFunction or GEPTerminal) in 
 * an array and the index represents the id used in the gep expression.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of symbol set.  Must be different from other symbol set instances)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of GEPSymbols in the symbol set)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>symbol.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != ec.gp.GEPSymbol</font></td>
 <td valign=top>(class of symbol (function or terminal) node <i>n</i> in the set)</td></tr>

 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>symbol.</tt><i>n</i></td>
 <td>symbol <i>n</i></td></tr>
 </table>
 
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class GEPSymbolSet implements Clique
{
    public final static String P_SPECIES = "species";   	
    public final static String P_SYMBOLSET = "symbolset";   	
    public final static String P_NAME = "name";
    public final static String P_SYMBOL = "symbol";
    public final static String P_FUNCTION = "function";
    public final static String P_FUNCTIONSIZE = "functionsize";
    public final static String P_FUNCTIONWEIGHT = "weight";   
    public final static String P_TERMINAL = "terminal";
    public final static String P_TERMINALSIZE = "terminalsize";
    public final static String P_TERMINALFILENAME = "terminalfilename";
    public final static String P_TESTINGTERMINALFILENAME = "testingdatafilename";
    public final static String P_TERMINALFILESEPARATOR = "terminalfileseparator";
    public static final int CHECK_BOUNDARY = 8;
    
    public final static String LOCATION_OF_FUNCTION_CLASSES = "ec.gep.symbols"; 

    /** Name of the GEPSymbolSet */
    public String name;
    /** Size of this symbol set (number of symbols defined (functions and terminals) */
    public int numberOfSymbols;
    /** Maximum arity of this functions/terminals in this symbol set */
    public int maxArity;

    /** The symbols (functions/terminals) that our gep expression can use.
     *  The index into this array is the 'id' stored in the genes of individuals
     *  that use this symbol set */
    public GEPSymbol symbols[];
    /** The terminals that our gep expression can use. This array holds indexes into the 
     * symbols array. */
    public int terminals[];
    /** The functions that our gep expression can use. This array holds indexes into the 
     * symbols array. */
    public int functions[];
    
    
    /** Number of Terminal and Function symbols in the symbol set */
    public int numberOfTerminals, numberOfFunctions;
    
    /** cumulative Normalized weights of all symbols (terminals and functions) in the set */
    public float cumulativeNormalizedSymbolWeights[];
    /** cumulative Normalized weights of just the terminal symbols in the set */
    public float cumulativeNormalizedTerminalWeights[];
    /** cumulative Normalized weights of just the function symbols in the set */
    public float cumulativeNormalizedFunctionWeights[];
    
    /** When faced with choosing a terminal or function in the head of a gene, how
     *  does one decide how many of each should be chosen on average? What if there 
     *  are many more terminals than functions or visa versa. Ferreira told me that 
     *  they use a simple rule: if fewer functions then choose functions 2/3 of the
     *  time otherwise choose functions 1/2 of the time. It's not clear how weights 
     *  factor into this rule ... I've used the weights of functions only to
     *  determine the distribution of functions amongst the functions being chosen.
     *  Otherwise the weights would have to interfere with the 2/3 rule.
     *  <br> <br>
     *  Question: Should the weights on the functions be added to give the total 
     *  number of functions in this case? For consideration when determining
     *  the proability of choosing a function versus a terminal. My gut feeling
     *  is that this should not be done and the weights only get used when
     *  determining which functions to choose amongst the functions ... but
     *  why do some of the GeneXProTools examples have 4 functions selected
     *  (+ - * /) and each has the same weight but the weights are not 1 (in
     *  some cases all are set to 2 or all are set to 4).
     */
    public double probabilityOfChoosingFunction;
    
    public Parameter defaultBase()
    {
        return GEPDefaults.base().push(P_SPECIES).push(P_SYMBOLSET);
    }

    
    /** Returns the name. */
    public String toString() { return name; }

    /** Sets up all the GEPSymbolSet symbols, loading them from the parameter
        file. Don't actually use this one ... just there to conform to
        Cliques requirements 
    */

    public void setup(final EvolutionState state, final Parameter base)
    {
    	// *** NOT USED - special setup version below ***
    }
    
    /** Sets up all the GEPSymbolSet symbols, loading them from the parameter
     *  file. 
     */
    public void setup(final EvolutionState state, final Parameter base, final Parameter def, GEPSpecies species)
    {       	
        // Name of file with the terminal (variable) definitions and training values 
        String terminalFilename;
        // Name of file with the test data values if specified
        String testingTerminalFilename;
        
    	// keep track of the maximum arity of any function
    	maxArity = 0;

        // What's my name? Don't really use this at this time ...
        name = state.parameters.getString(base.push(P_NAME), def.push(P_NAME));
        if (name==null || name.equals(""))
            state.output.warning("No name was given for this GEP symbol set...not required at this time.",
                               base.push(P_NAME), def.push(P_NAME));

        // How many functions do I have?
        numberOfFunctions = state.parameters.getInt(base.push(P_FUNCTIONSIZE), def.push(P_FUNCTIONSIZE),1);
        numberOfSymbols = numberOfFunctions;
        
        // How many terminals do I have? Check for a data file first ... 
        // if time series problem type and using raw time series data then 
        //      number of terminals will be specified in the embedding dimension value 
        //      provided in the parameter file
        // else if a file specified
        //      get the 1st line of the file and count the fields in it (#terminals is number of fields minus
        //      the number of chromosomes/dependent variables)
        // else 
        //      use the number of terminals specified in the parameter file
 
        terminalFilename = state.parameters.getStringWithDefault(base.push(P_TERMINALFILENAME), 
                def.push(P_TERMINALFILENAME), "");
        testingTerminalFilename = state.parameters.getStringWithDefault(base.push(P_TESTINGTERMINALFILENAME), 
                def.push(P_TESTINGTERMINALFILENAME), "");
        String terminalSymbolsfromFile[] = null;
        CsvReader terminalFileCSV = null;
        CsvReader testingTerminalFileCSV = null;
        // Are we processing raw time series data?
        boolean timeseriesWithRawDataValues = species.problemType==GEPSpecies.PT_TIMESERIES && 
                                              species.timeseriesEmbeddingDimension > 0;
        if (!terminalFilename.equals(""))
        {
        	String defaultTerminalFileSeparator = ","; // default field separator is comma
        	try 
        	{ 
            	// allow for gzip files .... end with .gz or .gzip\
            	if (terminalFilename.endsWith(".gz") || terminalFilename.endsWith(".gzip"))
            	{            		
            		terminalFileCSV = new CsvReader((InputStream)(new GZIPInputStream(new FileInputStream(terminalFilename))),
            				                         Charset.forName("ISO-8859-1"));
            		// set terminal file name to be the one with gzip or gz removed from the end
            		if (terminalFilename.endsWith(".gz"))
            			terminalFilename = terminalFilename.substring(0, terminalFilename.length()-3);
            		else
            			terminalFilename = terminalFilename.substring(0, terminalFilename.length()-5);
            	}
            	else
        		    terminalFileCSV = new CsvReader(terminalFilename);
        	}
        	catch (FileNotFoundException e)
        	{ state.output.fatal("The file with terminal definitions and/or values (" + terminalFilename +
        			             ") could not be found", base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
        	}
        	catch (IOException e)
        	{ state.output.fatal("The file with terminal definitions and/or values (" + terminalFilename +
        			             ") could not be found or the expected GZIP file could nor be opened", base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
        	}
        	// if filename has extension .dat it is space delimited, if .csv (or anything else 
        	// for that matter) it is comma delimited
        	// (separator can still be changed with the terminalfileseparator parameter)
        	if (terminalFilename.endsWith(".dat")) 
        		defaultTerminalFileSeparator = "space";
        	// if using a file for the terminals and their values then check for a non-default separator
            String terminalFileSeparator = state.parameters.getStringWithDefault(base.push(P_TERMINALFILESEPARATOR), 
                    def.push(P_TERMINALFILESEPARATOR), defaultTerminalFileSeparator);
            if ( terminalFileSeparator.toLowerCase().equals("comma"))
            	terminalFileSeparator = ",";
            else if (terminalFileSeparator=="\\t" || terminalFileSeparator.toLowerCase().equals("tab"))
            	terminalFileSeparator = "\t";
            else if (terminalFileSeparator=="space")
            	terminalFileSeparator = " ";
            terminalFileCSV.setDelimiter(terminalFileSeparator.charAt(0));
            // let's check for a testing data file at this time as well .. if no file for
            // names and training data no need to worry about this one.
            if (!testingTerminalFilename.equals(""))
            {
            	try 
            	{ 
                	// allow for gzip files .... end with .gz or .gzip\
                	if (testingTerminalFilename.endsWith(".gz") || testingTerminalFilename.endsWith(".gzip"))            		
                		testingTerminalFileCSV = new CsvReader((InputStream)(new GZIPInputStream(new FileInputStream(testingTerminalFilename))),
                				                         Charset.forName("ISO-8859-1"));
                	else
            		testingTerminalFileCSV = new CsvReader(testingTerminalFilename);
            	    testingTerminalFileCSV.setDelimiter(terminalFileSeparator.charAt(0));
            	}
            	catch (FileNotFoundException e)
            	{ state.output.fatal("The file with testing data values (" + testingTerminalFilename +
            			             ") could not be found", base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
            	}
            	catch (IOException e)
            	{ state.output.fatal("The file with testing data values (" + terminalFilename +
            			             ") could not be found or the expected GZIP file could nor be opened", base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
                }
            }
        }
        
        if (timeseriesWithRawDataValues)
        	numberOfTerminals = species.timeseriesEmbeddingDimension;
        else if (terminalFileCSV != null)
        {
        	// get the terminal symbols for the independent and dependent variables
        	try
        	{ terminalFileCSV.readHeaders();
        	  terminalSymbolsfromFile = terminalFileCSV.getHeaders();
        	}
        	catch (IOException e)
        	{ state.output.fatal("The file with variable (terminal) definitions and values (" + terminalFilename +
		             ") failed to read the headers" + e, base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
        	}
        	// 1 less for each dependent variable (number of chromosomes) at the end
        	numberOfTerminals = terminalSymbolsfromFile.length-species.numberOfChromosomes; 
        	if (numberOfTerminals < 1)
        	state.output.fatal("The file with terminal definitions and data values (" + terminalFilename +
			             ") has no independent variables specified in record 1", base.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME));
        	// if using a file for the terminals and their values then check for a non-default separator
        }
        else
        {
           numberOfTerminals = state.parameters.getInt(base.push(P_TERMINALSIZE), def.push(P_TERMINALSIZE),1);
        }
        numberOfSymbols += numberOfTerminals;
        
        if (numberOfSymbols < 1)
            state.output.error("The GEPSymbolSet \"" + name + "\" have at least 1 terminal symbol defined.",
                               base.push(P_TERMINALSIZE), def.push(P_TERMINALSIZE));
        
        // add a special Symbol for constants if we are using them ... it will be added to the
        // end of the array of symbols!
        if (species.useConstants)
        {
        	numberOfTerminals++; // special constant terminal
        	numberOfSymbols++;
       }
        
        symbols = new GEPSymbol[numberOfSymbols];
        
        int numberOfSymbolsWithoutConstantSymbol = numberOfSymbols;
        if (species.useConstants) // add the constant terminal symbol to the end
        {   symbols[numberOfSymbols-1] = (GEPSymbol)(new GEPConstantTerminalSymbol());
            symbols[numberOfSymbols-1].id = numberOfSymbols - 1;
            numberOfSymbolsWithoutConstantSymbol--;
        }

        Parameter pTerminal = base.push(P_TERMINAL);
        Parameter pdefTerminal = def.push(P_TERMINAL);
        Parameter pFunction = base.push(P_FUNCTION);
        Parameter pdefFunction = def.push(P_FUNCTION);
        
        // create hashtable of names of terminals and hash table with names of functions
        // so we can easily check that they are not duplicates
        Hashtable functionHT = new Hashtable();
        Hashtable terminalHT = new Hashtable();
        
        //      process the functions
        for(int x = 0; x<numberOfFunctions; x++)
        {    
            Parameter pp = pFunction.push(""+x);
            Parameter ppdef = pdefFunction.push(""+x);
            String function = state.parameters.getStringWithDefault(pp, ppdef, "");
            if (function.equals("")) // no name for the function
                state.output.fatal("Invalid function specifier: '" + function + "'", pp, ppdef);
            // make sure not specifying the same function more than once
            if (functionHT.get(function) != null)
                state.output.fatal("Function '" + function + "' was specified more than once in list of function symbols");
            else
            	functionHT.put(function, function);
            GEPFunctionSymbol fs = null;
            try {
                Class classDefinition = Class.forName(LOCATION_OF_FUNCTION_CLASSES+"."+function);
                fs = (GEPFunctionSymbol)classDefinition.newInstance();
            } catch (InstantiationException e) {
                state.output.fatal("Unable to create GEPFunctionSymbol class for function '" + function + "'. " + e);
            } catch (IllegalAccessException e) {
                state.output.fatal("Unable to create GEPFunctionSymbol class for function '" + function + "' " + e);
            } catch (ClassNotFoundException e) {
                state.output.fatal("Unable to create GEPFunctionSymbol class for function '" + function + "' " + e);
            }
            
            // if using a logical function must be a logical problem
            if (fs.isLogicalFunction() && (species.problemType != GEPSpecies.PT_LOGICAL))
                    state.output.fatal("Can only use logical functions with a logical problem type. Function "
                		               + function + " is  a logical function.", pp, ppdef);
            // if using a numerical function must be an non logical problem
            if (!fs.isLogicalFunction() && (species.problemType == GEPSpecies.PT_LOGICAL))
                    state.output.fatal("Can only use logical functions with a non logical problem type. Function "
                		               + function + " is a numerical function.", pp, ppdef);
           	
            symbols[x] = (GEPSymbol)fs;
            //symbols[x].setup(state, base);
            if (fs.arity < 1)
                    state.output.fatal("Arity must be > 0 for a GEPTerminalSymbol)", pp, ppdef);
            symbols[x].id = x;
            int weight = state.parameters.getInt(pp.push(P_FUNCTIONWEIGHT),ppdef.push(P_FUNCTIONWEIGHT),1);
            if (weight<1)
            {   state.output.warning("Weight for GEP Function must be > 0; defaulting to 1)",
                                   pp.push(P_FUNCTIONWEIGHT),ppdef.push(P_FUNCTIONWEIGHT));
                weight = 1;
            }
            symbols[x].weight = weight;
            if (symbols[x].arity > maxArity)
            	maxArity = symbols[x].arity;
        }
        
        // process the terminals  ... defined by default for timeseries data, in the
        // CSV file if specified and not timeseries, or in the params file if neither of those.
        for(int x = numberOfFunctions; x<numberOfSymbolsWithoutConstantSymbol; x++)
        {   // load the terminal symbols
    		int index = x-numberOfFunctions;
        	String terminal = "";
        	if (timeseriesWithRawDataValues)
        	{
        		// terminals get default names v0, v1, v2, v3, ... vn-1
        		terminal = "v" + index;
        	}
        	else if (terminalFileCSV==null)//terminals defined in param file
        	{
	            Parameter pp = pTerminal.push(""+index);
	            Parameter ppdef = pdefTerminal.push(""+index);
	            terminal = state.parameters.getStringWithDefault(pp, ppdef, "");
        	}
        	else
        	{   // terminals defined in CSV file
        		terminal = terminalSymbolsfromFile[index];
        	}
            if (terminal.equals("")) // no name for the terminal 
                state.output.fatal("Invalid terminal specifier: '" + terminal + "' for terminal # " + index);
            // make sure not specifying the same function more than once
            if (terminalHT.get(terminal) != null)
                state.output.fatal("Terminal symbol (indep var) '" + terminal + "' was specified more than once in list of terminal symbols (independent variables)");
            else
            	terminalHT.put(terminal, terminal);
            GEPTerminalSymbol ts = new GEPTerminalSymbol(terminal, this);
            symbols[x] = (GEPSymbol)ts;
            //symbols[x].setup(state, base);
            if (ts.arity !=0) // cannot happen
                state.output.fatal("Arity must be exactly 0 for a GEPTerminalSymbol)");
            symbols[x].id = x;
            symbols[x].weight = 1; // all Terminal symbols have weight of 1
        }
        
        // must be at least 1 Terminal symbol in the SymbolSet.
        // If not then the user didn't specify the terminals in the param file or in the data file
        if (numberOfTerminals < 1)
            state.output.fatal("Must be at least one Terminal Symbol in the set of GEPSymbols\n" +
            		           "Either did not specify the terminal symbols in the param file or\n" + 
            		           "did not specify the appropriate data file with the terminals specified in the first line."
            		          );
        
        // collect the id's (indices) of the terminal and function symbols that 
        // are in the set of symbols
        terminals = new int[numberOfTerminals];
        int terminalNum = 0;
        functions = new int[numberOfFunctions];
        int functionNum = 0;
        for (int x = 0; x<numberOfSymbols; x++)
        {
            if (symbols[x] instanceof GEPConstantTerminalSymbol)	
                terminals[terminalNum++] = x;
            else if (symbols[x] instanceof GEPTerminalSymbol)
            	terminals[terminalNum++] = x;
            else if (symbols[x] instanceof GEPFunctionSymbol)	
                functions[functionNum++] = x;
        }
        
        // collect the weights for symbols and terminals and normalize and cumulate them.
        // Then we can use these arrays to pick appropriate symbols or terminals according to
        // their weights ... using the RandomChooser.PickFromDistribution
        cumulativeNormalizedSymbolWeights = new float[numberOfSymbols];
        cumulativeNormalizedTerminalWeights = new float[numberOfTerminals];
        cumulativeNormalizedFunctionWeights = new float[numberOfFunctions];
        int j=0, k=0;
        for (int i=0; i<numberOfSymbols; i++)
        {
        	float weight = (float)(symbols[i].weight);
        	cumulativeNormalizedSymbolWeights[i] = weight;
        	if (symbols[i] instanceof GEPTerminalSymbol  || symbols[i] instanceof GEPConstantTerminalSymbol)
        		cumulativeNormalizedTerminalWeights[j++] = weight;
        	if (symbols[i] instanceof GEPFunctionSymbol)
        		cumulativeNormalizedFunctionWeights[k++] = weight;
        }
        RandomChoice.organizeDistribution(cumulativeNormalizedSymbolWeights);
        RandomChoice.organizeDistribution(cumulativeNormalizedTerminalWeights);
        RandomChoice.organizeDistribution(cumulativeNormalizedFunctionWeights);
        
        // use the 2/3 rule if fewer functions else the 1/2 rule (don't count the constant
        // terminal here)
    	if (numberOfFunctions < (numberOfTerminals - (species.useConstants ? 1 : 0)))
    		probabilityOfChoosingFunction = 2.0/3.0;
    	else
    		probabilityOfChoosingFunction = 0.5;
    	
    	// ... and finally get the training and testing data values for the terminals and dependent variable
    	// and put them into the Terminal instances (creating a 'special' Terminal Symbol to
    	// hold the dependent variable training and testing values)
    	
    	// If this is a time series problem AND we are using the raw time series data then
    	// we named the terminals v1, v2, ..., nn where n is the number of independent 
    	// variables as specified in the embedding dimension (which) was used to 
    	// determine the number of terminals. But we have to process the time series data
    	// to get the values for each terminal ... get the raw data from the CSV file
    	// if specified or from the user program ... then process it into rows of data 
    	// representing the independent variables and the dependent variable.
    	//
    	//   timeseries-delay -- if 1 uses each time series value, if 2 uses every other one, etc.
    	//   timeseries-embeddingdimension -- determines the number of timeseries points to use 
    	//        as independent variables when transforming the set of time series data. Another
    	//        data point is used as the dependent variable value. So the time series 'raw' data
    	//        consisting of a list of single values is processed by splitting the data into
    	//        groups (rows) of size embeddingdimension+1. From the end of the time series data
    	//        embeddingdimension+1 values are chosen (if delay is 1 all values are chosen, if 
    	//        2 every other one is chosen). The last value is the independent variable value.
    	//        Then the next row is selected by moving 'delay'
    	//        values from the end and chosing embeddingdimension+1 values. This is repeated
    	//        until no more sets of size embeddingdimension+1 can be chosen. If this produces
    	//        n sets of data then testingprediction of them are used for testing and
    	//        (n - testingpredictions) are used for training.
    	//
    	//        So if we had the data:
    	//        1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21
    	//        and delay was 1 and embeddingdimension was 4 then we'd process the set into 
    	//        the following 17 data sets. If testingpredictions was 6 then the 1st 11
    	//        would be used for training and the last 6 for testing
    	//        iv1 iv2 iv3 iv4  dv
    	//          1   2   3   4   5
    	//          2   3   4   5   6
    	//          3   4   5   6   7
    	//              . . .
    	//         14  15  16  17  18
    	//         15  16  17  18  19
    	//         16  17  18  19  20
    	//         17  18  19  20  21
    	//        If delay was 2 then 7 sets would be formed as:
    	//        iv1 iv2 iv3 iv4  dv
    	//          1   3   5   7   9
    	//          3   5   7   9  11
    	//              . . .
    	//          9  11  13  15  17
    	//         11  13  15  17  19
    	//         13  15  17  19  21
    	//   timeseries-testingpredictions -- specifies the number of sets of data to devote to testing
    	if (timeseriesWithRawDataValues)
    	{
    		GEPDependentVariable.symbol = "dependentVariable";
    		double rawTimeSeriesValues[] = null;
    		if (terminalFileCSV == null)
    			rawTimeSeriesValues = ((GEPProblem)state.evaluator.p_problem).getTimeSeriesDataValues();
    		else 
    			rawTimeSeriesValues = getRawTimeSeriesValuesFromCSVfile(state, terminalFileCSV);
    		if (rawTimeSeriesValues == null)
				state.output.fatal("Unable to get time series data values from User Program or CSV file");
    		Vector values[] = processRawTimeSeriesValues(state, species, rawTimeSeriesValues);
    		// have an array of vectors; 1 vector for each indep variable and the dep variable(s)
    		for (int i=0; i<values.length; i++)
    		{
    			// get the values for training ... and testing (specified by timeseriesTestingPredictions)
    			int sizeOfTrainingData = values[i].size()-species.timeseriesTestingPredictions;
    			double v[] = new double[sizeOfTrainingData];
    			double testingV[] = new double[species.timeseriesTestingPredictions];
    			for (int m=0; m<v.length; m++)
    				v[m] = ((Double)values[i].elementAt(m)).doubleValue();
    			for (int n=0; n<testingV.length; n++)
    				testingV[n] = ((Double)values[i].elementAt(n+sizeOfTrainingData)).doubleValue();
    			int depVarIndex = i-values.length+species.numberOfChromosomes;
    			if (depVarIndex>=0) // last column(s) in file is(are) the dependent variable(s)
    			{	GEPDependentVariable.trainingData.setValues(v, depVarIndex);
    			    GEPDependentVariable.testingData.setValues(testingV, depVarIndex);
    			}
    			else
    			{   ((GEPTerminalSymbol)symbols[numberOfFunctions+i]).setTrainingValues(v);
    			    ((GEPTerminalSymbol)symbols[numberOfFunctions+i]).setTestingValues(testingV);
    			}    			
    		}
    	}
    	// else If there is a file with the terminals and dep variable(s) use this else ask for
    	// the values from the User Program (problem).
    	else if (terminalFileCSV != null )//terminals defined in CSV file
    	{
    		GEPDependentVariable.symbol = terminalSymbolsfromFile[terminalSymbolsfromFile.length-1];
    		// get all the values into an array of vectors (each vector holds the values for a
    		// single terminal (dep or indep variable)
    		Vector values[] = new Vector[terminalSymbolsfromFile.length];
			for (int i=0; i<terminalSymbolsfromFile.length; i++)
				values[i] = new Vector();
    		try
    		{ while (terminalFileCSV.readRecord())
    		  {
    			for (int i=0; i<terminalSymbolsfromFile.length; i++)
    				values[i].add(terminalFileCSV.get(i));
    		  }
    		}
    		catch (IOException e)
    		{  state.output.fatal("The file with terminal definitions/values failed when reading records. " + e);
    		}
    		
    		for (int i=0; i<terminalSymbolsfromFile.length; i++)
    		{
    			double v[] = new double[values[i].size()];
    			for (int m=0; m<v.length; m++)
    				try
					{
						v[m] = Double.parseDouble((String)values[i].elementAt(m));
					}
	    			catch (Exception e)
	    			{
	    				state.output.fatal("Failed trying to read a training data set value. The field is supposed to be a number but was the string '"
	    						+ (String)values[i].elementAt(m) + "'.\n" + e);
	    			}
	    		int jj = terminalSymbolsfromFile.length-species.numberOfChromosomes;
    			if (i >= jj) // last column(s) in file is(are) the dependent variable(s)
    				GEPDependentVariable.trainingData.setValues(v, i-jj);
    			else
    			   ((GEPTerminalSymbol)symbols[numberOfFunctions+i]).setTrainingValues(v);
    		}
    		// get the testing data as well if a file was specified
    		if (testingTerminalFileCSV != null )//testing data defined in CSV file
        	{
        		// get all the values into an array of vectors (each vector holds the values for a
        		// single terminal (dep or indep variable)
        		Vector testingValues[] = new Vector[terminalSymbolsfromFile.length];
    			for (int i=0; i<terminalSymbolsfromFile.length; i++)
    				testingValues[i] = new Vector();
        		try
        		{ while (testingTerminalFileCSV.readRecord())
        		  {
        			for (int i=0; i<terminalSymbolsfromFile.length; i++)
        				testingValues[i].add(testingTerminalFileCSV.get(i));
        		  }
        		}
        		catch (IOException e)
        		{  state.output.fatal("The file with testing data values failed when reading records. " +
        				"\nMake sure the file has the same column separators as the testing data file." +
        				"\nAlso check that it has the same as the number of columns as the testing file" + e);
        		}
        		
        		for (int i=0; i<terminalSymbolsfromFile.length; i++)
        		{
        			double v[] = new double[testingValues[i].size()];
        			for (int m=0; m<v.length; m++)
        				try
        				{
        					v[m] = Double.parseDouble((String)testingValues[i].elementAt(m));
        				}
	        			catch (Exception e)
	        			{
	        				state.output.fatal("Failed trying to read a testing data set value. The field is supposed to be a number but was the string '"
	        						+ (String)testingValues[i].elementAt(m) + "'.\n" + e);
	        			}
	    	    	int jj = terminalSymbolsfromFile.length-species.numberOfChromosomes;
        			if (i >= jj) // last column(s) in file is(are) the dependent variable(s)
        				GEPDependentVariable.testingData.setValues(v, i-jj);
        			else
        			   ((GEPTerminalSymbol)symbols[numberOfFunctions+i]).setTestingValues(v);
        		}
         	}
    	}
    	// else terminals were defined in the param file and no CSV file 
		// defined so .... ask User Problem for the values, training and testing (if there are any)
    	else
    	{	    		
    		GEPDependentVariable.symbol = "dependentVariable";
            GEPProblem prob = (GEPProblem)state.evaluator.p_problem;
            double vals[] = null;
			for (int i = numberOfFunctions; i<numberOfSymbolsWithoutConstantSymbol; i++)
			{
				GEPTerminalSymbol ts = (GEPTerminalSymbol)symbols[i];
				vals = prob.getDataValues(ts.symbol);
				if (vals == null)
					state.output.fatal("Expecting user problem (GEPProblem/ProblemForm) to supply training data values for terminal symbol '" 
							+ ts + "'.");
				ts.setTrainingValues(vals);
				vals = prob.getTestingDataValues(ts.symbol);
				if (vals != null) // don't have to supply testing data
					ts.setTestingValues(vals);
			}
			// if just one dep var then ask user by requesting with getdataValues("dependentVariable")
			// and if more than one dep var (more than 1 chromosome) then ask for dep variables
			// with getDataValues("dependentVariable0"), getDataValues("dependentVariable1"), ...
			for (int i=0; i<species.numberOfChromosomes; i++)
			{
				String depVarSym = GEPDependentVariable.symbol;
				if (species.numberOfChromosomes>1)
					depVarSym = depVarSym + i;
				vals = prob.getDataValues(depVarSym);			
				if (vals == null)
					state.output.fatal("Expecting user problem (GEPProblem/ProblemForm) to supply training data values for dependent variable '"
							+ depVarSym + "'.");
				GEPDependentVariable.trainingData.setValues(vals, i);
				vals = prob.getTestingDataValues(depVarSym);			
				if (vals != null) // don't have to supply testing data
					GEPDependentVariable.testingData.setValues(vals, i);
			}
    	}
    	
    	// Some checking of data values to ensure they meet the requirements for the various problem types.
    	// For all problem types need to make sure all indep vars and the dep var have the same number of values!
    	int numValues = GEPDependentVariable.trainingData.values[0].length;
		for (int i = numberOfFunctions; i<numberOfSymbolsWithoutConstantSymbol; i++)
    	    if (((GEPTerminalSymbol)symbols[i]).trainingValues.length != numValues)
                state.output.fatal("Must have same number of values for all independent variables and the dependent variable."
                		+ "/nNumber of values for Dependent Variable is: " + numValues  
                		+ "/nNumber of values for Independent Variable '" + symbols[i].symbol 
                		+ "' is: " + ((GEPTerminalSymbol)symbols[i]).trainingValues.length);
		// For Classification and logical problems all dependent variable values must be either 0 or 1
		if (species.problemType == GEPSpecies.PT_CLASSIFICATION  || species.problemType == GEPSpecies.PT_LOGICAL)
		{
			double dvVals[] = GEPDependentVariable.trainingData.values[0];
			for (int i=0; i<numValues; i++)
				if (dvVals[i] != 0.0 && dvVals[i] != 1.0)
	                state.output.fatal("For classification/logical problems all dependent variable values must be either 1 or 0.\nFound value " + 
	                		dvVals[i] +  " at index " + i + "in the values.");
		}
		// For Logical problems all independent variable values must be 0 or 1
		if (species.problemType == GEPSpecies.PT_LOGICAL)
		{	// for each indep variable symbol
			for (int i = numberOfFunctions; i<numberOfSymbolsWithoutConstantSymbol; i++)
			{
				double ivVals[] = ((GEPTerminalSymbol)symbols[i]).trainingValues;
				for (int m=0; m<numValues; m++)
					if (ivVals[m] != 0.0 && ivVals[m] != 1.0)
		                state.output.fatal("For logical problems all independent variable values must be either 1 or 0.\nFound value " + 
		                		ivVals[m] +  " at index '" + m + "' in the variable '" + 
		                		((GEPTerminalSymbol)symbols[i]).symbol + "'.");
			}
		}
        state.output.exitIfErrors();
    }
    
    /**
     * This function lets us get the base index of the ids assigned to Terminal symbols.
     * In this implementation functions are assigned ids from 0 to numberOfFunctions-1, the
     * terminals get the next ids starting at numberOfFunctions. 
     * @return the first id assigned to terminal symbols
     */
    public int getBaseTerminalSymbolId()
    {
       return numberOfFunctions;
    }

    /** Reads floating values from a CSV file and returns them in an array.
     * 
     * @param state
     * @param terminalFileCSV the file with the float values
     * @return an array with all of the float values in the file
     */
    public double[] getRawTimeSeriesValuesFromCSVfile(EvolutionState state, CsvReader terminalFileCSV)
    {
    	// expect the CSV file to contain only float values ... 
		Vector rawValues = new Vector(100);
		try
		{ while (terminalFileCSV.readRecord())
		  {
			for (int i=0; i<terminalFileCSV.getColumnCount(); i++)
				rawValues.add(terminalFileCSV.get(i));
		  }
		}
		catch (IOException e)
		{  state.output.fatal("The file with time series raw values failed when reading records. " + e);
		}
		// convert the vector into an array
		double rvArray[] = new double[rawValues.size()];
		for (int i=0; i<rvArray.length; i++)
			rvArray[i] = Double.parseDouble((String)rawValues.elementAt(i));
		
		return rvArray;
    }
    
    /**
     * 
     * @param state
     * @param species a GEPSpecies that holds various parameters about the time series problem
     * @param rawTimeSeriesValues an array of float values; the raw time series values
     * @return an array of Vectors such that each vector has all of the 
     */
    public Vector[] processRawTimeSeriesValues(EvolutionState state, GEPSpecies species, double[] rawTimeSeriesValues)
    {
    	int rawLen = rawTimeSeriesValues.length;
    	int delay = species.timeseriesDelay;
    	int numVars = species.timeseriesEmbeddingDimension + species.numberOfChromosomes; // 1 extra for the each dep var
    	Vector v[] = new Vector[numVars];
    	// number or rows one can get from N values, with delay of D, and
    	// numvars of M is  ((N - M*D -1)/D)+2
    	// Basically you start at the end and take the last value, then the one 
    	// D steps before that one, etc. until you have M values. Then shift back D 
    	// values from the end and select the next M values spaced by D.
    	int numRows = ((rawLen - numVars*delay -1)/delay)+2;
    	// preallocate the vectors in the array v each with size numRows;
    	for (int i=0; i<numVars; i++)
    	{	v[i] = new Vector();
    		v[i].setSize(numRows);
    	}
    	int j = rawLen-1;
    	for (int k=numRows-1; k>=0; k--)
    	{
    		int m=numVars-1;
    		for (int x=j; x>(j-(numVars*delay)); x=x-delay, m--)
    			v[m].setElementAt(new Double(rawTimeSeriesValues[x]), k);
    		j = j-delay;
    	}
    	
    	return v;
    }
    
    public int chooseFunctionSymbol(EvolutionState state, int thread)
    {
        int functionChoice = RandomChoice.pickFromDistribution(
	    		cumulativeNormalizedFunctionWeights,
	    		state.random[thread].nextFloat(), CHECK_BOUNDARY);
	    // the value returned is the function index from 0 to number of functions ...
	    // need to get the index into the set of symbols
	    return functions[functionChoice];
    }

    public int chooseTerminalSymbol(EvolutionState state, int thread)
    {
        int terminalChoice = RandomChoice.pickFromDistribution(
	    		cumulativeNormalizedTerminalWeights,
	    		state.random[thread].nextFloat(), CHECK_BOUNDARY);
	    // the value returned is the function index from 0 to number of functions ...
	    // need to get the index into the set of symbols
	    return terminals[terminalChoice];
    }

    public int chooseFunctionOrTerminalSymbol(EvolutionState state, int thread, int genePos, GEPSpecies s)
    {
	    // the symbols are chosen according to being in the head or in the tail of the gene
	    // Ferreria indicated that the rule of thumb for choosing symbols for the head is:
	    //   if number of functions < number of terminals 
	    //   then prob of selecting a function is 2/3
	    // BUT
	    //   if number of functions >= number of terminals
	    //   then prob of selecting a function is 1/2
	    // 
	    // In the tail just the weights of the terminals (only in GeneXpro there are no 
	    // so they all have the same weight); in this implementation we can assign weights 
	    // to the terminals
	    if (genePos < s.headSize) 
	    {   // in the head choose from all symbols (terminals and functions)	
            if (state.random[thread].nextFloat() < probabilityOfChoosingFunction)
                // select from the functions
	            return chooseFunctionSymbol(state, thread);
 	        else
            	// select from the terminals
	            return chooseTerminalSymbol(state, thread);
	    }
	    else
	      	// in the tail choose only from terminal symbols
	        return chooseTerminalSymbol(state, thread);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }
}
