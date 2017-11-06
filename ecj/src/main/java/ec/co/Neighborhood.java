/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import java.util.Set;

/**
 * Defines which components are in the reachable set during the construction of
 * a solution to a combinatorial optimization problem.
 * 
 * Users can implement this interface to define various problems,
 * such as knapsack problems, satisfiability problems, or traveling salesmen
 * problems on arbitrary graphs.
 * 
 * @author Eric O. Scott
 */
public interface Neighborhood {
}
