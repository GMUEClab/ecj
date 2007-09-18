
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
import ec.util.*;


public class EvolveSlave 
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
	
	public static final String P_RUNTIME = "slave.runtime"; 
    
    public static final byte V_NOTHING = 0;
    public static final byte V_INDIVIDUAL = 1;
    public static final byte V_FITNESS = 2;
	
    public static final byte V_SHUTDOWN = 0;
    public static final byte V_EVALUATESIMPLE = 1;
    public static final byte V_EVALUATEGROUPED = 2;
    public static final byte V_CHECKPOINT = 3;
	
    /** The argument indicating that we're starting up from a checkpoint file. */
    public static final String A_CHECKPOINT = "-checkpoint";
	
    /** The argument indicating that we're starting fresh from a new parameter file. */
    public static final String A_FILE = "-file";
	
    /** flush announcements parameter */
    public static final String P_FLUSH = "flush";
	
    /** nostore parameter */
    public static final String P_STORE = "store";
	
    /** verbosity parameter */
    public static final String P_VERBOSITY = "verbosity";
	
    /** seed parameter */
    public static final String P_SEED = "seed";
	
    /** 'time' seed parameter value */
    public static final String V_SEED_TIME = "time";
	
    /** state parameter */
    public static final String P_STATE = "state";
	
    /** How long we sleep in between attempts to connect to the master (in milliseconds). */
    public static final int SLEEP_TIME = 100;
	
	/** How long to run an evolutionary process on the slaves (in seconds)? */ 
	public static long runTime=0; 
	
    public static void main(String[] args)
        {
		MersenneTwisterFast[] random = new MersenneTwisterFast[1];
        random[0] = new MersenneTwisterFast();
		EvolutionState state = Evolve.initialize(Evolve.loadParameterDatabase(args), 0);	
		state.startFresh(); 
       
        // 6. Open a server socket and listen for requests
        String slaveName = state.parameters.getString(new Parameter(P_EVALSLAVENAME),null);
		
        String masterHost = state.parameters.getString(new Parameter(P_EVALMASTERHOST),null );
        int masterPort = state.parameters.getInt(new Parameter(P_EVALMASTERPORT),null);
        boolean useCompression = state.parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
		runTime = state.parameters.getLong(new Parameter(P_RUNTIME), null); 
		
        // Continue to serve new masters until killed.
        while (true)
            {
            try
			{
                Socket socket;
                long connectAttemptCount = 0;
                state.output.message("Connecting to master at "+masterHost+":"+masterPort);
                while (true)
                    {
                    try
					{
                        socket = new Socket(masterHost, masterPort);
                        //                                              socket.setTcpNoDelay(true);
                        //                                              socket.setSendBufferSize(8000);
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
                state.output.message("Connected to master after " + (connectAttemptCount * SLEEP_TIME) + " ms");
				
                DataInputStream dataIn = null;
                DataOutputStream dataOut = null;
                try
                    {
						InputStream tmpIn = socket.getInputStream();
						if (useCompression)
							tmpIn = new CompressingInputStream(tmpIn);
						
						dataIn = new DataInputStream(tmpIn);
						OutputStream tmpOut = socket.getOutputStream();
						if (useCompression)
							tmpOut = new CompressingOutputStream(tmpOut);
						
						dataOut = new DataOutputStream(tmpOut);
                    }
                catch (IOException e)
                    {
                    state.output.fatal("Unable to open input stream from socket:\n"+e);
                    }
				
                // specify the slaveName
                if (slaveName==null)
                    {
                    slaveName = socket.getLocalAddress().toString() + "/" + System.currentTimeMillis();
                    state.output.message("No slave name specified.  Using: " + slaveName);
                    }
				
                dataOut.writeUTF(slaveName);
                dataOut.flush();
                
                // Read random state from Master
                random[0].readState(dataIn);
				
                state.random = random;
                // Is this a Simple or Grouped ProblemForm?
                int problemType;
                boolean done = false;
                try
                    {
						while (! done)
							{
							// 0 means to shut down
							problemType = dataIn.readByte();
							switch (problemType)
								{
								case V_SHUTDOWN:
									done = true;
									state.finish(state.R_FAILURE); 
									Evolve.cleanup(state); 
									socket.close();
									return;
									
								case V_EVALUATESIMPLE:
									evaluateSimpleProblemForm(state, dataIn, dataOut);
									break;
									
								case V_EVALUATEGROUPED:
									evaluateGroupedProblemForm(state, true, dataIn, dataOut);
									break;
									
								case V_CHECKPOINT:
									checkpointRandomState(state, dataOut);
									break;
								default:
									state.output.fatal("Unknown problem form specified: "+problemType);
								}
							}
                    }
                catch (IOException e)
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
	
    public static void evaluateSimpleProblemForm( EvolutionState state, DataInputStream dataIn, DataOutputStream dataOut )
        {
		// Read the subpopulation number and the number of individuals
        // from the master.
        int numInds = -1;
		int subPopNum=-1;
       
        try
            {
				numInds = dataIn.readInt();
				subPopNum = dataIn.readInt();
				
				if( state.population == null )
					state.population = new Population();
				if( state.population.subpops == null )
					state.population.subpops = new Subpopulation[subPopNum+1];
				if( state.population.subpops.length <= subPopNum )
					{
					Subpopulation[] temp = state.population.subpops;
					state.population.subpops = new Subpopulation[subPopNum+1];
					System.arraycopy( temp, 0, state.population.subpops, 0, temp.length );
					}
				if( state.population.subpops[subPopNum] == null )
					{
					Parameter param = new Parameter(P_SUBPOP).push("" + subPopNum);
					state.population.subpops[subPopNum] = 
						(Subpopulation)(state.parameters.getInstanceForParameterEq(param,null,Subpopulation.class));
					state.population.subpops[subPopNum].setup(state, param);
					}
				
				if (state.population.subpops[subPopNum].individuals == null) 
					state.population.subpops[subPopNum].individuals = new Individual[numInds]; 
				
				for(int x=0;x<numInds;x++)
					{					
					if (state.population.subpops[subPopNum].individuals[x] == null) { 
						Individual[] temp = state.population.subpops[subPopNum].individuals; 
						state.population.subpops[subPopNum].individuals = new Individual[numInds]; 
						System.arraycopy(temp, 0, state.population.subpops[subPopNum].individuals, 0, temp.length); 
					}
					
					 // Read the individuals from the stream
					state.population.subpops[subPopNum].individuals[x] = state.population.subpops[subPopNum].species.newIndividual( state, dataIn );
					}
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read the subpopulation number from the master:\n"+e);
            }
	
      
		// Evaluate the population until time is up, or the evolution stops 
		long startTime = System.currentTimeMillis(); 
		long endTime=0; 
		int result = state.R_NOTDONE; 
		while (result == state.R_NOTDONE) { 
			result = state.evolve(); 
			endTime = System.currentTimeMillis(); 
			if ((endTime - startTime) > runTime) 
				break;
		}
				
        try
            {
				// Return the evaluated individuals to the master
				for(int i=0;i<numInds;i++)
					{
					dataOut.writeByte( V_INDIVIDUAL);
					state.population.subpops[subPopNum].individuals[i].writeIndividual(state, dataOut);
					}
				dataOut.flush();
            }
        catch( IOException e ) { state.output.fatal("Caught fatal IOException\n"+e ); }
		}
	
    public static void evaluateGroupedProblemForm( EvolutionState state, boolean returnIndividuals,
                                                   DataInputStream dataIn, DataOutputStream dataOut )
        {
        // Read the subpopulation number and the number of individuals
        // from the master.
        int numInds = -1;
        boolean countVictoriesOnly = false;
        Subpopulation[] subPop = null;
        
        try
            {
				numInds = dataIn.readInt();
				subPop = new Subpopulation[numInds];
				for(int x=0;x<numInds;x++)
					{
					int subPopNum = dataIn.readInt();
					// Here we need to know the subpopulation number so as to create the
					// correct type of subpopulation in order to create the correct type
					// of individual.
					if( state.population == null )
						state.population = new Population();
					if( state.population.subpops == null )
						state.population.subpops = new Subpopulation[subPopNum+1];
					if( state.population.subpops.length <= subPopNum )
						{
						Subpopulation[] temp = state.population.subpops;
						state.population.subpops = new Subpopulation[subPopNum+1];
						System.arraycopy( temp, 0, state.population.subpops, 0, temp.length );
						}
					if( state.population.subpops[subPopNum] == null )
						{
						Parameter param = new Parameter(P_SUBPOP).push("" + subPopNum);
						subPop[x] = 
							(Subpopulation)(state.parameters.getInstanceForParameterEq(
																					   param,null,
																					   Subpopulation.class));
						// Setup the subpopulation so that it is in a valid state.
						subPop[x].setup(state, param);
						state.population.subpops[subPopNum] = subPop[x];
						}
					else
						subPop[x] = state.population.subpops[subPopNum];
					}
				countVictoriesOnly = dataIn.readBoolean();
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read the subpopulation number from the master:\n"+e);
            }
		
        // Read the individuals from the stream
        Individual inds[] = new Individual[numInds];
        boolean updateFitness[] = new boolean[numInds];
        try
            {
				for(int i=0;i<inds.length;++i)
					{
					inds[i] = subPop[i].species.newIndividual( state, dataIn );
					updateFitness[i] = dataIn.readBoolean();
					}
            }
        catch (IOException e)
            {
            state.output.fatal("Unable to read individual from master.");
            }
		
        // Evaluate the individual
        // TODO Check to make sure the real problem is an instance of GroupedProblemForm
        ((GroupedProblemForm)(state.evaluator.p_problem)).evaluate( state, inds, updateFitness, countVictoriesOnly, 0 );
		
        try
            {
				// Return the evaluated individual to the master
				// just write evaluated and fitness
				for(int i=0;i<inds.length;i++)
					{
					dataOut.writeByte(returnIndividuals ? V_INDIVIDUAL : (updateFitness[i] ? V_FITNESS : V_NOTHING));
					if (returnIndividuals)
						{
						inds[i].writeIndividual(state, dataOut);
						}
					else if (updateFitness[i])
						{
						dataOut.writeBoolean(inds[i].evaluated);
						inds[i].fitness.writeFitness(state,dataOut);
						}
					}
				dataOut.flush();
            }
        catch( IOException e ) { state.output.fatal("Caught fatal IOException\n"+e ); }
        }
	
    private static void checkpointRandomState(final EvolutionState state,
                                              DataOutputStream dataOut )
        {
        state.output.systemMessage("Checkpointing");
		
        try
            {
				state.random[0].writeState(dataOut);
				dataOut.flush();
            }
        catch (IOException e)
            {
            state.output.fatal("Exception while checkpointing random state:\n"+e);
            }
        }
}
