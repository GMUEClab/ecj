package ec.app.xor;

import ec.*;
import ec.neat.*;
import ec.simple.*;

public class XOR extends Problem implements SimpleProblemForm
    {
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
        if (ind.evaluated) return;

        if (!(ind instanceof NEATIndividual))
            state.output.fatal("Whoa! It's not a NEATIndividual!!!", null);
        
        NEATIndividual neatInd = (NEATIndividual)ind;
        
        if (!(neatInd.fitness instanceof SimpleFitness))
            state.output.fatal("Whoa! It's not a SimpleFitness!!!", null);
       
        //The four possible input combinations to xor
        //The first number is for biasing
        double[][] in=
            {
            {1.0, 0.0, 0.0},  // output 0
            {1.0, 0.0, 1.0},  //        1
            {1.0, 1.0, 0.0},  //        1
            {1.0, 1.0, 1.0}     //        0
            };
                
        double[] out = new double[4];
        double[] expectedOut = new double[] { 0.0, 1.0, 1.0, 0.0 };

        NEATNetwork net = neatInd.createNetwork();
        
        int netDepth = net.maxDepth();

        // Load and activate the network on each input
        for(int i = 0; i < in.length ;i++) 
            {
            net.loadSensors(in[i]);
            
            for(int relax = 0; relax < netDepth; relax++) 
                {
                net.activate(state);
                }
                
            // only have one output, so let's get it
            out[i] = net.getOutputResults()[0];

            net.flush();
            }


        // calculate fitness

        double errorSum = 0;
        for(int i = 0; i < out.length; i++)
            errorSum += Math.abs(out[i] - expectedOut[i]);

        double fitness = (4.0 - errorSum) * (4.0 - errorSum);

        // this is from the original code for counting as ideal
        boolean ideal = true;
        for(int i = 0; i < out.length; i++)
            if (Math.abs(out[i] - expectedOut[i]) > 0.5)
                { ideal = false; break; }
                                
        ((SimpleFitness)neatInd.fitness).setFitness(state, fitness, ideal);
        neatInd.evaluated = true;
        }

    }
