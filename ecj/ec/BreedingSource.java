/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;

/* 
 * BreedingSource.java
 * 
 * Created: Thu Nov 18 17:40:26 1999
 * By: Sean Luke
 */

/**
 * A BreedingSource is a Prototype which 
 * provides Individuals to populate new populations based on
 * old ones.  The BreedingSource/BreedingPipeline/SelectionMethod mechanism
 * is inherently designed to work within single subpopulations, which is
 * by far the most common case.  If for some
 * reason you need to breed among different subpopulations to produce new ones
 * in a manner that can't be handled with exchanges, you will probably have to
 * write your own custom Breeder; you'd have to write your own custom breeding
 * pipelines anyway of course, though you can probably get away with reusing
 * the SelectionMethods.
 *
 * <p>A BreedingSource may have parent sources which feed it as well.
 * Some BreedingSources, <i>SelectionMethods</i>,
 * are meant solely to plug into other BreedingSources, <i>BreedingPipelines</i>.
 * BreedingPipelines can plug into other BreedingPipelines, and can also be
 * used to provide the final Individual meant to populate a new generation.
 *
 * <p>Think of BreedingSources as Streams of Individuals; at one end of the
 * stream is the provider, a SelectionMethod, which picks individuals from
 * the old population.  At the other end of the stream is a BreedingPipeline
 * which hands you the finished product, a small set of new Individuals
 * for you to use in populating your new population.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.prob</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0, or undefined</font></td>
 <td valign=top>(probability this BreedingSource gets chosen.  Undefined is only valid if the caller of this BreedingSource doesn't need a probability)</td></tr>
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class BreedingSource implements Prototype, RandomChoiceChooser
    {
    public static final String P_PROB = "prob";
    public static final float NO_PROBABILITY = -1.0f;
    public static final int UNUSED = -1;
    /** CheckBoundary is 8 */
    public static final int CHECKBOUNDARY = 8;
    public static final int DEFAULT_PRODUCED = 1;
    
    /** The probability that this BreedingSource will be chosen 
        to breed over other BreedingSources.  This may or may
        not be used, depending on what the caller to this BreedingSource is.
        It also might be modified by external sources owning this object,
        for their own purposes.  A BreedingSource should not use it for
        any purpose of its own, nor modify it except when setting it up.

        <p>The most common modification is to normalize it with some other
        set of probabilities, then set all of them up in increasing summation;
        this allows the use of the fast static BreedingSource-picking utility
        method, BreedingSource.pickRandom(...).  In order to use this method,
        for example, if four
        breeding source probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.
    */

    public float probability;

    /** Sets up the BreedingPipeline.  You can use state.output.error here
        because the top-level caller promises to call exitIfErrors() after calling
        setup.  Note that probability might get modified again by
        an external source if it doesn't normalize right. 

        <p>The most common modification is to normalize it with some other
        set of probabilities, then set all of them up in increasing summation;
        this allows the use of the fast static BreedingSource-picking utility
        method, BreedingSource.pickRandom(...).  In order to use this method,
        for example, if four
        breeding source probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}.


        @see Prototype#setup(EvolutionState,Parameter)
    */
    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        if (!state.parameters.exists(base.push(P_PROB),def.push(P_PROB)))
            probability = NO_PROBABILITY;
        else
            {
            probability = state.parameters.getFloat(base.push(P_PROB),def.push(P_PROB),0.0);
            if (probability<0.0) state.output.error("Breeding Source's probability must be a floating point value >= 0.0, or empty, which represents NO_PROBABILITY.",base.push(P_PROB),def.push(P_PROB));
            }
        }

    public final float getProbability(final Object obj)
        {
        return ((BreedingSource)obj).probability;
        }

    public final void setProbability(final Object obj, final float prob)
        {
        ((BreedingSource)obj).probability = prob;
        }


    /** Picks a random source from an array of sources, with their
        probabilities normalized and summed as follows:  For example,
        if four
        breeding source probabilities are {0.3, 0.2, 0.1, 0.4}, then
        they should get normalized and summed by the outside owners
        as: {0.3, 0.5, 0.6, 1.0}. */

    public static int pickRandom(final BreedingSource[] sources,final float prob)
        {
        return RandomChoice.pickFromDistribution(sources,sources[0],
                                                 prob,CHECKBOUNDARY);
        }

    /** Normalizes and arranges the probabilities in sources so that they
        are usable by pickRandom(...).  If the sources have all zero probabilities,
        then a uniform selection is used.  Negative probabilities will
        generate an ArithmeticException, as will an empty source array. */
    public static void setupProbabilities(final BreedingSource[] sources)
        {
        RandomChoice.organizeDistribution(sources,sources[0],true);
        }


    /** Returns the "typical" number of individuals
        generated with one call of produce(...). */
    public abstract int typicalIndsProduced();

    /** Returns true if this BreedingSource, when attached to the given
        subpopulation, will produce individuals of the subpopulation's species.
        SelectionMethods should additionally make sure that their Fitnesses are
        of a valid type, if necessary. newpop *may* be the same as state.population
    */

    public abstract boolean produces(final EvolutionState state,
                                     final Population newpop,
                                     final int subpopulation,
                                     int thread);

    /** Called before produce(...), usually once a generation, or maybe only
        once if you're doing steady-state evolution, to let the breeding source
        "warm up" prior to producing.  Individuals should be produced from
        old individuals in positions [start...start+length] in the subpopulation 
        only.  May be called again to reset the BreedingSource for a whole
        'nuther subpopulation. */

    public abstract void prepareToProduce(final EvolutionState state,
                                          final int subpopulation,
                                          final int thread);

    /** Called after produce(...), usually once a generation, or maybe only
        once if you're doing steady-state evolution (at the end of the run). */
        
    public abstract void finishProducing(final EvolutionState s,
                                         final int subpopulation,
                                         final int thread);

    /** Produces <i>n</i> individuals from the given subpopulation
        and puts them into inds[start...start+n-1],
        where n = Min(Max(q,min),max), where <i>q</i> is the "typical" number of 
        individuals the BreedingSource produces in one shot, and returns
        <i>n</i>.  max must be >= min, and min must be >= 1. For example, crossover
        might typically produce two individuals, tournament selection might typically
        produce a single individual, etc. */
    public abstract int produce(final int min, 
                                final int max, 
                                final int start,
                                final int subpopulation,
                                final Individual[] inds,
                                final EvolutionState state,
                                final int thread) ;        

    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }



    /** A hook which should be passed to all your subsidiary breeding
        sources.  If you are a BreedingPipeline and you
        implement your sources in a way different
        than using the sources[] array, be sure to override this method
        so that it calls preparePipeline(hook) on all of your sources.
        This method might get called more than once, and by various objects
        as needed.  If you use it, you should determine somehow how to use
        it to send information under the assumption that it might be sent
        by nested items in the pipeline; you don't want to scribble over
        each other's calls! Note that this method should travel *all*
        breeding source paths regardless of whether or not it's redundant to
        do so. */
    public abstract void preparePipeline(final Object hook);
    }
