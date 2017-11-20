/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.util;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for static utility methods.
 * 
 * @author Eric O. Scott
 */
public class MiscTest
{
    
    public MiscTest()
    {
    }
    
    // <editor-fold defaultstate="collapsed" desc="containsNulls">
    @Test
    public void testContainsNulls1() {
        final Collection collection = new ArrayList() {{ }};
        assertFalse(Misc.containsNulls(collection));
    }
    
    @Test
    public void testContainsNulls2() {
        final Collection collection = new ArrayList() {{
            add(1); add(2.0); add(1); add(8.0);
        }};
        assertFalse(Misc.containsNulls(collection));
    }
    
    @Test
    public void testContainsNulls3() {
        final Collection collection = new ArrayList() {{
            add(1); add(2.0); add(null); add(8.0);
        }};
        assertTrue(Misc.containsNulls(collection));
    }
    
    @Test
    public void testContainsNulls4() {
        final Collection collection = new ArrayList() {{
            add(null); add(2.0); add(1); add(8.0);
        }};
        assertTrue(Misc.containsNulls(collection));
    }
    
    @Test
    public void testContainsNulls5() {
        final Collection collection = new ArrayList() {{
            add(1); add(2.0); add(1); add(null);
        }};
        assertTrue(Misc.containsNulls(collection));
    }
    
    @Test
    public void testContainsNulls6() {
        final Collection collection = new ArrayList() {{
            add(null);
        }};
        assertTrue(Misc.containsNulls(collection));
    }
    // </editor-fold>
}
