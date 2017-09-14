/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.gp;
import ec.*;

/* 
 * ADM.java
 * 
 * Created: Tue Oct 26 15:29:57 1999
 * By: Sean Luke
 */

/**
 * An ADM is an ADF which doesn't evaluate its arguments beforehand, but
 * instead only evaluates them (and possibly repeatedly) when necessary
 * at runtime.  For more information, see ec.gp.ADF.
 * @see ec.gp.ADF
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class ADM extends ADF
    {
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        // prepare a context
        ADFContext c = stack.push(stack.get());
        c.prepareADM(this);
        
        // evaluate the top of the associatedTree
        individual.trees[associatedTree].child.eval(
            state,thread,input,stack,individual,problem);

        // pop the context off, and we're done!
        if (stack.pop(1) != 1)
            state.output.fatal("Stack prematurely empty for " + toStringForError());
        }
    }
