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

package ec.app.gep.Multiplexer6Bit;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class Multiplexer6Bit extends GEPProblem implements SimpleProblemForm 
{
	// Attempt to discover a 6 bit multiplexer function using and, or and not
	//
	// This looks at 6 boolean variables representing 2 control bits and
	// 4 data bits. The 1st 2 bits are the control bits and they identify which 
	// of the next 4 input bits will be in the output. For example, if the 
	// control bits are 00 then the 3rd bit value is value of the output;
	// if the control bits are 01 then the 4th bit value is the output value;
	// if the control bits are 10 then the 5th bit value is the output value;
	// if the control bits are 11 then the 6th bit value is the output value;
	// This can be expressed as:
	//
	// A&~c1&~c2 v B&~c1&c2 v C&c1&~c2 v D&c1&c2 = output
	// 
	// where c1 is control bit 1 and c2 is control bit 2
	//       A, B, C, D are the 4 input bits
	// 
	// so if we have 6 bits labelled d0, d1, d2, d3, d4, d5
	// and d0=c1, d1=c2, A=d2, B=d3, C=d4, D=d5
	// 
	// output = d2&~d0&~d1 v d3&~d0&d1 v d4&d0&~d1 v d5&d0&d1
	
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
            // sensitivity/specificity fitness is normalized between 0 and 1000  (1000 * raw SS)
            double fitness = GEPFitnessFunction.SSfitness(true, (GEPIndividual)ind);
                        
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
