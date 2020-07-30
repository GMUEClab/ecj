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
import ec.util.*;
import ec.steadystate.QueueIndividual;

/**
 * SlaveMonitor.java
 *

 <P>The SlaveMonitor manages slave connections to each remote slave, and provides synchronization facilities
 for the slave connections and for various other objects waiting to be notified when new slaves are
 available, space is available in a slave's job queue, an individual has been completed, etc.

 <p>The monitor provides functions to create and delete slaves (registerSlave(), unregisterSlave()), 
 schedule a job for evaluation (scheduleJobForEvaluation(...)), block until all jobs have completed
 (waitForAllSlavesToFinishEvaluating(...)), test if any individual in a job has been finished
 (evaluatedIndividualAvailable()),  and block until an individual in a job is available and returned
 (waitForindividual()).
 
 <p>Generally speaking, the SlaveMonitor owns the SlaveConnections -- no one else
 should speak to them.  Also generally speaking, only MasterProblems create and speak to the SlaveMonitor.
  
 * @author Sean Luke, Liviu Panait, and Keith Sullivan
 * @version 1.0 
 */

public class SlaveMonitor
    {
    public final static String P_EVALNODELAY = "eval.no-delay";

    public final static String P_EVALSENDBUFER = "eval.send-buffer";

    public final static String P_EVALRECVBUFFER = "eval.recv-buffer";

    public static final String P_EVALMASTERPORT = "eval.master.port";
    public static final String P_EVALCOMPRESSION = "eval.compression";
    public static final String P_MAXIMUMNUMBEROFCONCURRENTJOBSPERSLAVE = "eval.masterproblem.max-jobs-per-slave";
    public static final String P_RESCHEDULELOSTJOBS = "eval.masterproblem.reschedule-lost-jobs";
    public static final int SEED_INCREMENT = 7919; // a large value (prime for fun) bigger than expected number of threads per slave

    public EvolutionState state;
    
    // set to true if slave connections should reschedule jobs before they are
    // completely shut down due to a lost slave.  We might not want this to
    // happen if we're doing asynchronous evolution, for example.  Do NOT
    // set this to true if you're doing generational evolution, it'll just
    // hang waiting for a (now-un-rescheduled) lost job.
    boolean rescheduleLostJobs;
    
    ThreadPool pool;
    
    /** A counter used to give slaves unique numbers so they can construct
        useful unique names for themselves. */
    int slaveNum = 0;
    
    /**
     *  The socket where slaves connect.
     */
    public ServerSocket servSock;
        
    /**
     * Indicates whether compression is used over the socket IO streams.
     */
    public boolean useCompression;

    boolean shutdownInProgress = false;
    Object[] shutdownInProgressLock = new Object[0];  // arrays are serializable
    final boolean isShutdownInProgress() { synchronized (shutdownInProgressLock) { return shutdownInProgress; } }
    final void setShutdownInProgress(boolean val) { synchronized (shutdownInProgressLock) { shutdownInProgress = val; } }
    
    int randomSeed;
    Thread thread;

    public boolean waitOnMonitor(Object monitor)
        {
        try
            {
            if (Thread.interrupted()) { return false; }
            else synchronized(monitor)
                     {
                     monitor.wait();
                     }
            }
        catch (InterruptedException e) { return false; }
        return true;
        }

    public void notifyMonitor(Object monitor)
        {
        monitor.notifyAll();
        }

    // the slaves (not really a queue)
    LinkedList<SlaveConnection> allSlaves = new LinkedList<>();

    // the available slaves
    LinkedList<SlaveConnection> availableSlaves = new LinkedList<>();

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
    public SlaveMonitor( final EvolutionState state, boolean showDebugInfo, final MasterProblem problemPrototype)
        {
        this.showDebugInfo = showDebugInfo;
        this.state = state;
        
        pool = new ThreadPool();
                
        int port = state.parameters.getInt(
            new Parameter( P_EVALMASTERPORT ),null);
                
        maxJobsPerSlave = state.parameters.getInt(
            new Parameter( P_MAXIMUMNUMBEROFCONCURRENTJOBSPERSLAVE ),null);

        rescheduleLostJobs = state.parameters.getBoolean(new Parameter(P_RESCHEDULELOSTJOBS), null, true);

        useCompression = state.parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
                
        final int noDelay = state.parameters.exists(new Parameter(P_EVALNODELAY), null) ? 
        	(state.parameters.getBoolean(new Parameter(P_EVALNODELAY), null, true) ? 1 : 0) : -1;

        final int sendBuffer = state.parameters.getInt(new Parameter(P_EVALSENDBUFER), null, -1); 
        final int recvBuffer = state.parameters.getInt(new Parameter(P_EVALRECVBUFFER), null, -1); 
        
        try
            {
            servSock = new ServerSocket(port);
            }
        catch( IOException e )
            {
            state.output.fatal("Unable to bind to port " + port + ": " + e);
            }
                
        randomSeed = (int)(System.currentTimeMillis());

        // spawn the thread
        thread = new Thread(new Runnable()
            {
            public void run()
                {
                Thread.currentThread().setName("SlaveMonitor::    ");
                Socket slaveSock;
                        
                while (!isShutdownInProgress())
                    {
                    slaveSock = null;
                    while( slaveSock==null && !isShutdownInProgress() )
                        {
                        try
                            {
                            slaveSock = servSock.accept();
                            }
                        catch( IOException e ) { slaveSock = null; }
                        }
                    if (slaveSock == null)
                    	break;

                    debug(Thread.currentThread().getName() + " Slave attempts to connect." );
						state.output.systemMessage( " Slave attempts to connect." );

					Object connection = registerSlave(state, slaveSock, problemPrototype, useCompression, noDelay, sendBuffer, recvBuffer);
                    if (connection instanceof SlaveConnection)
                        state.output.systemMessage( "Slave " + ((SlaveConnection)connection).slaveName + " connected successfully." );
					else
						state.output.systemMessage( "Slave " + ((String)connection) + " not permitted to connect." );
                    }

                debug( Thread.currentThread().getName() + " The monitor is shutting down." );
                }
            });
        thread.setDaemon(true);
        thread.start();
        }

    /**
       Registers a new slave with the monitor.  Upon registration, a slave is marked as available for jobs.
    */
    public Object registerSlave( EvolutionState state, Socket socket, Problem problemPrototype, boolean useCompression, int noDelay, int sendBuffer, int recvBuffer)
        {
		try
			{
			if (noDelay == 1)
				{
				socket.setTcpNoDelay(true);
				Output.initialMessage("NoDelay -> ON");
				}
			else if (noDelay == 0)
				{
				socket.setTcpNoDelay(false);
				Output.initialMessage("NoDelay -> OFF");
				}

			if (sendBuffer >= 0)
				{
				Output.initialMessage("SendBuffer Changed from " + socket.getSendBufferSize() + " to " + sendBuffer);
				socket.setSendBufferSize(sendBuffer);
				}

			if (recvBuffer >= 0)
				{
				Output.initialMessage("RecvBuffer Changed from " + socket.getReceiveBufferSize() + " to " + recvBuffer);
				socket.setReceiveBufferSize(recvBuffer);
				}
							
			DataInputStream dataIn = null;
			DataOutputStream dataOut = null;
			InputStream tmpIn = socket.getInputStream();
			OutputStream tmpOut = socket.getOutputStream();
			
			// The first thing we do is send a single byte indicating whether we're using compression or not
			tmpOut.write((byte)(useCompression ? 1 : 0));
			
			if (useCompression)
				{
				tmpIn = Output.makeCompressingInputStream(tmpIn);
				tmpOut = Output.makeCompressingOutputStream(tmpOut);
				if (tmpIn == null || tmpOut == null)
					state.output.fatal("You do not appear to have JZLib installed on your system, and so must set eval.compression=false. " +
						"To get JZLib, download from the ECJ website or from http://www.jcraft.com/jzlib/");
				}
																				
			dataIn = new DataInputStream(tmpIn);
			dataOut = new DataOutputStream(tmpOut);

			// write unique integer
			dataOut.writeInt(slaveNum++);
			dataOut.flush();

			// read slave name
			String slaveName = dataIn.readUTF();

			dataOut.writeInt(randomSeed);
			randomSeed+=SEED_INCREMENT;

			// write out additional data as necessary
			problemPrototype.sendAdditionalData(state, dataOut);
			dataOut.flush();
		
			if (isShutdownInProgress())  // no more registrations.  Kill the socket
				{
				try { dataOut.writeByte(Slave.V_SHUTDOWN); } catch (Exception e) { }  // exception, not IOException, because JZLib throws some array exceptions
				try { dataOut.flush(); } catch (Exception e) { }
				try { dataOut.close(); } catch (Exception e) { }
				try { dataIn.close(); } catch (Exception e) { }
				try { socket.close(); } catch (IOException e) { }
				return slaveName;
				}

			SlaveConnection newSlave = new SlaveConnection( state, slaveName, socket, dataOut, dataIn, this );
		
			synchronized(allSlaves)
				{
				allSlaves.addLast(newSlave);
				notifyMonitor(allSlaves);
				}
			synchronized(availableSlaves)
				{
				availableSlaves.addLast(newSlave);
				notifyMonitor(availableSlaves);
				}
			return newSlave;
			}
		catch (IOException ex)
			{
			System.err.println(ex);
			return "UNKNOWN";
			}
        
        }

    /**
       Unregisters a dead slave from the monitor.
    */
    public void unregisterSlave( SlaveConnection slave )
        {
        synchronized(allSlaves)
            {
            if (allSlaves.contains(slave))  // could have been removed if shutdown is in progress
                {
                allSlaves.remove(slave);
                notifyMonitor(allSlaves);
                }
            }
        synchronized(availableSlaves)
            {
            if (availableSlaves.contains(slave))  // could have been removed if shutdown is in progress
                {
                availableSlaves.remove(slave);
                notifyMonitor(availableSlaves);
                }
            }
        }

    /**
       Shuts down the slave monitor (also shuts down all slaves).
    */
    public void shutdown()
        {
        // kill the socket socket and bring down the thread
        setShutdownInProgress(true);
        try
            {
            servSock.close();
            }
        catch (IOException e)
            {
            }
        thread.interrupt();
        try { thread.join(); }
        catch (InterruptedException e) { }
        
        debug("Main Monitor Thread Shut Down");
        // gather all the slaves
        
        while(true)
            {
            SlaveConnection sc = null;
            synchronized(allSlaves)
                {
                if (allSlaves.isEmpty()) break;
                sc = (SlaveConnection)(allSlaves.removeFirst());
                }
            debug("Shutting Down Slave" + sc);
            if (sc != null) 
                sc.shutdown(state);  // it better not be null!
            debug("Shut Down Slave" + sc);
            }
        synchronized(allSlaves)
            {
            notifyMonitor(allSlaves);
            }

        pool.killAll();  // clean up the thread pool
        debug("Shut Down Completed");
        }

    /**
       Schedules a job for execution on one of the available slaves.  The monitor waits until at least one
       slave is available to perform the job.
    */
    public void scheduleJobForEvaluation( final EvolutionState state, Job job )
        {
        if (isShutdownInProgress()) return;  // no more jobs allowed.  This line rejects requests from slaveConnections when THEY'RE shutting down.
        
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
                debug("Waiting for an available slave." );
                waitOnMonitor(availableSlaves);
                }
            notifyMonitor(availableSlaves);
            }       
        debug( "Got a slave available for work." );

        result.scheduleJob(job);

        if( result.numJobs() < maxJobsPerSlave )
            {
            synchronized(availableSlaves) 
                {
                if( !availableSlaves.contains(result)) availableSlaves.addLast(result);  // so we're round-robin
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
        synchronized(allSlaves)
            {
            Iterator<SlaveConnection> iter = allSlaves.iterator();
            while( iter.hasNext() )
                {
                SlaveConnection slaveConnection = iter.next();
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
                Iterator<SlaveConnection> iter = allSlaves.iterator();
                while( iter.hasNext() )
                    {
                    SlaveConnection slaveConnection = iter.next();
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
                    evaluatedIndividuals.addLast( new QueueIndividual(job.inds[x], job.subPops[x]) );
                notifyMonitor(evaluatedIndividuals);
                }
            }
        }

    LinkedList<QueueIndividual> evaluatedIndividuals =  new LinkedList<>();

    public boolean evaluatedIndividualAvailable()
        {
        synchronized(evaluatedIndividuals)
            {
            try { evaluatedIndividuals.getFirst(); return true; }
            catch (NoSuchElementException e) { return false; }
            }
        }


    /** Blocks until an individual comes available */
    public QueueIndividual waitForIndividual()
        {
        while(true)
            {
            synchronized(evaluatedIndividuals)
                {
                if (evaluatedIndividualAvailable())
                    return (QueueIndividual)(evaluatedIndividuals.removeFirst());

                debug("Waiting for individual to be evaluated." );
                waitOnMonitor(evaluatedIndividuals);  // lets go of evaluatedIndividuals loc
                debug("At least one individual has been finished." );
                }
            }
        }

    /** Returns the number of available slave (not busy) */ 
    int numAvailableSlaves()
        {
        int i = 0;
        synchronized(availableSlaves) { i = availableSlaves.size(); }
        return i;
        }

    /**
     * @param s checkpoint file output stream
     * @throws IOException
     */
    void writeObject(ObjectOutputStream out) throws IOException
        {
        state.output.fatal("Not implemented yet: SlaveMonitor.writeObject");
        }
        
    /**
     * @param s checkpoint file input stream.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        state.output.fatal("Not implemented yet: SlaveMonitor.readObject");
        }
    }
