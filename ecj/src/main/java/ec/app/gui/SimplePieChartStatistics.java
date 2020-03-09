package ec.app.gui;

import ec.EvolutionState;
import ec.Fitness;
import ec.display.chart.PieChartStatistics;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;

public class SimplePieChartStatistics extends PieChartStatistics {

    private int[] seriesID;

    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        int numSubPops = state.parameters.getInt(new Parameter("pop.subpops"),null);

        seriesID = new int[numSubPops];

        for (int i = 0; i < numSubPops; ++i) {
            seriesID[i] = i; //series id identifies each sub pop
        }
    }

    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);

        for (int subPop = 0; subPop < state.population.subpops.size(); ++subPop) {
            Fitness bestFit = state.population.subpops.get(subPop).individuals.get(0).fitness;
            for (int i = 1; i < state.population.subpops.get(subPop).individuals.size(); ++i) {
                Fitness fit = state.population.subpops.get(subPop).individuals.get(i).fitness;
                if (fit.betterThan(bestFit))
                    bestFit = fit;

                //Best individual is found, make a PieChart
                makeSector(seriesID[subPop], ((DoubleVectorIndividual) state.population.subpops.get(subPop).individuals.get(i)).genome);
            }
        }
    }
}
