/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
import java.util.List;

/**
 * Defines a combinatorial optimization problem.
 *
 * Combinatorial optimization differs from other kinds of optimization and search in that algorithms for solving tasks
 * in this domain often proceed by building up partial solutions incrementally with the help of heuristic information.
 * So while most of ECJ's algorithms can be applied to a <code>Problem</code> that just provides a fitness function for
 * whole solutions, we need more than just a fitness function in order to apply ECJ's <code>CO</code> algorithms.
 *
 * <br/><br/>
 *
 * This interface in this package defines the additional information that users must
 * provide for combinatorial optimization problems.  The most important of these are
 *
 * <ul>
 *     <li>the component set, <code>getAllComponents()</code>, defining the pool of components that can be used to
 *     construct solutions in your problem domain,</li>
 *     <li>a neighborhood function, <code>getAllowedComponents()</code>, which defines the subset of components that
 *     can be added to a given partial solution without breaking it.</li>
 * </ul>
 *
 * The lists of <code>Component</code> objects returned by these methods come with their heuristic values pre-assigned.
 * A <code>ConstructiveProblemForm</code> thus also implicitly implements the heuristic evaluation function, which is
 * essential to many combinatorial optimization algorithms.
 * 
 * @author Eric O. Scott
 * @see ec.app.tsp
 * @see ec.app.knapsack
 */
public interface ConstructiveProblemForm<T extends Component> {
    
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
    public abstract boolean isCompleteSolution(final ConstructiveIndividual<T> solution);
 
    /** 
     * @param partialSolution A partial solution to this ConstructiveProblemForm.
     * @param component A component that we may wish to add to the partial solution.
     * 
     * @return True iff the constraint allows component to be added to the given
     * partialSolution.
     */
    public abstract boolean isViolated(final ConstructiveIndividual<T> partialSolution, final Component component);
    
    /**
     * Return the allowable "neighborhood" of components that 
     * 
     * @param partialSolution A collection of components that represent a
     * partial solution that we wish to add a component to.
     * 
     * @return The set of all components that can be added to the given partial 
     * solution without causing a constraint violation.
     */
    public abstract List<T> getAllowedComponents(final ConstructiveIndividual<T> partialSolution);
    
    /** @return all components that exist in the problem definition. */
    public abstract List<T> getAllComponents();
    
    /** Decode a String representation of a solution component.
     * 
     * @param s A String representing a component.
     * @return A component matching the provided String.
     */
    public abstract T getComponentFromString(final String s);
    
    
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
    public abstract T getArbitraryComponent(final EvolutionState state, final int thread);
    }
