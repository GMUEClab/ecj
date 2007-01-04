/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.koza;
import ec.util.*;
import ec.*;
import java.io.*;

/* 
 * KozaFitness.java
 * 
 * Created: Fri Oct 15 14:26:44 1999
 * By: Sean Luke
 */

/**
 * KozaFitness is a Fitness which stores an individual's fitness as described in
 * Koza I.  Well, almost.  In KozaFitness, standardized fitness and raw fitness
 * are considered the same (there are different methods for them, but they return
 * the same thing).  Standardized fitness ranges from 0.0 inclusive (the best)
 * to infinity exclusive (the worst).  Adjusted fitness converts this, using
 * the formula adj_f = 1/(1+f), into a scale from 0.0 exclusive (worst) to 1.0
 * inclusive (best).  While it's the standardized fitness that is stored, it
 * is the adjusted fitness that is printed out.
 * This is all just convenience stuff anyway; selection methods
 * generally don't use these fitness values but instead use the betterThan
 * and equalTo methods.
 *
 <p><b>Default Base</b><br>
 gp.koza.fitness
 *
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class KozaFitness extends Fitness
    {
    public static final String P_KOZAFITNESS = "fitness";

    /** This ranges from 0 (best) to infinity (worst).  Koza leaves the
        exact definition of rawFitness up to the domain problem, but I
        define it here as equivalent to the standardized fitness, hence
        the simple definitions of rawFitness() and standardizedFitness() 
        below. */
    protected float fitness;

    /** This auxillary measure is used in some problems for additional
        information.  It's a traditional feature of Koza-style GP, and so
        although I think it's not very useful, I'll leave it in anyway. */
    public int hits;

    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_KOZAFITNESS);
        }
        
    /**
       Do not use this function.  Use the identical setStandardizedFitness() instead.
       The reason for the name change is that fitness() returns a differently-defined
       value than setFitness() sets, ugh.
       @deprecated
    */
    public final void setFitness(final EvolutionState state, final float _f)
        {
        setStandardizedFitness(state,_f);
        }

    /** Set the standardized fitness in the half-open interval [0.0,infinity)
        which is defined (NOTE: DIFFERENT FROM fitness()!!!) as 0.0 
        being the IDEAL and infinity being worse than the worst possible.
        This is the GP tradition.  The fitness() function instead will output
        the equivalent of Adjusted Fitness.
    */
    public final void setStandardizedFitness(final EvolutionState state, final float _f)
        {
        if (_f < 0.0f || _f == Float.POSITIVE_INFINITY || Float.isNaN(_f))
            {
            state.output.warning("Bad fitness (may not be < 0, NaN, or infinity): " + _f  + ", setting to 0.");
            fitness = 0;
            }
        else fitness = _f;
        }

    /** Returns the adjusted fitness metric, which recasts the
        fitness to the half-open interval (0,1], where 1 is ideal and
        0 is worst.  Same as adjustedFitness().  */

    public final float fitness()
        {
        return 1.0f/(1.0f+fitness);     
        }

    /** Returns the raw fitness metric.  */

    public final float rawFitness()
        {
        return fitness;
        }

    /** Returns the standardized fitness metric, which is the same as the
        raw fitness metric in this scheme. */

    public final float standardizedFitness()
        {
        return fitness;
        }

    /** Returns the adjusted fitness metric, which recasts the fitness
        to the half-open interval (0,1], where 1 is ideal and 0 is worst.
        This metric is used when printing the fitness out. */

    public final float adjustedFitness()
        {
        return 1.0f/(1.0f+fitness);
        }

    public void setup(final EvolutionState state, final Parameter base) { }
    
    public final boolean isIdealFitness()
        {
        return fitness == 0.0f;
        }
    
    public boolean equivalentTo(final Fitness _fitness)
        {
        return ((KozaFitness)_fitness).fitness == fitness;
        }

    public boolean betterThan(final Fitness _fitness)
        {
        return ((KozaFitness)_fitness).fitness > fitness;  // note different from SimpleFitness
        }
 
    public String fitnessToString()
        {
        return FITNESS_PREAMBLE + Code.encode(fitness) + Code.encode(hits);
        }
        
    public String fitnessToStringForHumans()
        {
        return FITNESS_PREAMBLE + "Raw=" + fitness + " Adjusted=" + adjustedFitness() + " Hits=" + hits;
        }
            
    public final void readFitness(final EvolutionState state, 
                                  final LineNumberReader reader)
        throws IOException
        {
        DecodeReturn d = Code.checkPreamble(FITNESS_PREAMBLE, state, reader);
        
        // extract fitness
        Code.decode(d);
        if (d.type!=DecodeReturn.T_FLOAT)
            state.output.fatal("Reading Line " + d.lineNumber + ": " +
                               "Bad Fitness.");
        fitness = (float)d.d;
        
        // extract hits
        Code.decode(d);
        if (d.type!=DecodeReturn.T_INT)
            state.output.fatal("Reading Line " + d.lineNumber + ": " +
                               "Bad Fitness.");
        hits = (int)d.l;
        }

    public void writeFitness(final EvolutionState state,
                             final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeFloat(fitness);
        dataOutput.writeInt(hits);
        }

    public void readFitness(final EvolutionState state,
                            final DataInput dataInput) throws IOException
        {
        fitness = dataInput.readFloat();
        hits = dataInput.readInt();
        }


    }
