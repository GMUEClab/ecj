package ec.util;

import org.junit.*;
import static org.junit.Assert.*;


public class ParameterDatabaseTest
{
    public ParameterDatabase params;

    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
                
        params.set(new Parameter("a.alias"), "foo");
        params.set(new Parameter("foo.alias"), "a");
        params.set(new Parameter("a.b"), "ab");
        params.set(new Parameter("a.b.c"), "abc");
        params.set(new Parameter("a.b.default"), "def");
        params.set(new Parameter("a.b.c.alias"), "ali");
        params.set(new Parameter("ali.d"), "abcd");
        params.set(new Parameter("ali.e.f"), "abcef");
        params.set(new Parameter("ali.alias"), "a");
	params.set(new Parameter("q.r.alias"), "alias-value-in-parent");
	params.set(new Parameter("alias-value-in-child.v"), "eggs");

	final ParameterDatabase parent0 = new ParameterDatabase();
	parent0.set(new Parameter("x.y.alias"), "alias-in-parent");
	parent0.set(new Parameter("alias-in-parent.z"), "spinach");
	parent0.set(new Parameter("alias-value-in-parent.s"), "toast");
	parent0.set(new Parameter("t.u.alias"), "alias-value-in-child");

	params.addParent(parent0);
        
    }

    @Test public void aliasInParent()
    {
	assertEquals("spinach", params.getString(new Parameter("x.y.z"), null));
    }

    @Test public void aliasValueInParent()
    {
	assertEquals("toast", params.getString(new Parameter("q.r.s"), null));
    }

    @Test public void aliasValueInChild()
    {
	assertEquals("eggs", params.getString(new Parameter("t.u.v"), null));
    }

    @Test public void cycle()
    {
     assertEquals(null, params.getString(new Parameter("a.k"), null));
    }
    
    @Test public void cycle2()
    {
     assertEquals(null, params.getString(new Parameter("a.b.c.k"), null));
    }
    
    @Test public void happyPath1()
    {
     assertEquals("ab", params.getString(new Parameter("a.b"), null));
    }

    @Test public void happyPath2()
    {
     assertEquals("abc", params.getString(new Parameter("a.b.c"), null));
    }
    
    @Test public void default1()
    {
     assertEquals("def", params.getString( new Parameter("a.b.d"), null));
    }

    @Test public void default2()
    {
     assertEquals("def", params.getString( new Parameter("a.b.d.e"), null));
    }


    @Test public void alias1()
    {
     assertEquals("abcd", params.getString( new Parameter("a.b.c.d"), null));
    }

    @Test public void alias2()
    {
     assertEquals("abcef", params.getString( new Parameter("a.b.c.e.f"), null));
    }

    @Test public void nonExistant()
    {
     assertEquals(null, params.getString( new Parameter("im.not.here"), null));
    }


    @Test public void nullParam()
    {
     assertEquals(null, params.getString(null, null));
    }

}
