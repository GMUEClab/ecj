package ec.gp.ge;
import java.util.*;

/*
 * GrammarNode.java
 *
 * Created: Sun Dec  5 11:33:43 EST 2010
 * By: Houston Mooers and Sean Luke
 *
 */

/**
 * The abstract superclass of nodes used by GrammarParser to construct a parse graph to generate
 * GEIndividuals.  GrammarNode has a *head*, which typically holds the name of the node,
 * and an array of *children*, which are themselves GrammarNodes.
 *
 */

public abstract class GrammarNode implements java.io.Serializable
    {
    String head;
    // may be empty but it's not very expensive
    protected ArrayList<GrammarNode> children = new ArrayList<GrammarNode>();  

    public GrammarNode(String head)
        {
        this.head = head;
        }

    public String getHead()
        {
        return head;
        }

    public abstract String toString();
        
    /**
     * This is needed when we use a GrammarNode as a "key"
     * in hash-map, see GrammarParser.java for details.
     */
    public boolean equals(Object o)
        {
        boolean ret = true ;
        if((o instanceof GrammarNode) && head.equals(((GrammarNode)o).getHead()) 
            && (children.size() == ((GrammarNode)o).children.size()))
            {
            for(int i = 0 ; i < children.size() ; i++)
                {
                if(!children.get(i).getHead().equals(
                        ((GrammarNode)o).children.get(i).getHead()))
                    {
                    ret = false ; 
                    break;
                    }
                }
            }
        else
            ret = false ;
        return ret ;
        }

    /** As usual */
    public int hashCode()
        {
        final int prime = 7 ;
        int hash = 1 ;
        hash = prime * hash + ((head == null) ? 0 : head.hashCode());
        int tempHash = 0 ;
        if(children != null)
            for(int i = 0 ; i < children.size() ; i++)
                hash = prime * hash + children.get(i).getHead().hashCode();
        return hash ;
        }
    }
