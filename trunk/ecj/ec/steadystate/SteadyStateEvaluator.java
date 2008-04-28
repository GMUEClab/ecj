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
    public Individual getNextEvaluatedIndividual()
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
        return qind.ind;
        }
    
    /** Returns the subpopulation of the last evaluated individual returned by getNextEvaluatedIndividual, or potentially -1 if
        getNextEvaluatedIndividual was never called or hasn't returned an individual yet. */
    public int getSubpopulationOfEvaluatedIndividual()
        {
        return subpopulationBeingEvaluated;
        }
    }
