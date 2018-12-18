/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.rule;
import ec.*;
import ec.util.*;

/* 
 * RuleConstraints.java
 * 
 * Created: Tue Feb 20 13:16:00 2001
 * By: Liviu Panait and Sean Luke
 */

/**
 * RuleConstraints is a class for constraints applicable to rules.
 * You can subclass this to add additional constraints information
 * for different kinds of rules.
 *
 * @author Liviu Panait and Sean Luke
 * @version 1.0 

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of rule constraints)</td></tr>

 <tr><td valign=top><i>base.n</i>.<tt>name</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of rule constraint <i>n</i>)</td></tr>
 </table>

*/
public class RuleConstraints implements Clique
    {
    //    public static final int SIZE_OF_BYTE = 256;
    public final static String P_NAME = "name";
    //    public final static String P_SIZE = "size";

    /** The byte value of the constraints -- we can only have 256 of them */
    public byte constraintNumber;

    /** The name of the RuleConstraints object */
    public String name;

    /** Converting the rule to a string ( the name ) */
    public String toString() { return name; }


    public void setup(final EvolutionState state, final Parameter base)
        {
        // What's my name?
        name = state.parameters.getString(base.push(P_NAME),null);
        if (name==null)
            state.output.fatal("No name was given for this Rule Constraints.",
                base.push(P_NAME));

        // Register me
        RuleConstraints old_constraints = (RuleConstraints)(((RuleInitializer)state.initializer).ruleConstraintRepository.put(name,this));
        if (old_constraints != null)
            state.output.fatal("The rule constraints \"" + name + "\" has been defined multiple times.", base.push(P_NAME));
        }

    /** You must guarantee that after calling constraintsFor(...) one or
        several times, you call state.output.exitIfErrors() once. */

    public static RuleConstraints constraintsFor(final String constraintsName,
        final EvolutionState state)
        {
        RuleConstraints myConstraints = (RuleConstraints)(((RuleInitializer)state.initializer).ruleConstraintRepository.get(constraintsName));
        if (myConstraints==null)
            state.output.error("The rule constraints \"" + constraintsName + "\" could not be found.");
        return myConstraints;
        }
    }
