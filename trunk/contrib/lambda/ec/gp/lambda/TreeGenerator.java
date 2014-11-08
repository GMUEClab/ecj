/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda;

import java.util.Stack;

import ec.gp.GPNode;
/**
 * Use this class to generate a lambda tree by a string form of a lambda tree.
 * The string forms of lambda trees can be obtained in the output file of the evolution.
 * Or you can use the method GPTree.writeTree() to get the string form of a specific tree.
 * 
 * This can be considered as a special version of GPTree.readTree(), 
 * except that this only serves for lambda trees.
 * 
 * Use this to understand what the outputs of the lambda expressions actually are.
 * 
 * @author Ye Xiaomeng
 *
 */
public class TreeGenerator {
	private static final long serialVersionUID = 1;
	public static LambdaTree generateFrom(String input) {
		LambdaTree tree = new LambdaTree();
		boolean endReached = false;
		Stack<PNode> branchOut = new Stack<PNode>();
		// root node
		char ch;
		int start = 0;
		outer:while (start < input.length()) {
			ch = input.charAt(start);
			switch (ch) {
			case 'P':
				tree.child = new PNode();
				branchOut.push((PNode) tree.child);
				break outer;
			case 'L':
				tree.child = new LNode();
				break outer;
			case '(':
			case ')':
			case ' ':
				start ++;
				continue;
			default: // index
				endReached = true;
				int index = ch - 48;
				tree.child = new IndexNode(index);
				break outer;
			}
		}
		GPNode iter = tree.child;
		iter.parent = tree;

		// everything else
		int arg = 0;
		GPNode toBeParent;
		for (int i = start+1; i < input.length(); i++) {
			ch = input.charAt(i);
			switch (ch) {
			case 'P':
				if (endReached) {
					System.out.println("TreeGenerator failed");
					return null;
				}
				toBeParent = iter;
				iter.children[arg] = new PNode();
				iter = iter.children[arg];
				iter.argposition = (byte) arg;
				iter.parent = toBeParent;
				branchOut.push((PNode) iter);
				arg = 0;
				break;
			case 'L':
				if (endReached) {
					System.out.println("TreeGenerator failed");
					return null;
				}
				toBeParent = iter;
				iter.children[arg] = new LNode();
				iter = iter.children[arg];
				iter.argposition = (byte) arg;
				iter.parent = toBeParent;
				arg = 0;
				break;
			case '(':
			case ')':
			case ' ':
			case ',':
				break;
			default: // index
				if (endReached) {
					System.out.println("TreeGenerator failed");
					return null;
				}
				int index = ch - 48;
				toBeParent = iter;
				iter.children[arg] = new IndexNode(index);
				iter = iter.children[arg];
				iter.argposition = (byte) arg;
				iter.parent = toBeParent;
				if (branchOut.isEmpty()) {
					endReached = true;
					break;
				}

				iter = branchOut.pop();
				arg = 1;
				break;
			}
		}

		return tree;
	}
}
