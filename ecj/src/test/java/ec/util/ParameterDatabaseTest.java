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

    /** If the database contains
     * 
     * a.b = ab
     * 
     * then the query "a.b" should return "ab".
     */
    @Test public void happyPath1()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b"), "ab");
        assertEquals("ab", params.getString(new Parameter("a.b"), null));
        }

    /** If the database contains
     * 
     * a.b.c = foobarbaz
     * 
     * then the query "a.b.c" should return "foobarbaz".
     */
    @Test public void happyPath2()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b.c"), "foobarbaz");
        
        assertEquals("foobarbaz", params.getString(new Parameter("a.b.c"), null));
        }
    
    /** Aliases contained entirely within the parent file should resolve:
     * 
     * If the parent file contains the lines
     * 
     * x.y.alias = alias-in-parent
     * alias-in-parent.z = spinach
     * 
     * then querying "x.y.z" should yield "spinach"
     * 
     */
    @Test public void aliasInParent()
        {
        final ParameterDatabase params = new ParameterDatabase();
	final ParameterDatabase parent0 = new ParameterDatabase();
	parent0.set(new Parameter("x.y.alias"), "alias-in-parent");
	parent0.set(new Parameter("alias-in-parent.z"), "spinach");
	params.addParent(parent0);
        
	assertEquals("spinach", params.getString(new Parameter("x.y.z"), null));
        }

    /** Aliases contained in the parent but referenced in the child should resolve:
     * 
     * If the child file contains the line
     *
     * q.r.alias = alias-value-in-parent
     * 
     * and the parent file contains
     * 
     * alias-value-in-parent.s = toast
     * 
     * then querying "q.r.s" should yield "toast"
     * 
     */
    @Test public void aliasValueInParent()
        {
        final ParameterDatabase params = new ParameterDatabase();
	params.set(new Parameter("q.r.alias"), "alias-value-in-parent");
	final ParameterDatabase parent0 = new ParameterDatabase();
	parent0.set(new Parameter("alias-value-in-parent.s"), "toast");
	params.addParent(parent0);
        
	assertEquals("toast", params.getString(new Parameter("q.r.s"), null));
        }

    /** Aliases contained in the child but referenced in the parent should resolve:
     * 
     * If the parent file contains the line
     * 
     * t.u.alias = alias-value-in-child
     * 
     * and the child file contains
     * 
     * alias-value-in-child.v = eggs
     * 
     * then the query "t.u.v" should yield "eggs"
     * 
     */
    @Test public void aliasValueInChild()
        {
        final ParameterDatabase params = new ParameterDatabase();
	params.set(new Parameter("alias-value-in-child.v"), "eggs");
	final ParameterDatabase parent0 = new ParameterDatabase();
	parent0.set(new Parameter("t.u.alias"), "alias-value-in-child");
	params.addParent(parent0);
        
        assertEquals("eggs", params.getString(new Parameter("t.u.v"), null));
        }

    /** Cycles should return null without going into an infinite loop:
     * 
     * If the parameter file contains the lines
     * 
     * a.alias = foo
     * foo.alias = a
     * 
     * then the query "a.k" should return null.
     */
    @Test public void aliasCycle1()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.alias"), "foo");
        params.set(new Parameter("foo.alias"), "a");
        assertEquals(null, params.getString(new Parameter("a.k"), null));
        }
    
    /** Cycles should return null without going into an infinite loop:
     * 
     * If the parameter file contains the lines
     * 
     * a.b.c.alias = ali
     * ali.alias = a
     * 
     * and there is no value specified for a parameter "ali.k",
     * then the query "a.b.c.k" should return null.
     */
    @Test public void aliasCycle2()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b.c.alias"), "ali");
        params.set(new Parameter("ali.alias"), "a");
        assertEquals(null, params.getString(new Parameter("a.b.c.k"), null));
        }
    
    /** An alias with no appended parameters should resolve.  If we have
     * 
     * a.alias = b
     * b = solutionb
     * 
     * then querying "a" should return "solutionb"
     */
    @Test
    public void alias1()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.alias"), "b");
        params.set(new Parameter("b"), "solutionb");
        assertEquals("solutionb", params.getString( new Parameter("a"), null));
        }
    
    /** An alias with one appended parameter should resolve:
     * 
     * If the parameter file contains the lines:
     * 
     * a.b.c.alias = ali
     * ali.d = abcd
     * 
     * then querying "a.b.c.d" should return "abcd".
     */
    @Test public void alias2()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b.c.alias"), "ali");
        params.set(new Parameter("ali.d"), "abcd");
        assertEquals("abcd", params.getString( new Parameter("a.b.c.d"), null));
        }

    /** An alias with two appended parameters should resolve:
     * 
     * If the parameter file contains the lines:
     * 
     * a.b.c.alias = ali
     * ali.e.f = abcef
     * 
     * then querying "a.b.c.e.f" should return "abcef".
     */
    @Test public void alias3()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b.c.alias"), "ali");
        params.set(new Parameter("ali.e.f"), "abcef");
        assertEquals("abcef", params.getString( new Parameter("a.b.c.e.f"), null));
        }
    
    /** An alias with four appended parameters should resolve.  If we have
     * 
     * a.alias = b
     * b = solutionb
     * b.c.d.e.f = solutionbcdef
     * 
     * then querying "a.c.d.e.f" should return "solutionbcdef"
     */
    @Test
    public void alias4()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.alias"), "b");
        params.set(new Parameter("b"), "solutionb");
        params.set(new Parameter("b.c.d.e.f"), "solutionbdcef");
        assertEquals("solutionbdcef", params.getString( new Parameter("a.c.d.e.f"), null));
        }
    
    /** A chain of aliases that reference each other should resolve. If we have
     * 
     * m.alias = n
     * n.alias = o
     * o.alias = p
     * p.alias = a
     * a.alias = b
     * b = solutionb
     * 
     * then querying any of "m", "n", "o", "p", or "a" should return "solutionb".
     */
    @Test
    public void alias5()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("m.alias"), "n");
        params.set(new Parameter("n.alias"), "o");
        params.set(new Parameter("o.alias"), "p");
        params.set(new Parameter("p.alias"), "a");
        params.set(new Parameter("a.alias"), "b");
        params.set(new Parameter("b"), "solutionb");
        
        assertEquals("solutionb", params.getString( new Parameter("m"), null));
        assertEquals("solutionb", params.getString( new Parameter("n"), null));
        assertEquals("solutionb", params.getString( new Parameter("o"), null));
        assertEquals("solutionb", params.getString( new Parameter("p"), null));
        assertEquals("solutionb", params.getString( new Parameter("a"), null));
        assertEquals("solutionb", params.getString( new Parameter("b"), null));
        }

    /** Aliases that are themselves complex names (ex. x.y.z) should resolve. If
     * we have the chain of aliases
     * 
     * m.n.o.p.alias = x.y.z
     * x.y.z.alias = h.g
     * h.g.alias = m
     * m.alias = n
     * n.alias = o
     * o.alias = p
     * p.alias = a
     * a.u.v = solutionauv
     * 
     * then the query "m.n.o.p.u.v" should return "solutionauv."
     */
    @Test
    public void alias6()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("m.n.o.p.alias"), "x.y.z");
        params.set(new Parameter("x.y.z.alias"), "h.g");
        params.set(new Parameter("h.g.alias"), "m");
        params.set(new Parameter("m.alias"), "n");
        params.set(new Parameter("n.alias"), "o");
        params.set(new Parameter("o.alias"), "p");
        params.set(new Parameter("p.alias"), "a");
        params.set(new Parameter("a.alias"), "b");
        params.set(new Parameter("a.u.v"), "solutionauv");
        
        assertEquals("solutionauv", params.getString( new Parameter("m.n.o.p.u.v"), null));
        assertEquals("solutionauv", params.getString( new Parameter("n.u.v"), null));
        }

    /** An alias for a default should resolve.  If we have
     * 
     * q.q.q.default = q.r
     * q.r.alias = mm
     * mm.h = solutionmmh
     * 
     * then the query "q.q.q.*.h" should return "solutionmmb", where "*" is 
     * anything.
     */
    @Test
    public void aliasDefault()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("q.q.q.default"), "q.r");
        params.set(new Parameter("q.r.alias"), "mm");
        params.set(new Parameter("mm.h"), "solutionmmh");
        
        assertEquals("solutionmmh", params.getString( new Parameter("q.q.q.0.h"), null));
        assertEquals("solutionmmh", params.getString( new Parameter("q.q.q.*.h"), null));
        assertEquals("solutionmmh", params.getString( new Parameter("q.q.q.foo_bar.h"), null));
        }
    
    /** Don't check for aliases on "parent" parameters. So if we have
     * 
     * parent.alias = x
     * x.0 = myparent.params
     * 
     * then querying "parent.0" should return null.
     */
    @Test
    public void aliasParentParams()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("parent.alias"), "x");  
        params.set(new Parameter("x.0"), "myparent.params"); 
        assertEquals(null, params.getString( new Parameter("parent.0"), null));
        }
    
    /** Don't check for defaults on "parent" parameters. So if we have
     * 
     * parent.default = x
     * x = myparent.params
     * 
     * then querying "parent.*" should return null, where "*" is anything.
     */
    @Test
    public void defaultParentParams()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("parent.default"), "x");  
        params.set(new Parameter("x"), "myparent.params"); 
        assertEquals(null, params.getString( new Parameter("parent.0"), null));
        assertEquals(null, params.getString( new Parameter("parent.1"), null));
        assertEquals(null, params.getString( new Parameter("parent.*"), null));
        assertEquals(null, params.getString( new Parameter("parent.foo_bar"), null));
        }
    
    /** Don't check for aliases on "print-params". So if we have
     * 
     * print-params.alias = x
     * x = false
     * 
     * then querying "print-params" should return null.
     */
    @Test
    public void aliasPrintParams1()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter(ParameterDatabase.PRINT_PARAMS + ".alias"), "x");  
        params.set(new Parameter("x"), "false"); 
        assertEquals(null, params.getString( new Parameter(ParameterDatabase.PRINT_PARAMS), null));
        }
    
    /** If the parameter "im.not.here" does not appear in the database, querying
     * for it should return null.
     */
    @Test public void nonExistant()
        {
        assertEquals(null, params_A.getString( new Parameter("im.not.here"), null));
        }

    /** Querying for the null parameter returns null. */
    @Test public void nullParam()
        {
        assertEquals(null, params_A.getString(null, null));
        }

    
    /** Simple alias example from the manual.  If the database contains
     * 
     * hello.there.alias = foo
     * foo.dad = A
     * 
     * then querying "hello.there.dad" should return "A"
     */
    @Test public void manualAliasExample1()
        {   
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("hello.there.alias"), "foo");
        params.set(new Parameter("foo.dad"), "A");
        
        assertEquals("A", params.getString( new Parameter("hello.there.dad"), null));
        }

    /** Simple alias example from the manual.  If the database contains
     * 
     * hello.there.mom.alias = bar
     * bar.42 = 6x9
     * 
     * then querying "hello.there.mom.42" should return "6x9".
     */
    @Test public void manualAliasExample2()
        {   
        final ParameterDatabase params = new ParameterDatabase();
        manualAliasExampleParams.set(new Parameter("hello.there.mom.alias"), "bar");
        manualAliasExampleParams.set(new Parameter("bar.42"), "6x9");
        
        assertEquals("6x9", manualAliasExampleParams.getString( new Parameter("hello.there.mom.42"), null));
        }

    /** Exact parameter matches take precedence over alias matches.  So if the
     * database contains
     * 
     * hello.there.alias = foo
     * hello.there.mom.how.are.you = A
     * foo.mom.how.are.you = B
     * 
     * then the query "hello.there.mom.how.are.you" should return "A".
     */
    @Test public void manualAliasExample3()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("hello.there.alias"), "foo");
        params.set(new Parameter("hello.there.mom.how.are.you"), "A");
        params.set(new Parameter("foo.mom.how.are.you"), "B");
        
        assertEquals("A", params.getString( new Parameter("hello.there.mom.how.are.you"), null));
        }

    /** Aliases only match to full parameter names, not partial ones.  So if the
     * database contains
     * 
     * hello.there.alias = foo
     * foo = value
     * hello.therewhoa = howdy
     * 
     * then the query "hello.therewhoa" will return "howdy".
     * 
     */
    @Test public void manualAliasExample4()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("hello.there.alias"), "foo");
        params.set(new Parameter("foo"), "value");
        params.set(new Parameter("hello.therewhoa"), "howdy");
        assertEquals("howdy", params.getString( new Parameter("hello.therewhoa"), null));
        }

    /** Aliases don't match in the middle of a string, only at the beginning. So
     * if the database contains
     * 
     * hello.there.alias = foo
     * foo.mom = howdy
     * my.foo.mom = dooty
     * 
     * then the query "hello.there.mom" should return "howdy".
     */
    @Test public void manualAliasExample5()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("hello.there.alias"), "foo");
        params.set(new Parameter("foo.mom"), "howdy");
        params.set(new Parameter("my.hello.there.mom"), "dooty");
        
        assertEquals("howdy", params.getString( new Parameter("hello.there.mom"), null));
        }

    /** Cycles should return null without going into an infinite loop.  So if we
     * have
     * 
     * a.b.alias = foo
     * foo.alias = a.b
     * 
     * then querying "a.b.yo" should return null.
     */
    @Test
    public void manualAliasExample6()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.b.alias"), "foo");
        params.set(new Parameter("foo.alias"), "a.b");
        assertEquals(null, params.getString( new Parameter("a.b.yo"), null));
        }
    
    /** A default with no appended parameters should resolve.  i.e. if we have
     * 
     * a.default = c
     * c = solutionc
     * 
     * then querying "a.*" should return "solutionc", where "*" is anything.
     */
    @Test public void default1()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.default"), "c");
        params.set(new Parameter("c"), "solutionc");
        assertEquals("solutionc", params.getString(new Parameter("a.0"), null));
        assertEquals("solutionc", params.getString(new Parameter("a.1"), null));
        assertEquals("solutionc", params.getString(new Parameter("a. "), null));
        assertEquals("solutionc", params.getString(new Parameter("a.*"), null));
        assertEquals("solutionc", params.getString(new Parameter("a.foo_bar"), null));
        }

    /** A default with one appended parameter should resolve.  i.e. if we have
     * 
     * a.default = c
     * c.b = solutioncb
     * 
     * then querying "a.*.b" should return "solutioncb", where "*" is anything.
     */
    @Test public void default2()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.default"), "c");
        params.set(new Parameter("c.b"), "solutioncb");
        assertEquals("solutioncb", params.getString(new Parameter("a.0.b"), null));
        assertEquals("solutioncb", params.getString(new Parameter("a.1.b"), null));
        assertEquals("solutioncb", params.getString(new Parameter("a. .b"), null));
        assertEquals("solutioncb", params.getString(new Parameter("a.*.b"), null));
        assertEquals("solutioncb", params.getString(new Parameter("a.foo_bar.b"), null));
        }

    /** A default with two appended parameters should resolve.  i.e. if we have
     * 
     * a.default = c
     * c.b.c = solutioncbc
     * 
     * then querying "a.*.b.c" should return "solutioncbc", where "*" is anything.
     */
    @Test public void default3()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.default"), "c");
        params.set(new Parameter("c.b.c"), "solutioncbc");
        assertEquals("solutioncbc", params.getString(new Parameter("a.0.b.c"), null));
        assertEquals("solutioncbc", params.getString(new Parameter("a.1.b.c"), null));
        assertEquals("solutioncbc", params.getString(new Parameter("a. .b.c"), null));
        assertEquals("solutioncbc", params.getString(new Parameter("a.*.b.c"), null));
        assertEquals("solutioncbc", params.getString(new Parameter("a.foo_bar.b.c"), null));
        }

    /** A default with five appended parameters should resolve.  i.e. if we have
     * 
     * a.default = c
     * c.b.c.d.e.f = solutioncbcdef
     * 
     * then querying "a.*.b.c.d.e.f" should return "solutioncbc", where "*" is anything.
     */
    @Test public void default4()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("a.default"), "c");
        params.set(new Parameter("c.b.c.d.e.f"), "solutioncbcdef");
        assertEquals("solutioncbcdef", params.getString(new Parameter("a.0.b.c.d.e.f"), null));
        assertEquals("solutioncbcdef", params.getString(new Parameter("a.1.b.c.d.e.f"), null));
        assertEquals("solutioncbcdef", params.getString(new Parameter("a. .b.c.d.e.f"), null));
        assertEquals("solutioncbcdef", params.getString(new Parameter("a.*.b.c.d.e.f"), null));
        assertEquals("solutioncbcdef", params.getString(new Parameter("a.foo_bar.b.c.d.e.f"), null));
        }

    /** Chained defaults should resolve.  i.e. if we have
     * 
     * q.q.q.default = q.r
     * q.r.default = mm
     * mm.h = solutionmmh
     * 
     * then the query "q.q.q.*.*.h" should return "solutionmmb", where the "*"s
     * are anything.
     */
    @Test public void default5()
        {
        final ParameterDatabase params = new ParameterDatabase();
        params.set(new Parameter("q.q.q.default"), "q.r");
        params.set(new Parameter("q.r.default"), "mm");
        params.set(new Parameter("mm.h"), "solutionmmb");
        assertEquals("solutionmmb", params.getString(new Parameter("q.q.q.0.1.h"), null));
        assertEquals("solutionmmb", params.getString(new Parameter("q.q.q.1.0.h"), null));
        assertEquals("solutionmmb", params.getString(new Parameter("q.q.q.foo.bar.h"), null));
        }

    /** An extended example of the defaults mechanism.
     * 
     * If the parameter file contains
     * 
     *   gp.nc.default = gpnc
     *   gpnc = ec.gp.GPNodeConstraints
     *  
     *   gp.nc.0.name = nc0
     *   gp.nc.1.name = nc1
     *   gp.nc.2.name = nc2
     *   gp.nc.3.name = nc3
     * 
     *   gpnc.returns = nil
     *   gpnc.size = nil
     *   gpnc.child.c.default = gpncchild
     * 
     *   gpncchild = nil
     * 
     * XXX This is inconsistent.
     * 
     * It says gp.nc.default rather than gp.nc.X.default,
     * but then it says gpnc.child.c.default rather than cpnc.child.default
     * 
     */
    
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
