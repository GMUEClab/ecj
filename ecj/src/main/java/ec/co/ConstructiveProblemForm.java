/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
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
    
    /** @return all components that exist in the problem definition. */
    public abstract List<Component> getAllComponents();
    
    /** Decode a String representation of a solution component.
     * 
     * @param s A String representing a component.
     * @return A component matching the provided String.
     */
    public abstract Component getComponentFromString(final String s);
    
    
    /**
     * Chooses an arbitrary component from the problem domain.
     *
     * The intent here is that this gives us an efficient way to choose a component
     * to begin a solution with (as opposed to returning all possible components
     * so that some external method can choose one, which may have nonlinear
     * complexity in some domains).
     * 
     * @param state The state (used, for example, to access the simulation's PRNGs)
     * @param thread The thread the caller is operating on.  If the caller is single-threaded, just set this to zero.
     * @return An component selected arbitrarily from the problem domain
     */
    public abstract Component getArbitraryComponent(final EvolutionState state, final int thread);
}
