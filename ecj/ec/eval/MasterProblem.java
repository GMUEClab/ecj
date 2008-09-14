/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;
import ec.util.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleProblemForm;
import ec.steadystate.QueueIndividual;
import java.util.ArrayList;

import java.io.*;

/**
 * MasterProblem.java
 *

 <p>The MasterProblem is a special ECJ problem that performs evaluations by sending them to
 a remote Slave process to be evaluated.  As it implements both the
 <i>SimpleProblemForm</i> and the <i>GroupedProblemForm</i> interfaces, the MasterProblem
 can perform both traditional EC evaluations as well as coevolutionary evaluations.
 
 <p>When a MasterProblem is specified by the Evaluator, the Problem is set up as usual, but then
 the MasterProblem replaces it.  The Problem is not garbage collected -- instead, it's hung off the
 MasterProblem's <tt>problem</tt> variable.  In some sense the Problem is "pushed aside".
 
 <p>If the Evaluator begins by calling prepareToEvaluate(), and we're not doing coevolution, then
 the MasterProblem does not evaluate individuals immediately.  Instead, it waits for at most 
 <i>jobSize</i> individuals be submitted via evaluate(), and then sends them all off in a group,
 called a <i>job</i>, to the remote slave.  In other situations (coevolution, or no prepareToEvaluate())
 the MasterProblem sends off individuals immediately.
 
 <p>It may be the case that no Slave has space in its queue to accept a new job containing, among others,
 your new individual.  In this case, calling evaluate() will block until one comes available.  You can avoid
 this by testing for availability first by calling canEvaluate().  Note that canEvaluate() and evaluate()
 together are not atomic and so you should not rely on this facility if your system uses multiple threads.
 
 <P>When the individuals or their fitnesses return, they are immediately updated in place.  You have three
 options to wait for them:
 
 <ul>
 <li><p>You can wait for all the individuals to finish evaluation by calling finishEvaluating().
 If you call this method before a job is entirely filled, it will be sent in truncated format (which
 generally is perfectly fine).  You then block until all the jobs have been completed and the individuals
 updated.
    
 <li><p>You can block until at least one individual is available, by calling getNextEvaluatedIndividual(),
 which blocks and then returns the individual that was just completed.
    
 <li><p>You can test in non-blocking fashion to see if an individual is available, by calling 
 evaluatedIndividualAvailable().  If this returns true, you may then call getNextEvaluatedIndividual()
 to get the individual.  Note that this isn't atomic, so don't use it if you have multiple threads.
 </ul>
  
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>debug-info</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether the system should display information useful for debugging purposes)<br>

 <tr><td valign=top><i>base.</i><tt>job-size</tt><br>
 <font size=-1>integer &gt; 0 </font></td>
 <td valign=top>(how large should a job be at most?)<br>
 </td></tr>


 <!-- technically these are handled by the SlaveMonitor -->

 <tr><td valign=top><tt>eval.master.port</tt><br>
 <font size=-1>int</font></td>
 <td valign=top>(the port where the slaves will connect)<br>
 </td></tr>
 <tr><td valign=top><tt>eval.compression</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether the communication with the slaves should be compressed or not)<br>
 </td></tr>
 <tr><td valign=top><tt>eval.masterproblem.max-jobs-per-slave</tt><br>
 <font size=-1>int</font></td>
 <td valign=top>(the maximum load (number of jobs) per slave at any point in time)<br>
 </td></tr>

 </table>

 * @author Liviu Panait, Keith Sullivan, and Sean Luke
 * @version 1.0 
 */

public class MasterProblem extends Problem implements SimpleProblemForm, GroupedProblemForm 
    {
    public static final String P_DEBUG_INFO = "debug-info";
    public static final String P_JOB_SIZE = "job-size";
        
    int jobSize;
    boolean showDebugInfo;
    public Problem problem;
    public boolean batchMode;
    public SlaveMonitor monitor;

    // except for the problem, everything else is shallow-cloned
    public Object clone()
        {
        MasterProblem c = (MasterProblem)(super.clone());

        // shallow-cloned stuff
        c.monitor = monitor;
        c.batchMode = batchMode;
        c.jobSize = jobSize; 
        c.showDebugInfo = showDebugInfo;

        // deep-cloned stuff
        c.problem = (Problem)(problem.clone());

        return c;
        }

    // setup
    public void setup(final EvolutionState state, final Parameter base) 
        {
        Thread.currentThread().setName("MainThread: ");
        super.setup(state, base);
        showDebugInfo = state.parameters.getBoolean(base.push(P_DEBUG_INFO),null,false);
                
        jobSize = state.parameters.getIntWithDefault(base.push(P_JOB_SIZE),null,1);
        if (jobSize<=0)
            state.output.fatal("The job size must be an integer > 0.", base.push(P_JOB_SIZE));

        batchMode = false;
        }

    // prepare for a batch of evaluations
    public void prepareToEvaluate(final EvolutionState state, final int threadnum)
        {
        if (jobSize > 1) queue = new ArrayList();
        batchMode = true;
        }

    // wait until a batch of evaluations is finished
    public void finishEvaluating(final EvolutionState state, final int threadnum)
        {
        if(showDebugInfo)
            state.output.message(Thread.currentThread().getName() + "Waiting for all slaves to finish.");
        flush(state, threadnum);
        queue = null;  // get rid of it just in case
                
        monitor.waitForAllSlavesToFinishEvaluating( state );
        batchMode = false;
        if(showDebugInfo)
            state.output.message(Thread.currentThread().getName() + "All slaves have finished their jobs.");
        }

    // evaluate a regular individual
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
        if (jobSize > 1 && batchMode == true)    // chunked evaluation mechanism
            {
            queue.add(new QueueIndividual(ind, subpopulation));
            if (queue.size() >= jobSize)
                flush(state, threadnum);
            }
        else    /// ordinary evaluation mechanism  
            evaluate(state, new Individual[] { ind }, new int[] { subpopulation }, threadnum);           
        }
        
        
    ArrayList queue;
    void flush(EvolutionState state, int threadnum)
        {
        int subpopulation;
        if (queue!=null && queue.size() > 0 )
            {
            Individual[] inds = new Individual[queue.size()];
            int[] subpopulations = new int[queue.size()];
            for(int i = 0; i < queue.size(); i++)
                {
                QueueIndividual qind = (QueueIndividual)(queue.get(i));
                inds[i] = qind.ind;
                subpopulations[i] = qind.subpop; 
                }
            evaluate(state, inds, subpopulations, threadnum);
            }
        queue = new ArrayList();
        }


    // send a group of individuals to one slave for evaluation 
    void evaluate(EvolutionState state, Individual inds[], int[] subpopulations, int threadnum)
        {
        if(showDebugInfo)
            state.output.message(Thread.currentThread().getName() + "Starting a " + (batchMode ? "batched " : "") + "SimpleProblemForm evaluation.");

        // Acquire a slave socket
        Job job = new Job();
        job.type = Slave.V_EVALUATESIMPLE;
        job.inds = inds;
        job.subPops = subpopulations ;
        job.updateFitness = new boolean[inds.length]; 
        for (int i=0 ; i < inds.length; i++) 
            job.updateFitness[i]=true; 
        monitor.scheduleJobForEvaluation(state,job);
        if( !batchMode )
            monitor.waitForAllSlavesToFinishEvaluating( state );
        if(showDebugInfo) state.output.message(Thread.currentThread().getName() + "Finished a " + (batchMode ? "batched " : "") + "SimpleProblemForm evaluation.");
        }
        
        
        

    /* (non-Javadoc)
     * @see ec.simple.SimpleProblemForm#describe(ec.Individual, ec.EvolutionState, int, int, int)
     */
    public void describe(Individual ind, EvolutionState state, int subpopulation, int threadnum, 
        int log, int verbosity) 
        {
        if (!(problem instanceof SimpleProblemForm)) 
            {
            state.output.fatal("MasterProblem.describe(...) invoked, but the underlying Problem is not of SimpleProblemForm");
            }
                
        ((SimpleProblemForm)problem).describe( ind, state, subpopulation, threadnum, log, verbosity);
        }

    /* (non-Javadoc)
     * @see ec.coevolve.GroupedProblemForm#preprocessPopulation(ec.EvolutionState, ec.Population)
     */
    public void preprocessPopulation(EvolutionState state, Population pop) 
        {
        if (!(problem instanceof GroupedProblemForm)) 
            {
            state.output.fatal("MasterProblem.preprocessPopulation(...) invoked, but the underlying Problem is not of GroupedProblemForm");
            }
                
        ((GroupedProblemForm) problem).preprocessPopulation(state, pop);
        }

    /* (non-Javadoc)
     * @see ec.coevolve.GroupedProblemForm#postprocessPopulation(ec.EvolutionState, ec.Population)
     */
    public void postprocessPopulation(EvolutionState state, Population pop) 
        {
        if (!(problem instanceof GroupedProblemForm)) 
            {
            state.output.fatal("MasterProblem.postprocessPopulation(...) invoked, but the underlying Problem is not of GroupedProblemForm");
            }
                
        ((GroupedProblemForm) problem).postprocessPopulation(state, pop);
        }

    // regular coevolutionary evaluation
    public void evaluate(EvolutionState state, Individual[] inds,
        boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum)
        {
        if(showDebugInfo)
            state.output.message("Starting a GroupedProblemForm evaluation.");

        // Acquire a slave socket
        Job job = new Job();
        job.type = Slave.V_EVALUATEGROUPED;
        job.subPops = subpops;
        job.countVictoriesOnly = countVictoriesOnly;
        job.inds = inds;
        job.updateFitness = updateFitness;
        monitor.scheduleJobForEvaluation(state,job);
                
        if( !batchMode )
            monitor.waitForAllSlavesToFinishEvaluating( state );

        if(showDebugInfo)
            state.output.message("Finished the GroupedProblemForm evaluation.");
        }

    /** Custom serialization */
    private void writeObject(ObjectOutputStream out) throws IOException
        {
        out.writeObject(problem);
        }

    /** Custom serialization */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        problem = (Problem) in.readObject();
        }

    /** Initialize contacts with the slaves */
    public void initializeContacts( final EvolutionState state )
        {
        if(showDebugInfo)
            state.output.message(Thread.currentThread().getName() + "Spawning the server thread.");
        monitor = new SlaveMonitor(state, showDebugInfo);
        }

    /** Reinitialize contacts with the slaves */
    public void reinitializeContacts( final EvolutionState state )
        {
        initializeContacts(state);
        }

    /** Gracefully close contacts with the slaves */
    public void closeContacts(EvolutionState state, int result)
        {
        monitor.shutdown();
        }
        
    public boolean canEvaluate() 
        {
        return (monitor.numAvailableSlaves() != 0); 
        }
        
    /** This will only return true if (1) the EvolutionState is a SteadyStateEvolutionState and
        (2) an individual has returned from the system.  If you're not doing steady state evolution,
        you should not call this method.  */
    public boolean evaluatedIndividualAvailable()
        {
        return monitor.evaluatedIndividualAvailable();
        }
    
    /** This method blocks until an individual is available from the slaves (which will cause evaluatedIndividualAvailable()
        to return true), at which time it returns the individual.  You should only call this method
        if you're doing steady state evolution -- otherwise, the method will block forever. */
    public QueueIndividual getNextEvaluatedIndividual()
        {
        return monitor.waitForIndividual();
        }
    }
