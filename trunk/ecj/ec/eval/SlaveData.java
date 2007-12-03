/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import java.io.*;
import java.net.*;
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

    /**
     * Name of the slave process
     */
    public String slaveName;
        
    /**
     * Socket for communication with the slave process
     */
    public Socket evalSocket;
        
    /**
     * Used to transmit data to the slave.
     */
    public DataOutputStream dataOut;
        
    /**
     * Used to read results and randoms state from slave.
     */
    public DataInputStream dataIn;
        
    /**
     * Used to store the individuals currently evaluated by this slave, in case it crashes.
     * Each element in the queue is of type EvaluationData.
     */
    public JobQueue jobQueue;

    // a pointer to the monitor
    SlaveMonitor slaveMonitor;

    // a pointer to the worker thread that is in charge of this slave
    WorkerThread workerThread;

    // whether the slave is available or not (used to avoid duplicates in the list of available slaves)
    boolean isSlaveAvailable = false;

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
        this.slaveMonitor = slaveMonitor;
        this.jobQueue = new JobQueue(this.slaveMonitor);
        this.jobQueue.reset();
        workerThread = new WorkerThread(state,this,this.slaveMonitor.showDebugInfo);
        workerThread.start();
        }

    /**
       This method is called whenever there are any communication problems with the slave
       (indicating possibly that the slave might have crashed).  In this case, the jobs will
       be rescheduled for evaluation on other slaves.
    */
    public void shutdown( final EvolutionState state )
        {
        try
            {
            // 0 means shutdown
            dataOut.writeByte(Slave.V_SHUTDOWN);
            dataOut.flush();
            dataOut.close();
            dataIn.close();
            evalSocket.close();
            }
        catch (IOException e)
            {
            // Just ignore the exception since we're closing the socket and
            // I/O streams.
            }
        state.output.systemMessage( Thread.currentThread().getName() + "Slave is shutting down...." );
        slaveMonitor.markSlaveAsUnavailable(this);
        jobQueue.rescheduleJobs(state);
        slaveMonitor.unregisterSlave(this);
        state.output.systemMessage( Thread.currentThread().getName() + "Slave exists...." );
        }

    }


