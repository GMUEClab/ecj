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
import ec.gp.lambda.app.churchNumerals.*;
import ec.util.Parameter;

/**
 * A lambda tree is constructed from nodes.
 * An index node is a node with a value N. Read Section 3.2 for details.
 * @author Ye Xiaomeng
 *
 */
public class IndexNode extends LambdaNode implements Serializable{
	private static final long serialVersionUID = 1;
	public static final String P_INDEX_MAX = "indexMax";
	/**
	 * IMPORTANT: the range of index will be 0 to indexMax-1 so set your
	 * indexMax by 1 largerf
	 */
	public int indexMax;

	private int index;

	/**
	 * only used for the purpose of replacing overdue L.
	 */
	private int overdueNum = 0;

	public int getOverdueNum() {
		return overdueNum;
	}
	public void setOverdueNum(int toB) {
		overdueNum = toB;
	}
	public void clearOverdue() {
		overdueNum = 0;
	}

	// private LNode boundL= null;
	// public boolean hasOverDueBound(){
	// return (boundL != null);
	// }
	// public LNode getBoundL() {
	// return boundL;
	// }
	// public void setBoundL(LNode boundL) {
	// this.boundL = boundL;
	// }

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		indexMax = state.parameters.getInt(base.push(P_INDEX_MAX), null);
		if (indexMax < 1) {
			state.output.fatal("P_INDEX_MAX cannot be less than 1",
					base.push(P_INDEX_MAX));
		}
	}

	public IndexNode() {
		super();
	}

	public IndexNode(int index) {
		super();
		this.index = index;
	}
	
	public void valuePlusOne(){
		index = index+1;
	}
	public void valueMinusOne(){
		index = index -1;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int toB) {
		index = toB;
	}

	@Override
	public String toString() {
		return Integer.toString(index);
	}

	public int expectedChildren() {
		return 0;
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input,
			ADFStack stack, GPIndividual individual, Problem problem) {
		// TODO Auto-generated method stub

	}

	/**
	 * mimicing the behavior of a ERC node
	 */
	@Override
	public void resetNode(final EvolutionState state, final int thread) {
		//IMPORTANT: by -1)+1, we can exclude indexNode of value 0, which is extremely dangerous
		index = state.random[thread].nextInt(indexMax-1)+1;
	}

	@Override
	public boolean checkOverdueIndex(int Lnum) {
		if (index > Lnum) {
			overdueNum = index - Lnum;
			// find the true bound
			// int currentLnum = 0;
			// LambdaNode iter = this;
			// while(iter.parent instanceof LambdaNode){
			// iter = (LambdaNode) iter.parent;
			// if(iter.toString().equals("L")){
			// currentLnum ++;
			// }
			// if(currentLnum == Lnum){
			// boundL = (LNode) iter;
			// break;
			// }
			// }
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object clone() {
		Object newnode = super.clone();
		((IndexNode) newnode).setOverdueNum(this.getOverdueNum());
		return newnode;
	}
}
