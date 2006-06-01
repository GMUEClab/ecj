/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

/* 
 * Exchanger.java
 * 
 * Created: Tue Aug 10 21:59:17 1999
 * By: Sean Luke
 */

/**
 * The Exchanger is a singleton object whose job is to (optionally)
 * perform individual exchanges between subpopulations in the run,
 * or exchange individuals with other concurrent evolutionary run processes,
 * using sockets or whatever.  Keep in mind that other processes may go down,
 * or be started up from checkpoints, etc.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Exchanger implements Singleton
    {
    /** Initializes contacts with other processes, if that's what you're doing. Called at the beginning of an evolutionary run, before a population is set up. */
    public abstract void initializeContacts(EvolutionState state);

    /** Initializes contacts with other processes, if that's what you're doing.  Called after restarting from a checkpoint. */
    public abstract void reinitializeContacts(EvolutionState state);

    /** Performs exchanges after the population has been evaluated but before it has been bred,
        once every generation (or pseudogeneration). */
    public abstract Population preBreedingExchangePopulation(EvolutionState state);

    /** Performs exchanges after the population has been bred but before it has been evaluated,
        once every generation (or pseudogeneration). */
    public abstract Population postBreedingExchangePopulation(EvolutionState state);

    /** Called after preBreedingExchangePopulation(...) to evaluate whether or not
        the exchanger wishes the run to shut down (with ec.EvolutionState.R_FAILURE) --
        returns a String (which will be printed out as a message) if the exchanger
        wants to shut down, else returns null if the exchanger does NOT want to shut down.
        Why would you want to shut down?
        This would happen for two reasons.  First, another process might have found
        an ideal individual and the global run is now over.  Second, some network
        or operating system error may have occurred and the system needs to be shut
        down gracefully.  Note that if the exchanger wants to shut down, the system
        will shut down REGARDLESS of whether or not the user stated 
        ec.EvolutionState.quitOnRunComplete. */
    public abstract String runComplete(EvolutionState state);

    /** Closes contacts with other processes, if that's what you're doing.  Called at the end of an evolutionary run. result is either ec.EvolutionState.R_SUCCESS or ec.EvolutionState.R_FAILURE, indicating whether or not an ideal individual was found. */
    public abstract void closeContacts(EvolutionState state, int result);
    }
