/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Misc;
import ec.util.Parameter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents an <code>Individual</code> that can be incrementally constructed from a number of smaller components.
 *
 * This type is used by ECJ's combinatorial optimization to represent and construct solutions to problems like
 * TSP or Knapsack, where partial solutions are usually built one component at a time.
 *
 * @author Eric O. Scott
 */
public class ConstructiveIndividual<T extends Component> extends Individual implements Iterable<T>
    {
    private static final long serialVersionUID = 1;
    
    public static final String P_DEFAULTBASE = "constr-ind";
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
        return new Parameter(P_DEFAULTBASE);
        }

    @Override
    public Object clone()
        {
        ConstructiveIndividual<T> myobj = (ConstructiveIndividual<T>) (super.clone());

        myobj.components = new ArrayList<T>(components);
        myobj.componentsSet = new HashSet<T>(componentsSet);
        
        assert(repOK());
        return myobj;
        } 
    
    public List<T> getComponents()
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
    
    public void add(final EvolutionState state, final T component)
        {
        assert(component != null);
        components.add(component);
        componentsSet.add(component);
        assert(repOK());
        }
    
    public boolean contains(final T component)
        {
        return componentsSet.contains(component);
        }

    @Override
    public Iterator<T> iterator()
        {
        return components.iterator();
        }
    
    public boolean isEmpty()
        {
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
        final ConstructiveIndividual<T> ref = (ConstructiveIndividual<T>)ind;
        return components.equals(ref.components);
        }

    @Override
    public int hashCode()
        {
        int hash = 5;
        hash = 41 * hash + (this.components != null ? this.components.hashCode() : 0);
        return hash;
        }
    
    @Override
    public String toString()
        {
        return components.toString();
        }
    
    @Override
    public void writeGenotype(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(components.size());
        for (final Component c : components)
            c.writeComponent(state, dataOutput);
        assert(repOK());
        }
    
    @Override
    public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        // Obliterate our current contents
        components = new ArrayList<T>();
        componentsSet = new HashSet<T>();
        
        // Read in the new contents
        final int numComponents = dataInput.readInt();
        final ConstructiveProblemForm<T> problem = (ConstructiveProblemForm<T>) state.evaluator.p_problem;
        final T p_component = problem.getArbitraryComponent(state, 0);
        
        for (int i = 0; i < numComponents; i++)
            add(state, (T) p_component.readComponent(state, dataInput));
        
        assert(repOK());
        }
    
    public boolean repOK()
        {
        return P_DEFAULTBASE != null
            && !P_DEFAULTBASE.isEmpty()
            && components != null
            && !Misc.containsNulls(components)
            && componentsSet.size() == new HashSet<T>(components).size()
            && componentsSet.equals(new HashSet<T>(components));
        }
    }
