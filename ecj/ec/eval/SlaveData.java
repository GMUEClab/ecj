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
    /** Name of the slave process */
    String slaveName;
        
    /**  Socket for communication with the slave process */
    Socket evalSocket;
        
    /**  Used to transmit data to the slave. */
    DataOutputStream dataOut;
        
    /**  Used to read results and randoms state from slave. */
    public DataInputStream dataIn;
        
    /**
     * Used to store the individuals currently evaluated by this slave, in case it crashes.
     * Each element in the queue is of type EvaluationData.
     */
    JobQueue jobQueue;

    // a pointer to the evolution state
    EvolutionState state;

    // a pointer to the monitor
    SlaveMonitor slaveMonitor;

    // a pointer to the worker thread that is working for this slave
    Thread worker;

    // whether the slave is available or not (used to avoid duplicates in the list of available slaves)
    // set by the SlaveMonitor
    public boolean isSlaveAvailable = false;

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
        jobQueue = new JobQueue(slaveMonitor);
        jobQueue.reset();
	buildWorkerThread();
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
        state.output.systemMessage( SlaveData.this.toString() + " Slave is shutting down...." );
        slaveMonitor.markSlaveAsUnavailable(this);
        jobQueue.rescheduleJobs(state);
        slaveMonitor.unregisterSlave(this);
        state.output.systemMessage( SlaveData.this.toString() + " Slave exists...." );
        }


    public String toString() { return "Slave(" + slaveName + ")"; }

    // constructs the worker thread for the slave and starts it
    void buildWorkerThread()
	{
	final boolean showDebugInfo = this.slaveMonitor.showDebugInfo;
	
	worker = new Thread()
	    {
	    public void run()
		{
		while( true )
		    {
		    if(showDebugInfo)
			state.output.message( SlaveData.this.toString() + ": Waiting for an individual that was evaluated by the slave...." );

		    try
			{
			byte val = dataIn.readByte();

			// the jobs queue stores individuals in a first-come-first-serve order, which means that results will be received
			// back from the slave in the same order that individuals where sent there for evaluation
			Individual ind = jobQueue.getIndividual(state);

			if(showDebugInfo)
			    state.output.message( SlaveData.this.toString() + "The slave has an individual to send back...." );

			if (val == Slave.V_INDIVIDUAL)
			    {
			    ind.readIndividual(state, dataIn);
			    }
			else if (val == Slave.V_FITNESS)
			    {
			    ind.evaluated = dataIn.readBoolean();
			    ind.fitness.readFitness(state,dataIn);
			    }

			if(showDebugInfo)
			    state.output.message( SlaveData.this.toString() + "The slave has finished sending back the evaluated individual...." );

			// update the jobs queue and inform the slaves monitor that another result (either individual or only its fitness)
			// was read back from the slave
			EvaluationData ed = jobQueue.finishReadingIndividual( state, SlaveData.this );
			if( ed != null )
			    slaveMonitor.notifySlaveAvailability( SlaveData.this, ed );
			}
		    catch (Exception e)
			{
			state.output.systemMessage( "Slave " + slaveName + " disconnected.");
			shutdown(state);
			return;
			}
		    }
		}
	    };
	worker.start();
	}


    }


