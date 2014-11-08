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
/**
 * A lambda tree is constructed from nodes.
 * An L node is a node with a value P. Read Section 3.2 for details.
 * @author Ye Xiaomeng
 *
 */
public class PNode extends LambdaNode implements Serializable{
	private static final long serialVersionUID = 1;
	
	@Override
	public String toString() {
		return "P";
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input,
			ADFStack stack, GPIndividual individual, Problem problem) {
		// TODO Auto-generated method stub

	}
	public int expectedChildren(){
		return 2;
	}
	@Override
	public boolean checkOverdueIndex(int Lnum) {
		boolean result = false;
		for(GPNode iter : children) {
			if(((LambdaNode) iter).checkOverdueIndex(Lnum)){
				result = true;
			}
		}
		return result;
	}
}
