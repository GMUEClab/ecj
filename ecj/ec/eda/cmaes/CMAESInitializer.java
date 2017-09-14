/*
  Copyright 2015 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eda.cmaes;
import java.util.ArrayList;

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
        for(int i = 0; i < p.subpops.size(); i++)
            {
            Individual[] oldInds = (Individual[]) p.subpops.get(i).individuals.toArray(new Individual[0]);
            if (p.subpops.get(i).species instanceof CMAESSpecies)
                {
                int lambda = (int)(((CMAESSpecies)p.subpops.get(i).species).lambda);
                if (lambda < oldInds.length)  // need to reduce
                    {
                    Individual[] newInds = new Individual[lambda];
                    System.arraycopy(oldInds, 0, newInds, 0, lambda);
                    oldInds = newInds;
                    }
                else if (lambda > oldInds.length)  // need to increase
                    {
                    Individual[] newInds = new Individual[lambda];
                    System.arraycopy(oldInds, 0, newInds, 0, oldInds.length);
                    for(int j = oldInds.length; j < lambda; j++)
                        newInds[j] =  p.subpops.get(i).species.newIndividual(state, thread);
                    oldInds = newInds;
                    }
                }
            else state.output.fatal("Species of subpopulation " + i + " is not a CMAESSpecies.  It's a " + p.subpops.get(i).species);
            p.subpops.get(i).individuals = new ArrayList<Individual>();
            for(int j = 0; j < oldInds.length; j++)
                p.subpops.get(i).individuals.add(oldInds[j]);    // yuck, but 1.5 doesn't have Arrays.asList
            }
                        
        return p;
        }
    }
