/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.Fitness;
import ec.EvolutionState;
import ec.util.*;
import java.io.*;

/*
 * SimpleFitness.java
 *
 * Created: Tue Aug 10 20:10:42 1999
 * By: Sean Luke
 */

/**
 * A simple default fitness, consisting of a double floating-point value
 * where fitness A is superior to fitness B if and only if A > B.  
 * Fitness values may range from (-infinity,infinity) exclusive -- that is,
 * you may not have infinite fitnesses.  
 *
 * <p>Some kinds of selection methods require a more stringent definition of
 * fitness.  For example, FitProportionateSelection requires that fitnesses
 * be non-negative (since it must place them into a proportionate distribution).
 * You may wish to restrict yourself to values in [0,1] or [0,infinity) in
 * such cases.
 *
 <p><b>Default Base</b><br>
 simple.fitness

 * @author Sean Luke
 * @version 1.0
 */

public class SimpleFitness extends Fitness
    {
    private double fitness;
    protected boolean isIdeal;

    public Parameter defaultBase()
        {
        return SimpleDefaults.base().push(P_FITNESS);
        }

    /**
       Deprecated -- now redefined to set the fitness but ALWAYS say that it's not ideal.
       If you need to specify that it's ideal, you should use the new function 
       setFitness(final EvolutionState state, double _f, boolean _isIdeal).
       @deprecated
    */
    public void setFitness(final EvolutionState state, double _f)
        {
        setFitness(state,_f,false);
        }
        
    public void setFitness(final EvolutionState state, double _f, boolean _isIdeal)
        {
        // we now allow f to be *any* value, positive or negative
        if (_f >= Double.POSITIVE_INFINITY || _f <= Double.NEGATIVE_INFINITY || Double.isNaN(_f))
            {
            state.output.warning("Bad fitness: " + _f + ", setting to 0.");
            fitness = 0;
            }
        else fitness = _f;
        isIdeal = _isIdeal;
        }

    public double fitness()
        {
        return fitness;
        }

    public void setup(final EvolutionState state, Parameter base) 
        {
        super.setup(state,base);  // unnecessary but what the heck
        }

    public boolean isIdealFitness()
        {
        return isIdeal;
        }

    public boolean equivalentTo(final Fitness _fitness)
        {
        return ((SimpleFitness)_fitness).fitness() == fitness();
        }

    public boolean betterThan(final Fitness _fitness)
        {
        return ((SimpleFitness)_fitness).fitness() < fitness();
        }

    public String fitnessToString()
        {
        return FITNESS_PREAMBLE + Code.encode(fitness());
        }
        
    public String fitnessToStringForHumans()
        {
        return FITNESS_PREAMBLE + fitness();
        }

    /** Presently does not decode the fact that the fitness is ideal or not */
    public void readFitness(final EvolutionState state, 
        final LineNumberReader reader)
        throws IOException
        {
        setFitness(state, Code.readDoubleWithPreamble(FITNESS_PREAMBLE, state, reader));
        }

    public void writeFitness(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeDouble(fitness);
        dataOutput.writeBoolean(isIdeal);
        writeTrials(state, dataOutput);
        }

    public void readFitness(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        fitness = dataInput.readDouble();
        isIdeal = dataInput.readBoolean();
        readTrials(state, dataInput);
        }

    public void setToMeanOf(EvolutionState state, Fitness[] fitnesses)
        {
        // this is not numerically stable.  Perhaps we should have a numerically stable algorithm for sums
        // we're presuming it's not a very large number of elements, so it's probably not a big deal,
        // since this function is meant to be used mostly for gathering trials together.
        double f = 0;
        boolean ideal = true;
        for(int i = 0; i < fitnesses.length; i++)
            {
            SimpleFitness fit = (SimpleFitness)(fitnesses[i]);
            f += fit.fitness;
            ideal = ideal && fit.isIdeal;
            }
        f /= fitnesses.length;
        fitness = (double)f;
        isIdeal = ideal;
        }
    }
