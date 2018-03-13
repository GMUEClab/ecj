/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import java.util.Set;

/**
 * Defines a constrained combinatorial optimization problem.
 * 
 * Users can implement this interface to define the component, cost function, and 
 * solution constraints that make up a combinatorial optimization problems, 
 * such as knapsack problems, satisfiability problems, or vehicle routing problems.
 * 
 * @author Eric O. Scott
 */
public interface ConstructiveProblemForm {
    public abstract double cost(int component);
    public abstract int numComponents();
    public abstract Object getComponent(int component);
    public abstract Set<Integer> componentSet();
 
    /** 
     * @param partialSolution A partial solution to some ConstructiveProblemForm.
     * @param component A component that we may wish to add to the partial solution.
     * 
     * @return True iff the constraint allows component to be added to the given
     * partialSolution.
     */
    public boolean isViolated(final Set<Integer> partialSolution, final int component);
}
