/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.test;

import java.util.Arrays;

/**
 *
 * @author Eric 'Siggy' Scott
 */
public class Option<T> {
    public static final Option NONE = new Option();
    private final T val;
    
    private Option() { this.val = null; }
    
    public Option(final T val) { assert(val != null); this.val = val; }
    
    public Option(final Option<T> ref) { assert(ref != null); val = ref.val; }
    
    public boolean isDefined() { return val != null; }
    
    public T get() { return val; }
    
    public T getWithDefault(final T def) {
        if (val != null)
            return val;
        return def;
    }
    
    @Override
    public String toString() {
        return String.format("[Option: val=%s]", val);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Option))
            return false;
        final Option ref = (Option) o;
        return (val == null ? 
                    ref.val == null  
                    : (val.getClass().isArray() ? 
                        (ref.val.getClass().isArray() && Arrays.equals((Object[])val, (Object[])ref.val))
                        : val.equals(ref.val)));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.val != null ? this.val.hashCode() : 0);
        return hash;
    }
}
