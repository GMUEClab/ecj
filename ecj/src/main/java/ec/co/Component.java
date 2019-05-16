/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author Eric O. Scott
 */
public abstract class Component {
    
    /** @return The heuristic value of the component (higher is better). */
    public abstract double desirability();
    
    public void writeComponent(EvolutionState state, DataOutput output) throws IOException
        {
        state.output.fatal(String.format("%s: writeComponent() is not implemented.  This method is required in order to use a %s with ECJ's distributed evaluation or island model mechanisms.", this.getClass().getSimpleName(), Component.class.getSimpleName()));
        }
    
    public Component readComponent(EvolutionState state, DataInput input) throws IOException
        {
        state.output.fatal(String.format("%s: readComponent() is not implemented.  This method is required in order to use a %s with ECJ's distributed evaluation or island model mechanisms.", this.getClass().getSimpleName(), Component.class.getSimpleName()));
        return null;
        }   
}
