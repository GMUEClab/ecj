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

/* 
 * GEPTerminalSymbol.java
 * 
 * Created: Nov. 10, 2006
 * By: Bob Orchard
 */

/**
 * Supports encoding the information about a terminal: its arity (number
 * of argments is always 0 ... no args) and its name (symbol).
 * Also we store the symbol set that the terminal symbol belongs to 
 * for easy access to info when required (for example when calculating
 * the terminals used in an expression).
 */


public class GEPTerminalSymbol extends GEPSymbol
{
	/**
	 * The values used for training a model (the expression encoded in an individual)
	 * to determine how well the model performs on the training set of data.
	 */
	public double trainingValues[];
	/**
	 * The values used for testing a model (the expression encoded in an individual)
	 * to determine how well the model (which was evolved using the training values)
	 * performs on a test set of data.
	 */
	public double testingValues[];
	/**
	 * This is the symbolSet that the Terminal symbol belongs to.
	 */
	public GEPSymbolSet symbolSet;
	    
	
	public GEPTerminalSymbol(GEPSymbolSet ss) 
	{
		arity = 0;
		symbolSet = ss;
		trainingValues = null;
		testingValues = null;
	}
	
	public GEPTerminalSymbol(String sym, GEPSymbolSet ss) 
	{
		arity = 0;
		symbol = sym;
		symbolSet = ss;
		trainingValues = null;
		testingValues = null;
	}
	
	/**
	 * Copy the values provided so the training data is available as required.
	 * 
	 * @param vals - the training values for this terminal symbol 
	 */
	public void setTrainingValues( double vals[] )
	{
		trainingValues = new double[vals.length];
		System.arraycopy(vals, 0, trainingValues, 0, vals.length);
	}

	/**
	 * Copy the values provided so the testing data is available as required.
	 * 
	 * @param vals - the testing values for this terminal symbol 
	 */
	public void setTestingValues( double vals[] )
	{
		testingValues = new double[vals.length];
		System.arraycopy(vals, 0, testingValues, 0, vals.length);
	}

	/**
	 * The value of a terminal symbol is the value stored at the specified index 
	 * in its array of values.
	 * 
	 * @param useTrainingData if true use Training data else use Testing data
	 * @param valuesIndex determines which value to access from its array of values
	 * @return
	 */
	public double eval(boolean useTrainingData, int valuesIndex)
    {
    	try
    	{
    		if (useTrainingData) // use the training data...there MUST be some
    			return trainingValues[valuesIndex];
    		// else use the testing data if there is any specified
    		else
    			return testingValues[valuesIndex];
    	}
    	catch (Exception e)
    	{
    		System.err.println("Accessing data in TerminalSymbol '"+symbol+"' at index '" + valuesIndex +
    				"' but no values available or index out of range.\n" + e);
    		System.exit(0);
    	}
    	
    	return 0.0; // can't get here
    }
	

}
