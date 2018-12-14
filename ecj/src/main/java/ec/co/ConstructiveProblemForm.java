/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import java.util.List;

/**
 * Defines a constrained combinatorial optimization problem.
 * 
 * Users can implement this interface to define the component and 
 * solution constraints that make up a combinatorial optimization problems, 
 * such as knapsack problems, satisfiability problems, or vehicle routing problems.
 * 
 * @author Eric O. Scott
 */
public interface ConstructiveProblemForm {
    
    /**
     * @return The total number of components that exist in the problem.
     */
    public abstract int numComponents();
    
    /**
     * Determine whether a given specifies a complete solution to this problem.
     * 
     * @param solution A partial or complete solution to this ConstructiveProblemForm.
     * @return True iff the solution is complete (as opposed to partial).
     */
    public abstract boolean isCompleteSolution(final ConstructiveIndividual solution);
 
    /** 
     * @param partialSolution A partial solution to this ConstructiveProblemForm.
     * @param component A component that we may wish to add to the partial solution.
     * 
     * @return True iff the constraint allows component to be added to the given
     * partialSolution.
     */
    public abstract boolean isViolated(final ConstructiveIndividual partialSolution, final Component component);
    
    /**
     * Return the allowable "neighborhood" of components that 
     * 
     * @param partialSolution A collection of components that represent a
     * partial solution that we wish to add a component to.
     * 
     * @return The set of all components that can be added to the given partial 
     * solution without causing a constraint violation.
     */
    public abstract List<Component> getAllowedComponents(final ConstructiveIndividual partialSolution);
    
    /** Decode a String representation of a solution component.
     * 
     * @param s A String representing a component.
     * @return A component matching the provided String.
     */
    public abstract Component getComponentFromString(final String s);
}
