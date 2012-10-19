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
 * @author Liviu Panait and Sean Luke
 * @version 1.0 
 */

/**
   Several standard Evolutionary Computation functions are implemented: Rastrigin, De Jong's test suite
   F1-F4 problems (Sphere, Rosenbrock, Step, Noisy-Quartic), Booth (from [Schwefel, 1995]), and Griewangk.
   As the SimpleFitness is used for maximization problems, the mapping f(x) --> -f(x) is used to transform
   the problems into maximization ones.

   <p>Problems have been set up so that their traditional ranges are scaled so you can use a min-gene of -1.0
   and a max-gene of 1.0
   
   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: rosenbrock rastrigin sphere step noisy-quartic kdj-f1 kdj-f2 kdj-f3 kdj-f4 booth median schwefel product [or] griewangk</font></td>
   <td valign=top>(The vector problem to test against.  Some of the types are synonyms: kdj-f1 = sphere, kdj-f2 = rosenbrock, kdj-f3 = step, kdj-f4 = noisy-quartic.  "kdj" stands for "Ken DeJong", and the numbers are the problems in his test suite)</td></tr>
   </table>

*/
 
public class ECSuite extends Problem implements SimpleProblemForm
    {
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
    V_ROTATED_SCHWEFEL
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
    -512.03         // rotated-schwefel
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
    511.97          // rotated-schwefel
    };
                
        
    boolean alreadyChecked = false;
    public void checkRange(EvolutionState state, int problem)
        {
        if (alreadyChecked || state.generation > 0) return;
        alreadyChecked = true;
                
        for(int i = 0; i < state.population.subpops.length; i++)
            {
            if (!(state.population.subpops[i].species instanceof FloatVectorSpecies))
                {
                state.output.fatal("ECSuite requires species " + i + " to be a FloatVectorSpecies, but it is a: " +  state.population.subpops[i].species);
                }
            FloatVectorSpecies species = (FloatVectorSpecies)(state.population.subpops[i].species);
            for(int k = 0; k < species.minGenes.length; k++)
                {
                if (species.minGenes[k] != minRange[problem] ||
                    species.maxGenes[k] != maxRange[problem])
                    {
                    state.output.warning("Gene range is nonstandard for problem " + problemName[problem] + ".\nFirst occurrence: Subpopulation " + i + " Gene " + k + 
                        " range was [" + species.minGenes[k] + ", " + species.maxGenes[k] + 
                        "], expected [" + minRange[problem] + ", " + maxRange[problem] + "]");
                    return;  // done here
                    }
                }
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
            "  " + V_ROTATED_SCHWEFEL + "\n",
            base.push( P_WHICH_PROBLEM ) );
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
        int len = genome.length;

        // this curious break-out makes it easy to use the isOptimal() and function() methods
        // for other purposes, such as coevolutionary versions of this class.
                
        // compute the fitness on a per-function basis
        double fit = (function(state, problemType, temp.genome, threadnum));
                
        // compute if we're optimal on a per-function basis
        boolean isOptimal = isOptimal(problemType, fit);
                
        // set the fitness appropriately
        if ((float)fit < (0.0f - Float.MAX_VALUE))  // uh oh -- can be caused by Product for example
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, 0.0f - Float.MAX_VALUE, isOptimal );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else if ((float)fit > Float.MAX_VALUE)  // uh oh -- can be caused by Product for example
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, Float.MAX_VALUE, isOptimal );
            state.output.warnOnce("'Product' type used: some fitnesses are negative infinity, setting to lowest legal negative number.");
            }
        else
            {
            ((SimpleFitness)(ind.fitness)).setFitness( state, (float)fit, isOptimal );
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
                return fitness == 0.0f;

            case PROB_NOISY_QUARTIC:
            case PROB_BOOTH:
            case PROB_GRIEWANK:
            case PROB_MEDIAN:
            case PROB_SUM:
            case PROB_PRODUCT:
            case PROB_SCHWEFEL:
            case PROB_ROTATED_RASTRIGIN:    // not sure
            case PROB_ROTATED_SCHWEFEL:
            case PROB_MIN:
            default:
                return false;
            }
        }

    public double function(EvolutionState state, int function, double[] genome, int threadnum)
        {
                
        checkRange(state, function);
                
        double value = 0;
        double len = genome.length;
        switch(function)
            {
            case PROB_ROSENBROCK:
                for( int i = 1 ; i < len ; i++ )
                    {
                    double gj = genome[i-1] ;
                    double gi = genome[i] ;
                    value += 100 * (gj*gj - gj) * (gj*gj - gj) +  (1-gj) * (1-gj);
                    }
                return -value;

                
            case PROB_RASTRIGIN:
                final float A = 10.0f;
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
                    value += 6 + Math.floor( gi );
                    }
                return -value;


            case PROB_NOISY_QUARTIC:
                for( int i = 0 ; i < len ; i++ )
                    {
                    double gi = genome[i] ;
                    value += (i+1)*(gi*gi*gi*gi) + state.random[threadnum].nextDouble();
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
                    rotationMatrix[0] = buildRotationMatrix(ROTATION_SEED, (int)len);
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
                    rotationMatrix[0] = buildRotationMatrix(ROTATION_SEED, (int)len);
                }

            // now we know the matrix exists rotate the matrix and return its value
            double[] val = mul(rotationMatrix[0], genome);
            return function(state, PROB_SCHWEFEL, val, threadnum);
            }

            default:
                state.output.fatal( "ec.app.ecsuite.ECSuite has an invalid problem -- how on earth did that happen?" );
                return 0;  // never happens
            }
                
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
    public static double[ /* row */ ][ /* column */] buildRotationMatrix(double rotationSeed, int N)
        {
        MersenneTwisterFast rand = new MersenneTwisterFast(ROTATION_SEED);  // it's rare to need to do this, but we need to guarantee the same rotation space
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
