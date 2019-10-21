/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;
import java.util.List;

/**
 * A basic strategy for selecting a <code>Component</code> out of a list of candidates.
 *
 * <code>ComponentSelector</code> classes are intended to be used as building blocks for writing
 * <code>ConstructionRule</code>.
 *
 * @author Eric O. Scott
 * @see ConstructionRule
 */
public interface ComponentSelector extends Setup {
    public abstract Component choose(final EvolutionState state, final List<Component> components, final PheromoneTable pheromones, final int thread);
    }
