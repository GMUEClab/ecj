/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Mar 6, 2005 12:48:58 PM
 * 
 * By: spaus
 */
package ec.display;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author spaus
 */
public class ControlPanel extends JPanel 
    {

    final Console console;

    final static String P_JOBFILEPREFIX = "job-file-prefix";
    
    JLabel jLabel = null;
    JTextField numGensField = null;
    JCheckBox quitOnRunCompleteCheckbox = null;  //  @jve:decl-index=0:
    JLabel jLabel1 = null;
    JTextField numJobsField = null;  //  @jve:decl-index=0:
    JLabel jLabel2 = null;
    JLabel jLabel3 = null;
    JTextField evalThreadsField = null;
    JTextField breedThreadsField = null;
    JPanel jPanel = null;
    JRadioButton seedFileRadioButton = null;
    JTextField seedFileField = null;
    JButton seedFileButton = null;  //  @jve:decl-index=0:
    JRadioButton randomSeedsRadioButton = null;
    JTable seedsTable = null;
    JScrollPane jScrollPane = null;
    JLabel jLabel6 = null;
    JCheckBox checkpointCheckBox = null;
    JPanel checkpointPanel = null;
    JLabel jLabel7 = null;
    JTextField checkpointModuloField = null;
    JLabel jLabel8 = null;
    JTextField prefixField = null;
    JLabel jLabel10 = null;

    ButtonGroup seedButtonGroup;
    JButton generateSeedsButton = null;
    JRadioButton sequentialSeedsRadioButton = null;
    JLabel jLabel5 = null;
    JTextField jobFilePrefixField = null;
    /**
     * This is the default constructor
     */
    public ControlPanel(Console console) 
        {
        super();
        this.console = console;
        initialize();
        }
    
    public void disableControls() 
        {
        breedThreadsField.setEnabled(false);
        checkpointCheckBox.setEnabled(false);
        checkpointModuloField.setEnabled(false);
        evalThreadsField.setEnabled(false);
        generateSeedsButton.setEnabled(false);
        jobFilePrefixField.setEnabled(false);
        numGensField.setEnabled(false);
        numJobsField.setEnabled(false);
        prefixField.setEnabled(false);
        quitOnRunCompleteCheckbox.setEnabled(false);
        sequentialSeedsRadioButton.setEnabled(false);
        randomSeedsRadioButton.setEnabled(false);
        seedFileRadioButton.setEnabled(false);
        seedFileButton.setEnabled(false);
        seedsTable.setEnabled(false);
        }
    
    public void enableControls() 
        {
        breedThreadsField.setEnabled(true);
        checkpointCheckBox.setEnabled(true);
        if (checkpointCheckBox.isSelected()) 
            {
            checkpointModuloField.setEnabled(true);
            prefixField.setEnabled(true);
            }
        evalThreadsField.setEnabled(true);
        jobFilePrefixField.setEnabled(true);
        numGensField.setEnabled(true);
        numJobsField.setEnabled(true);
        quitOnRunCompleteCheckbox.setEnabled(true);
        sequentialSeedsRadioButton.setEnabled(true);
        randomSeedsRadioButton.setEnabled(true);
        if (randomSeedsRadioButton.isSelected()) 
            {
            generateSeedsButton.setEnabled(true);
            }
        seedFileRadioButton.setEnabled(true);
        if (seedFileRadioButton.isSelected()) 
            {
            seedFileButton.setEnabled(true);
            }
        seedsTable.setEnabled(true);
        }

    /**
     * This method initializes this
     * 
     * @return void
     */
    void initialize() 
        {
        jLabel5 = new JLabel();
        GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
        jLabel10 = new JLabel();
        GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
        jLabel6 = new JLabel();
        jLabel3 = new JLabel();
        jLabel2 = new JLabel();
        jLabel1 = new JLabel();
        jLabel = new JLabel();
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.setSize(975, 300);
        this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridheight = 1;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jLabel.setText("# Generations:");
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.gridy = 2;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.weightx = 0.0D;
        gridBagConstraints2.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints2.ipadx = 30;
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.gridy = 6;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.gridwidth = 2;
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jLabel1.setText("# Jobs:");
        gridBagConstraints6.gridx = 2;
        gridBagConstraints6.gridy = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints6.gridwidth = 1;
        gridBagConstraints7.gridx = 1;
        gridBagConstraints7.gridy = 3;
        gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
        jLabel2.setText("# Evaluation Threads:");
        gridBagConstraints8.gridx = 1;
        gridBagConstraints8.gridy = 4;
        gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints8.gridwidth = 1;
        jLabel3.setText("# Breeder Threads:");
        gridBagConstraints9.gridx = 2;
        gridBagConstraints9.gridy = 3;
        gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints9.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints10.gridx = 2;
        gridBagConstraints10.gridy = 4;
        gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints10.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints11.gridx = 11;
        gridBagConstraints11.gridy = 0;
        gridBagConstraints11.gridwidth = 6;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints11.gridheight = 13;
        gridBagConstraints11.weightx = 1.0D;
        gridBagConstraints25.gridx = 1;
        gridBagConstraints25.gridy = 5;
        gridBagConstraints25.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints25.insets = new java.awt.Insets(0,0,0,0);
        jLabel6.setText("Verbosity:");
        gridBagConstraints26.gridx = 2;
        gridBagConstraints26.gridy = 5;
        gridBagConstraints26.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints26.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints26.weightx = 0.0D;
        gridBagConstraints26.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints26.ipadx = 30;
        gridBagConstraints35.gridx = 1;
        gridBagConstraints35.gridy = 7;
        gridBagConstraints35.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints35.gridwidth = 2;
        gridBagConstraints35.gridheight = 1;
        gridBagConstraints36.gridx = 1;
        gridBagConstraints36.gridy = 8;
        gridBagConstraints36.gridheight = 1;
        gridBagConstraints36.gridwidth = 3;
        gridBagConstraints36.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints36.ipadx = 107;
        gridBagConstraints45.gridx = 5;
        gridBagConstraints45.gridy = 8;
        gridBagConstraints45.weighty = 1.0D;
        jLabel10.setText("");
        gridBagConstraints28.gridx = 1;
        gridBagConstraints28.gridy = 1;
        gridBagConstraints28.anchor = java.awt.GridBagConstraints.WEST;
        jLabel5.setText("Job file prefix:");
        gridBagConstraints37.gridx = 2;
        gridBagConstraints37.gridy = 1;
        gridBagConstraints37.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints37.insets = new java.awt.Insets(0,5,0,0);
        gridBagConstraints37.gridwidth = 3;
        this.add(jLabel, gridBagConstraints1);
        this.add(getNumGensField(), gridBagConstraints2);
        this.add(getQuitOnRunCompleteCheckbox(), gridBagConstraints4);
        this.add(jLabel1, gridBagConstraints5);
        this.add(getNumJobsField(), gridBagConstraints6);
        this.add(jLabel2, gridBagConstraints7);
        this.add(jLabel3, gridBagConstraints8);
        this.add(getEvalThreadsField(), gridBagConstraints9);
        this.add(getBreedThreadsField(), gridBagConstraints10);
        this.add(getJPanel(), gridBagConstraints11);
        this.add(jLabel6, gridBagConstraints25);
        //this.add(getVerbosityField(), gridBagConstraints26);
        this.add(getCheckpointCheckBox(), gridBagConstraints35);
        this.add(getCheckpointPanel(), gridBagConstraints36);
        this.add(jLabel10, gridBagConstraints45);
        this.add(jLabel5, gridBagConstraints28);
        this.add(getJobFilePrefixField(), gridBagConstraints37);
        this.add(Box.createRigidArea(new Dimension(5,0)));
        }
    /**
     * This method initializes jTextField   
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getNumGensField() 
        {
        if (numGensField == null) 
            {
            numGensField = new JTextField();
            numGensField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(KeyEvent e) 
                    {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(EvolutionState.P_GENERATIONS), ((JTextField)e.getSource()).getText());
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getString(new Parameter(EvolutionState.P_GENERATIONS),null));
                        }
                    }
                });
            numGensField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    console.parameters.set(new Parameter(EvolutionState.P_GENERATIONS), ((JTextField)e.getSource()).getText());
                    resizeSeedTable();
                    }
                });
            }
        return numGensField;
        }
    /**
     * This method initializes jCheckBox    
     *      
     * @return javax.swing.JCheckBox        
     */    
    JCheckBox getQuitOnRunCompleteCheckbox() 
        {
        if (quitOnRunCompleteCheckbox == null) 
            {
            quitOnRunCompleteCheckbox = new JCheckBox();
            quitOnRunCompleteCheckbox.setText("Quit on Run Complete");
            quitOnRunCompleteCheckbox.addItemListener(new java.awt.event.ItemListener() 
                { 
                public void itemStateChanged(java.awt.event.ItemEvent e) 
                    {    
                    console.parameters.set(new Parameter(EvolutionState.P_QUITONRUNCOMPLETE),
                        "" + ((JCheckBox)e.getSource()).isSelected());
                    }
                });
            }
        return quitOnRunCompleteCheckbox;
        }
    /**
     * This method initializes jTextField1  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getNumJobsField() 
        {
        if (numJobsField == null) 
            {
            numJobsField = new JTextField();
            numJobsField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        resizeSeedTable();
                        }
                    }
                });
            
            numJobsField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    resizeSeedTable();
                    }
                });
            }
        return numJobsField;
        }
    
    public int getNumJobs() 
        {
        try { return Integer.parseInt(getNumJobsField().getText()); } 
        catch (NumberFormatException e) { return 0; }
        }

    public int getThreadCount(String text)
        {
        try
            {
            if (text.toLowerCase().trim().equals("auto"))
                return Runtime.getRuntime().availableProcessors();
            else return Integer.parseInt(text);
            }
        catch (NumberFormatException e) { return 0; }
        }


    /**
     * @throws NumberFormatException
     */
    void resizeSeedTable()
        throws NumberFormatException 
        {
        int numJobs = Integer.parseInt(numJobsField.getText());
        int breedThreads = getThreadCount(breedThreadsField.getText());
        int evalThreads = getThreadCount(evalThreadsField.getText());
        
        int numThreads = Math.max(breedThreads, evalThreads);
        
        DefaultTableModel model =(DefaultTableModel)seedsTable.getModel(); 
        model.setColumnCount(numThreads);
        String[] columnHeaders = new String[numThreads];
        for (int i = 0; i < numThreads; ++i) 
            {
            columnHeaders[i] = "Thread "+i;
            }

        model.setColumnIdentifiers(columnHeaders);
        model.setRowCount(numJobs);

        if (seedFileRadioButton.isSelected()) 
            {
            File f = new File(seedFileField.getText());
            try 
                {
                loadSeeds(f);
                } 
            catch (IOException ex) 
                {
                System.err.println(ex.getMessage());
                }
            } 
        else if (randomSeedsRadioButton.isSelected()) 
            {
            generateRandomSeeds();
            } 
        else if (sequentialSeedsRadioButton.isSelected()) 
            {
            int seed;
            int i = 0;
            try 
                {
                for (int thread = 0; thread < numThreads; ++thread) 
                    {
                    seed = console.parameters.getInt(new Parameter("seed."+thread),null);
                    for (int job = 0; job < numJobs; ++job)
                        setSeed(""+(seed+(i++)),job,thread);
                    }
                }
            catch (NumberFormatException ex) 
                {
                javax.swing.JOptionPane.showMessageDialog(null, "The seed parameter for at least one thread not a fixed number (perhaps it's set to 'time'?), so sequential seeds cannot be used.\n"
                    + "Reverting to random number seeds for all threads.", "Adjusting Seeds", 
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                randomSeedsRadioButton.setSelected(true);
                generateRandomSeeds();
                }
            }
        else
            {
            for (int job = 0; job < numJobs; ++job) 
                {
                for (int thread = 0; thread < numThreads; ++thread) 
                    {
                    setSeed(console.parameters.getString(new Parameter("seed."+thread),null),job,thread);
                    }
                }
            }
        }

    /**
     * @throws NumberFormatException
     */
    void generateRandomSeeds()
        throws NumberFormatException 
        {
        int numJobs = Integer.parseInt(numJobsField.getText());
        int evalThreads = console.parameters.getInt(new Parameter(Evolve.P_EVALTHREADS),null);
        int breedThreads = console.parameters.getInt(new Parameter(Evolve.P_BREEDTHREADS),null);
        assert(evalThreads == getThreadCount(evalThreadsField.getText()));
        assert(breedThreads == getThreadCount(breedThreadsField.getText()));
        int numThreads = Math.max(breedThreads, evalThreads);
        
        int seed = (int)(System.currentTimeMillis());
        for (int job = 0; job < numJobs; ++job) 
            {
            for (int thread = 0; thread < numThreads; ++thread) 
                {
                seed = seed+(job*Math.min(breedThreads, evalThreads))+(thread*Math.max(breedThreads, evalThreads));
                setSeed(""+seed,job,thread);
                }
            }
        }
                
    public int getSeed(int experimentNum, int threadNum) 
        {
        try { return Integer.parseInt((String)seedsTable.getValueAt(experimentNum, threadNum)); }
        catch (RuntimeException e)
            {
            javax.swing.JOptionPane.showMessageDialog(null, "Error reading from seed table. Rebuilding random number seeds.", "Adjusting Seeds", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            generateRandomSeeds();
            return Integer.valueOf((String)seedsTable.getValueAt(experimentNum, threadNum)).intValue();
            }
        }
    
    public void setSeed(String seed, int experimentNum, int threadNum) 
        {
        seedsTable.setValueAt(seed, experimentNum, threadNum);
        }
    /**
     * This method initializes jTextField2  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getEvalThreadsField() 
        {
        if (evalThreadsField == null) 
            {
            evalThreadsField = new JTextField();
            evalThreadsField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(Evolve.P_EVALTHREADS), ((JTextField)e.getSource()).getText());
                        resizeSeedTable();
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getString(new Parameter(Evolve.P_EVALTHREADS),null));
                        }
                    }
                });
            evalThreadsField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    console.parameters.set(new Parameter(Evolve.P_EVALTHREADS), ((JTextField)e.getSource()).getText());
                    resizeSeedTable();
                    }
                });
            }
        return evalThreadsField;
        }
    /**
     * This method initializes jTextField3  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getBreedThreadsField() 
        {
        if (breedThreadsField == null) 
            {
            breedThreadsField = new JTextField();
            breedThreadsField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {    
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(Evolve.P_BREEDTHREADS), ((JTextField)e.getSource()).getText());
                        resizeSeedTable();
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getString(new Parameter(Evolve.P_BREEDTHREADS),null));
                        }
                    }
                });
            
            breedThreadsField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    console.parameters.set(new Parameter(Evolve.P_BREEDTHREADS), ((JTextField)e.getSource()).getText());
                    resizeSeedTable();
                    }
                });
            }
        return breedThreadsField;
        }
    /**
     * This method initializes jPanel       
     *      
     * @return javax.swing.JPanel   
     */    
    JPanel getJPanel() 
        {
        if (jPanel == null) 
            {
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            TitledBorder titledBorder28 = javax.swing.BorderFactory.createTitledBorder(null, "Random Seed(s)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null);
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.gridy = 1;
            gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints12.gridwidth = 5;
            gridBagConstraints12.insets = new java.awt.Insets(0,20,0,0);
            gridBagConstraints12.weightx = 1.0D;
            gridBagConstraints13.gridx = 5;
            gridBagConstraints13.gridy = 1;
            gridBagConstraints13.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints13.insets = new java.awt.Insets(0,5,0,0);
            gridBagConstraints14.gridx = 0;
            gridBagConstraints14.gridy = 3;
            gridBagConstraints14.gridwidth = 1;
            gridBagConstraints14.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints19.gridwidth = 6;
            gridBagConstraints19.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.gridy = 7;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints21.gridwidth = 6;
            gridBagConstraints21.gridheight = 1;
            gridBagConstraints21.weighty = 1.0D;
            gridBagConstraints21.weightx = 1.0D;
            jPanel.setBorder(titledBorder28);
            titledBorder28.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
            gridBagConstraints81.gridx = 1;
            gridBagConstraints81.gridy = 3;
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.gridy = 2;
            gridBagConstraints15.anchor = java.awt.GridBagConstraints.WEST;
            jPanel.add(getSeedFileRadioButton(), gridBagConstraints19);
            jPanel.add(getSeedFileField(), gridBagConstraints12);
            jPanel.add(getSeedFileButton(), gridBagConstraints13);
            jPanel.add(getRandomSeedsRadioButton(), gridBagConstraints14);
            jPanel.add(getJScrollPane(), gridBagConstraints21);
            seedButtonGroup = new ButtonGroup();
            seedButtonGroup.add(getRandomSeedsRadioButton());
            seedButtonGroup.add(getSeedFileRadioButton());
            seedButtonGroup.add(getSequentialSeedsRadioButton());
            jPanel.add(getGenerateSeedsButton(), gridBagConstraints81);
            jPanel.add(getSequentialSeedsRadioButton(), gridBagConstraints15);
            }
        return jPanel;
        }
    /**
     * This method initializes jRadioButton 
     *      
     * @return javax.swing.JRadioButton     
     */    
    JRadioButton getSeedFileRadioButton() 
        {
        if (seedFileRadioButton == null) 
            {
            seedFileRadioButton = new JRadioButton();
            seedFileRadioButton.setText("Seeds from file:");
            final ControlPanel cp = this;
            seedFileRadioButton.addItemListener(new java.awt.event.ItemListener() 
                { 
                public void itemStateChanged(java.awt.event.ItemEvent e) 
                    {
                    if (e.getStateChange() == ItemEvent.SELECTED) 
                        {
                        seedFileField.setEnabled(true);
                        seedFileButton.setEnabled(true);

                        String seedFileName = seedFileField.getText();
                        File seedFile = null;
                        if ( seedFileName == null ||
                            seedFileName.length() == 0) 
                            {
                            FileDialog fileDialog = new FileDialog(ControlPanel.this.console,"Load...",FileDialog.LOAD);
                            fileDialog.setDirectory(System.getProperty("user.dir"));
                            fileDialog.setFile("*.seed");
                            fileDialog.setVisible(true);
                            String fileName = fileDialog.getFile();
                            if ( fileName != null )
                                
                                {
                                seedFile = new File(fileDialog.getDirectory(),fileName);
                                }
                            }
                        else
                            {
                            seedFile = new File(seedFileName);
                            }

                        if (seedFile != null) 
                            {
                            try 
                                {
                                cp.loadSeeds(seedFile);
                                } 
                            catch (IOException ex) 
                                {
                                System.err.println(ex.getMessage());
                                }
                            }
                        }
                    else
                        {
                        seedFileField.setEnabled(false);
                        seedFileButton.setEnabled(false);
                        }
                    }
                });
            }
        return seedFileRadioButton;
        }
    /**
     * This method initializes jTextField4  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getSeedFileField() 
        {
        if (seedFileField == null) 
            {
            seedFileField = new JTextField();
            seedFileField.setEnabled(false);
            seedFileField.setEditable(false);
            }
        return seedFileField;
        }
    /**
     * This method initializes jButton      
     *      
     * @return javax.swing.JButton  
     */    
    JButton getSeedFileButton() 
        {
        if (seedFileButton == null) 
            {
            seedFileButton = new JButton();
            seedFileButton.setText("...");
            seedFileButton.setEnabled(false);
            final ControlPanel cp = this;
            seedFileButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    File seedFile = null;
                    FileDialog fileDialog = new FileDialog(ControlPanel.this.console,"Load...",FileDialog.LOAD);
                    fileDialog.setDirectory(System.getProperty("user.dir"));
                    fileDialog.setFile("*.seed");
                    fileDialog.setVisible(true);
                    String fileName = fileDialog.getFile();
                    if ( fileName != null )
                        
                        {
                        seedFile = new File(fileDialog.getDirectory(),fileName);
                        }

                    if (seedFile != null) 
                        {
                        try 
                            {
                            cp.loadSeeds(seedFile);
                            } 
                        catch (IOException ex) 
                            {
                            System.err.println(ex.getMessage());
                            }
                        }
                    }
                });
            }
        return seedFileButton;
        }
    /**
     * This method initializes jRadioButton1        
     *      
     * @return javax.swing.JRadioButton     
     */    
    JRadioButton getRandomSeedsRadioButton() 
        {
        if (randomSeedsRadioButton == null) 
            {
            randomSeedsRadioButton = new JRadioButton();
            randomSeedsRadioButton.setText("Random Seeds");
            randomSeedsRadioButton.setSelected(true);
            randomSeedsRadioButton.addItemListener(new java.awt.event.ItemListener() 
                { 
                public void itemStateChanged(java.awt.event.ItemEvent e) 
                    {    
                    if (e.getStateChange() == ItemEvent.SELECTED) 
                        {
                        generateSeedsButton.setEnabled(true);
                        }
                    else
                        {
                        generateSeedsButton.setEnabled(false);
                        }
                    }
                });
            }
        return randomSeedsRadioButton;
        }
    /**
     * This method initializes jTable       
     *      
     * @return javax.swing.JTable   
     */    
    JTable getSeedsTable() 
        {
        if (seedsTable == null) 
            {
            seedsTable = new JTable();
            seedsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            }
        return seedsTable;
        }
    /**
     * This method initializes jScrollPane  
     *      
     * @return javax.swing.JScrollPane      
     */    
    JScrollPane getJScrollPane() 
        {
        if (jScrollPane == null) 
            {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getSeedsTable());
            jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            }
        return jScrollPane;
        }
        
    /**
     * @param panel TODO
     * @param enabled TODO
     * 
     */
    void setEnabled(JPanel panel, boolean enabled) 
        {
        Component[] components = panel.getComponents();
        for (int i = 0; i < components.length; ++i) 
            {
            components[i].setEnabled(enabled);
            }
        }
    /**
     * This method initializes jCheckBox10  
     *      
     * @return javax.swing.JCheckBox        
     */    
    JCheckBox getCheckpointCheckBox() 
        {
        if (checkpointCheckBox == null) 
            {
            checkpointCheckBox = new JCheckBox();
            checkpointCheckBox.setText("Checkpoint");
            checkpointCheckBox.addItemListener(new java.awt.event.ItemListener() 
                { 
                public void itemStateChanged(java.awt.event.ItemEvent e) 
                    {    
                    if (e.getStateChange() == ItemEvent.SELECTED) 
                        {
                        setEnabled(checkpointPanel, true);
                        }
                    else
                        {
                        setEnabled(checkpointPanel, false);
                        }
                    
                    console.parameters.set(new Parameter(EvolutionState.P_CHECKPOINT),"" + ((JCheckBox)e.getSource()).isSelected());
                    }
                });
            }
        return checkpointCheckBox;
        }
    /**
     * This method initializes jPanel2      
     *      
     * @return javax.swing.JPanel   
     */    
    JPanel getCheckpointPanel() 
        {
        if (checkpointPanel == null) 
            {
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            jLabel8 = new JLabel();
            jLabel7 = new JLabel();
            GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            checkpointPanel = new JPanel();
            checkpointPanel.setLayout(new GridBagLayout());
            jLabel7.setText("Frequency:");
            jLabel7.setEnabled(false);
            gridBagConstraints39.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints39.gridx = 1;
            gridBagConstraints39.gridy = 0;
            gridBagConstraints39.ipady = 0;
            gridBagConstraints39.ipadx = 0;
            gridBagConstraints39.weightx = 0.0D;
            gridBagConstraints39.insets = new java.awt.Insets(0,5,0,0);
            checkpointPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), "Checkpointing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            jLabel8.setText("File Prefix:");
            jLabel8.setEnabled(false);
            gridBagConstraints40.gridx = 0;
            gridBagConstraints40.gridy = 1;
            gridBagConstraints40.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints41.gridx = 1;
            gridBagConstraints41.gridy = 1;
            gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints41.insets = new java.awt.Insets(0,5,0,0);
            gridBagConstraints41.gridwidth = 2;
            gridBagConstraints41.weightx = 0.5D;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
            checkpointPanel.add(jLabel7, gridBagConstraints3);
            checkpointPanel.add(getCheckpointModuloField(), gridBagConstraints39);
            checkpointPanel.add(jLabel8, gridBagConstraints40);
            checkpointPanel.add(getPrefixField(), gridBagConstraints41);
            }
        return checkpointPanel;
        }
    /**
     * This method initializes jTextField8  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getCheckpointModuloField() 
        {
        if (checkpointModuloField == null) 
            {
            checkpointModuloField = new JTextField();
            checkpointModuloField.setPreferredSize(new java.awt.Dimension(35,20));
            checkpointModuloField.setEnabled(false);
            checkpointModuloField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {    
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(EvolutionState.P_CHECKPOINTMODULO), ((JTextField)e.getSource()).getText());
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getString(new Parameter(EvolutionState.P_CHECKPOINTMODULO),null));
                        }
                    }
                });
            
            checkpointModuloField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    console.parameters.set(new Parameter(EvolutionState.P_CHECKPOINTMODULO), ((JTextField)e.getSource()).getText());
                    }
                });
            }
        return checkpointModuloField;
        }
    /**
     * This method initializes jTextField9  
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getPrefixField() 
        {
        if (prefixField == null) 
            {
            prefixField = new JTextField();
            prefixField.setEnabled(false);
            prefixField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {    
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(EvolutionState.P_CHECKPOINTPREFIX), ((JTextField)e.getSource()).getText());
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getString(new Parameter(EvolutionState.P_CHECKPOINTPREFIX),null));
                        }
                    }
                });
            
            prefixField.addFocusListener(new FocusListener()
                {
                public void focusGained(final FocusEvent e)
                    {
                    }

                public void focusLost(final FocusEvent e)
                    {
                    console.parameters.set(new Parameter(EvolutionState.P_CHECKPOINTPREFIX), ((JTextField)e.getSource()).getText());
                    }
                });
            }
        return prefixField;
        }
    public void loadParameters() 
        {
        numGensField.setText(console.parameters.getStringWithDefault(
                new Parameter(EvolutionState.P_GENERATIONS),null,"1"));
        quitOnRunCompleteCheckbox.setSelected(console.parameters.getBoolean(new Parameter(EvolutionState.P_QUITONRUNCOMPLETE),null,true));
        evalThreadsField.setText(console.parameters.getStringWithDefault(new Parameter(Evolve.P_EVALTHREADS),null,"1"));
        breedThreadsField.setText(console.parameters.getStringWithDefault(new Parameter(Evolve.P_BREEDTHREADS),null,"1"));
        checkpointCheckBox.setSelected(console.parameters.getBoolean(new Parameter(EvolutionState.P_CHECKPOINT),null,false));
        checkpointModuloField.setText(console.parameters.getStringWithDefault(new Parameter(EvolutionState.P_CHECKPOINTMODULO),null,"10"));
        prefixField.setText(console.parameters.getStringWithDefault(new Parameter(EvolutionState.P_CHECKPOINTPREFIX),null,"gc"));
        numJobsField.setText("1");
        jobFilePrefixField.setText(console.parameters.getStringWithDefault(new Parameter(P_JOBFILEPREFIX),null,""));
        
        resizeSeedTable();
        }
    
    void loadSeeds(File f)
        throws IOException 
        {
        LineNumberReader in = null;
        try
            {
            in = new LineNumberReader(new InputStreamReader(new FileInputStream(f)));
        
            seedFileField.setText(f.getAbsolutePath());
            // whether a seed is used for a particular thread or job depends
            // upon how many of each there are.  Just read seeds, one per line, until
            // numJobs * numThreads seeds are read.  If there are not enough seeds,
            // print a warning and generate the remaining necessary.
            int numJobs = 0;
            try { numJobs = Integer.parseInt(numJobsField.getText()); }
            catch(NumberFormatException e) { }
            int evalThreads = getThreadCount(console.parameters.getString(new Parameter(Evolve.P_EVALTHREADS),null));
            int breedThreads = getThreadCount(console.parameters.getString(new Parameter(Evolve.P_BREEDTHREADS),null));
            int numThreads = Math.max(evalThreads, breedThreads);
        
            // Read seeds for threads first
            // TODO Make this more robust (i.e. ensure we're reading integers).
            int job = 0;
            int thread = 0;
            String lastSeed = null;
            for (; job < numJobs; ++job) 
                {
                String seed = null;
                for (; thread < numThreads; ++thread) 
                    {
                    seed = in.readLine();
                    if (seed != null) 
                        {
                        setSeed(seed, job, thread);
                        lastSeed = seed;
                        }
                    else                   break;
                    }
                if (seed == null)
                    break;
                thread = 0;
                }

            // Finish filling out the table with sequential numbers starting from
            // the last good seed.
            // TODO Determine if this is reasonable.  Should we instead generate
            // random seeds?  Alternatively, should we indicate this as an error
            // to the user and abort?
            if ((job)*(thread) != (numJobs)*(numThreads)) 
                {
                int seedNum = Integer.valueOf(lastSeed).intValue();
                for (;job < numJobs; ++job) 
                    {
                    for (;thread < numThreads; ++thread) 
                        {
                        String seed = ""+(++seedNum);
                        setSeed(seed,job,thread);
                        }
                    thread = 0;
                    }
                }
            }
        finally 
            {
            if (in != null) try { in.close(); } catch (IOException e) { }
            }
        }
    /**
     * This method initializes jButton      
     *      
     * @return javax.swing.JButton  
     */    
    JButton getGenerateSeedsButton() 
        {
        if (generateSeedsButton == null) 
            {
            generateSeedsButton = new JButton();
            generateSeedsButton.setText("Generate");
            generateSeedsButton.setEnabled(false);
            generateSeedsButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {    
                    generateRandomSeeds();
                    }
                });
            }
        return generateSeedsButton;
        }
    /**
     * This method initializes jRadioButton 
     *      
     * @return javax.swing.JRadioButton     
     */    
    JRadioButton getSequentialSeedsRadioButton() 
        {
        if (sequentialSeedsRadioButton == null) 
            {
            sequentialSeedsRadioButton = new JRadioButton();
            sequentialSeedsRadioButton.setText("Sequential");
            sequentialSeedsRadioButton.addItemListener(new java.awt.event.ItemListener() 
                { 
                public void itemStateChanged(java.awt.event.ItemEvent e) 
                    {    
                    if (e.getStateChange() == ItemEvent.SELECTED) 
                        {
                        resizeSeedTable();
                        }
                    }
                });
            }
        return sequentialSeedsRadioButton;
        }
    /**
     * This method initializes jTextField   
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getJobFilePrefixField() 
        {
        if (jobFilePrefixField == null) 
            {
            jobFilePrefixField = new JTextField();
            jobFilePrefixField.addKeyListener(new java.awt.event.KeyAdapter() 
                { 
                public void keyPressed(java.awt.event.KeyEvent e) 
                    {    
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                        console.parameters.set(new Parameter(P_JOBFILEPREFIX), ((JTextField)e.getSource()).getText());
                        } 
                    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                        {
                        ((JTextField)e.getSource()).setText(console.parameters.getStringWithDefault(new Parameter(P_JOBFILEPREFIX),null,""));
                        }
                    }
                });
            }
        return jobFilePrefixField;
        }
    
    public String getJobFilePrefix() 
        {
        return jobFilePrefixField.getText();
        }
    }  //  @jve:decl-index=0:visual-constraint="66,13"
