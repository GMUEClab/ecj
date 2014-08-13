/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Feb 3, 2005
 *
 */
package ec.display;

import java.awt.*;
import javax.swing.*;
import ec.util.*;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.BadParameterException;
import ec.util.Checkpoint;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.ParamClassLoadException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.util.Version;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
/**
 * @author spaus
 *
 */
public class Console extends JFrame 
    {
    
    static final int DEFAULT_HEIGHT = 500;
    static final int DEFAULT_WIDTH = 975;
    ParameterDatabase parameters = null;
    EvolutionState state = null;
    Thread playThread = null;
    boolean playing = false;
    boolean paused = false;
    Object buttonLock = new Object();
    Object cleanupLock = new Object();
    int currentJob;
    final String[] clArgs;
    
    javax.swing.JPanel jContentPane = null;
    javax.swing.JMenuBar jJMenuBar = null;
    javax.swing.JMenu fileMenu = null;
    javax.swing.JMenu helpMenu = null;
    javax.swing.JMenuItem exitMenuItem = null;
    javax.swing.JMenuItem aboutMenuItem = null;
    JTabbedPane jTabbedPane = null;
    JToolBar jToolBar = null;
    JButton playButton = null;
    JButton pauseButton = null;
    JButton stopButton = null;
    
    JButton stepButton = null;
    JMenuItem loadParametersMenuItem = null;
    ParametersPanel paramPanel = null;
    ControlPanel conPanel = null;
    /**
     * @throws java.awt.HeadlessException
     */
    public Console(String[] clArgs) throws HeadlessException 
        {
        super();
        this.clArgs = clArgs;
        initialize();
        }
    
    /**
     * @param gc
     */
    public Console(GraphicsConfiguration gc, String[] clArgs) 
        {
        super(gc);
        this.clArgs = clArgs;
        initialize();
        }
    
    /**
     * @param title
     * @throws java.awt.HeadlessException
     */
    public Console(String title, String[] clArgs) throws HeadlessException 
        {
        super(title);
        this.clArgs = clArgs;
        initialize();
        }
    
    /**
     * @param title
     * @param gc
     */
    public Console(String title, GraphicsConfiguration gc, String[] clArgs) 
        {
        super(title, gc);
        this.clArgs = clArgs;
        initialize();
        }
    
    /**
     * This method initializes jTabbedPane      
     *  
     * @return javax.swing.JTabbedPane  
     */    
    JTabbedPane getJTabbedPane() 
        {
        if (jTabbedPane == null) 
            {
            jTabbedPane = new JTabbedPane();
            conPanel = new ControlPanel(this);
            conPanel.disableControls();
            jTabbedPane.add("Control",conPanel);
            paramPanel = new ParametersPanel(this);
            jTabbedPane.add("Parameters",paramPanel);
            jTabbedPane.addTab("Statistics", null, getStatisticsPane(), null);
            jTabbedPane.addTab("Inspection", null, getInspectionPane(), null);
            }
        return jTabbedPane;
        }
    
    /**
     * This method initializes jToolBar 
     *  
     * @return javax.swing.JToolBar     
     */    
    JToolBar getJToolBar() 
        {
        if (jToolBar == null) 
            {
            jToolBar = new JToolBar();
            jToolBar.add(getPlayButton());
            jToolBar.add(getStepButton());
            jToolBar.add(getPauseButton());
            jToolBar.add(getStopButton());
            }
        return jToolBar;
        }
    
    /**
     * This method initializes jButton  
     *  
     * @return javax.swing.JButton      
     */    
    JButton getPlayButton() 
        {
        if (playButton == null) 
            {
            playButton = new JButton();
            playButton.setIcon(new ImageIcon(getClass().getResource("/ec/display/Play.png")));
            playButton.setEnabled(false);
            playButton.setToolTipText("Play");
            playButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    synchronized(buttonLock) 
                        {
                        if (!playing || (playing && paused)) 
                            {
                            if (!paused) 
                                {
                                currentJob = 0;
                                spawnPlayThread(false);
                                } else 
                                {
                                resumePlayThread();
                                }
                            playButton.setEnabled(false);
                            stepButton.setEnabled(false);
                            pauseButton.setEnabled(true);
                            stopButton.setEnabled(true);
                            conPanel.disableControls();
                            paused = false;
                            playing = true;
                            }
                        }
                    }
                });
            }
        return playButton;
        }
    /**
     * This method initializes jButton1 
     *  
     * @return javax.swing.JButton      
     */    
    JButton getPauseButton() 
        {
        if (pauseButton == null) 
            {
            pauseButton = new JButton();
            pauseButton.setIcon(new ImageIcon(getClass().getResource("/ec/display/Pause.png")));
            pauseButton.setEnabled(false);
            pauseButton.setToolTipText("Pause");
            pauseButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    synchronized(buttonLock) 
                        {
                        if (playing && !paused) 
                            {
                            paused = true;
                            pausePlayThread();
                            stepButton.setEnabled(true);
                            playButton.setEnabled(true);
                            pauseButton.setEnabled(false);
                            }
                        }
                    }
                });
            }
        return pauseButton;
        }
    /**
     * This method initializes jButton2 
     *  
     * @return javax.swing.JButton      
     */    
    JButton getStopButton() 
        {
        if (stopButton == null) 
            {
            stopButton = new JButton();
            stopButton.setIcon(
                new ImageIcon(
                    getClass().getResource("/ec/display/Stop.png")));
            stopButton.setEnabled(false);
            stopButton.setToolTipText("Stop");
            stopButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    synchronized(buttonLock) 
                        {
                        if (playing) 
                            {
                            killPlayThread();
                            stopButton.setEnabled(false);
                            pauseButton.setEnabled(false);
                            stepButton.setEnabled(true);
                            playButton.setEnabled(true);
                            conPanel.enableControls();
                            paused = false;
                            playing = false;
                            }
                        }
                    }
                });
            }
        return stopButton;
        }
    /**
     * This method initializes jButton  
     *  
     * @return javax.swing.JButton      
     */    
    JButton getStepButton() 
        {
        if (stepButton == null) 
            {
            stepButton = new JButton();
            stepButton.setEnabled(false);
            stepButton.setIcon(new ImageIcon(getClass().getResource("/ec/display/Step.png")));
            stepButton.setPressedIcon(new ImageIcon(getClass().getResource("/ec/display/Stepping.png")));
            stepButton.setToolTipText("Step");
            stepButton.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    synchronized(buttonLock) 
                        {
                        paused = true;
                        setStep(true);
                        if (!playing) 
                            {
                            spawnPlayThread(false);
                            stopButton.setEnabled(true);
                            conPanel.disableControls();
                            playing = true;
                            }
                        
                        synchronized(playThread) 
                            {
                            playThread.notify();
                            }
                        }
                    }
                });
            }
        return stepButton;
        }
    /**
     * This method initializes jMenuItem        
     *  
     * @return javax.swing.JMenuItem    
     */    
    JMenuItem getLoadParametersMenuItem() 
        {
        if (loadParametersMenuItem == null) 
            {
            final String PARAMFILE_EXT = "params";
            this.getAboutMenuItem();
            loadParametersMenuItem = new JMenuItem();
            loadParametersMenuItem.setText("Load Parameters...");
            loadParametersMenuItem.addActionListener(new java.awt.event.ActionListener() 
                {
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {    
                    FileDialog fileDialog = new FileDialog(Console.this,"Open...",FileDialog.LOAD);
                    fileDialog.setDirectory(System.getProperty("user.dir"));
                    fileDialog.setFile("*."+PARAMFILE_EXT);
                    fileDialog.setVisible(true);
                    String fileName = fileDialog.getFile();
                    while (fileName != null && !fileName.endsWith("."+PARAMFILE_EXT)) 
                        {
                        JOptionPane optPane = new JOptionPane(fileDialog.getFile()+" is not a legal parameters file",JOptionPane.ERROR_MESSAGE);
                        JDialog optDialog = optPane.createDialog(Console.this,"Error!");
                        optDialog.setVisible(true);
                        fileDialog.setFile("*."+PARAMFILE_EXT);
                        fileDialog.setVisible(true);
                        fileName = fileDialog.getFile();
                        }
                    
                    if (fileName != null) 
                        {    
                        File f = new File(fileDialog.getDirectory(), fileName);
                        Console.this.loadParameters(f);
                        playButton.setEnabled(true);
                        stepButton.setEnabled(true);
                        conPanel.enableControls();
                        }
                    }
                });
            }
        return loadParametersMenuItem;
        }
    
    /**
     * This method initializes jMenuItem        
     *  
     * @return javax.swing.JMenuItem    
     */    
    JMenuItem getLoadCheckpointMenuItem() 
        {
        if (loadCheckpointMenuItem == null) 
            {
            loadCheckpointMenuItem = new JMenuItem();
            loadCheckpointMenuItem.setText("Load Checkpoint...");
            loadCheckpointMenuItem.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {    
                    JFileChooser chooser = new JFileChooser(
                        System.getProperty("user.dir"));
                    chooser.setFileFilter(new FileFilter() 
                        {
                        public boolean accept( File f )
                            
                            {
                            if ( f.isDirectory() )
                                return true;
                            
                            String extension = null;
                            String filename = f.getName();
                            int idx = filename.lastIndexOf( '.' );
                            if ( idx > 0 && idx < filename.length() - 1 )
                                
                                {
                                extension = filename.substring( idx + 1 ).toLowerCase();
                                }
                            
                            if ( extension != null )
                                
                                {
                                if ( extension.equals( "gz" ) )
                                    return true;
                                }
                            
                            return false;
                            }
                        
                        public String getDescription()
                            
                            {
                            return "Checkpoint Files";
                            }
                        });
                    int option = chooser.showOpenDialog( Console.this );
                    if ( option == JFileChooser.APPROVE_OPTION )
                        
                        {
                        File f = chooser.getSelectedFile();
                        Console.this.restoreFromCheckpoint(f);
                        playButton.setEnabled(true);
                        stepButton.setEnabled(true);
                        }
                    }
                });
            }
        return loadCheckpointMenuItem;
        }
    
    /**
     * This method initializes jTabbedPane1     
     *  
     * @return javax.swing.JTabbedPane  
     */    
    JTabbedPane getStatisticsPane() 
        {
        if (statisticsPane == null) 
            {
            statisticsPane = new JTabbedPane();
            }
        return statisticsPane;
        }
    /**
     * This method initializes jTabbedPane2     
     *  
     * @return javax.swing.JTabbedPane  
     */    
    JTabbedPane getInspectionPane() 
        {
        if (inspectionPane == null) 
            {
            inspectionPane = new JTabbedPane();
            }
        return inspectionPane;
        }
    
    /**
     * This method initializes jPanel       
     *      
     * @return javax.swing.JPanel   
     */    
    JPanel getStatusPane() 
        {
        if (statusPane == null) 
            {
            statusPane = new JPanel();
            statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.X_AXIS));
            statusPane.add(getStatusField(), null);
            }
        return statusPane;
        }
    /**
     * This method initializes jTextField   
     *      
     * @return javax.swing.JTextField       
     */    
    JTextField getStatusField() 
        {
        if (statusField == null) 
            {
            statusField = new JTextField();
            statusField.setEditable(false);
            }
        return statusField;
        }
    public static void main(String[] args) 
        {
        Console application = new Console(args);
        application.setVisible(true);
        }
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    void initialize() 
        {
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getJJMenuBar());
        this.setContentPane(getJContentPane());
        this.setJMenuBar(getJJMenuBar());
        this.setContentPane(getJContentPane());
        this.setTitle("ECJ Console");
        
        for (int i = 0; i < clArgs.length; i ++) {
            if (clArgs[i].equalsIgnoreCase("file")) {
                File file = new File(clArgs[i+1]);
                loadParameters(file);
                playButton.setEnabled(true);
                stepButton.setEnabled(true);
                conPanel.enableControls();
                }
            }
        }
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    javax.swing.JPanel getJContentPane() 
        {
        if(jContentPane == null) 
            {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getJToolBar(), java.awt.BorderLayout.NORTH);
            jContentPane.add(getStatusPane(), java.awt.BorderLayout.SOUTH);
            }
        return jContentPane;
        }
    /**
     * This method initializes jJMenuBar        
     *  
     * @return javax.swing.JMenuBar     
     */    
    javax.swing.JMenuBar getJJMenuBar() 
        {
        if (jJMenuBar == null) 
            {
            jJMenuBar = new javax.swing.JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getHelpMenu());
            }
        return jJMenuBar;
        }
    /**
     * This method initializes jMenu    
     *  
     * @return javax.swing.JMenu        
     */    
    javax.swing.JMenu getFileMenu() 
        {
        if (fileMenu == null) 
            {
            fileMenu = new javax.swing.JMenu();
            fileMenu.setText("File");
            fileMenu.add(getLoadParametersMenuItem());
            fileMenu.add(getLoadCheckpointMenuItem());
            fileMenu.add(new JSeparator());
            fileMenu.add(getExitMenuItem());
            }
        return fileMenu;
        }
    /**
     * This method initializes jMenu    
     *  
     * @return javax.swing.JMenu        
     */    
    javax.swing.JMenu getHelpMenu() 
        {
        if (helpMenu == null) 
            {
            helpMenu = new javax.swing.JMenu();
            helpMenu.setText("Help");
            helpMenu.add(getAboutMenuItem());
            }
        return helpMenu;
        }
    /**
     * This method initializes jMenuItem        
     *  
     * @return javax.swing.JMenuItem    
     */    
    javax.swing.JMenuItem getExitMenuItem() 
        {
        if (exitMenuItem == null) 
            {
            exitMenuItem = new javax.swing.JMenuItem();
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {    
                    System.exit(0);
                    }
                });
            }
        return exitMenuItem;
        }
    
    JFrame aboutFrame;
    
    /**
     * This method initializes jMenuItem        
     *  
     * @return javax.swing.JMenuItem    
     */    
    javax.swing.JMenuItem getAboutMenuItem() 
        {
        if (aboutMenuItem == null) 
            {
            aboutMenuItem = new javax.swing.JMenuItem();
            aboutMenuItem.setText("About ECJ");
            aboutMenuItem.addActionListener(new java.awt.event.ActionListener() 
                { 
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    
                    {    
                    if (aboutFrame == null)
                        
                        {
                        // construct the frame
                        aboutFrame = new JFrame("About ECJ");
                        JPanel p = new JPanel();  // 1.3.1 only has borders for JComponents, not Boxes
                        p.setBorder(BorderFactory.createEmptyBorder(25,30,30,30));
                        Box b = new Box(BoxLayout.Y_AXIS);
                        p.add(b,BorderLayout.CENTER);
                        aboutFrame.getContentPane().add(p,BorderLayout.CENTER);
                        aboutFrame.setResizable(false);
                        Font small = new Font("Dialog",0,10);

                        // start dumping in text
                        JLabel j = new JLabel("ECJ");
                        j.setFont(new Font("Serif",0,36));
                        b.add(j);
                                
                        j = new JLabel("An Evolutionary Computation System");
                        b.add(j);
                        j = new JLabel("Version " +Version.version);
                        b.add(j);
                        JLabel spacer = new JLabel(" ");
                        spacer.setFont(new Font("Dialog",0,6));
                        b.add(spacer);

                        j = new JLabel("By " + Version.author);
                        b.add(j);
                        
                        spacer = new JLabel(" ");
                        spacer.setFont(new Font("Dialog",0,6));
                        b.add(spacer);
                        
                        j = new JLabel("Contributors:");
                        b.add(j);
                        j = new JLabel("     " + Version.contributors);
                        b.add(j);
                        j = new JLabel("     " + Version.contributors2);
                        b.add(j);
                            
                        spacer = new JLabel(" ");
                        spacer.setFont(new Font("Dialog",0,6));
                        b.add(spacer);
                        
                        // can't figure out why I need a second one...
                        spacer = new JLabel(" ");
                        spacer.setFont(new Font("Dialog",0,6));
                        b.add(spacer);

                        j = new JLabel("ECJ's homepage is " + Version.authorURL);
                        j.setFont(small);
                        b.add(j);

                        j = new JLabel("For help, send mail to " + Version.authorEmail0 + "@" + 
                            Version.authorEmail1);
                        j.setFont(small);
                        b.add(j);

                        j = new JLabel("     " + Version.authorEmail2);
                        j.setFont(small);
                        b.add(j);

                        spacer.setFont(new Font("Dialog",0,6));
                        b.add(spacer);

                        j = new JLabel("Version " + Version.version + " released on " + Version.date + ".");
                        j.setFont(small);
                        b.add(j);

                        String javaVersion = System.getProperties().getProperty("java.version");
                        j = new JLabel("Current Java: " + javaVersion);
                        j.setFont(small);
                        b.add(j);
                        
                        j = new JLabel("Minimum Java: " + Version.minimumJavaVersion);
                        j.setFont(small);
                        b.add(j);
                                            
                        aboutFrame.pack();
                        }
                        
                    // if not on screen right now, move to center of screen
                    if (!aboutFrame.isVisible())
                        
                        {
                        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                        d.width -= aboutFrame.getWidth();
                        d.height -= aboutFrame.getHeight();
                        d.width /= 2;
                        d.height /= 2;
                        if (d.width < 0) d.width = 0;
                        if (d.height < 0) d.height = 0;
                        aboutFrame.setLocation(d.width,d.height);
                        }
                    
                    // show it!
                    aboutFrame.setVisible(true);
                    }
                });
            }
        return aboutMenuItem;
        }
    /**
     * @param f
     */
    void loadParameters(File f) 
        {
        try 
            {
            parameters = new ParameterDatabase(f,clArgs);
            }
        catch (FileNotFoundException ex) 
            {
            Output.initialError(
                "A File Not Found Exception was generated upon " +
                "reading the parameter file \"" + f.getPath() + 
                "\".\nHere it is:\n" + ex);
            }
        catch (IOException ex) 
            {
            Output.initialError(
                "An IO Exception was generated upon reading the " +
                "parameter file \"" + f.getPath() + 
                "\".\nHere it is:\n" + ex);
            }
        
        if (parameters == null) 
            {
            Output.initialError("No parameter file was loaded");
            } else 
            {
            paramPanel.loadParameters();
            conPanel.loadParameters();
            }
        }
    
    void restoreFromCheckpoint(File checkpoint) 
        {
        try 
            {
            state=Checkpoint.restoreFromCheckpoint(checkpoint.getCanonicalPath());
            parameters = state.parameters;
            paramPanel.loadParameters();
            conPanel.loadParameters();
            paused = true;
            setStep(false);
            spawnPlayThread(true);
            stopButton.setEnabled(true);
            }
        catch(OptionalDataException e) 
            {
            Output.initialError(
                "A ClassNotFoundException was generated upon" +
                "starting up from a checkpoint." +
                "\nHere it is:\n" + e); 
            }
        catch(ClassNotFoundException e) 
            {
            Output.initialError(
                "A ClassNotFoundException was generated upon" +
                "starting up from a checkpoint." +
                "\nHere it is:\n" + e); 
            }
        catch (IOException e) 
            { 
            Output.initialError(
                "An IO Exception was generated upon" +
                "starting up, probably in setting up a log" +
                "\nHere it is:\n" + e); 
            }
        }
    
    boolean threadIsToStop;
    
    void tellThreadToStop() 
        {
        threadIsToStop = true;
        }
    
    void setStep(boolean step) 
        {
        _step = step;
        }
    
    boolean isThreadToStop() 
        {
        return threadIsToStop;
        }
    
    boolean _step = false;
    
    boolean getStep() 
        {
        return _step;
        }
    
    void setPaused(boolean paused) 
        {
        this.paused = paused;
        }
    
    boolean isPaused() 
        {
        return paused;
        }
    
    void spawnPlayThread(final boolean rfc) 
        {
        threadIsToStop = false;
        
        Runnable run = new Runnable() 
            {
            Vector listeners = new Vector();
            boolean restoreFromCheckpoint = rfc;
            
            void addListener(EvolutionStateListener l) 
                {
                listeners.add(l);
                }
            

            
            void firePostEvolutionStep() 
                {
                EvolutionStateEvent evt = new EvolutionStateEvent(this);
                Iterator it = listeners.iterator();
                while (it.hasNext()) 
                    {
                    EvolutionStateListener l = (EvolutionStateListener)it.next();
                    l.postEvolution(evt);
                    }
                }
            
            void restoreFromCheckpoint() 
                {
                state.startFromCheckpoint();
                statisticsPane.removeAll();
                setupChartPanes();
                setupInspectionPanes();
                }
            
            /**
             * @throws BadParameterException
             * @throws ParamClassLoadException
             */
            void initializeEvolutionState()
                throws BadParameterException, ParamClassLoadException 
                {
                listeners.removeAllElements();
                Output output = initializeOutput();
                
                // 2. set up thread values
                /*
                  int breedthreads = parameters.getInt(
                  new Parameter(Evolve.P_BREEDTHREADS),null,1);
                  if (breedthreads < 1)
                  Output.initialError("Number of breeding threads should be an integer >0.",
                  new Parameter(Evolve.P_BREEDTHREADS));
                
                  int evalthreads = parameters.getInt(
                  new Parameter(Evolve.P_EVALTHREADS),null,1);
                  if (evalthreads < 1)
                  Output.initialError("Number of eval threads should be an integer >0.",
                  new Parameter(Evolve.P_EVALTHREADS));
                */
                
                int breedthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_BREEDTHREADS));
                int evalthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_EVALTHREADS));
                boolean auto = (Evolve.V_THREADS_AUTO.equalsIgnoreCase(parameters.getString(new Parameter(Evolve.P_BREEDTHREADS),null)) ||
                    Evolve.V_THREADS_AUTO.equalsIgnoreCase(parameters.getString(new Parameter(Evolve.P_EVALTHREADS),null)));  // at least one thread is automatic.  Seeds may need to be dynamic.

                // 3. create the Mersenne Twister random number generators,
                // one per thread
                MersenneTwisterFast[] random = new MersenneTwisterFast[breedthreads > evalthreads ? 
                    breedthreads : evalthreads];
                int[] seeds = new int[breedthreads > evalthreads ? 
                    breedthreads : evalthreads];
                
                String seed_message = "Seed: ";
                for (int x=0;x<random.length;x++)
                    
                    {
                    seeds[x] = conPanel.getSeed(currentJob,x);
                    seed_message = seed_message + seeds[x] + " ";
                    }
                
                for (int x=0;x<random.length;x++)
                    
                    {
                    for (int y=x+1;y<random.length;y++)
                        if (seeds[x]==seeds[y])
                            
                            {
                            Output.initialError(Evolve.P_SEED+"."+x+" ("+seeds[x]+") and "+Evolve.P_SEED+"."+y+" ("+seeds[y]+") ought not be the same seed."); 
                            }
                    random[x] = Evolve.primeGenerator(new MersenneTwisterFast(seeds[x]));   // we prime the generator to be more sure of randomness.
                    }
                
                state = (EvolutionState)parameters.getInstanceForParameter(
                    new Parameter(Evolve.P_STATE),null,EvolutionState.class);
                
                state.parameters = parameters;
                state.random = random;
                state.output = output;
                String jobFilePrefix = Console.this.conPanel.getJobFilePrefix();
                if (Console.this.conPanel.getNumJobs() > 1) 
                    {
                    if (jobFilePrefix == null || jobFilePrefix.length()<1) 
                        {
                        jobFilePrefix = "job";
                        }
                    jobFilePrefix = jobFilePrefix+"."+Console.this.currentJob+".";
                    state.output.setFilePrefix(jobFilePrefix);
                    }
                
                state.evalthreads = evalthreads;
                state.breedthreads = breedthreads;
                
                output.systemMessage("Threads:  breed/" + breedthreads + " eval/" + evalthreads);
                output.systemMessage(seed_message);
                
                state.startFresh();

                if (Console.this.conPanel.getNumJobs() > 0) 
                    {
                    state.checkpointPrefix = jobFilePrefix+state.checkpointPrefix;
                    }

                if (currentJob == 0) 
                    {
                    statisticsPane.removeAll();
                    }
                
                setupChartPanes();
                setupInspectionPanes();
                }
            
            /**
             * @throws NumberFormatException
             * @throws BadParameterException
             */
            void setupInspectionPanes()
                throws NumberFormatException, BadParameterException 
                {
                inspectionPane.removeAll();
                // Setup the Evolution State inspection pane
                JScrollPane stateInspectionPane = new JScrollPane();
                JTree stateInspectionTree = new JTree(
                    new ReflectedObject(Console.this.state));
                stateInspectionPane.setViewportView(stateInspectionTree);
                inspectionPane.add("Evolution State", stateInspectionPane);
                
                // Setup the subpopulation inspection panes
                Parameter p_subPops = new Parameter("pop.subpops");
                int numSubPops = parameters.getInt(p_subPops,null);
                for (int subPop = 0; subPop < numSubPops; ++subPop) 
                    {
                    SubpopulationPanel subPopPane = new SubpopulationPanel(Console.this, subPop);
                    subPopPane.setup(Console.this.state,p_subPops.push(""+subPop));
                    inspectionPane.add("SubPop "+subPop, subPopPane);
                    addListener(subPopPane);
                    }
                }

            /**
             * @throws BadParameterException
             */
            void setupChartPanes()
                throws BadParameterException 
                {
                // Set up statistics charts (if any)
                StatisticsChartPane statPane = new StatisticsChartPane();
                statPane.setup(state, new Parameter("stat"));
                if (statPane.numCharts > 0)
                    statisticsPane.addTab("Job "+currentJob, statPane);
                }

            public void run() 
                {
                
                try 
                    {
                    while (currentJob < conPanel.getNumJobs()) 
                        {
                        if (!restoreFromCheckpoint)
                            initializeEvolutionState();
                        else
                            restoreFromCheckpoint();
                        state.output.message("\nJob "+currentJob);
                        
                        result = EvolutionState.R_NOTDONE;
                        while (result == EvolutionState.R_NOTDONE &&
                            !Thread.currentThread().isInterrupted() &&
                            !isThreadToStop()) 
                            {
                            
                            try 
                                {
                                synchronized (playThread) 
                                    {
                                    while (isPaused() && ! getStep()) 
                                        {
                                        playThread.wait();
                                        }
                                    }
                                }
                            catch (InterruptedException e) 
                                {
                                // This can happen if the play thread is stopped 
                                // while paused
                                }
                            
                            if (!Thread.currentThread().isInterrupted() &&
                                !isThreadToStop()) 
                                {
                                result = state.evolve();
                                firePostEvolutionStep();
                                Console.this.getStatusField().setText("Job: "+currentJob+" Generation: "+state.generation);
                                setStep(false);
                                }
                            }
                        
                        /*
                         * If the play thread has been interrupted before the experiment
                         * has completed, consider the experiment a failure.
                         */
                        if (result == EvolutionState.R_NOTDONE)
                            result = EvolutionState.R_FAILURE;
                        
                        if (state != null && result != EvolutionState.R_NOTDONE) 
                            {
                            state.finish(result);
                            }
                        
                        currentJob++;
                        }
                    }
                catch (Exception e) // just in case there's a RuntimeException thrown
                    {
                    System.err.println("Exception when running job:\n\t");
                    e.printStackTrace();
                    }
                
                conPanel.enableControls();
                finishAndCleanup();
                }
            };
        
        playThread = new Thread(run);
        playThread.start();
        }
    
    /**
     * @return
     * @throws BadParameterException
     */
    Output initializeOutput()
        throws BadParameterException 
        {
        // 1. create the output
        //boolean store = parameters.getBoolean(new Parameter(Evolve.P_STORE),null,false);
        
        Output output = new Output(true);
        //output.setFlush(
        //    parameters.getBoolean(new Parameter(Evolve.P_FLUSH),null,false));
        
        
        // stdout is always log #0.  stderr is always log #1.
        // stderr accepts announcements, and both are fully verbose 
        // by default.
        output.addLog(ec.util.Log.D_STDOUT,false);
        output.addLog(ec.util.Log.D_STDERR,true);
        output.systemMessage(Version.message());
        return output;
        }
    
    /**
     * Pauses the background play thread.
     *
     */
    void pausePlayThread() 
        {
        setPaused(true);
        }
    
    void resumePlayThread() 
        {
        synchronized (playThread) 
            {
            setPaused(false);
            playThread.notify();
            }
        }
    
    /**
     * 
     */
    void killPlayThread() 
        {
        tellThreadToStop();
        
        try 
            {
            if (playThread != null) 
                {
                while (playThread.isAlive()) 
                    {
                    try 
                        {
                        playThread.interrupt();
                        } 
                    // Ignore security exceptions resulting from
                    // attempting to interrupt a thread.
                    // TODO Explain this better.
                    catch (SecurityException ex) { }
                    playThread.join(50);
                    }
                
                playThread = null;
                }
            }
        catch (InterruptedException ex) 
            {
            System.out.println("Interrupted while killing the play thread.  Shouldn't happen.");
            }
        }
    
    /**
     * 
     */
    void finishAndCleanup() 
        {
        synchronized(cleanupLock) 
            {
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stepButton.setEnabled(true);
            playButton.setEnabled(true);
            paused = false;
            playing = false;
            _step = false;
            currentJob = 0;
            }
        }
    
    int result;
    JMenuItem loadCheckpointMenuItem = null;
    JTabbedPane statisticsPane = null;
    JTabbedPane inspectionPane = null;
    JPanel statusPane = null;
    JTextField statusField = null;
    }  //  @jve:decl-index=0:visual-constraint="21,10"
