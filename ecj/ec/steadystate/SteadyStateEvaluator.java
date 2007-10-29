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
        
    public void evaluateIndividual(final EvolutionState state, Individual ind, int subpop)
        {
        problem.evaluate(state, ind, 0);
        addNextEvaluatedIndividual(ind, subpop); 
        }
        
    public boolean canEvaluate() 
        {
        return problem.canEvaluate(); 
        }
        
    public boolean isNextEvaluatedIndividualAvailable()
        {
        return (queue.size() != 0); 
        }
        
    /** Returns the QueueIndividual from the front of the queue. Assumes the user already knows that the queue is not empty */
    public QueueIndividual getNextEvaluatedIndividual()
        {
        return (QueueIndividual)queue.removeFirst(); 
        }
        
    /** Adds the individual and corresponding subpopulation to the queue */ 
    public void addNextEvaluatedIndividual (Individual ind, int subpop) 
        {
        QueueIndividual q = new QueueIndividual(ind, subpop);
        queue.addLast(q); 
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
