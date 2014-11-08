/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.churchNumerals.knownChurchNumerals;

import java.io.Serializable;

import ec.gp.GPNode;
import ec.gp.lambda.*;


/**
 * the hard-coded version of the multiplication-by-two function.
 */
public class TrueMultByTwo extends LambdaIndividual implements Serializable{
	private static final long serialVersionUID = 1;
	public TrueMultByTwo(){
		super();
		LambdaTree tree = TreeGenerator.generateFrom("L( L( P( 2, L (P (2, P (2, 1))))))");
//		LambdaTree tree = TreeGenerator.generateFrom("L L L P P 3 2 P P 3 2 1");

		this.trees[0] = tree;
	}
}
