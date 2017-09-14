/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import ec.*;
import java.util.*;
import ec.util.*;


/**
 * SlaveConnection.java
 *

 This class contains certain information associated with a slave: its name, connection socket,
 input and output streams, and the job queue.  Additionally, the class sets up an auxillary thread
 which reads and writes to the streams to talk to the slave in the background.  This thread uses
 the SlaveMonitor as its synchronization point (it sleeps with wait() and wakes up when notified()
 to do some work).
 
 <P>Generally SlaveConnection is only seen by communicates only with SlaveMonitor.

 * @author Liviu Panait, Keith Sullivan, and Sean Luke
 * @version 2.0 
 */

class SlaveConnection 
    {
    /** Name of the slave process */
    String slaveName;
        
    /**  Socket for communication with the slave process */
    Socket evalSocket;
        
    /**  Used to transmit data to the slave. */
    DataOutputStream dataOut;
        
    /**  Used to read results and randoms state from slave. */
    public DataInputStream dataIn;
        
    // a pointer to the evolution state
    EvolutionState state;

    // a pointer to the monitor
    SlaveMonitor slaveMonitor;

    // a pointer to the worker thread that is working for this slave
    ThreadPool.Worker reader;
    Runnable readerRun;
    ThreadPool.Worker writer;
    Runnable writerRun;

    // given that we expect the slave to return the evaluated individuals in the exact same order,
    // the jobs need to be represented as a queue.
    LinkedList jobs = new LinkedList();

    /**
       The constructor also creates the queue storing the jobs that the slave
       has been asked to evaluate.  It also creates and launches the worker
       thread that is communicating with the remote slave to read back the results
       of the evaluations.
    */
    public SlaveConnection( EvolutionState state,
        String slaveName,
        Socket evalSocket,
        DataOutputStream dataOut,
        DataInputStream dataIn,
        SlaveMonitor slaveMonitor )
        {
        this.slaveName = slaveName;
        this.evalSocket = evalSocket;
        this.dataOut = dataOut;
        this.dataIn = dataIn;
        this.state = state;
        this.slaveMonitor = slaveMonitor;
        buildThreads();
        showDebugInfo = slaveMonitor.showDebugInfo;
        }
        
    /**
       This method is called whenever there are any communication problems with the slave
       (indicating possibly that the slave might have crashed).  In this case, the jobs will
       be rescheduled for evaluation on other slaves.
    */
    boolean shuttingDown;
    Object shutDownLock = new int[0];  // serializable and lockable
    protected void shutdown( final EvolutionState state )
        {
        // prevent me from hitting this multiple times
        synchronized(shutDownLock) { if (shuttingDown) return; else shuttingDown = true; }
        
        // don't want to miss any of these so we'll wrap them individually
        try { dataOut.writeByte(Slave.V_SHUTDOWN); } catch (Exception e) { }  // exception, not IOException, because JZLib throws some array exceptions
        try { dataOut.flush(); } catch (Exception e) { }
        try { dataOut.close(); } catch (Exception e) { }
        try { dataIn.close(); } catch (Exception e) { }
        try { evalSocket.close(); } catch (IOException e) { }

        slaveMonitor.unregisterSlave(this);  // unregister me BEFORE I reschedule my jobs

        synchronized(jobs) 
            {
            // notify my threads now that I've closed stuff in case they're still waiting
            slaveMonitor.notifyMonitor(jobs);
            reader.interrupt();  // not important right now but...
            writer.interrupt(); // very important that we be INSIDE the jobs synchronization here so the writer doesn't try to wait on the monitor again.
            }
                
        // Now we exist the jobs synchronization to allow the writer to regain his
        // mutexes, otherwise he'll block.
                
        slaveMonitor.pool.join(reader, readerRun);
        slaveMonitor.pool.join(writer, writerRun);
        reader = null;
        writer = null;
        readerRun = null;
        writerRun = null;  // let GC

        state.output.systemMessage("Slave " + slaveName + " shut down." );
        if (slaveMonitor.rescheduleLostJobs)
            rescheduleJobs(state);  // AFTER we've shut down the slave
        }

    public String toString() { return "Slave(" + slaveName + ")"; }

    boolean showDebugInfo;
        
    final void debug(String s)
        {
        if (showDebugInfo) { System.err.println(Thread.currentThread().getName() + "->" + s); }
        }
    
    /**
       Returns the number of jobs that a slave is in charge of.
    */
    public int numJobs()
        {
        synchronized(jobs) { return jobs.size(); }
        }
    
    // constructs the worker thread for the slave and starts it
    void buildThreads()
        {
        reader = slaveMonitor.pool.start(readerRun = new Runnable()
            {
            public void run() { while (readLoop()); }
            });

        writer = slaveMonitor.pool.start(writerRun = new Runnable()
            {
            public void run() { while (writeLoop()); }
            });
        }
    
    
    // returns the oldest unsent job, or null if there is no unsent job.
    // marks the job as sent so we don't try to grab it next time
    // NOT SYNCHRONIZED -- YOU MUST SYNCHRONIZE ON jobs!
    Job oldestUnsentJob()
        {
        // jobs are loaded into the queue from the back and go to the front.
        // so the oldest jobs are in the front and we should search starting
        // at the front.  List iterators go from front to back, so we can iterate
        // starting with the oldest.
        
        // This all could have been O(1) if we had used two queues, but we're being
        // intentionally lazy to keep this from getting to complex.
        Iterator i = jobs.iterator();
        while(i.hasNext())
            {
            Job job = (Job)(i.next());
            if (!job.sent) { job.sent = true; return job; }
            }
        return null;
        }
        
    
    boolean writeLoop()
        {
        Job job = null;
        
        try
            {
            synchronized(jobs)
                {
                // check for an unsent job
                if ((job = oldestUnsentJob()) == null)  // automatically marks as sent
                    {
                    // failed -- wait and drop out of the loop and come in again
                    debug("" + Thread.currentThread().getName() + "Waiting for a job to send" );                    
                    // this is a copy of waitOnMonitor but I handle the InterruptedException
                    jobs.wait(); 
                    }
                }
            if (job != null)  // we got a job inside our synchronized wait
                {
                // send the job
                debug("" + Thread.currentThread().getName() + "Sending Job");
                if( job.type == Slave.V_EVALUATESIMPLE )
                    {
                    // Tell the server we're evaluating a SimpleProblemForm
                    dataOut.writeByte(Slave.V_EVALUATESIMPLE);
                    }
                else
                    {
                    // Tell the server we're evaluating a GroupedProblemForm
                    dataOut.writeByte(Slave.V_EVALUATEGROUPED);
                                        
                    // Tell the server whether to count victories only or not.
                    dataOut.writeBoolean(job.countVictoriesOnly);
                    }
                                
                // transmit number of individuals 
                dataOut.writeInt(job.inds.length); 
                            
                // Transmit the subpopulations to the slave 
                for(int x=0;x<job.subPops.length;x++)
                    dataOut.writeInt(job.subPops[x]);
                            
                debug("Starting to transmit individuals"); 
                            
                // Transmit the individuals to the server for evaluation...
                for(int i=0;i<job.inds.length;i++)
                    {
                    job.inds[i].writeIndividual(state, dataOut);
                    dataOut.writeBoolean(job.updateFitness[i]);
                    }
                dataOut.flush();
                }
            }
        catch (Exception e)     // just in case RuntimeException is thrown
            {
            shutdown(state);
            return false; 
            }
        return true;
        }
        
        
        
        
        
    boolean readLoop()
        {
        Job job = null;
        
        try
            {
            // block on an incoming job
            byte val = dataIn.readByte();
            debug(SlaveConnection.this.toString() + " Incoming Job");
            
            // okay, we've got a job.  Grab the earliest job, that's what's coming in
            
            synchronized(jobs) 
                {
                job = (Job)(jobs.getFirst());                           // NO SUCH ELEMENT EXCEPTION
                }
            debug("Got job: " + job);
            
            
            ///// NEXT STEP: COPY THE INDIVIDUALS FORWARD INTO NEWINDS.
            ///// WE DO THIS SO WE CAN LOAD THE INDIVIDUALS BACK INTO NEWINDS
            ///// AND THEN COPY THEM BACK INTO THE ORIGINAL INDS, BECAUSE ECJ
            ///// DOESN'T HAVE A COPY(INDIVIDUAL,INTO_INDIVIDUAL) FUNCTION
            
            job.copyIndividualsForward();

            // now start reading.  Remember that we've already got a byte.
            
            for(int i = 0; i < job.newinds.length; i++)
                {
                debug(SlaveConnection.this.toString() + " Individual# " + i);
                debug(SlaveConnection.this.toString() + " Reading Byte" );
                if (i > 0) val = dataIn.readByte();  // otherwise we've got it already
                debug(SlaveConnection.this.toString() + " Reading Individual" );
                if (val == Slave.V_INDIVIDUAL)
                    {
                    job.newinds[i].readIndividual(state, dataIn);
                    }
                else if (val == Slave.V_FITNESS)
                    {
                    job.newinds[i].evaluated = dataIn.readBoolean();
                    job.newinds[i].fitness.readFitness(state,dataIn);
                    }
                else if (val == Slave.V_NOTHING)
                    {
                    // do nothing
                    }
                debug( SlaveConnection.this.toString() + " Read Individual" );
                }

            ///// NEXT STEP: COPY THE NEWLY-READ INDIVIDUALS BACK INTO THE ORIGINAL
            ///// INDIVIDUALS.  THIS IS QUITE A HACK, IF YOU READ JOB.JAVA

            // Now we have all the individuals in so we're good.  Copy them back into the original individuals
            job.copyIndividualsBack(state);
            
            ///// LAST STEP: LET OTHERS KNOW WE'RE DONE AND AVAILABLE FOR ANOTHER JOB
            // we're all done!  Yank the job from the queue so others think we're available
            synchronized(jobs)
                {
                jobs.removeFirst();
                }

            // And let the slave monitor we just finished a job
            slaveMonitor.notifySlaveAvailability( SlaveConnection.this, job, state );
            }
        catch (IOException e)
            {
            shutdown(state);  // will redistribute jobs
            return false;
            }

        return true;
        }

    


    /**
       Adds a new jobs to the queue.  This implies that the slave will be in charge of executing
       this particular job.
    */
    public void scheduleJob( final Job job )
        {
        synchronized(jobs)
            {
            if (job.sent) // just in case
                state.output.fatal("Tried to schedule a job which had already been scheduled.");
            jobs.addLast(job);
            slaveMonitor.notifyMonitor(jobs);
            }
        }

    /**
       Reschedules the jobs in this job queue to other slaves in the system.  It assumes that the slave associated
       with this queue has already been removed from the available slaves, such that it is not assigned its own jobs.
    */
    // only called when we're shutting down, so we're not waiting for any notification.
    void rescheduleJobs( final EvolutionState state )
        {
        while( true )
            {
            Job job = null;
            synchronized(jobs)
                {
                if( jobs.isEmpty() ) { return; }
                job = (Job)(jobs.removeFirst());
                }
            debug(Thread.currentThread().getName() + " Waiting for a slave to reschedule the evaluation.");
            job.sent = false;  // reuse
            slaveMonitor.scheduleJobForEvaluation(state,job);
            debug(Thread.currentThread().getName() + " Got a slave to reschedule the evaluation.");
            }
        }
    }


