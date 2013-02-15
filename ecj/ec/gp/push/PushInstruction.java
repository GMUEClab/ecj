/**
   This is how you make custom Push instructions.  Instruction requires
   that you implement the Execute(...) method which gives you access to the
   underlying push stacks etc.  You may with also to override setup() 
   to set up your instruction initially.
*/


package ec.gp.push;
import org.spiderland.Psh.*;
import ec.*;
import ec.util.*;

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
