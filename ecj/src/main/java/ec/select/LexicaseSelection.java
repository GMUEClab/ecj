package ec.select;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.SelectionMethod;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Eric 'Siggy' Scott
 */
public class LexicaseSelection extends SelectionMethod
    {
    private static final long serialVersionUID = 1;
    
    public static final String P_LEXICASESELECT = "lexicaseselect";

    @Override
    public Parameter defaultBase()
        {
        return SelectDefaults.base().push(P_LEXICASESELECT);
        }

    @Override
    public int produce(final int subpopulation, final EvolutionState state, final int thread)
        {
        assert(state != null);
        assert(subpopulation >= 0);
        assert(subpopulation < state.population.subpops.size());
        assert(state.population.subpops.get(subpopulation) != null);
        assert(state.population.subpops.get(subpopulation).individuals.size() > 0);
        
        final ArrayList<Individual> pop = state.population.subpops.get(subpopulation).individuals;
        
        // Initialize the candidates to the entire population
        final ArrayList<Integer> candidates = new ArrayList<Integer>();
        for (int i = 0; i < pop.size(); i++)
            candidates.add(i);
        
        // Shuffle test cases
        assert(pop.get(candidates.get(0)).fitness.trials != null);
        final int numCases = pop.get(0).fitness.trials.size();
        if (numCases == 0)
            state.output.fatal(String.format("Attempted to use %s on an individual with an empty list of trials.", this.getClass().getSimpleName()));
        final int[] caseOrder = new int[numCases];
        for (int i = 0; i < numCases; i++)
            caseOrder[i] = i;
        shuffle(state, caseOrder);
        
        for (int i = 0; i < caseOrder.length; i++)
            {
            final int currentCase = caseOrder[i];
            
            // Find the best value of the current test case
            Fitness best = (Fitness) pop.get(candidates.get(0)).fitness.trials.get(currentCase);
            for (int j = 1; j < candidates.size(); j++)
                {
                assert(pop.get(candidates.get(j)).fitness.trials != null);
                assert(pop.get(candidates.get(j)).fitness.trials.size() == numCases);
                final Fitness caseFitness = (Fitness) pop.get(candidates.get(j)).fitness.trials.get(currentCase);
                if (caseFitness.betterThan(best))
                    best = caseFitness;
                }
            
            // Reduce candidates to the subset that performs best on the current test case
            final Iterator<Integer> it = candidates.iterator();
            while (it.hasNext())
                {
                final Fitness caseFitness = (Fitness) pop.get(it.next()).fitness.trials.get(currentCase);
                if (caseFitness.compareTo(best) > 0) // if strictly worse than best
                    it.remove();
                }
            
            // If only one individual is left, return it
            if (candidates.size() == 1)
                {
                return candidates.get(0);
                }
            
            // If this was the last test case, return a random candidate
            if (i == caseOrder.length - 1)
                {
                return candidates.get(state.random[0].nextInt(candidates.size()));
                }
            }
        throw new IllegalStateException();
        }
    

    private void shuffle(final EvolutionState state, final int[] a)
        {
        final MersenneTwisterFast mtf = state.random[0];
        for(int x = a.length - 1; x >= 1; x--)
            {
            int rand = mtf.nextInt(x+1);
            int obj = a[x];
            a[x] = a[rand];
            a[rand] = obj;
            }
        }
    
    }
