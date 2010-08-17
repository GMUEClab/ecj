/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp.ge;

import ec.*;
import ec.gp.*;
import ec.vector.*;


public class GEIndividual extends ByteVectorIndividual
    {
	public static final String TREE_PREAMBLE = "Tree: ";
	public static final String BAD_TREE = "[BAD]";
        
	public void printIndividualForHumans(EvolutionState state, int log)
		{
		super.printIndividualForHumans(state, log);
		state.output.print(TREE_PREAMBLE, log);
		GPIndividual ind = (((GESpecies)species).map(state, this, 0));
		if (ind == null) state.output.println(BAD_TREE, log);
		else ind.trees[0].printTreeForHumans(state, log);
		}
    }
