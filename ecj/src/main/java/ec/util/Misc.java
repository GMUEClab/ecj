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
}
