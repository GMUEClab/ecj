/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 16, 2005 12:36:14 PM
 * 
 * By: spaus
 */
package ec.app.gui;

import ec.display.chart.*;
import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;

/**
 * @author spaus
 */
public class SimpleXYSeriesChartStatistics
    extends XYSeriesChartStatistics {

    private int[] seriesID;
    
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        int numSubPops = state.parameters.getInt(new Parameter("pop.subpops"),null);
        seriesID = new int[numSubPops];
        
        for (int i = 0; i < numSubPops; ++i) {
            seriesID[i] = addSeries("SubPop "+i);
            }
    }
    
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        
        for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
            Fitness bestFit = state.population.subpops[subPop].individuals[0].fitness;
            for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
                Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
                if (fit.betterThan(bestFit))
                    bestFit = fit;
                }

            addDataPoint(seriesID[subPop], state.generation, bestFit.fitness());
            }
    }
    }
