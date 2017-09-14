/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.royaltree.func;
import ec.*;
import ec.app.lid.*;
import ec.gp.*;
import ec.util.*;

/*
 * RoyalTreeNode.java
 *
 */

/**
 * @author James McDermott
 */

public abstract class RoyalTreeNode extends GPNode
    {
    public abstract char value();
    public String toString() { return "" + value(); }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // no need to do anything here
        }
    
    }
