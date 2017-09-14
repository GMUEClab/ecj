/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.util.*;
import java.io.*;

/* 
 * EvolutionState.java
 * 
 * Created: Tue Aug 10 22:14:46 1999
 * By: Sean Luke
 */

/**
 * An EvolutionState object is a singleton object which holds the entire
 * state of an evolutionary run.  By serializing EvolutionState, the entire
 * run can be checkpointed out to a file.
 *
 * <p>The EvolutionState instance is passed around in a <i>lot</i> of methods,
 * so objects can read from the parameter database, pick random numbers,
 * and write to the output facility.
 *
 * <p>EvolutionState is a unique object in that it calls its own setup(...)
 * method, from run(...).
 *
 * <p>An EvolutionState object contains quite a few objects, including:
 <ul>
 <li><i>Objects you may safely manipulate during the multithreaded sections of a run:</i>
 <ul>
 <li> MersenneTwisterFast random number generators (one for each evaluation or breeding thread -- use the thread number you were provided to determine which random number generator to use)
 <li> The ParameterDatabase
 <li> The Output facility for writing messages and logging
 </ul>

 <li><i>Singleton objects:</i>
 <ul>
 <li> The Initializer.
 <li> The Finisher.
 <li> The Breeder.
 <li> The Evaluator.
 <li> The Statistics facility.
 <li> The Exchanger.
 </ul>

 <li><i>The current evolution state:</i>
 <ul>
 <li> The generation number.
 <li> The population.
 <li> The maximal number of generations.
 </ul>

 <li><i>Auxillary read-only information:</i>
 <ul>
 <li> The prefix to begin checkpoint file names with.
 <li> Whether to quit upon finding a perfect individual.
 <li> The number of breeding threads to spawn.
 <li> The number of evaluation threads to spawn.
 </ul>
 
 <li><i>A place to stash pointers to static variables so they'll get serialized: </i>
 <ul>
 <li> Statics
 </ul>
 </ul>

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>generations</tt><br>
 <font size=-1>int &gt;= 1</font> or undefined</td>
 <td valign=top>(maximal number of generations to run.  Either this or evaluations must be set, but not both.)</td></tr>

 <tr><td valign=top><tt>evaluations</tt><br>
 <font size=-1>int &gt;= 1</font> or undefined</td>
 <td valign=top>(maximal number of evaluations to run (in subpopulation 0).    Either this or generations must be set, but not both.)</td></tr>

 <tr><td valign=top><tt>checkpoint-modulo</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(how many generations should pass before we do a checkpoint?  
 The definition of "generations" depends on the particular EvolutionState 
 implementation you're using)</td></tr>

 <tr><td valign=top><tt>checkpoint</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(should we checkpoint?)</td></tr>
 
 <tr><td valign=top><tt>prefix</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(the prefix to prepend to checkpoint files -- see ec.util.Checkpoint)</td></tr>

 <tr><td valign=top><tt>checkpoint-directory</tt><br>
 <font size=-1>File (default is empty)</td>
 <td valign=top>(directory where the checkpoint files should be located)</td></tr>
 
 <tr><td valign=top><tt>quit-on-run-complete</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(do we prematurely quit the run when we find a perfect individual?)</td></tr>

 <tr><td valign=top><tt>init</tt><br>
 <font size=-1>classname, inherits and != ec.Initializer</font></td>
 <td valign=top>(the class for initializer)</td></tr>

 <tr><td valign=top><tt>finish</tt><br>
 <font size=-1>classname, inherits and != ec.Finisher</font></td>
 <td valign=top>(the class for finisher)</td></tr>

 <tr><td valign=top><tt>breed</tt><br>
 <font size=-1>classname, inherits and != ec.Breeder</font></td>
 <td valign=top>(the class for breeder)</td></tr>

 <tr><td valign=top><tt>eval</tt><br>
 <font size=-1>classname, inherits and != ec.Evaluator</font></td>
 <td valign=top>(the class for evaluator)</td></tr>

 <tr><td valign=top><tt>stat</tt><br>
 <font size=-1>classname, inherits or = ec.Statistics</font></td>
 <td valign=top>(the class for statistics)</td></tr>

 <tr><td valign=top><tt>exch</tt><br>
 <font size=-1>classname, inherits and != ec.Exchanger</font></td>
 <td valign=top>(the class for exchanger)</td></tr>

 </table>


 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><tt>init</tt></td>
 <td>initializer</td></tr>

 <tr><td valign=top><tt>finish</tt></td>
 <td>finisher</td></tr>

 <tr><td valign=top><tt>breed</tt></td>
 <td>breeder</td></tr>

 <tr><td valign=top><tt>eval</tt></td>
 <td>evaluator</td></tr>

 <tr><td valign=top><tt>stat</tt></td>
 <td>statistics</td></tr>

 <tr><td valign=top><tt>exch</tt></td>
 <td>exchanger</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class EvolutionState implements Singleton
    {
    private static final long serialVersionUID = 1;

    /** The parameter database (threadsafe).  Parameter objects are also threadsafe.
        Nonetheless, you should generally try to treat this database as read-only. */
    public ParameterDatabase parameters;

    /** An array of random number generators, indexed by the thread number you were given (or, if you're not in a multithreaded area, use 0).  These generators are not threadsafe in and of themselves, but if you only use the random number generator assigned to your thread, as was intended, then you get random numbers in a threadsafe way.  These generators must each have a different seed, of course.*/
    public MersenneTwisterFast[] random;

    /** An array of HashMaps, indexed by the thread number you were given (or, if you're not in a multithreaded area, use 0).  This allows you to store per-thread specialized information (typically keyed with a string).  */
    public HashMap[] data;

    /** The output and logging facility (threadsafe).  Keep in mind that output in Java is expensive. */
    public Output output;

    /** The requested number of threads to be used in breeding, excepting perhaps a "parent" thread which gathers the other threads.  If breedthreads = 1, then the system should not be multithreaded during breeding.  Don't modify this during a run. */
    public int breedthreads;  // how many threads to use in breeding

    /** The requested number of threads to be used in evaluation, excepting perhaps a "parent" thread which gathers the other threads.  If evalthreads = 1, then the system should not be multithreaded during evaluation. Don't modify this during a run.*/
    public int evalthreads;  // how many threads to use in evaluation

    /** Should we checkpoint at all? */
    public boolean checkpoint;

    /** The requested directory where checkpoints should be located.  This must be a directory, not a file.  You probably shouldn't modify this during a run.*/
    public File checkpointDirectory = null;

    /** The requested prefix to start checkpoint filenames, not including a following period.  You probably shouldn't modify this during a run.*/
    public String checkpointPrefix;

    /** The requested number of generations that should pass before we write out a checkpoint file. */
    public int checkpointModulo;

    /** An amount to add to each random number generator seed to "offset" it -- often this is simply the job number.  
        If you are using more random number generators
        internally than the ones initially created for you in the EvolutionState, you might want to create them with the seed
        value of <tt>seedParameter+randomSeedOffset</tt>.  At present the only such class creating additional generators
        is ec.eval.MasterProblem. */
    public int randomSeedOffset;

    /** Whether or not the system should prematurely quit when Evaluator returns true for runComplete(...) (that is, when the system found an ideal individual. */
    public boolean quitOnRunComplete;

    /** Current job iteration variables, set by Evolve.  The default version simply sets this to a single Object[1] containing
        the current job iteration number as an Integer (for a single job, it's 0).  You probably should not modify this inside
        an evolutionary run.  */
    public Object[] job;

    
    /** The original runtime arguments passed to the Java process. You probably should not modify this inside an evolutionary run.  */
    public String[] runtimeArguments;
            
    public static final int UNDEFINED = 0;

    /** The current generation of the population in the run.  For non-generational approaches, this probably should represent some kind of incrementing value, perhaps the number of individuals evaluated so far.  You probably shouldn't modify this. */
    public int generation;
    
    /** The current number of evaluations which have transpired so far in the run.  This is only updated on a generational boundary. */
    public int evaluations;
    
    /** The number of generations the evolutionary computation system will run until it ends, or UNDEFINED */
    public int numGenerations = UNDEFINED;

    /** The number of evaluations the evolutionary computation system will run until it ends (up to the next generation boundary), or UNDEFINED */
    public long numEvaluations = UNDEFINED;

    /** The current population.  This is <i>not</i> a singleton object, and may be replaced after every generation in a generational approach. You should only access this in a read-only fashion.  */
    public Population population;

    /** The population initializer, a singleton object.  You should only access this in a read-only fashion. */
    public Initializer initializer;

    /** The population finisher, a singleton object.  You should only access this in a read-only fashion. */
    public Finisher finisher;

    /** The population breeder, a singleton object.  You should only access this in a read-only fashion. */
    public Breeder breeder;

    /** The population evaluator, a singleton object.  You should only access this in a read-only fashion. */
    public Evaluator evaluator;

    /** The population statistics, a singleton object.  You should generally only access this in a read-only fashion. */
    public Statistics statistics;

    /** The population exchanger, a singleton object.  You should only access this in a read-only fashion. */
    public Exchanger exchanger;

    /** Global birthday tracker number for genes in representations such as NEAT. Accessed and modified during run time */

    public long innovationNumber;

    /** "The population has started fresh (not from a checkpoint)." */ 
    public final static int C_STARTED_FRESH = 0;

    /** "The population started from a checkpoint." */
    public final static int C_STARTED_FROM_CHECKPOINT = 1;

    /** "The evolution run has quit, finding a perfect individual." */
    public final static int R_SUCCESS = 0;

    /** "The evolution run has quit, failing to find a perfect individual." */
    public final static int R_FAILURE = 1;
    
    /** "The evolution run has not quit */
    public final static int R_NOTDONE = 2;

    public final static String P_INITIALIZER = "init";
    public final static String P_FINISHER = "finish";
    public final static String P_BREEDER = "breed";
    public final static String P_EVALUATOR = "eval";
    public final static String P_STATISTICS = "stat";
    public final static String P_EXCHANGER = "exch";
    public final static String P_GENERATIONS = "generations";
    public static final String P_EVALUATIONS = "evaluations";
    public final static String P_QUITONRUNCOMPLETE = "quit-on-run-complete";
    public final static String P_CHECKPOINTPREFIX = "checkpoint-prefix";
    public final static String P_CHECKPOINTMODULO = "checkpoint-modulo";
    public final static String P_CHECKPOINTDIRECTORY = "checkpoint-directory";
    public final static String P_CHECKPOINT = "checkpoint";
    public final static String P_INNOVATIONNUMBER = "innovation-number";
    final static String P_CHECKPOINTPREFIX_OLD = "prefix";

    
    

    /** This will be called to create your evolution state; immediately
        after the constructor is called,
        the parameters, random, and output fields will be set
        for you.  The constructor probably won't be called ever if
        restoring (deserializing) from a checkpoint.
    */
    public EvolutionState() { }
    
    /** Unlike for other setup() methods, ignore the base; it will always be null. 
        @see Prototype#setup(EvolutionState,Parameter)
    */
    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter p;
        
        // set up the per-thread data
        data = new HashMap[random.length];
        for(int i = 0; i < data.length; i++)
            data[i] = new HashMap();

        // we ignore the base, it's worthless anyway for EvolutionState

        p = new Parameter(P_CHECKPOINT);
        checkpoint = parameters.getBoolean(p,null,false);

        p = new Parameter(P_CHECKPOINTPREFIX);
        checkpointPrefix = parameters.getString(p,null);
        if (checkpointPrefix==null)
            {
            // check for the old-style checkpoint prefix parameter
            Parameter p2 = new Parameter(P_CHECKPOINTPREFIX_OLD);
            checkpointPrefix = parameters.getString(p2,null);
            if (checkpointPrefix==null)
                {
                output.fatal("No checkpoint prefix specified.",p);  // indicate the new style, not old parameter
                }
            else
                {
                output.warning("The parameter \"prefix\" is deprecated.  Please use \"checkpoint-prefix\".", p2);
                }
            }
        else
            {
            // check for the old-style checkpoint prefix parameter as an acciental duplicate
            Parameter p2 = new Parameter(P_CHECKPOINTPREFIX_OLD);
            if (parameters.getString(p2,null) != null)
                {
                output.warning("You have BOTH the deprecated parameter \"prefix\" and its replacement \"checkpoint-prefix\" defined.  The replacement will be used,  Please remove the \"prefix\" parameter.", p2);
                }
            
            }
            

        p = new Parameter(P_CHECKPOINTMODULO);
        checkpointModulo = parameters.getInt(p,null,1);
        if (checkpointModulo==0)
            output.fatal("The checkpoint modulo must be an integer >0.",p);
        
        p = new Parameter(P_CHECKPOINTDIRECTORY);
        if (parameters.exists(p, null))
            {
            checkpointDirectory = parameters.getFile(p,null);
            if (checkpointDirectory==null)
                output.fatal("The checkpoint directory name is invalid: " + checkpointDirectory, p);
            if (!checkpointDirectory.isDirectory())
                output.fatal("The checkpoint directory location is not a directory: " + checkpointDirectory, p);
            }
        else checkpointDirectory = null;
            
        p = new Parameter(P_EVALUATIONS);
        if (parameters.exists(p, null))
            {
            numEvaluations = parameters.getInt(p, null, 1);  // 0 would be UNDEFINED
            if (numEvaluations <= 0)
                output.fatal("If defined, the number of evaluations must be an integer >= 1", p, null);
            }
                
        p = new Parameter(P_GENERATIONS);
        if (parameters.exists(p, null))
            {
            numGenerations = parameters.getInt(p, null, 1);  // 0 would be UDEFINED                 
                                
            if (numGenerations <= 0)
                output.fatal("If defined, the number of generations must be an integer >= 1.", p, null);
            }
                        
        if (numEvaluations != UNDEFINED && numGenerations != UNDEFINED)
            {
            state.output.warning("Both generations and evaluations defined: whichever happens first is when ECJ will stop.");
            }
        else if (numEvaluations == UNDEFINED && numGenerations == UNDEFINED)  // uh oh, something must be defined
            output.fatal("Either evaluations or generations must be defined.", new Parameter(P_GENERATIONS), new Parameter(P_EVALUATIONS));

        p=new Parameter(P_QUITONRUNCOMPLETE);
        quitOnRunComplete = parameters.getBoolean(p,null,false);


        /* Set up the singletons */
        p=new Parameter(P_INITIALIZER);
        initializer = (Initializer)
            (parameters.getInstanceForParameter(p,null,Initializer.class));
        initializer.setup(this,p);

        p=new Parameter(P_FINISHER);
        finisher = (Finisher)
            (parameters.getInstanceForParameter(p,null,Finisher.class));
        finisher.setup(this,p);

        p=new Parameter(P_BREEDER);
        breeder = (Breeder)
            (parameters.getInstanceForParameter(p,null,Breeder.class));
        breeder.setup(this,p);

        p=new Parameter(P_EVALUATOR);
        evaluator = (Evaluator)
            (parameters.getInstanceForParameter(p,null,Evaluator.class));
        evaluator.setup(this,p);

        p=new Parameter(P_STATISTICS);
        statistics = (Statistics)
            (parameters.getInstanceForParameterEq(p,null,Statistics.class));
        statistics.setup(this,p);
        
        p=new Parameter(P_EXCHANGER);
        exchanger = (Exchanger)
            (parameters.getInstanceForParameter(p,null,Exchanger.class));
        exchanger.setup(this,p);

        p=new Parameter(P_INNOVATIONNUMBER);
        innovationNumber = parameters.getLong(p, null, Long.MIN_VALUE);
                
        generation = 0;
        }

    /** This method is called after a checkpoint
        is restored from but before the run starts up again.  You might use this
        to set up file pointers that were lost, etc. */
 
    public void resetFromCheckpoint() throws IOException
        {
        output.restart();   // may throw an exception if there's a bad file
        exchanger.reinitializeContacts(this);
        evaluator.reinitializeContacts(this);
        }

    public void finish(int result) {}

    public void startFromCheckpoint() {}

    public void startFresh() {}

    public int evolve()
        throws InternalError { return R_NOTDONE; }


    // This is broken out like this so that incrementEvaluations can get inlined
    Object[] lock = new Object[0];
    void synchronizedIncrementEvaluations(int val)
        {
        synchronized(lock)
            {
            evaluations++;
            }
        }
                                
    public void incrementEvaluations(int val)
        {
        if (evalthreads == 1)
            evaluations += val;
        else
            {
            synchronizedIncrementEvaluations(val);
            }
        }

    /** Starts the run. <i>condition</i> indicates whether or not the
        run was restarted from a checkpoint (C_STARTED_FRESH vs
        C_STARTED_FROM_CHECKPOINT).  At the point that run(...) has been
        called, the parameter database has already been set up, as have
        the random number generators, the number of threads, and the
        Output facility.  This method should call this.setup(...) to
        set up the EvolutionState object if condition equals C_STARTED_FRESH. */
    public void run(int condition)
        {
        if (condition == C_STARTED_FRESH)
            {
            startFresh();
            }
        else // condition == C_STARTED_FROM_CHECKPOINT
            {
            startFromCheckpoint();
            }
        
        /* the big loop */
        int result = R_NOTDONE;
        while ( result == R_NOTDONE )
            {
            result = evolve();
            }
        
        finish(result);
        }
    }
