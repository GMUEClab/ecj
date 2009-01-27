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
import java.util.Hashtable;
import ec.*;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

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
	private static Hashtable htReplacementArrays = null;

	// class to hold an integer array that is used as a set of indicies to select from
	static private class IntegerArray
	{
		public int indicies[] = null;
		IntegerArray(int numberInSelectionSet)
		{
			indicies = new int[numberInSelectionSet];
            for (int i=0; i<numberInSelectionSet; i++)
            	indicies[i] = i;
		}
	}

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);
        // clear any arrays stored in the hashtable by assigning a null pointer
        // will be allocated when 1st used after all initializations are completed
        htReplacementArrays = null;
    }
    
    
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
    * a set of 0 to n-1 without replacement, guaranteeing unique values
    * are selected. Since the number in the set and the number selected is usually 
    * small this should be efficient enough.
    * For more efficiency we add a hash table to keep a list of tables of various
    * sizes (numberInSelectionSet) for the replacement selections ... saves creating these tables each time and
    * having to populate them with a sequence of integers ... if numberInSelectionSet is large
    * could be a lot of overhead creating tables each time.
    */
    public int[] chooseWithoutReplacement(EvolutionState state, int thread, int numberToSelect, int numberInSelectionSet)
    {
    	int i;
    	// first get an integer array of the right size initialize with all of the indexes
    	// It may have already been created previously or we create it and save it in a hash table
    	int indicies[];
    	Integer numInSet = Integer.valueOf(numberInSelectionSet);
    	if (htReplacementArrays == null)
    		htReplacementArrays = new Hashtable();
    	IntegerArray integerArrayObject = (IntegerArray)htReplacementArrays.get(numInSet);
    	if (integerArrayObject == null) 
    	{   // create a selectionSet of the required size since one has not been created yet
    		integerArrayObject = new IntegerArray(numberInSelectionSet);
    		htReplacementArrays.put(numInSet, integerArrayObject);
    	}
    	indicies = integerArrayObject.indicies;
    	// then get the required selected set without replacement
        int selectionSet[] = new int[numberToSelect];
        MersenneTwisterFast srt = state.random[thread];
        int numRemaining = numberInSelectionSet;
        for (i=0; i<numberToSelect; i++)
        {
        	int sel = srt.nextInt(numRemaining);
        	selectionSet[i] = indicies[sel];
        	numRemaining--;
        	indicies[sel] = indicies[numRemaining];
        	indicies[numRemaining] = selectionSet[i]; // indicies always has ALL of the indexes!
        }
    	
        return selectionSet;
    }

}
