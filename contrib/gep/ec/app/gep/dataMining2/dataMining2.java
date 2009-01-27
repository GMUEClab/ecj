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

package ec.app.gep.dataMining2;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class dataMining2 extends GEPProblem implements SimpleProblemForm 
{
	// d0^4 + d0^3 + d0^2 + d0   .... 16 variables (d0, d1, ... , d15) but only 1 is significant (d0)
	
	public static final double IDEAL_FITNESS_MINIMUM = 999.99999999;
			
    public void setup(final EvolutionState state,
            final Parameter base)
    {
        super.setup(state, base);    
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
