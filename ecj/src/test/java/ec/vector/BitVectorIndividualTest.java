/*
  Copyright 2019 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.vector;

import ec.EvolutionState;
import ec.Initializer;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Javier Hilty
 */
public class BitVectorIndividualTest
    {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;

    public BitVectorIndividualTest()
        {
        }

    @Before
    public void setUp()
        {
	state = new EvolutionState();
        state.parameters = new ParameterDatabase();
	}
    
    @Test
    public void testGenotypeToStringForHumans()
        {
	BitVectorIndividual ind = new BitVectorIndividual();
	boolean[] gen = {true,false,true,false,true};
	ind.setGenome(gen);
	assertEquals("1 0 1 0 1 ", ind.genotypeToStringForHumans());
        }
    }
