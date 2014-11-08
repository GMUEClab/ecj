/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda;

import java.io.Serializable;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
/**
 * A lambda tree is constructed from nodes. Read Section 3.2 for details.
 * @author Ye Xiaomeng
 *
 */
public abstract class LambdaNode extends GPNode implements Serializable{
	private static final long serialVersionUID = 1;
	
	public LambdaNode() {
		super();
		// if (expectedChildren() != 0) {
		// children = new LambdaNode[expectedChildren()];
		// }

		// should I consider 0 size? Is it legal in java: new array[0]
		// this should be ok
		children = new LambdaNode[expectedChildren()];

	}

	// public Object clone() {
	// return super.clone();
	// }

	// there shouldn't be any method like this in LambdaNode
	// public LambdaNode applyTo(LambdaNode that) {
	// // this method is called only because this and that share the same
	// // parent "PNode"
	// LambdaNode toBreturn = this;
	// if (this.toString().equals("P")) {
	// toBreturn = ((LambdaNode) (this.children[0]))
	// .applyTo((LambdaNode) (this.children[1]));
	// toBreturn.applyTo(that);
	// }else if (this.toString().equals("L")) {
	// toBreturn.replaceLwith(that, 1);
	// }else{//this is an index node
	// //nothing
	// }
	// return toBreturn;
	// }

	// GOOD TO GO
	public void replaceLwith(GPNode that, int tracingIndex, int traversedLnum,
			boolean hasOverdue) {

		for (GPNode iter : children) {
			if (iter.toString().equals(tracingIndex + "")) {
				GPNode toReplace = (GPNode) that.clone();
				iter.replaceWith(toReplace);
				// after the replacement, change overdue index
				if (hasOverdue) {
					((LambdaNode) toReplace).fixOverdue(traversedLnum);
				}
			} else if (iter.toString().equals("P")) {
				((LambdaNode) iter).replaceLwith(that, tracingIndex,
						traversedLnum, hasOverdue);
			} else if (iter.toString().equals("L")) {
				((LambdaNode) iter).replaceLwith(that, tracingIndex + 1,
						traversedLnum + 1, hasOverdue);
			}

		}
	}

	// make this abstract
	public abstract boolean checkOverdueIndex(int Lnum);

	public void fixOverdue(int traversedLnum) {
		if (this instanceof IndexNode) {
			IndexNode theIndex = ((IndexNode) this);
			if (theIndex.getOverdueNum() != 0) {
				theIndex.setIndex(theIndex.getOverdueNum() + traversedLnum);
				theIndex.clearOverdue();
			}
		} else if (this instanceof LNode) {
			traversedLnum++;
		}
		for (GPNode iter : children) {
			((LambdaNode) iter).fixOverdue(traversedLnum);
		}
	}

	public void branchIndexDecrease() {// by 1
		if (this instanceof IndexNode) {
//			((IndexNode) this).setIndex(((IndexNode) this).getIndex() - 1);
			((IndexNode) this).valueMinusOne();
		}
		for (GPNode iter : children) {
			((LambdaNode) iter).branchIndexIncrease();
		}
	}

	public void branchIndexIncrease() {// by 1
		if (this instanceof IndexNode) {
//			((IndexNode) this).setIndex(((IndexNode) this).getIndex() + 1);
			((IndexNode)this).valuePlusOne();
		}
		for (GPNode iter : children) {
			((LambdaNode) iter).branchIndexIncrease();
		}
	}
}
