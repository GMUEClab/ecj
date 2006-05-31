/*
Copyright 2006 by Sean Luke
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


package ec;
import ec.util.Parameter;

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
    public void populate(EvolutionState state)
        {
        // let's populate!
        for(int x=0;x<subpops.length;x++)
            subpops[x].populate(state);
        }

    }
