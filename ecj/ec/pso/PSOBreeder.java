/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.pso;

import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
/**
 * PSOBreeder.java
 *
 
 <p>The PSOBreeder performs the calculations to determine new particle locations
 and performs the bookkeeping to keep track of personal, neighborhood, and global
 best solutions.
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>debug-info</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether the system should display information useful for debugging purposes)<br>
 </td></tr>

 </table>

 * @author Joey Harrison, Ankur Desai
 * @version 1.0 
 */
public class PSOBreeder extends Breeder
    {
    public void setup(EvolutionState state, Parameter base) 
        {
        // intentionally empty
        }
                
    public Population breedPopulation(EvolutionState state)
        {
        PSOSubpopulation subpop = (PSOSubpopulation) state.population.subpops[0];
                
        // update bests
        assignPersonalBests(subpop);
        assignNeighborhoodBests(subpop);
        assignGlobalBest(subpop);

        // make a temporary copy of locations so we can modify the current location on the fly
        DoubleVectorIndividual[] tempClone = new DoubleVectorIndividual[subpop.individuals.length];
        System.arraycopy(subpop.individuals, 0, tempClone, 0, subpop.individuals.length);
                
        // update particles             
        for (int i = 0; i < subpop.individuals.length; i++)
            {
            DoubleVectorIndividual ind = (DoubleVectorIndividual)subpop.individuals[i];
            DoubleVectorIndividual prevInd = (DoubleVectorIndividual)subpop.previousIndividuals[i];
            // the individual's personal best
            DoubleVectorIndividual pBest = (DoubleVectorIndividual)subpop.personalBests[i];
            // the individual's neighborhood best
            DoubleVectorIndividual nBest = (DoubleVectorIndividual)subpop.neighborhoodBests[i];
            // the individuals's global best
            DoubleVectorIndividual gBest = (DoubleVectorIndividual)subpop.globalBest;
                        
            // calculate update for each dimension in the genome
            for (int j = 0; j < ind.genomeLength(); j++)
                { 
                double velocity = ind.genome[j] - prevInd.genome[j];
                double pDelta = pBest.genome[j] - ind.genome[j];                        // difference to personal best
                double nDelta = nBest.genome[j] - ind.genome[j];                        // difference to neighborhood best
                double gDelta = gBest.genome[j] - ind.genome[j];                        // difference to global best
                double pWeight = state.random[0].nextDouble();                          // weight for personal best
                double nWeight = state.random[0].nextDouble();                          // weight for neighborhood best
                double gWeight = state.random[0].nextDouble();                          // weight for global best
                double newDelta = (velocity + pWeight*pDelta + nWeight*nDelta + gWeight*gDelta) / (1+pWeight+nWeight+gWeight);
                        
                // update this individual's genome for this dimension
                ind.genome[j] += newDelta * subpop.velocityMultiplier;     // it's obvious if you think about it
                }
            
            if (subpop.clampRange)
                ind.clamp();                 
            }               
                
        // update previous locations
        subpop.previousIndividuals = tempClone;
                                
        return state.population;
        }

    public void assignPersonalBests(PSOSubpopulation subpop)
        {
        for (int i = 0; i < subpop.personalBests.length; i++)                   
            if ((subpop.personalBests[i] == null) || subpop.individuals[i].fitness.betterThan(subpop.personalBests[i].fitness))
                subpop.personalBests[i] = (DoubleVectorIndividual)subpop.individuals[i].clone();
        }

    public void assignNeighborhoodBests(PSOSubpopulation subpop)
        {
        for (int j = 0; j < subpop.individuals.length; j++)
            {
            DoubleVectorIndividual hoodBest = subpop.neighborhoodBests[j];
            int start = (j - subpop.neighborhoodSize / 2);
            if (start < 0)
                start += subpop.individuals.length;
                        
            for (int i = 0; i < subpop.neighborhoodSize; i++)
                {
                DoubleVectorIndividual ind = (DoubleVectorIndividual)subpop.individuals[(start + i) % subpop.individuals.length];
                if((hoodBest == null) || ind.fitness.betterThan(hoodBest.fitness))
                    hoodBest = ind;
                }
                        
            if (hoodBest != subpop.neighborhoodBests[j])
                subpop.neighborhoodBests[j] = (DoubleVectorIndividual)hoodBest.clone();
            }
        }
        
    public void assignGlobalBest(PSOSubpopulation subpop)
        {
        DoubleVectorIndividual globalBest = subpop.globalBest;
        for (int i = 0; i < subpop.individuals.length; i++)
            {
            DoubleVectorIndividual ind = (DoubleVectorIndividual)subpop.individuals[i];
            if ((globalBest == null) || ind.fitness.betterThan(globalBest.fitness))
                globalBest = ind;
            }
        if (globalBest != subpop.globalBest)
            subpop.globalBest = (DoubleVectorIndividual)globalBest.clone();
        }
    }
