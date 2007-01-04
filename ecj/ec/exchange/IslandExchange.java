/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.exchange;
import java.util.*;
import java.io.*;
import java.net.*;
import ec.*;
import ec.util.*;

/* 
 * IslandExchange.java
 * 
 * Created Sat Feb 10 13:44:11 EST 2001
 * By: Liviu Panait and Sean Luke
 */

/**
 * IslandExchange is an Exchanger which 
 * implements a simple but quite functional asynchronous
 * island model for doing massive parallel distribution of evolution across
 * beowulf clusters.  One of its really nice features is that because everything
 * is in Java, your cluster can have mixed platforms in it (MacOS, Unix, 
 * Windoze, whatever you like).  You can also have multiple processes running
 * on the same machine, as long as they're given different client ports.
 * IslandExchange operates over TCP/IP with Java sockets, and is compatible 
 * with checkpointing.
 *
 * <p>IslandExchange uses an arbitrary graph topology for migrating individuals
 * from island (EC process) to island over the network.  There are a few
 * restrictions for simplicity, however:

 <ul>
 <li> Every island must have the same kind of subpopulations and species.
 <li> Every subpopulation will send the same number of migrants as any 
 other subpopulation.
 <li> Migrants from a subpopulation will go to the same subpopulation.
 </ul>

 * <p>Every island is a <i>client</i>.  Additionally one island is designated
 * a <i>server</i>.  Note that, just like in the Hair Club for Men, the server
 * is also a client.  The purpose of the server is to synchronize the clients
 * so that they all get set up properly and hook up to each other, then to
 * send them small signal messages (like informing them that another client has
 * discovered the ideal individual), and help them gracefully shut down.  Other
 * than these few signals which are routed through the server to the clients,
 * all other information -- namely the migrants themselves -- are sent directly
 * from client to client in a peer-to-peer fashion.
 *
 * <p>The topology of the network is stored solely in the server's parameter
 * database.  When the clients fire up, they first set up "Mailboxes" (where
 * immigrants from other clients will appear), then they go to the server 
 * and ask it who they should connect to to send migrants.  The server tells
 * them, and then they then hook up.  When a client has finished hooking up, it
 * reports this to the server.  After everyone has hooked up, the server tells
 * the clients to begin evolution, and they're off and running.
 *
 * <p>Islands send emigrants to other islands by copying good individuals
 * (selected with a SelectionMethod) and sending the good individuals to
 * the mailboxes of receiving clients.  Once an individual has been received,
 * it is considered to be unevaluated by the receiving island, even though 
 * it had been previously evaluated by the sending island.
 *
 * <p>The IslandExchange model is typically <i>asynchronous</i> because migrants may
 * appear in your mailbox at any time; islands do not wait for each other
 * to complete the next generation.  This is a more efficient usage of network
 * bandwidth.  When an island completes its breeding, it looks inside its
 * mailbox for new migrants.  It then replaces some of its newly-bred
 * individuals (chosen entirely at random)
 * with the migrants (we could have increased the population size so we didn't
 * waste that breeding time, but we were lazy).  It then flushes the mailbox,
 * which patiently sits waiting for more individuals.
 *
 * <p>Clients may also be given different start times and modulos for 
 * migrating.  For example, client A might be told that he begins sending emigrants
 * only after generation 6, and then sends emigrants on every 4 generations beyond
 * that.  The purpose for the start times and modulos is so that not every client
 * sends emigrants at the same time; this also makes better use of network bandwidth.
 *
 * <p>When a client goes down, the other clients deal with it gracefully; they
 * simply stop trying to send to it.  But if the server goes down, the clients
 * do not continue operation; they will shut themselves down.  This means that in
 * general you can shut down an entire island model network just by killing the
 * server process.  However, if the server quits because it runs out of generations,
 * it will wait for the clients to all quit before it finally stops.
 *
 * <p>IslandExchange works correctly with checkpointing.  If you restart from
 * a checkpoint, the IslandExchange will start up the clients and servers again
 * and reconnect.  Processes can start from different checkpoints, of course.
 * However, realize that if you restart from a checkpoint, some migrants
 * may have been lost in transit from island to island.  That's the nature of
 * networking without heavy-duty transaction management! This means that we
 * cannot guarantee that restarting from checkpoint will yield the same results
 * as the first run yielded.
 *
 * <p>Islands are not described in the topology parameters by their
 * IP addresses; instead, they are described by "ids", strings which uniquely 
 * identify each island.  For example, "gilligans-island" might be an id.  :-)
 * This allows you to move your topology to different IP addresses without having
 * to change all your parameter files!  You can even move your topology to totally
 * different machines, and restart from previous checkpoints, and everything
 * should still work correctly.
 *
 * <p>There are times, especially to experiment with dynamics, that you need
 * a <i>synchronous</i> island model.  If you specify synchronicity, the server's
 * stated modulo and offset override any modulii or offsets specified by clients.
 * Everyone will use the server's modulo and offset.  This means that everyone
 * will trade individuals on the same generation.  Additionally, clients will wait
 * until everyone else has traded, before they are permitted to continue evolving.
 * This has the effect of locking all the clients together generation-wise; no
 * clients can run faster than any other clients.
 * 
 * <p>One last item: normally in this model, the server is also a client.  But 
 * if for some reason you need the server to be a process all by itself, without
 * creating a client as well, you can do that.  You spawn such a server differently
 * than the main execution of ECJ.  To spawn a server on a given server params file
 * (let's say it's server.params) but NOT spawn a client, you do:
 <p><pre>
 java ec.exchange.IslandExchange -file server.params
 </pre>
 * <p> ...this sets up a special process which just spawns a server, and doesn't do
 * all the setup of an evolutionary run.  Of course as usual, for each of the 
 * clients, you'll run <tt>java ec.Evolve ...</tt> instead.

 <p><b>Parameters</b><br>
 <p><i>Note:</i> some of these parameters are only necessary for creating
 <b>clients</b>.  Others are necessary for creating the <b>server</b>.
 <table>
 <tr><td valign=top><tt><i>base</i>.chatty</tt><br>
 <font size=-1>boolean, default = true</font></td>
 <td valign=top> Should we be verbose or silent about our exchanges?
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.select</tt><br>
 <font size=-1>classname, inherits and != ec.SelectionMethod</font></td>
 <td valign=top>
 <i>client</i>: The selection method used for picking migrants to emigrate to other islands
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.select-to-die</tt><br>
 <font size=-1>classname, inherits and != ec.SelectionMethod, default is ec.select.RandomSelection</font></td>
 <td valign=top>
 <i>client</i>: The selection method used for picking individuals to be replaced by incoming migrants.
 <b>IMPORTANT Note</b>.  This selection method must <i>not</i> pick an individual based on fitness.
 The selection method will be called just after breeding but <i>before</i> evaluation; many individuals
 will not have had a fitness assigned at that point.  You might want to design a SelectionMethod
 other than RandomSelection, however, to do things like not picking elites to die.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.server-addr</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>
 <i>client</i>: The IP address of the server
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.server-port</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>client</i>: The port number of the server
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.client-port</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>client</i>: The port number of the client (where it will receive migrants) -- this should be different from the server port.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.id</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>
 <i>client</i>: The "name" the client is giving itself.  Each client should have a unique name.  For example, "gilligans-island".
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.compressed</tt><br>
 <font size=-1>bool = <tt>true</tt> (default) or <tt>false</tt></font></td>
 <td valign=top>
 <i>client</i>: Whether the communication with other islands should be compressed or not.  Compressing uses more CPU, but it may also significantly reduce communication.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.i-am-server</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>
 <i>client</i>: Is this client also the server?  If so, it'll read the server parameters and set up a server as well.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.sync</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>
 <i>server</i>: Are we doing a synchronous island model?  If so, the server's modulo and offset override any client's stated modulo and offset.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.start</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>
 <i>server</i>: (Only if island model is synchronous) The generation when islands begin sending emigrants.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.mod</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>
 <i>server</i>: (Only if island model is synchronous) The number of generations islands wait between sending emigrants.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.num-islands</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The number of islands in the topology.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.id</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>
 <i>server</i>: The ID of island #n in the topology.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.num-mig</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The number of islands that island #n sends emigrants to.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.mig.</tt><i>m</i><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The ID of island #m that island #n sends emigrants to.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.size</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The number of emigrants (per subpopulation) that island #n sends to other islands.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.start</tt><br>
 <font size=-1>int >= 0</font></td>
 <td valign=top>
 <i>server</i>: The generation when island #n begins sending emigrants.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.mod</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The number of generations that island #n waits between sending emigrants.
 </td></tr>
 <tr><td valign=top><tt><i>base</i>.island.<i>n</i>.mailbox-capacity</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>
 <i>server</i>: The maximum size (per subpopulation) of the mailbox for island #n.
 </td></tr>
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><tt><i>base</i>.select</tt></td>
 <td>selection method for the client's migrants</td></tr>
 </table>
  
 * @author Liviu Panait & Sean Luke
 * @version 2.0
 */

public class IslandExchange extends Exchanger
    {

    //// Client information

    /** The server address */
    public static final String P_SERVER_ADDRESS = "server-addr";

    /** The server port */
    public static final String P_SERVER_PORT = IslandExchangeServer.P_SERVER_PORT;

    /** The client port */
    public static final String P_CLIENT_PORT = "client-port";

    /** Whether the server is also on this island */
    public static final String P_IS_SERVER = "i-am-server";
    
    /** The id of the island */
    public static final String P_OWN_ID = "id";
        
    /** Whether the communication is compressed or not */
    public static final String P_COMPRESSED_COMMUNICATION = "compressed";

    /** The selection method for sending individuals to other islands */
    public static final String P_SELECT_METHOD = "select";

    /** The selection method for deciding individuals to be replaced by immigrants */
    public static final String P_SELECT_TO_DIE_METHOD = "select-to-die";

    /** How long we sleep in between attempts to connect or look for signals */
    public static final int SLEEP_TIME = 100;

    /** How long we sleep between checking for FOUND messages */
    public static final int FOUND_TIMEOUT = 100;

    /** Whether or not we're chatty */
    public static final String P_CHATTY = "chatty";

    /** Okay signal */
    public static final String OKAY = "okay";

    /** Synchronize signal */
    public static final String SYNC = "sync";

    /** Found signal */
    public static final String FOUND = "found";

    /** Our chattiness */
    boolean chatty;

    /** The thread of the server (is different than null only for the island with the server) */
    public Thread serverThread;

    /** My parameter base -- I need to keep this in order to help the server
        reinitialize contacts */
    // SERIALIZE
    public Parameter base;
    
    /** The address of the server */
    // SERIALIZE
    public String serverAddress;

    /** The port of the server */
    // SERIALIZE
    public int serverPort;

    /** The port of the client mailbox */
    // SERIALIZE
    public int clientPort;

    /** whether the server should be running on the current island or not */
    // SERIALIZE
    public boolean iAmServer;

    /** the id of the current island */
    // SERIALIZE
    public String ownId;

    /** whether the communication is compressed or not */
    // SERIALIZE
    public boolean compressedCommunication;

    /** the selection method for emigrants */
    // SERIALIZE
    public SelectionMethod immigrantsSelectionMethod;

    /** the selection method for individuals to be replaced by immigrants */
    // SERIALIZE
    public SelectionMethod indsToDieSelectionMethod;

    // the mailbox of the current client (exchanger)
    IslandExchangeMailbox mailbox;

    // the thread of the mailbox
    Thread mailboxThread;

    /// Communication with the islands where individuals have to be sent
    // Number of islands to send individuals to
    int number_of_destination_islands;

    /** synchronous or asynchronous communication */
    public boolean synchronous;

    /** how often to send individuals */
    public int modulo;

    /** after how many generations to start sending individuals */
    public int offset;

    /** how many individuals to send each time */
    public int size;

    // Sockets to the destination islands
    Socket[] outSockets;

    // DataOutputStream to the destination islands
    DataOutputStream[] outWriters;

    // so we can print out nice names for our outgoing connections
    String[] outgoingIds;

    // information on the availability of the different islands
    boolean[] running;

    // the capacity of the mailboxes
//    int mailboxCapacity;

    // the socket to the server
    Socket serverSocket;

    // reader and writer to the serverSocket
    DataOutputStream toServer;
    DataInputStream fromServer;

    // am I ONLY a server?
    static boolean just_server;

    public static void main(String[] args) throws InterruptedException
        {
        just_server = true;
        int x;
        ParameterDatabase parameters=null;
        Output output;
        boolean store;
        int verbosity;
        
        // The following is a little chunk of the ec.Evolve code sufficient
        // to get IslandExchange up and running all by itself.
        
        System.err.println("Island Exchange Server\n" +
                           "Used in ECJ by Sean Luke\n");
        
        
        // 0. find the parameter database
        for(x=0;x<args.length-1;x++)
            if (args[x].equals(Evolve.A_FILE))
                {
                try
                    {
                    parameters=new ParameterDatabase(
                        // not available in jdk1.1: new File(args[x+1]).getAbsoluteFile(),
                        new File(new File(args[x+1]).getAbsolutePath()),
                        args);
                    break;
                    }
                catch(FileNotFoundException e)
                    { Output.initialError(
                        "A File Not Found Exception was generated upon" +
                        "reading the parameter file \"" + args[x+1] + 
                        "\".\nHere it is:\n" + e); }
                catch(IOException e)
                    { Output.initialError(
                        "An IO Exception was generated upon reading the" +
                        "parameter file \"" + args[x+1] +
                        "\".\nHere it is:\n" + e); } 
                }
        if (parameters==null)
            Output.initialError(
                "No parameter file was specified." ); 

        // 1. create the output
        store = (parameters.getBoolean(new Parameter(Evolve.P_STORE),null,false));

        verbosity = parameters.getInt(new Parameter(Evolve.P_VERBOSITY),null,0);
        if (verbosity<0)
            Output.initialError("Verbosity should be an integer >= 0.\n",
                                new Parameter(Evolve.P_VERBOSITY)); 

        output = new Output(store,verbosity);
        output.setFlush(
            parameters.getBoolean(new Parameter(Evolve.P_FLUSH),null,false));


        // stdout is always log #0.  stderr is always log #1.
        // stderr accepts announcements, and both are fully verbose 
        // by default.
        output.addLog(ec.util.Log.D_STDOUT,Output.V_VERBOSE,false);
        output.addLog(ec.util.Log.D_STDERR,Output.V_VERBOSE,true); 
        
        
        // this is an ugly, ugly, ugly, UGLY HACK
        // it will only work if we don't ask interesting things
        // of our "EvolutionState"  :-)  you know, things like
        // random number generators or generation numbers!
        
        EvolutionState myEvolutionState = new
            EvolutionState();
        
        myEvolutionState.parameters = parameters;
        myEvolutionState.output = output;
        
        // set me up
        Parameter myBase = new Parameter(EvolutionState.P_EXCHANGER);

        IslandExchange ie = (IslandExchange)parameters.getInstanceForParameterEq(
            myBase, null, IslandExchange.class);
        
        ie.setup(myEvolutionState,myBase);
        ie.fireUpServer(myEvolutionState,myBase);
        ie.serverThread.join();
        
        // flush the output
        output.flush();
        System.err.flush();
        System.out.flush();
        System.exit(0);
        }

    // sets up the Island Exchanger
    public void setup( final EvolutionState state, final Parameter _base )
        {
        base = _base;

        Parameter p;
        
        // get the port of the server
        p = base.push( P_SERVER_PORT );
        serverPort = state.parameters.getInt( p, null, 1 );
        if( serverPort == 0 )
            state.output.fatal( "Could not get the port of the server, or it is invalid.", p );

        chatty = state.parameters.getBoolean(base.push(P_CHATTY), null, true);

        // by default, communication is not compressed
        compressedCommunication = state.parameters.getBoolean(base.push(P_COMPRESSED_COMMUNICATION),null,true);
        if( compressedCommunication )
            state.output.message( "Communication will be compressed" );

        // check whether it has to launch the main server for coordination
        p = base.push( P_IS_SERVER );
        iAmServer = state.parameters.getBoolean( p, null, false );

        // Am I ONLY the server or not?
        if (just_server)
            {
            // print out my IP address
            try
                {
                state.output.message("IP ADDRESS: " + InetAddress.getLocalHost().getHostAddress());
                }
            catch (java.net.UnknownHostException e) { }
            }
        else
            {
            // setup the selection method
            p = base.push( P_SELECT_METHOD );
            immigrantsSelectionMethod = (SelectionMethod)
                state.parameters.getInstanceForParameter( p, null, ec.SelectionMethod.class );
            immigrantsSelectionMethod.setup( state, base );

            // setup the selection method
            p = base.push( P_SELECT_TO_DIE_METHOD );
            if( state.parameters.exists( p ) )
                indsToDieSelectionMethod = (SelectionMethod)
                    state.parameters.getInstanceForParameter( p, null, ec.SelectionMethod.class );
            else // use RandomSelection
                indsToDieSelectionMethod = new ec.select.RandomSelection();
            indsToDieSelectionMethod.setup( state, base );

            // get the address of the server
            p = base.push( P_SERVER_ADDRESS );
            serverAddress = state.parameters.getStringWithDefault( p, null, "" );
            if( serverAddress.equalsIgnoreCase("") )
                state.output.fatal( "Could not get the address of the server.", p );

            // get the port of the client mailbox
            p = base.push( P_CLIENT_PORT );
            clientPort = state.parameters.getInt( p, null, 1 );
            if( clientPort == 0 )
                state.output.fatal( "Could not get the port of the client, or it is invalid.", p );

            // get the id of the island
            p = base.push( P_OWN_ID );
            ownId = state.parameters.getStringWithDefault( p, null, "" );
            if( ownId.equals("") )
                state.output.fatal( "Could not get the Id of the island.", p );
            }
        }

    /** Custom serialization */
    private void writeObject(ObjectOutputStream out) throws IOException
        {
        // this is all we need to write out -- everything else
        // gets recreated when we call reinitializeContacts(...) again...
    
        out.writeObject(base);
        out.writeObject(serverAddress);
        out.writeObject(ownId);
        out.writeBoolean(compressedCommunication);
        out.writeObject(immigrantsSelectionMethod);
        out.writeObject(indsToDieSelectionMethod);
        out.writeInt(serverPort);
        out.writeInt(clientPort);
        out.writeBoolean(iAmServer);
        }

    /** Custom serialization */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        // this is all we need to read in -- everything else
        // gets recreated when we call reinitializeContacts(...) again...
    
        base = (Parameter)(in.readObject());
        serverAddress=(String)(in.readObject());
        ownId=(String)(in.readObject());
        compressedCommunication = in.readBoolean();
        immigrantsSelectionMethod=(SelectionMethod)(in.readObject());
        indsToDieSelectionMethod=(SelectionMethod)(in.readObject());
        serverPort = in.readInt();
        clientPort = in.readInt();
        iAmServer = in.readBoolean();
        }
    
    
    /** Fires up the server. */

    public void fireUpServer(EvolutionState state, Parameter serverBase)
        {
        IslandExchangeServer serv = new IslandExchangeServer();
        serv.setupServerFromDatabase(state,serverBase);
        serverThread = serv.spawnThread();
        }
        

    /** Initializes contacts with other processes, if that's what you're doing. Called at the beginning of an evolutionary run, before a population is set up. */
    public void initializeContacts(EvolutionState state)
        {

        // launch the server
        if( iAmServer )
            {
            fireUpServer(state,base);
            state.output.message( "Server Launched." );
            }
        else
            {
            state.output.message( "I'm just a client." );
            }



        // In this thread, *I* am the client.  I connect to the server
        // and get the information from the server, then I connect
        // to the clients and go through the synchronization process
        // with the server.  Spawn the mailbox. When the server says "go", I'm done with
        // this function.


        /** Make our connections and hook up */
        long l = 0;
        try
            { 
            // spin until we get a connection
            state.output.message("Connecting to Server " + serverAddress + ", port " + serverPort);
            while(true)
                {
                try
                    {
                    serverSocket = new Socket(serverAddress,serverPort);
                    break;
                    }
                catch (IOException e)   // it's not up yet...
                    {
                    l++;
                    try
                        {
                        Thread.sleep( 5000 );
                        }
                    catch( InterruptedException f )
                        {
                        state.output.message(""+f);
                        }
                    state.output.message("Retrying");
                    }
                }
            
            // okay, we're connected.  Send our info.
            state.output.message("Connected to Server after " + (l * SLEEP_TIME) + " ms");
            fromServer = new DataInputStream(serverSocket.getInputStream());
            toServer = new DataOutputStream(serverSocket.getOutputStream());

            // sending the server own contact information
            toServer.writeUTF( ownId );
            toServer.flush();

            // Launch the mailbox thread (read from the server how many sockets to allocate
            // on the mailbox. Obtain the port and address of the mailbox.
            mailbox = new IslandExchangeMailbox( state, clientPort, fromServer.readInt(),
                                                 fromServer.readInt(), ownId, chatty, compressedCommunication );
            mailboxThread = new Thread( mailbox );
            mailboxThread.start();

            // record that the mailbox has been created
            state.output.message( "IslandExchangeMailbox created." );

            // tell the server the address and port of the mailbox
            try
                {
                toServer.writeUTF( InetAddress.getLocalHost().getHostAddress() );
                toServer.flush();
                state.output.message("My address is: " + InetAddress.getLocalHost().getHostAddress() );
                }
            catch( UnknownHostException e )
                {
                state.output.fatal( "Could not get the address of the local computer." );
                }
            toServer.writeInt( mailbox.getPort() );
            toServer.flush();

            // read from the server the modulo, offset and size it has to use.
            // this parameters allow an extendable/modifiable version where different
            // islands send different number of individuals (based on the size of their populations)
            synchronous = ( fromServer.readInt() == 1 );
            if( synchronous )
                {
                state.output.message( "The communication will be synchronous." );
                }
            else
                {
                state.output.message( "The communication will be asynchronous." );
                }
            modulo = fromServer.readInt();
            offset = fromServer.readInt();
            size = fromServer.readInt();

            // read the number of islands it has to send messages to
            number_of_destination_islands = fromServer.readInt();

            // allocate the arrays
            outSockets = new Socket[ number_of_destination_islands ];
            outWriters = new DataOutputStream[ number_of_destination_islands ];
            running = new boolean[ number_of_destination_islands ];
            outgoingIds = new String[ number_of_destination_islands ];

            // open connections to each of the destination islands
            for( int y = 0 ; y < number_of_destination_islands ; y++ )
                {
                // get the address and the port
                String address = fromServer.readUTF().trim();
                int port = fromServer.readInt();
                try
                    {
                    try
                        {
                        state.output.message( "Trying to connect to " + address + " : " + port );
                        // try opening a connection
                        outSockets[y] = new Socket( address, port );
                        }
                    catch( UnknownHostException e )
                        {
                        // gracefully handle communication errors
                        state.output.warning( "Unknown host exception while the client was opening a socket to " + address + " : " + port );
                        running[y] = false;
                        continue;
                        }

                    if( compressedCommunication )
                        {
                        outWriters[y] = new DataOutputStream(new CompressingOutputStream(outSockets[y].getOutputStream()));

                        // read the mailbox's id, then write my own id
                        outgoingIds[y] = new DataInputStream(new CompressingInputStream(outSockets[y].getInputStream())).readUTF().trim();
                        }
                    else
                        {
                        outWriters[y] = new DataOutputStream(outSockets[y].getOutputStream());

                        // read the mailbox's id, then write my own id
                        outgoingIds[y] = new DataInputStream(outSockets[y].getInputStream()).readUTF().trim();
                        }
                        
                    outWriters[y].writeUTF(ownId);
                    outWriters[y].flush();
            
                    running[y] = true;
                    }
                catch( IOException e )
                    {
                    // this is caused if the server had problems locating information
                    // on the mailbox of the other island, therefore remember the
                    // communication with this island is not setup properly
                    state.output.warning( "IO exception while the client was opening sockets to other islands' mailboxes :" + e );
                    running[y] = false;
                    }
                }

            // synchronization stuff: tells the server it finished connecting to other mailboxes
            toServer.writeUTF( OKAY );
            toServer.flush();

            // wait for the run signal
            fromServer.readUTF();

            }
        catch( IOException e )
            {
            state.output.fatal( "Error communicating to the server." );
            }

        // at this point, the mailbox is looking for incoming messages
        // form other islands. we have to exit the function. there is
        // one more thing to be done: to check for the server sending a
        // FOUND signal. In order to do this, we set the socket to the
        // server as non-blocking, and verify that for messages from the
        // server in the runComplete function
        try
            {
            serverSocket.setSoTimeout( FOUND_TIMEOUT );
            }
        catch( SocketException e )
            {
            state.output.fatal( "Could not set the connection to the server to non-blocking." );
            }

        }

    /** Initializes contacts with other processes, if that's what you're doing.  Called after restarting from a checkpoint. */
    public void reinitializeContacts(EvolutionState state)
        {
        // This function is almost the same as initializeContacts.
        // The only main difference is that when reinitializeContacts
        // is called, it's called because I started up from a checkpoint file.
        // This means that I'm in the middle of evolution, so the modulo
        // and start might cause me to update more recently than if I had
        // started fresh.  But maybe it won't make a difference in this method
        // if the way I determine when I'm firing off migrants is on a
        // generation-by-generation basis.

        initializeContacts( state );

        }



    public Population preBreedingExchangePopulation(EvolutionState state)
        {
        // sending individuals to other islands
        // BUT ONLY if my modulo and offset are appropriate for this
        // generation (state.generation)
        // I am responsible for returning a population.  This could
        // be a new population that I created fresh, or I could modify
        // the existing population and return that.

        // else, check whether the emigrants need to be sent
        if( ( state.generation >= offset ) &&
            ( ( modulo == 0 ) || ( ( ( state.generation - offset ) % modulo ) == 0 ) ) )
            {

            // send the individuals!!!!

            // for each of the islands where we have to send individuals
            for( int x = 0 ; x < number_of_destination_islands ; x++ )
                try
                    {

                    // check whether the communication is ok with the current island
                    if( running[x] )
                        {

                        if (chatty) state.output.message( "Sending " + size + " emigrants to island " + outgoingIds[x] );

                        // for each of the subpopulations
                        for( int subpop = 0 ; subpop < state.population.subpops.length ; subpop++ )
                            {
                            // send the subpopulation
                            outWriters[x].writeInt( subpop );

                            // send the number of individuals to be sent
                            // it's better to send this information too, such that islands can (potentially)
                            // send different numbers of individuals
                            outWriters[x].writeInt( size );

                            // select "size" individuals and send then to the destination as emigrants
                            immigrantsSelectionMethod.prepareToProduce( state, subpop, 0 );
                            for( int y = 0 ; y < size ; y++ ) // send all necesary individuals
                                {
                                int index = immigrantsSelectionMethod.produce( subpop, state, 0 );
                                state.population.subpops[subpop].individuals[index].
                                    writeGenotype( state, outWriters[x] );
                                outWriters[x].flush();  // just in case the individuals didn't do a println
                                }
                            immigrantsSelectionMethod.finishProducing( state, subpop, 0 ); // end the selection step
                            }
                        }
                    }
                catch( IOException e )
                    {
                    running[x] = false;
                    }
            }

        return state.population;

        }


    public Population postBreedingExchangePopulation(EvolutionState state)
        {
        // receiving individuals from other islands
        // same situation here of course.

        // if synchronous communication, synchronize with the mailbox
        if( ( state.generation >= offset ) && synchronous &&
            ( ( modulo == 0 ) || ( ( ( state.generation - offset ) % modulo ) == 0 ) ) )
            {

            state.output.message( "Waiting for synchronization...." );

            // set the socket to the server to blocking
            try
                {
                serverSocket.setSoTimeout( 0 );
                }
            catch( SocketException e )
                {
                state.output.fatal( "Could not set the connection to the server to blocking." );
                }

            try
                {
                // send the sync message
                toServer.writeUTF( SYNC );
                toServer.flush();
                // wait for the okay message
                String temp = fromServer.readUTF();
                if( temp.equals( IslandExchangeServer.GOODBYE ) )
                    {
                    alreadyReadGoodBye = true;
                    }
                }
            catch( IOException e )
                {
                state.output.fatal( "Could not communicate to the server. Exiting...." );
                }

            // set the socket to the server to non-blocking
            try
                {
                serverSocket.setSoTimeout( FOUND_TIMEOUT );
                }
            catch( SocketException e )
                {
                state.output.fatal( "Could not set the connection to the server to non-blocking." );
                }

            state.output.message( "Synchronized. Reading individuals...." );

            }

        // synchronize, because immigrants is also accessed by the mailbox thread
        synchronized( mailbox.immigrants )
            {
            for( int x = 0 ; x < mailbox.immigrants.length ; x++ )
                {
                if( mailbox.nImmigrants[x] > 0 )
                    {
                    if (chatty) state.output.message( "Immigrating " +  mailbox.nImmigrants[x] + " individuals from mailbox for subpopulation " + x );

                    boolean[] selected = new boolean[ state.population.subpops[x].individuals.length ];
                    int[] indeces = new int[ mailbox.nImmigrants[x] ];
                    for( int i = 0 ; i < selected.length ; i++ )
                        selected[i] = false;
                    indsToDieSelectionMethod.prepareToProduce( state, x, 0 );
                    for( int i = 0 ; i < mailbox.nImmigrants[x] ; i++ )
                        {
                        do {
                            indeces[i] = indsToDieSelectionMethod.produce( x, state, 0 );
                            } while( selected[indeces[i]] );
                        selected[indeces[i]] = true;
                        }
                    indsToDieSelectionMethod.finishProducing( state, x, 0 );

                    // there is no need to check for the differences in size: the mailbox.immigrants,
                    // state.population.subpops and the mailbox.person2die should have the same size
                    for( int y = 0 ; y < mailbox.nImmigrants[x] ; y++ )
                        {

                        // read the individual
                        state.population.subpops[x].
                            individuals[ indeces[y] ] = mailbox.immigrants[x][y];

                        // reset the evaluated flag (the individuals are not evaluated in the current island */
                        state.population.subpops[x].
                            individuals[ indeces[y] ].evaluated = false;

                        }

                    // reset the number of immigrants in the mailbox for the current subpopulation
                    // this doesn't need another synchronization, because the thread is already synchronized
                    mailbox.nImmigrants[x] = 0;

                    }
                }

            }

        return state.population;
        }

    // if the GOODBYE message sent by the server gets read in the wrong place, this
    // variable is set to true
    boolean alreadyReadGoodBye = false;

    // keeps the message to be returned next time on runComplete
    String message;

    /** Called after preBreedingExchangePopulation(...) to evaluate whether or not
        the exchanger wishes the run to shut down (with ec.EvolutionState.R_FAILURE).
        This would happen for two reasons.  First, another process might have found
        an ideal individual and the global run is now over.  Second, some network
        or operating system error may have occurred and the system needs to be shut
        down gracefully.
        This function does not return a String as soon as it wants to exit (another island found
        the perfect individual, or couldn't connect to the server). Instead, it sets a flag, called
        message, to remember next time to exit. This is due to a need for a graceful
        shutdown, where checkpoints are working properly and save all needed information. */
    public String runComplete(EvolutionState state)
        {

        // first test the flag, and exit if it was previously set
        if( message != null ) // if an error occured earlier
            {
            return message;
            }

        // check whether the server sent a FOUND message.
        // if it did, check whether it should exit or not
        try
            {
            // read a line. if it is successful, it means that the server sent a FOUND message
            // (this is the only message the server sends right now), and it should set the flag
            // for exiting next time when in this procedure
            String ww = fromServer.readUTF();
            if( ww != null || alreadyReadGoodBye ) // FOUND message sent from the server
                {
                // we should exit because some other individual has
                // found the perfect fit individual
                if( state.quitOnRunComplete )
                    {
                    message = "Exit: Another island found the perfect individual.";
                    state.output.message( "Another island found the perfect individual. Exiting...." );
                    toServer.writeUTF( OKAY );
                    toServer.flush();
                    }
                else
                    {
                    state.output.message( "Another island found the perfect individual." );
                    }
                }
            else // ( ww == null ) // the connection with the server was closed
                {
                // we should exit, because we cannot communicate with the
                // server anyway
                message = "Exit: Could not communicate with the server.";
                state.output.warning( "Could not communicate with the server. Exiting...." );
                }
            }
        catch( InterruptedIOException e )
            {
            // here don't do anything: it reaches this point when the server is on, but nobody found
            // the perfect individual. in this case, it should just return null, so that the
            // execution continues
            }
        catch( IOException e )
            {
            // some weird error
            // report it in a warning
            state.output.warning( "Some weird IO exception reported by the system in the IslandExchange::runComplete function.  Is it possible that the server has crashed?" );
            }

        return null;
        }

    /** Closes contacts with other processes, if that's what you're doing.  Called at the end of an evolutionary run. result is either ec.EvolutionState.R_SUCCESS or ec.EvolutionState.R_FAILURE, indicating whether or not an ideal individual was found. */
    public void closeContacts(EvolutionState state, int result)
        {

        state.output.message( "Shutting down the mailbox" );
        // close the mailbox and wait for the thread to terminate
        mailbox.shutDown();
        try
            {
            mailboxThread.join();
            }
        catch( InterruptedException e )
            {
            }
        state.output.message( "Mailbox shut down" );

        // if the run was successful (perfect individual was found)
        // then send a message to the server that it was found
        if( result == EvolutionState.R_SUCCESS )
            {
            try
                {
                toServer.writeUTF( FOUND );
                toServer.flush();
                }
            catch( IOException e ) {}
            }

        // close socket to server
        try
            {
            serverSocket.close();
            }
        catch( IOException e )
            {
            }

        // close out-going sockets
        for( int x = 0 ; x < number_of_destination_islands ; x++ )
            {
            // catch each exception apart (don't take into consideration the running variables)
            try
                {
                if( running[x] )
                    outSockets[x].close();
                }
            catch( IOException e )
                {
                }
            }

        // if the island also hosts the server, wait till it terminates
        if( iAmServer )
            {
            state.output.message( "Shutting down the server" );
            try
                {
                serverThread.join();
                }
            catch( InterruptedException e )
                {
                }
            state.output.message( "Server shut down" );
            }

        }

    /* (non-Javadoc)
     * @see ec.EvolutionState#finish(int)
     */
    public void finish(int result) {
    }

    /* (non-Javadoc)
     * @see ec.EvolutionState#startFromCheckpoint()
     */
    public void startFromCheckpoint() {
    }

    /* (non-Javadoc)
     * @see ec.EvolutionState#startFresh()
     */
    public void startFresh() {
    }

    /* (non-Javadoc)
     * @see ec.EvolutionState#evolve()
     */
    public int evolve()
        throws InternalError {
        return 0;
    }
        
    }

/** Class that contains all the mailbox functionality. It is supposed to wait on a new thread for incoming
    immigrants from other islands (it will receive in the constructor the number of islands that will send
    messages to the current island). Waiting on sockets is non-blocking, such that the order in which the
    islands send messages is unimportant. When immigrants are received, they are stored in a special buffer
    called immigrants. The storage is managed in a queue-like fashion, such that when the storage is full,
    the first immigrants that came are erased (hopefully the storage will be emptied fast enough such that
    this case doesn't appear too often).
    All accesses to the "immigrants" variable (also applies to nImmigrants) should be done only in the presence
    of synchronization, because there might be other threads using them too. The number of immigrants for each
    of the subpopulations (nImmigrants[x]) is between 0 and the size of the queue structure (received as a
    parameter in the constructor). */
class IslandExchangeMailbox implements Runnable
    {

    /** How much to wait before starting checking for immigrants */
    public static final int SLEEP_BETWEEN_CHECKING_FOR_IMMIGRANTS = 1000;

    /** How much to wait on a socket for a message, before starting to wait on another socket */
    public static final int CHECK_TIMEOUT = 1000;

    /** How much to wait while synchronizing */
    public static final int SYNCHRONIZATION_SLEEP = 100;

    /**  storage for the incoming immigrants: 2 sizes: the subpopulation and the index of the emigrant */
    public Individual[][] immigrants;

    /** the number of immigrants in the storage for each of the subpopulations */
    public int[] nImmigrants;

    // auxiliary variables to manage the queue storages
    int[] person2die;

    // the socket where it listens for incomming messages
    ServerSocket serverSocket;

    // the number of islands that send messages to the current mailbox
    int n_incoming;

    // whether the information on sockets is compressed or not (receives this information in the constructor)
    boolean compressedCommunication;

    // the sockets and readers for receiving incoming messages
    Socket[] inSockets;
    DataInputStream[] dataInput;
    String[] incomingIds;      // so we can print out nice names for our incoming connections

    // the state of the islands it is communicating to
    boolean[] running;

    // the state (to display messages mainly)
    EvolutionState state;

    // synchronization variable
    Boolean syncVar;

    // My ID
    String myId;
    
    boolean chatty;

    /**
       Public constructor used to initialize most of the parameters of the mailbox:
       state_p : the EvolutionState, used mainly for displaying messages
       port : the port used to listen for incoming messages
       n_incoming_p : the number of islands that will send messages to the current island
       how_many : how many immigrants to manage in the queue-like storage for each of the subpopulations
    */
    public IslandExchangeMailbox( final EvolutionState state_p, int port, int n_incoming_p, int how_many, String _myId, boolean chatty, boolean _compressedCommunication )
        {
        myId = _myId;
        compressedCommunication = _compressedCommunication;
        
        this.chatty = chatty;
    
        // initialize public variables from the parameters of the constructor
        state = state_p;
        n_incoming = n_incoming_p;

        Parameter p_numsubpops = new Parameter( ec.Initializer.P_POP ).push( ec.Population.P_SIZE );
        int numsubpops = state.parameters.getInt(p_numsubpops,null,1);
        if ( numsubpops == 0 )
            {
            // later on, Population will complain with this fatally, so don't
            // exit here, just deal with it and assume that you'll soon be shut
            // down
            }

        // allocate the storages:
        // - immigrants = storage for the immigrants that will come to the current island
        //   - first dimension: the number of subpopulations
        //   - second dimension: how many immigrants to store for each of the subpopulations.
        // - person2die = where to insert next in the queue structure "immigrants"
        // - nImmigrants = how many immigrants there are in the storage "immigrants" for each of the subpopulations
        immigrants = new Individual[ numsubpops ][ how_many ];
        person2die = new int[ numsubpops ];
        nImmigrants = new int[ numsubpops ];

        // set the synchronization variable to false (it will be set to true to signal exiting the waiting loop)
        syncVar = Boolean.FALSE;

        // create the ServerSocket to listen to incoming messages
        try
            {
            serverSocket = new ServerSocket( port, n_incoming );
            }
        catch( IOException e )
            {
            state.output.fatal( "Could not start mailbox for incoming messages.  Perhaps the port (" + port + ") is bad?\n...or someone else already has it?");
            }

        // allocate the sockets and the readers (will be used in the near future)
        inSockets = new Socket[ n_incoming ];
        dataInput = new DataInputStream[ n_incoming ];
        incomingIds = new String[ n_incoming ];

        // allocate the status of the different readers
        running = new boolean[ n_incoming ];

        }

    /** The main functionality of the mailbox: waiting for incoming messages and dealing with the incoming immigrants */
    public void run()
        {

        // wait for the "n_incoming" incoming connections from different islands, and initialize
        // the sockets and the readers to communicate with (receive messages from) them. All the
        // sockets are set to be non-blocking, such that they can be checked alternatively without
        // waiting for messages on a particular one.
        for( int x = 0 ; x < n_incoming ; x++ )
            {
            try
                {
                inSockets[x] = serverSocket.accept();

                DataOutputStream dataOutput;

                if( compressedCommunication )
                    {
                    dataInput[x] = new DataInputStream(new CompressingInputStream(inSockets[x].getInputStream()));
                    dataOutput = new DataOutputStream(new CompressingOutputStream(inSockets[x].getOutputStream()));
                    }
                else
                    {
                    dataInput[x] = new DataInputStream(inSockets[x].getInputStream());
                    dataOutput = new DataOutputStream(inSockets[x].getOutputStream());
                    }

                // send my id, then read an id
                dataOutput.writeUTF(myId);
                dataOutput.flush();
                incomingIds[x] = dataInput[x].readUTF().trim();    

                state.output.message( "Island " + incomingIds[x] + " connected to my mailbox" );

                // set the socket to non-blocking
                inSockets[x].setSoTimeout( CHECK_TIMEOUT );
                running[x] = true;
                }
            catch (IOException e)
                {
                running[x] = false;
                state.output.fatal( "An exception was generated while creating communication structures for island " + x + ".  Here it is: " + e );
                }
            }

        state.output.message( "All islands have connected to my client." );

        // variable used for deciding (based on the synchronized variable "syncVar") when to exit
        boolean shouldExit = false;

        // enter the main loop
        do
            {

            // wait some (do not check all the time, cause it would be a waste of time and computational resources)
            try
                {
                Thread.sleep( SLEEP_BETWEEN_CHECKING_FOR_IMMIGRANTS );
                }
            catch( InterruptedException e )
                {
                }

            // for each of the connections established with the islands
            for( int x = 0 ; x < n_incoming ; x++ )
                {
                if( running[x] )
                    {
                    try
                        {
                        // enter an infinite loop to receive all the messages form the "x"s island
                        // it will exit the loop as soon as there are no more messages coming from
                        // the "x"s island (non-blocking socket)
                        while( true )
                            {
                            // read the subpopulation where the immigrants need to be inserted. In case there
                            // is no incoming message, an exception will be generated and the infinite loop
                            // will be exited (the mailbox will search the next socket (communication link)
                            // for incoming messages
                            int subpop = dataInput[x].readInt();
                            
                            // if it gets to this point, it means that a number of individuals will be sent
                            // it is the time to set up the receiving storages
                            
                            // set the socket to blocking for reading the individuals
                            try
                                {
                                inSockets[x].setSoTimeout( 0 );
                                }
                            catch( SocketException e )
                                {
                                state.output.warning( "Could not set the socket to blocking while receiving individuals in the mailbox." );
                                }
                            
                            // how many individuals will be received in the current dialogue?
                            int how_many_to_come = dataInput[x].readInt();
                            
                            if (chatty) state.output.message( "Receiving " + how_many_to_come + " immigrants for subpopulation "  + subpop + " from island " + incomingIds[x]);

                            // synchronize on the immigrants (such that other threads cannot access it during its
                            // being modified)
                            synchronized( immigrants )
                                {
                                
                                // in case the immigrants buffer was emptied, the person2die is not reset (it is not public)
                                // so we have to reset it now
                                if( nImmigrants[subpop] == 0 ) // if it was reset
                                    person2die[subpop] = 0; // reset the person2die[x]
                                
                                // loop in order to receive all the incoming individuals in the current dialogue
                                for( int ind = 0 ; ind < how_many_to_come ; ind++ )
                                    {
                                    // read the individual
                                    try
                                        {
                                        // read the emigrant in the storage
                                        immigrants[subpop][person2die[subpop]] = state.population.subpops[subpop].species.
                                            newIndividual( state, dataInput[x] );

                                        //state.output.message( "Individual received." );
                                        
                                        // increase the queue index
                                        if( person2die[subpop] == immigrants[subpop].length - 1 )
                                            person2die[subpop] = 0;
                                        else
                                            person2die[subpop]++;
                                        
                                        // can increment it without synchronization, as we do synchronization on the immigrants
                                        if( nImmigrants[subpop] < immigrants[subpop].length )
                                            nImmigrants[subpop]++;

                                        }
                                    catch( IOException e )
                                        {
                                        // i hope it will also never happen :)
                                        state.output.message( "IO exception while communicating with an island" );
                                        running[x] = false;
                                        continue;
                                        }
                                    catch( NumberFormatException e )
                                        {
                                        // it happens when the socket is closed and cannot be doing any reading
                                        state.output.message( "IO exception while communicating with an island" );
                                        running[x] = false;
                                        continue;
                                        }
                                    }
                                } // end synchronized block on "immigrants"
                            
                            // set the socket to non-blocking (after current set of immigrants is over)
                            try
                                {
                                inSockets[x].setSoTimeout( CHECK_TIMEOUT );
                                }
                            catch( SocketException e )
                                {
                                state.output.warning( "Could not set the socket to non-blocking while receiving individuals in the mailbox." );
                                }
                            }
                        }
                    catch( InterruptedIOException e )
                        {
                        // here everything is ok
                        // just that there were no messages
                        }
                    catch( IOException e )
                        {
                        // now this is not nice
                        // report the error so that the programmer can fix it (hopefully)
                        state.output.message( "IO exception while communicating with an island" );
                        running[x] = false;
                        }
                    catch( NumberFormatException e )
                        {
                        // error received when some sockets break
                        state.output.message( "Socket closed" );
                        running[x] = false;
                        }
                    }
                }

            // again with synchronization, try to access the syncVar to check whether the mailbox needs to finish
            // running (maybe some other island already found the perfect individual, or the resources of the current
            // run have been wasted)
            synchronized( syncVar )
                {
                // get the value of the syncVar. If it is true, we should exit.
                shouldExit = syncVar.booleanValue();
                }
            }
        while( !shouldExit );

        // close the sockets (don't care about the running, but deal with exceptions)
        try
            {
            // close the ServerSocket
            serverSocket.close();
            }
        catch( IOException e )
            {
            }
        for( int x = 0 ; x < n_incoming ; x++ )
            {
            try
                {
                // close the sockets to communicate (receive messages) with the other islands
                inSockets[x].close();
                }
            catch( IOException e )
                {
                continue;
                }
            }

        }

    /**
       Method used to shutdown the mailbox. What it does is that it closes all communication links (sockets)
       and sets the syncVar to true (such that if the run() method is run on another thread, it will exit the
       loop and terminate.
    */
    public void shutDown()
        {

        // set the syncVar to true (such that if another thread executes this.run(), it will exit the main loop
        // (hopefully, the information from the server was correct
        synchronized( syncVar )
            {
            syncVar = Boolean.TRUE;
            }

        }

    /**
       Return the port of the ServerSocket (where the islands where the other islands should
       connect in order to send their emigrants).
    */
    public int getPort()
        {
        // return the port of the ServerSocket
        return serverSocket.getLocalPort();
        }

    }

/**
   The IslandExchangeServer is the class that manages the main server that coordinates all the islands. The class
   implements Runnable (for running on a different thread).
*/
class IslandExchangeServer implements Runnable
    {
    
    /*

    The server-specific parameters look roughly like this:

    exch.server-port = 8021
    exch.num-islands = 3
    exch.island.0.id = SurvivorIsland
    exch.island.0.num-mig = 1
    exch.island.0.mig.0 = GilligansIsland
    exch.island.0.size = 5
    exch.island.0.mod = 2
    exch.island.0.start = 4
    exch.island.1.id = GilligansIsland
    exch.island.1.mod = 1
    exch.island.1.start = 2
    exch.island.1.size = 10
    exch.island.1.num-mig = 2
    exch.island.1.mig.0 = SurvivorIsland
    exch.island.1.mig.1 = GilligansIsland
    exch.island.2.id = BermudaIsland
    exch.island.2.mod = 2
    ...
    */

    //// Server information

    /** The server port */
    public static final String P_SERVER_PORT = "server-port";

    /** The number of islands */
    public static final String P_NUM_ISLANDS = "num-islands";

    /** The parameter for the island's information */
    public static final String P_ISLAND = "island";

    /** The id */
    public static final String P_ID = "id";

    // The number of islands that will send emigrants to the current island
    public static final String P_NUM_INCOMING_MIGRATING_COUNTRIES = "num-incoming-mig";

    /** The number of islands where emigrants will be sent */
    public static final String P_NUM_MIGRATING_COUNTRIES = "num-mig";

    /** the parameter for migrating islands' ids */
    public static final String P_MIGRATING_ISLAND = "mig";

    /** The size of the mailbox (for each of the subpopulations) */
    public static final String P_MAILBOX_CAPACITY = "mailbox-capacity";

    /** The parameter for the modulo (how many generations should pass between consecutive sendings of individuals */
    public static final String P_MODULO = "mod";

    /** The number of emigrants to be sent */
    public static final String P_SIZE = "size";

    /** How many generations to pass at the beginning of the evolution before the first emigration from the current island */
    public static final String P_OFFSET = "start";

    /** Whether the execution should be synchronous or asynchronous */
    public static final String P_SYNCHRONOUS = "sync";

    /** The run message to be sent to the clients */
    public static final String RUN = "run";

    /** How much to wait for the found message (on a non-blocking socket) */
    public static final int FOUND_TIMEOUT = 100;

    /** How much to sleep between checking for a FOUND message */
    public static final int SLEEP_TIME = 100;

    /** The final message to be sent to all islands when an individual has been found */
    public static final String GOODBYE = "bye-bye";

    /** The found message */
    public static final String FOUND = IslandExchange.FOUND;

    /** The okay message */
    public static final String OKAY = IslandExchange.OKAY;

    /** The synchronize message */
    public static final String SYNC = IslandExchange.SYNC;

/** A class indicating all the information the server knows about
    a given island, including its mod, size, offset, and all the
    migrating islands it hooks to, etc. */
    public class IslandExchangeIslandInfo
        {
        /** how often to send individuals */
        public int modulo;
        /** the mailbox capacity (for each of the subpopulations) */
        public int mailbox_capacity;
        /** what generation to start sending individuals */
        public int offset;
        // how many individuals to send
        public int size;
        // to how many islands to send individuals
        public int num_mig;
        // the ids of the contries to send individuals to
        public String[] migrating_island_ids;
        // how many islands will send individuals to the mailbox
        public int num_incoming;

        // also later filled in:
        // the address of the mailbox where to receive information
        public String address;
        // the port of the mailbox
        public int port;
        }

    // The number of islands in the topology
    int numIslands;

    // The port of the server
    int serverPort;

    // the server's socket
    ServerSocket serverSocket;

    // Hashtable for faster lookup of information regarding islands
    Hashtable info;

    // Hashtable to count how many islands send individuals to each of the islands
    Hashtable info_immigrants;

    EvolutionState state;

    // Index of island ids sorted by parameter file
    String[] island_ids;
    
    // Index of island ids sorted by order of connection
    String[] connected_island_ids;


    // variables used if the execution is synchronous
    int global_modulo, global_offset;
    boolean synchronous;

    // how many individuals asked to be synchronized (when it reaches the total number of
    // running clients, the server resets this variable and allows everybody to continue running)
    boolean[] who_is_synchronized;

    /** This setup should get called from the IslandExchange setup method. */
    public void setupServerFromDatabase(final EvolutionState state_p, final Parameter base)
        {

        // Store the evolution state for further use in other functions ( ie. run )
        state = state_p;

        // Don't bother with getting the default base -- we're a singleton!

        Parameter p;

        // get the number of islands
        p = base.push( P_NUM_ISLANDS );
        numIslands = state.parameters.getInt( p, null, 1 );
        if( numIslands == 0 )
            state.output.fatal( "The number of islands must be >0.", p );

        // get the port of the server
        p = base.push( P_SERVER_PORT );
        serverPort = state.parameters.getInt( p, null, 1 );
        if( serverPort == 0 )
            state.output.fatal( "The server port must be >0.", p );
        
        // information on the islands = hashtable of ID and socket information
        info = new Hashtable( numIslands );

        // initialize the hash table to count how many islands send individuals
        // to each of the islands
        info_immigrants = new Hashtable( numIslands );

        // allocate the ids sorted by parameter file
        island_ids = new String[ numIslands ];
        
        // allocate the ids sorted by connection
        connected_island_ids = new String[ numIslands ] ;

        // check whether the execution is synchronous or asynchronous
        // if it is synchronous, there should be two parameters in the parameters file:
        // the global modulo and offset (such that the islands coordinate smoothly)
        p = base.push( P_SYNCHRONOUS );

        // get the value of the synchronous parameter (default is false)
        synchronous = state.parameters.getBoolean( p, null, false );

        // if synchronous, read the other two global parameters
        if( synchronous )
            {

            state.output.message( "The communication will be synchronous." );

            // get the global modulo
            p = base.push( P_MODULO );
            global_modulo = state.parameters.getInt( p, null, 1 );
            if( global_modulo == 0 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p );
            
            // get the global offset
            p = base.push( P_OFFSET );
            global_offset = state.parameters.getInt( p, null, 0 );
            if( global_offset == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p );
            
            }
        else
            {

            state.output.message( "The communication will be asynchronous." );

            }

        // get a new local base
        Parameter islandBase = base.push( P_ISLAND );

        // load the island topology
        for( int x = 0 ; x < numIslands ; x++ )
            {

            IslandExchangeIslandInfo ieii = new IslandExchangeIslandInfo();

            Parameter localBase = islandBase.push( "" + x );

            // get the id of the current island
            p = localBase.push( P_ID );
            island_ids[x] = state.parameters.getStringWithDefault( p, null, "" );
            if( island_ids[x].equals("") )
                state.output.fatal( "Parameter not found.", p );

            // get the mailbox capacity of the imigration from the current island
            p = localBase.push( P_MAILBOX_CAPACITY );
            ieii.mailbox_capacity = state.parameters.getInt( p, null, 0 );
            if( ieii.mailbox_capacity == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p );

            // get the size of the imigration from the current island
            p = localBase.push( P_SIZE );
            ieii.size = state.parameters.getInt( p, null, 0 );
            if( ieii.size == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p );

            // if synchronous execution, use the global modulo and offset
            if( synchronous )
                {
                ieii.modulo = global_modulo;
                ieii.offset = global_offset;
                }
            else
                {
                // get the modulo of the imigration from the current island
                p = localBase.push( P_MODULO );
                ieii.modulo = state.parameters.getInt( p, null, 1 );
                if( ieii.modulo == 0 )
                    state.output.fatal( "Parameter not found, or it has an incorrect value.", p );

                // get the offset of the imigration from the current island
                p = localBase.push( P_OFFSET );
                ieii.offset = state.parameters.getInt( p, null, 0 );
                if( ieii.offset == -1 )
                    state.output.fatal( "Parameter not found, or it has an incorrect value.", p );
                }

            // mark as uninitialized
            ieii.port = -1;

            // insert the id in the hashset with the ids of the islands
            info.put( island_ids[x], ieii );
            }

        // get the information on destination islands (with checking for consistency)
        for( int x = 0 ; x < numIslands ; x++ )
            {

            IslandExchangeIslandInfo ieii = (IslandExchangeIslandInfo)info.get( island_ids[x] );

            if( ieii == null )
                {
                state.output.error( "Inexistent information for island " + island_ids[x] + " stored in the server's information database." );
                continue;
                }

            Parameter localBase = islandBase.push( "" + x );

            // get the number of islands where individuals should be sent
            p = localBase.push( P_NUM_MIGRATING_COUNTRIES );
            ieii.num_mig = state.parameters.getInt( p, null, 0 );
            if( ieii.num_mig == -1 )
                state.output.fatal( "Parameter not found, or it has an incorrect value.", p );

            // if there is at least 1 destination islands
            if( ieii.num_mig > 0 )
                {

                // allocate the storage for ids
                ieii.migrating_island_ids = new String[ ieii.num_mig ];

                // store a new base parameter
                Parameter ll;
                ll = localBase.push( P_MIGRATING_ISLAND );

                // for each of the islands
                for( int y = 0 ; y < ieii.num_mig ; y++ )
                    {

                    // read the id & check for errors
                    ieii.migrating_island_ids[y] = state.parameters.getStringWithDefault( ll.push(""+y), null, null );
                    if( ieii.migrating_island_ids[y] == null )
                        state.output.fatal( "Parameter not found.", ll.push(""+y) );
                    else if( !info.containsKey( ieii.migrating_island_ids[y] ) )
                        state.output.fatal( "Unknown island.", ll.push(""+y) );
                    else
                        {
                        // insert this knowledge into the hashtable for counting how many islands
                        // send individuals to each island
                        Integer integer = (Integer)info_immigrants.get( ieii.migrating_island_ids[y] );
                        if( integer == null )
                            info_immigrants.put( ieii.migrating_island_ids[y],
                                                 new Integer(1) );
                        else
                            info_immigrants.put( ieii.migrating_island_ids[y],
                                                 new Integer( integer.intValue() + 1 ) );
                        }
                    }
                }

            // save the information back in the hash table
            // info.put( island_ids[x], ieii );                         // unneccessary -- Sean

            }

        for( int x = 0 ; x < numIslands ; x++ )
            {

            IslandExchangeIslandInfo ieii = (IslandExchangeIslandInfo)info.get( island_ids[x] );

            if( ieii == null )
                {
                state.output.fatal( "Inexistent information for island " + island_ids[x] + " stored in the server's information database." );
                }

            Integer integer = (Integer)info_immigrants.get( island_ids[x] );

            // if the information does not exist in the hasthable,
            // it means no islands send individuals there
            if( integer == null )
                ieii.num_incoming = 0;
            else
                ieii.num_incoming = integer.intValue();

            // save the information back in the hash table
            // info.put( island_ids[x], ieii );                 // unneccessary -- Sean

            }

        // allocate and reset this variable to false
        who_is_synchronized = new boolean[ numIslands ];

        for( int x = 0 ; x < numIslands ; x++ )
            who_is_synchronized[x] = false;

        }

    /** The main function running in the thread */
    public void run()
        {

        // sockets to communicate to each of the islands
        Socket[] con = new Socket[numIslands];

        // readers and writters for communication with each island
        DataInputStream[] dataIn = new DataInputStream[numIslands];
        DataOutputStream[] dataOut = new DataOutputStream[numIslands];



        // whether each client is working (and communicating with the server) or not
        boolean[] clientRunning = new boolean[numIslands];

        // initialize the running status of all clients
        for( int x = 0 ; x < numIslands ; x++ )
            clientRunning[x] = true;

        try
            {
            // create a server
            serverSocket = new ServerSocket(serverPort,numIslands);
            }
        catch ( IOException e )
            {
            state.output.fatal( "Error creating a socket on port " + serverPort );
            }

        // for each of the islands
        for(int x=0;x<numIslands;x++)
            {
            try
                {
                // set up connection with the island
                con[x] = serverSocket.accept();

                // initialize the reader and the writer
                dataIn[x] = new DataInputStream(con[x].getInputStream());
                dataOut[x] = new DataOutputStream(con[x].getOutputStream());

                // read the id
                connected_island_ids[x] = dataIn[x].readUTF().trim();

                state.output.message( "Island " + connected_island_ids[x] + " logged in" );

                // check whether the id appears in the information at the server
                if( !info.containsKey( connected_island_ids[x] ) )
                    {
                    state.output.error( "Incorrect ID (" + connected_island_ids[x] + ")" );
                    clientRunning[x] = false;
                    continue;
                    }

                IslandExchangeIslandInfo ieii = (IslandExchangeIslandInfo)info.get( connected_island_ids[x] );

                // redundant check, i know....
                if( ieii == null )
                    {
                    state.output.error( "Can't get IslandExchangeInfo for " + connected_island_ids[x]  );
                    clientRunning[x] = false;
                    continue;
                    }

                // check whether an island with this id already registered with the server
                if( ieii.port >= 0 )
                    {
                    state.output.error( "Multiple islands are claiming the same ID (" + connected_island_ids[x] + ")" );
                    clientRunning[x] = false;
                    continue;
                    }
            
                // send the number of ids that will be send through the communication link
                dataOut[x].writeInt( ieii.num_incoming );

                // send the capacity of the mailbox
                dataOut[x].writeInt( ieii.mailbox_capacity );
                                
                dataOut[x].flush();

                // read the address and port of the island
                ieii.address = dataIn[x].readUTF().trim();
                ieii.port = dataIn[x].readInt();

                state.output.message( "" + x + ": Island " + connected_island_ids[x] + " has address " +
                                      ieii.address + " : " + ieii.port );

                // re-insert the information in the hash table
                // info.put( id, ieii );                                // unnecessary -- Sean
                }
            catch( IOException e )
                {
                state.output.error( "Could not open connection #" + x );
                clientRunning[x] = false;
                }
            }

        state.output.exitIfErrors();

        // By this time, all mailboxes have been started and
        // they should be waiting for incoming messages. this is because
        // in order to send the server the information about the address and port
        // of the mailbox, they have to start them first. This is the reason
        // that makes us be able to start connecting without other synchronization
        // stuff right at this point.

        // Now, I think, we've got a 1:1 mapping of keys to items in the info hashtable
        // So we tell everyone who they will communicate to

        for( int x = 0 ; x < numIslands ; x++ )
            {
            if( clientRunning[x] )
                {
                IslandExchangeIslandInfo ieii = (IslandExchangeIslandInfo)info.get( connected_island_ids[x] );

                if( ieii == null )
                    {
                    state.output.warning( "There is no information about island " + connected_island_ids[x]);
                    clientRunning[x] = false;
                    continue;
                    }

                try
                    {
                    // send the synchronous, modulo, offset and size information to the current islands
                    if( synchronous )
                        dataOut[x].writeInt( 1 );
                    else
                        dataOut[x].writeInt( 0 );
                    dataOut[x].writeInt( ieii.modulo );
                    dataOut[x].writeInt( ieii.offset );
                    dataOut[x].writeInt( ieii.size );

                    // send the number of address-port pairs that will be sent
                    dataOut[x].writeInt( ieii.num_mig );

                    for( int y = 0 ; y < ieii.num_mig ; y++ )
                        {
                        IslandExchangeIslandInfo temp;

                        temp = (IslandExchangeIslandInfo)info.get( ieii.migrating_island_ids[y] );

                        if( temp == null )
                            {
                            state.output.warning( "There is incorrect information on the island " + connected_island_ids[x]  );
                            dataOut[x].writeUTF( " " );
                            dataOut[x].writeInt( -1 );
                            }
                        else
                            {
                            state.output.message( "Island " + connected_island_ids[x] + " should connect to island " +
                                                  ieii.migrating_island_ids[y] + " at " + temp.address + " : " + temp.port );
                            dataOut[x].writeUTF( temp.address );
                            dataOut[x].writeInt( temp.port );
                            }
                        }
                    dataOut[x].flush();
                    }
                catch( IOException e )
                    {
                    // other errors while reading
                    state.output.message("Server: Island " + island_ids[x] + " dropped connection");
                    clientRunning[x] = false;
                    continue;
                    }
                catch( NullPointerException e )
                    {
                    // other errors while reading
                    state.output.message("Server: Island " + island_ids[x] + " dropped connection");
                    clientRunning[x] = false;
                    try
                        {
                        dataIn[x].close();
                        dataOut[x].close();
                        con[x].close();
                        }
                    catch( IOException f )
                        {
                        }
                    continue;
                    }
                }
            }

        try
            {
            // Next we wait until everyone acknowledges this
            for(int x=0;x<dataIn.length;x++)
                {
                dataIn[x].readUTF();
                }

            // Now we tell everyone to start running
            for(int x=0;x<dataOut.length;x++)
                {
                dataOut[x].writeUTF( RUN );
                dataOut[x].flush();
                }
            }
        catch( IOException e )
            {
            }

        // Okay we've sent off our information.  Now we wait until a client
        // tells us that he's found the solution, or until all the clients
        // have broken connections 
        
        for(int x=0;x<con.length;x++)
            {
            try
                {
                con[x].setSoTimeout(FOUND_TIMEOUT);
                }
            catch( SocketException e )
                {
                state.output.error( "Could not set the connect with island " + x + " to non-blocking." );
                }
            }

        boolean shouldExit = false;

        while(!shouldExit)
            {
            // check whether there is at least one client running
            // otherwise the server might continue functioning just because the last client crashed or finished connection
            shouldExit = true;
            for( int x = 0 ; x < dataOut.length ; x++ )
                if( clientRunning[x] )
                    {
                    shouldExit = false;
                    break;
                    }
            if( shouldExit )
                break;

            // sleep a while
            try
                {
                Thread.sleep(SLEEP_TIME);
                }
            catch( InterruptedException e )
                {
                }

            String ww;

            for(int x=0;x<dataOut.length;x++)
                {
                if (clientRunning[x])
                    {

                    // initialize ww
                    ww = "";

                    // check to see if he's still up, and if he's
                    // sent us a "I found it" signal
                    try
                        {
                        ww = dataIn[x].readUTF().trim();
                        }
                    catch( InterruptedIOException e )
                        {
                        // means that it run out of time and got no message,
                        // so it should just continue with the other sockets
                        continue;
                        }
                    catch( IOException e )
                        {
                        // other errors while reading
                        state.output.message("Server: Island " + island_ids[x] + " dropped connection");
                        clientRunning[x] = false;
                        continue;
                        }
                    catch( NullPointerException e )
                        {
                        // other errors while reading
                        state.output.message("Server: Island " + island_ids[x] + " dropped connection");
                        clientRunning[x] = false;
                        try
                            {
                            dataIn[x].close();
                            dataOut[x].close();
                            con[x].close();
                            }
                        catch( IOException f )
                            {
                            }
                        continue;
                        }

                    if ( ww == null )  // the connection has been broken
                        {
                        state.output.message("Server: Island " + island_ids[x] + " dropped connection");
                        clientRunning[x] = false;
                        try
                            {
                            dataIn[x].close();
                            dataOut[x].close();
                            con[x].close();
                            }
                        catch( IOException e )
                            {
                            }
                        }
                    else if( ww.equals( FOUND ) ) // he found it!
                        {
                        // inform everyone that they need to shut down --
                        // we do not need to wrap
                        // our println statements in anything, they just
                        // return even if the client has broken the connection
                        for(int y=0;y<dataOut.length;y++)
                            {
                            if (clientRunning[y])
                                {
                                try
                                    {
                                    dataOut[y].writeUTF(GOODBYE);
                                    dataOut[y].close();
                                    dataIn[y].close();
                                    con[y].close();
                                    }
                                catch( IOException e )
                                    {
                                    }
                                }
                            }
                        // now we can just get out of all this and
                        // quit the thread 
                        shouldExit=true;
                        break;
                        }
                    else if( ww.equals( SYNC ) )
                        {
                        who_is_synchronized[x] = true;

                        boolean complete_synchronization = true;

                        for( int y = 0 ; y < numIslands ; y++ )
                            complete_synchronization = complete_synchronization &&
                                ( ( ! clientRunning[y] ) || who_is_synchronized[y] );

                        // if the number of total running islands is smaller than the
                        // number of islands that ask for synchronization, let them continue
                        // running
                        if( complete_synchronization )
                            {

                            for( int y = 0 ; y < numIslands ; y++ )
                                {
                                // send the okay message (the client can continue executing)
                                if( clientRunning[y] )
                                    try
                                        {
                                        dataOut[y].writeUTF( OKAY );
                                        dataOut[y].flush();
                                        }
                                    catch( IOException e ) {}
                                // reset the who_is_synchronized variable
                                who_is_synchronized[y] = false;
                                }
                            }

                        }
                    }
                }
            }
        state.output.message( "Server Exiting" );    
        }

    /** Here we spawn off the thread on ourselves */
    public Thread spawnThread()
        {
        Thread thread = new Thread( this );
        thread.start();
        return thread;
        }

    }

