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

public abstract class GrammarNode
    {
    String head;
    protected ArrayList children = new ArrayList();  // may be empty but it's not very expensive
        
    public GrammarNode(String head)
        {
        this.head = head;
        }
                
    public String getHead() { return head; }
    }