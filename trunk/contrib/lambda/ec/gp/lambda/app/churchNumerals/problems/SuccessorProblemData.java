/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.app.churchNumerals.problems;

import java.io.Serializable;

import ec.gp.GPData;

public class SuccessorProblemData extends GPData implements Serializable{
	private static final long serialVersionUID = 1;
	public int x;
	public void copyTo(final GPData gpd){
		((SuccessorProblemData)gpd).x = x;
	}
}
