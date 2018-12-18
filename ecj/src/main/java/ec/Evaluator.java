/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.ParamClassLoadException;
import ec.util.Parameter;
import ec.eval.MasterProblem;

/* 
 * Evaluator.java
 * 
 * Created: Tue Aug 10 20:53:30 1999
 * By: Sean Luke
 */

/**
 * An Evaluator is a singleton object which is responsible for the
 * evaluation process during the course of an evolutionary run.  Only
 * one Evaluator is created in a run, and is stored in the EvolutionState
 * object.
 *
 * <p>Evaluators typically do their work by applying an instance of some
 * subclass of Problem to individuals in the population.  Evaluators come
 * with a Problem prototype which they may clone as necessary to create
 * new Problem spaces to evaluate individuals in (Problems may be reused
 * to prevent constant cloning).
 *
 * <p>Evaluators may be multithreaded, with one Problem instance per thread
 * usually.  The number of threads they may spawn (excepting a parent
 * "gathering" thread) is governed by the EvolutionState's evalthreads value.
 *
 * <p>Be careful about spawning threads -- this system has no few synchronized 
 * methods for efficiency's sake, so you must either divvy up evaluation in 
 * a thread-safe fashion, or 
 * otherwise you must obtain the appropriate locks on individuals in the population
 * and other objects as necessary.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.problem</tt><br>
 <font size=-1>classname, inherits and != ec.Problem</font></td>
 <td valign=top>(the class for the Problem prototype p_problem)</td></tr>
 <tr><td valign=top><i>base</i><tt>.masterproblem</tt><br>
 <font size=-1>classname, inherits</font></td>
 <td valign=top>(the class for the MasterProblem prototype masterproblem)</td></tr>
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Evaluator implements Singleton
    {
    public static final String P_PROBLEM = "problem";

    public Problem p_problem;
    public MasterProblem masterproblem = null;

    public static final String P_MASTERPROBLEM = "masterproblem";
    public static final String P_IAMSLAVE = "i-am-slave";
    
    /** Evaluates the fitness of an entire population.  You will
        have to determine how to handle multiple threads on your own,
        as this is a very domain-specific thing. */
    public abstract void evaluatePopulation(final EvolutionState state);

    /** Returns non-NULL if the Evaluator believes that the run is
        finished: perhaps an ideal individual has been found or some
        other run result has shortcircuited the run so that it should
        end prematurely right now.  Typically a message is stored in
        the String for the user to know why the system shut down.*/
    public abstract String runComplete(final EvolutionState state);

    public String runComplete;
    /** Requests that the Evaluator quit soon for a user-defined reason provided in the message. */
    public void setRunComplete(String message) { runComplete = message; }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // Load my problem
        p_problem = (Problem)(state.parameters.getInstanceForParameter(
                base.push(P_PROBLEM),null,Problem.class));
        p_problem.setup(state,base.push(P_PROBLEM));
        
        // Am I a master problem and NOT a slave.  Note that the "eval.i-am-slave" parameter
        // is not set by the user but rather programmatically by the Slave.java class
        if(state.parameters.exists(base.push(P_MASTERPROBLEM),null)) // there's a master problem to load
            {
            // load the masterproblem so it can be accessed by the Slave as well (even though it's not used in its official capacity)
            masterproblem = (MasterProblem)(state.parameters.getInstanceForParameter(
                    base.push(P_MASTERPROBLEM),null,Problem.class));
            masterproblem.setup(state,base.push(P_MASTERPROBLEM));

            if (!state.parameters.getBoolean(base.push(P_IAMSLAVE),null,false))  // I am a master (or possibly a slave -- same params)
                {
                
                //try {
                //    Problem masterproblem = (Problem)(state.parameters.getInstanceForParameter(
                //            base.push(P_MASTERPROBLEM),null,Problem.class));
                //    masterproblem.setup(state,base.push(P_MASTERPROBLEM));
                                         
                /*
                 * If a MasterProblem was specified, interpose it between the
                 * evaluator and the real problem.  This allows seamless use
                 * of the master problem.
                 */
                masterproblem.problem = p_problem;
                p_problem = masterproblem;
                //     }
                // catch(ParamClassLoadException e)
                //     {
                //     state.output.fatal("Parameter has an invalid value: "+base.push(P_MASTERPROBLEM));
                //     }
                }
            }
        }
    
    /** Called to set up remote evaluation network contacts when the run is started.  Mostly used for client/server evaluation (see MasterProblem).  By default calls p_problem.initializeContacts(state) */
    public void initializeContacts(EvolutionState state)
        {
        p_problem.initializeContacts(state);
        }
    
    /**  Called to reinitialize remote evaluation network contacts when the run is restarted from checkpoint.  Mostly used for client/server evaluation (see MasterProblem).  By default calls p_problem.reinitializeContacts(state) */
    public void reinitializeContacts(EvolutionState state)
        {
        p_problem.reinitializeContacts(state);
        }
        
    /**  Called to shut down remote evaluation network contacts when the run is completed.  Mostly used for client/server evaluation (see MasterProblem).  By default calls p_problem.closeContacts(state,result) */
    public void closeContacts(EvolutionState state, int result)
        {
        p_problem.closeContacts(state,result);
        }
    }
