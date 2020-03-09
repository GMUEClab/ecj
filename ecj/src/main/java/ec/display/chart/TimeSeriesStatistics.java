package ec.display.chart;

import ec.EvolutionState;
import ec.util.Parameter;
import org.jfree.data.time.TimeSeriesCollection;

public class TimeSeriesStatistics extends XYSeriesChartStatistics {

    public TimeSeriesCollection timeCollection;

    public void setup(EvolutionState state, Parameter base)
    {
        super.setup(state, base);

        timeCollection = new TimeSeriesCollection();

    }
}
