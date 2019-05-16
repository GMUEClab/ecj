/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.knapsack.KnapsackComponent;
import ec.co.Component;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class GreedyComponentSelectorTest {
    private final static Parameter BASE = new Parameter("base");
    private EvolutionState state;
    private ParameterDatabase params;
    
    public GreedyComponentSelectorTest() {
    }
    
    @Before
    public void setUp() {
        params = new ParameterDatabase();
        state = new EvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
    }

    @Test
    public void testSetup1()
    {
        final GreedyComponentSelector instance = new GreedyComponentSelector();
        instance.setup(state, BASE);
        assertTrue(instance.repOK());
    }
    
    /** Select the component with the highest desirability. */
    @Test
    public void testChoose() {
        final GreedyComponentSelector instance = new GreedyComponentSelector();
        instance.setup(state, BASE);
        final List<Component> components = new ArrayList<Component>();
        components.add(new KnapsackComponent(23, 92));
        components.add(new KnapsackComponent(31, 57));
        components.add(new KnapsackComponent(29, 49));
        final Component expResult = new KnapsackComponent(23, 92);
        final Component result = instance.choose(state, components, null, 0);
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }
    
}
