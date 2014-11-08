/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.interpreter;

import java.io.Serializable;

import ec.EvolutionState;
import ec.app.regression.RegressionData;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.lambda.IndexNode;
import ec.gp.lambda.LNode;
import ec.gp.lambda.LambdaIndividual;
import ec.gp.lambda.LambdaNode;
import ec.gp.lambda.LambdaTree;
import ec.gp.lambda.PNode;
//singleton class, used to interprete lambda trees into natural numbers.
//author: E
import ec.gp.lambda.NodeCheck;

/**
 * This class is used to convert a natural number to its church numeral form, or backwards.
 * @author Ye Xiaomeng
 *
 */
public class CNInterpreter implements Serializable{
	private static final long serialVersionUID = 1;
	private static CNInterpreter singleton;

	/**
	 * @return the singleton CNInterpreter
	 */
	public static CNInterpreter getInterpreter() {
		if (singleton == null) {
			singleton = new CNInterpreter();
		}
		return singleton;
	}

	/**
	 * returns -1 if not a church numeral tree.
	 * 
	 * @param state
	 * @param theTree the lambdaTree that represents some church numeral
	 * @return the natural number that theTree stands for; -1 if not a church
	 *         numeral tree.
	 */
	public int interpreteNonfatal(final EvolutionState state, LambdaTree theTree) {
		if(theTree.infinite){
			return -1;
		}
		int naturalNumber = 0;
		
//		int punishment = theTree.child.depth();
		// first node
		GPNode iter = theTree.child;
		if (iter==null||!NodeCheck.checkForTypeNonfatal(state, iter, "L")) {
			return -1;
		}
		iter = iter.children[0];
		if (!NodeCheck.checkForTypeNonfatal(state, iter, "L")) {
			return -1;
		}
		// third node
		iter = iter.children[0];
		if(NodeCheck.checkForIndexNonfatal(state, iter, 1)){
			return 0;
		}
		
		if (!NodeCheck.checkForTypeNonfatal(state, iter, "P")) {
			return -1;
		}
		int numberOfP = iter.depth() - 1;// the last layer contains no "P"
		// iterate over all children.
		for (int i = 0; i < numberOfP - 1; i++) {
			GPNode lkid = iter.children[0];
			if (!NodeCheck.checkForIndexNonfatal(state, lkid, 2)) {
				return -1;
			}
			naturalNumber++;
			iter = iter.children[1];
			if (!NodeCheck.checkForTypeNonfatal(state, iter, "P")) {
				return -1;
			}
		}
		// last two nodes;
		GPNode lkid = iter.children[0];
		if (!NodeCheck.checkForIndexNonfatal(state, lkid, 2)) {
			return -1;
		}
		naturalNumber++;
		GPNode rkid = iter.children[1];
		if (!NodeCheck.checkForIndexNonfatal(state, rkid, 1)) {
			return -1;
		}

		return naturalNumber;
	}

	/**call this when you are confident that theTree encodes for a CN. If not, a fatal error is thrown.
	 * 
	 * @param state
	 * @param theTree the lambdaTree that represents some church numeral
	 * @return the natural number that theTree stands for
	 */
	public int interprete(final EvolutionState state, LambdaTree theTree) {
		int naturalNumber = 0;
		// first node
		GPNode iter = theTree.child;
		NodeCheck.checkForType(state, iter, "L");
		// second node
		iter = iter.children[0];
		NodeCheck.checkForType(state, iter, "L");
		// third node
		iter = iter.children[0];
		NodeCheck.checkForType(state, iter, "P");
		int numberOfP = iter.depth() - 1;// the last layer contains no "P"
		// iterate over all children.
		for (int i = 0; i < numberOfP - 1; i++) {
			GPNode lkid = iter.children[0];
			NodeCheck.checkForIndex(state, lkid, 2);
			naturalNumber++;
			iter = iter.children[1];
			NodeCheck.checkForType(state, iter, "P");
		}
		// last two nodes;
		GPNode lkid = iter.children[0];
		NodeCheck.checkForIndex(state, lkid, 2);
		naturalNumber++;
		GPNode rkid = iter.children[1];
		NodeCheck.checkForIndex(state, rkid, 1);

		return naturalNumber;
	}

	/**
	 * generate the lambdaTree that represents a natural number in the Church numeral form.
	 * @param state
	 * @param theInt the natural number you want to convert
	 * @return a lambdaTree that represents theInt in the Church numeral form
	 */
	public LambdaIndividual generateTreeForInt(final EvolutionState state,
			int theInt) {
		//just using lambda classes, because I know what I am doing
		LambdaIndividual toBreturn = new LambdaIndividual();
		LambdaTree tree = new LambdaTree();
		tree.child = new LNode();//first L
		
		GPNode iter = tree.child;
		iter.parent = tree;
		
		GPNode toBeParent = iter;
		iter.children[0] = new LNode();//second L
		iter = iter.children[0];
		iter.argposition = 0;
		iter.parent = toBeParent;
		
		if(theInt ==0){
			toBeParent = iter;
			GPNode temp = new IndexNode(1);
			iter.children[0] = temp;
			iter = iter.children[0];
			iter.argposition = 0;
			iter.parent = toBeParent;
		}else{
			toBeParent = iter;
			iter.children[0] = new PNode();
			iter = iter.children[0];
			iter.argposition = 0;
			iter.parent = toBeParent;
			
			toBeParent = iter;
			iter.children[0] = new IndexNode(2);
			iter.children[0].argposition = 0;
			iter.children[0].parent = toBeParent;
			for(int i=0;i<theInt-1; i++){
				toBeParent = iter;
				iter.children[1] = new PNode();
				iter = iter.children[1];
				iter.argposition = 1;
				iter.parent = toBeParent;

				toBeParent = iter;
				iter.children[0] = new IndexNode(2);
				iter.children[0].argposition = 0;
				iter.children[0].parent = toBeParent;
			}
			toBeParent = iter;
			iter.children[1] = new IndexNode(1);
			iter = iter.children[1];
			iter.argposition = 1;
			iter.parent = toBeParent;
		}
		//after the tree is set up
		
		toBreturn.trees[0] = tree;

		return toBreturn;
	}
}
