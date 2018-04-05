/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.util;

import java.util.Collection;

/**
 * Miscellaneous static utility methods.
 * 
 * @author Eric O. Scott
 */
public class Misc
    {
    /** Private constructor throws an error if called. */
    private Misc() throws AssertionError
        {
        throw new AssertionError(String.format("%s: Cannot create instance of static class.", Misc.class.getSimpleName()));
        }
    
    /** @return true iff c contains any null values. */
    public static boolean containsNulls(final Collection c)
        {
        assert(c != null);
        for (Object o : c)
            if (o == null)
                return true;
        return false;
        }
    
    public static boolean doubleEquals(final double a, final double b, final double epsilon) {
        final double diff = Math.abs(a - b);
        return diff < epsilon
            || (Double.isNaN(diff) && a == b); // Handle the case where a = b = Double.POSITIVE_INFINITY or a = b = Double.NEGATIVE_INFINITY.
        }
    }
