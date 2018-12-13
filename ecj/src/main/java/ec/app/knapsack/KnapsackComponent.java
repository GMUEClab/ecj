/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.knapsack;

import ec.co.Component;

/**
 * A component representing an object in a knapsack problem.
 * 
 * @author Eric O. Scott
 */
public class KnapsackComponent implements Component {
    private double size;
    private double value;
    
    /** Create a component with the given size and value.
     * 
     * @param size Must be positive and finite, else throws IAE.
     * @param value Must be positive and finite, else throws IAE.
     */
    public KnapsackComponent(final double size, final double value)
    {
        if (size <= 0.0 || !Double.isFinite(size))
            throw new IllegalArgumentException(String.format("%s: attempted to create a component with size %f, but must be positive and finite.", this.getClass().getSimpleName(), size));
        if (value <= 0.0 || !Double.isFinite(value))
            throw new IllegalArgumentException(String.format("%s: attempted to create a component with value %f, but must be positive and finite.", this.getClass().getSimpleName(), value));
        this.size = size;
        this.value = value;
        assert(repOK());
    }
    
    /** @return The size of the component. */
    public double size()
    {
        assert(repOK());
        return size;
    }
    
    /** @return The value of the component. */
    public double value()
    {
        assert(repOK());
        return value;
    }

    /** @return The heuristic value of the component (which is equal to its value). */
    @Override
    public double cost() {
        assert(repOK());
        return value;
    }
    
    /** @return False iff the object is in an inconsistent state. */
    public final boolean repOK()
    {
        return size > 0.0
                && Double.isFinite(size)
                && value > 0.0
                && Double.isFinite(value);
    }
    
    @Override
    public String toString()
    {
        return String.format("[%s: size=%f, value=%f]", this.getClass().getSimpleName(), size, value);
    }
}
