package ec.pso ;

import ec.* ;
import ec.vector.*;
import ec.util.*;
import java.util.* ;


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
        if (neighborhood == null)
            {
            PSOBreeder psob = (PSOBreeder)(state.breeder);
            if(psob.neighborhood == psob.C_NEIGHBORHOOD_RANDOM) // "random" scheme is the only thing that is available for now
                neighborhood = createRandomPattern(state.population.subpops[subpop].individuals.length,
                    psob.neighborhoodSize, state.random[thread]);
            else if(psob.neighborhood == psob.C_NEIGHBORHOOD_TOROIDAL)
                neighborhood = createToroidalPattern(myindex, state.population.subpops[subpop].individuals.length,
                    psob.neighborhoodSize);
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
    int[] createRandomPattern(int popsize, int neighborhoodSize, MersenneTwisterFast mtf)
        {
        HashSet already = new HashSet();
        int[] neighbors = new int[neighborhoodSize];
        
        Integer n = null;
        for(int i = 1; i < neighborhoodSize; i++)
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

        /*
          ArrayList indices = new ArrayList();
          int[] neighbours = new int[neighborhoodSize] ;
          for(int i = 0 ; i < popsize ; i++) indices.add(new Integer(i)); // O(N)
          for(int i = 0 ; i < neighborhoodSize ; i++)                                // O(K) ??
          neighbours[i] = ((Integer)indices.remove(mtf.nextInt(indices.size()))).intValue(); // O(1) ??
          return neighbours;
        */
        }

    // Creates a toroidal neighborhood pattern for the individual indexed by 'myindex'
    int[] createToroidalPattern(int myindex, int popsize, int neighborhoodSize)
        {
        int[] neighbours = new int[neighborhoodSize] ;
        int start = myindex - neighborhoodSize/2;
        if(start < 0) start += popsize ;
        for(int i = 0 ; i < neighborhoodSize ; i++)
            neighbours[i] = (start + i) % popsize ;
        return neighbours;
        }
    }
