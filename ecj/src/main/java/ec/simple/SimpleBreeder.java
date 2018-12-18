/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;
import ec.util.*;
import java.util.*;


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
 * to each thread to populate.  One array of BreedingSources is obtained
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
    /** An array[subpop] of the *fraction* of elites to keep for that subpopulation */
    public double[] eliteFrac;
    public boolean[] reevaluateElites;
    public boolean sequentialBreeding;
    public boolean clonePipelineAndPopulation;
    public Population backupPopulation = null;
    
    // This is a DOUBLE ARRAY of ARRAYLISTS of <INDIVIDUALS>
    // Individuals are stored here by the breed pop chunk methods, and afterwards
    // we coalesce them into the new population. 
    public ArrayList newIndividuals[/*subpop*/][/*thread*/];
        
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
            return (int) Math.max(Math.floor(state.population.subpops.get(subpopulation).individuals.size() * eliteFrac[subpopulation]), 1.0);  // AT LEAST 1 ELITE
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
        
    
    /** Returns the next subpopulation size.  By default this is the CURRENT subpopulation size,
        but it could be overridden if you have modified the current subpopulation size (as in NSGA-II) 
        but don't want the next subpopulation size to be affected.*/
    protected int nextSubpopulationSize(EvolutionState state, int subpop)
        {
        return state.population.subpops.get(subpop).individuals.size();
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

        int[] newSubpopSize = new int[state.population.subpops.size()];
        for(int i = 0; i < newSubpopSize.length; i++)
            {
            newSubpopSize[i] = nextSubpopulationSize(state, i);
            }

        // load elites into the front of newpop
        loadElites(state, newpop);

        // how many threads do we really need?  No more than the maximum number of individuals in any subpopulation
        int numThreads = 0;
        for(int x = 0; x < newpop.subpops.size(); x++)
            numThreads = Math.max(numThreads, newSubpopSize[x]);
        numThreads = Math.min(numThreads, state.breedthreads);
        if (numThreads < state.breedthreads)
            state.output.warnOnce("Largest subpopulation size (" + numThreads +") is smaller than number of breedthreads (" + state.breedthreads + "), so fewer breedthreads will be created.");
        
        newIndividuals = new ArrayList[state.population.subpops.size()][numThreads];
        for(int subpop = 0; subpop < state.population.subpops.size(); subpop++)
            for(int thread = 0; thread < numThreads; thread++)
                newIndividuals[subpop][thread] = new ArrayList<Individual>();
        
        int numinds[][] = new int[numThreads][newpop.subpops.size()];
        int from[][] = new int[numThreads][newpop.subpops.size()];
                
        // determine numinds and from
        for(int x = 0; x< newpop.subpops.size(); x++)
            {
            newIndividuals[x] = new ArrayList[numThreads];
            for(int i = 0; i < numThreads; i++) 
                newIndividuals[x][i] = new ArrayList<Individual>();

            if (!shouldBreedSubpop(state, x, 0))
                {
                // just copy over
                newpop.subpops.get(x).individuals.clear();  // get rid of elites, we're just copying everyone
                newpop.subpops.get(x).individuals.addAll(state.population.subpops.get(x).individuals);
                continue;
                }
            else
                {
                int numElites = numElites(state, x);
                int length = newSubpopSize[x] - numElites;

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

                    from[y][x] = currentFrom;
                    currentFrom += numinds[y][x];

                    if (numinds[y][x] == 0)
                        {
                        state.output.warnOnce("More threads exist than can be used to breed some subpopulations (first example: subpopulation " + x + ")");
                        }
                    }
                }
            }

        // spawn threads

        if (numThreads==1)
            {
            breedPopChunk(newpop,state,numinds[0],from[0],0);
            }
        else
            {
            // start up the threads
            for(int y=0;y<numThreads;y++)
                {
                SimpleBreederThread r = new SimpleBreederThread();
                r.threadnum = y;
                r.newpop = newpop;
                r.from = from[y];
                r.numinds = numinds[y];
                r.me = this;
                r.state = state;
                pool.start(r, "ECJ Breeding Thread " + y );
                }
                        
            pool.joinAll();
            }

        // Coalesce
        for(int subpop = 0; subpop < newpop.subpops.size(); subpop++)
            {
            ArrayList<Individual> newpopindividuals = newpop.subpops.get(subpop).individuals;
            for(int thread = 0; thread < numThreads; thread++)
                {
                newpopindividuals.addAll(newIndividuals[subpop][thread]);
                }
            }

        postProcess(state);

        return newpop;
        }

    /** A hook to do final modifications as necessary to the population after breeding has concluded.
        By default this does nothing, but multiobjective breeders, which temporarily replace the old
        population while it's being breeded, may use this to put the popuplation back again just in
        case statistics needs it.
    */
    public void postProcess(EvolutionState state) { }

    /** Returns true if we're doing sequential breeding and it's the subpopulation's turn (round robin,
        one subpopulation per generation).*/
    public boolean shouldBreedSubpop(EvolutionState state, int subpop, int threadnum)
        {
        return (!sequentialBreeding || (state.generation % state.population.subpops.size()) == subpop);
        }

    /** A private helper function for breedPopulation which breeds a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */
        
    //// NOTE that from at this point is only used by the spatial breeder to determine its position in space.

    protected void breedPopChunk(Population newpop, EvolutionState state, int[] numinds, int[] from, int threadnum) 
        {
        for(int subpop = 0; subpop< newpop.subpops.size(); subpop++)
            {
            ArrayList<Individual> putHere = (ArrayList<Individual>)newIndividuals[subpop][threadnum];

            // do regular breeding of this subpopulation
            BreedingSource bp = null;
            if (clonePipelineAndPopulation)
                bp = (BreedingSource) newpop.subpops.get(subpop).species.pipe_prototype.clone();
            else
                bp = (BreedingSource) newpop.subpops.get(subpop).species.pipe_prototype;
            bp.fillStubs(state, null);
                                                                        
            // check to make sure that the breeding pipeline produces
            // the right kind of individuals.  Don't want a mistake there! :-)
            if (!bp.produces(state,newpop,subpop,threadnum))
                state.output.fatal("The Breeding Source of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops.get(subpop).species.getClass().getName() + " or fitness " + newpop.subpops.get(subpop).species.f_prototype );
            bp.prepareToProduce(state,subpop,threadnum);
                                                                        
            // start breedin'!
                                                                        
            int x = 0;
            while(x<numinds[subpop])
                x += bp.produce(1,numinds[subpop]-x,subpop,
                    putHere,
                    state,threadnum, newpop.subpops.get(subpop).species.buildMisc(state, subpop, threadnum));
            if (x>numinds[subpop]) // uh oh!  Someone blew it!
                state.output.fatal("Whoa!  A breeding source overwrote the space of another source in subpopulation " + subpop + ".  You need to check your breeding pipeline code (in produce() ).");

            bp.finishProducing(state,subpop,threadnum);
            }
        }
        
    protected void breedPopChunkProduce(int position)
        {
        }
    
    static class EliteComparator implements SortComparatorL
        {
        ArrayList<Individual> inds;
        public EliteComparator(ArrayList<Individual> inds) {super(); this.inds = inds;}
        public boolean lt(long a, long b)
            { return inds.get((int)b).fitness.betterThan(inds.get((int)a).fitness); }
        public boolean gt(long a, long b)
            { return inds.get((int)a).fitness.betterThan(inds.get((int)b).fitness); }
        }

    protected void unmarkElitesEvaluated(EvolutionState state, Population newpop)
        {
        for(int sub = 0; sub< newpop.subpops.size(); sub++)
            {
            if (!shouldBreedSubpop(state, sub, 0))
                continue;
            for(int e=0; e < numElites(state, sub); e++)
                {
                int len = newpop.subpops.get(sub).individuals.size();
                if (reevaluateElites[sub])
                    newpop.subpops.get(sub).individuals.get(len - e - 1).evaluated = false;
                }
            }
        }

    /** A protected helper function for breedPopulation which loads elites into a subpopulation. */
    protected void loadElites(EvolutionState state, Population newpop)
        {
        // are our elites small enough?
        for(int x = 0; x< state.population.subpops.size(); x++)
            {
            if (numElites(state, x)> state.population.subpops.get(x).individuals.size())
                state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            if (numElites(state, x)== state.population.subpops.get(x).individuals.size())
                state.output.warning("The number of elites for subpopulation " + x + " is the actual size of the subpopulation", 
                    new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
            }
        state.output.exitIfErrors();

        // we assume that we're only grabbing a small number (say <10%), so
        // it's not being done multithreaded
        for(int sub = 0; sub< state.population.subpops.size(); sub++)
            {
            if (!shouldBreedSubpop(state, sub, 0))  // don't load the elites for this one, we're not doing breeding of it
                {
                continue;
                }
                        
            // if the number of elites is 1, then we handle this by just finding the best one.
            if (numElites(state, sub)==1)
                {
                int best = 0;
                ArrayList<Individual> oldinds = state.population.subpops.get(sub).individuals;
                for(int x=1;x<oldinds.size();x++)
                    if (oldinds.get(x).fitness.betterThan(oldinds.get(best).fitness))
                        best = x;
                ArrayList<Individual> inds = newpop.subpops.get(sub).individuals;
                inds.add((Individual)(oldinds.get(best).clone()));
                }
            else if (numElites(state, sub)>0)  // we'll need to sort
                {
                int[] orderedPop = new int[state.population.subpops.get(sub).individuals.size()];
                for(int x = 0; x< state.population.subpops.get(sub).individuals.size(); x++) orderedPop[x] = x;

                // sort the best so far where "<" means "not as fit as"
                QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops.get(sub).individuals));
                // load the top N individuals

                ArrayList<Individual> inds = newpop.subpops.get(sub).individuals;
                ArrayList<Individual> oldinds = state.population.subpops.get(sub).individuals;
                for(int x=oldinds.size()-numElites(state, sub);x<oldinds.size();x++)
                    inds.add((Individual)(oldinds.get(orderedPop[x]).clone()));
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
