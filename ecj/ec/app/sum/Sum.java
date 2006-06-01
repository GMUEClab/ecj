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
                         final int threadnum)
        {
        if (ind.evaluated) return;

        if (!(ind instanceof IntegerVectorIndividual))
            state.output.fatal("Whoa!  It's not an IntegerVectorIndividual!!!",null);
        
        int sum=0;
        IntegerVectorIndividual ind2 = (IntegerVectorIndividual)ind;
        for(int x=0; x<ind2.genome.length; x++)
            sum += ind2.genome[x];
        
        // Get the species for the individual
        IntegerVectorSpecies s = (IntegerVectorSpecies)ind2.species;
        
        // For this example, we assume that this individual is only using
        // one global maxGene, rather than custom maxGene values for each
        // separate gene.  Because we assume this, we know that the 
        // highest fitness is thus the global maxGene times the genome length.
        // but first, let's check to make sure that there are no custom
        // max gene values:
        
        if (s.individualGeneMinMaxUsed())  // uh oh...
            state.output.fatal("Whoa!  Can't use separate max-gene values for each gene in this problem!",null);
        
        // okay, we know we're fine.
        int maximumSum = (int)s.maxGene*ind2.genome.length;
        
        // assume we're using SimpleFitness
        ((SimpleFitness)ind2.fitness).setFitness(state,
                                                 /// ...the fitness...
                                                 (float)(((double)sum)/(maximumSum)),
                                                 ///... our definition of the ideal individual
                                                 sum == maximumSum);
        ind2.evaluated = true;
        }
    
    public void describe(final Individual ind, 
                         final EvolutionState state, 
                         final int threadnum, final int log,
                         final int verbosity) { }
    }
