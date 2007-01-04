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
 * VectorSpecies.java
 * 
 * Created: Thu Mar 22 17:44:00 2001
 * By: Liviu Panait
 */

/**
 * VectorSpecies is a species which can create VectorIndividuals.  Different
 * VectorSpecies are used for different kinds of VectorIndividuals: a plain
 * VectorSpecies is probably only applicable for BitVectorIndividuals.
 * 
 * <p>VectorSpecies contains a number of parameters guiding how the individual
 * crosses over and mutates.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.n</i>.<tt>genome-size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(size of the genome)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>chunk-size</tt><br>
 <font size=-1>1 &lt;= int &lt;= genome-size (default=1)</font></td>
 <td valign=top>(the chunk size for crossover (crossover will only occur on chunk boundaries))</td></tr>

 <tr><td valign=top><i>base</i>.<tt>crossover-type</tt><br>
 <font size=-1>string, one of: one, two, any</font></td>
 <td valign=top>(default crossover type (one-point, two-point, or any-point (uniform) crossover)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>crossover-prob</tt><br>
 <font size=-1>0.0 &gt;= float &gt;= 1.0 </font></td>
 <td valign=top>(probability that a gene will get crossed over during any-point crossover)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>mutation-prob</tt><br>
 <font size=-1>0.0 &lt;= float &lt;= 1.0 </font></td>
 <td valign=top>(probability that a gene will get mutated over default mutation)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 vector.species

 * @author Sean Luke and Liviu Panait
 * @version 1.0 
 */

public class VectorSpecies extends Species
    {

    public static final String P_VECTORSPECIES = "species";

    public final static String P_CROSSOVERTYPE = "crossover-type";
    public final static String P_CHUNKSIZE = "chunk-size";
    public final static String V_ONE_POINT = "one";
    public final static String V_TWO_POINT = "two";
    public final static String V_ANY_POINT = "any";
    public final static String P_MUTATIONPROB = "mutation-prob";
    public final static String P_CROSSOVERPROB = "crossover-prob";
    public final static String P_GENOMESIZE = "genome-size";

    public final static int C_ONE_POINT = 0;
    public final static int C_TWO_POINT = 1;
    public final static int C_ANY_POINT = 128;

    /** Probability that a gene will mutate */
    public float mutationProbability;
    /** Probability that a gene will cross over -- ONLY used in V_ANY_POINT crossover */
    public float crossoverProbability;
    /** What kind of crossover do we have? */
    public int crossoverType;
    /** How big of a genome should we create on initialization? */
    public int genomeSize;
    /** How big of chunks should we define for crossover? */
    public int chunksize;


    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_VECTORSPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // setup constraints  FIRST so the individuals can see them when they're
        // set up.
        
        Parameter def = defaultBase();
        
        genomeSize = state.parameters.getInt(base.push(P_GENOMESIZE),def.push(P_GENOMESIZE),1);
        if (genomeSize==0)
            state.output.error("VectorSpecies must have a genome size > 0",
                               base.push(P_GENOMESIZE),def.push(P_GENOMESIZE));
    
        chunksize = state.parameters.getIntWithDefault(base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE),1);
        if (chunksize <= 0 || chunksize > genomeSize)
            state.output.fatal("VectorSpecies must have a chunksize which is > 0 and < genomeSize",
                               base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));
        if (genomeSize % chunksize != 0)
            state.output.fatal("VectorSpecies must have a genomeSize which is a multiple of chunksize",
                               base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));

        mutationProbability = state.parameters.getFloat(
            base.push(P_MUTATIONPROB),def.push(P_MUTATIONPROB),0.0,1.0);
        if (mutationProbability==-1.0)
            state.output.error("VectorSpecies must have a mutation probability between 0.0 and 1.0 inclusive",
                               base.push(P_MUTATIONPROB),def.push(P_MUTATIONPROB));
    
        String ctype = state.parameters.getStringWithDefault(base.push(P_CROSSOVERTYPE), def.push(P_CROSSOVERTYPE), "");
        crossoverType = C_ONE_POINT;
        if (ctype==null)
            state.output.warning("No crossover type given for VectorSpecies, assuming one-point crossover",
                                 base.push(P_CROSSOVERTYPE),def.push(P_CROSSOVERTYPE));
        else if (ctype.equalsIgnoreCase(V_ONE_POINT))
            crossoverType=C_ONE_POINT;  // redundant
        else if (ctype.equalsIgnoreCase(V_TWO_POINT))
            crossoverType=C_TWO_POINT;
        else if (ctype.equalsIgnoreCase(V_ANY_POINT))
            crossoverType=C_ANY_POINT;
        else state.output.error("VectorSpecies given a bad crossover type: " + ctype,
                                base.push(P_CROSSOVERTYPE),def.push(P_CROSSOVERTYPE));
    
        if (crossoverType==C_ANY_POINT)
            {
            crossoverProbability = state.parameters.getFloat(
                base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB),0.0,1.0);
            if (crossoverProbability==-1.0)
                state.output.error("If it's going to use any-point crossover, VectorSpecies must have a crossover probability between 0.0 and 1.0 inclusive",
                                   base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB));
            }
        else crossoverProbability = 0.0f;
        state.output.exitIfErrors();
        
        // NOW call super.setup(...), which will in turn set up the prototypical individual
        super.setup(state,base);
        }

    public Individual newIndividual(final EvolutionState state, int thread) 
        
        {
        VectorIndividual newind = (VectorIndividual)(super.newIndividual(state, thread));

        newind.reset( state, thread);

        return newind;
        }
    }


