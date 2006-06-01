/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.io.*;

/* 
 * Species.java
 * 
 * Created: Tue Aug 10 20:31:50 1999
 * By: Sean Luke
 */

/**
 * Species is a prototype which defines the features for a set of individuals
 * in the population.  Typically, individuals may breed if they belong to the
 * same species (but it's not a hard-and-fast rule).  Each Subpopulation has
 * one Species object which defines the species for individuals in that
 * Subpopulation.
 *
 * <p>Species are generally responsible for creating individuals, through
 * their newIndividual(...) method.  This method usually clones its prototypical
 * individual and makes some additional modifications to the clone, then returns it.
 * Note that the prototypical individual does <b>not need to be a complete individual</b> --
 * for example, GPSpecies holds a GPIndividual which doesn't have any trees (the tree
 * roots are null).
 *
 * <p>Species also holds a prototypical breeding pipeline meant to breed
 * this individual.  To breed individuals of this species, clone the pipeline
 * and use the clone.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ind</tt><br>
 <font size=-1>classname, inherits and != ec.Individual</font></td>
 <td valign=top>(the class for the prototypical individual for the species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>numpipes</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(total number of breeding pipelines for the species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>pipe</tt><br>
 <font size=-1>classname, inherits and != ec.BreedingPipeline</font></td>
 <td valign=top>(the class for the prototypical Breeding Pipeline)</td></tr>

 </table>


 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ind</tt></td>
 <td>i_prototype (the prototypical individual)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>pipe</tt></td>
 <td>pipe_prototype (breeding pipeline prototype)</td></tr>

 </table>



 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Species implements Prototype
    {
    public static final String P_INDIVIDUAL = "ind";
    public static final String P_PIPE = "pipe";

    /** The prototypical individual for this species.       
     */

    public Individual i_prototype;
    public BreedingPipeline pipe_prototype;

    public Object clone()
        {
        try
            {
            Species myobj = (Species) (super.clone());
            myobj.i_prototype = (Individual) i_prototype.clone();
            myobj.pipe_prototype = (BreedingPipeline) pipe_prototype.clone();
            return myobj;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        } 



    /** override this to provide a brand-new individual to fill in a population. The CloneNotSupportedException permits you to use protoClone() rather than protoCloneSimple(), for efficiency gains.  It's assumed that the thread is thread 0. */

    public abstract Individual newIndividual(final EvolutionState state,
                                             final Subpopulation _population, 
                                             final Fitness _fitness);
    
    /**
       Override this to provide an individual read from a file; the individual will
       appear as it was written by printIndividual(...).  You should read and
       set up the fitness as well.  Don't close the file.  The default version of this
       method throws an error.
    */

    public Individual newIndividual(final EvolutionState state,
                                    final Subpopulation _population,
                                    final Fitness _fitness,
                                    final LineNumberReader reader)
        throws IOException
        {
        Individual newind = (Individual)(i_prototype.clone());
                
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = _fitness;
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // load that sucker
        newind.readIndividual(state,reader);

        // Set the species to me
        newind.species = this;

        // and we're ready!
        return newind;  
        }

    /**
       Override this to provide an individual read from a binary stream, likely using readIndividual(...).
       You should read and set up the fitness as well.  Don't close the file.   The default version of this method throws an error.
    */

    public Individual newIndividual(final EvolutionState state,
                                    final Subpopulation _population,
                                    final Fitness _fitness,
                                    final DataInput dataInput)
        throws IOException
        {
        Individual newind = (Individual)(i_prototype.clone());
        
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = _fitness;
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readGenotype(state,dataInput);

        // and we're ready!
        return newind;  
        }


    /** The default version of setup(...) loads requested pipelines and calls setup(...) on them and normalizes their probabilities.  
        If your individual prototype might need to know special things about the species (like parameters stored in it),
        then when you override this setup method, you'll need to set those parameters BEFORE you call super.setup(...),
        because the setup(...) code in Species sets up the prototype.
        @see Prototype#setup(EvolutionState,Parameter)
    */
 
    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        // load the breeding pipeline
        pipe_prototype = (BreedingPipeline)(
            state.parameters.getInstanceForParameter(
                base.push(P_PIPE),def.push(P_PIPE),BreedingPipeline.class));
        pipe_prototype.setup(state,base.push(P_PIPE));

        // I promised over in BreedingSource.java that this method would get called.
        state.output.exitIfErrors();

        // load our individual prototype
        i_prototype = (Individual)(state.parameters.getInstanceForParameter(base.push(P_INDIVIDUAL),def.push(P_INDIVIDUAL),Individual. class));
        // set the species to me before setting up the individual, so they know who I am
        i_prototype.species = this;
        i_prototype.setup(state,base.push(P_INDIVIDUAL));
        }
    }


