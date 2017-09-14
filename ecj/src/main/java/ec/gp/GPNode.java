/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;
import ec.util.*;
import java.io.*;
import java.util.*;

/* 
 * GPNode.java
 * 
 * Created: Fri Aug 27 17:14:12 1999
 * By: Sean Luke
 */

/**
 * GPNode is a GPNodeParent which is the abstract superclass of
 * all GP function nodes in trees.  GPNode contains quite a few functions
 * for cloning subtrees in special ways, counting the number of nodes
 * in subtrees in special ways, and finding specific nodes in subtrees.
 *
 * GPNode's lightClone() method does not clone its children (it copies the
 * array, but that's it).  If you want to deep-clone a tree or subtree, you
 * should use one of the cloneReplacing(...) methods instead.
 *
 * <p>GPNodes contain a number of important items:
 * <ul><li>A <i>constraints</i> object which defines the name of the node,
 * its arity, and its type constraints. This
 * object is shared with all GPNodes of the same function name/arity/returntype/childtypes.
 * <li>A <i>parent</i>.  This is either another GPNode, or (if this node
 * is the root) a GPTree.
 * <li>Zero or more <i>children</i>, which are GPNodes.
 * <li>An argument position in its parent.
 * </ul>
 *

 * <p>In addition to serialization for checkpointing, GPNodes may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeNode(...,DataOutput)/readNode(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives a GPNode in binary.  It is the most efficient approach to sending
 * GPNodes over networks, etc.  The default versions of writeNode/readNode both generate errors.
 * GPNode subclasses should override them to provide more functionality, particularly if you're planning on using
 * ECJ in a distributed fashion.  Both of these functions are called by GPNode's readRootedTree/writeRootedTree
 * respectively, which handle the reading/printing of the trees as a whole.
 *
 * <li><b>printNode(...,PrintWriter)/readNode(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives a GPNode in text encoded such that the GPNode is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will typically encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  Both of these functions are called by GPNode's readRootedTree/writeRootedTree
 * respectively, which handle the reading/printing of the trees as a whole.  Notably readRootedNode
 * will try to determine what kind of node is next, then call <b>readNode</b> on the prototype for that
 * node to generate the node.  <b>printNode</b> by default calls toString() and
 * prints the result, though subclasses often override this to provide additional functionality (notably
 * ERCs).
 *
 * <li><b>printNodeForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints a GPNode in a fashion intended for human consumption only.
 * <b>printNodeForHumans</b> by default calls toStringForHumans() (which by default calls toString()) and
 * prints the result.  printNodeForHumans is called by <b>printRootedTreeForHumans</b>, which handles
 * printing of the entire GPNode tree.
 * </ul>


 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>nc</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of the node constraints for the GPNode)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.node

 *
 * @author Sean Luke
 * @version 1.0 
 */

public abstract class GPNode implements GPNodeParent, Prototype
    {
    public static final String P_NODE = "node";
    public static final String P_NODECONSTRAINTS = "nc";
    public static final String GPNODEPRINTTAB = "    ";
    public static final int MAXPRINTBYTES = 40;

    public static final int NODESEARCH_ALL = 0;
    public static final int NODESEARCH_TERMINALS = 1;
    public static final int NODESEARCH_NONTERMINALS = 2;
    static final int NODESEARCH_CUSTOM = 3;  // should not be public

    public static final int CHILDREN_UNKNOWN = -1;
    
    // beats me if Java compilers will take advantage of the int->byte shortening.
    // They may want everything aligned, in which case they may buffer the object
    // anyway, hope not!

    /** The GPNode's parent.  4 bytes.  :-(  But it really helps simplify breeding. */
    public GPNodeParent parent;
    public GPNode children[];
    /** The argument position of the child in its parent. 
        This is a byte to save space (GPNode is the critical object space-wise) -- 
        besides, how often do you have 256 children? You can change this to a short
        or int easily if you absolutely need to.  It's possible to eliminate even
        this and have the child find itself in its parent, but that's an O(children[])
        operation, and probably not inlinable, so I figure a byte is okay. */
    public byte argposition;
    /** The GPNode's constraints.  This is a byte to save space -- how often do
        you have 256 different GPNodeConstraints?  Well, I guess it's not infeasible.
        You can increase this to an int without much trouble.  You typically 
        shouldn't access the constraints through this variable -- use the constraints(state)
        method instead. */
    public byte constraints;

    /* Returns the GPNode's constraints.  A good JIT compiler should inline this. */
    public final GPNodeConstraints constraints(final GPInitializer initializer) 
        { 
        return initializer.nodeConstraints[constraints]; 
        }

    /** The default base for GPNodes -- defined even though
        GPNode is abstract so you don't have to in subclasses. */
    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_NODE);
        }

    /** You ought to override this method to check to make sure that the
        constraints are valid as best you can tell.  Things you might
        check for:

        <ul>
        <li> children.length is correct
        <li> certain arguments in constraints.childtypes are 
        swap-compatible with each other
        <li> constraints.returntype is swap-compatible with appropriate 
        arguments in constraints.childtypes
        </ul>
        
        You can't check for everything, of course, but you might try some
        obvious checks for blunders.  The default version of this method
        simply calls numChildren() if it's defined (it returns something >= 0).
        If the value doesn't match the current number of children, an error is raised.
        This is a simple constraints check.

        The ultimate caller of this method must guarantee that he will eventually
        call state.output.exitIfErrors(), so you can freely use state.output.error
        instead of state.output.fatal(), which will help a lot.
        
        Warning: this method may get called more than once.
    */

    public void checkConstraints(final EvolutionState state,
        final int tree,
        final GPIndividual typicalIndividual,
        final Parameter individualBase)
        {
        int numChildren = expectedChildren();
        if (numChildren >= 0 && children.length != numChildren)  // uh oh
            state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase + 
                ", was expecting " + numChildren + " but got " + children.length);
        }
        
    /** 
        Returns the number of children this node expects to have.  This method is
        only called by the default implementation of checkConstraints(...), and by default
        it returns CHILDREN_UNKNOWN.  You can override this method to return a value >= 0,
        which will be checked for in the default checkConstraints(...), or you can leave
        this method alone and override checkConstraints(...) to check for more complex constraints
        as you see fit.
    */
        
    public int expectedChildren() { return CHILDREN_UNKNOWN; }

    /** 
        Sets up a <i>prototypical</i> GPNode with those features all nodes of that
        prototype share, and nothing more.  So no filled-in children, 
        no argposition, no parent.  Yet.

        This must be called <i>after</i> the GPTypes and GPNodeConstraints 
        have been set up.  Presently they're set up in GPInitializer,
        which gets called before this does, so we're safe. 
        
        You should override this if you need to load some special features on
        a per-function basis.  Note that base hangs off of a function set, so
        this method may get called for different instances in the same GPNode
        class if they're being set up as prototypes for different GPFunctionSets.

        If you absolutely need some global base, then you should use something
        hanging off of GPDefaults.base().

        The ultimate caller of this method must guarantee that he will eventually
        call state.output.exitIfErrors(), so you can freely use state.output.error
        instead of state.output.fatal(), which will help a lot.
    */

    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        // determine my constraints -- at this point, the constraints should have been loaded.
        String s = state.parameters.getString(base.push(P_NODECONSTRAINTS),
            def.push(P_NODECONSTRAINTS));
        if (s==null)
            state.output.fatal("No node constraints are defined for the GPNode " + 
                toStringForError(),base.push(P_NODECONSTRAINTS),
                def.push(P_NODECONSTRAINTS));
        else constraints = GPNodeConstraints.constraintsFor(s,state).constraintNumber;

        // The number of children is determined by the constraints.  Though
        // for some special versions of GPNode, we may have to enforce certain
        // rules, checked in children versions of setup(...)

        GPNodeConstraints constraintsObj = constraints(((GPInitializer)state.initializer));
        int len = constraintsObj.childtypes.length;
        if (len == 0) children = constraintsObj.zeroChildren;
        else children = new GPNode[len];
        }

    /** Returns the argument type of the slot that I fit into in my parent.  
        If I'm the root, returns the treetype of the GPTree. */
    public final GPType parentType(final GPInitializer initializer)
        {
        if (parent instanceof GPNode)
            return ((GPNode)parent).constraints(initializer).childtypes[argposition];
        else // it's a tree root
            return ((GPTree)parent).constraints(initializer).treetype;
        }


    /** Verification of validity of the node in the tree -- strictly for debugging purposes only */
    final int verify(EvolutionState state, GPFunctionSet set, int index)
        {
        if (!(state.initializer instanceof GPInitializer))
            { state.output.error("" + index + ": Initializer is not a GPInitializer"); return index+1; }
            
        GPInitializer initializer = (GPInitializer)(state.initializer);
        
        // 1. Is the parent and argposition right?
        if (parent == null)
            { state.output.error("" + index + ": null parent"); return index+1; }
        if (argposition < 0)
            { state.output.error("" + index + ": negative argposition"); return index+1; }
        if (parent instanceof GPTree && ((GPTree)parent).child != this)
            { state.output.error("" + index + ": I think I am a root node, but my GPTree does not think I am a root node"); return index+1; }
        if (parent instanceof GPTree && argposition != 0)
            { state.output.error("" + index + ": I think I am a root node, but my argposition is not 0"); return index+1; }
        if (parent instanceof GPNode && argposition >= ((GPNode)parent).children.length)
            { state.output.error("" + index + ": argposition outside range of parent's children array"); return index+1; }
        if (parent instanceof GPNode && ((GPNode)parent).children[argposition] != this)
            { state.output.error("" + index + ": I am not found in the provided argposition ("+argposition+") of my parent's children array"); return index+1; }

        // 2. Are the parents and argpositions right for my kids? [need to double check]
        if (children==null)
            { state.output.error("" + index + ": Null Children Array"); return index+1; }
        for(int x=0;x<children.length;x++)
            {
            if (children[x] == null)
                { state.output.error("" + index + ": Null Child (#" + x + " )"); return index+1; }
            if (children[x].parent != this)
                { state.output.error("" + index + ": child #"+x+" does not have me as a parent"); return index+1; }
            if (children[x].argposition < 0)
                { state.output.error("" + index + ": child #"+x+" argposition is negative"); return index+1; }
            if (children[x].argposition != x)
                { state.output.error("" + index + ": child #"+x+" argposition does not match position in the children array"); return index+1; }
            }
        
        // 3. Do I have valid constraints?
        if (constraints < 0 || constraints >= initializer.numNodeConstraints)
            { state.output.error("" + index + ": Preposterous node constraints (" + constraints + ")"); return index+1; }
        
        // 4. Am I swap-compatable with my parent?
        if (parent instanceof GPNode && !constraints(initializer).returntype.compatibleWith(initializer, 
                ((GPNode)(parent)).constraints(initializer).childtypes[argposition]))
            { state.output.error("" + index + ": Incompatable GP type between me and my parent"); return index+1; }
        if (parent instanceof GPTree && !constraints(initializer).returntype.compatibleWith(initializer,
                ((GPTree)(parent)).constraints(initializer).treetype))
            { state.output.error("" + index + ": I am root, but incompatable GP type between me and my tree return type"); return index+1; }
        
        // 5. Is my class in the GPFunctionSet?
        GPNode[] nodes = set.nodesByArity[constraints(initializer).returntype.type][children.length];
        boolean there = false;
        for(int x=0;x<nodes.length;x++)
            if (nodes[x].getClass() == this.getClass()) { there = true; break; }
        if (!there)
            { state.output.error("" + index + ": I'm not in the function set."); return index+1; }
            
        // otherwise we've passed -- go to next node
        index++;
        for(int x=0;x<children.length;x++)
            index = children[x].verify(state, set, index);
        state.output.exitIfErrors();
        return index;
        }


    /** Returns true if I can swap into node's position. */

    public final boolean swapCompatibleWith(final GPInitializer initializer,
        final GPNode node)
        {
        // I'm atomically compatible with him; a fast check
        if (constraints(initializer).returntype==node.constraints(initializer).returntype)  // no need to check for compatibility
            return true;

        // I'm set compatible with his parent's swap-position
        GPType type;
        if (node.parent instanceof GPNode)  // it's a GPNode
            type = 
                ((GPNode)(node.parent)).constraints(initializer).childtypes[node.argposition];
        else // it's a tree root; I'm set compatible with the GPTree type
            type = 
                ((GPTree)(node.parent)).constraints(initializer).treetype;
        
        return constraints(initializer).returntype.compatibleWith(initializer,type);
        }

    /** Returns the number of nodes, constrained by g.test(...)
        in the subtree for which this GPNode is root.  This might
        be sped up by caching the value.  O(n). */
    public int numNodes(final GPNodeGatherer g)
        {
        int s=0;
        for(int x=0;x<children.length;x++) s += children[x].numNodes(g);
        return s + (g.test(this) ? 1 : 0);
        }

    /** Returns the number of nodes, constrained by nodesearch,
        in the subtree for which this GPNode is root.
        This might be sped up by cacheing the value somehow.  O(n). */
    public int numNodes(final int nodesearch)
        {
        int s=0;
        for(int x=0;x<children.length;x++) s += children[x].numNodes(nodesearch);
        return s + ((nodesearch==NODESEARCH_ALL ||
                (nodesearch==NODESEARCH_TERMINALS && children.length==0) ||
                (nodesearch==NODESEARCH_NONTERMINALS && children.length>0)) ? 1 : 0);
        }

    /** Returns the depth of the tree, which is a value >= 1.  O(n). */
    public int depth()
        {
        int d=0;
        int newdepth;
        for(int x=0;x<children.length;x++)
            {
            newdepth = children[x].depth();
            if (newdepth>d) d = newdepth;
            }
        return d + 1;
        }
        
    /** Returns the path length of the tree, which is the sum of all paths from all nodes to the root.   O(n). */
    public int pathLength(int nodesearch) { return pathLength(NODESEARCH_ALL, 0); }
    
    int pathLength(int nodesearch, int currentDepth)
        {
        int sum = currentDepth;
        if (nodesearch == NODESEARCH_NONTERMINALS && children.length==0 ||  // I'm a leaf, don't include me
            nodesearch == NODESEARCH_TERMINALS && children.length > 0)  // I'm a nonleaf, don't include me
            sum = 0;
            
        for(int x=0;x<children.length;x++)
            sum += pathLength(nodesearch, currentDepth + 1);
        return sum;
        }
        
    /** Returns the mean depth of the tree, which is path length (sum of all paths from all nodes to the root) divided by the number of nodes.  O(n). */
    int meanDepth(int nodesearch)
        {
        return pathLength(nodesearch) / numNodes(nodesearch);
        }

    /** Returns the depth at which I appear in the tree, which is a value >= 0. O(ln n) avg.*/
    public int atDepth()
        {
        // -- new code, no need for recursion
        GPNodeParent cparent = parent;
        int count=0;

        while(cparent!=null && cparent instanceof GPNode)
            {
            count++;
            cparent = ((GPNode)(cparent)).parent;
            }
        return count;

        /* // -- old code
           if (parent==null) return 0;
           if (!(parent instanceof GPNode))  // found the root!
           return 0;
           else return 1 + ((GPNode)parent).atDepth();
        */
        }

    /** Returns an iterator over all the GPNodes in the subtree rooted by this GPNode,
        filtered by the provided GPNodeGatherer. */
    public Iterator iterator(final GPNodeGatherer g)
        {
        return new Iterator()
            {
            GPNode current;
            Iterator iter = iterator();
                        
            void fill()
                {
                if (current == null)
                    while(iter.hasNext())
                        {
                        GPNode node = (GPNode)(iter.next());
                        if (g.test(node))
                            {
                            current = node;
                            break;
                            }
                        }
                }
                        
            public boolean hasNext()
                {
                fill();
                return (current != null);
                }
                                
            public Object next()
                {
                fill();
                Object obj = current;
                current = null;
                return obj;
                }
                                
            public void remove()
                {
                throw new UnsupportedOperationException();
                }
            };
        }

    /** Returns an iterator over all the GPNodes in the subtree rooted by this GPNode,
        filtered by the provided nodesearch option (either NODSEARCH_TERMINALS, NODESEARCH_NONTERMINALS, 
        or NODESEARCH_ALL) */
    public Iterator iterator(final int nodesearch)
        {
        return iterator(new GPNodeGatherer()
            {
            public boolean test(GPNode node)
                {
                return (nodesearch==NODESEARCH_ALL ||
                    (nodesearch==NODESEARCH_TERMINALS && node.children.length==0) ||
                    (nodesearch==NODESEARCH_NONTERMINALS && node.children.length>0));
                }
            });
        }
                
    /** Returns an iterator over all the GPNodes in the subtree rooted by this GPNode. */
    public Iterator iterator()
        {
        return new Iterator()
            {
            GPNode current = GPNode.this;
            boolean used = false;
                        
            void fill()
                {
                if (used && current != null)
                    { 
                    // are we at a terminal node?
                    if (current.children == null || current.children.length == 0)
                        {
                        GPNode node = current;
                                        
                        while(true)  // look for a valid parent
                            {
                            if (node == GPNode.this)  // no parent, give up
                                {
                                current = null;
                                break;
                                }
                            else
                                {
                                GPNode par = (GPNode)(node.parent);  // this is safe because we're not the root at this point
                                                
                                if (node.argposition + 1 < par.children.length)  // go here
                                    {
                                    current = par.children[node.argposition + 1];
                                    break;
                                    }
                                else  // find another parent
                                    {
                                    node = par;
                                    }
                                }
                            }
                        }
                    else  // go down to first child
                        {
                        current = current.children[0];
                        }
                    used = false;
                    }
                }
                                
            public boolean hasNext()
                {
                fill();
                return (current != null);
                }
                                
            public Object next()
                {
                fill();
                used = true;
                return current;
                }
                                
            public void remove()
                {
                throw new UnsupportedOperationException();
                }
            };
        }

    /** Returns the p'th node, constrained by nodesearch,
        in the subtree for which this GPNode is root.
        Use numNodes(nodesearch) to determine the total number.  
        g.test(...) is used as the constraining predicate.
        p ranges from 0 to this number minus 1. O(n). The
        resultant node is returned in <i>g</i>.*/
    public GPNode nodeInPosition(int p, GPNodeGatherer g)
        {
        nodeInPosition(p, g, NODESEARCH_CUSTOM);
        return g.node;
        }

    /** Returns the p'th node, constrained by nodesearch,
        in the subtree for which this GPNode is root.
        Use numNodes(nodesearch) to determine the total number.  
        g.test(...) is used as the constraining predicate.
        p ranges from 0 to this number minus 1. O(n). The
        resultant node is returned in <i>g</i>.*/
    public GPNode nodeInPosition(int p, int nodesearch)
        {
        GPNodeGatherer g = new GPNodeGatherer() { public boolean test(GPNode node) { return true; } };
        nodeInPosition(p, g , nodesearch);
        return g.node;
        }

    /* Returns the p'th node, constrained by nodesearch,
       in the subtree for which this GPNode is root.
       Use numNodes(nodesearch) to determine the total number.  Or if
       you used numNodes(g), then when
       nodesearch == NODESEARCH_CUSTOM, g.test(...) is used
       as the constraining predicate.
       p ranges from 0 to this number minus 1. O(n). The
       resultant node is returned in <i>g</i>.*/
    int nodeInPosition(int p, final GPNodeGatherer g, final int nodesearch)
        {
        // am I of the type I'm looking for?
        if (nodesearch==NODESEARCH_ALL ||
            (nodesearch==NODESEARCH_TERMINALS && children.length==0) ||
            (nodesearch==NODESEARCH_NONTERMINALS && children.length>0) ||
            (nodesearch==NODESEARCH_CUSTOM && g.test(this)))
            {
            // is the count now at 0?  Is it me?
            if (p==0)
                {
                g.node = this; 
                return -1; // found it
                }
            // if it's not me, drop the count by 1
            else p--;
            }
        
        // regardless, check my children if I've not returned by now
        for(int x=0;x<children.length;x++)
            {
            p = children[x].nodeInPosition(p,g,nodesearch);
            if (p==-1) return -1; // found it
            }
        return p;
        }

    /** Returns the root ancestor of this node.  O(ln n) average case,
        O(n) worst case. */

    public GPNodeParent rootParent()
        {

        // -- new code, no need for recursion
        GPNodeParent cparent = this;
        while(cparent!=null && cparent instanceof GPNode)
            cparent = ((GPNode)(cparent)).parent;
        return cparent; 
        }

    /** Returns true if the subtree rooted at this node contains subnode.  O(n). */
    public boolean contains(final GPNode subnode)
        {
        if (subnode==this) return true;
        for(int x=0;x<children.length;x++)
            if (children[x].contains(subnode)) return true;
        return false;
        }


    /** Starts a node in a new life immediately after it has been cloned.
        The default version of this function does nothing.  The purpose of
        this function is to give ERCs a chance to set themselves to a new
        random value after they've been cloned from the prototype.
        You should not assume that the node is properly connected to other
        nodes in the tree at the point this method is called. */

    public void resetNode(final EvolutionState state, final int thread) { }

    /** A convenience function for identifying a GPNode in an error message */
    public String errorInfo() { return "GPNode " + toString() + " in the function set for tree " + ((GPTree)(rootParent())).treeNumber(); }


    public GPNode lightClone()
        { 
        try
            {
            GPNode obj = (GPNode)(super.clone());
            int len = children.length;
            if (len == 0) obj.children = children;  // we'll share arrays -- probably just using GPNodeConstraints.zeroChildren anyway
            else obj.children = new GPNode[len];
            return obj;
            }
        catch (CloneNotSupportedException e)
            { throw new InternalError(); } // never happens
        }

    /** Deep-clones the tree rooted at this node, and returns the entire
        copied tree.  The result has everything set except for the root
        node's parent and argposition.  This method is identical to
        cloneReplacing for historical reasons, except that it returns
        the object as an Object, not a GPNode. */    
 
    public Object clone()
        { 
        GPNode newnode = (GPNode)(lightClone());
        for(int x=0;x<children.length;x++)
            {
            newnode.children[x] = (GPNode)(children[x].cloneReplacing()); 
            // if you think about it, the following CAN'T be implemented by
            // the children's clone method.  So it's set here.
            newnode.children[x].parent = newnode;
            newnode.children[x].argposition = (byte)x;
            }
        return newnode;
        }

    /** Deep-clones the tree rooted at this node, and returns the entire
        copied tree.  The result has everything set except for the root
        node's parent and argposition.  This method is identical to
        cloneReplacing for historical reasons, except that it returns
        the object as a GPNode, not an Object. 
        @deprecated use clone() instead.
    */    
 
    public final GPNode cloneReplacing() 
        {
        return (GPNode)clone();
        }


    /** Deep-clones the tree rooted at this node, and returns the entire
        copied tree.  If the node oldSubtree is located somewhere in this
        tree, then its subtree is replaced with a deep-cloned copy of
        newSubtree.  The result has everything set except for the root
        node's parent and argposition. */
 
    public final GPNode cloneReplacing(final GPNode newSubtree, final GPNode oldSubtree) 
        {
        if (this==oldSubtree)
            return newSubtree.cloneReplacing();
        else
            {
            GPNode newnode = (GPNode)(lightClone());
            for(int x=0;x<children.length;x++)
                {
                newnode.children[x] = (GPNode)(children[x].cloneReplacing(newSubtree,oldSubtree)); 
                // if you think about it, the following CAN'T be implemented by
                // the children's clone method.  So it's set here.
                newnode.children[x].parent = newnode;
                newnode.children[x].argposition = (byte)x;
                }
            return newnode;     
            }
        }



    /** Deep-clones the tree rooted at this node, and returns the entire
        copied tree.  If the node oldSubtree is located somewhere in this
        tree, then its subtree is replaced with
        newSubtree (<i>not</i> a copy of newSubtree).  
        The result has everything set except for the root
        node's parent and argposition. */
 
    public final GPNode cloneReplacingNoSubclone(final GPNode newSubtree, final GPNode oldSubtree) 
        {
        if (this==oldSubtree)
            {
            return newSubtree;
            }
        else
            {
            GPNode newnode = (GPNode)(lightClone());
            for(int x=0;x<children.length;x++)
                {
                newnode.children[x] = (GPNode)(children[x].cloneReplacingNoSubclone(newSubtree,oldSubtree)); 
                // if you think about it, the following CAN'T be implemented by
                // the children's clone method.  So it's set here.
                newnode.children[x].parent = newnode;
                newnode.children[x].argposition = (byte)x;
                }
            return newnode;     
            }
        }






    /** Deep-clones the tree rooted at this node, and returns the entire
        copied tree.  If a node in oldSubtrees is located somewhere in this
        tree, then its subtree is replaced with a deep-cloned copy of the
        subtree rooted at its equivalent number in 
        newSubtrees.  The result has everything set except for the root
        node's parent and argposition. */
 
    public final GPNode cloneReplacing(final GPNode[] newSubtrees, final GPNode[] oldSubtrees) 
        {
        // am I a candidate?
        int candidate = -1;
        for(int x=0;x<oldSubtrees.length;x++)
            if (this==oldSubtrees[x]) { candidate=x; break; }

        if (candidate >= 0)
            return newSubtrees[candidate].cloneReplacing(newSubtrees,oldSubtrees);
        else
            {
            GPNode newnode = (GPNode)(lightClone());
            for(int x=0;x<children.length;x++)
                {
                newnode.children[x] = (GPNode)(children[x].cloneReplacing(newSubtrees,oldSubtrees)); 
                // if you think about it, the following CAN'T be implemented by
                // the children's clone method.  So it's set here.
                newnode.children[x].parent = newnode;
                newnode.children[x].argposition = (byte)x;
                }
            return newnode;     
            }
        }


    
    /** Clones a new subtree, but with the single node oldNode 
        (which may or may not be in the subtree) 
        replaced with a newNode (not a clone of newNode).  
        These nodes should be
        type-compatible both in argument and return types, and should have
        the same number of arguments obviously.  This function will <i>not</i>
        check for this, and if they are not the result is undefined. */


    public final GPNode cloneReplacingAtomic(final GPNode newNode, final GPNode oldNode) 
        {
        int numArgs;
        GPNode curnode;
        if (this==oldNode)
            {
            numArgs = Math.max(newNode.children.length,children.length);
            curnode = newNode;
            }
        else
            {
            numArgs = children.length;
            curnode = (GPNode)lightClone();
            }

        // populate

        for(int x=0;x<numArgs;x++)
            {
            curnode.children[x] = (GPNode)(children[x].cloneReplacingAtomic(newNode,oldNode)); 
            // if you think about it, the following CAN'T be implemented by
            // the children's clone method.  So it's set here.
            curnode.children[x].parent = curnode;
            curnode.children[x].argposition = (byte)x;
            }
        return curnode;
        }





    /** Clones a new subtree, but with each node in oldNodes[] respectively
        (which may or may not be in the subtree) replaced with
        the equivalent
        nodes in newNodes[] (and not clones).  
        The length of oldNodes[] and newNodes[] should
        be the same of course.  These nodes should be
        type-compatible both in argument and return types, and should have
        the same number of arguments obviously.  This function will <i>not</i>
        check for this, and if they are not the result is undefined. */


    public final GPNode cloneReplacingAtomic(final GPNode[] newNodes, final GPNode[] oldNodes) 
        {
        int numArgs;
        GPNode curnode;
        int found = -1;
        
        for(int x=0;x<newNodes.length;x++)
            {
            if (this==oldNodes[x]) { found=x; break; }
            }

        if (found > -1)
            {
            numArgs = Math.max(newNodes[found].children.length,
                children.length);
            curnode = newNodes[found];
            }
        else
            {
            numArgs = children.length;
            curnode = (GPNode)lightClone();
            }

        // populate

        for(int x=0;x<numArgs;x++)
            {
            curnode.children[x] = (GPNode)(children[x].cloneReplacingAtomic(newNodes,oldNodes)); 
            // if you think about it, the following CAN'T be implemented by
            // the children's clone method.  So it's set here.
            curnode.children[x].parent = curnode;
            curnode.children[x].argposition = (byte)x;
            }
        return curnode;
        }





    /** Replaces the node with another node in its position in the tree. 
        newNode should already have been cloned and ready to go.
        We presume that the other node is type-compatible and
        of the same arity (these things aren't checked).  */
        
    public final void replaceWith(final GPNode newNode)
        {
        // copy the parent and argposition
        newNode.parent = parent;
        newNode.argposition = argposition;
        
        // replace the parent pointer
        if (parent instanceof GPNode)
            ((GPNode)(parent)).children[argposition] = newNode;
        else
            ((GPTree)(parent)).child = newNode;
            
        // replace the child pointers
        for(byte x = 0;x<children.length;x++)
            {
            newNode.children[x] = children[x];
            newNode.children[x].parent = newNode;
            newNode.children[x].argposition = x;
            }
        }
    
    /** Returns true if I and the provided node are the same kind of
        node -- that is, we could have both been cloned() and reset() from
        the same prototype node.  The default form of this function returns
        true if I and the node have the same class, the same length children
        array, and the same constraints.  You may wish to override this in
        certain circumstances.   Here's an example of how nodeEquivalentTo(node)
        differs from nodeEquals(node): two ERCs, both of
        the same class, but one holding '1.23' and the other holding '2.45', which
        came from the same prototype node in the same function set.
        They should NOT be nodeEquals(...) but *should* be nodeEquivalent(...). */
    public boolean nodeEquivalentTo(GPNode node)
        {
        return (this.getClass().equals(node.getClass()) && 
            children.length == node.children.length &&
            constraints == node.constraints);
        }


    /** Returns a hashcode usually associated with all nodes that are 
        equal to you (using nodeEquals(...)).  The default form
        of this method returns the hashcode of the node's class.
        ERCs in particular probably will want to override this method.
    */
    public int nodeHashCode()
        {
        return (this.getClass().hashCode());
        }

    /** Returns a hashcode associated with all the nodes in the tree.  
        The default version adds the hash of the node plus its child
        trees, rotated one-off each time, which seems reasonable. */
    public int rootedTreeHashCode()
        {
        int hash = nodeHashCode();

        for(int x=0;x<children.length;x++)
            // rotate hash and XOR
            hash =
                (hash << 1 | hash >>> 31 ) ^
                children[x].rootedTreeHashCode();
        return hash;
        }

    /** Returns true if I am the "genetically" identical to this node, and our
        children arrays are the same length, though
        we may have different parents and children.  The default form
        of this method simply calls the much weaker nodeEquivalentTo(node).  
        You may need to override this to perform exact comparisons, if you're
        an ERC, ADF, or ADM for example.  Here's an example of how nodeEquivalentTo(node)
        differs from nodeEquals(node): two ERCs, both of
        the same class, but one holding '1.23' and the other holding '2.45', which
        came from the same prototype node in the same function set.
        They should NOT be nodeEquals(...) but *should* be nodeEquivalent(...).  */
    public boolean nodeEquals(final GPNode node)
        {
        return nodeEquivalentTo(node);
        }

    /** Returns true if the two rooted trees are "genetically" equal, though
        they may have different parents.  O(n). */
    public boolean rootedTreeEquals(final GPNode node)
        {
        if (!nodeEquals(node)) return false;
        for (int x=0;x<children.length;x++)
            if (!(children[x].rootedTreeEquals(node.children[x])))
                return false;
        return true;
        }

    /** Prints out a human-readable and Lisp-like atom for the node, 
        and returns the number of bytes in the string that you sent
        to the log (use print(),
        not println()).  The default version gets the atom from
        toStringForHumans(). 
    */
    public int printNodeForHumans(final EvolutionState state,
        final int log)
        {
        return printNodeForHumans(state, log, Output.V_VERBOSE);
        }

    /** Prints out a human-readable and Lisp-like atom for the node, 
        and returns the number of bytes in the string that you sent
        to the log (use print(),
        not println()).  The default version gets the atom from
        toStringForHumans(). 
        @deprecated Verbosity no longer has an effect. 
    */
    public int printNodeForHumans(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        String n = toStringForHumans();
        state.output.print(n,log);
        return n.length();
        }


    /** Prints out a COMPUTER-readable and Lisp-like atom for the node, which
        is also suitable for readNode to read, and returns
        the number of bytes in the string that you sent to the log (use print(),
        not println()).  The default version gets the atom from toString().
        O(1). 
    */
    public int printNode(final EvolutionState state, final int log)
        {
        printNode(state, log, Output.V_VERBOSE);
        String n = toString();
        return n.length();
        }

    /** Prints out a COMPUTER-readable and Lisp-like atom for the node, which
        is also suitable for readNode to read, and returns
        the number of bytes in the string that you sent to the log (use print(),
        not println()).  The default version gets the atom from toString().
        O(1). 
        @deprecated Verbosity no longer has an effect. 
    */
    public int printNode(final EvolutionState state, final int log, 
        final int verbosity)
        {
        String n = toString();
        state.output.print(n,log);
        return n.length();
        }


    /** Prints out a COMPUTER-readable and Lisp-like atom for the node, which
        is also suitable for readNode to read, and returns
        the number of bytes in the string that you sent to the log (use print(),
        not println()).  The default version gets the atom from toString().
        O(1). */

    public int printNode(final EvolutionState state,
        final PrintWriter writer)
        {
        String n = toString();
        writer.print(n);
        return n.length();
        }
                
    /** Returns a Lisp-like atom for the node and any nodes of the same class.
        This will almost always be identical to the result of toString() (and the default
        does exactly this), but for ERCs it'll be different: toString will include the
        encoded constant data, whereas name() will not include this information and will
        be the same for all ERCs of this type.  If two nodes are nodeEquivalentTo(...)
        each other, then they will have the same name().  If two nodes are nodeEquals(...)
        each other, then they will have the same toString().  */
                
    public String name() { return toString(); }

    /** Returns a Lisp-like atom for the node which can be read in again by computer.
        If you need to encode an integer or a float or whatever for some reason
        (perhaps if it's an ERC), you should use the ec.util.Code library.  */

    public abstract String toString();

    /** Returns a Lisp-like atom for the node which is intended for human
        consumption, and not to be read in again.  The default version
        just calls toString(). */

    public String toStringForHumans() { return toString(); }

    /** Returns a description of the node that can make it easy to identify
        in error messages (by default, at least its name and the tree it's found in).
        It's okay if this is a reasonably expensive procedure -- it won't be called
        a lot.  */

    public String toStringForError() 
        {
        GPTree rootp = (GPTree)rootParent();
        if (rootp!=null)
            {
            int tnum = ((GPTree)(rootParent())).treeNumber();
            return toString() + (tnum == GPTree.NO_TREENUM ? "" : " in tree " + tnum);
            }
        else return toString();
        }

    /** Produces the Graphviz code for a Graphviz tree of the subtree rooted at this node.
        For this to work, the output of toString() must not contain a double-quote. 
        Note that this isn't particularly efficient and should only be used to generate
        occasional trees for display, not for storing individuals or sending them over networks. */
    public String makeGraphvizTree()
        {
        return "digraph g {\ngraph [ordering=out];\nnode [shape=rectangle];\n" + makeGraphvizSubtree("n") + "}\n";
        }
    
    /** Produces the inner code for a graphviz subtree.  Called from makeGraphvizTree(). 
        Note that this isn't particularly efficient and should only be used to generate
        occasional trees for display, not for storing individuals or sending them over networks. */
    protected String makeGraphvizSubtree(String prefix)
        {
        String body = prefix + "[label = \"" + toStringForHumans() + "\"];\n";
        for(int x = 0; x < children.length; x++)
            {
            String newprefix;
            if (x < 10) newprefix = prefix + x;
            else newprefix = prefix + "n" + x;  // to distinguish it
            
            body = body + children[x].makeGraphvizSubtree(newprefix);
            body = body + prefix + " -> " + newprefix + ";\n";
            }
        return body;
        }

    /** Produces the LaTeX code for a LaTeX tree of the subtree rooted at this node, using the <tt>epic</tt>
        and <tt>fancybox</tt> packages, as described in sections 10.5.2 (page 307) 
        and 10.1.3 (page 278) of <i>The LaTeX Companion</i>, respectively.  For this to
        work, the output of toStringForHumans() must not contain any weird latex characters, notably { or } or % or \,
        unless you know what you're doing. See the documentation for ec.gp.GPTree for information
        on how to take this code snippet and insert it into your LaTeX file. 
        Note that this isn't particularly efficient and should only be used to generate
        occasional trees for display, not for storing individuals or sending them over networks. */
    
    public String makeLatexTree()
        {
        if (children.length==0)
            return "\\gpbox{"+toStringForHumans()+"}";
            
        String s = "\\begin{bundle}{\\gpbox{"+toStringForHumans()+"}}";
        for(int x=0;x<children.length;x++)
            s = s + "\\chunk{"+children[x].makeLatexTree()+"}";
        s = s + "\\end{bundle}";
        return s;
        }
        
    /** Producess a String consisting of the tree in pseudo-C form, given that the parent already will wrap the
        expression in parentheses (or not).  In pseudo-C form, functions with one child are printed out as a(b), 
        functions with more than two children are printed out as a(b, c, d, ...), and functions with exactly two
        children are either printed as a(b, c) or in operator form as (b a c) -- for example, (b * c).  Whether
        or not to do this depends on the setting of <tt>useOperatorForm</tt>.  Additionally, terminals will be
        printed out either in variable form -- a -- or in zero-argument function form -- a() -- depending on
        the setting of <tt>printTerminalsAsVariables</tt>.
        Note that this isn't particularly efficient and should only be used to generate
        occasional trees for display, not for storing individuals or sending them over networks. 
    */
                
    public String makeCTree(boolean parentMadeParens, boolean printTerminalsAsVariables, boolean useOperatorForm)
        {
        if (children.length==0)
            return (printTerminalsAsVariables ? toStringForHumans() : toStringForHumans() + "()");
        else if (children.length==1)
            return toStringForHumans() + "(" + children[0].makeCTree(true, printTerminalsAsVariables, useOperatorForm) + ")";
        else if (children.length==2 && useOperatorForm)
            return (parentMadeParens ? "" : "(") + 
                children[0].makeCTree(false, printTerminalsAsVariables, useOperatorForm) + " " + 
                toStringForHumans() + " " + children[1].makeCTree(false, printTerminalsAsVariables, useOperatorForm) + 
                (parentMadeParens ? "" : ")");
        else
            {
            String s = toStringForHumans() + "(" + children[0].makeCTree(true, printTerminalsAsVariables, useOperatorForm);
            for(int x = 1; x < children.length;x++)
                s = s + ", " + children[x].makeCTree(true, printTerminalsAsVariables, useOperatorForm);
            return s + ")";
            }
        }

    /**
       Produces a tree for human consumption in Lisp form similar to that generated by printTreeForHumans().
       Note that this isn't particularly efficient and should only be used to generate
       occasional trees for display, not for storing individuals or sending them over networks.
    */
    public StringBuilder makeLispTree(StringBuilder buf)
        {
        if (children.length==0)
            return buf.append(toStringForHumans());
        else
            {
            buf.append("(");
            buf.append(toStringForHumans());
            for(int x=0;x<children.length;x++)
                {
                buf.append(" ");
                children[x].makeLispTree(buf);
                }
            buf.append(")");
            return buf;
            //return s + ")";
            }
        }

    public String makeLispTree()
        {
        return makeLispTree(new StringBuilder()).toString();
        }


    /** Prints out the tree on a single line, with no ending \n, in a fashion that can
        be read in later by computer. O(n).  
        You should call this method with printbytes == 0. 
    */
    
    public int printRootedTree(final EvolutionState state,
        final int log, int printbytes)
        {
        return printRootedTree(state, log, Output.V_VERBOSE, printbytes);
        }


    /** Prints out the tree on a single line, with no ending \n, in a fashion that can
        be read in later by computer. O(n).  
        You should call this method with printbytes == 0. 
        @deprecated Verbosity no longer has an effect.
    */
    
    public int printRootedTree(final EvolutionState state,
        final int log, final int verbosity,
        int printbytes)
        {
        if (children.length>0) { state.output.print(" (",verbosity,log); printbytes += 2; }
        else { state.output.print(" ",log); printbytes += 1; }

        printbytes += printNode(state,log);

        for (int x=0;x<children.length;x++)
            printbytes = children[x].printRootedTree(state,log,printbytes);
        if (children.length>0) { state.output.print(")",log); printbytes += 1; }
        return printbytes;
        }


    /** Prints out the tree on a single line, with no ending \n, in a fashion that can
        be read in later by computer. O(n).  Returns the number of bytes printed.
        You should call this method with printbytes == 0. */
    
    public int printRootedTree(final EvolutionState state, final PrintWriter writer,
        int printbytes)
        {
        if (children.length>0) { writer.print(" ("); printbytes += 2; }
        else { writer.print(" "); printbytes += 1; }

        printbytes += printNode(state,writer);

        for (int x=0;x<children.length;x++)
            printbytes = children[x].printRootedTree(state,writer,printbytes);
        if (children.length>0) { writer.print(")"); printbytes += 1; }
        return printbytes;
        }


    /** Prints out the tree in a readable Lisp-like multi-line fashion. O(n).  
        You should call this method with tablevel and printbytes == 0.  
        No ending '\n' is printed.  
    */
    
    public int printRootedTreeForHumans(final EvolutionState state, final int log,
        int tablevel, int printbytes)
        {
        return printRootedTreeForHumans(state, log, Output.V_VERBOSE, tablevel, printbytes);
        }

    /** Prints out the tree in a readable Lisp-like multi-line fashion. O(n).  
        You should call this method with tablevel and printbytes == 0.  
        No ending '\n' is printed.  
        @deprecated Verbosity no longer has an effect.
    */
    
    public int printRootedTreeForHumans(final EvolutionState state, final int log,
        final int verbosity,
        int tablevel, int printbytes)
        {
        if (printbytes>MAXPRINTBYTES)
            { 
            state.output.print("\n",log);
            tablevel++;
            printbytes = 0;
            for(int x=0;x<tablevel;x++)
                state.output.print(GPNODEPRINTTAB,log);
            }

        if (children.length>0) { state.output.print(" (",log); printbytes += 2; }
        else { state.output.print(" ",log); printbytes += 1; }

        printbytes += printNodeForHumans(state,log);

        for (int x=0;x<children.length;x++)
            printbytes = children[x].printRootedTreeForHumans(state,log,tablevel,printbytes);
        if (children.length>0) { state.output.print(")",log); printbytes += 1; }
        return printbytes;
        }


    /** Reads the node symbol,
        advancing the DecodeReturn to the first character in the string
        beyond the node symbol, and returns a new, empty GPNode of the
        appropriate class representing that symbol, else null if the
        node symbol is not of the correct type for your GPNode class. You may
        assume that initial whitespace has been eliminated.  Generally should
        be case-SENSITIVE, unlike in Lisp.  The default
        version usually works for "simple" function names, that is, not ERCs
        or other stuff where you have to encode the symbol. */
    public GPNode readNode(DecodeReturn dret) 
        {
        int len = dret.data.length();

        // get my name
        String str2 = toString();
        int len2 = str2.length();

        if (dret.pos + len2 > len)  // uh oh, not enough space
            return null;

        // check it out
        for(int x=0; x < len2 ; x++)
            if (dret.data.charAt(dret.pos + x) != str2.charAt(x))
                return null;

        // looks good!  Check to make sure that
        // the symbol's all there is
        if (dret.data.length() > dret.pos+len2)
            {
            char c = dret.data.charAt(dret.pos+len2);
            if (!Character.isWhitespace(c) &&
                c != ')' && c != '(') // uh oh
                return null;
            }

        // we're happy!
        dret.pos += len2;
        return (GPNode)lightClone();
        }


    public void writeRootedTree(final EvolutionState state,final GPType expectedType,
        final GPFunctionSet set, final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(children.length);
        boolean isTerminal = (children.length == 0);

        // identify the node
        GPNode[] gpfi = isTerminal ? 
            set.terminals[expectedType.type] : 
            set.nonterminals[expectedType.type];
        
        int index=0;
        for( /*int index=0 */; index <gpfi.length;index++)
            if ((gpfi[index].nodeEquivalentTo(this))) break;
        
        if (index==gpfi.length)  // uh oh
            state.output.fatal("No node in the function set can be found that is equivalent to the node " + this +     
                " when performing writeRootedTree(EvolutionState, GPType, GPFunctionSet, DataOutput).");
        dataOutput.writeInt(index);  // what kind of node it is
        writeNode(state,dataOutput);

        GPInitializer initializer = ((GPInitializer)state.initializer);        
        for(int x=0;x<children.length;x++)
            children[x].writeRootedTree(state,constraints(initializer).childtypes[x],set,dataOutput);
        }


    public static GPNode readRootedTree(final EvolutionState state,
        final DataInput dataInput,
        GPType expectedType,
        GPFunctionSet set,
        GPNodeParent parent,
        int argposition) throws IOException
        {
        int len = dataInput.readInt();      // num children
        int index = dataInput.readInt();    // index in function set
        
        boolean isTerminal = (len == 0);
        GPNode[] gpfi = isTerminal ? 
            set.terminals[expectedType.type] : 
            set.nonterminals[expectedType.type];

        GPNode node = ((GPNode)(gpfi[index].lightClone()));
        
        if (node.children == null || node.children.length != len)
            state.output.fatal("Mismatch in number of children (" + len + 
                ") when performing readRootedTree(...DataInput...) on " + node);
        
        node.parent = parent;
        node.argposition = (byte)argposition;
        node.readNode(state,dataInput);

        // do its children
        GPInitializer initializer = ((GPInitializer)state.initializer);        
        for(int x=0;x<node.children.length;x++)
            node.children[x] = readRootedTree(state,dataInput,node.constraints(initializer).childtypes[x],set, node, x);

        return node;
        }

    /** Override this to write any additional node-specific information to dataOutput besides: the number of arguments, 
        the specific node class, the children, and the parent.  The default version of this method does nothing. */
    public void writeNode(final EvolutionState state, final DataOutput dataOutput) throws IOException
        {
        // do nothing
        }
        
    /** Override this to read any additional node-specific information from dataInput besides: the number of arguments,
        the specific node class, the children, and the parent.  The default version of this method does nothing. */
    public void readNode(final EvolutionState state, final DataInput dataInput) throws IOException
        {
        // do nothing
        }

    /** Reads the node and its children from the form printed out by printRootedTree. */
    public static GPNode readRootedTree(int linenumber,
        DecodeReturn dret, 
        GPType expectedType,
        GPFunctionSet set,
        GPNodeParent parent,
        int argposition,
        EvolutionState state) 
        {
        final char REPLACEMENT_CHAR = '@';

        // eliminate whitespace if any
        boolean isTerminal = true;
        int len = dret.data.length();
        for(  ;  dret.pos < len && 
                  Character.isWhitespace(dret.data.charAt(dret.pos)) ; dret.pos++);
        
        // if I'm out of space, complain
        
        if (dret.pos >= len)
            state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);
         
        // if I've found a ')', complain
        if (dret.data.charAt(dret.pos) == ')')
            {
            StringBuilder sb = new StringBuilder(dret.data);
            sb.setCharAt(dret.pos,REPLACEMENT_CHAR);
            dret.data = sb.toString();
            state.output.fatal("Reading line " + linenumber + ": " + "Premature ')' which I have replaced with a '" + REPLACEMENT_CHAR + "', in tree:\n" + dret.data);
            }
        
        // determine if I'm a terminal or not
        if (dret.data.charAt(dret.pos) == '(')
            {
            isTerminal=false;
            dret.pos++;
            // strip following whitespace
            for(  ;  dret.pos < len && 
                      Character.isWhitespace(dret.data.charAt(dret.pos)) ; dret.pos++);
            }
        
        // check again if I'm out of space
        
        if (dret.pos >= len)
            state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);
        
        // check again if I found a ')'
        if (dret.data.charAt(dret.pos) == ')')
            {
            StringBuilder sb = new StringBuilder(dret.data);
            sb.setCharAt(dret.pos,REPLACEMENT_CHAR);
            dret.data = sb.toString();
            state.output.fatal("Reading line " + linenumber + ": " + "Premature ')' which I have replaced with a '" + REPLACEMENT_CHAR + "', in tree:\n" + dret.data);
            }
        
        
        // find that node!
        GPNode[] gpfi = isTerminal ? 
            set.terminals[expectedType.type] : 
            set.nonterminals[expectedType.type];
        
        GPNode node = null;
        for(int x=0;x<gpfi.length;x++)
            if ((node = gpfi[x].readNode(dret)) != null) break;
        
        // did I find one?
        
        if (node==null)
            {
            if (dret.pos!=0) 
                {
                StringBuilder sb = new StringBuilder(dret.data);
                sb.setCharAt(dret.pos,REPLACEMENT_CHAR);
                dret.data = sb.toString();
                }
            else dret.data = "" + REPLACEMENT_CHAR + dret.data;
            state.output.fatal("Reading line " + linenumber + ": " + "I came across a symbol which I could not match up with a type-valid node.\nI have replaced the position immediately before the node in question with a '" + REPLACEMENT_CHAR + "':\n" + dret.data);
            }
        
        node.parent = parent;
        node.argposition = (byte)argposition;
        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        // do its children
        for(int x=0;x<node.children.length;x++)
            node.children[x] = readRootedTree(linenumber,dret,node.constraints(initializer).childtypes[x],set,node,x,state);
        
        // if I'm not a terminal, look for a ')'
        
        if (!isTerminal)
            {
            // clear whitespace
            for(  ;  dret.pos < len && 
                      Character.isWhitespace(dret.data.charAt(dret.pos)) ; dret.pos++);
            
            if (dret.pos >= len)
                state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);
            
            if (dret.data.charAt(dret.pos) != ')')
                {
                if (dret.pos!=0) 
                    {
                    StringBuilder sb = new StringBuilder(dret.data);
                    sb.setCharAt(dret.pos,REPLACEMENT_CHAR);
                    dret.data = sb.toString();
                    }
                else dret.data = "" + REPLACEMENT_CHAR + dret.data;
                state.output.fatal("Reading line " + linenumber + ": " + "A nonterminal node has too many arguments.  I have put a '" + 
                    REPLACEMENT_CHAR + "' just before the offending argument.\n" + dret.data);
                }
            else dret.pos++;  // get rid of the ')'
            }
        
        // return the node
        return node;
        }
    

    /** Evaluates the node with the given thread, state, individual, problem, and stack.
        Your random number generator will be state.random[thread].  
        The node should, as appropriate, evaluate child nodes with these same items
        passed to eval(...).

        <p>About <b>input</b>: <tt>input</tt> is special; it is how data is passed between
        parent and child nodes.  If children "receive" data from their parent node when
        it evaluates them, they should receive this data stored in <tt>input</tt>.
        If (more likely) the parent "receives" results from its children, it should
        pass them an <tt>input</tt> object, which they'll fill out, then it should
        check this object for the returned value.

        <p>A tree is typically evaluated by dropping a GPData into the root.  When the
        root returns, the resultant <tt>input</tt> should hold the return value.

        <p>In general, you should not be creating new GPDatas.  
        If you think about it, in most conditions (excepting ADFs and ADMs) you 
        can use and reuse <tt>input</tt> for most communications purposes between
        parents and children.  

        <p>So, let's say that your GPNode function implements the boolean AND function,
        and expects its children to return return boolean values (as it does itself).
        You've implemented your GPData subclass to be, uh, <b>BooleanData</b>, which
        looks like 
        
        * <tt><pre>public class BooleanData extends GPData 
        *    {
        *    public boolean result;
        *    public GPData copyTo(GPData gpd)
        *      {
        *      ((BooleanData)gpd).result = result;
        *      }
        *    }</pre></tt>

        <p>...so, you might implement your eval(...) function as follows:

        * <tt><pre>public void eval(final EvolutionState state,
        *                     final int thread,
        *                     final GPData input,
        *                     final ADFStack stack,
        *                     final GPIndividual individual,
        *                     final Problem problem
        *    {
        *    BooleanData dat = (BooleanData)input;
        *    boolean x;
        *
        *    // evaluate the first child
        *    children[0].eval(state,thread,input,stack,individual,problem);
        *  
        *    // store away its result
        *    x = dat.result;
        *
        *    // evaluate the second child
        *    children[1].eval(state,thread,input,stack,individual,problem);
        *
        *    // return (in input) the result of the two ANDed
        *
        *    dat.result = dat.result && x;
        *    return;
        *    }
        </pre></tt>
    */
    
    public abstract void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem);
    }

