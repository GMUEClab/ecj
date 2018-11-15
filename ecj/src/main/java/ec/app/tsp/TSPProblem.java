/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.tsp.TSPGraph.TSPEdge;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Traveling Salesmen Problem loaded from a file.
 * 
 * The format used for the file is similar to the TSPLIB format
 * (https://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/tsp95.pdf),
 * though we don't support all of TSPLIB's features.
 * 
 * @author Eric O. Scott
 */
public class TSPProblem extends Problem implements SimpleProblemForm, ConstructiveProblemForm {
    public final static String P_FILE = "file";
    public final static String P_ALLOW_CYCLES = "allow-cycles";
    public final static String P_DIRECTED = "directed";

    private boolean allowCycles;
    private TSPGraph graph;
    
    @Override
    public TSPEdge getComponent(final int component) {
        return graph.getEdge(component);
    }
    
    public int getComponentId(final int from, final int to)
    {
        return graph.edgeID(from, to);
    }
    
    public int numNodes()
    {
        return graph.numNodes();
    }
    
    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        assert(state != null);
        assert(base != null);
        final File file = state.parameters.getFile(base.push(P_FILE), null);
        allowCycles = state.parameters.getBoolean(base.push(P_ALLOW_CYCLES), null, false);
        final boolean directed = state.parameters.getBoolean(base.push(P_DIRECTED), null, false);
        if (file == null)
            state.output.fatal(String.format("%s: Unable to read file path '%s'.", this.getClass().getSimpleName(), base.push(P_FILE)), base.push(P_FILE));
        try
            {
            assert(file != null);
            graph = new TSPGraph(file, directed);
            }
        catch (final Exception e)
            {
            state.output.fatal(String.format("%s: Unable to load TSP instance from file '%s': %s", this.getClass().getSimpleName(), state.parameters.getString(base.push(P_FILE), null), e), base.push(P_FILE));
            }
        assert(repOK());
    }

    @Override
    public boolean isViolated(final ConstructiveIndividual partialSolution, final int component) {
        assert(partialSolution != null);
        final TSPEdge edge = graph.getEdge(component);
        boolean connected = false;
        for (final int c : partialSolution)
        {
            final TSPEdge solEdge = graph.getEdge(c);
            
            if (edge.from() == solEdge.from() || edge.from() == solEdge.to())
                connected = false; // We are starting from a node that is part of the tour (good!)
            if (edge.from() == solEdge.from() || edge.to() == solEdge.to())
                return true; // We're trying to move to a node that is already in the tour (bad!)
        }
        return connected;
    }

    @Override
    public Set<Integer> getAllowedComponents(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
            
        // If the solution is empty, then any component is allowed
        if (partialSolution.isEmpty())
            return componentSet();
        
        // Otherwise, only components in the current neighborhood are allowed
        final Set<Integer> allowedComponents = new HashSet<Integer>();
        // Focus on the most recently added node in the tour
        final int last = partialSolution.getLastAddedComponent();
        final int from = graph.getEdge(last).from();
        // Loop through every edge eminating from that node
        for (int to = 0; to < graph.numNodes(); to++)
        {
            if (from != to) // Disallow self-loops
            {
                final int reachable = graph.edgeID(from, to);
                if (!allowCycles && !partialSolution.contains(reachable)) // Allow the edge if it isn't already part of the tour
                    allowedComponents.add(reachable);
            }
        }
        assert(repOK());
        assert(allowedComponents.size() < numComponents());
        return allowedComponents;
    }

    /** Check whether a solution forms a valid tour of all the nodes. */
    @Override
    public boolean isCompleteSolution(final ConstructiveIndividual solution) {
        if (solution.size() != graph.numNodes())
            return false;
        final Set<Integer> visited = nodesVisited(solution);
        return visited.equals(componentSet());
    }
        
    private Set<Integer> nodesVisited(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
        final Set<Integer> nodesVisited = new HashSet<Integer>();
        for (final int c : partialSolution) {
            final TSPEdge edge = graph.getEdge(c);
            if (!allowCycles && (nodesVisited.contains(edge.to()) || nodesVisited.contains(edge.from())))
                throw new IllegalStateException(String.format("%s: '%s' is set to false, but an individual containing cycles was encountered.  Is your construction heuristic configured to avoid cycles?", this.getClass().getSimpleName(), P_ALLOW_CYCLES));
            nodesVisited.add(edge.to());
            nodesVisited.add(edge.from());
        }
        return nodesVisited;
    }

    /** Returns the distance between two nodes, rounded to the nearest integer. **/
    @Override
    public double cost(final int component)
    {
        assert(component >= 0);
        final TSPEdge edge = graph.getEdge(component);
        return edge.cost();
    }

    @Override
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
    {
        assert(state != null);
        assert(ind != null);
        assert(ind instanceof ConstructiveIndividual);
        assert(subpopulation >= 0);
        assert(subpopulation < state.population.subpops.size());
        assert(threadnum >= 0);
        
        if (!ind.evaluated)
            {
            final ConstructiveIndividual iind = (ConstructiveIndividual) ind;
            if (!isCompleteSolution(iind))
                state.output.fatal(String.format("%s: attempted to evaluate an incomplete solution.", this.getClass().getSimpleName()));
            assert(iind.size() == graph.numNodes());
            double cost = 0.0;
            for (final int c : iind.getComponents())
                cost += cost(c);
            assert(cost >= 0.0);
            assert(!Double.isNaN(cost));
            assert(!Double.isInfinite(cost));
            ((SimpleFitness)ind.fitness).setFitness(state, cost, false);
            ind.evaluated = true;
            }
    }

    @Override
    public int numComponents()
    {
        return graph.numEdges();
    }
    
    @Override
    public Set<Integer> componentSet()
    {
        return graph.edgeSet();
    }
    
    public final boolean repOK()
    {
        return P_FILE != null
                && !P_FILE.isEmpty()
                && P_ALLOW_CYCLES != null
                && !P_ALLOW_CYCLES.isEmpty()
                && P_DIRECTED != null
                && !P_DIRECTED.isEmpty()
                && graph != null;
    }
}
