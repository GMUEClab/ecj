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
}
