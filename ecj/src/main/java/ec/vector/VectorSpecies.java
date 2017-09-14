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
 * <p>VectorSpecies supports the following recombination methods:</p>
 * <ul>
 * <li><b>One-point crossover</b>.</li>
 * <li><b>Two-point crossover</b>.</li>
 * <li><b>Uniform crossover</b> - inaccurately called "any-point".</li>
 * <li><b>Line recombination</b> - children are random points on a line between
 *      the two parents.</li>
 * <li><b>Intermediate recombination</b> - the value of each component of the
 *      vector is between the values of that component of the parent vectors.
 *      </li>
 * </ul>
 * 
 * <P>Note that BitVectorIndividuals (which use VectorSpecies) and GeneVectorIndividuals
 * (which use GeneVectorSpecies, a subclass of VectorSpecies) do not support
 * Line or Intermediate Recombination.
 *
 * <p>Also note that for LongVectorIndividuals, there are certain values that will
 * never be created by line and intermediate recombination, because the
 * recombination is calculated using doubles and then rounded to the nearest
 * long. For large enough values (but still smaller than the maximum long), the
 * difference between one double and the next is greater than one.</p>
 *
 * <p>VectorSpecies has three wasy to determine the initial size of the individual:</p>
 * <ul>
 * <li><b>A fixed size</b>.</li>
 * <li><b>Geometric distribution</b>.</li>
 * <li><b>Uniform distribution</b></li>
 * </ul>
 *
 * <p>If the algorithm used is the geometric distribution, the VectorSpecies starts at a
 * minimum size and continues flipping a coin with a certain "resize probability",
 * increasing the size each time, until the coin comes up tails (fails).  The chunk size
 * must be 1 in this case.
 *
 * <p> If the algorithm used is the uniform distribution, the VectorSpecies picks a random
 * size between a provided minimum and maximum size, inclusive.  The chunk size
 * must be 1 in this case.
 *
 * <p>If the size is fixed, then you can also provide a "chunk size" which constrains the
 * locations in which crossover can be performed (only along chunk boundaries).  The genome
 * size must be a multiple of the chunk size in this case.
 *
 * <p>VectorSpecies also contains a number of parameters guiding how the individual
 * crosses over and mutates.
 *
 * <p><b>Per-Gene and Per-Segment Specification.</b>  VectorSpecies and its subclasses
 * specify a lot of parameters, notably mutation and initialization parameters, in one
 * of three ways.  We will use the <b><tt>mutation-probability</tt></b>
 * parameter as an example.
 *
 * <ol>
 * <li> Globally for all genes in the genome.
 *      This is done by specifying:
 *      <p><i>base</i>.<tt>mutation-probability</tt>
 *      <br><i>base</i>.<tt>max-gene</tt>
 *      <p><i>Note:</i> you <b>must</b> provide these values even if you don't use them,
 *      as they're used as defaults by #2 and #3 below.
 *<p>
 * <li> You may provide parameters for genes in segments (regions) along
 *      the genome.  The idea is to allow you to specify large chunks of genes
 *      all having the same parameter features.  
 *      To do this you must first specify how many segments there are:
 *      <p><i>base</i>.<tt>num-segments</tt>
 *      <p>The segments then may be defined by either start or end indices of genes. 
 *      This is controlled by specifying the value of:
 *      <p><i>base</i>.<tt>segment-type</tt>
 *      <p>...which can assume the value of start or end, with start being the default.
 *      The indices are defined using Java array style, i.e. the first gene has the index of 0, 
 *      and the last gene has the index of genome-size - 1.
 *      <p>Using this method, each segment is specified by<i>j</i>...
 *      <p><i>base</i>.<tt>segment.</tt><i>j</i><tt>.start</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.mutation-probability</tt>
 *      if segment-type value was chosen as start or by:
 *      <p><i>base</i>.<tt>segment.</tt><i>j</i><tt>.end</tt>
 *      <br><i>base</i>.<tt>segment.</tt><i>j</i><tt>.mutation-probability</tt>
 *      if segment-type value is equal to end.
 *<p>
 * <li> You may parameters for each separate gene.  
 *      This is done by specifying (for each gene location <i>i</i> you wish to specify)
 *      <p><i>base</i>.<tt>mutation-probability</tt>.<i>i</i>
 * </ol>
 * 
 * <p>Any settings for #3 override #2, and both override #1. 
 *
 * <p>The only parameter which can be specified this way in VectorSpecies is at present
 * <tt>mutation-probability</tt>.  However a number of parameters are specified this way
 * in subclasses. 
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>genome-size</tt><br>
 <font size=-1>int &gt;= 1 or one of: geometric, uniform</font></td>
 <td valign=top>(size of the genome, or if 'geometric' or 'uniform', the algorithm used to size the initial genome)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>chunk-size</tt><br>
 <font size=-1>1 &lt;= int &lt;= genome-size (default=1)</font></td>
 <td valign=top>(the chunk size for crossover (crossover will only occur on chunk boundaries))</td></tr>

 <tr><td valign=top><i>base</i>.<tt>geometric-prob</tt><br>
 <font size=-1>0.0 &lt;= double &lt; 1.0</font></td>
 <td valign=top>(the coin-flip probability for increasing the initial size using the geometric distribution)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>min-initial-size</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(the minimum initial size of the genome)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>max-initial-size</tt><br>
 <font size=-1>int &gt;= min-initial-size</font></td>
 <td valign=top>(the maximum initial size of the genome)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>crossover-type</tt><br>
 <font size=-1>string, one of: one, two, any</font></td>
 <td valign=top>(default crossover type (one-point, one-point-nonempty, two-point, two-point-nonempty, any-point (uniform), line, or intermediate)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>crossover-prob</tt><br>
 <font size=-1>0.0 &gt;= double &gt;= 1.0 </font></td>
 <td valign=top>(probability that a gene will get crossed over during any-point (uniform) or simulated binary crossover)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>line-extension</tt><br>
 <font size=-1>double &gt;= 0.0 </font></td>
 <td valign=top>(for line and intermediate recombination, how far along the line or outside of the hypercube children can be. If this value is zero, all children must be within the hypercube.)


 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-prob</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-prob</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-prob</tt>.<i>gene-number</i><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
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
    public final static String V_ONE_POINT_NO_NOP = "one-nonempty";
    public final static String V_TWO_POINT = "two";
    public final static String V_TWO_POINT_NO_NOP = "two-nonempty";
    public final static String V_ANY_POINT = "any";
    public final static String V_LINE_RECOMB = "line";
    public final static String V_INTERMED_RECOMB = "intermediate";
    public final static String V_SIMULATED_BINARY = "sbx";
    public final static String P_CROSSOVER_DISTRIBUTION_INDEX = "crossover-distribution-index";
    public final static String P_MUTATIONPROB = "mutation-prob";
    public final static String P_CROSSOVERPROB = "crossover-prob";
    public final static String P_GENOMESIZE = "genome-size";
    public final static String P_LINEDISTANCE = "line-extension";
    public final static String V_GEOMETRIC = "geometric";
    public final static String P_GEOMETRIC_PROBABILITY = "geometric-prob";
    public final static String V_UNIFORM = "uniform";
    public final static String P_UNIFORM_MIN = "min-initial-size";
    public final static String P_UNIFORM_MAX = "max-initial-size";
    public final static String P_NUM_SEGMENTS = "num-segments";
    public final static String P_SEGMENT_TYPE = "segment-type";
    public final static String P_SEGMENT_START = "start";
    public final static String P_SEGMENT_END = "end";
    public final static String P_SEGMENT = "segment";
    public final static String P_DUPLICATE_RETRIES = "duplicate-retries";



    public final static int C_ONE_POINT = 0;
    public final static int C_ONE_POINT_NO_NOP = 2;
    public final static int C_TWO_POINT = 4;
    public final static int C_TWO_POINT_NO_NOP = 8;
    public final static int C_ANY_POINT = 128;
    public final static int C_LINE_RECOMB = 256;
    public final static int C_INTERMED_RECOMB = 512;
    public final static int C_SIMULATED_BINARY = 1024;
    
    public final static int C_NONE = 0;
    public final static int C_GEOMETRIC = 1;
    public final static int C_UNIFORM = 2;

    /** How often do we retry until we get a non-duplicate gene? */
    protected int[] duplicateRetries;

    /** Probability that a gene will mutate, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] mutationProbability;
    
    /** Probability that a gene will cross over -- ONLY used in V_ANY_POINT crossover */
    public double crossoverProbability;
    /** What kind of crossover do we have? */
    public int crossoverType;
    /** How big of a genome should we create on initialization? */
    public int genomeSize;
    /** What should the SBX distribution index be? */
    public int crossoverDistributionIndex;
    /** How should we reset the genome? */
    public int genomeResizeAlgorithm;
    /** What's the smallest legal genome? */
    public int minInitialSize;
    /** What's the largest legal genome? */
    public int maxInitialSize;
    /** With what probability would our genome be at least 1 larger than it is now during initialization? */
    public double genomeIncreaseProbability;
    /** How big of chunks should we define for crossover? */
    public int chunksize;
    /** How far along the long a child can be located for line or intermediate recombination */
    public double lineDistance;
    /** Was the initial size determined dynamically? */
    public boolean dynamicInitialSize = false;

    public double mutationProbability(int gene)
        {
        double[] m = mutationProbability;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public int duplicateRetries(int gene)
        {
        int[] m = duplicateRetries;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_VECTORSPECIES);
        }


    protected void setupGenome(final EvolutionState state, final Parameter base)
        {        
        Parameter def = defaultBase();        

        String genomeSizeForm = state.parameters.getString(base.push(P_GENOMESIZE),def.push(P_GENOMESIZE));
        if (genomeSizeForm == null) // clearly an error
            {
            state.output.fatal("No genome size specified.", base.push(P_GENOMESIZE),def.push(P_GENOMESIZE));
            }
        else if (genomeSizeForm.equals(V_GEOMETRIC))
            {
            dynamicInitialSize = true;
            genomeSize = 1;
            genomeResizeAlgorithm = C_GEOMETRIC;
            chunksize = state.parameters.getIntWithDefault(base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE),1);
            if (chunksize != 1)
                state.output.fatal("To use Geometric size initialization, VectorSpecies must have a chunksize of 1",
                    base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));
            minInitialSize = state.parameters.getInt(base.push(P_UNIFORM_MIN),def.push(P_UNIFORM_MIN), 0);
            if (minInitialSize < 0)
                {
                state.output.warning("Gemoetric size initialization used, but no minimum initial size provided.  Assuming minimum is 0.");
                minInitialSize = 0;
                }
            genomeIncreaseProbability = state.parameters.getDoubleWithMax(base.push(P_GEOMETRIC_PROBABILITY),def.push(P_GEOMETRIC_PROBABILITY),0.0, 1.0);
            if (genomeIncreaseProbability < 0.0 || genomeIncreaseProbability >= 1.0)  // note >=
                state.output.fatal("To use Gemoetric size initialization, the genome increase probability must be >= 0.0 and < 1.0",
                    base.push(P_GEOMETRIC_PROBABILITY),def.push(P_GEOMETRIC_PROBABILITY));
            }
        else if (genomeSizeForm.equals(V_UNIFORM))
            {
            dynamicInitialSize = true;
            genomeSize = 1;
            genomeResizeAlgorithm = C_UNIFORM;
            chunksize = state.parameters.getIntWithDefault(base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE),1);
            if (chunksize != 1)
                state.output.fatal("To use Uniform size initialization, VectorSpecies must have a chunksize of 1",
                    base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));
            minInitialSize = state.parameters.getInt(base.push(P_UNIFORM_MIN),def.push(P_UNIFORM_MIN),0);
            if (minInitialSize < 0)
                state.output.fatal("To use Uniform size initialization, you must set a minimum initial size >= 0",
                    base.push(P_UNIFORM_MIN),def.push(P_UNIFORM_MIN));
            maxInitialSize = state.parameters.getInt(base.push(P_UNIFORM_MAX),def.push(P_UNIFORM_MAX),0);
            if (maxInitialSize < 0)
                state.output.fatal("To use Uniform size initialization, you must set a maximum initial size >= 0",
                    base.push(P_UNIFORM_MAX),def.push(P_UNIFORM_MAX));
            if (maxInitialSize < minInitialSize)
                state.output.fatal("To use Uniform size initialization, you must set a maximum initial size >= the minimum initial size",
                    base.push(P_UNIFORM_MAX),def.push(P_UNIFORM_MAX));
            }
        else  // it's a number
            {
            genomeSize = state.parameters.getInt(base.push(P_GENOMESIZE),def.push(P_GENOMESIZE),1);
            if (genomeSize==0)
                state.output.fatal("VectorSpecies must have a genome size > 0",
                    base.push(P_GENOMESIZE),def.push(P_GENOMESIZE));
                        
            genomeResizeAlgorithm = C_NONE;

            chunksize = state.parameters.getIntWithDefault(base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE),1);
            if (chunksize <= 0 || chunksize > genomeSize)
                state.output.fatal("VectorSpecies must have a chunksize which is > 0 and < genomeSize",
                    base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));
            if (genomeSize % chunksize != 0)
                state.output.fatal("VectorSpecies must have a genomeSize which is a multiple of chunksize",
                    base.push(P_CHUNKSIZE),def.push(P_CHUNKSIZE));
            }
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();        

        // We will construct, but NOT set up, a sacrificial individual here.
        // Actual setup is done at the end of this method (in super.setup(...) )
        // The purpose of this sacrificial individual is to enable methods such
        // as inNumericalTypeRange() to run properly, since they require knowledge
        // of which KIND of individual it is.
                
        i_prototype = (Individual)(state.parameters.getInstanceForParameter(
                base.push(P_INDIVIDUAL),def.push(P_INDIVIDUAL),
                Individual. class));

        // this will get thrown away and replaced with a new one during super.setup(...).
                
                
        
        // this might get called twice, I don't think it's a big deal
        setupGenome(state, base);


        // MUTATION

        double _mutationProbability = state.parameters.getDoubleWithMax(base.push(P_MUTATIONPROB), def.push(P_MUTATIONPROB), 0.0, 1.0);
        if (_mutationProbability == -1.0)
            state.output.fatal("Global mutation probability must be between 0.0 and 1.0 inclusive",
                base.push(P_MUTATIONPROB),def.push(P_MUTATIONPROB));
        mutationProbability = fill(new double[genomeSize + 1], _mutationProbability);

        int _duplicateRetries = state.parameters.getIntWithDefault(base.push(P_DUPLICATE_RETRIES), def.push(P_DUPLICATE_RETRIES), 0);
        if (_duplicateRetries < 0)
            {
            state.output.fatal("Duplicate Retries, if defined, must be a value >= 0", base.push(P_DUPLICATE_RETRIES), def.push(P_DUPLICATE_RETRIES));
            }
        duplicateRetries = fill(new int[genomeSize + 1], _duplicateRetries);
        
        // CROSSOVER

        String ctype = state.parameters.getStringWithDefault(base.push(P_CROSSOVERTYPE), def.push(P_CROSSOVERTYPE), null);
        crossoverType = C_ONE_POINT;
        if (ctype==null)
            state.output.warning("No crossover type given for VectorSpecies, assuming one-point crossover",
                base.push(P_CROSSOVERTYPE),def.push(P_CROSSOVERTYPE));
        else if (ctype.equalsIgnoreCase(V_ONE_POINT))
            crossoverType=C_ONE_POINT;  // redundant
        else if (ctype.equalsIgnoreCase(V_ONE_POINT_NO_NOP))
            crossoverType=C_ONE_POINT_NO_NOP;
        else if (ctype.equalsIgnoreCase(V_TWO_POINT))
            crossoverType=C_TWO_POINT;
        else if (ctype.equalsIgnoreCase(V_TWO_POINT_NO_NOP))
            crossoverType=C_TWO_POINT_NO_NOP;
        else if (ctype.equalsIgnoreCase(V_ANY_POINT))
            crossoverType=C_ANY_POINT;
        else if (ctype.equalsIgnoreCase(V_LINE_RECOMB))
            crossoverType=C_LINE_RECOMB;
        else if (ctype.equalsIgnoreCase(V_INTERMED_RECOMB))
            crossoverType=C_INTERMED_RECOMB;
        else if (ctype.equalsIgnoreCase(V_SIMULATED_BINARY))
            crossoverType=C_SIMULATED_BINARY;
        else state.output.fatal("VectorSpecies given a bad crossover type: " + ctype,
            base.push(P_CROSSOVERTYPE),def.push(P_CROSSOVERTYPE));
    
        if (crossoverType==C_LINE_RECOMB || crossoverType==C_INTERMED_RECOMB)
            {
            if (!(this instanceof IntegerVectorSpecies) && !(this instanceof FloatVectorSpecies))
                state.output.fatal("Line and intermediate recombinations are only supported by IntegerVectorSpecies and FloatVectorSpecies", base.push(P_CROSSOVERTYPE), def.push(P_CROSSOVERTYPE));
            lineDistance = state.parameters.getDouble(
                base.push(P_LINEDISTANCE), def.push(P_LINEDISTANCE), 0.0);
            if (lineDistance==-1.0)
                state.output.fatal("If it's going to use line or intermediate recombination, VectorSpecies needs a line extension >= 0.0  (0.25 is common)", base.push(P_LINEDISTANCE), def.push(P_LINEDISTANCE));
            }
        else lineDistance = 0.0;

        if (crossoverType==C_ANY_POINT)
            {
            crossoverProbability = state.parameters.getDoubleWithMax(
                base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB),0.0,0.5);
            if (crossoverProbability==-1.0)
                state.output.fatal("If it's going to use any-point crossover, VectorSpecies must have a crossover probability between 0.0 and 0.5 inclusive",
                    base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB));
            }
        else if (crossoverType==C_SIMULATED_BINARY)
            {
            if (!(this instanceof FloatVectorSpecies))
                state.output.fatal("Simulated binary crossover (SBX) is only supported by FloatVectorSpecies", base.push(P_CROSSOVERTYPE), def.push(P_CROSSOVERTYPE));
            crossoverDistributionIndex = state.parameters.getInt(base.push(P_CROSSOVER_DISTRIBUTION_INDEX), def.push(P_CROSSOVER_DISTRIBUTION_INDEX), 0);
            if (crossoverDistributionIndex < 0)
                state.output.fatal("If FloatVectorSpecies is going to use simulated binary crossover (SBX), the distribution index must be defined and >= 0.",
                    base.push(P_CROSSOVER_DISTRIBUTION_INDEX), def.push(P_CROSSOVER_DISTRIBUTION_INDEX));
            }
        else crossoverProbability = 0.0;

        state.output.exitIfErrors();
                
        if (crossoverType != C_ANY_POINT && state.parameters.exists(base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB)))
            state.output.warning("The 'crossover-prob' parameter may only be used with any-point crossover.  It states the probability that a particular gene will be crossed over.  If you were looking for the probability of crossover happening at *all*, look at the 'likelihood' parameter.",
                base.push(P_CROSSOVERPROB),def.push(P_CROSSOVERPROB));
        



        
        // SEGMENTS
        
        // Set number of segments to 0 by default
        int numSegments = 0;
        // Now check to see if segments of genes (genes having the same min and
        // max values) exist
        if (state.parameters.exists(base.push(P_NUM_SEGMENTS), def.push(P_NUM_SEGMENTS)))
            {
            if (dynamicInitialSize)
                state.output.warnOnce("Using dynamic initial sizing, but per-segment min/max gene declarations.  This is probably wrong.  You probably want to use global min/max declarations.",
                    base.push(P_NUM_SEGMENTS), def.push(P_NUM_SEGMENTS));
                        
            numSegments = state.parameters.getIntWithDefault(base.push(P_NUM_SEGMENTS), 
                def.push(P_NUM_SEGMENTS), 0);
                        
            if(numSegments == 0)
                state.output.warning(
                    "The number of genome segments has been defined to be equal to 0.\n"
                    + "Hence, no genome segments will be defined.", 
                    base.push(P_NUM_SEGMENTS), 
                    def.push(P_NUM_SEGMENTS));
            else if(numSegments < 0)
                state.output.fatal(
                    "Invalid number of genome segments: " + numSegments
                    + "\nIt must be a nonnegative value.", 
                    base.push(P_NUM_SEGMENTS), 
                    def.push(P_NUM_SEGMENTS));
                                                        
            //read the type of segment definition using the default start value
            String segmentType = state.parameters.getStringWithDefault(base.push(P_SEGMENT_TYPE), 
                def.push(P_SEGMENT_TYPE), P_SEGMENT_START);
                        
            if(segmentType.equalsIgnoreCase(P_SEGMENT_START))
                initializeGenomeSegmentsByStartIndices(state, base, def, numSegments);
            else if(segmentType.equalsIgnoreCase(P_SEGMENT_END))
                initializeGenomeSegmentsByEndIndices(state, base, def, numSegments);
            else
                state.output.fatal(
                    "Invalid specification of genome segment type: " + segmentType
                    + "\nThe " + P_SEGMENT_TYPE + " parameter must have the value of " + P_SEGMENT_START + " or " + P_SEGMENT_END, 
                    base.push(P_SEGMENT_TYPE), 
                    def.push(P_SEGMENT_TYPE));
            }
        state.output.exitIfErrors();          

            
            
        // PER-GENE VALUES

        for (int x = 0; x < genomeSize; x++)
            {
            loadParametersForGene(state, x, base, def, "" + x);
            }
        state.output.exitIfErrors();          
              
            
            
        // NOW call super.setup(...), which will in turn set up the prototypical individual
        super.setup(state,base);
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
        // our only per-gene parameter is mutation probablity.
        
        if (state.parameters.exists(base.push(P_MUTATIONPROB).push(postfix), def.push(P_MUTATIONPROB).push(postfix)))
            {
            mutationProbability[index] = state.parameters.getDoubleWithMax(base.push(P_MUTATIONPROB).push(postfix), def.push(P_MUTATIONPROB).push(postfix), 0.0, 1.0);
            if (mutationProbability[index] == -1.0)
                state.output.fatal("Per-gene or per-segment mutation probability must be between 0.0 and 1.0 inclusive",
                    base.push(P_MUTATIONPROB).push(postfix),def.push(P_MUTATIONPROB).push(postfix));
            }

        if (state.parameters.exists(base.push(P_DUPLICATE_RETRIES).push(postfix), def.push(P_DUPLICATE_RETRIES).push(postfix)))
            {
            duplicateRetries[index] = state.parameters.getInt(base.push(P_DUPLICATE_RETRIES).push(postfix), def.push(P_DUPLICATE_RETRIES).push(postfix));
            if (duplicateRetries[index] < 0)
                state.output.fatal("Duplicate Retries for gene " + index + ", if defined must be a value >= 0", 
                    base.push(P_DUPLICATE_RETRIES).push(postfix), def.push(P_DUPLICATE_RETRIES).push(postfix));
            }
                        
        }            

    /** Looks up genome segments using start indices.  Segments run up to the next declared start index.  */
    protected void initializeGenomeSegmentsByStartIndices(final EvolutionState state, 
        final Parameter base, 
        final Parameter def,
        int numSegments)
        {
        //loop in reverse order 
        int previousSegmentEnd = genomeSize;
        int currentSegmentEnd = 0;
                
        for (int i = numSegments - 1; i >= 0; i--)
            {
            //check if the segment data exist
            if (state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START), 
                    def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START)))
                {
                //Read the index of the end gene specifying current segment
                currentSegmentEnd = state.parameters.getInt(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START), 
                    def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START));
                                
                }
            else
                {
                state.output.fatal("Genome segment " + i + " has not been defined!" +
                    "\nYou must specify start indices for " + numSegments + " segment(s)", 
                    base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START),
                    base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_START));
                }
                        
            //check if the start index is valid
            if(currentSegmentEnd >= previousSegmentEnd || currentSegmentEnd < 0)
                state.output.fatal(
                    "Invalid start index value for segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be smaller than " + previousSegmentEnd +
                    " and greater than or equal to  " + 0);
                        
            //check if the index of the first segment is equal to 0
            if(i == 0 && currentSegmentEnd != 0)
                state.output.fatal(
                    "Invalid start index value for the first segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be equal to " + 0);
                        
            //and assign min and max values for all genes in this segment
            for(int j = previousSegmentEnd-1; j >= currentSegmentEnd; j--)
                {
                loadParametersForGene(state, j, base.push(P_SEGMENT).push(""+i), def.push(P_SEGMENT).push(""+i), "");
                }                        
                        
            previousSegmentEnd = currentSegmentEnd;
                        
            }
                
        }
        
    /** Looks up genome segments using end indices.  Segments run from the previously declared end index. */
    protected void initializeGenomeSegmentsByEndIndices(final EvolutionState state, 
        final Parameter base, 
        final Parameter def,
        int numSegments)
        {
        int previousSegmentEnd = -1;  
        int currentSegmentEnd = 0;
        // iterate over segments and set genes values for each segment
        for (int i = 0; i < numSegments; i++)
            {
            //check if the segment data exist
            if (state.parameters.exists(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END), def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END)))
                {
                //Read the index of the end gene specifying current segment
                currentSegmentEnd = state.parameters.getInt(base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END), 
                    def.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END));
                                
                }
            else
                {
                state.output.fatal("Genome segment " + i + " has not been defined!" +
                    "\nYou must specify end indices for " + numSegments + " segment(s)", 
                    base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END),
                    base.push(P_SEGMENT).push(""+i).push(P_SEGMENT_END));
                }
                        
            //check if the end index is valid
            if(currentSegmentEnd <= previousSegmentEnd || currentSegmentEnd >= genomeSize)
                state.output.fatal(
                    "Invalid end index value for segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be greater than " + previousSegmentEnd +
                    " and smaller than " + genomeSize);
                        
            //check if the index of the final segment is equal to the genomeSize
            if(i == numSegments - 1 && currentSegmentEnd != (genomeSize-1))
                state.output.fatal(
                    "Invalid end index value for the last segment " + i + ": " + currentSegmentEnd 
                    +  "\nThe value must be equal to the index of the last gene in the genome:  " + (genomeSize-1));
                        
                        
            //and assign min and max values for all genes in this segment
            for(int j = previousSegmentEnd+1; j <= currentSegmentEnd; j++)
                {
                loadParametersForGene(state, j, base.push(P_SEGMENT).push(""+i), def.push(P_SEGMENT).push(""+i), "");
                }
                        
            previousSegmentEnd = currentSegmentEnd;
            }
        }


    public Individual newIndividual(final EvolutionState state, int thread) 
        
        {
        VectorIndividual newind = (VectorIndividual)(super.newIndividual(state, thread));

        if (genomeResizeAlgorithm == C_NONE)
            newind.reset( state, thread );
        else if (genomeResizeAlgorithm == C_UNIFORM)
            {
            int size = state.random[thread].nextInt(maxInitialSize - minInitialSize + 1) + minInitialSize;
            newind.reset(state, thread, size);
            }
        else if (genomeResizeAlgorithm == C_GEOMETRIC)
            {
            int size = minInitialSize;
            while(state.random[thread].nextBoolean(genomeIncreaseProbability)) size++;
            newind.reset(state, thread, size);
            }
                        
        return newind;
        }




    // These convenience methods are used by subclasses to fill arrays and check to see if
    // arrays contain certain values.

    /** Utility method: fills the array with the given value and returns it. */
    protected long[] fill(long[] array, long val)
        {
        for(int i =0; i < array.length; i++) array[i] = val;
        return array;
        }
        
    /** Utility method: fills the array with the given value and returns it. */
    protected int[] fill(int[] array, int val)
        {
        for(int i =0; i < array.length; i++) array[i] = val;
        return array;
        }
        
    /** Utility method: fills the array with the given value and returns it. */
    protected boolean[] fill(boolean[] array, boolean val)
        {
        for(int i =0; i < array.length; i++) array[i] = val;
        return array;
        }
        
    /** Utility method: fills the array with the given value and returns it. */
    protected double[] fill(double[] array, double val)
        {
        for(int i =0; i < array.length; i++) array[i] = val;
        return array;
        }

    /** Utility method: returns the first array slot which contains the given value, else -1. */
    protected int contains(boolean[] array, boolean val)
        {
        for(int i =0; i < array.length; i++)
            if (array[i] == val) return i;
        return -1;
        }
        
    /** Utility method: returns the first array slot which contains the given value, else -1. */
    protected int contains(long[] array, long val)
        {
        for(int i =0; i < array.length; i++)
            if (array[i] == val) return i;
        return -1;
        }
        
    /** Utility method: returns the first array slot which contains the given value, else -1. */
    protected int contains(int[] array, int val)
        {
        for(int i =0; i < array.length; i++)
            if (array[i] == val) return i;
        return -1;
        }
        
    /** Utility method: returns the first array slot which contains the given value, else -1. */
    protected int contains(double[] array, double val)
        {
        for(int i =0; i < array.length; i++)
            if (array[i] == val) return i;
        return -1;
        }
    }


