/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.push;
import ec.gp.push.*;
import org.spiderland.Psh.*;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import java.io.*;
import java.util.*;

public class Regression extends PushProblem implements SimpleProblemForm
    {
    private static final long serialVersionUID = 1;

    public static final String P_SIZE = "size";
    public static final String P_FILE = "file";
    public static final String P_USE_FUNCTION = "use-function";
    public static final String P_MAX_STEPS = "max-steps";

    public int trainingSetSize;
    public boolean useFunction;  // if we have a file, should we use the function to compute the output values?  Or are they also contained?
    
    // these are read-only during evaluation-time, so
    // they can be just light-cloned and not deep cloned.
    // cool, huh?
    
    public double inputs[];
    public double outputs[];

    public double func(double x)
        { return x*x*x*x + x*x*x + x*x + x; }

    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        trainingSetSize = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (trainingSetSize<1) state.output.fatal("Training Set Size must be an integer greater than 0", base.push(P_SIZE)); 

        // should we load our x parameters from a file, or generate them randomly?
        //file = state.parameters.getFile(base.push(P_FILE), null);
        InputStream inputfile = state.parameters.getResource(base.push(P_FILE), null);

        // *IF* we load from a file, should we generate the output through the function, or load the output as well?
        useFunction = state.parameters.getBoolean(base.push(P_USE_FUNCTION), null, true);

        // Compute our inputs so they can be copied with clone later
        inputs = new double[trainingSetSize];
        outputs = new double[trainingSetSize];
        
        //if (file != null)  // use the file
        if (inputfile != null)
            {
            try
                {
                Scanner scan = new Scanner(inputfile);
                for(int x = 0; x < trainingSetSize; x++)
                    {
                    if (scan.hasNextDouble())
                        inputs[x] = scan.nextDouble();
                    else state.output.fatal("Not enough data points in file: expected " + (trainingSetSize * (useFunction ? 1 : 2)));
                    if (!useFunction)
                        {
                        if (scan.hasNextDouble())
                            outputs[x] = scan.nextDouble();
                        else state.output.fatal("Not enough data points in file: expected " + (trainingSetSize * (useFunction ? 1 : 2)));
                        }
                    }
                }
            catch (NumberFormatException e)
                {
                state.output.fatal("Some tokens in the file were not numbers.");
                }
            }
        else for(int x=0;x<trainingSetSize;x++)
                 {
                 // On p. 242 of Koza-I, he claims that the points are chosen from the
                 // fully-closed interval [-1, 1].  This is likely not true as Koza's lisp
                 // code usually selected stuff from half-open intervals.  But just to be
                 // absurdly exact here, we're allowing 1 as a valid number.
                 inputs[x] = state.random[0].nextDouble(true, true) * 2.0 - 1.0;     // fully closed interval.
                 }
                        
        for(int x=0;x<trainingSetSize;x++)
            {
            if (useFunction)
                outputs[x] = func(inputs[x]);
            state.output.message("{" + inputs[x] + "," + outputs[x] + "},");
            }

        maxSteps = state.parameters.getInt(base.push(P_MAX_STEPS),null,0);
        if (maxSteps < 0)
            state.output.fatal("Maximum Steps not specified, must be 1 or greater, or 0 to indicate no maximum number of steps.");
        if (maxSteps == 0)
            state.output.warning("No maximum number of steps:. Push interpreter may get into an infinite loop.");
        }


    public int maxSteps;
    
    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int hits = 0;
            double sum = 0.0;
            double result;
        
            Interpreter interpreter = getInterpreter(state, (GPIndividual) ind, threadnum);
            Program program = getProgram(state, (GPIndividual) ind);        

            for (int y=0;y<trainingSetSize;y++)
                {
                if (y > 0) // need to reset first
                    resetInterpreter(interpreter);
                
                // load it up and run it
                pushOntoFloatStack(interpreter, (float)(inputs[y]));
                executeProgram(program, interpreter, maxSteps);

                // It's possible to get NaN because cos(infinity) and
                // sin(infinity) are undefined (hence cos(exp(3000)) zings ya!)
                // So since NaN is NOT =,<,>,etc. any other number, including
                // NaN, we're CAREFULLY wording our cutoff to include NaN.
                // Interesting that this has never been reported before to
                // my knowledge.

                final double HIT_LEVEL = 0.01;
                final double PROBABLY_ZERO = 1E-6;  // The Psh interpreter seems less accurate, not sure why
                final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses

                result = Math.abs(outputs[y] - topOfFloatStack(interpreter));  // will be 0 if float stack is empty

                if (! (result < BIG_NUMBER ) )   // *NOT* (input.x >= BIG_NUMBER)
                    result = BIG_NUMBER;

                if (isFloatStackEmpty(interpreter)) // uh oh, invalid value
                    result = BIG_NUMBER;

                // very slight math errors can creep in when evaluating
                // two equivalent by differently-ordered functions, like
                // x * (x*x*x + x*x)  vs. x*x*x*x + x*x

                else if (result<PROBABLY_ZERO)  // slightly off
                    result = 0.0;
                    
                if (result <= HIT_LEVEL) hits++;  // whatever!

                sum += result;              
                }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            ind.evaluated = true;
            }
        }
    }
