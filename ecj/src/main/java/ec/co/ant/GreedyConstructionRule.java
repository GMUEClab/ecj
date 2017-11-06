/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.co.ConstructiveProblemForm;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;

/**
 *
 * @author Eric O. Scott
 */
public class GreedyConstructionRule implements ConstructionRule
{

    @Override
    public void setup(final EvolutionState state, final Parameter base)
    {
    }

    @Override
    public IntegerVectorIndividual constructSolution(final PheremoneMatrix pheremones, final ConstructiveProblemForm problem)
    {
        assert(pheremones != null);
        assert(problem != null);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
