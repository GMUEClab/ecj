/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;

/**
 * EvaluationData.java
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

public class EvaluationData
    {
    // the evolution state
    EvolutionState state;

    // the problem
    MasterProblem mp;

    // the thread number (probably of no consequence, as the random number generator cannot be
    // set to its previous state
    int threadnum;

    // either Slave.V_EVALUATESIMPLE or Slave.V_EVALUATEGROUPED
    int type;

    // for Slave.V_EVALUATESIMPLE
    int subPopNum;
    Individual ind;

    // for Slave.V_EVALUATEGROUPED
    int length;
    int[] subPops;
    boolean countVictoriesOnly;
    Individual[] inds;
    boolean[] updateFitness;
    int index; // how many have been read back from the slaves
    }
