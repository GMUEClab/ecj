/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.test;

import ec.EvolutionState;
import ec.Finisher;
import ec.util.Parameter;

/**
 *
 * @author Eric O. Scott
 */
public class StubFinisher extends Finisher
{
    @Override
    public void finishPopulation(EvolutionState state, int result)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        // Do nothing
    }
    
}
