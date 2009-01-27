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

package ec.app.gep.test1;

import ec.*;
import ec.gep.*;
import ec.simple.*;

/**
 * @author Bob Orchard
 *
 *  The problem is to find the equation x*x + y 
 *  
 *  Data and variable names are in the data file test1.txt
 */

public class test1 extends GEPProblem implements SimpleProblemForm 
{
    static double IDEAL_FITNESS_MINIMUM = 999.9999;
	
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) 
    {
        if (!ind.evaluated)  // don't bother reevaluating
        {
            // Mean Squared Error (MSE) fitness is normalized between 0 and 1000 (1000 * (1/(1+MSE))
            double fitness = GEPFitnessFunction.MSEfitness(true, (GEPIndividual)ind);
            
            // the fitness better be SimpleFitness!
            SimpleFitness f = ((SimpleFitness)ind.fitness);
            f.setFitness(state,(float)fitness, fitness >= IDEAL_FITNESS_MINIMUM);
            ind.evaluated = true;
           
	    if (fitness >= IDEAL_FITNESS_MINIMUM)
	    {	
	        ((GEPIndividual)ind).printIndividualForHumans(state, 1, 1);
	    }	
        }
    }
}
