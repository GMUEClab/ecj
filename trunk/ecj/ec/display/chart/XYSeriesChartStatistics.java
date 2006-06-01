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
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import ec.EvolutionState;
 import ec.util.Parameter;
 
/**
 * @author spaus
 */
 public abstract class XYSeriesChartStatistics
     extends ChartableStatistics 
     {
     
     public XYSeriesCollection seriesCollection;
      
      public void setup(EvolutionState state, Parameter base)
         {
super.setup(state, base);
    
    seriesCollection = new XYSeriesCollection();
        
        }
      
      public JFreeChart makeChart(){
JFreeChart chart = ChartFactory.createXYLineChart(this.title,this.xlabel,this.ylabel,this.seriesCollection,PlotOrientation.VERTICAL,true,false,false);
    
    return chart;
        }
      
      public int addSeries(String name)
         {
seriesCollection.addSeries(new XYSeries(name));
    return seriesCollection.getSeriesCount()-1;
        }
      
      public void addDataPoint(int seriesID, double x, double y)
         {
seriesCollection.getSeries(seriesID).add(x,y);
    }
     }
