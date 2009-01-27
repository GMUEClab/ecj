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

package ec.app.gep.BreastCancer;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 */

public class BreastCancer extends GEPProblem implements SimpleProblemForm 
{
	//Diagnosis of breast cancer:
    //
	// The goal is to classify a tumor as either benign (0) or malignant (1) based on nine different 
	// cell analysis (clump thickness, uniformity of cell size, uniformity of cell shape, marginal 
	// adhesion, single epithelial cell size, bare nuclei, bland chromatin, normal nucleoli, and mitoses).
    //
	//Real world data obtained from PROBEN1 (Prechelt, L., 1994. PROBEN1 - A set of neural network 
	// benchmark problems and benchmarking rules. Technical Report 21/94, Univ. Karlsruhe, Germany).
    //
	// Both the technical report and the data set cancer1 used here are available for anonymous FTP 
	// from Neural Bench archive at Carnegie Mellon University (machine ftp.cs.cmu.edu, directory 
	// /afs/cs/project/connect/bench/contrib/prechelt) and from machine ftp.ira.uka.de in directory 
	// /pub/neuron. The file name in both cases is proben1.tar.gz.	
	
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
            double fitness = GEPFitnessFunction.SSPNfitness(true, (GEPIndividual)ind);
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
