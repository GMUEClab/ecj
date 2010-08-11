/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * Rule.java
 * 
 * Created: Tue Feb 20 13:19:00 2001
 * By: Liviu Panait and Sean Luke
 */

/**
 * Rule is an abstract class for describing rules. It is abstract
 * because it is supposed to be extended by different classes
 * modelling different kinds of rules.
 * It provides the reset abstract method for randomizing the individual. 
 * It also provides the mutate function for mutating an individual rule
 * It also provides the clone function for cloning the rule.
 *
 * <p>You will need to implement some kind of artificial ordering between
 * rules in a ruleset using the Comparable interface,
 * so the ruleset can be sorted in such a way that it can be compared with
 * another ruleset for equality.  You should also implement hashCode
 * and equals 
 * in such a way that they aren't based on pointer information, but on actual
 * internal features. 
 *
 * <p>Every rule points to a RuleConstraints which handles information that
 * Rule shares with all the other Rules in a RuleSet.

 * <p>In addition to serialization for checkpointing, Rules may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeRule(...,DataOutput)/readRule(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives a Rule in binary.  It is the most efficient approach to sending
 * Rules over networks, etc.  The default versions of writeRule/readRule throw errors.
 * You don't need to implement them if you don't plan on using read/writeRule.
 *
 * <li><b>printRule(...,PrintWriter)/readRule(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives a Rule in text encoded such that the Rule is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will typically encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readRule</b>
 * reads in a line, then calls <b>readRuleFromString</b> on that line.
 * You are responsible for implementing readRuleFromString: the Code class is there to help you.
 * The default version throws an error if called.
 * <b>printRule</b> calls <b>printRuleToString<b>
 * and printlns the resultant string. You are responsible for implementing the printRuleToString method in such
 * a way that readRuleFromString can read back in the Rule println'd with printRuleToString.  The default form
 * of printRuleToString() simply calls <b>toString()</b> 
 * by default.  You might override <b>printRuleToString()</b> to provide better information.   You are not required to implement these methods, but without
 * them you will not be able to write Rules to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printRuleForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints a Rule in a fashion intended for human consumption only.
 * <b>printRuleForHumans</b> calls <b>printRuleToStringForHumans()<b> 
 * and printlns the resultant string.  The default form of this method just returns the value of
 * <b>toString()</b>. You may wish to override this to provide more information instead. 
 * You should handle one of these methods properly
 * to ensure Rules can be printed by ECJ.
 * </ul>

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>constraints</tt><br>
 <font size=-1>string</font></td>
 <td valign=top>(name of the rule constraint)</td></tr>
 </table>
 
 <p><b>Default Base</b><br>
 rule.rule


 * @author Liviu Panait and Sean luke
 * @version 1.0 
 */
public abstract class Rule implements Prototype, Comparable
    {
    public static final String P_RULE = "rule";
    public static final String P_CONSTRAINTS = "constraints";
    /**
       An index to a RuleConstraints
    */
    public byte constraints;

    /* Returns the Rule's constraints.  A good JIT compiler should inline this. */
    public final RuleConstraints constraints(final RuleInitializer initializer) 
        { 
        return initializer.ruleConstraints[constraints]; 
        }

    /** Rulerates a hash code for this rule -- the rule for this is that the hash code
        must be the same for two rules that are equal to each other genetically. */
    public abstract int hashCode();
    
    /** Unlike the standard form for Java, this function should return true if this
        rule is "genetically identical" to the other rule.  The default calls compareTo() */
    public boolean equals( final Object other )
        {
        return compareTo(other) == 0;
        }

    /**
       The reset method randomly reinitializes the rule.
    */
    public abstract void reset(final EvolutionState state, final int thread);

    /**
       Mutate the rule.  The default form just resets the rule.
    */
    public void mutate(final EvolutionState state, final int thread)
        {
        reset(state,thread);
        }

    /**
       Nice printing.  The default form simply calls printRuleToStringForHumans and prints the result,
       but you might want to override this.
    */
    public void printRuleForHumans( final EvolutionState state, final int log )
        { printRuleForHumans(state, log, Output.V_VERBOSE); }
                
    /**
       Nice printing.  The default form simply calls printRuleToStringForHumans and prints the result,
       but you might want to override this.
       @deprecated Verbosity no longer has an effect
    */
    public void printRuleForHumans( final EvolutionState state, final int log, final int verbosity )
        { state.output.println(printRuleToStringForHumans(),log);}

    /** Nice printing to a string. The default form calls toString().  */
    public String printRuleToStringForHumans()
        { return toString(); }
        
    /** Prints the rule to a string in a fashion readable by readRuleFromString.
        The default form calls printRuleToString().
        @deprecated */
    public String printRuleToString(final EvolutionState state)
        { return printRuleToString(); }
        
    /** Prints the rule to a string in a fashion readable by readRuleFromString.
        The default form simply calls toString() -- you should just override toString() 
        if you don't need the EvolutionState. */
    public String printRuleToString()
        { return toString(); }
        

    /** Reads a rule from a string, which may contain a final '\n'.
        Override this method.  The default form generates an error. */
    public void readRuleFromString(final String string, final EvolutionState state)
        { state.output.error("readRuleFromString(string,state) unimplemented in " + this.getClass()); }

    /**
       Prints the rule in a way that can be read by readRule().  The default form simply
       calls printRuleToString(state).   Override this rule to do custom writing to the log,
       or just override printRuleToString(...), which is probably easier to do.
    */
    public void printRule( final EvolutionState state, final int log )
        { printRule(state, log, Output.V_VERBOSE); }

    /**
       Prints the rule in a way that can be read by readRule().  The default form simply
       calls printRuleToString(state).   Override this rule to do custom writing to the log,
       or just override printRuleToString(...), which is probably easier to do.
       @deprecated Verbosity no longer has an effect
    */
    public void printRule( final EvolutionState state, final int log, final int verbosity )
        { state.output.println(printRuleToString(state),log); }

    /**
       Prints the rule in a way that can be read by readRule().  The default form simply
       calls printRuleToString(state).   Override this rule to do custom writing,
       or just override printRuleToString(...), which is probably easier to do.
    */
    public void printRule( final EvolutionState state, final PrintWriter writer )
        { writer.println(printRuleToString(state)); }

    /**
       Reads a rule printed by printRule(...).  The default form simply reads a line into
       a string, and then calls readRuleFromString() on that line.  Override this rule to do
       custom reading, or just override readRuleFromString(...), which is probably easier to do.
    */
    public void readRule(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        { readRuleFromString(reader.readLine(),state); }


    /** Override this if you need to write rules out to a binary stream */
    public void writeRule(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        state.output.fatal("writeRule(EvolutionState, DataOutput) not implemented in " + this.getClass());
        }

    /** Override this if you need to read rules in from a binary stream */
    public void readRule(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        state.output.fatal("readRule(EvolutionState, DataInput) not implemented in " + this.getClass());
        }


    public Parameter defaultBase()
        {
        return RuleDefaults.base().push(P_RULE);
        }

    public Object clone()
        {
        try { return super.clone(); }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }


    public void setup(EvolutionState state, Parameter base)
        {
        String constraintname = state.parameters.getString(
            base.push( P_CONSTRAINTS ),defaultBase().push(P_CONSTRAINTS));
        if (constraintname == null)
            state.output.fatal("No RuleConstraints name given",
                base.push( P_CONSTRAINTS ),defaultBase().push(P_CONSTRAINTS));

        constraints = RuleConstraints.constraintsFor(constraintname,state).constraintNumber;
        state.output.exitIfErrors();
        }
    
    /** This function replaces the old gt and lt functions that Rule used to require
        as it implemented the SortComparator interface.  If you had implemented those
        old functions, you can simply implement this function as:
        
        <tt><pre>
        public abstract int compareTo(Object o)
        {
        if (gt(this,o)) return 1;
        if (lt(this,o)) return -1;
        return 0;
        }
        </pre></tt>
    */
    public abstract int compareTo(Object o);
    }
