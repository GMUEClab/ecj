/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 17, 2005 11:20:52 AM
 * 
 * By: spaus
 */
package ec.display;

import java.awt.Color;

import javax.swing.JTabbedPane;


import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import ec.EvolutionState;
import ec.Setup;
import ec.Statistics;
import ec.display.chart.ChartableStatistics;
import ec.display.chart.StatisticsChartPaneTab;
import ec.util.Parameter;

/**
 * @author spaus
 */
public class StatisticsChartPane
    extends JTabbedPane
    implements Setup
    {
    public int numCharts;
    
    /**
     * 
     */
    public StatisticsChartPane()
        {
        super();
        initialize();
        }
    
    /**
     * @param tabPlacement
     */
    public StatisticsChartPane(int tabPlacement)
        {
        super(tabPlacement);
        initialize();
        }
    
    /**
     * @param tabPlacement
     * @param tabLayoutPolicy
     */
    public StatisticsChartPane(int tabPlacement, int tabLayoutPolicy)
        {
        super(tabPlacement, tabLayoutPolicy);
        initialize();
        }
    
    private void createCharts(Statistics statistics)
        {
        if (statistics instanceof ChartableStatistics)
            {
            ChartableStatistics chartStats = (ChartableStatistics)statistics;
            
            JFreeChart chart = chartStats.makeChart();

            chart.setBackgroundPaint(Color.white);
            ChartPanel chartPanel = new ChartPanel(chart);
            StatisticsChartPaneTab chartPaneTab = new StatisticsChartPaneTab(chartPanel);
            this.addTab("Chart "+(numCharts++),chartPaneTab);
            }
        
        if (statistics.children != null)
            {
            for (int i = 0; i < statistics.children.length; ++i)
                createCharts(statistics.children[i]);
            }
        }
    
    /* (non-Javadoc)
     * @see ec.Setup#setup(ec.EvolutionState, ec.util.Parameter)
     */
    public void setup(EvolutionState state, Parameter base)
        {
        numCharts = 0;
        createCharts(state.statistics);
        }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private  void initialize()
        {
        this.setSize(300,200);
        this.addContainerListener(new java.awt.event.ContainerAdapter()
            { 
            public void componentRemoved(java.awt.event.ContainerEvent e)
                {    
                StatisticsChartPane pane = (StatisticsChartPane)e.getSource();
                if (pane.getTabCount() < 1)
                    {
                    pane.getParent().remove(pane);
                    }
                }
            });
        }
    }
