/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;

/* 
 * SteadyStateExchangerForm
 * 
 * Created: Tue Aug 10 21:59:17 1999
 * By: Sean Luke
 */

/**
 * The SteadyStateExchangerForm is a badge which Exchanger subclasses
 * may wear if they work properly with the SteadyStateEvolutionState
 * mechanism.  The basic thing such classes must remember to do is:
 * Remember to call state.breeder.individualsReplaced(...) if
 * you modify or replace any individuals in a subpopulation.  Also,
 * realize that any individuals you exchange in will not be checked
 * to see if they're the ideal individual
 * @author Sean Luke
 * @version 1.0 
 */

public interface SteadyStateExchangerForm
    {
    }
