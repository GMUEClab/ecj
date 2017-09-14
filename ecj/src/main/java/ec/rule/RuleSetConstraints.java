/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;
import ec.*;
import ec.util.*;

/* 
 * RuleSetConstraints.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Liviu Panait and Sean Luke
 */

/**
 * RuleSetConstraints is an basic class for constraints applicable to rulesets.
 * There are two categories of parameters associated with this class.  First, there are parameters
 * which guide the initial number of rules to be created when a ruleset is initialized for
 * the first time, or totally reset.  Second, there are parameters which indicate how rulesets
 * are to be mutated under the "default" rule mutation operator.
 *
 * <p>First the initialization parameters.  You need to specify a distribution from which
 * the system will pick random integer values X.  When a ruleset is to be initialized, a
 * random value X is picked from this distribution, and the ruleset will be created with X initial rules.
 * You can specify the distribution in one of two ways.  First, you can specify a minimum and maximum
 * number of rules; the system will then pick an X uniformly from between the min and the max. 
 * Second, you can specify a full distribution of size probabilities for more control.  For example,
 * to specify that the system should make individuals with 0 rules 0.1 of the time, 1 rule 0.2 of the time,
 * and 2 rules 0.7 of the time, you set <i>reset-num-sizes</i> to 3 (for rule sizes up to but not including 3),
 * and then set  reset-size.0 to 0.1, reset-size.1 to 0.2, and reset-size.2 to 0.7.
 *
 * <p>Next the mutation parameters.  The default mutation procedure works as follows.  First, every rule
 * in the ruleset is mutated.  It is up to the rule to determine by how much or whether it will be mutated
 * (perhaps by flipping a coin) when its mutate function is called.  Second, the system repeatedly flips
 * a coin with "p-del" probability of being true, until it comes up false.  The number of times it came up
 * true is the number of rules to remove from the ruleset; rules to be removed are chosen at random.
 * Third, the system repeatedly flips
 * a coin with "p-add" probability of being true, until it comes up false.  The number of times it came up
 * true is the number of new randomly-generated rules to add to the ruleset; rules are added to the end of the array.
 * Fourth, with "p-rand-order" probability, the order of rules in the ruleset array is randomized; this last
 * item might or might not matter to you depending on whether or not your rule interpreter differs depending on rule order.  
 *
 * @author Liviu Panait and Sean Luke
 * @version 1.0 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of rule set constraints)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of rule set constraint <i>n</i>)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>reset-min-size</tt><br>
 <font size=-1>int >= 0 (default=0)</font></td>
 <td valign=top>(for rule set constraint <i>n</i>, the minimum number of rules that rulesets may contain upon initialization (resetting), see discussion above)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>reset-max-size</tt><br>
 <font size=-1>int >= <i>base.n</i>.<tt>reset-min-size</tt> (default=0)</font></td>
 <td valign=top>(for rule set constraint <i>n</i>, the maximum number of rules that rulesets may contain upon initialization (resetting), see discussion above)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>reset-num-sizes</tt><br>
 <font size=-1>int >= 0</font> (default=unset)</td>
 <td valign=top>(for rule set constraint <i>n</i>, the number of sizes in the size distribution for initializtion, see discussion above)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>reset-size</tt>.<i>i</i><br>
 <font size=-1>0.0 &lt; double &lt; 1.0</font></td>
 <td valign=top>(for rule set constraint <i>n</i>, the probability that <i>i</i> will be chosen as the number of rules upon initialization, see discussion above)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>p-add</tt><br>
 <font size=-1>0.0 &lt; double &lt; 1.0</font></td>
 <td valign=top>(the probability that a new rule will be added, see discussion)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>p-del</tt><br>
 <font size=-1>0.0 &lt; double &lt; 1.0</font></td>
 <td valign=top>(the probability that a rule will be deleted, see discussion)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>p-rand-order</tt><br>
 <font size=-1>0.0 &lt; double &lt; 1.0</font></td>
 <td valign=top>(the probability that the rules' order will be randomized, see discussion)</td></tr>
 </table>

*/
public class RuleSetConstraints implements Clique
    {
    private static final long serialVersionUID = 1;

    /** The size of a byte */
    //    public static final int SIZE_OF_BYTE = 256;
    public final static String P_NAME = "name";
    /** num rulesets */
    //    public final static String P_SIZE = "size";
    public final static String P_RULE = "rule";  // our prototype
    //    public static final int CHECK_BOUNDARY = 8;
    public static final String P_RESETMINSIZE = "reset-min-size";
    public static final String P_RESETMAXSIZE = "reset-max-size";
    public static final String P_NUMSIZES = "reset-num-sizes";
    public static final String P_RESETSIZE = "reset-size";

    public static final String P_MINSIZE = "min-size";
    public static final String P_MAXSIZE = "max-size";

    public int minSize;   // the minimum legal size
    public int maxSize;   // the maximum legal size

    public int resetMinSize;  // the minium possible size -- if unused, it's 0, but 0 is also a valid number, so check sizeDistribution==null
    public int resetMaxSize;  // the maximum possible size -- if unused, it's 0, but 0 is also a valid number, so check sizeDistribution==null
    public double[] sizeDistribution;

    // probability of adding a random rule to the rule set
    public static final String P_ADD_PROB = "p-add";
    public double p_add;

    // probability of removing a random rule from the rule set
    public static final String P_DEL_PROB = "p-del";
    public double p_del;

    // probability of randomizing the rule order in the rule set
    public static final String P_RAND_ORDER_PROB = "p-rand-order";
    public double p_randorder;

    /** Assuming that either resetMinSize and resetMaxSize, or sizeDistribution, is defined,
        picks a random size from resetMinSize...resetMaxSize inclusive, or randomly
        from sizeDistribution. */ 
    public int pickSize(final EvolutionState state, final int thread)
        {
        if (sizeDistribution!=null)
            // pick from distribution
            return RandomChoice.pickFromDistribution(
                sizeDistribution,
                state.random[thread].nextDouble());
        else
            // pick from resetMinSize...resetMaxSize
            return state.random[thread].nextInt(resetMaxSize-resetMinSize+1) + resetMinSize;
        }

    /**
       The prototype of the Rule that will be used in the RuleSet
       (the RuleSet contains only rules with the specified prototype).
    */
    public Rule rulePrototype;

    /**
       Returns a stochastic value picked to specify the number of rules
       to generate when calling reset() on this kind of Rule.  The default
       version picks from the min/max or distribution, but you can override
       this to do whatever kind of thing you like here.
    */
    public int numRulesForReset(final RuleSet ruleset, 
        final EvolutionState state, final int thread)
        {
        // the default just uses pickSize
        return pickSize(state,thread);
        }
        
    /** The byte value of the constraints -- we can only have 256 of them */
    public byte constraintNumber;

    /** The name of the RuleSetConstraints object */
    public String name;

    /** Converting the rule to a string ( the name ) */
    public String toString() { return name; }
    
    /** Sets up all the RuleSetConstraints, loading them from the parameter
        file.  This must be called before anything is called which refers
        to a type by name. */

    /** You must guarantee that after calling constraintsFor(...) one or
        several times, you call state.output.exitIfErrors() once. */

    public static RuleSetConstraints constraintsFor(final String constraintsName,
        final EvolutionState state)
        {
        RuleSetConstraints myConstraints = (RuleSetConstraints)(((RuleInitializer)state.initializer).ruleSetConstraintRepository.get(constraintsName));
        if (myConstraints==null)
            state.output.error("The rule constraints \"" + constraintsName + "\" could not be found.");
        return myConstraints;
        }


    public void setup(final EvolutionState state, final Parameter base)
        {
        // What's my name?
        name = state.parameters.getString(base.push(P_NAME),null);
        if (name==null)
            state.output.fatal("No name was given for this RuleSetConstraints.",
                base.push(P_NAME));

        // Register me
        RuleSetConstraints old_constraints = (RuleSetConstraints)(((RuleInitializer)state.initializer).ruleSetConstraintRepository.put(name,this));
        if (old_constraints != null)
            state.output.fatal("The rule constraints \"" + name + "\" has been defined multiple times.", base.push(P_NAME));
        
        // load my prototypical Rule
        rulePrototype = (Rule)(state.parameters.getInstanceForParameter(base.push(P_RULE),null,Rule.class));
        rulePrototype.setup(state,base.push(P_RULE));

        p_add = state.parameters.getDouble( base.push( P_ADD_PROB ), null, 0 );
        if( p_add < 0 || p_add > 1 )
            {
            state.output.fatal( "Parameter not found, or its value is outside of allowed range [0..1].",
                base.push( P_ADD_PROB ) );
            }
        p_del = state.parameters.getDouble( base.push( P_DEL_PROB ), null, 0 );
        if( p_del < 0 || p_del > 1 )
            {
            state.output.fatal( "Parameter not found, or its value is outside of allowed range [0..1].",
                base.push( P_DEL_PROB ) );
            }

        p_randorder = state.parameters.getDouble( base.push( P_RAND_ORDER_PROB ), null, 0 );
        if( p_randorder < 0 || p_randorder > 1 )
            {
            state.output.fatal( "Parameter not found, or its value is outside of allowed range [0..1].",
                base.push( P_RAND_ORDER_PROB ) );
            }

        // now, we are going to load EITHER min/max size OR a size distribution, or both
        // (the size distribution takes precedence)

        // reset min and max size

        if (state.parameters.exists(base.push(P_RESETMINSIZE), null) || 
            state.parameters.exists(base.push(P_RESETMAXSIZE), null))
            {
            if (!(state.parameters.exists(base.push(P_RESETMAXSIZE), null)))
                state.output.error("This RuleSetConstraints has a " + 
                    P_RESETMINSIZE + " but not a " + P_RESETMAXSIZE + ".");
           
            resetMinSize = state.parameters.getInt(
                base.push(P_RESETMINSIZE), null,0);
            if (resetMinSize==-1) 
                state.output.error("If min&max are defined, RuleSetConstraints must have a min size >= 0.",
                    base.push(P_RESETMINSIZE), null);
            
            resetMaxSize = state.parameters.getInt(
                base.push(P_RESETMAXSIZE), null,0);
            if (resetMaxSize==-1) 
                state.output.error("If min&max are defined, RuleSetConstraints must have a max size >= 0.",
                    base.push(P_RESETMAXSIZE), null);

            if (resetMinSize > resetMaxSize)
                state.output.error(
                    "If min&max are defined, RuleSetConstraints must have min size <= max size.",
                    base.push(P_RESETMINSIZE), null);
            state.output.exitIfErrors();
            }

        // load sizeDistribution

        if (state.parameters.exists(base.push(P_NUMSIZES),
                null))
            {
            int siz = state.parameters.getInt(
                base.push(P_NUMSIZES), null,1);
            if (siz==0)
                state.output.fatal("The number of sizes in the RuleSetConstraints's distribution must be >= 1. ");
            sizeDistribution = new double[siz];
            
            double sum = 0.0;
            for(int x=0;x<siz;x++)
                {
                sizeDistribution[x] = state.parameters.getDouble(
                    base.push(P_RESETSIZE).push(""+x),null, 0.0);
                if (sizeDistribution[x]<0.0)
                    {
                    state.output.warning(
                        "Distribution value #" + x + " negative or not defined, assumed to be 0.0",
                        base.push(P_RESETSIZE).push(""+x),null);
                    sizeDistribution[x] = 0.0;
                    }
                sum += sizeDistribution[x];
                }
            if (sum>1.0)
                state.output.warning(
                    "Distribution sums to greater than 1.0",
                    base.push(P_RESETSIZE),
                    null);
            if (sum==0.0)
                state.output.fatal(
                    "Distribution is all 0's",
                    base.push(P_RESETSIZE),
                    null);

            // normalize and prepare
            RandomChoice.organizeDistribution(sizeDistribution);
            }
        
        if (state.parameters.exists(base.push(P_MINSIZE), null))
            minSize = state.parameters.getInt( base.push( P_MINSIZE ), null, 0 );
        else minSize = 0;
        
        if (state.parameters.exists(base.push(P_MAXSIZE), null))
            maxSize = state.parameters.getInt( base.push( P_MAXSIZE ), null, 0 );
        else maxSize = Integer.MAX_VALUE;

        // sanity checks
        if (minSize > maxSize)
            {
            state.output.fatal("Cannot have min size greater than max size : (" + minSize + " > " + maxSize + ")",base.push(P_MINSIZE), null);
            }
        
        if (sizeDistribution != null)
            {
            if (minSize!=0)
                state.output.fatal("Using size distribution, but min size is not 0",
                    base.push(P_MINSIZE), null);
            if (sizeDistribution.length - 1 > maxSize)
                state.output.fatal("Using size distribution whose maximum size is higher than max size",
                    base.push(P_MAXSIZE), null);
            }
        else
            {
            if (resetMinSize < minSize)
                state.output.fatal("Cannot have min size greater than reset min size : (" + minSize + " > " + resetMinSize + ")",base.push(P_MINSIZE), null);
            if (resetMaxSize > maxSize)
                state.output.fatal("Cannot have max size less than reset max size : (" + maxSize + " > " + resetMaxSize + ")",base.push(P_MAXSIZE), null);                
            }

        }
    }

