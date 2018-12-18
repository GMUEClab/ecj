package ec.app.bbob;

import java.util.HashMap;

import ec.EvolutionState;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Problem;
import ec.Subpopulation;
import ec.gp.koza.HalfBuilder;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.vector.DoubleVectorIndividual;

/* 
 * BBOBenchmarks.java
 * 
 * Created: Fri Apr 2 09:00:00 2010
 * By: Faisal Abidi
 */

/**
 * The Black Box Optimization workshop (BBOB) has an annual competition for doing real-valued parameter optimization.
 * The examples shown here are more or less faithful reproductions of the BBOB 2010 C code, only using Mersenne Twister
 * instead of BBOB's random number generator.  Unfortunately, the original BBOB code has various magic numbers, unexplained
 * variables, and unfortunate algorithmic decisions.  We've reproduced them exactly rather than attempt to convert to a 
 * standard ECJ template, and simply apologize beforehand.
 *
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>type</tt><br>
 * <font size=-1> String = <tt>none </tt>(default)
 * <tt>, sphere, ellipsoidal, rastrigin, buch-rastrigin, linear-slope, attractive-sector, step-elipsoidal, rosenbrock, rosenbrock-rotated, ellipsoidal-2, discus, bent-cigar, sharp-ridge, different-powers, rastrigin-2,
 * weierstrass, schaffers-f7, schaffers-f7-2, griewak-rosenbrock, schwefel, gallagher-gaussian-101me, gallagher-gaussian-21hi, katsuura, lunacek</tt>
 * </font></td>
 * <td valign=top>(The particular function)
 * <tr>
 * <td valign=top><i>base</i>.<tt>noise</tt><br>
 * <font size=-1> String = <tt>none </tt>(default)
 * <tt>, gauss, uniform, cauchy, gauss-moderate, uniform-moderate, cauchy-moderate</tt>
 * </font></td>
 * <td valign=top>(what type of noise (if any) to add to the function value)
 * <tr>
 * <td valign=top><i>base</i>.<tt>reevaluate-noisy-problems</tt><br>
 * <font size=-1> boolean = <tt>true</tt>(default)
 * </font></td>
 * <td valign=top>(whether to reevaluate noisy problems)
 * </table>
 * 
 * 
 * @author Faisal Abidi
 * @version 1.0
 */

public class BBOBenchmarks extends Problem implements SimpleProblemForm
    {
    public static final String P_GENOME_SIZE = "genome-size";
    public static final String P_WHICH_PROBLEM = "type";
    public static final String P_NOISE = "noise";
    public static final String P_REEVALUATE_NOISY_PROBLEMS = "reevaluate-noisy-problems";
    public static final String P_ZERO_IS_BEST = "zeroIsBest";

    final public String[] problemTypes =
        { "sphere", "ellipsoidal", "rastrigin", "buche-rastrigin", "linear-slope", "attractive-sector", "step-ellipsoidal", "rosenbrock", "rosenbrock-rotated", "ellipsoidal-2", "discus", "bent-cigar", "sharp-ridge", "different-powers", "rastrigin-2",
          "weierstrass", "schaffers-f7", "schaffers-f7-2", "griewank-rosenbrock", "schwefel", "gallagher-gaussian-101me", "gallagher-gaussian-21hi", "katsuura", "lunacek" };

    final static public int SPHERE = 0;
    final static public int ELLIPSOIDAL = 1;
    final static public int RASTRIGIN = 2;
    final static public int BUCHE_RASTRIGIN = 3;
    final static public int LINEAR_SLOPE = 4;
    final static public int ATTRACTIVE_SECTOR = 5;
    final static public int STEP_ELLIPSOIDAL = 6;
    final static public int ROSENBROCK = 7;
    final static public int ROSENBROCK_ROTATED = 8;
    final static public int ELLIPSOIDAL_2 = 9;
    final static public int DISCUS = 10;
    final static public int BENT_CIGAR = 11;
    final static public int SHARP_RIDGE = 12;
    final static public int DIFFERENT_POWERS = 13;
    final static public int RASTRIGIN_2 = 14;
    final static public int WEIERSTRASS = 15;
    final static public int SCHAFFERS_F7 = 16;
    final static public int SCHAFFERS_F7_2 = 17;
    final static public int GRIEWANK_ROSENBROCK = 18;
    final static public int SCHWEFEL = 19;
    final static public int GALLAGHER_GAUSSIAN_101ME = 20;
    final static public int GALLAGHER_GAUSSIAN_21HI = 21;
    final static public int KATSUURA = 22;
    final static public int LUNACEK = 23;

    // Noise types
    final public String[] noiseTypes =
        { "none", "gauss", "uniform", "cauchy", "gauss-moderate", "uniform-moderate", "cauchy-moderate" };

    final static public int NONE = 0;
    final static public int GAUSSIAN = 1;
    final static public int UNIFORM = 2;
    final static public int CAUCHY = 3;
    final static public int GAUSSIAN_MODERATE = 4;
    final static public int UNIFORM_MODERATE = 5;
    final static public int CAUCHY_MODERATE = 6;

    public int problemType = 0; // defaults on SPHERE

    public int noise = NONE; // defaults to NONE
    
    public boolean reevaluateNoisyProblems;
    public boolean zeroIsBest;

    public static final int NHIGHPEAKS21 = 101;
    public static final int NHIGHPEAKS22 = 21;

    // DO NOT MODIFY THESE VARIABLES except in the setup method: global
    // variables are not threadsafe.
    double fOpt;
    double[] xOpt;
    double fAdd_Init;

    double f0;
    double[][] rotation;
    double[][] rot2;
    double[][] linearTF;
    double[] peaks21;
    double[] peaks22;
    int[] rperm;
    int[] rperm21;
    int[] rperm22;
    double[][] xLocal;
    double[][] xLocal21;
    double[][] xLocal22;
    double[][] arrScales;
    double[][] arrScales21;
    double[][] arrScales22;
    double[] aK;
    double[] bK;
    double[] peakvalues;
    double scales;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        String wp = state.parameters.getStringWithDefault(base.push(P_WHICH_PROBLEM), null, "");
        int i, j, k;
        Parameter p = new Parameter(Initializer.P_POP);
        int genomeSize = state.parameters.getInt(p.push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(P_GENOME_SIZE), null, 1);
        String noiseStr = state.parameters.getString(base.push(P_NOISE), null);
        for (i = 0; i < noiseTypes.length; i++)
            if (noiseStr.equals(noiseTypes[i]))
                noise = i;
                
        reevaluateNoisyProblems = state.parameters.getBoolean(base.push(P_REEVALUATE_NOISY_PROBLEMS), null, true);
        zeroIsBest = state.parameters.getBoolean(base.push(P_ZERO_IS_BEST), null, false);
                                
        double condition = 10.0;
        double alpha = 100.0;
        double tmp, tmp2, maxCondition;
        double[] fitValues = { 1.1, 9.1 };

        double[] arrCondition, peaks, tmpvect;

        for (i = 0; i < problemTypes.length; i++)
            if (wp.equals(problemTypes[i]))
                problemType = i;

        // common Initialization
        fOpt = zeroIsBest ? 0.0 : computeFopt(state.random[0]);
        xOpt = new double[genomeSize];

        switch (problemType)
            {
            case SPHERE:
                /* INITIALIZATION */
                computeXopt(xOpt, state.random[0]);
                break;
                        
            case ELLIPSOIDAL: // f2
                computeXopt(xOpt, state.random[0]);
                rotation = new double[genomeSize][genomeSize];
                computeRotation(rotation, state.random[0], genomeSize);
                if (noise != NONE)
                    {
                    rot2 = new double[genomeSize][genomeSize];
                    computeRotation(rot2, state.random[0], genomeSize);
                    }
                break;
                        
            case RASTRIGIN:
                computeXopt(xOpt, state.random[0]);
                break;
                        
            case BUCHE_RASTRIGIN:
                computeXopt(xOpt, state.random[0]);
                for (i = 0; i < genomeSize; i += 2)
                    xOpt[i] = Math.abs(xOpt[i]); /* Skew */
                break;
                        
            case LINEAR_SLOPE:
                computeXopt(xOpt, state.random[0]);
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.pow(Math.sqrt(alpha), ((double) i) / ((double) (genomeSize - 1)));
                    if (xOpt[i] > 0)
                        {
                        xOpt[i] = 5.;
                        }
                    else if (xOpt[i] < 0)
                        {
                        xOpt[i] = -5.;
                        }
                    fAdd_Init += 5. * tmp;
                    }
                break;
                        
            case ATTRACTIVE_SECTOR:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                /* decouple scaling from function definition */
                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(Math.sqrt(condition), ((double) k) / ((double) (genomeSize - 1))) * rot2[k][j];
                            }
                        }
                    }
                break;
                        
            case STEP_ELLIPSOIDAL:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                break;
                        
            case ROSENBROCK:
                computeXopt(xOpt, state.random[0]);
                scales = Math.max(1.0, Math.sqrt(genomeSize) / 8.0);
                if (noise == NONE)
                    for (i = 0; i < genomeSize; i++)
                        xOpt[i] *= 0.75;
                break;
                        
            case ROSENBROCK_ROTATED:
                /* INITIALIZATION */
                linearTF = new double[genomeSize][genomeSize];
                rotation = new double[genomeSize][genomeSize];
                /* computeXopt(state.random[0], genomeSize); */
                computeRotation(rotation, state.random[0], genomeSize);
                scales = Math.max(1.0, Math.sqrt(genomeSize) / 8.);
                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        linearTF[i][j] = scales * rotation[i][j];
                    }
                break;
                        
            case ELLIPSOIDAL_2:
                rotation = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                break;
                        
            case DISCUS:
                rotation = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                break;
                        
            case BENT_CIGAR:
                rotation = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                break;
                        
            case SHARP_RIDGE:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(Math.sqrt(condition), ((double) k) / ((double) (genomeSize - 1))) * rot2[k][j];
                            }
                        }
                    }
                break;
                        
            case DIFFERENT_POWERS:
                rotation = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                break;
                        
            case RASTRIGIN_2:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(Math.sqrt(condition), ((double) k) / ((double) (genomeSize - 1))) * rot2[k][j];
                            }
                        }
                    }
                break;
                        
            case WEIERSTRASS:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                aK = new double[12];
                bK = new double[12];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);

                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(1.0 / Math.sqrt(condition), ((double) k) / ((double) (genomeSize - 1))) * rot2[k][j];
                            }
                        }
                    }

                f0 = 0.0;
                for (i = 0; i < 12; i++) /*
                                          * number of summands, 20 in CEC2005, 10/12
                                          * saves 30% of time
                                          */
                    {
                    aK[i] = Math.pow(0.5, (double) i);
                    bK[i] = Math.pow(3., (double) i);
                    f0 += aK[i] * Math.cos(2 * Math.PI * bK[i] * 0.5);
                    }
                break;
                        
            case SCHAFFERS_F7:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                break;
                        
            case SCHAFFERS_F7_2:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                break;
                        
            case GRIEWANK_ROSENBROCK:
                rotation = new double[genomeSize][genomeSize];
                scales = Math.max(1.0, Math.sqrt(genomeSize) / 8.0);
                computeRotation(rotation, state.random[0], genomeSize);
                if (noise == NONE)
                    {
                    rot2 = new double[genomeSize][genomeSize];
                    linearTF = new double[genomeSize][genomeSize];
                    for (i = 0; i < genomeSize; i++)
                        {
                        for (j = 0; j < genomeSize; j++)
                            {
                            linearTF[i][j] = scales * rotation[i][j];
                            }
                        }
                    for (i = 0; i < genomeSize; i++)
                        {
                        xOpt[i] = 0.0;
                        for (j = 0; j < genomeSize; j++)
                            {
                            xOpt[i] += linearTF[j][i] * 0.5 / scales / scales;
                            }
                        }
                    }
                else
                    {
                    // TODO
                    }
                break;
                        
            case SCHWEFEL:
                /* INITIALIZATION */
                tmpvect = new double[genomeSize];

                for (i = 0; i < genomeSize; i++)
                    tmpvect[i] = nextDoubleClosedInterval(state.random[0]);
                for (i = 0; i < genomeSize; i++)
                    {
                    xOpt[i] = 0.5 * 4.2096874633;
                    if (tmpvect[i] - 0.5 < 0)
                        xOpt[i] *= -1.;
                    }
                break;
                        
            case GALLAGHER_GAUSSIAN_101ME:
                rotation = new double[genomeSize][genomeSize];
                maxCondition = 1000.0;
                arrCondition = new double[NHIGHPEAKS21];
                peaks21 = new double[genomeSize * NHIGHPEAKS21];
                rperm21 = new int[Math.max(genomeSize, NHIGHPEAKS21)];
                peaks = peaks21;
                peakvalues = new double[NHIGHPEAKS21];
                arrScales21 = new double[NHIGHPEAKS21][genomeSize];
                xLocal21 = new double[genomeSize][NHIGHPEAKS21];
                computeRotation(rotation, state.random[0], genomeSize);

                for (i = 0; i < NHIGHPEAKS21 - 1; i++)
                    peaks[i] = nextDoubleClosedInterval(state.random[0]);
                rperm = rperm21;
                for (i = 0; i < NHIGHPEAKS21 - 1; i++)
                    rperm[i] = i;
                QuickSort.qsort(rperm);

                /* Random permutation */

                arrCondition[0] = Math.sqrt(maxCondition);
                peakvalues[0] = 10;
                for (i = 1; i < NHIGHPEAKS21; i++)
                    {
                    arrCondition[i] = Math.pow(maxCondition, (double) (rperm[i - 1]) / ((double) (NHIGHPEAKS21 - 2)));
                    peakvalues[i] = (double) (i - 1) / (double) (NHIGHPEAKS21 - 2) * (fitValues[1] - fitValues[0]) + fitValues[0];
                    }
                arrScales = arrScales21;
                for (i = 0; i < NHIGHPEAKS21; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        peaks[j] = nextDoubleClosedInterval(state.random[0]);
                    for (j = 0; j < genomeSize; j++)
                        rperm[j] = j;
                    // qsort(rperm, genomeSize, sizeof(int), compare_doubles);
                    QuickSort.qsort(rperm);
                    for (j = 0; j < genomeSize; j++)
                        {
                        arrScales[i][j] = Math.pow(arrCondition[i], ((double) rperm[j]) / ((double) (genomeSize - 1)) - 0.5);
                        }
                    }

                for (i = 0; i < genomeSize * NHIGHPEAKS21; i++)
                    peaks[i] = nextDoubleClosedInterval(state.random[0]);
                xLocal = xLocal21;
                for (i = 0; i < genomeSize; i++)
                    {
                    xOpt[i] = 0.8 * (10. * peaks[i] - 5.);
                    for (j = 0; j < NHIGHPEAKS21; j++)
                        {
                        xLocal[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            xLocal[i][j] += rotation[i][k] * (10. * peaks[j * genomeSize + k] - 5.);
                            }
                        if (j == 0)
                            xLocal[i][j] *= 0.8;
                        }
                    }
                break;
                        
            case GALLAGHER_GAUSSIAN_21HI:
                rotation = new double[genomeSize][genomeSize];
                maxCondition = 1000.0;
                arrCondition = new double[NHIGHPEAKS22];
                peaks22 = new double[genomeSize * NHIGHPEAKS22];
                rperm22 = new int[Math.max(genomeSize, NHIGHPEAKS22)];
                arrScales22 = new double[NHIGHPEAKS22][genomeSize];
                xLocal22 = new double[genomeSize][NHIGHPEAKS22];
                peaks = peaks22;
                peakvalues = new double[NHIGHPEAKS22];
                computeRotation(rotation, state.random[0], genomeSize);
                peaks = peaks22;
                for (i = 0; i < NHIGHPEAKS22 - 1; i++)
                    peaks[i] = nextDoubleClosedInterval(state.random[0]);
                rperm = rperm22;
                for (i = 0; i < NHIGHPEAKS22 - 1; i++)
                    rperm[i] = i;
                // NOTE: confirm if this is a valid java conversion.
                QuickSort.qsort(rperm);
                /* Random permutation */
                arrCondition[0] = maxCondition;
                peakvalues[0] = 10;
                for (i = 1; i < NHIGHPEAKS22; i++)
                    {
                    arrCondition[i] = Math.pow(maxCondition, (double) (rperm[i - 1]) / ((double) (NHIGHPEAKS22 - 2)));
                    peakvalues[i] = (double) (i - 1) / (double) (NHIGHPEAKS22 - 2) * (fitValues[1] - fitValues[0]) + fitValues[0];
                    }
                arrScales = arrScales22;
                for (i = 0; i < NHIGHPEAKS22; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        peaks[j] = nextDoubleClosedInterval(state.random[0]);
                    for (j = 0; j < genomeSize; j++)
                        rperm[j] = j;
                    // qsort(rperm, genomeSize, sizeof(int), compare_doubles);
                    // NOTE: confirm if converted correctly
                    QuickSort.qsort(rperm);
                    for (j = 0; j < genomeSize; j++)
                        {
                        arrScales[i][j] = Math.pow(arrCondition[i], ((double) rperm[j]) / ((double) (genomeSize - 1)) - 0.5);
                        }
                    }

                for (i = 0; i < genomeSize * NHIGHPEAKS22; i++)
                    peaks[i] = nextDoubleClosedInterval(state.random[0]);
                xLocal = xLocal22;
                for (i = 0; i < genomeSize; i++)
                    {
                    xOpt[i] = 0.8 * (9.8 * peaks[i] - 4.9);
                    for (j = 0; j < NHIGHPEAKS22; j++)
                        {
                        xLocal[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            xLocal[i][j] += rotation[i][k] * (9.8 * peaks[j * genomeSize + k] - 4.9);
                            }
                        if (j == 0)
                            xLocal[i][j] *= 0.8;
                        }
                    }
                break;
                        
            case KATSUURA:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(Math.sqrt(condition), ((double) k) / (double) (genomeSize - 1)) * rot2[k][j];
                            }
                        }
                    }
                break;
                        
            case LUNACEK:
                rotation = new double[genomeSize][genomeSize];
                rot2 = new double[genomeSize][genomeSize];
                tmpvect = new double[genomeSize];
                linearTF = new double[genomeSize][genomeSize];
                double mu1 = 2.5;
                computeXopt(xOpt, state.random[0]);
                computeRotation(rotation, state.random[0], genomeSize);
                computeRotation(rot2, state.random[0], genomeSize);
                gauss(tmpvect, state.random[0]);
                for (i = 0; i < genomeSize; i++)
                    {
                    xOpt[i] = 0.5 * mu1;
                    if (tmpvect[i] < 0.)
                        xOpt[i] *= -1.;
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    for (j = 0; j < genomeSize; j++)
                        {
                        linearTF[i][j] = 0.0;
                        for (k = 0; k < genomeSize; k++)
                            {
                            linearTF[i][j] += rotation[i][k] * Math.pow(Math.sqrt(condition), ((double) k) / ((double) (genomeSize - 1))) * rot2[k][j];
                            }
                        }
                    }
                break;
                        
            default:
                String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                for (i = 0; i < problemTypes.length; i++)
                    outputStr += problemTypes[i] + "\n";
                state.output.fatal(outputStr, base.push(P_WHICH_PROBLEM));
            }

        }

    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
        if (noise != NONE)
            {
            if (!reevaluateNoisyProblems && ind.evaluated) // don't bother reevaluating
                return;
            }
        else if (ind.evaluated)  // don't bother reevaluating
            return;
            
        if (!(ind instanceof DoubleVectorIndividual))
            state.output.fatal("The individuals for this problem should be DoubleVectorIndividuals.");
        DoubleVectorIndividual temp = (DoubleVectorIndividual) ind;
        double[] genome = temp.genome;
        int genomeSize = genome.length;
        double value = 0;
        double fit;
        int i, j;
        double condition, alpha, beta, tmp = 0.0, tmp2, fAdd, fPen = 0.0, x1, fac, a, f = 0.0, f2;
        double[] tmx = new double[genomeSize];
        double[] tmpvect = new double[genomeSize];
        
        switch (problemType)
            {
            case SPHERE:// f1
                        /* Sphere function */
                fAdd = fOpt;
                if (noise != NONE)
                    {
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmp = Math.abs(genome[i]) - 5.;
                        if (tmp > 0.0)
                            {
                            fPen += tmp * tmp;
                            }
                        }
                    fAdd += 100. * fPen;
                    }
                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = genome[i] - xOpt[i];
                    value += tmp * tmp;
                    }
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    case GAUSSIAN_MODERATE:
                        value = fGauss(value, 0.01, state.random[threadnum]);
                        break;
                    case UNIFORM_MODERATE:
                        value = fUniform(value, 0.01 * (0.49 + 1. / genomeSize), 0.01, state.random[threadnum]);
                        break;
                    case CAUCHY_MODERATE:
                        value = fCauchy(value, 0.01, 0.05, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < noiseTypes.length; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case ELLIPSOIDAL:// f2
                /*
                 * separable ellipsoid with monotone transformation with noiseless
                 * condition 1e6 and noisy condition 1e4
                 */
                fAdd = fOpt;
                if (noise == NONE)
                    {
                    condition = 1e6;
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = genome[i] - xOpt[i];
                        }
                    }
                else
                    {
                    condition = 1e4;
                    fAdd = fOpt;

                    /* BOUNDARY HANDLING */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmp = Math.abs(genome[i]) - 5.;
                        if (tmp > 0.)
                            {
                            fPen += tmp * tmp;
                            }
                        }
                    fAdd += 100. * fPen;

                    /* TRANSFORMATION IN SEARCH SPACE */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = 0.;
                        for (j = 0; j < genomeSize; j++)
                            {
                            tmx[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                            }
                        }
                    }

                monotoneTFosc(tmx);
                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    value += Math.pow(condition, ((double) i) / ((double) (genomeSize - 1))) * tmx[i] * tmx[i];
                    }

                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        

            case RASTRIGIN:// f3
                /* Rastrigin with monotone transformation separable "condition" 10 */
                condition = 10;
                beta = 0.2;
                fAdd = fOpt;
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = genome[i] - xOpt[i];
                    }
                monotoneTFosc(tmx);
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = ((double) i) / ((double) (genomeSize - 1));
                    if (tmx[i] > 0)
                        tmx[i] = Math.pow(tmx[i], 1 + beta * tmp * Math.sqrt(tmx[i]));
                    tmx[i] = Math.pow(Math.sqrt(condition), tmp) * tmx[i];
                    }
                /* COMPUTATION core */
                tmp = 0;
                tmp2 = 0;
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp += Math.cos(2 * Math.PI * tmx[i]);
                    tmp2 += tmx[i] * tmx[i];
                    }
                value = 10 * (genomeSize - tmp) + tmp2;
                value += fAdd;

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case BUCHE_RASTRIGIN:// f4
                /* skew Rastrigin-Bueche, condition 10, skew-"condition" 100 */
                condition = 10.0;
                alpha = 100;
                fAdd = fOpt;
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        fPen += tmp * tmp;
                    }
                fPen *= 1e2;
                fAdd += fPen;

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = genome[i] - xOpt[i];
                    }

                monotoneTFosc(tmx);
                for (i = 0; i < genomeSize; i++)
                    {
                    if (i % 2 == 0 && tmx[i] > 0)
                        tmx[i] = Math.sqrt(alpha) * tmx[i];
                    tmx[i] = Math.pow(Math.sqrt(condition), ((double) i) / ((double) (genomeSize - 1))) * tmx[i];
                    }
                /* COMPUTATION core */
                tmp = 0.0;
                tmp2 = 0.0;
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp += Math.cos(2 * Math.PI * tmx[i]);
                    tmp2 += tmx[i] * tmx[i];
                    }
                value = 10 * (genomeSize - tmp) + tmp2;
                value += fAdd;

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case LINEAR_SLOPE:// f5
                /* linear slope */
                alpha = 100;
                fAdd = fOpt;
                /* BOUNDARY HANDLING */
                /* move "too" good coordinates back into domain */
                for (i = 0; i < genomeSize; i++)
                    {
                    if ((xOpt[i] == 5.) && (genome[i] > 5))
                        tmx[i] = 5.;
                    else if ((xOpt[i] == -5.) && (genome[i] < -5))
                        tmx[i] = -5.;
                    else
                        tmx[i] = genome[i];
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    if (xOpt[i] > 0)
                        {
                        value -= Math.pow(Math.sqrt(alpha), ((double) i) / ((double) (genomeSize - 1))) * tmx[i];
                        }
                    else
                        {
                        value += Math.pow(Math.sqrt(alpha), ((double) i) / ((double) (genomeSize - 1))) * tmx[i];
                        }
                    }
                value += fAdd;

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case ATTRACTIVE_SECTOR:// f6
                /* attractive sector function */
                alpha = 100.0;
                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {

                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += linearTF[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    if (tmx[i] * xOpt[i] > 0)
                        tmx[i] *= alpha;
                    value += tmx[i] * tmx[i];
                    }

                /* monotoneTFosc... */
                if (value > 0)
                    {
                    value = Math.pow(Math.exp(Math.log(value) / 0.1 + 0.49 * (Math.sin(Math.log(value) / 0.1) + Math.sin(0.79 * Math.log(value) / 0.1))), 0.1);
                    }
                else if (value < 0)
                    {
                    value = -Math.pow(Math.exp(Math.log(-value) / 0.1 + 0.49 * (Math.sin(0.55 * Math.log(-value) / 0.1) + Math.sin(0.31 * Math.log(-value) / 0.1))), 0.1);
                    }
                value = Math.pow(value, 0.9);
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case STEP_ELLIPSOIDAL:// f7
                /* step-ellipsoid, condition 100 */
                condition = 100.0;
                alpha = 10.0;
                fAdd = fOpt;
                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.0;
                    if (tmp > 0.0)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                if (noise == NONE)
                    fAdd += fPen;
                else
                    fAdd += 100. * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {

                    tmpvect[i] = 0.0;
                    tmp = Math.sqrt(Math.pow(condition / 10., ((double) i) / ((double) (genomeSize - 1))));
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += tmp * rot2[i][j] * (genome[j] - xOpt[j]);
                        }

                    }
                x1 = tmpvect[0];

                for (i = 0; i < genomeSize; i++)
                    {
                    if (Math.abs(tmpvect[i]) > 0.5)
                        tmpvect[i] = Math.round(tmpvect[i]);
                    else
                        tmpvect[i] = Math.round(alpha * tmpvect[i]) / alpha;
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * tmpvect[j];
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    value += Math.pow(condition, ((double) i) / ((double) (genomeSize - 1))) * tmx[i] * tmx[i];
                    }
                value = 0.1 * Math.max(1e-4 * Math.abs(x1), value);
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case ROSENBROCK:// f8
                /* Rosenbrock, non-rotated */
                fAdd = fOpt;
                if (noise == NONE)
                    {
                    /* TRANSFORMATION IN SEARCH SPACE */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = scales * (genome[i] - xOpt[i]) + 1;
                        }
                    }
                else
                    {
                    /* BOUNDARY HANDLING */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmp = Math.abs(genome[i]) - 5.;
                        if (tmp > 0.)
                            {
                            fPen += tmp * tmp;
                            }
                        }
                    fAdd += 100.0 * fPen;
                    /* TRANSFORMATION IN SEARCH SPACE */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = scales * (genome[i] - 0.75 * xOpt[i]) + 1;
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = (tmx[i] * tmx[i] - tmx[i + 1]);
                    value += tmp * tmp;
                    }
                value *= 1e2;
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = (tmx[i] - 1.);
                    value += tmp * tmp;
                    }

                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    case GAUSSIAN_MODERATE:
                        value = fGauss(value, 0.01, state.random[threadnum]);
                        break;
                    case UNIFORM_MODERATE:
                        value = fUniform(value, 0.01 * (0.49 + 1. / genomeSize), 0.01, state.random[threadnum]);
                        break;
                    case CAUCHY_MODERATE:
                        value = fCauchy(value, 0.01, 0.05, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < noiseTypes.length; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }                       
                        
                value += fAdd;

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;

                        
                        
                        
            case ROSENBROCK_ROTATED:// f9
                /* Rosenbrock, rotated */
                fAdd = fOpt;

                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.5;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += linearTF[i][j] * genome[j];
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = (tmx[i] * tmx[i] - tmx[i + 1]);
                    value += tmp * tmp;
                    }
                value *= 1e2;
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = (tmx[i] - 1.);
                    value += tmp * tmp;
                    }

                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case ELLIPSOIDAL_2:// f10
                /* ellipsoid with monotone transformation, condition 1e6 */
                condition = 1e6;

                fAdd = fOpt;
                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                monotoneTFosc(tmx);
                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    fAdd += Math.pow(condition, ((double) i) / ((double) (genomeSize - 1))) * tmx[i] * tmx[i];
                    }
                value = fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case DISCUS:// f11
                        /* DISCUS (tablet) with monotone transformation, condition 1e6 */
                condition = 1e6;
                fAdd = fOpt;
                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                monotoneTFosc(tmx);

                /* COMPUTATION core */
                value = condition * tmx[0] * tmx[0];
                for (i = 1; i < genomeSize; i++)
                    {
                    value += tmx[i] * tmx[i];
                    }
                value += fAdd; /* without noise */
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case BENT_CIGAR:// f12
                /* bent cigar with asymmetric space distortion, condition 1e6 */
                condition = 1e6;
                beta = 0.5;
                fAdd = fOpt;
                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    if (tmpvect[i] > 0)
                        {
                        tmpvect[i] = Math.pow(tmpvect[i], 1 + beta * ((double) i) / ((double) (genomeSize - 1)) * Math.sqrt(tmpvect[i]));
                        }
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * tmpvect[j];
                        }
                    }

                /* COMPUTATION core */
                value = tmx[0] * tmx[0];
                for (i = 1; i < genomeSize; i++)
                    {
                    value += condition * tmx[i] * tmx[i];
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case SHARP_RIDGE:// f13
                /* sharp ridge */
                condition = 10.0;
                alpha = 100.0;

                fAdd = fOpt;
                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += linearTF[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                /* COMPUTATION core */
                for (i = 1; i < genomeSize; i++)
                    {
                    value += tmx[i] * tmx[i];
                    }
                value = alpha * Math.sqrt(value);
                value += tmx[0] * tmx[0];
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case DIFFERENT_POWERS:// f14
                /* sum of different powers, between x^2 and x^6 */
                alpha = 4.0;
                fAdd = fOpt;
                if (noise != NONE)
                    {
                    /* BOUNDARY HANDLING */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmp = Math.abs(genome[i]) - 5.;
                        if (tmp > 0.)
                            {
                            fPen += tmp * tmp;
                            }
                        }
                    fAdd += 100. * fPen;
                    }

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    value += Math.pow(Math.abs(tmx[i]), 2. + alpha * ((double) i) / ((double) (genomeSize - 1)));
                    }
                value = Math.sqrt(value);
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case RASTRIGIN_2:// f15
                /* Rastrigin with asymmetric non-linear distortion, "condition" 10 */
                condition = 10.0;
                beta = 0.2;
                tmp = tmp2 = 0;

                fAdd = fOpt;
                /* BOUNDARY HANDLING */

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                monotoneTFosc(tmpvect);
                for (i = 0; i < genomeSize; i++)
                    {
                    if (tmpvect[i] > 0)
                        tmpvect[i] = Math.pow(tmpvect[i], 1 + beta * ((double) i) / ((double) (genomeSize - 1)) * Math.sqrt(tmpvect[i]));
                    }
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += linearTF[i][j] * tmpvect[j];
                        }
                    }
                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp += Math.cos(2. * Math.PI * tmx[i]);
                    tmp2 += tmx[i] * tmx[i];
                    }
                value = 10. * ((double) genomeSize - tmp) + tmp2;
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case WEIERSTRASS:// f16
                /* Weierstrass, condition 100 */
                condition = 100.0;
                fPen = 0;

                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += 10. / (double) genomeSize * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    }

                monotoneTFosc(tmpvect);
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += linearTF[i][j] * tmpvect[j];
                        }
                    }
                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = 0.0;
                    for (j = 0; j < 12; j++)
                        {
                        tmp += Math.cos(2 * Math.PI * (tmx[i] + 0.5) * bK[j]) * aK[j];
                        }
                    value += tmp;
                    }
                value = 10. * Math.pow(value / (double) genomeSize - f0, 3.);
                value += fAdd;
                ;

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case SCHAFFERS_F7:// f17
                /*
                 * Schaffers F7 with asymmetric non-linear transformation, condition
                 * 10
                 */
                condition = 10.0;
                beta = 0.5;
                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += 10. * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    if (tmpvect[i] > 0)
                        tmpvect[i] = Math.pow(tmpvect[i], 1 + beta * ((double) i) / ((double) (genomeSize - 1)) * Math.sqrt(tmpvect[i]));
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    tmp = Math.pow(Math.sqrt(condition), ((double) i) / ((double) (genomeSize - 1)));
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += tmp * rot2[i][j] * tmpvect[j];
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = tmx[i] * tmx[i] + tmx[i + 1] * tmx[i + 1];
                    value += Math.pow(tmp, 0.25) * (Math.pow(Math.sin(50 * Math.pow(tmp, 0.1)), 2.0) + 1.0);
                    }
                value = Math.pow(value / (double) (genomeSize - 1), 2.);
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case SCHAFFERS_F7_2:// f18
                /*
                 * Schaffers F7 with asymmetric non-linear transformation, condition
                 * 1000
                 */
                condition = 1e3;
                beta = 0.5;
                fPen = 0.0;
                fAdd = fOpt;
                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += 10. * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmpvect[i] += rotation[i][j] * (genome[j] - xOpt[j]);
                        }
                    if (tmpvect[i] > 0)
                        tmpvect[i] = Math.pow(tmpvect[i], 1. + beta * ((double) i) / ((double) (genomeSize - 1)) * Math.sqrt(tmpvect[i]));
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    tmp = Math.pow(Math.sqrt(condition), ((double) i) / ((double) (genomeSize - 1)));
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += tmp * rot2[i][j] * tmpvect[j];
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < genomeSize - 1; i++)
                    {
                    tmp = tmx[i] * tmx[i] + tmx[i + 1] * tmx[i + 1];
                    value += Math.pow(tmp, 0.25) * (Math.pow(Math.sin(50. * Math.pow(tmp, 0.1)), 2.) + 1.);
                    }
                value = Math.pow(value / (double) (genomeSize - 1), 2.);
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case GRIEWANK_ROSENBROCK:// f19
                /* F8f2 sum of Griewank-Rosenbrock 2-D blocks */
                fAdd = fOpt;
                if (noise == NONE)
                    {
                    /* TRANSFORMATION IN SEARCH SPACE */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = 0.5;
                        for (j = 0; j < genomeSize; j++)
                            {
                            tmx[i] += linearTF[i][j] * genome[j];
                            }
                        }
                    /* COMPUTATION core */
                    for (i = 0; i < genomeSize - 1; i++)
                        {
                        tmp2 = tmx[i] * tmx[i] - tmx[i + 1];
                        f2 = 100. * tmp2 * tmp2;
                        tmp2 = 1 - tmx[i];
                        f2 += tmp2 * tmp2;
                        tmp += f2 / 4000. - Math.cos(f2);
                        }
                    value = 10. + 10. * tmp / (double) (genomeSize - 1);
                    }
                else
                    {
                    /* BOUNDARY HANDLING */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmp = Math.abs(genome[i]) - 5.0;
                        if (tmp > 0.0)
                            {
                            fPen += tmp * tmp;
                            }
                        }
                    fAdd += 100.0 * fPen;

                    /* TRANSFORMATION IN SEARCH SPACE */
                    for (i = 0; i < genomeSize; i++)
                        {
                        tmx[i] = 0.5;
                        for (j = 0; j < genomeSize; j++)
                            {
                            tmx[i] += scales * rotation[i][j] * genome[j];
                            }
                        }
                    /* COMPUTATION core */
                    tmp = 0.;
                    for (i = 0; i < genomeSize - 1; i++)
                        {
                        f2 = 100. * (tmx[i] * tmx[i] - tmx[i + 1]) * (tmx[i] * tmx[i] - tmx[i + 1]) + (1 - tmx[i]) * (1 - tmx[i]);
                        tmp += f2 / 4000. - Math.cos(f2);
                        }
                    value = 1. + 1. * tmp / (double) (genomeSize - 1);
                    }
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case SCHWEFEL:// f20
                /* Schwefel with tridiagonal variable transformation */
                condition = 10.0;
                fPen = 0.0;
                fAdd = fOpt;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmpvect[i] = 2. * genome[i];
                    if (xOpt[i] < 0.)
                        tmpvect[i] *= -1.;
                    }

                tmx[0] = tmpvect[0];
                for (i = 1; i < genomeSize; i++)
                    {
                    tmx[i] = tmpvect[i] + 0.25 * (tmpvect[i - 1] - 2. * Math.abs(xOpt[i - 1]));
                    }

                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] -= 2 * Math.abs(xOpt[i]);
                    tmx[i] *= Math.pow(Math.sqrt(condition), ((double) i) / ((double) (genomeSize - 1)));
                    tmx[i] = 100. * (tmx[i] + 2 * Math.abs(xOpt[i]));
                    }

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(tmx[i]) - 500.0;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += 0.01 * fPen;

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    value += tmx[i] * Math.sin(Math.sqrt(Math.abs(tmx[i])));
                    }
                value = 0.01 * ((418.9828872724339) - value / (double) genomeSize);
                value += fAdd;/* without noise */
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case GALLAGHER_GAUSSIAN_101ME:// f21
                /*
                 * Gallagher with 101 Gaussian peaks, condition up to 1000, one
                 * global rotation
                 */
                a = 0.1;
                fac = -0.5 / (double) genomeSize;
                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                if (noise == NONE)
                    fAdd += fPen;
                else
                    fAdd += 100. * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * genome[j];
                        }
                    }

                /* COMPUTATION core */
                if (noise == NONE)
                    for (i = 0; i < NHIGHPEAKS21; i++)
                        {
                        tmp2 = 0.0;
                        for (j = 0; j < genomeSize; j++)
                            {
                            tmp = (tmx[j] - xLocal[j][i]);
                            tmp2 += arrScales[i][j] * tmp * tmp;
                            }
                        tmp2 = peakvalues[i] * Math.exp(fac * tmp2);
                        f = Math.max(f, tmp2);
                        }
                else
                    /* COMPUTATION core */
                    for (i = 0; i < NHIGHPEAKS21; i++)
                        {
                        tmp2 = 0.;
                        for (j = 0; j < genomeSize; j++)
                            {
                            tmp2 += arrScales[i][j] * (tmx[j] - xLocal[j][i]) * (tmx[j] - xLocal[j][i]);
                            }
                        tmp2 = peakvalues[i] * Math.exp(fac * tmp2);
                        f = Math.max(f, tmp2);
                        }

                f = 10.0 - f;
                /* monotoneTFosc */
                if (f > 0)
                    {
                    value = Math.log(f) / a;
                    value = Math.pow(Math.exp(value + 0.49 * (Math.sin(value) + Math.sin(0.79 * value))), a);
                    }
                else if (f < 0)
                    {
                    value = Math.log(-f) / a;
                    value = -Math.pow(Math.exp(value + 0.49 * (Math.sin(0.55 * value) + Math.sin(0.31 * value))), a);
                    }
                else
                    value = f;

                value *= value;
                switch (noise)
                    {
                    case NONE:
                        break;
                    case GAUSSIAN:
                        value = fGauss(value, 1.0, state.random[threadnum]);
                        break;
                    case UNIFORM:
                        value = fUniform(value, 0.49 + 1.0 / genomeSize, 1.0, state.random[threadnum]);
                        break;
                    case CAUCHY:
                        value = fCauchy(value, 1.0, 0.2, state.random[threadnum]);
                        break;
                    default:
                        String outputStr = "Invalid value for parameter, or parameter not found.\n" + "Acceptable values are:\n";
                        for (i = 0; i < 4; i++)
                            outputStr += noiseTypes[i] + "\n";
                        state.output.fatal(outputStr, new Parameter(P_NOISE));
                        break;
                    }
                value += fAdd;
                ; /* without noise */

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case GALLAGHER_GAUSSIAN_21HI:// f22
                /*
                 * Gallagher with 21 Gaussian peaks, condition up to 1000, one
                 * global rotation
                 */
                a = 0.1;
                f = 0;
                fac = -0.5 / (double) genomeSize;
                fPen = 0.0;

                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmx[i] += rotation[i][j] * genome[j];
                        }
                    }

                /* COMPUTATION core */
                for (i = 0; i < NHIGHPEAKS22; i++)
                    {
                    tmp2 = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmp = (tmx[j] - xLocal[j][i]);
                        tmp2 += arrScales[i][j] * tmp * tmp;
                        }
                    tmp2 = peakvalues[i] * Math.exp(fac * tmp2);
                    f = Math.max(f, tmp2);
                    }

                f = 10. - f;
                if (f > 0)
                    {
                    value = Math.log(f) / a;
                    value = Math.pow(Math.exp(value + 0.49 * (Math.sin(value) + Math.sin(0.79 * value))), a);
                    }
                else if (f < 0)
                    {
                    value = Math.log(-f) / a;
                    value = -Math.pow(Math.exp(value + 0.49 * (Math.sin(0.55 * value) + Math.sin(0.31 * value))), a);
                    }
                else
                    value = f;

                value *= value;
                value += fAdd;
                ; /* without noise */

                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case KATSUURA:// f23
                /* Katsuura function */
                condition = 100.0;
                fAdd = 0;
                fPen = 0;
                double arr;
                double prod = 1.0;
                double[] ptmx,
                    plinTF,
                    ptmp;

                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                /* write rotated difference vector into tmx */
                for (j = 0; j < genomeSize; j++)
                    /* store difference vector */
                    tmpvect[j] = genome[j] - xOpt[j];
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 0.0;
                    ptmx = tmx;
                    plinTF = linearTF[i];
                    ptmp = tmpvect;
                    for (j = 0; j < genomeSize; j++)
                        {
                        // *ptmx += *plinTF++ * *ptmp++;
                        ptmx[j] += plinTF[j] * ptmp[j];
                        }
                    }

                /*
                 * for (i = 0; i < genomeSize; i++) { tmx[i] = 0.0; for (j = 0; j <
                 * genomeSize; j++) { tmx[i] += linearTF[i][j] * (genome[j] -
                 * xOpt[j]); } }
                 */

                /* COMPUTATION core */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = 0.0;
                    for (j = 1; j < 33; j++)
                        {
                        tmp2 = Math.pow(2., (double) j);
                        arr = tmx[i] * tmp2;
                        tmp += Math.abs(arr - Math.round(arr)) / tmp2;
                        }
                    tmp = 1. + tmp * (double) (i + 1);
                    prod *= tmp;
                    }
                value = 10. / (double) genomeSize / (double) genomeSize * (-1. + Math.pow(prod, 10. / Math.pow((double) genomeSize, 1.2)));
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
                        
                        
                        
            case LUNACEK:// f24
                /* Lunacek bi-Rastrigin, condition 100 */
                /* in PPSN 2008, Rastrigin part rotated and scaled */
                condition = 100.0;
                double mu1 = 2.5;
                double tmp3,
                    tmp4;
                fPen = tmp2 = tmp3 = tmp4 = 0.0;
                double s = 1. - 0.5 / (Math.sqrt((double) (genomeSize + 20)) - 4.1);
                double d = 1.;
                double mu2 = -Math.sqrt((mu1 * mu1 - d) / s);

                fAdd = fOpt;

                /* BOUNDARY HANDLING */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp = Math.abs(genome[i]) - 5.;
                    if (tmp > 0.)
                        {
                        fPen += tmp * tmp;
                        }
                    }
                fAdd += 1e4 * fPen;

                /* TRANSFORMATION IN SEARCH SPACE */
                for (i = 0; i < genomeSize; i++)
                    {
                    tmx[i] = 2. * genome[i];
                    if (xOpt[i] < 0.)
                        tmx[i] *= -1.;
                    }

                /* COMPUTATION core */
                tmp = 0.0;
                for (i = 0; i < genomeSize; i++)
                    {
                    tmp2 += (tmx[i] - mu1) * (tmx[i] - mu1);
                    tmp3 += (tmx[i] - mu2) * (tmx[i] - mu2);
                    tmp4 = 0.0;
                    for (j = 0; j < genomeSize; j++)
                        {
                        tmp4 += linearTF[i][j] * (tmx[j] - mu1);
                        }
                    tmp += Math.cos(2 * Math.PI * tmp4);
                    }
                value = Math.min(tmp2, d * (double) genomeSize + s * tmp3) + 10. * ((double) genomeSize - tmp);
                value += fAdd;
                fit = (-value);
                ((SimpleFitness) (ind.fitness)).setFitness(state, fit, fit == 0.0);
            break;
            default:
                break;
            }
            
        ind.evaluated = true;
        }


    final static public double TOL = 1e-8;

    void gauss(double[] g, MersenneTwisterFast random)
        {
        /*
         * samples N standard normally distributed numbers being the same for a
         * given seed.
         */
        double[] uniftmp = new double[2 * g.length];
        int i;
        for (i = 0; i < uniftmp.length; i++)
            uniftmp[i] = nextDoubleClosedInterval(random);
        for (i = 0; i < g.length; i++)
            {

            g[i] = Math.sqrt(-2 * Math.log(uniftmp[i])) * Math.cos(2 * Math.PI * uniftmp[g.length + i]);
            if (g[i] == 0.0)
                g[i] = 1e-99;
            }
        return;
        }

    void gauss(double[] g, MersenneTwisterFast random, int n)
        {
        /*
         * samples N standard normally distributed numbers being the same for a
         * given seed.
         */
        double[] uniftmp = new double[2 * g.length];
        int i;
        for (i = 0; i < uniftmp.length; i++)
            uniftmp[i] = nextDoubleClosedInterval(random);
        for (i = 0; i < n; i++)
            {
            g[i] = Math.sqrt(-2 * Math.log(uniftmp[i])) * Math.cos(2 * Math.PI * uniftmp[n + i]);
            if (g[i] == 0.0)
                g[i] = 1e-99;
            }
        return;
        }

    void computeXopt(double[] xOpt, MersenneTwisterFast random)
        {
        int i;
        int n = xOpt.length;
        for (i = 0; i < n; i++)
            {
            xOpt[i] = 8 * (int) Math.floor(1e4 * nextDoubleClosedInterval(random)) / 1e4 - 4;
            if (xOpt[i] == 0.0)
                xOpt[i] = -1e-5;
            }
        }

    void monotoneTFosc(double[] f)
        {
        double a = 0.1;
        int i;
        int n = f.length;
        for (i = 0; i < n; i++)
            {
            if (f[i] > 0)
                {
                f[i] = Math.log(f[i]) / a;
                f[i] = Math.pow(Math.exp(f[i] + 0.49 * (Math.sin(f[i]) + Math.sin(0.79 * f[i]))), a);
                }
            else if (f[i] < 0)
                {
                f[i] = Math.log(-f[i]) / a;
                f[i] = -Math.pow(Math.exp(f[i] + 0.49 * (Math.sin(0.55 * f[i]) + Math.sin(0.31 * f[i]))), a);
                }
            }
        }

    double[][] reshape(double[][] b, double[] vector, int m, int n)
        {
        int i, j;
        for (i = 0; i < m; i++)
            {
            for (j = 0; j < n; j++)
                {
                b[i][j] = vector[j * m + i];
                }
            }
        return b;
        }

    void computeRotation(double[][] b, MersenneTwisterFast random, int genomeSize)
        {
        double[] gvect = new double[genomeSize * genomeSize];
        double prod;
        int i, j, k; /* Loop over pairs of column vectors */

        gauss(gvect, random);
        reshape(b, gvect, genomeSize, genomeSize);
        /* 1st coordinate is row, 2nd is column. */

        for (i = 0; i < genomeSize; i++)
            {
            for (j = 0; j < i; j++)
                {
                prod = 0;
                for (k = 0; k < genomeSize; k++)
                    {
                    prod += b[k][i] * b[k][j];
                    }
                for (k = 0; k < genomeSize; k++)
                    {
                    b[k][i] -= prod * b[k][j];
                    }
                }
            prod = 0;
            for (k = 0; k < genomeSize; k++)
                {
                prod += b[k][i] * b[k][i];
                }
            for (k = 0; k < genomeSize; k++)
                {
                b[k][i] /= Math.sqrt(prod);
                }
            }
        }

    double fGauss(double fTrue, double beta, MersenneTwisterFast random)
        {
        double fVal = fTrue * Math.exp(beta * nextDoubleClosedInterval(random));
        fVal += 1.01 * TOL;
        if (fTrue < TOL)
            {
            fVal = fTrue;
            }
        return fVal;
        }

    double fUniform(double fTrue, double alpha, double beta, MersenneTwisterFast random)
        {
        double fVal = Math.pow(nextDoubleClosedInterval(random), beta) * fTrue * Math.max(1.0, Math.pow(1e9 / (fTrue + 1e-99), alpha * nextDoubleClosedInterval(random)));
        fVal += 1.01 * TOL;
        if (fTrue < TOL)
            {
            fVal = fTrue;
            }
        return fVal;
        }

    double fCauchy(double fTrue, double alpha, double p, MersenneTwisterFast random)
        {
        double fVal;
        double tmp = nextDoubleClosedInterval(random) / Math.abs(nextDoubleClosedInterval(random) + 1e-199);
        /*
         * tmp is so as to actually do the calls to randn in order for the
         * number of calls to be the same as in the Matlab code.
         */
        if (nextDoubleClosedInterval(random) < p)
            fVal = fTrue + alpha * Math.max(0., 1e3 + tmp);
        else
            fVal = fTrue + alpha * 1e3;

        fVal += 1.01 * TOL;
        if (fTrue < TOL)
            {
            fVal = fTrue;
            }
        return fVal;
        }

    double computeFopt(MersenneTwisterFast random)
        {
        double[] gval = new double[1];
        double[] gval2 = new double[1];
        gauss(gval, random, 1);
        gauss(gval2, random, 1);
        return Math.min(1000.0, Math.max(-1000.0, (Math.round(100.0 * 100.0 * gval[0] / gval2[0]) / 100.0)));
        }

    double nextDoubleClosedInterval(MersenneTwisterFast random)
        {
        double tmp = random.nextDouble() * 2.0;
        while (tmp > 1.0)
            tmp = random.nextDouble() * 2.0;
        return tmp;
        }
    }
