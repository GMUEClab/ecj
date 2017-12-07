/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.ConstructiveIndividual;
import ec.co.ConstructiveProblemForm;
import ec.util.Misc;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A construction rule that ignores pheromones and selects the best local move 
 * at each step.
 * 
 * @author Eric O. Scott
 */
public class GreedyConstructionRule implements ConstructionRule
{
    public final static String P_MINIMIZE = "minimize";
    public final static String P_CYCLE = "cycle";
    private boolean minimize;
    private boolean cycle;
    
    public boolean isMinimize()
    {
        return minimize;
    }
    
    public boolean isLoop()
    {
        return cycle;
    }
    
    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
        assert(state != null);
        assert(base != null);
        minimize = state.parameters.getBoolean(base.push(P_MINIMIZE), null, true);
        cycle = state.parameters.getBoolean(base.push(P_CYCLE), null, true);
        assert(repOK());
    }

    /** Constructs a solution by greedily adding the lowest-cost component at 
     * each step until a complete solution is formed.  The pheromone matrix
     * argument is ignored, and may be null.
     */
    @Override
    public ConstructiveIndividual constructSolution(final EvolutionState state, final ConstructiveIndividual ind, final int startNode, final PheromoneMatrix pheromones)
    {
        assert(state != null);
        assert(startNode >= 0);
        assert(state.evaluator.p_problem instanceof ConstructiveProblemForm);
        
        final ConstructiveProblemForm problem = (ConstructiveProblemForm) state.evaluator.p_problem;
        
        assert(problem != null);
        
        // Prepare data structures
        final List<Integer> path = new ArrayList<Integer>();
        final Collection<Integer> allowedMoves = problem.componentSet();
        
        // Constructively build a new individual
        int currentNode = startNode;
        path.add(currentNode);
        allowedMoves.remove(currentNode);
        while (!allowedMoves.isEmpty())
            {
            currentNode = bestMove(currentNode, problem, allowedMoves);
            path.add(currentNode);
            allowedMoves.remove(currentNode);
            }
        // If we mean to construct a cycle, add an edge back to origin
        if (cycle)
            path.add(path.get(0));
        
        ind.setPath(listToIntArray(path));
        assert(repOK());
        return ind;
    }
    
    private static int[] listToIntArray(final List<Integer> l)
    {
        assert(l != null);
        assert(!Misc.containsNulls(l));
        final int[] a = new int[l.size()];
        for (int i = 0; i < l.size(); i++)
            a[i] = l.get(i);
        return a;
    }
    
    /** Greedily select the next move. */
    private int bestMove(final int currentNode, final ConstructiveProblemForm problem, final Collection<Integer> allowedMoves)
    {
        assert(problem != null);
        assert(allowedMoves != null);
        assert(!allowedMoves.isEmpty());
        assert(!Misc.containsNulls(allowedMoves));
        
        double bestCost = minimize ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        int best = -1;
        for (final int move : allowedMoves)
            {
            final double cost = problem.cost(currentNode, move);
            if (minimize ? cost <= bestCost : cost >= bestCost)
                {
                bestCost = cost;
                best = move;
                }
            }
        assert(best >= 0);
        return best;
    }
    
    /** Representation invariant, used for verification.
     * 
     * @return true if the class is found to be in an erroneous state.
     */
    public final boolean repOK()
    {
        return P_MINIMIZE != null
                && !P_MINIMIZE.isEmpty();
    }
}
