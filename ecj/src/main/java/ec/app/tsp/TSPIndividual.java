/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.app.tsp.TSPGraph.TSPComponent;
import ec.co.ConstructiveIndividual;
import java.io.DataInput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A constructive individual for use with Traveling Salesmen Problems.
 * 
 * Adds support for maintaining a "tabu list" of nodes, which ACO algorithms 
 * can use to prevent cycles from forming in solutions.
 * 
 * @author Eric O. Scott
 */
public class TSPIndividual extends ConstructiveIndividual<TSPComponent> {
    private static final long serialVersionUID = 1;
    
    private Set<Integer> visitedNodes = new HashSet<Integer>();
    
    private int lastNodeVisited = -1;
    
    public boolean visited(final int node)
        {
        assert(node >= 0);
        assert(repOK());
        return visitedNodes.contains(node);
        }
    
    public int getLastNodeVisited()
        {
        assert(repOK());
        return lastNodeVisited;
        }
    
    @Override
    public void add(final EvolutionState state, final TSPComponent component)
        {
        super.add(state, component);
        assert(component != null);
        if (!(component instanceof TSPComponent))
            state.output.fatal(String.format("%s: attempted to add a component of type %s, but must be %s.", this.getClass().getSimpleName(), component.getClass().getSimpleName(), TSPComponent.class.getSimpleName()));
        final TSPComponent e = (TSPComponent) component;
        if (visitedNodes.contains(e.from()) && visitedNodes.contains(e.to()))
            state.output.fatal(String.format("%s: attempted to add an edge connected two nodes that have already been visited, but this is disallowed.", this.getClass().getSimpleName()));
        if (!visitedNodes.isEmpty() && (!visitedNodes.contains(e.from()) && !visitedNodes.contains(e.to())))
            state.output.fatal(String.format("%s: attempted to add an edge that is disconnected from the existing tour, but this is disallowed.", this.getClass().getSimpleName()));
        
        // FIXME Trying to infer the directionality of the edge being added.  But what do we do when the first edge of the tour is being added?
        // XXX These complications never end.  Perhaps we should be recording solutions as DIRECTED TOURS, even on an undirected graph?
        // But to do this with an abstract "component set" scheme, that means we'd be recording different pheremone concentrations
        // for each direction, which contradicts the classical formulation of ACO for TSP.
        
        // 
        assert(visitedNodes.isEmpty() || visitedNodes.contains(e.from()));
        lastNodeVisited = e.to();
        
        visitedNodes.add(e.from());
        visitedNodes.add(e.to());
        assert(repOK());
        }
    
    @Override
    public void readGenotype(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        visitedNodes = new HashSet<Integer>();
        super.readGenotype(state, dataInput);
        }
    
    @Override
    public Object clone()
        {
        TSPIndividual myobj = (TSPIndividual) (super.clone());
        
        myobj.visitedNodes = new HashSet<Integer>(visitedNodes);
        
        assert(repOK());
        return myobj;
        } 
    
    @Override
    public boolean repOK()
        {
        return super.repOK()
            && visitedNodes != null;
        }
    }
