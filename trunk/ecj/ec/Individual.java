/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.Parameter;
import java.io.*;
import ec.util.*;

/*
 * Individual.java
 * Created: Tue Aug 10 19:58:13 1999
 */

/**
 * An Individual is an item in the EC population stew which is evaluated 
 * and assigned a fitness which determines its likelihood of selection.
 * Individuals are created most commonly by the newIndividual(...) method
 * of the ec.Species class.
 *
 * <P>In general Individuals are immutable.  That is, once they are created
 * their genetic material should not be modified.  This protocol helps insure that they are
 * safe to read under multithreaded conditions.  You can violate this protocol,
 * but try to do so when you know you have only have a single thread.
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
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b>reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>
 *
 * <p>Since individuals should be largely immutable, why is there a <b>readIndividual</b> method?
 * after all this method doesn't create a <i>new</i> individual -- it just erases the existing one.  This is
 * largely historical; but the method is used underneath by the various <b>newIndividual</b> methods in Species,
 * which <i>do</i> create new individuals read from files.  If you're trying to create a brand new individual
 * read from a file, look in Species.
 *
 * @author Sean Luke
 * @version 1.0
 */

public abstract class Individual implements Prototype
    {
    /** A reasonable parameter base element for individuals*/
    public static final String P_INDIVIDUAL = "individual";

    /** A string appropriate to put in front of whether or not the individual has been printed. */
    public static final String EVALUATED_PREAMBLE = "Evaluated: ";
    
    /** The fitness of the Individual. */
    public Fitness fitness;

    /** The species of the Individual.*/
    public Species species;
    
    /** Has the individual been evaluated and its fitness determined yet? */
    public boolean evaluated;

    public Object clone()
        {
        try 
            { 
            Individual myobj = (Individual) (super.clone());
            if (myobj.fitness != null) myobj.fitness = (Fitness)(fitness.clone());
            return myobj; 
            }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }

   
    /** Returns the "size" of the individual.  This is used for things like
        parsimony pressure.  The default form of this method returns 0 --
        if you care about parsimony pressure, you'll need to override the
        default to provide a more descriptive measure of size. */

    public long size() { return 0; }

    /** Returns true if I am genetically "equal" to ind.  This should
        mostly be interpreted as saying that we are of the same class
        and that we hold the same data. It should NOT be a pointer comparison. */
    public abstract boolean equals(Object ind);

    /** Returns a hashcode for the individual, such that individuals which
        are equals(...) each other always return the same
        hash code. */
    public abstract int hashCode();

    /** This should be used to set up only those things which you share in common
        with all other individuals in your species; individual-specific items
        which make you <i>you</i> should be filled in by Species.newIndividual(...),
        and modified by breeders. 
        @see Prototype#setup(EvolutionState,Parameter)
    */
    
    /** Overridden here because hashCode() is not expected to return the pointer
        to the object.  toString() normally uses hashCode() to print a unique identifier,
        and that's no longer the case.   You're welcome to override this anyway you 
        like to make the individual print out in a more lucid fashion. */
    public String toString()
        {
        return "" + this.getClass().getName() + "@" + 
            System.identityHashCode(this) + "{" + hashCode() + "}";
        }
        
    /** Print to a string the genotype of the Individual in a fashion readable by humans, and not intended
        to be parsed in again.  The fitness and evaluated flag should not be included.  The default form
        simply calls toString(), but you'll probably want to override this to something else. */
    public String genotypeToStringForHumans()
        {
        return toString();
        }
        
    /** Print to a string the genotype of the Individual in a fashion intended
        to be parsed in again via parseGenotype(...).
        The fitness and evaluated flag should not be included.  The default form
        simply calls toString(), which is almost certainly wrong, and you'll probably want to override
        this to something else. */
    public String genotypeToString()
        {
        return toString();
        }
              
    public void setup(final EvolutionState state, final Parameter base)
        {
        // does nothing by default.
        // So where is the species set?  The Species does so after it
        // loads me but before it calls setup on me.
        }

    /** Should print the individual out in a pleasing way for humans,
        including its
        fitness, using state.output.println(...,verbosity,log)
        You can get fitness to print itself at the appropriate time by calling 
        fitness.printFitnessForHumans(state,log,verbosity);
                
        <p>The default form of this method simply prints out whether or not the
        individual has been evaluated, its fitness, and then calls Individual.genotypeToStringForHumans().
        Feel free to override this to produce more sophisticated behavior, 
        though it is rare to need to -- instead you could just override genotypeToStringForHumans().
    */

    public void printIndividualForHumans(final EvolutionState state,
                                         final int log, 
                                         final int verbosity)
        {
        state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), 
                             verbosity, log);
        fitness.printFitnessForHumans(state,log,verbosity);
        state.output.println( genotypeToStringForHumans(), verbosity, log );
        }

    /** Should print the individual in a way that can be read by computer,
        including its fitness, using state.output.println(...,verbosity,log)
        You can get fitness to print itself at the appropriate time by calling 
        fitness.printFitness(state,log,verbosity);
                
        <p>The default form of this method simply prints out whether or not the
        individual has been evaluated, its fitness, and then calls Individual.genotypeToString().
        Feel free to override this to produce more sophisticated behavior, 
        though it is rare to need to -- instead you could just override genotypeToString().
    */

    public void printIndividual(final EvolutionState state,
                                final int log, 
                                final int verbosity)
        {
        state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), 
                             verbosity, log);
        fitness.printFitness(state,log,verbosity);
        state.output.println( genotypeToString(), verbosity, log );
        }

    /** Should print the individual in a way that can be read by computer,
        including its fitness.  You can get fitness to print itself at the
        appropriate time by calling fitness.printFitness(state,log,writer); 
        Usually you should try to use printIndividual(state,log,verbosity)
        instead -- use this method only if you can't print through the 
        Output facility for some reason.

        <p>The default form of this method simply prints out whether or not the
        individual has been evaluated, its fitness, and then calls Individual.genotypeToString().
        Feel free to override this to produce more sophisticated behavior, 
        though it is rare to need to -- instead you could just override genotypeToString().
    */

    public void printIndividual(final EvolutionState state,
                                final PrintWriter writer)
        {
        writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
        fitness.printFitness(state,writer);
        writer.println( genotypeToString() );
        }

    /** Reads in the individual from a form printed by printIndividual(), erasing the previous
        information stored in this Individual.  If you are trying to <i>create</i> an Individual
        from information read in from a stream or DataInput,
        see the various newIndividual() methods in Species. The default form of this method
        simply reads in evaluation information, then fitness information, and then 
        calls parseGenotype() (which you should implement).  The Species is not changed or
        attached, so you may need to do that elsewhere.  Feel free to override 
        this method to produce more sophisticated behavior, 
        though it is rare to need to -- instead you could just override parseGenotype(). */ 

    public void readIndividual(final EvolutionState state, 
                               final LineNumberReader reader)
        throws IOException
        {
        evaluated = Code.readBooleanWithPreamble(EVALUATED_PREAMBLE, state, reader);
        
        /*
        // First, was I evaluated?
        int linenumber = reader.getLineNumber();
        String s = reader.readLine();
        if (s==null || s.length() < EVALUATED_PREAMBLE.length()) // uh oh
        state.output.fatal("Reading Line " + linenumber + ": " +
        "Bad 'Evaluated?' line.");
        DecodeReturn d = new DecodeReturn(s, EVALUATED_PREAMBLE.length());
        Code.decode(d);
        if (d.type!=DecodeReturn.T_BOOLEAN)
        state.output.fatal("Reading Line " + linenumber + ": " +
        "Bad 'Evaluated?' line.");
        evaluated = (d.l!=0);
        */

        // Next, what's my fitness?
        fitness.readFitness(state,reader);

        // next, read me in
        parseGenotype(state, reader);
        }

    /** This method is used only by the default version of readIndividual(state,reader),
        and it is intended to be overridden to parse in that part of the individual that
        was outputted in the genotypeToString() method.  The default version of this method
        exits the program with an "unimplemented" error.  You'll want to override this method,
        or to override readIndividual(...) to not use this method. */
    protected void parseGenotype(final EvolutionState state,
                                 final LineNumberReader reader) throws IOException
        {
        state.output.fatal("parseGenotype(EvolutionState, LineNumberReader) not implemented in " + this.getClass());
        }
        
    /** Writes the binary form of an individual out to a DataOutput.  This is not for serialization:
        the object should only write out the data relevant to the object sufficient to rebuild it from a DataInput.
        The Species will be reattached later, and you should not write it.   The default version of this
        method writes the evaluated and fitness information, then calls writeGenotype() to write the genotype
        information.  Feel free to override this method to produce more sophisticated behavior, 
        though it is rare to need to -- instead you could just override writeGenotype(). 
    */
    public void writeIndividual(final EvolutionState state,
                                final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeBoolean(evaluated);
        fitness.writeFitness(state,dataOutput);
        writeGenotype(state,dataOutput);
        }
    
    
    /** Writes the genotypic information to a DataOutput.  Largely called by writeIndividual(), and
        nothing else.  The default simply throws an error.  Various subclasses of Individual override this as
        appropriate. For example, if your custom individual's genotype consists of an array of 
        integers, you might do this:

        * <pre><tt>
        * dataOutput.writeInt(integers.length);
        * for(int x=0;x<integers.length;x++)
        *     dataOutput.writeInt(integers[x]);
        * </tt></pre>
        */ 
    public void writeGenotype(final EvolutionState state,
                              final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeGenotype(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }

    /** Reads in the genotypic information from a DataInput, erasing the previous genotype
        of this Individual.  Largely called by readIndividual(), and nothing else.  
        If you are trying to <i>create</i> an Individual
        from information read in from a stream or DataInput,
        see the various newIndividual() methods in Species.
        The default simply throws an error.  Various subclasses of Individual override this as
        appropriate.  For example, if your custom individual's genotype consists of an array of 
        integers, you might do this:
        
        * <pre><tt>
        * integers = new int[dataInput.readInt()];
        * for(int x=0;x<integers.length;x++)
        *     integers[x] = dataInput.readInt();
        * </tt></pre>
        */

    public void readGenotype(final EvolutionState state,
                             final DataInput dataInput) throws IOException
        {
        state.output.fatal("readGenotype(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }

    /** Reads the binary form of an individual from a DataInput, erasing the previous
        information stored in this Individual.  This is not for serialization:
        the object should only read in the data written out via printIndividual(state,dataInput).  
        If you are trying to <i>create</i> an Individual
        from information read in from a stream or DataInput,
        see the various newIndividual() methods in Species. The default form of this method
        simply reads in evaluation information, then fitness information, and then 
        calls readGenotype() (which you will need to override -- its default form simply throws an error).
        The Species is not changed or attached, so you may need to do that elsewhere.  Feel free to override 
        this method to produce more sophisticated behavior, though it is rare to need to -- instead you could
        just override readGenotype().
    */
    public void readIndividual(final EvolutionState state,
                               final DataInput dataInput) throws IOException
        {
        evaluated = dataInput.readBoolean();
        fitness.readFitness(state,dataInput);
        readGenotype(state,dataInput);
        }
    
    }

