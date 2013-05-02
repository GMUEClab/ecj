package ec.gp.push;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import ec.coevolve.*;
import ec.util.*;
import org.spiderland.Psh.*;


/* 
 * PushProblem.java
 * 
 * Created: Fri Feb 15 23:00:04 EST 2013
 * By: Sean Luke
 */

/**
   A PushProblem contains useful methods to help you create an
   interpreter, write out the ECJ GP tree to a string, build a Push Program
   around this string, load the interpreter with all your custom instructions, 
   and run the Push Program on the interpreter.  
     
   <p>Commonly you'd also set up the interpreter's data stacks with some initial
   data, then after running the program you might inspect the stacks to determine
   the return value. PushProblem also contains some helpful methods to make it easy
   for you to set up and modify these stacks.
*/


public abstract class PushProblem extends GPProblem
    {
    StringBuilder buffer;
    
    public Object clone()
        {
        PushProblem other = (PushProblem)(super.clone());
        other.buffer = null;  // do not share
        return other;
        }
        
    /** Produces a Push Program from the provided GP Individual's tree. */
    public Program getProgram(EvolutionState state, GPIndividual ind)
        {
        if (buffer == null) buffer = new StringBuilder();
        else buffer.delete(0, buffer.length());  // StringBuilder stupidly doesn't have a clear() method
        try
            {
            String prog = ind.trees[0].child.makeLispTree(buffer).toString();
            if (!prog.startsWith("("))
                prog = "(" + prog + ")";
            return new Program(prog);
            }
        catch (Exception e)
            {
            // do nothing for the moment
            state.output.fatal("Push exception encountered while parsing program from GP Tree:\n" +
                ind.trees[0].child.makeLispTree(buffer) + "\n" + e);
            }
        return null;  // unreachable
        }
        
    /** Builds a Push Interpreter suitable for interpreting the Program given in getProgram(). */
    public Interpreter getInterpreter(EvolutionState state, GPIndividual ind, int threadnum)
        {
        // create an Interpreter
        Interpreter interpreter = new Interpreter(state.random[threadnum]);

        // Find the function set
        GPFunctionSet set = ind.trees[0].constraints((GPInitializer)(state.initializer)).functionset;
        GPNode[] terminals = set.terminals[0];  // only one type we assume
        
        // dump the additional instructions into the interpreter
        for(int i = 0; i < terminals.length; i++)
            if (terminals[i] instanceof Terminal)  // maybe has some instructions?
                {
                // This code is here rather than (more appropriately) in Terminal so that we can
                // free up Terminal from being reliant on the underlying library.
                Terminal op = (Terminal)(terminals[i]);
                PushInstruction[] customInstructions =  op.customInstructions;
                int[] indices = op.indices;
                String[] instructions = op.instructions;
                for(int j = 0; j < customInstructions.length; j++)
                    {
                    System.err.println(instructions[indices[j]]);
                    interpreter.AddInstruction(instructions[indices[j]], (PushInstruction)(customInstructions[j].clone()));   // or should this be DefineInstruction?
                    }
                }

        // all done
        return interpreter;
        }
    
    /** Executes the given program for up to maxSteps steps. */
    public void executeProgram(Program program, Interpreter interpreter, int maxSteps)
        {
        interpreter.Execute(program, maxSteps);
        }
    
    /** Clears the Interpreter's stacks so it is ready to execute another program. */
    public void resetInterpreter(Interpreter interpreter)
        {
        interpreter.ClearStacks();
        }

    /** Pushes a value onto the top of the float stack of the interpreter. */
    public void pushOntoFloatStack(Interpreter interpreter, float val)
        {
        interpreter.floatStack().push(val);
        }

    /** Pushes a value onto the top of the int stack of the interpreter. */
    public void pushOntoIntStack(Interpreter interpreter, int val)
        {
        interpreter.intStack().push(val);
        }
    
    /** Tests to see if the interpreter's float stack is empty. */
    public boolean isFloatStackEmpty(Interpreter interpreter)
        {
        return interpreter.floatStack().size() == 0;
        }
        
    /** Tests to see if the interpreter's int stack is empty. */
    public boolean isIntStackEmpty(Interpreter interpreter)
        {
        return interpreter.intStack().size() == 0;
        }
        
    /** Returns the top of the interpreter's float stack. */
    public float topOfFloatStack(Interpreter interpreter)
        {
        return interpreter.floatStack().top();
        }

    /** Returns the top of the interpreter's int stack. */
    public int topOfIntStack(Interpreter interpreter)
        {
        return interpreter.intStack().top();
        }
    }
