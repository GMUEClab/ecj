/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

/* 
 * Initializer.java
 * 
 * Created: Tue Aug 10 21:07:42 1999
 * By: Sean Luke
 */

/**
 * The Initializer is a singleton object whose job is to initialize the
 * population at the beginning of the run.  It does this by providing
 * a population through the initialPopulation(...) method.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>pop</tt><br>
 <font size=-1>classname, inherits or = ec.Population</font></td>
 <td valign=top>(the class for a new population)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><tt>pop</tt></td>
 <td>The base for a new population's set up parameters</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0 
 */

public abstract class Initializer implements Singleton
    {
    /** parameter for a new population */
    public static final String P_POP = "pop";

    /** Creates and returns a new initial population for the evolutionary run.
        This is commonly done by creating a Population, setting it up (call
        setup() on it!), and calling its populate() method. This method
        will likely only be called once in a run. */
    public abstract Population initialPopulation(final EvolutionState state, int thread);
    }
