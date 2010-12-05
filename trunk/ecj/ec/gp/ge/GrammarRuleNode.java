package ec.gp.ge;
import java.util.*;

/*
 * GrammarRuleNode.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Houston Mooers and Sean Luke
 *
 */

/**
 * A GrammarNode representing a Rule in the GE Grammar.  The head of the GrammarRuleNode
 * is the name of the rule; and the children are the various choices.  These are returned
 * by getChoice(...) and getNumChoices().  The merge(...) method unifies this GrammarRuleNode
 * with the choices of another node.
 * 
 */

public class GrammarRuleNode extends GrammarNode 
    {      
    public GrammarRuleNode(String head)
        {
        super(head);
        }
        
    /** Adds a choice to the children of this node. */
    public void addChoice(GrammarNode choice)
        {
        children.add(choice);
        }
        
    /** Returns the current number of choices to the node. */
    public int getNumChoices() { return children.size(); }
        
    /** Returns a given choice. */
    public GrammarNode getChoice(int index) { return (GrammarNode)(children.get(index)); }

    /** Adds to this node all the choices of another node. */
    public void merge(GrammarRuleNode other)
        {
        int n = other.getNumChoices();
        for(int i = 0 ; i < n; i++)
            addChoice(other.getChoice(i));
        }

    public String toString()
        {
        String ret = "" + head + " ::= ";
        Iterator i = children.iterator();
        boolean first = true;
        while(i.hasNext())
            {
            ret = ret + (first ? "" : "| ") + ((GrammarNode)(i.next())).getHead();
            first = false;
            }
        return ret;
        }
        
    }

