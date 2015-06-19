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

 <p><b>Default Base</b><br>
 ec.subpop

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
    private static final long serialVersionUID = 1;

    /* A new subpopulation should be loaded from this resource name if it is non-null;
       otherwise they should be created at random.  */
    public boolean loadInds;
    public Parameter file;

    /** The species for individuals in this subpopulation. */
    public Species species;

    /** The subpopulation's individuals. */
    public Individual[] individuals;

    /** Do we allow duplicates? */
    public int numDuplicateRetries;
    
    /** What is our fill behavior beyond files? */
    public int extraBehavior;
    
    public static final String P_SUBPOPULATION = "subpop";
    public static final String P_FILE = "file";
    public static final String P_SUBPOPSIZE = "size";  // parameter for number of subpops or pops
    public static final String P_SPECIES = "species";
    public static final String P_RETRIES = "duplicate-retries";
    public static final String P_EXTRA_BEHAVIOR = "extra-behavior";
    public static final String V_TRUNCATE = "truncate";
    public static final String V_WRAP = "wrap";
    public static final String V_FILL = "fill";

    public static final String NUM_INDIVIDUALS_PREAMBLE = "Number of Individuals: ";
    public static final String INDIVIDUAL_INDEX_PREAMBLE = "Individual Number: ";

    public static final int TRUNCATE = 0;
    public static final int WRAP = 1;
    public static final int FILL = 2;
        
        
    public Parameter defaultBase()
        {
        return ECDefaults.base().push(P_SUBPOPULATION);
        }

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
        
    /** Resizes the Subpopulation to a new size.  If the size is smaller, then
        the Subpopulation is truncated such that the higher indexed individuals
        may be deleted.  If the size is larger, then the resulting Subpopulation will have
        null individuals (this almost certainly is not what you will want).
    */
    
    public void resize(int toThis)
        {
        Individual[] temp = new Individual[toThis];
        System.arraycopy(individuals, 0, temp, 0, toThis);
        individuals = temp;
        }


    /** Sets all Individuals in the Subpopulation to null, preparing it to be reused. */
    public void clear()
        {
        for(int i = 0 ; i < individuals.length; i++)
            individuals[i] = null;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        int size;

        // do we load from a file?
        file = base.push(P_FILE);
        loadInds = state.parameters.exists(file,null);
        
        
        // what species do we use?

        species = (Species) state.parameters.getInstanceForParameter(
            base.push(P_SPECIES),def.push(P_SPECIES),
            Species.class);
        species.setup(state,base.push(P_SPECIES));

        // how big should our subpopulation be?
        // Note that EvolutionState.setup() has similar code, so if you change this, change it there too.
        
        size = state.parameters.getInt(
            base.push(P_SUBPOPSIZE),def.push(P_SUBPOPSIZE),1);
        if (size<=0)
            state.output.fatal(
                "Subpopulation size must be an integer >= 1.\n",
                base.push(P_SUBPOPSIZE),def.push(P_SUBPOPSIZE));
        
        // How often do we retry if we find a duplicate?
        numDuplicateRetries = state.parameters.getInt(
            base.push(P_RETRIES),def.push(P_RETRIES),0);
        if (numDuplicateRetries < 0) state.output.fatal(
            "The number of retries for duplicates must be an integer >= 0.\n",
            base.push(P_RETRIES),def.push(P_RETRIES));
        
        individuals = new Individual[size];
        
        extraBehavior = TRUNCATE;
        if (loadInds)
            {
            String extra = state.parameters.getStringWithDefault(base.push(P_EXTRA_BEHAVIOR), def.push(P_EXTRA_BEHAVIOR), null);
                
            if (extra == null)  // uh oh
                state.output.warning("Subpopulation is reading from a file, but no " + P_EXTRA_BEHAVIOR + 
                    " provided.  By default, subpopulation will be truncated to fit the file size.");
            else if (extra.equalsIgnoreCase(V_TRUNCATE))
                extraBehavior=TRUNCATE;  // duh
            else if (extra.equalsIgnoreCase(V_FILL))
                extraBehavior=FILL;
            else if (extra.equalsIgnoreCase(V_WRAP))
                extraBehavior=WRAP;
            else state.output.fatal("Subpopulation given a bad " + P_EXTRA_BEHAVIOR + ": " + extra,
                base.push(P_EXTRA_BEHAVIOR),def.push(P_EXTRA_BEHAVIOR));
            }
        }



    public void populate(EvolutionState state, int thread)
        {
        int len = individuals.length;           // original length of individual array
        int start = 0;                                          // where to start filling new individuals in -- may get modified if we read some individuals in
        
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds)
            {
            InputStream stream = state.parameters.getResource(file,null);
            if (stream == null)
                state.output.fatal("Could not load subpopulation from file", file);
            
            try { readSubpopulation(state, new LineNumberReader(new InputStreamReader(stream))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + state.parameters.getString(file, null) + ".  The IOException was: \n" + e,
                    file, null); }
            
            if (len < individuals.length)
                {
                state.output.message("Old subpopulation was of size " + len + ", expanding to size " + individuals.length);
                return;
                }
            else if (len > individuals.length)   // the population was shrunk, there's more space yet
                {
                // What do we do with the remainder?
                if (extraBehavior == TRUNCATE)
                    {
                    state.output.message("Old subpopulation was of size " + len + ", truncating to size " + individuals.length);
                    return;  // we're done
                    }
                else if (extraBehavior == WRAP)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled with copies of the read-in individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                                
                    int count = 0;
                    for(int i = start; i < individuals.length; i++)
                        {
                        individuals[i] = (Individual)(individuals[count].clone());
                        if (++count >= start) count = 0;
                        }
                    return;
                    }
                else // if (extraBehavior == FILL)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled using randomly generated individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                    // now go on to fill the rest below...
                    }                       
                }
            else // exactly right number, we're dont
                {
                return;
                }
            }

        // populating the remainder with random individuals
        HashMap h = null;
        if (numDuplicateRetries >= 1)
            h = new HashMap((individuals.length - start) / 2);  // seems reasonable

        for(int x=start;x<individuals.length;x++) 
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
        
    /** Prints an entire subpopulation in a form readable by humans. 
        @deprecated Verbosity no longer has meaning
    */
    public final void printSubpopulationForHumans(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printSubpopulationForHumans(state, log);
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). 
        @deprecated Verbosity no longer has meaning
    */
    public final void printSubpopulation(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printSubpopulation(state, log);
        }
        
    /** Prints an entire subpopulation in a form readable by humans, with a verbosity of Output.V_NO_GENERAL. */
    boolean warned = false;
    public void printSubpopulationForHumans(final EvolutionState state,
        final int log)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + individuals.length, log);
        for(int i = 0 ; i < individuals.length; i++)
            {
            state.output.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i), log);
            if (individuals[i] != null)
                individuals[i].printIndividualForHumans(state, log);
            else if (!warned)
                {
                state.output.warnOnce("Null individuals found in subpopulation");
                warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                }
            }
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader) with a verbosity of Output.V_NO_GENERAL. */
    public void printSubpopulation(final EvolutionState state,
        final int log)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.length), log);
        for(int i = 0 ; i < individuals.length; i++)
            {
            state.output.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i), log);
            individuals[i].printIndividual(state, log);
            }
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). */
    public void printSubpopulation(final EvolutionState state,
        final PrintWriter writer)
        {
        writer.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.length));
        for(int i = 0 ; i < individuals.length; i++)
            {
            writer.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i));
            individuals[i].printIndividual(state, writer);
            }
        }
    
    /** Reads a subpopulation from the format generated by printSubpopulation(....).  If the number of individuals is not identical, the individuals array will
        be deleted and replaced with a new array, and a warning will be generated as individuals will have to be created using newIndividual(...) rather
        than readIndividual(...).  */
    public void readSubpopulation(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        // read in number of individuals and check to see if this appears to be a valid subpopulation
        int numIndividuals = Code.readIntegerWithPreamble(NUM_INDIVIDUALS_PREAMBLE, state, reader);

        if (numIndividuals < 1)
            state.output.fatal("On reading subpopulation from text stream, the subpopulation size must be >= 1.  The provided value was: " + numIndividuals + ".");

        // read in individuals
        if (numIndividuals != individuals.length)
            {
            state.output.warnOnce("On reading subpopulation from text stream, the current subpopulation size didn't match the number of individuals in the file.  " + 
                "The size of the subpopulation will be revised accordingly.  There were " + numIndividuals +
                " individuals in the file and " + individuals.length + " individuals expected for the subopulation.");
            individuals = new Individual[numIndividuals];
            for(int i = 0 ; i < individuals.length; i++)
                {
                int j = Code.readIntegerWithPreamble(INDIVIDUAL_INDEX_PREAMBLE, state, reader);
                // sanity check
                if (j!=i) state.output.warnOnce("On reading subpopulation from text stream, some individual indexes in the subpopulation did not match.  " +
                    "The first was individual " + i + ", which is listed in the file as " + j);
                individuals[i] = species.newIndividual(state, reader);
                }
            }
        else for (int i = 0 ; i < individuals.length; i++)
                 {
                 int j = Code.readIntegerWithPreamble(INDIVIDUAL_INDEX_PREAMBLE, state, reader);
                 // sanity check
                 if (j!=i) state.output.warnOnce("On reading subpopulation from text stream, some individual indexes in the subpopulation did not match.  " +
                     "The first was individual " + i + ", which is listed in the file as " + j);
                 if (individuals[i] != null)
                     individuals[i].readIndividual(state, reader);
                 else
                     {
                     state.output.warnOnce("On reading subpopulation from text stream, some of the preexisting subpopulation's slots were null.  " +
                         "If you're starting an evolutionary run by reading an existing population from a file, this is expected -- ignore this message.");
                     individuals[i] = species.newIndividual(state, reader);
                     }
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
