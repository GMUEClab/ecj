/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.coevolve2;

import ec.*;
import ec.app.ecsuite.*;
import ec.coevolve.*;
import ec.vector.DoubleVectorIndividual;
import ec.simple.SimpleFitness;

public class CoevolutionaryECSuite extends ECSuite implements GroupedProblemForm
    {
    public void preprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).setFitness( state, Integer.MIN_VALUE, false );
        }

    public void postprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                pop.subpops[i].individuals[j].evaluated = true;
        }

    public void evaluate(final EvolutionState state,
        final Individual[] ind,  // the individuals to evaluate together
        final boolean[] updateFitness,  // should this individuals' fitness be updated?
        final boolean countVictoriesOnly, // can be neglected in cooperative coevolution
        int[] subpops,
        final int threadnum)
        {
		if (ind.length == 0)
			state.output.fatal("Number of individuals provided to CoevolutionaryECSuite is 0!");
		if (ind.length == 1)
			state.output.warnOnce("Coevolution used, but number of individuals provided to CoevolutionaryECSuite is 1.");
		
		int size = 0;
		for(int i = 0 ; i < ind.length; i++)
			if ( ! ( ind[i] instanceof CoevolutionaryDoubleVectorIndividual ) )
				state.output.error( "Individual " + i + "in coevolution is not a CoevolutionaryDoubleVectorIndividual." );
			else
				{
				CoevolutionaryDoubleVectorIndividual coind = (CoevolutionaryDoubleVectorIndividual)(ind[i]);
				size += coind.genome.length;
				}
		state.output.exitIfErrors();
		
		// concatenate all the arrays
		double[] vals = new double[size];
		int pos = 0;
		for(int i = 0 ; i < ind.length; i++)
			{
			System.err.println("-->" + i);
			CoevolutionaryDoubleVectorIndividual coind = (CoevolutionaryDoubleVectorIndividual)(ind[i]);
			System.err.println(coind.genome.length);
			System.arraycopy(coind.genome, 0, vals, pos, coind.genome.length);
			pos += coind.genome.length;
			}

		double fit = (function(state, problemType, vals, threadnum));
		boolean isOptimal = isOptimal(problemType, fit);

		for(int i = 0 ; i < ind.length; i++)
			{
			CoevolutionaryDoubleVectorIndividual coind = (CoevolutionaryDoubleVectorIndividual)(ind[i]);
			if (updateFitness[i])
				{
				if ( fit > coind.fitness.fitness() )
					{
					((SimpleFitness)(coind.fitness)).setFitness( state, (float) fit, isOptimal );
					coind.context = new CoevolutionaryDoubleVectorIndividual[ind.length];
					for(int j = 0; j < ind.length; j++)
						{
						if (i != j)
						coind.context[j] = (CoevolutionaryDoubleVectorIndividual)(ind[j]);
						}
					}
				}
			}
        }
    }
