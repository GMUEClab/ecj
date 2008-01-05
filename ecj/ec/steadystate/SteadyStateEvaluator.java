/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


                
package ec.steadystate;
import ec.simple.*;
import ec.*;
import ec.util.Parameter;
import java.util.LinkedList; 
import ec.eval.MasterProblem;

public class SteadyStateEvaluator extends SimpleEvaluator
{
    /** Holds the individuals that have been evaluated. */ 
    LinkedList queue; 
        
    SimpleProblemForm problem; 
        
    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);
        queue = new LinkedList(); 
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
        QueueIndividual q = new QueueIndividual(ind, subpop);
        queue.addLast(q); 
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
		
        return (queue.size() != 0); 
    }
        
    /** Returns the QueueIndividual from the front of the queue. Assumes the user already knows that the queue is not empty */
    public QueueIndividual getNextEvaluatedIndividual()
    {
		if (problem instanceof MasterProblem) { 
			Individual ind = (Individual)((MasterProblem)problem).server.slaveMonitor.getEvaluatedIndividual();
			QueueIndividual q = new QueueIndividual(ind,0);  
			for (int i=0; i < queue.size(); i++) { 
				QueueIndividual q1 = (QueueIndividual)queue.get(i); 
				if (q1.ind.equals(ind)) { 
					q.subpop = q1.subpop; 
					queue.remove(i); 
					break;
				}
			}
			return q; 
		}
        return (QueueIndividual)queue.removeFirst(); 
    }
}

/** Private data structure to augment ec.Individual with the corresponding subpopulation.  Even though its declared public 
    (so SteadyStateEvolutionState can access it), you should not use this class.  */ 
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
