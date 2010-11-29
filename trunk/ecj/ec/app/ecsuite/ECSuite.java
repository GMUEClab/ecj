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
 * Created: Thu MAr 22 16:27:15 2001
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

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: rosenbrock rastrigin sphere step noisy-quartic kdj-f1 kdj-f2 kdj-f3 kdj-f4 booth median [or] griewangk</font>/td>
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
    public static final String V_MEDIAN = "median";

    public static final int PROB_ROSENBROCK = 0;
    public static final int PROB_RASTRIGIN = 1;
    public static final int PROB_SPHERE = 2;
    public static final int PROB_STEP = 3;
    public static final int PROB_NOISY_QUARTIC = 4;
    public static final int PROB_BOOTH = 5;
    public static final int PROB_GRIEWANGK = 6;
    public static final int PROB_MEDIAN = 7;
    
    public int problemType = PROB_ROSENBROCK;  // defaults on Rosenbrock

    // for RASTRIGIN function
    public final static float A = 10.0f;

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
        else if( wp.compareTo( V_GRIEWANGK ) == 0 )
            problemType = PROB_GRIEWANGK;           
        else if( wp.compareTo( V_MEDIAN ) == 0 )
            problemType = PROB_MEDIAN;           
        else state.output.fatal(
            "Invalid value for parameter, or parameter not found.\n" +
            "Acceptable values are:\n" +
            "  " + V_ROSENBROCK + "(or " + V_F2 + ")\n" +
            "  " + V_RASTRIGIN + "\n" +
            "  " + V_SPHERE + "(or " + V_F1 + ")\n" +
            "  " + V_STEP + "(or " + V_F3 + ")\n" +
            "  " + V_NOISY_QUARTIC + "(or " + V_F4 + ")\n"+
            "  " + V_BOOTH + "\n" +
            "  " + V_GRIEWANGK + "\n" + 
			"  " + V_MEDIAN + "\n",
            base.push( P_WHICH_PROBLEM ) );
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
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
		((SimpleFitness)(ind.fitness)).setFitness( state, (float)fit, isOptimal );
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
            case PROB_GRIEWANGK:
            case PROB_MEDIAN:
			default:
				return false;
			
			}
		}

	public double function(EvolutionState state, int function, double[] genome, int threadnum)
		{
		double value = 0;
		int len = genome.length;
        switch(function)
            {
            case PROB_ROSENBROCK:
                for( int i = 1 ; i < len ; i++ )
                    value += 100*(genome[i-1]*genome[i-1]-genome[i])*
                        (genome[i-1]*genome[i-1]-genome[i]) +
                        (1-genome[i-1])*(1-genome[i-1]);
			return -value;

                
            case PROB_RASTRIGIN:
                value = len * A;
                for( int i = 0 ; i < len ; i++ )
                    value += ( genome[i]*genome[i] - A * Math.cos( 2 * Math.PI * genome[i] ) );
			return -value;

                
            case PROB_SPHERE:
                for( int i = 0 ; i < len ; i++ )
                    value += genome[i]*genome[i];
			return -value;


            case PROB_STEP:
                for( int i = 0 ; i < len ; i++ )
                    value += 6 + Math.floor( genome[i] );
			return -value;


            case PROB_NOISY_QUARTIC:
                for( int i = 0 ; i < len ; i++ )
                    value += (i+1)*(genome[i]*genome[i]*genome[i]*genome[i]) + // no longer : Math.pow( genome[i], 4 ) +
                        state.random[threadnum].nextDouble();
			return -value;


            case PROB_BOOTH:
                if( len != 2 )
                    state.output.fatal( "The Booth problem is defined for only two terms, and as a consequence the genome of the DoubleVectorIndividual should have size 2." );
                value = (genome[0] + 2*genome[1] - 7) * (genome[0] + 2*genome[1] - 7) +
                    (2*genome[0] + genome[1] - 5) * (2*genome[0] + genome[1] - 5);
			return -value;


            case PROB_GRIEWANGK:
                value = 1;
                double prod = 1;
                for( int i = 0 ; i < len ; i++ )
                    {
                    value += (genome[i]*genome[i])/4000.0;
                    prod *= Math.cos( genome[i] / Math.sqrt(i+1) );
                    }
                value -= prod;
			return -value;


            case PROB_MEDIAN:		// FIXME, need to do a better median-finding algorithm, such as http://www.ics.uci.edu/~eppstein/161/960130.html
				double[] sorted = new double[genome.length];
				System.arraycopy(genome, 0, sorted, 0, sorted.length);
				ec.util.QuickSort.qsort(sorted);
				return sorted[sorted.length / 2];		// note positive

            default:
                state.output.fatal( "ec.app.ecsuite.ECSuite has an invalid problem -- how on earth did that happen?" );
				return 0;  // never happens
            }
        }
    }
