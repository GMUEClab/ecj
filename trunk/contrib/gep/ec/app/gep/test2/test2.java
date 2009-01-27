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

package ec.app.gep.test2;

import ec.*;
import ec.gep.*;
import ec.simple.*;

/**
 * @author Bob Orchard
 *
 */

public class test2 extends GEPProblem implements SimpleProblemForm 
{
    public static final double IDEAL_FITNESS_MINIMUM = 999.9999;

    public double xvalues[] = { 9.500366, -6.130432, 3.252685, 7.88797, 9.090484,
			1.485199, -3.950531, 10.003326, -0.607453, 5.469299};	
    public double zvalues[] = { 9103.54918799079, 1213.47815867463, 160.18146840485, 
			4432.23529247904, 7671.79392962917, 11.8327154232663, 193.570365560718, 
			11124.3786278048, -0.326443121119579, 1093.78835980998};  // x^4 + x^3 + x^2 + x
	
	
    public double[] getDataValues( String label )
    {
    	if (label.equals("x"))
    		return (xvalues);
    	else if (label.equals("dependentVariable")) // always called 'dependentVariable'
    		return (zvalues);
    	else
    		return null;   		
    }

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
