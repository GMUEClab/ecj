/*
  Copyright 2018 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective;

import ec.Individual;
import ec.vector.DoubleVectorIndividual;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Eric O. Scott
 */
public class MultiObjectiveFitnessTest
    {
    
    public MultiObjectiveFitnessTest()
        {
        }
  
    /** Get sorted front when there are no ties. */
    @Test
    public void testGetSortedParetoFront1()
        {
        final boolean[] maximize = new boolean[] { true, true, true, true };
        
        final ArrayList<Individual> expResult = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 0.286, 7.976}, maximize));
                add(createIndForPoint(new double[] { 0.379, 7.771}, maximize));
                add(createIndForPoint(new double[] { 0.504, 7.663}, maximize));
                add(createIndForPoint(new double[] { 0.634, 7.643}, maximize));
                add(createIndForPoint(new double[] { 0.635, 6.895}, maximize));
        }};
        
        final ArrayList<Individual> inds = new ArrayList<Individual>() {{
                add(expResult.get(3));
                add(expResult.get(0));
                add(expResult.get(4));
                add(expResult.get(1));
                add(expResult.get(2));
        }};
        
        final ArrayList<Individual> result = MultiObjectiveFitness.getSortedParetoFront(inds);
        assertEquals(expResult, result);
        }

    /** Get sorted front when the first objective has a tie. */
    @Test
    public void testGetSortedParetoFront2()
        {
        final boolean[] maximize = new boolean[] { true, true, true, true };
        
        final ArrayList<Individual> expResult = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 1, 4, 5, 10}, maximize));
                add(createIndForPoint(new double[] { 10, 4, 1, 40}, maximize));
                add(createIndForPoint(new double[] { 15, 3, 0.5, 49}, maximize));
                add(createIndForPoint(new double[] { 15, 4, 0.5, 30}, maximize));
        }};
        
        final ArrayList<Individual> inds = new ArrayList<Individual>() {{
                add(expResult.get(1));
                add(expResult.get(2));
                add(expResult.get(3));
                add(expResult.get(0));
        }};
        
        final ArrayList<Individual> result = MultiObjectiveFitness.getSortedParetoFront(inds);
        assertEquals(expResult, result);
        }

    /** Get sorted front when the first objective has a tie and the second objective is being minimized. */
    /*@Test
    public void testGetSortedParetoFront3()
        {
        final boolean[] maximize = new boolean[] { true, false, true, true };
        final ArrayList<Individual> inds = new ArrayList<Individual>() {{
                add(createIndForPoint(new double[] { 10, 4, 1, 40}, maximize));
                add(createIndForPoint(new double[] { 15, 3, 0.5, 49}, maximize));
                add(createIndForPoint(new double[] { 15, 4, 0.5, 30}, maximize));
                add(createIndForPoint(new double[] { 1, 4, 5, 10}, maximize));
        }};
        
        ArrayList<Individual> expResult = new ArrayList<Individual>() {{
                add(inds.get(3));
                add(inds.get(0));
                add(inds.get(2));
                add(inds.get(1));
        }};
        
        ArrayList<Individual> result = MultiObjectiveFitness.getSortedParetoFront(inds);
        assertEquals(expResult, result);
        }*/
    
    private static Individual createIndForPoint(final double[] fitnesses, final boolean[] maximize)
        {
        final DoubleVectorIndividual ind = new DoubleVectorIndividual();
        ind.genome = fitnesses; // equals() looks at the genome, so we set genome = fitness so that we can easily compare individuals
        final MultiObjectiveFitness fitness = new MultiObjectiveFitness();
        fitness.objectives = fitnesses;
        fitness.maximize = maximize;
        ind.fitness = fitness;
        return ind;
        }
    }
