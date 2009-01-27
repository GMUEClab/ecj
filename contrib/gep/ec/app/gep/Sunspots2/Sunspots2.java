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

package ec.app.gep.Sunspots2;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class Sunspots2 extends GEPProblem implements SimpleProblemForm 
{

 /* Time Series Prediction task using the Wolfer sunspots data.
 */
	
	public static final double IDEAL_FITNESS_MINIMUM = 999.99999999;
	
	static double timeSeriesData[] = 
	         { 101,  82, 66, 35, 31,  7,  20,  92, 154, 125, 
		        85,  68, 38, 23, 10, 24,  83, 132, 131, 118,
		        90,  67, 60, 47, 41, 21,  16,   6,   4,   7,
		        14,  34, 45, 43, 48, 42,  28,  10,   8,   2,
		         0,   1,  5, 12, 14, 35,  46,  41,  30,  24,
		        16,   7,  4,  2,  8, 17,  36,  50,  62,  67,
		        71,  48, 28,  8, 13, 57, 122, 138, 103,  86, 
		        63,  37, 24, 11, 15, 40,  62,  98, 124,  96,
		        66,  64, 54, 39, 21,  7,   4,  23,  55,  94,
		        96,  77, 59, 44, 47, 30,  16,   7,  37,  74 
		      };
			
    public double[] getTimeSeriesDataValues()
    {
    	return timeSeriesData;
    }

	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) 
	{
        if (!ind.evaluated)  // don't bother reevaluating
        {
            // fitness is normalized between 0 and 1000  (1000 * (1/(1+RRSE))
            double fitness = GEPFitnessFunction.RRSEfitness(true, (GEPIndividual)ind);
                        
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
