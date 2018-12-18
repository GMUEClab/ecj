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
   <li>SCH: (Schaffer), (a.k.a. F1 in Srinivas & Deb); requires exactly 1 decision variables (genes)
   <li>F2: (Schaffer), (Srinivas & Deb),  (Coello Coello & Cortes); requires exactly 1 decision variables (genes)
   <li>unconstrained F3: Schaffer, Srinivas & Deb  (Chankong & Haimes); requires exactly 2 decision variables (genes)
   <li>QV: Quagliarella & Vicini
   <li>FON: Fonseca & Fleming (1995); requires exactly 3 decision variables (genes)
   <li>POL: Poloni; requires exactly 2 decision variables (genes)
   <li>KUR: Kursawe from the Errata of Zitzler's TIK-Report 103: "SPEA2: Improving the Strength Pareto Evolutionary Algorithm"
   (note that many different versions are described in the literature).
   </ul>   

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>type</tt><br>
   <font size=-1>String, one of: zdt1, zdt2, zdt3, zdt4, zdt6, sphere, sch, fon, qv, pol, kur, f1, f2, unconstrained-f3</font></td>
   <td valign=top>The multi-objective optimization problem to test against. </td></tr>
   </table>
   @author Gabriel Catalin Balan 
*/
 
public class MooSuite extends Problem implements SimpleProblemForm
    {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
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

    //Some of the following problems requires an exact number of decision variables (genes). This is mentioned in comment preceding the problem.

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
            "  " + P_F2 + "\n\n" +
            "Are you by any chance running moosuite.params?  Instead\n" + 
            "You should be running one of the params files for these\n" + 
            "specific problems, such as zdt2.params.\n",
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

        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

        double f, g, h, sum;
                
        switch(problemType)
            {
            case PROB_ZDT1:
                f = genome[0];
                objectives[0] = f;
                sum = 0;
                for(int i = 1; i< numDecisionVars; ++i)
                    sum += genome[i];
                g = 1d+9d*sum/(numDecisionVars - 1.0);
                h = 1d-Math.sqrt(f/g);
                objectives[1] = (g*h);
                break;
                
            case PROB_ZDT2:
                f = genome[0];
                objectives[0] = f;
                sum = 0;
                for(int i = 1; i< numDecisionVars; i++)
                    sum += genome[i];
                g = 1.0+9.0*sum/(numDecisionVars - 1.0);
                h = 1.0-(f/g)*(f/g);
                objectives[1] = (g*h);
                break;
                        
            case PROB_ZDT3:     
                f = genome[0];
                objectives[0] = f;
                sum = 0;
                for(int i = 1; i< numDecisionVars; i++)
                    sum += genome[i];
                g = 1.0+9.0*sum/(numDecisionVars - 1.0);
                double foverg = f/g;
                h = 1.0-Math.sqrt(foverg) - foverg * Math.sin(TEN_PI * f);
                objectives[1] = (g*h);
                break;
            case PROB_ZDT4:
                f = genome[0];
                objectives[0] = f;
                sum = 0;
                for(int i = 1; i< numDecisionVars; ++i)
                    sum += genome[i]*genome[i]- 10*Math.cos(FOUR_PI * genome[i]);
                                
                g = 1+10*(numDecisionVars - 1.0)+sum;
                h = 1-Math.sqrt(f/g);
                objectives[1] = (g*h);
                break;                
            case PROB_ZDT6:
                f = 1 - (Math.exp(-4 * genome[0]) * Math.pow(Math.sin(SIX_PI * genome[0]), 6));
                objectives[0] = f;
                sum = 0;
                for (int i = 1; i < numDecisionVars; ++i)
                    sum += genome[i];
                g = 1d + 9 * Math.pow(sum / (numDecisionVars - 1.0), 0.25);
                h = 1d - Math.pow(f / g, 2);
                objectives[1] = (g * h);
                break; 
            case PROB_SPHERE:
                int numObjectives = objectives.length;
                for(int j=0; j<numObjectives; ++j)
                    {
                    sum = (genome[j]-1)*(genome[j]-1);
                    for(int i=0; i<numDecisionVars; ++i)
                        if (i!=j)
                            sum += genome[i]*genome[i];
                    objectives[j] = sum;
                    }
                break;
            case PROB_SCH:
                if(numDecisionVars!=1) throw new RuntimeException("SCH needs exactly 1 decision variable (gene).");
                double x = genome[0];
                objectives[0]=(x*x);
                objectives[1]=((x-2)*(x-2));
                break;
            case PROB_F2:
                if(numDecisionVars!=1) throw new RuntimeException("F2 needs exactly 1 decision variable (gene).");
                x = genome[0];
                objectives[0]=( x<=1? -x: (x<=3? x-2:(x<=4? 4-x: x-4)));
                objectives[1]=((x-5)*(x-5));
                break;
            case PROB_F3:
                if(numDecisionVars!=2) throw new RuntimeException("F3 needs exactly 2 decision variable (gene).");
                double x1 = genome[0];
                double x2 = genome[1];
                objectives[0]=((x1-2)*(x1-2)+(x2-1)*(x2-1)+2);
                objectives[1]=(9*x1-(x2-1)*(x2-1));
                break;
            case PROB_FON:
                if(numDecisionVars!=3) throw new RuntimeException("FON needs exactly 3 decision variables (genes).");
                double sum1 = 0, sum2=0;
                for(int i = 0; i< numDecisionVars; i++)
                    {
                    double xi = genome[i];
                    double d = xi-ONE_OVER_SQRT_3;
                    double s = xi+ONE_OVER_SQRT_3;
                    sum1+=d*d;
                    sum2+=s*s;
                    }
                objectives[0] = 1 - Math.exp(-sum1);
                objectives[1] = 1 - Math.exp(-sum2);
                break;
            case PROB_POL:
                if(numDecisionVars!=2) throw new RuntimeException("POL needs exactly 2 decision variables (genes).");
                x1= genome[0];
                x2 = genome[1];
                double b1 = 0.5*Math.sin(x1) - 2*Math.cos(x1) +    Math.sin(x2)- 1.5*Math.cos(x2);
                double b2 = 1.5*Math.sin(x1) -   Math.cos(x1) + 2* Math.sin(x2)- 0.5*Math.cos(x2);
                objectives[0] = (1+(A1-b1)*(A1-b1)+(A2-b2)*(A2-b2));
                objectives[1] = ((x1+3)*(x1+3)+(x2+1)*(x2+1));
                break;
            case PROB_QV:
                sum=0;
                for(int i=0;i<numDecisionVars;i++)
                    {
                    double xi=genome[i];
                    sum+=xi*xi-10*Math.cos(TWO_PI*xi)+10;
                    }
                objectives[0] = Math.pow(sum/numDecisionVars, 0.25);
                sum=0;
                for(int i=0;i<numDecisionVars;i++)
                    {
                    double xi=genome[i]-1.5;
                    sum+=xi*xi-10*Math.cos(TWO_PI*xi)+10;
                    }
                objectives[1] = Math.pow(sum/numDecisionVars, 0.25);
                break;
            case PROB_KUR:
                // The version of the Kursawe function we use here is taken from the erata of Zitzler et al., "SPEA2: Improving the Strength Pareto Evolutionary Algorithm"
                // Note that many different versions are described in the literature!
                sum= 0;
                for(int i = 0; i < numDecisionVars; ++i)
                    {
                    double t1 = Math.pow(Math.abs(genome[i]), .8);
                    double t2 = 5 * Math.pow(Math.sin(genome[i]), 3);
                    sum += t1 + t2 + 3.5828;
                    }
                objectives[0] = sum;
                double nextSquared, thisSquared;
                thisSquared = genome[0]*genome[0];
                sum=0;
                for(int i = 0; i < numDecisionVars-1; ++i)
                    {
                    nextSquared = genome[i+1]*genome[i+1];
                    sum += 1 - Math.exp(-0.2 * Math.sqrt(thisSquared + nextSquared));
                    thisSquared = nextSquared;
                    }
                objectives[1] = sum;
                break;

            default:
                state.output.fatal( this.getClass().getSimpleName() + " has an invalid problem -- how on earth did that happen?" );
                break;
            }

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
        }
    }
