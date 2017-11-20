/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.vector.IntegerVectorIndividual;

/**
 *
 * @author Eric O. Scott
 */
public interface ConstructionRule extends Setup
{
    public abstract IntegerVectorIndividual constructSolution(EvolutionState state, int subpop, int startNode, PheromoneMatrix pheromones);
}
