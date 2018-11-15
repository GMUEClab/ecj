/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.tsp;

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
 *
 * @author Eric O. Scott
 */
public class TSPGraph {
    private enum TSPKeyword { TYPE, DIMENSION, EDGE_WEIGHT_TYPE, NODE_COORD_SECTION };
    private enum EdgeWeightType { EUC_2D, GEO, ATT }
    private EdgeWeightType edgeWeightType;
    private final boolean directed;
    private int dimension;
    private Map<Integer, double[]> nodes;
    
    public EdgeWeightType weightType()
    {
        return edgeWeightType;
    }
    
    public TSPGraph(final File file, final boolean directed) throws IOException
    {
        assert(file != null);
        this.directed = directed;
        final BufferedReader r = new BufferedReader(new FileReader(file));
        loadHeader(r);
        nodes = loadNodes(r);
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
    
    public int numNodes()
    {
        return nodes.size();
    }
     
    public int edgeID(final int from, final int to) {
        assert(from >= 0);
        assert(from < nodes.size());
        assert(to >= 0);
        assert(to < nodes.size());
        final int normalizedFrom = directed ? from: (int) Math.min(from, to);
        final int normalizedTo = directed ? to : (int) Math.max(from, to);
        final int id = nodes.size()*normalizedFrom + normalizedTo;
        assert(id >= 0);
        assert(edgeSet().contains(id));
        return id;
    }
    
    /** Interpret a component ID as an edge between two TSP nodes.
     * @return The index of the node the edge begins at.
     */
    private int directedFromNode(final int component)
    {
        assert(component >= 0);
        assert(repOK());
        return component/nodes.size();
    }

    /** Interpret a component ID as an edge between two TSP nodes.
     * @return The index of the node the edge ends at.
     */
    private int directedToNode(final int component)
    {
        assert(component >= 0);
        assert(repOK());
        return component % nodes.size();
    }
    
    private int fromNode(final int component)
    {
        assert(component >= 0);
        final int from = directedFromNode(component);
        final int to = directedToNode(component);
        assert(repOK());
        return directed ? from : (int) Math.min(from, to);
    }
    
    private int toNode(final int component)
    {
        assert(component >= 0);
        final int from = directedFromNode(component);
        final int to = directedToNode(component);
        assert(repOK());
        return directed ? to : (int) Math.max(from, to);
    }
    
    public int numEdges()
    {
        return (int) (directed ? Math.pow(nodes.size(), 2) // For a directed graph, the adajency matrix is full, with n^2 entries.
                : nodes.size()*(nodes.size() + 1)/2); // For an undirected graph, the adjacency matrix is upper triangular, with n(n+1)/2 individual entries.
    }
    
    public Set<Integer> edgeSet()
    {
        final Set<Integer> result = new HashSet<Integer>();
        if (directed) // For a directed graph, return ids for every element of the adjacency matrix
        {
            for (int i = 0; i < numEdges(); i++)
                result.add(i);
        }
        else // For an undirected graph, return only the ids of the upper-trangular portion of the adjacency matrix
        {
            int id = 0;
            // Fill each row of the upper-triangular matrix one by one
            for (int k = nodes.size(); k > 0; k--)
            {
                for (int i = 0; i < k; i++)
                    result.add(id++);
                id += 1 + nodes.size() - k;
            }
        }
        assert(repOK());
        assert(result.size() == numEdges());
        return result;
    }
    
    public TSPEdge getEdge(final int component)
    {
        return new TSPEdge(component);
    }
    
    public class TSPEdge
    {
        private int fromNode;
        private int toNode;
        private double[] from;
        private double[] to;
        
        public int from()
        {
            assert(repOK());
            return fromNode;
        }
        
        public int to()
        {
            assert(repOK());
            return toNode;
        }
        
        public TSPEdge(final int component)
        {
            assert(component >= 0);
            assert(edgeSet().contains(component));
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
                    && !(!directed && fromNode > toNode);
        }
        
        public double cost()
        {
            switch (weightType())
            {
            default:
            case EUC_2D:
                return euclideanDistance();
            case ATT:
                return attDistance();
            case GEO:
                return geoDistance();
            }
        }
        
        /** Euclidean distance, rounded to the nearest integer. */
        private double euclideanDistance()
        {
            return Math.rint(Math.sqrt(Math.pow(from[0] - to[0], 2) + Math.pow(from[1] - to[1], 2)));
        }

        /** A "pseudo-Euclidean" distance, used in some TSPLIB instances. */
        private double attDistance()
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
        private double geoDistance()
        {
            final double rrr = 6378.388;
            final double q1 = Math.cos(longitude(from) - longitude(to));
            final double q2 = Math.cos(latitude(from) - latitude(to));
            final double q3 = Math.cos(latitude(from) + latitude(to));
            return (int) (rrr * Math.acos(0.5 * ((1.0 + q1)*q2 - (1.0 - q1)*q3) ) + 1.0);
        }
    }

    /** Latitude is encoded in DDD.MM format by the first element of a point,
     * where DDD is degrees and MM is minutes.
     */
    private static double latitude(final double[] p)
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
    private static double longitude(final double[] p)
    {
        assert(p != null);
        assert(p.length == 2);
        final double deg = Math.rint(p[1]);
        final double min = p[1] - deg;
        return Math.PI * (deg + 5.0 * min / 3.0) / 180.0;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return nodes != null
                && nodes.size() == dimension
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
