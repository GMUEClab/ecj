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
    
    // <editor-fold defaultstate="collapsed" desc="doubleEquals">
    @Test
    public void testDoubleEquals1() {
        System.out.println("doubleEquals");
        final double a = Math.sqrt(2);
        final double b = Math.sqrt(3)*Math.sqrt(2)/Math.sqrt(3);
        assert(a != b);
        assertTrue(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals2() {
        System.out.println("doubleEquals");
        final double a = 1;
        final double b = 2;
        assertFalse(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals3() {
        System.out.println("doubleEquals");
        final double a = 0.00001;
        final double b = 0.00001;
        assertTrue(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals4() {
        System.out.println("doubleEquals");
        final double a = 0.000000000001;
        final double b = 0.000000000002;
        assertTrue(Misc.doubleEquals(a, b, 0.001));
        assertFalse(Misc.doubleEquals(a, b, 0.0000000000001));
    }
    
    @Test
    public void testDoubleEquals5() {
        System.out.println("doubleEquals");
        final double a = Double.POSITIVE_INFINITY;
        final double b = 10.0;
        assertFalse(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals6() {
        System.out.println("doubleEquals");
        final double a = Double.POSITIVE_INFINITY;
        final double b = Double.POSITIVE_INFINITY;
        assert(a == b);
        assertTrue(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals7() {
        System.out.println("doubleEquals");
        final double a = Double.NEGATIVE_INFINITY;
        final double b = Double.NEGATIVE_INFINITY;
        assert(a == b);
        assertTrue(Misc.doubleEquals(a, b, 0.00001));
    }
    
    @Test
    public void testDoubleEquals8() {
        System.out.println("doubleEquals");
        final double a = Double.NEGATIVE_INFINITY;
        final double b = Double.POSITIVE_INFINITY;
        assert(a != b);
        assertFalse(Misc.doubleEquals(a, b, 0.00001));
    }
    // </editor-fold>
    
}
