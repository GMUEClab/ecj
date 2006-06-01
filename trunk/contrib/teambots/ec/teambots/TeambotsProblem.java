/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.teambots;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import ec.*;
import ec.util.*;
import EDU.gatech.cc.is.abstractrobot.*;
import java.awt.*;

/* 
 * TeambotsProblem.java
 *
 * Created: Mon Mar 12 16:52:30 2001
 * By: Liviu Panait
 */

/**
 * TeambotsProblem.java
 *
 * This is a problem for evolving control systems with ECJ for Teambots for multiple robots. In the
 * simulation there can also be other robots that are not evolved.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>input-file</tt><br>
 <font size=-1>string</font></td>
 <td valign=top>(name of the Teambots configuration file)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>graphics</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 <td valign=top>(whether evaluations have graphical display or not (no graphics => greater speed)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>window-width</tt><br>
 <font size=-1>int &gt;= 0</td>
 <td valign=top>(wodth of window for graphical display)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>window-height</tt><br>
 <font size=-1>int &gt;= 0</td>
 <td valign=top>(height of window for graphical display)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>num-bots</tt><br>
 <font size=-1>int &gt;= 1</td>
 <td valign=top>(number of robots to evolve behaviors for)</td></tr>
 </table>
 * 
 * @author Liviu Panait
 * @version 1.0
 */

public abstract class TeambotsProblem extends Problem implements SimpleProblemForm
    {

    /** the width of the window of the simulation */
    public static final String P_WINDOWWIDTH = "window-width";
    /** the height of the window of the simulation */
    public static final String P_WINDOWHEIGHT = "window-height";
    int width, height;

    /** the number of control systems to be evolved */
    public static final String P_NUMBOTS = "num-bots";
    int numBots;

    /** how many robots are there to be evolved */
    public final int getNumberEvolvableRobots()
        {
        return numBots;
        }

    /** The name of the input file for the simulator */
    public static final String P_INPUTFILE = "input-file";

    /** Whether the simulation will be displayed or not */
    public static final String P_GRAPHICS = "graphics";

    /** the simulator */
    public ECSimulationCanvas simulator;

    /** whether the simulation will have graphics displaying or not */
    public boolean graphics;

    // the input file for the simulator
    private String inputFileName;

    protected Frame simulatorFrame = null;

    public void setup(final EvolutionState state, final Parameter base)
        {

        super.setup( state, base );

        inputFileName = state.parameters.getStringWithDefault( base.push( P_INPUTFILE ), null, "" );
        if( inputFileName == "" )
            {
            state.output.fatal( "Error reading parameter.", base.push( P_INPUTFILE ) );
            }

        numBots = state.parameters.getInt( base.push( P_NUMBOTS ), null, 1 );
        if( numBots < 1 )
            {
            state.output.fatal( "Parameter not existent or has value smaller than 1.",
                                base.push( P_NUMBOTS ) );
            }

        graphics = state.parameters.getBoolean( base.push( P_GRAPHICS ), null, false );

        if( !graphics )
            {
            // create a simulator without graphics
            simulator = new ECSimulationCanvas(null, 0, 0, inputFileName);
            }
        else
            {
            width = state.parameters.getInt( base.push( P_WINDOWWIDTH ), null, 0 );
            if( width < 0 )
                width = 500;
            height = state.parameters.getInt( base.push( P_WINDOWHEIGHT ), null, 0 );
            if( height < 0 )
                height = 500;

            simulatorFrame = new Frame();
            simulatorFrame.setVisible( false );
            simulatorFrame.setSize( width, height );
            simulator = new ECSimulationCanvas( simulatorFrame, width, height, inputFileName );
            simulatorFrame.add( "North", simulator );
            simulatorFrame.pack();
            simulatorFrame.setResizable(false);
            }

        }

    /** Evaluate the individual. */
    public void evaluate(final EvolutionState state, 
                         final Individual ind, 
                         final int threadnum)
        {

        // reset the random seed of the simulator
        simulator.seed = 0;
        simulator.reset();

        for( int i = 0 ; i < numBots ; i++ )
            {
            // create a control system
            EvolvedControlSystem ecs = getControlSystem( state, ind, threadnum, i );

            // set the control systems on the first robot that needs a control system.
            // this means that the robots for which the control systems are evolved
            // should be defined first in the input file, then there should be
            // other programmed robots.
            simulator.setControlSystem( i, (ControlSystemS)ecs );
            }

        // set the graphics (if needed)
        if( graphics )
            simulatorFrame.setVisible( true );

        if( simulator.descriptionLoaded() )
            {

            // start the simulator
            simulator.start();

            // wait for the simulator to end
            try
                {
                simulator.getThread().join();
                }
            catch( InterruptedException e )
                {
                }

            }

        // if graphics, hide the frame
        if( graphics )
            {
            simulatorFrame.setVisible( false );
            }

        }

    /** Get the control system for the nth robot. */
    public abstract EvolvedControlSystem getControlSystem( final EvolutionState state, final Individual ind,
                                                           final int threadnum, final int whichBot );

    }
