/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.majority;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.simple.*;
import ec.vector.*;

/**
   MajorityGA.java
        
   Implements a GA-style vector rule for the one-dimensional Majority-Ones cellular automaton problem.
   This code is in the spirit of Das, Crutchfield, Mitchel, and Hanson, "Evolving Globally Synchronized Cellular Automata",
   http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.55.7754&rep=rep1&type=pdf
   
   The primary difference is in the trials mechanism, in which we're using 25/25/50 rather than 50/50/0.
   
   If you run java ec.app.majority.MajorityGA, it'll test using the ABK rule instead (0.8342 using 10000 tests, 
   0.82528 if you use 100000 tests, 0.823961 if you use 1000000 tests)
*/




public class MajorityGA extends Problem implements SimpleProblemForm
    {
    CA ca = null;

    // How many trials in our training set
    public static final int NUM_TRIALS = 128;

    // CA description
    public static final int CA_WIDTH = 149;
    public static final int NEIGHBORHOOD = 7;
    
    // How long can I run the CA if it's not converging?
    public static final int STEPS = 200;

    int[][] trials = new int[NUM_TRIALS][CA_WIDTH];
    int[] majorities = new int[NUM_TRIALS];
    

    // kinds of trial types
    static final int MAJORITY_ZERO = 0;
    static final int MAJORITY_ONE = 1;
    static final int RANDOM = -1;
    
    boolean makeTrial(EvolutionState state, int thread, int[] trial, int trialType)
        {
        if (trialType == RANDOM)
            {
            int count = 0;
            for(int i = 0; i < CA_WIDTH; i++)
                {
                trial[i] = state.random[thread].nextInt(2);
                count += trial[i];
                }  
            return (count > CA_WIDTH / 2.0);  // > 74
            }
        else if (trialType == MAJORITY_ONE)
            {
            while(!makeTrial(state, thread, trial, RANDOM));
            return true;
            }               
        else if (trialType == MAJORITY_ZERO) // uniform selection
            {
            while(makeTrial(state, thread, trial, RANDOM));
            return false;
            }
        else
            {
            state.output.fatal("This should never happen");
            return false;
            }
        }
        

    public void generateTrials(EvolutionState state, int thread)
        {
        // the trials strategy here is: 25% ones, 25% zeros, and 50% random choice
        
        //MersenneTwisterFast mtf = state.random[thread];
        
        for(int i = 0; i < NUM_TRIALS / 4; i++)
            {
            majorities[i] = makeTrial(state, thread, trials[i], MAJORITY_ZERO) ? 1 : 0;
            }
        
        for(int i = NUM_TRIALS / 4; i < NUM_TRIALS / 2; i++)
            {
            majorities[i] = makeTrial(state, thread, trials[i], MAJORITY_ONE) ? 1 : 0;
            }
        for(int i = NUM_TRIALS / 2; i < NUM_TRIALS; i++)
            {
            majorities[i] = makeTrial(state, thread, trials[i], RANDOM) ? 1 : 0;
            }
        
        }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);
        generateTrials(state, 0);
        }



    // the purpose of this code is to guarantee that I regenerate trials each generation
    // and make sure that nobody is using them at the moment.

    int lockCount = 0;
    private Object[] lock = new Object[0];
        
    public void prepareToEvaluate(final EvolutionState state, final int threadnum)
        {
        if (threadnum != 0) 
            synchronized(lock) { lockCount++ ; }
        }

    public void finishEvaluating(final EvolutionState state, final int threadnum)
        {
        if (threadnum != 0)
            {
            synchronized(lock) { lockCount--; lock.notifyAll(); }
            }
        else  // I'm thread 0
            {
            synchronized(lock) 
                { 
                while(lockCount > 0)
                    try { lock.wait(); }
                    catch (InterruptedException e) { }
                }
                                
            // at this point I'm all alone!
            generateTrials(state, threadnum);
            }
        }


    public static boolean all(int[] vals, int val)
        {
        for(int i = 0; i < vals.length; i++)
            if (vals[i] != val) return false;
        return true;
        }


    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
        if (ca == null)
            ca = new CA(CA_WIDTH, NEIGHBORHOOD);

        // we always reevaluate         
        //if (!ind.evaluated)  // don't bother reevaluating
            {
            int sum = 0;
            
            boolean[] genome = ((BitVectorIndividual)ind).genome;
            
            // extract the rule
            int[] rule = ca.getRule();
            for(int i = 0; i < 128; i++)
                rule[i] = (genome[i] ? 1 : 0);
            ca.setRule(rule);  // for good measure though it doesn't matter
                        
            for(int i = 0; i < NUM_TRIALS; i++)
                {
                // set up and run the CA
                ca.setVals(trials[i]);
                ca.step(STEPS, true);
                
                // extract the fitness
                if (all(ca.getVals(), majorities[i]))
                    sum ++;
                }
            SimpleFitness f = ((SimpleFitness)ind.fitness);
            f.setFitness(state, sum / (double)NUM_TRIALS, false);
            ind.evaluated = true;
            }
        }
      
    
    
    public static final int NUM_TESTS = 10000;

    double density = 0.0;
    public void describe(
        final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum,
        final int log)
        {
        if (ca == null)
            ca = new CA(CA_WIDTH, NEIGHBORHOOD);

        int[] trial = new int[CA_WIDTH];
                
        boolean[] genome = ((BitVectorIndividual)ind).genome;
                
        // extract the rule
        int[] rule = ca.getRule();
        for(int i = 0; i < 128; i++)
            rule[i] = (genome[i] ? 1 : 0);
        ca.setRule(rule);  // for good measure though it doesn't matter
                
        double sum = 0;
        for(int i = 0; i < NUM_TESTS; i++)
            {
            // set up and run the CA
            int result = makeTrial(state, threadnum, trial, RANDOM) ? 1 : 0;
            ca.setVals(trial);
            ca.step(STEPS, true);
                        
            // extract the fitness
            if (all(ca.getVals(), result)) sum++;
            }
                
        density = (sum / NUM_TESTS);
        
        if (state.output == null)  // can happen if we call from main() below
            System.err.println("Generalization Accuracy: " + density);
        else
            {
            state.output.println("Generalization Accuracy: " + density, 1);  // stderr
            state.output.println("Generalization Accuracy: " + density, log);
            }
        }


    public static void main(String[] args)
        {
        // tests the ABK rule 
        
        int[] ABK = new int[] {
            0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,1,1
            };
        EvolutionState state = new EvolutionState();
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast(500) };
        MajorityGA ga = new MajorityGA();
        ga.setup(state, new Parameter(""));
        BitVectorIndividual bvi = new BitVectorIndividual();
        bvi.fitness = new ec.simple.SimpleFitness();
        bvi.genome = new boolean[128];
        for(int i = 0; i < 128; i++)
            bvi.genome[i] = (ABK[i] == 0 ? false : true);
        ga.evaluate(state, bvi, 0, 0);
        System.err.println("ABK Rule");
        ga.describe(state, bvi, 0, 0, 1);
        }
    }
