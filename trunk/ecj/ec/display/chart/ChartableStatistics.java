/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 16, 2005 12:25:57 PM
 * 
 * By: spaus
 */
package ec.display.chart;

import org.jfree.chart.JFreeChart;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;

/**
 * @author spaus
 */
public abstract class ChartableStatistics
    extends Statistics
    {
    
    public static final String P_TITLE = "title";
    public static final String P_XAXIS = "x-axis-label";
    public static final String P_YAXIS = "y-axis-label";
    
    public String title;
    public String xlabel;
    public String ylabel;
    
    public void setup(EvolutionState state, Parameter base)
        {
        super.setup(state, base);
        
        title = state.parameters.getStringWithDefault(base.push(P_TITLE),null,"Title");
        xlabel = state.parameters.getStringWithDefault(base.push(P_XAXIS),null,"x");
        ylabel = state.parameters.getStringWithDefault(base.push(P_YAXIS),null,"y");
        }
    
    public abstract JFreeChart makeChart();

    }
