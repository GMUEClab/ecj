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

package ec.app.gep.analogCircuit;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 * The goal is to find the transfer function expressing the yield of an analog circuit 
 * in terms of three parameter tolerances. The training set, consisting of n = 40 pairs 
 * of tolerances and their corresponding yields, was obtained from n runs of Monte Carlo 
 * simulations. Source:
 * 
 * Zielinski, L. and J. Rutkowski, 2004. Design Tolerancing with Utilization of Gene 
 * Expression Programming and Genetic Algorithm. In Proceedings of the International 
 * Conference on Signals and Electronic Systems, Poznan, Poland. 
 */

public class analogCircuit extends GEPProblem implements SimpleProblemForm 
{
	public double d0[] = { 0, 1, 1, 1, 4, 4, 1, 4, 3, 6, 10, 1, 1, 1, 14, 10, 17, 
			10, 1, 3, 17, 11, 1, 10, 23, 4, 12, 10, 11, 29, 24, 6, 17, 0, 14, 35, 
			35, 24, 29, 15};	
	public double d1[] = { 0, 2, 2, 2, 4, 3, 1, 2, 0, 2, 3, 7, 8, 5, 9, 13, 2, 5, 
			10, 3, 3, 21, 9, 10, 17, 19, 1, 22, 7, 14, 12, 12, 3, 21, 26, 7, 10, 
			15, 5, 28 };
	public double d2[] = { 0, 1, 2, 2, 4, 4, 3, 6, 5, 1, 2, 7, 6, 3, 13, 7, 9, 7, 
			16, 1, 19, 3, 9, 9, 24, 10, 15, 22, 10, 11, 28, 29, 24, 33, 24, 21, 
			19, 18, 2, 37 };
	public double dv[] = { 100, 99, 97, 99, 78, 82, 99, 80, 85, 78, 73, 66, 62, 
			89, 57, 64, 62, 66, 58, 93, 56, 65, 71, 72, 53, 65, 56, 58, 69, 43, 
			45, 62, 50, 51, 54, 39, 39, 43, 52, 48 }; 
	
	public static final double IDEAL_FITNESS_MINIMUM = 999.99999999;
		
    public void setup(final EvolutionState state,
            final Parameter base)
    {
        super.setup(state, base);
    }

    public double[] getDataValues( String label )
    {
    	if (label.equals("d0"))
    		return (d0);
    	else if (label.equals("d1"))
    		return (d1);
    	else if (label.equals("d2"))
    		return (d2);
    	else if (label.equals("dependentVariable")) // always called 'dependentVariable'
    		return (dv);
    	else
    		return null;   		
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
