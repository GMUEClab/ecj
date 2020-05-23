/*
  Copyright 2020 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.simple;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.Subpopulation;
import ec.coevolve.GroupedProblemForm;

/**
 * 
 * @author Eric O. Scott <escott8@gmu.edu>
 */
public class SimpleGroupedEvaluator extends SimpleEvaluator
    {
    private static final long serialVersionUID = 1;

    /** This protected helper function for evaluatePopulation evaluates a chunk
        of individuals in a subpopulation for a given thread.  We override it here 
        to modify evaluation so that it sends the entire chunk of individuals to a 
        GroupedProblemForm all at once (rather than one-by-one).  */
    @Override
    protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, Problem p)
        {
            ((ec.Problem)p).prepareToEvaluate(state,threadnum);
        
            if (!(p instanceof GroupedProblemForm))
            state.output.fatal("" + this.getClass() + " used, but the Problem is not of GroupedProblemForm");

            ArrayList<Subpopulation> subpops = state.population.subpops;
            int len = subpops.size();
            
            for(int pop=0;pop<len;pop++)
                {
                // start evaluatin'!
                int fp = from[pop];
                int upperbound = fp+numinds[pop];
                ArrayList<Individual> inds = new ArrayList<>(subpops.get(pop).individuals.subList(fp, upperbound));
                ((GroupedProblemForm)p).evaluate(state, toArray(inds), repeat(true, inds.size()), false, new int[] { pop }, threadnum);
                state.incrementEvaluations(upperbound - fp);
                }
                            
            ((ec.Problem)p).finishEvaluating(state,threadnum); 
        }

    /** Convert an ArrayList<Individual> to an Individual[] */
    private static Individual[] toArray(final ArrayList<Individual> inds)
        {
            final Individual[] result = new Individual[inds.size()];
            for (int i = 0; i < inds.size(); i++)
                result[i] = inds.get(i);
            return result;
        }

    /** Repeat a Boolean element `length` times. */
    private boolean[] repeat(final boolean value, final int length)
        {
            final boolean[] array = new boolean[length];
            for (int i = 0; i < length; i++)
                array[i] = value;
            return array;
        }
    }
