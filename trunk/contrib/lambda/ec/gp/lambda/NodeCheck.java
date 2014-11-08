/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda;

import java.io.Serializable;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.GPTree;

public class NodeCheck implements Serializable{
	private static final long serialVersionUID = 1;
	
	/**No fatal output will be done if type doesn't match.
	 * Instead, a boolean FALSE will be returned
	 * @param state
	 * @param toBchecked
	 * @param type
	 *            String: "L"/"P"/"I"
	 * @return false if type doesn't match
	 */
	public static boolean checkForTypeNonfatal(final EvolutionState state,
			GPNode toBchecked, String type) {
		if (type.equalsIgnoreCase("L")) {
			if (!(toBchecked instanceof LNode)) {
				return false;
			}else{
				return true;
			}
		} else if (type.equalsIgnoreCase("P")) {
			if (!(toBchecked instanceof PNode)) {
				return false;
			}else{
				return true;
			}
		} else if (type.equalsIgnoreCase("I")) {
			if (!(toBchecked instanceof IndexNode)) {
				return false;
			}else{
				return true;
			}
		} else {
			return false;
		}
	}
	/**
	 * throws a fatal error if type checking failed. Use this method if the type must match.
	 * @param state
	 * @param toBchecked
	 * @param type
	 *            String: "L"/"P"/"I"
	 */
	public static void checkForType(final EvolutionState state,
			GPNode toBchecked, String type) {
		if (type.equalsIgnoreCase("L")) {
			if (!(toBchecked instanceof LNode)) {
				state.output.fatal("Not a LNode", null, null);
			}
		} else if (type.equalsIgnoreCase("P")) {
			if (!(toBchecked instanceof PNode)) {
				state.output.fatal("Not a PNode", null, null);
			}
		} else if (type.equalsIgnoreCase("I")) {
			if (!(toBchecked instanceof IndexNode)) {
				state.output.fatal("Not a IndexNode", null, null);
			}
		} else {
			state.output
					.fatal("checkForType() called with wrong param: type must be either L/P/I",
							null, null);
		}
	}
	/**No fatal output will be done if index doesn't match.
	 * Instead, a boolean FALSE will be returned
	 * @param state
	 * @param toBchecked
	 * @param index
	 *            the index you want to check for: 1,2,3...
	 * @return false if doesn't match
	 */
	public static boolean checkForIndexNonfatal(final EvolutionState state,
			GPNode toBchecked, int index) {
		if (!(toBchecked instanceof IndexNode)) {
//			state.output.warning("Not a IndexNode", null, null);
			return false;
		}
		if(((IndexNode)toBchecked).getIndex()!=index){
			return false;
		}else{
			return true;
		}
	}
	/**
	 * throws a fatal error if index checking failed. Use this method if the index must match.
	 * @param state
	 * @param toBchecked
	 * @param index
	 *            the index you want to check for: 1,2,3...
	 */
	public static void checkForIndex(final EvolutionState state,
			GPNode toBchecked, int index) {
		if (!(toBchecked instanceof IndexNode)) {
			state.output.fatal("Not a IndexNode", null, null);
		}
		if(((IndexNode)toBchecked).getIndex()!=index){
			state.output.fatal("IndexNode's index does not match the target.", null, null);
		}
	}
	
	/**
	 * Check if a lambda tree representsthe identity function.
	 * @param state
	 * @param toBchecked
	 * @return true if toBchecked is the identity function.
	 */
	public static boolean isIdentityOrEquivalent(final EvolutionState state,
			GPTree toBchecked){
		LambdaTree toB = (LambdaTree.generateFromGPTree((LambdaTree)toBchecked)).betaReduce();
		GPNode iter = toB.child;
		if(iter instanceof LNode && iter.children[0] instanceof IndexNode && ((IndexNode)iter.children[0]).getIndex() ==1){
			return true;
		}else{
			return false;
		}
	}
}
