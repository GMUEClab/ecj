/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.test;

import ec.Breeder;
import ec.EvolutionState;
import ec.Population;
import ec.util.Parameter;

/**
 *
 * @author Eric O. Scott
 */
public class StubBreeder extends Breeder
{

    @Override
    public Population breedPopulation(EvolutionState state)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setup(EvolutionState state, Parameter base)
    {
        // Do nothing
    }
    
}
