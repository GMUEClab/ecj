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
/**
 * The LambdaIndividual whose lambda tree represents (lambda x.xx) 
 */
public class Omega extends LambdaIndividual implements Serializable {
	private static final long serialVersionUID = 1;
	public Omega(){
		super();
		LambdaTree tree = new LambdaTree();
		
		tree.child = new LNode();
		GPNode iter = tree.child;
		iter.children[0] = new PNode();
		iter = iter.children[0];
		iter.children[0] = new IndexNode(1);
		iter.children[1] = new IndexNode(1);
		
		this.trees[0] = tree;
	}
}
