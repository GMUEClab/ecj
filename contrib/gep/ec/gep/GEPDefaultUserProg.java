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
 * the case then the user will have to specify the 'fitness-function' paramater so that
 * this function can do the required fitness evaluation as evolution takes place.
 * 
 */

public class GEPDefaultUserProg extends GEPProblem implements SimpleProblemForm 
{	
	/**
	 * Holds the name of the fitness function to use when evaluating the model
	 */
	String fitnessFunction = "";
	/**
	 * The 1st numeric argument to be used with the fitness function
	 */
	Double ffArg0;
	/**
	 * The 2nd numeric argument to be used with the fitness function
	 */
	Double ffArg1;
	/**
	 * The number of numeric args that the fitness function requires
	 */
	int ffNumArgs = 0;
	
	/**
	 * The paramater for specifiying the fitness function
	 */
	public static final String P_FITNESS_FUNCTION = "fitness-function";
	public static final String P_FITNESS_FUNCTION_ARG0 = "fitness-function-arg0";
	public static final String P_FITNESS_FUNCTION_ARG1 = "fitness-function-arg1";
	
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
        // The fitness methods usually have 1 argument, but some have 1 or 2 extra
        // double values associated with them. So try to find the right one.
		fitnessMaxName = fitnessName + "maxFitness";
		fitnessName += "fitness";
        try
        {   // try to find method with 3 args
        	fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, 
        			new Class[]{GEPIndividual.class, double.class, double.class});
        	ffNumArgs = 2;
        }
        catch (Exception e1)
        {   
            try
            {   // try to find method with 2 args
            	fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, 
            			new Class[]{GEPIndividual.class, double.class});
            	ffNumArgs = 1;
            }
            catch (Exception e2)
            { 
            	try
            	{	// try to find method with 1 arg
            		fitnessMethod = GEPFitnessFunction.class.getMethod(fitnessName, new Class[]{GEPIndividual.class});
            		ffNumArgs = 0;
            	}
            	catch (NoSuchMethodException e3)
            	{
            		state.output.fatal("GEPDEfaultUserProg:No fitness-function '" + 
        			fitnessName + "' was found.\n" + e3);
            	}
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
    	// get the args from the parameter file if required.
    	if (ffNumArgs > 0)
    	{
    		if (state.parameters.exists(base.push(P_FITNESS_FUNCTION_ARG0)))
    			ffArg0 = new Double(state.parameters.getDoubleWithDefault(base.push(P_FITNESS_FUNCTION_ARG0), 
    					base.push(P_FITNESS_FUNCTION_ARG0), 0.0));
    		else
    			state.output.fatal("GEPDEfaultUserProg: Fitnsess function '" + fitnessName + 
    					"' requires 2 numeric arguments be specified with fitness-function-arg1 and fitness-function-arg1 parameters.", 
    					base.push(P_FITNESS_FUNCTION_ARG0));
    		if (ffNumArgs == 2)
    		{
        		if (state.parameters.exists(base.push(P_FITNESS_FUNCTION_ARG1)))
        			ffArg1 = new Double(state.parameters.getDoubleWithDefault(base.push(P_FITNESS_FUNCTION_ARG1), 
        					base.push(P_FITNESS_FUNCTION_ARG1), 0.0));
        		else
        			state.output.fatal("GEPDEfaultUserProg: Fitnsess function '" + fitnessName + 
        					"' requires 2 numeric arguments be specified with fitness-function-arg1 and fitness-function-arg1 parameters.", 
        					base.push(P_FITNESS_FUNCTION_ARG1));
    		}
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
	            switch (ffNumArgs)
	            {
	             default:
	             case 0:
	            	fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{(GEPIndividual)ind});
	            	break;
	             case 1:
	            	fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{(GEPIndividual)ind, ffArg0});
	            	break;
	             case 2:
	            	fitnessD = (Double)fitnessMethod.invoke(null, new Object[]{(GEPIndividual)ind, ffArg0, ffArg1});
	            }
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
