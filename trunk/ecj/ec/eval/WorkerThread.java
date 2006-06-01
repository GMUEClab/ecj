/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import ec.*;
import java.io.*;

/**
 * WorkerThread.java
 *

 A WorkerThread is in charge with receiving the results of evaluations that a certain slave has performed
 (there is one WorkerThread for each of the current slaves).  Each WorkerThread uses separate thread, such
 that they do not interfere with one another or with the other threads in the system.

 * @author Liviu Panait
 * @version 1.0 
 */

public class WorkerThread extends Thread
    {

    // pointers to the states of the evolution and of the slave 
    EvolutionState state;
    SlaveData slaveData;

    // whether debugging information is to be displayed
    boolean showDebugInfo;

    // a simple constructor
    WorkerThread( EvolutionState s, SlaveData sd, boolean showDebugInfo )
        {
        state = s;
        slaveData = sd;
        this.showDebugInfo = showDebugInfo;
        }

    /**
       The run method loops and reads back the results of evaluations from the slave associated with this worker thread.
    */
    public void run()
        {
        if(showDebugInfo)
            {
            currentThread().setName("WorkerThread("+slaveData.slaveName+")::    ");
            }
        while( true )
            {
            if(showDebugInfo)
                state.output.message( currentThread().getName() + "Waiting for an individual that was evaluated by the slave...." );

            try
                {
                // ... then read the results back
                DataInputStream dataIn = slaveData.dataIn;

                byte val = dataIn.readByte();

                // the jobs queue stores individuals in a first-come-first-serve order, which means that results will be received
                // back from the slave in the same order that individuals where sent there for evaluation
                Individual ind = slaveData.jobQueue.getIndividual(state);

                if(showDebugInfo)
                    state.output.message( currentThread().getName() + "The slave has an individual to send back...." );

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
                    state.output.message( currentThread().getName() + "The slave has finished sending back the evaluated individual...." );

                // update the jobs queue and inform the slaves monitor that another result (either individual or only its fitness)
                // was read back from the slave
                EvaluationData ed = slaveData.jobQueue.finishReadingIndividual( state, slaveData );
                if( ed != null )
                    slaveData.slaveMonitor.notifySlaveAvailability( slaveData, ed );
                }
            catch (Exception e)
                {
                state.output.systemMessage( "Slave " + slaveData.slaveName + " disconnected unexpectedly." );
                slaveData.shutdown(state);
                return;
                }
            }
        }
    }
