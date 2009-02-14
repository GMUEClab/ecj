package ec.app.moobenchmarks;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
import ec.multiobjective.*;

/**
 * Kursawe Model.
 * 
 * <p>Deb, K., Pratap, A., Agrawal, S. and Meyarivan, T. (2000). A fast and elitist 
 * multiobjective genetic algorithm: NSGA-II. Technical Report No. 2000001. Kanpur: 
 * Indian Institute of Technology Kanpur, India. 
 * http://citeseer.nj.nec.com/article/deb00fast.html
 * 
 * <p>Shinya Watanabe, Tomoyuki Hiroyasu, Mitsunori Miki 
 * Neighborhood Cultivation Genetic Algorithm for Multi-objective Optimization Problems
 * IPSJ Transactions on Mathematical Modeling and Its Applications Abstract 
 * Vol.43 No.SIG10 - 022.
 * 
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>numvariables</tt><br>
 * <font size=-1>int (default=10)</font></td>
 * <td valign=top>The number of variables; genome-size is set to this value internally.</td></tr>
 * </table>
 * 
 * @author  Gabriel Balan
 * 
 */
public class KURSAWE extends Problem implements SimpleProblemForm
{
	public int numDecisionVars;
	public static final String P_NUMVARS = "numvariables"; 


    public void setup(final EvolutionState state, final Parameter base) 
	{
		super.setup(state, base);
		Parameter def = defaultBase();
		numDecisionVars = state.parameters.getIntWithDefault(base.push(P_NUMVARS), def.push(P_NUMVARS),0);
		if (numDecisionVars< 1)
			state.output.fatal("The number of variables must be an integer > 0", base.push(P_NUMVARS), def.push(P_NUMVARS));

		/**
		 * I want to force the following values:
		 * 
		 * pop.subpop.0.species = ec.vector.FloatVectorSpecies
		 * pop.subpop.0.species.ind = ec.vector.FloatVectorIndividual
		 * pop.subpop.0.species.min-gene = -5
		 * pop.subpop.0.species.max-gene = 5
		 * pop.subpop.0.species.genome-size = numDecisionVars
		 */
		state.parameters.set(new Parameter("pop.subpop.0.species"),"ec.vector.FloatVectorSpecies");
		state.parameters.set(new Parameter("pop.subpop.0.species.ind"),"ec.vector.FloatVectorIndividual");
		state.parameters.set(new Parameter("pop.subpop.0.species.genome-size"),""+numDecisionVars);
		state.parameters.set(new Parameter("pop.subpop.0.species.min-gene"),"-5");
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"),"5");
		state.parameters.set(new Parameter("pop.subpop.0.species.fitness.numobjectives"),"2");
	}
	
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
	{
		if(ind.evaluated)
			return;
		float[] genome = ((FloatVectorIndividual)ind).genome;
		float[] fitnesses = ((MultiObjectiveFitness)ind.fitness).multifitness;
		double sum=0;
		double nextSquared, thisSquared;
		thisSquared = genome[0]*genome[0];
		for(int i = 0; i< numDecisionVars-1; ++i)
		{
			nextSquared = genome[i+1]*genome[i+1];
			sum += Math.exp(-0.2*Math.sqrt(thisSquared + nextSquared));
			thisSquared = nextSquared;
		}
		fitnesses[0] = (float)(-10*sum);
		sum= 0;
		for(int i = 0; i< numDecisionVars; ++i)
		{
			double sin_xi = Math.sin(genome[i]);
			sum += Math.pow(genome[i], .8)+5*sin_xi*sin_xi*sin_xi;
		}
		fitnesses[1] = (float)sum;
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
