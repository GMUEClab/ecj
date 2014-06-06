package ec.gp.ge;
import java.util.*;
import ec.gp.*;

/*
 * GrammarFunctionNode.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Houston Mooers and Sean Luke
 *
 */

/**
 * A GrammarNode representing a GPNode in the GE Grammar.  The head of the GrammarFunctionNode
 * is the name of the GPNode in the grammar; and the children are various arguments to the node
 * as defined by the grammar.  These are returned  by getArgument(...) and getNumArguments().
 * The GrammarFunctionNode holds a prototypical GPNode from which clones can be made.
 *
 */

public class GrammarFunctionNode extends GrammarNode
    {
    GPNode prototype;

    /** Determines the GPNode from the function set by the name.  If there is more than
        one such node (which shouldn't be the case) then only the first such node is
        used.  Stores the prototype. */

    public GrammarFunctionNode(GPFunctionSet gpfs, String name)
        {
        super(name);
        prototype = ((GPNode[]) (gpfs.nodesByName.get(name)))[0];
        }

    public GrammarFunctionNode(String name)
        {
        super(name);
        }

    /** Adds a given argument to the node. */
    public void addArgument(GrammarNode arg)
        {
        children.add(arg);
        }

    /** Returns the number of arguments. */
    public int getNumArguments()
        {
        return children.size();
        }

    /** Returna given argument. */
    public GrammarNode getArgument(int index)
        {
        return (GrammarNode)(children.get(index));
        }

    /** Returns the prototype without cloning it first.  Be certain to clone before using. */
    public GPNode getGPNodePrototype()
        {
        return prototype;
        }

    /** A better toString() function -- khaled */
    public String toString()
        {
        Iterator i = children.iterator();
        String ret = "(" + head + (i.hasNext() ? " " : "");
        while(i.hasNext())
            ret += ((GrammarNode)(i.next())).getHead() + (i.hasNext() ? " " : "");
        return ret + ")";
        }
    }
