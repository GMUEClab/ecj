/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.test;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.simple.SimpleProblemForm;
import ec.util.Output;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Eric O. Scott
 */
public class TestStatistics extends Statistics {
    public Individual[] getBestSoFar() { return best_of_run; }
    public int[] getGenerationOfBestSoFar() { return generation_of_best_of_run; }

    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";
    
    public static final String P_DO_FINAL = "do-final";
    public static final String P_DO_GENERATION = "do-generation";
    public static final String P_DO_MESSAGE = "do-message";
    public static final String P_DO_DESCRIPTION = "do-description";
    public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";
    
    public static final String P_STATISTICS_ROW_PREFIX = "row-prefix";

    /** The Statistics' log */
    public int statisticslog = 0;  // stdout
    public String rowPrefix;

    /** The best individual we've found so far */
    public Individual[] best_of_run = null;
    public int[] generation_of_best_of_run = null;
        
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
        rowPrefix = state.parameters.getStringWithDefault(base.push(P_STATISTICS_ROW_PREFIX), null, "");

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
        best_of_run = new Individual[state.population.subpops.size()];
        generation_of_best_of_run = new int[state.population.subpops.size()];
        }

    /** Logs the best individual of the generation. */
    boolean warned = false;
    public void postEvaluationStatistics(final EvolutionState state)
        {
        super.postEvaluationStatistics(state);
        
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.size()];  // quiets compiler complaints
        for(int x = 0; x< state.population.subpops.size(); x++)
            {
            best_i[x] = state.population.subpops.get(x).individuals.get(0);
            for(int y = 1; y< state.population.subpops.get(x).individuals.size(); y++)
                {
                if (state.population.subpops.get(x).individuals.get(y) == null)
                    {
                    if (!warned)
                        {
                        state.output.warnOnce("Null individuals found in subpopulation");
                        warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                        }
                    }
                else if (best_i[x] == null || state.population.subpops.get(x).individuals.get(y).fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops.get(x).individuals.get(y);
                if (best_i[x] == null)
                    {
                    if (!warned)
                        {
                        state.output.warnOnce("Null individuals found in subpopulation");
                        warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                        }
                    }
                }
        
            // now test to see if it's the new best_of_run
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                {
                if (best_of_run[x]!=null && best_i[x].fitness.fitness() != best_of_run[x].fitness.fitness())
                    { // Update the generation the best individual was found at, but only iff the new best is strictly better than the old best
                    generation_of_best_of_run[x] = state.generation;
                    }
                best_of_run[x] = (Individual)(best_i[x].clone());
                }
            }
        
        // print the best-of-generation individual
        if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
        if (doGeneration) state.output.println("Best Individual:",statisticslog);
        for(int x = 0; x< state.population.subpops.size(); x++)
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
        if (!doFinal)
             return;
        for (int i = 0; i < state.population.subpops.size(); i++)
            System.out.println(String.format("%s, %d, %s, %s", rowPrefix, i, best_of_run[i].fitness.fitness(), generation_of_best_of_run[i]));
        }
}
