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

/**
 * SlaveData.java
 *

 This class contains certain information associated with a slave.  Such information includes the
 name of the slave, the socket (and I/O streams) used to communicate with the slave, the job queue
 associated with the slave, as well as pointers to the slave monitor and to the worker thread (for
 synchronization purposes).

 * @author Liviu Panait
 * @version 1.0 
 */

class SlaveData 
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
    Thread worker;

    // given that we expect the slave to return the evaluated individuals in the exact same order,
    // the jobs need to be represented as a queue.
    private LinkedList jobs = new LinkedList();

    // auxiliary variable needed for rescheduling the jobs
    int numToReschedule = 0;

    /**
       The constructor also creates the queue storing the jobs that the slave
       has been asked to evaluate.  It also creates and launches the worker
       thread that is communicating with the remote slave to read back the results
       of the evaluations.
    */
    public SlaveData( EvolutionState state,
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
        buildWorkerThread();
        showDebugInfo = slaveMonitor.showDebugInfo;
        }
        
    /**
       This method is called whenever there are any communication problems with the slave
       (indicating possibly that the slave might have crashed).  In this case, the jobs will
       be rescheduled for evaluation on other slaves.
    */
    protected void shutdown( final EvolutionState state )
        {
        // don't want to miss any of these so we'll wrap them individually
        try { dataOut.writeByte(Slave.V_SHUTDOWN); } catch (IOException e) { }
        try { dataOut.flush(); } catch (IOException e) { }
        try { dataOut.close(); } catch (IOException e) { }
        try { dataIn.close(); } catch (IOException e) { }
        try { evalSocket.close(); } catch (IOException e) { }

        state.output.systemMessage( SlaveData.this.toString() + " Slave is shutting down...." );
        slaveMonitor.markSlaveAsUnavailable(this);
        rescheduleJobs(state);
        slaveMonitor.unregisterSlave(this);
        state.output.systemMessage( SlaveData.this.toString() + " Slave exists...." );
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
        synchronized(jobs) {  return jobs.size() + numToReschedule; }
        }

    
    
    // constructs the worker thread for the slave and starts it
    void buildWorkerThread()
        {
        worker = new Thread()
            {
            public void run()
                {
                while( processJob()); 
                }
            };
        worker.start();
        }
    
    
    
    
    // The main loop for the individual-reading thread.   This function
    // processes one job and provides it.
    boolean processJob()
        {
        Job job = null;
        
        
        ///// FIRST STEP: WAIT UNTIL I HAVE A JOB
        
        // get next job
        synchronized(jobs) 
            {
            while( true )
                {
                if( !jobs.isEmpty() ) 
                    {
                    job = (Job)(jobs.getFirst());  // keep in the queue so others think we're not available
                    break;
                    }
                debug("" + Thread.currentThread().getName() + "Waiting for a job" );
                slaveMonitor.waitOnMonitor(jobs);
                }
            }
        debug("Got job: " + job);
        




        ///// NEXT STEP: WRITE THE INDIVIDUALS OUT TO THE SLAVE
        
        if( job.type == Slave.V_EVALUATESIMPLE )
            {
            try 
                { 
                // Tell the server we're evaluating a SimpleProblemForm
                dataOut.writeByte(Slave.V_EVALUATESIMPLE);
                } 
            catch (Exception e)  { shutdown(state);  }
            }
        else
            {
            try 
                { 
                // Tell the server we're evaluating a GroupedProblemForm
                dataOut.writeByte(Slave.V_EVALUATEGROUPED);
                                
                // Tell the server whether to count victories only or not.
                dataOut.writeBoolean(job.countVictoriesOnly);
                } 
            catch (Exception e)  { shutdown(state); }
            }
                
        try 
            {
            // transmit number of individuals 
            dataOut.writeInt(job.inds.length); 
                        
            // Transmit the subpopulation number to the slave 
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
        catch (Exception e)  {  shutdown(state);  }
                



        ///// NEXT STEP: COPY THE INDIVIDUALS FORWARD INTO NEWINDS.
        ///// WE DO THIS SO WE CAN LOAD THE INDIVIDUALS BACK INTO NEWINDS
        ///// AND THEN COPY THEM BACK INTO THE ORIGINAL INDS, BECAUSE ECJ
        ///// DOESN'T HAVE A COPY(INDIVIDUAL,INTO_INDIVIDUAL) FUNCTION
            
        job.copyIndividualsForward();
        
        
        
        ///// NEXT STEP: READ THE INDIVIDUALS BACK FROM THE SLAVE
        
        // Now we read the individuals into newinds
        try
            {
            for(int i = 0; i < job.newinds.length; i++)
                {
                debug(SlaveData.this.toString() + " Individual# " + i);
                debug(SlaveData.this.toString() + " Reading Byte" );
                byte val = dataIn.readByte();
                debug(SlaveData.this.toString() + " Reading Individual" );
                if (val == Slave.V_INDIVIDUAL)
                    {
                    job.newinds[i].readIndividual(state, dataIn);
                    }
                else if (val == Slave.V_FITNESS)
                    {
                    job.newinds[i].evaluated = dataIn.readBoolean();
                    job.newinds[i].fitness.readFitness(state,dataIn);
                    }
                debug( SlaveData.this.toString() + " Read Individual" );
                }
            }
        catch (Exception e)
            {
            state.output.systemMessage( "Slave " + slaveName + " disconnected.");
            // put the job back, we'll have to do it over again on another slave
            
            // no need to put the job back -- it's still in the queue at the first position.
            synchronized(jobs)
                {
                //slaveMonitor.notifyMonitor(jobs);  // for good measure
                //jobs.addFirst(job);  // put back where it was
                }
            shutdown(state);  // will redistribute jobs
            return false;
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
        slaveMonitor.notifySlaveAvailability( SlaveData.this, job, state );

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
                //slaveMonitor.notifyMonitor(jobs);
                if( jobs.isEmpty() )
                    {
                    return;
                    }
                job = (Job)(jobs.removeFirst());
                numToReschedule = 1;
                //job.index = 0;
                }
                    
            debug(Thread.currentThread().getName() + " Waiting for a slave to reschedule the evaluation.");
                        
            slaveMonitor.scheduleJobForEvaluation(state,job);

            synchronized(jobs) 
                { 
                //slaveMonitor.notifyMonitor(jobs);
                numToReschedule = 0; 
                }
       
            debug(Thread.currentThread().getName() + " Got a slave to reschedule the evaluation.");
                        
            if( !job.batchMode )
                slaveMonitor.waitForAllSlavesToFinishEvaluating( state );

            }
        }
    }


