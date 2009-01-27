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

package ec.app.gep.IrisVirginica;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class IrisVirginica extends GEPProblem implements SimpleProblemForm 
{
	/* Implements the GeneXproTools	Iris Virginica classification example ... public domain data
	   In the classic iris problem the goal is to classify three different types of irises based 
	   on four measurements: sepal length, sepal width, petal length, and petal width.
	   The original iris dataset contains fifty examples each of three types of 
	   iris: Iris setosa, Iris versicolor, and Iris virginica. Here the sub-problem Virginica 
	   versus Not Virginica is analyzed, where 100 randomly chosen samples are used for training 
	   and the remaining 50 for testing.
	   
	   In one run with the following paramaters a very simple expression was found
	   to distinguish almost all of the training examples(24 true positive, 65 true negative 
	   and only 1 false positive). Used the starting random seed: 1583933764
	   
	   Expression found was:  (d2-d1) + C(d3-C) + (d2-d0)  where C was 1.809238125812137
	   
	   If the expression value is >= 0.5 then it is in the virginica class.
	   
		Params set:
		   
		# subpop size is 30 individuals
		pop.subpop.0.size =			30
	
		# ec.Species
		# ==============================
	
		gep.species.inversion-prob         = 0.1
		gep.species.mutation-prob          = 0.044
		gep.species.istransposition-prob   = 0.1
		gep.species.ristransposition-prob  = 0.1
		gep.species.onepointrecomb-prob    = 0.3
		gep.species.twopointrecomb-prob    = 0.3
		gep.species.generecomb-prob        = 0.1
		gep.species.genetransposition-prob = 0.1
	
		gep.species.use-constants             = true
		#examples for constants if used
		gep.species.numconstantspergene       = 2
		gep.species.integer-constants         = false
		gep.species.constants-lowerlimit      = 0
		gep.species.constants-upperlimit      = 10
		gep.species.rnc-mutation-prob         = 0.01
		gep.species.dc-mutation-prob          = 0.044
		gep.species.dc-inversion-prob         = 0.1
		gep.species.dc-istransposition-prob   = 0.1
	
		gep.species.numgenes = 3
		gep.species.gene.headsize = 8
		gep.species.gene.linking-function = +
	
		# if the problem is a classification type problem a threshold value (used
		# to convert real values to 0 or 1 during fitness calculations) must be 
		# specified (do not specify this unless it IS a classification type problem).
		gep.species.gene.classification-threshold  = 0.5
	
		gep.species.symbolset   = ec.gep.GEPSymbolSet
		gep.species.symbolset.terminalfilename = ec/gep/test/IrisVirginica.txt
		gep.species.symbolset.testingdatafilename = ec/gep/test/IrisVirginicaTest.dat
		gep.species.symbolset.functionsize = 4
		gep.species.symbolset.function.0 = Add
		gep.species.symbolset.function.0.weight = 1
		gep.species.symbolset.function.1 = Sub
		gep.species.symbolset.function.1.weight = 1
		gep.species.symbolset.function.2 = Mul
		gep.species.symbolset.function.2.weight = 1
		gep.species.symbolset.function.3 = Div
		gep.species.symbolset.function.3.weight = 1
    
    */

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
