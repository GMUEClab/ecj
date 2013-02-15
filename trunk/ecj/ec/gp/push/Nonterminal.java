
/** 
    ECJ implements Push's s-expressions as trees of nonterminals
    and terminals.  The nonterminals are all dummies -- this is the
    class in question.  Notably the nonterminals also have an arbitrary
    arity, requiring a custom tree builder (see PushBuilder).  The terminals
    are instances of Operator.java.
*/


package ec.gp.push;
import ec.gp.*;
import ec.*;

public class Nonterminal extends GPNode
    {
    public String toString() { return ""; }

    public int expectedChildren() { return -1; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // do nothing
        }
    }



