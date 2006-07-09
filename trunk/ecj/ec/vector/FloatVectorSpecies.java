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
 * FloatVectorSpecies is a subclass of VectorSpecies with special constraints
 * for floating-point vectors, namely FloatVectorIndividual and
 * DoubleVectorIndividual.
 * 
 * <p>
 * FloatVectorSpecies can specify numeric constraints on gene values in one of
 * two ways. First, they can simply specify a default min and max value. Or they
 * can specify an array of min/max pairs, one pair per gene. FloatVectorSpecies
 * will check to see if the second approach is to be used by looking for
 * parameter <i>base</i>.<tt>max-gene</tt>.0 in the array -- if it
 * exists, FloatvectorSpecies will assume all such parameters exist, and will
 * load up to the genome length. If a parameter is missing, in this range, a
 * warning will be issued during Individual setup. If the array is shorter than
 * the genome, then the default min/max values will be used for the remaining
 * genome values. This means that even if you specify the array, you need to
 * still specify the default min/max values just in case.
 * 
 * <p>
 * FloatVectorSpecies provides support for two ways of mutating a gene:
 * <ul>
 * <li>replacing the gene's value with a value uniformly-drawn from the gene's
 * range (the default behavior, legacy from the previous versions).</li>
 * <li>perturbing the gene's value with gaussian noise; if the gene-by-gene range 
 * is used, than the standard deviation is scaled to reflect each gene's range. 
 * If the gaussian mutation's standard deviation is too large for the range,
 * than there's a large probability the mutated value will land outside range.
 * We will resample a number of times (100) before giving up and using the 
 * previous mutation method.</li>
 * </ul>
 * 
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>min-gene</tt><br>
 * <font size=-1>double (default=0.0)</font></td>
 * <td valign=top>(the minimum gene value)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max-gene</tt><br>
 * <font size=-1>double &gt;= <i>base</i>.min-gene</font></td>
 * <td valign=top>(the maximum gene value)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>min-gene</tt>.<i>i</i><br>
 * <font size=-1>double (default=<i>base</i>.<tt>min-gene</tt>)</font></td>
 * <td valign=top>(the minimum gene value for gene <i>i</i>)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>max-gene</tt>.<i>i</i><br>
 * <font size=-1>double &gt;= <i>base</i>.min-gene.<i>i</i> (default=<i>base</i>.<tt>max-gene</tt>)</font></td>
 * <td valign=top>(the maximum gene value for gene <i>i</i>)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>mutation-type</tt><br>
 * <font size=-1><tt>reset</tt> or <tt>gauss</tt> (default=<tt>reset</tt>)</font></td>
 * <td valign=top>(the mutation type)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i>.<tt>mutation-stdev</tt><br>
 * <font size=-1>double &ge; 0</font></td>
 * <td valign=top>(the standard deviation or the gauss perturbation)</td>
 * </tr>
 * 
 *      <tr><td valign=top><i>base</i>.<tt>out-of-bounds-retries</tt><br>
 *  <font size=-1>int &ge; 0 (default=100)</font></td>
 *  <td valign=top>(number of times the gaussian mutation got the gene out of range 
 *  before we give up and reset the gene's value; 0 means "never give up")</td></tr>
 * 
 * </table>
 * 
 * @author Sean Luke and Gabriel Balan
 * @version 2.0
 */
public class FloatVectorSpecies extends VectorSpecies
    {
    public final static String P_MINGENE = "min-gene";

    public final static String P_MAXGENE = "max-gene";

    public final static String P_MUTATIONTYPE = "mutation-type";

    public static String P_STDEV = "mutation-stdev";

    public final static String V_RESET_MUTATION = "reset";

    public final static String V_GAUSS_MUTATION = "gauss";

    public final static int C_RESET_MUTATION = 0;

    public final static int C_GAUSS_MUTATION = 1;

    public final static String P_OUTOFBOUNDS_RETRIES = "out-of-bounds-retries";

    public double minGene;

    public double maxGene;

    /** Set to null if not specified */
    public double[] minGenes;

    /** Set to null if not specified */
    public double[] maxGenes;

    /** What kind of mutation do we have? */
    public int mutationType;

    public double gaussMutationStdev;

    /**
     * Set to null if not specified
     * 
     * If individualGeneMinMaxUsed, that this is used too.
     */
    public double[] gaussMutationStdevs;

    public int outOfRangeRetries=100;

    private boolean outOfRangeRetriesWarningPrinted = false;
    public void outOfRangeRetryLimitReached(EvolutionState state)
        {
        if(!outOfRangeRetriesWarningPrinted)
            {
            outOfRangeRetriesWarningPrinted=true;
            state.output.warning(
                "The limit of 'out-of-range' retries for gaussian mutation was reached.");
            }
        }
        
        
    public final boolean individualGeneMinMaxUsed()
        {
        return (maxGenes != null);
        }

    public final double maxGene(int gene)
        {
        if (maxGenes != null && gene >= 0 && gene < maxGenes.length)
            return maxGenes[gene];
        else
            return maxGene;
        }

    public final double minGene(int gene)
        {
        if (minGenes != null && gene >= 0 && gene < minGenes.length)
            return minGenes[gene];
        else
            return minGene;
        }

    public final double gaussMutationStdev(int gene)
        {
        if (maxGenes != null && gene >= 0 && gene < maxGenes.length)
            return gaussMutationStdevs[gene];
        else
            return gaussMutationStdev;
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
        super.setup(state, base);

        Parameter def = defaultBase();

        minGene = state.parameters.getDoubleWithDefault(base.push(P_MINGENE),
                                                        def.push(P_MINGENE), 0);
        maxGene = state.parameters.getDouble(base.push(P_MAXGENE), 
											def.push(P_MAXGENE), minGene);
        if (maxGene < minGene)
            state.output
                .fatal(
                    "FloatVectorSpecies must have a default min-gene which is <= the default max-gene",
                    base.push(P_MAXGENE), def.push(P_MAXGENE));

        // check to see if these longs are within the data type of the
        // particular individual
        if (!inNumericalTypeRange(minGene))
            state.output
                .fatal(
                    "This FloatvectorSpecies has a prototype of the kind: "
                    + i_prototype.getClass().getName()
                    + ", but doesn't have a min-gene value within the range of this prototype's genome's data types",
                    base.push(P_MINGENE), def.push(P_MINGENE));
        if (!inNumericalTypeRange(maxGene))
            state.output
                .fatal(
                    "This FloatvectorSpecies has a prototype of the kind: "
                    + i_prototype.getClass().getName()
                    + ", but doesn't have a max-gene value within the range of this prototype's genome's data types",
                    base.push(P_MAXGENE), def.push(P_MAXGENE));

        // Next check to see if the gene-by-gene min/max values exist
        if (state.parameters.exists(base.push(P_MAXGENE).push("0"), def.push(P_MAXGENE).push("0")))
            {
            minGenes = new double[genomeSize];
            maxGenes = new double[genomeSize];
            boolean warnedMin = false;
            boolean warnedMax = false;
            for (int x = 0; x < genomeSize; x++)
                {
                minGenes[x] = minGene;
                maxGenes[x] = maxGene;
                if (!state.parameters.exists(base.push(P_MINGENE).push("" + x),
                                             def.push(P_MINGENE).push("" + x)))
                    {
                    if (!warnedMin)
                        {
                        state.output.warning(
                            "FloatVectorSpecies has missing min-gene values for some genes.\n"
                            + "The first one is gene #" + x + ".",
                            base.push(P_MINGENE).push("" + x), def.push(
                                P_MINGENE).push("" + x));
                        warnedMin = true;
                        }
                    } else
                        minGenes[x] = state.parameters.getDoubleWithDefault(base.push(P_MINGENE).push("" + x), 
																			def.push(P_MINGENE).push("" + x), minGene);

                if (!state.parameters.exists(base.push(P_MAXGENE).push("" + x),
                                             def.push(P_MAXGENE).push("" + x)))
                    {
                    if (!warnedMax)
                        {
                        state.output.warning(
                            "FloatVectorSpecies has missing max-gene values for some genes.\n"
                            + "The first one is gene #" + x + ".",
                            base.push(P_MAXGENE).push("" + x), def.push(
                                P_MAXGENE).push("" + x));
                        warnedMax = true;
                        }
                    } else
                        maxGenes[x] = state.parameters.getDoubleWithDefault(base.push(P_MAXGENE).push("" + x), 
																			def.push(P_MAXGENE).push("" + x), maxGene);

                if (maxGenes[x] < minGenes[x])
                    state.output.fatal(
                        "FloatVectorSpecies must have a min-gene[" + x
                        + "] which is <= the max-gene[" + x + "]",
                        base.push(P_MAXGENE).push("" + x), def.push(
                            P_MAXGENE).push("" + x));

                // check to see if these longs are within the data type of the
                // particular individual
                if (!inNumericalTypeRange(minGenes[x]))
                    state.output
                        .fatal(
                            "This FloatvectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass().getName()
                            + ", but doesn't have a min-gene["
                            + x
                            + "] value within the range of this prototype's genome's data types",
                            base.push(P_MINGENE).push("" + x), def
                            .push(P_MINGENE).push("" + x));
                if (!inNumericalTypeRange(maxGenes[x]))
                    state.output
                        .fatal(
                            "This FloatvectorSpecies has a prototype of the kind: "
                            + i_prototype.getClass().getName()
                            + ", but doesn't have a max-gene["
                            + x
                            + "] value within the range of this prototype's genome's data types",
                            base.push(P_MAXGENE).push("" + x), def
                            .push(P_MAXGENE).push("" + x));
                }
            }

        String mtype = state.parameters.getStringWithDefault(base
                                                             .push(P_MUTATIONTYPE), null, V_RESET_MUTATION);
        mutationType = C_RESET_MUTATION;
        if (mtype == null)
            state.output
                .warning(
                    "No crossover type given for VectorSpecies, assuming one-point crossover",
                    base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            mutationType = C_RESET_MUTATION; // redundant
        else if (mtype.equalsIgnoreCase(V_GAUSS_MUTATION))
            mutationType = C_GAUSS_MUTATION;
        else
            state.output.fatal("FloatVectorSpecies given a bad mutation type: "
                               + mtype, base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));

        if (mutationType == C_GAUSS_MUTATION)
            {
            gaussMutationStdev = state.parameters.getDouble(base.push(P_STDEV),def.push(P_STDEV), 0);
            if (gaussMutationStdev <= 0)
                state.output
                    .fatal(
                        "If it's going to use gaussian mutation, FloatvectorSpecies must have a strictly positive standard deviation",
                        base.push(P_STDEV), def.push(P_STDEV));

            if (individualGeneMinMaxUsed())
                {
                gaussMutationStdevs = new double[genomeSize];
                double defaultRange = maxGene - minGene;
                double defaultStdev = gaussMutationStdev;
                double defaultStdevOverRange = defaultStdev/defaultRange;
                for (int x = 0; x < genomeSize; x++)
                    {
                    gaussMutationStdevs[x] = defaultStdevOverRange *(maxGene(x)-minGene((x)));
                    }
                }
                        
            outOfRangeRetries = state.parameters.getIntWithDefault(base.push(P_OUTOFBOUNDS_RETRIES), def.push(P_OUTOFBOUNDS_RETRIES), outOfRangeRetries);
            if(outOfRangeRetries<0)
                {
                state.output.fatal(
                    "If it's going to use gaussian mutation, FloatvectorSpecies must have a positive number of out-of-bounds retries or 0 (for don't give up)",
                    base.push(P_OUTOFBOUNDS_RETRIES), def.push(P_OUTOFBOUNDS_RETRIES));
                }
            }
        }
    }
