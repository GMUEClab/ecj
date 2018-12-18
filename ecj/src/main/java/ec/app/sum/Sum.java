/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.sum;
import ec.vector.*;
import ec.*;
import ec.simple.*;
import ec.util.*;

/* 
 * Sum.java
 * 
 * Created on Sat Jun 16 23:26:38 EDT 2001
 * By Sean Luke
 */

/**
 * Sum is a simple example of the ec.Vector package, implementing the
 * very simple sum problem (fitness = sum over vector).
 * This is a generalization of the common MaxOnes problem
 * (fitness = number of 1's in vector).

 *
 * @author Sean Luke
 * @version 1.0 
 */



public class Sum extends Problem implements SimpleProblemForm
    {
    public static final String P_SUM = "sum";
    
    public Parameter defaultBase()
        {
        return super.defaultBase().push(P_SUM);
        }

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (ind.evaluated) return;

        if (!(ind instanceof IntegerVectorIndividual))
            state.output.fatal("Whoa!  It's not an IntegerVectorIndividual!!!",null);

        IntegerVectorIndividual ind2 = (IntegerVectorIndividual)ind;
        IntegerVectorSpecies s = (IntegerVectorSpecies)ind2.species;
        
        long sum=0;
        long max=0;
        for(int x=0; x<ind2.genome.length; x++)
            {
            sum += ind2.genome[x];
            max += (int)(s.maxGene(x));  // perhaps this neededn't be computed over and over again
            }

        // Now we know that max is the maximum possible value, and sum is the fitness.
        
        // assume we're using SimpleFitness
        ((SimpleFitness)ind2.fitness).setFitness(state,
            /// ...the fitness...
            sum, 
            ///... our definition of the ideal individual
            sum >= max);  // it shouldn't ever be >, but just in case.
                
        ind2.evaluated = true;
        }
    }
