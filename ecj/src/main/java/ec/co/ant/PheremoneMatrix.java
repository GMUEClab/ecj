/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

/**
 *
 * @author Eric O. Scott
 */
public class PheremoneMatrix
{
    private final int numNodes;
    private final double[][] matrix;
    
    public PheremoneMatrix(int numNodes)
    {
        assert(numNodes > 0);
        this.numNodes = numNodes;
        
        // Initialize a square 2D array
        matrix = new double[numNodes][];
        for (int i = 0; i < numNodes; i++)
            matrix[i] = new double[numNodes];
    }
    
    public int numNodes()
    {
        return numNodes;
    }
    
    public double get(int from, int to)
    {
        assert(from >= 0);
        assert(to >= 0);
        assert(from < numNodes);
        assert(to < numNodes);
        return matrix[from][to];
    }
    
    public void set(int from, int to, double value)
    {
        assert(from >= 0);
        assert(to >= 0);
        assert(from < numNodes);
        assert(to < numNodes);
        matrix[from][to] = value;
        // XXX Assuming a symmetric pheremone distribution.  This would need to change if we wanted to handle, say, non-symmetric TSP problems.
        matrix[to][from] = value;
    }
    
    @Override
    public PheremoneMatrix clone()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
