package ec.evolve;

import ec.*;
import ec.steadystate.*;
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
 <tr><td valign=top><i>base</i>.<tt>start</tt><br>
 <font size=-1>int &gt;= 0 (default = 1)</font></td>
 <td valign=top>The first generation where the clock may be started.</td></tr>
 <tr><td valign=top><i>base</i>.<tt>restart-type</tt><br>
 <font size=-1>random (default) or fixed</font></td>
 <td valign=top>Either initiates clock at a random value or a fixed one.</td></tr>
 <tr><td valign=top><i>base</i>.<tt>restart-upper-bound</tt><br>
 <font size=-1>1 &lt; int &lt; \inf</font></td>
 <td valign=top>Maximum time clock can initiate with.</td></tr>
 </table>
*/
 
public class RandomRestarts extends Statistics
    {
    public static final String P_RESTART_TYPE = "restart-type";
    public static final String P_RESTART_UPPERBOUND = "restart-upper-bound";
    public static final String P_START = "start";

    public int countdown;              // what we'll use for the "ticking" clock
    public int upperbound;             // highest possible value on the clock
    public int start;
   
    String restartType;    // are we doing random or fixed?

    /** Gets the clock ticking. */
    public void setup( final EvolutionState state, final Parameter base )
        {
        super.setup( state, base );

        restartType = state.parameters.getString(base.push(P_RESTART_TYPE),  null);
        
        if (restartType == null)
            restartType = "random";
        
        upperbound = state.parameters.getInt( base.push(P_RESTART_UPPERBOUND), null, 1);

        if (state.parameters.exists(base.push(P_START), null))
            {
            start = state.parameters.getInt(base.push(P_START), null, 0);
            if (start < 0) 
                state.output.fatal("Start value must be >= 0", base.push(P_START));
            }
        else start = 1;

        if( upperbound < 1 )
            state.output.fatal("Parameter either not found or invalid (<1).", base.push(P_RESTART_UPPERBOUND));
                        
        if( !restartType.equals( "random" ) && !restartType.equals( "fixed" ) )
            state.output.fatal("Parameter must be either 'fixed' or 'random'.", base.push(P_RESTART_TYPE));
        }

    /**
     * Checks the clock; if it's time to restart, we repopulate the population. 
     * Afterwards, we reset the clock.
     *
     * If it's not time yet, the clock goes tick.
     */
    public void preEvaluationStatistics( final EvolutionState state )
        {
        super.preEvaluationStatistics(state);
        if (state.generation == start) resetClock(state); // first time only
        if (state.generation >= start) possiblyRestart(state);
        }

    void possiblyRestart(EvolutionState state)
        {
        countdown--;
        Subpopulation currentSubp;
        // time to restart!
        if( countdown == 0 )
            {
            state.output.message( "Restarting the population prior to evaluating generation " + state.generation );
            // for each subpopulation
            for( int subp = 0; subp < state.population.subpops.length; subp++ )
                {
                currentSubp = state.population.subpops[subp];
                boolean temp = currentSubp.loadInds;
                // disable loadInds so we generate candidates randomly
                currentSubp.loadInds = false;
                currentSubp.populate( state, 0 );
                currentSubp.loadInds = temp;
                }
            this.resetClock( state );
            }
        }

    void resetClock( final EvolutionState state )
        {
        if(restartType.equals( "fixed" ))
            countdown = upperbound;
        else
            // might need to fix random index to support multithreading
            countdown = state.random[0].nextInt( upperbound) + 1;
        }
    }

