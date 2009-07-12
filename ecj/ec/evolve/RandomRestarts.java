package ec.evolve;

import ec.*;
import ec.util.*;
import java.io.*;

/**
 * A special Statistics class which performs random restarts on the population,
 * effectively reininitializing the population and starting over again.
 * RandomRestarts has two ways of determining when to perform a restart.  If
 * the restart type is "fixed", then the restart will occur precisely when
 * the generation is a multiple of restart-upper-bound, minus one.  (That's
 * hardly random, of course).  If the restart type is "random", then at the
 * beginning of the run, and after every restart, a new restart is chosen 
 * randomly from one to restart-upper-bound.
 *
 * <p>This class is compatible with populations which load from files -- it
 * temporarily disables the load-from-file feature when telling the population
 * to populate itself again, forcing the population to do so by creating random
 * individuals.
 *
 * @author James O'Beirne
  
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>restart-type</tt><br>
 <font size=-1>random or fixed</font></td>
 <td valign=top>Either initiates clock at a random value or a fixed one.</td></tr>
 <tr><td valign=top><i>base</i>.<tt>restart-upper-bound</tt><br>
 <font size=-1>1 &lt; int &lt; \inf</font></td>
 <td valign=top>Maximum time clock can initiate with.</td></tr>
 </table>
*/
 
public class RandomRestarts extends Statistics
    {
    /** Two options available here: "fixed" and "random"; "fixed"
     *  will initate the restart timer at the value specified for
     *  <i>restart-upper-bound</i>, "random" will initiate the restart
     *  timer somewhere below the value specified for 
     *  <i>restart-upper-bound</i> */
    public static final String P_RESTART_TYPE               = "restart-type";

    /** This is the highest value at which the "ticking" 
     *  restart clock can initiate at.  */
    public static final String P_RESTART_UPPERBOUND = "restart-upper-bound";

    int             countdown;              // what we'll use for the "ticking" clock
    int             upperbound;             // highest possible value on the clock
    String  restartType;    // are we doing random or fixed?


    /** Gets the clock ticking. */
    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );

        restartType =   state.parameters.getString( 
            base.push(P_RESTART_TYPE), 
            null 
            );
        upperbound      =       state.parameters.getInt( 
            base.push(P_RESTART_UPPERBOUND), 
            null,
            1
            );

        if( upperbound < 1 )
            state.output.fatal( 
                "Parameter either not found or invalid (<1).", 
                base.push(P_RESTART_UPPERBOUND)
                );
        if( !restartType.equals( "random" ) && !restartType.equals( "fixed" ) )
            state.output.fatal( 
                "Parameter must be either 'fixed' or 'random'.", 
                base.push(P_RESTART_TYPE) 
                );

        // start counting down
        this.resetClock( state );
        }

    /**
     * Checks the clock; if it's time to restart, we repopulate the population. 
     * Afterwards, we reset the clock.
     *
     * If it's not time yet, the clock goes tick.
     */
    public void preEvaluationStatistics( final EvolutionState state )
        {
        Subpopulation   currentSubp;
        File                    tempFile;
        // time to restart!
        if( countdown == 0 )
            {
            System.out.println( "Restarting the population!" );
            // for each subpopulation
            for( int subp = 0; subp < state.population.subpops.length; subp++ )
                {
                currentSubp = state.population.subpops[subp];
                tempFile = currentSubp.loadInds;
                // disable loadInds so we generate candidates randomly
                currentSubp.loadInds = null;
                currentSubp.populate( state, 0 );
                currentSubp.loadInds = tempFile;
                }
            this.resetClock( state );
            }
        else
            countdown--;
        }

    /** Method used internally to avoid code duplication. */
    private void resetClock( final EvolutionState state )
        {
        if(restartType.equals( "fixed" ))
            countdown = upperbound;
        else
            // might need to fix random index to suppoer multithreading
            countdown = state.random[0].nextInt( upperbound + 1 );
        }
    }

