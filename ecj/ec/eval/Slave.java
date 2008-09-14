/*
  Copyright 2006 by Sean Paus, Sean Luke, and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Oct 8, 2004
 */
package ec.eval;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleEvolutionState;
import ec.util.*;

/**
 * Slave.java
 *
 
 <p>Slave is the main entry point for a slave evaluation process.  The slave works with a master process,
 receiving individuals from the master, evaluating them, and reporting the results back to the master, thus
 enabling distributed evolution.  
 
 <p>Slave replicates most of the functionality of
 the ec.Evolve class, for example in terms of parameters and checkpointing.  This is mostly because it needs
 to bootstrap and set up the EvolutionState in much the same way that ec.Evolve does.  Additionally, depending
 on settings below, the Slave may act like a mini-evolver on the individuals it receives from the master.
 
 <p>Like ec.Evolve, Slave is run with like this:
 
 <p><tt>java ec.eval.Slave -file </tt><i>parameter_file [</i><tt>-p </tt><i>parameter=value]*</i>
 
 <p>This starts a new slave, using the parameter file <i>parameter_file</i>.
 The user can provide optional overriding parameters on the command-line with the <tt>-p</tt> option.
 
 <p>Slaves need to know some things in order to run: the master's IP address and socket port number,
 whether to do compression, and whether or not to return individuals or just fitnesses.
 
 <p>Slaves presently always run in single-threaded mode and receive their random number generator seed
 from the master.  Thus they ignore any seed parameters given to them.
 
 <p>Slaves run in one of three modes:
 
 <ul>
 <p><li>"Regular" mode, which does a loop where it receives N individuals, evaluates them, and
 returns either the individuals or their new fitnesses.
 <p><li>"Regular Coevolutionary" mode, which does a loop where it receives N individuals to assess together in
 a single coevolutionary evaluation, evaluates them, and returns either the individuals or their new fitnesses
 (or only some fitnesses if only some are requested).
 <p><li>"Evolve" mode, which does a loop where it receives
 N individuals, evaluates them, and if there's some more time left, does a little evolution on those individuals as
 if they were a population, then when the time is up, the current individuals in the population are returned in lieu
 of the original individuals.  In this second form, individuals MUST be returned, not fitnesses.  This mode is not
 available if you're doing coevolution.
 </ul>
 
 <p><b>Parameters</b><br>
 <table>
 
 <tr><td valign=top><tt>eval.slave-name</tt><br>
 <font size=-1> String </font></td>
 <td valign=top>(the slave's name, only for debugging purposes.  If not specified, the slave makes one up.)</td></tr>

 <tr><td valign=top><tt>eval.master.host</tt><br>
 <font size=-1> String </font></td>
 <td valign=top>(the IP Address of the master.)</td></tr>

 <tr><td valign=top><tt>eval.master.port</tt><br>
 <font size=-1> integer &gt;= 1024 </font></td>
 <td valign=top>(the socket port number of the master.)</td></tr>

 <tr><td valign=top><tt>eval.compression</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default) </font></td>
 <td valign=top>(should we use compressed streams in communicating with the master?)</td></tr>

 <tr><td valign=top><tt>eval.run-evolve</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default) </font></td>
 <td valign=top>(should we immediately evaluate the individuals and return them (or their fitnesses), or if we have extra time (defined by eval.runtime),
 should we do a little evolution on our individuals first?)</td></tr>

 <tr><td valign=top><tt>eval.runtime</tt><br>
 <font size=-1> integer &gt; 0 </font></td>
 <td valign=top>(if eval.run-evolve is true, how long (in milliseconds wall-clock time) should we allow the individuals to evolve?)</td></tr>

 <tr><td valign=top><tt>eval.return-inds</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default) </font></td>
 <td valign=top>(should we return whole individuals or (if false) just the fitnesses of the individuals?  This must be TRUE if eval.run-evolve is true.)</td></tr>

 <!-- 
 <tr><td valign=top><tt>nostore</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should the ec.util.Output facility <i>not</i> store announcements in memory?)</td></tr>

 <tr><td valign=top><tt>flush</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should I flush all output as soon as it's printed (useful for debugging when an exception occurs))</td></tr>
 -->

 <tr><td valign=top><tt>verbosity</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(the ec.util.Output object's verbosity)</td></tr>

 <tr><td valign=top><tt>state</tt><br>
 <font size=-1>classname, inherits and != ec.EvolutionState</font></td>
 <td valign=top>(the EvolutionState object class)</td></tr>

 <tr><td valign=top><tt>print-accessed-params</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(at the end of a run, do we print out a list of all the parameters requested during the run?)</td></tr>

 <tr><td valign=top><tt>print-used-params</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(at the end of a run, do we print out a list of all the parameters actually <i>used</i> during the run?)</td></tr>

 <tr><td valign=top><tt>print-unaccessed-params</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(at the end of a run, do we print out a list of all the parameters NOT requested during the run?)</td></tr>

 <tr><td valign=top><tt>print-unused-params</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(at the end of a run, do we print out a list of all the parameters NOT actually used during the run?)</td></tr>

 <tr><td valign=top><tt>print-all-params</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(at the end of a run, do we print out a list of all the parameters stored in the parameter database?)</td></tr>

 </table>
 
 * 
 * @author Liviu Panait, Sean Paus, Keith Sullivan, and Sean Luke
 */
 
 
 
public class Slave 
    {
    public final static String P_PRINTACCESSEDPARAMETERS = "print-accessed-params";
        
    public final static String P_PRINTUSEDPARAMETERS = "print-used-params";
        
    public final static String P_PRINTALLPARAMETERS = "print-all-params";
        
    public final static String P_PRINTUNUSEDPARAMETERS = "print-unused-params";
        
    public final static String P_PRINTUNACCESSEDPARAMETERS = "print-unaccessed-params";
        
    public final static String P_EVALSLAVENAME = "eval.slave-name";
        
    public final static String P_EVALMASTERHOST = "eval.master.host";
        
    public final static String P_EVALMASTERPORT = "eval.master.port";
        
    public final static String P_EVALCOMPRESSION = "eval.compression";
    
    public final static String P_RETURNINDIVIDUALS = "eval.return-inds";
        
    public static final String P_SUBPOP = "pop.subpop";
    
    public static final byte V_NOTHING = 0;
    public static final byte V_INDIVIDUAL = 1;
    public static final byte V_FITNESS = 2;
        
    public static final byte V_SHUTDOWN = 0;
    public static final byte V_EVALUATESIMPLE = 1;
    public static final byte V_EVALUATEGROUPED = 2;
//    public static final byte V_CHECKPOINT = 3;
        
    /* The argument indicating that we're starting up from a checkpoint file. */
//    public static final String A_CHECKPOINT = "-checkpoint";
        
    /** The argument indicating that we're starting fresh from a new parameter file. */
    public static final String A_FILE = "-file";
        
    /* flush announcements parameter */
    // public static final String P_FLUSH = "flush";
        
    /* nostore parameter */
    // public static final String P_STORE = "store";
        
    /** verbosity parameter */
    public static final String P_VERBOSITY = "verbosity";
        
    /** state parameter */
    public static final String P_STATE = "state";
        
    /** Time to run evolution on the slaves in seconds */ 
    public static final String P_RUNTIME = "eval.runtime"; 
    public static int runTime=0; 
        
    /** Should slave run its own evolutionary process? */ 
    public static final String P_RUNEVOLVE = "eval.run-evolve"; 
    public static boolean runEvolve=false; 
        
    /** How long we sleep in between attempts to connect to the master (in milliseconds). */
    public static final int SLEEP_TIME = 100;
        
    public static void main(String[] args)
        {
        EvolutionState state = null;
        ParameterDatabase parameters = null;
        Output output = null;

        int verbosity;
        boolean store;
        int x;
                
        // 0. find the parameter database
        for (x = 0; x < args.length - 1; x++)
            if (args[x].equals(A_FILE))
                {
                try
                    {
                    parameters = new ParameterDatabase(
                        // not available in jdk1.1: new File(args[x+1]).getAbsoluteFile(),
                        new File(new File(args[x + 1]).getAbsolutePath()),
                        args);
                    break;
                    }
                catch(FileNotFoundException e)
                    { 
                    Output.initialError(
                        "A File Not Found Exception was generated upon" +
                        "reading the parameter file \"" + args[x+1] + 
                        "\".\nHere it is:\n" + e); }
                catch(IOException e)
                    { 
                    Output.initialError(
                        "An IO Exception was generated upon reading the" +
                        "parameter file \"" + args[x+1] +
                        "\".\nHere it is:\n" + e); } 
                }
        if (parameters == null)
            Output.initialError("No parameter file was specified." ); 
                
        // 5. Determine whether or not to return entire Individuals or just Fitnesses
        //    (plus whether or not the Individual has been evaluated).
        
        boolean returnIndividuals = parameters.getBoolean(new Parameter(P_RETURNINDIVIDUALS),null,false);
                
                
        // 6. Open a server socket and listen for requests
        String slaveName = parameters.getString(
            new Parameter(P_EVALSLAVENAME),null);
                
        String masterHost = parameters.getString(
            new Parameter(P_EVALMASTERHOST),null );
        int masterPort = parameters.getInt(
            new Parameter(P_EVALMASTERPORT),null);
        boolean useCompression = parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
                
        runTime = parameters.getInt(new Parameter(P_RUNTIME), null, 0); 
                
        runEvolve = parameters.getBoolean(new Parameter(P_RUNEVOLVE),null,false); 
        
        if (runEvolve && !returnIndividuals)
            {
            Output.initialError("You have the slave running in 'evolve' mode, but it's only returning fitnesses to the master, not whole individuals.  This is almost certainly wrong.",
                new Parameter(P_RUNEVOLVE), new Parameter(P_RETURNINDIVIDUALS));
            }
        
        Output.initialMessage("ECJ Slave");
        if (runEvolve) Output.initialMessage("Running in Evolve mode, evolve time is " + runTime + " milliseconds");
        if (returnIndividuals) Output.initialMessage("Whole individuals will be returned");
        else Output.initialMessage("Only fitnesses will be returned");
        
        
        // Continue to serve new masters until killed.
        while (true)
            {
            try
                {
                Socket socket;
                long connectAttemptCount = 0;
                Output.initialMessage("Connecting to master at "+masterHost+":"+masterPort);
                while (true)
                    {
                    try
                        {
                        socket = new Socket(masterHost, masterPort);
                        break;
                        }
                    catch (ConnectException e)   // it's not up yet...
                        {
                        connectAttemptCount++;
                        try
                            {
                            Thread.sleep(SLEEP_TIME);
                            }
                        catch( InterruptedException f )
                            {
                            }
                        }
                    }
                Output.initialMessage("Connected to master after " + (connectAttemptCount * SLEEP_TIME) + " ms");
                                
                DataInputStream dataIn = null;
                DataOutputStream dataOut = null;

                try
                    {
                    InputStream tmpIn = socket.getInputStream();
                    OutputStream tmpOut = socket.getOutputStream();
                    if (useCompression)
                        {
                        //Output.initialError("JDK 1.5 has broken compression.  For now, you must set eval.compression=false");
                        /*
                          tmpIn = new CompressingInputStream(tmpIn);
                          tmpOut = new CompressingOutputStream(tmpOut);
                        */
                        tmpIn = Output.makeCompressingInputStream(tmpIn);
                        tmpOut = Output.makeCompressingOutputStream(tmpOut);
                        if (tmpIn == null || tmpOut == null)
                            Output.initialError("You do not appear to have JZLib installed on your system, and so must set eval.compression=false.  " +
                                "To get JZLib, download from the ECJ website or from http://www.jcraft.com/jzlib/");
                        }
                                                
                    dataIn = new DataInputStream(tmpIn);
                    dataOut = new DataOutputStream(tmpOut);
                    }
                catch (IOException e)
                    {
                    Output.initialError("Unable to open input stream from socket:\n"+e);
                    }
                                
                // specify the slaveName
                if (slaveName==null)
                    {
                    slaveName = socket.getLocalAddress().toString() + "/" + System.currentTimeMillis();
                    Output.initialMessage("No slave name specified.  Using: " + slaveName);
                    }
                                
                dataOut.writeUTF(slaveName);
                dataOut.flush();

                // 1. create the output
                // store = parameters.getBoolean(new Parameter(P_STORE), null, false);
                
                verbosity = parameters.getInt(new Parameter(P_VERBOSITY), null, 0);
                if (verbosity < 0)
                    Output.initialError("Verbosity should be an integer >= 0.\n",
                        new Parameter(P_VERBOSITY));
                
                if (output != null) output.close();
                output = new Output(true, verbosity);
                //output.setFlush(
                //    parameters.getBoolean(new Parameter(P_FLUSH),null,false));
                
                // stdout is always log #0. stderr is always log #1.
                // stderr accepts announcements, and both are fully verbose
                // by default.
                output.addLog(ec.util.Log.D_STDOUT, Output.V_VERBOSE, false);
                output.addLog(ec.util.Log.D_STDERR, Output.V_VERBOSE, true);

                output.systemMessage(Version.message());


                // 2. set up thread values

/*
  int breedthreads = parameters.getInt(
  new Parameter(Evolve.P_BREEDTHREADS),null,1);

  if (breedthreads < 1)
  output.fatal("Number of breeding threads should be an integer >0.",
  new Parameter(Evolve.P_BREEDTHREADS),null);


  int evalthreads = parameters.getInt(
  new Parameter(Evolve.P_EVALTHREADS),null,1);

  if (evalthreads < 1)
  output.fatal("Number of eval threads should be an integer >0.",
  new Parameter(Evolve.P_EVALTHREADS),null);
*/

                int breedthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_BREEDTHREADS));
                int evalthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_EVALTHREADS));

                // Note that either breedthreads or evalthreads (or both) may be 'auto'.  We don't warn about this because
                // the user isn't providing the thread seeds.
                

                // 3. create the Mersenne Twister random number generators,
                // one per thread

                MersenneTwisterFast[] random = new MersenneTwisterFast[breedthreads > evalthreads ? 
                    breedthreads : evalthreads];
        
                int seed = dataIn.readInt();
                for(int i = 0; i < random.length; i++)
                    random[i] = new MersenneTwisterFast(seed++);

                // 4. Set up the evolution state
                
                // what evolution state to use?
                state = (EvolutionState)
                    parameters.getInstanceForParameter(new Parameter(P_STATE),null,
                        EvolutionState.class);
                state.parameters = new ParameterDatabase();
                state.parameters.addParent(parameters);
                state.random = random;
                state.output = output;
                state.evalthreads = evalthreads;
                state.breedthreads = breedthreads;
        
                state.setup(state, null);
                state.population = state.initializer.setupPopulation(state, 0);
                

                // Is this a Simple or Grouped ProblemForm?
                int problemType;
                try
                    {
                    while (true)
                        {
                        EvolutionState newState = state;
                        
                        if (runEvolve) 
                            {
                            // Construct and use a new EvolutionState.  This will be inefficient the first time around
                            // as we've set up TWO EvolutionStates in a row with no good reason.
                            ParameterDatabase coverDatabase = new ParameterDatabase();  // protect the underlying one
                            coverDatabase.addParent(state.parameters);
                            newState = (EvolutionState) Evolve.initialize(coverDatabase, 0);
                            newState.startFresh();
                            newState.output.message("Replacing random number generators, ignore above seed message");
                            newState.random = state.random;  // continue with RNG
                            }
                        
                        // 0 means to shut down
                        System.err.println("reading next problem");
                        problemType = dataIn.readByte();
                        System.err.println("Read problem: " + (int)problemType);
                        switch (problemType)
                            {
                            case V_SHUTDOWN:
                                socket.close();
                                return;  // we're outa here
                            case V_EVALUATESIMPLE:
                                evaluateSimpleProblemForm(newState, returnIndividuals, dataIn, dataOut, args);
                                break;
                            case V_EVALUATEGROUPED:
                                evaluateGroupedProblemForm(newState, returnIndividuals, dataIn, dataOut);
                                break;
                            default:
                                state.output.fatal("Unknown problem form specified: "+problemType);
                            }
                        //System.err.println("Done Evaluating Individual");
                        }

                    } catch (IOException e)    
                    {
                    // Since an IOException can happen here if the peer closes the socket
                    // on it's end, we don't necessarily have to exit.  Maybe we don't
                    // even need to print a warning, but we'll do so just to indicate
                    // something happened.
                    state.output.warning("Unable to read type of evaluation from master.  Maybe the master closed its socket and exited?:\n"+e);
                    }
                } 
            catch (UnknownHostException e)
                {
                state.output.fatal(e.getMessage());
                }
            catch (IOException e)
                {
                state.output.fatal("Unable to connect to master:\n" + e);
                }
            }
        }
            
    public static void evaluateSimpleProblemForm( EvolutionState state, boolean returnIndividuals,
        DataInputStream dataIn, DataOutputStream dataOut, String[] args )
        {
        ParameterDatabase params=null; 
        
        // first load the individuals
        int numInds=1; 
        try
            {
            numInds = dataIn.readInt();
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read the number of individuals from the master:\n"+e);
            }
        
        // load the subpops 
        int[] subpops = new int[numInds];  // subpops desired by each ind
        int[] indsPerSubpop = new int[state.population.subpops.length];  // num inds for each subpop
        for(int i = 0; i < numInds; i++)
            {
            try
                {
                subpops[i] = dataIn.readInt();
                if (subpops[i] < 0 || subpops[i] >= state.population.subpops.length)
                    state.output.fatal("Bad subpop number for individual #" + i + ": " + subpops[i]);
                indsPerSubpop[subpops[i]]++;
                }
            catch (IOException e)
                {
                state.output.fatal("Unable to read the subpop number from the master:\n"+e);
                }
            }
        
                
        // Read the individual(s) from the stream  and evaluate 
        
        boolean[] updateFitness = new boolean[numInds];
        Individual[] inds = new Individual[numInds];
        try
            {
            for (int i=0; i < numInds; i++) 
                { 
                inds[i] = state.population.subpops[subpops[i]].species.newIndividual(state, dataIn);
                if (!runEvolve) 
                    ((SimpleProblemForm)(state.evaluator.p_problem)).evaluate( state, inds[i], subpops[i], 0 );
                updateFitness[i] = dataIn.readBoolean(); 
                }
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read individual from master." + e);
            }
        
        
        if (runEvolve) 
            {
            long startTime = System.currentTimeMillis(); 
            long endTime=0; 

            // Now we need to reset the subpopulations.  They were already set up with the right
            // classes, Species, etc. in state.setup(), so all we need to do is modify the number
            // of individuals in each subpopulation.
        
            for(int subpop = 0; subpop < state.population.subpops.length; subpop++)
                {
                if (state.population.subpops[subpop].individuals.length != indsPerSubpop[subpop])
                    state.population.subpops[subpop].individuals = new Individual[indsPerSubpop[subpop]];
                }
            
            // Disperse into the population
            int[] counts = new int[state.population.subpops.length];
            for(int i =0; i < numInds; i++)
                state.population.subpops[subpops[i]].individuals[counts[subpops[i]]++] = inds[i];
            
            // Evaluate the population until time is up, or the evolution stops
            int result = state.R_NOTDONE; 
            while (result == state.R_NOTDONE) 
                { 
                result = state.evolve(); 
                endTime = System.currentTimeMillis(); 
                if ((endTime - startTime) > runTime) 
                    break;
                }
                
            // re-gather from population in the same order
            counts = new int[state.population.subpops.length];
            for(int i =0; i < numInds; i++)
                inds[i] = state.population.subpops[subpops[i]].individuals[counts[subpops[i]]++];
            state.finish(result);
            Evolve.cleanup(state);
            }


        //System.err.println("Returning Individuals ");
        // Return the evaluated individual to the master
        try 
            { 
            returnIndividualsToMaster(state, inds, updateFitness, dataOut, returnIndividuals); 
            } 
        catch( IOException e ) { state.output.fatal("Caught fatal IOException\n"+e ); }
        }
    
    public static void evaluateGroupedProblemForm( EvolutionState state, boolean returnIndividuals,
        DataInputStream dataIn, DataOutputStream dataOut )
        {
        boolean countVictoriesOnly = false;

        // first load the individuals
        int numInds = 1;
        try
            {
            numInds = dataIn.readInt();
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read the number of individuals from the master:\n"+e);
            }

        // load the subpops 
        int[] subpops = new int[numInds];  // subpops desired by each ind
        int[] indsPerSubpop = new int[state.population.subpops.length];  // num inds for each subpop
        for(int i = 0; i < numInds; i++)
            {
            try
                {
                subpops[i] = dataIn.readInt();
                if (subpops[i] < 0 || subpops[i] >= state.population.subpops.length)
                    state.output.fatal("Bad subpop number for individual #" + i + ": " + subpops[i]);
                indsPerSubpop[subpops[i]]++;
                }
            catch (IOException e)
                {
                state.output.fatal("Unable to read the subpop number from the master:\n"+e);
                }
            }

        // Read the individuals from the stream
        Individual inds[] = new Individual[numInds];
        boolean updateFitness[] = new boolean[numInds];
        try
            {
            for(int i=0;i<inds.length;++i)
                {
                inds[i] = state.population.subpops[subpops[i]].species.newIndividual( state, dataIn );
                updateFitness[i] = dataIn.readBoolean();
                }
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read individual from master.");
            }
                
        // Evaluate the individuals together
        ((GroupedProblemForm)(state.evaluator.p_problem)).evaluate( state, inds, updateFitness, countVictoriesOnly, subpops, 0 );
                                
        try 
            {
            returnIndividualsToMaster(state, inds, updateFitness, dataOut, returnIndividuals); 
            } 
        catch( IOException e ) { state.output.fatal("Caught fatal IOException\n"+e ); }
        }
        
    private static void returnIndividualsToMaster(EvolutionState state, Individual []inds, boolean[] updateFitness,
        DataOutputStream dataOut, boolean returnIndividuals) throws IOException 
        {
        // Return the evaluated individual to the master
        // just write evaluated and fitness
        for(int i=0;i<inds.length;i++)
            {
            //System.err.println("Returning Individual " + i);
            //System.err.println("writing byte: " + ( returnIndividuals ? V_INDIVIDUAL : (updateFitness[i] ? V_FITNESS : V_NOTHING)));
            dataOut.writeByte(returnIndividuals ? V_INDIVIDUAL : (updateFitness[i] ? V_FITNESS : V_NOTHING));
            //System.err.println("wrote byte");
            if (returnIndividuals)
                {
//              System.err.println("Writing Individual");
                inds[i].writeIndividual(state, dataOut);
//              System.err.println("Wrote Individual");
                }
            else if (updateFitness[i])
                {
                dataOut.writeBoolean(inds[i].evaluated);
                inds[i].fitness.writeFitness(state,dataOut);
                }
            }
//      System.err.println("flushing");
        dataOut.flush();
//      System.err.println("flushed");
        }
    }
