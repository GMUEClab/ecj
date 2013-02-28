/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;

import java.util.Enumeration;
import java.util.Hashtable;

import ec.simple.SimpleInitializer;
import ec.util.Parameter;
import ec.EvolutionState;

/* 
 * RuleInitializer.java
 * 
 * Created: Fri Sep 14 14:00:02 2001
 * By: Liviu Panait
 *
 */
 
/** A SimpleInitializer subclass designed to be used with rules.  Basically,
    the RuleInitializer sets up the RuleConstraints and RuleSetConstraints cliques
    at setup() time, and does nothing else different from SimpleInitializer. 
    The RuleInitializer also specifies the parameter bases for the RuleSetConstraints
    and RuleConstraints objects.  
 
    <p><b>Parameter bases</b><br>
    <table>
    <tr><td valign=top><tt>rule.rsc</tt></td>
    <td>RuleSetConstraints</td></tr>
    <tr><td valign=top><tt>rule.rc</tt></td>
    <td>RuleConstraints</td></tr>
    </table>
*/

public class RuleInitializer extends SimpleInitializer
    {
    private static final long serialVersionUID = 1;

    // used just here, so far as I know :-)
    public static final int SIZE_OF_BYTE = 256;
    public final static String P_RULESETCONSTRAINTS = "rsc";
    public final static String P_RULECONSTRAINTS = "rc";
    public final static String P_SIZE = "size";

    public Hashtable ruleConstraintRepository;
    public RuleConstraints[] ruleConstraints;
    public byte numRuleConstraints;
    
    public Hashtable ruleSetConstraintRepository;
    public RuleSetConstraints[] ruleSetConstraints;
    public byte numRuleSetConstraints;
    
    /** Sets up the RuleConstraints and RuleSetConstraints cliques. */
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        ruleConstraintRepository = new Hashtable();
        ruleConstraints = new RuleConstraints[SIZE_OF_BYTE];
        numRuleConstraints = 0;
        
        ruleSetConstraintRepository = new Hashtable();
        ruleSetConstraints = new RuleSetConstraints[SIZE_OF_BYTE];
        numRuleSetConstraints = 0;
        
        // Now let's load our constraints and function sets also.
        // This is done in a very specific order, don't change it or things
        // will break.
        setupConstraints(
            state, RuleDefaults.base().push( P_RULECONSTRAINTS ) );
        setupRuleSetConstraints(
            state, RuleDefaults.base().push( P_RULESETCONSTRAINTS ) );
        }

    /** Sets up all the RuleConstraints, loading them from the parameter
        file.  This must be called before anything is called which refers
        to a type by name. */
    
    public void setupConstraints(final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing Rule Constraints");
        
        // How many RuleConstraints do we have?
        int x = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of rule constraints must be at least 1.",base.push(P_SIZE));
        
        // Load our constraints
        for (int y=0;y<x;y++)
            {
            RuleConstraints c;
            // Figure the constraints class
            if (state.parameters.exists(base.push(""+y), null))
                c = (RuleConstraints)(state.parameters.getInstanceForParameterEq(
                        base.push(""+y),null,RuleConstraints.class));
            else
                {
                state.output.message("No Rule Constraints specified, assuming the default class: ec.rule.RuleConstraints for " + base.push(""+y));
                c = new RuleConstraints();
                }
            c.setup(state,base.push(""+y));
            }
        
        // set our constraints array up
        Enumeration e = ruleConstraintRepository.elements();
        while(e.hasMoreElements())
            {
            RuleConstraints c = (RuleConstraints)(e.nextElement());
            c.constraintNumber = numRuleConstraints;
            ruleConstraints[numRuleConstraints] = c;
            numRuleConstraints++;
            }
        }
    
    public void setupRuleSetConstraints(final EvolutionState state,
        final Parameter base)
        {
        state.output.message("Processing Ruleset Constraints");
        // How many RuleSetConstraints do we have?
        int x = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (x<=0) 
            state.output.fatal("The number of RuleSetConstraints must be at least 1.",base.push(P_SIZE));
        
        // Load our RuleSetConstraints
        for (int y=0;y<x;y++)
            {
            RuleSetConstraints c;
            // Figure the RuleSetConstraints class
            if (state.parameters.exists(base.push(""+y), null))
                c = (RuleSetConstraints)(state.parameters.getInstanceForParameterEq(
                        base.push(""+y),null,RuleSetConstraints.class));
            else
                {
                state.output.message("No RuleSetConstraints specified, assuming the default class: ec.gp.RuleSetConstraints for " + base.push(""+y));
                c = new RuleSetConstraints();
                }
            c.setup(state,base.push(""+y));
            }

        // set our constraints array up
        Enumeration e = ruleSetConstraintRepository.elements();
        while(e.hasMoreElements())
            {
            RuleSetConstraints c = (RuleSetConstraints)(e.nextElement());
            c.constraintNumber = numRuleSetConstraints;
            ruleSetConstraints[numRuleSetConstraints] = c;
            numRuleSetConstraints++;
            }            
        }
    }
