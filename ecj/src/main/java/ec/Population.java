/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.io.*;
import java.util.ArrayList;

/* 
 * Population.java
 * 
 * Created: Tue Aug 10 20:50:54 1999
 * By: Sean Luke
 */

/**
 * A Population is the repository for all the Individuals being bred or
 * evaluated in the evolutionary run at a given time.
 * A Population is basically an array of Subpopulations, each of which
 * are arrays of Individuals coupled with a single Species per Subpoulation.
 *
 * <p>The first Population is created using the initializePopulation method
 * of the Initializer object, which typically calls the Population's
 * populate() method in turn.  On generational systems, subsequent populations
 * are created on a generation-by-generation basis by the Breeder object,
 * replacing the previous Population.
 *
 * <p>In a multithreaded area of a run, Populations should be considered
 * immutable.  That is, once they are created, they should not be modified,
 * nor anything they contain.  This protocol helps ensure read-safety under
 * multithreading race conditions.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>subpops</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the number of subpopulations)</td></tr>

 <tr><td valign=top><i>base.</i><tt>subpop</tt><i>.n</i><br>
 <font size=-1>classname, inherits or = ec.Subpopulation</font></td>
 <td valign=top>(the class for subpopulation #<i>n</i>)</td></tr>

 <tr><td valign=top><i>base.</i><tt>default-subpop</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(the default subpopulation index.  The parameter base of this subpopulation will be used as the default base for all subpopulations which do not define one themselves./tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>subpop</tt><i>.n</i></td>
 <td>Subpopulation #<i>n</i>.</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Population implements Cloneable, Setup
    {
    private static final long serialVersionUID = 1;

    public ArrayList<Subpopulation> subpops = new ArrayList<Subpopulation>();
    public static final String P_SIZE = "subpops";
    public static final String P_SUBPOP = "subpop";
    public static final String P_DEFAULT_SUBPOP = "default-subpop";
    public static final String P_FILE = "file";
    public static final String NUM_SUBPOPS_PREAMBLE = "Number of Subpopulations: ";
    public static final String SUBPOP_INDEX_PREAMBLE = "Subpopulation Number: ";


    /* A new population should be loaded from this resource name if it is non-null;
       otherwise they should be created at random.  */
    public boolean loadInds;
    public Parameter file;

    /** Returns an instance of Population just like it had been before it was
        populated with individuals. You may need to override this if you override
        Population. <b>IMPORTANT NOTE</b>: if the size of the array in
        Population has been changed, then the clone will take on the new array
        size.  This helps some evolution strategies.
    */

    public Population emptyClone()
        {
        try
            {
            Population p = (Population)clone();
            p.subpops = new ArrayList<Subpopulation>(subpops.size());
            for(int x = 0; x< subpops.size(); x++)
                p.subpops.add( (Subpopulation) (subpops.get(x).emptyClone()));
            return p;   
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // never happens
        }
                
    public void clear()
        {
        for(int x = 0; x< subpops.size(); x++)
            ((Subpopulation)(subpops.get(x))).clear();
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // how big should subpops be?  Don't have a default base

        Parameter p;

        // do we load from a file?
        file = base.push(P_FILE);
        loadInds = state.parameters.exists(file,null);
        
        // how many subpopulations do we have?
        
        p = base.push(P_SIZE);
        int size = state.parameters.getInt(p,null,1);
        if (size<=0) // uh oh
            state.output.fatal("Population size must be >0.\n",base.push(P_SIZE));
        subpops = new ArrayList<Subpopulation>(subpops.size());

        // Set up the subpopulations
        for (int x=0;x<size;x++)
            {
            p = base.push(P_SUBPOP).push(""+x);
            if (!state.parameters.exists(p,null))
                {
                p = base.push(P_DEFAULT_SUBPOP);
                int defaultSubpop = state.parameters.getInt(p, null, 0); 
                if ( defaultSubpop >= 0)
                    {
                    state.output.warning("Using subpopulation " + defaultSubpop + " as the default for subpopulation " + x);
                    p = base.push(P_SUBPOP).push(""+defaultSubpop);
                    }
                // else an error will occur on the next line anyway.
                }
            subpops.add((Subpopulation) (state.parameters.getInstanceForParameterEq(p, null, Subpopulation.class)));  // Subpopulation.class is fine
            subpops.get(x).setup(state,p);
            
            // test for loadinds
            if (loadInds && subpops.get(x).loadInds)  // uh oh
                state.output.fatal("Both a subpopulation and its parent population have been told to load from files.  This can't happen.  It's got to be one or the other.",
                    base.push(P_FILE), null);
            }
        }

    /** Populates the population with new random individuals. */ 
    public void populate(EvolutionState state, int thread)
        {
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds)
            {
            InputStream stream = state.parameters.getResource(file,null);
            if (stream == null)
                state.output.fatal("Could not load population from file", file);
            
            try { readPopulation(state, new LineNumberReader(new InputStreamReader(stream))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + state.parameters.getString(file, null) + ".  The IOException was: \n" + e,
                    file, null); }
            }
        else
            {
            // let's populate!
            for(int x = 0; x< subpops.size(); x++)
                subpops.get(x).populate(state, thread);
            }
        }
        
        
    /** Prints an entire population in a form readable by humans. 
        @deprecated Verbosity no longer has meaning
    */
    public final void printPopulationForHumans(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printPopulationForHumans(state, log);
        }
        
    /** Prints an entire population in a form readable by humans but also parseable by the computer using readPopulation(EvolutionState, LineNumberReader).
        @deprecated Verbosity no longer has meaning
    */
    public final void printPopulation(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printPopulation(state, log);
        }
        
    /** Prints an entire population in a form readable by humans, with a verbosity of Output.V_NO_GENERAL. */
    public void printPopulationForHumans(final EvolutionState state,
        final int log)
        {
        state.output.println(NUM_SUBPOPS_PREAMBLE + subpops.size(),  log);
        for(int i = 0; i < subpops.size(); i++)
            {
            state.output.println(SUBPOP_INDEX_PREAMBLE + i,  log);
            subpops.get(i).printSubpopulationForHumans(state, log);
            }
        }
        
    /** Prints an entire population in a form readable by humans but also parseable by the computer using readPopulation(EvolutionState, LineNumberReader), with a verbosity of Output.V_NO_GENERAL. */
    public void printPopulation(final EvolutionState state,
        final int log)
        {
        state.output.println(NUM_SUBPOPS_PREAMBLE + Code.encode(subpops.size()),  log);
        for(int i = 0; i < subpops.size(); i++)
            {
            state.output.println(SUBPOP_INDEX_PREAMBLE + Code.encode(i),  log);
            subpops.get(i).printSubpopulation(state, log);
            }
        }
        
    /** Prints an entire population in a form readable by humans but also parseable by the computer using readPopulation(EvolutionState, LineNumberReader). */
    public void printPopulation(final EvolutionState state,
        final PrintWriter writer)
        {
        writer.println(NUM_SUBPOPS_PREAMBLE + Code.encode(subpops.size()));
        for(int i = 0; i < subpops.size(); i++)
            {
            writer.println(SUBPOP_INDEX_PREAMBLE + Code.encode(i));         
            subpops.get(i).printSubpopulation(state, writer);
            }
        }
    
    /** Reads a population from the format generated by printPopulation(....).  The number of subpopulations and the species information must be identical. */
    public void readPopulation(final EvolutionState state, 
        final LineNumberReader reader) throws IOException
        {
        // read the number of subpops and check to see if this appears to be a valid individual
        int numSubpops = Code.readIntegerWithPreamble(NUM_SUBPOPS_PREAMBLE, state, reader);
        
        // read in subpops
        if (numSubpops != subpops.size())  // definitely wrong
            state.output.fatal("On reading population from text stream, the number of subpopulations was wrong.");

        for(int i = 0; i < subpops.size(); i++)
            {
            int j = Code.readIntegerWithPreamble(SUBPOP_INDEX_PREAMBLE, state, reader);
            // sanity check
            if (j!=i) state.output.warnOnce("On reading population from text stream, some subpopulation indexes in the population did not match.");
            subpops.get(i).readSubpopulation(state, reader);
            }
        }
    
    /** Writes a population in binary form, in a format readable by readPopulation(EvolutionState, DataInput). */
    public void writePopulation(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(subpops.size());
        for(int i = 0; i < subpops.size(); i++)
            subpops.get(i).writeSubpopulation(state, dataOutput);
        }
    
    /** Reads a population in binary form, from the format generated by writePopulation(...). The number of subpopulations and the species information must be identical. */
    public void readPopulation(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int numSubpopulations = dataInput.readInt();
        if (numSubpopulations != subpops.size())
            state.output.fatal("On reading subpopulation from binary stream, the number of subpopulations was wrong.");

        for(int i = 0; i < subpops.size(); i++)
            subpops.get(i).readSubpopulation(state, dataInput);
        }


    }
