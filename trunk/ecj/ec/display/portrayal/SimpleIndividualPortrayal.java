/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 14, 2005 7:38:51 PM
 * 
 * By: spaus
 */
package ec.display.portrayal;

import java.awt.BorderLayout;
import java.io.CharArrayWriter;
import java.io.IOException;

import javax.swing.JTextPane;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Log;
import ec.util.LogRestarter;
import ec.util.Output;
import ec.util.Parameter;
/**
 * This portrayal uses a text pane to display the results of 
 * <code>printIndividualForHumans()</code>.  It is the default portrayal.
 * 
 * @author spaus
 */
public class SimpleIndividualPortrayal
    extends IndividualPortrayal
    {
    
    private static final LogRestarter restarter = new LogRestarter()
        {
        public Log reopen(Log l)
            throws IOException
            {
            return null;
            }
        
        public Log restart(Log l)
            throws IOException
            {
            return null;
            }
        };
    
    final JTextPane textPane;
    private CharArrayWriter printIndividualWriter;
    
    public SimpleIndividualPortrayal()
        {
        super(new BorderLayout());
        textPane = new JTextPane();
        textPane.setEditable(false);
        this.add(textPane,BorderLayout.CENTER);
        printIndividualWriter = new CharArrayWriter();
        }
    
    public void portrayIndividual(EvolutionState state, Individual individual)
        {
        int printIndividualLog = state.output.addLog(printIndividualWriter,restarter,0,false,false);
        
        individual.printIndividualForHumans(state,printIndividualLog,Output.V_NO_GENERAL);
        textPane.setText(printIndividualWriter.toString());
        textPane.setCaretPosition(0);
        state.output.removeLog(printIndividualLog);
        printIndividualWriter.reset();
        }
    
    public void setup(EvolutionState state, Parameter base)
        {
        }
    }
