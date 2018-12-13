/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

/**
 *
 * @author Eric O. Scott
 */
public interface Component {
    
    /** @return The heuristic cost of the component. */
    public double cost();
}
