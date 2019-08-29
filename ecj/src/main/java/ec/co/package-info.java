/**
 * The ec.co package contains combinatorial optimization (CO) algorithms and the components necessary to build them.
 *
 * Combinatorial optimization differs from other kinds of optimization and search in that algorithms for solving tasks
 * in this domain often proceed by building up partial solutions (in our case, a list of <code>Component</code> objects)
 * incrementally with the help of heuristic information. So while most of ECJ's algorithms can be applied to a
 * <code>Problem</code> that just provides a fitness function for whole solutions, we need more than just a fitness
 * function in order to apply ECJ's <code>CO</code> algorithms.
 *
 * <br/><br/>
 *
 * The <code>ConstructiveProblemForm</code> interface in this package defines the additional information that users must
 * provide for combinatorial optimization problems.
 *
 * <br/><br/>
 *
 * Have a look at the the <code>ec.app.tsp</code> and <code>ec.app.knapsack</code> example apps to get a sense of how
 * these components can be used in practice.
 */
package ec.co;