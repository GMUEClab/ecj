/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.churchNumerals.knownChurchNumerals;

import java.io.Serializable;

import ec.gp.GPNode;
import ec.gp.lambda.*;
//import ec.gp.lambda.LambdaIndividual;
//import ec.gp.lambda.LambdaTree;

/**
 * the hard-coded version of the successor function.
 * lambda n.lambda f.lambda x.f (n f x)
 */
public class TrueSuccessor extends LambdaIndividual implements Serializable{
	private static final long serialVersionUID = 1;
	public TrueSuccessor(){
		super();
		LambdaTree tree = new LambdaTree();
		
		tree.child = new LNode();
		GPNode iter = tree.child;
		iter.children[0] = new LNode();
		iter = iter.children[0];
		iter.children[0] = new LNode();
		iter = iter.children[0];
		iter.children[0] = new PNode();
		iter = iter.children[0];
		iter.children[0] = new IndexNode(2);
		iter.children[1] = new PNode();
		iter = iter.children[1];
		iter.children[0] = new PNode();
		iter.children[1] = new IndexNode(1);
		iter = iter.children[0];
		iter.children[0] = new IndexNode(3);
		iter.children[1] = new IndexNode(2);
		
		this.trees[0] = tree;
	}
}
