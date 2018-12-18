package ec.gp.push;
import org.spiderland.Psh.*;
import ec.*;
import ec.util.*;

/* 
 * PushInstruction.java
 * 
 * Created: Fri Feb 15 23:00:04 EST 2013
 * By: Sean Luke
 */


/** 
    PushInstruction encapsulates a custom Push instruction.  This
    class requires that you implement a Psh method called <b><tt>Execute(...)</tt></b>.
    You will need to consult Psh to understand what you can do, and how to do it.  But
    for some examples, see the <b>Atan.java</b> and <b>Print.java</b> classes in
    <b>ec/app/push/</b>.
        
    <p>PushInstruction is a Prototype, so you may with also to override setup() 
    to set up your instruction initially.

    <p><b>Default Base</b><br>
    gp.push.func
*/



public abstract class PushInstruction extends Instruction implements Prototype
    {
    public static final String P_INSTRUCTION = "func";
    public Parameter defaultBase() 
        { 
        return PushDefaults.base().push(P_INSTRUCTION);
        }
        
    public void setup(EvolutionState state, Parameter base) { }
    
    public Object clone() 
        {
        try
            { 
            PushInstruction myobj = (PushInstruction) (super.clone());
    
            return myobj;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        } 
        
    public abstract void Execute(Interpreter interpreter);
    }
