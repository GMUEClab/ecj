/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.app.knapsack;

import ec.EvolutionState;
import ec.co.Component;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A component representing an object in a knapsack problem.
 *
 * The heuristic <code>desirability()</code> of a <code>KnapsackComponent</code> is equal to its <code>value</code>
 * attribute.
 * 
 * @author Eric O. Scott
 * @see KnapsackProblem
 * @see ec.co
 */
public class KnapsackComponent extends Component {
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

    /** @return The heuristic value of the component (higher is better). */
    @Override
    public double desirability() {
        assert(repOK());
        return value;
        }
    
    @Override
    public void writeComponent(final EvolutionState state, final DataOutput output) throws IOException
        {
        output.writeDouble(size);
        output.writeDouble(value);
        }
    
    @Override
    public Component readComponent(final EvolutionState state, final DataInput input) throws IOException
        {
        final double size = input.readDouble();
        final double value = input.readDouble();
        return new KnapsackComponent(size, value);
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
    public boolean equals(final Object o)
        {
        if (!(o instanceof KnapsackComponent))
            return false;
        final KnapsackComponent ref = (KnapsackComponent) o;
        return size == ref.size
            && value == ref.value;
        }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.size) ^ (Double.doubleToLongBits(this.size) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
        }
    
    @Override
    public String toString()
        {
        return String.format("%s[size=%f, value=%f]", this.getClass().getSimpleName(), size, value);
        }
    }
