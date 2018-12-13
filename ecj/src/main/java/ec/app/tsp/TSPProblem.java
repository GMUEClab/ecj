/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.tsp.TSPGraph.TSPComponent;
import ec.co.Component;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private boolean allowCycles;
    private TSPGraph graph;
    
    public TSPComponent getComponent(final int from, final int to) {
        return graph.getEdge(from, to);
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
        if (file == null)
            state.output.fatal(String.format("%s: Unable to read file path '%s'.", this.getClass().getSimpleName(), base.push(P_FILE)), base.push(P_FILE));
        try
            {
            assert(file != null);
            graph = new TSPGraph(file);
            }
        catch (final Exception e)
            {
            state.output.fatal(String.format("%s: Unable to load TSP instance from file '%s': %s", this.getClass().getSimpleName(), state.parameters.getString(base.push(P_FILE), null), e), base.push(P_FILE));
            }
        assert(repOK());
    }

    public boolean isViolated(final ConstructiveIndividual partialSolution, final Component component) {
        assert(partialSolution != null);
        if (!(component instanceof TSPComponent))
            throw new IllegalArgumentException(String.format("%s: attempted to verify a component of type %s, but must be %s.", this.getClass().getSimpleName(), component.getClass().getSimpleName(), TSPComponent.class.getSimpleName()));
        final TSPComponent edge = (TSPComponent) component;
        boolean connected = false;
        for (final Object c : partialSolution)
        {
            assert(c instanceof TSPComponent);
            final TSPComponent solEdge = (TSPComponent) c;
            
            if (edge.from() == solEdge.from() || edge.from() == solEdge.to())
                connected = false; // We are starting from a node that is part of the tour (good!)
            if (edge.from() == solEdge.from() || edge.to() == solEdge.to())
                return true; // We're trying to move to a node that is already in the tour (bad!)
        }
        return connected;
    }

    @Override
    public List<Component> getAllowedComponents(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
        
        if (!(partialSolution instanceof TSPIndividual))
            throw new IllegalStateException(String.format("%s: received an individual of type %s, but must be %s.", this.getClass().getSimpleName(), partialSolution.getClass().getSimpleName(), TSPIndividual.class.getSimpleName()));
        final TSPIndividual tspSol = (TSPIndividual) partialSolution;
        
        final List<Component> allowedComponents = new ArrayList<Component>();
        
        // If the solution is empty, then any component is allowed
        if (partialSolution.isEmpty())
            allowedComponents.addAll(graph.getAllEdges());
        else
        { // Otherwise, only edges extending from either end of the paht are allowed
            // Focus on the most recently added node in the tour
            final TSPComponent lastEdge = (TSPComponent) partialSolution.get((int) partialSolution.size() - 1);

            // Loop through every edge eminating from that node
            for (int to = 0; to < graph.numNodes(); to++)
            {
                if (allowCycles || !tspSol.visited(to))
                    allowedComponents.add(graph.getEdge(lastEdge.to(), to));
            }
        }
        assert(repOK());
        assert(allowedComponents.size() <= numComponents());
        return allowedComponents;
    }

    /** Check whether a solution forms a valid tour of all the nodes. */
    @Override
    public boolean isCompleteSolution(final ConstructiveIndividual solution) {
        if (solution.size() != graph.numNodes())
            return false;
        final Set<Integer> visited = nodesVisited(solution);
        return visited.equals(graph.getAllEdges());
    }
        
    private Set<Integer> nodesVisited(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
        final Set<Integer> nodesVisited = new HashSet<Integer>();
        for (final Object c : partialSolution) {
            final TSPComponent edge = (TSPComponent) c;
            if (!allowCycles && (nodesVisited.contains(edge.to()) || nodesVisited.contains(edge.from())))
                throw new IllegalStateException(String.format("%s: '%s' is set to false, but an individual containing cycles was encountered.  Is your construction heuristic configured to avoid cycles?", this.getClass().getSimpleName(), P_ALLOW_CYCLES));
            nodesVisited.add(edge.to());
            nodesVisited.add(edge.from());
        }
        return nodesVisited;
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
            final ConstructiveIndividual<Component> iind = (ConstructiveIndividual) ind;
            if (!isCompleteSolution(iind))
                state.output.fatal(String.format("%s: attempted to evaluate an incomplete solution.", this.getClass().getSimpleName()));
            assert(iind.size() == graph.numNodes());
            double cost = 0.0;
            for (final Component c : iind.getComponents())
                cost += c.cost();
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
    
    public final boolean repOK()
    {
        return P_FILE != null
                && !P_FILE.isEmpty()
                && P_ALLOW_CYCLES != null
                && !P_ALLOW_CYCLES.isEmpty()
                && graph != null;
    }
}
