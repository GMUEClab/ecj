/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.churchNumerals.knownChurchNumerals;

import java.io.Serializable;

import ec.gp.lambda.IndexNode;
import ec.gp.lambda.LambdaIndividual;
import ec.gp.GPNode;
import ec.gp.lambda.*;
//@deprecated. Left here for now, just in case.
public class OverdueExample extends LambdaIndividual implements Serializable{
	private static final long serialVersionUID = 1;
	//overdue in right branch
	public OverdueExample(){
		super();
		LambdaTree tree = new LambdaTree();
		//L(P(L(L(2))), (L(2)))
		tree.child = new LNode();
		GPNode iter = tree.child;
		iter.children[0] = new PNode();
		iter = iter.children[0];
		iter.children[0] = new LNode();
		iter.children[1] = new LNode();
		iter.children[1].children[0] = new IndexNode(2);
		iter = iter.children[0];
		iter.children[0] = new LNode();
		iter = iter.children[0];
		iter.children[0] = new IndexNode(2);
		
		this.trees[0] = tree;
	}
}
