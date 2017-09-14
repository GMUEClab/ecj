/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.coevolve;
import java.util.ArrayList;

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

 <dt><b>K-Random-Opponents-One-Way</b><dd>
 Each individual's fitness is calculated based on K competitions against random opponents.
 For details, see "A Comparison of Two Competitive Fitness Functions" by Liviu Panait and
 Sean Luke in the Proceedings of GECCO 2002.

 <dt><b>K-Random-Opponents-Two-Way</b><dd>
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
 <i>rand-1-way</i> (K-Random-Opponents, each game counts for only one of the players)<br>
 <i>rand-2-way</i> (K-Random-Opponents, each game counts for both players)<br>
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>group-size</tt><br>
 <font size=-1> int</font></td>
 <td valign=top>(how many individuals per group, used in rand-1-way</i> and <i>rand-2-way</i> tournaments)<br>
 <i>group-size</i> &gt;= 1 for <i>rand-1-way</i> or <i>rand-2-way</i><br>
 </td></tr>

 <tr><td valign=top><i>base.</i><tt>over-eval</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(if the tournament style leads to an individual playing more games than others (as can be the case for rand-2-way),
 should the extra games be used for his fitness evaluatiuon?)</td></tr>

 </table>

 *
 * @author Sean Luke & Liviu Panait
 * @version 1.0 
 */


public class CompetitiveEvaluator extends Evaluator
    {
    public static final int STYLE_SINGLE_ELIMINATION=1;
    public static final int STYLE_ROUND_ROBIN=2;
    public static final int STYLE_N_RANDOM_COMPETITORS_ONEWAY=3;
    public static final int STYLE_N_RANDOM_COMPETITORS_TWOWAY=4;

    public static final String P_COMPETE_STYLE = "style";
    public int style;

    public static final String P_GROUP_SIZE = "group-size";
    public int groupSize;

    public static final String P_OVER_EVAL = "over-eval";
    public boolean allowOverEvaluation;

    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );
                                
        String temp;
        temp = state.parameters.getStringWithDefault( base.push( P_COMPETE_STYLE ), null, "" );
        if( temp.equalsIgnoreCase( "single-elim-tournament" ) )
            {
            style = STYLE_SINGLE_ELIMINATION;
            }
        else if( temp.equalsIgnoreCase( "round-robin" ) )
            {
            style = STYLE_ROUND_ROBIN;
            }
        else if( temp.equalsIgnoreCase( "rand-1-way" ) )
            {
            style = STYLE_N_RANDOM_COMPETITORS_ONEWAY;
            }
        else if( temp.equalsIgnoreCase( "rand-2-way" ) )
            {
            style = STYLE_N_RANDOM_COMPETITORS_TWOWAY;
            }
        else if (temp.equalsIgnoreCase( "rand-2-ways" ) )
            {
            state.output.fatal("'rand-2-ways' is no longer a valid style name: use 'rand-2-way'",
                base.push(P_COMPETE_STYLE), null);
            }
        else
            {
            state.output.fatal( "Incorrect value for parameter. Acceptable values: " +
                "single-elim-tournament, round-robin, rand-1-way, rand-2-way" , base.push( P_COMPETE_STYLE ) );
            }

        if( style == STYLE_N_RANDOM_COMPETITORS_ONEWAY || style == STYLE_N_RANDOM_COMPETITORS_TWOWAY )
            {
            groupSize = state.parameters.getInt( base.push( P_GROUP_SIZE ), null, 1 );
            if( groupSize < 1 )
                {
                state.output.fatal( "Incorrect value for parameter", base.push( P_GROUP_SIZE ) );
                }
            }
        allowOverEvaluation = state.parameters.getBoolean( base.push( P_OVER_EVAL ), null, false );
        }

    public String runComplete( final EvolutionState state )
        {
        return null;
        }

    public void randomizeOrder(final EvolutionState state, final ArrayList<Individual> individuals)
        {
        // copy the inds into a new array, then dump them randomly into the
        // subpopulation again
        Individual[] queue = new Individual[individuals.size()];
        int len = queue.length;
        individuals.toArray(queue);
        
        
        for(int x=len;x>0;x--)
            {
            int i = state.random[0].nextInt(x);
            individuals.set(x-1, queue[i]);
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
        boolean[] assessFitness = new boolean[state.population.subpops.size()];
        for(int i = 0; i < assessFitness.length; i++)
            assessFitness[i] = true;                                        // update everyone's fitness in preprocess and postprocess
        
        for (int y=0;y<state.evalthreads;y++)
            {
            // figure numinds
            if (y<state.evalthreads-1) // not last one
                numinds[y] = state.population.subpops.get(0).individuals.size()/
                    state.evalthreads;
            else
                numinds[y] = 
                    state.population.subpops.get(0).individuals.size()/
                    state.evalthreads +
                    
                    (state.population.subpops.get(0).individuals.size() -
                        (state.population.subpops.get(0).individuals.size() /
                        state.evalthreads)
                    *state.evalthreads);
            // figure from
            from[y] = (state.population.subpops.get(0).individuals.size()/
                state.evalthreads) * y;
            }
        
        randomizeOrder( state, state.population.subpops.get(0).individuals );
        
        GroupedProblemForm prob = (GroupedProblemForm)(p_problem.clone());

        prob.preprocessPopulation(state,state.population, assessFitness, style == STYLE_SINGLE_ELIMINATION);
                
        switch(style)
            {
            case STYLE_SINGLE_ELIMINATION:
                evalSingleElimination( state, state.population.subpops.get(0).individuals, 0, prob);
                break;
            case STYLE_ROUND_ROBIN:
                evalRoundRobin( state, from, numinds, state.population.subpops.get(0).individuals, 0, prob );
                break;
            case STYLE_N_RANDOM_COMPETITORS_ONEWAY:
                evalNRandomOneWay( state, from, numinds, state.population.subpops.get(0).individuals, 0, prob );
                break;
            case STYLE_N_RANDOM_COMPETITORS_TWOWAY:
                evalNRandomTwoWay( state, from, numinds, state.population.subpops.get(0).individuals, 0, prob );
                break;
            default:
                state.output.fatal("Invalid competition style in CompetitiveEvaluator.evaluatePopulation()");
            }
    
        state.incrementEvaluations(prob.postprocessPopulation(state, state.population, assessFitness, style == STYLE_SINGLE_ELIMINATION));
        }
    
    public void evalSingleElimination( final EvolutionState state,
        final ArrayList<Individual> individuals,
        final int subpop,
        final GroupedProblemForm prob )
        {
        // for a single-elimination tournament, the subpop[0] size must be 2^n for
        // some value n.  We don't check that here!  Check it in setup.
        
        // create the tournament array
        Individual[] tourn = individuals.toArray(new Individual[individuals.size()]);

        int len = tourn.length;
        Individual[] competition = new Individual[2];
        int[] subpops = new int[] { subpop, subpop };
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

                prob.evaluate(state,competition,updates,true,subpops, 0);
                }

            for(int x=0;x<len/2;x++)
                {
                // if the second individual is better, or coin flip if equal, than we switch them around
                if( tourn[len-x-1].fitness.betterThan(tourn[x].fitness) ||
                    (tourn[len-x-1].fitness.equivalentTo(tourn[x].fitness) && state.random[0].nextBoolean()))
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


    public void evalRoundRobin( final EvolutionState state,
        int[] from, int[] numinds,
        final ArrayList<Individual> individuals, int subpop,
        final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalRoundRobinPopChunk(state,from[0],numinds[0],0,individuals, subpop, prob);
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
                r.subpop = subpop;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++) 
                try { t[y].join(); }
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
        final ArrayList<Individual> individuals, int subpop,
        final GroupedProblemForm prob)
        {
        Individual[] competition = new Individual[2];
        int[] subpops = new int[] { subpop, subpop };
        boolean[] updates = new boolean[2];
        updates[0] = updates[1] = true;
        int upperBound = from+numinds;
        
        // evaluate chunk of population against entire population
        // since an individual x will be evaluated against all 
        // other individuals <x in other threads, only evaluate it against
        // individuals >x in this thread.
        for(int x=from;x<upperBound;x++)
            for(int y=x+1;y<individuals.size();y++)
                {
                competition[0] = individuals.get(x);
                competition[1] = individuals.get(y);
                prob.evaluate(state,competition,updates,false, subpops, 0);
                }
        }


    public void evalNRandomOneWay( final EvolutionState state, 
        int[] from, int[] numinds, 
        final ArrayList<Individual> individuals, int subpop, 
        final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalNRandomOneWayPopChunk(state,from[0],numinds[0],0,individuals, subpop, prob);
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
                r.subpop = subpop;
                r.me = this;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++) 
                try { t[y].join(); }
                catch(InterruptedException e)
                    {
                    state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                    }
            }
        }
    
    public void evalNRandomOneWayPopChunk( final EvolutionState state,
        int from, int numinds, int threadnum,
        final ArrayList<Individual> individuals,
        final int subpop,
        final GroupedProblemForm prob )
        {
        Individual[] queue = individuals.toArray(new Individual[individuals.size()]);
        int len = queue.length;
        

        Individual[] competition = new Individual[2];
        int subpops[] = new int[] { subpop, subpop };
        boolean[] updates = new boolean[2];
        updates[0] = true;
        updates[1] = false;
        int upperBound = from+numinds;
        
        for(int x=from;x<upperBound;x++)
            {
            competition[0] = individuals.get(x);
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
                if( competition[1] != individuals.get(x) )
                    {
                    prob.evaluate(state,competition,updates,false,subpops, 0);
                    y++;
                    }
                }
            }
        }

    public void evalNRandomTwoWay( final EvolutionState state,
        int[] from, int[] numinds,
        final ArrayList<Individual> individuals, int subpop, 
        final GroupedProblemForm prob )
        {
        if (state.evalthreads==1)
            evalNRandomTwoWayPopChunk(state,from[0],numinds[0],0,individuals, subpop, prob);
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
                r.subpop = subpop;
                r.state = state;
                r.p = prob;
                r.inds = individuals;
                t[y] = new Thread(r);
                t[y].start();
                }
            
            // gather the threads
            for (int y=0;y<state.evalthreads;y++)
                try { t[y].join(); }
                catch(InterruptedException e)
                    {
                    state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                    }
            }
        }
    
    public void evalNRandomTwoWayPopChunk( final EvolutionState state,
        int from, int numinds, int threadnum,
        final ArrayList<Individual> individuals,
        final int subpop,
        final GroupedProblemForm prob )
        {

        // the number of games played for each player
        EncapsulatedIndividual[] individualsOrdered = new EncapsulatedIndividual[individuals.size()];
        EncapsulatedIndividual[] queue = new EncapsulatedIndividual[individuals.size()];
        for( int i = 0 ; i < individuals.size() ; i++ )
            individualsOrdered[i] = new EncapsulatedIndividual( individuals.get(i), 0 );

        Individual[] competition = new Individual[2];
        int[] subpops = new int[] { subpop, subpop }; 
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
            if( individuals.size() - x - 1 <= groupSize - queue[x].nOpponentsMet )
                {
                for( int y = x+1 ; y < queue.length ; y++ )
                    {
                    competition[1] = queue[y].ind;
                    updates[1] = (queue[y].nOpponentsMet < groupSize) || allowOverEvaluation;
                    prob.evaluate( state, competition, updates, false, subpops, 0 );
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
                    prob.evaluate( state, competition, updates, false, subpops, 0 );
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
                    prob.evaluate( state, competition, updates, false, subpops, 0 );
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

/*
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
*/

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
    public int subpop;
    public ArrayList<Individual> inds;
    }

class RoundRobinCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalRoundRobinPopChunk(state,from,numinds,threadnum,inds, subpop, p); }
    }

class NRandomOneWayCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalNRandomOneWayPopChunk(state,from,numinds,threadnum,inds, subpop, p); }
    }
class NRandomTwoWayCompetitiveEvaluatorThread extends CompetitiveEvaluatorThread
    {
    public synchronized void run()
        { me.evalNRandomTwoWayPopChunk(state,from,numinds,threadnum,inds, subpop, p); }
    }
