package ec.display.chart;

import ec.EvolutionState;
import ec.util.Parameter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class PieChartStatistics extends ChartableStatistics {
    private static final long serialVersionUID = 1;

    public DefaultPieDataset dataset;

    public void setup(EvolutionState state, Parameter base)
    {
        super.setup(state, base);
        dataset = new DefaultPieDataset();

    }

    public JFreeChart makeChart(){
        JFreeChart chart = ChartFactory.createPieChart(this.title, this.dataset, true, true, false);

        return chart;
    }

    public void makeSector(int seriesID, double[] genes)
    {
        for (int i = 0; i < genes.length; i++)
        {
            dataset.setValue("Genome "+seriesID, genes[i]);
        }
    }
}
