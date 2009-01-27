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
import ec.simple.*;
import ec.util.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * @author Bob Orchard
 *
 * A class that will be the default user program if none is specified. If this is 
 * the case then the user will have to specify the 'fitness-function' parameter so that
 * this function can do the required fitness evaluation as evolution takes place.
 * Note that some fitness functions (such as AH -Absolute/Hits) take extra double arguments.
 * These are passed as a single extra argumnet in a double array. To specifiy these the user must
 * provide a series of parameters of the form:
 * 
 * fitness-function-arg0  1st extra double value
 * fitness-function-arg1  2nd extra double value
 * ...
 * 
 */

public class GEPDefaultUserProg extends GEPProblem implements SimpleProblemForm 
{	
	/**
	 * Holds the name of the fitness function to use when evaluating the model
	 */
	String fitnessFunction = "";
	/**
	 * The extra numeric (double) arguments to be used with certain fitness functions
	 */
	double ffArgs[];
	/**
	 * True if extra numeric args provided for the fitness function 
	 */
	boolean ffExtraArgs = false;
	
	/**
	 * Number of args required for the maxFitness function 
	 */
	int ffMaxArgs = 1;
	
	/**
	 * Flag to indicate the we are to use the training data set
	 */
	boolean ffUseTrainingSet = true;
	
	/**
	 * The parameters for specifying the fitness function:
	 * 
	 * fitness-function will be a string with the name of the fitness function to be used
	 * fitness-function-argN (with N = 0, 1, 2, ...) will be the extra double args for the ff
	 * fitness-function-ideal-percent-threshold will be a % of MaxThreshold required to be considered ideal;
	 *     default value will be 99.999999; use this to calculate maximum value for fitness to be 
	 *     considered ideal; so if max fitness value is 1000 and ideal threshold is 99.9 then
	 *     a fitness value >= 999 will be considered ideal; by default a value >= 999.99999
	 *     will be ideal
	 */
	public static final String P_FITNESS_FUNCTION = "fitness-function";
	public static final String P_FITNESS_FUNCTION_ARG = "fitness-function-arg";
	public static final String P_FITNESS_FUNCTION_IDEAL_PERCENT_THRESHOLD = "fitness-function-ideal-percent-threshold";
	
	/**
	 * Will use Java reflection to get find method to call for the user specified fitness function
	 */
	Method fitnessMethod = null;
	/**
	 * Will use Java reflection to get find method to call to find the maximum values returned 
	 * by the user specified fitness function
	 */
	Method fitnessMaxMethod = null;
	/**
	 * Percentage value of maximum fitness required to be considered an ideal fitness value.
	 */
	double idealValuePercentThreshold;
	public static final double idealValuePercentThresholdDefault = 99.999999;
	
    public void setup(final EvolutionState state, final Parameter base)
    {
        // get the requested function from the parameter file
        String fitnessName = state.parameters.getStringWithDefault(base.push(P_FITNESS_FUNCTION), 
        		base.push(P_FITNESS_FUNCTION), "");
        String fitnessMaxName = "";
        if (fitnessName.equals(""))
        	state.output.fatal("Must specify the fitness-function parameter when using the default user program (GEPDefaultUserProg).", 
        			base.push(P_FITNESS_FUNCTION));
        // get the fitness method (in GEPFitnessFunction) to use when evaluate is called.
        // The fitness methods usually have 2 arguments, but some have extra
        // double values associated with them. So try to find the right one.
		fitnessMaxName = fitnessName + "maxFitness";
		fitnessName += "fitness";
        try
        {   // try to find method with extra args -- 3rd arg is array of doubles
        	fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, 
        			new Class[]{boolean.class, GEPIndividual.class, double[].class});
        	ffExtraArgs = true; /// will have at least 1 numeric argument in an array of doubles
        }
        catch (Exception e1)
        {   
        	try
        	{	// try to find method with only 2 arguments
        		fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, new Class[]{boolean.class, GEPIndividual.class});
        		ffExtraArgs = false; // no extra numeric args for the fitness function
        	}
        	catch (NoSuchMethodException e3)
        	{
        		state.output.fatal("GEPDEfaultUserProg:No fitness-function '" + 
    			fitnessName + "' was found.\n" + e3);
        	}
        }
        // the maxFitness functions have 1 or 2 args
        try
        {
        	fitnessMaxMethod = GEPFitnessFunction.class.getMethod(fitnessMaxName, new Class[]{GEPIndividual.class});
        	ffMaxArgs = 1;
        }
        catch (NoSuchMethodException e1)
        {
        	try
        	{
        	   fitnessMaxMethod = GEPFitnessFunction.class.getMethod(fitnessMaxName, new Class[]{boolean.class, GEPIndividual.class});
        	   ffMaxArgs = 2;
        	}
	    	catch (NoSuchMethodException e3)
	    	{
	    		state.output.fatal("GEPDEfaultUserProg:No max fitness-function '" + 
				fitnessMaxName + "' was found.\n" + e3);
	    	}
        }
    	// get the extra args from the parameter file if required.
    	if (ffExtraArgs)
    	{
    		Vector extraArgsVec = new Vector();
    		int argnum = 0;
    		String ffArg_param_name = P_FITNESS_FUNCTION_ARG + argnum;
    		while (state.parameters.exists(base.push(ffArg_param_name)))
    		{
    			Double ffarg = new Double(state.parameters.getDoubleWithDefault(base.push(ffArg_param_name), 
    					base.push(ffArg_param_name), 0.0));
    			extraArgsVec.add(ffarg);
    			argnum++;
    			ffArg_param_name = P_FITNESS_FUNCTION_ARG + argnum;
    		}
    		ffArgs = new double[extraArgsVec.size()];
    		for (int i=0; i<ffArgs.length; i++)
    			ffArgs[i] = ((Double)extraArgsVec.get(i)).doubleValue();
    	}
        // the ideal fitness threshold value is determined by the fitness-function-ideal-percent-threshold
    	idealValuePercentThreshold =  
    		state.parameters.getDoubleWithDefault(base.push(P_FITNESS_FUNCTION_IDEAL_PERCENT_THRESHOLD), 
            		base.push(P_FITNESS_FUNCTION_IDEAL_PERCENT_THRESHOLD), idealValuePercentThresholdDefault);
    	
    	if (idealValuePercentThreshold <= 0.0 || idealValuePercentThreshold > 100.0)
    	{
    		state.output.warning("GEPDEfaultUserProg: setup found parameter " + P_FITNESS_FUNCTION_IDEAL_PERCENT_THRESHOLD +
    				" to be less than zero or greater than 100; defaulting to " + idealValuePercentThresholdDefault);
    		idealValuePercentThreshold = idealValuePercentThresholdDefault;
    	}
    }

	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) 
	{
        if (!ind.evaluated)  // don't bother reevaluating
        {
        	double fitness = 0.0, fitnessMax = 0.0;
        	Double fitnessD, fitnessMaxD;
        	// execute the fitness function
        	try
        	{
        		if (ffExtraArgs)
        			fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{ffUseTrainingSet, (GEPIndividual)ind, ffArgs});
        		else
	            	fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{ffUseTrainingSet, (GEPIndividual)ind});
        		
	            fitness = fitnessD.doubleValue();
	        	// execute the function that provides the maximum value for the fitness (e.g. 1000)
	            if (ffMaxArgs == 1)
	                fitnessMaxD = (Double)fitnessMaxMethod.invoke(null, new Object[]{(GEPIndividual)ind});
	            else
	            	fitnessMaxD = (Double)fitnessMaxMethod.invoke(null, new Object[]{ffUseTrainingSet, (GEPIndividual)ind});
	            fitnessMax = fitnessMaxD.doubleValue();
        	}
            catch (Exception e)
            {
            	state.output.fatal("GEPDefaultUserProg: Error executing fitness function or fitness max value function\n" +e);
            }

            // the fitness better be SimpleFitness!
            SimpleFitness f = ((SimpleFitness)ind.fitness);

            // Ideal fitness is a % of the max fitness 
            f.setFitness(state,(float)fitness, fitness >= fitnessMax*idealValuePercentThreshold);
            ind.evaluated = true;
        }
        
	}

}
