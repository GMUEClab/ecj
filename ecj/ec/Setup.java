/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.util.Parameter;
import java.io.Serializable;

/* 
 * Setup.java
 * 
 * Created: Mon Oct  4 17:15:44 1999
 * By: Sean Luke
 */

/**
 * Setup classes are classes which get set up once from user-supplied parameters
 * prior to being used.
 *
 * Defines a single method, setup(...), which is called at least once for the
 * object, or for some object from which it was cloned.  This method
 * allows the object to set itself up by reading from parameter lists and
 * files on-disk.  You may assume that this method is called in a non-threaded
 * environment, hence your thread number is 0 (so you can determine which
 * random number generator to use).
 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface Setup extends Serializable
    {
    /** Sets up the object by reading it from the parameters stored
        in <i>state</i>, built off of the parameter base <i>base</i>.
        If an ancestor implements this method, be sure to call
        super.setup(state,base);  before you do anything else. */
    public void setup(final EvolutionState state, final Parameter base);
    }
