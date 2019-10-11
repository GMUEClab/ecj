/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.util;


/**
 * An immutable 2-dimensional point.
 * 
 * @author Eric O. Scott
 */
public class IIntPoint {
    public final int x,y;
    public IIntPoint(int x, int y) { this.x = x; this.y = y; }
    
    public int[] toIntArray() { return new int[] { x, y }; }

    public boolean repOK() {
        return true;
        }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof IIntPoint))
            return false;
        final IIntPoint ref = (IIntPoint)o;
        return x == ref.x
            && y == ref.y;
        }

    @Override
    public int hashCode()
        {
        int hash = 7;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        return hash;
        }
    
    @Override
    public String toString() { return String.format("[%s: x=%d, y=%d]", this.getClass().getSimpleName(), x, y); }
    }
