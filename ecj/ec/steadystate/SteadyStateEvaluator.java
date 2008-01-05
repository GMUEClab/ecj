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
    /** Holds the individuals that have been evaluated, mapped as:
	Individual -> QueueIndividual(Individual, subpopulation).
	We implement it as a LinkedHashMap rather than a Linked List to
	enable us to do ~O(1) removals in the asynchronous form of 
	getNextEvaluatedIndividual.  We don't use a plain HashMap in order
	to enable ~O(1) discovery of the "first" (indeed, any arbitrary) object
	in the Map in the NON-asynchronous form of getNextEvaluatedIndividual.  */ 
    LinkedHashMap queue;
    
    /** Our problem. */
    SimpleProblemForm problem; 
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        queue = new LinkedHashMap(); 
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
        problem.evaluate(state, ind, 0);
        queue.put(ind, new QueueIndividual(ind, subpop));
        }
    
    /** Returns true if we're ready to evaluate an individual.  Ordinarily this is ALWAYS true,
        except in the asynchronous evolution situation, where we may not have a processor ready yet. */
    public boolean canEvaluate() 
        {
        return problem.canEvaluate(); 
        }
        
    /** Returns true if an evaluated individual is in the queue and ready to come back to us.  
        Ordinarily this is ALWAYS true at the point that we call it, except in the asynchronous 
        evolution situation, where we may not have a job completed yet. */
    public boolean isNextEvaluatedIndividualAvailable()
    {
		if (problem instanceof MasterProblem) 
			return (((MasterProblem)problem).server.slaveMonitor.getNumberEvaluatedIndividuals() != 0); 
		
        else return (queue.size() != 0);   // in non-asynchronous, an individual is always ready to go...
        }
        
    /** Returns the QueueIndividual from the front of the queue. Assumes the user already knows that the queue is not empty */
    public QueueIndividual getNextEvaluatedIndividual()
    {
	Individual ind;
	if (problem instanceof MasterProblem)
	    // pull out the individual and look for its [ind,subpop] combination and return that -- the subpop
	    // is important to our customers but slaveMonitor doesn't provide it.
	    ind = ((MasterProblem)problem).server.slaveMonitor.getEvaluatedIndividual();
        else
	    // just get an arbitrary individual
	    ind = (Individual)(queue.keySet().iterator().next());
	return (QueueIndividual)(queue.remove(ind));
    }
    }

/** Private data structure to augment ec.Individual with the corresponding subpopulation.  You should not use this class.  */ 
class QueueIndividual 
    { 
    Individual ind;
    int subpop; 
    public  QueueIndividual(Individual i, int s)
        {
        ind = i; 
        subpop=s; 
        }
    };
