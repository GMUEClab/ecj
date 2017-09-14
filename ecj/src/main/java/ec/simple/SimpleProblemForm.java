/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;

/* 
 * SimpleProblemForm.java
 * 
 * Created: Tue Mar  6 11:33:37 EST 2001
 * By: Sean Luke
 */

/**
 * SimpleProblemForm is an interface which defines methods
 * for Problems to implement simple, single-individual (non-coevolutionary)
 * evaluation.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface SimpleProblemForm
    {
    /** Evaluates the individual in ind, if necessary (perhaps
        not evaluating them if their evaluated flags are true),
        and sets their fitness appropriately. 
    */

    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum);

    /** "Reevaluates" an individual,
        for the purpose of printing out
        interesting facts about the individual in the context of the
        Problem, and logs the results.  This might be called to print out 
        facts about the best individual in the population, for example.  */
    
    public void describe(
        final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum,
        final int log);
    }
