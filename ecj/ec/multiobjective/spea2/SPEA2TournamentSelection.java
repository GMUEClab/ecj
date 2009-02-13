/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.*;
import ec.select.*;

/* 
 * SPEA2TournamentSelection.java
 * 
 * Created: Wed Jun 26 11:20:32 PDT 2002
 * By: Robert Hubley, Institute for Systems Biology
 *     (based on TournamentSelection.java by Sean Luke)
 */

/**
 * Following Zitzler's paper, this class performs binary tournament selection 
 * using individuals from the archive.  
 * 
 * Does a simple tournament selection, limited to the subpopulation it's
 * working in at the time and only within the boundry of the SPEA2 archive
 * (between 0-archiveSize).
 *
 * <p>NOTE: The SPEA2Breeder class leaves the individuals
 * vector with only archiveSize number indviduals.  
 * The archive is located at the LAST <code>archiveSize</code> positions of the population.
 * The rest of the positions are null.
 *
 * @author Robert Hubley (based on TournamentSelection by Sean Luke)
 * @version 1.0 
 */

// This all assumes that the archive is the LAST N INDIVIDUALS in the individuals array
public class SPEA2TournamentSelection extends TournamentSelection
    {
    public int produce(final int subpopulation,
        final EvolutionState state,
        final int thread)
        {
        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int archiveSize = ((SPEA2Subpopulation)state.population.subpops[subpopulation]).archiveSize; 
        int archiveStart = state.population.subpops[subpopulation].individuals.length - archiveSize;


        // the one change from TournamentSelection: we only pick individuals from the archive
        int i = archiveStart + state.random[thread].nextInt(archiveSize); 
        int bad = i;
        
        for (int x=1;x<size;x++)
            {
            int j = archiveStart + state.random[thread].nextInt(archiveSize); 
            if (pickWorst)
                { if (!(oldinds[j].fitness.betterThan(oldinds[i].fitness))) { bad = i; i = j; } else bad = j; }
            else
                { if (oldinds[j].fitness.betterThan(oldinds[i].fitness)) { bad = i; i = j;} else bad = j; }
            }
            
        if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
            i = bad;
        return i;
        }

    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 
        {
        int n = 1;
        if (n>max) n = max;
        if (n<min) n = min;

        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int archiveSize = ((SPEA2Subpopulation)state.population.subpops[subpopulation]).archiveSize; 
        int archiveStart = oldinds.length - archiveSize;

        for(int q = 0; q < n; q++)
            {
            // the one change from TournamentSelection: we only pick individuals from the archive
            int i = archiveStart + state.random[thread].nextInt(archiveSize); 
            int bad = i;
                
            for (int x=1;x<size;x++)
                {
                int j = archiveStart + state.random[thread].nextInt(archiveSize);              
                        
                if (pickWorst)
                    { if (!(oldinds[j].fitness.betterThan(oldinds[i].fitness)))  { bad = i; i = j; } else bad = j; }
                else
                    { if (oldinds[j].fitness.betterThan(oldinds[i].fitness))  { bad = i; i = j; } else bad = j; }
                }
            if (probabilityOfSelection != 1.0 && !state.random[thread].nextBoolean(probabilityOfSelection))
                i = bad;
            inds[start+q] = oldinds[i];  // note it's a pointer transfer, not a copy!
            }
        return n;
        }
    }
