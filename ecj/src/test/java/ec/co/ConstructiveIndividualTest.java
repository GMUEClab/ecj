/*
  Copyright 2019 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.knapsack.KnapsackComponent;
import ec.app.knapsack.KnapsackProblem;
import ec.simple.SimpleEvaluator;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests.
 * 
 * @author Eric O. Scott
 */
public class ConstructiveIndividualTest {
    
    private EvolutionState state;
    
    public ConstructiveIndividualTest() {
    }
    
    @Before
    public void setUp() {
        state = new EvolutionState();
        state.output = Evolve.buildOutput();
        state.output.getLog(0).silent = true;
        state.output.getLog(1).silent = true;
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
        state.evaluator = new SimpleEvaluator();
        
        final Parameter base = new Parameter("problem");
        state.parameters = new ParameterDatabase();
        state.parameters.set(base.push(KnapsackProblem.P_SIZES), "5 6 7 8");
        state.parameters.set(base.push(KnapsackProblem.P_VALUES), "5 6 7 8");
        state.parameters.set(base.push(KnapsackProblem.P_KNAPSACK_SIZE), "4");
        state.evaluator.p_problem = new KnapsackProblem();
        state.evaluator.p_problem.setup(state, base);
    }

    /** Getting the component 0 should return the first component that was added.
     */
    @Test
    public void testGet1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        final Component expResult = new KnapsackComponent(5, 5);
        final Component result = instance.get(0);
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }

    /** Getting the component 2 should return the third component, in the order
     * that they were added.
     */
    @Test
    public void testGet2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        final Component expResult = new KnapsackComponent(7, 7);
        final Component result = instance.get(2);
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }

    /** Cloning and empty individual yields an empty individual. */
    @Test
    public void testClone1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        final ConstructiveIndividual expResult = new ConstructiveIndividual();
        final ConstructiveIndividual result = (ConstructiveIndividual) instance.clone();
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
        assertTrue(result.repOK());
    }

    /** Cloning a non-empty individual yields an individual with identical 
     * components */
    @Test
    public void testClone2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        final ConstructiveIndividual expResult = new ConstructiveIndividual();
        expResult.add(state, new KnapsackComponent(5, 5));
        expResult.add(state, new KnapsackComponent(6, 6));
        expResult.add(state, new KnapsackComponent(7, 7));
        final ConstructiveIndividual result = (ConstructiveIndividual) instance.clone();
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
        assertTrue(result.repOK());
    }

    /** Return all the components, in the order that they were added. */
    @Test
    public void testGetComponents1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        final List expResult = new ArrayList<Component>() {{
                add(new KnapsackComponent(5, 5));
                add(new KnapsackComponent(6, 6));
                add(new KnapsackComponent(7, 7));
        }};
        final List result = instance.getComponents();
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }

    /** Return an empty list if no components have been added. */
    @Test
    public void testGetComponents2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        final List expResult = new ArrayList();
        final List result = instance.getComponents();
        assertEquals(expResult, result);
        assertTrue(instance.repOK());
    }

    /** Replace the components with an empty list. */
    @Test
    public void testSetComponents1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        
        final List<Component> input = new ArrayList<Component>();
        instance.setComponents(state, input);
        final List<Component> expResult = new ArrayList<Component>();
        
        assertEquals(expResult, instance.getComponents());
        assertFalse(expResult.contains(new KnapsackComponent(5, 5)));
        assertFalse(expResult.contains(new KnapsackComponent(6, 6)));
        assertFalse(expResult.contains(new KnapsackComponent(7, 7)));
        assertTrue(instance.repOK());
    }
    
    /** Add all the components in a list of components. */
    @Test
    public void testSetComponents2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        final List<Component> input = new ArrayList<Component>() {{
                add(new KnapsackComponent(5, 5));
                add(new KnapsackComponent(6, 6));
                add(new KnapsackComponent(7, 7));
        }};
        instance.setComponents(state, input);
        final List<Component> expResult = new ArrayList<Component>() {{
                add(new KnapsackComponent(5, 5));
                add(new KnapsackComponent(6, 6));
                add(new KnapsackComponent(7, 7));
        }};
        assertEquals(expResult, instance.getComponents());
        assertTrue(expResult.contains(new KnapsackComponent(5, 5)));
        assertTrue(expResult.contains(new KnapsackComponent(6, 6)));
        assertTrue(expResult.contains(new KnapsackComponent(7, 7)));
        assertTrue(instance.repOK());
    }

    /** When we add a component, it should be accessible via both the get() and
     * contains() method.*/
    @Test
    public void testAdd1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        assertEquals(new KnapsackComponent(5, 5), instance.get(0));
        assertTrue(instance.contains(new KnapsackComponent(5, 5)));
        assertTrue(instance.repOK());
    }

    /** When we add a component, it should appear after the last-added 
     * component.
     */
    @Test
    public void testAdd2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        assertEquals(new KnapsackComponent(5, 5), instance.get(0));
        assertTrue(instance.contains(new KnapsackComponent(5, 5)));
        assertEquals(new KnapsackComponent(6, 6), instance.get(1));
        assertTrue(instance.contains(new KnapsackComponent(6, 6)));
        assertTrue(instance.repOK());
    }

    /** We shouldn't contain a component until after it is added. */
    @Test
    public void testContains() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        assertFalse(instance.contains(new KnapsackComponent(5, 5)));
        instance.add(state, new KnapsackComponent(5, 5));
        assertTrue(instance.contains(new KnapsackComponent(5, 5)));
        assertTrue(instance.repOK());
    }

    /** When we iterate, we should encounter components in the order that they 
     * were added. */
    @Test
    public void testIterator1() {
        final ConstructiveIndividual<KnapsackComponent> instance = new ConstructiveIndividual<KnapsackComponent>();
        final List<KnapsackComponent> input = new ArrayList<KnapsackComponent>() {{
                add(new KnapsackComponent(5, 5));
                add(new KnapsackComponent(6, 6));
                add(new KnapsackComponent(7, 7));
        }};
        instance.setComponents(state, input);
        
        int i = 0;
        for (final KnapsackComponent c : instance)
        {
            assertEquals(input.get(i), c);
            i++;
        }
        assertTrue(instance.repOK());
    }
    
    /** If we are empty, our iterator should be empty. */
    @Test
    public void testIterator2() {
        final ConstructiveIndividual<KnapsackComponent> instance = new ConstructiveIndividual<KnapsackComponent>();
        
        for (final KnapsackComponent c : instance)
            fail("Non-empty iterator for empty individual");
        assertTrue(instance.repOK());
    }

    /** A brand new instance should be empty. */
    @Test
    public void testIsEmpty1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        assertTrue(instance.isEmpty());
        assertTrue(instance.repOK());
    }

    /** We shouldn't be empty if something has been added. */
    @Test
    public void testIsEmpty2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        assertFalse(instance.isEmpty());
        assertTrue(instance.repOK());
    }

    /** If we are empty, size should be zero. */
    @Test
    public void testSize1() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        assertEquals(0, instance.size());
        assertTrue(instance.repOK());
    }

    /** Size should be 3 if we've added 3 components. */
    @Test
    public void testSize2() {
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        assertEquals(3, instance.size());
        assertTrue(instance.repOK());
    }
    
    /** Individuals with the same components should be equal and have the 
     * same hashCode. */
    @Test
    public void testEquals() {
        final ConstructiveIndividual instance1 = new ConstructiveIndividual();
        instance1.add(state, new KnapsackComponent(5, 5));
        instance1.add(state, new KnapsackComponent(6, 6));
        instance1.add(state, new KnapsackComponent(7, 7));
        final ConstructiveIndividual instance2 = new ConstructiveIndividual();
        instance2.add(state, new KnapsackComponent(5, 5));
        instance2.add(state, new KnapsackComponent(6, 6));
        instance2.add(state, new KnapsackComponent(7, 7));
        final ConstructiveIndividual instance3 = new ConstructiveIndividual();
        instance3.add(state, new KnapsackComponent(5, 5));
        
        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
        assertEquals(instance1.hashCode(), instance2.hashCode());
        assertNotEquals(instance1, instance3);
        assertNotEquals(instance3, instance1);
        assertTrue(instance1.repOK());
        assertTrue(instance2.repOK());
        assertTrue(instance3.repOK());
    }

    /** If we write out a genotype of an empty individual and read it back in, 
     * we should get an empty individual. */
    @Test
    public void testWriteGenotype1() throws Exception {
        final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        final DataOutput dataOutput = new DataOutputStream(arrayStream);
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.writeGenotype(state, dataOutput);
        
        final DataInput dataInput = new DataInputStream(new ByteArrayInputStream(arrayStream.toByteArray()));
        
        final ConstructiveIndividual result = new ConstructiveIndividual();
        result.readGenotype(state, dataInput);
        
        assertEquals(instance, result);
        assertTrue(result.isEmpty());
        assertTrue(instance.repOK());
        assertTrue(result.repOK());
    }
    
    /** If we write out a genotype of an individual and read it back in, 
     * we should get an equivalent individual. */
    @Test
    public void testWriteGenotype2() throws Exception {
        final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        final DataOutput dataOutput = new DataOutputStream(arrayStream);
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        instance.writeGenotype(state, dataOutput);
        
        final DataInput dataInput = new DataInputStream(new ByteArrayInputStream(arrayStream.toByteArray()));
        
        final ConstructiveIndividual result = new ConstructiveIndividual();
        result.readGenotype(state, dataInput);
        
        assertEquals(instance, result);
        assertEquals(3, result.size());
        assertTrue(instance.repOK());
        assertTrue(result.repOK());
    }
    
    /** If we read a genotype into an individual that already has components,
     * the original components should be discarded in favor of the new genome. */
    @Test
    public void testReadGenotype() throws Exception {
        final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        final DataOutput dataOutput = new DataOutputStream(arrayStream);
        final ConstructiveIndividual instance = new ConstructiveIndividual();
        instance.add(state, new KnapsackComponent(5, 5));
        instance.add(state, new KnapsackComponent(6, 6));
        instance.add(state, new KnapsackComponent(7, 7));
        instance.writeGenotype(state, dataOutput);
        
        final DataInput dataInput = new DataInputStream(new ByteArrayInputStream(arrayStream.toByteArray()));
        
        final ConstructiveIndividual result = new ConstructiveIndividual();
        result.add(state, new KnapsackComponent(10, 10));
        result.add(state, new KnapsackComponent(11, 11));
        result.readGenotype(state, dataInput);
        
        assertEquals(instance, result);
        assertEquals(3, result.size());
        assertFalse(result.contains(new KnapsackComponent(10, 10)));
        assertFalse(result.contains(new KnapsackComponent(11, 11)));
        assertTrue(instance.repOK());
        assertTrue(result.repOK());
    }
}
