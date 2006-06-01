/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.EvolutionState;
import ec.Population;
import ec.Exchanger;
import ec.util.Parameter;
import ec.steadystate.*;

/* 
 * SimpleExchanger.java
 * 
 * Created: Tue Aug 10 21:59:17 1999
 * By: Sean Luke
 */

/**
 * A SimpleExchanger is a default Exchanger which, well, doesn't do anything.
 * Most applications don't need Exchanger facilities; this simple version
 * will suffice.
 * 
 * <p>The SimpleExchanger implements the SteadyStateExchangerForm, mostly
 * because it does nothing with individuals.  For this reason, it is final;
 * implement your own Exchanger if you need to do something more advanced.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public final class SimpleExchanger extends Exchanger implements SteadyStateExchangerForm
    {
    public void setup(final EvolutionState state, final Parameter base) { }

    /** Doesn't do anything. */
    public void initializeContacts(final EvolutionState state)
        {
        // don't care
        return;
        }

    /** Doesn't do anything. */
    public void reinitializeContacts(final EvolutionState state)
        {
        // don't care
        return;
        }

    /** Simply returns state.population. */
    public Population preBreedingExchangePopulation(final EvolutionState state)
        {
        // don't care
        return state.population;
        }

    /** Simply returns state.population. */
    public Population postBreedingExchangePopulation(final EvolutionState state)
        {
        // don't care
        return state.population;
        }

    /** Doesn't do anything. */
    public void closeContacts(final EvolutionState state, final int result)
        {
        // don't care
        return;
        }

    /** Always returns null */
    public String runComplete(final EvolutionState state)
        {
        return null;
        }

    }
