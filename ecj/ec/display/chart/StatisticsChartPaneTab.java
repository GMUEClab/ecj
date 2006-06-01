/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 17, 2005 12:29:01 PM
 * 
 * By: spaus
 */
package ec.display.chart;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import ec.display.StatisticsChartPane;
/**
 * @author spaus
 */
public class StatisticsChartPaneTab
    extends JPanel
    {
    
    private JPanel jPanel = null;
    private JButton printButton = null;
    private JButton closeButton = null;
    private final ChartPanel chartPane;
    /**
     * 
     */
    public StatisticsChartPaneTab(ChartPanel chartPane)
        {
        super();
        this.chartPane = chartPane;
        initialize();
        this.add(chartPane,BorderLayout.CENTER);
        }
    
    /**
     * @param isDoubleBuffered
     */
    public StatisticsChartPaneTab(ChartPanel chartPane, boolean isDoubleBuffered)
        {
        super(isDoubleBuffered);
        this.chartPane = chartPane;
        initialize();
        this.add(chartPane,BorderLayout.CENTER);
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
        this.add(getJPanel(), java.awt.BorderLayout.SOUTH);
        }
    
    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel       
     */    
    private JPanel getJPanel()
        {
        if (jPanel == null)
            {
            jPanel = new JPanel();
            jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
            jPanel.add(Box.createHorizontalGlue());
            jPanel.add(getPrintButton(), null);
            jPanel.add(getCloseButton(), null);
            }
        return jPanel;
        }
    
    /**
     * This method initializes jButton  
     *  
     * @return javax.swing.JButton      
     */    
    private JButton getPrintButton()
        {
        if (printButton == null)
            {
            printButton = new JButton();
            printButton.setText("Export to PDF...");
            final JFreeChart chart = chartPane.getChart(); 
            printButton.addActionListener(new java.awt.event.ActionListener()
                { 
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {    
                    try
                       
                        {
                        int width = chartPane.getWidth();
                        int height = chartPane.getHeight();
                        
                        FileDialog fileDialog = new FileDialog(new Frame(),"Export...",FileDialog.SAVE);
                        fileDialog.setDirectory(System.getProperty("user.dir"));
                        fileDialog.setFile("*.pdf");
                        fileDialog.setVisible(true);
                        String fileName = fileDialog.getFile();
                        if ( fileName != null )
                           
                            {
                            if (!fileName.endsWith(".pdf"))
                                {
                                fileName = fileName+".pdf";
                                }
                            File f = new File(fileDialog.getDirectory(), fileName);
                            Document document = new Document(new com.lowagie.text.Rectangle(width,height));
                            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(f));
                            document.addAuthor("ECJ Console");
                            document.open();
                            PdfContentByte cb = writer.getDirectContent();
                            PdfTemplate tp = cb.createTemplate(width, height);
                            Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
                            Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
                            chart.draw(g2, rectangle2D);
                            g2.dispose();
                            cb.addTemplate(tp, 0, 0);
                            document.close();
                            }
                        }
                    catch( Exception ex )
                       
                        {
                        ex.printStackTrace();
                        }
                    }
                });
            }
        return printButton;
        }
    
    /**
     * This method initializes jButton1 
     *  
     * @return javax.swing.JButton      
     */    
    private JButton getCloseButton()
        {
        if (closeButton == null)
            {
            closeButton = new JButton();
            closeButton.setText("Close");
            final StatisticsChartPaneTab pane = this;
            closeButton.addActionListener(new java.awt.event.ActionListener()
                { 
                public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    StatisticsChartPane parent = (StatisticsChartPane)pane.getParent();
                    parent.removeTabAt(parent.indexOfComponent(pane));
                    }
                });
            }
        return closeButton;
        }
    }
