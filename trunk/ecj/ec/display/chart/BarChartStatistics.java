/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 16, 2005 12:25:57 PM
 * 
 * By: spaus, Hovden
 */
 package ec.display.chart;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.DefaultCategoryDataset;
 
 import ec.EvolutionState;
 import ec.util.Parameter;
 
/**
 * @author spaus
 */
 public class BarChartStatistics
     extends ChartableStatistics
     {
     
     public DefaultCategoryDataset dataset;
      
      public void setup(EvolutionState state, Parameter base)
         {
super.setup(state, base);
    dataset = new DefaultCategoryDataset();
        
        }
      
      public JFreeChart makeChart(){
JFreeChart chart = ChartFactory.createBarChart(this.title,
                                               this.xlabel,this.ylabel, this.dataset, PlotOrientation.VERTICAL,
                                               false, true, false);
    
    return chart;
        }
      
      public void makeBar(int seriesID, double[] genes)
         {
for (int i = 0; i < genes.length; i++)
        {
dataset.setValue(genes[i], "Genome "+seriesID, String.valueOf(i));
    }
                                          }
      
     }
