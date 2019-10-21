/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.ConstructiveIndividual;

/**
 * Defines a rule for constructing ants based on pheromone values.
 *
 * Contrast to <code>ec.co.grasp.ConstructionRule</code>, which doesn't take pheromones into account.
 *
 * @author Eric O. Scott
 */
public interface ConstructionRule extends Setup
    {
    public abstract ConstructiveIndividual constructSolution(EvolutionState state, ConstructiveIndividual ind, PheromoneTable pheromones, int thread);
    }
