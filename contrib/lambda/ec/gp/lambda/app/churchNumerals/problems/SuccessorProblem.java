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
 * Use this Problem class if you want to evolve the Successor function:
 * The Successor function takes a church numeral as an input, and outputs the next church numeral.
 * 
 * Used in SuccessorProblem.params
 * 
 * @author Ye Xiaomeng
 * 
 */
public class SuccessorProblem extends LambdaProblem implements SimpleProblemForm, Serializable {

	private static final long serialVersionUID = 1;
	public static final String P_SIZE = "size";
	public static final String P_INPUT_RANDOM = "inputRandom";
	public boolean inputRandom;
	public int trainingSetSize;
	public int inputs[];
	public int outputs[];
	public static final String P_INPUT_NUMBER_MAX = "inputMax";
	public int inputMax;
	// TODO:: make the following enum?
	boolean evalBasedOnInterpretedCNValue = false;
	boolean excludeIdentity = true;
	boolean evalBasedOnTomiSD = false; // SD: structural differences

	public int func(int x) {
		return x + 1;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		inputMax = state.parameters.getInt(base.push(P_INPUT_NUMBER_MAX), null);
		if (inputMax < 1) {
			state.output.fatal("P_INPUT_NUMBER_MAX cannot be less than 1",
					base.push(P_INPUT_NUMBER_MAX));
		}
		if (!(input instanceof SuccessorProblemData)) {
			state.output.fatal("GPData class must subclass from "
					+ SuccessorProblemData.class, base.push(P_DATA), null);
		}
		trainingSetSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
		if (trainingSetSize < 1) {
			state.output.fatal(
					"Training Set Size must be an integer greater than 0",
					base.push(P_SIZE));
		}
		inputRandom = state.parameters.getBoolean(base.push(P_INPUT_RANDOM),
				null, false);

		inputs = new int[trainingSetSize];
		outputs = new int[trainingSetSize];
		// TODO:: maybe you want to check the size and the number of inputs
		// match (<=)
		// if(!inputRandom){
		// trainingSetSize
		// }
		if (!inputRandom) {
			for (int x = 0; x < inputMax; x++) {
				inputs[x] = x;
			}
		} else {
			for (int x = 0; x < trainingSetSize; x++) {
				inputs[x] = state.random[0].nextInt(inputMax);
			}
		}
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
			int badTreeForNumber = 100;
			int superBadForInfReduction = 140;
			int badForIdentityFunctions = 100;
			// punish all individuals that are identity functions.
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
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, sum);
			f.hits = hits;
			ind.evaluated = true;
		}

	}
}
