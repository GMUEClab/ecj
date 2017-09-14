/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;

/* 
 * GPTree.java
 * 
 * Created: Fri Aug 27 17:14:02 1999
 * By: Sean Luke
 */

/**
 * GPTree is a GPNodeParent which holds the root GPNode of a tree
 * of GPNodes.  GPTrees typically fill out an array held in a GPIndividual
 * (their "owner") and their roots are evaluated to evaluate a Genetic
 * programming tree.
 *
 * GPTrees also have <i>constraints</i>, which are shared, and define items
 * shared among several GPTrees.

 * <p>In addition to serialization for checkpointing, GPTrees may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeTree(...,DataOutput)/readTree(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives a GPTree in binary.  It is the most efficient approach to sending
 * GPTrees over networks, etc.  The default versions of writeTree/readTree call writeRootedTree/readRootedTree
 * on their respective GPNode roots.
 *
 * <li><b>printTree(...,PrintWriter)/readTree(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives a GPTree in text encoded such that the GPTree is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will typically encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readTree</b>
 * largely calls readRootedTree on the GPNode root.  Likewise, <b>printTree</b> calls printRootedTree
 *
 * <li><b>printTreeForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints a GPTree in a fashion intended for human consumption only.
 * <b>printTreeForHumans</b> calls either <b>makeCTree</b> and prints the result,
 * calls <b>makeLatexTree</b> and prints the result, or calls <b>printRootedTreeForHumans</b> on the root.
 * Which one is called depends on the kind of tree you have chosen to print for humans, as is discussed next.
 * </ul>



 * <p>GPTrees can print themselves for humans in one of <b>four</b> styles:
 * <ol><li>A GPTree can print the tree as a Koza-style Lisp s-expression, which is the default.  
 * <li> A GPTree can print itself in pseudo-C format:
 *     <ol><li>Terminals can be printed either as variables "a" or as zero-argument functions "a()"
 *     <li>One-argument nonterminals are printed as functions "a(b)"
 *     <li>Two-argument nonterminals can be printed either as operators "b a c" or as functions "a(b, c)"
 *     <li>Nonterminals with more arguments are printed as functions "a(b, c, d, ...)"
 * </ol>
 * <li>A GPTree can print the tree AT&amp;T's Graphviz format.  You can snip the code out and save 
 * it as a ".dot" file to render in any Graphviz renderer (for example, use <a href="http://www.pixelglow.com/graphviz/">
 * this MacOS X front end to the renderer</a>).
 * <li>A GPTree can print the tree as a LaTeX2e code snippet, which can be inserted
 * into a LaTeX2e file and will result in a picture of the tree!  Cool, no?
 * </ol>
 *
 * <p>You turn the C-printing feature on with the <b>c</b> parameter, plus certain
 * optional parameters (<b>c-operators</b>, <b>c-variables</b>) as described below.
 * You turn the latex-printing <b>latex</b> parameter below.  The C-printing parameter
 * takes precedence.
 * <p>
 * Here's how the latex system works.  To insert the code, you'll need to include the
 * <tt>epic</tt>,<tt>ecltree</tt>, and probably the <tt>fancybox</tt> packages,
 * in that order.  You'll also need to define the command <tt>\gpbox</tt>, which
 * takes one argument (the string name for the GPNode) and draws a box with that
 * node.  Lastly, you might want to set a few parameters dealing with the 
 * <tt>ecltree</tt> package.
 *
 * <p>Here's an example which looks quite good (pardon the double-backslashes
 * in front of the usepackage statements -- javadoc is freaking out if I put
 * a single backslash.  So you'll need to remove the extra backslash in order
 * to try out this example):
 
 <p><table width=100% border=0 cellpadding=0 cellspacing=0>
 <tr><td bgcolor="#DDDDDD"><font size=-1><tt>
 <pre>

 \documentclass[]{article}
 \\usepackage{epic}     <b>% required by ecltree and fancybox packages</b>
 \\usepackage{ecltree}  <b>% to draw the GP trees</b>
 \\usepackage{fancybox} <b>% required by \Ovalbox</b>

 \begin{document}

 <b>% minimum distance between nodes on the same line</b>
 \setlength{\GapWidth}{1em}    

 <b>% draw with a thick dashed line, very nice looking</b>
 \thicklines \drawwith{\dottedline{2}}   

 <b>% draw an oval and center it with the rule.  You may want to fool with the
 % rule values, though these seem to work quite well for me.  If you make the
 % rule smaller than the text height, then the GP nodes may not line up with
 % each other horizontally quite right, so watch out.</b>
 \newcommand{\gpbox}[1]{\Ovalbox{#1\rule[-.7ex]{0ex}{2.7ex}}}
                
 <b>% Here's the tree which the GP system spat out</b>
 \begin{bundle}{\gpbox{progn3}}\chunk{\begin{bundle}{\gpbox{if-food-ahead}}
 \chunk{\begin{bundle}{\gpbox{progn3}}\chunk{\gpbox{right}}
 \chunk{\gpbox{left}}\chunk{\gpbox{move}}\end{bundle}}
 \chunk{\begin{bundle}{\gpbox{if-food-ahead}}\chunk{\gpbox{move}}
 \chunk{\gpbox{left}}\end{bundle}}\end{bundle}}\chunk{\begin{bundle}{\gpbox{progn2}}
 \chunk{\begin{bundle}{\gpbox{progn2}}\chunk{\gpbox{move}}
 \chunk{\gpbox{move}}\end{bundle}}\chunk{\begin{bundle}{\gpbox{progn2}}
 \chunk{\gpbox{right}}\chunk{\gpbox{left}}\end{bundle}}\end{bundle}}
 \chunk{\begin{bundle}{\gpbox{if-food-ahead}}\chunk{\begin{bundle}{\gpbox{if-food-ahead}}
 \chunk{\gpbox{move}}\chunk{\gpbox{left}}\end{bundle}}
 \chunk{\begin{bundle}{\gpbox{if-food-ahead}}\chunk{\gpbox{left}}\chunk{\gpbox{right}}
 \end{bundle}}\end{bundle}}\end{bundle}

 \end{document}
 </pre></tt></font></td></tr></table>

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tc</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(The tree's constraints)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>print-style</tt><br>
 <font size=-1>String, one of: c, dot, latex, lisp</td>
 <td valign=top>(specifies the print style of the tree)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>c-operators</tt><br>
 <font size=-1>bool = <tt>true</tt> (default) or <tt>false</tt></td>
 <td valign=top>(when printing using c, print two-argument functions operators "b a c"?  The alternative is functions "a(b, c)."</td></tr>
 <tr><td valign=top><i>base</i>.<tt>c-variables</tt><br>
 <font size=-1>bool = <tt>true</tt> (default) or <tt>false</tt></td>
 <td valign=top>(when printing using c, print zero-argument functions as variables "a"?  The alternative is functions "a()".)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.tree

 * @author Sean Luke
 * @version 1.0 
 */

public class GPTree implements GPNodeParent, Prototype
    {
    public static final String P_TREE = "tree";
    public static final String P_TREECONSTRAINTS = "tc";
    public static final String P_USEGRAPHVIZ = "graphviz";
    public static final String P_USELATEX = "latex";
    public static final String P_USEC = "c";
    public static final String P_USEOPS = "c-operators";
    public static final String P_USEVARS = "c-variables";
    public static final int NO_TREENUM = -1;

    public static final String P_PRINT_STYLE = "print-style";
    public static final String V_LISP = "lisp";
    public static final String V_DOT = "dot";
    public static final String V_LATEX = "latex";
    public static final String V_C = "c";
    public static final int PRINT_STYLE_LISP = 0;
    public static final int PRINT_STYLE_DOT = 1;
    public static final int PRINT_STYLE_LATEX = 2;
    public static final int PRINT_STYLE_C = 3;


    /** the root GPNode in the GPTree */
    public GPNode child;

    /** the owner of the GPTree */
    public GPIndividual owner;

    /** constraints on the GPTree  -- don't access the constraints through
        this variable -- use the constraints() method instead, which will give
        the actual constraints object. */
    public byte constraints;

    /** The print style of the GPTree. */
    public int printStyle;

    /** When using c to print for humans, do we print terminals as variables? 
        (as opposed to zero-argument functions)? */
    public boolean printTerminalsAsVariablesInC;

    /** When using c to print for humans, do we print two-argument nonterminals in operator form "a op b"? 
        (as opposed to functions "op(a, b)")? */
    public boolean printTwoArgumentNonterminalsAsOperatorsInC;

    public final GPTreeConstraints constraints( final GPInitializer initializer ) 
        { return initializer.treeConstraints[constraints]; }

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_TREE);
        }

    /** Returns true if I am "genetically" the same as tree,
        though we may have different owners. */
    public boolean treeEquals(GPTree tree)
        {
        return child.rootedTreeEquals(tree.child);
        }

    /** Returns a hash code for comparing different GPTrees.  In
        general, two trees which are treeEquals(...) should have the
        same hash code. */
    public int treeHashCode()
        {
        return child.rootedTreeHashCode();
        }

    /** Like clone() but doesn't copy the tree. */
    public GPTree lightClone()
        {
        try 
            { 
            return (GPTree)(super.clone());  // note that the root child reference is copied, not cloned
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // never happens
        }

    /** Deep-clones the tree.  Note that you should not deep-clone trees attached to the
        prototypical GPIndividual: they are blank trees with no root, and this method
        will generate a NullPointerException as a result. */
    public Object clone()
        {
        GPTree newtree = lightClone();
        newtree.child = (GPNode)(child.clone());  // force a deep copy
        newtree.child.parent = newtree;
        newtree.child.argposition = 0;
        return newtree;
        }
    
    /** An expensive function which determines my tree number -- only
        use for errors, etc. Returns ec.gp.GPTree.NO_TREENUM if the 
        tree number could not be
        determined (might happen if it's not been assigned yet). */
    public int treeNumber()
        {
        if (owner==null) return NO_TREENUM;
        if (owner.trees==null) return NO_TREENUM;
        for(int x=0;x<owner.trees.length;x++)
            if (owner.trees[x]==this) return x;
        return NO_TREENUM;
        }

    /** Sets up a prototypical GPTree with those features it shares with
        other GPTrees in its position in its GPIndividual, and nothhing more.

        This must be called <i>after</i> the GPTypes and GPNodeConstraints 
        have been set up.  Presently they're set up in GPInitializer,
        which gets called before this does, so we're safe. */
    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        // get rid of deprecated values
        if (state.parameters.exists(base.push(P_USEGRAPHVIZ), def.push(P_USEGRAPHVIZ)))
            state.output.error("Parameter no longer used.  See GPTree.java for details.", base.push(P_USEGRAPHVIZ), def.push(P_USEGRAPHVIZ));
        if (state.parameters.exists(base.push(P_USELATEX), def.push(P_USELATEX)))
            state.output.error("Parameter no longer used.  See GPTree.java for details.", base.push(P_USELATEX), def.push(P_USELATEX));
        if (state.parameters.exists(base.push(P_USEC), def.push(P_USEC)))
            state.output.error("Parameter no longer used.  See GPTree.java for details.", base.push(P_USEC), def.push(P_USEC));
        state.output.exitIfErrors();

        String style = state.parameters.getString(base.push(P_PRINT_STYLE), def.push(P_PRINT_STYLE));
        if (style == null)  // assume Lisp
            printStyle = PRINT_STYLE_LISP;
        else if (style.equals(V_C))
            printStyle = PRINT_STYLE_C;
        else if (style.equals(V_DOT))
            printStyle = PRINT_STYLE_DOT;
        else if (style.equals(V_LATEX))
            printStyle = PRINT_STYLE_LATEX;

        // in C, treat terminals as variables?  By default, yes.
        printTerminalsAsVariablesInC = state.parameters.getBoolean(base.push(P_USEVARS),def.push(P_USEVARS),true);

        // in C, treat two-child functions as operators?  By default, yes.
        printTwoArgumentNonterminalsAsOperatorsInC = state.parameters.getBoolean(base.push(P_USEOPS),def.push(P_USEOPS),true);

        // determine my constraints -- at this point, the constraints should have been loaded.
        String s = state.parameters.getString(base.push(P_TREECONSTRAINTS),
            def.push(P_TREECONSTRAINTS));
        if (s==null)
            state.output.fatal("No tree constraints are defined for the GPTree " + base + ".");
        else 
            constraints = GPTreeConstraints.constraintsFor(s,state).constraintNumber;
        
        state.output.exitIfErrors();  // because I promised
        // we're not loading the nodes at this point
        }


    /** Verification of validity of the tree -- strictly for debugging purposes only */
    public final void verify(EvolutionState state)
        {
        if (!(state.initializer instanceof GPInitializer))
            { state.output.error("Initializer is not a GPInitializer"); return; }
            
        GPInitializer initializer = (GPInitializer)(state.initializer);

        if (child == null)
            { state.output.error("Null root child of GPTree."); return; }
        if (owner == null)
            { state.output.error("Null owner of GPTree."); return; }
        if (owner.trees == null)
            { state.output.error("Owner has null trees."); return; }
        if (treeNumber() == NO_TREENUM) 
            { state.output.error("No Tree Number! I appear to be an orphan GPTree."); return; }
        if (constraints < 0 || constraints >= initializer.numTreeConstraints)
            { state.output.error("Preposterous tree constraints (" + constraints + ")"); return; }

        child.verify(state, constraints(initializer).functionset, 0);
        state.output.exitIfErrors();
        }

    /** Prints out the tree in single-line fashion suitable for reading
        in later by computer. O(n). 
        The default version of this method simply calls child's 
        printRootedTree(...) method. 
    */

    public void printTree(final EvolutionState state, final int log)
        {
        printTree(state, log, Output.V_VERBOSE);
        }

    /** Prints out the tree in single-line fashion suitable for reading
        in later by computer. O(n). 
        The default version of this method simply calls child's 
        printRootedTree(...) method. 
        @deprecated Verbosity no longer has an effect
    */

    public void printTree(final EvolutionState state, final int log,
        final int verbosity)
        {
        child.printRootedTree(state,log,0);
        // printRootedTree doesn't print a '\n', so I need to do so here
        state.output.println("",log);
        }

    /** Prints out the tree in single-line fashion suitable for reading
        in later by computer. O(n). 
        The default version of this method simply calls child's 
        printRootedTree(...) method. */

    public void printTree(final EvolutionState state,
        final PrintWriter writer)
        {
        child.printRootedTree(state,writer,0);
        // printRootedTree doesn't print a '\n', so I need to do so here
        writer.println();
        }

    /** Reads in the tree from a form printed by printTree. */
    public void readTree(final EvolutionState state,
        final LineNumberReader reader) throws IOException
        {
        int linenumber = reader.getLineNumber();

        // the next line will be the child
        String s = reader.readLine();
        if (s==null)  // uh oh
            state.output.fatal("Reading Line " + linenumber + ": " +
                "No Tree found.");
        else
            {
            GPInitializer initializer = ((GPInitializer)state.initializer);
            child = GPNode.readRootedTree(linenumber,new DecodeReturn(s),
                constraints(initializer).treetype,
                constraints(initializer).functionset,this,0,state);
            }
        }

    public void writeTree(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        GPInitializer initializer = ((GPInitializer)state.initializer);
        child.writeRootedTree(state,constraints(initializer).treetype, constraints(initializer).functionset, dataOutput);
        }

    public void readTree(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        GPInitializer initializer = ((GPInitializer)state.initializer);
        child = GPNode.readRootedTree(state,dataInput,constraints(initializer).treetype, constraints(initializer).functionset, this,0);
        }


    /** Prints out the tree in a readable Lisp-like fashion. O(n). 
        The default version of this method simply calls child's 
        printRootedTreeForHumans(...) method. */
    
    public void printTreeForHumans(final EvolutionState state, final int log)
        {               
        printTreeForHumans(state, log, Output.V_VERBOSE);
        }


    /** Prints out the tree in a readable Lisp-like fashion. O(n). 
        The default version of this method simply calls child's 
        printRootedTreeForHumans(...) method. 
        @deprecated Verbosity no longer has an effect.
    */
    
    public void printTreeForHumans(final EvolutionState state, final int log,
        final int verbosity)
        {               
        if (printStyle==PRINT_STYLE_C) state.output.print(child.makeCTree(true, 
                printTerminalsAsVariablesInC, printTwoArgumentNonterminalsAsOperatorsInC),log);
        else if (printStyle==PRINT_STYLE_LATEX) state.output.print(child.makeLatexTree(),log);
        else if (printStyle==PRINT_STYLE_DOT) state.output.print(child.makeGraphvizTree(), log);
        else child.printRootedTreeForHumans(state,log,0,0);
        // printRootedTreeForHumans doesn't print a '\n', so I need to do so here
        state.output.println("",log);
        }

    /** Builds a new randomly-generated rooted tree and attaches it to the GPTree. */

    public void buildTree(final EvolutionState state, final int thread) 
        {
        GPInitializer initializer = ((GPInitializer)state.initializer);
        child = constraints(initializer).init.newRootedTree(state,
            constraints(initializer).treetype,
            thread,
            this,
            constraints(initializer).functionset,
            0,
            GPNodeBuilder.NOSIZEGIVEN);
        }
    }
