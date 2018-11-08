/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    public final static String P_UNDIRECTED = "undirected";
            
    private int dimension;
    private enum TSPKeyword { TYPE, DIMENSION, EDGE_WEIGHT_TYPE, NODE_COORD_SECTION };
    private enum EdgeWeightType { EUC_2D, GEO, ATT }
    private EdgeWeightType edgeWeightType;
    private Map<Integer, double[]> nodes;
    private boolean allowCycles;
    private boolean undirected;
    
    @Override
    public TSPEdge getComponent(int component) {
        return new TSPEdge(component);
    }
    
    public int numNodes()
    {
        return nodes.size();
    }
    
    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        assert(state != null);
        assert(base != null);
        final File file = state.parameters.getFile(base.push(P_FILE), null);
        allowCycles = state.parameters.getBoolean(base.push(P_ALLOW_CYCLES), null, false);
        undirected = state.parameters.getBoolean(base.push(P_UNDIRECTED), null, false);
        if (file == null)
            state.output.fatal(String.format("%s: Unable to read file path '%s'.", this.getClass().getSimpleName(), base.push(P_FILE)), base.push(P_FILE));
        try
            {
            assert(file != null);
            final BufferedReader r = new BufferedReader(new FileReader(file));
            loadHeader(r);
            nodes = loadNodes(r);
            }
        catch (final Exception e)
            {
            state.output.fatal(String.format("%s: Unable to load TSP instance from file '%s': %s", this.getClass().getSimpleName(), state.parameters.getString(base.push(P_FILE), null), e), base.push(P_FILE));
            }
        assert(repOK());
    }
    
    /** Read the dimensionality of problem from a file in TSPLIB formate by
     * looking for the 'DIMENSION' attribute. */
    private void loadHeader(final BufferedReader tspReader) throws IOException
    {
        assert(tspReader != null);
        String line;
        while ( (line = tspReader.readLine()) != null && !line.trim().toUpperCase().equals(TSPKeyword.NODE_COORD_SECTION.toString()))
            {
            readLine(line);
            }
        if (dimension == 0)
            throw new IllegalStateException("No valid 'DIMENSION' attribute found in TSP file.  Are you sure this file is in TSPLIB format?");
    }
    
    private void readLine(final String line)
    {
        assert(line != null);
        final String[] keyValue = line.split(":");
        if (keyValue.length != 2)
            throw new IllegalStateException(String.format("%s: invalid TSPLIB specification '%s'.  Expected a key-value pair.", this.getClass().getSimpleName(), line));
        final String value = keyValue[1].trim();
        TSPKeyword keyword;
        try
            {
            keyword = TSPKeyword.valueOf(keyValue[0].trim().toUpperCase());
            }
        catch (final IllegalArgumentException e)
            {
            // We only recognize a subset of TSPLib keywords, and this isn't one of them.  Ignore it.
            return;
            }
        assert(!keyword.equals(TSPKeyword.NODE_COORD_SECTION));
        switch (keyword)
            {
            case DIMENSION:
                try
                    {
                    this.dimension = Integer.valueOf(value);
                    }
                catch (final NumberFormatException e)
                    {
                    throw new NumberFormatException(String.format("%s: invalid value '%s' found for %s attribute.  Integer expected.", this.getClass().getSimpleName(), value, TSPKeyword.DIMENSION));
                    }
                if (dimension <= 0)
                    throw new IllegalStateException(String.format("%s: invalid value '%d' found for %s attribute.  Must be positive", this.getClass().getSimpleName(), dimension, TSPKeyword.DIMENSION));
                break;
            case EDGE_WEIGHT_TYPE:
                try
                    {
                    this.edgeWeightType = EdgeWeightType.valueOf(value.toUpperCase());
                    }
                catch (final IllegalArgumentException e)
                    {
                    throw new IllegalStateException(String.format("%s: invalid value '%s' found for %s attribute.  Recognized values are %s.", this.getClass().getSimpleName(), value, TSPKeyword.EDGE_WEIGHT_TYPE, Arrays.asList(EdgeWeightType.values())));
                    }
                break;
            case TYPE:
                if (!value.trim().toUpperCase().equals("TSP"))
                    throw new IllegalStateException(String.format("%s: invalid problem type '%s' found for %s attribute.  Only 'TSP' is supported.", this.getClass().getSimpleName(), value, TSPKeyword.TYPE));
                break;
            default:
                throw new UnsupportedOperationException(String.format("%s: no logic has been implemented to handle the '%s' attribute.", this.getClass().getSimpleName(), keyword));
            }
    }
    
    /** Load a TSP problem from a file and store it as a Map from IDs to points. 
     * The reader should already be advanced one line beyond the occurrence of 
     * A_NODE_COORD_SECTION, so that the next line is the first node in the list. */
    private Map<Integer, double[]> loadNodes(final BufferedReader r) throws IOException
    {
        assert(r != null);
        final Map<Integer, double[]> nodes = new HashMap<Integer, double[]>();
        String line;
        while ( (line = r.readLine()) != null && !line.trim().equals("EOF") )
            {
            final String[] cols = line.split(" ");
            if (cols.length != 3)
                throw new IllegalStateException(String.format("%s: Node '%s' has %d columns, expected 3.", TSPProblem.class.getSimpleName(), line, cols.length));
            final int id = Integer.valueOf(cols[0].trim()) - 1; // TSPLIB IDs start from 1, but we want them to start from 0
            final double x = Double.valueOf(cols[1].trim());
            final double y = Double.valueOf(cols[2].trim());
            nodes.put(id, new double[] {x, y});
            }
        if (nodes.size() != dimension)
            throw new IllegalStateException(String.format("%s: TSP problem 'DIMENSION' is specified to be %d, but %d nodes were found.", TSPProblem.class.getSimpleName(), dimension, nodes.size()));
        return nodes;
    }

    @Override
    public boolean isViolated(final ConstructiveIndividual partialSolution, final int component) {
        assert(partialSolution != null);
        final TSPEdge edge = new TSPEdge(component);
        boolean connected = false;
        for (final int c : partialSolution)
        {
            final TSPEdge solEdge = new TSPEdge(c);
            
            if (edge.fromNode == solEdge.fromNode || edge.fromNode == solEdge.toNode)
                connected = false; // We are starting from a node that is part of the tour (good!)
            if (edge.toNode == solEdge.fromNode || edge.toNode == solEdge.toNode)
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
        final int from = fromNode(last);
        // Loop through every edge eminating from that node
        for (int to = 0; to < dimension; to++)
        {
            if (from != to) // Disallow self-loops
            {
                final int reachable = componentID(from, to);
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
        if (solution.size() != nodes.size())
            return false;
        final Set<Integer> visited = nodesVisited(solution);
        return visited.equals(componentSet());
    }
        
    private Set<Integer> nodesVisited(final ConstructiveIndividual partialSolution) {
        assert(partialSolution != null);
        final Set<Integer> nodesVisited = new HashSet<Integer>();
        for (final int c : partialSolution) {
            if (!allowCycles && (nodesVisited.contains(toNode(c)) || nodesVisited.contains(fromNode(c))))
                throw new IllegalStateException(String.format("%s: '%s' is set to false, but an individual containing cycles was encountered.  Is your construction heuristic configured to avoid cycles?", this.getClass().getSimpleName(), P_ALLOW_CYCLES));
            nodesVisited.add(toNode(c));
            nodesVisited.add(fromNode(c));
        }
        return nodesVisited;
    }

    /** Computes Euclidean distance between two nodes, rounded to the nearest integer. **/
    @Override
    public double cost(final int component)
    {
        assert(component >= 0);
        assert(component < Math.pow(nodes.size(), 2));
        final TSPEdge edge = new TSPEdge(component);
        switch (edgeWeightType)
            {
            default:
            case EUC_2D:
                return edge.euclideanDistance();
            case ATT:
                return edge.attDistance();
            case GEO:
                return edge.geoDistance();
            }
    }
    
    public class TSPEdge
    {
        private int fromNode;
        private int toNode;
        private double[] from;
        private double[] to;
        
        public TSPEdge(final int component)
        {
            assert(component >= 0);
            assert(component < numComponents());
            assert(componentSet().contains(component));
            fromNode = fromNode(component);
            toNode = toNode(component);
            from = nodes.get(fromNode);
            to = nodes.get(toNode);
            assert(repOK());
        }
        
        public final boolean repOK()
        {
            return fromNode >= 0
                    && fromNode < nodes.size()
                    && toNode >= 0
                    && toNode < nodes.size()
                    //&& fromNode != toNode
                    && from != null
                    && to != null
                    && from.length == 2
                    && to.length == 2
                    && !(undirected && fromNode > toNode);
        }
        
        /** Euclidean distance, rounded to the nearest integer. */
        public double euclideanDistance()
        {
            return Math.rint(Math.sqrt(Math.pow(from[0] - to[0], 2) + Math.pow(from[1] - to[1], 2)));
        }

        /** A "pseudo-Euclidean" distance, used in some TSPLIB instances. */
        public double attDistance()
        {
            final double xd = from[0] - to[0];
            final double yd = from[1] - to[1];
            final double rft = Math.sqrt((xd*xd + yd*yd) / 10.0);
            final double tft = Math.rint(rft);
            if (tft < rft)
                return tft + 1;
            else
                return tft;
        }

        /** A geographical distance based on latitude and longitude. */
        public double geoDistance()
        {
            final double rrr = 6378.388;
            final double q1 = Math.cos(longitude(from) - longitude(to));
            final double q2 = Math.cos(latitude(from) - latitude(to));
            final double q3 = Math.cos(latitude(from) + latitude(to));
            return (int) (rrr * Math.acos(0.5 * ((1.0 + q1)*q2 - (1.0 - q1)*q3) ) + 1.0);
        }
    }
     
    private int componentID(final int from, final int to) {
        assert(from >= 0);
        assert(from < nodes.size());
        assert(to >= 0);
        assert(to < nodes.size());
        final int normalizedFrom = undirected ? (int) Math.min(from, to) : from;
        final int normalizedTo = undirected ? (int) Math.max(from, to) : to;
        final int id = nodes.size()*normalizedFrom + normalizedTo;
        assert(id >= 0);
        assert(id < numComponents());
        assert(componentSet().contains(id));
        return id;
    }
    
    /** Interpret a component ID as an edge between two TSP nodes.
     * @return The index of the node the edge begins at.
     */
    public int directedFromNode(final int component)
    {
        assert(component >= 0);
        assert(component < numComponents());
        assert(repOK());
        return component/nodes.size();
    }

    /** Interpret a component ID as an edge between two TSP nodes.
     * @return The index of the node the edge ends at.
     */
    public int directedToNode(final int component)
    {
        assert(component >= 0);
        assert(component < numComponents());
        assert(repOK());
        return component % nodes.size();
    }
    
    public int fromNode(final int component)
    {
        assert(component >= 0);
        assert(component < numComponents());
        final int from = directedFromNode(component);
        final int to = directedToNode(component);
        assert(repOK());
        return undirected ? (int) Math.min(from, to) : from;
    }
    
    public int toNode(final int component)
    {
        assert(component >= 0);
        assert(component < numComponents());
        final int from = directedFromNode(component);
        final int to = directedToNode(component);
        assert(repOK());
        return undirected ? (int) Math.max(from, to) : to;
    }

    /** Latitude is encoded in DDD.MM format by the first element of a point,
     * where DDD is degrees and MM is minutes.
     */
    private double latitude(final double[] p)
    {
        assert(p != null);
        assert(p.length == 2);
        final double deg = Math.rint(p[0]);
        final double min = p[0] - deg;
        return Math.PI * (deg + 5.0 * min / 3.0) / 180.0;
    }

    /** Longitude is encoded in DDD.MM format by the first element of a point,
     * where DDD is degrees and MM is minutes.
     */
    private double longitude(final double[] p)
    {
        assert(p != null);
        assert(p.length == 2);
        final double deg = Math.rint(p[1]);
        final double min = p[1] - deg;
        return Math.PI * (deg + 5.0 * min / 3.0) / 180.0;
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
            assert(iind.size() == nodes.size());
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
        return (int) (undirected ? nodes.size()*(nodes.size() + 1)/2 // For an undirected graph, the adjacency matrix is upper triangular, with n(n+1)/2 individual entries.
                : Math.pow(nodes.size(), 2)); // For a directed graph, the adajency matrix is full, with n^2 entries.
    }
    
    @Override
    public Set<Integer> componentSet()
    {
        final Set<Integer> result = new HashSet<Integer>();
        if (undirected) // For an undirected graph, return only the ids of the lower-trangular portion of the adjacency matrix
        {
            while (result.size() < numComponents())
            {
                int id = 0;
                for (int k = 1; k <= nodes.size(); k++)
                {
                    for (int i = 0; i < k; i++)
                    {
                        result.add(id);
                        id++;
                    }
                    id += nodes.size() - k;
                }
            }
        }
        else // For a directed graph, return ids for every element of the adjacency matrix
        {
            for (int i = 0; i < numComponents(); i++)
                result.add(i);
        }
        assert(repOK());
        assert(result.size() < numComponents());
        return result;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return nodes != null
                && !containsNullKey(nodes)
                && !containsNullValue(nodes)
                && !pointsInvalid(nodes.values());
    }
    
    private static boolean containsNullKey(final Map map)
    {
        assert(map != null);
        for (Object o : map.keySet())
            if (o == null)
                return true;
        return false;
    }
    
    private static boolean containsNullValue(final Map map)
    {
        assert(map != null);
        for (Object o : map.values())
            if (o == null)
                return true;
        return false;
    }
    
    private static boolean pointsInvalid(final Collection<double[]> points) {
        assert(points != null);
        for (double[] a : points)
            if (a.length != 2 || Double.isNaN(a[0])|| Double.isInfinite(a[0]) || Double.isNaN(a[1]) || Double.isInfinite(a[1]))
                return true;
        return false;
    }
}
