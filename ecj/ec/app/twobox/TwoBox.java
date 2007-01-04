/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.twobox;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * TwoBox.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * TwoBox implements the TwoBox problem, with or without ADFs,
 * as discussed in Koza-II.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.twobox.TwoBoxData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the TwoBox problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>(the size of the training set)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>range</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>(the range of dimensional values in the training set -- they'll be integers 1...range inclusive)</td></tr>
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

public class TwoBox extends GPProblem implements SimpleProblemForm
    {
    public static final String P_SIZE = "size";
    public static final String P_RANGE= "range";

    public int currentIndex;
    public int trainingSetSize;
    public int range;
    
    // these are read-only during evaluation-time, so
    // they can be just light-cloned and not deep cloned.
    // cool, huh?
    
    public double inputsl0[];
    public double inputsw0[];
    public double inputsh0[];
    public double inputsl1[];
    public double inputsw1[];
    public double inputsh1[];
    public double outputs[];

    // we'll need to deep clone this one though.
    public TwoBoxData input;

    public final double func(final double l0, final double w0, 
                             final double h0, final double l1, 
                             final double w1, final double h1)
        { return l0*w0*h0-l1*w1*h1; }

    public Object clone()
        {
        // don't bother copying the inputs and outputs; they're read-only :-)
        // don't bother copying the currentIndex; it's transitory
        // but we need to copy our twobox data
        TwoBox myobj = (TwoBox) (super.clone());

        myobj.input = (TwoBoxData)(input.clone());
        return myobj;
        }

    public void setup(final EvolutionState state,
                      final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        trainingSetSize = state.parameters.getInt(base.push(P_SIZE),null,1);
        if (trainingSetSize<1) state.output.fatal("Training Set Size must be an integer greater than 0"); 

        range = state.parameters.getInt(base.push(P_RANGE),null,1);
        if (trainingSetSize<1) state.output.fatal("Range must be an integer greater than 0"); 

        // Compute our inputs so they
        // can be copied with clone later
        
        inputsl0 = new double[trainingSetSize];
        inputsw0 = new double[trainingSetSize];
        inputsh0 = new double[trainingSetSize];
        inputsl1 = new double[trainingSetSize];
        inputsw1 = new double[trainingSetSize];
        inputsh1 = new double[trainingSetSize];
        outputs = new double[trainingSetSize];
        
        for(int x=0;x<trainingSetSize;x++)
            {
            inputsl0[x] = state.random[0].nextInt(range)+1;
            inputsw0[x] = state.random[0].nextInt(range)+1;
            inputsh0[x] = state.random[0].nextInt(range)+1;
            inputsl1[x] = state.random[0].nextInt(range)+1;
            inputsw1[x] = state.random[0].nextInt(range)+1;
            inputsh1[x] = state.random[0].nextInt(range)+1;
            outputs[x] = func(inputsl0[x],inputsw0[x],inputsh0[x],
                              inputsl1[x],inputsw1[x],inputsh1[x]);
            state.output.println("{" + inputsl0[x]+ "," + inputsw0[x]+ "," +
                                 inputsh0[x]+ "," + inputsl1[x]+ "," +
                                 inputsw1[x]+ "," + inputsh1[x]+ "," +
                                 outputs[x] + "},",3000,0);
            }

        // set up our input -- don't want to use the default base, it's unsafe
        input = (TwoBoxData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA), null, TwoBoxData.class);
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
                currentIndex = y;
                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                final double HIT_LEVEL = 0.01;
                final double PROBABLY_ZERO = 1.11E-15;
                final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses
                    
                result = Math.abs(outputs[y] - input.x);

                // very slight math errors can creep in when evaluating
                // two equivalent by differently-ordered functions, like
                // x * (x*x*x + x*x)  vs. x*x*x*x + x*x

                if (result<PROBABLY_ZERO)  // slightly off
                    result = 0.0;

                if (result > BIG_NUMBER)
                    result = BIG_NUMBER;                    

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
