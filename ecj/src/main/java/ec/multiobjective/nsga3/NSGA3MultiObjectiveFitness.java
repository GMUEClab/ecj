/*
  Copyright 2017 by Ben Brumbac
  Modifications Copyright 2017 Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective.nsga3;

import java.io.*;
import ec.util.Code;
import ec.multiobjective.MultiObjectiveFitness;
import ec.EvolutionState;
import ec.Fitness;
import java.util.*;

/* 
 * NSGA3MultiObjectiveFitness.java
 * 
 * Created: Sat Jan 20 2018
 * By: Ben Brumback and Sean Luke
 */

/**
 * NSGA3MultiObjectiveFitness is a subclass of MultiObjeciveFitness which
 * adds auxiliary fitness measures (normalizedFitness, rank) largely used by MultiObjectiveStatistics.
 * It also redefines the comparison measures to compare based on rank.
 *
 */
 
/* 
 * NOTE: normalizedFitness could probably use some double checking to make sure it has all the functions that make sense.
 */

public class NSGA3MultiObjectiveFitness extends MultiObjectiveFitness
    {
        
    public static final String NSGA3_RANK_PREAMBLE = "Rank: ";

    public String[] getAuxilliaryFitnessNames() { return new String[] { "Rank"}; }
    public double[] getAuxilliaryFitnessValues() { return new double[] { rank }; }
        
    /** Pareto front rank measure (lower ranks are better) */
    public int rank;
        
    // This is used in NSGA-3
    public ArrayList<Double> normalizedFitness;
        
    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + NSGA3_RANK_PREAMBLE + Code.encode(rank);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + NSGA3_RANK_PREAMBLE + rank;
        }

    public void readFitness(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        super.readFitness(state, reader);
        rank = Code.readIntegerWithPreamble(NSGA3_RANK_PREAMBLE, state, reader);
        }

    public void writeFitness(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state, dataOutput);
        dataOutput.writeInt(rank);
        writeTrials(state, dataOutput);
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        rank = dataInput.readInt();
        readTrials(state, dataInput);
        }
        
    //Below are the normized fitness functions which are used in NSGA-3
        
    public void initNorm()
        {
        normalizedFitness = new ArrayList<Double>();
        }
        
    public void initNorm(int length)
        {
        normalizedFitness = new ArrayList<Double>(length);
        for(int i = 0; i < length; i++)
            normalizedFitness.add(0.0);
        }
        
    public ArrayList<Double> getNormFit()
        {
        return normalizedFitness;
        }
        
    public void setNormFit(ArrayList<Double> normFit)
        {
        normalizedFitness = normFit;
        }
        
    public void setNormValue(int position, Double value)
        {
        normalizedFitness.set(position, value);
        }

    public boolean equivalentTo(Fitness _fitness)
        {
        NSGA3MultiObjectiveFitness other = (NSGA3MultiObjectiveFitness) _fitness;
        return (rank == ((NSGA3MultiObjectiveFitness) _fitness).rank);
        }

    /**
     * We specify the tournament selection criteria and Rank (lower
     * values are better)
     */
         
    // I think this selection needs to be redone? Im not sure
    public boolean betterThan(Fitness _fitness)
        {
        NSGA3MultiObjectiveFitness other = (NSGA3MultiObjectiveFitness) _fitness;
        // Rank should always be minimized.
        if (rank < ((NSGA3MultiObjectiveFitness) _fitness).rank)
            return true;
        else return (rank > ((NSGA3MultiObjectiveFitness) _fitness).rank);
        }
    }
