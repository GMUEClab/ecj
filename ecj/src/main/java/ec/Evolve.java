/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.*;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.PrintWriter;

/* 
 * Evolve.java
 * 
 * Created: Wed Aug 11 17:49:01 1999
 * By: Sean Luke
 */

/**
 * Evolve is the main entry class for an evolutionary computation run.
 *
 * <p> An EC run is done with one of two argument formats:
 *
 * <p><tt>java ec.Evolve -file </tt><i>parameter_file [</i><tt>-p </tt><i>parameter=value]*</i>
 *
 * <p>This starts a new evolutionary run, using the parameter file <i>parameter_file</i>.
 * The user can provide optional overriding parameters on the command-line with the <tt>-p</tt> option.
 *
 * <p><tt>java ec.Evolve -checkpoint </tt><i>checkpoint_file</i>
 * 
 * <p>This starts up an evolutionary run from a previous checkpoint file.
 *
 * <p>The basic Evolve class has a main() loop with a simple job iteration facility.
 * If you'd like to run the evolutionary system four times, each with a different random
 * seed, you might do:
 * 
 * <p><tt>java ec.Evolve -file </tt><i>parameter_file</i> <tt>-p jobs=4</tt>
 *
 * <p>Here, Evolve will run the first time with the random seed equal to whatever's specified
 * in your file, then job#2 will be run with the seed + 1, job#3 with the seed + 2, 
 * and job#4 with the seed + 3.  If you have multiple seeds, ECJ will try to make sure they're
 * all different even across jobs by adding the job number * numberOfSeeds to each of them.
 * This means that if you're doing multiple jobs with multiple seeds, you should probably set
 * seed.0 to x, seed.1 to x+1, seed.2 to x+2, etc. for best results.  It also works if seed.0
 * is x, seed.1 is y (a number much bigger than x), seed.2 is z (a number much bigger than y) etc.
 *
 * If you set seed.0=time etc. for multiple jobs, the values of each seed will be set to the
 * current time that the job starts plus the job number * numberOfSeeds.  As current time always
 * goes up, this shouldn't be an issue.  However it's theoretically possible that if you checkpoint and restart
 * on another system with a clock set back in time, you could get the same seed in a later job.
 *
 * <p><b>main() has been designed to be modified.</b>  The comments for the Evolve.java file contain
 * a lot discussion of how ECJ's main() bootstraps the EvolutionState object and runs it, plus a much
 * simpler example of main() and explanations for how main() works.
 *

 <p><b>Parameters</b><br>
 <table>

 <tr><td valign=top><tt>jobs</tt></br>
 <font size=-1> int >= 1 (default)</font></td>
 <td valign=top>(The number of jobs to iterate.  The current job number (0...jobs-1) will be added to each seed UNLESS the seed is loaded from the system time.  The job number also gets added as a prefix (if the number of jobs is more than 1)).</td></tr>

 <tr><td valign=top><tt>nostore</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should the ec.util.Output facility <i>not</i> store announcements in memory?)</td></tr>

 <tr><td valign=top><tt>flush</tt><br>
 <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(should I flush all output as soon as it's printed (useful for debugging when an exception occurs))</td></tr>

 <tr><td valign=top><tt>evalthreads</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the number of threads to spawn for evaluation)</td></tr>

 <tr><td valign=top><tt>breedthreads</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(the number of threads to spawn for breeding)</td></tr>

 <tr><td valign=top><tt>seed.</tt><i>n</i><br>
 <font size=-1>int != 0, or string  = <tt>time</tt></font></td>
 <td valign=top>(the seed for random number generator #<i>n</i>.  <i>n</i> should range from 0 to Max(evalthreads,breedthreads)-1.  If value is <tt>time</tt>, then the seed is based on the system clock plus <i>n</i>.)</td></tr>

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
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Evolve 
    {
    public final static String P_PRINTACCESSEDPARAMETERS = "print-accessed-params";
    public final static String P_PRINTUSEDPARAMETERS = "print-used-params";
    public final static String P_PRINTALLPARAMETERS = "print-all-params";
    public final static String P_PRINTUNUSEDPARAMETERS = "print-unused-params";
    public final static String P_PRINTUNACCESSEDPARAMETERS = "print-unaccessed-params";

    /** The argument indicating that we're starting up from a checkpoint file. */
    public static final String A_CHECKPOINT = "-checkpoint";
    
    /** The argument indicating that we're starting fresh from a new parameter file. */
    public static final String A_FILE = "-file";
    
    /** The argument indicating that we're starting fresh from a parameter file stored in a jar file or as some resource. */
    public static final String A_FROM = "-from";

    /** The argument indicating the class where the resource is relative to. */
    public static final String A_AT = "-at";

    /** The argument indicating a request to print out the help message. */
    public static final String A_HELP = "-help";

    /** evalthreads parameter */
    public static final String P_EVALTHREADS = "evalthreads";

    /** breedthreads parameter */
    public static final String P_BREEDTHREADS = "breedthreads";

    /** seed parameter */
    public static final String P_SEED = "seed";

    /** 'time' seed parameter value */
    public static final String V_SEED_TIME = "time";

    /** state parameter */
    public static final String P_STATE = "state";
    
    /** 'auto' thread parameter value */
    public static final String V_THREADS_AUTO = "auto";
    
    /** Should we muzzle stdout and stderr? */
    public static final String P_SILENT = "silent";

    /** Should we muzzle stdout and stderr? [deprecated] */
    static final String P_MUZZLE = "muzzle";



    /** Optionally prints the help message. */
    public static void checkForHelp(String[] args) 
        {
        for(int x=0;x<args.length;x++)
            if (args[x].equals(A_HELP))
                {
                System.err.println(Version.message());
                System.err.println(
                    "Format:\n\n" + 
                    "    java ec.Evolve -file FILE [-p PARAM=VALUE] [-p PARAM=VALUE] ...\n" +
                    "    java ec.Evolve -from FILE [-p PARAM=VALUE] [-p PARAM=VALUE] ...\n" + 
                    "    java ec.Evolve -from FILE -at CLASS [-p PARAM=VALUE] [-p PARAM=VALUE] ...\n" + 
                    "    java ec.Evolve -checkpoint CHECKPOINT\n" + 
                    "    java ec.Evolve -help\n\n" +
                    "-help                   Shows this message and exits.\n\n" +
                    "-file FILE              Launches ECJ using the provided parameter FILE.\n\n" +
                    "-from FILE              Launches ECJ using the provided parameter FILE\n" + 
                    "                        which is defined relative to the directory\n" + 
                    "                        holding the classfile ec/Evolve.class  If this\n" + 
                    "                        class file is found inside a Jar file, then the\n" + 
                    "                        FILE will also be assumed to be in that Jar file,\n" +
                    "                        at the proper relative location.\n\n" +
                    "-from FILE -at CLASS    Launches ECJ using the provided parameter FILE\n" + 
                    "                        which is defined relative to the directory\n" + 
                    "                        holding the classfile CLASS (for example,\n" + 
                    "                        ec/ant/ant.class).  If this class file is found\n" +
                    "                        inside a Jar file, then the FILE will also be\n" + 
                    "                        assumed to be in that Jar file, at the proper\n" +
                    "                        relative location.\n\n" +
                    "-p PARAM=VALUE          Overrides the parameter PARAM in the parameter\n" +
                    "                        file, setting it to the value VALUE instead.  You\n" + 
                    "                        can override as many parameters as you like on\n" + 
                    "                        the command line.\n\n" +
                    "-checkpoint CHECKPOINT  Launches ECJ from the provided CHECKPOINT file.\n"
                    );
                System.exit(1);
                }
        }

    /** Restores an EvolutionState from checkpoint if "-checkpoint FILENAME" is in the command-line arguments. */
    public static EvolutionState possiblyRestoreFromCheckpoint(String[] args)
        {
        for(int x=0;x<args.length-1;x++)
            if (args[x].equals(A_CHECKPOINT))
                {
                System.err.println("Restoring from Checkpoint " + args[x+1]);
                try
                    {
                    return Checkpoint.restoreFromCheckpoint(args[x+1]);
                    }
                catch(Exception e)
                    {
                    Output.initialError("An exception was generated upon starting up from a checkpoint.\nFor help, try:  java ec.Evolve -help\n\n" + e);
                    }
                }
        return null;  // should never happen
        }
    
    /** Loads a ParameterDatabase from checkpoint if "-params" is in the command-line arguments. */
    public static ParameterDatabase loadParameterDatabase(String[] args) 
        {
        // search for a -file
        ParameterDatabase parameters = null;        
        for(int x=0;x<args.length-1;x++)
            if (args[x].equals(A_FILE))
                try
                    {
                    parameters = new ParameterDatabase(
                        new File(new File(args[x+1]).getAbsolutePath()),
                        args);
                    break;
                    }
                catch(Exception e)
                    {
                    e.printStackTrace();
                    Output.initialError("An exception was generated upon reading the parameter file \"" + args[x+1] + "\".\nHere it is:\n" + e); 
                    }
                    
        // search for a resource class (we may or may not use this)
        Class cls = null;
        for(int x=0;x<args.length-1;x++)
            if (args[x].equals(A_AT))
                try
                    {
                    if (parameters != null)  // uh oh
                        Output.initialError("Both -file and -at arguments provided.  This is not permitted.\nFor help, try:  java ec.Evolve -help");
                    else 
                        cls = Class.forName(args[x+1]);
                    break;
                    }
                catch (Exception e)
                    {
                    e.printStackTrace();
                    Output.initialError(
                        "An exception was generated upon extracting the class to load the parameter file relative to: " + args[x+1] + 
                        "\nFor help, try:  java ec.Evolve -help\n\n" + e);
                    }
                    
        // search for a resource (we may or may not use this)
        for(int x=0;x<args.length-1;x++)
            if (args[x].equals(A_FROM))
                try
                    {
                    if (parameters != null)  // uh oh
                        Output.initialError("Both -file and -from arguments provided.  This is not permitted.\nFor help, try:  java ec.Evolve -help");
                    else 
                        {
                        if (cls == null)  // no -at
                            cls = Evolve.class;
                        parameters = new ParameterDatabase(args[x+1], cls, args);
                        System.err.println("Using database resource location " + parameters.getLabel());
                        }
                    break;
                    }
                catch (Exception e)
                    {
                    e.printStackTrace();
                    Output.initialError(
                        "The parameter file is missing at the resource location: " + args[x+1] + " relative to the class: " + cls + "\n\nFor help, try:  java ec.Evolve -help");
                    }

        if (parameters == null)
            Output.initialError("No parameter or checkpoint file was specified.\nFor help, try:   java ec.Evolve -help" );
        return parameters;
        }
    
    
    /** Loads the number of threads. */
    public static int determineThreads(Output output, ParameterDatabase parameters, Parameter threadParameter)
        {
        int thread = 1;
        String tmp_s = parameters.getString(threadParameter,null);
        if (tmp_s==null) // uh oh
            {
            output.fatal("Threads number must exist.",threadParameter,null);
            }
        else if (V_THREADS_AUTO.equalsIgnoreCase(tmp_s))
            {
            Runtime runtime = Runtime.getRuntime();
            try { return ((Integer)runtime.getClass().getMethod("availableProcessors", (Class[])null).
                    invoke(runtime,(Object[])null)).intValue(); }
            catch (Exception e)
                { 
                output.fatal("Whoa! This Java version is too old to have the Runtime.availableProcessors() method available.\n" + 
                    "This means you can't use 'auto' as a threads option.",threadParameter,null);
                }
            }
        else
            {
            try
                {
                thread = parameters.getInt(threadParameter,null);
                if (thread <= 0)
                    output.fatal("Threads value must be > 0", threadParameter, null);
                }
            catch (NumberFormatException e)
                {
                output.fatal("Invalid, non-integer threads value ("+thread+")",threadParameter,null);
                }
            }
        return thread;
        }
        
    /** Primes the generator.  Mersenne Twister seeds its first 624 numbers using a basic
        linear congruential generator; thereafter it uses the MersenneTwister algorithm to
        build new seeds.  Those first 624 numbers are generally just fine, but to be extra
        safe, you can prime the generator by calling nextInt() on it some (N>1) * 624 times.
        This method does exactly that, presently with N=2. */
    public static MersenneTwisterFast primeGenerator(MersenneTwisterFast generator)
        {
        // 624 = MersenneTwisterFast.N  which is private duh
        for(int i = 0; i < 624 * 2 + 1; i++)
            generator.nextInt();
        return generator;
        }

    /** Loads a random generator seed.  First, the seed is loaded from the seedParameter.  If the parameter
        is V_SEED_TIME, the seed is set to the currentTime value.  Then the seed is incremented by the offset. 
        This method is broken out of initialize(...) primarily to share code with ec.eval.MasterProblem.*/
    public static int determineSeed(Output output, ParameterDatabase parameters, Parameter seedParameter, long currentTime, int offset, boolean auto)
        {
        int seed = 1;  // have to initialize to make the compiler happy
        String tmp_s = parameters.getString(seedParameter,null);
        if (tmp_s==null && !auto) // uh oh
            {
            output.fatal("Seed must exist.",seedParameter,null);
            }
        else if (V_SEED_TIME.equalsIgnoreCase(tmp_s) || (tmp_s == null && auto))
            {
            if (tmp_s == null && auto)
                output.warnOnce("Using automatic determination number of threads, but not all seeds are defined.\nThe rest will be defined using the wall clock time.");
            seed = (int)currentTime;  // using low-order bits so it's probably okay
            if (seed==0)
                output.fatal("Whoa! This Java version is returning 0 for System.currentTimeMillis(), which ain't right.  This means you can't use '"+V_SEED_TIME+"' as a seed ",seedParameter,null);
            }
        else
            {
            try
                {
                seed = parameters.getInt(seedParameter,null);
                }
            catch (NumberFormatException e)
                {
                output.fatal("Invalid, non-integer seed value ("+seed+")",seedParameter,null);
                }
            }
        return seed + offset;
        }


    /** Constructs and sets up an Output object. */
    
    public static Output buildOutput()
        {
        Output output;
        // 1. create the output

        output = new Output(true);

        // stdout is always log #0.  stderr is always log #1.
        // stderr accepts announcements, and both are fully verbose 
        // by default.
        output.addLog(ec.util.Log.D_STDOUT,false);
        output.addLog(ec.util.Log.D_STDERR,true);
                
        return output;
        }


    /** Initializes an evolutionary run given the parameters and a random seed adjustment (added to each random seed).
        The adjustment offers a convenient way to change the seeds of the random number generators each time you
        do a new evolutionary run.  You are of course welcome to replace the random number generators after initialize(...)
        but before startFresh(...) 
        
        <p>This method works by first setting up an Output (using buildOutput), then calling initialize(ParameterDatabase, seed, output)
    */
                
    public static EvolutionState initialize(ParameterDatabase parameters, int randomSeedOffset)
        {
        return initialize(parameters, randomSeedOffset, buildOutput());
        }


    /** Initializes an evolutionary run given the parameters and a random seed adjustment (added to each random seed),
        with the Output pre-constructed.
        The adjustment offers a convenient way to change the seeds of the random number generators each time you
        do a new evolutionary run.  You are of course welcome to replace the random number generators after initialize(...)
        but before startFresh(...) */
                
    public static EvolutionState initialize(ParameterDatabase parameters, int randomSeedOffset, Output output)
        {
        EvolutionState state=null;
        MersenneTwisterFast[] random;
        int[] seeds;
        int breedthreads = 1;
        int evalthreads = 1;
        boolean store;
        int x;
        
        // Should we muzzle stdout and stderr?
        
        if (parameters.exists(new Parameter(P_MUZZLE), null))
            output.warning("" + new Parameter(P_MUZZLE) + " has been deprecated.  We suggest you use " + 
                new Parameter(P_SILENT) + " or similar newer options.");
        
        if (parameters.getBoolean(new Parameter(P_SILENT), null, false) ||
            parameters.getBoolean(new Parameter(P_MUZZLE), null, false))
            {
            output.getLog(0).silent = true;
            output.getLog(1).silent = true;
            }

        // output was already created for us.  
        output.systemMessage(Version.message());
                
        // 2. set up thread values
        
        breedthreads = Evolve.determineThreads(output, parameters, new Parameter(P_BREEDTHREADS));
        evalthreads = Evolve.determineThreads(output, parameters, new Parameter(P_EVALTHREADS));
        boolean auto = (V_THREADS_AUTO.equalsIgnoreCase(parameters.getString(new Parameter(P_BREEDTHREADS),null)) ||
            V_THREADS_AUTO.equalsIgnoreCase(parameters.getString(new Parameter(P_EVALTHREADS),null)));  // at least one thread is automatic.  Seeds may need to be dynamic.

        // 3. create the Mersenne Twister random number generators,
        // one per thread

        random = new MersenneTwisterFast[breedthreads > evalthreads ? 
            breedthreads : evalthreads];
        seeds = new int[random.length];
                                                
        String seedMessage = "Seed: ";
        int time = (int)(System.currentTimeMillis());
        for (x=0;x<random.length;x++)
            {
            seeds[x] = determineSeed(output, parameters, new Parameter(P_SEED).push(""+x),
                time+x,random.length * randomSeedOffset, auto);
            for (int y=0;y<x;y++)
                if (seeds[x]==seeds[y])
                    output.fatal(P_SEED+"."+x+" ("+seeds[x]+") and "+P_SEED+"."+y+" ("+seeds[y]+") ought not be the same seed.",null,null); 
            random[x] = Evolve.primeGenerator(new MersenneTwisterFast(seeds[x]));    // we prime the generator to be more sure of randomness.
            seedMessage = seedMessage + seeds[x] + " ";
            }

        // 4.  Start up the evolution
                
        // what evolution state to use?
        state = (EvolutionState)
            parameters.getInstanceForParameter(new Parameter(P_STATE),null,
                EvolutionState.class);
        state.parameters = parameters;
        state.random = random;
        state.output = output;
        state.evalthreads = evalthreads;
        state.breedthreads = breedthreads;
        state.randomSeedOffset = randomSeedOffset;

        output.systemMessage("Threads:  breed/" + breedthreads + " eval/" + evalthreads);
        output.systemMessage(seedMessage);
                
        return state;
        }
                
                
    /** Begins a fresh evolutionary run with a given state.  The state should have been
        provided by initialize(...).  The jobPrefix is added to the front of output and
        checkpoint filenames.  If it's null, nothing is added to the front.  */
        
    public static void cleanup(EvolutionState state)
        {
        // flush the output
        state.output.flush();

        // Possibly print out the run parameters
        PrintWriter pw = new PrintWriter(System.err);
                
        // before we print out access information, we need to still "get" these
        // parameters, so that they show up as accessed and gotten.
        state.parameters.getBoolean(new Parameter(P_PRINTUSEDPARAMETERS),null,false);
        state.parameters.getBoolean(new Parameter(P_PRINTACCESSEDPARAMETERS),null,false);
        state.parameters.getBoolean(new Parameter(P_PRINTUNUSEDPARAMETERS),null,false);
        state.parameters.getBoolean(new Parameter(P_PRINTUNACCESSEDPARAMETERS),null,false);
        state.parameters.getBoolean(new Parameter(P_PRINTALLPARAMETERS),null,false);
                
        //...okay, here we go...
                
        if (state.parameters.getBoolean(new Parameter(P_PRINTUSEDPARAMETERS),null,false))
            {
            pw.println("\n\nUsed Parameters\n===============\n");
            state.parameters.listGotten(pw);
            }

        if (state.parameters.getBoolean(new Parameter(P_PRINTACCESSEDPARAMETERS),null,false))
            {
            pw.println("\n\nAccessed Parameters\n===================\n");
            state.parameters.listAccessed(pw);
            }

        if (state.parameters.getBoolean(new Parameter(P_PRINTUNUSEDPARAMETERS),null,false))
            {
            pw.println("\n\nUnused Parameters\n"+
                "================= (Ignore parent.x references) \n");
            state.parameters.listNotGotten(pw);
            }

        if (state.parameters.getBoolean(new Parameter(P_PRINTUNACCESSEDPARAMETERS),null,false))
            {
            pw.println("\n\nUnaccessed Parameters\n"+
                "===================== (Ignore parent.x references) \n");
            state.parameters.listNotAccessed(pw);
            }

        if (state.parameters.getBoolean(new Parameter(P_PRINTALLPARAMETERS),null,false))
            {
            pw.println("\n\nAll Parameters\n==============\n");
            // list only the parameters visible.  Shadowed parameters not shown
            state.parameters.list(pw,false);
            }

        pw.flush();

        System.err.flush();
        System.out.flush();
                
        // finish by closing down Output.  This is because gzipped and other buffered
        // streams just don't shut write themselves out, and finalize isn't called
        // on them because Java's being obnoxious.  Pretty stupid.
        state.output.close();
        }




    /*

     * MAIN
     * 
     * Evolve has... evolved from previous Evolves.  The goal behind these changes is:
     *       1. To provide a simple jobs facility
     *       2. To make it easy for you to make your own main(), including more
     *          sophisticated jobs facilities.
     * 
     * Before we get into the specifics of this file, let's first look at the main
     * evolution loop in EvolutionState.java.  The general code is:
     *       1.  If I was loaded from a checkpoint, call the hook startFromCheckpoint()
     *       2.  If I'm instead starting from scratch, call the hook startFresh() 
     *       3.  Loop:
     *               4. result = evolve() 
     *               5. If result != EvolutionState.R_NOTDONE, break from loop
     *       6.      Call the hook finish(result)
     * 
     * That's all there's to it.  Various EvolutionState classes need to implement
     * the startFromCheckpoint, startFresh, evolve, and finish methods.  This basic
     * evolution loop is encapsulated in a convenience method called EvolutionState.run(...).
     * 
     * Evolve.java is little more than code to fire up the right EvolutionState class,
     * call run(...), and then shut down.  The complexity mostly comes from bringing
     * up the class (loading it from checkpoint or from scratch) and in shutting down.
     * Here's the general mechanism:
     * 
     * - To load from checkpoint, we must find the checkpoint filename and call
     *       Checkpoint.restoreFromCheckpoint(filename) to generate the EvolutionState
     *       instance.  Evolve.java provides a convenience function for this called
     *       possiblyRestoreFromCheckpoint(...), which returns null if there *isn't*
     *       a checkpoint file to load from.  Else it returns the unfrozen EvolutionState.
     *       
     * - To instead set up from scratch, you have to do a bunch of stuff to set up the state.
     *       First, you need to load a parameter database.  Evolve.java has a convenience function
     *       for that called loadParameterDatabase(...).  Second, you must do a series
     *       of items: (1) generate an Output object (2) identify the number of threads
     *       (3) create the MersenneTwisterFast random number generators (4) instantiate
     *       the EvolutionState subclass instance (5) plug these items, plus the random 
     *       seed offset and the parameter database, into the instance.  These five
     *       steps are done for you in a convenience function called initialize(...).
     * 
     * -     Now the state is ready to go. Call run(...) on your EvolutionState
     *       (or do the evolution loop described above manually if you wish)
     * 
     * - Finally, to shut down, you need to (1) flush the Output (2) print out
     *       the used, accessed, unused, unaccessed, and all parameters if the user
     *       requested a printout at the end [rarely] (3) flush System.err and System.out
     *       for good measure, and (4) close Output -- which closes its streams except
     *       for System.err and System.out.  There is a convenience function for this as
     *       well.  It's called cleanup(...).
     *       
     * - Last, you shut down with System.exit(0) -- very important because it quits
     *       any remaining threads the user might have had running and forgot about.
     *       
     * So there you have it.  Several convenience functions in Evolve...
     *       Evolve.possiblyRestoreFromCheckpoint
     *       Evolve.loadParameterDatabase
     *       Evolve.initialize
     *       EvolutionState.run
     *       Evolve.cleanup
     * ... result in a very simple basic main() function:
     *       
     *
     *               public static void main(String[] args)
     *                       {
     *                       EvolutionState state = possiblyRestoreFromCheckpoint(args);
     *                       if (state!=null)  // loaded from checkpoint
     *                               state.run(EvolutionState.C_STARTED_FROM_CHECKPOINT);
     *                       else
     *                               {
     *                               state = initialize(loadParameterDatabase(args), 0);
     *                               state.run(EvolutionState.C_STARTED_FRESH);
     *                               }
     *                       cleanup(state);
     *                       System.exit(0);
     *                       }
     *
     *
     * Piece of cake!
     * 
     * The more extravagant main(...) you see below just has a few extra gizmos for
     * doing basic job iteration.  EvolutionState has two convenience slots for
     * doing job iteration:
     *
     *       job                                     (an Object[]    use this as you like)
     *       runtimeArguments        (a String[]             put args in here)
     *
     * The reason these are slots in EvolutionState is so you can store this information
     * across checkpoints and continue where you had started job-number-wise when the
     * user starts up from a checkpoint again.
     * 
     * You'll probably want the EvolutionState to output its stat files etc. using unique
     * prefixes to differentiate between jobs (0.stat, 1.stat, or whatever you like -- it
     * doesn't have to be numbers), and you'll also probably want checkpoint files to be
     * similarly prefixed.  So you'll probably want to do:
     *
     *       state.output.setFilePrefix(jobPrefix);
     *       state.checkpointPrefix = jobPrefix + state.checkpointPrefix;
     *
     * The extravagant main below is basically doing this.  We're using state.job to stash
     * away a single iterated job number, stored as an Integer in state.job[0], and then
     * iterating that way, making sure we stash the job number and runtime arguments each time 
     * so we can recover them when loading from checkpoint.  We use the "jobs" parameter 
     * to determine how many jobs to run.  If this number is 1, we don't even bother to set
     * the file prefixes, so ECJ generates files just like it used to.
     *
     * It's important to note that this main was created with the assumption that you might
     * modify it for your own purposes.  Do you want a nested loop, perhaps to do all combinations
     * of two parameters or something?  Rewrite it to use two array slots in the job array.
     * Want to store more information on a per-job basis?  Feel free to use the job array any
     * way you like -- it's ONLY used by this main() loop.
     *
     */






    /** Top-level evolutionary loop.  */

    public static void main(String[] args)
        {
        EvolutionState state;
        ParameterDatabase parameters;
        
        // should we print the help message and quit?
        checkForHelp(args);
                
        // if we're loading from checkpoint, let's finish out the most recent job
        state = possiblyRestoreFromCheckpoint(args);
        int currentJob = 0;                             // the next job number (0 by default)

        // this simple job iterator just uses the 'jobs' parameter, iterating from 0 to 'jobs' - 1
        // inclusive.  The current job number is stored in state.jobs[0], so we'll begin there if
        // we had loaded from checkpoint.
                
        if (state != null)  // loaded from checkpoint
            {
            // extract the next job number from state.job[0] (where in this example we'll stash it)
            try
                {
                if (state.runtimeArguments == null)
                    Output.initialError("Checkpoint completed from job started by foreign program (probably GUI).  Exiting...");
                args = state.runtimeArguments;                          // restore runtime arguments from checkpoint
                currentJob = ((Integer)(state.job[0])).intValue() + 1;  // extract next job number
                }
            catch (Exception e)
                {
                Output.initialError("EvolutionState's jobs variable is not set up properly.  Exiting...");
                }

            state.run(EvolutionState.C_STARTED_FROM_CHECKPOINT);
            cleanup(state);
            }

        // A this point we've finished out any previously-checkpointed job.  If there was
        // one such job, we've updated the current job number (currentJob) to the next number.
        // Otherwise currentJob is 0.

        // Now we're going to load the parameter database to see if there are any more jobs.
        // We could have done this using the previous parameter database, but it's no big deal.
        parameters = loadParameterDatabase(args);
        if (currentJob == 0)  // no current job number yet
            currentJob = parameters.getIntWithDefault(new Parameter("current-job"), null, 0);
        if (currentJob < 0)
            Output.initialError("The 'current-job' parameter must be >= 0 (or not exist, which defaults to 0)");
            
        int numJobs = parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        if (numJobs < 1)
            Output.initialError("The 'jobs' parameter must be >= 1 (or not exist, which defaults to 1)");
                
                
        // Now we know how many jobs remain.  Let's loop for that many jobs.  Each time we'll
        // load the parameter database scratch (except the first time where we reuse the one we
        // just loaded a second ago).  The reason we reload from scratch each time is that the
        // experimenter is free to scribble all over the parameter database and it'd be nice to
        // have everything fresh and clean.  It doesn't take long to load the database anyway,
        // it's usually small.
        for(int job = currentJob ; job < numJobs; job++)
            {
            // We used to have a try/catch here to catch errors thrown by this job and continue to the next.
            // But the most common error is an OutOfMemoryException, and printing its stack trace would
            // just create another OutOfMemoryException!  Which dies anyway and has a worthless stack
            // trace as a result.
                        
            // try
                {
                // load the parameter database (reusing the very first if it exists)
                if (parameters == null)
                    parameters = loadParameterDatabase(args);
                            
                // Initialize the EvolutionState, then set its job variables
                state = initialize(parameters, job);                // pass in job# as the seed increment
                state.output.systemMessage("Job: " + job);
                state.job = new Object[1];                                  // make the job argument storage
                state.job[0] = Integer.valueOf(job);                    // stick the current job in our job storage
                state.runtimeArguments = args;                              // stick the runtime arguments in our storage
                if (numJobs > 1)                                                    // only if iterating (so we can be backwards-compatible),
                    {
                    String jobFilePrefix = "job." + job + ".";
                    state.output.setFilePrefix(jobFilePrefix);     // add a prefix for checkpoint/output files 
                    state.checkpointPrefix = jobFilePrefix + state.checkpointPrefix;  // also set up checkpoint prefix
                    }
                                    
                // Here you can set up the EvolutionState's parameters further before it's setup(...).
                // This includes replacing the random number generators, changing values in state.parameters,
                // changing instance variables (except for job and runtimeArguments, please), etc.





                // now we let it go
                state.run(EvolutionState.C_STARTED_FRESH);
                cleanup(state);  // flush and close various streams, print out parameters if necessary
                parameters = null;  // so we load a fresh database next time around
                }
            /*
              catch (Throwable e)  // such as an out of memory error caused by this job
              {
              e.printStackTrace();
              state = null;
              System.gc();  // take a shot!
              }
            */
            }

        System.exit(0);
        }
    }
