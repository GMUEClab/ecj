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
 * <p>Prior to breeding a subpopulation, a SimpleBreeder may first fill part of the new
 * subpopulation up with the best <i>n</i> individuals from the old subpopulation.
 * By default, <i>n</i> is 0 for each subpopulation (that is, this "elitism"
 * is not done).  The elitist step is performed by a single thread.
 *
 * <p>If the <i>sequential</i> parameter below is true, then breeding is done specially:
 * instead of breeding all Subpopulations each generation, we only breed one each generation.
 * The subpopulation index to breed is determined by taking the generation number, modulo the
 * total number of subpopulations.  Use of this parameter outside of a coevolutionary context
 * (see ec.coevolve.MultiPopCoevolutionaryEvaluator) is very rare indeed.
 *
 * <p>SimpleBreeder adheres to the default-subpop parameter in Population: if either an 'elite'
 * or 'reevaluate-elites' parameter is missing, it will use the default subpopulation's value
 * and signal a warning.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt><i>base</i>.elite.<i>i</i></tt><br>
 <font size=-1>int >= 0 (default=0)</font></td>
 <td valign=top>(the number of elitist individuals for subpopulation <i>i</i>)</td></tr>
 <tr><td valign=top><tt><i>base</i>.reevaluate-elites.<i>i</i></tt><br>
 <font size=-1>boolean (default = false)</font></td>
 <td valign=top>(should we reevaluate the elites of subpopulation <i>i</i> each generation?)</td></tr>
 <tr><td valign=top><tt><i>base</i>.sequential</tt><br>
 <font size=-1>boolean (default = false)</font></td>
 <td valign=top>(should we breed just one subpopulation each generation (as opposed to all of them)?)</td></tr>
 </table>
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleBreeder extends Breeder
    {
    public static final String P_ELITE = "elite";
    public static final String P_ELITE_FRAC = "elite-fraction";
    public static final String P_REEVALUATE_ELITES = "reevaluate-elites";
    public static final String P_SEQUENTIAL_BREEDING = "sequential";
    public static final String P_CLONE_PIPELINE_AND_POPULATION = "clone-pipeline-and-population";
    /** An array[subpop] of the number of elites to keep for that subpopulation */
    public int[] elite;
    public double[] eliteFrac;
    public boolean[] reevaluateElites;
    public boolean sequentialBreeding;
    public boolean clonePipelineAndPopulation;
    public Population backupPopulation = null;
        
    public static final int NOT_SET = -1;
    
    public ThreadPool pool = new ThreadPool();

    public boolean usingElitism(int subpopulation)
        {
        return (elite[subpopulation] > 0 ) || (eliteFrac[subpopulation] > 0);
        }
                
    public int numElites(EvolutionState state, int subpopulation)
        {
        if (elite[subpopulation] != NOT_SET)
            {
            return elite[subpopulation];
            }
        else if (eliteFrac[subpopulation] == 0)
            {
            return 0; // no elites
            }
        else if (eliteFrac[subpopulation] != NOT_SET)
            {
            return (int) Math.max(Math.floor(state.population.subpops[subpopulation].individuals.length * eliteFrac[subpopulation]), 1.0);  // AT LEAST 1 ELITE
            }
        else 
            {
            state.output.warnOnce("Elitism error (SimpleBreeder).  This shouldn't be able to happen.  Please report.");
            return 0;  // this shouldn't happen
            }
        }
    
    public void setup(final EvolutionState state, final Parameter base) 
        {
        Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        int size = state.parameters.getInt(p,null,1);  // if size is wrong, we'll let Population complain about it -- for us, we'll just make 0-sized arrays and drop out.

        eliteFrac = new double[size];
        elite = new int[size];
        for(int i = 0; i < size; i++) 
            eliteFrac[i] = elite[i] = NOT_SET;
        reevaluateElites = new boolean[size];
                
        sequentialBreeding = state.parameters.getBoolean(base.push(P_SEQUENTIAL_BREEDING), null, false);
        if (sequentialBreeding && (size == 1)) // uh oh, this can't be right
            state.output.fatal("The Breeder is breeding sequentially, but you have only one population.", base.push(P_SEQUENTIAL_BREEDING));

        clonePipelineAndPopulation =state.parameters.getBoolean(base.push(P_CLONE_PIPELINE_AND_POPULATION), null, true);
        if (!clonePipelineAndPopulation && (state.breedthreads > 1)) // uh oh, this can't be right
            state.output.fatal("The Breeder is not cloning its pipeline and population, but you have more than one thread.", base.push(P_CLONE_PIPELINE_AND_POPULATION));

        int defaultSubpop = state.parameters.getInt(new Parameter(Initializer.P_POP).push(Population.P_DEFAULT_SUBPOP), null, 0);
        for(int x=0;x<size;x++)
            {
            // get elites
            if (state.parameters.exists(base.push(P_ELITE).push(""+x),null))
                {
                if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+x),null))
                    state.output.error("Both elite and elite-frac specified for subpouplation " + x + ".", base.push(P_ELITE_FRAC).push(""+x), base.push(P_ELITE_FRAC).push(""+x));
                else 
                    {
                    elite[x] = state.parameters.getIntWithDefault(base.push(P_ELITE).push(""+x),null,0);
                    if (elite[x] < 0)
                        state.output.error("Elites for subpopulation " + x + " must be an integer >= 0", base.push(P_ELITE).push(""+x));
                    }
                }
            else if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+x),null))
                {
                eliteFrac[x] = state.parameters.getDoubleWithMax(base.push(P_ELITE_FRAC).push(""+x),null,0.0, 1.0);
                if (eliteFrac[x] < 0.0)
                    state.output.error("Elite Fraction of subpopulation " + x + " must be a real value between 0.0 and 1.0 inclusive", base.push(P_ELITE_FRAC).push(""+x));
                }
            else if (defaultSubpop >= 0)
                {
                if (state.parameters.exists(base.push(P_ELITE).push(""+defaultSubpop),null))
                    {
                    elite[x] = state.parameters.getIntWithDefault(base.push(P_ELITE).push(""+defaultSubpop),null,0);
                    if (elite[x] < 0)
                        state.output.warning("Invalid default subpopulation elite value.");  // we'll fail later
                    }
                else if (state.parameters.exists(base.push(P_ELITE_FRAC).push(""+defaultSubpop),null))
                    {
                    eliteFrac[x] = state.parameters.getDoubleWithMax(base.push(P_ELITE_FRAC).push(""+defaultSubpop),null,0.0, 1.0);
                    if (eliteFrac[x] < 0.0)
                        state.output.warning("Invalid default subpopulation elite-frac value.");  // we'll fail later
                    }
                else  // elitism is 0
                    {
                    elite[x] = 0;
                    }
                }
            else // elitism is 0
                {
                elite[x] = 0;
                }
                                        
            // get reevaluation
            if (defaultSubpop >= 0 && !state.parameters.exists(base.push(P_REEVALUATE_ELITES).push(""+x),null))
                {
                reevaluateElites[x] = state.parameters.getBoolean(base.push(P_REEVALUATE_ELITES).push(""+defaultSubpop), null, false);
                if (reevaluateElites[x])
                    state.output.warning("Elite reevaluation not specified for subpopulation " + x + ".  Using values for default subpopulation " + defaultSubpop + ": " + reevaluateElites[x]);
                }
            else
                {
                reevaluateElites[x] = state.parameters.getBoolean(base.push(P_REEVALUATE_ELITES).push(""+x), null, false);
                }
            }

        state.output.exitIfErrors();
        }

    /** Elites are often stored in the top part of the subpopulation; this function returns what
        part of the subpopulation contains individuals to replace with newly-bred ones
        (up to but not including the elites). */
    public int computeSubpopulationLength(EvolutionState state, Population newpop, int subpopulation, int threadnum)
        {
        if (!shouldBreedSubpop(state, subpopulation, threadnum))
            return newpop.subpops[subpopulation].individuals.length;  // we're not breeding the population, just copy over the whole thing
        return newpop.subpops[subpopulation].individuals.length - numElites(state, subpopulation); // we're breeding population, so elitism may have happened 
        }

    /** A simple breeder that doesn't attempt to do any cross-
        population breeding.  Basically it applies pipelines,
        one per thread, to various subchunks of a new population. */
    public Population breedPopulation(EvolutionState state) 
        {
        Population newpop = null;
        if (clonePipelineAndPopulation)
            newpop = (Population) state.population.emptyClone();
        else
            {
            if (backupPopulation == null)
                backupPopulation = (Population) state.population.emptyClone();
            newpop = backupPopulation;
            newpop.clear();
            backupPopulation = state.population;  // swap in
            }
        
        // load elites into top of newpop
        loadElites(state, newpop);


        // how many threads do we really need?  No more than the maximum number of individuals in any subpopulation
        int numThreads = 0;
        for(int x = 0; x < state.population.subpops.length; x++)
            numThreads = Math.max(numThreads, state.population.subpops[x].individuals.length);
        numThreads = Math.min(numThreads, state.breedthreads);
        if (numThreads < state.breedthreads)
            state.output.warnOnce("Largest subpopulation size (" + numThreads +") is smaller than number of breedthreads (" + state.breedthreads +
                "), so fewer breedthreads will be created.");
            
        int numinds[][] = 
            new int[numThreads][state.population.subpops.length];
        int from[][] = 
            new int[numThreads][state.population.subpops.length];
        
        for(int x=0;x<state.population.subpops.length;x++)
            {
            int length = computeSubpopulationLength(state, newpop, x, 0);

            // we will have some extra individuals.  We distribute these among the early subpopulations
            int individualsPerThread = length / numThreads;  // integer division
            int slop = length - numThreads * individualsPerThread;
            int currentFrom = 0;
                                
            for(int y=0;y<numThreads;y++)
                {
                if (slop > 0)
                    {
                    numinds[y][x] = individualsPerThread + 1;
                    slop--;
                    }
                else
                    numinds[y][x] = individualsPerThread;
                    
                if (numinds[y][x] == 0)
                    {
                    state.output.warnOnce("More threads exist than can be used to breed some subpopulations (first example: subpopulation " + x + ")");
                    }
                    
                from[y][x] = currentFrom;
                currentFrom += numinds[y][x];
                }
            }

/*
  for(int y=0;y<state.breedthreads;y++)
  for(int x=0;x<state.population.subpops.length;x++)
  {
  // the number of individuals we need to breed
  int length = computeSubpopulationLength(state, newpop, x, 0);
  // the size of each breeding chunk except the last one
  int firstBreedChunkSizes = length/state.breedthreads;
  // the size of the last breeding chunk
  int lastBreedChunkSize = 
  firstBreedChunkSizes + length - firstBreedChunkSizes * (state.breedthreads);
                
  // figure numinds
  if (y < state.breedthreads-1) // not the last one
  numinds[y][x] = firstBreedChunkSizes;
  else // the last one
  numinds[y][x] = lastBreedChunkSize;
                
  // figure from
  from[y][x] = (firstBreedChunkSizes * y);
  }
*/            
        if (numThreads==1)
            {
            breedPopChunk(newpop,state,numinds[0],from[0],0);
            }
        else
            {
            /*
              Thread[] t = new Thread[numThreads];
                
              // start up the threads
              for(int y=0;y<numThreads;y++)
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
              for(int y=0;y<numThreads;y++) 
              try
              {
              t[y].join();
              }
              catch(InterruptedException e)
              {
              state.output.fatal("Whoa! The main breeding thread got interrupted!  Dying...");
              }
            */


            // start up the threads
            for(int y=0;y<numThreads;y++)
                {
                SimpleBreederThread r = new SimpleBreederThread();
                r.threadnum = y;
                r.newpop = newpop;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                pool.start(r, "ECJ Breeding Thread " + y );
                }
                
            pool.joinAll();
            }
        return newpop;
        }

    /** Returns true if we're doing sequential breeding and it's the subpopulation's turn (round robin,
        one subpopulation per generation).*/
    public boolean shouldBreedSubpop(EvolutionState state, int subpop, int threadnum)
        {
        return (!sequentialBreeding || (state.generation % state.population.subpops.length) == subpop);
        }

    /** A private helper function for breedPopulation which breeds a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */

    protected void breedPopChunk(Population newpop, EvolutionState state,
        int[] numinds, int[] from, int threadnum) 
        {
        for(int subpop=0;subpop<newpop.subpops.length;subpop++)
            {
            // if it's subpop's turn and we're doing sequential breeding...
            if (!shouldBreedSubpop(state, subpop, threadnum))  
                {
                // instead of breeding, we should just copy forward this subpopulation.  We'll copy the part we're assigned
                for(int ind=from[subpop] ; ind < numinds[subpop] - from[subpop]; ind++)
                    // newpop.subpops[subpop].individuals[ind] = (Individual)(state.population.subpops[subpop].individuals[ind].clone());
                    // this could get dangerous
                    newpop.subpops[subpop].individuals[ind] = state.population.subpops[subpop].individuals[ind];
                }
            else
                {
                // do regular breeding of this subpopulation
                BreedingPipeline bp = null;
                if (clonePipelineAndPopulation)
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype.clone();
                else
                    bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype;
                                        
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
        }
    
    static class EliteComparator implements SortComparatorL
        {
        Individual[] inds;
        public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
        public boolean lt(long a, long b)
            { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
        public boolean gt(long a, long b)
            { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
        }

    protected void unmarkElitesEvaluated(EvolutionState state, Population newpop)
        {
        for(int sub=0;sub<newpop.subpops.length;sub++)
            {
            if (!shouldBreedSubpop(state, sub, 0))
                continue;
            for(int e=0; e < numElites(state, sub); e++)
                {
                int len = newpop.subpops[sub].individuals.length;
                if (reevaluateElites[sub])
                    newpop.subpops[sub].individuals[len - e - 1].evaluated = false;
                }
            }
        }

    /** A private helper function for breedPopulation which loads elites into
        a subpopulation. */

    protected void loadElites(EvolutionState state, Population newpop)
        {
        // are our elites small enough?
        for(int x=0;x<state.population.subpops.length;x++)
            {
            if (numElites(state, x)>state.population.subpops[x].individuals.length)
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            if (numElites(state, x)==state.population.subpops[x].individuals.length)
                state.output.warning("The number of elites for subpopulation " + x + " is the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            }
        state.output.exitIfErrors();

        // we assume that we're only grabbing a small number (say <10%), so
        // it's not being done multithreaded
        for(int sub=0;sub<state.population.subpops.length;sub++) 
            {
            if (!shouldBreedSubpop(state, sub, 0))  // don't load the elites for this one, we're not doing breeding of it
                {
                continue;
                }
                        
            // if the number of elites is 1, then we handle this by just finding the best one.
            if (numElites(state, sub)==1)
                {
                int best = 0;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=1;x<oldinds.length;x++)
                    if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                        best = x;
                Individual[] inds = newpop.subpops[sub].individuals;
                inds[inds.length-1] = (Individual)(oldinds[best].clone());
                }
            else if (numElites(state, sub)>0)  // we'll need to sort
                {
                int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
                for(int x=0;x<state.population.subpops[sub].individuals.length;x++) orderedPop[x] = x;

                // sort the best so far where "<" means "not as fit as"
                QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
                // load the top N individuals

                Individual[] inds = newpop.subpops[sub].individuals;
                Individual[] oldinds = state.population.subpops[sub].individuals;
                for(int x=inds.length-numElites(state, sub);x<inds.length;x++)
                    inds[x] = (Individual)(oldinds[orderedPop[x]].clone());
                }
            }
                
        // optionally force reevaluation
        unmarkElitesEvaluated(state, newpop);
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
