/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.*;

/*
 * VectorIndividual.java
 * Created: Tue Mar 13 15:03:12 EST 2001
 */

/**
 * VectorIndividual is the abstract superclass of simple individual representations
 * which consist of vectors of values (booleans, integers, floating-point, etc.)
 *
 * <p>This class contains two methods, defaultCrossover and defaultMutate, which can
 * be overridden if all you need is a simple crossover and a simple mutate mechanism.
 * the VectorCrossoverPipeline and VectorMutationPipeline classes use these methods to do their
 * handiwork.  For more sophisticated crossover and mutation, you'll need to write
 * a custom breeding pipeline.
 *
 * <p>The <i>kind</i> of default crossover and mutation, and associated information,
 * is stored in the VectorIndividual's VectorSpecies object, which is obtained through
 * the <tt>species</tt> variable.  For example, 
 * VectorIndividual assumes three common types of crossover as defined in VectorSpecies
 * which you should implement in your defaultCrossover method: one-point, 
 * two-point, and any-point (otherwise known as "uniform") crossover.
 *
 * <p>VectorIndividual is typically used for fixed-length vector representations;
 * however, it can also be used with variable-length representations.  Two methods have
 * been provided in all subclasses of VectorIndividual to help you there: split and
 * join, which you can use to break up and reconnect VectorIndividuals in a variety
 * of ways.  Note that you may want to override the reset() method to create individuals
 * with different initial lengths.
 *
 * <p>VectorIndividuals must belong to the species VectorSpecies (or some subclass of it).
 *
 
 * <P><b>From ec.Individual:</b>
 *
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b> reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>

 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the gene array.  If you add instance variables to
 * a VectorIndividual or subclass, you'll need to read/write those variables as well.

 * @author Sean Luke
 * @version 1.0
 */

public abstract class VectorIndividual extends Individual
    {
    /** Destructively crosses over the individual with another in some default manner.  In most
        implementations provided in ECJ, one-, two-, and any-point crossover is done with a 
        for loop, rather than a possibly more efficient approach like arrayCopy().  The disadvantage
        is that arrayCopy() takes advantage of a CPU's bulk copying.  The advantage is that arrayCopy()
        would require a scratch array, so you'd be allocing and GCing an array for every crossover.
        Dunno which is more efficient.  */
    public void defaultCrossover(EvolutionState state, int thread, 
        VectorIndividual ind) { }

    /** Destructively mutates the individual in some default manner.  The default version calls reset()*/
    public void defaultMutate(EvolutionState state, int thread) { reset(state,thread); }

    /** Initializes the individual. */
    public abstract void reset(EvolutionState state, int thread);

    /** Returns the gene array.  If you know the type of the array, you can cast it and work on
        it directly.  Otherwise, you can still manipulate it in general, because arrays (like
        all objects) respond to clone() and can be manipulated with arrayCopy without bothering
        with their type.  This might be useful in creating special generalized crossover operators
        -- we apologize in advance for the fact that Java doesn't have a template system.  :-( 
        The default version returns null. */
    public Object getGenome() { return null; }
    
    /** Sets the gene array.  See getGenome().  The default version does nothing.
     */
    public void setGenome(Object gen) { }

    /** Returns the length of the gene array.  By default, this method returns 0. */
    public int genomeLength() { return 0; }

    /** Initializes the individual to a new size.  Only use this if you need to initialize variable-length individuals. */
    public void reset(EvolutionState state, int thread, int newSize)
        {
        setGenomeLength(newSize);
        reset(state, thread);
        }

    /** Sets the genome length.  If the length is longer, then it is filled with a default value (likely 0 or false).
        This may or may not be a valid value -- you will need to set appropriate values here. 
        The default implementation does nothing; but all subclasses in ECJ implement a subset of this. */
    public void setGenomeLength(int len) { }

    /** Splits the genome into n pieces, according to points, which *must* be sorted. 
        pieces.length must be 1 + points.length.  The default form does nothing -- be careful
        not to use this method if it's not implemented!  It should be trivial to implement it
        for your genome -- just like at the other implementations.  */
    public void split(int[] points, Object[] pieces) { }

    /** Joins the n pieces and sets the genome to their concatenation.  The default form does nothing. 
        It should be trivial to implement it
        for your genome -- just like at the other implementations.  */
    public void join(Object[] pieces) { }

    /** Clones the genes in pieces, and replaces the genes with their copies.  Does NOT copy the array, but modifies it in place.
        If the VectorIndividual holds numbers or booleans etc. instead of genes, nothing is cloned
        (why bother?). */
    public void cloneGenes(Object piece) { }  // default does nothing.
    
    public long size() { return genomeLength(); }
    }
