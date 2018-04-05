/*
  Portions copyright 2010 by Sean Luke, Robert Hubley, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.spea2;

import java.io.*;
import ec.util.*;
import ec.multiobjective.*;
import ec.*;

/* 
 * SPEA2MultiObjectiveFitness.java
 * 
 * Created: Sat Oct 16 11:24:43 EDT 2010
 * By: Sean Luke
 * Replaces earlier class by: Robert Hubley, with revisions by Gabriel Balan and Keith Sullivan
 */

/**
 * SPEA2MultiObjectiveFitness is a subclass of MultiObjectiveFitness which adds three auxiliary fitness
 * measures used in SPEA2: strength S(i), kthNNDistance D(i), and a final fitness value R(i) + D(i).  
 * Note that so-called "raw fitness" (what Sean calls "Wimpiness" in Essentials of Metaheuristics) is 
 * not retained.
 * 
 * <p>The fitness comparison operators solely use the 'fitness' value R(i) + D(i).
 */

public class SPEA2MultiObjectiveFitness extends MultiObjectiveFitness
    {
    public static final String SPEA2_FITNESS_PREAMBLE = "Fitness: ";
    public static final String SPEA2_STRENGTH_PREAMBLE = "Strength: ";
    public static final String SPEA2_DISTANCE_PREAMBLE = "Distance: ";

    public String[] getAuxilliaryFitnessNames() { return new String[] { "Strength", "Raw Fitness", "Kth NN Distance" }; }
    public double[] getAuxilliaryFitnessValues() { return new double[] { strength, fitness, kthNNDistance }; }
        
    /** SPEA2 strength (# of nodes it dominates) */
    public double strength; // S(i)

    /** SPEA2 NN distance */
    public double kthNNDistance; // D(i)

    /** Final SPEA2 fitness.  Equals the raw fitness R(i) plus the kthNNDistance D(i). */
    public double fitness;

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + SPEA2_FITNESS_PREAMBLE + Code.encode(fitness) + "\n" + SPEA2_STRENGTH_PREAMBLE + Code.encode(strength) + "\n" + SPEA2_DISTANCE_PREAMBLE + Code.encode(kthNNDistance);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + SPEA2_STRENGTH_PREAMBLE + strength + "\n" + SPEA2_DISTANCE_PREAMBLE + kthNNDistance + " " + SPEA2_FITNESS_PREAMBLE + fitness;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        super.readFitness(state, reader);
        fitness = Code.readDoubleWithPreamble(SPEA2_FITNESS_PREAMBLE, state, reader);
        strength = Code.readDoubleWithPreamble(SPEA2_STRENGTH_PREAMBLE, state, reader);
        kthNNDistance = Code.readDoubleWithPreamble(SPEA2_DISTANCE_PREAMBLE, state, reader);
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state, dataOutput);
        dataOutput.writeDouble(fitness);
        dataOutput.writeDouble(strength);
        dataOutput.writeDouble(fitness);
        dataOutput.writeDouble(kthNNDistance);
        writeTrials(state, dataOutput);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        fitness = dataInput.readDouble();
        strength = dataInput.readDouble();
        fitness = dataInput.readDouble();
        kthNNDistance = dataInput.readDouble();
        readTrials(state, dataInput);
        }

    /**
     * The selection criteria in SPEA2 uses the computed fitness, and not
     * pareto dominance.
     */
    public boolean equivalentTo(Fitness _fitness)
        {
        return fitness == ((SPEA2MultiObjectiveFitness)_fitness).fitness;
        }

    /**
     * The selection criteria in SPEA2 uses the computed fitness, and not
     * pareto dominance.
     */
    public boolean betterThan(Fitness _fitness)
        {
        return fitness < ((SPEA2MultiObjectiveFitness)_fitness).fitness;
        }
    }
