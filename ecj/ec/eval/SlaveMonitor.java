/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;

import java.io.*;
import java.util.*;
import java.net.*;
import ec.steadystate.SteadyStateEvolutionState;

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
    public boolean waitOnMonitor(Object monitor)
        {
        try
            {
            monitor.wait();
            }
        catch (InterruptedException e) { return false; }
        return true;
        }

    public void notifyMonitor(Object monitor)
        {
        monitor.notifyAll();
        }

    // the slaves (not really a queue)
    private LinkedList allSlaves = new LinkedList();

    // the available slaves
    private LinkedList availableSlaves = new LinkedList();

    // the maximum number of jobs per slave
    int maxJobsPerSlave;

    // whether the system should display information that is useful for debugging 
    boolean showDebugInfo;
    
    final void debug(String s)
        {
        if (showDebugInfo) { System.err.println(Thread.currentThread().getName() + "->" + s); }
        }
    
    /**
       Simple constructor that initializes the data structures for keeping track of the state of each slave.
       The constructor receives two parameters: a boolean flag indicating whether the system should display
       information that is useful for debugging, and the maximum load per slave (the maximum number of jobs
       that a slave can be entrusted with at each time).
    */
    public SlaveMonitor( boolean showDebugInfo, int maxJobsPerSlave )
        {
        this.showDebugInfo = showDebugInfo;
        this.maxJobsPerSlave = maxJobsPerSlave;
        }

    /**
       Registers a new slave with the monitor.  Upon registration, a slave is marked as available for jobs.
    */
    public void registerSlave( EvolutionState state, String name, Socket socket, DataOutputStream out, DataInputStream in)
        {
	SlaveConnection newSlave = new SlaveConnection( state, name, socket, out, in, this );
	
        synchronized(availableSlaves)
            {
            availableSlaves.addLast(newSlave);
            notifyMonitor(availableSlaves);
            }
        synchronized(allSlaves)
            {
            allSlaves.addLast(newSlave);
            notifyMonitor(allSlaves);
            }
        }

    /**
       Mark a slave as unavailable (the slave has reached its maximum load).
    */
    public void markSlaveAsUnavailable( SlaveConnection slave )
        {
        synchronized(availableSlaves)
            {
            availableSlaves.remove(slave);
            notifyMonitor(availableSlaves);
            }
        }

    /**
       Unregisters a dead slave from the monitor.
    */
    public void unregisterSlave( SlaveConnection slave )
        {
        synchronized(allSlaves)
            {
            allSlaves.remove(slave);
            notifyMonitor(allSlaves);
            }
        markSlaveAsUnavailable(slave);
        }

    /**
       Shuts down the slave monitor (also shuts down all slaves).
    */
    public void shutdown( final EvolutionState state )
        {
        synchronized(allSlaves)
            {
            while( !allSlaves.isEmpty() )
                {
                ((SlaveConnection)(allSlaves.removeFirst())).shutdown(state);
                }
            notifyMonitor(allSlaves);
            }
        }

    /**
       Schedules a job for execution on one of the available slaves.  The monitor waits until at least one
       slave is available to perform the job.
    */
    public void scheduleJobForEvaluation( final EvolutionState state, Job job )
        {
        SlaveConnection result = null;
        synchronized(availableSlaves)
            {
            while( true)
                {
                if (!availableSlaves.isEmpty()) 
                    {
                    result = (SlaveConnection)(availableSlaves.removeFirst());
                    break;
                    }
                debug("Waiting for a slave that is available." );
                waitOnMonitor(availableSlaves);
                }
            notifyMonitor(availableSlaves);
            }       
        debug( "Got a slave that is available for work." );

        result.scheduleJob(job);

        if( result.numJobs() < maxJobsPerSlave )
            {
            synchronized(availableSlaves) 
                {
                if( !availableSlaves.contains(result)) availableSlaves.addLast(result); 
                notifyMonitor(availableSlaves);
                }
            }
        }

    /**
       This method returns only when all slaves have finished the jobs that they were assigned.  While this method waits,
       new jobs can be assigned to the slaves.  This method is usually invoked from MasterProblem.finishEvaluating.  You
       should not abuse using this method: if there are two evaluation threads, where one of them waits until all jobs are
       finished, while the second evaluation thread keeps posting jobs to the slaves, the first thread might have to wait
       until the second thread has had all its jobs finished.
    */
    public void waitForAllSlavesToFinishEvaluating( final EvolutionState state )
        {
        //System.out.println("+ waitForAllSlavesToFinishEvaluating");

        synchronized(allSlaves)
            {
            Iterator iter = allSlaves.iterator();
            while( iter.hasNext() )
                {
                SlaveConnection slaveConnection = (SlaveConnection)(iter.next());
                try { slaveConnection.dataOut.flush(); } catch (java.io.IOException e) {} // we'll catch this error later....
                }
            notifyMonitor(allSlaves);
            }
            
        boolean shouldCycle = true;
        synchronized(allSlaves)
            {
            while( shouldCycle )
                {
                shouldCycle = false;
                Iterator iter = allSlaves.iterator();
                while( iter.hasNext() )
                    {
                    SlaveConnection slaveConnection = (SlaveConnection)(iter.next());
                    int jobs = slaveConnection.numJobs();
                    if( jobs != 0 )
                        {
                        debug("Slave " + slaveConnection + " has " + jobs + " more jobs to finish." );
                        shouldCycle = true;
                        break;
                        }                               
                    }
                if( shouldCycle )
                    {
                    debug("Waiting for slaves to finish their jobs." );
                    waitOnMonitor(allSlaves);
                    debug("At least one job has been finished." );
                    }
                }
            notifyMonitor(allSlaves);
            }
        debug("All slaves have finished their jobs." );
        //System.out.println("- waitForAllSlavesToFinishEvaluating");
        }

    /**
       Notifies the monitor that the particular slave has finished performing a job, and it (probably) is
       available for other jobs.
    */
    void notifySlaveAvailability( SlaveConnection slave, final Job job, EvolutionState state )
        {
        // first announce that a slave in allSlaves has finished, so people blocked on waitForAllSlavesToFinishEvaluating
        // can wake up and realize it.
        
        synchronized(allSlaves)
            {
            notifyMonitor(allSlaves);
            }

        // now announce that we've got a new available slave if someone wants it
        
        if( slave.numJobs() < maxJobsPerSlave )
            {
            synchronized(availableSlaves)
                { 
                if( !availableSlaves.contains(slave)) availableSlaves.addLast(slave);
                notifyMonitor(availableSlaves);
                }
            }

        debug("Notify the monitor that the slave is available." );

        // now announce that we've got a new completed individual if someone is waiting for it

        if( state instanceof ec.steadystate.SteadyStateEvolutionState )
            {
            // Perhaps we should the individuals by fitness first, so the fitter ones show up later
            // and don't get immediately wiped out by less fit ones.  Or should it be the other way
            // around?  We might revisit that in the future.
            
            // At any rate, add ALL the individuals that came back to the evaluatedIndividuals LinkedList
            synchronized(evaluatedIndividuals)
                {
                for(int x=0; x<job.inds.length;x++)
                    evaluatedIndividuals.addLast( job.inds[x] );
                notifyMonitor(evaluatedIndividuals);
                }
            }
        }

    LinkedList evaluatedIndividuals =  new LinkedList();

    public boolean evaluatedIndividualAvailable()
        {
        synchronized(evaluatedIndividuals)
            {
            // return evaluatedIndividuals.size();   // believe it or not, this is O(n)!!!
            try { evaluatedIndividuals.getFirst(); return true; }
            catch (NoSuchElementException e) { return false; }
            }
        }

    /** Returns null if there is no such individual, else returns the next individual. */
    public Individual getNextAvailableIndividual()
        {
        synchronized(evaluatedIndividuals)
            {
            try { return (Individual)(evaluatedIndividuals.removeFirst()); }
            catch (NoSuchElementException e) { return null; }
            }
        }

    /** Blocks until an individual comes available */
    public Individual waitForIndividual()
        {
        while( true)
            {
            synchronized(evaluatedIndividuals)
                {
                if (evaluatedIndividualAvailable())
                    return getNextAvailableIndividual();

                debug("Waiting for individual to be evaluated." );
                waitOnMonitor(evaluatedIndividuals);  // lets go of evaluatedIndividuals loc
                debug("At least one individual has been finished." );
                }
            }
        }

    /** Returns the number of available slave (not busy) */ 
    public int numAvailableSlaves()
        {
        int i = 0;
        //System.out.println("+ numAvailableSlaves");
        synchronized(availableSlaves) { i = availableSlaves.size(); }
        //System.out.println("- numAvailableSlaves");
        return i;
        }
    }
