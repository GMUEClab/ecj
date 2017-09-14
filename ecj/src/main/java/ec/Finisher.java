/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

/* 
 * Finisher.java
 * 
 * Created: Tue Aug 10 21:09:18 1999
 * By: Sean Luke
 */

/**
 * Finisher is a singleton object which is responsible for cleaning up a
 * population after a run has completed.  This is typically done after
 * final statistics have been performed but before the exchanger's
 * contacts have been closed.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Finisher implements Singleton
    {
    /** Cleans up the population after the run has completed. result is either ec.EvolutionState.R_SUCCESS or ec.EvolutionState.R_FAILURE, indicating whether or not an ideal individual was found. */
    public abstract void finishPopulation(EvolutionState state, int result);
    }
