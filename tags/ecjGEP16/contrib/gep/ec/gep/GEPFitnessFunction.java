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

/**
 * @author Bob Orchard
 *
 * This class provides a set of static functions that support all (or almost all) of the gep
 * fitness functions found in the commercial version of gep, GeneXProTools 4.0. 
 * They include:
 * <code>

    AEWSR (Absolute Error with Selection Range) 
	AH (Absolute/Hits)
	MSE (mean squared error) 
	RMSE (root mean squared error) 
	MAE (mean absolute error) 
	RSE (relative squared error) 
	RRSE (root relative squared error) 
	RAE (relative absolute error) 
	REWSR (Relative Error with Selection Range) 
	RH (Relative/Hits) 
	rMSE (relative MSE) 
	rRMSE (relative RMSE) 
	rMAE (relative MAE) 
	rRSE (relative RSE) 
	rRRSE (relative RRSE) 
	rRAE (relative RAE) 
	RS (R-square) 
	CC (Correlation Coefficient) 
	NH (Number of Hits) 
	NHWP(Number of Hits with Penalty) 
	ACC (Accuracy) 
	SACC (Squared Accuracy) 
	SS (Sensitivity/Specificity) 
	PPNVP (PPV/NPV Positive Predictive Value / Negative Predictive Value ) 
	SSPN (Sensitivity/Specificity/PPV/NPV ) 
	WCorrRMSE (Weighted correlation coefficient and Root Mean Squared Error)
	
 </code>
 * For each of the fitness functions there are 3 methods: one to calcualte the fitness 
 * (with a value from 0 to some maximum); one to give the raw fitness value, prior
 * to being mapped between 0 and the maximum value; one to provide the maximum value 
 * for that fitness function type. By convention we use the names XXXXfitness, XXXXrawFitness
 * and XXXXmaxFitness for all of the fitness functions. XXXX is the pneumonic for the 
 * fitness function (RRSE, MAE, etc.). This allows a user to specifiy a fitness function to be
 * used in the default user prog (they don't provide ANY code if the use this) using
 * these short names. The XXXXmaxFitness functions all have 1 arg when most don't need one. 
 * This was just to make it easier to call the method based on the XXXX value only. See
 * GEPDefaultUserProg.java.
 * <li>
 * For example the functions for the MSE fitness function are called as:
 * <li>
 * <code>
  		GEPFitnessFunction.MSEfitness( gepindividual );
  			- calculates the fitness using the individual's gene expressions
  			- it gets the raw fitness first from MSErawFitness (the mean squared error
  			  of the predicted values versus the expected values)
  			- then it normalizes the result between 0 and 1000  
  				(1000 * (1/(1 + raw MSE))
  		GEPFitnessFunction.MSErawFitness( gepindividual );
  			- sum((predicted valuei - expected valuei)**2)/n
  		GEPFitnessFunction.NHmaxFitness( gepindividual );
 *			- in this case max is always 1000
  </code>
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
	
    //************************* RAE (Relative Absolute Error) *****************
	

	/**
	 * Calculates the 'raw' fitness for the RAE (Relative Absolute Error) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RAErawFitness(GEPIndividual ind) 
	{
        double sumOfAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();
        double dvSumOfAbsoluteError = GEPDependentVariable.getDependentVariableSumOfAbsoluteError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RAEfitness(GEPIndividual ind) 
    {
        double RAE = RAErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRAErawFitness(GEPIndividual ind) 
	{
        double sumOfRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();
        double dvSumOfRelativeError = GEPDependentVariable.getDependentVariableSumOfRelativeError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRAEfitness(GEPIndividual ind) 
    {
        double rRAE = rRAErawFitness(ind);
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
	 * Calculates the 'raw' fitness for the rRAE (relative RAE) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double MAErawFitness(GEPIndividual ind) 
	{
        double sumOfAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
            error = result - expectedResult;
            sumOfAbsoluteError += Math.abs(error); 
        }
        // the raw fitness ... MAE
        return (sumOfAbsoluteError/dependentVar.length);
	}
 
	/**
	 * Calculates the fitness for the rRAE (relative RAE) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double MAEfitness(GEPIndividual ind) 
    {
        double MAE = MAErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rMAErawFitness(GEPIndividual ind) 
	{
        double sumOfRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rMAE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfRelativeError += Math.abs(error); 
        }
        // the raw fitness ... rMAE
        return (sumOfRelativeError/dependentVar.length);
	}
    	
	/**
	 * Calculates the fitness for the rMAE (relative Mean Absolute Error) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rMAEfitness(GEPIndividual ind) 
    {
        double rMAE = rMAErawFitness(ind);
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
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double MSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error * error; 
        }
        // the raw fitness ... MSE
        return (sumOfSquaredAbsoluteError/dependentVar.length);
	}
    	
	/**
	 * Calculates the fitness for the MSE (Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double MSEfitness(GEPIndividual ind) 
    {
        double MSE = MSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rMSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rMSE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += error * error; 
        }
        // the raw fitness ... rMSE
        return (sumOfSquaredRelativeError/dependentVar.length);
	}
    	
	/**
	 * Calculates the fitness for the rMSE (relative Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rMSEfitness(GEPIndividual ind) 
    {
        double rMSE = rMSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RMSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
            if (Double.isInfinite(result) || Double.isNaN(result))
            	return(Double.NaN);
            error = result - expectedResult;
            sumOfSquaredAbsoluteError += error * error; 
        }
        // the raw fitness ... RMSE
        return Math.sqrt(sumOfSquaredAbsoluteError/dependentVar.length);
	}
    	
	/**
	 * Calculates the fitness for the RMSE (Root Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RMSEfitness(GEPIndividual ind) 
    {
        double RMSE = RMSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRMSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);

            if (expectedResult == 0.0)
            {   expectedResult = RELATIVE_ERROR_ZERO_FACTOR;
                result += RELATIVE_ERROR_ZERO_FACTOR;
            	System.err.println("Warning: expected result (test value) is 0 in rRMSE fitness calculation. Adjusting to avoid division by zero.");
            }
            error = (result - expectedResult)/expectedResult;
            sumOfSquaredRelativeError += error * error; 
        }
        // the raw fitness ... rRMSE
        return Math.sqrt(sumOfSquaredRelativeError/dependentVar.length);
	}
    	
	/**
	 * Calculates the fitness for the rRMSE (relative Root Mean Squared Error) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRMSEfitness(GEPIndividual ind) 
    {
        double rRMSE = rRMSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();
    	double dvSumOfSquaredAbsoluteError = GEPDependentVariable.getDependentVariableSumOfSquaredAbsoluteError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RSEfitness(GEPIndividual ind) 
    {
        double RSE = RSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double relativeError;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();       
    	double dvSumOfSquaredRelativeError = GEPDependentVariable.getDependentVariableSumOfSquaredRelativeError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);

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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRSEfitness(GEPIndividual ind) 
    {
        double rRSE = rRSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RRSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredAbsoluteError = 0.0;
        double expectedResult;
        double result;
        double error;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues(); 
    	double dvSumOfSquaredAbsoluteError = GEPDependentVariable.getDependentVariableSumOfSquaredAbsoluteError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RRSEfitness(GEPIndividual ind) 
    {
        double RRSE = RRSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double rRRSErawFitness(GEPIndividual ind) 
	{
        double sumOfSquaredRelativeError = 0.0;
        double expectedResult;
        double result;
        double relativeError;

        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();       
    	double dvSumOfSquaredRelativeError = GEPDependentVariable.getDependentVariableSumOfSquaredRelativeError();

        for (int i=0; i<dependentVar.length; i++)
        {
            expectedResult = dependentVar[i];
            result = ind.eval(i);

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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double rRRSEfitness(GEPIndividual ind) 
    {
        double rRRSE = rRRSErawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double CCrawFitness(GEPIndividual ind) 
	{
        double expectedResult;
        double predictedValue;

        double dv[] = GEPDependentVariable.getDependentVariableValues();       
    	double dvVariance = GEPDependentVariable.getDependentVariableVariance();
    	double dvMean = GEPDependentVariable.getDependentVariableMean();
    	double dvStdDev = Math.sqrt(dvVariance);

    	// mean of the calculated (predicted) values
    	double sumOfPredictedValues = 0.0;
    	double predictedValues[] = new double[dv.length];
        for (int i=0; i<dv.length; i++)
        {
        	predictedValues[i] = ind.eval(i);
        	sumOfPredictedValues += predictedValues[i];
        }
        double meanOfPredictedValues = sumOfPredictedValues/dv.length;
        
        double sum1 = 0.0;
        double sum2 = 0.0;
        for (int i=0; i<dv.length; i++)
        {
            expectedResult = dv[i];
            predictedValue = predictedValues[i];
            double diff = (predictedValue-meanOfPredictedValues);
            sum1 += (expectedResult-dvMean)*(diff);
            sum2 += diff*diff;
        }
        double covariance = sum1/dv.length;
        double stdDev = Math.sqrt(sum2/dv.length);
        
        // the raw fitness ... CC
        double cc = covariance/(dvStdDev*stdDev);
        return Math.min(1.0, Math.max(cc, -1.0)); // in case of math imprecision in calculations
	}
    	
	/**
	 * Calculates the fitness for the CC (Correlation Coefficient) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double CCfitness(GEPIndividual ind) 
    {
        double CC = CCrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RSrawFitness(GEPIndividual ind) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();       
    	double sumOfPV = 0.0;
    	double sumOfPVtimesDV = 0.0;
    	double sumOfPVsquared = 0.0;
    	double sumOfDVsquared = 0.0;
    	double sumOfDV = 0.0;
    	int len = dv.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	sumOfPV += predictedValue;
        	sumOfPVsquared += predictedValue*predictedValue;
        	expectedResult = dv[i];
        	sumOfDV += expectedResult;
        	sumOfDVsquared += expectedResult*expectedResult;
        	sumOfPVtimesDV += predictedValue*expectedResult;
        }
        
        double top = (dv.length*sumOfPVtimesDV) - (sumOfDV*sumOfPV);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RSfitness(GEPIndividual ind) 
    {
        double RS = RSrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double AHrawFitness(GEPIndividual ind, double precision) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
        double sumOfHits = 0.0;
    	int len = dv.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to AHfitness, setting to -precision.");
    	}
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	expectedResult = dv[i];
        	if (Math.abs(predictedValue-expectedResult) <= precision)
        	    sumOfHits += 1.0;
        }        
        // the raw fitness ... AH
        return sumOfHits;
	}
    	
	/**
	 * Calculates the fitness for the AH (Absolute/Hits) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision double array expected to have a single value that is the 
	 *        percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double AHfitness(GEPIndividual ind, double precision[]) 
    {
		if (precision.length != 1)
			System.err.println("Warning: 2nd arg to AHfitness method expected to have 1 double value (precision) but has " + precision.length);
        double AH = AHrawFitness(ind, precision[0]);
        // fitness is between 0 and the number of test cases
        return (AH);
	}

	/**
	 * The max value for this type of fitness is the length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return value length of the test data set
	 */
	public static double AHmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length);
	}

    //************************* RH (Relative/Hits) *****************
	
	/**
	 * Calculates the 'raw' fitness for the RH (Relative/Hits) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double RHrawFitness(GEPIndividual ind, double precision) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
        double sumOfHits = 0.0;
    	int len = dv.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
		    System.err.println("Warning: precision (" + precision + ") < 0 in call to RHfitness, setting to -precision.");
    	}
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	expectedResult = dv[i];
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param precision double array expected to have a single value that is the 
	 *        percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	public static double RHfitness(GEPIndividual ind, double precision[]) 
    {
		if (precision.length != 1)
			System.err.println("Warning: 2nd arg to RHfitness method expected to have 1 double value (precision) but has " + precision.length);
        double RH = RHrawFitness(ind, precision[0]);
        // fitness is between 0 and the number of test cases
        return (RH);
	}

	/**
	 * The max value for this type of fitness is the length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double RHmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length);
	}

    //************************* AEWSR (Absolute Error with Selection Range) *****************
	
	/**
	 * Calculates the 'raw' fitness for the AEWSR (Absolute Error with Selection Range) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range range for the fitness calculation
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double AEWSRrawFitness(GEPIndividual ind, double range, double precision) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
    	int len = dv.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to AEWSR fitness, setting to -precision.");
    	}
    	
    	double totalError = 0.0;
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	expectedResult = dv[i];
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range_precision an array with 2 values expected - 1st the range for the fitness calculation
	 *        and 2nd the precision specified as a percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	static double AEWSRrange = 1.0;
	public static double AEWSRfitness(GEPIndividual ind, double range_precision[]) 
    {
		if (range_precision.length != 2)
			System.err.println("Warning: 2nd arg to AEWSRfitness method expected to have 2 double values (range and precision) but has " + range_precision.length);
		AEWSRrange = range_precision[0];
		double precision = range_precision[1];
        double AEWSR = AEWSRrawFitness(ind, AEWSRrange, precision);
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
	public static double AEWSRmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length * AEWSRrange);
	}

	/**
	 * The max value for this type of fitness is range * length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range value of the range used when the fitness was calculated; 1st value in doouble array
	 * @return range * length of the test data set
	 */
	public static double AEWSRmaxFitness(GEPIndividual ind, double range[]) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length * range[0]);
	}

    //************************* REWSR (Relative Error with Selection Range) *****************
	
	/**
	 * Calculates the 'raw' fitness for the REWSR (Relative Error with Selection Range) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range range for the fitness calcualtion
	 * @param precision specified as a percentage deviation from the expected value
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 */
	public static double REWSRrawFitness(GEPIndividual ind, double range, double precision) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
    	int len = dv.length;
    	
    	if (precision < 0.0)
    	{	precision = -precision;
    		System.err.println("Warning: precision (" + precision + ") < 0 in call to REWSR fitness, setting to -precision.");
    	}
    	
    	double totalError = 0.0;
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	expectedResult = dv[i];
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param range_precision an array with 2 values expected - 1st the range for the fitness calculation
	 *        and 2nd the precision specified as a percentage deviation from the expected value
	 * @return the fitness value after normalization from 0 to max value
	 */
	static double REWSRrange = 1.0;
	public static double REWSRfitness(GEPIndividual ind, double range_precision[]) 
    {
		if (range_precision.length != 2)
			System.err.println("Warning: 2nd arg to REWSRfitness method expected to have 2 double values (range and precision) but has " + range_precision.length);
		REWSRrange = range_precision[0];
		double precision = range_precision[1];
        double REWSR = REWSRrawFitness(ind, REWSRrange, precision);
        // fitness is between 0 and the number of test cases
        return (REWSR);
	}

	/**
	 * The max value for this type of fitness is range * length of the test data set.
	 * In this case range is not specified so use the value set when AEWSTfitness was last called.
	 * This version of the method is here to support the use of this fitness function from the GEPDefaultUserProg.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return range * length of the test data set
	 */
	public static double REWSRmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length * REWSRrange);
	}


    //************************* NH (Number of Hits) *****************
	
	/**
	 * Calculates the 'raw' fitness for the NH (Number of Hits) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 * Assumes that values are 1 or 0
	 */
	public static double NHrawFitness(GEPIndividual ind) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
        double sumOfHits = 0.0;
    	int len = dv.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
        	expectedResult = dv[i];
        	if (predictedValue == expectedResult)
        	    sumOfHits += 1.0;
        }        
        // the raw fitness ... NH
        return sumOfHits;
	}
    	
	/**
	 * Calculates the fitness for the NH (Number of Hits) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 * Assumes that values are 1 or 0
	 */
	public static double NHfitness(GEPIndividual ind) 
    {
        double NH = NHrawFitness(ind);
        // fitness is between 0 and the number of test cases
        return (NH);
	}

	/**
	 * The max value for this type of fitness is length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double NHmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length);
	}

    //************************* NHWP (Number of Hits with Penalty) *****************
	
	/**
	 * Calculates the 'raw' fitness for the NHWP (Number of Hits with Penalty) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double NHWPrawFitness(GEPIndividual ind) 
	{
        int confusionMatrix[] = getConfusionMatrixValues( ind );
        int truePositives = confusionMatrix[0];
        int trueNegatives = confusionMatrix[3];
        
        // the raw fitness ... NHWP
        if (truePositives==0 || trueNegatives==0)
            return 0.0;
        
        return (truePositives + trueNegatives);
	}
    	
	/**
	 * Calculates the fitness for the NHWP (Number of Hits with Penalty) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double NHWPfitness(GEPIndividual ind) 
    {
        double NHWP = NHWPrawFitness(ind);
        // fitness is between 0 and the number of test cases
        return (NHWP);
	}

	/**
	 * The max value for this type of fitness is length of the test data set.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return length of the test data set
	 */
	public static double NHWPmaxFitness(GEPIndividual ind) 
	{
		// maximum value is the number of test cases (since each one could meet the threshold)
		return (GEPDependentVariable.getDependentVariableValues().length);
	}
	
    //************************* ACC (Accuracy) *****************
	
	/**
	 * Calculates the 'raw' fitness for the ACC (Accuracy) type 
	 * fitness (before the normalization from 0 to max value is done).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double ACCrawFitness(GEPIndividual ind) 
	{
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
    	int len = dv.length;
        int confusionMatrix[] = getConfusionMatrixValues( ind );
        int truePositives = confusionMatrix[0];
        int trueNegatives = confusionMatrix[3];
    	
    	// the raw fitness ... ACC
        return ((truePositives + trueNegatives)/len);
	}
    	
	/**
	 * Calculates the fitness for the ACC (Accuracy) type fitness. 
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double ACCfitness(GEPIndividual ind) 
    {
        double ACC = ACCrawFitness(ind);
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SACCfitness(GEPIndividual ind) 
    {
        double ACC = ACCrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSrawFitness(GEPIndividual ind) 
	{
        int confusionMatrix[] = getConfusionMatrixValues( ind );
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
	 * Gets the raw fitness and then normalizes between 0 and max value (1000).
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSfitness(GEPIndividual ind) 
    {
        double SS = SSrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double PPVNPVrawFitness(GEPIndividual ind) 
	{
        int confusionMatrix[] = getConfusionMatrixValues( ind );
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double PPVNPVfitness(GEPIndividual ind) 
    {
        double PPVNPV = PPVNPVrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSPNrawFitness(GEPIndividual ind) 
	{
        int confusionMatrix[] = getConfusionMatrixValues( ind );
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
	 * Gets the raw fitness and then normalized between 0 and max value.
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @return the fitness value after normalization from 0 to max value
	 *
	 *  Expect all values to be 0 or 1; for classification threshold value should be set.
	 *  
	 */
	public static double SSPNfitness(GEPIndividual ind) 
    {
        double SSPN = SSPNrawFitness(ind);
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
	 * @param ind the GEP individual that needs its fitness calculated.
	 * @param corrWeight_RMSEweight - array that has RMSEweight, the weight to be applied to the normalized (from 0 to 1000) Correlation Coefficient
	 *        and RMSEweight, the weight to be applied to the normalized (from 0 to 1000) RMSE
	 * @return the fitness value
	 */
	public static double WCorrRMSEfitness(GEPIndividual ind, double corrWeight_RMSEweight[]) 
	{
        double dependentVar[] = GEPDependentVariable.getDependentVariableValues();
        int nSamples = dependentVar.length;
        double fitness = 0.0;
    	double modelMinusTargetSquared = 0.0;
    	double MSE = 0.0, RMSE=0.0, sx=0.0,   sx2=0.0,  sxy = 0.0, 
    	       sy = 0.0,  sy2=0.0,  ssx2=0.0, ssy2=0.0, ssxy=0.0;

    	for (int i=0; i<nSamples; i++)
    	{
    		double target = dependentVar[i];
    		double model = ind.eval(i);
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
	 * Given an individual calculate the true positive, false negative, false positive and
	 * true negative values of the model formed by the individual versus the expected values.
	 * 
	 * @param ind the individual for which we want to calculate the confusion matrix values
	 * @return double array with 4 values representing TP FN FP TN respectively
	 */
	public static int[] getConfusionMatrixValues(GEPIndividual ind) 
	{
        double expectedResult;
        double predictedValue;
        double dv[] = GEPDependentVariable.getDependentVariableValues();  
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
    	int len = dv.length;
    	
        for (int i=0; i<len; i++)
        {
        	predictedValue = ind.eval(i);
			if (Double.isNaN(predictedValue))
				// set all counts to 0 and return when an invalid value is found
		        return new int[]{0, 0, 0, 0};

        	expectedResult = dv[i];
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


}
