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
public class PheromoneMatrix
{
    private final int numNodes;
    private final double[][] matrix;
    
    public PheromoneMatrix(final int numNodes)
    {
        assert(numNodes > 0);
        this.numNodes = numNodes;
        
        // Initialize a square 2D array
        matrix = new double[numNodes][];
        for (int i = 0; i < numNodes; i++)
            matrix[i] = new double[numNodes];
        assert(repOK());
    }
    
    public void initZero()
    {
        for (int i = 0; i < numNodes; i++)
            for (int j = 0; j < numNodes; j++)
                matrix[i][j] = 0;
    }
    
    public int numNodes()
    {
        return numNodes;
    }
    
    public double get(final int from, final int to)
    {
        assert(from >= 0);
        assert(to >= 0);
        assert(from < numNodes);
        assert(to < numNodes);
        return matrix[from][to];
    }
    
    public void set(final int from, final int to, final double value)
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
    public PheromoneMatrix clone()
    {
        final PheromoneMatrix newMat = new PheromoneMatrix(numNodes);
        for (int i = 0; i < numNodes; i++)
            for (int j = 0; j < numNodes; j++)
                newMat.set(i, j, get(i, j));
        return newMat;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return numNodes > 0
                && matrix != null
                && matrix.length == numNodes
                && isSquare(matrix);
    }
    
    private static boolean isSquare(final double[][] matrix) {
        assert(matrix != null);
        for (int i = 0; i < matrix.length; i++)
            {
            if (matrix[i] == null || matrix[i].length != matrix.length)
                return false;
            }
        return true;
    }
}
