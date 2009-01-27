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

package ec.app.gep.DNAmicroarrays;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class DNAmicroarrays extends GEPProblem implements SimpleProblemForm 
{
	/* ALL-AML Leukemia (http://sdmc.lit.org.sg/GEDatasets/Datasets.html#ALL-AML_Leukemia)

		Publication:
		"Molecular Classification of Cancer: Class Discovery and Class Prediction by Gene 
		Expression Monitoring". Science, 286:531-537, October 1999

		Number of Instances:
		38 training samples v.s. 34 testing samples

		Number of Attributes:
		7129 (all numeric)

		Number of Classes:
		2 (ALL v.s. AML)

		Data Source:
		http://www-genome.wi.mit.edu/cgi-bin/cancer/publications/pub_paper.cgi?mode=view&paper_id=43

		Description:
		Training dataset consists of 38 bone marrow samples (27 ALL and 11 AML), over 7129 
		probes from 6817 human genes. Also 34 samples testing data is provided, with 20 ALL 
		and 14 AML.

		In this run, the transformed datasets available at:

		http://sdmc.lit.org.sg/GEDatasets/Data/ALL-AML_Leukemia.zip.

		were used with "0" representing "ALL" and "1" representing "AML". The 7129 genes were 
		numbered d0-d7128.
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
            // SSPN fitness is normalized between 0 and 1000  (1000 * raw SSPN)
            double fitness = GEPFitnessFunction.SSfitness(true, (GEPIndividual)ind);
            // with parsimony pressure
            //fitness = GEPFitnessFunction.parsimonyFitness((GEPIndividual)ind, fitness);
                        
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
