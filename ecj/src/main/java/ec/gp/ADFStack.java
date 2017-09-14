/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;

/* 
 * ADFStack.java
 * 
 * Created: Mon Oct 25 19:48:12 1999
 * By: Sean Luke
 */

/**
 * ADFStack is a special data object used to hold ADF data.
 * This object is a weird beast and takes some explaining.
 * It consists of a main stack, a secondary "substack", and a "reserve" area 
 * (also implemented as a stack,
 * but it doesn't have to be).  The reserve is used to "recycle" objects
 * rather than having to create then new every time.
 *
 * <P>When an ADF is evaluated, it first
 * evaluates its children, then it calls push() on the ADFstack.
 * push() either creates a new ADFContext, or it fetches one from the
 * reserve if possible.  It then pushes the context on the main stack,
 * and also returns the context.  The ADF fills the context's arguments
 * with the results of its childrens' evaluation, and sets numargs to 
 * the number of
 * arguments, then evaluates the
 * ADF's associated function tree, 
 *
 * <p>When an ADM is evaluated, it calls push() on the ADFstack.
 * The ADM then fills the context's adm node with itself, and sets numargs
 * to the number of children it has.  Then it calls the ADM's associated
 * function tree.
 *
 * <p>In that tree, if an argument terminal of value <i>n</i> is evaluated,
 * the argument terminal calls evaluate(...) on the top context 
 * on the ADF stack and returns the result.
 * This method does different things depending on whether the top context
 * represented an ADF or an ADM.  If it was an ADF, the context simply sets
 * input to the value of argument <i>n</i> in the context's argument list,
 * and returns input.  If it was an ADM, the context pops itself off the
 * stack and pushes itself on the substack (to set up the right context
 * for evaluating an original child of the ADM), then evaluates child <i>n</i>
 * of the ADM, then pops itself off the substack and pushes itself back
 * on the stack to restore the context.  Input is set to the evaluated
 * results, and input is returned.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>context</tt><br>
 <font size=-1>classname, inherits and != ec.gp.GPContext</font></td>
 <td valign=top>(the stack's GPContext class)</td></tr> 
 </table>

 <p><b>Parameters</b><br>
 gp.adf-stack

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>context</tt><br>
 <td valign=top>(context_proto)</td></tr> 
 </table>
 
 * @author Sean Luke
 * @version 1.0 
 */

public class ADFStack implements Prototype 
    {
    private static final long serialVersionUID = 1;

    public static final String P_ADFSTACK = "adf-stack";
    public static final String P_ADF = "adf";
    public static final String P_CONTEXT = "context";
    public ADFContext context_proto;
    public static final int INITIAL_STACK_SIZE = 2;  // seems like a good size
    protected int onStack;
    protected int onSubstack;
    protected int inReserve;
    protected ADFContext[] stack;
    protected ADFContext[] substack;
    protected ADFContext[] reserve;

    public ADFStack() 
        { 
        stack = new ADFContext[INITIAL_STACK_SIZE];
        substack = new ADFContext[INITIAL_STACK_SIZE];
        reserve = new ADFContext[INITIAL_STACK_SIZE];
        onStack=0;
        onSubstack = 0;
        inReserve = 0;
        }

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_ADFSTACK);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        // load our prototype

        Parameter p = base.push(P_CONTEXT);
        Parameter d = defaultBase().push(P_CONTEXT);

        context_proto = (ADFContext)
            (state.parameters.getInstanceForParameterEq(p,d,ADFContext.class));
        context_proto.setup(state,p);
        }

    public Object clone()
        {
        try
            {
            ADFStack myobj = (ADFStack) (super.clone());

            // deep-cloned stuff
            myobj.context_proto = (ADFContext)(context_proto.clone());

            // clone the stack arrays -- dunno if this is faster than new ADFContext[...]
            myobj.stack = (ADFContext[])(stack.clone());
            myobj.substack = (ADFContext[])(substack.clone());
            myobj.reserve = (ADFContext[])(reserve.clone());

            // fill 'em up
            for(int x=0;x<onStack;x++)
                myobj.stack[x] = (ADFContext)(stack[x].clone());
            for(int x=0;x<onSubstack;x++)
                myobj.substack[x] = (ADFContext)(substack[x].clone());
            for(int x=0;x<inReserve;x++)
                myobj.reserve[x] = (ADFContext)(reserve[x].clone());
            return myobj;
            }
        catch (CloneNotSupportedException e) 
            { throw new InternalError(); } // never happens
        }
    
    /** Returns an ADFContext from the stack's reserve, or creates one
        fresh if there are none in reserve.  While you can throw this
        ADFContext away if you like, it'd be good if you actually didn't
        call this function unless you expected to push the 
        context onto the stack with push(ADFContext obj) -- karma!
    */
    public final ADFContext get()
        {
        // Remove one from reserve

        ADFContext obj;
        if (inReserve>0) obj = reserve[--inReserve];
        else obj = (ADFContext)(context_proto.clone());  // hopefully that doesn't have to happen too many times
        return obj;
        }


    /** Pushes an ADFContext onto the main stack.  The best way to get an
        ADFContext to push onto the stack is with get(). Returns obj. */

    public final ADFContext push(ADFContext obj)
        {
        // Double stack if necessary
        if (onStack==stack.length)
            {
            ADFContext[] newstack = new ADFContext[stack.length * 2];
            System.arraycopy(stack,0,newstack,0,stack.length);
            stack = newstack;
            }

        // Add to stack
        stack[onStack++] = obj;
        
        // return it
        return obj;
        }


    /** Pops off <i>n</i> items from the stack, if possible. Returns
        the number of items actually popped off. */ 
    public final int pop(int n)
        {
        int x;
        for(x = 0 ; x < n; x++)
            {
            // Anything left on the stack?
            if (onStack==0) break;

            // Remove one from stack
            ADFContext obj = stack[--onStack];
            
            // Double reserve if necessary
            if (inReserve==reserve.length)
                {
                ADFContext[] newreserve = new ADFContext[reserve.length * 2];
                System.arraycopy(reserve,0,newreserve,0,reserve.length);
                reserve = newreserve;
                }
            
            // Add to reserve
            reserve[inReserve++] = obj;         
            }
        return x;
        }
    



    /** Returns the <i>n</i>th item in the stack (0-indexed), or null if
        this goes to the bottom of the stack. */
    public final ADFContext top(int n)
        {
        // is this beyond the stack?
        if (onStack-n <= 0) return null;
        else return stack[onStack-n-1];
        }


    /** Moves <i>n</i> items onto the substack (pops them off the stack and pushes them onto the substack).  Returns the actual number of items for which this was done. */
    public final int moveOntoSubstack(int n)
        {
        int x;
        for(x=0;x<n;x++)
            {
            // is the stack empty?
            if (onStack==0) break;  // uh oh
            
            // Remove one from stack
            ADFContext obj = stack[--onStack];
            
            // Double substack if necessary
            if (onSubstack == substack.length)
                {
                ADFContext[] newsubstack = new ADFContext[substack.length * 2];
                System.arraycopy(substack,0,newsubstack,0,substack.length);
                substack = newsubstack;
                }
            
            // Add to substack
            substack[onSubstack++] = obj;
            }
        return x;
        }

    /** Moves <i>n</i> items onto the stack (popss them off the substack and pushes them onto the stack). Returns the actual number of items moved from the Substack onto the main stack */
    public final int moveFromSubstack(int n)
        {
        int x;
        for(x=0;x<n;x++)
            {
            // is the substack empty?
            if (onSubstack==0) break; // uh oh
            
            // Remove one from stack
            ADFContext obj = substack[--onSubstack];

            // Double stack if necessary
            if (onStack==stack.length)
                {
                ADFContext[] newstack = new ADFContext[stack.length * 2];
                System.arraycopy(stack,0,newstack,0,stack.length);
                stack = newstack;
                }
            
            // Add to stack
            stack[onStack++] = obj;
            }
        return x;
        }

    /** Pops off all items on the stack and the substack. */
    public final void reset()
        {
        if (onSubstack>0) moveFromSubstack(onSubstack);
        if (onStack>0) pop(onStack);
        }

    }
