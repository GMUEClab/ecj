/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.helloworld;

import java.io.Serializable;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;

public class HelloWorld extends Problem implements SimpleProblemForm, Serializable {
	private static final long serialVersionUID = 1;
	public static char[] target = "HELLOWORLD".toCharArray();
	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated) {
			return;
		}
		if(!(ind instanceof CharVectorIndividual)){
			state.output.fatal("Whoa!  It's not a CharVectorIndividual!!!",null);
		}
		int sum =0;
		CharVectorIndividual ind2 = (CharVectorIndividual) ind;
		for(int x=0; x< ind2.genomeLength();x++){
			sum += Math.abs(ind2.genome[x]-target[x]);
		}
		sum = 260- sum;
        if (!(ind2.fitness instanceof SimpleFitness))
            state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);
        ((SimpleFitness)ind2.fitness).setFitness(state,
            /// ...the fitness...
            (float)sum,
            ///... is the individual ideal?  Indicate here...
            sum == 260);
        ind2.evaluated = true;	}
}
