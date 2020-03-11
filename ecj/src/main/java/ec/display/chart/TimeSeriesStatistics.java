package ec.display.chart;

import ec.EvolutionState;
import ec.util.Parameter;
import org.jfree.data.time.TimeSeriesCollection;

public class TimeSeriesStatistics extends XYSeriesChartStatistics {
    private static final long serialVersionUID = 1;

    public TimeSeriesCollection timeCollection;

    public void setup(EvolutionState state, Parameter base)
    {
        super.setup(state, base);

        timeCollection = new TimeSeriesCollection();

    }
}
