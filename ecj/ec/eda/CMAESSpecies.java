/*
  Copyright 2015 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda;
import ec.*;
import ec.vector.*;
import ec.util.*;
import java.io.*;

/* 
 * CMAESSpecies.java
 * 
 * Created: Wed Jul  8 12:29:50 EDT 2015
 * By: Sam McKay and Sean Luke
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
 <font size=-1>classname, inherits and != ec.BreedingPipeline</font></td>
 <td valign=top>(the class for the prototypical Breeding Pipeline)</td></tr>

 </table>


 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ind</tt></td>
 <td>i_prototype (the prototypical individual)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>pipe</tt></td>
 <td>pipe_prototype (breeding pipeline prototype)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt></td>
 <td>f_prototype (the prototypical fitness)</td></tr>

 </table>



 * @author Sam McKay and Sean Luke
 * @version 1.0 
 */

public class CMAESSpecies extends FloatVectorSpecies
    {
    public static final String P_SPECIES = "species";
    
    public Parameter defaultBase()
        {
        return EDADefaults.base().push(P_SPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        
        Parameter def = defaultBase();

		// set myself up and also define my initial distribution here

        }

    public Object clone()
        {
        CMAESSpecies myobj = (CMAESSpecies) (super.clone());
            
        // clone my distribution and other variables here
            
        return myobj;
        } 

    public Individual newIndividual(final EvolutionState state, int thread)
        {
        Individual newind = super.newIndividual(state, thread);
        
        // sample the genome of newind from the distribution here
        
        return newind;
        }
    }


