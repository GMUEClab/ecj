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
   <li>ZDT1: Zitzler, Deb & Thiele
   <li>ZDT2: Zitzler, Deb & Thiele 
   <li>ZDT3: Zitzler, Deb & Thiele 
   <li>ZDT4: Zitzler, Deb & Thiele 
   <li>ZDT6: Zitzler, Deb & Thiele 
   <li>SPHERE: ftp.tik.ee.ethz.ch/pub/people/zitzler/ZLT2001a.pdf 
   <li>SCH: (Schaffer), (a.k.a. F1 in Srinivas & Deb)
   <li>F2: (Schaffer), (Srinivas & Deb),  (Coello Coello & Cortes)
   <li>unconstrained F3: Schaffer, Srinivas & Deb  (Chankong & Haimes)
   <li>QV: Quagliarella & Vicini
   <li>FON: Fonseca & Fleming
   <li>POL: Poloni
	   <li>KUR: Kursawe from the Errata of Zitzler's TIK-Report 103: "SPEA2: Improving the Strength Pareto Evolutionary Algorithm"
	    (note that many different versions are described in the literature).
   </ul>   

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: zdt-t1, zdt-t2, zdt-t3, zdt-t4, zdt-t6, sphere (aka sch)</font></td>
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
    public static final String P_SCH = "sch";
    public static final String P_FON = "fon";
    public static final String P_QV = "qv";
    public static final String P_POL = "pol";
    public static final String P_KUR = "kur";
    public static final String P_F1 = "f1";    
    public static final String P_F2 = "f2";
    public static final String P_F3 = "unconstrained-f3";

    public static final int PROB_SPHERE = 0;
    public static final int PROB_ZDT1 = 1;
    public static final int PROB_ZDT2 = 2;
    public static final int PROB_ZDT3 = 3;
    public static final int PROB_ZDT4 = 4;
    public static final int PROB_ZDT6 = 6;
    public static final int PROB_FON = 7;
    public static final int PROB_POL = 8;
    public static final int PROB_KUR = 9;
    public static final int PROB_QV = 10;
    public static final int PROB_SCH = 11;
    public static final int PROB_F2 = 12;
    public static final int PROB_F3 = 13;

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
        else if ( wp.compareTo( P_FON) == 0 )
            problemType = PROB_FON;
        else if ( wp.compareTo( P_POL) == 0 )
            problemType = PROB_POL;
        else if ( wp.compareTo( P_QV) == 0 )
            problemType = PROB_QV;
        else if ( wp.compareTo( P_KUR) == 0 )
            problemType = PROB_KUR;
        else if( wp.compareTo( P_SPHERE) == 0)
            problemType = PROB_SPHERE;         
        else if( wp.compareTo( P_F2) == 0)
            problemType = PROB_F2;
        else if( wp.compareTo( P_F3) == 0)
            problemType = PROB_F3;
        else if( wp.compareTo( P_SCH) == 0 || wp.compareTo( P_F1) == 0 )
            problemType = PROB_SCH;         
        else state.output.fatal(
            "Invalid value for parameter, or parameter not found.\n" +
            "Acceptable values are:\n" +
            "  " + P_ZDT1 + "\n" +
            "  " + P_ZDT2 + "\n" +
            "  " + P_ZDT3 + "\n" +
            "  " + P_ZDT4 + "\n" +
            "  " + P_ZDT6 + "\n" +
            "  " + P_POL + "\n" +
            "  " + P_FON + "\n" +
            "  " + P_KUR + "\n" +
            "  " + P_SPHERE + "\n" +
            "  " + P_SCH + "(or " + P_F1 + ")\n"+
            "  " + P_F2 + "\n",
            base.push( P_WHICH_PROBLEM ) );
        }
    private static final double TWO_PI = Math.PI*2;//QV uses it.
    private static final double TEN_PI = Math.PI*10;//ZDT3 uses it.
	private static final double FOUR_PI = Math.PI*4;//ZDT4 uses it.
	private static final double SIX_PI = Math.PI*6;//ZDT6 uses it.
	private static final double ONE_OVER_SQRT_3 = 1d/Math.sqrt(3);//FON uses it.
	private static final double A1 = 0.5*Math.sin(1) - 2*Math.cos(1) +    Math.sin(2)- 1.5*Math.cos(2);//POL uses it
	private static final double A2 = 1.5*Math.sin(1) -   Math.cos(1) + 2* Math.sin(2)- 0.5*Math.cos(2);//POL uses it

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
        		f = 1 - (Math.exp(-4 * genome[0]) * Math.pow(Math.sin(SIX_PI * genome[0]), 6));
        		fitnesses[0] = (float)f;
        		sum = 0;
        		for (int i = 1; i < numDecisionVars; ++i)
        			sum += genome[i];
        		g = 1d + 9 * Math.pow(sum / (numDecisionVars - 1), 0.25);
        		h = 1d - Math.pow(f / g, 2);
        		fitnesses[1] = (float) (g * h);
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
            case PROB_SCH:
            	if(numDecisionVars!=1) throw new RuntimeException("SCH needs exactly 1 decision variable (gene).");
        		double x = genome[0];
        		fitnesses[0]=(float)(x*x);
        		fitnesses[1]=(float)((x-2)*(x-2));
        		break;
            case PROB_F2:
            	if(numDecisionVars!=1) throw new RuntimeException("F2 needs exactly 1 decision variable (gene).");
        		x = genome[0];
        		fitnesses[0]=(float)( x<=1? -x: (x<=3? x-2:(x<=4? 4-x: x-4)));
        		fitnesses[1]=(float)((x-5)*(x-5));
        		break;
            case PROB_F3:
            	if(numDecisionVars!=2) throw new RuntimeException("F3 needs exactly 2 decision variable (gene).");
        		double x1 = genome[0];
        		double x2 = genome[1];
        		fitnesses[0]=(float)((x1-2)*(x1-2)+(x2-1)*(x2-1)+2);
        		fitnesses[1]=(float)(9*x1-(x2-1)*(x2-1));
        		break;
        	case PROB_FON:
            	if(numDecisionVars!=3) throw new RuntimeException("FON needs exactly 3 decision variables (genes).");
            	double sum1 = 0, sum2=0;
        		for(int i = 1; i< numDecisionVars; ++i)
        		{
        			double xi = genome[i];
        			double d = xi-ONE_OVER_SQRT_3;
        			double s = xi+ONE_OVER_SQRT_3;
        			sum1+=d*d;
        			sum2+=s*s;
        		}
        		fitnesses[0] = (float)Math.exp(-sum1);
        		fitnesses[1] = (float)Math.exp(-sum2);
            	break;
            case PROB_POL:
            	if(numDecisionVars!=2) throw new RuntimeException("POL needs exactly 2 decision variables (genes).");
            	x1= genome[0];
            	x2 = genome[1];
            	double b1 = 0.5*Math.sin(x1) - 2*Math.cos(x1) +    Math.sin(x2)- 1.5*Math.cos(x2);
            	double b2 = 1.5*Math.sin(x1) -   Math.cos(x1) + 2* Math.sin(x2)- 0.5*Math.cos(x2);
            	fitnesses[0] = (float)(1+(A1-b1)*(A1-b1)+(A2-b2)*(A2-b2));
            	fitnesses[1] = (float)((x1+3)*(x1+3)+(x2+1)*(x2+1));
            	break;
            case PROB_QV:
            	sum=0;
            	for(int i=0;i<numDecisionVars;i++)
            	{
            		double xi=genome[i];
            		sum+=xi*xi-10*Math.cos(TWO_PI*xi)+10;
            	}
            	fitnesses[0] = (float)Math.pow(sum/numDecisionVars, 0.25);
            	sum=0;
            	for(int i=0;i<numDecisionVars;i++)
            	{
            		double xi=genome[i]-1.5;
            		sum+=xi*xi-10*Math.cos(TWO_PI*xi)+10;
            	}
            	fitnesses[1] = (float)Math.pow(sum/numDecisionVars, 0.25);
            	break;
            case PROB_KUR:
        		double nextSquared, thisSquared;
        		thisSquared = genome[0]*genome[0];
        		sum=0;
        		for(int i = 0; i< numDecisionVars-1; ++i)
        		{
        			nextSquared = genome[i+1]*genome[i+1];
        			sum += 1d-Math.exp(-0.2*Math.sqrt(thisSquared + nextSquared));
        			thisSquared = nextSquared;
        		}
        		fitnesses[1] = (float)sum;
        		sum= 0;
        		for(int i = 0; i< numDecisionVars; ++i)
        		{
        			double sin_xi = Math.sin(genome[i]);
        			
        			double t1 = Math.pow(Math.abs(genome[i]), .8);
        			double t2 = 5*sin_xi*sin_xi*sin_xi;
        			
        			sum +=t1+t2+ 3.5828;
        		}
        		fitnesses[0] = (float)sum;
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
