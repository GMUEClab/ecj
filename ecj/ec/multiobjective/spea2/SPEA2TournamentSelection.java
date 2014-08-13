/*
  Portions copyright 2010 by Sean Luke, Robert Hubley, and George Mason University
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
 * Created: Sat Oct 16 11:24:43 EDT 2010
 * By: Sean Luke
 * Replaces earlier class by: Robert Hubley, with revisions by Gabriel Balan and Keith Sullivan
 */

/**
 * This is a special version of TournamentSelection which restricts the selection to only
 * the archive region (the top 'archiveSize' elements in the subpopulation).
 */

// This all assumes that the archive is the LAST N INDIVIDUALS in the individuals array
public class SPEA2TournamentSelection extends TournamentSelection
    {
    public int getRandomIndividual(int number, int subpopulation, EvolutionState state, int thread)
        {
        //Individual[] oldinds = state.population.subpops[subpopulation].individuals;
        int archiveSize = ((SimpleBreeder)(state.breeder)).numElites(state, subpopulation);
        int archiveStart = state.population.subpops[subpopulation].individuals.length - archiveSize;

        return archiveStart + state.random[thread].nextInt(archiveSize);
        }
    }
