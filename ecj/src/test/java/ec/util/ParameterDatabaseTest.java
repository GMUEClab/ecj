package ec.util;

import org.junit.*;
import static org.junit.Assert.*;


public class ParameterDatabaseTest
{
    public ParameterDatabase params_A;
    public ParameterDatabase manualAliasExampleParams;
    public ParameterDatabase manualDefaultExampleParams;

    @Before
    public void setUp()
        {
        params_A = new ParameterDatabase();
                
        params_A.set(new Parameter("a.alias"), "foo");
        params_A.set(new Parameter("foo.alias"), "a");
        params_A.set(new Parameter("a.b"), "ab");
        params_A.set(new Parameter("a.b.c"), "abc");
        params_A.set(new Parameter("a.b.default"), "def");
        params_A.set(new Parameter("a.b.c.alias"), "ali");
        params_A.set(new Parameter("ali.d"), "abcd");
        params_A.set(new Parameter("ali.e.f"), "abcef");
        params_A.set(new Parameter("ali.alias"), "a");
	params_A.set(new Parameter("q.r.alias"), "alias-value-in-parent");
	params_A.set(new Parameter("alias-value-in-child.v"), "eggs");

	final ParameterDatabase parent0 = new ParameterDatabase();
	parent0.set(new Parameter("x.y.alias"), "alias-in-parent");
	parent0.set(new Parameter("alias-in-parent.z"), "spinach");
	parent0.set(new Parameter("alias-value-in-parent.s"), "toast");
	parent0.set(new Parameter("t.u.alias"), "alias-value-in-child");

	params_A.addParent(parent0);
        
        // Examples taken from the ECJ manual
        manualAliasExampleParams = new ParameterDatabase();
        
        manualAliasExampleParams.set(new Parameter("hello.there.alias"), "foo");
        manualAliasExampleParams.set(new Parameter("hello.there.mom.alias"), "bar");
        manualAliasExampleParams.set(new Parameter("hello.there.mom.how.are.you"), "whoa");
        manualAliasExampleParams.set(new Parameter("hello.there.brother"), "hey");
        manualAliasExampleParams.set(new Parameter("foo.dad"), "A");
        manualAliasExampleParams.set(new Parameter("bar.42"), "6x9");
        manualAliasExampleParams.set(new Parameter("hello.therewhoa"), "howdy");
        manualAliasExampleParams.set(new Parameter("my.hello.there.mom"), "dooty");
        manualAliasExampleParams.set(new Parameter("a.b.alias"), "foo");
        manualAliasExampleParams.set(new Parameter("foo.alias"), "a.b");
        
        manualDefaultExampleParams = new ParameterDatabase();
        manualDefaultExampleParams.set(new Parameter("gp.nc.default"), "gpnc");
        manualDefaultExampleParams.set(new Parameter("gpnc"), "ec.gp.GPNodeConstraints");
        manualDefaultExampleParams.set(new Parameter("gp.nc.0.name"), "nc0");
        manualDefaultExampleParams.set(new Parameter("gp.nc.1.name"), "nc1");
        manualDefaultExampleParams.set(new Parameter("gp.nc.2.name"), "nc2");
        manualDefaultExampleParams.set(new Parameter("gp.nc.3.name"), "nc3");
        manualDefaultExampleParams.set(new Parameter("gpnc.returns"), "nil");
        manualDefaultExampleParams.set(new Parameter("gpnc.size"), "nil");
        manualDefaultExampleParams.set(new Parameter("gpnc.child.c.default"), "gpncchild");
        manualDefaultExampleParams.set(new Parameter("gpncchild"), "nil");
        }

    @Test public void aliasInParent()
        {
	assertEquals("spinach", params_A.getString(new Parameter("x.y.z"), null));
        }

    @Test public void aliasValueInParent()
        {
	assertEquals("toast", params_A.getString(new Parameter("q.r.s"), null));
        }

    @Test public void aliasValueInChild()
        {
            assertEquals("eggs", params_A.getString(new Parameter("t.u.v"), null));
        }

    @Test public void cycle()
        {
        assertEquals(null, params_A.getString(new Parameter("a.k"), null));
        }
    
    @Test public void cycle2()
        {
        assertEquals(null, params_A.getString(new Parameter("a.b.c.k"), null));
        }
    
    @Test public void happyPath1()
        {
        assertEquals("ab", params_A.getString(new Parameter("a.b"), null));
        }

    @Test public void happyPath2()
        {
        assertEquals("abc", params_A.getString(new Parameter("a.b.c"), null));
        }
    
    @Test public void default1()
        {
        // FIXME This requires that we return a default *value.*  But we intend the default mechanism to act as an *alias parameter.*
        assertEquals("def", params_A.getString( new Parameter("a.b.d"), null));
        }

    @Test public void default2()
        {
        // FIXME This requires that we return a default *value.*  But we intended the default mechanism to act as an *alias parameter.*
        assertEquals("def", params_A.getString( new Parameter("a.b.d.e"), null));
        }

    @Test public void alias1()
        {
        assertEquals("abcd", params_A.getString( new Parameter("a.b.c.d"), null));
        }

    @Test public void alias2()
        {
        assertEquals("abcef", params_A.getString( new Parameter("a.b.c.e.f"), null));
        }

    @Test public void nonExistant()
        {
        assertEquals(null, params_A.getString( new Parameter("im.not.here"), null));
        }


    @Test public void nullParam()
        {
        assertEquals(null, params_A.getString(null, null));
        }

    @Test public void manualAliasExample1()
        {
        assertEquals("A", manualAliasExampleParams.getString( new Parameter("hello.there.dad"), null));
        }

    @Test public void manualAliasExample2()
        {
        assertEquals("6x9", manualAliasExampleParams.getString( new Parameter("hello.there.mom.42"), null));
        }

    @Test public void manualAliasExample3()
        {
        assertEquals("whoa", manualAliasExampleParams.getString( new Parameter("hello.there.mom.how.are.you"), null));
        }

    @Test public void manualAliasExample4()
        {
        assertEquals("howdy", manualAliasExampleParams.getString( new Parameter("hello.therewhoa"), null));
        }

    @Test public void manualAliasExample5()
        {
        assertEquals("dooty", manualAliasExampleParams.getString( new Parameter("my.hello.there.mom"), null));
        }

    @Test
    public void manualAliasExample6()
        {
        //XXX Accessing a cycle should also produce an error message
        assertEquals(null, manualAliasExampleParams.getString( new Parameter("a.b.yo"), null));
        }
    
    /* TODO This describes the behavior that we want from default, but which currently is not properly implemented.
    @Test public void manualDefaultExample()
        {
        assertEquals("ec.gp.GPNodeConstraints", manualDefaultExampleParams.getString( new Parameter("gp.nc.0"), null));
        assertEquals("nc0", manualDefaultExampleParams.getString( new Parameter("gp.nc.0.name"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.0.returns"), null));
        assertEquals("0", manualDefaultExampleParams.getString( new Parameter("gp.nc.0.size"), null));
        assertEquals("ec.gp.GPNodeConstraints", manualDefaultExampleParams.getString( new Parameter("gp.nc.1"), null));
        assertEquals("nc1", manualDefaultExampleParams.getString( new Parameter("gp.nc.1.name"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.1.returns"), null));
        assertEquals("1", manualDefaultExampleParams.getString( new Parameter("gp.nc.1.size"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.1.child.0"), null));
        assertEquals("gp.nc.2", manualDefaultExampleParams.getString( new Parameter("ec.gp.GPNodeConstraints"), null));
        assertEquals("nc2", manualDefaultExampleParams.getString( new Parameter("gp.nc.2.name"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.2.returns"), null));
        assertEquals("2", manualDefaultExampleParams.getString( new Parameter("gp.nc.2.size"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.2.child.0"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.2.child.1"), null));
        assertEquals("ec.gp.GPNodeConstraints", manualDefaultExampleParams.getString( new Parameter("gp.nc.3"), null));
        assertEquals("nc3", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.name"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.returns"), null));
        assertEquals("3", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.size"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.child.0"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.child.1"), null));
        assertEquals("nil", manualDefaultExampleParams.getString( new Parameter("gp.nc.3.child.2"), null));
        }
    */
    
    /** Happy path should handle double arrays, including scientific notation and +/- infinity. */
    @Test
    public void testGetDoubles1()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoubles(p, Double.NEGATIVE_INFINITY);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** If the parameter is empty, return null. */
    @Test
    public void testGetDoubles2()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "");
        final double[] result = instance.getDoubles(p, Double.NEGATIVE_INFINITY);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** If the parameter contains non-double garbage, return null. */
    @Test
    public void testGetDoubles3()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "Hello");
        final double[] result = instance.getDoubles(p, Double.NEGATIVE_INFINITY);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** If a value is below the given minimum, return null.  */
    @Test
    public void testGetDoubles4()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoubles(p, -1000);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** If a value is below the given minimum, return null.  */
    @Test
    public void testGetDoubles5()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 -1e-20 5.1e15");
        final double[] result = instance.getDoubles(p, 0.0);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** If a value is exactly the given minimum, return the array.  */
    @Test
    public void testGetDoubles6()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 1e-3 5.1e15");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 1e-3, 5.1e15 };
        final double[] result = instance.getDoubles(p, 1e-3);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** If a value is exactly the given minimum, return the array.  */
    @Test
    public void testGetDoubles7()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 0 5.1e15");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 0.0, 5.1e15 };
        final double[] result = instance.getDoubles(p, 0.0);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Use default parameter if first one is missing. */
    @Test
    public void testGetDoubles8()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** If both primary and default parameters are missing, return null. */
    @Test
    public void testGetDoubles9()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with default parameter and minimum value. */
    @Test
    public void testGetDoubles10()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15 };
        final double[] result = instance.getDoubles(a, b, 0);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Enforce minimum value with default parameter. */
    @Test
    public void testGetDoubles11()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoubles(a, b, 2.0);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with minimum value and expected length. */
    @Test
    public void testGetDoubles12()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15 };
        final double[] result = instance.getDoubles(a, Double.NEGATIVE_INFINITY, 4);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Enforce minimum value with expected length. */
    @Test
    public void testGetDoubles13()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoubles(a, 2.0, 4);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce expected length with minimum value. */
    @Test
    public void testGetDoubles14()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoubles(a, 0.0, 5);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce both expected length and minimum value. */
    @Test
    public void testGetDoubles15()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoubles(a, 2.0, 5);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with default parameter, minimum value, and expected length. */
    @Test
    public void testGetDoubles16()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY, 6);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Return null for a missing default parameter with minimum value and expected length. */
    @Test
    public void testGetDoubles17()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY, 6);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with primary parameter, default parameter, minimum value, and expected length. */
    @Test
    public void testGetDoubles18()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        instance.set(b, "0 9 8");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY, 6);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Enforce minimum value with default parameter and expected length. */
    @Test
    public void testGetDoubles19()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoubles(a, b, 0, 6);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce expected length with default parameter and minimum value. */
    @Test
    public void testGetDoubles20()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoubles(a, b, Double.NEGATIVE_INFINITY, 7);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce minimum value and expected length with default parameter. */
    @Test
    public void testGetDoubles21()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoubles(a, b, 0, 7);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path should handle double arrays, including scientific notation and +/- infinity. */
    @Test
    public void testGetDoublesUnconstrained1()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter p = new Parameter("alpha");
        instance.set(p, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesUnconstrained(p);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Ignore default parameter if first one is present. */
    @Test
    public void testGetDoublesUnconstrained2()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        instance.set(b, "5 6 7");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesUnconstrained(a, b);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Use default parameter if first one is missing. */
    @Test
    public void testGetDoublesUnconstrained3()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesUnconstrained(a, b);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** If both primary and default parameters are missing, return null. */
    @Test
    public void testGetDoublesUnconstrained4()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        final double[] result = instance.getDoublesUnconstrained(a, b);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with expected length. */
    @Test
    public void testGetDoublesUnconstrained5()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesUnconstrained(a, 6);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** If parameter doesn't match expected length, return null. */
    @Test
    public void testGetDoublesUnconstrained6()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoublesUnconstrained(a, 5);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with default parameter and expected length. */
    @Test
    public void testGetDoublesUnconstrained7()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesUnconstrained(a, b, 6);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Enforce expected length with default parameter. */
    @Test
    public void testGetDoublesUnconstrained8()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoublesUnconstrained(a, b, 5);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with maximum value */
    @Test
    public void testGetDoublesWithMax1()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesWithMax(a, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Return null if an element is greater than the maximum value. */
    @Test
    public void testGetDoublesWithMax2()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 2000");
        final double[] result = instance.getDoublesWithMax(a, 0.0, 1000);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Return null if an element is less than the minimum value. */
    @Test
    public void testGetDoublesWithMax3()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 -8 5.6 5.1e15");
        final double[] result = instance.getDoublesWithMax(a, 0.0, 1000);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce both maximum and minimum value. */
    @Test
    public void testGetDoublesWithMax4()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 -8 5.6 5.1e15 2000");
        final double[] result = instance.getDoublesWithMax(a, 0.0, 1000);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with expected length. */
    @Test
    public void testGetDoublesWithMax6()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        final double[] result = instance.getDoublesWithMax(a, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 6);
        assertArrayEquals(expected, result, 0.000001);
        }
    
    /** Enforce expected length. */
    @Test
    public void testGetDoublesWithMax7()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15 Infinity -Infinity");
        final double[] result = instance.getDoublesWithMax(a, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce maximum value with expected length. */
    @Test
    public void testGetDoublesWithMax8()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoublesWithMax(a, Double.NEGATIVE_INFINITY, 1000, 4);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce minimum value with expected length. */
    @Test
    public void testGetDoublesWithMax9()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoublesWithMax(a, 2.0, Double.POSITIVE_INFINITY, 4);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce minimum value and maximum value with expected length. */
    @Test
    public void testGetDoublesWithMax10()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoublesWithMax(a, 2.0, 1000, 4);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Enforce minimum value, maximum value, and expected length. */
    @Test
    public void testGetDoublesWithMax11()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        instance.set(a, "1.2 3.4 5.6 5.1e15");
        final double[] result = instance.getDoublesWithMax(a, 2.0, 1000, 6);
        assertArrayEquals(null, result, 0.000001);
        }
    
    /** Happy path with default parameter and expected length. */
    @Test
    public void testGetDoublesWithMax12()
        {
        final ParameterDatabase instance = new ParameterDatabase();
        final Parameter a = new Parameter("alpha");
        final Parameter b = new Parameter("beta");
        instance.set(b, "1.2 3.4 5.6 5.1e15");
        final double[] expected = new double[] { 1.2, 3.4, 5.6, 5.1e15 };
        final double[] result = instance.getDoublesWithMax(a, b, 0.0, Double.POSITIVE_INFINITY, 4);
        assertArrayEquals(expected, result, 0.000001);
        }
    
}
