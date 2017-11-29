/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import java.util.Set;

/**
 *
 * @author Eric O. Scott
 */
public interface ConstructiveProblemForm {
    public abstract double cost(int from, int to);
    public abstract int numComponents();
    public abstract Set<Integer> componentSet();
}
