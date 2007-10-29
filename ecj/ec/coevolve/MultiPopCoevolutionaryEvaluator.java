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
 <tr><td valign=top><i>base.</i><tt>subpop.X.num-rand-ind</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of random individuals from subpopulation X to be selected as partners for evaluating individuals in other subpopulations  -- individuals are selected at random, with replacement, and they are usually different for each of the individuals in the other subpopulations)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.X.num-elites</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of elite individuals from subpopulation X to be selected as partners for evaluating individuals in other subpopulations)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.X.num-ind</tt><br>
 <font size=-1> int &gt;= 0</font></td>
 <td valign=top>(the number of  individuals from subpopulation X in the previous generation to be selected as partners for evaluating individuals in other subpopulations  -- individuals are selected, with replacement, by using SelectionMethods described next, and they are usually different for each of the individuals in the other subpopulations)
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop.X.select</tt><br>
 <font size=-1> instance of ec.SelectionMethod</font></td>
 <td valign=top>(the SelectionMethod used to select partners from the individuals in subpopulation X at the previous generation)
 </td></tr>

 </table>

 *
 * @author Liviu Panait
 * @version 2.0 
 */

public class MultiPopCoevolutionaryEvaluator extends Evaluator
    {
    // the preamble for selecting partners from each subpopulation
    public static final String P_SUBPOP = "subpop";

    // the number of random partners selected from the current generation
    public static final String P_NUM_RAND_IND = "num-rand-ind";
    protected int[] numRand;
    public int getNumRandomPartners(int subpop)
        {
        return numRand[subpop];
        }

    // the number of elite partners selected from the previous generation
    public static final String P_NUM_ELITE = "num-elites";
    protected int[] numElite;
    public int getNumEliteIndividuals(int subpop)
        {
        return numElite[subpop];
        }
    Individual[][] eliteIndividuals;


    // the number of other partners selected from the previous generation
    public final static String P_NUM_IND = "num-ind";
    protected int[] numInd;
    public int getNumPreviousGenerationPartners(int subpop)
        {
        return numInd[subpop];
        }

    // the selection method used to select the other partners from the previous generation
    public static final String P_SELECTIONMETHOD = "select";
    SelectionMethod[] selectionMethod;

    // the total number of partners (numRand+numElite+numInd)
    protected int[] numSelected;
    public int getNumPartners(int subpop)
        {
        return numSelected[subpop];
        }

    Population previousPopulation;

    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );

        // at this point, we do not know the number of subpopulations, so we read it as well from the parameters file
        Parameter tempSubpop = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numSubpopulations = state.parameters.getInt( tempSubpop, null, 0 );
        if( numSubpopulations <= 0 )
            state.output.fatal( "Parameter not found, or it has a non-positive value.", tempSubpop );

        selectionMethod = new SelectionMethod[numSubpopulations];
        numElite = new int[numSubpopulations];
        numRand = new int[numSubpopulations];
        numInd = new int[numSubpopulations];
        numSelected = new int[numSubpopulations];

        for( int i = 0 ; i < numSubpopulations ; i++ )
            {
            numElite[i] = state.parameters.getInt( base.push(P_SUBPOP).push(""+i).push(P_NUM_ELITE), null, 0 );
            if( numElite[i] < 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_SUBPOP).push(""+i).push(P_NUM_ELITE) );

            numRand[i] = state.parameters.getInt( base.push(P_SUBPOP).push(""+i).push(P_NUM_RAND_IND), null, 0 );
            if( numRand[i] < 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_SUBPOP).push(""+i).push(P_NUM_RAND_IND) );

            numInd[i] = state.parameters.getInt( base.push(P_SUBPOP).push(""+i).push(P_NUM_IND), null, 0 );
            if( numInd[i] < 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", base.push(P_SUBPOP).push(""+i).push(P_NUM_IND) );
            else if( numInd[i] > 0 )
                {
                selectionMethod[i] = (SelectionMethod)
                    (state.parameters.getInstanceForParameter(
                        base.push(P_SUBPOP).push(""+i).push(P_SELECTIONMETHOD),null,SelectionMethod.class));
                selectionMethod[i].setup(state,base.push(P_SUBPOP).push(""+i).push(P_SELECTIONMETHOD));
                }
                                                  
            numSelected[i] = numElite[i] + numRand[i] + numInd[i];

            if( numSelected[i] <= 0 )
                state.output.fatal( "The total number of partners to be selected from subpopulation " + i + " should be > 0." );

            }
        }

    public boolean runComplete( final EvolutionState state )
        {
        return false;
        }

    public void evaluatePopulation(final EvolutionState state)
        {

        beforeCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );

        ((GroupedProblemForm)p_problem).preprocessPopulation(state,state.population);
        performCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        ((GroupedProblemForm)p_problem).postprocessPopulation(state, state.population);

        afterCoevolutionaryEvaluation( state, state.population, (GroupedProblemForm)p_problem );
        }

    public void beforeCoevolutionaryEvaluation( final EvolutionState state,
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
            eliteIndividuals = new Individual[state.population.subpops.length][];
            for( int i = 0 ; i < state.population.subpops.length ; i++ )
                eliteIndividuals[i] = new Individual[numElite[i]];
            // copy the first individuals in each subpopulation (they are already randomly generated)
            for( int i = 0 ; i < eliteIndividuals.length ; i++ )
                {
                if( numElite[i] > state.population.subpops[i].individuals.length )
                    state.output.fatal( "Number of elite partners is greater than the size of the subpopulation." );
                for( int j = 0; j < numElite[i] ; j++ )
                    eliteIndividuals[i][j] = (Individual)(state.population.subpops[i].individuals[j].clone());
                }
            }
        }

    private Individual[] mates = null;
    private boolean[] updates = null;
    public void performCoevolutionaryEvaluation( final EvolutionState state,
                                                 final Population population,
                                                 final GroupedProblemForm prob )
        {

        // such that the selection methods actually select from the previous population
        Population currentPopulation = population;

        for( int i = 0 ; i < selectionMethod.length ; i++ )
            if( numInd[i] > 0 )
                {
                state.population = previousPopulation;
                selectionMethod[i].prepareToProduce( state, i, 0 );
                state.population = currentPopulation;
                }

        // the individuals to be evaluated together (one from each subpopulation)
        if( mates == null || mates.length != numSelected.length )
            mates = new Individual[numSelected.length];

        // the fitnesses of which individuals need to be updated
        if( updates == null || updates.length != mates.length )
            updates = new boolean[mates.length];
        for( int i = 0 ; i < updates.length ; i++ )
            updates[i] = false;

        // the indexes (in order to generate all possible combinations)
        int[] indexes = new int[numSelected.length-1];

        for( int i = 0 ; i < population.subpops.length ; i++ )
            {
            updates[i] = true;

            int totalCases = 1;
            for( int j = 0 ; j < population.subpops.length ; j++ )
                if( j != i )
                    totalCases = totalCases * numSelected[j];

            for( int j = 0 ; j < population.subpops[i].individuals.length ; j++ )
                {
                mates[i] = population.subpops[i].individuals[j];

                // generate all possible combinations of mates
                for( int k = 0 ; k < indexes.length ; k++ )
                    indexes[k] = 0;
                for( int testcase = 0 ; testcase < totalCases ; testcase++ )
                    {
                    // select the mates according to the current case
                    int curI = 0;
                    for( int k = 0 ; k < mates.length ; k++ )
                        if( k != i )
                            {
                            if( indexes[curI] < numElite[k] ) // the first numElite[k] individuals should be the elite
                                {
                                mates[k] = eliteIndividuals[k][indexes[curI]];
                                }
                            else if( indexes[curI] < numElite[k] + numRand[k] ) // the next numRand[k] individuals are random ones from current population
                                {
                                mates[k] = population.subpops[k].individuals[state.random[0].nextInt(population.subpops[k].individuals.length)];
                                }
                            else // the remaining individuals are to be selected from the previous population
                                {
                                // in the first generation, pick them at random anyway
                                if( state.generation == 0 )
                                    {
                                    mates[k] = population.subpops[k].individuals[state.random[0].nextInt(population.subpops[k].individuals.length)];
                                    }
                                else
                                    {
                                    state.population = previousPopulation;
                                    int index = selectionMethod[k].produce( k, state, 0 );
                                    state.population = currentPopulation;

                                    mates[k] = previousPopulation.subpops[k].individuals[index];                    
                                    }
                                }
                            curI++;
                            }

                    // perform the coevolutionary evaluation of the group of individuals
                    prob.evaluate(state,mates,updates,false,0);

                    curI = 0;
                    // select the next case
                    for( int k = 0 ; k < numSelected.length ; k++ )
                        if( k != i )
                            {
                            if( indexes[curI] < numSelected[k]-1 )
                                {
                                indexes[curI]++;
                                break;
                                }
                            else
                                {
                                indexes[curI] = 0;
                                }
                            curI++;
                            }
                    }
                }

            updates[i] = false;
            }

        for( int i = 0 ; i < selectionMethod.length ; i++ )
            if( numInd[i] > 0 )
                {
                state.population = previousPopulation;
                selectionMethod[i].finishProducing( state, i, 0 );
                state.population = currentPopulation;
                }

        }

    public void afterCoevolutionaryEvaluation( final EvolutionState state,
                                               final Population population,
                                               final GroupedProblemForm prob )
        {


        // for each subpopulation, select the individuals to be used for evaluation for the other subpopulations
        for( int i = 0 ; i < numElite.length ; i++ )
            if( numElite[i] > 0 )
                {
                loadElites( state, state.population.subpops[i], i );
                }

        // deal with the previous population
        previousPopulation = (Population)(state.population.emptyClone());
        for( int i = 0 ; i < previousPopulation.subpops.length ; i++ )
            for( int j = 0 ; j < previousPopulation.subpops[i].individuals.length ; j++ )
                previousPopulation.subpops[i].individuals[j] = (Individual)(state.population.subpops[i].individuals[j].clone());

        }

    public void loadElites( final EvolutionState state,
                            final Subpopulation subpop,
                            int whichSubpop )
        {
        if (numElite[whichSubpop]==1)
            {
            int best = 0;
            Individual[] oldinds = subpop.individuals;
            for(int x=1;x<oldinds.length;x++)
                if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                    best = x;
            eliteIndividuals[whichSubpop][0] = (Individual)(state.population.subpops[whichSubpop].individuals[best].clone());
            }
        else if (numElite[whichSubpop]>0)  // we'll need to sort
            {
            int[] orderedPop = new int[subpop.individuals.length];
            for(int x=0;x<subpop.individuals.length;x++) orderedPop[x] = x;

            // sort the best so far where "<" means "more fit than"
            QuickSort.qsort(orderedPop, new EliteComparator(subpop.individuals));

            // load the top N individuals
            for( int j = 0 ; j < numElite[whichSubpop] ; j++ )
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

