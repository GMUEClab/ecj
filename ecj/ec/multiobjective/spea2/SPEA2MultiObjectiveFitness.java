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

/* 
 * SPEA2MultiObjectiveFitness.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (based on MultiObjectiveFitness.java by Sean Luke)
 */

/**
 * SPEA2MultiObjectiveFitness is a subclass of Fitness which implements
 * basic multiobjective fitness functions along with support for the
 * ECJ SPEA2 (Strength Pareto Evolutionary Algorithm) extensions.
 *
 * <p>The object contains two items: an array of floating point values
 * representing the various multiple fitnesses (ranging from 0.0 (worst)
 * to infinity (best)), and a single SPEA2 fitness value which represents
 * the individual's overall fitness ( a function of the number of 
 * individuals it dominates and it's raw score where 0.0 is the best).

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-objectives</tt><br>
 (else)<tt>multi.num-objectives</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the number of fitnesses in the multifitness array)</td></tr>
 </table>

 * @author Robert Hubley (based on MultiObjectiveFitness by Sean Luke)
 * @version 1.0 
 */

public class SPEA2MultiObjectiveFitness extends MultiObjectiveFitness
    {
    public static final String SPEA2FIT_PREAMBLE = "SPEA2Fitness: ";

    /** SPEA2 overall fitness */
    public double SPEA2Fitness;   // F(i)

    /** SPEA2 strength (# of nodes it dominates) */
    public double SPEA2Strength;  // S(i)

    /** SPEA2 RAW fitness */
    public double SPEA2RawFitness;  // R(i)

    /** SPEA2 NN distance */
    public double SPEA2kthNNDistance;  // D(i)

    /** Returns the sum of the squared differences between the vector
        fitness values.
    */
    public float calcDistance(SPEA2MultiObjectiveFitness otherFit)
        {
        float s = 0;
        for (int i = 0; i < multifitness.length; i++)
            {
            s += (multifitness[i] - otherFit.multifitness[i]) *
                (multifitness[i] - otherFit.multifitness[i]);
            }
        return s;
        }

    public String fitnessToString()
        {
        return super.fitnessToString() + "\n" + SPEA2FIT_PREAMBLE + Code.encode(SPEA2Fitness);
        }

    public String fitnessToStringForHumans()
        {
        return super.fitnessToStringForHumans() + "\n" + 
            SPEA2FIT_PREAMBLE + "S=" + SPEA2Strength + " R=" + SPEA2RawFitness + 
            " D= " + SPEA2kthNNDistance + " F=" + SPEA2Fitness;
        }
        
    public void readFitness(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        {
        super.readFitness(state,reader);
        Code.readDoubleWithPreamble(SPEA2FIT_PREAMBLE, state, reader);
        // NOTE: At this time I am not reading/writing the SPEA2 strength, raw, 
        //       and distance values.  These are intermediate values to the 
        //       overall SPEA2Fitness and so are not really worth preserving.
        }


    public void writeFitness(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        super.writeFitness(state,dataOutput);
        dataOutput.writeDouble(SPEA2Fitness);
        dataOutput.writeDouble(SPEA2Strength);
        dataOutput.writeDouble(SPEA2RawFitness);
        dataOutput.writeDouble(SPEA2kthNNDistance);
        }

    public void readFitness(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        super.readFitness(state,dataInput);
        SPEA2Fitness = dataInput.readDouble();
        SPEA2Strength = dataInput.readDouble();
        SPEA2RawFitness = dataInput.readDouble();
        SPEA2kthNNDistance = dataInput.readDouble();
        }
    }
