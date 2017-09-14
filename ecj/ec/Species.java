/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.io.*;
import java.util.*;

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

 <tr><td valign=top><i>base</i>.<tt>fitness</tt><br>
 <font size=-1>classname, inherits and != ec.Fitness</font></td>
 <td valign=top>(the class for the prototypical fitness for the species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>numpipes</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(total number of breeding pipelines for the species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>pipe</tt><br>
 <font size=-1>classname, inherits and != ec.BreedingSource</font></td>
 <td valign=top>(the class for the prototypical Breeding Source)</td></tr>

 </table>


 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ind</tt></td>
 <td>i_prototype (the prototypical individual)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>pipe</tt></td>
 <td>pipe_prototype (breeding source prototype)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt></td>
 <td>f_prototype (the prototypical fitness)</td></tr>

 </table>



 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Species implements Prototype
    {
    public static final String P_INDIVIDUAL = "ind";
    public static final String P_PIPE = "pipe";
    public static final String P_FITNESS = "fitness";

    /** The prototypical individual for this species. */
    public Individual i_prototype;
    
    /** The prototypical breeding pipeline for this species. */
    public BreedingSource pipe_prototype;

    /** The prototypical fitness for individuals of this species. */
    public Fitness f_prototype;    

    public Object clone()
        {
        try
            {
            Species myobj = (Species) (super.clone());
            myobj.i_prototype = (Individual) i_prototype.clone();
            myobj.f_prototype = (Fitness) f_prototype.clone();
            myobj.pipe_prototype = (BreedingSource) pipe_prototype.clone();
            return myobj;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        } 
        
    /** Called whenever the Breeder calls produce(...) on a BreedingPipeline, in order to pass
        a new "misc" object.  Customize this as you see fit: the default just builds an empty hashmap. */
    public HashMap<String, Object> buildMisc(EvolutionState state, int subpopIndex, int thread)
        {
        return new HashMap<String,Object>();
        }
   
    /** Provides a brand-new individual to fill in a population.  The default form
        simply calls clone(), creates a fitness, sets evaluated to false, and sets
        the species.  If you need to make a more custom genotype (as is the case
        for GPSpecies, which requires a light rather than deep clone), 
        you will need to override this method as you see fit.
    */
    
    public Individual newIndividual(final EvolutionState state, int thread)
        {
        Individual newind = (Individual)(i_prototype.clone());

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;

        // ...and we're ready!
        return newind;
        }
    
    /**
       Provides an individual read from a stream, including
       the fitness; the individual will
       appear as it was written by printIndividual(...).  Doesn't 
       close the stream.  Sets evaluated to false and sets the species.
       If you need to make a more custom mechanism (as is the case
       for GPSpecies, which requires a light rather than deep clone), 
       you will need to override this method as you see fit.
    */

    public Individual newIndividual(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        {
        Individual newind = (Individual)(i_prototype.clone());
        
        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // load that sucker
        newind.readIndividual(state,reader);

        // Set the species to me
        newind.species = this;

        // and we're ready!
        return newind;  
        }

    /**
       Provides an individual read from a DataInput source, including
       the fitness.  Doesn't 
       close the DataInput.  Sets evaluated to false and sets the species.
       If you need to make a more custom mechanism (as is the case
       for GPSpecies, which requires a light rather than deep clone), 
       you will need to override this method as you see fit.
    */

    public Individual newIndividual(final EvolutionState state,
        final DataInput dataInput)
        throws IOException
        {
        Individual newind = (Individual)(i_prototype.clone());
        
        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readIndividual(state,dataInput);

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
        pipe_prototype = (BreedingSource)(
            state.parameters.getInstanceForParameter(
                base.push(P_PIPE),def.push(P_PIPE),BreedingSource.class));
        pipe_prototype.setup(state,base.push(P_PIPE));

        // I promised over in BreedingSource.java that this method would get called.
        state.output.exitIfErrors();

        // load our individual prototype
        i_prototype = (Individual)(state.parameters.getInstanceForParameter(
                base.push(P_INDIVIDUAL),def.push(P_INDIVIDUAL),
                Individual. class));
        // set the species to me before setting up the individual, so they know who I am
        i_prototype.species = this;
        i_prototype.setup(state,base.push(P_INDIVIDUAL));
        
        // load our fitness
        f_prototype = (Fitness) state.parameters.getInstanceForParameter(
            base.push(P_FITNESS),def.push(P_FITNESS),
            Fitness.class);
        f_prototype.setup(state,base.push(P_FITNESS));
        }
    }


