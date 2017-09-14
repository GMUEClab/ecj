package ec.vector;

import ec.*;
import ec.util.*;

/* 
 * FloatVectorSpecies.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Sean Luke
 */

/**
 * FloatVectorSpecies is a subclass of VectorSpecies with special
 * constraints for floating-point vectors, namely FloatVectorIndividual and
 * DoubleVectorIndividual.
 *
 * <p>FloatVectorSpecies can specify a number of parameters globally, per-segment, and per-gene.
 * See <a href="VectorSpecies.html">VectorSpecies</a> for information on how to this works.
 *
 * <p>FloatVectorSpecies defines a minimum and maximum gene value.  These values
 * are used during initialization and, depending on whether <tt>mutation-bounded</tt>
 * is true, also during various mutation algorithms to guarantee that the gene value
 * will not exceed these minimum and maximum bounds.
 *
 * <p>
 * FloatVectorSpecies provides support for five ways of mutating a gene.
 * <ul>
 * <li><b>reset</b> Replacing the gene's value with a value uniformly drawn from the gene's
 * range (the default behavior).</li>
 * <li><b>gauss</b>Perturbing the gene's value with gaussian noise; if the gene-by-gene range 
 * is used, than the standard deviation is scaled to reflect each gene's range. 
 * If the gaussian mutation's standard deviation is too large for the range,
 * than there's a large probability the mutated value will land outside range.
 * We will try again a number of times (100) before giving up and using the 
 * previous mutation method.</li>
 * <li><b>polynomial</b> Perturbing the gene's value with noise chosen from a <i>polynomial distribution</i>,
 * similar to the gaussian distribution.  The polynomial distribution was popularized
 * by Kalyanmoy Deb and is found in many of his publications (see http://www.iitk.ac.in/kangal/deb.shtml).
 * The polynomial distribution has two options.  First, there is the <i>index</i>.  This
 * variable defines the shape of the distribution and is in some sense the equivalent of the
 * standard deviation in the gaussian distribution.  The index is an integer.  If it is zero,
 * the polynomial distribution is simply the uniform distribution from [1,-1].  If it is 1, the
 * polynomial distribution is basically a triangular distribution from [1,-1] peaking at 0.  If
 * it is 2, the polynomial distribution follows a squared function, again peaking at 0.  Larger
 * values result in even more peaking and narrowness.  The default values used in nearly all of
 * the NSGA-II and Deb work is 20.  Second, there is whether or not the value is intended for
 * <i>bounded</i> genes.  The default polynomial distribution is used when we assume the gene can
 * take on literally any value, even beyond the min and max values.  For genes which are restricted
 * to be between min and max, there is an alternative version of the polynomial distribution, used by
 * Deb's team but not discussed much in the literature, desiged for that situation.  We assume boundedness
 * by default, and have found it to be somewhat better for NSGA-II and SPEA2 problems.  For a description
 * of this alternative version, see "A Niched-Penalty Approach for Constraint Handling in Genetic Algorithms"
 * by Kalyanmoy Deb and Samir Agrawal.  Deb's default implementation bounds the result to min or max;
 * instead ECJ's implementation of the polynomial distribution retries until it finds a legal value.  This
 * will be just fine for ranges like [0,1], but for smaller ranges you may be waiting a long time.
 * <li><b>integer-reset</b> Replacing the gene's value with a value uniformly drawn from the gene's range
 * but restricted to only integers.
 * <li><b>integer-random-walk</b> Replacing the gene's value by performing a random walk starting at the gene
 * value.  The random walk either adds 1 or subtracts 1 (chosen at random), then does a coin-flip
 * to see whether to continue the random walk.  When the coin-flip finally comes up false, the gene value
 * is set to the current random walk position.
 * </ul>
 *
 * <p>
 * FloatVectorSpecies provides support for two ways of initializing a gene.  The initialization procedure
 * is determined by the choice of mutation procedure as described above.  If the mutation is floating-point
 * (<tt>reset, gauss, polynomial</tt>), then initialization will be done by resetting the gene
 * to uniformly chosen floating-point value between the minimum and maximum legal gene values, inclusive.
 * If the mutation is integer (<tt>integer-reset, integer-random-walk</tt>), then initialization will be done
 * by performing the same kind of reset, but restricting values to integers only.
 * 
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>min-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>min-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>min-gene</tt>.<i>gene-number</i><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 <td valign=top>(probability that a gene will get mutated over default mutation)</td></tr>
 * <font size=-1>double (default=0.0)</font></td>
 * <td valign=top>(the minimum gene value)</td>
 * </tr>
 * 
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>max-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>max-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>max-gene</tt>.<i>gene-number</i><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 <td valign=top>(probability that a gene will get mutated over default mutation)</td></tr>
 * <font size=-1>double &gt;= <i>base</i>.min-gene</font></td>
 * <td valign=top>(the maximum gene value)</td>
 * </tr>
 * 
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-prob</tt>.<i>gene-number</i><br>
 * <font size=-1><tt>reset</tt>, <tt>gauss</tt>, <tt>polynomial</tt>, <tt>integer-reset</tt>, or <tt>integer-random-walk</tt> (default=<tt>reset</tt>)</font></td>
 * <td valign=top>(the mutation type)</td>
 * </tr>
 * 
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-stdev</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-stdev</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-stdev</tt>.<i>gene-number</i><br>
 * <font size=-1>double &ge; 0</font></td>
 * <td valign=top>(the standard deviation or the gauss perturbation)</td>
 * </tr>
 * 
 * <tr>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>distribution-index</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>distribution-index</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>distribution-index</tt>.<i>gene-number</i><br>
 * <font size=-1>int &ge; 0</font></td>
 * <td valign=top>(the mutation distribution index for the polynomial mutation distribution)</td>
 * </tr>
 * 
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>alternative-polynomial-version</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>alternative-polynomial-version</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>alternative-polynomial-version</tt>.<i>gene-number</i><br>
 *  <font size=-1>boolean (default=true)</font></td>
 *  <td valign=top>(whether to use the "bounded" variation of the polynomial mutation or the standard ("unbounded") version)</td>
 * </tr>
 *
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>random-walk-probability</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>random-walk-probability</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>random-walk-probability</tt>.<i>gene-number</i><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 *  <td valign=top>(the probability that a random walk will continue.  Random walks go up or down by 1.0 until the coin flip comes up false.)</td>
 * </tr>
 * 
 * <tr>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-bounded</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-bounded</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-bounded</tt>.<i>gene-number</i><br>
 *  <font size=-1>boolean (default=true)</font></td>
 *  <td valign=top>(whether mutation is restricted to only being within the min/max gene values.  Does not apply to SimulatedBinaryCrossover (which is always bounded))</td>
 * </tr>
 * 
 <tr><td>&nbsp;
 * <td valign=top><i>base</i>.<tt>out-of-bounds-retries</tt><br>
 *  <font size=-1>int &ge; 0 (default=100)</font></td>
 *  <td valign=top>(number of times the gaussian mutation got the gene out of range 
 *  before we give up and reset the gene's value; 0 means "never give up")</td>
 * </tr>
 *
 * </table>
 * @author Sean Luke, Gabriel Balan, Rafal Kicinger
 * @version 2.0
 */
 
public class FloatVectorSpecies extends VectorSpecies
    {
    public final static String P_MINGENE = "min-gene";

    public final static String P_MAXGENE = "max-gene";

    public final static String P_MUTATIONTYPE = "mutation-type";

    public final static String P_STDEV = "mutation-stdev";

    public final static String P_MUTATION_DISTRIBUTION_INDEX = "mutation-distribution-index";

    public final static String P_POLYNOMIAL_ALTERNATIVE = "alternative-polynomial-version";

    public final static String V_RESET_MUTATION = "reset";

    public final static String V_GAUSS_MUTATION = "gauss";

    public final static String V_POLYNOMIAL_MUTATION = "polynomial";
    
    public final static String V_INTEGER_RANDOM_WALK_MUTATION = "integer-random-walk";

    public final static String V_INTEGER_RESET_MUTATION = "integer-reset";

    public final static String P_RANDOM_WALK_PROBABILITY = "random-walk-probability";

    public final static String P_OUTOFBOUNDS_RETRIES = "out-of-bounds-retries";

    public final static String P_MUTATION_BOUNDED = "mutation-bounded";

    public final static int C_RESET_MUTATION = 0;

    public final static int C_GAUSS_MUTATION = 1;

    public final static int C_POLYNOMIAL_MUTATION = 2;

    public final static int C_INTEGER_RESET_MUTATION = 3;

    public final static int C_INTEGER_RANDOM_WALK_MUTATION = 4;
        

    /** Min-gene value, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] minGene;

    /** Max-gene value, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] maxGene;

    /** Mutation type, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected int[] mutationType;

    /** Standard deviation for Gaussian Mutation, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] gaussMutationStdev;

    /** Whether mutation is bounded to the min- and max-gene values, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected boolean[] mutationIsBounded;

    /** Whether the mutationIsBounded value was defined, per gene.
        Used internally only.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    boolean mutationIsBoundedDefined;

    /** The distribution index for Polynomial Mutation, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected int[] mutationDistributionIndex;

    /** Whether the Polynomial Mutation method is the "alternative" method, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected boolean[] polynomialIsAlternative;

    /** Whether the polymialIsAlternative value was defined, per gene.
        Used internally only.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    boolean polynomialIsAlternativeDefined;

    /** The continuation probability for Integer Random Walk Mutation, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] randomWalkProbability;

    /** The number of times Polynomial Mutation or Gaussian Mutation retry for valid
        numbers until they get one. */
    public int outOfBoundsRetries;
    public static final int DEFAULT_OUT_OF_BOUNDS_RETRIES = 100;
                
    static final double SIMULATED_BINARY_CROSSOVER_EPS = 1.0e-14;   

    public void outOfRangeRetryLimitReached(EvolutionState state)
        {
        state.output.warnOnce(
            "The limit of 'out-of-range' retries for gaussian or polynomial mutation (" + DEFAULT_OUT_OF_BOUNDS_RETRIES + ") was reached.");
        }
    
    public double maxGene(int gene)
        {
        double[] m = maxGene;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public double minGene(int gene)
        {
        double[] m = minGene;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public int mutationType(int gene)
        {
        int[] m = mutationType;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public double gaussMutationStdev(int gene)
        {
        double[] m = gaussMutationStdev;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public boolean mutationIsBounded(int gene)
        {
        boolean[] m = mutationIsBounded;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public int mutationDistributionIndex(int gene)
        {
        int[] m = mutationDistributionIndex;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public boolean polynomialIsAlternative(int gene)
        {
        boolean[] m = polynomialIsAlternative;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public double randomWalkProbability(int gene)
        {
        double[] m = randomWalkProbability;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }


    public boolean inNumericalTypeRange(double geneVal)
        {
        if (i_prototype instanceof FloatVectorIndividual)
            return (geneVal <= Float.MAX_VALUE && geneVal >= -Float.MAX_VALUE);
        else if (i_prototype instanceof DoubleVectorIndividual)
            return true; // geneVal is valid for all double
        else
            return false; // dunno what the individual is...
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();
        
        setupGenome(state, base);
        
        // OUT OF BOUNDS RETRIES

        outOfBoundsRetries = state.parameters.getIntWithDefault(base.push(P_OUTOFBOUNDS_RETRIES), def.push(P_OUTOFBOUNDS_RETRIES), DEFAULT_OUT_OF_BOUNDS_RETRIES);
        if(outOfBoundsRetries<0)
            state.output.fatal("Out of bounds retries must be >= 0.", base.push(P_OUTOFBOUNDS_RETRIES), def.push(P_OUTOFBOUNDS_RETRIES));


        // CREATE THE ARRAYS
        
        minGene = new double[genomeSize + 1];
        maxGene = new double[genomeSize + 1];
        mutationType = fill(new int[genomeSize + 1], -1);
        gaussMutationStdev = fill(new double[genomeSize + 1], Double.NaN);
        mutationDistributionIndex = fill(new int[genomeSize + 1], -1);
        polynomialIsAlternative = new boolean[genomeSize + 1];
        mutationIsBounded = new boolean[genomeSize + 1];
        randomWalkProbability = fill(new double[genomeSize + 1], Double.NaN);
        

        // GLOBAL MIN/MAX GENES
        
        double _minGene = state.parameters.getDoubleWithDefault(base.push(P_MINGENE), def.push(P_MINGENE), 0);
        double _maxGene = state.parameters.getDouble(base.push(P_MAXGENE), def.push(P_MAXGENE), _minGene);
        if (_maxGene < _minGene)
            state.output.fatal("FloatVectorSpecies must have a default min-gene which is <= the default max-gene",
                base.push(P_MAXGENE), def.push(P_MAXGENE));
        fill(minGene, _minGene);
        fill(maxGene, _maxGene);



        /// MUTATION

        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE), null);
        int _mutationType = C_RESET_MUTATION;
        if (mtype == null)
            state.output.warning("No global mutation type given for FloatVectorSpecies, assuming 'reset' mutation",
                base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            _mutationType = C_RESET_MUTATION; // redundant
        else if (mtype.equalsIgnoreCase(V_POLYNOMIAL_MUTATION))
            _mutationType = C_POLYNOMIAL_MUTATION; // redundant
        else if (mtype.equalsIgnoreCase(V_GAUSS_MUTATION))
            _mutationType = C_GAUSS_MUTATION;
        else if (mtype.equalsIgnoreCase(V_INTEGER_RESET_MUTATION))
            {
            _mutationType = C_INTEGER_RESET_MUTATION;
            state.output.warnOnce("Integer Reset Mutation used in FloatVectorSpecies.  Be advised that during initialization these genes will only be set to integer values.");
            }
        else if (mtype.equalsIgnoreCase(V_INTEGER_RANDOM_WALK_MUTATION))
            {
            _mutationType = C_INTEGER_RANDOM_WALK_MUTATION;
            state.output.warnOnce("Integer Random Walk Mutation used in FloatVectorSpecies.  Be advised that during initialization these genes will only be set to integer values.");
            }
        else
            state.output.fatal("FloatVectorSpecies given a bad mutation type: "
                + mtype, base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        fill(mutationType, _mutationType);


        if (_mutationType == C_POLYNOMIAL_MUTATION)
            {
            int _mutationDistributionIndex = state.parameters.getInt(base.push(P_MUTATION_DISTRIBUTION_INDEX), def.push(P_MUTATION_DISTRIBUTION_INDEX), 0);
            if (_mutationDistributionIndex < 0)
                state.output.fatal("If FloatVectorSpecies is going to use polynomial mutation as its global mutation type, the global distribution index must be defined and >= 0.",
                    base.push(P_MUTATION_DISTRIBUTION_INDEX), def.push(P_MUTATION_DISTRIBUTION_INDEX));
            fill(mutationDistributionIndex, _mutationDistributionIndex);
            
            if (!state.parameters.exists(base.push(P_POLYNOMIAL_ALTERNATIVE), def.push(P_POLYNOMIAL_ALTERNATIVE)))
                state.output.warning("FloatVectorSpecies is using polynomial mutation as its global mutation type, but " + P_POLYNOMIAL_ALTERNATIVE + " is not defined.  Assuming 'true'");
            boolean _polynomialIsAlternative = state.parameters.getBoolean(base.push(P_POLYNOMIAL_ALTERNATIVE), def.push(P_POLYNOMIAL_ALTERNATIVE), true);
            fill(polynomialIsAlternative, _polynomialIsAlternative);
            polynomialIsAlternativeDefined = true;
            }
        if (_mutationType == C_GAUSS_MUTATION)
            {
            double _gaussMutationStdev = state.parameters.getDouble(base.push(P_STDEV),def.push(P_STDEV), 0);
            if (_gaussMutationStdev <= 0)
                state.output.fatal("If it's going to use gaussian mutation as its global mutation type, FloatvectorSpecies must have a strictly positive standard deviation",
                    base.push(P_STDEV), def.push(P_STDEV));
            fill(gaussMutationStdev, _gaussMutationStdev);
            }
        if (_mutationType == C_INTEGER_RANDOM_WALK_MUTATION)
            {
            double _randomWalkProbability = state.parameters.getDoubleWithMax(base.push(P_RANDOM_WALK_PROBABILITY),def.push(P_RANDOM_WALK_PROBABILITY), 0.0, 1.0);
            if (_randomWalkProbability <= 0)
                state.output.fatal("If it's going to use random walk mutation as its global mutation type, FloatvectorSpecies must a random walk mutation probability between 0.0 and 1.0.",
                    base.push(P_RANDOM_WALK_PROBABILITY), def.push(P_RANDOM_WALK_PROBABILITY));
            fill(randomWalkProbability, _randomWalkProbability);
            }        
        
        if (_mutationType == C_POLYNOMIAL_MUTATION || 
            _mutationType == C_GAUSS_MUTATION ||
            _mutationType == C_INTEGER_RANDOM_WALK_MUTATION )
            {
            if (!state.parameters.exists(base.push(P_MUTATION_BOUNDED), def.push(P_MUTATION_BOUNDED)))
                state.output.warning("FloatVectorSpecies is using gaussian, polynomial, or integer random walk mutation as its global mutation type, but " + P_MUTATION_BOUNDED + " is not defined.  Assuming 'true'");
            boolean _mutationIsBounded = state.parameters.getBoolean(base.push(P_MUTATION_BOUNDED), def.push(P_MUTATION_BOUNDED), true);
            fill(mutationIsBounded, _mutationIsBounded);
            mutationIsBoundedDefined = true;
            }
            


        // CALLING SUPER
                
        // This will cause the remaining parameters to get set up, and
        // all per-gene and per-segment parameters to get set up as well.
        // We need to do this at this point because the global params need
        // to get set up first, and also prior to the prototypical individual
        // getting setup at the end of super.setup(...).

        super.setup(state, base);




              
        // VERIFY
        
        for(int x=0 ; x < genomeSize + 1; x++)
            {
            if (maxGene[x] != maxGene[x])  // uh oh, NaN
                state.output.fatal("FloatVectorSpecies found that max-gene[" + x + "] is NaN");

            if (minGene[x] != minGene[x])  // uh oh, NaN
                state.output.fatal("FloatVectorSpecies found that min-gene[" + x + "] is NaN");

            if (maxGene[x] < minGene[x])
                state.output.fatal("FloatVectorSpecies must have a min-gene[" + x + "] which is <= the max-gene[" + x + "]");

            // check to see if these longs are within the data type of the particular individual
            if (!inNumericalTypeRange(minGene[x]))
                state.output.fatal("This FloatvectorSpecies has a prototype of the kind: "
                    + i_prototype.getClass().getName()
                    + ", but doesn't have a min-gene["
                    + x
                    + "] value within the range of this prototype's genome's data types");
            if (!inNumericalTypeRange(maxGene[x]))
                state.output.fatal("This FloatvectorSpecies has a prototype of the kind: "
                    + i_prototype.getClass().getName()
                    + ", but doesn't have a max-gene["
                    + x
                    + "] value within the range of this prototype's genome's data types");
                    
            if (((mutationType[x] == FloatVectorSpecies.C_INTEGER_RESET_MUTATION || 
                        mutationType[x] == FloatVectorSpecies.C_INTEGER_RANDOM_WALK_MUTATION))  // integer type
                && (maxGene[x] != Math.floor(maxGene[x])))
                state.output.fatal("Gene " + x + " is using an integer mutation method, but the max gene is not an integer (" + maxGene[x] + ").");
                                 
            if (((mutationType[x] == FloatVectorSpecies.C_INTEGER_RESET_MUTATION || 
                        mutationType[x] == FloatVectorSpecies.C_INTEGER_RANDOM_WALK_MUTATION))  // integer type
                && (minGene[x] != Math.floor(minGene[x])))
                state.output.fatal("Gene " + x + " is using an integer mutation method, but the min gene is not an integer (" + minGene[x] + ").");
            }       
                        
        /*
        //Debugging
        for(int i = 0; i < minGene.length; i++)
        System.out.println("Min: " + minGene[i] + ", Max: " + maxGene[i]);
        */
        }
    
    
    
    protected void loadParametersForGene(EvolutionState state, int index, Parameter base, Parameter def, String postfix)
        {       
        super.loadParametersForGene(state, index, base, def, postfix);
        
        double minVal = state.parameters.getDoubleWithDefault(base.push(P_MINGENE).push(postfix), def.push(P_MINGENE).push(postfix), Double.NaN);
        if (minVal == minVal)  // it's not NaN
            {                        
            //check if the value is in range
            if (!inNumericalTypeRange(minVal))
                state.output.fatal("Min Gene Value out of range for data type " + i_prototype.getClass().getName(),
                    base.push(P_MINGENE).push(postfix), 
                    base.push(P_MINGENE).push(postfix));
            else minGene[index] = minVal;

            if (dynamicInitialSize)
                state.output.warnOnce("Using dynamic initial sizing, but per-gene or per-segment min-gene declarations.  This is probably wrong.  You probably want to use global min/max declarations.",
                    base.push(P_MINGENE).push(postfix), 
                    base.push(P_MINGENE).push(postfix));
            }
            
        double maxVal = state.parameters.getDoubleWithDefault(base.push(P_MAXGENE).push(postfix), def.push(P_MAXGENE).push(postfix), Double.NaN);
        if (maxVal == maxVal)  // it's not NaN
            {                        
            //check if the value is in range
            if (!inNumericalTypeRange(maxVal))
                state.output.fatal("Max Gene Value out of range for data type " + i_prototype.getClass().getName(),
                    base.push(P_MAXGENE).push(postfix), 
                    base.push(P_MAXGENE).push(postfix));
            else maxGene[index] = maxVal;

            if (dynamicInitialSize)
                state.output.warnOnce("Using dynamic initial sizing, but per-gene or per-segment max-gene declarations.  This is probably wrong.  You probably want to use global min/max declarations.",
                    base.push(P_MAXGENE).push(postfix), 
                    base.push(P_MAXGENE).push(postfix));
            }
        
        if ((maxVal == maxVal && !(minVal == minVal)))
            state.output.warning("Max Gene specified but not Min Gene", base.push(P_MINGENE).push(postfix), def.push(P_MINGENE).push(postfix));
                
        if ((minVal == minVal && !(maxVal == maxVal)))
            state.output.warning("Min Gene specified but not Max Gene", base.push(P_MAXGENE).push(postfix), def.push(P_MINGENE).push(postfix));


        /// MUTATION
                   
        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix), null);
        int mutType = -1;
        if (mtype == null) { }  // we're cool
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            mutType = mutationType[index] = C_RESET_MUTATION; 
        else if (mtype.equalsIgnoreCase(V_POLYNOMIAL_MUTATION))
            mutType = mutationType[index] = C_POLYNOMIAL_MUTATION;
        else if (mtype.equalsIgnoreCase(V_GAUSS_MUTATION))
            mutType = mutationType[index] = C_GAUSS_MUTATION;
        else if (mtype.equalsIgnoreCase(V_INTEGER_RESET_MUTATION))      
            {
            mutType = mutationType[index] = C_INTEGER_RESET_MUTATION;
            state.output.warnOnce("Integer Reset Mutation used in FloatVectorSpecies.  Be advised that during initialization these genes will only be set to integer values.");
            }
        else if (mtype.equalsIgnoreCase(V_INTEGER_RANDOM_WALK_MUTATION))
            {
            mutType = mutationType[index] = C_INTEGER_RANDOM_WALK_MUTATION;
            state.output.warnOnce("Integer Random Walk Mutation used in FloatVectorSpecies.  Be advised that during initialization these genes will only be set to integer values.");
            }
        else
            state.output.fatal("FloatVectorSpecies given a bad mutation type: " + mtype, 
                base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix));


        if (mutType == C_POLYNOMIAL_MUTATION)
            {
            if (state.parameters.exists(base.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix), def.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix)))
                {
                mutationDistributionIndex[index] = state.parameters.getInt(base.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix), def.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix), 0);
                if (mutationDistributionIndex[index] < 0)
                    state.output.fatal("If FloatVectorSpecies is going to use polynomial mutation as a per-gene or per-segment type, the global distribution index must be defined and >= 0.",
                        base.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix), def.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix));
                }
            else if (mutationDistributionIndex[index] != mutationDistributionIndex[index])  // it's NaN
                state.output.fatal("If FloatVectorSpecies is going to use polynomial mutation as a per-gene or per-segment type, either the global or per-gene/per-segment distribution index must be defined and >= 0.",
                    base.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix), def.push(P_MUTATION_DISTRIBUTION_INDEX).push(postfix));
            
            if (state.parameters.exists(base.push(P_POLYNOMIAL_ALTERNATIVE).push(postfix), def.push(P_POLYNOMIAL_ALTERNATIVE).push(postfix)))
                {
                polynomialIsAlternative[index] = state.parameters.getBoolean(base.push(P_POLYNOMIAL_ALTERNATIVE).push(postfix), def.push(P_POLYNOMIAL_ALTERNATIVE).push(postfix), true);
                }
            }
        if (mutType == C_GAUSS_MUTATION)
            {
            if (state.parameters.exists(base.push(P_STDEV).push(postfix),def.push(P_STDEV).push(postfix)))
                {
                gaussMutationStdev[index] = state.parameters.getDouble(base.push(P_STDEV).push(postfix),def.push(P_STDEV).push(postfix), 0);
                if (gaussMutationStdev[index] <= 0)
                    state.output.fatal("If it's going to use gaussian mutation as a per-gene or per-segment type, it must have a strictly positive standard deviation",
                        base.push(P_STDEV).push(postfix), def.push(P_STDEV).push(postfix));
                }
            else if (gaussMutationStdev[index] != gaussMutationStdev[index])
                state.output.fatal("If FloatVectorSpecies is going to use gaussian mutation as a per-gene or per-segment type, either the global or per-gene/per-segment standard deviation must be defined.",
                    base.push(P_STDEV).push(postfix), def.push(P_STDEV).push(postfix));
            }
        if (mutType == C_INTEGER_RANDOM_WALK_MUTATION)
            {
            if (state.parameters.exists(base.push(P_RANDOM_WALK_PROBABILITY).push(postfix),def.push(P_RANDOM_WALK_PROBABILITY).push(postfix)))
                {
                randomWalkProbability[index] = state.parameters.getDoubleWithMax(base.push(P_RANDOM_WALK_PROBABILITY).push(postfix),def.push(P_RANDOM_WALK_PROBABILITY).push(postfix), 0.0, 1.0);
                if (randomWalkProbability[index] <= 0)
                    state.output.fatal("If it's going to use random walk mutation as a per-gene or per-segment type, FloatVectorSpecies must a random walk mutation probability between 0.0 and 1.0.",
                        base.push(P_RANDOM_WALK_PROBABILITY).push(postfix), def.push(P_RANDOM_WALK_PROBABILITY).push(postfix));
                }
            else
                state.output.fatal("If FloatVectorSpecies is going to use polynomial mutation as a per-gene or per-segment type, either the global or per-gene/per-segment random walk mutation probability must be defined.",
                    base.push(P_RANDOM_WALK_PROBABILITY).push(postfix), def.push(P_RANDOM_WALK_PROBABILITY).push(postfix));
            }  
        
        if (mutType == C_POLYNOMIAL_MUTATION ||
            mutType == C_GAUSS_MUTATION ||
            mutType == C_INTEGER_RANDOM_WALK_MUTATION)
            {
            if (state.parameters.exists(base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix)))
                {
                mutationIsBounded[index] = state.parameters.getBoolean(base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix), true);
                }
            else if (!mutationIsBoundedDefined)
                state.output.fatal("If FloatVectorSpecies is going to use gaussian, polynomial, or integer random walk mutation as a per-gene or per-segment type, the mutation bounding must be defined.",
                    base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix));
            }
         
        }
    
        
    }
