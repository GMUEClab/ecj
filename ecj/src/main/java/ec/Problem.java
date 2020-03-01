/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec; 

import ec.util.*;
import java.io.*;

/* 
 * Problem.java
 * 
 * Created: Fri Oct 15 14:16:17 1999
 * By: Sean Luke
 */

/**
 * Problem is a prototype which defines the problem against which we will
 * evaluate individuals in a population. 
 *
 * <p>Since Problems are Prototypes, you should expect a new Problem class to be
 * cloned and used, on a per-thread basis, for the evolution of each
 * chunk of individuals in a new population.  If you for some reason
 * need global Problem information, you will have to provide it
 * statically, or copy pointers over during the clone() process
 * (there is likely only one Problem prototype, depending on the
 * Evaluator class used).
 *
 * <p>Note that Problem does not implement a specific evaluation method.
 * Your particular Problem subclass will need to implement a some kind of
 * Problem Form (for example, SimpleProblemForm) appropriate to the kind of
 * evaluation being performed on the Problem.  These Problem Forms will provide
 * the evaluation methods necessary.
 *
 * <p>Problem forms will define some kind of <i>evaluation</i> method.  This method
 * may be called in one of two ways by the Evaluator.
 *
 * <ul> 
 * <li> The evaluation is called for a series of individuals.  This is the old approach,
 * and it means that each individual must be evaluated and modified as specified by the
 * Problem Form during the evaluation call.
 * <li> prepareToEvaluate is called, then a series of individuals is evaluated, and then
 * finishEvaluating is called.  This is the new approach, and in this case the Problem
 * is free to delay evaluating and modifying the individuals until finishEvaluating has
 * been called.  The Problem may perfectly well evaluate and modify the individuals during
 * each evaluation call if it likes.  It's just given this additional option.
 * </ul>
 *
 * <p>Problems should be prepared for both of the above situations.  The easiest way
 * to handle it is to simply evaluate each individual as his evaluate(...) method is called,
 * and do nothing during prepareToEvaluate or finishEvaluating.  That should be true for
 * the vast majority of Problem types.
 *
 * @author Sean Luke
 * @version 2.0 
 */

public abstract class Problem implements Prototype
    {
    private static final long serialVersionUID = 1;

    public static final String P_PROBLEM = "problem";
    
    /** Here's a nice default base for you -- you can change it if you like */
    public Parameter defaultBase()
        {
        return new Parameter(P_PROBLEM);
        }

    // default form does nothing
    public void setup(final EvolutionState state, final Parameter base) 
        {
        }

    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }

    /** May be called by the Evaluator prior to a series of individuals to 
        evaluate, and then ended with a finishEvaluating(...).  If this is the
        case then the Problem is free to delay modifying the individuals or their
        fitnesses until at finishEvaluating(...).  If no prepareToEvaluate(...)
        is called prior to evaluation, the Problem must complete its modification
        of the individuals and their fitnesses as they are evaluated as stipulated
        in the relevant evaluate(...) documentation for SimpleProblemForm 
        or GroupedProblemForm.  The default method does nothing.  Note that
        prepareToEvaluate() can be called *multiple times* prior to finishEvaluating()
        being called -- in this case, the subsequent calls may be ignored. */
    public void prepareToEvaluate(final EvolutionState state, final int threadnum)
        {
        }
        
    /** Will be called by the Evaluator after prepareToEvaluate(...) is called
        and then a series of individuals are evaluated.  However individuals may
        be evaluated without prepareToEvaluate or finishEvaluating being called
        at all.  See the documentation for prepareToEvaluate for more information. 
        The default method does nothing.*/
    public void finishEvaluating(final EvolutionState state, final int threadnum)
        {
        }

    /** Called to set up remote evaluation network contacts when the run is started.  By default does nothing. */
    public void initializeContacts( EvolutionState state )
        {
        }

    /**  Called to reinitialize remote evaluation network contacts when the run is restarted from checkpoint.  By default does nothing. */
    public void reinitializeContacts( EvolutionState state )
        {
        }
    
    /**  Called to shut down remote evaluation network contacts when the run is completed.  By default does nothing. */
    public void closeContacts(EvolutionState state, int result)
        {
        }
        
    /** Asynchronous Steady-State EC only: Returns true if the problem is ready to evaluate.  In most cases, 
        the default is true.  */ 
    public boolean canEvaluate()
        { 
        return true; 
        }

    /** Part of SimpleProblemForm.  Included here so you don't have to write the default version, which usually does nothing. */
    public void describe(
        final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum,
        final int log)
        {
        return;
        }

    /** @deprecated  Use the version without verbosity */
    public final void describe(final Individual ind, 
        final EvolutionState state, 
        final int subpopulation,
        final int threadnum,
        final int log,
        final int verbosity)
        {
        describe(state, ind, subpopulation, threadnum, log);
        }

    /** This method is called from the SlaveMonitor's accept() thread to optionally send additional data to the
        Slave via the dataOut stream.  By default it does nothing.  If you override this you must also override (and use) 
        receiveAdditionalData() and transferAdditionalData(). */
    public void sendAdditionalData(EvolutionState state, DataOutputStream dataOut)
        {
        // do nothing
        }

    /** This method is called on a Problem by the Slave.  You should use this method to store away
        received data via the dataIn stream for later transferring to the current EvolutionState via the
        transferAdditionalData method.  You should NOT expect this Problem to be used for by the Slave
        for evolution (though it might).  By default this method does nothing, which is the usual situation. 
        The EvolutionState is provided solely for you to be able to output warnings and errors: do not rely
        on it for any other purpose (including access of the random number generator or storing any data).  */
    public void receiveAdditionalData(EvolutionState state, DataInputStream dataIn)
        {
        // do nothing
        }

    /** This method is called by a Slave to transfer data previously loaded via receiveAdditionalData() to
        a running EvolutionState at the beginning of evolution.  It may be called multiple times if multiple
        EvolutionStates are created. By default this method does nothing, which is the usual situation. */
    public void transferAdditionalData(EvolutionState state)
        {
        // do nothing
        }
    }


