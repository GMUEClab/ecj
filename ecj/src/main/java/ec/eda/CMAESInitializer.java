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
                {
                int lambda = (int)(((CMAESSpecies)p.subpops[i].species).lambda);
                if (lambda < p.subpops[i].individuals.length)  // need to reduce
                    {
                    Individual[] newInds = new Individual[(int)(((CMAESSpecies)p.subpops[i].species).lambda)];
                    System.arraycopy(p.subpops[i].individuals, 0, newInds, 0, lambda);
                    p.subpops[i].individuals = newInds;
                    }
                else if (lambda > p.subpops[i].individuals.length)  // need to increase
                    {
                    Individual[] newInds = new Individual[(int)(((CMAESSpecies)p.subpops[i].species).lambda)];
                    System.arraycopy(p.subpops[i].individuals, 0, newInds, 0, p.subpops[i].individuals.length);
                    for(int j = p.subpops[i].individuals.length; j < lambda; j++)
                        newInds[j] =  p.subpops[i].species.newIndividual(state, thread);
                    p.subpops[i].individuals = newInds;
                    }
                }
            else state.output.fatal("Species of subpopulation " + i + " is not a CMAESSpecies.  It's a " + p.subpops[i].species);

            state.output.message("Size of Subpopulation " + i + " changed to " + p.subpops[i].individuals.length);        
            }
                        
        return p;
        }
    }
