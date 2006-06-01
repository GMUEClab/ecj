/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;

/* 
 * ADFContext.java
 * 
 * Created: Tue Oct 26 13:36:46 1999
 * By: Sean Luke
 */

/**
 * ADFContext is the object pushed onto an ADF stack which represents
 * the current context of an ADM or ADF function call, that is, how to
 * get the argument values that argument_terminals need to return.
 *
 * <p><i>adf</i> contains the relevant ADF/ADM node. 
 * If it's an ADF
 * function call, then <i>arguments[]</i> contains the evaluated arguments
 * to the ADF.  If it's an ADM function call,
 * then <i>arguments[]</i> is set to false.
 *
 * <p>You set up the ADFContext object for a given ADF or ADM node with
 * the prepareADF(...) and prepareADM(...) functions.
 *
 * <p>To evaluate an argument number from an ADFContext, call evaluate(...),
 * and the results are evaluated and copied into input.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.data</tt><br>
 <font size=-1>classname, inherits and != ec.GPData</font></td>
 <td valign=top>(the class for the ADFContext's basic GPData type -- typically this is the same as GPProblem's GPData type)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.adf-context

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i><tt>.data</tt><br>
 <td valign=top>(the ADFContext's basic GPData type)</td></tr> 
 </table>
 * @author Sean Luke
 * @version 1.0 
 */

public class ADFContext implements Prototype
    {
    public final static String P_DATA = "data";
    public final static String P_ADFCONTEXT = "adf-context";

    public final static int INITIAL_ARGUMENT_SIZE = 2;  // seems reasonable

    /** The ADF/ADM node proper */
    public ADF adf;

    /** A prototypical GPData node. */
    public GPData arg_proto;

    /** An array of GPData nodes (none of the null, when it's used) 
        holding an ADF's arguments' return results */
    public GPData[] arguments;

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_ADFCONTEXT);
        }

    public ADFContext() 
        {
        arguments = new GPData[INITIAL_ARGUMENT_SIZE];
        }

    public Object clone()
        {
        try 
            {
            ADFContext myobj = (ADFContext) (super.clone());

            // deep-clone the context proto
            myobj.arg_proto = (GPData)(arg_proto.clone());

            // deep-clone the contexts
            myobj.arguments = new GPData[arguments.length];
            for(int x=0;x<myobj.arguments.length;x++)
                myobj.arguments[x] = (GPData)(arguments[x].clone());

            return myobj;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); }
        }

 

    public void setup(final EvolutionState state, final Parameter base)
        {
        // load the prototype
        Parameter p = base.push(P_DATA);
        Parameter def = defaultBase(); 
        Parameter d = def.push(P_DATA);

        if (state.parameters.exists(p,d))
            {
            arg_proto = (GPData)
                (state.parameters.getInstanceForParameter(
                    p,d,GPData.class));
            arg_proto.setup(state,p);
            }
        else // snarf it from eval.problem.data, hacked up from the Problem's data type,
            // 'cause I'm not sure if Problem's been set up yet
            {
            Parameter pp = new Parameter(EvolutionState.P_EVALUATOR).
                push(Evaluator.P_PROBLEM).push(GPProblem.P_DATA);
            Parameter dd = GPDefaults.base().push(GPProblem.P_GPPROBLEM).
                push(GPProblem.P_DATA);

            state.output.warning("No ADF GPData specified; using (and setting up) from\n " 
                                 + pp + "\nor default base " + dd, p,d);
            arg_proto = (GPData)
                (state.parameters.getInstanceForParameter(pp,dd,GPData.class));
            arg_proto.setup(state,pp);  // note setting up from Problem's base!
            }

        // clone off the prototype
        for (int x=0;x<INITIAL_ARGUMENT_SIZE;x++)
            arguments[x] = (GPData)(arg_proto.clone());
        }


    /** Evaluates the argument number in the current context */
    public void evaluate(final EvolutionState state,
                         final int thread,
                         final GPData input,
                         final ADFStack stack,
                         final GPIndividual individual,
                         final Problem problem,
                         final int argument)
        {
        // do I have that many arguments?
        if (argument >= adf.children.length || argument < 0)  // uh oh 
            {
            individual.printIndividual(state,0,3000);
            state.output.fatal("Invalid argument number for " + adf.errorInfo());
            }

        // Am I an ADM or an ADF?
        if (adf==null)
            state.output.fatal("ADF is null for " + adf.errorInfo());
        else if (adf instanceof ADF)  // it's an ADF
            arguments[argument].copyTo(input);
        else // it's an ADM
            {
            // get rid of my context temporarily
            if (stack.moveOntoSubstack(1)!=1)
                state.output.fatal("Substack prematurely empty for "  + adf.errorInfo());

            // Call the GPNode
            adf.children[argument].eval(state,thread,input,stack,individual,problem);
            
            // restore my context
            if (stack.moveFromSubstack(1)!=1)
                state.output.fatal("Stack prematurely empty for " + adf.errorInfo());
            }
        }

    
    /** Increases arguments to accommodate space if necessary.
        Sets adf to a.
        You need to then fill out the arguments yourself. */
    public final void prepareADF(ADF a)
        {
        // set to the length requested or longer
        if (a.children.length > arguments.length)
            {
            GPData[] newarguments = new GPData[a.children.length];
            System.arraycopy(arguments,0,newarguments,0,arguments.length);
            // fill gap -- ugh, luckily this doesn't happen but a few times
            for(int x=arguments.length;x<newarguments.length;x++)
                newarguments[x] = (GPData)(arg_proto.clone());
            arguments = newarguments;
            }
        adf = a;
        }

    /** Sets adf to a */
    public final void prepareADM(ADM a)
        {
        adf = a;
        }


    }
