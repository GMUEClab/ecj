/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.Evaluator;
import ec.EvolutionState;
import ec.util.Parameter;
import ec.Individual;
import ec.simple.*;

/* 
 * SPEA2Evaluator.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *    (based on Evaluator.java by Sean Luke)
 */

/**
 * The SPEA2Evaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its
 * own problem space.  One Problem instance is cloned from p_problem for
 * each evaluating thread.  The Problem must implement SimpleProblemForm.
 *
 * The evaluator is also responsible for calculating the SPEA2Fitness
 * function.  This function depends on the entire population and so
 * cannot be calculated in the Problem class.
 *
 * @author Robert Hubley (based on Evaluator.java by Sean Luke)
 * @version 1.0 
 */

public class SPEA2Evaluator extends Evaluator
    {

    // checks to make sure that the Problem implements SimpleProblemForm
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        if (!(p_problem instanceof SimpleProblemForm))
            state.output.fatal("SPEA2Evaluator used, but the Problem is not of SimpleProblemForm",
                               base.push(P_PROBLEM));
        }

    /** A simple evaluator that doesn't do any coevolutionary
        evaluation.  Basically it applies evaluation pipelines,
        one per thread, to various subchunks of a new population. */
    public void evaluatePopulation(final EvolutionState state)
        {
        int numinds[][] = 
            new int[state.evalthreads][state.population.subpops.length];
        int from[][] = 
            new int[state.evalthreads][state.population.subpops.length];

        if (!(state.population.subpops[0] instanceof SPEA2Subpopulation))
            state.output.fatal("SPEA2Evaluator must only be used with a SPEA2Subpopulation!",
                               null);

        for(int y=0;y<state.evalthreads;y++)
            {
            for(int x=0;x<state.population.subpops.length;x++)
                {
                // figure numinds
                if (y<state.evalthreads-1) // not last one
                    {
                    numinds[y][x]=
                        state.population.subpops[x].individuals.length/
                        state.evalthreads;
                    }
                else // in case we're slightly off in division
                    {
                    numinds[y][x]=
                        state.population.subpops[x].individuals.length/
                        state.evalthreads +
                        (state.population.subpops[x].individuals.length -
                         (state.population.subpops[x].individuals.length /
                          state.evalthreads)  // note integer division
                         *state.evalthreads);                   
                    }

                // figure from
                from[y][x]=
                    (state.population.subpops[x].individuals.length/
                     state.evalthreads) * y;
                }
            }

        if (state.evalthreads==1)
            {
            evalPopChunk(state,numinds[0],from[0],0,(SimpleProblemForm)(p_problem.clone()));  
            }
        else
            {
            Thread[] t = new Thread[state.evalthreads];

            // start up the threads
            for(int y=0;y<state.evalthreads;y++)
                {
                SPEA2EvaluatorThread r = new SPEA2EvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.me = this;
                r.state = state;
                r.p = (SimpleProblemForm)(p_problem.clone());
                t[y] = new Thread(r);
                t[y].start();
                }

            // gather the threads
            for(int y=0;y<state.evalthreads;y++) 
                {
                try
                    {
                    t[y].join();
                    }
                catch(InterruptedException e)
                    {
                    state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                    }
                }
            }

        // Ok...now all individuals have been evaluated
        // so we can go ahead and calculate the raw and
        // density values of the SPEA2 fitness function

        // Each subpopulation
        double rawFitness;
        double kthDistance;
        double vol;
        int kTH;
        double MAXDOUBLE = 99999999;
        for(int x = 0;x<state.population.subpops.length;x++)
            {

            Individual[] inds = state.population.subpops[x].individuals;
            double[][] distances = new double[inds.length][inds.length];
            // For each individual calculate the strength
            for(int y=0;y<inds.length;y++)
                {
                // Calculate the node strengths
                int myStrength = 0;
                for(int z=0;z<inds.length;z++)
                    {
                    if ( inds[y].fitness.betterThan(inds[z].fitness) ) 
                        {
                        myStrength++;
                        }
                    }
                ((SPEA2MultiObjectiveFitness)inds[y].fitness).SPEA2Strength = myStrength;
                } //For each individual y calculate the strength

            double sumKthDistance = 0;
            // For each individual calculate the Raw fitness and distances
            for(int y=0;y<inds.length;y++)
                {
                rawFitness = 0;
                kthDistance = 0;
                for(int z=0;z<inds.length;z++)
                    {

                    // Raw fitness 
                    if ( inds[z].fitness.betterThan(inds[y].fitness) )
                        {
                        rawFitness += ((SPEA2MultiObjectiveFitness)inds[z].fitness).
                            SPEA2Strength;
                        }

                    // Set distances
                    if ( y == z ) {
                        distances[y][z] = 0;
                        }else if ( z > y ) {
                            distances[y][z] =
                                ((SPEA2MultiObjectiveFitness)inds[y].fitness).
                                calcDistance( (SPEA2MultiObjectiveFitness)inds[z].fitness );
                            distances[z][y] = distances[y][z];
                            }

                    } // For each individual z calculate RAW fitness distances

                // Density component
                // calculate k value
                kTH = (int)Math.sqrt(inds.length-1);
                vol = volSphere(((SPEA2MultiObjectiveFitness)inds[0].fitness).multifitness.length);

                // calc k-th nearest neighbor distance
                double dist = 0;
                int index = -1;
                for(int j=0;j<inds.length;j++)
                    {
                    dist = MAXDOUBLE;
                    for(int k=0;k<inds.length;k++)
                        {
                        if ( distances[y][k] < dist )
                            {
                            index = k; //index of current jth NN
                            dist = distances[y][k];
                            if ( dist == 0 ) { break; }
                            } // k
                        }
                    distances[y][index] = distances[y][j];
                    if ( dist > 0 && j >= kTH ) { break; }
                    } // j

                if ( dist == 0 ) 
                    { // exception: only possible when all are equal
                    dist = 1; 
                    }

                kthDistance = 1 /
                    Math.pow(dist,((SPEA2MultiObjectiveFitness)inds[0].fitness).multifitness.length) *
                    kTH / inds.length / vol;
                sumKthDistance += kthDistance;

                // Set SPEA2 raw fitness value for each individual
                ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                 individuals[y].fitness).SPEA2RawFitness = rawFitness;

                // Set SPEA2 k-th NN distance value for each individual
                ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                 individuals[y].fitness).SPEA2kthNNDistance = kthDistance;

                } // For each individual y

            // Normalize the kthDistance value
            for(int y=0;y<inds.length;y++)
                {
                ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                 individuals[y].fitness).SPEA2kthNNDistance /= sumKthDistance;

                // Set SPEA2 fitness value for each individual
                ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                 individuals[y].fitness).SPEA2Fitness =
                    ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                     individuals[y].fitness).SPEA2RawFitness  +
                    ((SPEA2MultiObjectiveFitness)state.population.subpops[x].
                     individuals[y].fitness).SPEA2kthNNDistance;

                }

            } // For each subpopulation

        }

    /** Private helper function.  This calculates the area of
        a sphere in n dimensions for use in the SPEA2 density
        calculation. */
    public double volSphere(int dimensions)
        {
        double PI = 3.14159;
        double vol = 1;
        if ( dimensions % 2 == 0 ) 
            {
            for ( int i=1; i<=dimensions/2; i++ ) 
                {
                vol *= i;
                } // for (int i=1;...
            vol = Math.pow(PI,dimensions/2)/vol; 
            } // if ( dimensions % 2 == 0...
        else 
            {
            for ( int i=(dimensions-1)/2+1; i<=dimensions; i++ )
                {
                vol *= i;
                } // for ( int i=(dimensions-1)/2+1;...
            vol = Math.pow(2,dimensions) * Math.pow(PI,(dimensions-1)/2) * vol;
            } // else
        return vol;

        }



    /** A private helper function for evaluatePopulation which evaluates a chunk
        of individuals in a subpopulation for a given thread.
        Although this method is declared
        public (for the benefit of a private helper class in this file),
        you should not call it. */
    public void evalPopChunk(EvolutionState state, int[] numinds, int[] from,
                             int threadnum, SimpleProblemForm p)
        {
        for(int pop=0;pop<state.population.subpops.length;pop++)
            {
            // start evaluatin'!
            int upperbound = from[pop]+numinds[pop];
            for (int x=from[pop];x<upperbound;x++)
                ((SimpleProblemForm)p).evaluate(state,state.population.subpops[pop].individuals[x], threadnum);
            }
        }

    /** The SPEA2Evaluator determines that a run is complete by asking
        each individual in each population if he or she is optimal; if it
        finds an individual somewhere that's optimal,
        it signals that the run is complete. */
    public boolean runComplete(final EvolutionState state)
        {
        for(int x = 0;x<state.population.subpops.length;x++)
            for(int y=0;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].
                    individuals[y].fitness.isIdealFitness())
                    return true;
        return false;
        }
    }

/** A private helper class for implementing multithreaded evaluation */
class SPEA2EvaluatorThread implements Runnable
    {
    public int[] numinds;
    public int[] from;
    public SPEA2Evaluator me;
    public EvolutionState state;
    public int threadnum;
    public SimpleProblemForm p;
    public synchronized void run() 
        { me.evalPopChunk(state,numinds,from,threadnum,p); }
    }
