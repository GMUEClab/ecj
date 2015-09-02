/*
  Copyright 2015 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda;
import ec.*;
import ec.simple.*;

/* 
 * CMAESInitializer.java
 * 
 * Created: Wed Jul  8 12:35:31 EDT 2015
 * By: Sam McKay and Sean Luke
 */

/**
 * CMAESInitializer is a SimpleInitializer which ensures that the subpopulations are all set to the provided
 * or computed lambda values.
 *
 * @author Sam McKay and Sean Luke
 * @version 1.0 
 */

public class CMAESInitializer extends SimpleInitializer
    {
    private static final long serialVersionUID = 1;

    public Population setupPopulation(final EvolutionState state, int thread)
        {
        Population p = super.setupPopulation(state, thread);
        
        // reset to lambda in size!
        for(int i = 0; i < p.subpops.length; i++)
            {
            if (p.subpops[i].species instanceof CMAESSpecies)
                p.subpops[i].individuals = new Individual[(int)(((CMAESSpecies)p.subpops[i].species).lambda)];

            state.output.message("Size of Subpopulation " + i + " changed to " + p.subpops[i].individuals.length);        
            }
                        
        return p;
        }
    }
