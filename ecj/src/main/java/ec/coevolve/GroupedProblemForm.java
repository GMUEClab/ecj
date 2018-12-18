/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.coevolve;
import ec.*;

/**
 * GroupedProblemForm.java
 *

 <p>GroupedProblemForm is an interface which defines methods
 for Problems to implement simple coevolutionary evaluation.
 In particular, the evaluate method receives as parameters a
 set of individuals that need to be evaluated. An additional
 vector-parameter (updateFitness) marks which individual
 fitnesses need to be updated during the evaluation process.

 *
 * @author Sean Luke & Liviu Panait
 * @version 1.0
 */

public interface GroupedProblemForm
    {
    /** Set up the population <tt>pop</tt> (such as fitness information) prior to evaluation.
        Although this method is not static, you should not use it to write to any instance
        variables in the GroupedProblem instance; this is because it's possible that
        the instance used is in fact the prototype, and you will have no guarantees that
        your instance variables will remain valid during the evaluate(...) process.
        Do not assume that <tt>pop</tt> will be the same as <tt>state.pop</tt> -- it 
        may not.  <tt>state</tt> is only provided to give you access to EvolutionState
        features.   Typically you'd use this method to set the Fitness values of all
        Individuals to 0.
                
        <p> <i>countVictoriesOnly</i> will be set if Individuals' fitness is to be based on
        whether they're the winner of a test, instead of based on the specifics of the scores
        in the tests.  This really only happens for Single-Elimination Tournament 
        one-population competitive coevolution.

        <p> <i>prepareForFitnessAssessment</i> will indicate which subpopulations will have their
        fitness values updated this time around, during postprocessPopulation.  It may not be
        the same as updateFitness[] in evaluate(...).
                
        <p>If you are basing fitness on trials, this method should create the initial trials
        <b>if the prepareForFitnessAssessment[...] is true for that
        subpopulation</b>.
    */ 
    public void preprocessPopulation(final EvolutionState state, Population pop, final boolean[] prepareForFitnessAssessment, final boolean countVictoriesOnly);

    /** Finish processing the population (such as fitness information) after evaluation.
        Although this method is not static, you should not use it to write to any instance
        variables in the GroupedProblem instance; this is because it's possible that
        the instance used is in fact the prototype, and you will have no guarantees that
        your instance variables will remain valid during the evaluate(...) process.
        Do not assume that <tt>pop</tt> will be the same as <tt>state.pop</tt> -- it 
        may not.  <tt>state</tt> is only provided to give you access to EvolutionState
        features. 
                
        <p> <i>countVictoriesOnly</i> will be set if Individuals' fitness is to be based on
        whether they're the winner of a test, instead of based on the specifics of the scores
        in the tests.  This really only happens for Single-Elimination Tournament 
        one-population competitive coevolution.  If this is set, probably would leave the Fitnesses
        as they are here (they've been set and incremented in evaluate(...)), but if it's not set,
        you may want to set the Fitnesses to the maximum or average or the various trials
        performed. 
                
        <p> <i>assessFitness</i> will indicate which subpopulations should have their final
        fitness values assessed.  You should <b>not</b> clear the trials of individuals
        for which assessFitness[] is false.  Instead allow trials to accumulate and
        ultimately update the fitnesses later when the flag is set.  assessFitness[] may not be
        the same as updateFitness[] in evaluate(...).
        
        <p>Should return the number of individuals evaluated (not tested: but actually had their
        fitnesses modified -- or would have if the evaluated flag wasn't set).
    */
    public int postprocessPopulation(final EvolutionState state, Population pop, final boolean[] assessFitness, final boolean countVictoriesOnly);

    /** Evaluates the individuals found in ind together.  If updateFitness[i] is true,
        then you should use this evaluation to update the fitness of the individual in
        ind[i].  Individuals which are updated should have their fitnesses modified so
        that immediately after evaluation (and prior to postprocessPopulation(...) being
        called) individuals' fitnesses can be checked to see which is better than which.
        Do not assume that the individuals in <tt>ind</tt> will actually be in <tt>state.pop</tt>
        (they may not -- this method may be called at the end of a run to determine the
        best individual of the run in some kind of contest).
                
        <p> <i>countVictoriesOnly</i> will be set if Individuals' fitness is to be based on
        whether they're the winner of a test, instead of based on the specifics of the scores
        in the tests.  This really only happens for Single-Elimination Tournament 
        one-population competitive coevolution.  If this is set, you should increment the Fitness of the winner
        each time.  If it's not set, you should update Fitness as you see fit, then set
        the final Fitness in preprocessPopulation. 
    */
    public void evaluate(final EvolutionState state,
        final Individual[] ind,  // the individuals to evaluate together
        final boolean[] updateFitness,  // should this individuals' fitness be updated?
        final boolean countVictoriesOnly, // don't bother updating Fitness with socres, just victories
        final int[] subpops,
        final int threadnum);
    }





