/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.simple;
import ec.*;
import ec.steadystate.*;
import java.io.IOException;
import ec.util.*;
import java.io.File;

/* 
 * SimpleStatistics.java
 * 
 * Created: Tue Aug 10 21:10:48 1999
 * By: Sean Luke
 */

/**
 * A basic Statistics class suitable for simple problem applications.
 *
 * SimpleStatistics prints out the best individual, per subpopulation,
 * each generation.  At the end of a run, it also prints out the best
 * individual of the run.  SimpleStatistics outputs this data to a log
 * which may either be a provided file or stdout.  Compressed files will
 * be overridden on restart from checkpoint; uncompressed files will be 
 * appended on restart.
 *
 * <p>SimpleStatistics implements a simple version of steady-state statistics:
 * if it quits before a generation boundary,
 * it will include the best individual discovered, even if the individual was discovered
 * after the last boundary.  This is done by using individualsEvaluatedStatistics(...)
 * to update best-individual-of-generation in addition to doing it in
 * postEvaluationStatistics(...).

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class SimpleStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
    {
    public Individual[] getBestSoFar() { return best_of_run; }

    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";
    
    public static final String P_DO_FINAL = "do-final";
    public static final String P_DO_GENERATION = "do-generation";
    public static final String P_DO_MESSAGE = "do-message";
    public static final String P_DO_DESCRIPTION = "do-description";
    public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

    /** The Statistics' log */
    public int statisticslog = 0;  // stdout

    /** The best individual we've found so far */
    public Individual[] best_of_run = null;
        
    /** Should we compress the file? */
    public boolean compress;
    public boolean doFinal;
    public boolean doGeneration;
    public boolean doMessage;
    public boolean doDescription;
    public boolean doPerGenerationDescription;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        compress = state.parameters.getBoolean(base.push(P_COMPRESS),null,false);
                
        File statisticsFile = state.parameters.getFile(
            base.push(P_STATISTICS_FILE),null);

        doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL),null,true);
        doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION),null,true);
        doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE),null,true);
        doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION),null,true);
        doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION),null,false);

        if (silentFile)
            {
            statisticslog = Output.NO_LOGS;
            }
        else if (statisticsFile!=null)
            {
            try
                {
                statisticslog = state.output.addLog(statisticsFile, !compress, compress);
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
            }
        else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));
        }

    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
        
        // set up our best_of_run array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        best_of_run = new Individual[state.population.subpops.length];
        }

    /** Logs the best individual of the generation. */
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++)
            {
            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++)
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
        
            // now test to see if it's the new best_of_run
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());
            }
        
        // print the best-of-generation individual
        if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
        if (doGeneration) state.output.println("Best Individual:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++)
            {
            if (doGeneration) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doGeneration) best_i[x].printIndividualForHumans(state,statisticslog);
            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of generation" + 
                (best_i[x].evaluated ? " " : " (evaluated flag not set): ") +
                best_i[x].fitness.fitnessToStringForHumans());
                
            // describe the winner if there is a description
            if (doGeneration && doPerGenerationDescription) 
                {
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0, statisticslog);   
                }   
            }
        }

    /** Allows MultiObjectiveStatistics etc. to call super.super.finalStatistics(...) without
        calling super.finalStatistics(...) */
    protected void bypassFinalStatistics(EvolutionState state, int result)
        { super.finalStatistics(state, result); }

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);
        
        // for now we just print the best fitness 
        
        if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++ )
            {
            if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doFinal) best_of_run[x].printIndividualForHumans(state,statisticslog);
            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

            // finally describe the winner if there is a description
            if (doFinal && doDescription) 
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);      
            }
        }
    }
