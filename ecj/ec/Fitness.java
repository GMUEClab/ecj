/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import java.io.*;
import ec.util.*;

/*
 * Fitness.java
 *
 * Created: Tue Aug 10 20:10:42 1999
 * By: Sean Luke
 */

/**
 * Fitness is a prototype which describes the fitness of an individual.
 * Every individual contains exactly one Fitness object.
 * Fitness objects are compared to each other with the equivalentTo()
 * and betterThan(), etc. methods. 
 *
 <p>Rules: 
 <table>
 <tr><td><b>comparison</b></td><td><b>method</b></td></tr>
 <tr><td>a &gt; b</td><td>a.betterThan(b)</td>
 <tr><td>a &gt;= b</td><td>a.betterThan(b) || a.equivalentTo(b)</td>
 <tr><td>a = b</td><td>a.equivalentTo(b)</td>
 </table>

 This applies even to multiobjective pareto-style dominance, eg:
 <ul>
 <li> a dominates b :: a &gt; b
 <li> a and b do not dominate each other :: a = b
 <li> b dominates a :: a &lt; b
 </ul>

 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><tt>fit</tt></td>
 <td>default fitness base</td></tr>
 </table>

 * @author Sean Luke
 * @version 1.0
 */


public abstract class Fitness implements Prototype
    {
    /** base parameter for defaults */
    public static final String P_FITNESS = "fitness";

    /** Basic preamble for printing Fitness values out */
    public static final String FITNESS_PREAMBLE = "Fitness: ";

    /** Should return an absolute fitness value ranging from negative
        infinity to infinity, NOT inclusive (thus infinity, negative
        infinity, and NaN are NOT valid fitness values).  This should
        be interpreted as: negative infinity is worse than the WORST
        possible fitness, and positive infinity is better than the IDEAL
        fitness.
        
        <p>You are free to restrict this range any way you like: for example,
        your fitness values might fall in the range [-5.32, 2.3]
        
        <p>Selection methods relying on fitness proportionate information will
        <b>assume the fitness is non-negative</b> and should throw an error
        if it is not.  Thus if you plan on using FitProportionateSelection, 
        BestSelection, or
        GreedyOverselection, for example, your fitnesses should assume that 0
        is the worst fitness and positive fitness are better.  If you're using
        other selection methods (Tournament selection, various ES selection
        procedures, etc.) your fitness values can be anything.
        
        <p>Similarly, if you're writing a selection method and it needs positive
        fitnesses, you should check for negative values and issue an error; and
        if your selection method doesn't need an <i>absolute</i> fitness
        value, it should use the equivalentTo() and betterThan() methods instead.
        
        <p> If your fitness scheme does not use a metric quantifiable to
        a single positive value (for example, MultiObjectiveFitness), you should 
        perform some reasonable translation.
    */
    public abstract float fitness();

    /** Should return true if this is a good enough fitness to end the run */
    public abstract boolean isIdealFitness();

    /** Should return true if this fitness is in the same equivalence class
        as _fitness, that is, neither is clearly better or worse than the
        other.  You may assume that _fitness is of the same class as yourself.
        For any two fitnesses fit1 and fit2 of the same class,
        it must be the case that fit1.equivalentTo(fit2) == fit2.equivalentTo(fit1),
        and that only one of fit1.betterThan(fit2), fit1.equivalentTo(fit2),
        and fit2.betterThan(fit1) can be true.
    */
    public abstract boolean equivalentTo(Fitness _fitness);
    
    /** Should return true if this fitness is clearly better than _fitness;
        You may assume that _fitness is of the same class as yourself. 
        For any two fitnesses fit1 and fit2 of the same class,
        it must be the case that fit1.equivalentTo(fit2) == fit2.equivalentTo(fit1),
        and that only one of fit1.betterThan(fit2), fit1.equivalentTo(fit2),
        and fit2.betterThan(fit1) can be true.
    */ 
    public abstract boolean betterThan(Fitness _fitness);

    /** Should print the fitness out fashion pleasing for humans to read, 
        using state.output.println(...,verbosity,log).  The default version
        of this method calls fitnessToStringForHumans() and println's the
        resultant string.
    */
    public void printFitnessForHumans(EvolutionState state, int log, 
                                      int verbosity)
        {
        state.output.println( fitnessToStringForHumans(), verbosity, log);
        }

    /** Should print the fitness out in a computer-readable fashion, 
        using state.output.println(...,verbosity,log).  You might use
        ec.util.Code to encode fitness values.  The default version
        of this method calls fitnessToString() and println's the
        resultant string.
    */
    public void printFitness(EvolutionState state, int log, 
                             int verbosity)
        {
        state.output.println( fitnessToString(), verbosity, log);
        }
    
    /** Should print the fitness out in a computer-readable fashion, 
        using writer.println(...).  You might use
        ec.util.Code to encode fitness values.  The default version
        of this method calls fitnessToString() and println's the
        resultant string.
    */
    public void printFitness(final EvolutionState state,
                             final PrintWriter writer)
        {
        writer.println( fitnessToString() );
        }

    /** Reads in the fitness from a form outputted by fitnessToString() and thus
        printFitnessForHumans(...).  The default version of this method
        exits the program with an "unimplemented" error. */ 
    public void readFitness(final EvolutionState state, 
                            final LineNumberReader reader)
        throws IOException
        {
        state.output.fatal("readFitness(EvolutionState, DataOutput)  not implemented in " + this.getClass());
        }
        
    /** Print to a string the fitness in a fashion readable by humans, and not intended
        to be parsed in again.  The default form
        simply calls toString(), but you'll probably want to override this to something else. */
    public String fitnessToStringForHumans()
        {
        return toString();
        }
        
    /** Print to a string the fitness in a fashion intended
        to be parsed in again via readFitness(...).
        The fitness and evaluated flag should not be included.  The default form
        simply calls toString(), which is almost certainly wrong, 
        and you'll probably want to override this to something else. */
    public String fitnessToString()
        {
        return toString();
        }
        
    /** Writes the binary form of an individual out to a DataOutput.  This is not for serialization:
        the object should only write out the data relevant to the object sufficient to rebuild it from a DataInput.
        The default version exits the program with an "unimplemented" error; you should override this.
    */
    public void writeFitness(final EvolutionState state,
                             final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeFitness(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }


    /** Reads the binary form of an individual from a DataInput.  This is not for serialization:
        the object should only read in the data written out via printIndividual(state,dataInput).  
        The default version exits the program with an "unimplemented" error; you should override this.
    */
    public void readFitness(final EvolutionState state,
                            final DataInput dataInput) throws IOException
        {
        state.output.fatal("readFitness(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }

    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }



    public void setup(EvolutionState state, Parameter base)
        {
        // by default does nothing
        }
    }

