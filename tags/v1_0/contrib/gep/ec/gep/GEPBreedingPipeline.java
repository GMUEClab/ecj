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
import java.util.Vector;

import ec.*;
import ec.util.MersenneTwisterFast;

/* 
 * GEPBreedingPipeline.java
 *  
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * A GEPBreedingPipeline is a BreedingPipeline which produces only
 * members of some subclass of GEPSpecies. This is NOT just a convenience
 * superclass for the GEP breeding pipelines; you must be a
 * GEPBreedingPipeline in order to breed GEPSpecies; they work much differently than
 * other pipelines since they must move ALL individuals (excluding the elite ones)
 * from one breeding operator to the next.
 * <br>
 * We also provide a useful method for selecting m random integers from 
 * a set of 0 to n-1 without replacement, guaranteeing that unique values
 * are selected.
 *
 * @author Bob Orchard
 * @version 1.0 
 */

public abstract class GEPBreedingPipeline extends BreedingPipeline 
{

    /** Returns true if <i>s</i> is a GEPSpecies. */
    public boolean produces(final EvolutionState state,
                            final Population newpop,
                            final int subpopulation,
                            final int thread)
    {
        if (!super.produces(state,newpop,subpopulation,thread)) return false;

        // we produce individuals which are owned by subclasses of GEPSpecies
        if (newpop.subpops[subpopulation].species instanceof GEPSpecies)
            return true;
        return false;
    }
    
    /* 
    * We  provide here, a useful method for selecting m random integers from 
    * a set of 0 to n-1 without replacement, guaranteeing uniques values
    * are selected. Since the number in the set and the number selected is usually 
    * small this should be efficient enough.
    */
    public int[] chooseWithoutReplacement(EvolutionState state, int thread, int numberToSelect, int numberInSelectionSet)
    {
/*    	int i;
        Vector indicies = new Vector(numberInSelectionSet);
        int selectionSet[] = new int[numberToSelect];
        for (i=0; i<numberInSelectionSet; i++)
        	indicies.add(new Integer(i));
        MersenneTwisterFast srt = state.random[thread];
        for (i=0; i<numberToSelect; i++)
        {
        	int sel = srt.nextInt(indicies.size());
        	selectionSet[i] = ((Integer)indicies.elementAt(sel)).intValue();
        	indicies.removeElementAt(sel);
        }
*/
    	int i;
        int indicies[] = new int[numberInSelectionSet];
        int selectionSet[] = new int[numberToSelect];
        for (i=0; i<numberInSelectionSet; i++)
        	indicies[i] = i;
        MersenneTwisterFast srt = state.random[thread];
        int numRemaining = numberInSelectionSet;
        for (i=0; i<numberToSelect; i++)
        {
        	int sel = srt.nextInt(numRemaining);
        	selectionSet[i] = indicies[sel];
        	numRemaining--;
        	indicies[sel] = indicies[numRemaining];
        }
    	
        return selectionSet;
    }

}
