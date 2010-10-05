/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga2;

import java.io.*;
import ec.util.Code;
import ec.multiobjective.MultiObjectiveFitness;
import ec.EvolutionState;
import ec.Fitness;

/* 
 * NSGA2MultiObjectiveFitness.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi
 *     (based on MultiObjectiveFitness.java by Sean Luke)
 */

/**
 * NSGA2MultiObjectiveFitness is a subclass of Fitness which implements basic
 * multiobjective fitness functions along with support for the ECJ NSGA2
 * (Non-Dominated Sorting Genetic Algorithm) extensions.
 * 
 * <p>
 * The object contains three items: an array of floating point values
 * representing the various multiple fitnesses (ranging from 0.0 (worst) to
 * infinity (best) in the case of maximization); a Rank which represents the
 * pareto rank of the individual; and Sparsity value which represents a metric
 * for how far away the neighbors are (See member: <code>NSGA2Sparsity</code>).
 * 
 * @author Faisal Abidi (based on MultiObjectiveFitness by Sean Luke)
 * @version 1.0
 */

public class NSGA2MultiObjectiveFitness extends MultiObjectiveFitness
    {
    public static final String NSGA2FIT_PREAMBLE = "NSGA2Fitness: ";

    /** NSGA2 rank (# of the Pareto-Front rank it belongs to) */
    public int NSGA2Rank;

    /**
     * NSGA2 Sparsity (Manhattan Distance between 2 immediate neighbors in same
     * rank)
     */
    public double NSGA2Sparsity;

    /*
     * Determines whether this multiobjective fitness Pareto-dominates the other
     * multiobjective fitness.
     */
    public boolean paretoDominates(MultiObjectiveFitness otherFit)
        {
        // MultiobjectiveFitness.betterThan() is based on pareto domination
        return super.betterThan(otherFit);
        }

    /*
     * MultiObjectiveFitness other = otherFit; boolean abeatsb = false; if
     * (maximize != other.isMaximizing()) throw new RuntimeException(
     * "Attempt made to compare two multiobjective fitnesses; but one expects higher values to be better and the other expectes lower values to be better."
     * ); float[] other_objectives = other.getObjectives(); if
     * (objectives.length != other_objectives.length) throw new
     * RuntimeException(
     * "Attempt made to compare two multiobjective fitnesses; but they have different numbers of objectives."
     * ); if (maximize) { for (int x = 0; x < objectives.length; x++) { if
     * (objectives[x] > other_objectives[x]) abeatsb = true; if (objectives[x] <
     * other_objectives[x]) return false; } } else { for (int x = 0; x <
     * objectives.length; x++) { if (objectives[x] < other_objectives[x])
     * abeatsb = true; if (objectives[x] > other_objectives[x]) return false; }
     * } return abeatsb; }
     */

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + NSGA2FIT_PREAMBLE + Code.encode(NSGA2Rank);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + NSGA2FIT_PREAMBLE + "R=" + NSGA2Rank + " S= " + NSGA2Sparsity;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        super.readFitness(state, reader);
        Code.readDoubleWithPreamble(NSGA2FIT_PREAMBLE, state, reader);
        // NOTE: NSGA2 does not have a composite fitness value.
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state, dataOutput);
        dataOutput.writeInt(NSGA2Rank);
        dataOutput.writeDouble(NSGA2Sparsity);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        NSGA2Rank = dataInput.readInt();
        NSGA2Sparsity = dataInput.readDouble();
        }

    /**
     * This is where we specify the tournament selection criteria, Rank (lower
     * values are better) and Sparsity (higher valuesa are better)
     */
    public boolean betterThan(Fitness _fitness)
        {
        NSGA2MultiObjectiveFitness other = (NSGA2MultiObjectiveFitness) _fitness;
        // Rank should always be minimized.
        if (NSGA2Rank < ((NSGA2MultiObjectiveFitness) _fitness).NSGA2Rank)
            return true;
        else if (NSGA2Rank == ((NSGA2MultiObjectiveFitness) _fitness).NSGA2Rank)
            {
            // Sparsity should always be maximized.
            long f = Double.doubleToRawLongBits(NSGA2Sparsity);
            long g = Double.doubleToRawLongBits(other.NSGA2Sparsity);
            if (NSGA2Sparsity > other.NSGA2Sparsity)
                return true;
            }
        return false;
        }
    }
