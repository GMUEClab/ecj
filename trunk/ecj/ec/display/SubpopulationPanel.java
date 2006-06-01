/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 14, 2005 7:39:29 PM
 * 
 * By: spaus
 */
package ec.display;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JList;

import ec.EvolutionState;
import ec.Setup;
import ec.display.portrayal.IndividualPortrayal;
import ec.display.portrayal.SimpleIndividualPortrayal;
import ec.util.ParamClassLoadException;
import ec.util.Parameter;
import ec.util.ReflectedObject;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.JTree;
/**
 * @author spaus
 */
public class SubpopulationPanel
    extends JPanel
    implements EvolutionStateListener, Setup
    {
    
    private final Console console;
    private final int subPopNum;
    private JList individualsList = null;
    private JScrollPane individualListPane = null;
    private JSplitPane subpopPane = null;
    private JSplitPane individualDisplayPane = null;
    private IndividualPortrayal portrayal = null;
    private JScrollPane inspectionPane = null;
    private JTree inspectionTree = null;
    /**
     * 
     */
    public SubpopulationPanel(Console console, int subPopNum)
        {
        super();
        this.console = console;
        this.subPopNum = subPopNum;
        
        initialize();
        }
    
    /**
     * @param isDoubleBuffered
     */
    public SubpopulationPanel(Console console, int subPopNum, boolean isDoubleBuffered)
        {
        super(isDoubleBuffered);
        this.console = console;
        this.subPopNum = subPopNum;
        
        initialize();
        }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private  void initialize()
        {
        this.setLayout(new BorderLayout());
        this.setSize(300,200);
        this.add(getSubpopPane(), java.awt.BorderLayout.CENTER);
        }
    
    /**
     * This method initializes jList    
     *  
     * @return javax.swing.JList        
     */    
    private JList getIndividualsList()
        {
        if (individualsList == null)
            {
            individualsList = new JList();
            individualsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            int size = console.parameters.getInt(new Parameter("pop.subpop."+subPopNum+".size"),null);
            DefaultListModel model = new DefaultListModel();
            for (int i = 0; i < size; ++i)
                {
                model.add(i,new Integer(i));
                }
            individualsList.setModel(model);
            individualsList.addListSelectionListener(new ListSelectionListener()
                {
                public void valueChanged(ListSelectionEvent evt)
                    {
                    if (evt.getValueIsAdjusting() == false)
                        {
                        JList source = (JList)evt.getSource();
                        int idx = source.getSelectedIndex();
                        inspectionTree.setModel(new ReflectedObject(console.state.population.subpops[subPopNum].individuals[idx]));
                        portrayal.portrayIndividual(console.state,console.state.population.subpops[subPopNum].individuals[idx]);
                        }
                    }
                });
            }
        return individualsList;
        }
    
    public void postEvolution(EvolutionStateEvent evt)
        {
        int idx = individualsList.getSelectedIndex();
        if (idx >= 0)
            {
            inspectionTree.setModel(new ReflectedObject(console.state.population.subpops[subPopNum].individuals[idx]));
            portrayal.portrayIndividual(console.state,console.state.population.subpops[subPopNum].individuals[idx]);
            }
        }
    /**
     * This method initializes jScrollPane      
     *  
     * @return javax.swing.JScrollPane  
     */    
    private JScrollPane getIndividualListPane()
        {
        if (individualListPane == null)
            {
            individualListPane = new JScrollPane();
            individualListPane.setViewportView(getIndividualsList());
            individualListPane.setPreferredSize(new java.awt.Dimension(75,131));
            }
        return individualListPane;
        }
    /**
     * This method initializes jSplitPane       
     *  
     * @return javax.swing.JSplitPane   
     */    
    private JSplitPane getSubpopPane()
        {
        if (subpopPane == null)
            {
            subpopPane = new JSplitPane();
            subpopPane.setLeftComponent(getIndividualListPane());
            subpopPane.setRightComponent(getIndividualDisplayPane());
            subpopPane.setResizeWeight(0.0D);
            subpopPane.setDividerLocation(100);
            }
        return subpopPane;
        }
    
    /**
     * This method initializes jSplitPane1  
     *      
     * @return javax.swing.JSplitPane       
     */    
    private JSplitPane getIndividualDisplayPane()
        {
        if (individualDisplayPane == null)
            {
            individualDisplayPane = new JSplitPane();
            individualDisplayPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            individualDisplayPane.setTopComponent(getInspectionPane());
            individualDisplayPane.setResizeWeight(0.5D);
            }
        return individualDisplayPane;
        }

    public void setup(EvolutionState state, Parameter base)
        {
        try
            {
            portrayal = (IndividualPortrayal)state.parameters.getInstanceForParameter(base.push("portrayal"),null,IndividualPortrayal.class);
            }
        catch (ParamClassLoadException ex)
            {
            // default to SimpleIndividualPortrayal
            portrayal = new SimpleIndividualPortrayal();
            }
        portrayal.setup(state, base);
        individualDisplayPane.setBottomComponent(new JScrollPane((JComponent)portrayal));
        }
    /**
     * This method initializes jScrollPane  
     *      
     * @return javax.swing.JScrollPane      
     */    
    private JScrollPane getInspectionPane()
        {
        if (inspectionPane == null)
            {
            inspectionPane = new JScrollPane();
            inspectionPane.setViewportView(getInspectionTree());
            }
        return inspectionPane;
        }
    /**
     * This method initializes jTree        
     *      
     * @return javax.swing.JTree    
     */    
    private JTree getInspectionTree()
        {
        if (inspectionTree == null)
            {
            Object[] emptyTreeModel = new Object[0];
            inspectionTree = new JTree(emptyTreeModel);
            }
        return inspectionTree;
        }
    }  //  @jve:decl-index=0:visual-constraint="423,73"
