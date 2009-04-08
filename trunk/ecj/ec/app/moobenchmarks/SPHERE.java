package ec.app.moobenchmarks;

import ec.*;
import ec.util.*;
import ec.vector.*;
import ec.simple.*;
import ec.multiobjective.*;

/**
 * 
 * Sphere Model (SPH-m) m=2,3
 * 
 * <p>ftp.tik.ee.ethz.ch/pub/people/zitzler/ZLT2001a.pdf
 * 
 * @author  Gabriel Balan
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
		 * pop.subpop.0.species = ec.vector.DoubleVectorSpecies
		 * pop.subpop.0.species.ind = ec.vector.DoubleVectorIndividual
		 * pop.subpop.0.species.min-gene = -1000
		 * pop.subpop.0.species.max-gene = 1000
		 * pop.subpop.0.species.genome-size = numDecisionVars
		 */
		state.parameters.set(new Parameter("pop.subpop.0.species"),"ec.vector.DoubleVectorSpecies");
		state.parameters.set(new Parameter("pop.subpop.0.species.ind"),"ec.vector.DoubleVectorIndividual");
		state.parameters.set(new Parameter("pop.subpop.0.species.genome-size"),""+numDecisionVars);
		state.parameters.set(new Parameter("pop.subpop.0.species.min-gene"),"-1000");
		state.parameters.set(new Parameter("pop.subpop.0.species.max-gene"),"1000");

	}
	
	public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
	{
		if(ind.evaluated)
			return;
		double[] genome = ((DoubleVectorIndividual)ind).genome;
		float[] fitnesses = ((MultiObjectiveFitness)ind.fitness).multifitness;
		int numObjectives = fitnesses.length;

		for(int j=0; j<numObjectives; ++j)
		{
			double sum = (genome[j]-1)*(genome[j]-1);
			for(int i=0; i<numDecisionVars; ++i)
				if (i!=j)
					sum += genome[i]*genome[i];
			fitnesses[j] = (float)sum;
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
