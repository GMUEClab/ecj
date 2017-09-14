/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


                
package ec.steadystate;
import ec.simple.*;
import ec.*;
import ec.util.Parameter;
import java.util.*; 
import ec.eval.MasterProblem;

/* 
 * SteadyStateEvaluator.java
 * 
 */

/**
 * This subclass of Evaluator performs the evaluation portion of Steady-State Evolution and (in distributed form)
 * Asynchronous Evolution. The procedure is as follows.  We begin with an empty Population and one by
 * one create new Indivdiuals and send them off to be evaluated.  In basic Steady-State Evolution the
 * individuals are immediately evaluated and we wait for them; but in Asynchronous Evolution the individuals are evaluated
 * for however long it takes and we don't wait for them to finish.  When individuals return they are
 * added to the Population until it is full.  No duplicate individuals are allowed.
 *
 * <p>At this point the system switches to its "steady state": individuals are bred from the population
 * one by one, and sent off to be evaluated.  Once again, in basic Steady-State Evolution the
 * individuals are immediately evaluated and we wait for them; but in Asynchronous Evolution the individuals are evaluated
 * for however long it takes and we don't wait for them to finish.  When an individual returns, we
 * mark an individual in the Population for death, then replace it with the new returning individual.
 * Note that during the steady-state, Asynchronous Evolution could be still sending back some "new" individuals
 * created during the initialization phase, not "bred" individuals.
 *
 * <p>The determination of how an individual is marked for death is done by the SteadyStateBreeder.
 *
 * <p>When SteadyStateEvaluator sends indivduals off to be evaluated, it stores them in an internal queue, along
 * with the subpopulation in which they were destined.  This tuple is defined by QueueIndividual.java
 * 
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SteadyStateEvaluator extends SimpleEvaluator
    {
    LinkedList queue = new LinkedList();
    
    /** Holds the subpopulation currently being evaluated.  */ 
    int subpopulationBeingEvaluated = -1;

    /** Our problem. */
    SimpleProblemForm problem; 
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        if (!cloneProblem)
            state.output.fatal("cloneProblem must be true for SteadyStateEvaluator -- we'll use only one Problem anyway.");
        }
        
    public void prepareToEvaluate(EvolutionState state, int thread) 
        {
        problem = (SimpleProblemForm)p_problem.clone();
                
        /* 
           We only call prepareToEvaluate during Asynchronous Evolution.
        */
        if (problem instanceof MasterProblem) 
            ((MasterProblem)problem).prepareToEvaluate(state, thread); 
        }
        
    /** Submits an individual to be evaluated by the Problem, and adds it and its subpopulation to the queue. */
    public void evaluateIndividual(final EvolutionState state, Individual ind, int subpop)
        {
        problem.evaluate(state, ind, subpop, 0);
        queue.addLast(new QueueIndividual(ind, subpop));
        }
    
    /** Returns true if we're ready to evaluate an individual.  Ordinarily this is ALWAYS true,
        except in the asynchronous evolution situation, where we may not have a processor ready yet. */
    public boolean canEvaluate() 
        {
        if (problem instanceof MasterProblem)
            return ((MasterProblem)problem).canEvaluate();
        else return true;
        }
        
    /** Returns an evaluated individual is in the queue and ready to come back to us.  
        Ordinarily this is ALWAYS true at the point that we call it, except in the asynchronous 
        evolution situation, where we may not have a job completed yet, in which case NULL is
        returned. Once an individual is returned by this function, no other individual will
        be returned until the system is ready to provide us with another one.  NULL will
        be returned otherwise.  */
    public Individual getNextEvaluatedIndividual(EvolutionState state)
        {
        QueueIndividual qind = null;
        
        if (problem instanceof MasterProblem)
            {
            if (((MasterProblem)problem).evaluatedIndividualAvailable())
                qind = ((MasterProblem)problem).getNextEvaluatedIndividual();
            }
        else
            {
            qind = (QueueIndividual)(queue.removeFirst());
            }
        
        if (qind == null) return null;
        
        subpopulationBeingEvaluated = qind.subpop;
        state.incrementEvaluations(1);
        return qind.ind;
        }
    
    /** Returns the subpopulation of the last evaluated individual returned by getNextEvaluatedIndividual, or potentially -1 if
        getNextEvaluatedIndividual was never called or hasn't returned an individual yet. */
    public int getSubpopulationOfEvaluatedIndividual()
        {
        return subpopulationBeingEvaluated;
        }
        
    /** The SimpleEvaluator determines that a run is complete by asking
        each individual in each population if he's optimal; if he 
        finds an individual somewhere that's optimal,
        he signals that the run is complete. */
    public boolean isIdealFitness(final EvolutionState state, final Individual ind)
        {
        return (ind.fitness.isIdealFitness());
        }

    }


