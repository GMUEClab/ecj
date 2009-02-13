/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.multiobjective.spea2;

import ec.*;
import ec.util.MersenneTwisterFast;
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
 * The evaluator is also responsible for calculating the SPEA2Fitness
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
 *  <p>Note that the field SPEA2kthNNDistance in SPEA2MultiObjectiveFitness is supposed to be
 *  "D(i)," the DENSITY = 1/(kth distance+2).  So the fields'name is confusing. In here I 
 *  go with density.
 *  
 * @author Robert Hubley (based on Evaluator.java by Sean Luke), some refactoring by Gabriel Balan
 * @version 1.1
 */
public class SPEA2Evaluator extends SimpleEvaluator
    {
    /** A simple evaluator that doesn't do any coevolutionary
        evaluation.  Basically it applies evaluation pipelines,
        one per thread, to various subchunks of a new population. */
    public void evaluatePopulation(final EvolutionState state)
        {
        for(int i =0; i < state.population.subpops.length; i++)
            if (!(state.population.subpops[i] instanceof SPEA2Subpopulation))
                state.output.fatal("SPEA2Evaluator must only be used with a SPEA2Subpopulation!", null);
        super.evaluatePopulation(state);
        computeAuxiliaryData(state);
        }

    public void computeAuxiliaryData(EvolutionState state)
        {
        
        // Ok...now all individuals have been evaluated
        // so we can go ahead and calculate the raw and
        // density values of the SPEA2 fitness function

        // Each subpopulation
        for(int x = 0;x<state.population.subpops.length;x++)
            {
            Individual[] inds = state.population.subpops[x].individuals;
            computeAuxiliaryData(state, inds);
            }
        }
        
    public void computeAuxiliaryData(EvolutionState state, Individual[] inds)
        {
        double[][] distances = new double[inds.length][inds.length];
        for(int y=0;y<inds.length;y++)
            for(int z=0;z<inds.length;z++)
                {// Set distances
                if ( y == z ) {
                    distances[y][z] = 0;
                    }else if ( z > y ) {//nice, don't double the work.
                    distances[y][z] =
                        ((SPEA2MultiObjectiveFitness)inds[y].fitness).
                        calcDistance( (SPEA2MultiObjectiveFitness)inds[z].fitness );
                    distances[z][y] = distances[y][z];
                    }
                }
                
        // For each individual calculate the strength
        for(int y=0;y<inds.length;y++)
            {
            // Calculate the node strengths
            int myStrength = 0;
            for(int z=0;z<inds.length;z++)
                if (inds[y].fitness.betterThan(inds[z].fitness)) 
                    myStrength++;
            ((SPEA2MultiObjectiveFitness)inds[y].fitness).SPEA2Strength = myStrength;
            } //For each individual y calculate the strength
                
        // calculate k value
        int kTH = (int)Math.sqrt(inds.length)-1;//-1 cause the paper counts from 1.
        
        // For each individual calculate the Raw fitness and kth-distance
        for(int y=0;y<inds.length;y++)
            {
            double rawFitness = 0;
            for(int z=0;z<inds.length;z++)
                {
                        
                // Raw fitness 
                if ( inds[z].fitness.betterThan(inds[y].fitness) )
                    {
                    rawFitness += ((SPEA2MultiObjectiveFitness)inds[z].fitness).SPEA2Strength;
                    }
                } // For each individual z calculate RAW fitness distances
            // Set SPEA2 raw fitness value for each individual
                                    
            SPEA2MultiObjectiveFitness indYFitness = ((SPEA2MultiObjectiveFitness)inds[y].fitness);
            indYFitness.SPEA2RawFitness = rawFitness;
                        
            // Density component
                        
            // calc k-th nearest neighbor distance
            double kthDistance = orderStatistics(distances[y], kTH, state.random[0]);
            double density = 1d/(2+kthDistance);
                        
            // Set SPEA2 k-th NN distance value for each individual
            indYFitness.SPEA2kthNNDistance = density;
            // Set SPEA2 fitness value for each individual
            indYFitness.SPEA2Fitness =indYFitness.SPEA2RawFitness + indYFitness.SPEA2kthNNDistance;
            } // For each individual y
        }
    
    double orderStatistics(double[] array, int kth,MersenneTwisterFast rng)
        {
        return randomizedSelect(array, 0, array.length-1, kth, rng);
        }
    /* OrderStatistics [Cormen, p187]:
     * find the ith smallest element of the array between indices p and r*/
    double randomizedSelect(double[] array, int p, int r, int i,MersenneTwisterFast rng)
        {
        if(p==r) return array[p];
        int q = randomizedPartition(array, p, r, rng);
        int k = q-p+1;
        if(i<=k)
            return randomizedSelect(array, p, q, i,rng);
        else
            return randomizedSelect(array, q+1, r, i-k,rng);
        }
    /* [Cormen, p162]*/
    int randomizedPartition(double[] array, int p, int r, MersenneTwisterFast rng)
        {
        int i = rng.nextInt(r-p+1)+p;
        //exchange array[p]<->array[i]
        double tmp = array[i];
        array[i]=array[p];
        array[p]=tmp;
        return partition(array,p,r);
        }
    /* [cormen p 154]*/
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
