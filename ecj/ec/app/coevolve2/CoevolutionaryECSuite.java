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
import java.util.*;

public class CoevolutionaryECSuite extends ECSuite implements GroupedProblemForm
    {
    public void preprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).trials = new ArrayList();
        }

    public void postprocessPopulation(final EvolutionState state, Population pop, boolean countVictoriesOnly)
        {
        for( int i = 0 ; i < pop.subpops.length ; i++ )
            for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ )
                {
                SimpleFitness fit = ((SimpleFitness)(pop.subpops[i].individuals[j].fitness));
                                
                // we take the max over the trials
                double max = Double.MIN_VALUE;
                int len = fit.trials.size();
                for(int l = 0; l < len; l++)
                    max = Math.max(((Double)(fit.trials.get(l))).doubleValue(), max);  // it'll be the first one, but whatever
                                        
                fit.setFitness(state, (float)(max), isOptimal(problemType, (float)max));
                pop.subpops[i].individuals[j].evaluated = true;
                fit.trials = null;  // let GC
                }
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
            CoevolutionaryDoubleVectorIndividual coind = (CoevolutionaryDoubleVectorIndividual)(ind[i]);
            System.arraycopy(coind.genome, 0, vals, pos, coind.genome.length);
            pos += coind.genome.length;
            }

        double trial = (function(state, problemType, vals, threadnum));

        // update individuals to reflect the trial
        for(int i = 0 ; i < ind.length; i++)
            {
            CoevolutionaryDoubleVectorIndividual coind = (CoevolutionaryDoubleVectorIndividual)(ind[i]);
            if (updateFitness[i])
                {
                // Update the context if this is the best trial
                // it'd be nice and O(1) to just maintain the best trial as trial #0, but it won't work
                // properly if we're merging Individuals when doing distributed evaluations, so we need
                // to find the max in O(n) fashion by hunting through the whole collection, sorry.  This needs
                // to be rethought a bit.
                double max = Double.MIN_VALUE;
                int len = coind.fitness.trials.size();
                for(int l = 0; l < len; l++)
                    max = Math.max(((Double)(coind.fitness.trials.get(l))).doubleValue(), max);  // it'll be the first one, but whatever

                if (max < trial)  // we've found a new best trial, update the context
                    {
                    // update the context
                    coind.context = new CoevolutionaryDoubleVectorIndividual[ind.length];
                    for(int j = 0; j < ind.length; j++)
                        {
                        if (i != j)
                            coind.context[j] = (CoevolutionaryDoubleVectorIndividual)(ind[j]);
                        }
                    }
                // add the trial to the individual's list of trials
                coind.fitness.trials.add(new Double(trial));
                                                                        
                // finally set the fitness for good measure
                ((SimpleFitness)(coind.fitness)).setFitness(state, (float)trial, false);
                }
            }
        }
    }
