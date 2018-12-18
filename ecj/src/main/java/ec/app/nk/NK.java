package ec.app.nk; 

import ec.*; 
import ec.simple.*; 
import ec.vector.*; 
import ec.util.*;
import java.util.*;

/**
   NK implmements the NK-landscape developed by Stuart Kauffman (in the book <i>The Origins of
   Order: Self-Organization and Selection in Evolution</a>).  In the NK model, the fitness 
   contribution of each allele depends on how that allele interacts with K other alleles.  Based on 
   this interaction, each gene contributes a random number between 0 and 1.  The individual's 
   fitness is the average of these N random numbers.  

   <p><b>Parameters</b><br>
   <table>
   <tr><td valign=top><i>base</i>.<tt>k</tt><br>
   <font size=-1>int >= 0 && < 31</td>
   <td valign=top>(number of interacting alleles)</td></tr>
   <tr><td valign=top><i>base</i>.<tt>adjacent</tt><br> 
   <font size=-1>boolean</font></td>
   <td valign=top>(should interacting alleles be adjacent to the given allele)</td></tr> 
   </table>
 
   @author Keith Sullivan
   @version 1.0
*/


public class NK extends Problem implements SimpleProblemForm 
    { 
    public static final String P_N = "n"; 
    public static final String P_K = "k"; 
    public static final String P_ADJACENT="adjacent"; 
        
    int k; 
    boolean adjacentNeighborhoods;
    HashMap oldValues; 
        
    public void setup(final EvolutionState state, final Parameter base) 
        {
        super.setup(state, base); 
                
        k = state.parameters.getInt(base.push(P_K), null, 1); 
        if ((k < 0) || (k > 31))
            state.output.fatal("Value of k must be between 0 and 31", base.push(P_K)); 
                
        adjacentNeighborhoods = state.parameters.getBoolean(base.push(P_ADJACENT), null, true); 
        oldValues = new HashMap(); 
        }
        
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
        {
        BitVectorIndividual ind2 = (BitVectorIndividual) ind; 
        double fitness =0; 
        int n = ind2.genome.length; 
                
        for (int i=0; i < n; i++) 
            { 
            boolean tmpInd[] = new boolean[k+1]; 
            tmpInd[0] = ind2.genome[i];
                        
            double val=0;
            if (adjacentNeighborhoods) 
                { 
                int offset = n - k/2; 
                for (int j=0; j < k; j++) 
                    {
                    tmpInd[j+1] = ind2.genome[(j+i + offset) % n]; 
                    }
                }
            else 
                { 
                int j;
                for (int l=0; l < k; l++) 
                    { 
                    while ((j = state.random[0].nextInt(k)) == i);
                    tmpInd[l+1] = ind2.genome[j]; 
                    }
                }
                        
            if (oldValues.containsKey(tmpInd))
                val = ((Double)oldValues.get(tmpInd)).doubleValue(); 
            else 
                { 
                double tmp=0; 
                for (int j=0; j < tmpInd.length; j++)  
                    if (tmpInd[j]) tmp += 1 << j; 
                val = tmp /  Integer.MAX_VALUE; 
                                                                
                oldValues.put(tmpInd, new Double(val)); 
                }
                        
            fitness += val; 
            }
                                
        fitness /= n;
        ((SimpleFitness)(ind2.fitness)).setFitness( state, fitness, false);
        ind2.evaluated = true; 
        }
    }
