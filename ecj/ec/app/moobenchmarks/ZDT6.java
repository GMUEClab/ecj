package ec.app.moobenchmarks;


import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
import ec.multiobjective.*;

/**
 * 
 * ZDT-T6 includes two difficulties caused by the nonuniformity 
 * of the search space: 
 * <ol>
 * <li> the pareto-optimal solutions are nonuniformly distributed along 
 * the global pareto front (the front is biased for solutions for wihich
 * f1(X) is near one)
 * <li> the density of solutions is lowest near the Pareto optimal front 
 * and highest away from the front.
 * </ol>
 * 
 * <p>The global Pareto-optimal front is formed with g(X) = 1 and it
 * is non-convex.
 * 
 * <p>Zitzler, E., Deb, K., and Thiele, L., 2000, Comparison of Multiobjective Evolutionary
 * Algorithms: Empirical Results, Evolutionary Computation, Vol. 8, No. 2, pp173-195.
 * 
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>numvariables</tt><br>
 * <font size=-1>int (default=10)</font></td>
 * <td valign=top>The number of variables; genome-size is set to this value internally.</td></tr>
 * </table>
 * 
 * @author  Gabriel Balan  
 */
public class ZDT6 extends Problem implements SimpleProblemForm
{
	public int numDecisionVars;
	public static final String P_NUMVARS = "numvariables"; 


    public void setup(final EvolutionState state, final Parameter base) 
	{
		super.setup(state, base);
		Parameter def = defaultBase();
		numDecisionVars = state.parameters.getIntWithDefault(base.push(P_NUMVARS), def.push(P_NUMVARS),10);
		if (numDecisionVars< 2)
			state.output.fatal("The number of variables must be an integer > 1", base.push(P_NUMVARS), def.push(P_NUMVARS));


		/**
		 * I want to force the following values:
		 * 
		 * pop.subpop.0.species = ec.vector.FloatVectorSpecies
		 * pop.subpop.0.species.ind = ec.vector.FloatVectorIndividual
		 * pop.subpop.0.species.min-gene = 0
		 * pop.subpop.0.species.max-gene = 1
		 * pop.subpop.0.species.genome-size = numDecisionVars //could be anything, should be 10
		 */
		state.parameters.set(new Parameter("pop.subpop.0.species"),"ec.vector.FloatVectorSpecies");
		state.parameters.set(new Parameter("pop.subpop.0.species.ind"),"ec.vector.FloatVectorIndividual");
		state.parameters.set(new Parameter("pop.subpop.0.species.genome-size"),""+numDecisionVars);
		state.parameters.set(new Parameter("pop.subpop.0.species.min-gene"),"0");
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"),"1");
		state.parameters.set(new Parameter("pop.subpop.0.species.fitness.numobjectives"),"2");
	}
	private static final double FOUR_PI = Math.PI*4;
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
	{
		if(ind.evaluated)
			return;
		float[] genome = ((FloatVectorIndividual)ind).genome;
		float[] fitnesses = ((MultiObjectiveFitness)ind.fitness).multifitness;
		float f = 1- (float)(Math.exp(-4*genome[0])*Math.pow(Math.sin(6*Math.PI*genome[0]),6));
		fitnesses[0] = f;
		float sum = 0;
		for(int i = 1; i< numDecisionVars; ++i)
			sum += genome[i];
		double g = 1+9*Math.pow(sum/(numDecisionVars-1), 0.25);
		double h = 1-Math.sqrt(f/g);
		fitnesses[1] = (float)(g*h);
		ind.evaluated = true;
	}
    public void describe(final Individual ind, 
            final EvolutionState state, 
            final int subpopulation,
            final int threadnum,
            final int log,
            final int verbosity)
    {}	
}
