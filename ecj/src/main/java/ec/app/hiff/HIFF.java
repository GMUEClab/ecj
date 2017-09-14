package ec.app.hiff; 

import ec.*; 
import ec.util.*; 
import ec.vector.*;
import ec.simple.*;

/** 
    HIFF implements the Hierarchical If-And-Only-If problem developed by Watson, Hornby and Pollack.
    See <a href="http://www.cs.brandeis.edu/~richardw/hiff.html">The HIFF Generator</a> for more information
    and papers.

    <p><b>Parameters</b><br>
    <table>
    <tr><td valign=top><i>base</i>.<tt>p</tt><br>
    <font size=-1>int >= 0 </td>
    <td valign=top>(number of blocks at each level)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>k</tt><br>
    <font size=-1>int >= 0 </td>
    <td valign=top>(number of hierarchical levels)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>rc</tt><br>
    <font size=-1>double </td>
    <td valign=top>(ratio of block contributions)</td></tr>
    </table>

    @author Keith Sullivan
    @version 1.0
*/

public class HIFF extends Problem implements SimpleProblemForm 
    {

    public static final String P_K = "k";
    public static final String P_P = "p"; 
    public static final String P_RC = "rc"; 
        
    int K, P, Rc; 
                
    public void setup(EvolutionState state, Parameter base) 
        {
        super.setup(state, base); 
                
        K = state.parameters.getInt(base.push(P_K), null, 0); 
        if (K < 0) 
            state.output.fatal("k must be > 0", base.push(P_K)); 
                
        P = state.parameters.getInt(base.push(P_P), null, 0); 
        if (P < 0) 
            state.output.fatal("p must be > 0", base.push(P_P)); 
                
        Rc = state.parameters.getInt(base.push(P_RC), null, 0); 
        if (Rc < 0) 
            state.output.fatal("rc must be > 0", base.push(P_RC)); 
        }
        
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
        {
        BitVectorIndividual ind2 = (BitVectorIndividual) ind; 
                
        double genes[] = new double[ind2.genome.length];
        for (int i=0; i < genes.length; i++) 
            genes[i] = ((ind2.genome[i]) ? 1 : 0); 
        double fitness = H(genes); 
                
        ((SimpleFitness)(ind2.fitness)).setFitness( state, fitness, false);
        ind2.evaluated = true; 
        }
                
    double H(double genes[]) 
        {
        double bonus=1, F=0; 
        int last = genes.length;
                
        for (int i=0; i < last; i++) 
            F += f(genes[i]) ; 
                
        for (int i=1; i <= P; i++) 
            { 
            last /= K; 
            bonus *= Rc; 
            for (int j=0; j < last; j++) 
                { 
                genes[j] = t(genes, j*K); 
                F += f(genes[j]) * bonus; 
                }
            }
                
        return F;
        }
        
    double t(double transform[], int first) 
        {
        int s=0; 
        for (int i=first+1; i < first+K; i++) 
            { 
            if (transform[first] == transform[i]) 
                s++; 
            }
        if (s == (K-1)) return transform[first]; 
                
        return -1; 
        }
        
    double f(double b) 
        {
        if (b != -1) return 1; 
        return 0;
        }
    }
