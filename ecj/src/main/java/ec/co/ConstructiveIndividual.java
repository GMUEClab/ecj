/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
import ec.Individual;
import ec.co.ant.AntSpecies;
import ec.util.Misc;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Eric O. Scott
 */
public class ConstructiveIndividual<T extends Component> extends Individual implements Iterable<T>
{
    
    public static final String P_CONSTRUCTIVEINDIVIDUAL = "constr-ind";
    private List<T> components = new ArrayList<T>();
    /** A set representation of the components, to allow for quick "contains()" checking */
    private Set<T> componentsSet = new HashSet<T>();
    
    public T get(final int i)
    {
        return components.get(i);
    }
    
    @Override
    public Parameter defaultBase()
    {
        return AntSpecies.DEFAULT_BASE.push(P_CONSTRUCTIVEINDIVIDUAL);
    }

    @Override
    public Object clone()
    {
        ConstructiveIndividual myobj = (ConstructiveIndividual) (super.clone());

        myobj.components = new ArrayList<T>(components);
        myobj.componentsSet = new HashSet<T>(componentsSet);
        
        assert(repOK());
        return myobj;
    } 
    
    public Collection<T> getComponents()
    {
        assert(repOK());
        return new ArrayList<T>(components); // Defensive copy
    }
    
    public void setComponents(final EvolutionState state, final Collection<T> newComponents)
    {
        assert(newComponents != null);
        components = new ArrayList<T>(newComponents.size());
        componentsSet = new HashSet<T>();
        for (final T c : newComponents)
            add(state, c);
        assert(repOK());
    }
    
    public void add(final EvolutionState state, final T component) {
        assert(component != null);
        components.add(component);
        componentsSet.add(component);
        assert(repOK());
    }
    
    public boolean contains(final T component) {
        return componentsSet.contains(component);
    }

    @Override
    public Iterator<T> iterator() {
        return components.iterator();
    }
    
    public boolean isEmpty() {
        return this.components.isEmpty();
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
        final ConstructiveIndividual ref = (ConstructiveIndividual)ind;
        return components.equals(ref.components);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.components != null ? this.components.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return components.toString();
    }
    
    public boolean repOK()
    {
        return P_CONSTRUCTIVEINDIVIDUAL != null
                && !P_CONSTRUCTIVEINDIVIDUAL.isEmpty()
                && components != null
                && !Misc.containsNulls(components)
                && componentsSet.size() == components.size()
                && componentsSet.equals(new HashSet<T>(components));
    }
}
