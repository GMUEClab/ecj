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

    /** The 'minimize' attribute should default to true. */
    @Test
    public void testSetup1()
    {
        final GreedyComponentSelector instance = new GreedyComponentSelector();
        instance.setup(state, BASE);
        assertTrue(instance.isMinimize());
        assertTrue(instance.repOK());
    }

    /** Set the minimize attribute. */
    @Test
    public void testSetup2()
    {
        state.parameters.set(BASE.push(GreedyComponentSelector.P_MINIMIZE), "false");
        final GreedyComponentSelector instance = new GreedyComponentSelector();
        instance.setup(state, BASE);
        assertFalse(instance.isMinimize());
        assertTrue(instance.repOK());
    }

    /** By default, select the component with the lowest cost. */
    @Test
    public void testChoose1() {
        final GreedyComponentSelector instance = new GreedyComponentSelector();
        instance.setup(state, BASE);
        final List<Component> components = new ArrayList<Component>();
        components.add(new KnapsackComponent(23, 92));
        components.add(new KnapsackComponent(31, 57));
        components.add(new KnapsackComponent(29, 49));
        final Component expResult = new KnapsackComponent(29, 49);
        final Component result = instance.choose(state, components, null, 0);
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }
    
    /** When minimize is set to false, select the component with the highest cost. */
    @Test
    public void testChoose2() {
        state.parameters.set(BASE.push(GreedyComponentSelector.P_MINIMIZE), "false");
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
