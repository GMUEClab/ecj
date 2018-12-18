/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.majority;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/**
   MajorityGP.java
        
   Implements a GP-style vector rule for the one-dimensional Majority-Ones cellular automaton problem.
   This code is in the spirit of Das, Crutchfield, Mitchel, and Hanson, "Evolving Globally Synchronized Cellular AUtomata",
   http://web.cecs.pdx.edu/~mm/EGSCA.pdf
   
   The primary difference is in the trials mechanism, in which we're using 25/25/50 rather than 50/50/0.
*/

public class MajorityGP extends GPProblem implements SimpleProblemForm
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
            MajorityData input = (MajorityData)(this.input);

            int sum = 0;
            
            // extract the rule
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);

            int[] rule = ca.getRule();
            for(int i = 0; i < 64; i++)
                rule[i] = (int)(((input.data0) >> i) & 0x1);
            for(int i = 64; i < 128; i++)
                rule[i] = (int)(((input.data1) >> (i - 64)) & 0x1);
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
            f.setFitness(state, sum / (double)NUM_TRIALS, (sum == NUM_TRIALS));
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

        MajorityData input = (MajorityData)(this.input);

        // extract the rule
        ((GPIndividual)ind).trees[0].child.eval(
            state,threadnum,input,stack,((GPIndividual)ind),this);
                
        int[] rule = ca.getRule();
        for(int i = 0; i < 64; i++)
            rule[i] = (int)(((input.data0) >> i) & 0x1);
        for(int i = 64; i < 128; i++)
            rule[i] = (int)(((input.data1) >> (i - 64)) & 0x1);
        ca.setRule(rule);  // for good measure though it doesn't matter

        // print rule                
        String s = "Rule: ";
        for(int i = 0; i < rule.length; i++)
            s += rule[i];
        state.output.println(s, log);
                        
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
        state.output.println("Generalization Accuracy: " + density, 1);  // stderr
        state.output.println("Generalization Accuracy: " + density, log);
        }





    }
