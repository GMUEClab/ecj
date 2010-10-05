/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.spea2;

import java.io.*;
import ec.util.DecodeReturn;
import ec.util.Code;
import ec.multiobjective.MultiObjectiveFitness;
import ec.EvolutionState;
import ec.Fitness;

/* 
 * SPEA2MultiObjectiveFitness.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (based on MultiObjectiveFitness.java by Sean Luke)
 */

/**
 * SPEA2MultiObjectiveFitness is a subclass of Fitness which implements basic
 * multiobjective fitness functions along with support for the ECJ SPEA2
 * (Strength Pareto Evolutionary Algorithm) extensions.
 * 
 * <p>
 * The object contains two items: an array of floating point values representing
 * the various multiple fitnesses (ranging from 0.0 (worst) to infinity (best)),
 * and a single SPEA2 fitness value which represents the individual's overall
 * fitness ( a function of the number of individuals it dominates and it's raw
 * score where 0.0 is the best).
 * 
 * @author Robert Hubley (based on MultiObjectiveFitness by Sean Luke)
 * @version 1.0
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
        return super.fitnessToStringForHumans() + "\n" + "S=" + strength + " D=" + kthNNDistance + " " + SPEA2_FITNESS_PREAMBLE + fitness;
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
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        fitness = dataInput.readDouble();
        strength = dataInput.readDouble();
        fitness = dataInput.readDouble();
        kthNNDistance = dataInput.readDouble();
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
