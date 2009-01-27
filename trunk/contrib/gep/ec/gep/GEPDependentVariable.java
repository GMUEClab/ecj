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
 * Supports encoding information about the problem's dependent variable(s). The standard
 * problems supported by Ferreira's GEP system will always have a single dependent 
 * variable in the set of data (testing data and training data). However, in this implementation
 * of GEP each individual in the population can have multiple chromosomes that are 
 * represented by multiple dependent variables. 
 * <p>
 * The dependent variable(s) values and associated special statistical information is stored here 
 * so it can be accessed as needed.
 * There are (often) 2 sets of dependent variable data (as there are for the independent 
 * variables). There is training data, used during evolution to create the models. And there
 * is testing data that is used to verify the quality of the data. So we create 2 instances of 
 * the GEPDependentVariable to hold the training and testing data dependent variable information.
 * these are static variable in this class (GEPDependentVariable.trainingData and GEPDependentVariable.testingData). 
 */


public class GEPDependentVariable implements Serializable
{	
	/** Constructor for the GEPDependentVariable class will setup the required data
	 *  components with the required sizes (according to the number of dependent variables in the
	 *  problem).
	 */
	public GEPDependentVariable() 
	{
	}
	
	/** This will hold the dependent variable information for the training data
	 */
	public static GEPDependentVariable trainingData = new GEPDependentVariable();

	/** This will hold the dependent variable information for the testing data
	 */
	public static GEPDependentVariable testingData = new GEPDependentVariable();


	/** Must store the values for the Dependent Variable(s) of the problem so evaluations can 
	 *  be performed on expressions using this terminal symbol. 
	 */
	public double values[][] = null;
	
	/**
	 * The symbol to use when displaying the dependent variable in human readable form.
	 */
	static String symbol = "dependentVariable";
	
    /** Mean value of the training values for the dependent variable */
	public double dependentVariableMean[] = null;
    
    /** Sum of the absolute error between dependent variables training values and their mean value. The
     *  absolute error is Abs(dv[i] - dvMean).
     */
	public double dependentVariableSumOfAbsoluteError[] = null;
    /** Sum of the squares of the absolute errors between dependent variables training values and their mean 
     *  value. The absolute error is Abs(dv[i] - dvMean).
     */
	public double dependentVariableSumOfSquaredAbsoluteError[] = null;
    /** Sum of the relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	public double dependentVariableSumOfRelativeError[] = null;
    /** Sum of the squares relative errors between dependent variables training values and their mean value.
     *  The relative error is Abs((dv[i] - dvMean)/dvMean).
     */
	public double dependentVariableSumOfSquaredRelativeError[] = null;
    
    /** Variance of the training values for the dependent variable */
	public double dependentVariableVariance[] = null;
    
    
	/**
	 * Once we know how many dependent variables there will be we can assign the arrays
	 * for the training and testing values and the special statistic values for each 
	 * dependent variable.
	 * 
	 * @param numberOfDependentVariables
	 */
	public void setup( int numberOfDependentVariables )
	{
		values = new double[numberOfDependentVariables][];
		dependentVariableMean = new double[numberOfDependentVariables];
		dependentVariableSumOfAbsoluteError = new double[numberOfDependentVariables];
		dependentVariableSumOfSquaredAbsoluteError = new double[numberOfDependentVariables];
		dependentVariableSumOfRelativeError = new double[numberOfDependentVariables];
		dependentVariableSumOfSquaredRelativeError = new double[numberOfDependentVariables];
		dependentVariableVariance = new double[numberOfDependentVariables];
	}
	
	/**
	 * Copy the values provided so the data for the (only) dependent variable is 
	 * available as required. Also calculate a set of useful statistical values 
	 * for the dependent variable values. Normally used in the 'normal' case
	 * when there is only one dependent variable (one chromosome).
	 * 
	 * @param vals - the training values for this terminal symbol 
	 */
	public void setValues( double vals[] )
	{
		setValues( vals, 0 );
	}

	/**
	 * Copy the values provided so the data for the specified dependent variable is 
	 * available as required. Also calculate a set of useful statistical values 
	 * for the dependent variable values.
	 * 
	 * @param vals - the values for this terminal symbol 
	 * @param depVarIndex - which dependent variable are we storing values for
	 */
	public void setValues( double vals[], int depVarIndex )
	{
		values[depVarIndex] = new double[vals.length];
		System.arraycopy(vals, 0, values[depVarIndex], 0, vals.length);
		calculateDependentVariableStats( depVarIndex );
	}
	
	/**
	 * Calculate a set of useful statistical values 
	 * for the dependent variable values.
	 * 
	 * @param depVarIndex - which dependent variable are we storing values for
	 */
	public void calculateDependentVariableStats( int depVarIndex )
	{
		if (values[depVarIndex]==null) // should not happen for the training data ... will be tested earlier
			return;
    	// now we can calculate the mean and variance etc. for the dependent variable values
        double sumOfDV = 0.0;
        for (int i=0; i<values[depVarIndex].length; i++)
        	sumOfDV += values[depVarIndex][i];        
        dependentVariableMean[depVarIndex] = sumOfDV/values[depVarIndex].length;
        
        dependentVariableSumOfAbsoluteError[depVarIndex] = 0.0;
        dependentVariableSumOfSquaredAbsoluteError[depVarIndex] = 0.0;
        dependentVariableSumOfRelativeError[depVarIndex] = 0.0;
        dependentVariableSumOfSquaredRelativeError[depVarIndex] = 0.0;
        for (int i=0; i<values[depVarIndex].length; i++)
        {	
        	double diff = values[depVarIndex][i] - dependentVariableMean[depVarIndex];
	        dependentVariableSumOfAbsoluteError[depVarIndex] += Math.abs(diff);
	        dependentVariableSumOfSquaredAbsoluteError[depVarIndex] += diff*diff;
	        double diffRelative = diff/dependentVariableMean[depVarIndex];
	        dependentVariableSumOfRelativeError[depVarIndex] += Math.abs(diffRelative);
	        dependentVariableSumOfSquaredRelativeError[depVarIndex] += diffRelative*diffRelative;        
        }
        dependentVariableVariance[depVarIndex] = dependentVariableSumOfSquaredAbsoluteError[depVarIndex]/values[depVarIndex].length;
	}

	/** A function to get the data values (training or testing) for the specified dependent variable.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  included in a file or by asking the User's GEPProgram to supply them via a call to the method
     *  setTerminalSymbolDataValues that must be provided with the User's GEPProgram.
	 * @param depVarIndex - which dependent variable
     * @return the dependent variable values in an array
     */
    public double[] getDependentVariableValues( int depVarIndex )
    {
    	   return values[depVarIndex];
    }

    /** A function to get the (testing or training) values for the dependent variable.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  included in a file or by asking the User's GEPProgram to supply them via a call to the method
     *  setTerminalSymbolDataValues that must be provided with the User's GEPProgram.
     *  Assumes only 1 dependent variable.
     * @return the dependent variable values in an array
     */
    public double[] getDependentVariableValues( )
    {
    	   return values[0];
    }

    /** A function to get the specified dependent variable's mean value.
	 * @param depVarIndex - which dependent variable
     * @return the mean of the set of values of the dependent variable
     */
    public double getDependentVariableMean( int depVarIndex )
    {
     	   return dependentVariableMean[depVarIndex];
    }

    /** A function to get the mean value for the 1st (probably only) dependent variable.
     * @return the mean of the set of values of the dependent variable
     */
    public double getDependentVariableMean( )
    {
     	   return dependentVariableMean[0];
    }

    /** A function to get the sum of the absolute errors for the 1st (probably only) dependent variable.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
     * @return the sum of the absolute error of the set of values of the dependent variable
     */
    public double getDependentVariableSumOfAbsoluteError( )
    {
      	   return dependentVariableSumOfAbsoluteError[0];
    }

    /** A function to get the dependent variable's sum of the absolute errors.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
	 * @param depVarIndex - which dependent variable
     * @return the sum of the absolute error of the set of values of the dependent variable
     */
    public double getDependentVariableSumOfAbsoluteError( int depVarIndex )
    {
      	   return dependentVariableSumOfAbsoluteError[depVarIndex];
    }

    /** A function to get the dependent variable's sum of the squares of the absolute errors.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
	 * @param depVarIndex - which dependent variable
     * @return the sum of the squares of the absolute error of the set of values of the dependent variable
     */
    public double getDependentVariableSumOfSquaredAbsoluteError( int depVarIndex )
    {
       	   return dependentVariableSumOfSquaredAbsoluteError[depVarIndex];
    }

    /** A function to get the sum of the squares of the absolute errors for the 1st (probably only) dependent variable.
     *  The absolute error for any dependent variable value is relative to the mean of the dependent 
     *  variable values.
     * @return the sum of the squares of the absolute error of the set of values of the dependent variable
     */
    public double getDependentVariableSumOfSquaredAbsoluteError( )
    {
       	   return dependentVariableSumOfSquaredAbsoluteError[0];
    }

    /** A function to get the dependent variable's sum of the relative errors.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
	 * @param depVarIndex - which dependent variable
     * @return sum of dependent variable's relative error
     */
    public double getDependentVariableSumOfRelativeError( int depVarIndex )
    {
        	return dependentVariableSumOfRelativeError[depVarIndex];
    }

    /** A function to get the sum of the relative errors for the 1st (probably only) dependent variable.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
     * @return sum of dependent variable's relative error
     */
    public double getDependentVariableSumOfRelativeError( )
    {
        	return dependentVariableSumOfRelativeError[0];
    }

    /** A function to get the dependent variable's sum of the squares of the relative errors.
     * @return sum of dependent variable's sum of squared relative error
     */
    public double getDependentVariableSumOfSquaredRelativeError( int depVarIndex )
    {
        	return dependentVariableSumOfSquaredRelativeError[depVarIndex];
    }

    /** A function to get the sum of the squares of the relative errors for the 1st (probably only) dependent variable.
     * @return sum of dependent variable's sum of squared relative error
     */
    public double getDependentVariableSumOfSquaredRelativeError( )
    {
        	return dependentVariableSumOfSquaredRelativeError[0];
    }

    /** A function to get the dependent variable's variance value stored in the GEPspecies SymbolSet.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
     * @return sum of dependent variable's variance
     */
    public double getDependentVariableVariance( int depVarIndex )
    {
        	return dependentVariableVariance[depVarIndex];
    }

    /** A function to get the variance value stored in the GEPspecies SymbolSet for the 1st (probably only) dependent variable.
     *  The values for the dependent variable GEPTerminalSymbol are set when the data values are 
     *  stored in the TeminalSymbols for the independent variables and the dependent variable.
     *  There is only one symbolSet for the GEPSpecies and the dependent variable is the same for all 
     *  individuals in the population.
     * @return  sum of dependent variable's variance
     */
    public double getDependentVariableVariance( )
    {
        	return dependentVariableVariance[0];
    }

}
