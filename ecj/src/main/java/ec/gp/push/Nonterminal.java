package ec.gp.push;
import ec.gp.*;
import ec.*;

/* 
 * Nonterminal.java
 * 
 * Created: Fri Feb 15 23:00:04 EST 2013
 * By: Sean Luke
 */
 
/**
 *
 * Nonterminal is one of two GPNodes which are used to encode Push programs in ECJ GP trees.
  
 <p>ECJ implements Push's s-expressions as trees of nonterminals
 and terminals.  The nonterminals are all dummy instances of the Nonterminal class.
 Terminals are all instances of the Terminal class.
    
 <p>The nonterminals and terminals aren't actually evaluated.  Instead, the
 tree is printed out as a lisp s-expression and sent to the Push interpreter.

 <p>The Nonterminal class can have any number of children.  When writing itself out
 via printNodeForHumans, it displays itself as "."  But when writing itself out via
 printNode, it doesn't display anything at all (which produces a proper-looking Push program).
   
 <p>You must specify a size distribution for PushBuilder.
  
 * @author Sean Luke
 * @version 1.0 
 */

/** 
    ECJ implements Push's s-expressions as trees of nonterminals
    and terminals.  The nonterminals are all dummies -- this is the
    class in question.  Notably the nonterminals also have an arbitrary
    arity, requiring a custom tree builder (see PushBuilder).  The terminals
    are instances of Terminal.java.
*/

public class Nonterminal extends GPNode
    {
    public String toString() { return "."; }   // display a "." when being printed in computer-readable fashion
    public String toStringForHumans() { return ""; }  // don't print it when being displayed
    
    // Note that expectedChildren() is not overridden, so the default is CHILDREN_UKNOWN
    // which results in arbitrary arity.

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // do nothing at all
        }
    }



