/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.gpsemantics.func;
import ec.*;
import ec.gp.*;
import ec.util.*;

/*
 * SemanticNode.java
 *
 */

/**
 * @author James McDermott
 */

public abstract class SemanticNode extends GPNode
    {
    public String toString() { return (("" + value()) + index()); }

    public abstract char value();
    public abstract int index();
    public int expectedChildren() { return 0; } // will be overridden by J
 
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // No need to evaluate or look at children.
        }
    }
