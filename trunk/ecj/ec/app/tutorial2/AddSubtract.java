/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.tutorial2;
import ec.*;
import ec.simple.*;
import ec.vector.*;

public class AddSubtract extends Problem implements SimpleProblemForm
    {
    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int threadnum)
        {
        if (ind.evaluated) return;

        if (!(ind instanceof IntegerVectorIndividual))
            state.output.fatal("Whoa!  It's not a IntegerVectorIndividual!!!",null);
        
        IntegerVectorIndividual ind2 = (IntegerVectorIndividual)ind;
        
        int rawfitness = 0;
        for(int x=0; x<ind2.genome.length; x++)
            if (x % 2 == 0) rawfitness += ind2.genome[x];
            else rawfitness -= ind2.genome[x];
        
        // We finish by taking the ABS of rawfitness.  By the way,
        // in SimpleFitness, fitness values must be set up so that 0 is <= the worst
        // fitness and +infinity is >= the ideal possible fitness.  Our raw fitness
        // value here satisfies this. 
        if (rawfitness < 0) rawfitness = -rawfitness;
        if (!(ind2.fitness instanceof SimpleFitness))
            state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);
        ((SimpleFitness)ind2.fitness).setFitness(state,
                                                 // what the heck, lets normalize the fitness for genome length
                                                 // so it's within float range
                                                 (float)(((double)rawfitness)/ind2.genome.length),
                                                 ///... is the individual ideal?  Indicate here...
                                                 false);
        ind2.evaluated = true;
        }
    
    public void describe(final Individual ind, 
                         final EvolutionState state, 
                         final int threadnum, final int log,
                         final int verbosity)
        {
        }
    }
