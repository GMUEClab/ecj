/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec;
import ec.util.Parameter;
import java.util.*;
import java.io.*;

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
 * using the populate() method.  This method typically populates
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

 <tr><td valign=top><i>base</i>.<tt>fitness</tt></td>
 <td>f_prototype (the prototypical fitness)</td></tr>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */


public class Subpopulation implements Group
    {
    /** A new subpopulation should be loaded from this file if it is non-null;
        otherwise they should be created at random.  */
    public File loadInds;

    /** The prototypical fitness for individuals in this subpopulation. */
    public Fitness f_prototype;
    
    /** The species for individuals in this subpopulation. */
    public Species species;

    /** The subpopulation's individuals. */
    public Individual[] individuals;

    /** Do we allow duplicates? */
    public int numDuplicateRetries;
    
    public static final String P_FILE = "file";
    public static final String P_SUBPOPSIZE = "size";  // parameter for number of subpops or pops
    public static final String P_SPECIES = "species";
    public static final String P_FITNESS = "fitness";
    public static final String P_RETRIES = "duplicate-retries";

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
            p.f_prototype = f_prototype;  // don't throw it away...maybe this is a bad idea...
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

        // what fitness metric do we use?

        f_prototype = (Fitness) state.parameters.getInstanceForParameter(
            base.push(P_FITNESS),null,
            Fitness.class);
        f_prototype.setup(state,base.push(P_FITNESS));

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



    /** Note: don't call populate() in a multithreaded environment unless
        at least one instance of Fitness has been called already.  Otherwise
        MultiObjectiveFitness *may* have an initial race condition. 
        As presently coded, Subpopulation's constructor runs in a
        single thread only, during population initialization, which
        is single-threaded on purpose. */

    public void populate(EvolutionState state)
        {
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds!=null)
            {
            // let's make some individuals!
            try
                {
                LineNumberReader reader = new LineNumberReader(new FileReader(loadInds));
                for(int x=0;x<individuals.length;x++)
                    individuals[x] = species.newIndividual(
                        state,this,(Fitness)(f_prototype.clone()),reader);
                state.output.message("Loading subpopulation from file " + loadInds);
                }
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
                    individuals[x] = species.newIndividual(
                        state,this,(Fitness)(f_prototype.clone()));

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
    }
