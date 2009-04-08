/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.moosuite;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

/**
   Several standard Multi-objective benchmarks are implemented: 
   <ul>
   <li>ZDT1: covex Pareto front, formed with g(X) = 1. 
   <li>ZDT2, 
   <li>ZDT3, 
   <li>ZDT4, 
   <li>ZDT6, 
   <li>SPHERE. 
   </ul>
   
   <p>[ZDT]: Zitzler, E., Deb, K., and Thiele, L., 2000, Comparison of Multiobjective Evolutionary
   Algorithms: Empirical Results, Evolutionary Computation, Vol. 8, No. 2, pp173-195.
   
   <p>Schaffer, J. D. (1985).Multiple objective optimization with vector evaluated genetic
   algorithms. In J. J. Grefenstette (Ed.), Proceedings of an International Conference
   on Genetic Algorithms and Their Applications, Pittsburgh, PA, pp. 93-100.
   sponsored by Texas Instruments and U.S. Navy Center for Applied Research in
   Artificial Intelligence (NCARAI).
   
   <p>Laumanns, M., G. Rudolph, and H.-P. Schwefel (2001, June). Mutation control
   and convergence in evolutionary multi-objective optimization. In Proceedings of
   the 7th International Mendel Conference on Soft Computing (MENDEL 2001), Brno, Czech Republic.
   

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: zdt-t1, zdt-t2, zdt-t3, zdt-t4, zdt-t6, sphere</font></td>
   <td valign=top>The multi-objective optimization problem to test against. </td></tr>
   
   <tr><td valign=top><i>base</i>.<tt>num-variables</tt><br>
   <font size=-1>int (default=30)</font></td>
   <td valign=top>The number of variables; genome-size is set to this value internally.</td></tr>
   </table>
   
   
   @author Gabriel Catalin Balan 

*/
 
public class MooSuite extends Problem implements SimpleProblemForm
    {
    public static final String P_WHICH_PROBLEM = "type";
    public static final String P_ZDT1 = "zdt1";
    public static final String P_ZDT2 = "zdt2";
    public static final String P_ZDT3 = "zdt3";
    public static final String P_ZDT4 = "zdt4";
    public static final String P_ZDT6 = "zdt6";
    public static final String P_SPHERE = "sphere";


    public static final int PROB_SPHERE = 0;
    public static final int PROB_ZDT1 = 1;
    public static final int PROB_ZDT2 = 2;
    public static final int PROB_ZDT3 = 3;
    public static final int PROB_ZDT4 = 4;
    public static final int PROB_ZDT6 = 6;

    public int problemType = PROB_ZDT1;  // defaults on zdt1

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        String wp = state.parameters.getStringWithDefault( base.push( P_WHICH_PROBLEM ), null, "" );
        if( wp.compareTo( P_ZDT1) == 0 )
            problemType = PROB_ZDT1;
        else if ( wp.compareTo( P_ZDT2) == 0 )
            problemType = PROB_ZDT2;
        else if ( wp.compareTo( P_ZDT3) == 0 )
            problemType = PROB_ZDT3;
        else if ( wp.compareTo( P_ZDT4) == 0 )
            problemType = PROB_ZDT4;
        else if ( wp.compareTo( P_ZDT6) == 0 )
            problemType = PROB_ZDT6;
        else if( wp.compareTo( P_SPHERE) == 0 )
            problemType = PROB_SPHERE;         
        else state.output.fatal(
            "Invalid value for parameter, or parameter not found.\n" +
            "Acceptable values are:\n" +
            "  " + P_ZDT1 + "\n" +
            "  " + P_ZDT2 + "\n" +
            "  " + P_ZDT3 + "\n" +
            "  " + P_ZDT4 + "\n" +
            "  " + P_ZDT6 + "\n" +
            "  " + P_SPHERE + "\n",
            base.push( P_WHICH_PROBLEM ) );
        }
	
    private static final double TEN_PI = Math.PI*10;//ZDT3 uses it.
	private static final double FOUR_PI = Math.PI*4;//ZDT4 uses it.
	
    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if( !( ind instanceof DoubleVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals." );

        DoubleVectorIndividual temp = (DoubleVectorIndividual)ind;
        double[] genome = temp.genome;
        int numDecisionVars = genome.length;

		float[] fitnesses = ((MultiObjectiveFitness)ind.fitness).multifitness;

		double f, g, h, sum;
		
        switch(problemType)
            {
            case PROB_ZDT1:
        		f = genome[0];
        		fitnesses[0] = (float)f;
        		sum = 0;
        		for(int i = 1; i< numDecisionVars; ++i)
        			sum += genome[i];
        		g = 1d+9d*sum/(numDecisionVars-1);
        		h = 1d-Math.sqrt(f/g);
        		fitnesses[1] = (float)(g*h);
        		break;
                
            case PROB_ZDT2:
        		f = genome[0];
        		fitnesses[0] = (float)f;
        		sum = 0;
        		for(int i = 1; i< numDecisionVars; ++i)
        			sum += genome[i];
        		g = 1d+9d*sum/(numDecisionVars-1);
        		h = 1d-(f/g)*(f/g);
        		fitnesses[1] = (float)(g*h);
        		break;
        		
            case PROB_ZDT3:	
            	f = genome[0];
        		fitnesses[0] = (float)f;
        		sum = 0;
        		for(int i = 1; i< numDecisionVars; ++i)
        			sum += genome[i];
        		g = 1+9*sum/(numDecisionVars-1);
        		double foverg = f/g;
        		h = 1-Math.sqrt(foverg) - foverg * Math.sin(TEN_PI * f);
        		fitnesses[1] = (float)(g*h);
        		break;
            case PROB_ZDT4:
            	f = genome[0];
        		fitnesses[0] = (float)f;
        		sum = 0;
        		for(int i = 1; i< numDecisionVars; ++i)
        			sum += genome[i]*genome[i]- 10*Math.cos(FOUR_PI * genome[i]);
        			
        		g = 1+10*(numDecisionVars-1)+sum;
        		h = 1-Math.sqrt(f/g);
        		fitnesses[1] = (float)(g*h);
        		break;                
            case PROB_ZDT6:
        		break; 
            case PROB_SPHERE:
        		int numObjectives = fitnesses.length;
        		for(int j=0; j<numObjectives; ++j)
        		{
        			sum = (genome[j]-1)*(genome[j]-1);
        			for(int i=0; i<numDecisionVars; ++i)
        				if (i!=j)
        					sum += genome[i]*genome[i];
        			fitnesses[j] = (float)sum;
        		}
        		break;

            default:
                state.output.fatal( "ec.app.ecsuite.ECSuite has an invalid problem -- how on earth did that happen?" );
                break;
            }

        ind.evaluated = true;
        }

    public void describe(final Individual ind, 
        final EvolutionState state, 
        final int subpopulation,
        final int threadnum,
        final int log,
        final int verbosity)
        {
        return;
        }
    }
