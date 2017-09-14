package ec.pso ;

import ec.* ;
import ec.vector.*;
import ec.util.*;
import java.util.* ;
import java.io.*;

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
    public static final String AUXILLARY_PREAMBLE = "Auxillary: ";
        
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

    public int hashCode()
        {
        int hash = super.hashCode();
        // no need to change anything I think
        return hash;
        }


    public boolean equals(Object ind)
        {
        if (!super.equals(ind)) return false;
        Particle i = (Particle) ind;

        if ((velocity == null && i.velocity != null) ||
            (velocity != null && i.velocity == null))
            return false;
                        
        if (velocity != null)
            {
            if (velocity.length != i.velocity.length)
                return false;
            for (int j = 0; j < velocity.length; j++)
                if (velocity[j] != i.velocity[j])
                    return false;
            }
                        
        if ((neighborhood == null && i.neighborhood != null) ||
            (neighborhood != null && i.neighborhood == null))
            return false;
                        
        if (neighborhood != null)
            {
            if (neighborhood.length != i.neighborhood.length)
                return false;
            for (int j = 0; j < neighborhood.length; j++)
                if (neighborhood[j] != i.neighborhood[j])
                    return false;
            }

        if ((neighborhoodBestGenome == null && i.neighborhoodBestGenome != null) ||
            (neighborhoodBestGenome != null && i.neighborhoodBestGenome == null))
            return false;
                        
        if (neighborhoodBestGenome != null)
            {
            if (neighborhoodBestGenome.length != i.neighborhoodBestGenome.length)
                return false;
            for (int j = 0; j < neighborhoodBestGenome.length; j++)
                if (neighborhoodBestGenome[j] != i.neighborhoodBestGenome[j])
                    return false;
            }
                        
        if ((neighborhoodBestFitness == null && i.neighborhoodBestFitness != null) ||
            (neighborhoodBestFitness != null && i.neighborhoodBestFitness == null))
            return false;
                        
        if (neighborhoodBestFitness != null)
            {
            if (!neighborhoodBestFitness.equals(i.neighborhoodBestFitness))
                return false;
            }

        if ((personalBestGenome == null && i.personalBestGenome != null) ||
            (personalBestGenome != null && i.personalBestGenome == null))
            return false;
                        
        if (personalBestGenome != null)
            {
            if (personalBestGenome.length != i.personalBestGenome.length)
                return false;
            for (int j = 0; j < personalBestGenome.length; j++)
                if (personalBestGenome[j] != i.personalBestGenome[j])
                    return false;
            }

        if ((personalBestFitness == null && i.personalBestFitness != null) ||
            (personalBestFitness != null && i.personalBestFitness == null))
            return false;
                        
        if (personalBestFitness != null)
            {
            if (!personalBestFitness.equals(i.personalBestFitness))
                return false;
            }

        return true;
        }

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
        
        // printIndividual(state, 0);
        
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
            already.add(Integer.valueOf(myIndex));
            }
        else
            neighbors = new int[neighborhoodSize];
        
        Integer n = null;
        for(int i = 0; i < neighborhoodSize; i++)
            {
            do
                {
                neighbors[i] = mtf.nextInt(popsize);
                n = Integer.valueOf(neighbors[i]);
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
        
        
        
        
    /// The following methods handle modifying the auxillary data when the
    /// genome is messed around with.
    
    void resetAuxillaryInformation()
        {
        neighborhood = null;
        neighborhoodBestGenome = null;
        neighborhoodBestFitness = null;
        personalBestGenome = null;
        personalBestFitness = null;
        for(int i = 0; i < velocity.length; i++)
            velocity[i] = 0.0;
        }
        
    public void reset(EvolutionState state, int thread)
        {
        super.reset(state, thread);
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
        resetAuxillaryInformation();
        }
    
    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void setGenomeLength(int len)
        {
        super.setGenomeLength(len);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
        resetAuxillaryInformation();
        }
        
    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void setGenome(Object gen)
        {
        super.setGenome(gen);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
        resetAuxillaryInformation();
        }

    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void join(Object[] pieces)
        {
        super.join(pieces);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
        resetAuxillaryInformation();
        }
        
        
    
    
    /// gunk for reading and writing, but trying to preserve some of the 
    /// auxillary information
        
    StringBuilder encodeAuxillary()
        {
        StringBuilder s = new StringBuilder();
        s.append(AUXILLARY_PREAMBLE);
        s.append(Code.encode(true));
        s.append(Code.encode(neighborhood!=null));
        s.append(Code.encode(neighborhoodBestGenome != null));
        s.append(Code.encode(neighborhoodBestFitness != null));
        s.append(Code.encode(personalBestGenome != null));
        s.append(Code.encode(personalBestFitness != null));
        s.append("\n");
        
        // velocity
        s.append(Code.encode(velocity.length));
        for(int i = 0; i < velocity.length; i++)
            s.append(Code.encode(velocity[i]));
        s.append("\n");
        
        // neighborhood 
        if (neighborhood != null)
            {
            s.append(Code.encode(neighborhood.length));
            for(int i = 0; i < neighborhood.length; i++)
                s.append(Code.encode(neighborhood[i]));
            s.append("\n");
            }

        // neighborhood best
        if (neighborhoodBestGenome != null)
            {
            s.append(Code.encode(neighborhoodBestGenome.length));
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                s.append(Code.encode(neighborhoodBestGenome[i]));
            s.append("\n");
            }

        if (neighborhoodBestFitness != null)
            s.append(neighborhoodBestFitness.fitnessToString());

        // personal     best
        if (personalBestGenome != null)
            {
            s.append(Code.encode(personalBestGenome.length));
            for(int i = 0; i < personalBestGenome.length; i++)
                s.append(Code.encode(personalBestGenome[i]));
            s.append("\n");
            }

        if (personalBestFitness != null)
            s.append(personalBestFitness.fitnessToString());
        s.append("\n");
                
        return s;
        }
    
    public void printIndividual(final EvolutionState state, final int log)
        {
        super.printIndividual(state, log);
        state.output.println(encodeAuxillary().toString(), log);
        }

    public void printIndividual(final EvolutionState state, final PrintWriter writer)
        {
        super.printIndividual(state, writer);
        writer.println(encodeAuxillary().toString());
        }

    public void readIndividual(final EvolutionState state, 
        final LineNumberReader reader)
        throws IOException
        {
        super.readIndividual(state, reader);
        
        // Next, read auxillary header.
        DecodeReturn d = new DecodeReturn(Code.readStringWithPreamble(AUXILLARY_PREAMBLE, state, reader));
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean v = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean n = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean nb = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean nbf = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean pb = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean pbf = (d.l != 0);

        // Next, read auxillary arrays.
        if (v)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Velocity length missing.");
            velocity = new double[(int)(d.l)];
            for(int i = 0; i < velocity.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Velocity information not long enough");
                velocity[i] = d.d;
                }
            }
        else velocity = new double[genome.length];

        if (n)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Neighborhood length missing.");
            neighborhood = new int[(int)(d.l)];
            for(int i = 0; i < neighborhood.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_INT)
                    state.output.fatal("Neighborhood information not long enough");
                neighborhood[i] = (int)(d.l);
                }
            }
        else neighborhood = null;

        if (nb)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Neighborhood-Best length missing.");
            neighborhoodBestGenome = new double[(int)(d.l)];
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Neighborhood-Best genome not long enough");
                neighborhoodBestGenome[i] = d.d;
                }
            }
        else neighborhoodBestGenome = null;

        if (nbf)
            {
            // here we don't know what kind of fitness it is.  So we'll do our best and guess
            // that it's the same fitness as our own Particle 
            neighborhoodBestFitness = (Fitness)(fitness.clone());
            neighborhoodBestFitness.readFitness(state, reader);
            }

        if (pb)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Personal-Best length missing.");
            personalBestGenome = new double[(int)(d.l)];
            for(int i = 0; i < personalBestGenome.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Personal-Best genome not long enough");
                personalBestGenome[i] = d.d;
                }
            }
        else personalBestGenome = null;

        if (pbf)
            {
            // here we don't know what kind of fitness it is.  So we'll do our best and guess
            // that it's the same fitness as our own Particle 
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestFitness.readFitness(state, reader);
            }
        }

    public void writeIndividual(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        super.writeIndividual(state, dataOutput);
        
        if (velocity != null)  // it's always non-null
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(velocity.length);
            for(int i = 0; i < velocity.length; i++)
                dataOutput.writeDouble(velocity[i]);
            }
        else dataOutput.writeBoolean(false);  // this will never happen
 
 
        if (neighborhood != null)
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(neighborhood.length);
            for(int i = 0; i < neighborhood.length; i++)
                dataOutput.writeDouble(neighborhood[i]);
            }
        else dataOutput.writeBoolean(false);


        if (neighborhoodBestGenome != null)
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(neighborhoodBestGenome.length);
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                dataOutput.writeDouble(neighborhoodBestGenome[i]);
            }
        else dataOutput.writeBoolean(false);


        if (neighborhoodBestFitness != null)
            {
            dataOutput.writeBoolean(true);
            neighborhoodBestFitness.writeFitness(state, dataOutput);
            }
        else dataOutput.writeBoolean(false);


        if (personalBestGenome != null)  // it's always non-null
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(personalBestGenome.length);
            for(int i = 0; i < personalBestGenome.length; i++)
                dataOutput.writeDouble(personalBestGenome[i]);
            }
        else dataOutput.writeBoolean(false);


        if (personalBestFitness != null)
            {
            dataOutput.writeBoolean(true);
            personalBestFitness.writeFitness(state, dataOutput);
            }
        else dataOutput.writeBoolean(false);
        }

    public void readIndividual(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        super.readIndividual(state, dataInput);
        
        // Next, read auxillary arrays.
        if (dataInput.readBoolean())
            {
            velocity = new double[dataInput.readInt()];
            for(int i = 0; i < velocity.length; i++)
                velocity[i] = dataInput.readDouble();
            }
        else velocity = new double[genome.length];

        if (dataInput.readBoolean())
            {
            neighborhood = new int[dataInput.readInt()];
            for(int i = 0; i < neighborhood.length; i++)
                neighborhood[i] = dataInput.readInt();
            }
        else neighborhood = null;
        
        if (dataInput.readBoolean())
            {
            neighborhoodBestGenome = new double[dataInput.readInt()];
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                neighborhoodBestGenome[i] = dataInput.readDouble();
            }
        else neighborhoodBestGenome = null;

        if (dataInput.readBoolean())
            {
            neighborhoodBestFitness = (Fitness)(fitness.clone());
            neighborhoodBestFitness.readFitness(state, dataInput);
            }

        if (dataInput.readBoolean())
            {
            personalBestGenome = new double[dataInput.readInt()];
            for(int i = 0; i < personalBestGenome.length; i++)
                personalBestGenome[i] = dataInput.readDouble();
            }
        else personalBestGenome = null;

        if (dataInput.readBoolean())
            {
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestFitness.readFitness(state, dataInput);
            }
        }

    }
