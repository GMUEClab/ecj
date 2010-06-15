/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.steadystate;

/* 
 * SteadyStateBSourceForm.java
 * 
 * Created: Sat Nov 20 17:00:18 1999
 * By: Sean Luke
 *
 * This form is required of all BreedingSources in a pipeline used in Steady-State Evolution.
 * It consists of two methods: <b>sourcesAreProperForm</b> which checks recursively to determine
 * that a BreedingPipeline's own sources are also using this interface; and <b>individualReplaced</b>,
 * which informs the BreedingSource that a new individual has entered into the population, displacing
 * another.  This is important because it may signal to SelectionMethods that they need to update their
 * statistics.
 *
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public interface SteadyStateBSourceForm
    {
    /** Called whenever an individual has been replaced by another
        in the population. */
    public void individualReplaced(final SteadyStateEvolutionState state,
        final int subpopulation,
        final int thread,
        final int individual);
    
    /** Issue an error (not a fatal -- we guarantee that callers
        of this method will also call exitIfErrors) if any
        of your sources, or <i>their</i> sources, etc., are not
        of SteadyStateBSourceForm.*/
    public void sourcesAreProperForm(final SteadyStateEvolutionState state);
    }
