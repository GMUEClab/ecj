/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec;
import java.util.*;
import java.io.*;
import ec.util.*;

/* 
 * Subpopulation.java
 * 
 * Created: Tue Aug 10 20:34:14 1999
 * By: Sean Luke
 */

/**
 * Subpopulation is a group which is basically an array of Individuals.
 * There is always one or more Subpopulations in the Population.  Each
 * Subpopulation has a Species, which governs the formation of the Individuals
 * in that Subpopulation.  Subpopulations also contain a Fitness prototype
 * which is cloned to form Fitness objects for individuals in the subpopulation.
 *
 * <p>An initial subpopulation is populated with new random individuals 
 * using the populate(...) method.  This method typically populates
 * by filling the array with individuals created using the Subpopulations' 
 * species' emptyClone() method, though you might override this to create
 * them with other means, by loading from text files for example.
 *
 * <p>In a multithreaded area of a run, Subpopulations should be considered
 * immutable.  That is, once they are created, they should not be modified,
 * nor anything they contain.  This protocol helps ensure read-safety under
 * multithreading race conditions.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(total number of individuals in the subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>species</tt><br>
 <font size=-1>classname, inherits and != ec.Species</font></td>
 <td valign=top>(the class of the subpopulations' Species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt><br>
 <font size=-1>classname, inherits and != ec.Fitness</font></td>
 <td valign=top>(the class for the prototypical Fitness for individuals in this subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>file</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(pathname of file from which the population is to be loaded.  If not defined, or empty, then the population will be initialized at random in the standard manner)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>duplicate-retries</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(during initialization, when we produce an individual which already exists in the subpopulation, the number of times we try to replace it with something unique.  Ignored if we're loading from a file.)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */


public class Subpopulation implements Group
    {
    /** A new subpopulation should be loaded from this file if it is non-null;
        otherwise they should be created at random.  */
    public File loadInds;

    /** The species for individuals in this subpopulation. */
    public Species species;

    /** The subpopulation's individuals. */
    public Individual[] individuals;

    /** Do we allow duplicates? */
    public int numDuplicateRetries;
    
    public static final String P_FILE = "file";
    public static final String P_SUBPOPSIZE = "size";  // parameter for number of subpops or pops
    public static final String P_SPECIES = "species";
    public static final String P_RETRIES = "duplicate-retries";

    public static final String NUM_INDIVIDUALS_PREAMBLE = "Number of Individuals: ";
    public static final String INDIVIDUAL_INDEX_PREAMBLE = "Individual Number: ";

    /** Returns an instance of Subpopulation just like it had been before it was
        populated with individuals. You may need to override this if you override
        Subpopulation.   <b>IMPORTANT NOTE</b>: if the size of the array in
        Subpopulation has been changed, then the clone will take on the new array
        size.  This helps some evolution strategies.
        @see Group#emptyClone()
    */
    
    public Group emptyClone()
        {
        try
            {
            Subpopulation p = (Subpopulation)clone();
            p.species = species;  // don't throw it away...maybe this is a bad idea...
            p.individuals = new Individual[individuals.length];  // empty
            return p;   
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // never happens
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        int size;

        // do we load from a file?
        loadInds = state.parameters.getFile(
            base.push(P_FILE),null);

        // what species do we use?

        species = (Species) state.parameters.getInstanceForParameter(
            base.push(P_SPECIES),null,
            Species.class);
        species.setup(state,base.push(P_SPECIES));

        // how big should our subpopulation be?
        
        size = state.parameters.getInt(
            base.push(P_SUBPOPSIZE),null,1);
        if (size<=0)
            state.output.fatal(
                "Subpopulation size must be an integer >= 1.\n",
                base.push(P_SUBPOPSIZE),null);
        
        // How often do we retry if we find a duplicate?
        numDuplicateRetries = state.parameters.getInt(
            base.push(P_RETRIES),null,0);
        if (numDuplicateRetries < 0) state.output.fatal(
            "The number of retries for duplicates must be an integer >= 0.\n",
            base.push(P_RETRIES),null);
        
        individuals = new Individual[size];
        }



    public void populate(EvolutionState state, int thread)
        {
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds!=null)
            {
            /*
            // let's make some individuals!
            try
            {
            LineNumberReader reader = new LineNumberReader(new FileReader(loadInds));
            for(int x=0;x<individuals.length;x++)
            individuals[x] = species.newIndividual(state,reader);
            state.output.message("Loading subpopulation from file " + loadInds);
            }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + loadInds + ".  The IOException was: \n" + e); }
            */
            try { readSubpopulation(state, new LineNumberReader(new FileReader(loadInds))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + loadInds + ".  The IOException was: \n" + e); }
            }
        else
            {
            Hashtable h = null;
            if (numDuplicateRetries >= 1)
                h = new Hashtable(individuals.length / 2);  // seems reasonable

            for(int x=0;x<individuals.length;x++) 
                {
                for(int tries=0; 
                    tries <= /* Yes, I see that*/ numDuplicateRetries; 
                    tries++)
                    {
                    individuals[x] = species.newIndividual(state, thread);

                    if (numDuplicateRetries >= 1)
                        {
                        // check for duplicates
                        Object o = h.get(individuals[x]);
                        if (o == null) // found nothing, we're safe
                            // hash it and go
                            {
                            h.put(individuals[x],individuals[x]);
                            break;
                            }
                        }
                    }  // oh well, we tried to cut down the duplicates
                }
            }
        }
        
    /** Prints an entire subpopulation in a form readable by humans. */
    public void printSubpopulationForHumans(final EvolutionState state,
                                            final int log, 
                                            final int verbosity)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + individuals.length, verbosity, log);
        for(int i = 0 ; i < individuals.length; i++)
            individuals[i].printIndividualForHumans(state, log, verbosity);
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). */
    public void printSubpopulation(final EvolutionState state,
                                   final int log, 
                                   final int verbosity)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.length), verbosity, log);
        for(int i = 0 ; i < individuals.length; i++)
            individuals[i].printIndividual(state, log, verbosity);
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). */
    public void printSubpopulation(final EvolutionState state,
                                   final PrintWriter writer)
        {
        writer.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.length));
        for(int i = 0 ; i < individuals.length; i++)
            individuals[i].printIndividual(state, writer);
        }
    
    /** Reads a subpopulation from the format generated by printSubpopulation(....).  If the number of individuals is not identical, the individuals array will
        be deleted and replaced with a new array, and a warning will be generated as individuals will have to be created using newIndividual(...) rather
        than readIndividual(...). */
    public void readSubpopulation(final EvolutionState state, 
                                  final LineNumberReader reader) throws IOException
        {
        // read in number of individuals and check to see if this appears to be a valid subpopulation
        int numIndividuals = Code.readIntegerWithPreamble(NUM_INDIVIDUALS_PREAMBLE, state, reader);

        // read in individuals
        if (numIndividuals != individuals.length)
            {
            state.output.warnOnce("On reading subpopulation from text stream, the subpopulation size was incorrect.\n" + 
                                  "Had to resize and use newIndividual() instead of readIndividual().");
            individuals = new Individual[numIndividuals];
            for(int i = 0 ; i < individuals.length; i++)
                {
                int j = Code.readIntegerWithPreamble(INDIVIDUAL_INDEX_PREAMBLE, state, reader);
                // sanity check
                if (j!=i) state.output.warnOnce("On reading subpopulation from text stream, some individual indexes in the subpopulation did not match.");
                individuals[i] = species.newIndividual(state, reader);
                }
            }
        else for(int i = 0 ; i < individuals.length; i++)
            {
            int j = Code.readIntegerWithPreamble(INDIVIDUAL_INDEX_PREAMBLE, state, reader);
            // sanity check
            if (j!=i) state.output.warnOnce("On reading subpopulation from text stream, some individual indexes in the subpopulation did not match.");
            individuals[i].readIndividual(state, reader);
            }
        }
        
    /** Writes a subpopulation in binary form, in a format readable by readSubpopulation(EvolutionState, DataInput). */
    public void writeSubpopulation(final EvolutionState state,
                                   final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(individuals.length);
        for(int i = 0 ; i < individuals.length; i++)
            individuals[i].writeIndividual(state, dataOutput);
        }
    
    /** Reads a subpopulation in binary form, from the format generated by writeSubpopulation(...).  If the number of individuals is not identical, the individuals array will
        be deleted and replaced with a new array, and a warning will be generated as individuals will have to be created using newIndividual(...) rather
        than readIndividual(...) */
    public void readSubpopulation(final EvolutionState state,
                                  final DataInput dataInput) throws IOException
        {
        int numIndividuals = dataInput.readInt();
        if (numIndividuals != individuals.length)
            {
            state.output.warnOnce("On reading subpopulation from binary stream, the subpopulation size was incorrect.\n" + 
                                  "Had to resize and use newIndividual() instead of readIndividual().");
            individuals = new Individual[numIndividuals];
            for(int i = 0 ; i < individuals.length; i++)
                individuals[i] = species.newIndividual(state, dataInput);
            }
        else for(int i = 0 ; i < individuals.length; i++)
            individuals[i].readIndividual(state, dataInput);
        }
    }
