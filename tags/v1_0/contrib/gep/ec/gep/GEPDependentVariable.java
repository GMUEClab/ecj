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

import java.io.Serializable;

import ec.*;
import ec.gep.*;
import ec.util.Parameter;

/* 
 * GEPDependentVariable.java
 * 
 * Created: Feb 7, 2007
 * By: Bob Orchard
 */

/**
 * Supports encoding the information about the problem's dependent variable. 
 * <p>
 * The dependent variable and associated special information is stored here as static
 * information (class variables) so it can be accessed as needed so no instances are created.
 * There are (often) 2 sets of dependent variable data (as there are for the independent 
 * variables). There is training data, used during evolution to create the models. And there
 * is testing data that is used to verify the quality of the data. So we have a status variable
 * (boolean) that identifies which set of data to use, training ot testing. Training data 
 * is used during evolution is the normal mode of access. But the setting can be changed to
 * access the testing data to evaluate the model(s) produced from the training data 
 * against the test data. Users should use care when switching modes of access
 * and be sure to set it back to access training data. 
 */


public abstract class GEPDependentVariable implements Serializable
{
	/**
	 * Static flag that determines if the dependent variable and terminals (independent variables)
	 * should supply training or testing data when accessed. This is required since the models
	 * are encoded in expression trees without regard to the data sets needed when they are evaluated.
	 * So when evaluation is done the data used depends on this setting.
	 * Defaults to using the training data set, as is required of course during evolution when the
	 * models are being explored.
	 * <p>
	 * When the data values are stored (setValues and setTestingValues) we also calcualte a number of
	 * statistical values for the dependent variables that can be used in fitness functions. This 
	 * allows these values to be calculated only once.
	 */
	public static boolean useTrainingData = true;
	
	/** Must store the values for the Dependent Variable of the problem so evaluations can 
	 *  be performed on expressions using this terminal symbol. These values are
	 *  the ones used in training the system.
	 */
	public static double values[] = null;
	
	/**
	 * The symbol to use when displaying the dependent variable in human readable form.
	 */
	static String symbol = "dependentVariable";
	/**
	 * The dependent variable values used for testing a model. Used
	 * to determine how well the model (which was evolved using the training values)
	 * performs on a test set of data.
	 */
	public static double testingValues[] = null;
	
    /** Mean value of the training values for the dependent variable */
	static double dependentVariableMean = 0.0;
    
    /** Sum of the absolute error between dependent variables training values and their mean value. The
     *  absolute error is Abs(dv[i] - dvMean).
     */
	static double dependentVariableSumOfAbsoluteError = 0.0;
    /** Sum of the squares of the absolute errors between dependent variables training values and their mean 
     *  value. The absolute error is Abs(dv[i] - dvMean).
     */
	static double dependentVariableSumOfSquaredAbsoluteError = 0.0;
    /** Sum of the relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	static double dependentVariableSumOfRelativeError = 0.0;
    /** Sum of the squares relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	static double dependentVariableSumOfSquaredRelativeError = 0.0;
    
    /** Variance of the training values for the dependent variable */
	static double dependentVariableVariance = 0.0;
    
    /** Mean value of the training values for the dependent variable */
	static double testDependentVariableMean = 0.0;
    
    /** Sum of the absolute error between dependent variables training values and their mean value. The
     *  absolute error is Abs(dv[i] - dvMean).
     */
	static double testDependentVariableSumOfAbsoluteError = 0.0;
    /** Sum of the squares of the absolute errors between dependent variables training values and their mean 
     *  value. The absolute error is Abs(dv[i] - dvMean).
     */
	static double testDependentVariableSumOfSquaredAbsoluteError = 0.0;
    /** Sum of the relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	static double testDependentVariableSumOfRelativeError = 0.0;
    /** Sum of the squares relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	static double testDependentVariableSumOfSquaredRelativeError = 0.0;
    
    /** Variance of the training values for the dependent variable */
	static double testDependentVariableVariance = 0.0;
    
	/**
	 * make sure these are empty each time we start a new problem
	 */
	public static void setup( )
	{
		values = null;
		testingValues = null;
	}
	
		
	/**
	 * Copy the values provided so the training data is available as required. Also
	 * calcualte a set of useful statistical values for the training dependent variable
	 * values.
	 * 
	 * @param vals - the training values for this terminal symbol 
	 */
	public static void setValues( double vals[] )
	{
		values = new double[vals.length];
		System.arraycopy(vals, 0, values, 0, vals.length);
		calculateDependentVariableStats();
	}

	/**
	 * Copy the values provided so the testing data is available as required. Also
	 * calcualte a set of useful statistical values for the testing dependent variable
	 * values.
	 * 
	 * @param vals - the testing values for this terminal symbol 
	 */
	public static void setTestingValues( double vals[] )
	{
		testingValues = new double[vals.length];
		System.arraycopy(vals, 0, testingValues, 0, vals.length);
		calculateTestDependentVariableStats();
	}

	static void calculateTestDependentVariableStats()
	{
		if (testingValues==null)
			return ;
    	// now we can calculate the mean and variance etc. for the dependent variable testing values
        double sumOfDV = 0.0;
        for (int i=0; i<testingValues.length; i++)
        	sumOfDV += testingValues[i];        
        testDependentVariableMean = sumOfDV/testingValues.length;
        
        testDependentVariableSumOfAbsoluteError = 0.0;
        testDependentVariableSumOfSquaredAbsoluteError = 0.0;
        testDependentVariableSumOfRelativeError = 0.0;
        testDependentVariableSumOfSquaredRelativeError = 0.0;
        for (int i=0; i<testingValues.length; i++)
        {	
        	double diff = testingValues[i] - testDependentVariableMean;
	        testDependentVariableSumOfAbsoluteError += Math.abs(diff);
	        testDependentVariableSumOfSquaredAbsoluteError += diff*diff;
	        double diffRelative = diff/testDependentVariableMean;
	        testDependentVariableSumOfRelativeError += Math.abs(diffRelative);
	        testDependentVariableSumOfSquaredRelativeError += diffRelative*diffRelative;        
        }
        testDependentVariableVariance = testDependentVariableSumOfSquaredAbsoluteError/testingValues.length;
	}
	
	static void calculateDependentVariableStats()
	{
		if (values==null) // should not happen ... will be tested earlier
			return;
    	// now we can calculate the mean and variance etc. for the dependent variable training values
        double sumOfDV = 0.0;
        for (int i=0; i<values.length; i++)
        	sumOfDV += values[i];        
        dependentVariableMean = sumOfDV/values.length;
        
        dependentVariableSumOfAbsoluteError = 0.0;
        dependentVariableSumOfSquaredAbsoluteError = 0.0;
        dependentVariableSumOfRelativeError = 0.0;
        dependentVariableSumOfSquaredRelativeError = 0.0;
        for (int i=0; i<values.length; i++)
        {	
        	double diff = values[i] - dependentVariableMean;
	        dependentVariableSumOfAbsoluteError += Math.abs(diff);
	        dependentVariableSumOfSquaredAbsoluteError += diff*diff;
	        double diffRelative = diff/dependentVariableMean;
	        dependentVariableSumOfRelativeError += Math.abs(diffRelative);
	        dependentVariableSumOfSquaredRelativeError += diffRelative*diffRelative;        
        }
        dependentVariableVariance = dependentVariableSumOfSquaredAbsoluteError/values.length;
	}

    /** A function to get the testing or training values for the dependent variable.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  included in a file or by asking the User's GEPProgram to supply them via a call to the method
     *  setTerminalSymbolDataValues that must be provided with the User's GEPProgram.
     * @return the dependent variable values in an array
     */
    static public double[] getDependentVariableValues( )
    {
    	if (useTrainingData)
    	   return values;
    	else
     	   return testingValues;
    }

    /** A function to get the dependent variable's mean value.
     * @return the mean of the set of values of the dependent variable
     */
    static public double getDependentVariableMean( )
    {
    	if (useTrainingData)
     	   return dependentVariableMean;
     	else
      	   return testDependentVariableMean;
    }

    /** A function to get the dependent variable's sum of the absolute errors.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
     * @return the sum of the absolute error of the set of values of the dependent variable
     */
    static public double getDependentVariableSumOfAbsoluteError( )
    {
    	if (useTrainingData)
      	   return dependentVariableSumOfAbsoluteError;
      	else
       	   return testDependentVariableSumOfAbsoluteError;
    }

    /** This is a convenience function to get the dependent variable's sum of the squares of the absolute errors.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
     * @return the sum of the squares of the absolute error of the set of values of the dependent variable
     */
    static public double getDependentVariableSumOfSquaredAbsoluteError( )
    {
    	if (useTrainingData)
       	   return dependentVariableSumOfSquaredAbsoluteError;
       	else
           return testDependentVariableSumOfSquaredAbsoluteError;
    }

    /** This is a convenience function to get the dependent variable's sum of the relative errors.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
     * @return
     */
    static public double getDependentVariableSumOfRelativeError( )
    {
    	if (useTrainingData)
        	return dependentVariableSumOfRelativeError;
        else
            return testDependentVariableSumOfRelativeError;
    }

    /** This is a convenience function to get the dependent variable's sum of the squares of the relative errors.
     * @return
     */
    static public double getDependentVariableSumOfSquaredRelativeError( )
    {
    	if (useTrainingData)
        	return dependentVariableSumOfSquaredRelativeError;
        else
            return testDependentVariableSumOfSquaredRelativeError;
    }

    /** This is a convenience function to get the dependent variable's variance value stored in the GEPspecies SymbolSet.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
     * @return
     */
    static public double getDependentVariableVariance( )
    {
    	if (useTrainingData)
        	return dependentVariableVariance;
        else
            return testDependentVariableVariance;
    }

}
