/*
  Copyright 2017 by Sean Luke
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
    
    public final static String A_DIMENSION = "DIMENSION";
    public final static String A_EDGE_WEIGHT_TYPE = "EDGE_WEIGHT_TYPE";
    public final static String A_NODE_COORD_SECTION = "NODE_COORD_SECTION";
    
    private int dimension;
    private enum EdgeWeightType { EUC_2D, GEO, ATT }
    private EdgeWeightType edgeWeightType;
    private Map<Integer, double[]> nodes;
    
    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        assert(state != null);
        assert(base != null);
        final File file = state.parameters.getFile(base.push(P_FILE), null);
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
        while ( (line = tspReader.readLine()) != null && !line.equals(A_NODE_COORD_SECTION))
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
        final String key = keyValue[0].trim().toUpperCase();
        final String value = keyValue[1].trim();
        if (key.equals(A_DIMENSION))
            {
            try
                {
                this.dimension = Integer.valueOf(value);
                }
            catch (final NumberFormatException e)
                {
                throw new NumberFormatException(String.format("%s: invalid value '%s' found for %s attribute.  Integer expected.", this.getClass().getSimpleName(), value, A_DIMENSION));
                }
            if (dimension <= 0)
                throw new IllegalStateException(String.format("%s: invalid value '%d' found for %s attribute.  Must be positive", this.getClass().getSimpleName(), dimension, A_DIMENSION));
            }
        else if (key.equals(A_EDGE_WEIGHT_TYPE))
            {
            try
                {
                this.edgeWeightType = EdgeWeightType.valueOf(value.toUpperCase());
                }
            catch (final IllegalArgumentException e)
                {
                throw new NumberFormatException(String.format("%s: invalid value '%s' found for %s attribute.  Recognized values are %s.", this.getClass().getSimpleName(), value, A_EDGE_WEIGHT_TYPE, Arrays.asList(EdgeWeightType.values())));
                }
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

    /** Computes Euclidean distance between two nodes, rounded to the nearest integer. **/
    @Override
    public double desireability(final int from, final int to)
    {
        assert(from >= 0);
        assert(from < numComponents());
        assert(to >= 0);
        assert(to < numComponents());
        final double[] fp = nodes.get(from);
        final double[] tp = nodes.get(to);
        switch (edgeWeightType)
            {
            default:
            case EUC_2D:
                return euclideanDistance(fp, tp);
            case ATT:
                return attDistance(fp, tp);
            case GEO:
                return geoDistance(fp, tp);
            }
    }
    
    /** Euclidean distance, rounded to the nearest integer. */
    private double euclideanDistance(final double[] from, final double[] to)
    {
        assert(from != null);
        assert(to != null);
        assert(from.length == 2);
        assert(to.length == 2);
        return Math.rint(Math.sqrt(Math.pow(from[0] - to[0], 2) + Math.pow(from[1] - to[1], 2)));
    }
    
    /** A "pseudo-Euclidean" distance, used in some TSPLIB instances. */
    private double attDistance(final double[] from, final double[] to)
    {
        assert(from != null);
        assert(to != null);
        assert(from.length == 2);
        assert(to.length == 2);
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
    private double geoDistance(final double[] from, final double[] to)
    {
        assert(from != null);
        assert(to != null);
        assert(from.length == 2);
        assert(to.length == 2);
        final double rrr = 6378.388;
        final double q1 = Math.cos(longitude(from) - longitude(to));
        final double q2 = Math.cos(latitude(from) - latitude(to));
        final double q3 = Math.cos(latitude(from) + latitude(to));
        return (int) (rrr * Math.acos(0.5 * ((1.0 + q1)*q2 - (1.0 - q1)*q3) ) + 1.0);
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
            assert(iind.genomeLength() == numComponents());
            int currentNode = iind.genome[0];
            double cost = 0.0;
            for (int i = 1; i < iind.genomeLength(); i++)
                cost += desireability(currentNode, iind.genome[i]);
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
        return nodes.size();
    }
    
    @Override
    public Set<Integer> componentSet()
    {
        return new HashSet<Integer>(nodes.keySet()); // Defensive copy
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
