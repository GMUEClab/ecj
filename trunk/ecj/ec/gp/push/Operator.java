/** 
    ECJ implements Push's s-expressions as trees of nonterminals
    and terminals.  The nonterminals are all dummies -- this is the
    class in question.  Notably the nonterminals also have an arbitrary
    arity, requiring a custom tree builder (see PushBuilder).  The terminals
    are instances of Operator.java.
    
    <p>The nonterminals and terminals aren't actually evaluated.  Instead, the
    tree is printed out as a lisp s-expression and sent to the Push interpreter.
    
    <p>Operators are implemented as ERCs which hold the actual Push instruction
    or atom as a string ('value').  There are four kinds of operators at present:
    
    <ol>
    <li> Built-in Push instructions like float.* or integer.swap
    <li> Floating-point ERCs (defined by "float.erc")
    <li> Integer ERCs (defined by "integer.erc")
    <li> Custom Push instructions
    </ol>
    
    You specify your instructions like this:
    
    push.op.size = 7
    push.op.0 = float.erc
    push.op.1 = float.+
    
    # This is a custom instruction
    push.op.2 = float.print
    push.op.2.func = ec.gp.push.example.MyPushInstruction
    
    push.op.3 = float.%
    push.op.4 = float.-
    push.op.5 = float.dup
    push.op.6 = float.swap
    
    
    <p>For the (at present) two kinds of ERCs, you can specify a minimum
    and a maximum value.  Here are the defaults:
    
    push.op.float.min = -10
    push.op.float.max = 10
    push.op.int.min = -10
    push.op.int.max = 10
*/



package ec.gp.push;
import ec.gp.*;
import ec.*;

import ec.util.*;

public class Operator extends ERC
    {
    public static final String P_OP = "op";
    public static final String P_NUM_OPS = "size";
    public static final String P_FUNC = "func";
    public static final String P_FLOAT = "float";
    public static final String P_INTEGER = "int";
    public static final String P_MIN = "min";
    public static final String P_MAX = "max";
    
    public static final int FLOAT_ERC = 0;  // ultimately this needs to be a special kind of class
    public static final int INTEGER_ERC = 1;  // ultimately this needs to be a special kind of class
    public static final String[] ERC_NAMES = { "float.erc", "integer.erc" };
     
    public static double minFloatERC = -10.0;  // inclusive
    public static double maxFloatERC = 10.0;   // inclusive
    public static int minIntegerERC = -10;
    public static int maxIntegerERC = 10;
    
    /** Names of all the Push operators I can be set to.  This includes names for custom PushInstructions. */
    public String[] ops;

	/** A list of custom PushInstructions I can be set to. */
    public PushInstruction[] instructions;
    /** For each PushInstruction, a pointer into ops which gives the name of that instruction. 
    	Note that some operators in ops are built-in Push instructions and will have nothing
    	pointing to them. */
    public int[] indices;  // point to locations in ops
    
    /** The current name of the Push Operator I am set to. */
    String value;
    
    public String name() { return "OP"; }
    
    public int expectedChildren() { return 0; }
    
    public String toStringForHumans() { return value; }
    
    public Parameter defaultBase()
        {
        return PushDefaults.base().push(P_OP);
        }
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        
        Parameter def = defaultBase();
                
        // Load my operators
        int len = state.parameters.getInt(base.push(P_OP).push(P_NUM_OPS), def.push(P_NUM_OPS), 1);
        if (len < 1)
            state.output.fatal("Number of operators must be >= 1", base.push(P_OP).push(P_NUM_OPS), def.push(P_NUM_OPS));
        
        ops = new String[len];
        PushInstruction[] insts = new PushInstruction[len];
        
        for(int i =0; i < len; i++)
            {
            ops[i] = state.parameters.getString(base.push(P_OP).push("" + i), def.push("" + i));
            if (ops[i] == null)
                state.output.fatal("Operator number " + i + " is missing.", base.push(P_OP).push("" + i), def.push("" + i));

            // load Instruction if there is one
            Parameter bb = base.push(P_OP).push("" + i).push(P_FUNC);
            Parameter dd = def.push("" + i).push(P_FUNC);
            if (state.parameters.exists(bb, dd))  // got one
                {
                String s = state.parameters.getString(bb, dd);
                state.output.message("Adding Instruction " + ops[i] + " --> " + s);
                PushInstruction inst = (PushInstruction)(state.parameters.getInstanceForParameter(bb, dd, PushInstruction.class));
                if (inst == null)  // uh oh
                    state.output.fatal("Operator number " + i + ", named " + ops[i] + ", has an invalid function class: " + s);
                // load that sucker
                insts[i] = inst;
                }
            }
            
        // compress instruction list
        int count = 0;
        for(int i =0 ;i < len; i++)
            if (insts[i] != null)
                count++;
        instructions = new PushInstruction[count];
        indices = new int[count];
        
        count = 0;
        for(int i=0; i<len;i++)
            if (insts[i] != null)
                {
                instructions[count] = insts[i];
                indices[count] = i;
                count++;
                }
                
        final double NaN = 0.0 / 0.0;
                
        // load float ERC bounds
        Parameter b = base.push(P_FLOAT).push(P_MIN);
        Parameter d = def.push(P_FLOAT).push(P_MIN);
        
        if (!state.parameters.exists(b, d))
            state.output.warning("No " + ERC_NAMES[FLOAT_ERC] + " min value provided, using " + minFloatERC, b, d);
        else
            {
            double min = state.parameters.getDoubleWithDefault(b,d, NaN);
            if (min != min)  // it's NaN
                state.output.fatal("Malformed " + ERC_NAMES[FLOAT_ERC] + " min value", b, d);
            else minFloatERC = min;
            }

        b = base.push(P_FLOAT).push(P_MAX);
        d = def.push(P_FLOAT).push(P_MAX);

        if (!state.parameters.exists(b, d))
            state.output.warning("No " + ERC_NAMES[FLOAT_ERC] + " max value provided, using " + maxFloatERC, b, d);
        else
            {
            double max = state.parameters.getDoubleWithDefault(b, d, NaN);
            if (max != max)  // it's NaN
                state.output.fatal("Malformed " + ERC_NAMES[FLOAT_ERC] + " max value", b, d);
            else maxFloatERC = max;
            }
        if (minFloatERC > maxFloatERC)  // uh oh
            state.output.fatal("" + ERC_NAMES[FLOAT_ERC] + " min value is greater than max value.\nMin: " + minFloatERC +"\nMax: " + maxFloatERC);



        b = base.push(P_INTEGER).push(P_MIN);
        d = def.push(P_INTEGER).push(P_MIN);

        // load integer ERC bounds
        if (!state.parameters.exists(b, d))
            state.output.warning("No " + ERC_NAMES[INTEGER_ERC] + " min value provided, using " + minIntegerERC, b, d);
        else
            {
            double min = state.parameters.getDoubleWithDefault(b, d, NaN);
            if ((min != min) || (min != (int)min))  // it's NaN or invalid
                state.output.fatal("Malformed " + ERC_NAMES[INTEGER_ERC] + " min value", base.push(P_INTEGER).push(P_MIN), def.push(P_INTEGER).push(P_MIN));
            minIntegerERC = (int)min;
            }

        b = base.push(P_INTEGER).push(P_MAX);
        d = def.push(P_INTEGER).push(P_MAX);

        if (!state.parameters.exists(b, d))
            state.output.warning("No " + ERC_NAMES[INTEGER_ERC] + " max value provided, using " + maxIntegerERC, b, d);
        else
            {
            double max = state.parameters.getDoubleWithDefault(b, d, NaN);
            if ((max != max) || (max != (int)max))  // it's NaN or invalid
                state.output.fatal("Malformed " + ERC_NAMES[INTEGER_ERC] + " max value", b, d);
            else maxIntegerERC = (int)max;
            }
        if (minIntegerERC > maxIntegerERC)  // uh oh
            state.output.fatal("" + ERC_NAMES[INTEGER_ERC] + " min value is greater than max value.\nMin: " + minIntegerERC +"\nMax: " + maxIntegerERC);

        }

    public boolean nodeEquals(GPNode other)
        {
        if (other == null) return false;
        if (!(other instanceof Operator)) return false;
        Operator o = (Operator) other;
        return (o.value == value); 
        }
        
    public String encode()
    	{
    	return Code.encode(value);
    	}
    
    public boolean decode(final DecodeReturn dret)
    	{
    	Code.decode(dret);
    	if (dret.type == DecodeReturn.T_STRING)
    		{
    		value = dret.s;
    		// verify
    		for(int i = 0; i < ops.length; i++)
    			if (ops[i].equals(value))
    				return true;
    		}
    	// otherwise, uh oh
    	return false;
    	}
    
    public void resetNode(EvolutionState state, int thread)
        {
        int i = state.random[thread].nextInt(ops.length);
        if (ops[i].endsWith("erc")) // it's an erc
            {
            // we'll assume we don't have a lot of ercs
            for(int j = 0 ; j < ERC_NAMES.length; j++)
                {
                if (ops[i].equals(ERC_NAMES[j]))
                    {
                    switch(j)
                        {
                        case FLOAT_ERC:
                            value = "" + (state.random[thread].nextDouble(true, true) * (maxFloatERC - minFloatERC) + minFloatERC);
                            break;
                        case INTEGER_ERC:
                            value = "" + (state.random[thread].nextInt(maxIntegerERC - minIntegerERC + 1) + minIntegerERC);
                            break;
                        default:
                            state.output.fatal("The following PUSH ERC is unknown: " + ops[i]);
                            break;
                        }
                    break;  // break from for-loop
                    }
                }
            }
        else // it's an operator
            {
            value = ops[i];
            }
        }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // do nothing
        }
    }



