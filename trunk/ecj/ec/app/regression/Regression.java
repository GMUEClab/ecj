/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.regression;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Regression.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Regression implements the Koza (quartic) Symbolic Regression problem.
 *
 * <p>The equation to be regressed is y = x^4 + x^3 + x^2 + x, {x in [-1,1]}
 * <p>This equation was introduced in J. R. Koza, GP II, 1994.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.regression.RegressionData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Regression problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>(the size of the training set)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Regression extends GPProblem implements SimpleProblemForm
    {
    public static final String P_SIZE = "size";

    public double currentValue;
    public int trainingSetSize;
    
    // these are read-only during evaluation-time, so
    // they can be just light-cloned and not deep cloned.
    // cool, huh?
    
    public double inputs[];
    public double outputs[];

    // we'll need to deep clone this one though.
    public RegressionData input;

    public double func(double x)
        { return x*x*x*x + x*x*x + x*x + x; }

    public Object clone()
        {
        // don't bother copying the inputs and outputs; they're read-only :-)
        // don't bother copying the currentValue; it's transitory
        // but we need to copy our regression data
        Regression myobj = (Regression) (super.clone());

        myobj.input = (RegressionData)(input.clone());
        return myobj;
        }

    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        trainingSetSize = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (trainingSetSize<1) state.output.fatal("Training Set Size must be an integer greater than 0"); 

        // Compute our inputs so they can be copied with clone later
        
        inputs = new double[trainingSetSize];
        outputs = new double[trainingSetSize];
        
        for(int x=0;x<trainingSetSize;x++)
            {
            inputs[x] = state.random[0].nextDouble() * 2.0 - 1.0;
            outputs[x] = func(inputs[x]);
            state.output.println("{" + inputs[x] + "," + outputs[x] + "},",3000,0);
            }

        // set up our input -- don't want to use the default base, it's unsafe
        input = (RegressionData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA), null, RegressionData.class);
        input.setup(state,base.push(P_DATA));
        }


    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int hits = 0;
            double sum = 0.0;
            double result;
            for (int y=0;y<trainingSetSize;y++)
                {
                currentValue = inputs[y];
                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                // It's possible to get NaN because cos(infinity) and
                // sin(infinity) are undefined (hence cos(exp(3000)) zings ya!)
                // So since NaN is NOT =,<,>,etc. any other number, including
                // NaN, we're CAREFULLY wording our cutoff to include NaN.
                // Interesting that this has never been reported before to
                // my knowledge.

                final double HIT_LEVEL = 0.01;
                final double PROBABLY_ZERO = 1.11E-15;
                final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses

                result = Math.abs(outputs[y] - input.x);

                if (! (result < BIG_NUMBER ) )   // *NOT* (input.x >= BIG_NUMBER)
                    result = BIG_NUMBER;

                // very slight math errors can creep in when evaluating
                // two equivalent by differently-ordered functions, like
                // x * (x*x*x + x*x)  vs. x*x*x*x + x*x

                else if (result<PROBABLY_ZERO)  // slightly off
                    result = 0.0;
                    
                if (result <= HIT_LEVEL) hits++;  // whatever!

                sum += result;              }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)sum);
            f.hits = hits;
            ind.evaluated = true;
            }
        }
    }
