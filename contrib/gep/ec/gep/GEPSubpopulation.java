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

package ec.gep;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

import ec.*;
import ec.simple.*;
import ec.util.Parameter;

/* 
 * GEPSubpopulation.java
 * 
 * Created By: Bob Orchard
 */

/**
 *  See ec.Subpopulation for most definitions and parameters.
 *  
 *  This was created to extend the ability of the GEP system when creating the initial population.
 *  There are cases where one can get an initial population with many (if not all) individuals with
 *  fitness values of zero. This will lead to very slow conversion on a solultion. This is often
 *  due to initial models having expressions that result in NaN or +/- infinity for the
 *  training values. Since these are improper models we'd like to try to start with better ones.
 *  
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>zero-fitness-retries</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(during initialization, when we produce an individual which already exists in the subpopulation, the number of times we try to replace it with something unique.  Ignored if we're loading from a file.)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 </table>

 */

public class GEPSubpopulation extends Subpopulation {

    /** Do we allow individuals with 0 fitness in the initial population? 
     *  Try to generate individuals with non-zero fitness if this count is > 0
     **/
    public int numZeroFitnessRetries;
    
    public static final String P_ZERO_FITNESS_RETRIES = "zero-fitness-retries";

    public void setup(final EvolutionState state, final Parameter base)
    {
     super.setup(state, base);
     
     // How often do we retry if we find a zero fitness value?
     numZeroFitnessRetries = state.parameters.getInt(
         base.push(P_ZERO_FITNESS_RETRIES),null,0);
     if (numZeroFitnessRetries < 0) state.output.fatal(
         "The number of retries for individuals with 0 fitness must be an integer >= 0.\n",
         base.push(P_ZERO_FITNESS_RETRIES),null);
    
    }



    public void populate(EvolutionState state, int thread)
    {
    // should we load individuals from a file? -- duplicates are permitted
    if (loadInds!=null)
        {
        /*
        // let's make some individuals!
        try
        {
        LineNumberReader reader = new LineNumberReader(new FileReader(loadInds));
        for(int x=0;x<individuals.length;x++)
        individuals[x] = species.newIndividual(state,reader);
        state.output.message("Loading subpopulation from file " + loadInds);
        }
        catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + loadInds + ".  The IOException was: \n" + e); }
        */
        try { readSubpopulation(state, new LineNumberReader(new FileReader(loadInds))); }
        catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + loadInds + ".  The IOException was: \n" + e); }
        }
    else
        {
        Hashtable h = null;
        if (numDuplicateRetries >= 1)
            h = new Hashtable(individuals.length / 2);  // seems reasonable

        for(int x=0;x<individuals.length;x++) 
            {
            for(int tries=0; 
                tries <= /* Yes, I see that*/ numDuplicateRetries; 
                tries++)
                {
                individuals[x] = species.newIndividual(state, thread);

                for (int zeroFitnessTries=0; zeroFitnessTries <= numZeroFitnessRetries; zeroFitnessTries++)
                {
                	((SimpleProblemForm)(state.evaluator.p_problem)).evaluate(state, individuals[x], 0, 0);
                	if (individuals[x].fitness.fitness() <= 0.0)
                		individuals[x] = species.newIndividual(state, thread);
                	else break;
                }
                if (numDuplicateRetries >= 1)
                    {
                    // check for duplicates
                    Object o = h.get(individuals[x]);
                    if (o == null) // found nothing, we're safe
                        // hash it and go
                        {
                        h.put(individuals[x],individuals[x]);
                        break;
                        }
                    }
                }  // oh well, we tried to cut down the duplicates
            }
        }
    }
    

}
