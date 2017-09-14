/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;
import java.io.*;
import ec.util.*;
import java.util.*;

/**
 * Job.java
 *

 This class stores information regarding a job submitted to a Slave: the individuals,
 the subpopulations in which they are stored, a scratch array for the individuals used
 internally, and various coevolutionary information (whether we should only count victories
 single-elimination-tournament style; which individuals should have their fitnesses updated).
 
 <p>Jobs are of two types: traditional evaluations (Slave.V_EVALUATESIMPLE), and coevolutionary
 evaluations (Slave.V_EVALUATEGROUPED).  <i>type</i> indicates the type of job.
 For traditional evaluations, we may submit a group of individuals all at one time.  
 Only the individuals and their subpopulation numbers are needed. 
 Coevolutionary evaluations require the number of individuals, the subpopulations they come from, the
 pointers to the individuals, boolean flags indicating whether their fitness is to be updated or
 not, and another boolean flag indicating whether to count only victories in competitive tournament.

 * @author Liviu Panait
 * @version 1.0 
 */

public class Job
    {
    // either Slave.V_EVALUATESIMPLE or Slave.V_EVALUATEGROUPED
    int type;

    boolean sent = false;
    Individual[] inds;   // original individuals
    Individual[] newinds;  // individuals that were returned -- may be different individuals!
    int[] subPops; 
    boolean countVictoriesOnly;
    boolean[] updateFitness;
    
    void copyIndividualsForward()
        {
        if (newinds == null || newinds.length != inds.length)
            newinds = new Individual[inds.length];
        for(int i=0; i < inds.length; i++)
            {
            newinds[i] = (Individual)(inds[i].clone());
            // delete the trials since they'll get remerged
            newinds[i].fitness.trials = null;
            // delete the context, since it'll get remerged
            newinds[i].fitness.setContext(null);
            }
        }
        
    void copyIndividualsBack(EvolutionState state)
        {
        for(int i = 0; i < inds.length; i++)
            inds[i].merge(state, newinds[i]);
        newinds = null;
        }
    }
