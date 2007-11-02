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
import ec.gep.*;
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
	 * The parameters for specifiying the fitness function:
	 * 
	 * fitness-function will be a string with the name of the fitness function to be used
	 * fitness-function-argn (with n = 0, 1, 2, ...) will be the extra double args for the ff
	 */
	public static final String P_FITNESS_FUNCTION = "fitness-function";
	public static final String P_FITNESS_FUNCTION_ARG = "fitness-function-arg";
	
	/**
	 * Will use Java reflection to get find method to call for the user specified fitness function
	 */
	Method fitnessMethod = null;
	/**
	 * Will use Java reflection to get find method to call to find the maximum values returned 
	 * by the user specified fitness function
	 */
	Method fitnessMaxMethod = null;
	
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
        // The fitness methods usually have 1 argument, but some have extra
        // double values associated with them. So try to find the right one.
		fitnessMaxName = fitnessName + "maxFitness";
		fitnessName += "fitness";
        try
        {   // try to find method with extra args -- 2nd arg is array of doubles
        	fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, 
        			new Class[]{GEPIndividual.class, double[].class});
        	ffExtraArgs = true; /// will have at least 1 numeric argument in an array of doubles
        }
        catch (Exception e1)
        {   
        	try
        	{	// try to find method with only 1 argument
        		fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, new Class[]{GEPIndividual.class});
        		ffExtraArgs = false; // no extra numeric args for the fitness function
        	}
        	catch (NoSuchMethodException e3)
        	{
        		state.output.fatal("GEPDEfaultUserProg:No fitness-function '" + 
    			fitnessName + "' was found.\n" + e3);
        	}
        }
        try
        {
        	fitnessMaxMethod = GEPFitnessFunction.class.getMethod(fitnessMaxName, new Class[]{GEPIndividual.class});
        }
    	catch (NoSuchMethodException e3)
    	{
    		state.output.fatal("GEPDEfaultUserProg:No max fitness-function '" + 
			fitnessName + "' was found.\n" + e3);
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
    }

	public void evaluate(EvolutionState state, Individual ind, int threadnum) 
	{
        if (!ind.evaluated)  // don't bother reevaluating
        {
        	double fitness = 0.0, fitnessMax = 0.0;
        	Double fitnessD;
        	// execute the fitness function
        	try
        	{
        		if (ffExtraArgs)
        			fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{(GEPIndividual)ind, ffArgs});
        		else
	            	fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{(GEPIndividual)ind});
        		
	            fitness = fitnessD.doubleValue();
	        	// execute the function that provides the maximum value for the fitness (e.g. 1000)         
	            Double fitnessMaxD = (Double)fitnessMaxMethod.invoke(null, new Object[]{(GEPIndividual)ind});
	            fitnessMax = fitnessMaxD.doubleValue();
        	}
            catch (Exception e)
            {
            	state.output.fatal("GEPDefaultUserProg: Error executing fitness function or fitness max value function\n" +e);
            }

            // the fitness better be SimpleFitness!
            SimpleFitness f = ((SimpleFitness)ind.fitness);
            // using 0.000001 as the test for close enough for ideal fitness ...
            // should this be a % of the max fitness? or a parameter the user can supply?
            f.setFitness(state,(float)fitness, fitness >= fitnessMax-0.000001);
            ind.evaluated = true;
        }
        
	}

}
