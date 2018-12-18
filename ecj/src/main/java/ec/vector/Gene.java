/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.*;
import ec.util.*;
import java.io.*;


/*
 * Gene.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 * Revised to Gene.java from VectorGene.java:  Wed Oct 31 23:42:38 EDT 2012
 */

/**
 * Gene is an abstract superclass of objects which may be used in
 * the genome array of GeneVectorIndividuals.
 *

 * <p>In addition to serialization for checkpointing, Genes may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeGene(...,DataOutput)/readGene(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives a Gene in binary.  It is the most efficient approach to sending
 * Genes over networks, etc.  The default versions of writeGene/readGene throw errors.
 * You don't need to implement them if you don't plan on using read/writeGene.
 *
 * <li><b>printGene(...,PrintWriter)/readGene(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives a Gene in text encoded such that the Gene is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will typically encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readGene</b>
 * reads in a line, then calls <b>readGeneFromString</b> on that line.
 * You are responsible for implementing readGeneFromString: the Code class is there to help you.
 * The default version throws an error if called.
 * <b>printGene</b> calls <b>printGeneToString<b>
 * and printlns the resultant string. You are responsible for implementing the printGeneToString method in such
 * a way that readGeneFromString can read back in the Gene println'd with printGeneToString.  The default form
 * of printGeneToString() simply calls <b>toString()</b> 
 * by default.  You might override <b>printGeneToString()</b> to provide better information.   You are not required to implement these methods, but without
 * them you will not be able to write Genes to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printGeneForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints a Gene in a fashion intended for human consumption only.
 * <b>printGeneForHumans</b> calls <b>printGeneToStringForHumans()<b> 
 * and printlns the resultant string.  The default form of this method just returns the value of
 * <b>toString()</b>. You may wish to override this to provide more information instead. 
 * You should handle one of these methods properly
 * to ensure Genes can be printed by ECJ.
 * </ul>

 <p><b>Default Base</b><br>
 vector.gene

 * @author Sean Luke
 * @version 2.0
 */
 
public abstract class Gene implements Prototype
    {
    public static final String P_GENE = "gene";

    /** @deprecated */
    private static final String P_VECTOR_GENE = "vect-gene";

    public void setup(final EvolutionState state, final Parameter base)
        {
        // nothing by default
        }
        
    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_GENE);
        }
    
    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }
        


    /** Generates a hash code for this gene -- the rule for this is that the hash code
        must be the same for two genes that are equal to each other genetically. */
    public abstract int hashCode();
    
    /** Unlike the standard form for Java, this function should return true if this
        gene is "genetically identical" to the other gene. */
    public abstract boolean equals( final Object other );

    /**
       The reset method randomly reinitializes the gene.
    */
    public abstract void reset(final EvolutionState state, final int thread);

    /**
       Mutate the gene.  The default form just resets the gene.
    */
    public void mutate(final EvolutionState state, final int thread)
        {
        reset(state,thread);
        }

    /**
       Nice printing.  The default form simply calls printGeneToStringForHumans and prints the result, 
       but you might want to override this.
    */
    public void printGeneForHumans( final EvolutionState state, final int verbosity, final int log )
        {  state.output.println(printGeneToStringForHumans(),log); }

    /** Prints the gene to a string in a human-readable fashion.  The default simply calls toString(). */
    public String printGeneToStringForHumans()
        { return toString(); }

    /** Prints the gene to a string in a fashion readable by readGeneFromString and parseable by readGene(state, reader).
        Override this.  The default form returns toString(). */
    public String printGeneToString()
        { return toString(); }

    /** Reads a gene from a string, which may contain a final '\n'.
        Override this method.  The default form generates an error.
    */
    public void readGeneFromString(final String string, final EvolutionState state)
        { state.output.error("readGeneFromString(string,state) unimplemented in " + this.getClass()); }

    /**
       Prints the gene in a way that can be read by readGene().  The default form simply
       calls printGeneToString().   Override this gene to do custom writing to the log,
       or just override printGeneToString(...), which is probably easier to do.
    */
    public void printGene( final EvolutionState state, final int verbosity, final int log )
        { state.output.println(printGeneToString(),log); }

    /**
       Prints the gene in a way that can be read by readGene().  The default form simply
       calls printGeneToString(state).   Override this gene to do custom writing,
       or just override printGeneToString(...), which is probably easier to do.
    */
    public void printGene( final EvolutionState state, final PrintWriter writer )
        { writer.println(printGeneToString()); }

    /**
       Reads a gene printed by printGene(...).  The default form simply reads a line into
       a string, and then calls readGeneFromString() on that line.  Override this gene to do
       custom reading, or just override readGeneFromString(...), which is probably easier to do.
    */
    public void readGene(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        { readGeneFromString(reader.readLine(),state); }

    /** Override this if you need to write rules out to a binary stream */
    public void writeGene(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeGene(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }

    /** Override this if you need to read rules in from a binary stream */
    public void readGene(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        state.output.fatal("readGene(EvolutionState, DataInput) not implemented in " + this.getClass());
        }

    }
