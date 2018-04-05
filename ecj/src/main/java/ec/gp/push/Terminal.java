package ec.gp.push;
import ec.gp.*;
import ec.*;
import ec.util.*;

/* 
 * Terminal.java
 * 
 * Created: Fri Feb 15 23:00:04 EST 2013
 * By: Sean Luke
 */


/** 
    Terminal is the leaf node in Push trees and is used to represent Push
    instructions of all types.

    <p>ECJ implements Push's s-expressions as trees of nonterminals
    and terminals.  The nonterminals are all dummy instances of the Nonterminal class.
    Terminals are all instances of the Terminal class.
    
    <p>The nonterminals and terminals aren't actually evaluated.  Instead, the
    tree is printed out as a lisp s-expression and sent to the Push interpreter.
    
    <p>Terminals are implemented as ERCs which hold the actual Push instruction
    or atom as a string ('value').  There are four kinds of instructions at present:
    
    <ol>
    <li> Built-in Push instructions like float.* or integer.swap
    <li> Floating-point ERCs (defined by "float.erc")
    <li> Integer ERCs (defined by "integer.erc")
    <li> Custom Push instructions
    </ol>
    
    <p>You specify your instructions like this:
    
    <tt><pre>
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
    </pre></tt>
    
    <p>For the (at present) two kinds of ERCs, you can specify a minimum
    and a maximum value.  Here are the defaults:
    
    <tt><pre>
    push.op.float.min = -10
    push.op.float.max = 10
    push.op.int.min = -10
    push.op.int.max = 10
    </tt></pre>
    
    The full list of Psh instructions is:
    
    <p><tt>
    integer.+<br>
    integer.-<br>
    integer./<br>
    integer.\%<br>
    integer.*<br>
    integer.pow<br>
    integer.log<br>
    integer.=<br>
    integer.><br>
    integer.*lt;<br>
    integer.min<br>
    integer.max<br>
    integer.abs<br>
    integer.neg<br>
    integer.ln<br>
    integer.fromfloat<br>
    integer.fromboolean<br>
    integer.rand<br>
    float.+<br>
    float.-<br>
    float./<br>
    float.\%<br>
    float.*<br>
    float.pow<br>
    float.log<br>
    float.=<br>
    float.><br>
    float.&lt;<br>
    float.min<br>
    float.max<br>
    float.sin<br>
    float.cos<br>
    float.tan<br>
    float.exp<br>
    float.abs<br>
    float.neg<br>
    float.ln<br>
    float.frominteger<br>
    float.fromboolean<br>
    float.rand<br>
    boolean.=<br>
    boolean.not<br>
    boolean.and<br>
    boolean.or<br>
    boolean.xor<br>
    boolean.frominteger<br>
    boolean.fromfloat<br>
    boolean.rand<br>
    true<br>
    false<br>
    code.quote<br>
    code.fromboolean<br>
    code.frominteger<br>
    code.fromfloat<br>
    code.noop<br>
    code.do*times<br>
    code.do*count<br>
    code.do*range<br>
    code.=<br>
    code.if<br>
    code.rand<br>
    exec.k<br>
    exec.s<br>
    exec.y<br>
    exec.noop<br>
    exec.do*times<br>
    exec.do*count<br>
    exec.do*range<br>
    exec.=<br>
    exec.if<br>
    exec.rand<br>
    input.index<br>
    input.inall<br>
    input.inallrev<br>
    input.stackdepth<br>
    frame.push<br>
    frame.pop<br>
    </tt>
    
    
    <p><b>Parameters</b><br>
    <table>
    <tr><td valign=top><i>base</i>.<tt>op.size</tt><br>
    <font size=-1>int >= 1</font></td>
    <td valign=top>(Number of instructions in Push's internal "instruction set")</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op</tt>.<i>i</i><br>
    <font size=-1>String</font></td>
    <td valign=top>(Name of instruction <i>i</i>)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op</tt>.<i>i</i>.<tt>func</tt><br>
    <font size=-1>classname, inherits and != ec.gp.push.PushInstruction</font></td>
    <td valign=top>(PushInstruction corresponding to instruction <i>i</i>, if it is a custom instruction)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op.float.min</tt><br>
    <font size=-1>float</font></td>
    <td valign=top>(Minimum value for a Push floating-point ERC)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op.float.max</tt><br>
    <font size=-1>float</font></td>
    <td valign=top>(Maximum value for a Push floating-point ERC)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op.int.min</tt><br>
    <font size=-1>int</font></td>
    <td valign=top>(Minimum value for a Push integer ERC)</td></tr>
    <tr><td valign=top><i>base</i>.<tt>op.int.max</tt><br>
    <font size=-1>int</font></td>
    <td valign=top>(Maximum value for a Push integer ERC)</td></tr>
    </table>

    <p><b>Default Base</b><br>
    gp.push
*/




public class Terminal extends ERC
    {
    public static final String P_INSTRUCTION = "in";
    public static final String P_NUM_INSTRUCTIONS = "size";
    public static final String P_FUNC = "func";
    public static final String P_FLOAT = "erc.float";
    public static final String P_INTEGER = "erc.int";
    public static final String P_MIN = "min";
    public static final String P_MAX = "max";
    
    public static final int FLOAT_ERC = 0;  // ultimately this needs to be a special kind of class
    public static final int INTEGER_ERC = 1;  // ultimately this needs to be a special kind of class
    public static final String[] ERC_NAMES = { "float.erc", "integer.erc" };
     
    public static double minFloatERC = -10.0;  // inclusive
    public static double maxFloatERC = 10.0;   // inclusive
    public static int minIntegerERC = -10;
    public static int maxIntegerERC = 10;
    
    /** Names of all the Push instructions I can be set to.  This includes names for custom PushInstructions. */
    public String[] instructions;

    /** A list of custom PushInstructions I can be set to. */
    public PushInstruction[] customInstructions;
    /** For each PushInstruction, a pointer into instructions which gives the name of that instruction. 
        Note that some instructions in instructions are built-in Push instructions and will have nothing
        pointing to them. */
    public int[] indices;  // point to locations in instructions
    
    /** The current name of the Push Terminal I am set to. */
    String value;
    
    public String name() { return "IN"; }
    
    public int expectedChildren() { return 0; }
    
    public String toStringForHumans() { return value; }
    
    public Parameter defaultBase()
        {
        return PushDefaults.base().push(P_INSTRUCTION);
        }
        
    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state, base);
        
        Parameter def = defaultBase();
                
        // Load my standard instructions
        int len = state.parameters.getInt(base.push(P_INSTRUCTION).push(P_NUM_INSTRUCTIONS), def.push(P_NUM_INSTRUCTIONS), 1);
        if (len < 1)
            state.output.fatal("Number of instructions must be >= 1", base.push(P_INSTRUCTION).push(P_NUM_INSTRUCTIONS), def.push(P_NUM_INSTRUCTIONS));
        
        instructions = new String[len];
        PushInstruction[] insts = new PushInstruction[len];
        
        for(int i =0; i < len; i++)
            {
            instructions[i] = state.parameters.getString(base.push(P_INSTRUCTION).push("" + i), def.push("" + i));
            if (instructions[i] == null)
                state.output.fatal("Terminal number " + i + " is missing.", base.push(P_INSTRUCTION).push("" + i), def.push("" + i));

            // load Instruction if there is one
            Parameter bb = base.push(P_INSTRUCTION).push("" + i).push(P_FUNC);
            Parameter dd = def.push("" + i).push(P_FUNC);
            if (state.parameters.exists(bb, dd))  // got one
                {
                String s = state.parameters.getString(bb, dd);
                state.output.message("Adding Instruction " + instructions[i] + " --> " + s);
                PushInstruction inst = (PushInstruction)(state.parameters.getInstanceForParameter(bb, dd, PushInstruction.class));
                if (inst == null)  // uh oh
                    state.output.fatal("Terminal number " + i + ", named " + instructions[i] + ", has an invalid function class: " + s);
                // load that sucker
                insts[i] = inst;
                }
            }
            
        // compress instruction list
        int count = 0;
        for(int i =0 ;i < len; i++)
            if (insts[i] != null)
                count++;
        customInstructions = new PushInstruction[count];
        indices = new int[count];
        
        count = 0;
        for(int i=0; i<len;i++)
            if (insts[i] != null)
                {
                customInstructions[count] = insts[i];
                indices[count] = i;
                count++;
                }
                
        final double NaN = 0.0 / 0.0;
                
        // load float ERC bounds
        Parameter b = base.push(P_FLOAT).push(P_MIN);
        Parameter d = PushDefaults.base().push(P_FLOAT).push(P_MIN);
        
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
        d = PushDefaults.base().push(P_FLOAT).push(P_MAX);

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
        d = PushDefaults.base().push(P_INTEGER).push(P_MIN);

        // load integer ERC bounds
        if (!state.parameters.exists(b, d))
            state.output.warning("No " + ERC_NAMES[INTEGER_ERC] + " min value provided, using " + minIntegerERC, b, d);
        else
            {
            double min = state.parameters.getDoubleWithDefault(b, d, NaN);
            if ((min != min) || (min != (int)min))  // it's NaN or invalid
                state.output.fatal("Malformed " + ERC_NAMES[INTEGER_ERC] + " min value", b, d);
            minIntegerERC = (int)min;
            }

        b = base.push(P_INTEGER).push(P_MAX);
        d = PushDefaults.base().push(P_INTEGER).push(P_MAX);

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
        if (!(other instanceof Terminal)) return false;
        Terminal o = (Terminal) other;
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
            for(int i = 0; i < instructions.length; i++)
                if (instructions[i].equals(value))
                    return true;
            }
        // otherwise, uh oh
        return false;
        }
    
    public void resetNode(EvolutionState state, int thread)
        {
        int i = state.random[thread].nextInt(instructions.length);
        if (instructions[i].endsWith("erc")) // it's an erc
            {
            // we'll assume we don't have a lot of ercs
            for(int j = 0 ; j < ERC_NAMES.length; j++)
                {
                if (instructions[i].equals(ERC_NAMES[j]))
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
                            state.output.fatal("The following PUSH ERC is unknown: " + instructions[i]);
                            break;
                        }
                    break;  // break from for-loop
                    }
                }
            }
        else // it's an instruction
            {
            value = instructions[i];
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



