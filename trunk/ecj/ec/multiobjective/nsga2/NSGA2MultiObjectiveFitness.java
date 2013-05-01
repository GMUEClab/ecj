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
 * By: Faisal Abidi and Sean Luke
 */

/**
 * NSGA2MultiObjectiveFitness is a subclass of MultiObjeciveFitness which
 * adds auxiliary fitness measures (sparsity, rank) largely used by MultiObjectiveStatistics.
 * It also redefines the comparison measures to compare based on rank, and break ties
 * based on sparsity. 
 *
 */

public class NSGA2MultiObjectiveFitness extends MultiObjectiveFitness
    {
    public static final String NSGA2_RANK_PREAMBLE = "Rank: ";
    public static final String NSGA2_SPARSITY_PREAMBLE = "Sparsity: ";

    public String[] getAuxilliaryFitnessNames() { return new String[] { "Rank", "Sparsity" }; }
    public double[] getAuxilliaryFitnessValues() { return new double[] { rank, sparsity }; }
        
    /** Pareto front rank measure (lower ranks are better) */
    public int rank;

    /** Sparsity along front rank measure (higher sparsity is better) */
    public double sparsity;

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + NSGA2_RANK_PREAMBLE + Code.encode(rank) + "\n" + NSGA2_SPARSITY_PREAMBLE + Code.encode(sparsity);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + NSGA2_RANK_PREAMBLE + rank + "\n" + NSGA2_SPARSITY_PREAMBLE + sparsity;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        super.readFitness(state, reader);
        rank = Code.readIntegerWithPreamble(NSGA2_RANK_PREAMBLE, state, reader);
        sparsity = Code.readDoubleWithPreamble(NSGA2_SPARSITY_PREAMBLE, state, reader);
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state, dataOutput);
        dataOutput.writeInt(rank);
        dataOutput.writeDouble(sparsity);
        writeTrials(state, dataOutput);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        rank = dataInput.readInt();
        sparsity = dataInput.readDouble();
        readTrials(state, dataInput);
        }

    public boolean equivalentTo(Fitness _fitness)
        {
        NSGA2MultiObjectiveFitness other = (NSGA2MultiObjectiveFitness) _fitness;
        return (rank == ((NSGA2MultiObjectiveFitness) _fitness).rank) &&
            (sparsity == other.sparsity);
        }

    /**
     * We specify the tournament selection criteria, Rank (lower
     * values are better) and Sparsity (higher values are better)
     */
    public boolean betterThan(Fitness _fitness)
        {
        NSGA2MultiObjectiveFitness other = (NSGA2MultiObjectiveFitness) _fitness;
        // Rank should always be minimized.
        if (rank < ((NSGA2MultiObjectiveFitness) _fitness).rank)
            return true;
        else if (rank > ((NSGA2MultiObjectiveFitness) _fitness).rank)
            return false;
                
        // otherwise try sparsity
        return (sparsity > other.sparsity);
        }
    }
