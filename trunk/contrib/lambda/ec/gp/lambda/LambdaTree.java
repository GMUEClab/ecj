
/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda;

import java.io.Serializable;
import java.util.ArrayList;

import javax.naming.spi.DirStateFactory.Result;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.util.Parameter;
/**
 * A lambda tree is constructed from nodes.
 * A lambda tree represents an unique lambda expression. Read Section 3.1 for details.
 * @author Ye Xiaomeng
 *
 */
public class LambdaTree extends GPTree implements Serializable{
	private static final long serialVersionUID = 1;
	public boolean infinite = false;
	int betaReduceCounter;
	public int betaReduceCounterMax = 0;
	LambdaTree theClone;
	LambdaTree theCloneForL;
	public static final String P_BETA_REDUCTION_MAX = "brm";
	@Override
	public Object clone() {
		Object newTree = super.clone();
		((LambdaTree) newTree).infinite = this.infinite;
		((LambdaTree) newTree).betaReduceCounterMax = this.betaReduceCounterMax;
		return newTree;
	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){ 
		super.setup(state, base);
		setupBetaReductionMax(state, base);
	}
	// StringBuffer aBuffer;
	public void setupBetaReductionMax(final EvolutionState state,
			final Parameter base) {
		if (betaReduceCounterMax != 0) {
			return;// it's already set
		}
		betaReduceCounterMax = state.parameters.getInt(
				base.push(P_BETA_REDUCTION_MAX), null);
		if (betaReduceCounterMax <= 0) {
			state.output
					.fatal("No/Improper definition of P_BETA_REDUCTION_MAX.");
		}
	}

	public LambdaTree betaReduce() {
		betaReduceCounter = 0;
		theClone = (LambdaTree) this.clone();
		LambdaNode iter = (LambdaNode) theClone.child;
		betaReduce(iter);

		return theClone;
	}

	// helper method
	/**
	 * returns true if the reduced tree is lead by an LNode
	 */
	private boolean betaReduce(LambdaNode aNode) {
		if (aNode.parent == null) {
			return false;
		}
		if (infinite) {
			return false;
		}

		LambdaNode temp = null;
		if (aNode.toString().equals("P")) {
			// aBuffer.append(betaReduceCounter+theClone.child.makeCTree(false,
			// true, false)+"\n");
			betaReduceCounter++;
			if (betaReduceCounter == betaReduceCounterMax) {
				infinite = true;
				theClone.infinite = true;
//				return false;
			}
			if(betaReduceCounter == 501){
				@SuppressWarnings("unused")
				boolean somethingbad =false;
			}
			if (aNode.children[0].toString().equals("P")) {
				if (betaReduce((LambdaNode) aNode.children[0])) {
					betaReduce(aNode);
				} else {
					betaReduce((LambdaNode) aNode.children[1]);
				}
			} else if (aNode.children[0] instanceof LNode) {
				// before the replacement, check if there is any possible
				// overdue indexNode
				boolean hasOverdue = ((LambdaNode) aNode.children[1])
						.checkOverdueIndex(0);
				boolean leftHasOverdue = ((LambdaNode) aNode.children[0])
						.checkOverdueIndex(0);
				// the true beta reduce only happens here
				((LNode) (aNode.children[0])).replaceLwith(
						(GPNode) aNode.children[1], 1, 0, hasOverdue);
				// deep copy the whole subtree
				temp = (LambdaNode) aNode.children[0].children[0].clone();
				// remove P, left L, and right branch.
				temp.parent = aNode.parent;// aNode is P
				int oldArgPosition = aNode.argposition;
				if (aNode.parent instanceof GPNode) {
					((GPNode) aNode.parent).children[oldArgPosition] = temp;
				} else {// it's a tree root
					((GPTree) aNode.parent).child = temp;
				}
				aNode.parent = null;// break the link
				aNode = temp;
				// will this work??
				if (leftHasOverdue) {
					aNode.fixOverdue(0);
				}
				betaReduce(aNode);
			} else if (aNode.children[0] instanceof IndexNode) {
				betaReduce((LambdaNode) aNode.children[1]);
				// subtree after the indexNode is disregarded.
				// temp = (LambdaNode) aNode.children[0].lightClone();
			}
			if (aNode.toString().equals("L")) {
				return true;
			} else {
				return false;
			}
		} else if (aNode.toString().equals("L")) {
			betaReduce((LambdaNode) (aNode.children[0]));
			return true;
		} else {// aNode is an indexNode
			return false;
			// stopping criteria? indexNode cannot be further reduced
		}
	}

	private ArrayList<LNode> trunkL; // stores L nodes in the trunk, for clone

	public LambdaTree regularize(){
//		if(this.child.makeCTree(false, true, false).equals("0")){
//			System.out.println("found 0");}
//		System.out.println(this.child.makeCTree(false, true, false));
		return this.betaReduce().floatL();
	}
	
	public LambdaTree floatL() {
		System.out.println("floatL() called");
		trunkL = new ArrayList<LNode>();
		// beta reduction added
		theCloneForL = (LambdaTree) this.clone();
		if(infinite){
			return theCloneForL;
		}
		
		LambdaNode iter = (LambdaNode) theCloneForL.child;
		while (iter instanceof LNode) {
			trunkL.add((LNode) iter);
			iter = (LambdaNode) iter.children[0];
		}
		floatL(iter);
		return theCloneForL;
	}

	private void floatL(LambdaNode aNode) {
		if (aNode instanceof LNode && !trunkL.contains(aNode)) {
			// actual floating
			LambdaNode oldChild = (LambdaNode) aNode.children[0];
			LambdaNode oldParent = (LambdaNode) aNode.parent;// IMPORTANT, the
																// oldParent
																// will never be
																// a tree
			byte oldArg = aNode.argposition;
			byte oldParentArg = oldParent.argposition;
			GPNodeParent newParent = oldParent.parent;

			aNode.parent = newParent;
			aNode.argposition = oldParentArg;
			aNode.children[0] = oldParent;
			oldParent.argposition = 0;
			oldParent.parent = aNode;
			oldParent.children[oldArg] = oldChild;
			oldChild.argposition = oldArg;
			oldChild.parent = oldParent;
			if (newParent instanceof LambdaNode) {
				((LambdaNode) newParent).children[oldParentArg] = aNode;
			} else {
				((LambdaTree) newParent).child = aNode;
			}

			if (oldParent instanceof PNode) {// change index of other branches
				int theOtherArg = 0;
				if (oldArg == 1) {
					theOtherArg = 0;
				} else {// oldArg == 0
					theOtherArg = 1;
				}
				((LambdaNode) oldParent.children[theOtherArg])
						.branchIndexIncrease();
			} else if (oldParent instanceof LNode) {
				// no other branches exist. No need to change index value.
			} else {
				System.err.println("This should never happen, 1431234");
			}

			// float it once again if possible
			if (trunkL.contains(newParent) || newParent instanceof LambdaTree) {
				trunkL.add((LNode) aNode);
			}
			if (newParent instanceof PNode) {
				// beta reduction from the beginning
				theCloneForL = theCloneForL.regularize();//important, this will produce a second theCloneForL
			} else {
				floatL(aNode);
			}
		} else {
			for (GPNode iter : aNode.children) {
				floatL((LambdaNode) iter);
			}
		}		
	}
	
	public static LambdaTree generateFromGPTree(LambdaTree that){
		LambdaTree toB = new LambdaTree();
		toB.betaReduceCounterMax = that.betaReduceCounterMax;
		toB.child = (GPNode) (that.child.clone());
		return toB;
	}
}
