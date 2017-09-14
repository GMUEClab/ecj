/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.ecsuite;

import ec.util.*;
import ec.*;
import ec.simple.*;
import ec.vector.*;

/*
 * ECSuite.java
 *
 * Created: Thu Mar 22 16:27:15 2001
 * By: Liviu Panait and Sean Luke
 */

/*
 * @author Liviu Panait and Sean Luke and Khaled Talukder
 * @version 2.0
 */

/**
   Several standard Evolutionary Computation functions are implemented.
   As the SimpleFitness is used for maximization problems, the mapping f(x) --> -f(x) is used to transform
   the problems into maximization ones.

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: rosenbrock rastrigin sphere step noisy-quartic kdj-f1 kdj-f2 kdj-f3 kdj-f4 booth griewank median sum product schwefel min rotated-rastrigin rotated-schwefel rotated-griewank langerman lennard-jones lunacek</font></td>
   <td valign=top>(The vector problem to test against.  Some of the types are synonyms: kdj-f1 = sphere, kdj-f2 = rosenbrock, kdj-f3 = step, kdj-f4 = noisy-quartic.  "kdj" stands for "Ken DeJong", and the numbers are the problems in his test suite)</td></tr>
   <tr><td valign=top><i>base</i>.<tt>seed</tt><br>
   <font size=-1>int > 0</font></td>
   <td valign=top>(Random number seed for rotated problems)</td></tr>
   </table>

*/

public class ECSuite extends Problem implements SimpleProblemForm
    {
    public static final String P_SEED = "seed";
    public static final String P_WHICH_PROBLEM = "type";

    public static final String V_ROSENBROCK = "rosenbrock";
    public static final String V_RASTRIGIN = "rastrigin";
    public static final String V_SPHERE = "sphere";
    public static final String V_STEP = "step";
    public static final String V_NOISY_QUARTIC = "noisy-quartic";
    public static final String V_F1 = "kdj-f1";
    public static final String V_F2 = "kdj-f2";
    public static final String V_F3 = "kdj-f3";
    public static final String V_F4 = "kdj-f4";
    public static final String V_BOOTH = "booth";
    public static final String V_GRIEWANGK = "griewangk";
    public static final String V_GRIEWANK = "griewank";
    public static final String V_MEDIAN = "median";
    public static final String V_SUM = "sum";
    public static final String V_PRODUCT = "product";
    public static final String V_SCHWEFEL = "schwefel";
    public static final String V_MIN = "min";
    public static final String V_ROTATED_RASTRIGIN = "rotated-rastrigin";
    public static final String V_ROTATED_SCHWEFEL = "rotated-schwefel";
    public static final String V_ROTATED_GRIEWANK = "rotated-griewank";
    public static final String V_LANGERMAN = "langerman" ;
    public static final String V_LENNARDJONES = "lennard-jones" ;
    public static final String V_LUNACEK = "lunacek" ;

    public static final int PROB_ROSENBROCK = 0;
    public static final int PROB_RASTRIGIN = 1;
    public static final int PROB_SPHERE = 2;
    public static final int PROB_STEP = 3;
    public static final int PROB_NOISY_QUARTIC = 4;
    public static final int PROB_BOOTH = 5;
    public static final int PROB_GRIEWANK = 6;
    public static final int PROB_MEDIAN = 7;
    public static final int PROB_SUM = 8;
    public static final int PROB_PRODUCT = 9;
    public static final int PROB_SCHWEFEL = 10;
    public static final int PROB_MIN = 11;
    public static final int PROB_ROTATED_RASTRIGIN = 12;
    public static final int PROB_ROTATED_SCHWEFEL = 13;
    public static final int PROB_ROTATED_GRIEWANK = 14;
    public static final int PROB_LANGERMAN = 15 ;
    public static final int PROB_LENNARDJONES = 16 ;
    public static final int PROB_LUNACEK = 17 ;


    public int problemType = PROB_ROSENBROCK;  // defaults on Rosenbrock

    public static final String problemName[] = new String[]
    {
    V_ROSENBROCK,
    V_RASTRIGIN,
    V_SPHERE,
    V_STEP,
    V_NOISY_QUARTIC,
    V_BOOTH,
    V_GRIEWANK,
    V_MEDIAN,
    V_SUM,
    V_PRODUCT,
    V_SCHWEFEL,
    V_MIN,
    V_ROTATED_RASTRIGIN,
    V_ROTATED_SCHWEFEL,
    V_ROTATED_GRIEWANK,
    V_LANGERMAN,
    V_LENNARDJONES,
    V_LUNACEK
    };

    public static final double minRange[] = new double[]
    {
    -2.048,         // rosenbrock
    -5.12,          // rastrigin
    -5.12,          // sphere
    -5.12,          // step
    -1.28,          // noisy quartic
    -5.12,          // booth
    -600.0,         // griewank
    0.0,            // median
    0.0,            // sum
    0.0,            // product
    -512.03,        // schwefel
    0.0,            // min
    -5.12,          // rotated-rastrigin
    -512.03,        // rotated-schwefel
    -600.0,         // rotated-griewank
    0,              // langerman
    -3.0,           // lennard-jones
    -5.0,       // lunacek
    };

    public static final double maxRange[] = new double[]
    {
    2.048,          // rosenbrock
    5.12,           // rastrigin
    5.12,           // sphere
    5.12,           // step
    1.28,           // noisy quartic
    5.12,           // booth
    600.0,          // griewank
    1.0,            // median
    1.0,            // sum
    2.0,            // product
    511.97,         // schwefel
    1.0,            // min
    5.12,           // rotated-rastrigin
    511.97,         // rotated-schwefel
    600.0,          // rotated-griewank
    10,             // langerman
    3.0,                                // lennard-jones
    5.0                 // lunacek
    };

    public long seed;  // rotation seed for rotation problems

    boolean alreadyChecked = false;
    public void checkRange(EvolutionState state, int problem, double[] genome)
        {
        if (alreadyChecked || state.generation > 0) return;
        alreadyChecked = true;

        for(int i = 0; i < state.population.subpops.size(); i++)
            {
            if (!(state.population.subpops.get(i).species instanceof FloatVectorSpecies))
                {
                state.output.fatal("ECSuite requires species " + i + " to be a FloatVectorSpecies, but it is a: " +  state.population.subpops.get(i).species);
                }
            FloatVectorSpecies species = (FloatVectorSpecies)(state.population.subpops.get(i).species);
            for(int k = 0; k < genome.length; k++)
                {
                if (species.minGene(k) != minRange[problem] ||
                    species.maxGene(k) != maxRange[problem])
                    {
                    state.output.warning("Gene range is nonstandard for problem " + problemName[problem] + ".\nFirst occurrence: Subpopulation " + i + " Gene " + k +
                        " range was [" + species.minGene(k) + ", " + species.maxGene(k) +
                        "], expected [" + minRange[problem] + ", " + maxRange[problem] + "]");
                    return;  // done here
                    }
                }
            }

        if (problemType == PROB_LANGERMAN)
            {
            // Langerman has a maximum genome size of 10
            if (genome.length > 10)
                state.output.fatal("The Langerman function requires that the genome size be a value from 1 to 10 inclusive.  It is presently " + genome.length);
            }

        else if (problemType == PROB_LENNARDJONES)
            {
            // Lennard-Jones requires that its genomes be multiples of 3
            if (genome.length % 3 != 0)
                state.output.fatal("The Lennard-Jones function requires that the genome size be a multiple of 3.  It is presently " + genome.length);
            }

        }

    // nothing....
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        String wp = state.parameters.getStringWithDefault( base.push( P_WHICH_PROBLEM ), null, "" );
        if( wp.compareTo( V_ROSENBROCK ) == 0 || wp.compareTo (V_F2)==0 )
            problemType = PROB_ROSENBROCK;
        else if ( wp.compareTo( V_RASTRIGIN ) == 0 )
            problemType = PROB_RASTRIGIN;
        else if ( wp.compareTo( V_SPHERE ) == 0 || wp.compareTo (V_F1)==0)
            problemType = PROB_SPHERE;
        else if ( wp.compareTo( V_STEP ) == 0 || wp.compareTo (V_F3)==0)
            problemType = PROB_STEP;
        else if ( wp.compareTo( V_NOISY_QUARTIC ) == 0 || wp.compareTo (V_F4)==0)
            problemType = PROB_NOISY_QUARTIC;
        else if( wp.compareTo( V_BOOTH ) == 0 )
            problemType = PROB_BOOTH;
        else if( wp.compareTo( V_GRIEWANK ) == 0 )
            problemType = PROB_GRIEWANK;
        else if (wp.compareTo( V_GRIEWANGK ) == 0 )
            {
            state.output.warning("Incorrect parameter name (\"griewangk\") used, should be \"griewank\"", base.push( P_WHICH_PROBLEM ), null );
            problemType = PROB_GRIEWANK;
            }
        else if( wp.compareTo( V_MEDIAN ) == 0 )
            problemType = PROB_MEDIAN;
        else if( wp.compareTo( V_SUM ) == 0 )
            problemType = PROB_SUM;
        else if( wp.compareTo( V_PRODUCT ) == 0 )
            problemType = PROB_PRODUCT;
        else if (wp.compareTo( V_SCHWEFEL ) == 0 )
            problemType = PROB_SCHWEFEL;
        else if (wp.compareTo( V_MIN ) == 0 )
            problemType = PROB_MIN;
        else if (wp.compareTo( V_ROTATED_RASTRIGIN) == 0)
            problemType = PROB_ROTATED_RASTRIGIN;
        else if (wp.compareTo( V_ROTATED_SCHWEFEL) == 0)
            problemType = PROB_ROTATED_SCHWEFEL;
        else if (wp.compareTo( V_ROTATED_GRIEWANK) == 0)
            problemType = PROB_ROTATED_GRIEWANK;
        else if (wp.compareTo(V_LANGERMAN) == 0)
            problemType = PROB_LANGERMAN ;
        else if (wp.compareTo(V_LENNARDJONES) == 0)
            problemType = PROB_LENNARDJONES ;
        else if (wp.compareTo(V_LUNACEK) == 0)
            problemType = PROB_LUNACEK ;

        else state.output.fatal(
            "Invalid value for parameter, or parameter not found.\n" +
            "Acceptable values are:\n" +
            "  " + V_ROSENBROCK + " (or " + V_F2 + ")\n" +
            "  " + V_RASTRIGIN + "\n" +
            "  " + V_SPHERE + " (or " + V_F1 + ")\n" +
            "  " + V_STEP + " (or " + V_F3 + ")\n" +
            "  " + V_NOISY_QUARTIC + " (or " + V_F4 + ")\n"+
            "  " + V_BOOTH + "\n" +
            "  " + V_GRIEWANK + "\n" +
            "  " + V_MEDIAN + "\n" +
            "  " + V_SUM + "\n" +
            "  " + V_PRODUCT + "\n" +
            "  " + V_SCHWEFEL + "\n"+
            "  " + V_MIN + "\n"+
            "  " + V_ROTATED_RASTRIGIN + "\n" +
            "  " + V_ROTATED_SCHWEFEL + "\n" +
            "  " + V_ROTATED_GRIEWANK + "\n" +
            "  " + V_LANGERMAN + "\n" +
            "  " + V_LENNARDJONES + "\n" +
            "  " + V_LUNACEK + "\n",
            base.push( P_WHICH_PROBLEM ) );
        
        seed = state.parameters.getLongWithDefault( base.push( P_SEED ), null, ROTATION_SEED );
        if (seed <= 0)
            state.output.fatal("If a rotation seed is provided, it must be > 0", base.push( P_SEED ), null);
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (ind.evaluated)  // don't bother reevaluating
            return;

        if( !( ind instanceof DoubleVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals." );

        DoubleVectorIndividual temp = (DoubleVectorIndividual)ind;
        double[] genome = temp.genome;
        //int len = genome.length;

        // this curious break-out makes it easy to use the isOptimal() and function() methods
        // for other purposes, such as coevolutionary versions of this class.

        // compute the fitness on a per-function basis
        double fit = (function(state, problemType, temp.genome, threadnum));

        // compute if we're optimal on a per-function basis
        boolean isOptimal = isOptimal(problemType, fit);

        // set the fitness appropriately
        if (fit < (0.0 - Double.MAX_VALUE))  // uh oh -- can be caused by Product for example
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, 0.0 - Double.MAX_VALUE, isOptimal );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else if (fit > Double.MAX_VALUE)  // uh oh -- can be caused by Product for example
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, Double.MAX_VALUE, isOptimal );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, fit, isOptimal );
            }
        ind.evaluated = true;
        }


    public boolean isOptimal(int function, double fitness)
        {
        switch(problemType)
            {
            case PROB_ROSENBROCK:
            case PROB_RASTRIGIN:
            case PROB_SPHERE:
            case PROB_STEP:
                return fitness == 0.0;

            case PROB_NOISY_QUARTIC:
            case PROB_BOOTH:
            case PROB_GRIEWANK:
            case PROB_MEDIAN:
            case PROB_SUM:
            case PROB_PRODUCT:
            case PROB_SCHWEFEL:
            case PROB_ROTATED_RASTRIGIN:    // not sure
            case PROB_ROTATED_SCHWEFEL:
            case PROB_ROTATED_GRIEWANK:
            case PROB_MIN:
            case PROB_LANGERMAN:        // may be around -1.4
            case PROB_LENNARDJONES:
            case PROB_LUNACEK:
            default:
                return false;
            }
        }

    public double function(EvolutionState state, int function, double[] genome, int threadnum)
        {

        checkRange(state, function, genome);

        double value = 0;
        double len = genome.length;
        switch(function)
            {
            case PROB_ROSENBROCK:
                for( int i = 1 ; i < len ; i++ )
                    {
                    double gj = genome[i-1] ;
                    double gi = genome[i] ;
                    value += (1 - gj) * (1 - gj) + 100 * (gi - gj*gj) * (gi - gj*gj);
                    }
                return -value;


            case PROB_RASTRIGIN:
                final double A = 10.0;
                value = len * A;
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i]  ;
                    value += ( gi*gi - A * Math.cos( 2 * Math.PI * gi ) );
                    }
                return -value;


            case PROB_SPHERE:
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += gi * gi;
                    }
                return -value;


            case PROB_STEP:
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    // The reason for the 6 is that this is the equation De Jong used in the De Jong Test Suite
                    value += 6 + Math.floor( gi );
                    }
                return -value;


            case PROB_NOISY_QUARTIC:
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += (i+1)*(gi*gi*gi*gi) + state.random[threadnum].nextGaussian();  // gauss(0,1)
                    }
                return -value;


            case PROB_BOOTH:
                if( len != 2 )
                    state.output.fatal( "The Booth problem is defined for only two terms, and as a consequence the genome of the DoubleVectorIndividual should have size 2." );
                double g0 = genome[0] ;
                double g1 = genome[1] ;
                value = (g0 + 2*g1 - 7) * (g0 + 2*g1 - 7) +
                    (2*g0 + g1 - 5) * (2*g0 + g1 - 5);
                return -value;


            case PROB_GRIEWANK:
                value = 1;
                double prod = 1;
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += (gi*gi)/4000.0;
                    prod *= Math.cos( gi / Math.sqrt(i+1) );
                    }
                value -= prod;
                return -value;


            case PROB_SCHWEFEL:
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += -gi * Math.sin(Math.sqrt(Math.abs(gi)));
                    }
                return -value;


            case PROB_MEDIAN:           // FIXME, need to do a better median-finding algorithm, such as http://www.ics.uci.edu/~eppstein/161/960130.html
                double[] sorted = new double[(int)len];
                System.arraycopy(genome, 0, sorted, 0, sorted.length);
                ec.util.QuickSort.qsort(sorted);
                return sorted[sorted.length / 2] ;               // note positive

            case PROB_SUM:
                value = 0.0;
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += gi;
                    }
                return value;                                                                   // note positive

            case PROB_MIN:
                value = genome[0] ;
                for( int i = 1 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    if (value > gi) value = gi;
                    }
                return value;                                                                   // note positive

            case PROB_PRODUCT:
                value = 1.0;
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value *= gi;
                    }
                return value;                                                                   // note positive

            case PROB_ROTATED_RASTRIGIN:
                {
                synchronized(rotationMatrix)            // synchronizations are rare in ECJ.  :-(
                    {
                    if (rotationMatrix[0] == null)
                        rotationMatrix[0] = buildRotationMatrix(state, seed, (int)len);
                    }

                // now we know the matrix exists rotate the matrix and return its value
                double[] val = mul(rotationMatrix[0], genome);
                return function(state, PROB_RASTRIGIN, val, threadnum);
                }

            case PROB_ROTATED_SCHWEFEL:
                {
                synchronized(rotationMatrix)            // synchronizations are rare in ECJ.  :-(
                    {
                    if (rotationMatrix[0] == null)
                        rotationMatrix[0] = buildRotationMatrix(state, seed, (int)len);
                    }

                // now we know the matrix exists rotate the matrix and return its value
                double[] val = mul(rotationMatrix[0], genome);
                return function(state, PROB_SCHWEFEL, val, threadnum);
                }

            case PROB_ROTATED_GRIEWANK:
                {
                synchronized(rotationMatrix)            // synchronizations are rare in ECJ.  :-(
                    {
                    if (rotationMatrix[0] == null)
                        rotationMatrix[0] = buildRotationMatrix(state, seed, (int)len);
                    }

                // now we know the matrix exists rotate the matrix and return its value
                double[] val = mul(rotationMatrix[0], genome);
                return function(state, PROB_GRIEWANK, val, threadnum);
                }

            case PROB_LANGERMAN:
                {
                return 0.0 - langerman(genome);
                }

            case PROB_LENNARDJONES:
                {
                int numAtoms = genome.length / 3;
                double v = 0.0 ;

                for(int i = 0 ; i < numAtoms - 1 ; i++ )
                    {
                    for(int j = i + 1 ; j < numAtoms ; j++ )
                        {
                        // double d = dist(genome, i, j);
                        double a = genome[i * 3] - genome[j * 3];
                        double b = genome[i * 3 + 1] - genome[j * 3 + 1];
                        double c = genome[i * 3 + 2] - genome[j * 3 + 2];

                        double d = Math.sqrt(a * a + b * b + c * c);

                        double r12 = Math.pow(d, -12.0);
                        double r6 = Math.pow(d, -6.0);
                        double e = r12 - r6 ;
                        v += e ;
                        }
                    }
                v *= -4.0 ;
                return v;
                }

            case PROB_LUNACEK:
                {
                // Lunacek function: for more information, please see --
                // http://arxiv.org/pdf/1207.4318.pdf
                // http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.154.1657
                // http://www.cs.unm.edu/~neal.holts/dga/benchmarkFunction/lunacek.html
                // http://www.cs.colostate.edu/sched/pubs/ppsn08impact.pdf
                                
                double s = 1.0 - (1.0 / (2.0 * Math.sqrt(genome.length + 20.0) - 8.2)) ;
                
                // depth of the sphere, could be 1, 2, 3, or 4. 1 is deeper than 4
                // this could be also be a fraction I guess.
                double d = 1.0 ; 
                double mu1 = 2.5 ;
                double mu2 = -1.0 * Math.sqrt(Math.abs((mu1 * mu1 - d) / s));  // probably don't need the abs
                double sum1 = 0.0;
                double sum2 = 0.0;
                double sum3 = 0.0;

                for(int i = 0 ; i < genome.length ; i++)
                    {
                    double genomei = genome[i];
                    sum1 += (genomei - mu1)*(genomei - mu1) ;
                    sum2 += (genomei - mu2)*(genomei - mu2) ;
                    sum3 += 1.0 - Math.cos(2.0 * Math.PI * (genomei - mu1));
                    }
                return Math.min(sum1, d * genome.length + s * sum2) + 10.0 * sum3 ;
                }
                        
            default:
                state.output.fatal( "ec.app.ecsuite.ECSuite has an invalid problem -- how on earth did that happen?" );
                return 0;  // never happens
            }

        }

    // magic arrays for the Langerman problem

    private double[][] afox10 =
        {
        {9.681, 0.667, 4.783, 9.095, 3.517, 9.325, 6.544, 0.211, 5.122, 2.020},
        {9.400, 2.041, 3.788, 7.931, 2.882, 2.672, 3.568, 1.284, 7.033, 7.374},
        {8.025, 9.152, 5.114, 7.621, 4.564, 4.711, 2.996, 6.126, 0.734, 4.982},
        {2.196, 0.415, 5.649, 6.979, 9.510, 9.166, 6.304, 6.054, 9.377, 1.426},
        {8.074, 8.777, 3.467, 1.863, 6.708, 6.349, 4.534, 0.276, 7.633, 1.567},
        {7.650, 5.658, 0.720, 2.764, 3.278, 5.283, 7.474, 6.274, 1.409, 8.208},
        {1.256, 3.605, 8.623, 6.905, 0.584, 8.133, 6.071, 6.888, 4.187, 5.448},
        {8.314, 2.261, 4.224, 1.781, 4.124, 0.932, 8.129, 8.658, 1.208, 5.762},
        {0.226, 8.858, 1.420, 0.945, 1.622, 4.698, 6.228, 9.096, 0.972, 7.637},
        {7.305, 2.228, 1.242, 5.928, 9.133, 1.826, 4.060, 5.204, 8.713, 8.247},
        {0.652, 7.027, 0.508, 4.876, 8.807, 4.632, 5.808, 6.937, 3.291, 7.016},
        {2.699, 3.516, 5.874, 4.119, 4.461, 7.496, 8.817, 0.690, 6.593, 9.789},
        {8.327, 3.897, 2.017, 9.570, 9.825, 1.150, 1.395, 3.885, 6.354, 0.109},
        {2.132, 7.006, 7.136, 2.641, 1.882, 5.943, 7.273, 7.691, 2.880, 0.564},
        {4.707, 5.579, 4.080, 0.581, 9.698, 8.542, 8.077, 8.515, 9.231, 4.670},
        {8.304, 7.559, 8.567, 0.322, 7.128, 8.392, 1.472, 8.524, 2.277, 7.826},
        {8.632, 4.409, 4.832, 5.768, 7.050, 6.715, 1.711, 4.323, 4.405, 4.591},
        {4.887, 9.112, 0.170, 8.967, 9.693, 9.867, 7.508, 7.770, 8.382, 6.740},
        {2.440, 6.686, 4.299, 1.007, 7.008, 1.427, 9.398, 8.480, 9.950, 1.675},
        {6.306, 8.583, 6.084, 1.138, 4.350, 3.134, 7.853, 6.061, 7.457, 2.258},
        {0.652, 0.343, 1.370, 0.821, 1.310, 1.063, 0.689, 8.819, 8.833, 9.070},
        {5.558, 1.272, 5.756, 9.857, 2.279, 2.764, 1.284, 1.677, 1.244, 1.234},
        {3.352, 7.549, 9.817, 9.437, 8.687, 4.167, 2.570, 6.540, 0.228, 0.027},
        {8.798, 0.880, 2.370, 0.168, 1.701, 3.680, 1.231, 2.390, 2.499, 0.064},
        {1.460, 8.057, 1.336, 7.217, 7.914, 3.615, 9.981, 9.198, 5.292, 1.224},
        {0.432, 8.645, 8.774, 0.249, 8.081, 7.461, 4.416, 0.652, 4.002, 4.644},
        {0.679, 2.800, 5.523, 3.049, 2.968, 7.225, 6.730, 4.199, 9.614, 9.229},
        {4.263, 1.074, 7.286, 5.599, 8.291, 5.200, 9.214, 8.272, 4.398, 4.506},
        {9.496, 4.830, 3.150, 8.270, 5.079, 1.231, 5.731, 9.494, 1.883, 9.732},
        {4.138, 2.562, 2.532, 9.661, 5.611, 5.500, 6.886, 2.341, 9.699, 6.500}
        };

    private double[] cfox10 =
        {
        0.806,  0.517,  1.5,    0.908,  0.965,
        0.669,  0.524,  0.902,  0.531,  0.876,
        0.462,  0.491,  0.463,  0.714,  0.352,
        0.869,  0.813,  0.811,  0.828,  0.964,
        0.789,  0.360,  0.369,  0.992,  0.332,
        0.817,  0.632,  0.883,  0.608,  0.326
        };

    private double langerman(double genome[])
        {

        double  sum = 0 ;

        for ( int i = 0 ; i < 30 ; i++ )
            {
            // compute squared distance
            double distsq = 0.0;
            double t;
            double[] afox10i = afox10[i];
            for(int j = 0; j < genome.length; j++)
                {
                t = genome[j] - afox10i[j];
                distsq += t * t;
                }

            sum += cfox10[i] * Math.exp(-distsq / Math.PI) * Math.cos(distsq * Math.PI);
            }
        return 0 - sum;
        }




    /*

      -----------------
      Rotation facility
      -----------------

      This code is just used by the Rotated Schwefel and Rotated Rastrigin functions to rotate their
      functions by a certain amount.  The code is largely based on the rotation scheme described in
      "Completely Derandomized Self-Adaptation in Evolutionary Strategies",
      Nikolaus Hansen and Andreas Ostermeier, Evolutionary Computation 9(2): 159--195.

      We fix a hard-coded rotation matrix which is the same for all problems, in order to guarantee
      correctness in gathering results over multiple jobs.  But you can change that easily if you like.

    */

    public static double[][][] rotationMatrix = new double[1][][];  // the actual matrix is stored in rotationMatrix[0] -- a hack

    /** Dot product between two column vectors.  Does not modify the original vectors. */
    public static double dot(double[] x, double[] y)
        {
        double val = 0;
        for(int i =0; i < x.length; i++)
            val += x[i] * y[i];
        return val;
        }

    /** Multiply a column vector against a matrix[row][column].  Does not modify the original vector or matrix. */
    public static double[] mul(double [/* row */ ][ /* column */] matrix, double[] x)
        {
        double[] val = new double[matrix.length];
        for(int i = 0; i < matrix.length; i++)
            {
            double sum = 0.0;
            double[] m = matrix[i];
            for(int j = 0; j < m.length; j++)
                sum += m[j] * x[j];
            val[i] = sum;
            }
        return val;
        }

    /** Scalar multiply against a column vector. Does not modify the original vector. */
    public static double[] scalarMul(double scalar, double[] x)
        {
        double[] val = new double[x.length];
        for(int i =0; i < x.length; i++)
            val[i] = x[i] * scalar;
        return val;
        }

    /** Subtract two column vectors.  Does not modify the original vectors. */
    public static double[] sub(double[] x, double[] y)
        {
        double[] val = new double[x.length];
        for(int i =0; i < x.length; i++)
            val[i] = x[i] - y[i];
        return val;
        }

    /** Normalize a column vector.  Does not modify the original vector. */
    public static double[] normalize(double[] x)
        {
        double[] val = new double[x.length];
        double sumsq = 0;
        for(int i =0; i < x.length; i++)
            sumsq += x[i] * x[i];
        sumsq = Math.sqrt(sumsq);
        for(int i =0; i < x.length; i++)
            val[i] = x[i] / sumsq;
        return val;
        }


    /** Fixed rotation seed so all jobs use exactly the same rotation space. */
    public static final long ROTATION_SEED = 9731297;

    /** Build an NxN rotation matrix[row][column] with a given seed. */
    public static double[ /* row */ ][ /* column */] buildRotationMatrix(EvolutionState state, long rotationSeed, int N)
        {
        if (rotationSeed == ROTATION_SEED)
            state.output.warnOnce("Default rotation seed being used (" + rotationSeed + ")");
                
        MersenneTwisterFast rand = new MersenneTwisterFast(rotationSeed);  // it's rare to need to do this, but we need to guarantee the same rotation space
        for(int i = 0; i < 624 * 4; i++) // prime the MT for 4 full sample iterations to get it warmed up
            rand.nextInt();
        
        double o[ /* row */ ][ /* column */ ] = new double[N][N];

        // make random values
        for(int i = 0; i < N; i++)
            for(int k = 0; k < N; k++)
                o[i][k] = rand.nextGaussian();

        // build random values
        for(int i = 0; i < N; i++)
            {
            // extract o[i] -> no
            double[] no = new double[N];
            for(int k=0; k < N; k++)
                no[k] = o[i][k];

            // go through o[i] and o[j], modifying no
            for(int j = 0; j < i; j++)
                {
                double d = dot(o[i], o[j]);
                double[] val = scalarMul(d, o[j]);
                no = sub(no, val);
                }
            o[i] = normalize(no);
            }

        return o;
        }

    }
