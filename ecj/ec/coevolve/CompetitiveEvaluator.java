/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.coevolve;
import ec.*;
import ec.util.*;

/** 
 * CompetitiveEvaluator.java
 *

 <p>CompetitiveEvaluator is a Evaluator which performs <i>competitive fitness evaluations</i>.  
 Competitive fitness is where individuals' fitness is determined by testing them against 
 other members of the same subpopulation.  Competitive fitness topologies differ from
 co-evolution topologies in that co-evolution is a term I generally reserve for 
 multiple sbupopulations which breed separately but compete against other subpopulations 
 during evaluation time.  Individuals are evaluated regardless of whether or not they've
 been evaluated in the past.

 <p>Your Problem is responsible for setting up the fitness appropriately.  
 CompetitiveEvaluator expects to use Problems which adhere to the GroupedProblemForm interface, 
 which defines a new evaluate(...) function, plus a preprocess(...) and postprocess(...) function.

 <p>This competitive fitness evaluator is single-threaded -- maybe we'll hack in multithreading later. 
 And it only has two individuals competing during any fitness evaluation.  The order of individuals in the 
 subpopulation will be changed during the evaluation process.  There are seven evaluation topologies
 presently supported:

 <p><dl>
 <dt><b>Single Elimination Tournament</b><dd>
 All members of the population are paired up and evaluated.  In each pair, the "winner" is the individual
 which winds up with the superior fitness.  If neither fitness is superior, then the "winner" is picked
 at random.  Then all the winners are paired up and evaluated, and so on, just like in a single elimination
 tournament.  It is important that the <b>population size be a <i>power of two</i></b>, else some individuals
 will not have the same number of "wins" as others and may lose the tournament as a result.

 <dt><b>Round Robin</b><dd>
 Every member of the population are paired up and evaluated with all other members of the population, not
 not including the member itself (we might add in self-play as a future later if people ask for it, it's
 easy to hack in).

 <dt><b>Pseudo Round Robin</b><dd>
 The population is split into groups with <i>group-size</i> individuals, and there is a round robin
 tournament for each such group.

 <dt><b>K-Random-Opponents-One-Way</b><dd>
 Each individual's fitness is calculated based on K competitions against random opponents.
 For details, see "A Comparison of Two Competitive Fitness Functions" by Liviu Panait and
 Sean Luke in the Proceedings of GECCO 2002.

 <dt><b>K-Random-Opponents-Two-Ways</b><dd>
 Each individual's fitness is calculated based on K competitions against random opponents. The advantage of
 this method over <b>K-Random-Opponents-One-Way</b> is a reduced number of competitions (when I competes
 against J, both I's and J's fitnesses are updated, while in the previous method only one of the individuals
 has its fitness updated).
 For details, see "A Comparison of Two Competitive Fitness Functions" by Liviu Panait and
 Sean Luke in the Proceedings of GECCO 2002.
 </dl> 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>style</tt><br>
 <font size=-1>string with possible values: </font></td>
 <td valign=top>(the style of the tournament)<br>
 <i>single-elim-tournament</i> (a single elimination tournament)<br>
 <i>round-robin</i> (a round robin tournament)<br>
 <i>pseudo-round-robin</i> (population is split into groups with <i>group-size</i> individuals, and there is a round robin tournament for each such group)<br>
 <i>rand-1-way</i> (K-Random-Opponents, each game counts for only one of the players)<br>
 <i>rand-2-ways</i> (K-Random-Opponents, each game counts for both players)<br>
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>group-size</tt><br>
 <font size=-1> int</font></td>
 <td valign=top>(how many individuals per group, used in pseudo-round-robin, rand-1-way</i> and <i>rand-2-ways</i> tournaments)<br>
 <i>group-size</i> &gt;= 2 for <i>pseudo-round-robin</i><br>
 <i>group-size</i> &gt;= 1 for <i>rand-1-way</i> or <i>rand-2-ways</i><br>
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>over-eval</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(if the tournament style leads to an individual playing more games than others, should the extra games be used for his fitness evaluatiuon?)</td></tr>

 </table>

 *
 * @author Sean Luke & Liviu Panait
 * @version 1.0 
 */

/*
// double elimination tournament has been removed for the time being because although
// it's a perfectly good method, it's not actually double elimination tournament -- we
// will revisit this later.  In the mean time, here's what the comments were:

<dt><b>Double Elimination Tournament</b><dd>
Similar to Single Elimination Tournament, except that there are in fact two single elimination
tournaments going on: the normal one, where undefeated individuals compete, and another one with
the individuals that lost one game. When an individual loses in the first tournament, he's placed
in the second tournament. When the individual loses in the second tournament, he's assigned a fitness
better than that of all individuals already eliminated from the second tournament, but worse than that
of all inviduals still competing.

<i>double-elim-tournament</i> (a double elimination tournament)<br>




// world cup tournament has been removed for the time being because its involves both ranking and
// non-ranking tournaments and we need to think this more before releasing it.  here's some comments
// about the method, and the class also contains some commented out code.

<dt><b>World Cup</b><dd>
The population is split into groups with <i>group-size</i> individuals, and there is a round robin
tournament for each such group. The first best <i>qualify-per-group</i> individuals in each group
qualify to a single elimination tournament that ranks (more or less) the better teams.

<i>world-cup</i> (grouping of individuals and round-robin tournaments for each group, then seeding and a single elimination tournament with the better individuals)<br>

<i>group-size</i> &gt;= 2 for <i>world-cup</i><br>

*/


public class CompetitiveEvaluator extends Evaluator
    {
    public static final int STYLE_SINGLE_ELIMINATION=1;
    public static final int STYLE_ROUND_ROBIN=2;
    public static final int STYLE_N_RANDOM_COMPETITORS_ONEWAY=3;
    public static final int STYLE_N_RANDOM_COMPETITORS_TWOWAY=4;
    public static final int STYLE_PSEUDO_ROUND_ROBIN=5;
//    public static final int STYLE_WORLD_CUP=6;
//    public static final int STYLE_DOUBLE_ELIMINATION=7;

    public static final String competeStyle = "style";
    public int style;

    public static final String size = "group-size";
    public int groupSize;

//    public static final String P_QUALIFY_PER_GROUP = "qualify-per-group";
//    public int numQualify;

    public static final String overEval = "over-eval";
    public boolean allowOverEvaluation;

    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );
        String temp;
        temp = state.parameters.getStringWithDefault( base.push( competeStyle ), null, "" );
        if( temp.equalsIgnoreCase( "single-elim-tournament" ) )
            {
            style = STYLE_SINGLE_ELIMINATION;
            }
        /*else if( temp.equalsIgnoreCase( "double-elim-tournament" ) )
          {
          style = STYLE_DOUBLE_ELIMINATION;
          }*/
        else if( temp.equalsIgnoreCase( "round-robin" ) )
            {
            style = STYLE_ROUND_ROBIN;
            }
        else if( temp.equalsIgnoreCase( "pseudo-round-robin" ) )
            {
            style = STYLE_PSEUDO_ROUND_ROBIN;
            }
        /*else if( temp.equalsIgnoreCase( "world-cup" ) )
          {
          style = STYLE_WORLD_CUP;
          }*/
        else if( temp.equalsIgnoreCase( "rand-1-way" ) )
            {
            style = STYLE_N_RANDOM_COMPETITORS_ONEWAY;
            }
        else if( temp.equalsIgnoreCase( "rand-2-ways" ) )
            {
            style = STYLE_N_RANDOM_COMPETITORS_TWOWAY;
            }
        else
            {
            state.output.fatal( "Incorrect value for parameter. Acceptable values: " +
                                "single-elim-tournament, round-robin, rand-1-way, rand-2-ways" 
                                /* + ", world-cup, double-elim-tournament"*/ , base.push( competeStyle ) );
            }

        if( style == STYLE_N_RANDOM_COMPETITORS_ONEWAY || style == STYLE_N_RANDOM_COMPETITORS_TWOWAY )
            {
            groupSize = state.parameters.getInt( base.push( size ), null, 1 );
            if( groupSize < 1 )
                {
                state.output.fatal( "Incorrect value for parameter", base.push( size ) );
                }
            }
        if( style == STYLE_PSEUDO_ROUND_ROBIN )
            {
            groupSize = state.parameters.getInt( base.push( size ), null, 2 );
            if( groupSize < 2 )
                {
                state.output.fatal( "Incorrect value for parameter. It should be >= 2.", base.push( size ) );
                }
            }

        /*if( style == STYLE_WORLD_CUP )
          {
          groupSize = state.parameters.getInt( base.push( size ), null, 2 );
          if( groupSize < 2 )
          {
          state.output.fatal( "Incorrect value for parameter. It should be >= 2.", base.push( size ) );
          }
          numQualify = state.parameters.getInt( base.push( P_QUALIFY_PER_GROUP ), null, 1 );
          if( numQualify < 1 || numQualify >= groupSize )
          {
          state.output.fatal( "Incorrect value for parameter. It should be >= 1 and smaller than " +
          base.push( size ).toString(),
          base.push( P_QUALIFY_PER_GROUP ) );
          }
          }*/

        allowOverEvaluation = state.parameters.getBoolean( base.push( overEval ), null, false );

        }

    public boolean runComplete( final EvolutionState state )
        {
        return false;
        }

    public void randomizeOrder(final EvolutionState state, final Individual[] individuals)
        {
        // copy the inds into a new array, then dump them randomly into the
        // subpopulation again
        Individual[] queue = new Individual[individuals.length];
        int len = queue.length;
        System.arraycopy(individuals,0,queue,0,len);

        for(int x=len;x>0;x--)
            {
            int i = state.random[0].nextInt(x);
            individuals[x-1] = queue[i];
            // get rid of queue[i] by swapping the highest guy there and then
            // decreasing the highest value  :-)
            queue[i] = queue[x-1];
            }
        }

    /**
     * An evaluator that performs coevolutionary evaluation.  Like SimpleEvaluator,
     * it applies evolution pipelines, one per thread, to various subchunks of
     * a new population.
     */
    public void evaluatePopulation(final EvolutionState state)
        {
        int numinds[] = new int[state.evalthreads];
        int from[] = new int[state.evalthreads];
        
        for (int y=0;y<state.evalthreads;y++)
            {
            // figure numinds
            if (y<state.evalthreads-1) // not last one
                numinds[y] = state.population.subpops[0].individuals.length/
                    state.evalthreads;
            else
                numinds[y] = 
                    state.population.subpops[0].individuals.length/
                    state.evalthreads +
                    
                    (state.population.subpops[0].individuals.length -
                     (state.population.subpops[0].individuals.length /
                      state.evalthreads)
                     *state.evalthreads);
            // figure from
            from[y] = (state.population.subpops[0].individuals.length/
                       state.evalthreads) * y;
            }
        
        randomizeOrder( state, state.population.subpops[0].individuals );
        
        GroupedProblemForm prob = (GroupedProblemForm)(p_problem.clone());

        prob.preprocessPopulation(state,state.population);
        switch(style)
            {
            case STYLE_SINGLE_ELIMINATION:
                evalSingleElimination( state, state.population.subpops[0].individuals, prob);
                break;
                // case STYLE_DOUBLE_ELIMINATION:
                //      evalDoubleElimination( state, state.population.subpops[0].individuals, prob);
                //      break;
            case STYLE_ROUND_ROBIN:
                evalRoundRobin( state, from, numinds, state.population.subpops[0].individuals, prob );
                break;
            case STYLE_N_RANDOM_COMPETITORS_ONEWAY:
                evalNRandomOneWay( state, from, numinds, state.population.subpops[0].individuals, prob );
                break;
            case STYLE_N_RANDOM_COMPETITORS_TWOWAY:
                evalNRandomTwoWay( state, from, numinds, state.population.subpops[0].individuals, prob );
                break;
            case STYLE_PSEUDO_ROUND_ROBIN:
                evalPseudoRoundRobin( state, state.population.subpops[0].individuals, prob );
                break;
                //          case STYLE_WORLD_CUP:
                //              evalWorldCup( state, state.population.subpops[0].individuals, prob );
                //              break;
            }
    
        prob.postprocessPopulation(state, state.population);
        }
    
    public void evalSingleElimination( final EvolutionState state,
                                       final Individual[] individuals,
                                       final GroupedProblemForm prob )
        {

        // for a single-elimination tournament, the subpop[0] size must be 2^n for
        // some value n.  We don't check that here!  Check it in setup.
        
        // create the tournament array
        Individual[] tourn = new Individual[individuals.length];
        System.arraycopy( individuals, 0, tourn, 0, individuals.length );
        int len = tourn.length;
        Individual[] competition = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = updates[1] = true;

        // the "top half" of our array will be losers.
        // the bottom half will be winners.  Then we cut our array in half and repeat.
        while( len > 1 )
            {
            for(int x=0;x<len/2;x++)
                {
                competition[0] = tourn[x];
                competition[1] = tourn[len-x-1];

                prob.evaluate(state,competition,updates,true,0);
                }

            for(int x=0;x<len/2;x++)
                {
                // if the second individual is better, than we switch them around
                if( tourn[len-x-1].fitness.betterThan(tourn[x].fitness) )
                    {
                    Individual temp = tourn[x];
                    tourn[x] = tourn[len-x-1];
                    tourn[len-x-1] = temp;
                    }

                }

            // last part of the tournament: deal with odd values of len!
            if( len%2 != 0 )
                len = 1 + len/2;
            else
                len /= 2;
            }
        }


/*
  public void evalDoubleElimination( final EvolutionState state,
  final Individual[] individuals,
  final GroupedProblemForm prob )
  {

  // perform a single elimination tournament
  evalSingleElimination( state, individuals, prob );

  // if there are less than two individuals, there's no need to go any further
  if( individuals.length <= 2 )
  return;

  // get the winner of the single elimination tournament
  int index = 0;
  for( int i = 1 ; i < individuals.length ; i++ )
  if( individuals[i].fitness.betterThan(individuals[index].fitness) )
  index = i;

  // swap the winner on the last position in the array
  Individual ti = individuals[individuals.length-1];
  individuals[individuals.length-1] = individuals[index];
  individuals[index] = ti;

  // create an alternate array with all the individuals that lost some game in the first tournament
  Individual[] temp = new Individual[individuals.length-1];
  System.arraycopy(individuals,0,temp,0,temp.length);

  // here I should sort the individuals!
  QuickSort.qsort(temp, new IndComparator());

  // do another single elimination tournament with the losers
  evalSingleElimination( state, temp, prob );

  // compute the highest fitness of individuals in the second tournament
  float max = ((SimpleFitness)(temp[0].fitness)).fitness();
  for( int i = 1 ; i < temp.length ; i++ )
  if( max < ((SimpleFitness)(temp[i].fitness)).fitness() )
  max = ((SimpleFitness)(temp[i].fitness)).fitness();

  // the best individual should have the highest fitness, which we set to 1+max (just computed)
  ((SimpleFitness)(individuals[individuals.length-1].fitness)).setFitness( state, (float)(max+1), false );
  }
*/

/*
  public void evalWorldCup( final EvolutionState state,
  final Individual[] individuals,
  final GroupedProblemForm prob )
  {
  // the number of groups for the tournament
  int numGroups = 0;

  int count = 0;

  // the number of individuals that will qualify from groups to the final tournament
  int totalIndividualsQualified;

  Individual temp;

  // if there would be only one individual left in the last group, include him in the previous group
  if( individuals.length % groupSize <= 1 )
  {
  numGroups = individuals.length / groupSize;
  totalIndividualsQualified = numGroups * numQualify;
  }
  else
  {
  numGroups = individuals.length / groupSize + 1;
  totalIndividualsQualified = (numGroups-1)*numQualify + Math.min(individuals.length%groupSize,numQualify);
  }

  // the individuals for the final tournament
  Individual[] tournament = new Individual[ totalIndividualsQualified ];

  // perform the round robin tournament for each of the groups.
  // no round robin tournament is necessary for the last group if it contains
  // less individuals then the number to qualify
  for( int i = 0 ; i < individuals.length-1 ; i+=groupSize )
  {
  int last = Math.min( i+groupSize, individuals.length );
  // if there would be only one individual left after this tournament, include him here....
  if( last == individuals.length-1 )
  last = individuals.length;

  Individual[] inds = new Individual[last-i];
  System.arraycopy( individuals, i, inds, 0, last-i );
  evalRoundRobin( state, inds, prob );

  // here I should sort the individuals!
  QuickSort.qsort(inds, new IndComparator());

  for( int x = 0 ; x < Math.min(numQualify,last-i) ; x++ )
  tournament[(i/groupSize)*numQualify+x] = inds[x];
  }

  float max = individuals[0].fitness.fitness();
  for( int i = 1 ; i < individuals.length ; i++ )
  if( max < individuals[i].fitness.fitness() )
  max = individuals[i].fitness.fitness();

  // perform the single elimination tournament with the winners
  evalSingleElimination( state, tournament, prob );

  float min = tournament[0].fitness.fitness();
  for( int i = 1 ; i < tournament.length ; i++ )
  if( min > tournament[i].fitness.fitness() )
  min = tournament[i].fitness.fitness();

  for( int i = 0 ; i < tournament.length ; i++ )
  {
  ((SimpleFitness)(tournament[i].fitness)).setFitness( state, tournament[i].fitness.fitness() + max - min, false );
  }

  }
*/

    public void evalRoundRobin( final EvolutionState state,
                                int[] from, int[] numinds,
                                final Individual[] individuals,
                                final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalRoundRobinPopChunk(state,numinds[0],from[0],0,individuals,prob);
        else
            {
            Thread[] t = new Thread[state.evalthreads];
            
            // start up the threads
            for (int y=0;y<state.evalthreads;y++)
                {
                CompetitiveEvaluatorThread r = new RoundRobinCompetitiveEvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                }
            }
        
        }

    /**
     * A private helper function for evalutatePopulation which evaluates a chunk
     * of individuals in a subpopulation for a given thread.
     * 
     * Although this method is declared public (for the benefit of a private
     * helper class in this file), you should not call it.
     * 
     * @param state
     * @param numinds
     * @param from
     * @param threadnum
     * @param prob
     */
    public void evalRoundRobinPopChunk(final EvolutionState state,
                                       int from, int numinds, int threadnum, 
                                       final Individual[] individuals,
                                       final GroupedProblemForm prob)
        {
        Individual[] competition = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = updates[1] = true;
        int upperBound = from+numinds;
        
        // evaluate chunk of population against entire population
        // since an individual x will be evaluated against all 
        // other individuals <x in other threads, only evaluate it against
        // individuals >x in this thread.
        for(int x=from;x<upperBound;x++)
            for(int y=x+1;y<individuals.length;y++)
                {
                competition[0] = individuals[x];
                competition[1] = individuals[y];
                prob.evaluate(state,competition,updates,false,0);
                }
        }

    public void evalPseudoRoundRobin( final EvolutionState state,
                                      final Individual[] individuals,
                                      final GroupedProblemForm prob )
        {

        }
    
    public void evalPseudoRoundRobinPopChunk( final EvolutionState state,
                                              int from, int numinds, int threadnum,
                                              final Individual[] individuals,
                                              final GroupedProblemForm prob )
        {
        Individual[] competition = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = updates[1] = true;

        
        for( int i = 0 ; i < individuals.length-1 ; i+=groupSize )
            {
            int last = Math.min( i+groupSize, individuals.length );

            // if there would be only one individual left after this tournament, include him here....
            if( last == individuals.length-1 )
                last = individuals.length;

            for(int x=i;x<last;x++)
                for(int y=x+1;y<last;y++)
                    {
                    competition[0] = individuals[x];
                    competition[1] = individuals[y];
                    prob.evaluate(state,competition,updates,false,0);
                    }
            }
        }

    public void evalNRandomOneWay( final EvolutionState state, 
                                   int[] from, int[] numinds, 
                                   final Individual[] individuals,
                                   final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalNRandomOneWayPopChunk(state,from[0],numinds[0],0,individuals,prob);
        else
            {
            Thread[] t = new Thread[state.evalthreads];
            
            // start up the threads
            for (int y=0;y<state.evalthreads;y++)
                {
                CompetitiveEvaluatorThread r = new NRandomOneWayCompetitiveEvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                }
            }
        }
    
    public void evalNRandomOneWayPopChunk( final EvolutionState state,
                                           int from, int numinds, int threadnum,
                                           final Individual[] individuals,
                                           final GroupedProblemForm prob )
        {
        Individual[] queue = new Individual[individuals.length];
        int len = queue.length;
        System.arraycopy(individuals,0,queue,0,len);

        Individual[] competition = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = true;
        updates[1] = false;
        int upperBound = from+numinds;
        
        for(int x=from;x<upperBound;x++)
            {
            competition[0] = individuals[x];
            // fill up our tournament
            for(int y=0;y<groupSize;)
                {
                // swap to end and remove
                int index = state.random[0].nextInt(len-y);
                competition[1] = queue[index];
                queue[index] = queue[len-y-1];
                queue[len-y-1] = competition[1];
                // if the opponent is not the actual individual, we can
                // have a competition
                if( competition[1] != individuals[x] )
                    {
                    prob.evaluate(state,competition,updates,false,0);
                    y++;
                    }
                }
            }
        }

    public void evalNRandomTwoWay( final EvolutionState state,
                                   int[] from, int[] numinds,
                                   final Individual[] individuals,
                                   final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalNRandomTwoWayPopChunk(state,from[0],numinds[0],0,individuals,prob);
        else
            {
            Thread[] t = new Thread[state.evalthreads];
            
            // start up the threads
            for (int y=0;y<state.evalthreads;y++)
                {
                CompetitiveEvaluatorThread r = new NRandomTwoWayCompetitiveEvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                }
            }
        }
    
    public void evalNRandomTwoWayPopChunk( final EvolutionState state,
                                           int from, int numinds, int threadnum,
                                           final Individual[] individuals,
                                           final GroupedProblemForm prob )
        {

        // the number of games played for each player
        EncapsulatedIndividual[] individualsOrdered = new EncapsulatedIndividual[individuals.length];
        EncapsulatedIndividual[] queue = new EncapsulatedIndividual[individuals.length];
        for( int i = 0 ; i < individuals.length ; i++ )
            individualsOrdered[i] = new EncapsulatedIndividual( individuals[i], 0 );

        Individual[] competition = new Individual[2];
        boolean[] updates = new boolean[2];
        updates[0] = true;
        int upperBound = from+numinds;
        
        for(int x=from;x<upperBound;x++)
            {
            System.arraycopy(individualsOrdered,0,queue,0,queue.length);
            competition[0] = queue[x].ind;

            // if the rest of individuals is not enough to fill
            // all games remaining for the current individual
            // (meaning that the current individual has left a
            // lot of games to play versus players with index
            // greater than his own), then it should play with
            // all. In the end, we should check that he finished
            // all the games he needs. If he did, everything is
            // ok, otherwise he should play with some other players
            // with index smaller than his own, but all these games
            // will count only for his fitness evaluation, and
            // not for the opponents' (unless allowOverEvaluations is set to true)

            // if true, it means that he has to play against all opponents with greater index
            if( individuals.length - x - 1 <= groupSize - queue[x].nOpponentsMet )
                {
                for( int y = x+1 ; y < queue.length ; y++ )
                    {
                    competition[1] = queue[y].ind;
                    updates[1] = (queue[y].nOpponentsMet < groupSize) || allowOverEvaluation;
                    prob.evaluate( state, competition, updates, false, 0 );
                    queue[x].nOpponentsMet++;
                    if( updates[1] )
                        queue[y].nOpponentsMet++;
                    }
                }
            else // here he has to play against a selection of the opponents with greater index
                {
                // we can use the queue structure because we'll just rearrange the indexes
                // but we should make sure we also rearrange the other vectors referring to the individuals

                for( int y = 0 ; groupSize > queue[x].nOpponentsMet ; y++ )
                    {
                    // swap to the end and remove from list
                    int index = state.random[0].nextInt( queue.length - x - 1 - y )+x+1;
                    competition[1] = queue[index].ind;

                    updates[1] = (queue[index].nOpponentsMet < groupSize) || allowOverEvaluation;
                    prob.evaluate( state, competition, updates, false, 0 );
                    queue[x].nOpponentsMet++;
                    if( updates[1] )
                        queue[index].nOpponentsMet++;

                    // swap the players (such that a player will not be considered twice)
                    EncapsulatedIndividual temp = queue[index];
                    queue[index] = queue[queue.length - y - 1];
                    queue[queue.length - y - 1] = temp;

                    }

                }

            // if true, it means that the current player needs to play some games with other players with lower indexes.
            // this is an unfortunate situation, since all those players have already had their groupSize games for the evaluation
            if( queue[x].nOpponentsMet < groupSize )
                {
                for( int y = queue[x].nOpponentsMet ; y < groupSize ; y++ )
                    {
                    // select a random opponent with smaller index (don't even care for duplicates)
                    int index;
                    if( x > 0 ) // if x is 0, then there are no players with smaller index, therefore pick a random one
                        index = state.random[0].nextInt( x );
                    else
                        index = state.random[0].nextInt( queue.length-1 )+1;
                    // use the opponent for the evaluation
                    competition[1] = queue[index].ind;
                    updates[1] = (queue[index].nOpponentsMet < groupSize) || allowOverEvaluation;
                    prob.evaluate( state, competition, updates, false, 0 );
                    queue[x].nOpponentsMet++;
                    if( updates[1] )
                        queue[index].nOpponentsMet++;
                    
                    }
                }

            }
        }

    int nextPowerOfTwo( int N )
        {
        int i = 1;
        while( i < N )
            i *= 2;
        return i;
        }

    int whereToPutInformation;
    void fillPositions( int[] positions, int who, int totalPerDepth, int total )
        {
        if(totalPerDepth >= total - 1 )
            {
            positions[whereToPutInformation] = who;
            whereToPutInformation++;
            }
        else
            {
            fillPositions( positions, who, totalPerDepth*2+1, total );
            fillPositions( positions, totalPerDepth-who, totalPerDepth*2+1, total );
            }
        }

    }

// used by the K-Random-Opponents-One-Way and K-Random-Opponents-Two-Ways evaluations
class EncapsulatedIndividual
    {
    public Individual ind;
    public int nOpponentsMet;
    public EncapsulatedIndividual( Individual ind_, int value_ )
        {
        ind = ind_;
        nOpponentsMet = value_;
        }
    };

// used by the Single-Elimination-Tournament, (Double-Elimination-Tournament and World-Cup) evaluations
class IndividualAndVictories
    {
    public Individual ind;
    public int victories;
    public IndividualAndVictories( Individual ind_, int value_ )
        {
        ind = ind_;
        victories = value_;
        }
    };

class IndComparator implements SortComparator
    {
    public boolean lt(Object a, Object b)
        { return ((Individual)a).fitness.betterThan(((Individual)b).fitness); }
    public boolean gt(Object a, Object b)
        { return ((Individual)b).fitness.betterThan(((Individual)a).fitness); }
    }

abstract class CompetitiveEvaluatorThread implements Runnable
    {
    public int numinds;
    public int from;
    public CompetitiveEvaluator me;
    public EvolutionState state;
    public int threadnum;
    public GroupedProblemForm p;
    public Individual[] inds;
    }

class RoundRobinCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalRoundRobinPopChunk(state,from,numinds,threadnum,inds,p); }
    }

class NRandomOneWayCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalNRandomOneWayPopChunk(state,from,numinds,threadnum,inds,p); }
    }
class NRandomTwoWayCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalNRandomTwoWayPopChunk(state,from,numinds,threadnum,inds,p); }
    }
