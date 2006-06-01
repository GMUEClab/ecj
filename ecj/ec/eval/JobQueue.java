/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;
import java.util.LinkedList;

/**
 * JobQueue.java
 *

 The queue of jobs that are assigned to a slave (there is a one-to-one mapping between slaves and
 job queues.  The system keeps track of all jobs assigned to a slave such that, if that slave
 crashed, the jobs can be rescheduled for execution on other threads.  As evaluated individuals
 (or only their fitness) are read back from the slaves, the queue of jobs is continuously updated.
 For regular evolution, a job consists of an individual that needs to be evaluated, and as such,
 a job is removed from the queue of jobs each time an individual is read back from the slave.  On
 the other hand, coevolutionary settings involve multiple individuals that are evaluated together;
 in this case, the queue of jobs keeps track how many more individuals need to be read back from
 the slave before the current job is finished.  Of course, a slave that crashes in the middle of
 the run requires that the entire job needs to be rescheduled to another slave.

 The queue is a first-in-first-out data structure.  This implies that the jobs are expected to be
 read back from the slave in exactly the same order as they were sent there.

 * @author Liviu Panait
 * @version 1.0 
 */

public class JobQueue
    {

    // this is the only synchronization device in the application
    SlaveMonitor slaveMonitor;

    // given that we expect the slave to return the evaluated individuals in the exact same order,
    // the objects need to be represented as a queue.
    LinkedList objects;

    // auxiliary variable needed for rescheduling the jobs
    int numToReschedule = 0;

    /**
       A simple constructor.  A pointer to the slave monitor is necessary for synchronization purposes.
    */
    public JobQueue( SlaveMonitor sm )
        {
        slaveMonitor = sm;
        synchronized( slaveMonitor )
            {
            objects = new LinkedList();
            }
        }

    /**
       Resets the state of the queue (empties the queue of jobs)
    */
    public void reset()
        {
        synchronized( slaveMonitor )
            {
            objects.clear();
            }
        }

    /**
       Adds a new jobs to the queue.  This implies that the slave will be in charge of executing
       this particular job.
    */
    public void scheduleJob( final EvaluationData ed )
        {
        synchronized( slaveMonitor )
            {
            objects.addLast(ed);
            // for synchronization purposes, we may need to inform other treads that a job has been scheduled
            // (other threads may be waiting to schedule jobs as well)
            slaveMonitor.notifyAll();
            }
        }

    /**
       As the slave executes jobs, it sends back individuals that have been evaluated (or only their fitness).
       This function returns a pointer to the next individual that has to be read back from the slave (only
       individuals know how to read themselves).  In a traditional evolutionary algorithm, this pointer is the
       one to the only individual associated with the job (the one that was sent to the slave to be evaluated).
       However, multiple individuals could be sent to the slave for evaluation in a coevolutionary algorithm.
       In this case, the function uses the information on how many individuals in this current job have been
       read back from the slave, and returns a pointer to the next individual that is expected from the slave.
    */
    public Individual getIndividual( final EvolutionState state )
        {
        synchronized( slaveMonitor )
            {
            while( true )
                {
                if( !objects.isEmpty() )
                    break;
                if(slaveMonitor.showDebugInfo)
                    state.output.message( Thread.currentThread().getName() + "Waiting in getIndividual because there are no individuals to be evaluated" );
                try
                    {
                    slaveMonitor.wait();
                    if(slaveMonitor.showDebugInfo)
                        state.output.message( Thread.currentThread().getName() + "An individual might be available" );
                    }
                catch (InterruptedException e) 
                    {}
                }

            EvaluationData ed = (EvaluationData)(objects.getFirst());
            if( ed.type == Slave.V_EVALUATESIMPLE )
                {
                Individual result = ed.ind;
                slaveMonitor.notifyAll();
                return result;
                }
            else
                {
                if( ed.index >= ed.length )
                    {
                    if(slaveMonitor.showDebugInfo)
                        state.output.message( "Should not have gotten here!" );
                    System.exit(1);
                    }
                Individual result = ed.inds[ed.index];
                slaveMonitor.notifyAll();
                return result;
                }
            }
        }

    /**
       Once an evaluated individual has been read back from a slave, the job queue might undergo certain updates.
       In a traditional evolutionary algorithm, a job is finished once the individual is read back, and as such,
       the job should be removed from the list of jobs that the slave is currently in charge of.  In a coevolutionary
       algorithm, each job consists of multiple individuals.  In this case, a job is finished only when all individuals
       associated with a job have been received back from the slave.
                
       The function returns the job that has just finished, or null if no job was finished (only for coevolutionary algorithms).
    */
    public EvaluationData finishReadingIndividual( final EvolutionState state, final SlaveData slaveData )
        {
        synchronized( slaveMonitor )
            {
            EvaluationData ed = (EvaluationData)(objects.getFirst());
            if( ed.type == Slave.V_EVALUATESIMPLE )
                {
                EvaluationData result = (EvaluationData)(objects.removeFirst());
                if(slaveMonitor.showDebugInfo)
                    state.output.message( Thread.currentThread().getName() + objects.size() + " individuals remaining in the slave's queue." );
                slaveMonitor.notifyAll();
                return result;
                }
            else
                {
                if( ed.index >= ed.length )
                    {
                    if(slaveMonitor.showDebugInfo)
                        state.output.message( Thread.currentThread().getName() + "Should not have gotten here!" );
                    System.exit(1);
                    }

                ed.index++;

                if( ed.index == ed.length )
                    {
                    EvaluationData result = (EvaluationData)(objects.removeFirst());
                    if(slaveMonitor.showDebugInfo)
                        state.output.message( Thread.currentThread().getName() + objects.size() + " individuals remaining to be read back for the current coevolutioary evaluation." );
                    slaveMonitor.notifyAll();
                    return result;
                    }
                }
            }
        return null;
        }

    /**
       Reschedules the jobs in this job queue to other slaves in the system.  It assumes that the slave associated
       with this queue has already been removed from the available slaves, such that it is not assigned its own jobs.
    */
    public void rescheduleJobs( final EvolutionState state )
        {
        while( true )
            {
            EvaluationData ed = null;
            synchronized( slaveMonitor )
                {
                if( objects.isEmpty() )
                    return;
                ed = (EvaluationData)(objects.removeFirst());
                numToReschedule = 1;
                }
            if( ed.type == Slave.V_EVALUATESIMPLE )
                {
                if(slaveMonitor.showDebugInfo)
                    state.output.message(Thread.currentThread().getName() + "Waiting for a slave to reschedule the evaluation.");
                synchronized( slaveMonitor )
                    {
                    slaveMonitor.scheduleJobForEvaluation(ed.state,ed);
                    numToReschedule = 0;
                    }
                if(slaveMonitor.showDebugInfo) 
                    state.output.message(Thread.currentThread().getName() + "Got a slave to reschedule the evaluation.");
                if( !ed.mp.batchMode )
                    slaveMonitor.waitForAllSlavesToFinishEvaluating( ed.state );
                }
            else
                {
                if(slaveMonitor.showDebugInfo)
                    state.output.message(Thread.currentThread().getName() + "Waiting for a slave to reschedule this coevolutionary evaluation");
                ed.index = 0;
                synchronized( slaveMonitor )
                    {
                    slaveMonitor.scheduleJobForEvaluation(ed.state,ed);
                    numToReschedule = 0;
                    }
                if(slaveMonitor.showDebugInfo) 
                    state.output.message(Thread.currentThread().getName() + "Got a slave to reschedule this coevolutionary evaluation");
                if( !ed.mp.batchMode )
                    slaveMonitor.waitForAllSlavesToFinishEvaluating( ed.state );
                }
            synchronized( slaveMonitor )
                {
                slaveMonitor.notifyAll();
                }
            }
        }

    /**
       Returns the number of jobs that a slave is in charge of.
    */
    public int numJobs()
        {
        synchronized( slaveMonitor )
            {
            // we also need to account for the jobs that are in the process of being rescheduled.
            return objects.size() + numToReschedule;
            }
        }

    }
