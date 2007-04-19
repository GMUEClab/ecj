/** Some code taken from Sean Luke's ECJ (ec.EvolutionState, ec.Evolve) and 
 * Màrk Jelasity's DRM (drm.agentbase.IAgent, drm.agents.CollectiveAgent).
 * Copyright 2006 Alberto Cuesta Cañada, licensed under the Academic Free License.
 * @author Alberto Cuesta Cañada
 * @version 0.1 
 */
package ec.drm;

import java.io.*;
import java.util.*;

import ec.*;
import ec.util.*;

import drm.agentbase.*;
import drm.core.*;

public class EvolutionAgent extends ec.simple.SimpleEvolutionState implements IAgent{

	/** Serialization identificator */
	private static final long serialVersionUID = 1L;
	
    public static final String P_TYPE = "type";
    public static final String P_JOB = "job";
    public static final String P_ID = "id";
    
    public static final String P_FLUSH = "flush";
    public static final String P_STORE = "store";
    public static final String P_VERBOSITY = "verbosity";
    public static final String P_EVALTHREADS = "evalthreads";
    public static final String P_BREEDTHREADS = "breedthreads";
    public static final String P_SEED = "seed";
	
	public static final String M_MIGRATION = "ecj.migration";
	public static final String M_STATS = "ecj.stats";
	public static final String M_PING = "ecj.ping";
	public static final String M_FINISHED = "ecj.finished";
	public static final String M_IDEAL_FOUND = "ecj.ideal_found";
	
	/** The Base that hosts the agent. An agent can only live on a base. */
	protected transient IBase base = null;


	/** This is the type identifier of the agent. No use for yet, could subordinate to
	 * job to define different types of agents inside a single application. Right now
	 * equals usually to class name.*/
	protected String type;

	/** This is the job name the agent participates in.	Defines the application the
	 * agent is working in.*/
	protected String job;
	
	/** This is the id of the agent. Identifies it inside a job.*/
	protected String id;

    /** The root agent. The presence of this is not crutial, it only provides
	* a fixed and reliable address to fall back to and it is the default place to send statistics.	*/
	protected Address root = null;

	/** If this flag is set the agent should stop executing.
	* Every thread run by the agent should watch this flag unless it reimplements
	* onDestruction().
	* @see #onDestruction()	*/
	protected volatile transient boolean shouldLive = true;
	
	/** The drm version the agent is designed for.
	* @see drm.agentbase.Base#RELEASE_VERSION */
	protected static final int VERSION = 200000;

    /** This container is for passing experiment information to spawning children,
     * i. e. pairs of points for a symbolic regression problem.*/
    public Object data = null;
    
    /** True if we are not connected to DRM or we are a root node. 
     * This means that we can receive stats and transfer them to a file or stdout. */
    public boolean iamroot = true;
	
    /** Set to true by handleMessage if another agent founds an ideal individual. */
    public boolean ideal_found = false;
    
	/** This is called when the agent is put into a base.
	* At this time the base is already set using the {@link #setBase(IBase)}
	* method.
	* @param from The address of the base from which the agent was sent.
	* If the agent has never been on any base it is null.
	* @param to The address of the local base at the time of the arrival.
	* It is not guaranteed to remain valid trough the lifetime of the
	* agent. If the agent has never been on a base it is null.
	* @see #setBase(IBase) */
	public void onArrival( Address from, Address to ){
		shouldLive = true;
		
		if( root != null && root.isLocal() && from != null )
			root = new Address( from.getHost(), from.port, root.name );	
	}

	/** Extending classes that override this method must make sure that
	* they call <code>super.onDestruction</code>.
	* The suggested practice is that every implementation must begin with the
	* line <pre>super.onDestruction();</pre> */
	public void onDestruction() { shouldLive = false; }

	/** Removes the agent from the base. <b>Look out!</b> The method that calls this
	* method will not exit, the thread will not be stopped as a result.
	* It only calles <code>base.destroyAgent(getName())</code>.
	* So this method must be followed by exiting from the method explicitly
	* e.g. with a <code>return</code>. */
	public void suicide() { base.destroyAgent(getName()); }
	
	/** This returns the fully qualified and unique name of the agent that is
	 * used everywhere, composed by type.job.id */
	public String getName() { return type+"."+job+"."+id; }

	public String getId() { return id; }
	
	public String getJob() { return job; }

	public String getType() { return type; }

	public void setRoot( Address r ) { 
		root = r;
		if(root != null) iamroot = false;
	}
	
	public void setBase( IBase b ) { base = b; }
	
	public int version() { return VERSION; }
	
	/** This returns a reference through which information about the hosting DRM
	* can be requested.
	* @throws ClassCastException If the IDRM interface is not implemented
	* by the base proxy known by the agent.	*/
	public IDRM getDRM() { return (IDRM)base; }
	
	/** Returns the root address. */
	public Address getRootAddress(){ return root; }
	/** Called to discover peers that participate in the same job.*/

	public Address[] getPeerAddresses(){
		Iterator nodes = getDRM().getContributions().iterator();
		Iterator agents;
		List peers = new ArrayList();
		String name;
		ContributionBox cb;
		NodeContribution nc;
		
		while(nodes.hasNext()){
			cb = (ContributionBox)nodes.next();
			nc = (NodeContribution)cb.contribution;
			if(nc.getAgents() == null) break; // Hack: you could call this so fast that the node hasn't noticed any agents yet
			agents = nc.getAgents().iterator();
			while(agents.hasNext()){
				name = (String)agents.next();
				if(name.contains(job))
					peers.add(new Address(cb.contributor.getHost(),cb.contributor.port, name));
			}
		}
		if(peers.size() > 0)
			return (Address[])peers.toArray( new Address[ peers.size() ] );
		return new Address[0];
	}

	/** Creates a message and fires it.
	* @param recipient Recipient of the message.
	* @param type Type of the message.
	* @param contstr The string content of the message.
	* @param object Object to be wrapped in the message. If null, then
	*       no object is wrapped, null is written.
	* @return The request to track the status of the sending process.
	* @see IBase#fireMessage(Message) */
	public final IRequest fireMessage(Address recipient,String type,Object object){
		try{
			return base.fireMessage( new Message(
			new Address(getName()), recipient, type, object ) );
			//using a local address as sender is intentional,base fixes it
		}catch(IOException e){
			return new StaticRequest(IRequest.ERROR,e);
		}
	}

	/** Creates a message and fires it to a local destination.
	* @param recipient The name of the recipient of the message. It is
	*       assumed to be a local agent.
	* @param type Type of the message.
	* @param contstr The string content of the message.
	* @param object Object to be wrapped in the message. If null, then
	*       no object is wrapped, null is written. */
	public final IRequest fireMessage(String recipient,String type,Object object){
		try	{
			return base.fireMessage( new Message( 
			new Address(getName()),new Address(recipient),type,object) );
		}catch(IOException e){
			return new StaticRequest(IRequest.ERROR,e);
		}
	}
	
	/** Handles incoming messages. */
	public boolean handleMessage( Message m, Object object ) {
		if( m.getType().equals(M_MIGRATION) ){
			if(!(object instanceof ExchangerData)){
				output.error("Migration data must be sent in ExchangerData format.");
				return false;
			}
			if(!(exchanger instanceof DRMExchanger)){
				output.error("Only ec.drm.DRMExchanger can handle DRM migration messages.");
				return false;
			}
			((DRMExchanger)exchanger).storeData((ExchangerData)object);
		}else if( m.getType().equals(M_STATS) ){
			if(!(object instanceof StatsData)){
				output.error("Stats data must be sent in StatsData format.");
				return false;
			}
			if(!(statistics instanceof DRMStatistics)){
				output.error("Only ec.drm.DRMStatistics can handle DRM stats messages.");
				return false;
			}
			((DRMStatistics)statistics).printStatistics(this,(StatsData)object);
		}else if( m.getType().equals(M_FINISHED) ){
			output.message(m.getSender().name + " has finished.");
		}else if( m.getType().equals(M_IDEAL_FOUND) ){
			output.message(m.getSender().name + " found an ideal individual.");
		}else if( m.getType().equals(M_PING) ){
			m.setReply(new Long(System.currentTimeMillis()));			
		}else return false;
		return true;
	}
	
	public String setName(Parameter base){
		type = parameters.getStringWithDefault(
        		base.push(P_TYPE),null,"EvolutionAgent");

        job = parameters.getStringWithDefault(
        		base.push(P_JOB),null,"job");

        id = parameters.getStringWithDefault(
        		base.push(P_ID),null,("" + (Math.random())).substring(2,9));
        return getName();
	}

    /** This method is called after a checkpoint
    is restored from but before the run starts up again.  You might use this
    to set up file pointers that were lost, etc. */

    /** Unlike for other setup() methods, ignore the base; it will always be null. 
    @see Prototype#setup(EvolutionState,Parameter)*/
		
	public void setup(final EvolutionState state, final Parameter base) {
	    int[] seeds;
	    int x;
	    int verbosity;
	    boolean store;

	    Parameter p;
	    
	    // make a new Statics
	
	    statics = new Vector();
	
	    // 1. create the output
	    p = new Parameter(P_STORE);
	    store = parameters.getBoolean(p,null,false);

	    p = new Parameter(P_VERBOSITY);
	    verbosity = parameters.getInt(p,null,0);
	    if (verbosity<0)
	        Output.initialError("Verbosity should be an integer >= 0.\n",p); 
	
	    output = new Output(store,verbosity);

	    p = new Parameter(P_FLUSH);
	    output.setFlush(parameters.getBoolean(p,null,false));
	
	
	    // stdout is always log #0.  stderr is always log #1.
	    // stderr accepts announcements, and both are fully verbose 
	    // by default.
	    output.addLog(ec.util.Log.D_STDOUT,Output.V_VERBOSE,false);
	    output.addLog(ec.util.Log.D_STDERR,Output.V_VERBOSE,true);
	    
	    // 2. set up thread values
	    
	    p = new Parameter(P_BREEDTHREADS);
	    breedthreads = parameters.getInt(p,null,1);
	    if (breedthreads < 1)
	        output.fatal("Number of breeding threads should be an integer >0.",p,null);

	    p = new Parameter(P_EVALTHREADS);
	    evalthreads = parameters.getInt(p,null,1);
	    if (evalthreads < 1)
	        output.fatal("Number of eval threads should be an integer >0.",p,null);
	
	    // 3. create the Mersenne Twister random number generators,
	    // one per thread
	
	    random = new MersenneTwisterFast[breedthreads > evalthreads ? 
	                                     breedthreads : evalthreads];
	    seeds = new int[random.length];
	                                            
	    String seedMessage = "Seed: ";
	    int time = (int)(System.currentTimeMillis());
	    for (x=0;x<random.length;x++)
	        {
	    	p = new Parameter(P_SEED);
	        seeds[x] = Evolve.determineSeed(output,parameters,p.push(""+x),
	                                 time+x,random.length * randomSeedOffset);
	        for (int y=0;y<x;y++)
	            if (seeds[x]==seeds[y])
	                output.fatal(P_SEED+"."+x+" ("+seeds[x]+") and "+P_SEED+"."+y+" ("+seeds[y]+") ought not be the same seed.",null,null); 
	        random[x] = new MersenneTwisterFast(seeds[x]);
	        seedMessage = seedMessage + seeds[x] + " ";
	        }
	
	    // 4.  Start up the evolution
	
	    //output.systemMessage(Version.message()); 
	    output.systemMessage("Threads:  breed/" + breedthreads + " eval/" + evalthreads);
	    output.systemMessage(seedMessage);
	    
	    p = new Parameter(P_CHECKPOINT);
	    checkpoint = parameters.getBoolean(p,null,false);

	    p = new Parameter(P_CHECKPOINTPREFIX);
	    checkpointPrefix = parameters.getString(p,null);
	    if (checkpointPrefix==null)
	        output.fatal("No checkpoint prefix specified.",p);

	    p = new Parameter(P_CHECKPOINTMODULO);
	    checkpointModulo = parameters.getInt(p,null,1);
	    if (checkpointModulo==0)
	        output.fatal("The checkpoint modulo must be an integer >0.",p);

	    p = new Parameter(P_GENERATIONS);
	    numGenerations = parameters.getInt(p,null,1);
	    if (numGenerations==0)
	        output.fatal("The number of generations must be an integer >0.",p);

	    p = new Parameter(P_QUITONRUNCOMPLETE);
	    quitOnRunComplete = parameters.getBoolean(p,null,false);
	
	    /* Set up the singletons */
	    
	    p = new Parameter(P_INITIALIZER);
	    initializer = (Initializer)
	        (parameters.getInstanceForParameter(p,null,Initializer.class));
	    initializer.setup(this,p);
	
	    p = new Parameter(P_FINISHER);
	    finisher = (Finisher)
	        (parameters.getInstanceForParameter(p,null,Finisher.class));
	    finisher.setup(this,p);
	
	    p = new Parameter(P_BREEDER);
	    breeder = (Breeder)
	        (parameters.getInstanceForParameter(p,null,Breeder.class));
	    breeder.setup(this,p);
	
	    p = new Parameter(P_EVALUATOR);
	    evaluator = (Evaluator)
	        (parameters.getInstanceForParameter(p,null,Evaluator.class));
	    evaluator.setup(this,p);
	
	    p = new Parameter(P_STATISTICS);
	    statistics = (Statistics)
	        (parameters.getInstanceForParameterEq(p,null,Statistics.class));
	    statistics.setup(this,p);
	    
	    p = new Parameter(P_EXCHANGER);
	    exchanger = (Exchanger)
	        (parameters.getInstanceForParameter(p,null,Exchanger.class));
	    exchanger.setup(this,p);
	            
	    generation = 0;
    }

	/** Usually called after setup(), initialize() calls the initializers of the
	 *  EvolutionState modules, effectively enabling a EvolutionState to start evolving. 
	 *	@see setup(EvolutionState,Parameter)
	 */
	public void startFresh() {
	    // POPULATION INITIALIZATION
	    output.message("Initializing Generation 0");
	    statistics.preInitializationStatistics(this);
	    population = initializer.initialPopulation(this, 0); // unthreaded
	    statistics.postInitializationStatistics(this);
	
	    // INITIALIZE CONTACTS -- done after initialization to allow
	    // a hook for the user to do things in Initializer before
	    // an attempt is made to connect to island models etc.
	    exchanger.initializeContacts(this);
	    evaluator.initializeContacts(this);
	}
	
	public void startFromCheckpoint()
	    {
	    try{output.restart();}   // may throw an exception if there's a bad file
	    catch(IOException e){Output.initialError("Exception when restarting output: \n" + e);}
	    if(exchanger instanceof DRMExchanger)((DRMExchanger)exchanger).reset(this);
	    else exchanger.reinitializeContacts(this);
	    if(statistics instanceof DRMStatistics)((DRMStatistics)statistics).reset(this);
	    evaluator.reinitializeContacts(this);
	    }
	
    /**
     * @param result
     */
    public void finish(int result) 
        {
        super.finish(result);
        if(!iamroot){
        	fireMessage(root,M_FINISHED,null);
        	if(result == R_SUCCESS)
        		fireMessage(root,M_IDEAL_FOUND,null);
        	}
        }
	
    /* Some code copied from ec.EvolutionState.run() */
    public void run(){
    	/* I'd prefer to do setup before, ideally on onArrival */
    	setup(this,null);
    	
    	/* Initialize population, evaluator, exchanger, statistics, etc...*/
    	startFresh();
    	
        /* the evolving loop */
        int result = R_NOTDONE;
        while ( result == R_NOTDONE && !(ideal_found && quitOnRunComplete))
            {
            result = evolve();
            }
        
        finish(result);

        output.message("All tasks finished.");
		suicide();
    }
}