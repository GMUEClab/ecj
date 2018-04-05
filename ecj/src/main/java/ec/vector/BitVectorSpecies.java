/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.util.*;
import java.io.*;
import ec.*;

/* 
 * BitVectorSpecies.java
 * 
 * Created: Mon Feb  4 15:57:44 EST 2013
 * By: Sean Luke
 */

/**
 * BitVectorSpecies is a subclass of VectorSpecies with special
 * constraints for boolean vectors, namely BitVectorIndividual.
 *
 * <p>BitVectorSpecies can specify some parameters globally, per-segment, and per-gene.
 * See <a href="VectorSpecies.html">VectorSpecies</a> for information on how to this works.
 *
 * <p>
 * BitVectorSpecies provides support for two ways of mutating a gene.
 * <ul>
 * <li><b>reset</b> Replacing the gene's value with a value uniformly drawn from the gene's
 * range [true, false].</li>
 * <li><b>flip</b>Flipping the bit of the gene value.  This is the default.
 * </ul>
 *
 * <p>
 * BitVectorSpecies provides support for two ways of initializing a gene.  The initialization procedure
 * is determined by the choice of mutation procedure as described above.  If the mutation is floating-point
 * (<tt>reset, gauss, polynomial</tt>), then initialization will be done by resetting the gene
 * to uniformly chosen double floating-point value between the minimum and maximum legal gene values, inclusive.
 * If the mutation is integer (<tt>integer-reset, integer-random-walk</tt>), then initialization will be done
 * by performing the same kind of reset, but restricting values to integers only.
 * 
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-prob</tt>.<i>gene-number</i><br>
 * <font size=-1><tt>reset</tt>, <tt>flip</tt>, (default=<tt>flip</tt>)</font></td>
 * <td valign=top>(the mutation type)</td>
 * </tr>
 * 
 * </table>
 * @author Sean Luke, Gabriel Balan, Rafal Kicinger
 * @version 1.0
 */

public class BitVectorSpecies extends VectorSpecies
    {
    public final static String P_MUTATIONTYPE = "mutation-type";
    public final static String V_RESET_MUTATION = "reset";
    public final static String V_FLIP_MUTATION = "flip";

    public final static int C_RESET_MUTATION = 0;
    public final static int C_FLIP_MUTATION = 1;

    /** Mutation type, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected int[] mutationType;

    public int mutationType(int gene)
        {
        int[] m = mutationType;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();
        
        setupGenome(state, base);
        
        // CREATE THE ARRAYS
        
        mutationType = fill(new int[genomeSize + 1], -1);
        
        
        /// MUTATION

        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE), null);
        int _mutationType = C_FLIP_MUTATION;
        if (mtype == null)
            state.output.warning("No global mutation type given for BitVectorSpecies, assuming 'flip' mutation",
                base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            _mutationType = C_RESET_MUTATION; // redundant
        else if (mtype.equalsIgnoreCase(V_FLIP_MUTATION))
            _mutationType = C_FLIP_MUTATION;
        else
            state.output.fatal("BitVectorSpecies given a bad mutation type: "
                + mtype, base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        fill(mutationType, _mutationType);




        // CALLING SUPER
                
        // This will cause the remaining parameters to get set up, and
        // all per-gene and per-segment parameters to get set up as well.
        // We need to do this at this point because the global params need
        // to get set up first, and also prior to the prototypical individual
        // getting setup at the end of super.setup(...).

        super.setup(state, base);
        }




    /** Called when VectorSpecies is setting up per-gene and per-segment parameters.  The index
        is the current gene whose parameter is getting set up.  The Parameters in question are the
        bases for the gene.  The postfix should be appended to the end of any parameter looked up
        (it often contains a number indicating the gene in question), such as
        state.parameters.exists(base.push(P_PARAM).push(postfix), def.push(P_PARAM).push(postfix)
                        
        <p>If you override this method, be sure to call super(...) at some point, ideally first.
    */
    protected void loadParametersForGene(EvolutionState state, int index, Parameter base, Parameter def, String postfix)
        {       
        super.loadParametersForGene(state, index, base, def, postfix);

        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix), null);
        if (mtype == null) { }  // we're cool
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            mutationType[index] = C_RESET_MUTATION; 
        else if (mtype.equalsIgnoreCase(V_FLIP_MUTATION))
            mutationType[index] = C_FLIP_MUTATION;
        else
            state.output.fatal("BitVectorSpecies given a bad mutation type: " + mtype, 
                base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix));
        }            
    }


