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
import ec.util.*;
import java.util.*;

public class CoevolutionaryECSuite extends ECSuite implements GroupedProblemForm
    {
    public static final String P_SHOULD_SET_CONTEXT = "set-context";
    boolean shouldSetContext;
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state, base);
                
        // load whether we should set context or not
        shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT), null, true);
        }
        
    public void preprocessPopulation(final EvolutionState state, Population pop, boolean[] prepareForAssessment, boolean countVictoriesOnly)
        {
        for(int i = 0; i < pop.subpops.size(); i++ )
            if (prepareForAssessment[i])
                for(int j = 0; j < pop.subpops.get(i).individuals.size() ; j++ )
                    ((SimpleFitness)(pop.subpops.get(i).individuals.get(j).fitness)).trials = new ArrayList();
        }

    public int postprocessPopulation(final EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly)
        {
        int total = 0;
        for(int i = 0; i < pop.subpops.size(); i++ )
            if (assessFitness[i])
                for(int j = 0; j < pop.subpops.get(i).individuals.size() ; j++ )
                    {
                    SimpleFitness fit = ((SimpleFitness)(pop.subpops.get(i).individuals.get(j).fitness));
                                                                        
                    // we take the max over the trials
                    double max = Double.NEGATIVE_INFINITY;
                    int len = fit.trials.size();
                    for(int l = 0; l < len; l++)
                        max = Math.max(((Double)(fit.trials.get(l))).doubleValue(), max);  // it'll be the first one, but whatever
                                        
                    fit.setFitness(state, max, isOptimal(problemType, max));
                    pop.subpops.get(i).individuals.get(j).evaluated = true;
                    total++;
                    }
        return total;
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
            if ( ! ( ind[i] instanceof DoubleVectorIndividual ) )
                state.output.error( "Individual " + i + "in coevolution is not a DoubleVectorIndividual." );
            else
                {
                DoubleVectorIndividual coind = (DoubleVectorIndividual)(ind[i]);
                size += coind.genome.length;
                }
        state.output.exitIfErrors();
                
        // concatenate all the arrays
        double[] vals = new double[size];
        int pos = 0;
        for(int i = 0 ; i < ind.length; i++)
            {
            DoubleVectorIndividual coind = (DoubleVectorIndividual)(ind[i]);
            System.arraycopy(coind.genome, 0, vals, pos, coind.genome.length);
            pos += coind.genome.length;
            }

        double trial = (function(state, problemType, vals, threadnum));

        // update individuals to reflect the trial
        for(int i = 0 ; i < ind.length; i++)
            {
            DoubleVectorIndividual coind = (DoubleVectorIndividual)(ind[i]);
            if (updateFitness[i])
                {
                // Update the context if this is the best trial.  We're going to assume that the best
                // trial is trial #0 so we don't have to search through them.
                int len = coind.fitness.trials.size();
                                
                if (len == 0)  // easy
                    {
                    if (shouldSetContext) coind.fitness.setContext(ind, i);
                    coind.fitness.trials.add(new Double(trial));
                    }
                else if (((Double)(coind.fitness.trials.get(0))).doubleValue() < trial)  // best trial is presently #0
                    {
                    if (shouldSetContext) coind.fitness.setContext(ind, i);
                    // put me at position 0
                    Double t = (Double)(coind.fitness.trials.get(0));
                    coind.fitness.trials.set(0, new Double(trial));  // put me at 0
                    coind.fitness.trials.add(t);  // move him to the end
                    }
                                                                        
                // finally set the fitness for good measure
                ((SimpleFitness)(coind.fitness)).setFitness(state, trial, false);
                }
            }
        }
    }
