/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda;

import java.io.Serializable;

import ec.gp.GPIndividual;
import ec.gp.GPNode;
/**
 * A lambda individual contains a lambda tree. 
 * A lambda individual is the smallest unit that shows a phenotype/genotype.
 * @author Ye Xiaomeng
 *
 */
public class LambdaIndividual extends GPIndividual implements Serializable{
	private static final long serialVersionUID = 1;
	static boolean applyToWithLFloat = false;
	static boolean getApplyToWithLFloat(){
		return applyToWithLFloat;
	}

	public LambdaIndividual() {
		trees = new LambdaTree[1];
	}
 
	public LambdaIndividual applyTo(LambdaIndividual that) {
		PNode theP = new PNode();
		theP.children[0] = (GPNode)this.trees[0].child.clone();
		theP.children[1] = (GPNode)that.trees[0].child.clone();
		LambdaIndividual toBreturn = new LambdaIndividual();
		//new individual will inherit the BRM
		LambdaTree newLambdaTree = new LambdaTree();
		newLambdaTree.betaReduceCounterMax = ((LambdaTree)this.trees[0]).betaReduceCounterMax;
		
		toBreturn.trees[0] = newLambdaTree;
		// notice that betaReduce() returns a new clone
		toBreturn.trees[0].child = theP;

		if (getApplyToWithLFloat()) {
			toBreturn.trees[0] = ((LambdaTree) toBreturn.trees[0]).regularize();
		} else {
			toBreturn.trees[0] = ((LambdaTree) toBreturn.trees[0]).betaReduce();
		}
		return toBreturn;
	}

	public void selfSimplify() {
		// (Beta-reduction + L floating) on itself.
		LambdaTree theTree = ((LambdaTree) this.trees[0]).regularize();
		this.trees[0] = theTree;
	}

	public boolean hasInfReduction() {
		return ((LambdaTree) trees[0]).infinite;
	}
}
