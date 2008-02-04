/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.eval;

import java.io.*;
import java.net.*;

import ec.*;
import ec.util.*;

/**
 * MasterProblemServer.java
 *

 <p>The server awaits for incoming slaves to connect.  Upon one such connection is established,
 the server creates and starts a worker thread to manage all the incoming communication from
 this slave.
 
 <p><b>Parameters</b><br>
 <table>
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

 * @author Liviu Panait
 * @version 1.0 
 */

public class MasterProblemServer
    implements Runnable, Serializable
    {
    public static final String P_EVALMASTERPORT = "eval.master.port";
        
    public static final String P_EVALCOMPRESSION = "eval.compression";

    public static final String P_MAXIMUMNUMBEROFCONCURRENTJOBSPERSLAVE = "eval.masterproblem.max-jobs-per-slave";

    // display debugging information?
    boolean showDebugInfo;

    // The slave monitor
    public SlaveMonitor slaveMonitor;
        
    /**
     *  The socket where slaves connect.
     */
    public ServerSocket servSock;
        
    /**
     * Indicates whether compression is used over the socket IO streams.
     */
    public boolean useCompression;
        
    public EvolutionState state;
        
    /**
     * Indicates to the background thread that a shutdown is in progress and to
     * stop processing.
     */
    private boolean shutdownInProgress = false;

    // the random seed for the slaves
    private int randomSeed;

    // simple constructor
    public MasterProblemServer( boolean showDebugInfo )
        {
        this.showDebugInfo = showDebugInfo;
        }

    /**
     * After the MasterProblemServer is created, it needs to be told to
     * initialize itself from information in the parameter database. It
     * distinguishes between starting fresh and restoring from a checkpoint by
     * the state of the randomStates and slaves arrays.
     * 
     * @param state the evolution state
     */
    public void setupServerFromDatabase( final EvolutionState state )
        {
        this.state = state;
                
        int port = state.parameters.getInt(
            new Parameter( P_EVALMASTERPORT ),null);
                
        int maxNumberOfJobs = state.parameters.getInt(
            new Parameter( P_MAXIMUMNUMBEROFCONCURRENTJOBSPERSLAVE ),null);

        useCompression = state.parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
                
        try
            {
            servSock = new ServerSocket(port);
            }
        catch( IOException e )
            {
            state.output.fatal("Unable to bind to port " + port + ": " + e);
            }
                
        slaveMonitor = new SlaveMonitor(showDebugInfo,maxNumberOfJobs); // no maximum number of slaves!

        randomSeed = (int)(System.currentTimeMillis());
        }

    /**
     * Indicates that the background thread is to shut down and closes the
     * server socket. (should probably be synchronized).
     */
    public void shutdown()
        {
        state.output.systemMessage("Shutting down server thread.");
        shutdownInProgress = true;
        try
            {
            servSock.close();
            }
        catch (IOException e)
            {
            }
        slaveMonitor.shutdown( state );
        }
        
    /**
     * Writes the slaves' random states to the checkpoint file.
     * 
     * @param s checkpoint file output stream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException
        {
        state.output.message("Not implemented yet: MasterProblemServer.writeObject");
        System.exit(1);
        }
        
    /**
     * Restores the slaves random states from the checkpoint file.
     * 
     * @param s checkpoint file input stream.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        state.output.message("Not implemented yet: MasterProblemServer.readObject");
        System.exit(1);
        }

    final void debug(String s)
        {
        if (showDebugInfo) { System.err.println(Thread.currentThread().getName() + "->" + s); }
        }

    /**
       The run method waits for incoming slaves, and launches new worker threads (one per incoming slave)
       to handle the communication with the slave.
    */
    public void run()
        {
        Thread.currentThread().setName("MasterProblemServer::    ");
        Socket slaveSock;
                
        while (!shutdownInProgress)
            {
            slaveSock = null;
            while( slaveSock==null && !shutdownInProgress )
                {
                try
                    {
                    slaveSock = servSock.accept();
                    }
                catch( IOException e ) { slaveSock = null; }
                }

            debug(Thread.currentThread().getName() + " Slave attempts to connect." );

            if( shutdownInProgress )
                {
                debug( Thread.currentThread().getName() + " The server is shutting down." );
                break;
                }

            SlaveData newSlave = null;

            try
                {
                DataInputStream dataIn = null;
                DataOutputStream dataOut = null;
                InputStream tmpIn = slaveSock.getInputStream();
                OutputStream tmpOut = slaveSock.getOutputStream();
                if (this.useCompression)
                    {
                    debug("Using Compression");
                    tmpIn = new CompressingInputStream(tmpIn);
                    tmpOut = new CompressingOutputStream(tmpOut);
                    /*
                      com.jcraft.jzlib.ZInputStream in = new com.jcraft.jzlib.ZInputStream(tmpIn, com.jcraft.jzlib.JZlib.Z_BEST_SPEED);
                      in.setFlushMode(com.jcraft.jzlib.JZlib.Z_PARTIAL_FLUSH);
                      tmpIn = in;
                      com.jcraft.jzlib.ZOutputStream out = new com.jcraft.jzlib.ZOutputStream(tmpOut, com.jcraft.jzlib.JZlib.Z_BEST_SPEED);
                      out.setFlushMode(com.jcraft.jzlib.JZlib.Z_PARTIAL_FLUSH);
                      tmpOut = out;
                    */
                    }
                                                                                                
                dataIn = new DataInputStream(tmpIn);
                dataOut = new DataOutputStream(tmpOut);
                String slaveName = dataIn.readUTF();

                MersenneTwisterFast random = new MersenneTwisterFast(randomSeed);
                randomSeed++;
                
                // Write random state for eval thread to slave
                random.writeState(dataOut);
                dataOut.flush();

                newSlave = new SlaveData( state, slaveName, slaveSock, dataOut, dataIn, slaveMonitor );

                slaveMonitor.registerSlave(newSlave);
                state.output.systemMessage( "Slave " + slaveName + " connected successfully." );
                }
            catch (IOException e)
                {
                if( newSlave != null )
                    {
                    newSlave.shutdown(state);
                    }
                }
            }
        }
        
    /**
     * Creates and starts a background thread for this server. 
     * 
     * @return the background thread
     */
    public Thread spawnThread()
        {
        Thread thread = new Thread(this);
        thread.start();
        return thread;
        }
        
    }

