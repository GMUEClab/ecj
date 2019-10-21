/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.Component;

/**
 * A data structure that maintains the pheromone values for different components.
 * 
 * @author Eric O. Scott
 */
public interface PheromoneTable extends Setup {
    
    /** Retrieve the pheromone concentration for component. /
     * 
     * @param state The state of the simulation
     * @param c The ID of a component
     * @param thread The index of the current thread
     * @return  The pheromone concentration associated with component c
     */
    public abstract double get(final EvolutionState state, final Component c, final int thread);
    
    /** Set the pheromone concentration for component c.
     * 
     * This may have side effects (such as simultaneously updating the concentration for a related component).
     * 
     * @param c The ID of a component
     * @param value The value to set its concentration to
     */
    public abstract void set(final Component c, final double value);
    }
