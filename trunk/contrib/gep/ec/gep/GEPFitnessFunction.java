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

import java.util.*;
import com.csvreader.*;

import ec.EvolutionState;
import ec.util.Parameter;


/**

 /*
 * @author Bob Orchard
 */

/**
 * This class provides a set of static functions that support all (or almost all) of the gep
 * fitness functions found in the commercial version of gep, GeneXProTools 4.0 plus a few extra that we've
 * added. 
 * <li>
 * They include:
 * <pre><code>
 *  AEWSR (Absolute Error with Selection Range) 
 *  AH (Absolute/Hits)
 *  MSE (mean squared error) 
 *  RMSE (root mean squared error) 
 *  MAE (mean absolute error) 
 *  RSE (relative squared error) 
 *  RRSE (root relative squared error) 
 *  RAE (relative absolute error) 
 *  REWSR (Relative Error with Selection Range) 
 *  RH (Relative/Hits) 
 *  rMSE (relative MSE) 
 *  rRMSE (relative RMSE) 
 *  rMAE (relative MAE) 
 *  rRSE (relative RSE) 
 *  rRRSE (relative RRSE) 
 *  rRAE (relative RAE) 
 *  RS (R-square) 
 *  CC (Correlation Coefficient) 
 *  NH (Number of Hits) 
 *  NHWP(Number of Hits with Penalty) 
 *  ACC (Accuracy) 
 *  SACC (Squared Accuracy) 
 *  SS (Sensitivity/Specificity) 
 *  PPNVP (PPV/NPV Positive Predictive Value / Negative Predictive Value ) 
 *  SSPN (Sensitivity/Specificity/PPV/NPV ) 
 *  WCorrRMSE (Weighted correlation coefficient and Root Mean Squared Error)
 *  
 *  (Multiple chromosome fitness functions)
 *  MCSCE1   (Simple classification error for multiple chromosome classification problems)
 *  MCSCE2   (Simple classification error for multiple chromosome classification problems)
 *  MCSCE3   (Simple classification error for multiple chromosome classification problems)
 *  MCMSE    (MultipleChromosome Mean Squared Error) 
 *  SEWD     (Sammon Error With Dissimilarity)
 *
 * </code></pre>
 * For each of the fitness functions there are 3 methods: one to calculate the fitness 
 * (with a value from 0 to some maximum); one to give the raw fitness value, prior
 * to being mapped between 0 and the maximum value; one to provide the maximum value 
 * for that fitness function type. By convention we use the names XXXXfitness, XXXXrawFitness
 * and XXXXmaxFitness for all of the fitness functions. XXXX is the pneumonic for the 
 * fitness function (RRSE, MAE, etc.). This allows a user to specify a fitness function to be
 * used by the default user prog (they don't provide ANY code if they use this) using
 * these short names. The XXXXmaxFitness functions (almost) all have 1 arg when most don't need one. 
 * This was just to make it easier to call the method based on the XXXX value only. See
 * GEPDefaultUserProg.java.
 * <li>
 * For example the functions for the MSE fitness function are called as:
 * <li>
 * <pre><code>
 * 		GEPFitnessFunction.MSEfitness( useTrainingData, gepindividual );
 *			- calculates the fitness using the individual's gene expressions and either the testing or training data
 *			- it gets the raw fitness first from MSErawFitness (the mean squared error
 *			  of the predicted values versus the expected values)
 *			- then it normalizes the result between 0 and 1000  
 *				(1000 * (1/(1 + raw MSE))
 *		GEPFitnessFunction.MSErawFitness(useTrainingData, gepindividual );
 *			- sum((predicted valuei - expected valuei)**2)/n
 *		GEPFitnessFunction.NHmaxFitness( gepindividual );
 *			- in this case max is always 1000
 * </code></pre>
 * <li>
 * A few fitness functions take 1 or more extra double parameters. These must be passed to the XXXXfitness
 * method in an array of doubles. Again this is to make the GEPDefaultUserProg simpler to implement.
 * Users will not find any problems but if you add new fitness functions with extra double parameters 
 * you must use the double array to pass them, even if there is only 1 extra double value. See the 
 * AH (AHfitness) fitness function for example.
 */
public final class GEPFitnessFunction
{
	/** An amount to add to predicted and expected (test) values when the expected value
	 *  is 0.0 and the fitness function is using a relative calculation. This avoids
	 *  a division by 0 but users should note that relative error may not be what they
	 *  want when this happens.
	 */
	static double RELATIVE_ERROR_ZERO_FACTOR = 0.000000001;
	
	/**
	 * The default values used to determine the parsimony pressure applied when
	 * adjusting the fitness to prefer smaller expressions. Most times this default
	 * is fine and using the parsimonyfitness(individual, fitness) method is Ok. 
	 * If another value is to be used then use the method 
	 * parsimonyfitness(individual, fitness, parsimonyPressureFactor) should be used.
	 */
	static double PARSIMONY_PRESSURE_FACTOR = 5000.0;
	
	/**
	 * Perform any special things that might be required during setup so that the 
	 * fitness functions have all the required information.
	 * <p>
	 * If we are using multiple chromosomes and using the SEWD fitness function 
	 * (Sammon Error With Dissimilarity) then the user must provide a 'dissimilarity'
	 * matrix that is pre-calculated. This matrix must be in a file identified in 
	 * the params file by the parameter gep.fitness.dissimilarity-matrix.
	 * The file is a comma separated values file with:
	 * <br>
	 * <pre><code>
	 * row 1:  number of rows in the matrix
	 * row 2:  value of the diagonal of the matrix
	 * row 3+: values for each row in the diagonal matrix (excluding the diagonal value)
	 * 
	 * So we might have something like:
	 * 4                     - number of rows
	 * 3.56                  - row 1 (diagonal value)
	 * 2.33                  - row 2  
	 * 4.56, 5.67            - row 3
	 * 6.67, 8.99, 10.66     - row 4
	 * </code><pre>
	 * The matrix is stored in the static variable originalDisimMatrix.
	 * 
	 * @param state - provides access to the parameters so we can get gep.fitness.dissimilarity-matrix.
	 * 
	 */
	public static void setup(final EvolutionState state)
	{
		// check parameter gep.fitness.dissimilarity-matrix for file with dissimilarity matrix
		Parameter p = GEPDefaults.base().push("fitness").push("dissimilarity-matrix");
		String filename = state.parameters.getStringWithDefault(p, p, "");
		
		if (filename.equals(""))
			return;
		
		/* special setup for certain fitness functions */
        CsvReader disimMatrixFileCSV = null;
    	try 
    	{ 
    		 disimMatrixFileCSV = new CsvReader(filename);
    	}
    	catch (FileNotFoundException e)
    	{ System.out.println("WARNING: The dissimilarity matrix data file could not be found");
    		return;
    	}
    	disimMatrixFileCSV.setDelimiter(',');
		try
		{ disimMatrixFileCSV.readRecord(); // get number of rows
	      int nrows = Integer.parseInt(disimMatrixFileCSV.get(0));
	      originalDisimMatrix = new double[nrows][];
	      originalDisimMatrix[0] = new double[1];
	      disimMatrixFileCSV.readRecord(); // get diagonal value
	      originalDisimMatrix[0][0] = Double.parseDouble(disimMatrixFileCSV.get(0));
		  for (int i=1; i<nrows; i++)
		  {	originalDisimMatrix[i] = new double[i];
	        disimMatrixFileCSV.readRecord(); // get row values
			for (int j=0; j<i; j++) // store row values
			    originalDisimMatrix[i][j] = Double.parseDouble(disimMatrixFileCSV.get(j));
		  }
		}
		catch (IOException e)
		{  System.out.println("Problem reading the dissimilarity matrix" + e);
		}
	
	    for (int i=1; i<originalDisimMatrix.length; i++)
		   for (int j=0; j<originalDisimMatrix[i].length; j++)
			   sumOfTheLowerTriangle += originalDisimMatrix[i][j];
	}
	
    //************************* RAE (Relative Absolute Error) *****************
	

	/**
	 * Calculates the 'raw' fitness for the RAE (Relative Absolute Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RAE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RAErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        double dvSumOfAbsoluteError = dv.getDependentVariableSumOfAbsoluteError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            error = result - expectedResult;
            sumOfAbsoluteError += Math.abs(error); 
        }
        
        if (dvSumOfAbsoluteError == 0.0)
        {   dvSumOfAbsoluteError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of error for dependent variable is 0 in RAE fitness calculation. Adjusting to avoid division by zero.");
        }
        // the raw fitness ... RAE
        return (sumOfAbsoluteError/dvSumOfAbsoluteError);
	}
    	
	/**
	 * Calculates the fitness for the RAE (Relative Absolute Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+RAE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RAEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double RAE = RAErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+RAE))
        return (1000.0)/(1.0+RAE);
	}


	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double RAEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rRAE (relative RAE) *****************
	

	/**
	 * Calculates the 'raw' fitness for the rRAE (relative RAE) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rRAE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRAErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        double dvSumOfRelativeError = dv.getDependentVariableSumOfRelativeError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rRAE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfRelativeError += Math.abs(error); 
        }
        if (dvSumOfRelativeError == 0.0)
        {   dvSumOfRelativeError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of error for dependent variable is 0 in rRAE fitness calculation. Adjusting to avoid division by zero.");
        }
        // the raw fitness ... rRAE
        return (sumOfRelativeError/dvSumOfRelativeError);
	}
    
	/**
	 * Calculates the fitness for the rRAE (relative RAE) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rRAE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRAEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rRAE = rRAErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rRAE))
        return (1000.0)/(1.0+rRAE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rRAEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* MAE (Mean Absolute Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the MAE (Mean Absolute Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw MAE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double MAErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            error = result - expectedResult;
            sumOfAbsoluteError += Math.abs(error); 
        }
        // the raw fitness ... MAE
        return (sumOfAbsoluteError/dvValues.length);
	}
 
	/**
	 * Calculates the fitness for the MAE (Mean Absolute Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+MAE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double MAEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double MAE = MAErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+MAE))
        return (1000.0)/(1.0+MAE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MAEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rMAE (relative Mean Absolute Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the rMAE (relative Mean Absolute Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rMAE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rMAErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rMAE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfRelativeError += Math.abs(error); 
        }
        // the raw fitness ... rMAE
        return (sumOfRelativeError/dvValues.length);
	}
    	
	/**
	 * Calculates the fitness for the rMAE (relative Mean Absolute Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rMAE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rMAEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rMAE = rMAErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rMAE))
        return (1000.0)/(1.0+rMAE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rMAEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* MSE (Mean Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the MSE (Mean Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done) when there
	 * are multiple chromosomes (dep vars). Just does the avg of the MSE for each dep var.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw MSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double MSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error * error; 
        }
        // the raw fitness ... MSE
        return (sumOfSquaredAbsoluteError/dvValues.length);
	}
    	
    	
	/**
	 * Calculates the fitness for the MSE (Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+MSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double MSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double MSE = MSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+MSE))
        return (1000.0)/(1.0+MSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rMSE (relative Mean Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the rMSE (relative Mean Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rRMSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rMSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rMSE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += error * error; 
        }
        // the raw fitness ... rMSE
        return (sumOfSquaredRelativeError/dvValues.length);
	}
    	
	/**
	 * Calculates the fitness for the rMSE (relative Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rMSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rMSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rMSE = rMSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rMSE))
        return (1000.0)/(1.0+rMSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rMSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* RMSE (Root Mean Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RMSE (Root Mean Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RMSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RMSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            if (Double.isInfinite(result) || Double.isNaN(result))
            	return(Double.NaN);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error * error; 
        }
        // the raw fitness ... RMSE
        return Math.sqrt(sumOfSquaredAbsoluteError/dvValues.length);
	}
    	
	/**
	 * Calculates the fitness for the RMSE (Root Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+RMSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RMSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double RMSE = RMSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+RMSE))
        return (1000.0)/(1.0+RMSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double RMSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rRMSE (relative Root Mean Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the rRMSE (relative Root Mean Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rRMSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRMSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rRMSE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += error * error; 
        }
        // the raw fitness ... rRMSE
        return Math.sqrt(sumOfSquaredRelativeError/dvValues.length);
	}
    	
	/**
	 * Calculates the fitness for the rRMSE (relative Root Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rRMSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRMSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rRMSE = rRMSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rRMSE))
        return (1000.0)/(1.0+rRMSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rRMSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* RSE (Relative Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RSE (Relative Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double dvSumOfSquaredAbsoluteError = dv.getDependentVariableSumOfSquaredAbsoluteError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error*error; 
        }
        // the raw fitness ... RSE
        if (dvSumOfSquaredAbsoluteError == 0.0)
        {   dvSumOfSquaredAbsoluteError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of squared error for dependent variable is 0 in RSE fitness calculation. Adjusting to avoid division by zero.");
        }
        return (sumOfSquaredAbsoluteError/dvSumOfSquaredAbsoluteError);
	}
    	
	/**
	 * Calculates the fitness for the RSE (Relative Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+RSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double RSE = RSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+RSE))
        return (1000.0)/(1.0+RSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double RSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rRSE (relative RSE) *****************
	
	/**
	 * Calculates the 'raw' fitness for the rRSE (relative RSE) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rRSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double relativeError;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double dvSumOfSquaredRelativeError = dv.getDependentVariableSumOfSquaredRelativeError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rRSE fitness calculation. Adjusting to avoid division by zero.");
            }
            relativeError = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += relativeError*relativeError; 
        }
        // the raw fitness ... rRSE
        if (dvSumOfSquaredRelativeError == 0.0)
        {   dvSumOfSquaredRelativeError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of squared relative error for dependent variable is 0 in rRSE fitness calculation. Adjusting to avoid division by zero.");
        }
        return (sumOfSquaredRelativeError/dvSumOfSquaredRelativeError);
	}
    	
	/**
	 * Calculates the fitness for the rRSE (Relative RSE) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rRSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rRSE = rRSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rRSE))
        return (1000.0)/(1.0+rRSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rRSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* RRSE (Root Relative Squared Error) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RRSE (Root Relative Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RRSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RRSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double dvSumOfSquaredAbsoluteError = dv.getDependentVariableSumOfSquaredAbsoluteError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error*error; 
        }
        // the raw fitness ... RRSE
        if (dvSumOfSquaredAbsoluteError == 0.0)
        {   dvSumOfSquaredAbsoluteError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of squared error for dependent variable is 0 in RRSE fitness calculation. Adjusting to avoid division by zero.");
        }
        return Math.sqrt(sumOfSquaredAbsoluteError/dvSumOfSquaredAbsoluteError);
	}
    	
	/**
	 * Calculates the fitness for the RRSE (Root Relative Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+RRSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RRSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double RRSE = RRSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+RRSE))
        return (1000.0)/(1.0+RRSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double RRSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* rRRSE (relative RRSE) *****************
	
	/**
	 * Calculates the 'raw' fitness for the rRRSE (relative Root Relative Squared Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw rRRSE
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRRSErawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double relativeError;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double dvSumOfSquaredRelativeError = dv.getDependentVariableSumOfSquaredRelativeError(chromosomeNum);

        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            result = ind.eval(chromosomeNum, useTrainingData, i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rRRSE fitness calculation. Adjusting to avoid division by zero.");
            }
            relativeError = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += relativeError*relativeError; 
        }
        // the raw fitness ... rRRSE
        if (dvSumOfSquaredRelativeError == 0.0)
        {   dvSumOfSquaredRelativeError = RELATIVE_ERROR_ZERO_FACTOR;
        	System.err.println("Warning: sum of squared relative error for dependent variable is 0 in rRRSE fitness calculation. Adjusting to avoid division by zero.");
        }
        return Math.sqrt(sumOfSquaredRelativeError/dvSumOfSquaredRelativeError);
	}
    	
	/**
	 * Calculates the fitness for the rRRSE (relative Root Relative Squared Error) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * (1/(1+rRRSE)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRRSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double rRRSE = rRRSErawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+rRRSE))
        return (1000.0)/(1.0+rRRSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double rRRSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* CC (Correlation Coefficient) *****************
	
	/**
	 * Calculates the 'raw' fitness for the CC (Correlation Coefficient) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw CC
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double CCrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double dvVariance = dv.getDependentVariableVariance(chromosomeNum);
    	double dvMean = dv.getDependentVariableMean(chromosomeNum);
    	double dvStdDev = Math.sqrt(dvVariance);

    	// mean of the calculated (predicted) values
    	double sumOfPredictedValues = 0.0;
    	double predictedValues[] = new double[dvValues.length];
        for (int i=0; i<dvValues.length; i++)
        {
        	predictedValues[i] = ind.eval(chromosomeNum, useTrainingData, i);
        	sumOfPredictedValues += predictedValues[i];
        }
        double meanOfPredictedValues = sumOfPredictedValues/dvValues.length;
        
        double sum1 = 0.0;
        double sum2 = 0.0;
        for (int i=0; i<dvValues.length; i++)
        {
            expectedResult = dvValues[i];
            predictedValue = predictedValues[i];
            double diff = (predictedValue-meanOfPredictedValues);
            sum1 += (expectedResult-dvMean)*(diff);
            sum2 += diff*diff;
        }
        double covariance = sum1/dvValues.length;
        double stdDev = Math.sqrt(sum2/dvValues.length);
        
        // the raw fitness ... CC
        double cc = covariance/(dvStdDev*stdDev);
        return Math.min(1.0, Math.max(cc, -1.0)); // in case of math imprecision in calculations
	}
    	
	/**
	 * Calculates the fitness for the CC (Correlation Coefficient) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (500 * (CC+1)).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double CCfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double CC = CCrawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (500 * (CC +1))
        // CC can be between -1.0 and 1.0
        return (500.0 * (CC+1.0));
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double CCmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* RS (R-Squared) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RS (R-Squared) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RS
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RSrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	double sumOfPV = 0.0;
    	double sumOfPVtimesDV = 0.0;
    	double sumOfPVsquared = 0.0;
    	double sumOfDVsquared = 0.0;
    	double sumOfDV = 0.0;
    	int len = dvValues.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	sumOfPV += predictedValue;
        	sumOfPVsquared += predictedValue*predictedValue;
        	expectedResult = dvValues[i];
        	sumOfDV += expectedResult;
        	sumOfDVsquared += expectedResult*expectedResult;
        	sumOfPVtimesDV += predictedValue*expectedResult;
        }
        
        double top = (dvValues.length*sumOfPVtimesDV) - (sumOfDV*sumOfPV);
        double bottom = Math.sqrt(((len*sumOfDVsquared)-(sumOfDV*sumOfDV)) * ((len*sumOfPVsquared)-(sumOfPV*sumOfPV)));
                
        // the raw fitness ... RS
        if (bottom == 0.0)
        {   bottom = RELATIVE_ERROR_ZERO_FACTOR;
    	    System.err.println("Warning: denominator is 0 in RS (R-Squared) fitness calculation. Adjusting to avoid division by zero.");
        }
        	
        return top/bottom;
	}
    	
	/**
	 * Calculates the fitness for the RS (R-Squared) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value as (maxValue * RS * RS).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RSfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double RS = RSrawFitness(useTrainingData, ind, 0);
        // raw fitness is normalized between 0 and 1000  (1000 * RS * RS)
        // RS can be between -1.0 and 1.0
        return (1000.0 * RS * RS);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double RSmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

    //************************* AH (Absolute/Hits) *****************
	
	/**
	 * Calculates the 'raw' fitness for the AH (Absolute/Hits) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw AH
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double AHrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum, double precision) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        double sumOfHits = 0.0;
    	int len = dvValues.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to AHfitness, setting to -precision.");
    	}
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	expectedResult = dvValues[i];
        	if (Math.abs(predictedValue-expectedResult) <= precision)
        	    sumOfHits += 1.0;
        }        
        // the raw fitness ... AH
        return sumOfHits;
	}
    	
	/**
	 * Calculates the fitness for the AH (Absolute/Hits) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (number of test cases).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision double array expected to have a single value that is the 
	 *        percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double AHfitness(boolean useTrainingData, GEPIndividual ind, double precision[]) 
    {
		if (precision.length != 1)
			System.err.println("Warning: 2nd arg to AHfitness method expected to have 1 double value (precision) but has " + precision.length);
        double AH = AHrawFitness(useTrainingData, ind, 0, precision[0]);
        // fitness is between 0 and the number of test cases
        return (AH);
	}

	/**
	 * The max value for this type of fitness is the length of the test data set.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value length of the test data set
	 */
	public static double AHmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
			                      GEPDependentVariable.testingData.getDependentVariableValues().length);
	}

    //************************* RH (Relative/Hits) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RH (Relative/Hits) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw RH
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RHrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum, double precision) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        double sumOfHits = 0.0;
    	int len = dvValues.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
		    System.err.println("Warning: precision (" + precision + ") < 0 in call to RHfitness, setting to -precision.");
    	}
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	expectedResult = dvValues[i];
        	if (expectedResult == 0.0)
        	{
        		expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
        		predictedValue += RELATIVE_ERROR_ZERO_FACTOR;
        		System.err.println("Warning: expected result = 0 in calculation of RHfitness, adjusting to avoid division by zero.");
        	}
        	if (Math.abs(((predictedValue-expectedResult)/expectedResult)*100.0) <= precision)
        	    sumOfHits += 1.0;
        }        
        // the raw fitness ... RH
        return sumOfHits;
	}
    	
	/**
	 * Calculates the fitness for the RH (Relative/Hits) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (number of test cases).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision double array expected to have a single value that is the 
	 *        percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RHfitness(boolean useTrainingData, GEPIndividual ind, double precision[]) 
    {
		if (precision.length != 1)
			System.err.println("Warning: 2nd arg to RHfitness method expected to have 1 double value (precision) but has " + precision.length);
        double RH = RHrawFitness(useTrainingData, ind, 0, precision[0]);
        // fitness is between 0 and the number of test cases
        return (RH);
	}

	/**
	 * The max value for this type of fitness is the length of the test data set.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double RHmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
                                  GEPDependentVariable.testingData.getDependentVariableValues().length);
	}

    //************************* AEWSR (Absolute Error with Selection Range) *****************
	
	/**
	 * Calculates the 'raw' fitness for the AEWSR (Absolute Error with Selection Range) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw AEWSR
	 * @param range range for the fitness calculation
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double AEWSRrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum, double range, double precision) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	int len = dvValues.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to AEWSR fitness, setting to -precision.");
    	}
    	
    	double totalError = 0.0;
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	expectedResult = dvValues[i];
        	double err = Math.abs(predictedValue-expectedResult);
        	if (err <= precision)
        	    err = 0.0;
        	totalError += range - err;
        }        
        // the raw fitness ... AEWSR
        return totalError;
	}
    	
	/**
	 * Calculates the fitness for the AEWSR (Absolute Error with Selection Range) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (see code details).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range_precision an array with 2 values expected - 1st the range for the fitness calculation
	 *        and 2nd the precision specified as a percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */

	static double AEWSRrange = 1.0;
	public static double AEWSRfitness(boolean useTrainingData, GEPIndividual ind, double range_precision[]) 
    {
		if (range_precision.length != 2)
			System.err.println("Warning: 2nd arg to AEWSRfitness method expected to have 2 double values (range and precision) but has " + range_precision.length);
		AEWSRrange = range_precision[0];
		double precision = range_precision[1];
        double AEWSR = AEWSRrawFitness(useTrainingData, ind, 0, AEWSRrange, precision);
        // fitness is between 0 and the number of test cases
        return (AEWSR);
	}

	/**
	 * The max value for this type of fitness is range * length of the test data set.
	 * In this case range is not specified so use the value set when AEWSTfitness was last called.
	 * This version of the method is here to support the use of this fitness function from the GEPDefaultUserProg.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return range * length of the test data set
	 */
	public static double AEWSRmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		int len = (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
            GEPDependentVariable.testingData.getDependentVariableValues().length);
		return (len * AEWSRrange);
	}

	/**
	 * The max value for this type of fitness is range * length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range value of the range used when the fitness was calculated; 1st value in double array
	 * @return range * length of the test data set
	 */
	public static double AEWSRmaxFitness(boolean useTrainingData, GEPIndividual ind, double range[]) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		int len = (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
            GEPDependentVariable.testingData.getDependentVariableValues().length);
		return (len * range[0]);
	}

    //************************* REWSR (Relative Error with Selection Range) *****************
	
	/**
	 * Calculates the 'raw' fitness for the REWSR (Relative Error with Selection Range) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw REWSR
	 * @param range range for the fitness calcualtion
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double REWSRrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum, double range, double precision) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	int len = dvValues.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to REWSR fitness, setting to -precision.");
    	}
    	
    	double totalError = 0.0;
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	expectedResult = dvValues[i];
        	if (expectedResult == 0.0)
        	{
        		expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
        		predictedValue += RELATIVE_ERROR_ZERO_FACTOR;
        		System.err.println("Warning: expected result = 0 in calculation of REWSRfitness, adjusting to avoid division by zero.");
        	}
        	double err = Math.abs(((predictedValue-expectedResult)/expectedResult)*100.0);
        	if (err <= precision)
        	    err = 0.0;
        	totalError += range - err;
        }        
        // the raw fitness ... REWSR
        return totalError;
	}
    	
	/**
	 * Calculates the fitness for the REWSR (Relative Error with Selection Range) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (see code for details).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range_precision an array with 2 values expected - 1st the range for the fitness calculation
	 *        and 2nd the precision specified as a percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	static double REWSRrange = 1.0;
	public static double REWSRfitness(boolean useTrainingData, GEPIndividual ind, double range_precision[]) 
    {
		if (range_precision.length != 2)
			System.err.println("Warning: 2nd arg to REWSRfitness method expected to have 2 double values (range and precision) but has " + range_precision.length);
		REWSRrange = range_precision[0];
		double precision = range_precision[1];
        double REWSR = REWSRrawFitness(useTrainingData, ind, 0, REWSRrange, precision);
        // fitness is between 0 and the number of test cases
        return (REWSR);
	}

	/**
	 * The max value for this type of fitness is range * length of the test data set.
	 * In this case range is not specified so use the value set when AEWSTfitness was last called.
	 * This version of the method is here to support the use of this fitness function from the GEPDefaultUserProg.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return range * length of the test data set
	 */
	public static double REWSRmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		int len = (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
            GEPDependentVariable.testingData.getDependentVariableValues().length);
		return (len * REWSRrange);
	}
	

    //************************* NH (Number of Hits) *****************
	
	/**
	 * Calculates the 'raw' fitness for the NH (Number of Hits) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw NH
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 * Assumes that values are 1 or 0
	 */
	public static double NHrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        double sumOfHits = 0.0;
    	int len = dvValues.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
        	expectedResult = dvValues[i];
        	if (predictedValue == expectedResult)
        	    sumOfHits += 1.0;
        }        
        // the raw fitness ... NH
        return sumOfHits;
	}
    	
	/**
	 * Calculates the fitness for the NH (Number of Hits) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (number of test cases).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 * Assumes that values are 1 or 0
	 */
	public static double NHfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double NH = NHrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and the number of test cases
        return (NH);
	}

	/**
	 * The max value for this type of fitness is length of the test data set.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double NHmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
                                  GEPDependentVariable.testingData.getDependentVariableValues().length);
	}

    //************************* NHWP (Number of Hits with Penalty) *****************
	
	/**
	 * Calculates the 'raw' fitness for the NHWP (Number of Hits with Penalty) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw NHWP
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double NHWPrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        int confusionMatrix[] = getConfusionMatrixValues(useTrainingData, ind , chromosomeNum);
        int truePositives = confusionMatrix[0];
        int trueNegatives = confusionMatrix[3];
        
        // the raw fitness ... NHWP
        if (truePositives==0 || trueNegatives==0)
            return 0.0;
        
        return (truePositives + trueNegatives);
	}
    	
	/**
	 * Calculates the fitness for the NHWP (Number of Hits with Penalty) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (number of test cases).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double NHWPfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double NHWP = NHWPrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and the number of test cases
        return (NHWP);
	}

	/**
	 * The max value for this type of fitness is length of the test data set.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double NHWPmaxFitness(boolean useTrainingData, GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (useTrainingData ? GEPDependentVariable.trainingData.getDependentVariableValues().length :
                                  GEPDependentVariable.testingData.getDependentVariableValues().length);
	}
	
    //************************* ACC (Accuracy) *****************
	
	/**
	 * Calculates the 'raw' fitness for the ACC (Accuracy) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw ACC
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double ACCrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
    	int len = dvValues.length;
        int confusionMatrix[] = getConfusionMatrixValues(useTrainingData, ind, chromosomeNum );
        int truePositives = confusionMatrix[0];
        int trueNegatives = confusionMatrix[3];
    	
    	// the raw fitness ... ACC
        return ((double)(truePositives + trueNegatives)/(double)len);
	}
    	
	/**
	 * Calculates the fitness for the ACC (Accuracy) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (mavValue * ACC).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double ACCfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double ACC = ACCrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and 1000
        return (1000.0*ACC);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double ACCmaxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}

    //************************* SACC (Squared Accuracy) *****************
	
	/**
	 * Calculates the fitness for the SACC (Squared Accuracy) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (maxValue * ACC * ACC).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SACCfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double ACC = ACCrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and 1000
        return (1000.0*ACC*ACC);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double SACCmaxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}

    //************************* SS (Sensitivity/specificity) *****************
	
	/**
	 * Calculates the 'raw' fitness for the SS (Sensitivity/specificity) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
v	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw SS
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        int confusionMatrix[] = getConfusionMatrixValues(useTrainingData, ind, chromosomeNum );
        int truePositives = confusionMatrix[0];
        int falseNegatives = confusionMatrix[1];
        int falsePositives = confusionMatrix[2];
        int trueNegatives = confusionMatrix[3];
    	
        int TPplusFN = truePositives+falseNegatives;
        int TNplusFP = trueNegatives+falsePositives;
        
        if (TPplusFN==0 || TNplusFP==0)
        	return 0.0;
        
        double SE = (double)truePositives/(double)TPplusFN;
        double SP = (double)trueNegatives/(double)TNplusFP;
        
        // the raw fitness ... SS
        return (SE*SP);
	}
    	
	/**
	 * Calculates the fitness for the SS (Sensitivity/specificity) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (maxValue * SS).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double SS = SSrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and 1000
        return (1000.0*SS);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double SSmaxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}

    //**************** PPVNPV (Positive Predictive Value / Negative Predictive Value ) **********
	
	/**
	 * Calculates the 'raw' fitness for the PPVNPV (Positive Predictive Value / Negative Predictive Value ) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw PPVNPV
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double PPVNPVrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        int confusionMatrix[] = getConfusionMatrixValues(useTrainingData, ind, chromosomeNum );
        int truePositives = confusionMatrix[0];
        int falseNegatives = confusionMatrix[1];
        int falsePositives = confusionMatrix[2];
        int trueNegatives = confusionMatrix[3];
    	
        int TPplusFP = truePositives+falsePositives;
        int TNplusFN = trueNegatives+falseNegatives;
        if (TPplusFP==0 || TNplusFN==0)
        	return 0.0;
        	
        double PPV = (double)truePositives/(double)TPplusFP;
        double NPV = (double)trueNegatives/(double)TNplusFN;
        
        // the raw fitness ... PPVNPV
        return (PPV*NPV);
	}
    	
	/**
	 * Calculates the fitness for the PPVNPV (Positive Predictive Value / Negative Predictive Value ) type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (maxValue * PPVNPV).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double PPVNPVfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double PPVNPV = PPVNPVrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and 1000
        return (1000.0*PPVNPV);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double PPVNPVmaxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}

    //**************** SSPN (Sensitivity/Specificity/PPV/NPV  ) **********
	
	/**
	 * Calculates the 'raw' fitness for the SSPN (Sensitivity/Specificity/PPV/NPV type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param chromosomeNum which chromosome in the individual to use to calc the raw SSPN
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSPNrawFitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
	{
        int confusionMatrix[] = getConfusionMatrixValues(useTrainingData, ind, chromosomeNum );
        int truePositives = confusionMatrix[0];
        int falseNegatives = confusionMatrix[1];
        int falsePositives = confusionMatrix[2];
        int trueNegatives = confusionMatrix[3];
    	
        int TPplusFN = truePositives+falseNegatives;
        int TNplusFP = trueNegatives+falsePositives;
        if (TPplusFN==0 || TNplusFP==0)
        	return 0.0;
        int TPplusFP = truePositives+falsePositives;
        int TNplusFN = trueNegatives+falseNegatives;
        if (TPplusFP==0 || TNplusFN==0)
        	return 0.0;
        	
        double PPV = (double)truePositives/(double)TPplusFP;
        double NPV = (double)trueNegatives/(double)TNplusFN;
        double SE = (double)truePositives/(double)TPplusFN;
        double SP = (double)trueNegatives/(double)TNplusFP;
        
        // the raw fitness ... SSPN
        return (SE*SP*PPV*NPV);
	}
    	
	/**
	 * Calculates the fitness for the SSPN (Sensitivity/Specificity/PPV/NPV type fitness. 
	 * Gets the raw fitness and then normalizes between 0 and max value (maxValue * SSPN).
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSPNfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double SSPN = SSPNrawFitness(useTrainingData, ind, 0);
        // fitness is between 0 and 1000
        return (1000.0*SSPN);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double SSPNmaxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}
	
    //************************* WCorrRMSE (Weighted correlation coefficient and Root Mean Squared Error) *****************
	
	/**
	 * Calculates the fitness for the WCorrRMSE (Weighted correlation coefficient 
	 * and Root Mean Squared Error) type fitness (before the normalization from 
	 * 0 to max value is done). The function calculates the RMSE and the correlation coefficient
	 * (both normalized to a value from 0 to 1000) and uses the weights to calculate a 
	 * weighted average of these ... also between 0 and 1000.
	 * 
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param corrWeight_RMSEweight - array that has RMSEweight, the weight to be applied to the normalized (from 0 to 1000) Correlation Coefficient
	 *        and RMSEweight, the weight to be applied to the normalized (from 0 to 1000) RMSE
	 * @return the fitness value
	 */
	public static double WCorrRMSEfitness(boolean useTrainingData, GEPIndividual ind, double corrWeight_RMSEweight[])
	{
		return WCorrRMSEfitness(useTrainingData, ind, 0, corrWeight_RMSEweight);
	}
	
	public static double WCorrRMSEfitness(boolean useTrainingData, GEPIndividual ind, int chromosomeNum, double corrWeight_RMSEweight[]) 
	{
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        int nSamples = dvValues.length;
        double fitness = 0.0;
    	double modelMinusTargetSquared = 0.0;
    	double MSE = 0.0, RMSE=0.0, sx=0.0,   sx2=0.0,  sxy = 0.0, 
    	       sy = 0.0,  sy2=0.0,  ssx2=0.0, ssy2=0.0, ssxy=0.0;

    	for (int i=0; i<nSamples; i++)
    	{
    		double target = dvValues[i];
    		double model = ind.eval(chromosomeNum, useTrainingData, i);
    		double temp1 = model - target; 
    		modelMinusTargetSquared += temp1 * temp1;

    		//     for the variances
            sx  += model;
            sx2 += model * model;
            sy  += target;
            sy2 += target * target;
            //     for the covariance
            sxy += model * target;
    	} 

    	MSE = modelMinusTargetSquared / nSamples;

        RMSE = Math.sqrt(MSE);
  
        //       the following is positive in [0,1000]
    	double fitnessRMSE = (1000.0/(1.0+RMSE));

    	//      variances
        ssx2 = sx2 - (sx * sx)/nSamples;
        ssy2 = sy2 - (sy * sy)/nSamples;
        ssxy = sxy - (sx * sy)/nSamples;
        //      correlation coeff
        double corr = ssxy/Math.sqrt(ssx2 * ssy2);

        //       the following is positive in [0,1000]
        double fitnessCorr = (corr+1) * 500.0;

        //      FITNESS FUNCTION
        //        
		if (corrWeight_RMSEweight.length != 2)
			System.err.println("Warning: 2nd arg to WCorrRMSEfitness method expected to have 2 double values (correlation weight and RMSE weight) but has " + corrWeight_RMSEweight.length);
		double corrWeight = corrWeight_RMSEweight[0];
		double RMSEweight = corrWeight_RMSEweight[1];

        fitness = (RMSEweight * fitnessRMSE + corrWeight * fitnessCorr)/(RMSEweight + corrWeight);
        
        return fitness;
	}
    	

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double WCorrRMSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}



	
	// ******* Functions for Parsimony pressure ****************************

	/**
	 * Calculates a parsimony fitness value given a normal fitness value. It uses the 
	 * default parsimony pressure factor (PARSIMONY_PRESSURE_FACTOR). This is used 
	 * to try to get the solutions to evolve towards more compact solutions 
	 * with fewer terms in the expression. 
	 * @param ind the GEP individual that needs its fitness adjusted for parsimony
	 * @param fitness the fitness to be adjusted
	 */
	public static double parsimonyFitness(GEPIndividual ind, double fitness)
	{
		return parsimonyFitness(ind, fitness, PARSIMONY_PRESSURE_FACTOR);
	}

	/**
	 * Calculates a parsimony fitness value given a normal fitness value. It uses a 
	 * user defined pressure factor. This is used to try to get the solutions 
	 * to evolve towards more compact solutions with fewer terms in the expression.
	 * It calculates the new fitness as:
	 * <code>
	 * 
	 *   fitness * ( 1 + (      1              (smax - sind) ) )
	 *                     ----------------- * -------------
	 *                     parsimonyPressure   (smax - smin)
	 *                     
	 *   where  smin is number of genes in genome
	 *          smax is size of each gene (size of head  + size of tail)
	 *          sind is the size of the expression in the individual
	 *               (number of nodes in the expression)
	 *   
	 *  </code>
	 * @param ind the GEP individual that needs its fitness adjusted for parsimony
	 * @param fitness the fitness to be adjusted
	 * @param parsimonyFactor the value to be used as the parsimony pressure factor.
	 */
	public static double parsimonyFitness(GEPIndividual ind, double fitness, double parsimonyFactor)
	{
		GEPSpecies gepspecies = (GEPSpecies)ind.species;
		int smin = gepspecies.numberOfGenes;
		int sizeOfGene = gepspecies.geneSize;
		int smax = smin*sizeOfGene;
		int sind = (int)ind.size(); // number of nodes in all of its parsed genes (excluding linking fcns)

		return fitness*(1.0+(((double)(smax-sind))/(((double)(smax-smin))*parsimonyFactor)));
	}
	
	// ******* Functions for confusion matrices ****************************

	/**
	 * Given an individual calculate the true positive (TP), false negative (FN), false positive (FP) and
	 * true negative (TN) values of the model formed by the individual versus the expected values.
	 * 
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the individual for which we want to calculate the confusion matrix values
	 * @return double array with 4 values representing TP FN FP TN respectively
	 */
	public static int[] getConfusionMatrixValues(boolean useTrainingData, GEPIndividual ind) 
	{
		return getConfusionMatrixValues(useTrainingData, ind, 0);
	}
	
	public static int[] getConfusionMatrixValues(boolean useTrainingData, GEPIndividual ind, int chromosomeNum) 
		{
        double expectedResult;
        double predictedValue;
        GEPDependentVariable dv;

        if (useTrainingData) 
        	dv = GEPDependentVariable.trainingData;
        else
        	dv = GEPDependentVariable.testingData;
        
        double dvValues[] = dv.getDependentVariableValues(chromosomeNum);
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
    	int len = dvValues.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(chromosomeNum, useTrainingData, i);
			if (Double.isNaN(predictedValue))
				// set all counts to 0 and return when an invalid value is found
		        return new int[]{0, 0, 0, 0};

        	expectedResult = dvValues[i];
        	if (predictedValue == 1.0)
        		if (expectedResult == 1.0)
        		    truePositives++;
        		else
        		    falsePositives += 1;
        	else // predicted value is 0
        		if (expectedResult == 0.0)
        		    trueNegatives++;
        		else
        			falseNegatives++;
        }        
        int results[] = new int[]{truePositives, falseNegatives, falsePositives, trueNegatives};
        return results;
	}

    //************************* MCSCE1, MCSCE2, MCSCE3 (Multiple Chromosome Simple Classification Error) *****************
	
	
	/**
	 *  MCSCE1contingencyTable   Simple classification error for multiple chromosome classification problems
	   
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   <br>
	   and in each of these rows only 1 of the classi values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
	   <br>
	   NOTE: uses 2 thresholds to determine if the predicted and expected classes are 
	   disciminated clearly enough; the 1st threshold is for the bestValue of the class 
	   predictions; it must equal or exceed this threshold; the 2nd threshold is for the 
	   secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined.

	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the individual for which we want to calculate the confusion matrix values
	 * @param bestValueThreshold 
	 * @param secondBestValueThreshold
	 * @return double array with values representing the matches of predicted versus expected

	*/
	public static int[][] MCSCE1contingencyTable(boolean useTrainingData, GEPIndividual ind, double bestValueThreshold, double secondBestValueThreshold) 
	{
	       int classWithTheMaximumPredictedValue;    /* this will be the predicted class if it meets the specified conditions */
	       int classWithTheSecondBestPredictedValue; 
	        GEPDependentVariable dv;

	        if (useTrainingData) 
	        	dv = GEPDependentVariable.trainingData;
	        else
	        	dv = GEPDependentVariable.testingData;
	        
	       double dvValues[] = dv.getDependentVariableValues();
	       int numberOfObjects          = dvValues.length;
	       int numberOfClasses          = ((GEPSpecies)ind.species).numberOfChromosomes;
	       int mi_ContingencyTable[][]  = new int [numberOfClasses][numberOfClasses+1]; // extra column for unclassified counts
	       double maximumPredictedValue, secondBestPredictedvalue, value;
	       double dependentVar[];
	       double md_ExpectedValues[][] = new double [numberOfObjects][numberOfClasses];

	       // bestValueThreshold must be > secondBestValueThreshold
	       if ( bestValueThreshold < secondBestValueThreshold )
	       System.err.println("Error: MCSCE1 -- first threshold value must be less than the second.");

	       /* construction of the expected class values */
	       for(int j=0; j < numberOfClasses; j++)
	       {    /* get the j-th column */
	            dependentVar = dv.getDependentVariableValues(j);
	            for(int i = 0; i < numberOfObjects; i++)
	               md_ExpectedValues[i][j] = dependentVar[i];
	       } 
	 	                
	       /* construction of the contingency table */
	       for(int i=0; i< numberOfObjects; i++)
	       {
	    	 int expectedClass = 0;
	         classWithTheMaximumPredictedValue = 0;
	         maximumPredictedValue = ind.eval(classWithTheMaximumPredictedValue, useTrainingData, i);
	         secondBestPredictedvalue = maximumPredictedValue;
	         double maximumExpectedValue = md_ExpectedValues[i][expectedClass]; 
			 if (Double.isNaN(maximumPredictedValue))
			 {
				// set all counts to 0 on diagonal and return when an invalid value is found
				for (int ii=0; ii<numberOfClasses; ii++)
					mi_ContingencyTable[ii][ii] = 0;
				return mi_ContingencyTable;
			 }
			 
	         for(int j=1; j < numberOfClasses; j++)
	         {
	             /* determine the class with the largest expected (1) value */
	             value = md_ExpectedValues[i][j];
	             if(value > maximumExpectedValue)
	             { maximumExpectedValue = value;
	               expectedClass        = j;
	             }
	 
	             /* determine the class with the largest predicted value its class */
	             value = ind.eval(j, useTrainingData, i);
			  	 if (Double.isNaN(value))
				 {
					// set all counts to 0 on diagonal and return when an invalid value is found
					for (int ii=0; ii<numberOfClasses; ii++)
						mi_ContingencyTable[ii][ii] = 0;
					return mi_ContingencyTable;
				 }
	             if(value > maximumPredictedValue)
	             { /* update the second best and the absolute best values and the corresp. classes */
	               secondBestPredictedvalue             = maximumPredictedValue;
	               classWithTheSecondBestPredictedValue = classWithTheMaximumPredictedValue;
	               maximumPredictedValue                = value;
	               classWithTheMaximumPredictedValue    = j;
	             }
	         } /* for(j */
	         
	         /* update the contingency table */

	         // NOTE THAT THE CLASS ASSIGNMENT TO THE CLASS HAVING THE MAXIMUM RESPONSE IS MADE ONLY IF
	         // THAT RESPONSE SURPASSES THE THRESHOLD AND IF THE SECOND BEST IS SUFFICIENTLY LOWER THAN 
	         // THE MAXIMUM RESPONSE (PROVIDED THAT THERE ARE NO TIES).
             // OTHERWISE THE OBJECT IS CONSIDERED AS UNCLASSIFIABLE
           
	         if((maximumPredictedValue >= bestValueThreshold) && 
	        	(secondBestPredictedvalue <= secondBestValueThreshold) && 
	        	(maximumPredictedValue != secondBestPredictedvalue)
	           )
	            ++(mi_ContingencyTable[expectedClass][classWithTheMaximumPredictedValue]);
	         else /* the object is unclassifiable */
                 ++(mi_ContingencyTable[expectedClass][numberOfClasses]);
	         

	      } /* for(i */
	        
	      return mi_ContingencyTable;
	}
	
	/**
	 *  MCSCE1rawFitness   Simple classification error for multiple chromosome classification problems

	   Calculate the matches between predicted and expected classes in a contingency table (using 2 thresholds to
	   determine if the predicted and expected classes are disciminated clearly enough; the 1st threshold
	   is for the bestValue of the class predictions; it must equal or exceed this threshold; the 2nd
	   threshold is for the secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined).
	   <br>
	   Then the classificationError is the ratio between the number of miss-matches and the number of
	   of objects.
	   <br>
	   A fitness function can be constructed from this error in the usual way.
       <br>
	   @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @param bestValueThreshold 
	   @param secondBestValueThreshold
	   @return double value with ratio between the number of miss-matches and the number of
	           of objects

	*/
	public static double MCSCE1rawFitness(boolean useTrainingData, GEPIndividual ind, double bestValueThreshold, double secondBestValueThreshold) 
	{
	      int mi_ContingencyTable[][] = MCSCE1contingencyTable(useTrainingData, ind, bestValueThreshold, secondBestValueThreshold);
	      GEPDependentVariable dv;
	        
	      if (useTrainingData) 
	        	dv = GEPDependentVariable.trainingData;
	      else
	        	dv = GEPDependentVariable.testingData;
	        
	      int numberOfObjects = dv.getDependentVariableValues().length;
	      int numberOfClasses = ((GEPSpecies)ind.species).numberOfChromosomes;
	      int numberOfMatches = 0;
	      // diagonal of table has the matching counts
	      for (int i=0; i<numberOfClasses; i++)
	        	  numberOfMatches += mi_ContingencyTable[i][i];
	      // count matches in the contingency table ... mismatches/numberOfClasses
	      double classificationError = ((double)(numberOfObjects-numberOfMatches))/numberOfObjects;

	      return classificationError;
	}
	
	/**
	 *  MCSCE1   Simple classification error for multiple chromosome classification problems
       <br>
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   and in each of these rows only 1 of the classi values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
<br>
	   NOTE: uses 2 thresholds to determine if the predicted and expected classes are 
	   disciminated clearly enough; the 1st threshold is for the bestValue of the class 
	   predictions; it must equal or exceed this threshold; the 2nd threshold is for the 
	   secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined.
	   <br>
	   The classificationError (raw fitness) is the ratio between the number of miss-matches and the number of
	   of objects.
	   A fitness function can be constructed from this error in the usual way.
	   <br>
	   @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @return double value with fitness between 0 and 1000
	*/
	public static double MCSCE1fitness(boolean useTrainingData, GEPIndividual ind, double parameters[]) 
    {
		// get the extra parameters (1 or 2 of them)
		// 1st one is the bestValueThreshold, 2nd one is the secondBestValueThreshold
		// if only 1 parameter specified then 2nd is same as 1st
		double bestValueThreshold = parameters[0];
		double secondBestValueThreshold;
		if (parameters.length < 2)
			secondBestValueThreshold = bestValueThreshold;
		else
			secondBestValueThreshold = parameters[1];
        double MCSCE = MCSCE1rawFitness(useTrainingData, ind, bestValueThreshold, secondBestValueThreshold);
        // return fitness between 0 and 1000 rather than error value (0 to 1) from raw fitness value.
        return (1000.0*(1.0-MCSCE));
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MCSCE1maxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}
	
	/**
	 *  MCSCE2contingencyTable  Simple classification error for multiple chromosome classification problems
	   <br>
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   and in each of these rows only 1 of the classi values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
<br>
	   NOTE: uses a threshold to determine if the predicted and expected classes are 
	   disciminated clearly enough; the second highest value predicted for the example 
	   must be less than the largest value by at least threshold or it is considered 
	   an undefined class prediction. 
<br>
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the individual for which we want to calculate the confusion matrix values
	 * @param discriminationThreshold 
	 * @return double array with values representing the matches of predicted versus expected
	   	   
	*/
	public static int[][] MCSCE2contingencyTable(boolean useTrainingData, GEPIndividual ind, double discriminationThreshold) 
	{
	       int classWithTheMaximumPredictedValue; /* this will be the predicted class if it meets the specified conditions */
	       double maximumPredictedValue, value;
		   GEPDependentVariable dv;
		        
		   if (useTrainingData) 
		        	dv = GEPDependentVariable.trainingData;
		   else
		        	dv = GEPDependentVariable.testingData;
		        
	       int numberOfObjects          = dv.getDependentVariableValues().length;
	       int numberOfClasses          = ((GEPSpecies)ind.species).numberOfChromosomes;
	       int numberOfExtendedClasses  = numberOfClasses+1;/* including the UNCLASSIFIED case (for future use) */ /// ???
	       int mi_ContingencyTable[][]  = new int [numberOfClasses][numberOfExtendedClasses];
	       double dependentVar[];
	       double md_ExpectedValues[][] = new double [numberOfObjects][numberOfClasses];
	        
	       /* construction of the expected class values (SORRY ABOUT THE MEMORY LEAK) */
	       for(int j=0; j < numberOfClasses; j++)
	       {/* get the j-th column */
	            dependentVar = dv.getDependentVariableValues(j);
	            for(int i = 0; i < numberOfObjects; i++)
	               md_ExpectedValues[i][j] = dependentVar[i];
	       } 
	 	                
	       /* construction of the contingency table */
	       for(int i=0; i< numberOfObjects; i++)
	       {
	    	 int expectedClass = 0;
	         classWithTheMaximumPredictedValue = 0;
	         maximumPredictedValue             = ind.eval(classWithTheMaximumPredictedValue, useTrainingData, i);
	         double maximumExpectedValue       = md_ExpectedValues[i][expectedClass]; 
			 if (Double.isNaN(maximumPredictedValue))
			 {
				// set all counts to 0 on diagonal and return
				for (int ii=0; ii<numberOfClasses; ii++)
					mi_ContingencyTable[ii][ii] = 0;
				return mi_ContingencyTable;
			 }
	         for(int j=1; j < numberOfClasses; j++)
	         {
	             /* determine the class with the largest expected (1) value */
	             value = md_ExpectedValues[i][j];
	             if(value > maximumExpectedValue)
	             { maximumExpectedValue = value;
	               expectedClass        = j;
	             }
	 
	             /* determine the class with the largest predicted value its class */
	             value = ind.eval(j, useTrainingData, i);
				 if (Double.isNaN(value))
				 {
					// set all counts to 0 on diagonal and return
					for (int ii=0; ii<numberOfClasses; ii++)
						mi_ContingencyTable[ii][ii] = 0;
					return mi_ContingencyTable;
				 }
	             if(value > maximumPredictedValue+discriminationThreshold)
	             { maximumPredictedValue             = value;
	               classWithTheMaximumPredictedValue = j;
	             }
	             else
	             {
	            	 if (value >= maximumPredictedValue-discriminationThreshold)
	            		 classWithTheMaximumPredictedValue = numberOfClasses; // UNCLASSIFIED --- 0 to numberOfClasses-1 are the classes
	            	 if (value > maximumPredictedValue)
	            		 maximumPredictedValue = value;
	             }
	         } /* for(j */
	         
	         /* update the contingency table */
	         mi_ContingencyTable[expectedClass][classWithTheMaximumPredictedValue]++; 
	         
	      } /* for(i */
	        
	      return mi_ContingencyTable;
	}

	/**
	 *  MCSCE2rawFitness   Simple classification error for multiple chromosome classification problems
       <br>
	   Calculate the matches between predicted and expected classes in a contingency table (using a threshold to
	   determine if the predicted and expected classes are disciminated clearly enough;  
	   the second highest value predicted for the example must be less than the largest 
	   value by at least threshold ot it is considered an undefined class prediction. 
	   <br>
	   Then the classificationError is the ratio between the number of miss-matches and the number of
	   of objects.
	   <br>
	   A fitness function can be constructed from this error in the usual way.
<br>
	   @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @param  threshold to decide if predicted class undefined or not.
	   @return double value with ratio between the number of miss-matches and the number of
	           of objects
	*/
	public static double MCSCE2rawFitness(boolean useTrainingData, GEPIndividual ind, double threshold) 
	{
	      int mi_ContingencyTable[][] = MCSCE2contingencyTable(useTrainingData, ind, threshold);
	        
		  GEPDependentVariable dv;   
		  if (useTrainingData) 
		        	dv = GEPDependentVariable.trainingData;
		  else
		        	dv = GEPDependentVariable.testingData;
	      int numberOfObjects = dv.getDependentVariableValues().length;
	      int numberOfClasses = ((GEPSpecies)ind.species).numberOfChromosomes;
	      int numberOfMatches = 0;
	      // diagonal of table has the matching counts
	      for (int i=0; i<numberOfClasses; i++)
	        	  numberOfMatches += mi_ContingencyTable[i][i];
	      // count matches in the contingency table ... mismatches/numberOfClasses
	      double classificationError = ((double)(numberOfObjects-numberOfMatches))/numberOfObjects;

	      return classificationError;
	}
	
	/**
	 *  MCSCE2   Simple classification error for multiple chromosome classification problems
       <br>
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   and in each of these rows only 1 of the class values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
<br>
	   NOTE: uses a threshold to determine if the predicted and expected classes are 
	   disciminated clearly enough;  the second highest value predicted for the example 
	   must be less than the largest value by at least threshold ot it is considered an
	   undefined class prediction. 
	   <br>
	   The classificationError (raw fitness) is the ratio between the number of miss-matches and the number of
	   of objects.
	   A fitness function can be constructed from this error in the usual way.
	   <br>
	   @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @param  parameters array with 1 value: threshold to decide if predicted class undefined or not.
	   @return double value with fitness between 0 and 1000
	*/
	public static double MCSCE2fitness(boolean useTrainingData, GEPIndividual ind, double parameters[]) 
 {
	 // get the extra parameter threshold, amount by which highest value of predictions must be
	 // to be a clear 'winner'; if less than this then the class is undefined
	 double threshold = parameters[0];
     double MCSCE = MCSCE2rawFitness(useTrainingData, ind, threshold);
     // return fitness between 0 and 1000 rather than error value (0 to 1) from raw fitness value.
     return (1000.0*(1.0-MCSCE));
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MCSCE2maxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}
	
	/**
	 *  MCSCE3contingencyTable   Simple classification error for multiple chromosome classification problems
	   <br>
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   and in each of these rows only 1 of the classi values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
<br>
	   NOTE: as in MCSCE1 it uses 2 thresholds to determine if the predicted and expected classes are 
	   disciminated clearly enough; the 1st threshold is for the bestValue of the class 
	   predictions; it must equal or exceed this threshold; the 2nd threshold is for the 
	   secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined. Unlike
	   MCSCE1, the threshold values will be between 0,0 and 1.0 and the predicted values will be
	   normalized between 0 and 1 prior to comparing to the threshold values. The normalization
	   is done by using the range of ALL predicted values over the classes and examples.
<br>
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the individual for which we want to calculate the confusion matrix values
	 * @param bestValueThreshold 
	 * @param secondBestValueThreshold
	 * @return double array with values representing the matches of predicted versus expected
     */

	public static int[][] MCSCE3contingencyTable(boolean useTrainingData, GEPIndividual ind, double bestValueThreshold, double secondBestValueThreshold) 
	{
       int classWithTheMaximumPredictedValue;    /* this will be the predicted class if it meets the specified conditions */
       int classWithTheSecondBestPredictedValue; 
	   GEPDependentVariable dv;   
	   if (useTrainingData) 
		        	dv = GEPDependentVariable.trainingData;
	   else
		        	dv = GEPDependentVariable.testingData;
       int numberOfObjects          = dv.getDependentVariableValues().length;
       int numberOfClasses          = ((GEPSpecies)ind.species).numberOfChromosomes;
       int mi_ContingencyTable[][]  = new int [numberOfClasses][numberOfClasses+1]; // extra column for unclassified counts
       double maximumPredictedValue, secondBestPredictedvalue, value;
       double generalMinimumPredictedValue, generalMaximumPredictedValue, range;
       double dependentVar[];
       double md_ExpectedValues[][] = new double [numberOfObjects][numberOfClasses];
       double md_PredictedValues[][] = new double [numberOfObjects][numberOfClasses];
       
       // thresholds must be between 0.0 and 1.0 and bestValueThreshold must be > secondBestValueThreshold
       if (secondBestValueThreshold > 1.0 || secondBestValueThreshold < 0.0 || 
    		   bestValueThreshold > 1.0 || bestValueThreshold < 0.0 ||
    		   bestValueThreshold < secondBestValueThreshold
    	  )
       System.err.println("Error: MCSCE3 -- threshold values must be between 0.0 and 1.0 and first must be less than the second.");
       /*  FIND  (generalMinimumPredictedValue) (generalMaximumPredictedValue) 
       */
       generalMinimumPredictedValue = generalMaximumPredictedValue = ind.eval(0, useTrainingData, 0);
	   for(int i=0; i< numberOfObjects; i++)
	       for(int j=0; j < numberOfClasses; j++)
	       { value = ind.eval(j, useTrainingData, i);
	         md_PredictedValues[i][j] = value;
	         if(Double.isNaN(value))/* SEVERE ERROR CONDITION. RETURN IF FOUND */
			 {
				// set all counts to 0 on diagonal and return when an invalid value is found
				for (int ii=0; ii<numberOfClasses; ii++)
					mi_ContingencyTable[ii][ii] = 0;
				return mi_ContingencyTable;
			 }

	         if(value < generalMinimumPredictedValue)
	           generalMinimumPredictedValue = value;
 
	         if(value > generalMaximumPredictedValue)
	           generalMaximumPredictedValue = value;
	       }
	   range = generalMaximumPredictedValue - generalMinimumPredictedValue;
	 	     
	   /* construction of the expected class values */
	   for(int j=0; j < numberOfClasses; j++)
	   {   /* get the j-th column */
	       dependentVar = dv.getDependentVariableValues(j);
	       for(int i = 0; i < numberOfObjects; i++)
	           md_ExpectedValues[i][j] = dependentVar[i];
	   } 

	   /* construction of the contingency table */
	   for(int i=0; i< numberOfObjects; i++)
	   {
	       int expectedClass                 = 0;
	       classWithTheMaximumPredictedValue = 0;
	       maximumPredictedValue = md_PredictedValues[i][classWithTheMaximumPredictedValue];
	       secondBestPredictedvalue = maximumPredictedValue;
	       double maximumExpectedValue = md_ExpectedValues[i][expectedClass]; 
		   if (Double.isNaN(maximumPredictedValue))
		   {
		     // set all counts to 0 on diagonal and return when an invalid value is found
		     for (int ii=0; ii<numberOfClasses; ii++)
					mi_ContingencyTable[ii][ii] = 0;
			 return mi_ContingencyTable;
		   }	         
			 
	       for(int j=1; j < numberOfClasses; j++)
	       {
	             /* determine the class with the largest expected (1) value */
	             value = md_ExpectedValues[i][j];
	             if(value > maximumExpectedValue)
	             { maximumExpectedValue = value;
	               expectedClass        = j;
	             }
	 
	             /* determine the class with the largest predicted value its class */
	             value = md_PredictedValues[i][j];
	             if(value > maximumPredictedValue)
	             { /* update the second best and the absolute best values and the corresp. classes */
	               secondBestPredictedvalue             = maximumPredictedValue;
	               classWithTheSecondBestPredictedValue = classWithTheMaximumPredictedValue;
	               maximumPredictedValue                = value;
	               classWithTheMaximumPredictedValue    = j;
	             }
	        } /* for(j */
	         
	        /* NORMALIZATION (RE-SCALING) [generalMinimumPredictedValue, generalMaximumPredictedValue] -> [0,1]              */
	        /* (bestValueThreshold) and (secondBestValueThreshold) are considered in [0,1] */
	        maximumPredictedValue    = (maximumPredictedValue - generalMinimumPredictedValue)/range;
	        secondBestPredictedvalue = (secondBestPredictedvalue - generalMinimumPredictedValue)/range;

	        /* update the contingency table */
	        // NOTE THAT THE CLASS ASSIGNMENT TO THE CLASS HAVING THE MAXIMUM RESPONSE IS MADE ONLY IF
	        // THAT RESPONSE SURPASSES THE THRESHOLD AND IF THE SECOND BEST IS SUFFICIENTLY LOWER THAN 
            // THE MAXIMUM RESPONSE (PROVIDED THAT THERE ARE NO TIES).
            // OTHERWISE THE OBJECT IS CONSIDERED AS UNCLASSIFIABLE
           
	        if ( (maximumPredictedValue >= bestValueThreshold)          && 
	        	   (secondBestPredictedvalue <= secondBestValueThreshold) && 
	        	   (maximumPredictedValue != secondBestPredictedvalue)
	           )
	            ++(mi_ContingencyTable[expectedClass][classWithTheMaximumPredictedValue]);
	        else /* the object is unclassifiable */
              ++(mi_ContingencyTable[expectedClass][numberOfClasses]);
	         
	      } /* for(i */
	        
	      return mi_ContingencyTable;
	}

	
	/**
	 *  MCSCE3rawFitness   Simple classification error for multiple chromosome classification problems
       <br>
	   Calculate the matches between predicted and expected classes in a contingency table (as in MCSCE1 it uses
	   2 thresholds to determine if the predicted and expected classes are disciminated clearly enough; the 1st 
	   threshold is for the bestValue of the class predictions; it must equal or exceed this threshold; the 2nd
	   threshold is for the secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined; however, nnlike
	   MCSCE1, the threshold values will be between 0,0 and 1.0 and the predicted values will be
	   normalized between 0 and 1 prior to comparing to the threshold values. The normalization
	   is done by using the range of ALL predicted values over the classes and examples).
	   <br>
	   Then the classificationError is the ratio between the number of miss-matches and the number of
	   of objects.
	   <br>
	   A fitness function can be constructed from this error in the usual way.
<br>
	 * @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @param bestValueThreshold 
	   @param secondBestValueThreshold
	   @return double value with ratio between the number of miss-matches and the number of
	           of objects

	*/
	public static double MCSCE3rawFitness(boolean useTrainingData, GEPIndividual ind, double bestValueThreshold, double secondBestValueThreshold) 
	{
	      int mi_ContingencyTable[][] = MCSCE3contingencyTable(useTrainingData, ind, bestValueThreshold, secondBestValueThreshold);
	        
		  GEPDependentVariable dv;   
		  if (useTrainingData) 
			        	dv = GEPDependentVariable.trainingData;
		  else
			        	dv = GEPDependentVariable.testingData;
	      int numberOfObjects = dv.getDependentVariableValues().length;
	      int numberOfClasses = ((GEPSpecies)ind.species).numberOfChromosomes;
	      int numberOfMatches = 0;
	      // diagonal of table has the matching counts
	      for (int i=0; i<numberOfClasses; i++)
	        	  numberOfMatches += mi_ContingencyTable[i][i];
	      // count matches in the contingency table ... mismatches/numberOfClasses
	      double classificationError = ((double)(numberOfObjects-numberOfMatches))/numberOfObjects;

	      return classificationError;
	}
	
	/**
	 *  MCSCE3   Simple classification error for multiple chromosome classification problems
       <br>
	   For each object (example) the expected values of the dependent variables are given by a BINARY vector
	   of length equal to the number of classes (chromosomes). So each row in an example will look like:
	   <br>
	   x1, x2, ... xn, class1, class2, ... classm
	   <br>
	   where there are n independent variables and m classes.
	   and in each of these rows only 1 of the classi values will be a 1 and the rest will be 0,
	   specifying the class that this example belongs to.
	   <br>
	   The individuals in the population will have 'm' chromosomes (1 for each class) and each chromosome
	   will have a function associated with it. These functions are used to predict the class that the example
	   belongs to by selecting the chromosome with the largest value as the predicted class.
	   <br>
	   For object (i), a match happens when its predicted class coincides with the expected class
	   (the one for which the binary vector is (1) ). Keep track of these in a contingency table where each row
	   represents an expected class and each column represents a predicted class (includes one extra
	   column for the UNCLASSIFIED classes). The entries represent the number of objects (examples)
	   that were of a particular class and predicted to be of a particular class. The correct
	   predictions are located in positions (i, i) where i = 0 to numberClasses-1.
<br>
	   NOTE: like MSCE1 it uses 2 thresholds to determine if the predicted and expected classes are 
	   disciminated clearly enough; the 1st threshold is for the bestValue of the class 
	   predictions; it must equal or exceed this threshold; the 2nd threshold is for the 
	   secondBest value of class prediction; it must be less than or equal to this threshold;
	   if the threshold conditions are not satisfied then the class is undefined. Unlike
	   MCSCE1, the threshold values will be between 0,0 and 1.0 and the predicted values will be
	   normalized between 0 and 1 prior to comparing to the threshold values. The normalization
	   is done by using the range of ALL predicted values over the classes and examples.
	   <br>
	   The classificationError (raw fitness) is the ratio between the number of miss-matches and the number of
	   of objects.
	   A fitness function can be constructed from this error in the usual way.
	   <br>
	 * @param useTrainingData true if using training data, else use testing data
	   @param ind the individual for which we want to calculate the confusion matrix values
	   @param  parameters array with 2 values: bestValueThreshold and secondBestValueThreshold
	   @return double value with fitness between 0 and 1000
	*/
	public static double MCSCE3fitness(boolean useTrainingData, GEPIndividual ind, double parameters[]) 
    {
		// get the extra parameters (1 or 2 of them)
		// 1st one is the bestValueThreshold, 2nd one is the secondBestValueThreshold
		// if only 1 parameter specified then 2nd is same as 1st
		double bestValueThreshold = parameters[0];
		double secondBestValueThreshold;
		if (parameters.length < 2)
			secondBestValueThreshold = bestValueThreshold;
		else
			secondBestValueThreshold = parameters[1];
     double MCSCE = MCSCE3rawFitness(useTrainingData, ind, bestValueThreshold, secondBestValueThreshold);
     // return fitness between 0 and 1000 rather than error value (0 to 1) from raw fitness value.
     return (1000.0*(1.0-MCSCE));
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MCSCE3maxFitness(GEPIndividual ind) 
	{
		// maximum value is always 1000
		return (1000.0);
	}
	

	
    //************************* MCMSE (MultipleChromosome Mean Squared Error) *****************
	
	/**
	 * Calculates the MSE (Mean Squared Error) type fitness for multiple chromosome problems (average of the MSE for all dependent variables (chromsosomes)). 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param useTrainingData true if using training data, else use testing data
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double MCMSEfitness(boolean useTrainingData, GEPIndividual ind) 
    {
        double MMSE = 0.0;
        int numDepVars = ((GEPSpecies)ind.species).numberOfChromosomes;
        for (int i=0; i<numDepVars; i++)
        	MMSE += MSErawFitness(useTrainingData, ind, i);
        MMSE = MMSE/numDepVars; // it is just the average of all the chromosome MSE values.
        // raw fitness is normalized between 0 and 1000  (1000 * (1/(1+MSE))
        return (1000.0)/(1.0+MMSE);
	}

	/**
	 * The max value for this type of fitness is always 1000.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value 1000.0
	 */
	public static double MCMSEmaxFitness(GEPIndividual ind) 
	{
		// always 1000
		return 1000.0;
	}

	
	  //****************** Sammon Error With Dissimilarity (SEWD) *********************

	   private static double ZERO                                         = 1.0E-6;

	   private static int    WEIGHTED_EUCLIDEAN_DISTANCE_INDEX            = 0;
	   private static int    NUMBER_OF_COMMON_VARIABLES_INDEX             = 1;
	   private static int    NORMALIZED_WEIGHTED_EUCLIDEAN_DISTANCE_INDEX = 2;
	   
	   private static double originalDisimMatrix[][]; 
	   private static double sumOfTheLowerTriangle = 0.0;
	   

	   /* Depending on how the disimilarity matrix is structured, this function
	    * should return the appropriate dissimilarity value. As written is assumes that
	    * the lower left of the matrix is supplied in the matrix. so values in the upper
	    * right are returned usiong the corresponding value in the lower left.
	    */
	   private static double get_sim_disim_value_from_simDisimMatrix(double[][] originalDisimMatrix, int ii, int jj) {
		   if (ii<jj) return originalDisimMatrix[jj][ii];
		   if (ii>jj)  return originalDisimMatrix[ii][jj];
		   return originalDisimMatrix[0][0]; // diagonal ... probably never happens
	   }

	   /**
	    * Compute the weighted Euclidean distance between x and y.
	    * <br><br>
	    * Assumptions: returnValues is allocated by caller and is of length 3.
	    * 
	    * @param x   array of values
	    * @param y   another array of values
	    * @param weights
	    *            for the variables or null if no weights are required or all
	    *            variables have the same influence.
	    * @param xmiss double value to use to indicate a missing value. 
	    *            If xmiss is null, then no missing value is indicated and, hence,
	    *            the data should be complete.
	    * @param normalize
	    *            if 1 then result should be normalized by the number of common
	    *            variables (numCommonVariables).
	    * @param deNormalize
	    *            if normalize==1 then if deNormalize==1 then the result should
	    *            be multiplied by the total number of variables. (It gives the
	    *            -equivalent- distance had all variables been defined and with
	    *            each contributing to the distance the same as the normalized
	    *            value).
	    * @param returnValues 
	    *            array with 3 values returned. 
	    *     [0] = weighted Euclidean distance
	    *     [1] = number of common variables (i.e. nbr of variables not
	    *           simultaneously missing values in both vectors). If no 
	    *           distance can be computed then number of common variables == 0
	    *     [2] = dist : normalised distance.
	    */
	   private static void euclidean_distanceW(double[] x, double[] y, double[] weights,
	            double[] xmiss, int normalize, int deNormalize, double[] returnValues)
	   {
	      int ii;
	      int numVarsX = x.length;
	      int numVarsY = y.length;

	      if(returnValues == null) 
	      {
	         throw new IllegalArgumentException("euclidean_distanceW(): returnValues = null (but should be allocated)");
	      }
	      if(returnValues.length != 3) 
	      {
	         throw new IllegalArgumentException("euclidean_distanceW(): returnValues.length() = " + returnValues.length
	                  + "  (but should be 3)");
	      }
	      /* initialize the return values */
	      returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] = 0.0;
	      returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX] = 0.0;
	      returnValues[NORMALIZED_WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] = 0.0;

	      if(numVarsX != numVarsY) 
	      {
	         throw new IllegalArgumentException("euclidean_distanceW(): numVarsX = " + numVarsX + "  numVarsY = "
	                  + numVarsY);
	      }
//	      switch((xmiss == null) ? 1 : 0) {
//	         case 0: {/* incomplete information */
	      if (xmiss != null)
	            for(ii = 0; ii < numVarsX; ii++) 
	            {
	               if(x[ii] == xmiss[0] || y[ii] == xmiss[0])
	                  continue;
	               returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] += weights[ii] * (x[ii] - y[ii]) * (x[ii] - y[ii]);/* weights are used */
	               returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX]++;
	            }
//	            break;
//	         }
//	         default: {/* complete information ) */
	      else
	            for(ii = 0; ii < numVarsX; ii++)
	               returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] += weights[ii] * (x[ii] - y[ii]) * (x[ii] - y[ii]);/* weights are used */
	            returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX] = numVarsX;
//	         }
//	      }

	      if(returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX] <= 0) {
	         return; /* no values in common, so no distance can be computed */
	      }
	      if(normalize == 1) {
	         returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] /= returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX];
	         if(deNormalize == 1)
	            returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] *= numVarsX;
	      } else {/* normalize = 0 */
	         if(deNormalize == 1)/* you can't de-normalize if you didn't normalize first. */
	            returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] = (returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] / returnValues[NUMBER_OF_COMMON_VARIABLES_INDEX])
	                     * numVarsX;
	      }
	      returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX] = Math.sqrt(returnValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX]);
	   }

	   /** 
	    *  This method computes the sammon error between the original dissimilarity matrix
	    *  and a new data matrix. The original dissimilarity matrix (in the GEP case)
	    *  is the one read from the file and given as input from the user about
	    *  the dissimilarity between objects in the original space.
	    *  The new data matrix (in the GEP case) is constructed based on the
	    *  chromosome(s). Accessing the dissimilarity matrix is performed via the
	    *  following function:
	    *  <br><pre>
	    *       get_sim_disim_value_from_simDisimMatrix(...)
	    *  </pre><br>
	    *  and so its implementation must be based on the actual way in which the
	    *  values are read from the user's file. However, we have specified the format
	    *  of the file to be such that the lower left of the matrix is provided in the file.
	    *  
	    *  @param dataMatrixInTheNewSpace the new matrix being compared to the original matrix from the file
	    *  @param xmiss array of values that represent missing values in the data (only use the 
	    *               1st value in this implementation.
	    */
	   private static double sammon_error(double[][] dataMatrixInTheNewSpace, double[] xmiss)
	   {
	      int ii, jj;
	      double[] euclideanDistanceValues = new double[3];
	      double sammonError = -1;
	      double sum;
	      double originalDisimValue;
	      int dataMatrixInTheNewSpaceLen = dataMatrixInTheNewSpace.length;
	      double weights[] = new double[dataMatrixInTheNewSpace[0].length];
	  
	      for(ii = 0; ii < dataMatrixInTheNewSpace[0].length; ii++) // no weights in this version -- all set to 1
	          weights[ii] = 1.0;

	      if (originalDisimMatrix.length != dataMatrixInTheNewSpaceLen)
	    	  throw new IllegalArgumentException("number of rows in originalDisimMatrix and dataMatrixInTheNewSpace should be the same.");
	      
	      sum = 0.0;
	      for(ii = 0; ii < dataMatrixInTheNewSpaceLen-1; ii++) 
	      {
	         for(jj = ii + 1; jj < dataMatrixInTheNewSpaceLen; jj++) 
	         {
	        	 euclidean_distanceW(dataMatrixInTheNewSpace[ii], dataMatrixInTheNewSpace[jj], weights, xmiss,
	                     0 /*normalize*/, 0 /*deNormalize*/, euclideanDistanceValues);
	            originalDisimValue = get_sim_disim_value_from_simDisimMatrix(originalDisimMatrix, ii, jj);

	            if(originalDisimValue < ZERO)/* prevent division by 0 */
	               originalDisimValue = ZERO;

	            if(xmiss == null || originalDisimValue != xmiss[0]) 
	            {
	               if(euclideanDistanceValues[NUMBER_OF_COMMON_VARIABLES_INDEX] > 0) 
	               {
	                  double d = euclideanDistanceValues[WEIGHTED_EUCLIDEAN_DISTANCE_INDEX];
	                  //sum += (Math.pow((originalDisimValue - d), 2) / originalDisimValue);
	                  double t = originalDisimValue - d;
	                  sum += (t*t) / originalDisimValue;
	               }
	            }
	         }
	      }
	      sammonError = sum / sumOfTheLowerTriangle;
	      return (sammonError);
	   }

	   /** 
	    * This method calulates Standard Euclidean Weighted Distance fitness value: 
	    * <br>
	    * 1) assumes the original disimilarity matrix was properly read from the file (see setup)
	    * <br>
	    * 2) constructs the new data matrix based on the particular individual
	    * <br>
	    * 3) and computes the fitness (based on Sammon's error)
	    *    between the original disimilarity matrix and the
	    *    GEP constructed data matrix.
	    *    
	    */
	   public static double SEWDfitness(boolean useTrainingData, GEPIndividual ind) 
	   {		   
		 GEPDependentVariable dv;
		        
		 if (useTrainingData) 
		        	dv = GEPDependentVariable.trainingData;
		 else
		        	dv = GEPDependentVariable.testingData;
		        
	      int numValues = dv.getDependentVariableValues().length;
	      int numDepVars = ((GEPSpecies)ind.species).numberOfChromosomes;
	      double[][] dataMatrixInTheNewSpace = new double[numValues][numDepVars];
	      for (int i=0; i<numValues; i++)
	    	  for (int j=0; j<numDepVars; j++)
	    		  dataMatrixInTheNewSpace[i][j] = ind.eval(j, useTrainingData, i);

	      double[] xmiss = new double[1];
	      xmiss[0] = -999; /* should be specified from the parameters file but for testing ok to hardcode here */

	      double error = sammon_error(dataMatrixInTheNewSpace, xmiss);
	      
	      return (1000.0)/(1.0+error); /// ????
	   }

		/** 
		 * The max value for this type of fitness is always 1000.
		 * @param ind the GEP individual that needs its fitness calculated.
		 * @return value 1000.0
		 */
		public static double SEWDmaxFitness(GEPIndividual ind) 
		{
			// always 1000
			return 1000.0;
		}


}
