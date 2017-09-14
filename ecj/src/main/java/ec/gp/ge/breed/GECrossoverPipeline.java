/*
  Copyright 2013 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.gp.ge.breed;

/* 
 * GECrossoverPipeline.java
 * 
 * Created: Thu Jan  2 14:45:51 EST 2014
 * By: Sean Luke
 */
 
import ec.vector.*;
import ec.vector.breed.*;
import ec.*;
import ec.util.*;
import ec.gp.ge.*;


/**
   GECrossoverPipeline is just like ListCrossoverPipeline, except that it will additionally
   check to verify that the first crossover point is within the range of consumed genes
   in each parent.  This is not uncommon in the GE literature.
   
   <p>For simplicity, GECrossoverPipeline shares the same default base as ListCrossoverPipeline,
   since it adds no new parameters.
   
   <p><b>Number of Sources</b><br>
   2

   <p><b>Default Base</b><br>
   vector.list-xover
**/

public class GECrossoverPipeline extends ListCrossoverPipeline
    {
    public Object computeValidationData(EvolutionState state, VectorIndividual[] parents, int thread)
        {
        if (!(parents[0] instanceof GEIndividual) ||
            !(parents[1] instanceof GEIndividual))
            state.output.fatal("Non GEIndividuals used with GECrossoverPipeline.", null, null);
        
        return new int[] {      ((GESpecies)(parents[0].species)).consumed(state, ((GEIndividual)(parents[0])), thread),
            ((GESpecies)(parents[1].species)).consumed(state, ((GEIndividual)(parents[1])), thread) } ;
        }

    public boolean isValidated(int[][] split, Object validationData)
        {
        int[] consumed = (int[]) validationData;
                
        return split[0][0] < consumed[0] && split[1][0] < consumed[1];
        }    
    }
    
    
    
    
    
    
    
