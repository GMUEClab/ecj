/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.tutorial3;
import ec.*;
import ec.util.*;

public class OurSelection extends SelectionMethod
    {
    // We have to specify a default base
    public static final String P_OURSELECTION = "our-selection";
    public Parameter defaultBase() { return new Parameter(P_OURSELECTION); }

    public static final String P_MIDDLEPROBABILITY = "middle-probability";  // our parameter name

    public double middleProbability;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);   // always call super.setup(...) first if it exists!

        Parameter def = defaultBase();

        // gets a double between min (0.0) and max (1.0), from the parameter
        // database, returning a value of min-1 (-1.0) if the parameter doesn't exist or was 
        // outside this range.
        middleProbability = state.parameters.getDouble(base.push(P_MIDDLEPROBABILITY),
                                                       def.push(P_MIDDLEPROBABILITY),0.0,1.0);
        if (middleProbability < 0.0)
            state.output.fatal("Middle-Probability must be between 0.0 and 1.0",
                               base.push(P_MIDDLEPROBABILITY),def.push(P_MIDDLEPROBABILITY));
        } 

    public int produce(final int subpopulation, final EvolutionState state, final int thread)
        {
        //toss a coin
        if (state.random[thread].nextBoolean(middleProbability))
            {
            //pick three individuals, return the middle one
            Individual[] inds = state.population.subpops[subpopulation].individuals;
            int one = state.random[thread].nextInt(inds.length);
            int two = state.random[thread].nextInt(inds.length);
            int three = state.random[thread].nextInt(inds.length);
            // generally the betterThan(...) method imposes an ordering,
            // so you shouldn't see any cycles here except in very unusual domains...
            if (inds[two].fitness.betterThan(inds[one].fitness))
                {
                if (inds[three].fitness.betterThan(inds[two].fitness)) //  1 < 2 < 3
                    return two;
                else if (inds[three].fitness.betterThan(inds[one].fitness)) //  1 < 3 < 2
                    return three;
                else //  3 < 1 < 2
                    return one;
                }
            else if (inds[three].fitness.betterThan(inds[one].fitness)) //  2 < 1 < 3
                return one;
            else if (inds[three].fitness.betterThan(inds[two].fitness)) //  2 < 3 < 1
                return three;
            else //  3 < 2 < 1
                return two;
            }
        else        //select a random individual's index
            {
            return state.random[thread].nextInt(
                state.population.subpops[subpopulation].individuals.length);
            }
        }
    }  // close the class
