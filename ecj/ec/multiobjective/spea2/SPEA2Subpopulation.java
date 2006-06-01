/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.*;
import ec.util.Parameter;

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
 <td>f_prototype (the prototypical fitness)</td></tr>

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

    }
