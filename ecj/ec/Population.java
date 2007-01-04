/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.io.*;

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

public class Population implements Group
    {
    public Subpopulation[] subpops;
    public static final String P_SIZE = "subpops";
    public static final String P_SUBPOP = "subpop";
    public static final String NUM_SUBPOPS_PREAMBLE = "Number of Subpopulations: ";
    public static final String SUBPOP_INDEX_PREAMBLE = "Subpopulation Number: ";


    /** Returns an instance of Population just like it had been before it was
        populated with individuals. You may need to override this if you override
        Population. <b>IMPORTANT NOTE</b>: if the size of the array in
        Population has been changed, then the clone will take on the new array
        size.  This helps some evolution strategies.
        @see Group#emptyClone()
    */

    public Group emptyClone()
        {
        try
            {
            Population p = (Population)clone();
            p.subpops = new Subpopulation[subpops.length];
            for(int x=0;x<subpops.length;x++)
                p.subpops[x] = (Subpopulation)(subpops[x].emptyClone());
            return p;   
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // never happens
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // how big should subpops be?  Don't have a default base

        Parameter p;

        p = base.push(P_SIZE);
        int size = state.parameters.getInt(p,null,1);
        if (size==0) // uh oh
            state.output.fatal("Population size must be >0.\n",base.push(P_SIZE));
        subpops = new Subpopulation[size];

        // Load the subpopulations
        for (int x=0;x<size;x++)
            {
            p = base.push(P_SUBPOP).push(""+x);
            subpops[x] = (Subpopulation)(state.parameters.getInstanceForParameterEq(p,null,Subpopulation.class));  // Subpopulation.class is fine
            subpops[x].setup(state,p);
            }
        }

    /** Populates the population with new random individuals. */ 
    public void populate(EvolutionState state, int thread)
        {
        // let's populate!
        for(int x=0;x<subpops.length;x++)
            subpops[x].populate(state, thread);
        }
        
        
    /** Prints an entire population in a form readable by humans. */
    public void printPopulationForHumans(final EvolutionState state,
                                         final int log, 
                                         final int verbosity)
        {
        state.output.println(NUM_SUBPOPS_PREAMBLE + subpops.length, verbosity, log);
        for(int i = 0 ; i < subpops.length; i++)
            {
            state.output.println(SUBPOP_INDEX_PREAMBLE + i, verbosity, log);
            subpops[i].printSubpopulationForHumans(state, log, verbosity);
            }
        }
        
    /** Prints an entire population in a form readable by humans but also parseable by the computer using readPopulation(EvolutionState, LineNumberReader). */
    public void printPopulation(final EvolutionState state,
                                final int log, 
                                final int verbosity)
        {
        state.output.println(NUM_SUBPOPS_PREAMBLE + Code.encode(subpops.length), verbosity, log);
        for(int i = 0 ; i < subpops.length; i++)
            {
            state.output.println(SUBPOP_INDEX_PREAMBLE + Code.encode(i), verbosity, log);
            subpops[i].printSubpopulation(state, log, verbosity);
            }
        }
        
    /** Prints an entire population in a form readable by humans but also parseable by the computer using readPopulation(EvolutionState, LineNumberReader). */
    public void printPopulation(final EvolutionState state,
                                final PrintWriter writer)
        {
        writer.println(NUM_SUBPOPS_PREAMBLE + Code.encode(subpops.length));
        for(int i = 0 ; i < subpops.length; i++)
            {
            writer.println(SUBPOP_INDEX_PREAMBLE + Code.encode(i));         
            subpops[i].printSubpopulation(state, writer);
            }
        }
    
    /** Reads a population from the format generated by printPopulation(....).  The number of subpopulations and the species information must be identical. */
    public void readPopulation(final EvolutionState state, 
                               final LineNumberReader reader) throws IOException
        {
        // read the number of subpops and check to see if this appears to be a valid individual
        int numSubpops = Code.readIntegerWithPreamble(NUM_SUBPOPS_PREAMBLE, state, reader);
        
        // read in subpops
        if (numSubpops != subpops.length)  // definitely wrong
            state.output.fatal("On reading population from text stream, the number of subpopulations was wrong.");

        for(int i = 0 ; i < subpops.length; i++)
            {
            int j = Code.readIntegerWithPreamble(SUBPOP_INDEX_PREAMBLE, state, reader);
            // sanity check
            if (j!=i) state.output.warnOnce("On reading population from text stream, some subpopulation indexes in the population did not match.");
            subpops[i].readSubpopulation(state, reader);
            }
        }
    
    /** Writes a population in binary form, in a format readable by readPopulation(EvolutionState, DataInput). */
    public void writePopulation(final EvolutionState state,
                                final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(subpops.length);
        for(int i = 0 ; i < subpops.length; i++)
            subpops[i].writeSubpopulation(state, dataOutput);
        }
    
    /** Reads a population in binary form, from the format generated by writePopulation(...). The number of subpopulations and the species information must be identical. */
    public void readPopulation(final EvolutionState state,
                               final DataInput dataInput) throws IOException
        {
        int numSubpopulations = dataInput.readInt();
        if (numSubpopulations != subpops.length)
            state.output.fatal("On reading subpopulation from binary stream, the number of subpopulations was wrong.");

        for(int i = 0 ; i < subpops.length; i++)
            subpops[i].readSubpopulation(state, dataInput);
        }


    }
