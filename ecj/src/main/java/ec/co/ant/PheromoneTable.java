/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.Setup;

/**
 * A data structure that maintains the pheromone values for different components.
 * 
 * @author Eric O. Scott
 */
public interface PheromoneTable extends Setup {
    
    /** Retrieve the pheromone concentration for component. /
     * 
     * @param c The ID of a component
     * @return  The pheromone concentration associated with component c
     */
    public abstract double get(final int c);
    
    /** Set the pheromone concentration for component c.
     * 
     * This may have side effects (such as simultaneously updating the concentration for a related component).
     * 
     * @param c The ID of a component
     * @param value The value to set its concentration to
     */
    public abstract void set(final int c, final double value);
}
