package ec.eda.dovs;

import ec.*;
import ec.simple.*;
import ec.util.*;

/**
 * DOVSFitness is a subclass of Fitness which implements contains important
 * statistics about simulation results of the individual. These statistics will
 * be used to determine the total simulation number that are necessary for a
 * individual. And we hope after such number of simulations are done, we have
 * high confidence of the fitness value of the individual.
 * 
 * 
 * <p>
 * <b>Default Base</b><br>
 * dovs.fitness
 * 
 * @author Ermo Wei and David Freelan
 */

public class DOVSFitness extends SimpleFitness
    {
    /** Sum of the all the squared fitness value with all the evaluation. */
    public double sumSquared;

    /** Sum of the all the fitness value with all the evaluation. */
    public double sum;

    /** Mean fitness value of the current individual. */
    public double mean;

    /** Number of evaluation have been performed on this individual. */
    public int numOfObservations;

    /** Variance of the fitness value of the current individual. */
    public double variance;

    public void setup(final EvolutionState state, Parameter base)
        {
        super.setup(state, base); // unnecessary but what the heck

        sumSquared = 0;
        sum = 0;
        mean = 0;
        numOfObservations = 0;
        variance = 0;
        }

    /** Reset the fitness to initial status. */
    public void reset()
        {
        sumSquared = 0;
        sum = 0;
        mean = 0;
        numOfObservations = 0;
        variance = 0;
        }

    /** Return the number of simulation have done with current individual. */
    public int numOfObservations()
        {
        return numOfObservations;
        }

    /**
     * Record the result of the new simulation. This will update some of the
     * statistics of the current fitness value.
     */
    public double recordObservation(EvolutionState state, double result)
        {

        sum += result;
        sumSquared += result * result;
        numOfObservations++;
        mean = sum / numOfObservations;
        if (numOfObservations == 1)
            {
            variance = 0;
            }
        else
            {
            variance = (sumSquared - numOfObservations * mean * mean) / (numOfObservations - 1);
            }

        setFitness(state, mean, false);

        return mean;
        }
    }
