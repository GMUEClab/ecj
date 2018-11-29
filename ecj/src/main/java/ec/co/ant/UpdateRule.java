/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.Subpopulation;

/**
 *
 * @author Eric O. Scott
 */
public interface UpdateRule extends Setup
{
    public abstract void updatePheromones(final EvolutionState state, final PheromoneTable pheremones, final Subpopulation subpop);
}
