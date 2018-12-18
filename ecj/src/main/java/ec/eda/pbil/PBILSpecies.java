/*
  Copyright 2018 by Sunil Kumar Rajendran
  With modifications by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.pbil;
import ec.*;
import ec.vector.*;
import ec.util.*;
import java.util.*;
import java.util.Collections;


/* 
 * PBILSpecies.java
 * 
 * Created: Wed Jan 10 16:30:00 EDT 2018
 * By: Sunil Kumar Rajendran
 */

/**
 * PBILSpecies is an IntegerVectorSpecies which implements a faithful version of the
 * PBIL algorithm.  The class has two basic methods.  The newIndividual(...)
 * method generates a new random individual underneath the current PBIL marginal
 * distribution.  The updateDistribution(...) method revises the marginal 
 * distribution to reflect the fitness results of the population.
 * 
 * <p>PBILSpecies must be used in combination with PBILBreeder, which will
 * call it at appropriate times to revise the distribution and to generate a new
 * subpopulation of individuals.  Since the initial population is built based on
 * the marginal distributions, SimpleInitializer is used to generate the initial 
 * population. 
 *
 * <p>PBILSpecies <b>needs the population size and also truncation size</b>. The
 * truncation size which is the 'b' parameter, denotes how many fittest 
 * individuals to pick out from the generated population of individuals. 
 * It also needs the genome size and the minimum and maximum values of the genes. 
 * The size of the range of min and max gene values should be specified for each 
 * gene in the parameters file. 
 *
 * <p>PBILSpecies also uses the learning rate 'alpha' based on which it decides 
 * the amount of old distribution to be retained and how much of the new 
 * distribution to be added. 'alpha' and 'b' values are printed out when running
 * so the user may see what values it used for that given run.  
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>alpha</tt><br>
 <font size=-1>Floating-point value ranging from 0 to 1.0 </font></td>
 <td valign=top>(the learning rate parameter)<br> 
 If not provided, this defaults to 0.05.
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>popsize</tt><br>
 <font size=-1>Integer > 0 </font></td>
 <td valign=top>(pop.subpop.0.size population size)<br>
 This parameter must be provided.
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>b</tt><br>
 <font size=-1>Integer ranging from 1 to popsize </font></td>
 <td valign=top>(b truncated population size)<br>
 This parameter must be provided.
 </td></tr>

 <tr><td valign=top><i>base</i>.<tt>n</tt><br>
 <font size=-1>Integer > 1</td>
 <td valign=top>(n genome size)<br>
 This parameter must be provided. 
 </td></tr>

 </table>


 <p><b>Default Base</b><br>
 eda.pbil.species


 * @author Sunil Kumar Rajendran
 * @version 1.0 
 */

public class PBILSpecies extends IntegerVectorSpecies
    {
    public static final String P_PBIL_SPECIES = "pbil.species";
    
    public static final String P_ALPHA = "alpha";
    public static final String P_B = "b";
   
    public double alpha;
    public int b;
    
    private List<double[]> distributions;
    
    public double[] getMarginalDistribution(final int gene)
        {
        assert(gene >= 0);
        assert(gene < distributions.size());
        return Arrays.copyOf(distributions.get(gene), distributions.get(gene).length);
        }
   
    public void setup(final EvolutionState state, final Parameter base)
        {
        assert(state != null);
        assert(base != null);
        super.setup(state, base);
        Parameter def = defaultBase();
        Parameter subpopDefaultBase =  ECDefaults.base().push(Subpopulation.P_SUBPOPULATION);
                
        //display minGene and maxGene values for each gene
        for(int i =0;i<genomeSize;i++)
            {
            state.output.message("minGene " + i + " = " + minGene[i]);
            state.output.message("maxGene " + i + " = " + maxGene[i]);
            }
        
        alpha = state.parameters.getDouble(base.push(P_ALPHA), subpopDefaultBase);
        if ((alpha < 0) | (alpha > 1))
            state.output.fatal(String.format("%s: the %s parameter is %f, but must be a valid number in the range 0 to 1", this.getClass().getSimpleName(), base.push(P_ALPHA), alpha), base.push(P_ALPHA), def.push(P_ALPHA));
        
        b = state.parameters.getInt(base.push(P_B), def.push(P_B), 1);
        if (b < 1)
            state.output.fatal(String.format("%s: the %s parameter must be a positive integer.", this.getClass().getSimpleName(), base.push(P_B)), base.push(P_B), def.push(P_B));

        distributions = new ArrayList<double[]>();  
        for(int i=0;i<genomeSize;i++)
            {
            double[] marginalDist=new double[(int)maxGene[i]-(int)minGene[i]+1];
            for(int j = 0; j <maxGene[i]-minGene[i]+1; j++)
                {
                marginalDist[j] = 1.0d / (maxGene[i]-minGene[i]+1);
                }
            distributions.add(marginalDist);
            }
     
        state.output.message("alpha: " + alpha);
        state.output.message("b:     " + b);
        assert(distributions.size() == genomeSize);
        }



    public Object clone()
        {
        // clone the distribution and other variables here
        PBILSpecies myobj = (PBILSpecies) (super.clone());
            
        return myobj;
        } 


    
    public Individual newIndividual(final EvolutionState state, int thread)
        {
        Individual newind = super.newIndividual(state, thread);
        MersenneTwisterFast random = state.random[thread];
        double rand;

        if (!(newind instanceof IntegerVectorIndividual))  
            state.output.fatal("To use PBILSpecies, the species must be initialized with a IntegerVectorIndividual.  But it contains a " + newind);
        
        IntegerVectorIndividual ivind = (IntegerVectorIndividual)(newind);
        List<double[]> temp=new ArrayList<double[]>();

        for(int i=0;i<genomeSize;i++)
            {
            temp.add(distributions.get(i));
            }

        //for every gene value slot in the individual, checks the most probable 
        //gene value using the generated random number
        for(int i=0;i<genomeSize;i++)
            { 
            rand = random.nextDouble();
            boolean q = true;
            double cB = 0;
            for(int j=0;j<maxGene[i]-minGene[i]+1;j++)
                { 
                if(q)
                    { 
                    cB = temp.get(i)[j];
                    }
                if(rand<cB)
                    {
                    ivind.genome[i] = j + (int)minGene[i];
                    break;
                    }
                else if(j<maxGene[i]-minGene[i])
                    {
                    cB = cB + temp.get(i)[j+1];
                    q = false; 
                    }
                }
            }

        return ivind ;
        }

    

    /** Revises the PBIL distribution to reflect the current fitness results in the provided subpopulation. */
    public void updateDistribution(final EvolutionState state, final Subpopulation subpop)
        {
        
        Collections.sort(subpop.individuals);
        List<double[]> Nj=new ArrayList<double[]>();
        int arz[][]=new int[b][genomeSize];
        
        //truncation selection of 'b' fittest individuals
        for(int i=0;i<b;i++)
            {
            IntegerVectorIndividual ivind = (IntegerVectorIndividual)(subpop.individuals.get(i));
            arz[i]=ivind.genome;
            }
        
        //creates a new marginal distribution for the new population arz
        for(int i=0;i<genomeSize;i++)
            {
            double Nj_temp[]=new double[(int)(maxGene[i]-minGene[i]+1)];
            for(int j = 0; j<maxGene[i]-minGene[i]+1; j++)
                {
                Nj_temp[j]=marginalDist(i,j,arz);
                }
            Nj.add(Nj_temp);
            }
        
        //ignore part of current distribution and roll in part of new 
        //probabilities
        for(int i=0;i<genomeSize;i++)
            {
            double[] tempDist = distributions.get(i);
            double[] tempNewDist = Nj.get(i);
                
            for(int j=0;j<maxGene[i]-minGene[i]+1;j++)
                {
                tempDist[j] *= (1-alpha);
                tempNewDist[j] *= (alpha);
                tempDist[j] += tempNewDist[j];
                }
                
            distributions.set(i,tempDist);
            }
        }
    
    private double marginalDist(int i,int k, int arz[][])
        {
        double count=0;
        
        //Given the current gene value of a gene in the new population, 
        //returns the number of individuals that possess that gene value.
        for(int j=0;j<b;j++)
            {
            if(arz[j][i]==(k+(int)minGene[i]))
                {
                count++;
                }
            }
        return count/b;
        }
    }

