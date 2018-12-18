/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.coevolve;

import java.util.ArrayList;

import ec.*;
import ec.simple.*;
import ec.util.*;

/** 
 * MultiPopCoevolutionaryEvaluator.java
 *

 <p>MultiPopCoevolutionaryEvaluator is an Evaluator which performs <i>competitive or cooperative multi-population
 coevolution</i>.  Competitive coevolution is where individuals' fitness is determined by
 testing them against individuals from other subpopulation.  Cooperative coevolution is where individuals
 form teams together with members of other subpopulations, and the individuals' fitness is computed based
 on the performance of such teams.  This evaluator assumes that the problem can only evaluate groups of
 individuals containing one individual from each subpopulation.  Individuals are evaluated regardless of
 whether or not they've been evaluated in the past.

 <p>Your Problem is responsible for updating up the fitness appropriately with values usually obtained
 from teaming up the individual with different partners from the other subpopulations.
 MultiPopCoevolutionaryEvaluator expects to use Problems which adhere to the GroupedProblemForm
 interface, which defines a new evaluate(...) function, plus a preprocess(...) and postprocess(...) function.

 <p>This coevolutionary evaluator is single-threaded -- maybe we'll hack in multithreading later.  It allows
 any number of subpopulations (implicitly, any number of individuals being evaluated together). The order of
 individuals in the subpopulation may be changed during the evaluation process.

 <p>Ordinarily MultiPopCoevolutionaryEvaluator does "parallel" coevolution: all subpopulations are evaluated
 simultaneously, then bred simultaneously.  But if you set the "sequential" parameter in the class 
 ec.simple.SimpleBreeder, then MultiPopCoevolutionary behaves in a sequential fashion common in the "classic"
 version of cooperative coevolution: only one subpopulation is evaluated and bred per generation.
 The subpopulation index to breed is determined by taking the generation number, modulo the
 total number of subpopulations.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><b>breed</b>.sequential</tt><br>
 <font size=-1>boolean (default = false)</font></td>
 <td valign=top>(should we evaluate and breed a single subpopulation each generation?  Note that this is a SimpleBreeder parameter. )
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.num-current</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of random individuals from any given subpopulation fropm the current population to be selected as collaborators)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.num-gurus</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of guru individuals from any given subpopulation from the previous population to be selected as collaborators. For generation 0, random individuals from the current population will be used.  )
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.num-prev</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of random individuals from any given subpopulation from the previous population to be selected as collaborators.   For generation 0, random individuals from the current population will be used)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.X.select-prev</tt><br>
 <font size=-1> instance of ec.SelectionMethod</font></td>
 <td valign=top>(the SelectionMethod used to select partners from the individuals in subpopulation X at the previous generation)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.X.select-current</tt><br>
 <font size=-1> instance of ec.SelectionMethod</font></td>
 <td valign=top>(the SelectionMethod used to select partners from the individuals in subpopulation X at the current generation.
 <b>WARNING.</b>  This SelectionMethod must not select based on fitness, since fitness hasn't been set yet.
 RandomSelection is a good choice. )

 <tr><td valign=top><i>base.</i><tt>shuffling</tt><br>
 <font size=-1> boolean (default = false)</font></td>
 <td valign=top>(instead of selecting individuals from )
 </td></tr>
 

 </table>

 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0 
 */

public class MultiPopCoevolutionaryEvaluator extends Evaluator
    {
    private static final long serialVersionUID = 1;

    // the preamble for selecting partners from each subpopulation
    public static final String P_SUBPOP = "subpop";

    // the number of random partners selected from the current generation
    public static final String P_NUM_RAND_IND = "num-current";
    protected int numCurrent;

    // the number of shuffled random partners selected from the current generation
    public static final String P_NUM_SHUFFLED = "num-shuffled";
    protected int numShuffled;

    // the number of guru partners selected from the previous generation
    public static final String P_NUM_GURU = "num-gurus";
    protected int numGuru;
    Individual[/*subpopulation*/][/*the gurus*/] guruIndividuals;

    // the number of random partners selected from the current and previous generations
    public final static String P_NUM_IND = "num-prev";
    protected int numPrev;
    Population previousPopulation;

    // the selection method used to select the other partners from the previous generation
    public static final String P_SELECTION_METHOD_PREV = "select-prev";
    SelectionMethod[] selectionMethodPrev;

    // the selection method used to select the other partners from the current generation
    public static final String P_SELECTION_METHOD_CURRENT = "select-current";
    SelectionMethod[] selectionMethodCurrent;
                        
    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );
                
        // evaluators are set up AFTER breeders, so I can check this now
        if (state.breeder instanceof SimpleBreeder &&
            ((SimpleBreeder)(state.breeder)).sequentialBreeding)  // we're going sequentil
            state.output.message("The Breeder is breeding sequentially, so the MultiPopCoevolutionaryEvaluator is also evaluating sequentially.");
                                
        // at this point, we do not know the number of subpopulations, so we read it as well from the parameters file
        Parameter tempSubpop = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numSubpopulations = state.parameters.getInt( tempSubpop, null, 0 );
        if( numSubpopulations <= 0 )
            state.output.fatal( "Parameter not found, or it has a non-positive value.", tempSubpop );

        numGuru = state.parameters.getInt( base.push(P_NUM_GURU), null, 0 );
        if( numGuru < 0 )
            state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_GURU) );

        numShuffled = state.parameters.getInt( base.push(P_NUM_SHUFFLED), null, 0 );
        if( numShuffled < 0 )
            state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_SHUFFLED) );

        numCurrent = state.parameters.getInt( base.push(P_NUM_RAND_IND), null, 0 );
        selectionMethodCurrent = new SelectionMethod[numSubpopulations];
        if( numCurrent < 0 )
            state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_RAND_IND) );
        else if( numCurrent == 0 )
            state.output.message( "Not testing against current individuals:  Current Selection Methods will not be loaded.");
        else if( numCurrent > 0 )
            {
            for(int i = 0; i < numSubpopulations; i++)
                {
                selectionMethodCurrent[i] = (SelectionMethod)
                    (state.parameters.getInstanceForParameter(
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_CURRENT),base.push(P_SELECTION_METHOD_CURRENT),SelectionMethod.class));
                if (selectionMethodCurrent[i] == null)
                    state.output.error("No selection method provided for subpopulation " + i,
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_CURRENT),
                        base.push(P_SELECTION_METHOD_CURRENT));
                else selectionMethodCurrent[i].setup(state,base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_CURRENT));
                }
            }

        numPrev = state.parameters.getInt( base.push(P_NUM_IND), null, 0 );
        selectionMethodPrev = new SelectionMethod[numSubpopulations];
        if( numPrev < 0 )
            state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_IND) );
        else if( numPrev == 0 )
            state.output.message( "Not testing against previous individuals:  Previous Selection Methods will not be loaded.");
        else if( numPrev > 0 )
            {
            for(int i = 0; i < numSubpopulations; i++)
                {
                selectionMethodPrev[i] = (SelectionMethod)
                    (state.parameters.getInstanceForParameter(
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_PREV),base.push(P_SELECTION_METHOD_PREV),SelectionMethod.class));
                if (selectionMethodPrev[i] == null)
                    state.output.error("No selection method provided for subpopulation " + i,
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_PREV),
                        base.push(P_SELECTION_METHOD_PREV));
                else selectionMethodPrev[i].setup(state,base.push(P_SUBPOP).push(""+i).push(P_SELECTION_METHOD_PREV));
                }
            }
                                                                                          
        if( numGuru + numCurrent + numPrev + numShuffled <= 0 )
            state.output.error( "The total number of partners to be selected should be > 0." );
        state.output.exitIfErrors();
        }

    public String runComplete( final EvolutionState state )
        {
        return null;
        }

    /** Returns true if the subpopulation should be evaluated.  This will happen if the Breeder
        believes that the subpopulation should be breed afterwards. */
    public boolean shouldEvaluateSubpop(EvolutionState state, int subpop, int threadnum)
        {
        return (state.breeder instanceof SimpleBreeder &&
            ((SimpleBreeder)(state.breeder)).shouldBreedSubpop(state, subpop, threadnum));
        }

    public void evaluatePopulation(final EvolutionState state)
        {
        // determine who needs to be evaluated
        boolean[] preAssessFitness = new boolean[state.population.subpops.size()];
        boolean[] postAssessFitness = new boolean[state.population.subpops.size()];
        for(int i = 0; i < state.population.subpops.size(); i++)
            {
            postAssessFitness[i] = shouldEvaluateSubpop(state, i, 0);
            preAssessFitness[i] = postAssessFitness[i] || (state.generation == 0);  // always prepare (set up trials) on generation 0
            }

                
        // do evaluation
        beforeCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );

        ((GroupedProblemForm)p_problem).preprocessPopulation(state,state.population, preAssessFitness, false);
        performCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        state.incrementEvaluations(((GroupedProblemForm)p_problem).postprocessPopulation(state, state.population, postAssessFitness, false));

        afterCoevolutionaryEvaluation( state, (GroupedProblemForm)p_problem );
        }

    protected void beforeCoevolutionaryEvaluation( final EvolutionState state, final Population population, final GroupedProblemForm prob )
        {
        if (state.generation == 0)
            {
            //
            // create arrays for the guru individuals in the population at the previous generation.
            // deep clone the guru individuals as random individuals (in the initial generation, nobody has been evaluated yet).
            //
            
            // deal with the gurus
            guruIndividuals = new Individual[state.population.subpops.size()][numGuru];
            // copy the first individuals in each subpopulation (they are already randomly generated)
            for( int i = 0 ; i < guruIndividuals.length ; i++ )
                {
                if( numGuru > state.population.subpops.get(i).individuals.size() )
                    state.output.fatal( "Number of guru partners is greater than the size of the subpopulation." );
                for( int j = 0; j < numGuru ; j++ )
                    guruIndividuals[i][j] = (Individual)(state.population.subpops.get(i).individuals.get(j).clone());  // just take the first N individuals of each subpopulation
                }
                        
            // test for shuffled
            if (numShuffled > 0)
                {
                int size = state.population.subpops.get(0).individuals.size();
                for (int i = 0; i < state.population.subpops.size(); i++)
                    {
                    if (state.population.subpops.get(i).individuals.size() != size)
                        state.output.fatal("Shuffling was requested in MultiPopCoevolutionaryEvaluator, but the subpopulation sizes are not the same.  " +
                            "Specifically, subpopulation 0 has size " + size + " but subpopulation " + i + " has size " + state.population.subpops.get(i).individuals.size());
                    }
                }
            }
        }

    // individuals to evaluate together
    Individual[] inds = null;
    // which individual should have its fitness updated as a result
    boolean[] updates = null;
        


    protected void shuffle(EvolutionState state, int[] a)
        {
        MersenneTwisterFast mtf = state.random[0];
        for(int x = a.length - 1; x >= 1; x--)
            {
            int rand = mtf.nextInt(x+1);
            int obj = a[x];
            a[x] = a[rand];
            a[rand] = obj;
            }
        }



    public void performCoevolutionaryEvaluation( final EvolutionState state,
        final Population population,
        final GroupedProblemForm prob )
        {
        int evaluations = 0;
                
        inds = new Individual[population.subpops.size()];
        updates = new boolean[population.subpops.size()];

        // we start by warming up the selection methods
        if (numCurrent > 0)
            for( int i = 0 ; i < selectionMethodCurrent.length; i++)
                selectionMethodCurrent[i].prepareToProduce( state, i, 0 );

        if (numPrev > 0)
            for( int i = 0 ; i < selectionMethodPrev.length ; i++ )
                {
                // do a hack here
                Population currentPopulation = state.population;
                state.population = previousPopulation;
                selectionMethodPrev[i].prepareToProduce( state, i, 0 );
                state.population = currentPopulation;
                }

        // build subpopulation array to pass in each time
        int[] subpops = new int[state.population.subpops.size()];
        for(int j = 0; j < subpops.length; j++)
            subpops[j] = j;
                
                
        // handle shuffled always
                
        if (numShuffled > 0)
            {
            int[/*numShuffled*/][/*subpop*/][/*shuffledIndividualIndexes*/] ordering = null;
            // build shuffled orderings
            ordering = new int[numShuffled][state.population.subpops.size()][state.population.subpops.get(0).individuals.size()];
            for(int c = 0; c < numShuffled; c++)
                for(int m = 0; m < state.population.subpops.size(); m++)
                    {
                    for(int i = 0; i < state.population.subpops.get(0).individuals.size(); i++)
                        ordering[c][m][i] = i;
                    if (m != 0)
                        shuffle(state, ordering[c][m]);
                    }
                                
            // for each individual
            for(int i = 0; i < state.population.subpops.get(0).individuals.size(); i++)
                for(int k = 0; k < numShuffled; k++)
                    {
                    for(int ind = 0; ind < inds.length; ind++)
                        { inds[ind] = state.population.subpops.get(ind).individuals.get(ordering[k][ind][i]); updates[ind] = true; }
                    prob.evaluate(state,inds,updates, false, subpops, 0);
                    evaluations++;
                    }
            }

                        
        // for each subpopulation
        for(int j = 0; j < state.population.subpops.size(); j++)
            {
            // now do gurus and randoms
                
            if (!shouldEvaluateSubpop(state, j, 0)) continue;  // don't evaluate this subpopulation

            // for each individual
            for(int i = 0; i < state.population.subpops.get(j).individuals.size(); i++)
                {
                Individual individual = state.population.subpops.get(j).individuals.get(i);
                                
                // Test against all the gurus
                for(int k = 0; k < guruIndividuals[j].length; k++)
                    {
                    for(int ind = 0; ind < inds.length; ind++)
                        {
                        if (ind == j) { inds[ind] = individual; updates[ind] = true; }
                        else  { inds[ind] = guruIndividuals[ind][k]; updates[ind] = false; }
                        }
                    prob.evaluate(state,inds,updates, false, subpops, 0);
                    evaluations++;
                    }
                                        
                // test against random selected individuals of the current population
                for(int k = 0; k < numCurrent; k++)
                    {
                    for(int ind = 0; ind < inds.length; ind++)
                        {
                        if (ind == j) { inds[ind] = individual; updates[ind] = true; }
                        else { inds[ind] = produceCurrent(ind, state, 0); updates[ind] = true; }
                        }
                    prob.evaluate(state,inds,updates, false, subpops, 0);
                    evaluations++;
                    }

                // Test against random individuals of previous population
                for(int k = 0; k < numPrev; k++)
                    {
                    for(int ind = 0; ind < inds.length; ind++)
                        {
                        if (ind == j) { inds[ind] = individual; updates[ind] = true; }
                        else { inds[ind] = producePrevious(ind, state, 0); updates[ind] = false; }
                        }
                    prob.evaluate(state,inds,updates, false, subpops, 0);
                    evaluations++;
                    }
                }
            }
                        
        // now shut down the selection methods
        if (numCurrent > 0)
            for( int i = 0 ; i < selectionMethodCurrent.length; i++)
                selectionMethodCurrent[i].finishProducing( state, i, 0 );

        if (numPrev > 0)
            for( int i = 0 ; i < selectionMethodPrev.length ; i++ )
                {
                // do a hack here
                Population currentPopulation = state.population;
                state.population = previousPopulation;
                selectionMethodPrev[i].finishProducing( state, i, 0 );
                state.population = currentPopulation;
                }
                
        state.output.message("Evaluations: " + evaluations);
        }


    /** Selects one individual from the previous subpopulation.  If there is no previous
        population, because we're at generation 0, then an individual from the current
        population is selected at random. */
    protected Individual producePrevious(int subpopulation, EvolutionState state, int thread)
        {
        if (state.generation == 0)  
            {
            // pick current at random.  Can't use a selection method because they may not have fitness assigned
            return state.population.subpops.get(subpopulation).individuals.get(state.random[0].nextInt(state.population.subpops.get(subpopulation).individuals.size()));
            }
        else
            {
            // do a hack here -- back up population, replace with the previous population, run the selection method, replace again
            Population currentPopulation = state.population;
            state.population = previousPopulation;
            Individual selected =
                state.population.subpops.get(subpopulation).individuals.get(selectionMethodPrev[subpopulation].produce(subpopulation, state, thread));
            state.population = currentPopulation;
            return selected;
            }
        }


    /** Selects one individual from the given subpopulation. */
    protected Individual produceCurrent(int subpopulation, EvolutionState state, int thread)
        {
        return state.population.subpops.get(subpopulation).individuals.get(selectionMethodCurrent[subpopulation].produce(subpopulation, state, thread));
        }



    protected void afterCoevolutionaryEvaluation( final EvolutionState state, final GroupedProblemForm prob )
        {
        if( numGuru > 0 )
            {
            for(int i = 0; i < state.population.subpops.size(); i++)
                if (shouldEvaluateSubpop(state, i, 0))          // only load gurus for subpopulations which are actually changing
                    loadGurus( state, i );
            }
                                
        // copy over the previous population
        if (numPrev > 0)
            {
            previousPopulation = (Population)(state.population.emptyClone());
            for(int i = 0; i < previousPopulation.subpops.size(); i++ )
                for(int j = 0; j < previousPopulation.subpops.get(i).individuals.size() ; j++ )
                    previousPopulation.subpops.get(i).individuals.add(j, (Individual)(state.population.subpops.get(i).individuals.get(j).clone()));
            }
        }


    void loadGurus( final EvolutionState state, int whichSubpop )
        {
        Subpopulation subpop = state.population.subpops.get(whichSubpop);
                
        if (numGuru==1)
            {
            int best = 0;
            ArrayList<Individual> oldinds = subpop.individuals;
            for(int x=1;x<oldinds.size();x++)
                if (oldinds.get(x).fitness.betterThan(oldinds.get(best).fitness))
                    best = x;
            guruIndividuals[whichSubpop][0] = (Individual)(state.population.subpops.get(whichSubpop).individuals.get(best).clone());
            }
        else if (numGuru > 0)  // we'll need to sort
            {
            int[] orderedPop = new int[subpop.individuals.size()];
            for(int x=0;x<subpop.individuals.size();x++) orderedPop[x] = x;

            // sort the best so far where "<" means "more fit than"
            QuickSort.qsort(orderedPop, new GuruComparator(subpop.individuals));

            // load the top N individuals
            for( int j = 0 ; j < numGuru ; j++ )
                guruIndividuals[whichSubpop][j] = (Individual)(state.population.subpops.get(whichSubpop).individuals.get(orderedPop[j]).clone());
            }
        }

    }

class GuruComparator implements SortComparatorL
    {
    ArrayList<Individual> inds;
    public GuruComparator(ArrayList<Individual> inds) {super(); this.inds = inds;}
    public boolean lt(long a, long b)
        { return inds.get((int)a).fitness.betterThan(inds.get((int)b).fitness); }
    public boolean gt(long a, long b)
        { return inds.get((int)b).fitness.betterThan(inds.get((int)a).fitness); }
    }

