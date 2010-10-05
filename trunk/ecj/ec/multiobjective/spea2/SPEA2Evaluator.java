/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective.spea2;

import ec.*;
import ec.util.MersenneTwisterFast;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*; 
/* 
 * SPEA2Evaluator.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *    (based on Evaluator.java by Sean Luke)
 */
/**
 * 
 * The SPEA2Evaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its
 * own problem space.  One Problem instance is cloned from p_problem for
 * each evaluating thread.
 *
 * The evaluator is also responsible for calculating the customFitnessMetric
 * function.  This function depends on the entire population and so
 * cannot be calculated in the Problem class.
 *
 * 
 * <p>This is actually a modified version of Robert Hubley's SPEA2Evaluator,
 * but this time following Zitzler2001 to the letter. 
 * 
 * Differences: 
 * <nl>
 * <li> kth = sqrt(popsize) -1 instead of  kth = sqrt(popsize-1); "-1" because indices start from 0.
 * <li> density = 1/(kth distance+2) instead of some hypersphere volume formula.
 * <li> In order to find the k'th element, this uses the order statistics algorithm (Cormen p187, O(n) expected time)
 * instead of Hubley's O(n^2) algrithm.
 * </nl>
 * 
 *  <p>Note that the field kthNNDistance in SPEA2MultiObjectiveFitness is supposed to be
 *  "D(i)," the DENSITY = 1/(kth distance+2).  So the fields'name is confusing. In here I 
 *  go with density.
 *  
 * @author Robert Hubley (based on Evaluator.java by Sean Luke), some refactoring by Gabriel Balan
 * @version 1.1
 */
public class SPEA2Evaluator extends SimpleEvaluator
    {
    public void evaluatePopulation(final EvolutionState state)
        {
        super.evaluatePopulation(state);
		
		// build SPEA2 fitness values
        for(int x = 0;x<state.population.subpops.length;x++)
            {
            Individual[] inds = state.population.subpops[x].individuals;
            computeAuxiliaryData(state, inds);
            }
        }

	/** Returns a matrix of sum squared distances from each individual to each other individual. */
	public double[][] calculateDistances(EvolutionState state, Individual[] inds)
		{
		double[][] distances = new double[inds.length][inds.length];
        for(int y=0;y<inds.length;y++)
            {
            distances[y][y] = 0;
            for(int z=y+1;z<inds.length;z++)
                {
                distances[z][y] = distances[y][z] =
                    ((SPEA2MultiObjectiveFitness)inds[y].fitness).
                    sumSquaredObjectiveDistance( (SPEA2MultiObjectiveFitness)inds[z].fitness );
                }
            }
		return distances;
		}

    public void computeAuxiliaryData(EvolutionState state, Individual[] inds)
        {
        double[][] distances = calculateDistances(state, inds);
		        
        // For each individual calculate the strength
        for(int y=0;y<inds.length;y++)
            {
            // Calculate the node strengths
            int myStrength = 0;
            for(int z=0;z<inds.length;z++)
                if (((SPEA2MultiObjectiveFitness)inds[y].fitness).paretoDominates((MultiObjectiveFitness)inds[z].fitness)) 
                    myStrength++;
            ((SPEA2MultiObjectiveFitness)inds[y].fitness).strength = myStrength;
            } //For each individual y calculate the strength
                
        // calculate k value
        int kTH = (int) Math.sqrt(inds.length);  // note that the first element is k=1, not k=0 
        
        // For each individual calculate the Raw fitness and kth-distance
        for(int y=0;y<inds.length;y++)
            {
            double fitness = 0;
            for(int z=0;z<inds.length;z++)
                {
                // Raw fitness 
                if ( ((SPEA2MultiObjectiveFitness)inds[z].fitness).paretoDominates((MultiObjectiveFitness)inds[y].fitness) )
                    {
                    fitness += ((SPEA2MultiObjectiveFitness)inds[z].fitness).strength;
                    }
                } // For each individual z calculate RAW fitness distances
            // Set SPEA2 raw fitness value for each individual
                                    
            SPEA2MultiObjectiveFitness indYFitness = ((SPEA2MultiObjectiveFitness)inds[y].fitness);
                        
            // Density component
                        
            // calc k-th nearest neighbor distance.
			// distances are squared, so we need to take the square root.
            double kthDistance = Math.sqrt(orderStatistics(distances[y], kTH, state.random[0]));
                        
            // Set SPEA2 k-th NN distance value for each individual
            indYFitness.kthNNDistance = 1.0 / ( 2 + kthDistance);
                        
            // Set SPEA2 fitness value for each individual
            indYFitness.fitness = fitness + indYFitness.kthNNDistance;
            }
        }
    
        
	/** Returns the kth smallest element in the array.  Note that here k=1 means the smallest element in the array (not k=0).
		Uses a randomized sorting technique, hence the need for the random number generator. */
    double orderStatistics(double[] array, int kth, MersenneTwisterFast rng)
        {
        return randomizedSelect(array, 0, array.length-1, kth, rng);
        }
                
                
    /* OrderStatistics [Cormen, p187]:
     * find the ith smallest element of the array between indices p and r */
    double randomizedSelect(double[] array, int p, int r, int i, MersenneTwisterFast rng)
        {
        if(p==r) return array[p];
        int q = randomizedPartition(array, p, r, rng);
        int k = q-p+1;
        if(i<=k)
            return randomizedSelect(array, p, q, i,rng);
        else
            return randomizedSelect(array, q+1, r, i-k,rng);
        }
                
                
    /* [Cormen, p162] */
    int randomizedPartition(double[] array, int p, int r, MersenneTwisterFast rng)
        {
        int i = rng.nextInt(r-p+1)+p;
        //exchange array[p]<->array[i]
        double tmp = array[i];
        array[i]=array[p];
        array[p]=tmp;
        return partition(array,p,r);
        }
                
                
    /* [cormen p 154] */
    int partition(double[] array, int p, int r)
        {
        double x = array[p];
        int i=p-1;
        int j=r+1;
        while(true)
            {
            do{j--;}
            while(array[j]>x);
            do{i++;}
            while(array[i]<x);
            if(i<j)
                {
                //exchange array[i]<->array[j]
                double tmp = array[i];
                array[i]=array[j];
                array[j]=tmp;
                }
            else
                return j;
            }
        }
    }
