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
public class TSPProblem extends Problem implements SimpleProblemForm, ConstructiveProblemForm<TSPComponent> {
    private static final long serialVersionUID = 1;

    public final static String P_FILE = "file";
    public final static String P_ALLOW_CYCLES = "allow-cycles";

    private boolean allowCycles;
    private TSPGraph graph;
    
    public TSPComponent getComponent(final int from, final int to) {
        return graph.getEdge(from, to);
        }
    
    public TSPComponent getComponentFromString(final String s)
        {
        assert(s != null);
        assert(!s.isEmpty());
        final String error = String.format("%s: failed to decode string representation of %s.  It must have the form '%s[from=M, to=N]' where M, N are integers, but was '%s'.", this.getClass().getSimpleName(), TSPComponent.class.getSimpleName(), TSPComponent.class.getSimpleName(), s);
        
        String[] splits = s.split("\\["); // "TSPComponent" "from=M, to=N]"
        if (splits.length != 2)
            throw new IllegalArgumentException(error);
        final String name = splits[0].trim();
        if (!name.equals(TSPComponent.class.getSimpleName()))
            throw new IllegalArgumentException(error);
        
        splits = splits[1].split(","); // "from=M" "to=N]"
        if (splits.length != 2)
            throw new IllegalArgumentException(error);
        final String fromStr = splits[0]; // "from=M"
        final String toStr = splits[1].substring(0, splits[1].length() - 1); // "to=N"
        
        splits = fromStr.split("="); // "from" "M"
        if (!splits[0].trim().equals("from"))
            throw new IllegalArgumentException(error);
        final int from;
        try {
            from = Integer.parseInt(splits[1]);
            }
        catch (final NumberFormatException e)
            {
            throw new IllegalArgumentException(error);
            }
        
        splits = toStr.split("="); // "from" "M"
        if (!splits[0].trim().equals("to"))
            throw new IllegalArgumentException(error);
        final int to;
        try {
            to = Integer.parseInt(splits[1]);
            }
        catch (final NumberFormatException e)
            {
            throw new IllegalArgumentException(error);
            }
        
        assert(repOK());
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

    public boolean isViolated(final ConstructiveIndividual<TSPComponent> partialSolution, final Component component) {
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
    
    /**
     * Chooses a random edge in the TSP graph, disallowing self-loops.
     *
     * The intent here is that this gives us an efficient way to choose a component
     * to begin a solution with (as opposed to returning all possible components
     * so that some external method can choose one, has quadratic complexity
     * in the case of TSP).
     * 
     * @param state The state.  Its PRNG field (state.random) must exist.
     * @param thread The thread the caller is operating on.  If the caller is single-threaded, just set this to zero.
     * @return An component selected at random from all of the non-self-loop edges in the TSP graph.
     */
    @Override
    public TSPComponent getArbitraryComponent(final EvolutionState state, final int thread)
        {
        assert(state != null);
        assert(state.random != null);
        assert(thread >= 0);
        assert(graph.numNodes() > 1); // A degenerate graph would cause an infinite loop
        final int from = state.random[thread].nextInt(graph.numNodes());
        int to = from;
        while (to == from) // Keep sampling 'to' until we get something other than 'from'
            to = state.random[thread].nextInt(graph.numNodes());
        final TSPComponent result = graph.getEdge(from, to);
        assert(repOK());
        return result;
        }

    @Override
    public List<TSPComponent> getAllComponents() {
        return new ArrayList<TSPComponent>(graph.getAllEdges());
        }
    
    @Override
    public List<TSPComponent> getAllowedComponents(final ConstructiveIndividual<TSPComponent> partialSolution) {
        assert(partialSolution != null);
        
        if (!(partialSolution instanceof TSPIndividual))
            throw new IllegalStateException(String.format("%s: received an individual of type %s, but must be %s.", this.getClass().getSimpleName(), partialSolution.getClass().getSimpleName(), TSPIndividual.class.getSimpleName()));
        final TSPIndividual tspSol = (TSPIndividual) partialSolution;
        
        final List<TSPComponent> allowedComponents = new ArrayList<>();
        
        // If the solution is empty, then any non-self-loop component is allowed
        if (partialSolution.isEmpty())
            {
            for (final TSPComponent edge : graph.getAllEdges())
                if (edge.to() != edge.from()) // Disallow self-loops
                    allowedComponents.add(edge);
            }
        else
            { // Otherwise, only edges extending from either end of the path are allowed
            // Focus on the most recently added node in the tour
            final int mostRecentNode = tspSol.getLastNodeVisited();
            assert(mostRecentNode == tspSol.get((int) partialSolution.size() - 1).to());
            // Loop through every edge eminating from that node
            for (int to = 0; to < graph.numNodes(); to++)
                {
                if (mostRecentNode != to) // Disallow self-loops
                    if (allowCycles || !tspSol.visited(to))
                        allowedComponents.add(graph.getEdge(mostRecentNode, to));
                }
            }
        assert(repOK());
        assert(allowedComponents.size() <= numComponents());
        return allowedComponents;
        }

    /** Check whether a solution forms a valid tour of all the nodes. */
    @Override
    public boolean isCompleteSolution(final ConstructiveIndividual<TSPComponent> solution) {
        final Set<Integer> visited = nodesVisited(solution);
        return visited.size() == graph.numNodes()
            && visited.containsAll(graph.getNodes())
            && graph.getNodes().containsAll(visited);
        }
        
    private Set<Integer> nodesVisited(final ConstructiveIndividual<TSPComponent> partialSolution) {
        assert(partialSolution != null);
        final Set<Integer> nodesVisited = new HashSet<Integer>();
        for (final Object c : partialSolution) {
            final TSPComponent edge = (TSPComponent) c;
            if (!allowCycles && nodesVisited.contains(edge.to()))
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
            final double fitness = 1.0/totalDistance(state, ind);
            ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
            ind.evaluated = true;
            }
        }

    private double totalDistance(final EvolutionState state, final Individual ind)
        {
        final TSPIndividual tind = (TSPIndividual) ind;
        assert(ind != null);
        if (!isCompleteSolution(tind))
            state.output.fatal(String.format("%s: attempted to evaluate an incomplete solution.", this.getClass().getSimpleName()));
        assert(tind.size() == graph.numNodes() - 1);
        final List<TSPComponent> components = tind.getComponents();

        double distance = 0.0;
        for (final TSPComponent c : tind.getComponents())
            distance += c.distance();

        // The edge connecting the end to the beginning is implicit, so we add it here
        assert(components.get(components.size() - 1).to() != components.get(0).from());
        distance += graph.getEdge(components.get(components.size() - 1).to(), components.get(0).from()).distance();

        assert(distance >= 0.0);
        assert(!Double.isNaN(distance));
        assert(!Double.isInfinite(distance));
        return distance;
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
