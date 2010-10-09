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
    public static final String NSGA2_RANK_PREAMBLE = "Rank: ";
    public static final String NSGA2_SPARSITY_PREAMBLE = "Sparsity: ";

	public String[] getAuxilliaryFitnessNames() { return new String[] { "Rank", "Sparsity" }; }
	public double[] getAuxilliaryFitnessValues() { return new double[] { rank, sparsity }; }
	
    public int rank;
	public double sparsity;

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + NSGA2_RANK_PREAMBLE + Code.encode(rank) + "\n" + NSGA2_SPARSITY_PREAMBLE + Code.encode(sparsity);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + "R=" + rank + " S=" + sparsity;
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
        }

    public void readFitness(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        super.readFitness(state, dataInput);
        rank = dataInput.readInt();
        sparsity = dataInput.readDouble();
        }

    /**
     * This is where we specify the tournament selection criteria, Rank (lower
     * values are better) and Sparsity (higher valuesa are better)
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
