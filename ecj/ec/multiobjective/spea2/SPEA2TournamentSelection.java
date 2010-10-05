/*
  Copyright 2006 by Robert Hubley
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.multiobjective.spea2;
import ec.*;
import ec.select.*;
import ec.simple.*;

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
    public int getRandomIndividual(int number, int subpopulation, EvolutionState state, int thread)
        {
        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int archiveSize = ((SimpleBreeder)(state.breeder)).elite[subpopulation];
        int archiveStart = state.population.subpops[subpopulation].individuals.length - archiveSize;

        return archiveStart + state.random[thread].nextInt(archiveSize);
        }
    }
