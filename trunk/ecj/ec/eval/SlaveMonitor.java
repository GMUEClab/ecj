/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;

import java.io.*;
import java.util.*;

/**
 * SlaveMonitor.java
 *

 The SlaveMonitor is the main tool used by the evolutionary computation process to synchronize the work of
 multiple threads (for example, for different slaves).  The monitor is in charge of keeping track of all the
 slaves (either those that are busy with various jobs they have received, or those that are available to
 perform jobs on behalf of the main evolutionary process).  Additionally, the monitor provides methods to
 schedule a job to be executed by the next available slave, as well as a mechanism to wait until all jobs
 have been finished.

 * @author Liviu Panait
 * @version 1.0 
 */

public class SlaveMonitor
    {

    // the slaves (not really a queue)
    LinkedList allSlaves = null;

    // the available slaves
    LinkedList availableSlaves = null;

    // the maximum number of jobs per slave
    int maxJobsPerSlave;

    // whether the system should display information that is useful for debugging 
    boolean showDebugInfo;

    /**
       Simple constructor that initializes the data structures for keeping track of the state of each slave.
       The constructor receives two parameters: a boolean flag indicating whether the system should display
       information that is useful for debugging, and the maximum load per slave (the maximum number of jobs
       that a slave can be entrusted with at each time).
    */
    public SlaveMonitor( boolean showDebugInfo, int maxJobsPerSlave )
        {
        this.showDebugInfo = showDebugInfo;
        allSlaves = new LinkedList();
        availableSlaves = new LinkedList();
        this.evaluatedIndividuals = new LinkedList();
        this.maxJobsPerSlave = maxJobsPerSlave;
        }

    /**
       Registers a new slave with the monitor.  Upon registration, a slave is marked as available for jobs.
    */
    public synchronized void registerSlave( SlaveData slave )
        {
        allSlaves.addLast(slave);
        availableSlaves.addLast(slave);
        slave.isSlaveAvailable = true;
        notifyAll();
        }

    /**
       Mark a slave as unavailable (the slave has reached its maximum load).
    */
    public synchronized void markSlaveAsUnavailable( SlaveData slave )
        {
        availableSlaves.remove(slave);
        slave.isSlaveAvailable=false;
        notifyAll();
        }

    /**
       Unregisters a dead slave from the monitor.
    */
    public synchronized void unregisterSlave( SlaveData slave )
        {
        availableSlaves.remove(slave);
        slave.isSlaveAvailable=false;
        allSlaves.remove(slave);
        notifyAll();
        }

    /**
       Shuts down the slave monitor (also shuts down all slaves).
    */
    public synchronized void shutdown( final EvolutionState state )
        {
        while( !allSlaves.isEmpty() )
            {
            SlaveData slave = (SlaveData)(allSlaves.removeFirst());
            slave.shutdown( state );
            }
        }

    /**
       Schedules a job for execution on one of the available slaves.  The monitor waits until at least one
       slave is available to perform the job.
    */
    public synchronized void scheduleJobForEvaluation( final EvolutionState state, EvaluationData toEvaluate )
        {
        while( availableSlaves.isEmpty() )
            {
            try
                {
                if(showDebugInfo)
                    state.output.message( Thread.currentThread().getName() + "Waiting for a slave that is available." );
                wait();
                }
            catch (InterruptedException e) {}
            }
        if(showDebugInfo)
            state.output.message( Thread.currentThread().getName() + "Got a slave that is available for work." );

        SlaveData result = (SlaveData)(availableSlaves.removeFirst());
        result.isSlaveAvailable = false;
        result.jobQueue.scheduleJob(toEvaluate);
        DataOutputStream dataOut = result.dataOut;

        if( toEvaluate.type == Slave.V_EVALUATESIMPLE )
            {
            try
                {
                // Tell the server we're evaluating a SimpleProblemForm
                dataOut.writeByte(Slave.V_EVALUATESIMPLE);

                // Transmit the subpopulation number to the slave 
                dataOut.writeInt(toEvaluate.subPopNum);
                                                        
                // Transmit the individual to the server for evaluation...
                toEvaluate.ind.writeIndividual(state, dataOut);
                dataOut.flush();

                if( result.jobQueue.numJobs() < maxJobsPerSlave )
                    {
                    if( !result.isSlaveAvailable )
                        availableSlaves.addLast(result);
                    result.isSlaveAvailable = true;
                    }
                }
            catch (Exception e)
                {
                result.shutdown( state );
                }
            }
        else
            {
            try
                {
                // Tell the server we're evaluating a GroupedProblemForm
                dataOut.writeByte(Slave.V_EVALUATEGROUPED);

                // Tell the server how many individuals are involved in this evaluation
                dataOut.writeInt(toEvaluate.inds.length);
                                                        
                // Transmit the subpopulation number to the slave 
                for(int x=0;x<toEvaluate.subPops.length;x++)
                    dataOut.writeInt(toEvaluate.subPops[x]);
                                                        
                // Tell the server whether to count victories only or not.
                dataOut.writeBoolean(toEvaluate.countVictoriesOnly);
                                                        
                // Transmit the individuals to the server for evaluation...
                for(int i=0;i<toEvaluate.inds.length;i++)
                    {
                    toEvaluate.inds[i].writeIndividual(state, dataOut);
                    dataOut.writeBoolean(toEvaluate.updateFitness[i]);
                    }
                dataOut.flush();

                if( result.jobQueue.numJobs() < maxJobsPerSlave )
                    {
                    if( !result.isSlaveAvailable )
                        availableSlaves.addLast(result);
                    result.isSlaveAvailable = true;
                    }
                }
            catch (Exception e)
                {
                result.shutdown( state );
                }
            }
                
        // we are not sure whether this notifyAll is useful for anything or not, but it does not hurt for sure (in may incur a small
        // computation to wake up all threads that wait on the monitor, and for them to figure out whether they should wait some more or not).
        // it may disappear in the future, in case we discover it is not useful.
        notifyAll();
        }

    /**
       This method returns only when all slaves have finished the jobs that they were assigned.  While this method waits,
       new jobs can be assigned to the slaves.  This method is usually invoked from MasterProblem.finishEvaluating.  You
       should not abuse using this method: if there are two evaluation threads, where one of them waits until all jobs are
       finished, while the second evaluation thread keeps posting jobs to the slaves, the first thread might have to wait
       until the second thread has had all its jobs finished.
    */
    public synchronized void waitForAllSlavesToFinishEvaluating( final EvolutionState state )
        {
        Iterator iter;
                
        iter = allSlaves.iterator();
        while( iter.hasNext() )
            {
            SlaveData slaveData = (SlaveData)(iter.next());
            try { slaveData.dataOut.flush(); } catch (java.io.IOException e) {} // we'll catch this error later....
            }

        boolean shouldCycle = true;
        while( shouldCycle )
            {
            shouldCycle = false;
            iter = allSlaves.iterator();
            while( iter.hasNext() )
                {
                SlaveData slaveData = (SlaveData)(iter.next());
                if( slaveData.jobQueue.numJobs() != 0 )
                    {
                    if(showDebugInfo)
                        state.output.message( Thread.currentThread().getName() + "Slave " +
                                              slaveData.workerThread.getName() + " has " + slaveData.jobQueue.numJobs() + " more jobs to finish." );
                    shouldCycle = true;
                    break;
                    }                               
                }
            if( shouldCycle )
                {
                try
                    {
                    if(showDebugInfo)
                        state.output.message( Thread.currentThread().getName() + "Waiting for slaves to finish their jobs." );
                    wait();
                    }
                catch (InterruptedException e) {}
                if(showDebugInfo)
                    state.output.message( Thread.currentThread().getName() + "At least one job has been finished." );
                }
            }

        if(showDebugInfo)
            state.output.message( Thread.currentThread().getName() + "All slaves have finished their jobs." );
                        
        notifyAll();

        }

    /**
       Notifies the monitor that the particular slave has finished performing a job, and it (probably) is
       available for other jobs.
    */
    public synchronized void notifySlaveAvailability( SlaveData slave, final EvaluationData ed )
        {
        final EvolutionState state = ed.state;
        if( slave.jobQueue.numJobs() < maxJobsPerSlave )
            {
            if( !slave.isSlaveAvailable )
                availableSlaves.addLast(slave);
            slave.isSlaveAvailable = true;
            }

        if( showDebugInfo )
            state.output.message( Thread.currentThread().getName() + "Notify the monitor that the slave is available." );

        if( ed.type == Slave.V_EVALUATESIMPLE && state instanceof ec.eval.AsynchronousEvolutionState )
            evaluatedIndividuals.addLast( ed.ind );

        notifyAll();
        }

    LinkedList evaluatedIndividuals = null;

    public synchronized Individual waitForIndividual( final EvolutionState state )
        {
        while( evaluatedIndividuals.size() == 0 )
            {
            try
                {
                if(showDebugInfo)
                    state.output.message( Thread.currentThread().getName() + "Waiting for individual to be evaluated." );
                wait();
                }
            catch (InterruptedException e) {}
            if(showDebugInfo)
                state.output.message( Thread.currentThread().getName() + "At least one individual has been finished." );
            }
        return (Individual)(evaluatedIndividuals.removeFirst());
        }

    /**
       Returns the number of slaves (available and busy) that are currently registered with the monitor.
    */
    public synchronized int numSlaves()
        {
        return allSlaves.size();
        }

    }
