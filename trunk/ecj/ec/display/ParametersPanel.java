/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.display;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ec.util.Parameter;
/*
 * Created on Apr 4, 2005 8:54:38 PM
 * 
 * By: spaus
 */

/**
 * @author spaus
 */
public class ParametersPanel
    extends JPanel
    {
    
    private final Console console;
    
    private JScrollPane parameterTreeScrollPane = null;
    private JTree parameterTree = null;
    
    private JScrollPane parameterTableScrollPane = null;
    private JTable parameterTable = null;
    private JSplitPane jSplitPane = null;
    

    
    /**
     * This method initializes jScrollPane      
     *  
     * @return javax.swing.JScrollPane  
     */    
    private JScrollPane getParameterTreeScrollPane()
        {
        if (parameterTreeScrollPane == null)
            {
            parameterTreeScrollPane = new JScrollPane();
            parameterTreeScrollPane.setViewportView(getParameterTree());
            parameterTreeScrollPane.setPreferredSize(new java.awt.Dimension(150,363));
            }
        return parameterTreeScrollPane;
        }
    /**
     * This method initializes jTree    
     *  
     * @return javax.swing.JTree        
     */    
    private JTree getParameterTree()
        {
        if (parameterTree == null)
            {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            DefaultTreeModel model = new DefaultTreeModel(root);
            parameterTree = new JTree(model);
            parameterTree.setRootVisible(false);
            parameterTree.setShowsRootHandles(false);
            parameterTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
                { 
                public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                    {
                    DefaultTableModel tableModel = (DefaultTableModel)parameterTable.getModel();
                    int rows = tableModel.getRowCount();
                    for (int row = rows-1; row >= 0; --row)
                        {
                        tableModel.removeRow(row);
                        }

                    TreePath path = e.getPath();
                    Object[] oPath = path.getPath();
                    StringBuffer sBuff = new StringBuffer();
                    // Ignore the root node.
                    for (int i = 1; i < oPath.length; ++i)
                        {
                        sBuff.append(oPath[i]);
                        if (i < oPath.length-1)
                            {
                            sBuff.append('.');
                            }
                        }

                    String[] newRow =
                        {"", "", ""};
                    DefaultMutableTreeNode selected = (DefaultMutableTreeNode)oPath[oPath.length-1];
                    if (!selected.equals(oPath[0]))
                        {
                        newRow[0] = sBuff.toString();
                        Parameter param = new Parameter(newRow[0]);
                        newRow[1] = console.parameters.getString(param,null);
                        newRow[2] = "";
                        try
                            {
                            File file = console.parameters.fileFor(param);
                            if (file != null)
                                newRow[2] = file.getCanonicalPath();
                            } catch (IOException ex)
                                {
                                }
                        
                        if (newRow[1] != null)
                            {
                            tableModel.addRow(newRow);
                            }
                        }

                    if (!newRow[0].equals(""))
                        {
                        newRow[0] = newRow[0]+".";
                        }
                    
                    if(!selected.isLeaf())
                        {
                        int children = selected.getChildCount();
                        for (int ch = 0; ch < children; ++ch)
                            {
                            TreeNode child = selected.getChildAt(ch);
                            String childRow[] = new String[3];
                            childRow[0] = newRow[0]+child;
                            Parameter param = new Parameter(childRow[0]);
                            childRow[1] = console.parameters.getString(param,null);
                            childRow[2] = "";
                            try
                                {
                                File file = console.parameters.fileFor(param);
                                if (file != null)
                                    childRow[2] = file.getCanonicalPath();
                                } catch (IOException ex)
                                    {
                                    }
                            if (childRow[1] != null)
                                {
                                tableModel.addRow(childRow);
                                }
                            }
                        }
                    }
                });
            }
        return parameterTree;
        }
    /**
     * This method initializes jScrollPane1     
     *  
     * @return javax.swing.JScrollPane  
     */    
    private JScrollPane getParameterTableScrollPane()
        {
        if (parameterTableScrollPane == null)
            {
            parameterTableScrollPane = new JScrollPane();
            parameterTableScrollPane.setViewportView(getParameterTable());
            }
        return parameterTableScrollPane;
        }
    /**
     * This method initializes jTable   
     *  
     * @return javax.swing.JTable       
     */    
    private JTable getParameterTable()
        {
        if (parameterTable == null)
            {
            String[] cn =
                {"Parameter", "Value", "Source"};
            DefaultTableModel model = new DefaultTableModel(cn,0);
            model.addTableModelListener(new TableModelListener()
                {
                public void tableChanged(TableModelEvent evt)
                    {
                    if (evt.getColumn() == 1 && 
                        evt.getType() == TableModelEvent.UPDATE)
                        {

                        int row = evt.getFirstRow();
                        DefaultTableModel model = (DefaultTableModel)evt.getSource();
                        String key = (String)model.getValueAt(row,0);
                        String value = (String)model.getValueAt(row,1);
                        Parameter parameter = new Parameter(key);
                        if (!console.parameters.getString(parameter,null).equals(value))
                            {
                            System.out.println("setting parameter "+parameter+" to "+value);
                            console.parameters.set(parameter, value);
                            }
                        }
                    }
                });
            
            parameterTable = new JTable(model);
            parameterTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
            parameterTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            parameterTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            parameterTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            parameterTable.getColumnModel().getColumn(2).setPreferredWidth(450);

            final JComboBox valueEditor = new JComboBox();
//            valueEditor.setEditable(true);
            parameterTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(valueEditor));
            valueEditor.addFocusListener(new FocusAdapter()
                {
                public void focusGained(FocusEvent evt)
                    {
                    int row = parameterTable.getSelectedRow();
                    String pName = (String)parameterTable.getValueAt(row,0);
                    Set values = console.parameters.getShadowedValues(new Parameter(pName));
                    DefaultComboBoxModel model = new DefaultComboBoxModel(values.toArray());
                    valueEditor.setModel(model);
                    }
                });
            }
        
        return parameterTable;
        }
    /**
     * This method initializes jSplitPane   
     *      
     * @return javax.swing.JSplitPane       
     */    
    private JSplitPane getJSplitPane()
        {
        if (jSplitPane == null)
            {
            jSplitPane = new JSplitPane();
            jSplitPane.setLeftComponent(getParameterTreeScrollPane());
            jSplitPane.setRightComponent(getParameterTableScrollPane());
            jSplitPane.setDividerSize(5);
            jSplitPane.setDividerLocation(200);
            }
        return jSplitPane;
        }
    
    /**
     * This is the default constructor
     */
    public ParametersPanel(Console console)
        {
        super();
        initialize();
        this.console = console;
        }
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
        {
        this.setLayout(new BorderLayout());
        this.setSize(645, 321);
        this.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
        }
    
    void loadParameters()
        {
        parameterTree.setModel(console.parameters.buildTreeModel());
        parameterTree.setRootVisible(true);
        }
    
    }
