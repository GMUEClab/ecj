/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.*;
import ec.util.Parameter;
import java.io.*;
import ec.util.DecodeReturn;
import ec.util.Code;

/* 
 * SPEA2Subpopulation.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (based on Subpopulation.java by Sean Luke)
 */

/**
 * SPEA2Subpopulation is a simple subclass of Subpopulation which
 * adds the archiveSize field.  The archive is portion of the
 * subpopulation so archive size may not exceed the population
 * size.
 * 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>archive-size</tt><br>
 <font size=-1>int &gt;= 1 &lt; populationSize</font></td>
 <td valign=top>(total number of individuals from the population which are in the archive)</td></tr>
 </table>


 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt></td>
 <td>species.f_prototype (the prototypical fitness)</td></tr>

 </table>

 @see Subpopulation Subpopulation

 *
 * @author Robert Hubley (based on Subpopulation.java by Sean Luke)
 * @version 1.0 
 */


public class SPEA2Subpopulation extends Subpopulation
    {
    /** The SPEA2 archive size */
    public int archiveSize;

    public static final String P_ARCHIVESIZE = "archive-size";
    public static final String ARCHIVE_PREAMBLE = "SPEA2 Archive Size: ";

    /** Returns an instance of Subpopulation just like it had been before it was
        populated with individuals. You may need to override this if you override
        Subpopulation.   <b>IMPORTANT NOTE</b>: if the size of the array in
        Subpopulation has been changed, then the clone will take on the new array
        size.  This helps some evolution strategies.
        @see Group#emptyClone()
    */
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // What is the size of the archive?

        archiveSize = state.parameters.getInt(
            base.push(P_ARCHIVESIZE),null,1);
        if (archiveSize<=0 || archiveSize >= (individuals.length)-1)
            state.output.fatal(
                "Archive size must be an integer >= 1 and <= population size-1.\n",
                base.push(P_ARCHIVESIZE),null);

        }


    /** Overridden to include the archive size in the stream. */
    public void printSubpopulationForHumans(final EvolutionState state,
                                            final int log, 
                                            final int verbosity)
        {
        state.output.println(ARCHIVE_PREAMBLE + archiveSize, verbosity, log);
        super.printSubpopulationForHumans(state, log, verbosity);
        }
        
    /** Overridden to include the archive size in the stream. */
    public void printSubpopulation(final EvolutionState state,
                                   final int log, 
                                   final int verbosity)
        {
        state.output.println(ARCHIVE_PREAMBLE + Code.encode(archiveSize), verbosity, log);
        super.printSubpopulation(state, log, verbosity);
        }
        
    /** Overridden to include the archive size in the stream. */
    public void printSubpopulation(final EvolutionState state,
                                   final PrintWriter writer)
        {
        writer.println(ARCHIVE_PREAMBLE + Code.encode(archiveSize));
        super.printSubpopulation(state, writer);
        }
        

    /** Overridden to include the archive size in the stream. */
    public void readSubpopulation(final EvolutionState state, 
                                  final LineNumberReader reader) throws IOException
        {
        // check the size
        int size = Code.readIntegerWithPreamble(NUM_INDIVIDUALS_PREAMBLE, state, reader);

        // read in individuals
        if (archiveSize != size)
            state.output.fatal("On reading a SPEA2 subpopulation from a text stream, the archive sizes did not match.");
        
        super.readSubpopulation(state, reader);
        }
        
    /** Overridden to include the archive size in the stream. */
    public void writeSubpopulation(final EvolutionState state,
                                   final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(archiveSize);
        super.writeSubpopulation(state, dataOutput);
        }
    
    /** Overridden to include the archive size in the stream. */
    public void readSubpopulation(final EvolutionState state,
                                  final DataInput dataInput) throws IOException
        {
        int size = dataInput.readInt();
        
        // read in individuals
        if (archiveSize != size)
            state.output.fatal("On reading a SPEA2 subpopulation from a binary stream, the archive sizes did not match.");

        super.readSubpopulation(state, dataInput);
        }

    }
