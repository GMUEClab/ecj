/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.Initializer;
import ec.Individual;
import ec.BreedingPipeline;
import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
import ec.util.*;

/* 
 * SimpleBreeder.java
 * 
 * Created: Tue Aug 10 21:00:11 1999
 * By: Sean Luke
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange,
 * and using a generational approach.  A SimpleBreeder may have multiple
 * threads; it divvys up a subpopulation into chunks and hands one chunk
 * to each thread to populate.  One array of BreedingPipelines is obtained
 * from a population's Species for each operating breeding thread.
 *
 * Prior to breeding a subpopulation, a SimpleBreeder may first fill part of the new
 * subpopulation up with the best <i>n</i> individuals from the old subpopulation.
 * By default, <i>n</i> is 0 for each subpopulation (that is, this "elitism"
 * is not done).  The elitist step is performed by a single thread.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.elite.<i>i</i></tt><br>
 <font size=-1>int >= 0 (default=0)</font></td>
 <td valign=top>(the number of elitist individuals for subpopulation <i>i</i>)</td></tr>
 </table>
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleBreeder extends Breeder
    {
    public static final String P_ELITE = "elite";
    /** An array[subpop] of the number of elites to keep for that subpopulation */
    public int[] elite;

    public void setup(final EvolutionState state, final Parameter base) 
        {
        Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        int size = state.parameters.getInt(p,null,1);  // if size is wrong, we'll let Population complain about it -- for us, we'll just make 0-sized arrays and drop out.

        elite = new int[size];

        for(int x=0;x<size;x++)
            {
            elite[x] = state.parameters.getIntWithDefault(base.push(P_ELITE).push(""+x),null,0);
            if (elite[x]<0) state.output.error("The number of elites for subpopulation " + x + " must be >= 0",base.push(P_ELITE).push(""+x));
            }

        state.output.exitIfErrors();
        }

    /** A simple breeder that doesn't attempt to do any cross-
        population breeding.  Basically it applies pipelines,
        one per thread, to various subchunks of a new population. */
    public Population breedPopulation(EvolutionState state) 
        {
        int numinds[][] = 
            new int[state.breedthreads][state.population.subpops.length];
        int from[][] = 
            new int[state.breedthreads][state.population.subpops.length];

        Population newpop = (Population) state.population.emptyClone();
        
        // are our elites small enough?
        for(int x=0;x<state.population.subpops.length;x++)
            if (elite[x]>state.population.subpops[x].individuals.length)
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
        state.output.exitIfErrors();

        // load elites into top of newpop
        loadElites(state, newpop);

        for(int y=0;y<state.breedthreads;y++)
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // the number of individuals we need to breed
                int length = state.population.subpops[x].individuals.length - elite[x];
                // the size of each breeding chunk except the last one
                int firstBreedChunkSizes = length/state.breedthreads;
                // the size of the last breeding chunk
                int lastBreedChunkSize = 
                    firstBreedChunkSizes + length - firstBreedChunkSizes * (state.breedthreads);
                
                //System.out.println("Sizes " + length + " " + firstBreedChunkSizes + " " + lastBreedChunkSize);
                
                // figure numinds
                if (y < state.breedthreads-1) // not the last one
                    numinds[y][x] = firstBreedChunkSizes;
                else // the last one
                    numinds[y][x] = lastBreedChunkSize;
                
                // figure from
                from[y][x] = (firstBreedChunkSizes * y);
                }
            
        if (state.breedthreads==1)
            {
            breedPopChunk(newpop,state,numinds[0],from[0],0);
            }
        else
            {
            Thread[] t = new Thread[state.breedthreads];
                
            // start up the threads
            for(int y=0;y<state.breedthreads;y++)
                {
                SimpleBreederThread r = new SimpleBreederThread();
                r.threadnum = y;
                r.newpop = newpop;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                t[y] = new Thread(r);
                t[y].start();
                }
                
            // gather the threads
            for(int y=0;y<state.breedthreads;y++) try
                {
                t[y].join();
                }
            catch(InterruptedException e)
                {
                state.output.fatal("Whoa! The main breeding thread got interrupted!  Dying...");
                }
            }
        return newpop;
        }


    /** A private helper function for breedPopulation which breeds a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    public void breedPopChunk(Population newpop, EvolutionState state,
                              int[] numinds, int[] from, int threadnum) 
        {
        //System.out.println("Breeding: " + numinds[0] + " Starting at: " + from[0]);
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            BreedingPipeline bp = (BreedingPipeline)newpop.subpops[subpop].
                species.pipe_prototype.clone();
                
            // check to make sure that the breeding pipeline produces
            // the right kind of individuals.  Don't want a mistake there! :-)
            int x;
            if (!bp.produces(state,newpop,subpop,threadnum))
                state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
            bp.prepareToProduce(state,subpop,threadnum);
                
            // start breedin'!
                
            x=from[subpop];
            int upperbound = from[subpop]+numinds[subpop];
            while(x<upperbound)
                x += bp.produce(1,upperbound-x,x,subpop,
                                newpop.subpops[subpop].individuals,
                                state,threadnum);
            if (x>upperbound) // uh oh!  Someone blew it!
                state.output.fatal("Whoa!  A breeding pipeline overwrote the space of another pipeline in subpopulation " + subpop + ".  You need to check your breeding pipeline code (in produce() ).");

            bp.finishProducing(state,subpop,threadnum);
            }
        }
    
    class EliteComparator implements SortComparatorL
        {
        Individual[] inds;
        public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
        public boolean lt(long a, long b)
            { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
        public boolean gt(long a, long b)
            { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
        }

    /** A private helper function for breedPopulation which loads elites into
        a subpopulation. */

    public void loadElites(EvolutionState state, Population newpop)
        {
        // we assume that we're only grabbing a small number (say <10%), so
        // it's not being done multithreaded
        for(int sub=0;sub<state.population.subpops.length;sub++) 
            // if the number of elites is 1, then we handle this by just finding the best one.
            if (elite[sub]==1)
                {
                int best = 0;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=1;x<oldinds.length;x++)
                    if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                        best = x;
                Individual[] inds = newpop.subpops[sub].individuals;
                inds[inds.length-1] = (Individual)(oldinds[best].clone());
                }
            else if (elite[sub]>0)  // we'll need to sort
                {
                int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
                for(int x=0;x<state.population.subpops[sub].individuals.length;x++) orderedPop[x] = x;

                // sort the best so far where "<" means "not as fit as"
                QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
                // load the top N individuals

                Individual[] inds = newpop.subpops[sub].individuals;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=inds.length-elite[sub];x<inds.length;x++)
                    inds[x] = (Individual)(oldinds[orderedPop[x]].clone());
                }
        }
    }


/** A private helper class for implementing multithreaded breeding */
class SimpleBreederThread implements Runnable
    {
    Population newpop;
    public int[] numinds;
    public int[] from;
    public SimpleBreeder me;
    public EvolutionState state;
    public int threadnum;
    public void run()
        {
        me.breedPopChunk(newpop,state,numinds,from,threadnum);
        }
    }
