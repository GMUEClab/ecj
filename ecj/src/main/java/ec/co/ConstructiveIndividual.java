/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.Individual;
import ec.co.ant.AntSpecies;
import ec.util.Misc;
import ec.util.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eric O. Scott
 */
public class ConstructiveIndividual extends Individual
{
    
    public static final String P_CONSTRUCTIVEINDIVIDUAL = "constr-ind";
    public Set<Integer> components = new HashSet<Integer>();
    
    @Override
    public Parameter defaultBase()
    {
        return AntSpecies.DEFAULT_BASE.push(P_CONSTRUCTIVEINDIVIDUAL);
    }

    @Override
    public Object clone()
    {
        ConstructiveIndividual myobj = (ConstructiveIndividual) (super.clone());

        // must clone the path
        myobj.components = new HashSet<Integer>(components);
        
        assert(repOK());
        return myobj;
    } 
    
    public Set<Integer> getComponents()
    {
        assert(repOK());
        return new HashSet<Integer>(components);
    }
    
    public void setComponents(final Collection<Integer> newComponents)
    {
        assert(newComponents != null);
        this.components = new HashSet<Integer>(newComponents);
        assert(repOK());
    }
    
    @Override
    public long size()
    {
        assert(repOK());
        return components.size();
    }

    @Override
    public boolean equals(final Object ind)
    {
        if (ind == this)
            return true;
        if (!(ind instanceof ConstructiveIndividual))
            return false;
        return components.equals(((ConstructiveIndividual)ind).components);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.components != null ? this.components.hashCode() : 0);
        return hash;
    }
    
    public final boolean repOK()
    {
        return P_CONSTRUCTIVEINDIVIDUAL != null
                && !P_CONSTRUCTIVEINDIVIDUAL.isEmpty()
                && components != null
                && !Misc.containsNulls(components);
    }
}
