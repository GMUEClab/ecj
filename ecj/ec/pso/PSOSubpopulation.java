/*
  Copyright 2006 by Ankur Desai, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.pso;

import ec.*;
import ec.util.*;
import ec.vector.*;
import java.io.*;

/**
 * PSOSubpopulation.java
 *
 
 <p>Particle Swarm Optimization (PSO) is a population-oriented stochastic search 
 technique similar to genetic algorithms, evolutionary strategies, and other evolutionary
 computation algorithms. The technique discovers solutions for N-dimensional 
 parameterized problems: basically it discovers the point in N-dimensional space which
 maximizes some quality function. 
   
 <p>PSOSubpopulation handles initialization and input/output of the swarm.   
 
 <p><b>Parameters</b><br>
 <table>
 
 <tr><td valign=top><i>base</i>.<tt>neighborhood-size</tt><br>
 <font size=-1>integer</font></td>
 <td valign=top>(the number of individuals per neighborhood)<br></td></tr>
 
 <tr><td valign=top><i>base</i>.<tt>clamp</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(clamp the individual to stay within the bounds or not)<br></td></tr>
 
 <tr><td valign=top><i>base</i>.<tt>initial-velocity-scale</tt><br>
 <font size=-1>double</font></td>
 <td valign=top>(particles are initialized with a random velocity and this value provides bounds. 
 A value of 1.0 means that the velocity will be within +/- the range of the genotype.)<br></td></tr>

 <tr><td valign=top><i>base</i>.<tt>velocity-multiplier</tt><br>
 <font size=-1>double</font></td>
 <td valign=top>(particle velocities are multiplied by this value before the particle is updated. 
 Increasing this value helps particles to escape local optima, but slows convergence. The default 
 value of 1.5 is geared toward multi-modal landscapes.)<br></td></tr>
 
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>Subpopulation</td></tr>
 </table>
 
 * @author Joey Harrison, Ankur Desai
 * @version 1.0 
 */
public class PSOSubpopulation extends Subpopulation
    {
    public int neighborhoodSize;       
    public static final String P_NEIGHBORHOOD_SIZE = "neighborhood-size";
        
    public boolean clampRange;
    public static final String P_CLAMP_RANGE = "clamp";
        
    public double initialVelocityScale;
    public static final String P_INITIAL_VELOCITY_SCALE = "initial-velocity-scale";
    
    public double velocityMultiplier;
    public static final String P_VELOCITY_MULTIPLIER = "velocity-multiplier";
        
    public DoubleVectorIndividual globalBest;
    public DoubleVectorIndividual[] neighborhoodBests;
    public DoubleVectorIndividual[] personalBests;
    public DoubleVectorIndividual[] previousIndividuals;
    
    public static final String GLOBAL_BEST_PREAMBLE = "Global-Best Individual: ";
    public static final String NEIGHBORHOOD_BEST_PREAMBLE = "Neighborhood Best Individuals: ";
    public static final String PERSONAL_BEST_PREAMBLE = "Personal Best Individuals ";
    public static final String PREVIOUS_INDIVIDUAL_PREAMBLE = "Previous Individuals ";
    public static final String INDIVIDUAL_EXISTS_PREAMBLE = "Exists: ";

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        
        if (!(species instanceof FloatVectorSpecies))
            state.output.error("PSOSubpopulation requires that its species is ec.vector.FloatVectorSpecies or a subclass.  Yours is: " + species.getClass(),
                               null,null);
        if (!(species.i_prototype instanceof DoubleVectorIndividual))
            state.output.error("PSOSubpopulation requires that its species' prototypical individual be is ec.vector.DoubleVectorSpecies or a subclass.  Yours is: " + species.getClass(),
                               null,null);
        
        neighborhoodBests = new DoubleVectorIndividual[individuals.length];
        personalBests = new DoubleVectorIndividual[individuals.length];
        previousIndividuals = new DoubleVectorIndividual[individuals.length];
        
        neighborhoodSize = state.parameters.getInt(base.push(P_NEIGHBORHOOD_SIZE), null);
        clampRange = state.parameters.getBoolean(base.push(P_CLAMP_RANGE), null, false);
        initialVelocityScale = state.parameters.getDouble(base.push(P_INITIAL_VELOCITY_SCALE), null,0);
        velocityMultiplier = state.parameters.getDouble(base.push(P_VELOCITY_MULTIPLIER), null,0.1);
        }
    
    public void populate(EvolutionState state, int thread)
        {
        super.populate(state, thread);
        
        if (loadInds == null)  // we're generating new individuals, not reading them from a file
            {
            FloatVectorSpecies fvSpecies = (FloatVectorSpecies)species;
            double range = fvSpecies.maxGene - fvSpecies.minGene;
                    
            for (int i = 0; i < individuals.length; i++)
                {
                DoubleVectorIndividual prevInd = (DoubleVectorIndividual)individuals[i].clone();
                                    
                // pick a genome near prevInd but not outside the box
                for(int j = 0; j < prevInd.genomeLength(); j++)
                    {
                    double val = prevInd.genome[j];
                    do 
                        prevInd.genome[j] = val + (range * initialVelocityScale) * (state.random[thread].nextDouble()*2.0 - 1.0);
                    while (prevInd.genome[j] < fvSpecies.minGene(j) || prevInd.genome[j] > fvSpecies.maxGene(j));
                    }
                previousIndividuals[i] = prevInd;
                }
            }
        }

    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void printSubpopulationForHumans(final EvolutionState state,
                                            final int log, 
                                            final int verbosity)
        {
        // global best
        state.output.println(GLOBAL_BEST_PREAMBLE, verbosity, log);
        if (globalBest == null) 
            state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "false", verbosity, log);
        else 
            {
            state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "true", verbosity, log);
            globalBest.printIndividualForHumans(state, log, verbosity);
            }
        
        // neighborhoodBests
        state.output.println(NEIGHBORHOOD_BEST_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (neighborhoodBests[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "false", verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "true", verbosity, log);
                neighborhoodBests[i].printIndividualForHumans(state, log, verbosity);
                }
            
        // personalBests
        state.output.println(PERSONAL_BEST_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (personalBests[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "false", verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "true", verbosity, log);
                personalBests[i].printIndividualForHumans(state, log, verbosity);
                }

        // neighborhoodBests
        state.output.println(PREVIOUS_INDIVIDUAL_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (previousIndividuals[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "false", verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + "true", verbosity, log);
                previousIndividuals[i].printIndividualForHumans(state, log, verbosity);
                }

        super.printSubpopulationForHumans(state, log, verbosity);
        }
        
    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void printSubpopulation(final EvolutionState state,
                                   final int log, 
                                   final int verbosity)
        {
        // global best
        state.output.println(GLOBAL_BEST_PREAMBLE, verbosity, log);
        if (globalBest == null) 
            state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false), verbosity, log);
        else 
            {
            state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true), verbosity, log);
            globalBest.printIndividual(state, log, verbosity);
            }
        
        // neighborhoodBests
        state.output.println(NEIGHBORHOOD_BEST_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (neighborhoodBests[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false), verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true), verbosity, log);
                neighborhoodBests[i].printIndividual(state, log, verbosity);
                }
            
        // personalBests
        state.output.println(PERSONAL_BEST_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (personalBests[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false), verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true), verbosity, log);
                personalBests[i].printIndividual(state, log, verbosity);
                }

        // neighborhoodBests
        state.output.println(PREVIOUS_INDIVIDUAL_PREAMBLE, verbosity, log);
        for(int i = 0; i < individuals.length; i++)
            if (previousIndividuals[i] == null)
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false), verbosity, log);
            else 
                {
                state.output.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true), verbosity, log);
                previousIndividuals[i].printIndividual(state, log, verbosity);
                }

        super.printSubpopulation(state, log, verbosity);
        }
        
    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void printSubpopulation(final EvolutionState state,
                                   final PrintWriter writer)
        {
        // global best
        writer.println(GLOBAL_BEST_PREAMBLE);
        if (globalBest == null) 
            writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false));
        else 
            {
            writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true));
            globalBest.printIndividual(state, writer);
            }
        
        // neighborhoodBests
        writer.println(NEIGHBORHOOD_BEST_PREAMBLE);
        for(int i = 0; i < individuals.length; i++)
            if (neighborhoodBests[i] == null)
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false));
            else 
                {
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true));
                neighborhoodBests[i].printIndividual(state, writer);
                }
            
        // personalBests
        writer.println(PERSONAL_BEST_PREAMBLE);
        for(int i = 0; i < individuals.length; i++)
            if (personalBests[i] == null)
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false));
            else 
                {
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true));
                personalBests[i].printIndividual(state, writer);
                }

        // neighborhoodBests
        writer.println(PREVIOUS_INDIVIDUAL_PREAMBLE);
        for(int i = 0; i < individuals.length; i++)
            if (previousIndividuals[i] == null)
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(false));
            else 
                {
                writer.println(INDIVIDUAL_EXISTS_PREAMBLE + Code.encode(true));
                previousIndividuals[i].printIndividual(state, writer);
                }

        super.printSubpopulation(state, writer);
        }
    
    DoubleVectorIndividual possiblyReadIndividual(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        if (Code.readBooleanWithPreamble(INDIVIDUAL_EXISTS_PREAMBLE, state, reader))
            return (DoubleVectorIndividual)species.newIndividual(state, reader);
        else return null;
        }
    
    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void readSubpopulation(final EvolutionState state, 
                                  final LineNumberReader reader) throws IOException
        {
        // global best
        Code.checkPreamble(GLOBAL_BEST_PREAMBLE, state, reader);
        globalBest = possiblyReadIndividual(state, reader);
        
        // neighborhoodBests
        Code.checkPreamble(NEIGHBORHOOD_BEST_PREAMBLE, state, reader);
        for(int i = 0; i < individuals.length; i++)
            neighborhoodBests[i] = possiblyReadIndividual(state, reader); 
            
        // personalBests
        Code.checkPreamble(PERSONAL_BEST_PREAMBLE, state, reader);
        for(int i = 0; i < individuals.length; i++)
            personalBests[i] = possiblyReadIndividual(state, reader); 

        // neighborhoodBests
        Code.checkPreamble(PREVIOUS_INDIVIDUAL_PREAMBLE, state, reader);
        for(int i = 0; i < individuals.length; i++)
            previousIndividuals[i] = possiblyReadIndividual(state, reader); 
        
        super.readSubpopulation(state, reader);
        }
        
    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void writeSubpopulation(final EvolutionState state,
                                   final DataOutput dataOutput) throws IOException
        {
        // global best
        if (globalBest == null) 
            dataOutput.writeBoolean(false);
        else
            {
            dataOutput.writeBoolean(true);
            globalBest.writeIndividual(state, dataOutput);
            }
        
        // neighborhoodBests
        for(int i = 0; i < individuals.length; i++)
            if (neighborhoodBests[i] == null)
                dataOutput.writeBoolean(false);
            else 
                {
                dataOutput.writeBoolean(true);
                neighborhoodBests[i].writeIndividual(state, dataOutput);
                }
            
        // personalBests
        for(int i = 0; i < individuals.length; i++)
            if (personalBests[i] == null)
                dataOutput.writeBoolean(false);
            else 
                {
                dataOutput.writeBoolean(true);
                personalBests[i].writeIndividual(state, dataOutput);
                }

        // previous Individuals
        for(int i = 0; i < individuals.length; i++)
            if (previousIndividuals[i] == null)
                dataOutput.writeBoolean(false);
            else 
                {
                dataOutput.writeBoolean(true);
                previousIndividuals[i].writeIndividual(state, dataOutput);
                }

        super.writeSubpopulation(state, dataOutput);
        }
    
    /** Overridden to include the global best, neighborhood bests, personal bests, and previous individuals in the stream.
        The neighborhood size, clamp range, and initial velocity scale are not included -- it's assumed you're using the
        same values for them on reading, or understand that the values are revised. */
    public void readSubpopulation(final EvolutionState state,
                                  final DataInput dataInput) throws IOException
        {
        // global best
        globalBest = (dataInput.readBoolean() ? (DoubleVectorIndividual)species.newIndividual(state, dataInput) : null);
        
        // neighborhoodBests
        for(int i = 0; i < individuals.length; i++)
            neighborhoodBests[i] = (dataInput.readBoolean() ? (DoubleVectorIndividual)species.newIndividual(state, dataInput): null); 
            
        // personalBests
        for(int i = 0; i < individuals.length; i++)
            personalBests[i] = (dataInput.readBoolean() ? (DoubleVectorIndividual)species.newIndividual(state, dataInput): null); 

        // previous Individuals
        for(int i = 0; i < individuals.length; i++)
            previousIndividuals[i] = (dataInput.readBoolean() ? (DoubleVectorIndividual)species.newIndividual(state, dataInput): null); 

        super.readSubpopulation(state, dataInput);
        }       
    }
