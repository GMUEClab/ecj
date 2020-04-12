/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;

import java.util.List;

/**
 * Defines a rule for updating pheromone values using a list of "ants."
 *
 * @author Eric O. Scott
 */
public interface UpdateRule extends Setup
    {
    void updatePheromones(final EvolutionState state, final PheromoneTable pheromones, final List individuals);
    }
