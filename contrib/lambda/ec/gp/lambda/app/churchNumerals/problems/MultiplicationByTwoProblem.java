/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.churchNumerals.problems;

import java.io.Serializable;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.gp.lambda.LambdaIndividual;
import ec.gp.lambda.LambdaTree;
import ec.gp.lambda.NodeCheck;
import ec.gp.lambda.interpreter.CNInterpreter;
import ec.gp.lambda.interpreter.Tomi;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
/**
 * highly mimicking Regression.java
 * 
 * Use this Problem class if you want to evolve the Multiplication-by-two function:
 * The function takes a church numeral as an input, and outputs another church numeral twice as big.
 * 
 * Used in MultiplicationByTwoProblem.params
 * 
 * @author Ye Xiaomeng
 * 
 */
public class MultiplicationByTwoProblem extends LambdaProblem implements
		SimpleProblemForm, Serializable {
	private static final long serialVersionUID = 1;
	public int trainingSetSize = 5;
	// public int inputs[] = { 2, 5, 7, 10, 20 };
	// public int inputs[] = {1,2,3,4,5};
	public int inputs[] = { 1, 2, 3, 4, 0 };
	public int outputs[];
	boolean evalBasedOnInterpretedCNValue = false;
	boolean excludeIdentity = true;

	public int func(int x) {
		return x * 2;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		outputs = new int[trainingSetSize];
		for (int x = 0; x < trainingSetSize; x++) {
			outputs[x] = func(inputs[x]);
		}
	}

	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (!ind.evaluated) {
			float sum = 0;
			int hits = 0;
			int superBadForInfReduction = 140;
			int badTreeForNumber = 100;
			int badForIdentityFunctions = 100;

			if (excludeIdentity
					&& NodeCheck.isIdentityOrEquivalent(state,
							((LambdaIndividual) ind).trees[0])) {
				sum += badForIdentityFunctions;
			} else {
				CNInterpreter interp = CNInterpreter.getInterpreter();
				if (evalBasedOnInterpretedCNValue) {
					// not using eval for this, instead, use lambda interpreter
					for (int y = 0; y < trainingSetSize; y++) {
						int theInputInteger = inputs[y];// solely for debug
														// reason
						LambdaIndividual inputTree = interp.generateTreeForInt(
								state, theInputInteger);
						LambdaIndividual combinedTree = ((LambdaIndividual) ind)
								.applyTo(inputTree);

						if (((LambdaTree) combinedTree.trees[0]).infinite) {
							sum += superBadForInfReduction;
						} else {
							int value = interp.interpreteNonfatal(state,
									((LambdaTree) combinedTree.trees[0]));
							if (value == -1) {
								// something wrong with the tree
								sum += badTreeForNumber;
							} else {
								int diff = Math.abs(outputs[y] - value);
								sum += diff;
								if (diff == 0) {
									hits++;
								}
							}
						}
					}
				} else {
					// eval based on Tominaga method
					for (int y = 0; y < trainingSetSize; y++) {
						int theInputInteger = inputs[y];
						int theOutputInt = outputs[y];
						LambdaIndividual inputTree = interp.generateTreeForInt(
								state, theInputInteger);
						LambdaIndividual outputTree = interp
								.generateTreeForInt(state, theOutputInt);
						LambdaIndividual combinedTree = ((LambdaIndividual) ind)
								.applyTo(inputTree);

						if (((LambdaTree) combinedTree.trees[0]).infinite) {
							sum += superBadForInfReduction;
						} else {
							float diff = tomi.diff(
									(LambdaTree) combinedTree.trees[0],
									(LambdaTree) outputTree.trees[0]);
							sum += diff;
							if (diff == 0) {
								hits++;
							}
						}
					}
				}
			}
			// the fitness better be KozaFitness!
			if(ind.fitness==null){
				ind.fitness = new KozaFitness();
			}
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, sum);
			f.hits = hits;
			ind.evaluated = true;
		}

	}
}
