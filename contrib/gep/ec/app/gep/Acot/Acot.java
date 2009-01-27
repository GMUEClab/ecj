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

package ec.app.gep.Acot;

import ec.*;
import ec.gep.*;
import ec.simple.*;
import ec.util.*;

/**
 * @author Bob Orchard
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class Acot extends GEPProblem implements SimpleProblemForm 
{
	public double d0[] = { -4.83371, -2.832672, -2.450012, -4.087188, 1.764069, 
				3.117889, -2.37262, 2.959961, -9.791626, 5.834656, 
				6.461578, -2.049163, 5.536804, -8.468872, -1.947632, 
				4.042388, -7.923981, -6.171875, 4.865174, 0.748108, 
				-7.331268, 9.734741, 3.203827, 6.808136, 6.143737, 
				-6.481415, 6.478943, 0.59082, 7.007354, 9.614777, 
				8.614411, 0.122894, -2.205597, -1.527985, 6.449463, 
				-2.765625, 7.462006, 8.943696, -7.640595, 10.510193, 
				-8.85141, 3.787354, 9.49408, 3.857972, 4.878724, 2.530701, 
				-1.838776, -10.274475, 8.499023, -6.599457, 4.213104, 
				-5.061157, 4.581726, 7.170105, -4.207184, 8.169006, -5.834442, 
				6.678772, -8.471558, 8.60791, -0.475128, -1.529175, 7.212036, 
				-1.688873, -8.433563, 4.978669, 7.610016, 6.983338, -9.12091, 
				-9.971741, -3.653992, -2.010986, 7.824829, 5.764221, -5.066558, 
				-8.305694, 3.696533, 3.682587, -7.811798, 6.572998, 7.460632, 
				-6.138458, -1.323303, -0.585846, -2.174499, 7.304077, 4.330902, 
				-8.731293, -7.403015, -9.107269, -3.576965, 4.497925, 8.719666, 
				-2.198028, 6.076995, 6.793885, -7.965363, -5.422485, 6.164703, 
				7.102264 };	
	 // ArcCotangent ??
	public double dv[] = { -0.204002518946359, -0.339365885046348, -0.387522092113786, 
				-0.23995309898349, 0.515703851271719, 0.310364838997948, 
				-0.398881150981012, 0.325803110566599, -0.101775219113995, 
				0.169740521776484, 0.153542857700792, -0.454004938789551, 
				0.17868335196723, -0.117535240729037, -0.4743448875263, 
				0.242509879207332, -0.125535562693882, -0.16062939840381, 
				0.202719156879737, 0.928507198080555, -0.135565428267246, 
				0.102365808997645, 0.302544758523445, 0.145840232253069, 
				0.161352398626661, -0.15308024294798, 0.153137741056787, 
				1.03715417142293, 0.141750125877162, 0.103633962049548, 
				0.115567285852831, 1.44851546689027, -0.42567111948264, 
				-0.57950183891494, 0.153826756152, -0.346955299723584, 
				0.133218502617967, 0.111348124793134, -0.130140143503072, 
				9.48601719304978E-02, -0.112499334695348, 0.258145313345448, 
				0.104941855452515, 0.253621885575468, 0.202171368811478, 
				0.376316141354259, -0.498101571229328, -9.70229826177712E-02, 
				0.117122083963137, -0.15038361480415, 0.233042246284276, 
				-0.195070714200252, 0.214888462410712, 0.138574102239854, 
				-0.233358396018627, 0.121807889527326, -0.169746628740468, 
				0.148624043292117, -0.117498316995845, 0.115653790450597, 
				-1.12724355904412, -0.57914518379372, 0.137778614635782, 
				-0.534598447940947, -0.118022780757836, 0.198219362072984, 
				0.130657160517551, 0.142231073751556, -0.109202021407799, 
				-0.099949229595542, -0.267132358167109, -0.461460025622122, 
				0.127109307831937, 0.171774317146796, -0.194867994093927, 
				-0.119822569536449, 0.264199940787423, 0.26515430898479, 
				-0.127319059035346, 0.150979830656403, 0.133242747702631, 
				-0.161488760836055, -0.647129481967589, -1.04084919276999, 
				-0.431036429046218, 0.136063905296291, 0.226921846159594, 
				-0.114033703349753, -0.134267392560912, -0.109364285648061, 
				-0.272606780871568, 0.218766635858583, 0.114184441106594, 
				-0.426965415381473, 0.163093395976242, 0.146141817137733, 
				-0.124890152806863, -0.182368282843355, 0.160813070832659, 
				0.139880646214395 }; 
	
	public static final double IDEAL_FITNESS_MINIMUM = 999.99999999;
	
	public double meanOfDV = 0.0;
	
    public void setup(final EvolutionState state,
            final Parameter base)
    {
        super.setup(state, base);   
    }

    public double[] getDataValues( String label )
    {
    	if (label.equals("d0"))
    		return (d0);
    	else if (label.equals("dependentVariable"))
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
