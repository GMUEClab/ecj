/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * RuleIndividual.java
 * 
 * Created: Tue May 29 18:20:20 EDT 2001
 * By: Sean Luke
 */

/**
 * RuleIndividual is an Individual with an array of RuleSets, each of which
 * is a set of Rules.  RuleIndividuals belong to some subclass of RuleSpecies
 * (or just RuleSpecies itself).
 *
 * <p>RuleIndividuals really have basically one parameter: the number
 * of RuleSets to use.  This is determined by the <tt>num-rulesets</tt>
 * parameter.

 * <P><b>From ec.Individual:</b>  
 *
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b>reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans<b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>

 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the Rulesets.  If you add instance variables to
 * a RuleIndividual or subclass, you'll need to read/write those variables as well.

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-rulesets</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>(number of rulesets used)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>ruleset</tt>.<i>n</i><br>
 <font size=-1>Classname, subclass of or = ec.rule.RuleSet</font></td>
 <td valign=top>(class of ruleset <i>n</i>)</td></tr>
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ruleset</tt>.<i>n</i><br>
 <td>RuleSet <i>n</i></td></tr>
 </table>

 <p><b>Default Base</b><br>
 rule.individual

 * @author Sean Luke
 * @version 1.0 
 */
public class RuleIndividual extends Individual
    {
    private static final long serialVersionUID = 1;

    public static final String P_RULESET = "ruleset";
    public static final String P_NUMRULESETS = "num-rulesets";
    
    /** The individual's rulesets. */
    public RuleSet[] rulesets;
    
    public Parameter defaultBase()
        {
        return RuleDefaults.base().push(P_INDIVIDUAL);
        }

    public Object clone()
        {
        RuleIndividual myobj = (RuleIndividual) (super.clone());   
        myobj.rulesets = new RuleSet[rulesets.length];
        for(int x=0;x<rulesets.length;x++) 
            myobj.rulesets[x] = (RuleSet)(rulesets[x].clone());
        return myobj;
        } 

    /** Called by pipelines before they've modified the individual and
        it might need to be "fixed"  -- basically a hook for you to override.
        By default, calls validateRules on each ruleset. */
    public void preprocessIndividual(final EvolutionState state, final int thread)
        {
        for (int x=0;x<rulesets.length;x++)
            rulesets[x].preprocessRules(state,thread);
        }

    /** Called by pipelines after they've modified the individual and
        it might need to be "fixed"  -- basically a hook for you to override.
        By default, calls validateRules on each ruleset. */
    public void postprocessIndividual(final EvolutionState state, final int thread)
        {
        for (int x=0;x<rulesets.length;x++)
            rulesets[x].postprocessRules(state,thread);
        }
        
    public boolean equals(Object ind)
        {
        if (ind == null) return false;
        // My loose definition: ind must be a 
        if (!getClass().equals(ind.getClass()))  // not the same class, I'm conservative that way
            return false;

        RuleIndividual other = (RuleIndividual)ind;
        if (rulesets.length != other.rulesets.length) return false;
        for(int x=0;x<rulesets.length;x++)
            if (!rulesets[x].equals(other.rulesets[x])) return false;
        return true;
        }

    public int hashCode()
        {
        int hash = this.getClass().hashCode();
        for(int x=0;x<rulesets.length;x++)
            // rotate hash and XOR
            hash =
                (hash << 1 | hash >>> 31 ) ^ rulesets[x].hashCode();
        return hash;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        // I'm the top-level setup, I guess
        int numrulesets = state.parameters.getInt(
            base.push(P_NUMRULESETS), defaultBase().push(P_NUMRULESETS),
            1);  // need at least 1 ruleset!
        if (numrulesets == 0)
            state.output.fatal("RuleIndividual needs at least one RuleSet!",
                base.push(P_NUMRULESETS), defaultBase().push(P_NUMRULESETS));

        rulesets  = new RuleSet[numrulesets];

        for(int x=0;x<numrulesets;x++)
            {
            rulesets[x] = (RuleSet)(state.parameters.getInstanceForParameterEq(
                    base.push(P_RULESET).push(""+x),defaultBase().push(P_RULESET),
                    RuleSet.class));
            rulesets[x].setup(state,base.push(P_RULESET).push(""+x));
            }
        }

    public void printIndividualForHumans(final EvolutionState state,
        final int log)
        {
        state.output.println(EVALUATED_PREAMBLE + (evaluated ? "true" : "false"), log);
        fitness.printFitnessForHumans(state,log);
        for(int x=0;x<rulesets.length;x++)
            {
            state.output.println("Ruleset " + x + ":", log);
            rulesets[x].printRuleSetForHumans(state, log);
            }
        }

    public void printIndividual(final EvolutionState state,
        final int log)
        {
        state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), log);
        fitness.printFitness(state, log);
        for(int x=0;x<rulesets.length;x++)
            {
            state.output.println("Ruleset " + x + ":", log);
            rulesets[x].printRuleSet(state,log);
            }
        }

    /** Overridden for the RuleIndividual genotype, writing each ruleset in turn. */
    public void printIndividual(final EvolutionState state,
        final PrintWriter writer)
        {
        writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
        fitness.printFitness(state,writer);
        for(int x=0;x<rulesets.length;x++)
            {
            writer.println("Ruleset " + x + ":");
            rulesets[x].printRuleSet(state,writer);
            }
        }
    
    /** Overridden for the RuleIndividual genotype, writing each ruleset in turn. */
    public void writeGenotype(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(rulesets.length);
        for(int x=0;x<rulesets.length;x++)
            rulesets[x].writeRuleSet(state,dataOutput);
        }

    /** Overridden for the RuleIndividual genotype. */
    public void readGenotype(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (rulesets==null || rulesets.length != len)
            state.output.fatal("Number of RuleSets differ in RuleIndividual when reading from readGenotype(EvolutionState, DataInput).");
        for(int x=0;x<rulesets.length;x++)
            rulesets[x].readRuleSet(state,dataInput);
        }


    /** Overridden for the RuleIndividual genotype. */
    public void parseGenotype(final EvolutionState state, 
        final LineNumberReader reader)
        throws IOException
        {
        // read my ruleset
        for(int x=0;x<rulesets.length;x++)
            {
            reader.readLine();  // throw it away -- it's the ruleset# indicator
            rulesets[x].readRuleSet(state,reader);
            }
        }

    public long size() 
        { 
        long size=0;
        for(int x=0;x<rulesets.length;x++) 
            size+= rulesets[x].numRules();
        return size;
        }
    
    public void reset(EvolutionState state, int thread)
        {
        for(int x=0;x<rulesets.length;x++) 
            rulesets[x].reset(state,thread);
        }

    /** Mutates the Individual.  The default implementation simply calls mutate(...) on each of
        the RuleSets. */
    public void mutate(EvolutionState state, int thread)
        {
        for(int x=0;x<rulesets.length;x++) 
            rulesets[x].mutate(state,thread);
        }
    }

