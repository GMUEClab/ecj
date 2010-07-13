/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.coevolve;

import ec.*;
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

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>subpop.num-current</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of random individuals from any given subpopulation to be selected as collaborators)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.num-elites</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of elite individuals from any given subpopulation to be selected as collaborators. For generation 0, random individuals from the current population will be used.  )
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
 </td></tr>
 

 </table>

 *
 * @author Liviu Panait and Sean Luke
 * @version 2.0 
 */

public class MultiPopCoevolutionaryEvaluator extends Evaluator
    {
    // the preamble for selecting partners from each subpopulation
    public static final String P_SUBPOP = "subpop";

    // the number of random partners selected from the current generation
    public static final String P_NUM_RAND_IND = "num-current";
    protected int numCurrent;

    // the number of elite partners selected from the previous generation
    public static final String P_NUM_ELITE = "num-elites";
    protected int numElite;
    Individual[][] eliteIndividuals;

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

        // at this point, we do not know the number of subpopulations, so we read it as well from the parameters file
        Parameter tempSubpop = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numSubpopulations = state.parameters.getInt( tempSubpop, null, 0 );
        if( numSubpopulations <= 0 )
            state.output.fatal( "Parameter not found, or it has a non-positive value.", tempSubpop );

        selectionMethodPrev = new SelectionMethod[numSubpopulations];

		numElite = state.parameters.getInt( base.push(P_NUM_ELITE), null, 0 );
		if( numElite < 0 )
			state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_ELITE) );

		numCurrent = state.parameters.getInt( base.push(P_NUM_RAND_IND), null, 0 );
		selectionMethodCurrent = new SelectionMethod[numSubpopulations];
		if( numCurrent < 0 )
			state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_NUM_RAND_IND) );
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
											  
		if( numElite + numCurrent + numPrev <= 0 )
			state.output.error( "The total number of partners to be selected should be > 0." );
		state.output.exitIfErrors();
        }

    public boolean runComplete( final EvolutionState state )
        {
        return false;
        }

    public void evaluatePopulation(final EvolutionState state)
        {
        beforeCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );

        ((GroupedProblemForm)p_problem).preprocessPopulation(state,state.population, false);
        performCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        ((GroupedProblemForm)p_problem).postprocessPopulation(state, state.population, false);

        afterCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        }

    protected void beforeCoevolutionaryEvaluation( final EvolutionState state,
        final Population population,
        final GroupedProblemForm prob )
        {
        if( state.generation == 0 )
            {
            //
            // create arrays for the elite individuals in the population at the previous generation.
            // deep clone the elite individuals as random individuals (in the initial generation, nobody has been evaluated yet).
            //
            
            // deal with the elites
            eliteIndividuals = new Individual[state.population.subpops.length][numElite];
            // copy the first individuals in each subpopulation (they are already randomly generated)
            for( int i = 0 ; i < eliteIndividuals.length ; i++ )
                {
                if( numElite > state.population.subpops[i].individuals.length )
                    state.output.fatal( "Number of elite partners is greater than the size of the subpopulation." );
                for( int j = 0; j < numElite ; j++ )
                    eliteIndividuals[i][j] = (Individual)(state.population.subpops[i].individuals[j].clone());
                }
            }
        }

	// individuals to evaluate together
	Individual[] inds = null;
	// which individual should have its fitness updated as a result
	boolean[] updates = null;
	
    public void performCoevolutionaryEvaluation( final EvolutionState state,
        final Population population,
        final GroupedProblemForm prob )
        {
		inds = new Individual[population.subpops.length];
		updates = new boolean[population.subpops.length];
		
		// we start by warming up the selection methods
		for( int i = 0 ; i < selectionMethodPrev.length ; i++ )
                {
                selectionMethodCurrent[i].prepareToProduce( state, i, 0 );
				
				// do a hack here
                Population currentPopulation = state.population;
				state.population = previousPopulation;
                selectionMethodPrev[i].prepareToProduce( state, i, 0 );
				state.population = currentPopulation;
                }


		// build subpopulation array to pass in each time
		int[] subpops = new int[state.population.subpops.length];
		for(int j = 0; j < subpops.length; j++) subpops[j] = j;

		// for each subpopulation
		for(int j = 0; j < state.population.subpops.length; j++)
			// for each individual
			for(int i = 0; i < state.population.subpops[j].individuals.length; i++)
				{
				
				// Test against all the elites
				for(int k = 0; k < eliteIndividuals[j].length; k++)
					{
					for(int ind = 0; ind < inds.length; ind++)
						{
						if (ind == j) { inds[ind] = state.population.subpops[j].individuals[i]; updates[ind] = true; }
						else  { inds[ind] = eliteIndividuals[ind][k]; updates[ind] = false; }
						}
                    prob.evaluate(state,inds,updates, false, subpops, 0);
					}
					
				// Test against random individuals of currrent population
				for(int k = 0; k < numCurrent; k++)
					{
					for(int ind = 0; ind < inds.length; ind++)
						{
						if (ind == j) { inds[ind] = state.population.subpops[j].individuals[i]; updates[ind] = true; }
						else { inds[ind] = produce(selectionMethodCurrent[j], j, i, state, 0); updates[ind] = false; }
						}
                    prob.evaluate(state,inds,updates, false, subpops, 0);
					}

				// Test against random individuals of previous population
				for(int k = 0; k < numCurrent; k++)
					{
					for(int ind = 0; ind < inds.length; ind++)
						{
						if (ind == j) { inds[ind] = state.population.subpops[j].individuals[i]; updates[ind] = true; }
						else if (state.generation > 0)
							{ 
							// do a hack here
							Population currentPopulation = state.population;
							state.population = previousPopulation;
							inds[ind] = produce(selectionMethodPrev[j], j, i, state, 0); 
							state.population = currentPopulation;
							
							updates[ind] = false;
							}
						else
							{
							inds[ind] = state.population.subpops[j].individuals[
								state.random[0].nextInt(state.population.subpops[j].individuals.length)];
							updates[ind] = false;
							}
						}
                    prob.evaluate(state,inds,updates, false, subpops, 0);
					}
				}

		// now shut down the selection methods
        for( int i = 0 ; i < selectionMethodPrev.length ; i++ )
                {
                selectionMethodCurrent[i].finishProducing( state, i, 0 );
				
				// do a hack here
                Population currentPopulation = state.population;
				state.population = previousPopulation;
                selectionMethodPrev[i].finishProducing( state, i, 0 );
				state.population = currentPopulation;
                }
        }


	protected Individual produce(SelectionMethod method, int subpopulation, int individual, EvolutionState state, int thread)
		{
		return state.population.subpops[subpopulation].individuals[method.produce(subpopulation, state, thread)];
		}

    protected void afterCoevolutionaryEvaluation( final EvolutionState state,
        final Population population,
        final GroupedProblemForm prob )
        {
		if( numElite > 0 )
			{
			for(int i = 0 ; i < state.population.subpops.length; i++)
				loadElites( state, state.population.subpops[i], i );
			}
				
        // copy over the previous population
        previousPopulation = (Population)(state.population.emptyClone());
        for( int i = 0 ; i < previousPopulation.subpops.length ; i++ )
            for( int j = 0 ; j < previousPopulation.subpops[i].individuals.length ; j++ )
                previousPopulation.subpops[i].individuals[j] = (Individual)(state.population.subpops[i].individuals[j].clone());
        }

	void loadElites( final EvolutionState state,
        final Subpopulation subpop,
        int whichSubpop )
        {
        if (numElite==1)
            {
            int best = 0;
            Individual[] oldinds = subpop.individuals;
            for(int x=1;x<oldinds.length;x++)
                if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                    best = x;
            eliteIndividuals[whichSubpop][0] = (Individual)(state.population.subpops[whichSubpop].individuals[best].clone());
            }
        else if (numElite > 0)  // we'll need to sort
            {
            int[] orderedPop = new int[subpop.individuals.length];
            for(int x=0;x<subpop.individuals.length;x++) orderedPop[x] = x;

            // sort the best so far where "<" means "more fit than"
            QuickSort.qsort(orderedPop, new EliteComparator(subpop.individuals));

            // load the top N individuals
            for( int j = 0 ; j < numElite ; j++ )
                eliteIndividuals[whichSubpop][j] = (Individual)(state.population.subpops[whichSubpop].individuals[orderedPop[j]].clone());
            }
        }

    }

class EliteComparator implements SortComparatorL
    {
    Individual[] inds;
    public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
    public boolean lt(long a, long b)
        { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
    public boolean gt(long a, long b)
        { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
    }

