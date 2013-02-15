/**
   All a Push Problem does is create an interpreter, write out the
   tree to a string and build a Push Program out of it, load the
   interpreter with all your custom instructions, and run the program
   on the interpreter.  This is all done in the execute() procedure
   which you call from your evaluate() procedure.  
   Then your job is to inspect the results of the interpreter's stack etc.
*/

package ec.gp.push;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import ec.coevolve.*;
import ec.util.*;
import org.spiderland.Psh.*;

public abstract class PushProblem extends GPProblem
    {
    /** Produces a Push Program from the provided GP Individual's tree. */
    public Program getProgram(EvolutionState state, GPIndividual ind)
        {
        try
            {
            return new Program("(" + ind.trees[0].child.makeLispTree() + ")");
            }
        catch (Exception e)
            {
            // do nothing for the moment
            state.output.fatal("Push exception encountered while parsing program from GP Tree:\n" +
                ind.trees[0].child.makeLispTree() + "\n" + e);
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
            if (terminals[i] instanceof Operator)  // maybe has some instructions?
            	{
            	// This code is here rather than (more appropriately) in Operator so that we can
            	// free up Operator from being reliant on the underlying library.
            	Operator op = (Operator)(terminals[i]);
            	PushInstruction[] instructions =  op.instructions;
            	int[] indices = op.indices;
            	String[] ops = op.ops;
        		for(int j = 0; j < instructions.length; j++)
            		interpreter.AddInstruction(ops[indices[j]], (PushInstruction)(instructions[j].clone()));   // or should this be DefineInstruction?
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
