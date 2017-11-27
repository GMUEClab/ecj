/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.Individual;
import ec.co.ant.AntSpecies;
import ec.util.Parameter;
import java.util.Arrays;

/**
 *
 * @author Eric O. Scott
 */
public class ConstructiveIndividual extends Individual
{
    
    public static final String P_CONSTRUCTIVEINDIVIDUAL = "constr-ind";
    public int[] genome = new int[] { };
    
    public Parameter defaultBase()
    {
        return AntSpecies.DEFAULT_BASE.push(P_CONSTRUCTIVEINDIVIDUAL);
    }

    public Object clone()
    {
        ConstructiveIndividual myobj = (ConstructiveIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (int[])(genome.clone());
        
        return myobj;
    } 
    
    public void setGenome(final int[] genome)
    {
        assert(genome != null);
        this.genome = genome;
        assert(repOK());
    }
    
    public int genomeLength()
    {
        return genome.length;
    }

    @Override
    public boolean equals(final Object ind)
    {
        if (ind == this)
            return true;
        if (!(ind instanceof ConstructiveIndividual))
            return false;
        return Arrays.equals(genome, ((ConstructiveIndividual)ind).genome);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Arrays.hashCode(this.genome);
        return hash;
    }
    
    public final boolean repOK()
    {
        return P_CONSTRUCTIVEINDIVIDUAL != null
                && !P_CONSTRUCTIVEINDIVIDUAL.isEmpty()
                && genome != null;
    }
}
