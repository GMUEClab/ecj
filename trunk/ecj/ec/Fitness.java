/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import java.io.*;
import ec.util.*;
import java.util.*;

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


public abstract class Fitness implements Prototype, Comparable
    {
    /** Auxiliary variable, used by coevolutionary processes, to compute the
        number of trials used to compute this Fitness value.  By default trials=null and stays that way. */
    public ArrayList trials = null;
	
	/** Auxiliary variable, used by coevolutionary processes, to store the individuals
		involved in producing this given Fitness value.  By default context=null and stays that way.
		Note that individuals stored here may possibly not themselves have Fitness values to avoid
		circularity when cloning.
		*/
	public Individual[] context = null;

	public void setContext(Individual[] cont)
		{
		if (cont == null)
			context = null;
		else // make sure it's deep-cloned and stripped of context itself
			{
			context = new Individual[cont.length];
			for(int i = 0; i < cont.length; i++)
				{
				if (cont[i] == null)
					context[i] = null;
				else 
					{
					// we first remove context so we don't have any circularity in cloning 
					Individual[] c = cont[i].fitness.context;
					cont[i].fitness.context = null;
					
					// now clone the individual in place
					context[i] = (Individual)(cont[i].clone());
					
					// now put the context back
					cont[i].fitness.context = c;
					}
				}
			}
		}
	
	/** Treat the Individual[] you receive from this as read-only. */
	public Individual[] getContext()
		{
		return context;
		}

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
        with a verbosity of Output.V_NO_GENERAL.
    */
    public void printFitnessForHumans(EvolutionState state, int log)
        {
        printFitnessForHumans( state, log, Output.V_NO_GENERAL);
        }

    /** Should print the fitness out fashion pleasing for humans to read, 
        using state.output.println(...,verbosity,log).  The default version
        of this method calls fitnessToStringForHumans(), adds context (collaborators) if any,
		and printlns the resultant string.
        @deprecated Verbosity no longer has meaning
    */
    public void printFitnessForHumans(EvolutionState state, int log, 
        int verbosity)
        {
		String s = fitnessToStringForHumans();
		if (context != null)
			{
			for(int i = 0; i < context.length; i++)
				{
				if (context[i] != null)
					{
					s += "\nCollaborator " + i + ": ";
					// temporarily de-link the context of the collaborator
					// to avoid loops
					Individual[] c = context[i].fitness.context;
					context[i].fitness.context = null;
					s += context[i].genotypeToStringForHumans();
					// relink
					context[i].fitness.context = c;
					}
				else // that's me!
					{
					// do nothing
					}
				}
			}
        state.output.println( s, verbosity, log);
        }

    /** Should print the fitness out in a computer-readable fashion, 
        with a verbosity of Output.V_NO_GENERAL.
    */
    public void printFitness(EvolutionState state, int log)
        {
        printFitness( state, log, Output.V_NO_GENERAL);
        }

    /** Should print the fitness out in a computer-readable fashion, 
        using state.output.println(...,verbosity,log).  You might use
        ec.util.Code to encode fitness values.  The default version
        of this method calls fitnessToString() and println's the
        resultant string.
        @deprecated Verbosity no longer has meaning
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
        and you'll probably want to override this to something else.  When overriding, you
		may wish to check to see if the 'trials' variable is non-null, and issue an error if so.  */
    public String fitnessToString()
        {
        return toString();
        }
        
    /** Writes the binary form of an individual out to a DataOutput.  This is not for serialization:
        the object should only write out the data relevant to the object sufficient to rebuild it from a DataInput.
        The default version exits the program with an "unimplemented" error; you should override this, and be
		certain to also write the 'trials' variable as well.
    */
    public void writeFitness(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeFitness(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }


	/** Writes trials out to DataOutput */
	public void writeTrials(final EvolutionState state, final DataOutput dataOutput) throws IOException
		{
		if (trials == null)
			dataOutput.writeInt(-1);
		else
			{
			int len = trials.size();
			dataOutput.writeInt(len);
			for(int i = 0; i < len; i++)
				dataOutput.writeDouble(((Double)(trials.get(i))).doubleValue());
			}
		}

    /** Reads the binary form of an individual from a DataInput.  This is not for serialization:
        the object should only read in the data written out via printIndividual(state,dataInput).  
        The default version exits the program with an "unimplemented" error; you should override this, and be
		certain to also write the 'trials' variable as well.
    */
    public void readFitness(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        state.output.fatal("readFitness(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }


	/** Reads trials in from DataInput. */
	public void readTrials(final EvolutionState state, final DataInput dataInput) throws IOException
		{
		int len = dataInput.readInt();
		if (len >= 0)
			{
			trials = new ArrayList(len);
			for(int i = 0; i < len; i++)
				trials.add(new Double(dataInput.readDouble()));
			}
		}

	double bestTrial(ArrayList l)
		{
		if (l == null || l.size() == 0) return Double.MIN_VALUE;
		double best = ((Double)(l.get(0))).doubleValue();
		int len = l.size();
		for (int i = 1 ; i < len; i ++)
			{
			double next = ((Double)(l.get(i))).doubleValue();
			if (next > best) best = next;
			}
		return best;
		}

	/** Merges the other fitness into this fitness.  The other fitnessis assumed to be more recent, and
		is assumed to be a clone which will never be used again, so we can steal things from it and
		share data with it.
		This method is typically called by coevolution in combination with distributed evauation where
		the Individual may be sent to various different sites to have trials performed on it, and
		the results must be merged together to form a relevant fitness.  By default the only thing that
		is "merged" are the trials arrays, as follows: the portion of 'other' trials which is different from the
		start of our trials is concatenated to the end of our trials.  For example, if our trials were
		123456 and the other trials were 123789, then the result would be 123456789.  In coevolution nothing
		else needs to be merged usually, though you may need to override this to handle other things specially.
		*/
	public void merge(EvolutionState state, Fitness other)
		{
		// first let's merge trials.  We assume they're Doubles
		
		if (other.trials == null) return;
		else if (trials == null && other.trials != null)
			{
			trials = new ArrayList(other.trials);   // copy him over
			context = other.getContext();			// grab the context
			}
		else  // gotta look for different strings and concatenate
			{
			// first question: who has the best context?
			if (bestTrial(trials) < bestTrial(other.trials)) // other is beter
				context = other.getContext();
			
			// now load the trials
			
			// find the common substring
			int len = Math.min(trials.size(), other.trials.size());
			int i = 0; 
			for( ; i < len; i++)
				if (!trials.get(i).equals(other.trials.get(i)))
					break;
			
			// at this point they differ, so we concatenate the rest into us
			len = other.trials.size();
			for(int j = i; j < len; j++)
				{ 
				trials.add(other.trials.get(j));
				}
			}
		}
		

    public Object clone()
        {
        try 
			{
			Fitness f = (Fitness)(super.clone());
			if (f.trials != null) f.trials = new ArrayList(trials);  // we can do a light clone because trials consist only of Doubles
			f.setContext(f.getContext()); // deep-clones and removes context just in case
			return f;
			}
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }


    public void setup(EvolutionState state, Parameter base)
        {
        // by default does nothing
        }

    /**
       Returns -1 if I am FITTER than the other Fitness, 1 if I am LESS FIT than the other Fitness,
       and 0 if we are equivalent.
    */
    public int compareTo(Object o)
        {
        Fitness other = (Fitness) o;
        if (this.betterThan(other)) return -1;
        if (other.betterThan(this)) return 1;
        return 0;
        }
        
    /** Sets the fitness to be the same value as the mean of the provided fitnesses.  The default
        version of this method exits with an "unimplemented" error; you should override this. */
    public void setToMeanOf(EvolutionState state, Fitness[] fitnesses)
        {
        state.output.fatal("setToMeanOf(EvolutionState, Fitness[]) not implemented in " + this.getClass());
        }
    }

