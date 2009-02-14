package ec.app.moobenchmarks;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
import ec.multiobjective.*;

/**
 * @author  Gabriel Balan
 * 
 * Sphere Model (SPH-m)
 * 
 * Schaffer, J. D. (1985).Multiple objective optimization with vector evaluated genetic
 * algorithms. In J. J. Grefenstette (Ed.), Proceedings of an International Conference
 * on Genetic Algorithms and Their Applications, Pittsburgh, PA, pp. 93-100.
 * sponsored by Texas Instruments and U.S. Navy Center for Applied Research in
 * Artificial Intelligence (NCARAI).
 * 
 * Laumanns, M., G. Rudolph, and H.-P. Schwefel (2001, June). Mutation control
 * and convergence in evolutionarymulti-objective optimization. In Proceedings of
 * the 7th International Mendel Conference on Soft Computing (MENDEL 2001),
 * Brno, Czech Republic.
 */
public class SPHERE extends Problem implements SimpleProblemForm
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
		 * pop.subpop.0.species.min-gene = -1000
		 * pop.subpop.0.species.max-gene = 1000
		 * pop.subpop.0.species.genome-size = numDecisionVars
		 */
		state.parameters.set(new Parameter("pop.subpop.0.species"),"ec.vector.FloatVectorSpecies");
		state.parameters.set(new Parameter("pop.subpop.0.species.ind"),"ec.vector.FloatVectorIndividual");
		state.parameters.set(new Parameter("pop.subpop.0.species.genome-size"),""+numDecisionVars);
		state.parameters.set(new Parameter("pop.subpop.0.species.min-gene"),"-1000");
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"),"1000");

	}
	
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
	{
		if(ind.evaluated)
			return;
		float[] genome = ((FloatVectorIndividual)ind).genome;
		float[] fitnesses = ((MultiObjectiveFitness)ind.fitness).multifitness;
		int numObjectives = fitnesses.length;

		for(int j=0; j<numObjectives; ++j)
		{
			float sum = (genome[j]-1)*(genome[j]-1);
			for(int i=0; i<numDecisionVars; ++i)
				if (i!=j)
					sum += genome[i]*genome[i];
			fitnesses[j] = sum;
		}			
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
