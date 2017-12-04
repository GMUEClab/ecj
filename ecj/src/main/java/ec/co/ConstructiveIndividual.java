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
    public int[] path = new int[] { };
    
    public Parameter defaultBase()
    {
        return AntSpecies.DEFAULT_BASE.push(P_CONSTRUCTIVEINDIVIDUAL);
    }

    public Object clone()
    {
        ConstructiveIndividual myobj = (ConstructiveIndividual) (super.clone());

        // must clone the path
        myobj.path = (int[])(path.clone());
        
        return myobj;
    } 
    
    public void setPath(final int[] path)
    {
        assert(path != null);
        this.path = path;
        assert(repOK());
    }
    
    public int pathLength()
    {
        return path.length;
    }

    @Override
    public boolean equals(final Object ind)
    {
        if (ind == this)
            return true;
        if (!(ind instanceof ConstructiveIndividual))
            return false;
        return Arrays.equals(path, ((ConstructiveIndividual)ind).path);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Arrays.hashCode(this.path);
        return hash;
    }
    
    public final boolean repOK()
    {
        return P_CONSTRUCTIVEINDIVIDUAL != null
                && !P_CONSTRUCTIVEINDIVIDUAL.isEmpty()
                && path != null;
    }
}
