/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;
import java.io.*;
import ec.util.*;

/**
 * Job.java
 *

 This class stores information that is necessary to reschedule jobs when a slave crashes.
 Jobs are of two types: traditional evaluations (Slave.V_EVALUATESIMPLE), and coevolutionary
 evaluations (Slave.V_EVALUATEGROUPED).  <i>type</i> indicates the (duh!) type of job.
 For traditional evaluations, only the individual and its subpopulation number are needed.  Instead,
 Coevolutionary evaluations require the number of individuals, the subpopulations they come from, the
 pointers to the individuals, boolean flags indicating whether their fitness is to be updated or
 not, and another boolean flag indicating whether to count only victories in competitive tournament.

 In addition, pointers to the evaluation state, the master problem, and the thread number are stored,
 as they are required for rescheduling the evaluation.

 * @author Liviu Panait
 * @version 1.0 
 */

public class Job
    {
    // either Slave.V_EVALUATESIMPLE or Slave.V_EVALUATEGROUPED
    int type;

    Individual []inds;   // original individuals
    Individual []newinds;  // individuals that were returned -- may be different individuals!
    int []subPops; 
    boolean countVictoriesOnly;
    boolean[] updateFitness;
    boolean batchMode;
    
    void copyIndividualsForward()
	{
	if (newinds == null || newinds.length != inds.length)
	    newinds = new Individual[inds.length];
	for(int i=0; i < inds.length; i++)
	    {
	    newinds[i] = (Individual)(inds[i].clone());
	    }
	}
	
    // a ridiculous hack
    void copyIndividualsBack(EvolutionState state)
	{
	try
	    {
	    DataPipe p = new DataPipe();
	    DataInputStream in = p.input;
	    DataOutputStream out = p.output;
	    
	    for(int i = 0; i < inds.length; i++)
		{
		p.reset();
		newinds[i].writeIndividual(state, out);
		inds[i].readIndividual(state, in);
		}
		
	    newinds = null;
	    }
	catch (IOException e) 
	    { 
	    e.printStackTrace();
	    state.output.fatal("Caught impossible IOException in Job.copyIndividualsBack()");
	    }
	}
    }
