package ec.pso ;

import ec.* ;
import ec.vector.*;
import ec.util.*;
import java.util.* ;

/*
 * Particle.java
 * Created: Thu May  2 17:09:40 EDT 2013
 */

/**
 * Particle is a DoubleVectorIndividual with additional statistical information
 * necessary to perform Particle Swarm Optimization.  Specifically, it has a 
 * VELOCITY, a NEIGHBORHOOD of indexes of individuals, a NEIGHBORHOOD BEST genome
 * and fitness, and a PERSONAL BEST genome and fitness.  These elements, plus the
 * GLOBAL BEST genome and fitness found in PSOBreeder, are used to collectively
 * update the particle's location in space.
 *
 * <p> Particle updates its location in two steps.  First, it gathers current
 * neighborhood and personal best statistics via the update(...) method.  Then
 * it updates the particle's velocity and location (genome) according to these
 * statistics in the tweak(...) method.  Notice that neither of these methods is
 * the defaultMutate(...) method used in DoubleVectorIndividual: this means that
 * in *theory* you could rig up Particles to also be mutated if you thought that
 * was a good reason.
 * 
 * <p> Many of the parameters passed into the tweak(...) method are based on
 * weights determined by the PSOBreeder.
 *
 * @author Khaled Ahsan Talukder
 */


public class Particle extends DoubleVectorIndividual
    {
    // my velocity
    public double[] velocity ;
        
    // the individuals in my neighborhood
    public int[] neighborhood = null ;

    // the best genome and fitness members of my neighborhood ever achieved
    public double[] neighborhoodBestGenome = null;
    public Fitness neighborhoodBestFitness = null;

    // the best genome and fitness *I* personally ever achieved
    public double[] personalBestGenome = null;
    public Fitness personalBestFitness = null;


    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        velocity = new double[genome.length] ;
        }
        

    public Object clone()
        {
        Particle myobj = (Particle) (super.clone());
        // must clone the velocity and neighborhood pattern if they exist
        if (velocity != null) velocity = (double[])(velocity.clone());
        if (neighborhood != null) neighborhood = (int[])(neighborhood.clone());
        return myobj;
        }

    public void update(final EvolutionState state, int subpop, int myindex, int thread)
        {
        // update personal best
        if (personalBestFitness == null || fitness.betterThan(personalBestFitness))
            {
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestGenome = (double[])(genome.clone());
            }
        
        // initialize neighborhood if it's not been created yet
        PSOBreeder psob = (PSOBreeder)(state.breeder);
        if (neighborhood == null || psob.neighborhood == psob.C_NEIGHBORHOOD_RANDOM_EACH_TIME)
            {
            if (psob.neighborhood == psob.C_NEIGHBORHOOD_RANDOM) // "random" scheme is the only thing that is available for now
                neighborhood = createRandomPattern(myindex, psob.includeSelf, 
                    state.population.subpops[subpop].individuals.length, psob.neighborhoodSize, state, thread);
            else if (psob.neighborhood == psob.C_NEIGHBORHOOD_TOROIDAL || psob.neighborhood == psob.C_NEIGHBORHOOD_RANDOM_EACH_TIME)
                neighborhood = createToroidalPattern(myindex, psob.includeSelf,
                    state.population.subpops[subpop].individuals.length, psob.neighborhoodSize);
            else // huh?
                state.output.fatal("internal error: invalid PSO neighborhood style: " + psob.neighborhood);
            }

        // identify neighborhood best
        neighborhoodBestFitness = fitness;  // initially me
        neighborhoodBestGenome = genome;
        for(int i = 0 ; i < neighborhood.length ; i++)
            {
            int ind = neighborhood[i] ;
            if (state.population.subpops[subpop].individuals[ind].fitness.betterThan(fitness))
                {
                neighborhoodBestFitness = state.population.subpops[subpop].individuals[ind].fitness;
                neighborhoodBestGenome = ((DoubleVectorIndividual)(state.population.subpops[subpop].individuals[ind])).genome;
                }
            }
                
        // clone neighborhood best
        neighborhoodBestFitness = (Fitness)(neighborhoodBestFitness.clone());
        neighborhoodBestGenome = (double[])(neighborhoodBestGenome.clone());
        }

    // velocityCoeff:       cognitive/confidence coefficient for the velocity
    // personalCoeff:       cognitive/confidence coefficient for self
    // informantCoeff:      cognitive/confidence coefficient for informants/neighbours
    // globalCoeff:         cognitive/confidence coefficient for global best, this is not done in the standard PSO
    public void tweak(
        EvolutionState state,  double[] globalBest,
        double velocityCoeff, double personalCoeff, 
        double informantCoeff, double globalCoeff, 
        int thread)
        {
        for(int x = 0 ; x < genomeLength() ; x++)
            {
            double xCurrent = genome[x] ;
            double xPersonal = personalBestGenome[x] ;
            double xNeighbour = neighborhoodBestGenome[x] ;
            double xGlobal = globalBest[x] ;
            double beta = state.random[thread].nextDouble() * personalCoeff ;
            double gamma = state.random[thread].nextDouble() * informantCoeff ;
            double delta = state.random[thread].nextDouble() * globalCoeff ;

            double newVelocity = (velocityCoeff * velocity[x]) + (beta * (xPersonal - xCurrent)) + (gamma * (xNeighbour - xCurrent)) + (delta * (xGlobal - xCurrent)) ;
            velocity[x] = newVelocity ;
            genome[x] += newVelocity ;
            }
                
        evaluated = false ;
        }

    // Creates a toroidal neighborhood pattern for the individual
    int[] createRandomPattern(int myIndex, boolean includeSelf, int popsize, int neighborhoodSize, EvolutionState state, int threadnum)
        {
        MersenneTwisterFast mtf = state.random[threadnum];
        HashSet already = new HashSet();
        int[] neighbors = null;
        
        if (includeSelf)
            {
            neighbors = new int[neighborhoodSize + 1];
            neighbors[neighborhoodSize] = myIndex;  // put me at the top
            already.add(new Integer(myIndex));
            }
        else
            neighbors = new int[neighborhoodSize];
        
        Integer n = null;
        for(int i = 0; i < neighborhoodSize; i++)
            {
            do
                {
                neighbors[i] = mtf.nextInt(popsize);
                n = new Integer(neighbors[i]);
                }
            while (already.contains(n));
            already.add(n);
            }
        return neighbors;

        }

    // Creates a toroidal neighborhood pattern for the individual indexed by 'myindex'
    int[] createToroidalPattern(int myindex, boolean includeSelf, int popsize, int neighborhoodSize)
        {
        int[] neighbors = null;

        if (includeSelf)
            {
            neighbors = new int[neighborhoodSize + 1];
            neighbors[neighborhoodSize] = myindex;  // put me at the top
            }
        else
            neighbors = new int[neighborhoodSize];
        
        int pos = 0;
        for(int i = myindex - neighborhoodSize / 2; i < myindex; i++)
            {
            neighbors[pos++] = ((i % popsize) + popsize) % popsize;
            }
                
        for(int i = myindex + 1; i < neighborhoodSize - (neighborhoodSize / 2) + 1; i++)
            {
            neighbors[pos++] = ((i % popsize) + popsize) % popsize;
            }

        return neighbors;
        }
    }
