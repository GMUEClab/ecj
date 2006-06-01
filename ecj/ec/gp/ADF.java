/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * ADF.java
 * 
 * Created: Mon Oct 25 18:42:09 1999
 * By: Sean Luke
 */

/**
 * An ADF is a GPNode which implements an "Automatically Defined Function",
 * as described in Koza II.  
 *
 * <p>In this system, the ADF facility consists of several classes: ADF,
 * ADM, ADFStack, ADFContext, and ADFArgument. ADFs, and their cousins
 * ADMs ("Automatically Defined Macros [Lee Spector]"), appear as
 * typical function nodes in a GP tree.  However, they have a special
 * <i>associated tree</i> in the individual's tree forest which 
 * they evaluate as a kind of a "subfunction".
 *
 * <p>When an ADF is evaluated, it first evaluates all of its children
 * and stores away their results.  It then evaluates its associated tree.
 * In the associated tree may exist one or more <i>ADF Argument Terminals</i>,
 * defined by the ADFArgument class.  These terminal nodes are associated
 * with a single number which represents the "argument" in the original ADF
 * which evaluated their tree.  When an Argument Terminal is evaluated,
 * it returns the stored result for that child number in the parent ADF.
 * Ultimately, when the associated tree completes its evaluation, the ADF
 * returns that value.
 *
 * <p>ADMs work slightly differently.  When an ADM is evaluated, it
 * immediately evaluates its associated tree without first evaluating
 * any children.  When an Argument Terminal is evaluated, it evaluates
 * the subtree of the appropriate child number in the parent ADM and returns
 * that result.  These subtrees can be evaluated many times.  When the
 * associated tree completes its evaluation, the ADM returns that value.
 * 
 * <p>Obviously, if you have Argument Terminals in a tree, that tree must
 * be only callable by ADFs and ADMs, otherwise the Argument Terminals
 * won't have anything to return.  Furthermore, you must make sure that
 * you don't have an Argument Terminal in a tree whose number is higher
 * than the smallest arity (number of arguments) of a calling ADF or ADM.
 *
 * <p>The mechanism behind ADFs and ADMs is complex, requiring two specially-
 * stored stacks (contained in the ADFStack object) of ADFContexts.  For
 * information on how this mechanism works, see ADFStack.
 *
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tree</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(The "associated tree" of the ADF)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>name</tt><br>
 <font size=-1>String, can be undefined</font></td>
 <td valign=top>(A simple "name" of the ADF to distinguish it from other ADF functions in your function set.  Use only letters, numbers, hyphens, and underscores.  Lowercase is best.)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.adf

 * @see ec.gp.ADFStack
 * @author Sean Luke
 * @version 1.0 
 */

public class ADF extends GPNode
    {
    public static final String P_ADF = "adf";
    public static final String P_ASSOCIATEDTREE = "tree";
    public static final String P_FUNCTIONNAME = "name";

    /** The ADF's associated tree */
    public int associatedTree;

    /** The "function name" of the ADF, to distinguish it from other ADF
        functions you might provide.  */
    public String functionName;

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_ADF);
        }

    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(associatedTree);
        dataOutput.writeUTF(functionName);
        }
         
 
    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        associatedTree = dataInput.readInt();
        functionName = dataInput.readUTF();
        }

    /** Returns functionName.hashCode() + class.hashCode() + associatedTree.  Hope
        that's reasonably random. */

    public int nodeHashCode()
        {
        return (this.getClass().hashCode() + functionName.hashCode() + associatedTree);
        }

    /** Determines node equality by comparing the class, associated tree, and
        function name of the nodes. */
    public boolean nodeEquals(final GPNode node)
        {
        if (!this.getClass().equals(node.getClass()) ||
            children.length != node.children.length) return false;
        ADF adf = (ADF)node;
        return (associatedTree==adf.associatedTree && functionName.equals(adf.functionName));
        }

    /** Checks type-compatibility constraints between the ADF, its argument terminals, and the tree type of its associated tree, and also checks to make sure the tree exists, there aren't invalid argument terminals in it, and there are sufficient argument terminals (a warning).  Whew! */
    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        
        // does the associated tree exist?
        
        if (associatedTree < 0 || associatedTree > typicalIndividual.trees.length)
            state.output.error("The node " + toStringForError() + " of individual " + 
                               individualBase + " must have an associated tree that is >= 0 and < " + typicalIndividual.trees.length);
        else
            {
            
            // is the associated tree of the correct type?  Issue an error.
            GPInitializer initializer = ((GPInitializer)state.initializer);
            
            if (!constraints(initializer).returntype.compatibleWith(initializer,
                                                                    typicalIndividual.trees[associatedTree].constraints(initializer).treetype))
                state.output.error("The return type of the node " + toStringForError() 
                                   + " of individual " + 
                                   individualBase + "is not type-compatible with the tree type of its associated tree.");
            
            GPNode[][] funcs = 
                typicalIndividual.trees[associatedTree].
                constraints(initializer).functionset.nodes;
                        
            ADFArgument validArgument[] = new ADFArgument[children.length];

            for(int w=0;w<funcs.length;w++)
                {
                // does the tree's function set have argument terminals 
                // that are beyond what I can provide?  (issue an error)
                
                GPNode[] gpfi = funcs[w];
                for (int x=0;x<gpfi.length;x++)
                    if (gpfi[x] instanceof ADFArgument)
                        {
                        ADFArgument argument = (ADFArgument)(gpfi[x]);
                        int arg = argument.argument;
                        if (arg >= children.length)  // uh oh
                            state.output.error("The node " +
                                               toStringForError() + 
                                               " in individual "  + 
                                               individualBase + " would call its associated tree, which has an argument terminal with an argument number (" + arg + ") >= the ADF/ADM's arity (" + children.length +").  The argument terminal in question is " 
                                               + gpfi[x].toStringForError()); 
                        else
                            {
                            if (validArgument[arg]!=null && validArgument[arg]!=argument)  // got one already
                                state.output.warning("There exists more than one Argument terminal for argument #" 
                                                     + arg + " for the node " +
                                                     toStringForError() + 
                                                     " in individual " + 
                                                     individualBase);
                            else validArgument[arg] = argument;
                            
                            // is the argument terminal of the correct return type?  Issue an error.
                            if (!gpfi[x].constraints(initializer).returntype.compatibleWith(initializer,
                                                                                            constraints(initializer).childtypes[arg]))
                                state.output.error("The node " +
                                                   toStringForError() + 
                                                   " in individual " +
                                                   individualBase + " would call its associated tree, which has an argument terminal which is not type-compatible with the related argument position of the ADF/ADM.  The argument terminal in question is " 
                                                   + gpfi[x].toStringForError()); 
                            }
                        }
                }

            // does the tree's function set have fewer argument terminals
            // than I can provide? (issue a warning)
            
            for (int x=0;x<children.length;x++)
                if (validArgument[x] == null) 
                    state.output.warning("There is no argument terminal for argument #" 
                                         + x + " for the node " 
                                         + toStringForError() + " in individual " + 
                                         individualBase);
            
            }
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // we don't know our name yet, (used in toStringForError(),
        // which is used in GPNode's setup(...) method),
        // so WE load parameters before our parent does.

        Parameter def = defaultBase();

        functionName = state.parameters.getString(base.push(P_FUNCTIONNAME),def.push(P_FUNCTIONNAME));
        if (functionName == null)
            {
            state.output.warning("ADF/ADM node has no function name.  Using a blank name.",
                                 base.push(P_FUNCTIONNAME),def.push(P_FUNCTIONNAME));
            functionName = "";
            }

        associatedTree = 
            state.parameters.getInt(base.push(P_ASSOCIATEDTREE),def.push(P_FUNCTIONNAME),0);
        if (associatedTree < 0)
            state.output.fatal(
                "ADF/ADM node must have a positive-numbered associated tree.",
                base.push(P_ASSOCIATEDTREE),def.push(P_FUNCTIONNAME));
        // now we let our parent set up.  
        super.setup(state,base);
        }
    
    public String toString() { return "ADF" + functionName + "[" +associatedTree + "]"; }
    
    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        // get a context and prepare it
        ADFContext c = stack.get();
        c.prepareADF(this);

        // evaluate my arguments and load 'em in 
        for(int x=0;x<children.length;x++)
            {
            input.copyTo(c.arguments[x]);
            children[x].eval(state,thread,c.arguments[x],
                             stack,individual,problem);
            }

        // Now push the context onto the stack.
        stack.push(c);

        // evaluate the top of the associatedTree
        individual.trees[associatedTree].child.eval(
            state,thread,input,stack,individual,problem);

        // pop the context off, and we're done!
        if (stack.pop(1) != 1)
            state.output.fatal("Stack prematurely empty for " + toStringForError());
        }
            
    }
